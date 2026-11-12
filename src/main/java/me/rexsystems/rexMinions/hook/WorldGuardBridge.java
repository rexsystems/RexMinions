package me.rexsystems.rexMinions.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class WorldGuardBridge {

    private final Logger logger;
    private final Object hook;
    private final Method allows;
    private final AtomicBoolean failed = new AtomicBoolean();

    private WorldGuardBridge(Logger logger, Object hook, Method allows) {
        this.logger = logger;
        this.hook = hook;
        this.allows = allows;
    }

    public static WorldGuardBridge create(Logger logger) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            return new WorldGuardBridge(logger, null, null);
        }
        try {
            Class<?> type = Class.forName("me.rexsystems.rexMinions.hook.WorldGuardRegionHook");
            Object hook = type.getDeclaredConstructor().newInstance();
            Method allows = type.getMethod("allows", UUID.class, Location.class);
            return new WorldGuardBridge(logger, hook, allows);
        } catch (Throwable failure) {
            logger.warning("WorldGuard hook failed to load. Minions will keep working without region checks. " + failure.getMessage());
            return new WorldGuardBridge(logger, null, null);
        }
    }

    public boolean allows(UUID owner, Location location) {
        if (hook == null || allows == null || owner == null || location == null) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(allows.invoke(hook, owner, location));
        } catch (Throwable failure) {
            if (failed.compareAndSet(false, true)) {
                logger.warning("WorldGuard hook failed. Minions will keep working without region checks. " + failure.getMessage());
            }
            return true;
        }
    }
}
