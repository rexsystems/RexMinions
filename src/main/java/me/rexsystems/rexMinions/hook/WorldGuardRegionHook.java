package me.rexsystems.rexMinions.hook;

import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.UUID;

public final class WorldGuardRegionHook {

    public boolean allows(UUID owner, Location location) {
        try {
            return query(owner, location);
        } catch (Throwable failure) {
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(failure);
        }
    }

    private boolean query(UUID owner, Location location) throws Exception {
        Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Object worldGuard = wgClass.getMethod("getInstance").invoke(null);
        Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
        Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Object query = container.getClass().getMethod("createQuery").invoke(container);
        Class<?> adapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Object adapted = adapter.getMethod("adapt", Location.class).invoke(null, location);
        Object set = null;
        for (Method method : query.getClass().getMethods()) {
            if (!method.getName().equals("getApplicableRegions") || method.getParameterCount() != 1) {
                continue;
            }
            if (method.getParameterTypes()[0].isInstance(adapted)) {
                set = method.invoke(query, adapted);
                break;
            }
        }
        if (set == null) {
            throw new NoSuchMethodException("getApplicableRegions");
        }
        Object regions = set.getClass().getMethod("getRegions").invoke(set);
        boolean anyRegion = false;
        boolean member = false;
        for (Object region : (Iterable<?>) regions) {
            String id = String.valueOf(region.getClass().getMethod("getId").invoke(region));
            if ("__global__".equalsIgnoreCase(id)) {
                continue;
            }
            anyRegion = true;
            Object members = region.getClass().getMethod("getMembers").invoke(region);
            Object owners = region.getClass().getMethod("getOwners").invoke(region);
            if (contains(members, owner) || contains(owners, owner)) {
                member = true;
            }
        }
        if (!anyRegion) {
            return true;
        }
        return member;
    }

    private static boolean contains(Object domain, UUID owner) throws Exception {
        if (domain == null || owner == null) {
            return false;
        }
        Method method = domain.getClass().getMethod("contains", UUID.class);
        return Boolean.TRUE.equals(method.invoke(domain, owner));
    }
}
