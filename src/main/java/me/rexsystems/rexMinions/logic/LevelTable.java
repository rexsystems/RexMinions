package me.rexsystems.rexMinions.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LevelTable {

    public static final int RANGE_CAP = 32;

    public record Level(int level, int speedTicks, int range) {
        public Level {
            if (level < 1) {
                level = 1;
            }
            if (speedTicks < 1) {
                speedTicks = 1;
            }
            range = capRange(range);
        }
    }

    private final List<Level> levels;

    private LevelTable(List<Level> levels) {
        this.levels = levels;
    }

    public static LevelTable of(List<Level> levels) {
        List<Level> copy = new ArrayList<>();
        if (levels != null) {
            copy.addAll(levels);
        }
        copy.sort(Comparator.comparingInt(Level::level));
        return new LevelTable(List.copyOf(copy));
    }

    public Level lookup(int level) {
        if (levels.isEmpty()) {
            return new Level(Math.max(1, level), 100, 3);
        }
        Level lowerOrExact = null;
        for (Level candidate : levels) {
            if (candidate.level() <= level) {
                lowerOrExact = candidate;
            }
        }
        if (lowerOrExact != null) {
            return lowerOrExact;
        }
        return levels.getFirst();
    }

    public static int capRange(int range) {
        if (range < 1) {
            return 1;
        }
        return Math.min(range, RANGE_CAP);
    }

    public static int period(int speedTicks, int efficiencyLevel, int ticksPerLevel, int minimum) {
        int reduced = speedTicks - Math.max(0, efficiencyLevel) * Math.max(0, ticksPerLevel);
        int floor = Math.max(1, minimum);
        return Math.max(floor, reduced);
    }
}
