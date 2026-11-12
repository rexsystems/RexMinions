package me.rexsystems.rexMinions.logic;

public final class ClickGate {

    private ClickGate() {
    }

    public static boolean cancels(boolean rexMinionItem, boolean rexMinionTarget) {
        return rexMinionItem || rexMinionTarget;
    }
}
