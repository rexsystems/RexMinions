package me.rexsystems.rexMinions.action;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.minion.ActiveMinion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Stores {

    private Stores() {
    }

    public static boolean linkable(Material type) {
        return type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.BARREL;
    }

    public static ItemStack deposit(RexMinions plugin, ActiveMinion minion, ItemStack stack) {
        if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
            return null;
        }
        ItemStack left = intoLink(minion, stack.clone());
        if (left == null || left.getAmount() <= 0) {
            return null;
        }
        return intoStorage(plugin, minion, left);
    }

    public static void keepOrDrop(RexMinions plugin, ActiveMinion minion, ItemStack stack, Location at) {
        ItemStack left = deposit(plugin, minion, stack);
        if (left == null || left.getAmount() <= 0 || at == null || at.getWorld() == null) {
            return;
        }
        at.getWorld().dropItemNaturally(at.clone().add(0.5, 0.5, 0.5), left);
    }

    public static Inventory linkedInventory(ActiveMinion minion) {
        Location link = minion.link();
        if (link == null || link.getWorld() == null) {
            return null;
        }
        World world = link.getWorld();
        int x = link.getBlockX();
        int y = link.getBlockY();
        int z = link.getBlockZ();
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return null;
        }
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
            return null;
        }
        Block block = world.getBlockAt(x, y, z);
        if (!linkable(block.getType())) {
            return null;
        }
        if (!(block.getState() instanceof Container container)) {
            return null;
        }
        return container.getInventory();
    }

    private static ItemStack intoLink(ActiveMinion minion, ItemStack stack) {
        Inventory inventory;
        try {
            inventory = linkedInventory(minion);
        } catch (Throwable ignored) {
            return stack;
        }
        if (inventory == null) {
            return stack;
        }
        Map<Integer, ItemStack> leftover = inventory.addItem(stack);
        if (leftover.isEmpty()) {
            return null;
        }
        ItemStack remain = leftover.values().iterator().next();
        if (remain == null || remain.getAmount() <= 0) {
            return null;
        }
        return remain;
    }

    private static ItemStack intoStorage(RexMinions plugin, ActiveMinion minion, ItemStack incoming) {
        int max = plugin.config().storageSize();
        List<ItemStack> items = new ArrayList<>(minion.storage());
        int amount = incoming.getAmount();
        for (ItemStack existing : items) {
            if (amount <= 0) {
                break;
            }
            if (!existing.isSimilar(incoming)) {
                continue;
            }
            int room = existing.getMaxStackSize() - existing.getAmount();
            if (room <= 0) {
                continue;
            }
            int move = Math.min(room, amount);
            existing.setAmount(existing.getAmount() + move);
            amount -= move;
        }
        while (amount > 0 && items.size() < max) {
            ItemStack created = incoming.clone();
            int move = Math.min(created.getMaxStackSize(), amount);
            created.setAmount(move);
            items.add(created);
            amount -= move;
        }
        minion.setStorage(items);
        if (amount <= 0) {
            return null;
        }
        ItemStack left = incoming.clone();
        left.setAmount(amount);
        return left;
    }
}
