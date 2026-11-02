package com.tj.launcher;

import java.util.List;
import java.util.Locale;

/** Pure rules for the return reset (JVM-testable). */
public final class ResetPolicy {
    private ResetPolicy() {}

    /** Packages that must never be killed: us, the stock launcher (Quickstep keeps recents/gestures alive), the keyboard, and core system UI/phone. */
    public static boolean shouldKill(String pkg, String selfPkg, String stockLauncherPkg, String imePkg) {
        if (pkg == null || pkg.isEmpty()) return false;
        if (pkg.equals(selfPkg) || pkg.equals(stockLauncherPkg) || pkg.equals(imePkg)) return false;
        return !(pkg.equals("android") || pkg.equals("com.android.systemui") || pkg.equals("com.android.phone")
                || pkg.equals("com.android.providers.telephony") || pkg.equals("com.android.server.telecom")
                || pkg.startsWith("com.android.inputmethod") || pkg.equals("com.mediatek.ims"));
    }

    /** Index of the first label containing the query (case-insensitive), preferring a label that STARTS with it; -1 if none. */
    public static int pickLabel(List<String> labels, String query) {
        if (query == null || query.trim().isEmpty()) return -1;
        String q = query.trim().toLowerCase(Locale.ROOT);
        int contains = -1;
        for (int i = 0; i < labels.size(); i++) {
            String l = labels.get(i) == null ? "" : labels.get(i).toLowerCase(Locale.ROOT);
            if (l.startsWith(q)) return i;
            if (contains < 0 && l.contains(q)) contains = i;
        }
        return contains;
    }
}
