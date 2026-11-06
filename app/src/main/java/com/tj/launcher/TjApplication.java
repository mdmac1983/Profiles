package com.tj.launcher;

import android.app.Application;

/** Installs the crash-loop breaker before anything else of ours runs. Does nothing else at start-up (no receivers, no services, no work at boot) except one content observer on battery saver (AC3). */
public class TjApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        try { CrashGuard.install(this); } catch (Throwable ignored) { }
        try { CrashGuard.clearIfNewer(this); } catch (Throwable ignored) { }   // CBR1: a tripped breaker is cleared by the first start of a NEWER version (a fair try)
        try { AnimationSpeedup.restoreIfPending(this); } catch (Throwable ignored) { }   // an interrupted reset must never leave the animations off
        try { if (getPackageName().equals(Application.getProcessName())) SwitchMigration.run(this); } catch (Throwable ignored) { }   // v0.7.3: undo the old breaker reset's "false" values once (all three switches stored as off = its signature)
        try { if (getPackageName().equals(Application.getProcessName())) EntranceWatcher.start(this); } catch (Throwable ignored) { }   // AD4: the rotation-tile and charger entrances (the page flick is fed by the Launcher touch hook)
        try { if (getPackageName().equals(Application.getProcessName())) BatterySaverGuard.start(this); } catch (Throwable ignored) { }   // AC3: watch battery saver for the life of the MAIN process (one observer, no polling; the short-lived :wallpaper_chooser process has no business reading the mode)
    }
}
