package me.rexsystems.rexMinions.storage;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemCodec {

    private ItemCodec() {
    }

    public static String encode(List<ItemStack> items) {
        YamlConfiguration yaml = new YamlConfiguration();
        List<ItemStack> clean = new ArrayList<>();
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null && !item.getType().isAir()) {
                    clean.add(item.clone());
                }
            }
        }
        yaml.set("items", clean);
        return yaml.saveToString();
    }

    public static String encode(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "";
        }
        return encode(List.of(item));
    }

    public static List<ItemStack> decode(String data) {
        List<ItemStack> out = new ArrayList<>();
        if (data == null || data.isBlank()) {
            return out;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(data);
        } catch (InvalidConfigurationException ex) {
            return out;
        }
        List<?> raw = yaml.getList("items");
        if (raw == null) {
            return out;
        }
        for (Object entry : raw) {
            if (entry instanceof ItemStack stack && !stack.getType().isAir()) {
                out.add(stack.clone());
            }
        }
        return out;
    }

    public static ItemStack decodeOne(String data) {
        List<ItemStack> items = decode(data);
        if (items.isEmpty()) {
            return null;
        }
        return items.getFirst();
    }
}
