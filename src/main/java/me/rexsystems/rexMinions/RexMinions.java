package me.rexsystems.rexMinions;

import me.rexsystems.rexMinions.command.MinionsCommand;
import me.rexsystems.rexMinions.config.ConfigManager;
import me.rexsystems.rexMinions.config.PriceList;
import me.rexsystems.rexMinions.config.TypeRegistry;
import me.rexsystems.rexMinions.gui.MinionMenus;
import me.rexsystems.rexMinions.hook.WorldGuardBridge;
import me.rexsystems.rexMinions.listener.MinionListener;
import me.rexsystems.rexMinions.minion.Keys;
import me.rexsystems.rexMinions.minion.MinionItems;
import me.rexsystems.rexMinions.minion.MinionService;
import me.rexsystems.rexMinions.papi.RexMinionsPlaceholders;
import me.rexsystems.rexMinions.storage.MinionDatabase;
import me.rexsystems.rexMinions.util.MessageService;
import me.rexsystems.rexMinions.util.Placeholders;
import me.rexsystems.rexMinions.util.Sounds;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class RexMinions extends JavaPlugin {

    private ConfigManager configManager;
    private TypeRegistry types;
    private PriceList prices;
    private Keys keys;
    private MinionDatabase database;
    private MinionItems items;
    private MinionService minions;
    private MinionMenus menus;
    private WorldGuardBridge regions;
    private RexMinionsPlaceholders placeholders;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.load();
        this.types = new TypeRegistry(this);
        this.types.load();
        this.prices = new PriceList(this);
        this.prices.load();
        this.keys = new Keys(this);
        this.items = new MinionItems(this);
        this.database = new MinionDatabase(this);
        try {
            this.database.open();
        } catch (Exception failure) {
            getLogger().severe("Could not open minions.db: " + failure.getMessage());
        }
        this.minions = new MinionService(this);
        this.minions.load();
        this.menus = new MinionMenus(this);
        this.regions = WorldGuardBridge.create(getLogger());

        getServer().getPluginManager().registerEvents(new MinionListener(this), this);

        MinionsCommand command = new MinionsCommand(this);
        PluginCommand pluginCommand = getCommand("rexminions");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new RexMinionsPlaceholders(this);
            this.placeholders.register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("RexMinions enabled.");
    }

    @Override
    public void onDisable() {
        if (menus != null) {
            menus.captureOpen();
        }
        if (minions != null) {
            minions.shutdown();
        }
        if (database != null) {
            database.close();
        }
        if (placeholders != null) {
            placeholders.unregister();
        }
        getLogger().info("RexMinions disabled.");
    }

    public void reloadFiles() {
        configManager.load();
        types.load();
        prices.load();
    }

    public ConfigManager config() {
        return configManager;
    }

    public TypeRegistry types() {
        return types;
    }

    public PriceList prices() {
        return prices;
    }

    public Keys keys() {
        return keys;
    }

    public MinionDatabase database() {
        return database;
    }

    public MinionItems items() {
        return items;
    }

    public MinionService minions() {
        return minions;
    }

    public MinionMenus menus() {
        return menus;
    }

    public WorldGuardBridge regions() {
        return regions;
    }

    public void message(CommandSender sender, String path, Map<String, String> tokens) {
        String text = configManager.prefixed(path);
        if (sender instanceof Player player) {
            text = Placeholders.apply(player, text);
        }
        MessageService.send(sender, text, tokens == null ? Map.of() : tokens);
    }

    public void sound(Player player, String path) {
        if (player == null) {
            return;
        }
        Sounds.play(player.getLocation(), configManager.sound(path), Map.of(), configManager.soundVolume(), configManager.soundPitch());
    }
}
