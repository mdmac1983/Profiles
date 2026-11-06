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
    /** The package of windows that system_server itself owns (some ROMs host dialogs there). It is quiet, so it is kept rather than risk missing a system dialog. */
    public static final String ANDROID = "android";

    /** True for the events that cannot matter: a content change inside an app that is neither SystemUI nor the system's own windows. */
    public static boolean isNoise(int eventType, CharSequence packageName) {
        if (eventType != TYPE_WINDOW_CONTENT_CHANGED) return false;
        return packageName == null || !(SYSTEM_UI.contentEquals(packageName) || ANDROID.contentEquals(packageName));
    }

    /**
     * AN1 (Tj, 2026-11-06: "the battery saver tile is mostly unresponsive ... turns on very slow ... I'm almost certain this has something to do with the launcher or adb or accessibility"). Every node search the helper starts
     * is executed by SystemUI's own UI thread, the very thread that handles the tap on a quick-settings tile. While the shade is open SystemUI sends BURSTS of content changes (tiles redrawing, a ripple, the clock) and each
     * one started the power-menu scan (two whole-tree text searches in every system window) plus the pencil search: dozens of tree walks a second on a 2 GB MT8168. A content change cannot OPEN the power menu (that is a
     * new window: the window events start the scan and the next 300 ms are scanned in every window); it matters only while a menu is already up. Pure, so the rule is JVM-tested.
     */
    public static boolean needsPowerMenuScan(int eventType, boolean menuKnownUp, boolean inFullScanWindow) {
        if (eventType != TYPE_WINDOW_CONTENT_CHANGED) return true;   // window changes and clicks always start the scan, exactly as before
        return menuKnownUp || inFullScanWindow;
    }

    /** AN1: the shortest gap between two FULL pencil searches (a whole-tree walk in SystemUI's shade window): 40 ms after a window change or a click, 200 ms inside a burst of content changes. Following a pencil that is already known is one node, not a tree, and is not limited by this. */
    public static long minPencilSearchGapMs(int eventType) { return eventType == TYPE_WINDOW_CONTENT_CHANGED ? 200L : 40L; }

    /** android.view.accessibility.AccessibilityWindowInfo types */
    public static final int WINDOW_TYPE_APPLICATION = 1, WINDOW_TYPE_INPUT_METHOD = 2;

    /** android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED / TYPE_WINDOWS_CHANGED: a window appeared, changed or went away. */
    public static final int TYPE_WINDOW_STATE_CHANGED = 0x00000020, TYPE_WINDOWS_CHANGED = 0x00400000;

    /** True when this event can mean "a new window just showed up" (a power menu is a new window). Right after one, the search looks in EVERY window like before; only the quiet follow-up checks are narrowed. */
    public static boolean windowsJustChanged(int eventType) {
        return (eventType & (TYPE_WINDOW_STATE_CHANGED | TYPE_WINDOWS_CHANGED)) != 0;
    }

    /** True for a window that belongs to the system, not to an app or the keyboard (status bar, shade, dialogs of SystemUI, volume, global actions: AccessibilityWindowInfo.TYPE_SYSTEM and friends). */
    public static boolean isSystemWindow(int windowType) {
        return windowType != WINDOW_TYPE_APPLICATION && windowType != WINDOW_TYPE_INPUT_METHOD;
    }

    /**
     * Which windows are searched for the power menu? When a window titled "ActionsDialog" (AOSP's power dialog) exists, only that one. Right after a window change ({@code windowsJustChanged}), every window,
     * exactly as before this optimisation (so an unusual power menu on another Android build is still found). Otherwise (clicks, scrolls inside SystemUI, the 1.5 s re-check) only system windows:
     * never the foreground app's window, whose tree can be large and whose UI thread would run the search.
     */
    public static boolean mayHoldPowerMenu(boolean someWindowIsTheActionsDialog, boolean thisWindowIsTheActionsDialog, int windowType, boolean windowsJustChanged) {
        if (someWindowIsTheActionsDialog) return thisWindowIsTheActionsDialog;
        if (windowsJustChanged) return true;
        return isSystemWindow(windowType);
    }
}
