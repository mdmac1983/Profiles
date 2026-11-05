package com.tj.launcher;

import android.app.Application;

/** Installs the crash-loop breaker before anything else of ours runs. Does nothing else at start-up (no receivers, no services, no work at boot) except one content observer on battery saver (AC3). */
public class TjApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        try { CrashGuard.install(this); } catch (Throwable ignored) { }
        try { CrashGuard.clearIfNewer(this); } catch (Throwable ignored) { }   // CBR1: a tripped breaker is cleared by the first start of a NEWER version (a fair try)
        try { AnimationSpeedup.restoreIfPending(this); } catch (Throwable ignored) { }   // an interrupted reset must never leave the animations off
        try { BatterySaverGuard.start(this); } catch (Throwable ignored) { }   // AC3: watch battery saver for the life of the process (one observer, no polling)
    }
}
