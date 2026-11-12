package me.rexsystems.rexMinions.config;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.logic.LevelTable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TypeRegistry {

    public static final List<String> IDS = List.of(
            "slayer",
            "miner",
            "collector",
            "fisher",
            "farmer",
            "seller",
            "lumber"
    );

    private final RexMinions plugin;
    private Map<String, MinionType> types = Map.of();

    public TypeRegistry(RexMinions plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File folder = new File(plugin.getDataFolder(), "types");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create " + folder.getAbsolutePath());
        }
        for (String id : IDS) {
            File file = new File(folder, id + ".yml");
            if (file.exists()) {
                continue;
            }
            plugin.saveResource("types/" + id + ".yml", false);
        }
        Map<String, MinionType> loaded = new LinkedHashMap<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String id = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT);
                try {
                    loaded.put(id, read(id, YamlConfiguration.loadConfiguration(file)));
                } catch (RuntimeException failure) {
                    plugin.getLogger().warning("Skipped type file " + file.getName() + ": " + failure.getMessage());
                }
            }
        }
        this.types = Collections.unmodifiableMap(loaded);
    }

    public MinionType get(String id) {
        if (id == null) {
            return null;
        }
        return types.get(id.toLowerCase(Locale.ROOT));
    }

    public List<String> ids() {
        return List.copyOf(types.keySet());
    }

    private MinionType read(String id, YamlConfiguration yaml) {
        List<LevelTable.Level> levels = new ArrayList<>();
        ConfigurationSection section = yaml.getConfigurationSection("levels");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int level;
                try {
                    level = Integer.parseInt(key);
                } catch (NumberFormatException ex) {
                    continue;
                }
                ConfigurationSection row = section.getConfigurationSection(key);
                int speed = row == null ? section.getInt(key + ".speed-ticks", 100) : row.getInt("speed-ticks", 100);
                int range = row == null ? section.getInt(key + ".range", 3) : row.getInt("range", 3);
                levels.add(new LevelTable.Level(level, speed, range));
            }
        }
        Set<Material> generators = new LinkedHashSet<>();
        for (String name : yaml.getStringList("generators")) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                generators.add(material);
            }
        }
        return new MinionType(
                id,
                yaml.getBoolean("enabled", true),
                yaml.getString("name", id),
                yaml.getString("item-name", yaml.getString("name", id)),
                material(yaml.getString("material"), Material.ARMOR_STAND),
                materialOrNull(yaml.getString("helmet")),
                materialOrNull(yaml.getString("chestplate")),
                materialOrNull(yaml.getString("leggings")),
                materialOrNull(yaml.getString("boots")),
                yaml.getStringList("lore"),
                LevelTable.of(levels),
                generators,
                yaml.getBoolean("leave-bottom", true),
                yaml.getBoolean("replant", false)
        );
    }

    private static Material material(String name, Material fallback) {
        Material parsed = materialOrNull(name);
        return parsed == null ? fallback : parsed;
    }

    private static Material materialOrNull(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return Material.matchMaterial(name.trim());
    }
}
