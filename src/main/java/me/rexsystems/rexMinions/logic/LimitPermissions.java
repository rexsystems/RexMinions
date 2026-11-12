package me.rexsystems.rexMinions.logic;

import java.util.Collection;
import java.util.Locale;

public final class LimitPermissions {

    public static final String PREFIX = "rexminions.limit.";

    private LimitPermissions() {
    }

    public static int highest(Collection<String> nodes, int defaultLimit) {
        int highest = Integer.MIN_VALUE;
        boolean any = false;
        if (nodes != null) {
            for (String node : nodes) {
                Integer parsed = read(node);
                if (parsed == null) {
                    continue;
                }
                any = true;
                if (parsed > highest) {
                    highest = parsed;
                }
            }
        }
        if (!any || highest <= 0) {
            return defaultLimit;
        }
        return highest;
    }

    public static Integer read(String node) {
        if (node == null) {
            return null;
        }
        String trimmed = node.trim().toLowerCase(Locale.ROOT);
        if (!trimmed.startsWith(PREFIX)) {
            return null;
        }
        String suffix = trimmed.substring(PREFIX.length());
        if (suffix.isEmpty()) {
            return null;
        }
        for (int i = 0; i < suffix.length(); i++) {
            if (!Character.isDigit(suffix.charAt(i))) {
                return null;
            }
        }
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
