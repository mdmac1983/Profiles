package com.tj.launcher;

/**
 * AZ1 (Tj, 2026-11-06 20:40: "the wallpaper patch on the lock screen is almost perfect, but there is a slight flicker when I press the side key to open the power menu. I think this is when it is darkening to
 * match the darker lock screen when the power menu displays ... can the darkening to match the background be faster or fade so it doesn't appear like a flicker?").
 *
 * What the stock menu does (Android 10 source, research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md): {@code GlobalActionsDialog.ActionsDialog.show()} fades its OWN background scrim in over
 * {@link #OPEN_MS} ms with {@code Interpolators.FAST_OUT_SLOW_IN} = {@code PathInterpolator(0.4, 0, 0.2, 1)}; the scrim alpha is {@code 0.2 * curve(t)}. It is not a window dim, so nothing can switch it off
 * from outside, and the animator-duration setting would only make it instant. Our patch is an accessibility overlay ABOVE that dialog and must reproduce the dim itself. Switching it in ONE step (v0.8.0) made the
 * patch darker than its still-bright surround for ~200 ms, and lighter than it for as long as the node scan delayed the redraw: that is the flicker.
 * The cure is to follow the SAME curve from the SAME start. The start is the accessibility event of the new dialog window (its {@code getEventTime()} is {@code SystemClock.uptimeMillis()} of the sender, the
 * same clock as ours), so whatever lag there is between the dialog and our reaction is skipped in the curve instead of showing as a step: at the first frame the patch already has the brightness of the
 * surround. Pure functions, JVM-tested; {@link TjAccessibilityService} draws.
 */
public final class PatchFade {
    /** The stock dialog's fade-in (ViewPropertyAnimator.setDuration(300)). */
    public static final long OPEN_MS = 300;
    /**
     * Added to the elapsed time of every frame (the patch darkens that much EARLIER). Theory: the dialog's window-state event is created in the first traversal of its window, the same frame in which the scrim animation
     * starts, and both the patch and the scrim are composed through the same pipeline, so about 0 (a vsync at most). Measured on the Android 10 emulator, section "pt" (run 249, animations on, the event handled
     * 2-15 ms after its creation; worst patch-vs-surround mismatch while the menu opens): lead -25 ms: 3.3 % (patch a little late), 0: 2.8 % (late), 25: 4.2 % (early, then late), 50: 5.6 % (early), 80: 7.7 % (early);
     * the old one-step dim: 26 %. 10 sits between the two best; Launcher control has a +-100 ms nudge for a tablet that differs.
     */
    public static final long LEAD_MS = 10;
    /** A window event older than this is not the one that opened the menu: its scrim is complete, so there is nothing to follow (the patch goes straight to its final brightness, as before). */
    public static final long FRESH_MS = 600;
    /** The node scan of a freshly opened menu waits this long (it blocks the thread that must redraw the patch every frame): the fade plus a margin. */
    public static final long SETTLE_MS = OPEN_MS + 30;

    private PatchFade() {}

    /** {@code Interpolators.FAST_OUT_SLOW_IN}: y of the cubic Bezier (0,0) (0.4,0) (0.2,1) (1,1) at x, for x in 0..1 (the x(s) of that curve is monotonic, so a bisection finds s). */
    public static float fastOutSlowIn(float x) {
        if (x <= 0f) return 0f;
        if (x >= 1f) return 1f;
        double lo = 0, hi = 1, s = 0.5;
        for (int i = 0; i < 30; i++) {
            s = (lo + hi) / 2;
            double u = 1 - s;
            double bx = 3 * u * u * s * 0.4 + 3 * u * s * s * 0.2 + s * s * s;
            if (bx < x) lo = s; else hi = s;
        }
        return (float) (3 * (1 - s) * s * s + s * s * s);
    }

    /** How much of the stock scrim is there {@code elapsedMs} after the dialog began to fade in: 0 = none, 1 = complete. */
    public static float openProgress(long elapsedMs) {
        if (elapsedMs <= 0) return 0f;
        if (elapsedMs >= OPEN_MS) return 1f;
        return fastOutSlowIn(elapsedMs / (float) OPEN_MS);
    }

    /**
     * How long ago the dialog began to fade in, judged from the newest window event: {@code now - eventMs} when that event is fresh, else {@link #OPEN_MS} (= complete, no fade). {@code eventMs} 0 = no event seen.
     */
    public static long lagMs(long nowMs, long eventMs) {
        if (eventMs <= 0 || nowMs < eventMs) return OPEN_MS;
        long lag = nowMs - eventMs;
        return lag > FRESH_MS ? OPEN_MS : lag;
    }

    /** True while a frame drawn {@code nowMs} still has something to follow: the elapsed time (plus the lead) has not reached the end of the stock fade. */
    public static boolean fading(long nowMs, long startMs) { return fading(nowMs, startMs, LEAD_MS); }
    public static boolean fading(long nowMs, long startMs, long leadMs) { return nowMs + leadMs - startMs < OPEN_MS; }

    /** The progress to draw at {@code nowMs} for a fade that started at {@code startMs} (uptime ms; may lie in the past): the lead is added so the frame is right when it is SEEN. */
    public static float progressAt(long nowMs, long startMs) { return progressAt(nowMs, startMs, LEAD_MS); }
    public static float progressAt(long nowMs, long startMs, long leadMs) { return openProgress(nowMs + leadMs - startMs); }

    /** The brightness between the resting value and the open value: {@code progress} 0 = resting, 1 = open (the stock scrim complete). */
    public static float mix(float kRest, float kOpen, float progress) { return kRest + (kOpen - kRest) * progress; }
}
