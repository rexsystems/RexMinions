package me.rexsystems.rexMinions.config;

import me.rexsystems.rexMinions.RexMinions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PriceList {

    private final RexMinions plugin;
    private Map<Material, Double> prices = Map.of();

    public PriceList(RexMinions plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "prices.yml");
        if (!file.exists()) {
            plugin.saveResource("prices.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("prices");
        Map<Material, Double> loaded = new HashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null) {
                    continue;
                }
                double price = section.getDouble(key, 0);
                if (price > 0) {
                    loaded.put(material, price);
                }
            }
        }
        this.prices = Collections.unmodifiableMap(loaded);
    }

    public double price(Material material) {
        if (material == null) {
            return 0;
        }
        return prices.getOrDefault(material, 0.0);
    }
}
