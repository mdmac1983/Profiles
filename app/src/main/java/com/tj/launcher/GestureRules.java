package com.tj.launcher;

/**
 * BT1e (v1.4, user 2026-11-09: gestures, "ok with suggested": FBOP + Rental: swipe down = notifications, swipe up = drawer, double tap = lock screen, each changeable;
 * none opens Owner). The pure rules (JVM-tested): which gesture a finished touch on the settled home screen was, and what each choice does. Swipe up stays Launcher3's own
 * drawer gesture (it is handled before we could see it). Release default as suggested, debug default "nothing" (the emulator sections tap the home screen).
 */
public final class GestureRules {
    public enum Gesture {
        SWIPE_DOWN("gesture_swipe_down", "Swipe down"), DOUBLE_TAP("gesture_double_tap", "Double tap"), TWO_FINGER_DOWN("gesture_two_down", "Two-finger swipe down");
        public final String key, title;
        Gesture(String key, String title) { this.key = key; this.title = title; }
    }

    public enum Action {
        NOTHING("none", "Nothing"), NOTIFICATIONS("notifications", "Notifications"), QUICK_SETTINGS("quick", "Quick settings"), LOCK("lock", "Lock the screen"),
        RECENTS("recents", "Recent apps"), DRAWER("drawer", "App drawer");
        public final String id, title;
        Action(String id, String title) { this.id = id; this.title = title; }
    }

    public static final long TAP_MAX_MS = 300, DOUBLE_TAP_GAP_MS = 350, SWIPE_MAX_MS = 700;

    private GestureRules() {}

    public static Action defaultFor(Gesture g, boolean debuggable) {
        if (debuggable) return Action.NOTHING;
        switch (g) { case SWIPE_DOWN: return Action.NOTIFICATIONS; case DOUBLE_TAP: return Action.LOCK; default: return Action.NOTHING; }
    }

    public static Action parse(String id, Action def) {
        if (id != null) for (Action a : Action.values()) if (a.id.equals(id)) return a;
        return def;
    }

    public static Action next(Action a) { Action[] v = Action.values(); return v[(a == null ? 0 : a.ordinal() + 1) % v.length]; }

    public static boolean isTap(float dx, float dy, long dt, int widthPx) {
        float slop = widthPx * 0.04f;
        return Math.abs(dx) < slop && Math.abs(dy) < slop && dt <= TAP_MAX_MS;
    }

    /**
     * A finger went up. {@code pointers} = the most fingers that were down; {@code prevTapUpMs} / {@code prevTapX,Y} = the previous single tap (0 = none).
     * Returns the gesture or null. A double tap needs the second tap close to the first one, in time and in place.
     */
    public static Gesture classify(float dx, float dy, long dt, int pointers, int widthPx, long upMs, float upX, float upY, long prevTapUpMs, float prevTapX, float prevTapY) {
        float down = widthPx * 0.15f;
        if (pointers >= 2) return dy > down && Math.abs(dx) < dy && dt <= SWIPE_MAX_MS ? Gesture.TWO_FINGER_DOWN : null;
        if (pointers != 1) return null;
        if (dy > down && Math.abs(dx) < dy / 2f && dt <= SWIPE_MAX_MS) return Gesture.SWIPE_DOWN;
        if (isTap(dx, dy, dt, widthPx) && prevTapUpMs > 0 && upMs - prevTapUpMs <= DOUBLE_TAP_GAP_MS
                && Math.abs(upX - prevTapX) < widthPx * 0.1f && Math.abs(upY - prevTapY) < widthPx * 0.1f) return Gesture.DOUBLE_TAP;
        return null;
    }
}
