package me.rexsystems.rexMinions.minion;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rexsystems.rexMinions.storage.ItemCodec;
import me.rexsystems.rexMinions.storage.MinionDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ActiveMinion {

    private final UUID id;
    private final UUID owner;
    private final String type;
    private final int level;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private ItemStack tool;
    private final List<ItemStack> storage = new ArrayList<>();
    private String linkWorld;
    private Integer linkX;
    private Integer linkY;
    private Integer linkZ;
    private double balance;
    private int cursor;
    private int viewers;
    private boolean removed;
    private ScheduledTask task;

    public ActiveMinion(MinionDatabase.StoredMinion row) {
        this.id = row.id();
        this.owner = row.owner();
        this.type = row.type();
        this.level = Math.max(1, row.level());
        this.world = row.world();
        this.x = row.x();
        this.y = row.y();
        this.z = row.z();
        this.yaw = row.yaw();
        this.tool = ItemCodec.decodeOne(row.tool());
        this.storage.addAll(ItemCodec.decode(row.storage()));
        this.linkWorld = row.linkWorld();
        this.linkX = row.linkX();
        this.linkY = row.linkY();
        this.linkZ = row.linkZ();
        this.balance = row.balance();
        this.cursor = Math.max(0, row.cursor());
    }

    public synchronized UUID id() {
        return id;
    }

    public synchronized UUID owner() {
        return owner;
    }

    public synchronized String typeId() {
        return type;
    }

    public synchronized int level() {
        return level;
    }

    public synchronized boolean removed() {
        return removed;
    }

    public synchronized boolean paused() {
        return removed || viewers > 0;
    }

    public synchronized void pause() {
        viewers = 1;
    }

    public synchronized void resume() {
        viewers = 0;
    }

    public synchronized void markRemoved() {
        this.removed = true;
        cancelTask();
    }

    public synchronized Location location() {
        World loaded = Bukkit.getWorld(world);
        if (loaded == null) {
            return null;
        }
        return new Location(loaded, x, y, z, yaw, 0);
    }

    public synchronized boolean chunkLoaded() {
        World loaded = Bukkit.getWorld(world);
        if (loaded == null) {
            return false;
        }
        int chunkX = (int) Math.floor(x) >> 4;
        int chunkZ = (int) Math.floor(z) >> 4;
        return loaded.isChunkLoaded(chunkX, chunkZ);
    }

    public synchronized ItemStack tool() {
        return tool == null ? null : tool.clone();
    }

    public synchronized void setTool(ItemStack tool) {
        this.tool = tool == null || tool.getType().isAir() ? null : tool.clone();
    }

    public synchronized List<ItemStack> storage() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack item : storage) {
            if (item != null && !item.getType().isAir()) {
                copy.add(item.clone());
            }
        }
        return copy;
    }

    public synchronized void setStorage(List<ItemStack> items) {
        storage.clear();
        if (items == null) {
            return;
        }
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                storage.add(item.clone());
            }
        }
    }

    public synchronized int storageSlotsUsed() {
        return storage.size();
    }

    public synchronized Location link() {
        if (linkWorld == null || linkX == null || linkY == null || linkZ == null) {
            return null;
        }
        World loaded = Bukkit.getWorld(linkWorld);
        if (loaded == null) {
            return null;
        }
        return new Location(loaded, linkX, linkY, linkZ);
    }

    public synchronized void setLink(Location location) {
        if (location == null || location.getWorld() == null) {
            linkWorld = null;
            linkX = null;
            linkY = null;
            linkZ = null;
            return;
        }
        linkWorld = location.getWorld().getName();
        linkX = location.getBlockX();
        linkY = location.getBlockY();
        linkZ = location.getBlockZ();
    }

    public synchronized double balance() {
        return balance;
    }

    public synchronized void addBalance(double amount) {
        this.balance = Math.max(0, this.balance + amount);
    }

    public synchronized int cursor() {
        return cursor;
    }

    public synchronized void setCursor(int cursor) {
        this.cursor = Math.max(0, cursor);
    }

    public synchronized void bindTask(ScheduledTask task) {
        cancelTask();
        this.task = task;
    }

    public synchronized void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public synchronized MinionDatabase.StoredMinion snapshot() {
        return new MinionDatabase.StoredMinion(
                id,
                type,
                level,
                world,
                x,
                y,
                z,
                yaw,
                owner,
                ItemCodec.encode(tool),
                ItemCodec.encode(storage),
                linkWorld,
                linkX,
                linkY,
                linkZ,
                balance,
                cursor
        );
    }
}
