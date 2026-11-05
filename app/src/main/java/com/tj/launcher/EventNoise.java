package com.tj.launcher;

/**
 * AB2 (efficiency review for the ARMv7 / MT8168 / 2 GB tablet, Tj 2026-11-05). The accessibility helper receives EVERY content-change event of EVERY app (a game or a scrolling list sends hundreds a
 * second), and each one used to start the power-menu scan: two whole-view-tree text searches in every window, which makes the foreground app's own UI thread walk its views. Nothing in another
 * app's content change can open the power menu, the shade or the PIN pad, so those events are dropped right at the door; window changes, clicks and SystemUI's own events still pass.
 * Pure (no Android classes) so the rule is JVM-tested.
 */
public final class EventNoise {
    private EventNoise() {}

    /** android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED */
    public static final int TYPE_WINDOW_CONTENT_CHANGED = 0x00000800;
    public static final String SYSTEM_UI = "com.android.systemui";

    /** True for the events that cannot matter: a content change inside an app that is not SystemUI. */
    public static boolean isNoise(int eventType, CharSequence packageName) {
        if (eventType != TYPE_WINDOW_CONTENT_CHANGED) return false;
        return packageName == null || !SYSTEM_UI.contentEquals(packageName);
    }

    /** android.view.accessibility.AccessibilityWindowInfo types */
    public static final int WINDOW_TYPE_APPLICATION = 1, WINDOW_TYPE_INPUT_METHOD = 2;

    /**
     * Which windows are searched for the power menu? When a window titled "ActionsDialog" (AOSP's power dialog) exists, only that one. Otherwise every window except app windows and the keyboard
     * (the menu is a system window). Never the foreground app's window: its tree can be large and the search runs on its UI thread.
     */
    public static boolean mayHoldPowerMenu(boolean someWindowIsTheActionsDialog, boolean thisWindowIsTheActionsDialog, int windowType) {
        if (someWindowIsTheActionsDialog) return thisWindowIsTheActionsDialog;
        return windowType != WINDOW_TYPE_APPLICATION && windowType != WINDOW_TYPE_INPUT_METHOD;
    }
}
