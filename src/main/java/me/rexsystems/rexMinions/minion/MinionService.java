package me.rexsystems.rexMinions.minion;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.action.MinionActions;
import me.rexsystems.rexMinions.action.ToolWear;
import me.rexsystems.rexMinions.config.MinionType;
import me.rexsystems.rexMinions.logic.ActionBudget;
import me.rexsystems.rexMinions.logic.LevelTable;
import me.rexsystems.rexMinions.logic.LimitPermissions;
import me.rexsystems.rexMinions.logic.PickupOrder;
import me.rexsystems.rexMinions.storage.MinionDatabase;
import me.rexsystems.rexMinions.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MinionService {

    public enum PlaceResult {
        PLACED,
        LIMIT,
        DENIED_REGION,
        NO_SPACE,
        DISABLED,
        UNKNOWN_TYPE,
        NOT_LOADED
    }

    public enum PickupResult {
        GIVEN,
        DROPPED,
        FAILED
    }

    private final RexMinions plugin;
    private final MinionActions actions;
    private final Map<UUID, ActiveMinion> minions = new ConcurrentHashMap<>();
    private final Map<UUID, Long> useTick = new HashMap<>();
    private final Object placeLock = new Object();
    private volatile boolean running = true;

    public MinionService(RexMinions plugin) {
        this.plugin = plugin;
        this.actions = new MinionActions(plugin);
    }

    public void load() {
        for (MinionDatabase.StoredMinion row : plugin.database().loadAll()) {
            if (row.id() == null || minions.containsKey(row.id())) {
                continue;
            }
            ActiveMinion minion = new ActiveMinion(row);
            minions.put(minion.id(), minion);
            arm(minion);
        }
    }

    public void shutdown() {
        running = false;
        for (ActiveMinion minion : minions.values()) {
            minion.cancelTask();
            plugin.database().save(minion.snapshot());
        }
    }

    public Collection<ActiveMinion> all() {
        return List.copyOf(minions.values());
    }

    public ActiveMinion find(UUID id) {
        if (id == null) {
            return null;
        }
        ActiveMinion minion = minions.get(id);
        if (minion == null || minion.removed()) {
            return null;
        }
        return minion;
    }

    public ActiveMinion find(Entity entity) {
        String raw = plugin.keys().entityId(entity);
        if (raw == null) {
            return null;
        }
        try {
            return find(UUID.fromString(raw));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public int count(UUID owner) {
        int count = 0;
        for (ActiveMinion minion : minions.values()) {
            if (!minion.removed() && owner.equals(minion.owner())) {
                count++;
            }
        }
        return count;
    }

    public int limitOf(Player player) {
        List<String> nodes = new ArrayList<>();
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getValue()) {
                nodes.add(info.getPermission());
            }
        }
        return LimitPermissions.highest(nodes, plugin.config().defaultLimit());
    }

    public boolean canManage(Player player, ActiveMinion minion) {
        return player.getUniqueId().equals(minion.owner()) || player.hasPermission("rexminions.admin");
    }

    public synchronized boolean beginUse(UUID playerId, long tick) {
        Long previous = useTick.get(playerId);
        if (previous != null && previous == tick) {
            return false;
        }
        useTick.put(playerId, tick);
        return true;
    }

    public PlaceResult tryPlace(Player player, String typeId, int level, Location location) {
        if (!running || !plugin.config().enabled()) {
            return PlaceResult.DISABLED;
        }
        MinionType type = plugin.types().get(typeId);
        if (type == null || !type.enabled()) {
            return PlaceResult.UNKNOWN_TYPE;
        }
        if (location == null || location.getWorld() == null || !chunkLoaded(location)) {
            return PlaceResult.NOT_LOADED;
        }
        Block block = location.getBlock();
        if (!free(block)) {
            return PlaceResult.NO_SPACE;
        }
        if (!plugin.regions().allows(player.getUniqueId(), location)) {
            return PlaceResult.DENIED_REGION;
        }
        int safeLevel = Math.max(1, level);
        synchronized (placeLock) {
            if (count(player.getUniqueId()) >= limitOf(player)) {
                return PlaceResult.LIMIT;
            }
            UUID id = UUID.randomUUID();
            MinionDatabase.StoredMinion row = new MinionDatabase.StoredMinion(
                    id,
                    type.id(),
                    safeLevel,
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    player.getUniqueId(),
                    "",
                    "",
                    null,
                    null,
                    null,
                    null,
                    0,
                    0
            );
            ActiveMinion minion = new ActiveMinion(row);
            minions.put(id, minion);
            plugin.database().save(row);
            arm(minion);
            return PlaceResult.PLACED;
        }
    }

    public PickupResult pickup(Player player, ActiveMinion minion) {
        if (minion == null || minion.removed()) {
            return PickupResult.FAILED;
        }
        ItemStack item = plugin.items().create(minion.typeId(), minion.level());
        Location dropAt = minion.location();
        boolean room = hasRoom(player, item);
        boolean[] dropped = {false};
        boolean removed = PickupOrder.execute(
                () -> removeEntity(minion),
                () -> {
                    if ("give".equals(PickupOrder.handoff(room))) {
                        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                        for (ItemStack extra : leftover.values()) {
                            drop(dropAt, extra);
                            dropped[0] = true;
                        }
                    } else {
                        drop(dropAt, item);
                        dropped[0] = true;
                    }
                    giveOrDrop(player, dropAt, minion.tool());
                    for (ItemStack stored : minion.storage()) {
                        giveOrDrop(player, dropAt, stored);
                    }
                    return true;
                }
        );
        if (!removed) {
            return PickupResult.FAILED;
        }
        return dropped[0] ? PickupResult.DROPPED : PickupResult.GIVEN;
    }

    public boolean linkNearest(ActiveMinion minion) {
        Location origin = minion.location();
        if (origin == null || origin.getWorld() == null || !minion.chunkLoaded()) {
            return false;
        }
        int distance = plugin.config().linkDistance();
        World world = origin.getWorld();
        Block best = null;
        int bestScore = Integer.MAX_VALUE;
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();
        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                for (int dz = -distance; dz <= distance; dz++) {
                    int score = dx * dx + dy * dy + dz * dz;
                    if (score == 0 || score > distance * distance || score >= bestScore) {
                        continue;
                    }
                    int x = ox + dx;
                    int y = oy + dy;
                    int z = oz + dz;
                    if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                        continue;
                    }
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block;
                    try {
                        block = world.getBlockAt(x, y, z);
                    } catch (Throwable ignored) {
                        continue;
                    }
                    if (!me.rexsystems.rexMinions.action.Stores.linkable(block.getType())) {
                        continue;
                    }
                    best = block;
                    bestScore = score;
                }
            }
        }
        if (best == null) {
            return false;
        }
        minion.setLink(best.getLocation());
        plugin.database().save(minion.snapshot());
        return true;
    }

    public int collectBalance(Player player, ActiveMinion minion) {
        int whole = (int) Math.floor(minion.balance());
        if (whole <= 0) {
            return 0;
        }
        int amount = Math.min(64, whole);
        ItemStack payout = new ItemStack(plugin.config().currency(), amount);
        giveOrDrop(player, player.getLocation(), payout);
        minion.addBalance(-amount);
        plugin.database().save(minion.snapshot());
        return amount;
    }

    public void save(ActiveMinion minion) {
        if (minion != null && !minion.removed()) {
            plugin.database().save(minion.snapshot());
        }
    }

    public List<ActiveMinion> ownedBy(UUID owner) {
        List<ActiveMinion> list = new ArrayList<>();
        for (ActiveMinion minion : minions.values()) {
            if (!minion.removed() && owner.equals(minion.owner())) {
                list.add(minion);
            }
        }
        list.sort(Comparator.comparing(ActiveMinion::typeId).thenComparingInt(ActiveMinion::level));
        return list;
    }

    private void arm(ActiveMinion minion) {
        Location location = minion.location();
        if (location == null) {
            plugin.getLogger().warning("Minion " + minion.id() + " is in a missing world and was not scheduled.");
            return;
        }
        Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> ensureEntity(minion), 1L);
        schedule(minion);
    }

    private void schedule(ActiveMinion minion) {
        if (!running || minion.removed() || !plugin.isEnabled()) {
            return;
        }
        Location location = minion.location();
        if (location == null) {
            return;
        }
        long delay = period(minion);
        ScheduledTask task = Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduled -> tick(minion), delay);
        minion.bindTask(task);
    }

    private void tick(ActiveMinion minion) {
        if (!running || minion.removed() || !plugin.isEnabled()) {
            return;
        }
        if (!plugin.config().enabled() || !minion.chunkLoaded()) {
            schedule(minion);
            return;
        }
        ensureEntity(minion);
        MinionType type = plugin.types().get(minion.typeId());
        if (type == null || !type.enabled() || minion.paused()) {
            schedule(minion);
            return;
        }
        ActionBudget budget = new ActionBudget(plugin.config().actionsPerTick());
        try {
            actions.run(minion, type, budget);
        } catch (Throwable failure) {
            plugin.getLogger().warning("Minion " + minion.id() + " skipped a tick: " + failure.getMessage());
        }
        if (budget.used() > 0) {
            minion.setTool(ToolWear.apply(minion.tool(), plugin.config().useDurability()));
            plugin.database().save(minion.snapshot());
        }
        schedule(minion);
    }

    private long period(ActiveMinion minion) {
        MinionType type = plugin.types().get(minion.typeId());
        int speed = 100;
        if (type != null) {
            speed = type.levels().lookup(minion.level()).speedTicks();
        }
        ItemStack tool = minion.tool();
        int efficiency = tool == null ? 0 : tool.getEnchantmentLevel(Enchantment.EFFICIENCY);
        return LevelTable.period(speed, efficiency, plugin.config().efficiencyTicks(), plugin.config().minimumTicks());
    }

    private void ensureEntity(ActiveMinion minion) {
        if (!running || minion.removed() || !minion.chunkLoaded()) {
            return;
        }
        Location location = minion.location();
        if (location == null) {
            return;
        }
        if (findExisting(location, minion.id()) != null) {
            return;
        }
        spawn(minion, location);
    }

    private Entity findExisting(Location location, UUID id) {
        if (location == null || location.getWorld() == null || !chunkLoaded(location)) {
            return null;
        }
        World world = location.getWorld();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        String wanted = id.toString();
        for (Entity entity : world.getChunkAt(chunkX, chunkZ).getEntities()) {
            String raw = plugin.keys().entityId(entity);
            if (wanted.equals(raw)) {
                return entity;
            }
        }
        return null;
    }

    private void spawn(ActiveMinion minion, Location location) {
        MinionType type = plugin.types().get(minion.typeId());
        if (type == null || location.getWorld() == null) {
            return;
        }
        Component name = TextParser.parse("<white>" + TextParser.escapeValue(type.name()) + " <gray>" + minion.level());
        World world = location.getWorld();
        if ("marker".equals(plugin.config().entityMode())) {
            world.spawn(location, Marker.class, marker -> {
                marker.setPersistent(true);
                marker.setCustomNameVisible(plugin.config().showName());
                marker.customName(name);
                tag(marker, minion);
            });
            return;
        }
        world.spawn(location, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setVisible(true);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setSmall(plugin.config().smallArmorStand());
            stand.setMarker(false);
            stand.setPersistent(true);
            stand.setRemoveWhenFarAway(false);
            stand.setCollidable(false);
            stand.setCanPickupItems(false);
            stand.setCustomNameVisible(plugin.config().showName());
            stand.customName(name);
            EntityEquipment equipment = stand.getEquipment();
            if (equipment != null) {
                equipment.setHelmet(cosmetic(type.helmet()));
                equipment.setChestplate(cosmetic(type.chestplate()));
                equipment.setLeggings(cosmetic(type.leggings()));
                equipment.setBoots(cosmetic(type.boots()));
            }
            lock(stand, EquipmentSlot.HEAD);
            lock(stand, EquipmentSlot.CHEST);
            lock(stand, EquipmentSlot.LEGS);
            lock(stand, EquipmentSlot.FEET);
            lock(stand, EquipmentSlot.HAND);
            lock(stand, EquipmentSlot.OFF_HAND);
            tag(stand, minion);
        });
    }

    private void tag(Entity entity, ActiveMinion minion) {
        entity.getPersistentDataContainer().set(plugin.keys().minion, PersistentDataType.BYTE, (byte) 1);
        entity.getPersistentDataContainer().set(plugin.keys().id, PersistentDataType.STRING, minion.id().toString());
        entity.getPersistentDataContainer().set(plugin.keys().type, PersistentDataType.STRING, minion.typeId());
    }

    private static void lock(ArmorStand stand, EquipmentSlot slot) {
        stand.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
    }

    private static ItemStack cosmetic(Material material) {
        if (material == null || material.isAir()) {
            return null;
        }
        return new ItemStack(material);
    }

    private boolean removeEntity(ActiveMinion minion) {
        Location location = minion.location();
        Entity entity = findExisting(location, minion.id());
        if (entity == null) {
            return false;
        }
        if (entity instanceof ArmorStand stand && stand.getEquipment() != null) {
            stand.getEquipment().clear();
        }
        entity.remove();
        if (entity.isValid()) {
            return false;
        }
        minion.markRemoved();
        minions.remove(minion.id());
        plugin.database().delete(minion.id());
        return true;
    }

    private static boolean hasRoom(Player player, ItemStack stack) {
        int need = stack.getAmount();
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (ItemStack content : contents) {
            if (content == null || content.getType().isAir()) {
                return true;
            }
            if (!content.isSimilar(stack)) {
                continue;
            }
            need -= content.getMaxStackSize() - content.getAmount();
            if (need <= 0) {
                return true;
            }
        }
        return false;
    }

    private static void giveOrDrop(Player player, Location dropAt, ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack.clone());
        for (ItemStack extra : leftover.values()) {
            drop(dropAt, extra);
        }
    }

    private static void drop(Location location, ItemStack stack) {
        if (location == null || location.getWorld() == null || stack == null || stack.getType().isAir()) {
            return;
        }
        location.getWorld().dropItem(location, stack.clone());
    }

    private static boolean chunkLoaded(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private static boolean free(Block block) {
        Material type = block.getType();
        if (type == Material.LAVA || type == Material.FIRE || type == Material.SOUL_FIRE) {
            return false;
        }
        return type.isAir() || block.isReplaceable();
    }
}
