package me.rexsystems.rexMinions.command;

import me.rexsystems.rexMinions.RexMinions;
import me.rexsystems.rexMinions.config.TypeRegistry;
import me.rexsystems.rexMinions.minion.ActiveMinion;
import me.rexsystems.rexMinions.util.MessageService;
import me.rexsystems.rexMinions.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MinionsCommand implements CommandExecutor, TabCompleter {

    private final RexMinions plugin;

    public MinionsCommand(RexMinions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            for (String line : plugin.config().helpLines()) {
                MessageService.send(sender, line);
            }
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> reload(sender);
            case "list" -> list(sender);
            case "give" -> give(sender, args);
            default -> plugin.message(sender, "unknown-command", Map.of());
        }
        return true;
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("rexminions.reload")) {
            plugin.message(sender, "no-permission", Map.of());
            return;
        }
        plugin.reloadFiles();
        plugin.message(sender, "reloaded", Map.of());
    }

    private void list(CommandSender sender) {
        boolean admin = !(sender instanceof Player) || sender.hasPermission("rexminions.admin");
        List<ActiveMinion> rows;
        int limit;
        if (sender instanceof Player player && !admin) {
            if (!player.hasPermission("rexminions.use")) {
                plugin.message(sender, "no-permission", Map.of());
                return;
            }
            rows = plugin.minions().ownedBy(player.getUniqueId());
            limit = plugin.minions().limitOf(player);
        } else if (sender instanceof Player player) {
            rows = new ArrayList<>(plugin.minions().all());
            limit = plugin.minions().limitOf(player);
        } else {
            rows = new ArrayList<>(plugin.minions().all());
            limit = plugin.config().defaultLimit();
        }
        if (rows.isEmpty()) {
            plugin.message(sender, "list-empty", Map.of());
            return;
        }
        plugin.message(sender, "list-header", Map.of(
                "count", Integer.toString(rows.size()),
                "limit", Integer.toString(limit)
        ));
        String path = admin ? "list-line-admin" : "list-line";
        for (ActiveMinion minion : rows) {
            String owner = Bukkit.getOfflinePlayer(minion.owner()).getName();
            plugin.message(sender, path, Map.of(
                    "type", typeName(minion),
                    "level", Integer.toString(minion.level()),
                    "world", world(minion),
                    "x", Integer.toString(blockX(minion)),
                    "y", Integer.toString(blockY(minion)),
                    "z", Integer.toString(blockZ(minion)),
                    "owner", owner == null ? minion.owner().toString() : owner
            ));
        }
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rexminions.give")) {
            plugin.message(sender, "no-permission", Map.of());
            return;
        }
        if (args.length < 3) {
            plugin.message(sender, "unknown-command", Map.of());
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.message(sender, "unknown-player", Map.of());
            return;
        }
        String typeId = args[2].toLowerCase(Locale.ROOT);
        if (plugin.types().get(typeId) == null) {
            plugin.message(sender, "unknown-type", Map.of("type", args[2]));
            return;
        }
        int level = 1;
        int amount = 1;
        try {
            if (args.length >= 4) {
                level = Integer.parseInt(args[3]);
            }
            if (args.length >= 5) {
                amount = Integer.parseInt(args[4]);
            }
        } catch (NumberFormatException ex) {
            plugin.message(sender, "invalid-number", Map.of());
            return;
        }
        if (level < 1 || amount < 1 || amount > 64) {
            plugin.message(sender, "invalid-number", Map.of());
            return;
        }
        ItemStack template = plugin.items().create(typeId, level);
        int left = amount;
        while (left > 0) {
            ItemStack stack = template.clone();
            int give = Math.min(template.getMaxStackSize(), left);
            stack.setAmount(give);
            ItemStack delivery = stack.clone();
            SchedulerUtils.runForPlayer(plugin, target, () -> {
                var leftover = target.getInventory().addItem(delivery);
                for (ItemStack extra : leftover.values()) {
                    target.getWorld().dropItem(target.getLocation(), extra);
                }
            });
            left -= give;
        }
        plugin.message(sender, "gave", Map.of(
                "amount", Integer.toString(amount),
                "type", plugin.types().get(typeId).name(),
                "level", Integer.toString(level),
                "player", target.getName()
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("help");
            if (sender.hasPermission("rexminions.give")) {
                options.add("give");
            }
            if (sender.hasPermission("rexminions.use") || sender.hasPermission("rexminions.admin")) {
                options.add("list");
            }
            if (sender.hasPermission("rexminions.reload")) {
                options.add("reload");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                options.add(player.getName());
            }
            return filter(options, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            options.addAll(TypeRegistry.IDS);
            return filter(options, args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return filter(List.of("1", "2", "3"), args[3]);
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("give")) {
            return filter(List.of("1", "16", "64"), args[4]);
        }
        return List.of();
    }

    private String typeName(ActiveMinion minion) {
        if (plugin.types().get(minion.typeId()) == null) {
            return minion.typeId();
        }
        return plugin.types().get(minion.typeId()).name();
    }

    private static String world(ActiveMinion minion) {
        var location = minion.location();
        if (location != null && location.getWorld() != null) {
            return location.getWorld().getName();
        }
        return "unknown";
    }

    private static int blockX(ActiveMinion minion) {
        var location = minion.location();
        return location == null ? 0 : location.getBlockX();
    }

    private static int blockY(ActiveMinion minion) {
        var location = minion.location();
        return location == null ? 0 : location.getBlockY();
    }

    private static int blockZ(ActiveMinion minion) {
        var location = minion.location();
        return location == null ? 0 : location.getBlockZ();
    }

    private static List<String> filter(List<String> options, String token) {
        String prefix = token == null ? "" : token.toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matched.add(option);
            }
        }
        return matched;
    }
}
