package me.rexsystems.rexMinions.gui;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.config.MinionType;
import me.rexsystems.rexMinions.logic.LevelTable;
import me.rexsystems.rexMinions.minion.ActiveMinion;
import me.rexsystems.rexMinions.util.Names;
import me.rexsystems.rexMinions.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MinionMenus {

    private final RexMinions plugin;
    private final Set<UUID> switching = ConcurrentHashMap.newKeySet();

    public MinionMenus(RexMinions plugin) {
        this.plugin = plugin;
    }

    public boolean switching(UUID playerId) {
        return switching.contains(playerId);
    }

    public void openMain(Player player, ActiveMinion minion) {
        MinionType type = plugin.types().get(minion.typeId());
        String typeName = type == null ? minion.typeId() : type.name();
        Map<String, String> tokens = tokens(minion, typeName);
        MinionHolder holder = new MinionHolder(minion.id(), MinionHolder.Kind.MAIN);
        int size = plugin.config().guiRows() * 9;
        Inventory inventory = Bukkit.createInventory(holder, size, TextParser.parse(TextParser.apply(plugin.config().guiTitle(), tokens)));
        holder.attach(inventory);
        ItemStack filler = named(plugin.config().filler(), Component.empty(), List.of());
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, filler);
        }
        inventory.setItem(plugin.config().slot("info", 4), button(type == null ? Material.ARMOR_STAND : type.material(), "info-name", List.of(), tokens));
        ItemStack tool = minion.tool();
        if (tool != null) {
            inventory.setItem(plugin.config().slot("tool", 11), tool);
        } else {
            inventory.setItem(plugin.config().slot("tool", 11), null);
        }
        inventory.setItem(plugin.config().slot("link", 13), button(Material.CHEST, "link-name", plugin.config().guiLore("link-lore"), tokens));
        inventory.setItem(plugin.config().slot("storage", 15), button(Material.BARREL, "storage-name", plugin.config().guiLore("storage-lore"), tokens));
        inventory.setItem(plugin.config().slot("balance", 22), button(plugin.config().currency(), "balance-name", plugin.config().guiLore("balance-lore"), tokens));
        inventory.setItem(plugin.config().slot("pickup", 26), button(Material.BARRIER, "pickup-name", plugin.config().guiLore("pickup-lore"), tokens));
        minion.pause();
        open(player, inventory);
    }

    public void openStorage(Player player, ActiveMinion minion) {
        MinionType type = plugin.types().get(minion.typeId());
        String typeName = type == null ? minion.typeId() : type.name();
        Map<String, String> tokens = tokens(minion, typeName);
        MinionHolder holder = new MinionHolder(minion.id(), MinionHolder.Kind.STORAGE);
        int size = plugin.config().storageSize();
        Inventory inventory = Bukkit.createInventory(holder, size, TextParser.parse(TextParser.apply(plugin.config().storageTitle(), tokens)));
        holder.attach(inventory);
        List<ItemStack> items = minion.storage();
        minion.setStorage(List.of());
        for (int index = 0; index < items.size() && index < size; index++) {
            inventory.setItem(index, items.get(index));
        }
        minion.pause();
        plugin.minions().save(minion);
        open(player, inventory);
    }

    public void capture(Inventory inventory, ActiveMinion minion) {
        if (!(inventory.getHolder() instanceof MinionHolder holder)) {
            return;
        }
        if (holder.kind() == MinionHolder.Kind.MAIN) {
            ItemStack tool = inventory.getItem(plugin.config().slot("tool", 11));
            minion.setTool(tool);
            inventory.setItem(plugin.config().slot("tool", 11), null);
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null && !stack.getType().isAir()) {
                items.add(stack.clone());
            }
        }
        minion.setStorage(items);
        inventory.clear();
    }

    public void captureOpen() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                if (!(inventory.getHolder() instanceof MinionHolder holder)) {
                    continue;
                }
                ActiveMinion minion = plugin.minions().find(holder.minionId());
                if (minion == null) {
                    continue;
                }
                capture(inventory, minion);
            } catch (Throwable ignored) {
            }
        }
    }

    private void open(Player player, Inventory inventory) {
        switching.add(player.getUniqueId());
        try {
            player.openInventory(inventory);
        } finally {
            switching.remove(player.getUniqueId());
        }
    }

    private Map<String, String> tokens(ActiveMinion minion, String typeName) {
        MinionType type = plugin.types().get(minion.typeId());
        LevelTable.Level stats = type == null
                ? new LevelTable.Level(minion.level(), 100, 3)
                : type.levels().lookup(minion.level());
        return Map.of(
                "type", typeName,
                "level", Integer.toString(minion.level()),
                "range", Integer.toString(stats.range()),
                "speed", Integer.toString(stats.speedTicks()),
                "distance", Integer.toString(plugin.config().linkDistance()),
                "balance", String.format(Locale.ROOT, "%.2f", minion.balance()),
                "currency", Names.enumName(plugin.config().currency().name())
        );
    }

    private ItemStack button(Material material, String namePath, List<String> loreLines, Map<String, String> tokens) {
        Material used = material == null || material.isAir() ? Material.PAPER : material;
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(TextParser.parse(TextParser.apply(line, tokens)));
        }
        return named(used, TextParser.parse(TextParser.apply(plugin.config().guiString(namePath, namePath), tokens)), lore);
    }

    private static ItemStack named(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
