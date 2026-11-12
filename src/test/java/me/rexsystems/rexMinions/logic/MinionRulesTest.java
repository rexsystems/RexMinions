package me.rexsystems.rexMinions.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinionRulesTest {

    @Test
    void lookupSpeedAndRange() {
        LevelTable table = LevelTable.of(List.of(
                new LevelTable.Level(1, 80, 4),
                new LevelTable.Level(2, 50, 7),
                new LevelTable.Level(3, 30, 9)
        ));

        LevelTable.Level two = table.lookup(2);
        assertEquals(50, two.speedTicks());
        assertEquals(7, two.range());

        LevelTable.Level above = table.lookup(5);
        assertEquals(30, above.speedTicks());
        assertEquals(9, above.range());
    }

    @Test
    void rangeCappedAt32() {
        LevelTable table = LevelTable.of(List.of(new LevelTable.Level(1, 20, 100)));
        assertEquals(32, table.lookup(1).range());
        assertEquals(32, LevelTable.capRange(80));
        assertEquals(32, LevelTable.RANGE_CAP);
    }

    @Test
    void actionCapStopsAfterN() {
        ActionBudget budget = new ActionBudget(3);
        assertTrue(budget.tryTake());
        assertTrue(budget.tryTake());
        assertTrue(budget.tryTake());
        assertFalse(budget.tryTake());
        assertTrue(budget.exhausted());
        assertEquals(3, budget.used());
    }

    @Test
    void permissionLimitUsesHighestN() {
        assertEquals(8, LimitPermissions.highest(List.of(
                "rexminions.limit.3",
                "rexminions.use",
                "rexminions.limit.8",
                "rexminions.limit.1"
        ), 2));
        assertEquals(5, LimitPermissions.highest(List.of("rexminions.limit.0"), 5));
        assertEquals(6, LimitPermissions.highest(List.of("rexminions.limit.0", "rexminions.limit.6"), 9));
        assertEquals(4, LimitPermissions.highest(List.of("rexminions.admin"), 4));
    }

    @Test
    void pickupIsRemoveThenGive() {
        assertEquals(List.of("remove", "give"), PickupOrder.plan(true));
        assertEquals(List.of("remove", "drop"), PickupOrder.plan(false));

        List<String> order = new ArrayList<>();
        assertTrue(PickupOrder.execute(
                () -> {
                    order.add("remove");
                    return true;
                },
                () -> {
                    order.add("give");
                    return true;
                }
        ));
        assertEquals(List.of("remove", "give"), order);

        List<String> blocked = new ArrayList<>();
        assertFalse(PickupOrder.execute(
                () -> {
                    blocked.add("remove");
                    return false;
                },
                () -> {
                    blocked.add("give");
                    return true;
                }
        ));
        assertEquals(List.of("remove"), blocked);
    }

    @Test
    void ordinaryClickIsNotCancelled() {
        assertFalse(ClickGate.cancels(false, false));
        assertTrue(ClickGate.cancels(true, false));
        assertTrue(ClickGate.cancels(false, true));
    }
}
