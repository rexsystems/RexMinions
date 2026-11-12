package me.rexsystems.rexMinions.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

public final class Sounds {

    private Sounds() {
    }

    public static void play(Location location, String configured, Map<String, String> placeholders, float volume, float pitch) {
        if (location == null || location.getWorld() == null || configured == null || configured.isBlank()) {
            return;
        }
        Sound sound = resolve(TextParser.apply(configured, placeholders));
        if (sound == null) {
            return;
        }
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    static Sound resolve(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String trimmed = name.trim();
        try {
            Field field = Sound.class.getField(trimmed.toUpperCase(Locale.ROOT));
            if (Sound.class.isAssignableFrom(field.getType())) {
                return (Sound) field.get(null);
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through to a namespaced key.
        }
        String raw = trimmed.toLowerCase(Locale.ROOT);
        NamespacedKey key = raw.contains(":")
                ? NamespacedKey.fromString(raw)
                : NamespacedKey.minecraft(raw);
        if (key == null) {
            return null;
        }
        return Registry.SOUNDS.get(key);
    }
}
