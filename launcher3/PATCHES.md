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
- `Launcher.java`: `onResume`/`onPause` call `TjHooks` (foreground tracking);
  `onNewIntent`'s `alreadyOnHome` is also true when the router passes `EXTRA_HOME_WHILE_ON_HOME` (the invisible router answers Home, so Launcher has already lost focus).
- Manifest: `Launcher` no longer has the HOME category — `com.tj.launcher.RouterActivity` is the only HOME activity (fails open to the stock launcher).
- `res/values/strings.xml`: `app_name` Launcher3 -> Launcher.
- `go/res/xml/device_profiles.xml`: single grid 5 columns x 7 rows + 5-icon dock (measured from Tj's stock-launcher photos, 170 dpi), icon 60dp, text 14sp (AOSP Go values; measured against the stock photo: stock tile 55 px); `res/xml/default_workspace_5x7.xml` (copy of the 5x5 default, workspace icons on columns 0/2/4, rows 0/2).
- `config/BaseFlags.java`: `QSB_ON_FIRST_SCREEN = false` (stock has no home search bar).
- `res/values/styles.xml`: `allAppsScrimColor` #EAFFFFFF -> #80FFFFFF (stock drawer measured from Tj's photos: 50% white over the wallpaper, least-squares fit, residual 8).
- No dock (Tj, 2026-11-02): `com.tj.launcher.TjLook.NO_DOCK`; `DeviceProfile` sets `hotseatBarSizePx = 0`; `Hotseat.setInsets` hides the view; `ScrimView.updateDragHandleVisibility` no longer shows the "^" caret when accessibility is on; grid is now 5 columns x 8 rows (`go/res/xml/device_profiles.xml`, `res/xml/default_workspace_5x8.xml` without hotseat entries).
- (removed 2026-11-04, N3a: the `Launcher.dispatchTouchEvent` override / two-finger twist gesture is gone; Launcher3 touch handling is stock again. Quickstep is reachable only by an unlock handshake.)
- Hidden apps (Tj, 2026-11-02): `AppFilter.shouldShowApp` -> `com.tj.launcher.HiddenApps.isShown`; `SessionCommitReceiver.queueAppIconAddition` and `AllAppsList.addPromiseApp` skip not-allowed packages; `PackageUpdatedTask` OP_REMOVE forgets the package (reinstall = new = hidden until allowed).
- `BaseDraggingActivity.startActivitySafely`: `RecentsPolicy.apply` adds FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to every launch except the target app (Solitaire), so Recents shows only Solitaire.
- `WallpaperOffsetInterpolator.setOffsetSafely`: calls `TjHooks.onWallpaperOffset` (W3b: the wallpaper patch over the power-menu area needs the current wallpaper x offset).
- `Launcher.onCreate`: `TjHooks.watchState(mStateManager)` (D0: the power-menu wallpaper patch is shown only while the home screen is in the NORMAL state, not during All Apps / transitions, not over other apps).
