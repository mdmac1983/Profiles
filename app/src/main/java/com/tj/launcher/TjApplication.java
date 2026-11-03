package com.tj.launcher;

import android.app.Application;

/** Installs the crash-loop breaker before anything else of ours runs. Does nothing else at start-up (no receivers, no services, no work at boot). */
public class TjApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        try { CrashGuard.install(this); } catch (Throwable ignored) { }
        try { AnimationSpeedup.restoreIfPending(this); } catch (Throwable ignored) { }   // an interrupted reset must never leave the animations off
    }
}
