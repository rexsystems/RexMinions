package me.rexsystems.rexMinions.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class MinionHolder implements InventoryHolder {

    public enum Kind {
        MAIN,
        STORAGE
    }

    private final UUID minionId;
    private final Kind kind;
    private Inventory inventory;

    public MinionHolder(UUID minionId, Kind kind) {
        this.minionId = minionId;
        this.kind = kind;
    }

    public UUID minionId() {
        return minionId;
    }

    public Kind kind() {
        return kind;
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
