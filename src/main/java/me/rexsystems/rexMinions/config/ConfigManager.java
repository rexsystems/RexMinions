package me.rexsystems.rexMinions.config;

import me.rexsystems.rexMinions.RexMinions;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;

public final class ConfigManager {

    private final RexMinions plugin;
    private FileConfiguration config;

    public ConfigManager(RexMinions plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        new ConfigAutoUpdater(plugin).update();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean enabled() {
        return config.getBoolean("settings.enabled", true);
    }

    public int defaultLimit() {
        return Math.max(0, config.getInt("settings.default-limit", 5));
    }

    public int actionsPerTick() {
        return Math.max(0, Math.min(64, config.getInt("settings.actions-per-tick", 4)));
    }

    public int blocksPerTick() {
        return Math.max(1, Math.min(4096, config.getInt("settings.blocks-per-tick", 512)));
    }

    public int linkDistance() {
        return Math.max(1, Math.min(32, config.getInt("settings.link-distance", 8)));
    }

    public int storageSize() {
        int size = config.getInt("settings.storage-size", 27);
        size = Math.max(9, Math.min(54, size));
        return (size / 9) * 9;
    }

    public boolean replant() {
        return config.getBoolean("settings.replant", true);
    }

    public boolean useDurability() {
        return config.getBoolean("settings.use-durability", true);
    }

    public boolean fortune() {
        return config.getBoolean("settings.fortune", true);
    }

    public boolean looting() {
        return config.getBoolean("settings.looting", true);
    }

    public int efficiencyTicks() {
        return Math.max(0, config.getInt("settings.efficiency-ticks", 4));
    }

    public int minimumTicks() {
        return Math.max(1, config.getInt("settings.minimum-ticks", 10));
    }

    public String entityMode() {
        String mode = config.getString("settings.entity", "armor_stand");
        if (mode == null) {
            return "armor_stand";
        }
        return mode.toLowerCase(Locale.ROOT);
    }

    public boolean smallArmorStand() {
        return config.getBoolean("settings.small-armor-stand", false);
    }

    public boolean showName() {
        return config.getBoolean("settings.show-name", true);
    }

    public Material currency() {
        Material material = Material.matchMaterial(config.getString("settings.currency", "EMERALD"));
        if (material == null || material.isAir()) {
            return Material.EMERALD;
        }
        return material;
    }

    public String guiTitle() {
        return config.getString("gui.title", "<gold>{type}");
    }

    public int guiRows() {
        int rows = config.getInt("gui.rows", 3);
        return Math.max(1, Math.min(6, rows));
    }

    public Material filler() {
        Material material = Material.matchMaterial(config.getString("gui.filler", "GRAY_STAINED_GLASS_PANE"));
        return material == null ? Material.GRAY_STAINED_GLASS_PANE : material;
    }

    public int slot(String name, int fallback) {
        int slot = config.getInt("gui.slots." + name, fallback);
        int size = guiRows() * 9;
        if (slot < 0 || slot >= size) {
            return Math.min(fallback, size - 1);
        }
        return slot;
    }

    public String guiString(String path, String fallback) {
        String value = config.getString("gui." + path);
        return value == null ? fallback : value;
    }

    public List<String> guiLore(String path) {
        List<String> lines = config.getStringList("gui." + path);
        return lines == null ? List.of() : lines;
    }

    public String storageTitle() {
        return config.getString("gui.storage-title", "<gold>Storage");
    }

    public String sound(String path) {
        return config.getString("sounds." + path, "");
    }

    public float soundVolume() {
        return (float) config.getDouble("sounds.volume", 1.0);
    }

    public float soundPitch() {
        return (float) config.getDouble("sounds.pitch", 1.0);
    }

    public String message(String path) {
        return config.getString("messages." + path, "");
    }

    public String prefix() {
        String prefix = message("prefix");
        return prefix == null ? "" : prefix;
    }

    public String prefixed(String path) {
        String raw = message(path);
        if (raw == null) {
            return "";
        }
        return raw.replace("<prefix>", prefix()).replace("{prefix}", prefix());
    }

    public List<String> helpLines() {
        List<String> lines = config.getStringList("messages.help");
        if (lines == null || lines.isEmpty()) {
            return List.of(prefixed("unknown-command"));
        }
        String prefix = prefix();
        return lines.stream()
                .map(line -> line.replace("<prefix>", prefix).replace("{prefix}", prefix))
                .toList();
    }
}
