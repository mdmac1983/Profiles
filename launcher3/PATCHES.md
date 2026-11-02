# Deviations from AOSP Launcher3 android-10.0.0_r47 (Go flavor, no Quickstep)

Upstream: `bash tools/fetch_launcher3.sh /tmp/x` then diff `/tmp/x/Launcher3/<dir>` against `launcher3/<dir>`. Keep this list current; every patch is also a git commit.
Imported dirs: `src`, `src_ui_overrides`, `go` (minus `go/quickstep`), `res`, `protos`, `proto_overrides`, `iconloaderlib`, `src_plugins`, `plugin_core_src` (= SystemUI `plugin_core/src`, 8 files, replaces the unpublished `plugin_core.jar`).

## Manifest (`app/src/main/AndroidManifest.xml`, merged from upstream AndroidManifest-common.xml + AndroidManifest.xml + go/AndroidManifest.xml)
- Removed the `<permission>` declaration of `com.android.launcher.permission.INSTALL_SHORTCUT` (the stock launcher already declares it; a second declaration blocks install).
- Removed `RECEIVE_BOOT_COMPLETED` (safety rule: nothing runs at boot) and the backup agent (`allowBackup=false`).
- All provider authorities/permissions use `${packageName}` = `com.tj.launcher`, so they cannot collide with the stock `com.android.launcher3`.
- applicationId `com.tj.launcher`, resource namespace stays `com.android.launcher3` (the code imports `com.android.launcher3.R`).
- `launcher3/iconloaderlib/src_full_lib` is not compiled (unused by the app; needs hidden UserHandle.getIdentifier).

## Source patches (Launcher.java and resources)
- `Launcher.java`: `onResume`/`onPause` call `TjHooks` (foreground tracking); `dispatchTouchEvent` override calls `TjHooks.onTouch` (observation only, never consumes);
  `onNewIntent`'s `alreadyOnHome` is also true when the router passes `EXTRA_HOME_WHILE_ON_HOME` (the invisible router answers Home, so Launcher has already lost focus).
- Manifest: `Launcher` no longer has the HOME category — `com.tj.launcher.RouterActivity` is the only HOME activity (fails open to the stock launcher).
- `res/values/strings.xml`: `app_name` Launcher3 -> Launcher.
