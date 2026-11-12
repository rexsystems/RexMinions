package me.rexsystems.rexMinions.listener;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.gui.MinionHolder;
import me.rexsystems.rexMinions.logic.ClickGate;
import me.rexsystems.rexMinions.minion.ActiveMinion;
import me.rexsystems.rexMinions.minion.MinionService;
import me.rexsystems.rexMinions.util.Names;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MinionListener implements Listener {

    private final RexMinions plugin;

    public MinionListener(RexMinions plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        boolean placing = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean minionItem = placing && plugin.keys().isMinionItem(event.getItem());
        if (!ClickGate.cancels(minionItem, false)) {
            return;
        }
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        if (!plugin.minions().beginUse(event.getPlayer().getUniqueId(), Bukkit.getCurrentTick())) {
            return;
        }
        place(event.getPlayer(), event.getItem(), event.getHand(), target(event));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntity(PlayerInteractEntityEvent event) {
        boolean target = plugin.keys().isMinionEntity(event.getRightClicked());
        ItemStack hand = event.getPlayer().getInventory().getItem(event.getHand());
        boolean minionItem = plugin.keys().isMinionItem(hand);
        if (!ClickGate.cancels(minionItem, target)) {
            return;
        }
        event.setCancelled(true);
        if (target) {
            openOrPickup(event.getPlayer(), event.getRightClicked(), event.getPlayer().isSneaking());
            return;
        }
        if (!plugin.minions().beginUse(event.getPlayer().getUniqueId(), Bukkit.getCurrentTick())) {
            return;
        }
        place(event.getPlayer(), hand, event.getHand(), inFront(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!ClickGate.cancels(false, plugin.keys().isMinionEntity(event.getEntity()))) {
            return;
        }
        event.setCancelled(true);
        if (event instanceof EntityDamageByEntityEvent by && by.getDamager() instanceof Player player) {
            openOrPickup(player, event.getEntity(), true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onManipulate(PlayerArmorStandManipulateEvent event) {
        if (!ClickGate.cancels(false, plugin.keys().isMinionEntity(event.getRightClicked()))) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!ClickGate.cancels(plugin.keys().isMinionItem(event.getItemInHand()), false)) {
            return;
        }
        event.setCancelled(true);
        if (!plugin.minions().beginUse(event.getPlayer().getUniqueId(), Bukkit.getCurrentTick())) {
            return;
        }
        Location location = event.getBlock().getLocation().add(0.5, 0, 0.5);
        location.setYaw(event.getPlayer().getLocation().getYaw() + 180f);
        place(event.getPlayer(), event.getItemInHand(), event.getHand(), location);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Integer looting = event.getEntity().getPersistentDataContainer().get(plugin.keys().looting, PersistentDataType.INTEGER);
        if (looting == null || looting <= 0) {
            return;
        }
        event.getEntity().getPersistentDataContainer().remove(plugin.keys().looting);
        for (ItemStack drop : event.getDrops()) {
            if (drop == null || drop.getMaxStackSize() <= 1) {
                continue;
            }
            int bonus = ThreadLocalRandom.current().nextInt(looting + 1);
            drop.setAmount(Math.min(drop.getMaxStackSize(), drop.getAmount() + bonus));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MinionHolder holder)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ActiveMinion minion = plugin.minions().find(holder.minionId());
        if (minion == null) {
            event.setCancelled(true);
            return;
        }
        if (holder.kind() == MinionHolder.Kind.STORAGE) {
            return;
        }
        if (event.getClick() == ClickType.DOUBLE_CLICK || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }
        int toolSlot = plugin.config().slot("tool", 11);
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory() != top) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }
        if (event.getSlot() == toolSlot) {
            return;
        }
        event.setCancelled(true);
        if (event.getSlot() == plugin.config().slot("link", 13)) {
            if (plugin.minions().linkNearest(minion)) {
                Location link = minion.link();
                plugin.message(player, "linked", Map.of(
                        "x", Integer.toString(link.getBlockX()),
                        "y", Integer.toString(link.getBlockY()),
                        "z", Integer.toString(link.getBlockZ())
                ));
                plugin.sound(player, "link");
            } else {
                plugin.message(player, "link-missing", Map.of(
                        "distance", Integer.toString(plugin.config().linkDistance())
                ));
                plugin.sound(player, "error");
            }
            return;
        }
        if (event.getSlot() == plugin.config().slot("storage", 15)) {
            plugin.menus().openStorage(player, minion);
            return;
        }
        if (event.getSlot() == plugin.config().slot("balance", 22)) {
            int paid = plugin.minions().collectBalance(player, minion);
            if (paid <= 0) {
                plugin.message(player, "balance-empty", Map.of());
                plugin.sound(player, "error");
            } else {
                plugin.message(player, "balance-paid", Map.of(
                        "amount", paid + " " + Names.enumName(plugin.config().currency().name())
                ));
            }
            player.closeInventory();
            return;
        }
        if (event.getSlot() == plugin.config().slot("pickup", 26)) {
            player.closeInventory();
            finishPickup(player, minion);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MinionHolder holder) || holder.kind() == MinionHolder.Kind.STORAGE) {
            return;
        }
        int toolSlot = plugin.config().slot("tool", 11);
        for (int slot : event.getRawSlots()) {
            if (slot < top.getSize() && slot != toolSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MinionHolder holder)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        ActiveMinion minion = plugin.minions().find(holder.minionId());
        if (minion == null) {
            return;
        }
        plugin.menus().capture(event.getInventory(), minion);
        if (!plugin.menus().switching(player.getUniqueId())) {
            minion.resume();
        }
        plugin.minions().save(minion);
    }

    private void openOrPickup(Player player, Entity entity, boolean pickup) {
        ActiveMinion minion = plugin.minions().find(entity);
        if (minion == null) {
            return;
        }
        if (!player.hasPermission("rexminions.use") && !player.hasPermission("rexminions.admin")) {
            plugin.message(player, "no-permission", Map.of());
            return;
        }
        if (!plugin.minions().canManage(player, minion)) {
            plugin.message(player, "not-owner", Map.of());
            plugin.sound(player, "error");
            return;
        }
        if (pickup) {
            finishPickup(player, minion);
            return;
        }
        plugin.menus().openMain(player, minion);
    }

    private void finishPickup(Player player, ActiveMinion minion) {
        String typeName = typeName(minion);
        MinionService.PickupResult result = plugin.minions().pickup(player, minion);
        if (result == MinionService.PickupResult.FAILED) {
            plugin.message(player, "cannot-place", Map.of());
            plugin.sound(player, "error");
            return;
        }
        plugin.message(player, "picked-up", Map.of("type", typeName));
        if (result == MinionService.PickupResult.DROPPED) {
            plugin.message(player, "dropped", Map.of());
        }
        plugin.sound(player, "pickup");
    }

    private void place(Player player, ItemStack stack, EquipmentSlot hand, Location location) {
        if (!player.hasPermission("rexminions.use")) {
            plugin.message(player, "no-permission", Map.of());
            plugin.sound(player, "error");
            return;
        }
        String typeId = plugin.keys().itemType(stack);
        int level = plugin.keys().itemLevel(stack);
        MinionService.PlaceResult result = plugin.minions().tryPlace(player, typeId, level, location);
        if (result != MinionService.PlaceResult.PLACED) {
            refuse(player, result);
            return;
        }
        consume(player, hand);
        plugin.message(player, "placed", Map.of(
                "type", typeName(typeId),
                "level", Integer.toString(level)
        ));
        plugin.sound(player, "place");
    }

    private void refuse(Player player, MinionService.PlaceResult result) {
        plugin.sound(player, "error");
        switch (result) {
            case LIMIT -> plugin.message(player, "limit", Map.of(
                    "count", Integer.toString(plugin.minions().count(player.getUniqueId())),
                    "limit", Integer.toString(plugin.minions().limitOf(player))
            ));
            case DENIED_REGION -> plugin.message(player, "worldguard", Map.of());
            case DISABLED -> plugin.message(player, "disabled", Map.of());
            case UNKNOWN_TYPE -> plugin.message(player, "unknown-type", Map.of("type", ""));
            default -> plugin.message(player, "cannot-place", Map.of());
        }
    }

    private static void consume(Player player, EquipmentSlot hand) {
        ItemStack stack = player.getInventory().getItem(hand);
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        if (stack.getAmount() <= 1) {
            player.getInventory().setItem(hand, null);
            return;
        }
        stack.setAmount(stack.getAmount() - 1);
    }

    private static Location target(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block relative = clicked.getRelative(event.getBlockFace());
            Location location = relative.getLocation().add(0.5, 0, 0.5);
            location.setYaw(player.getLocation().getYaw() + 180f);
            return location;
        }
        return inFront(player);
    }

    private static Location inFront(Player player) {
        Location eye = player.getEyeLocation();
        Location spot = eye.add(eye.getDirection().normalize().multiply(1.5));
        Location location = new Location(spot.getWorld(), spot.getBlockX() + 0.5, spot.getBlockY(), spot.getBlockZ() + 0.5);
        location.setYaw(player.getLocation().getYaw() + 180f);
        return location;
    }

    private String typeName(ActiveMinion minion) {
        return typeName(minion.typeId());
    }

    private String typeName(String typeId) {
        if (plugin.types().get(typeId) == null) {
            return typeId == null ? "" : typeId;
        }
        return plugin.types().get(typeId).name();
    }
}
