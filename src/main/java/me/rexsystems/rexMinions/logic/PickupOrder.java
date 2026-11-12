package me.rexsystems.rexMinions.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class PickupOrder {

    private PickupOrder() {
    }

    public static String handoff(boolean inventoryHasRoom) {
        return inventoryHasRoom ? "give" : "drop";
    }

    public static List<String> plan(boolean inventoryHasRoom) {
        List<String> steps = new ArrayList<>(2);
        steps.add("remove");
        steps.add(handoff(inventoryHasRoom));
        return List.copyOf(steps);
    }

    public static boolean execute(BooleanSupplier removeFirst, BooleanSupplier thenGiveOrDrop) {
        if (removeFirst == null || !removeFirst.getAsBoolean()) {
            return false;
        }
        return thenGiveOrDrop != null && thenGiveOrDrop.getAsBoolean();
    }
}
