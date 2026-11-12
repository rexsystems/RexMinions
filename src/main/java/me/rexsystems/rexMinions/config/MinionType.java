package me.rexsystems.rexMinions.config;

import me.rexsystems.rexMinions.logic.LevelTable;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public final class MinionType {

    private final String id;
    private final boolean enabled;
    private final String name;
    private final String itemName;
    private final Material material;
    private final Material helmet;
    private final Material chestplate;
    private final Material leggings;
    private final Material boots;
    private final List<String> lore;
    private final LevelTable levels;
    private final Set<Material> generators;
    private final boolean leaveBottom;
    private final boolean replantSapling;

    public MinionType(
            String id,
            boolean enabled,
            String name,
            String itemName,
            Material material,
            Material helmet,
            Material chestplate,
            Material leggings,
            Material boots,
            List<String> lore,
            LevelTable levels,
            Set<Material> generators,
            boolean leaveBottom,
            boolean replantSapling
    ) {
        this.id = id;
        this.enabled = enabled;
        this.name = name;
        this.itemName = itemName;
        this.material = material;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.lore = List.copyOf(lore);
        this.levels = levels;
        this.generators = Set.copyOf(generators);
        this.leaveBottom = leaveBottom;
        this.replantSapling = replantSapling;
    }

    public String id() {
        return id;
    }

    public boolean enabled() {
        return enabled;
    }

    public String name() {
        return name;
    }

    public String itemName() {
        return itemName;
    }

    public Material material() {
        return material;
    }

    public Material helmet() {
        return helmet;
    }

    public Material chestplate() {
        return chestplate;
    }

    public Material leggings() {
        return leggings;
    }

    public Material boots() {
        return boots;
    }

    public List<String> lore() {
        return lore;
    }

    public LevelTable levels() {
        return levels;
    }

    public Set<Material> generators() {
        return generators;
    }

    public boolean leaveBottom() {
        return leaveBottom;
    }

    public boolean replantSapling() {
        return replantSapling;
    }
}
