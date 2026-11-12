package me.rexsystems.rexMinions.minion;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.config.MinionType;
import me.rexsystems.rexMinions.logic.LevelTable;
import me.rexsystems.rexMinions.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MinionItems {

    private final RexMinions plugin;

    public MinionItems(RexMinions plugin) {
        this.plugin = plugin;
    }

    public ItemStack create(String typeId, int level) {
        MinionType type = plugin.types().get(typeId);
        int safeLevel = Math.max(1, level);
        if (type == null) {
            return new ItemStack(Material.ARMOR_STAND);
        }
        LevelTable.Level stats = type.levels().lookup(safeLevel);
        Map<String, String> tokens = Map.of(
                "level", Integer.toString(safeLevel),
                "range", Integer.toString(stats.range()),
                "speed", Integer.toString(stats.speedTicks()),
                "type", type.name()
        );
        ItemStack item = new ItemStack(type.material() == null ? Material.ARMOR_STAND : type.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextParser.parse(TextParser.apply(type.itemName(), tokens)));
        List<Component> lore = new ArrayList<>();
        for (String line : type.lore()) {
            lore.add(TextParser.parse(TextParser.apply(line, tokens)));
        }
        meta.lore(lore);
        meta.getPersistentDataContainer().set(plugin.keys().type, PersistentDataType.STRING, type.id());
        meta.getPersistentDataContainer().set(plugin.keys().level, PersistentDataType.INTEGER, safeLevel);
        item.setItemMeta(meta);
        return item;
    }
}
