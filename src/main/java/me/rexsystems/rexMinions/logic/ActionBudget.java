package me.rexsystems.rexMinions.logic;

public final class ActionBudget {

    private final int cap;
    private int used;

    public ActionBudget(int cap) {
        this.cap = Math.max(0, cap);
    }

    public boolean tryTake() {
        if (used >= cap) {
            return false;
        }
        used++;
        return true;
    }

    public boolean exhausted() {
        return used >= cap;
    }

    public int used() {
        return used;
    }

    public int cap() {
        return cap;
    }
}
