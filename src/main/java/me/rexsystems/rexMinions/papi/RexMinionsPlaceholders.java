package me.rexsystems.rexMinions.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rexsystems.rexMinions.RexMinions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class RexMinionsPlaceholders extends PlaceholderExpansion {

    private final RexMinions plugin;

    public RexMinionsPlaceholders(RexMinions plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rexminions";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RexSystems";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        String key = params.toLowerCase(Locale.ROOT);
        if (key.equals("count")) {
            return Integer.toString(plugin.minions().count(player.getUniqueId()));
        }
        if (key.equals("limit")) {
            if (player instanceof Player online) {
                return Integer.toString(plugin.minions().limitOf(online));
            }
            return Integer.toString(plugin.config().defaultLimit());
        }
        if (key.startsWith("count_")) {
            String type = key.substring("count_".length());
            int count = 0;
            for (var minion : plugin.minions().ownedBy(player.getUniqueId())) {
                if (minion.typeId().equalsIgnoreCase(type)) {
                    count++;
                }
            }
            return Integer.toString(count);
        }
        return null;
    }
}
