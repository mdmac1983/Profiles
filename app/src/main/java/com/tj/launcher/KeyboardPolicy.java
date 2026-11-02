package com.tj.launcher;

import java.util.List;
import java.util.Locale;

/** Pure rules for the keyboard switch (JVM-testable). Only ever picks from keyboards that are ALREADY ENABLED, so a switch can never leave the tablet without a usable keyboard. */
public final class KeyboardPolicy {
    public static final class Ime {
        public final String id, pkg;
        public final boolean system;
        public Ime(String id, String pkg, boolean system) { this.id = id; this.pkg = pkg; this.system = system; }
    }

    private KeyboardPolicy() {}

    /** The stock keyboard: first enabled SYSTEM keyboard, preferring the AOSP / Google ones. */
    public static String pickStock(List<Ime> enabled) {
        Ime best = null; int bestScore = -1;
        for (Ime i : enabled) {
            if (!i.system) continue;
            int score = 1;
            String p = i.pkg == null ? "" : i.pkg.toLowerCase(Locale.ROOT);
            if (p.contains("inputmethod.latin")) score += 3;
            if (p.startsWith("com.android") || p.startsWith("com.google")) score += 1;
            if (score > bestScore) { bestScore = score; best = i; }
        }
        return best == null ? null : best.id;
    }

    public static String pickSwiftKey(List<Ime> enabled) {
        for (Ime i : enabled) if (i.pkg != null && i.pkg.toLowerCase(Locale.ROOT).contains("swiftkey")) return i.id;
        return null;
    }

    /** Which keyboard to use for a mode; null = leave things alone. Quickstep mode returns SwiftKey, or the keyboard that was the default before we ever switched. */
    public static String target(String mode, List<Ime> enabled, String originalId) {
        if (Mode.QUICKSTEP.equals(mode)) {
            String sk = pickSwiftKey(enabled);
            if (sk != null) return sk;
            return originalId != null && containsId(enabled, originalId) ? originalId : null;
        }
        return pickStock(enabled);
    }

    private static boolean containsId(List<Ime> l, String id) { for (Ime i : l) if (id.equals(i.id)) return true; return false; }
}
