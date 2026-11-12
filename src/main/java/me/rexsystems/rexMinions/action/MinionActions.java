package me.rexsystems.rexMinions.action;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.config.MinionType;
import me.rexsystems.rexMinions.logic.ActionBudget;
import me.rexsystems.rexMinions.logic.LevelTable;
import me.rexsystems.rexMinions.minion.ActiveMinion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class MinionActions {

    private static final Material[] FISH = {
            Material.COD, Material.COD, Material.COD, Material.COD, Material.COD, Material.COD,
            Material.SALMON, Material.SALMON, Material.SALMON,
            Material.TROPICAL_FISH,
            Material.PUFFERFISH
    };

    private final RexMinions plugin;

    public MinionActions(RexMinions plugin) {
        this.plugin = plugin;
    }

    public void run(ActiveMinion minion, MinionType type, ActionBudget budget) {
        if (minion.paused() || budget.cap() <= 0) {
            return;
        }
        Location origin = minion.location();
        if (origin == null || origin.getWorld() == null) {
            return;
        }
        LevelTable.Level stats = type.levels().lookup(minion.level());
        int range = stats.range();
        switch (type.id()) {
            case "slayer" -> slayer(minion, origin, range, budget);
            case "miner" -> miner(minion, type, origin, range, budget);
            case "collector" -> collector(minion, origin, range, budget);
            case "fisher" -> fisher(minion, origin, range, budget);
            case "farmer" -> farmer(minion, origin, range, budget);
            case "seller" -> seller(minion, budget);
            case "lumber" -> lumber(minion, type, origin, range, budget);
            default -> {
            }
        }
    }

    private void slayer(ActiveMinion minion, Location origin, int range, ActionBudget budget) {
        World world = origin.getWorld();
        for (Entity entity : world.getNearbyEntities(origin, range, range, range)) {
            if (budget.exhausted()) {
                return;
            }
            if (!(entity instanceof LivingEntity living) || !(living instanceof Enemy)) {
                continue;
            }
            if (living instanceof Tameable tameable && tameable.isTamed()) {
                continue;
            }
            if (plugin.keys().isMinionEntity(living)) {
                continue;
            }
            if (!chunkLoaded(living.getLocation())) {
                continue;
            }
            if (!plugin.regions().allows(minion.owner(), living.getLocation())) {
                continue;
            }
            if (!budget.tryTake()) {
                return;
            }
            if (plugin.config().looting()) {
                ItemStack tool = minion.tool();
                int looting = tool == null ? 0 : tool.getEnchantmentLevel(Enchantment.LOOTING);
                if (looting > 0) {
                    living.getPersistentDataContainer().set(plugin.keys().looting, PersistentDataType.INTEGER, looting);
                }
            }
            living.damage(Math.max(1.0, living.getHealth() + 100));
            if (!living.isDead() && living.isValid()) {
                living.getPersistentDataContainer().remove(plugin.keys().looting);
            }
        }
    }

    private void miner(ActiveMinion minion, MinionType type, Location origin, int range, ActionBudget budget) {
        if (type.generators().isEmpty()) {
            return;
        }
        ItemStack tool = ToolWear.dropTool(minion.tool(), plugin.config().fortune());
        for (Block block : slice(minion, origin, range)) {
            if (budget.exhausted()) {
                return;
            }
            if (!type.generators().contains(block.getType())) {
                continue;
            }
            if (!plugin.regions().allows(minion.owner(), block.getLocation())) {
                continue;
            }
            if (!budget.tryTake()) {
                return;
            }
            for (ItemStack drop : block.getDrops(tool)) {
                Stores.keepOrDrop(plugin, minion, drop, block.getLocation());
            }
            block.setType(Material.AIR);
        }
    }

    private void collector(ActiveMinion minion, Location origin, int range, ActionBudget budget) {
        for (Entity entity : origin.getWorld().getNearbyEntities(origin, range, range, range)) {
            if (budget.exhausted()) {
                return;
            }
            if (!(entity instanceof org.bukkit.entity.Item item) || item.isDead()) {
                continue;
            }
            ItemStack stack = item.getItemStack();
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            if (!budget.tryTake()) {
                return;
            }
            int before = stack.getAmount();
            ItemStack left = Stores.deposit(plugin, minion, stack.clone());
            if (left != null && left.getAmount() >= before) {
                continue;
            }
            if (left == null || left.getAmount() <= 0) {
                item.remove();
            } else {
                item.setItemStack(left);
            }
        }
    }

    private void fisher(ActiveMinion minion, Location origin, int range, ActionBudget budget) {
        boolean water = false;
        for (Block block : slice(minion, origin, range)) {
            if (block.getType() == Material.WATER) {
                water = true;
                break;
            }
        }
        if (!water || !budget.tryTake()) {
            return;
        }
        Material fish = FISH[ThreadLocalRandom.current().nextInt(FISH.length)];
        Stores.keepOrDrop(plugin, minion, new ItemStack(fish), origin);
    }

    private void farmer(ActiveMinion minion, Location origin, int range, ActionBudget budget) {
        ItemStack tool = ToolWear.dropTool(minion.tool(), plugin.config().fortune());
        boolean replant = plugin.config().replant();
        for (Block block : slice(minion, origin, range)) {
            if (budget.exhausted()) {
                return;
            }
            Material type = block.getType();
            if (type == Material.MELON || type == Material.PUMPKIN) {
                if (!plugin.regions().allows(minion.owner(), block.getLocation())) {
                    continue;
                }
                if (!budget.tryTake()) {
                    return;
                }
                for (ItemStack drop : block.getDrops(tool)) {
                    Stores.keepOrDrop(plugin, minion, drop, block.getLocation());
                }
                block.setType(Material.AIR);
                continue;
            }
            if (!(block.getBlockData() instanceof Ageable ageable) || !crop(type)) {
                continue;
            }
            if (ageable.getAge() < ageable.getMaximumAge()) {
                continue;
            }
            if (!plugin.regions().allows(minion.owner(), block.getLocation())) {
                continue;
            }
            if (!budget.tryTake()) {
                return;
            }
            if (replant) {
                Material product = product(type);
                if (product != null) {
                    int amount = 1;
                    ItemStack held = minion.tool();
                    if (plugin.config().fortune() && held != null) {
                        int fortune = held.getEnchantmentLevel(Enchantment.FORTUNE);
                        if (fortune > 0) {
                            amount += ThreadLocalRandom.current().nextInt(fortune + 1);
                        }
                    }
                    Stores.keepOrDrop(plugin, minion, new ItemStack(product, amount), block.getLocation());
                }
                ageable.setAge(type == Material.SWEET_BERRY_BUSH ? 1 : 0);
                block.setBlockData(ageable);
            } else {
                for (ItemStack drop : block.getDrops(tool)) {
                    Stores.keepOrDrop(plugin, minion, drop, block.getLocation());
                }
                block.setType(Material.AIR);
            }
        }
    }

    private void seller(ActiveMinion minion, ActionBudget budget) {
        Inventory linked = null;
        try {
            linked = Stores.linkedInventory(minion);
        } catch (Throwable ignored) {
            linked = null;
        }
        if (linked != null) {
            for (int slot = 0; slot < linked.getSize(); slot++) {
                if (budget.exhausted()) {
                    return;
                }
                ItemStack stack = linked.getItem(slot);
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                double price = plugin.prices().price(stack.getType());
                if (price <= 0) {
                    continue;
                }
                if (!budget.tryTake()) {
                    return;
                }
                minion.addBalance(price * stack.getAmount());
                linked.setItem(slot, null);
            }
            return;
        }
        List<ItemStack> items = new ArrayList<>(minion.storage());
        boolean changed = false;
        for (int index = 0; index < items.size(); index++) {
            if (budget.exhausted()) {
                break;
            }
            ItemStack stack = items.get(index);
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            double price = plugin.prices().price(stack.getType());
            if (price <= 0) {
                continue;
            }
            if (!budget.tryTake()) {
                break;
            }
            minion.addBalance(price * stack.getAmount());
            items.set(index, null);
            changed = true;
        }
        if (changed) {
            minion.setStorage(items);
        }
    }

    private void lumber(ActiveMinion minion, MinionType type, Location origin, int range, ActionBudget budget) {
        ItemStack tool = ToolWear.dropTool(minion.tool(), plugin.config().fortune());
        Set<String> seen = new HashSet<>();
        for (Block start : slice(minion, origin, range)) {
            if (budget.exhausted()) {
                return;
            }
            if (!Tag.LOGS.isTagged(start.getType()) || !seen.add(key(start))) {
                continue;
            }
            List<Block> tree = flood(start, seen);
            if (!hasLeaves(tree)) {
                continue;
            }
            Block bottom = lowest(tree);
            for (Block log : tree) {
                if (budget.exhausted()) {
                    return;
                }
                if (!inRange(origin, log, range)) {
                    continue;
                }
                if (type.leaveBottom() && log.equals(bottom)) {
                    continue;
                }
                if (!plugin.regions().allows(minion.owner(), log.getLocation())) {
                    continue;
                }
                if (!budget.tryTake()) {
                    return;
                }
                Material sapling = sapling(log.getType());
                boolean stump = log.equals(bottom);
                for (ItemStack drop : log.getDrops(tool)) {
                    Stores.keepOrDrop(plugin, minion, drop, log.getLocation());
                }
                log.setType(Material.AIR);
                if (stump && type.replantSapling() && sapling != null) {
                    plant(log, sapling);
                }
            }
        }
    }

    private List<Block> slice(ActiveMinion minion, Location origin, int range) {
        World world = origin.getWorld();
        int size = range * 2 + 1;
        int volume = size * size * size;
        int limit = Math.min(plugin.config().blocksPerTick(), volume);
        int cursor = minion.cursor();
        List<Block> blocks = new ArrayList<>();
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();
        for (int n = 0; n < limit; n++) {
            int index = Math.floorMod(cursor + n, volume);
            int dx = (index % size) - range;
            int dy = ((index / size) % size) - range;
            int dz = (index / (size * size)) - range;
            int x = ox + dx;
            int y = oy + dy;
            int z = oz + dz;
            if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                continue;
            }
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            if (!inRange(origin, x, y, z, range)) {
                continue;
            }
            try {
                blocks.add(world.getBlockAt(x, y, z));
            } catch (Throwable ignored) {
                // The block sits in another region. Skip it this tick.
            }
        }
        minion.setCursor(Math.floorMod(cursor + limit, Math.max(1, volume)));
        return blocks;
    }

    private static boolean inRange(Location origin, Block block, int range) {
        return inRange(origin, block.getX(), block.getY(), block.getZ(), range);
    }

    private static boolean inRange(Location origin, int x, int y, int z, int range) {
        double dx = (x + 0.5) - origin.getX();
        double dy = (y + 0.5) - origin.getY();
        double dz = (z + 0.5) - origin.getZ();
        return dx * dx + dy * dy + dz * dz <= (double) range * range;
    }

    private static boolean chunkLoaded(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private static boolean crop(Material type) {
        return switch (type) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART, COCOA,
                    SWEET_BERRY_BUSH, PITCHER_CROP, TORCHFLOWER_CROP -> true;
            default -> false;
        };
    }

    private static Material product(Material crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            case NETHER_WART -> Material.NETHER_WART;
            case COCOA -> Material.COCOA_BEANS;
            case SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            case PITCHER_CROP -> Material.PITCHER_POD;
            case TORCHFLOWER_CROP -> Material.TORCHFLOWER;
            default -> null;
        };
    }

    private static List<Block> flood(Block start, Set<String> seen) {
        List<Block> logs = new ArrayList<>();
        ArrayDeque<Block> queue = new ArrayDeque<>();
        queue.add(start);
        World world = start.getWorld();
        while (!queue.isEmpty() && logs.size() < 160) {
            Block current = queue.removeFirst();
            logs.add(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        int x = current.getX() + dx;
                        int y = current.getY() + dy;
                        int z = current.getZ() + dz;
                        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                            continue;
                        }
                        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                            continue;
                        }
                        Block next = world.getBlockAt(x, y, z);
                        if (!Tag.LOGS.isTagged(next.getType()) || !seen.add(key(next))) {
                            continue;
                        }
                        queue.add(next);
                    }
                }
            }
        }
        return logs;
    }

    private static boolean hasLeaves(List<Block> logs) {
        for (Block log : logs) {
            World world = log.getWorld();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int x = log.getX() + dx;
                        int y = log.getY() + dy;
                        int z = log.getZ() + dz;
                        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
                            continue;
                        }
                        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                            continue;
                        }
                        if (Tag.LEAVES.isTagged(world.getBlockAt(x, y, z).getType())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static Block lowest(List<Block> logs) {
        Block best = logs.getFirst();
        for (Block log : logs) {
            if (log.getY() < best.getY()
                    || (log.getY() == best.getY() && log.getX() < best.getX())
                    || (log.getY() == best.getY() && log.getX() == best.getX() && log.getZ() < best.getZ())) {
                best = log;
            }
        }
        return best;
    }

    private static void plant(Block at, Material sapling) {
        Block below = at.getRelative(0, -1, 0);
        Material ground = below.getType();
        if (ground != Material.DIRT && ground != Material.GRASS_BLOCK && ground != Material.PODZOL
                && ground != Material.COARSE_DIRT && ground != Material.ROOTED_DIRT && ground != Material.MOSS_BLOCK
                && ground != Material.MUD && ground != Material.MYCELIUM && ground != Material.MUDDY_MANGROVE_ROOTS
                && ground != Material.CRIMSON_NYLIUM && ground != Material.WARPED_NYLIUM) {
            return;
        }
        at.setType(sapling);
    }

    private static Material sapling(Material log) {
        return switch (log) {
            case OAK_LOG, OAK_WOOD -> Material.OAK_SAPLING;
            case SPRUCE_LOG, SPRUCE_WOOD -> Material.SPRUCE_SAPLING;
            case BIRCH_LOG, BIRCH_WOOD -> Material.BIRCH_SAPLING;
            case JUNGLE_LOG, JUNGLE_WOOD -> Material.JUNGLE_SAPLING;
            case ACACIA_LOG, ACACIA_WOOD -> Material.ACACIA_SAPLING;
            case DARK_OAK_LOG, DARK_OAK_WOOD -> Material.DARK_OAK_SAPLING;
            case MANGROVE_LOG, MANGROVE_WOOD -> Material.MANGROVE_PROPAGULE;
            case CHERRY_LOG, CHERRY_WOOD -> Material.CHERRY_SAPLING;
            case CRIMSON_STEM, CRIMSON_HYPHAE -> Material.CRIMSON_FUNGUS;
            case WARPED_STEM, WARPED_HYPHAE -> Material.WARPED_FUNGUS;
            default -> null;
        };
    }

    private static String key(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
}
