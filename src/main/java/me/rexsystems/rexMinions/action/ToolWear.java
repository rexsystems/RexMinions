package me.rexsystems.rexMinions.action;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

public final class ToolWear {

    private ToolWear() {
    }

    public static ItemStack dropTool(ItemStack tool, boolean fortune) {
        if (tool == null || tool.getType().isAir()) {
            return new ItemStack(Material.AIR);
        }
        ItemStack copy = tool.clone();
        if (!fortune) {
            copy.removeEnchantment(Enchantment.FORTUNE);
        }
        return copy;
    }

    public static ItemStack apply(ItemStack tool, boolean useDurability) {
        if (!useDurability || tool == null || tool.getType().isAir()) {
            return tool;
        }
        if (tool.getType().getMaxDurability() <= 0) {
            return tool;
        }
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable) || meta.isUnbreakable()) {
            return tool;
        }
        int unbreaking = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        if (unbreaking > 0) {
            double skip = unbreaking / (double) (unbreaking + 1);
            if (ThreadLocalRandom.current().nextDouble() < skip) {
                return tool;
            }
        }
        int next = damageable.getDamage() + 1;
        if (next >= tool.getType().getMaxDurability()) {
            return null;
        }
        damageable.setDamage(next);
        ItemStack worn = tool.clone();
        worn.setItemMeta(meta);
        return worn;
    }
}
