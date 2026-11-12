package me.rexsystems.rexMinions.minion;

import me.rexsystems.rexMinions.RexMinions;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class Keys {

    public final NamespacedKey minion;
    public final NamespacedKey type;
    public final NamespacedKey level;
    public final NamespacedKey id;
    public final NamespacedKey looting;

    public Keys(RexMinions plugin) {
        this.minion = new NamespacedKey(plugin, "minion");
        this.type = new NamespacedKey(plugin, "type");
        this.level = new NamespacedKey(plugin, "level");
        this.id = new NamespacedKey(plugin, "id");
        this.looting = new NamespacedKey(plugin, "looting");
    }

    public boolean isMinionItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        String value = item.getItemMeta().getPersistentDataContainer().get(type, PersistentDataType.STRING);
        return value != null && !value.isBlank();
    }

    public boolean isMinionEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        Byte mark = entity.getPersistentDataContainer().get(minion, PersistentDataType.BYTE);
        return mark != null && mark == (byte) 1;
    }

    public String itemType(ItemStack item) {
        if (!isMinionItem(item)) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(type, PersistentDataType.STRING);
    }

    public int itemLevel(ItemStack item) {
        if (!isMinionItem(item)) {
            return 1;
        }
        Integer value = item.getItemMeta().getPersistentDataContainer().get(level, PersistentDataType.INTEGER);
        return value == null || value < 1 ? 1 : value;
    }

    public String entityId(Entity entity) {
        if (!isMinionEntity(entity)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(id, PersistentDataType.STRING);
    }
}
