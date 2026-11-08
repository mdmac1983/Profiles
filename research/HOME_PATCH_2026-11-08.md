# The lock-screen patch for the UNLOCKED HOME SCREEN (TASKS BI1, 2026-11-08)

Tj, ~04:00 UTC (verbatim): "For the custom launcher, do the same logic and wallpaper patch that you did for the lock screen, but implement it for the unlocked home screen."
Scope: the unlocked custom HOME screen only. The drawer and other apps keep today's replacement panel.

## What the lock-screen patch is (read from the code, line numbers of `TjAccessibilityService`)
A small `TYPE_ACCESSIBILITY_OVERLAY` window (the "slot window": the learned stock panel's left/right + 10 dp, from the Screenshot entry's top - 8 dp down to the panel bottom + 32 dp; about 132 x 145 px on the tablet) holds REAL pixels cut once from a system screenshot. It RESTS over the slot from before the menu opens (`patchShow`, :2877), flags NOT_FOCUSABLE | NOT_TOUCHABLE | WATCH_OUTSIDE_TOUCH, TRANSLUCENT (:773-777). When the stock dialog's window event arrives (`fastMenuOpen` :123, class `*ActionsDialog*`) it switches to its OPEN look, which follows the stock 300 ms scrim fade (`PatchFade`) with the scrim MEASURED on the tablet (`ScrimFit`, applied by `setOpenLook` :3082). A finger going down anywhere hides it (ACTION_OUTSIDE -> `onOutsideTouch` :2819). Edges are feathered by two masks (`buildPatchMasks` :817). The stock panel is NOT replaced: its first two entries stay stock and the patch covers the third. The pixel source is `PatchShot` (a crop = window + 24 px, stored per rotation). Card or no card: `cardWanted()` :389 (lock = cardless; home and apps = our replacement card).

## What is the same on the home screen (no new work)
* The stock dialog and its panel geometry: the learned rect (`power_card_rect_r<rot>`, `power_geo_r<rot>`) is stored by `evaluateScan` in the cover world, which includes the custom home, and is shared with the lock screen.
* The window, its flags, the masks, the open / close fade, the tap on the hidden spot, the scrim measurement (the stored `ScrimFit` slot is keyed by the lock wallpaper signature and is applied to anything under the dialog).

## What is different (the whole project)
1. **The pixels behind the slot are not a still image.** On the home screen they are the SYSTEM WALLPAPER (behind a transparent Launcher window, `windowShowWallpaper`) plus whatever the Launcher draws on top. At the tablet's 5 x 8 grid (cells 114 x 114 px) the slot (x about 465-600, y about 530-680) holds the lower part of one icon and its label in the right-hand column (stock layout: WiFiToggle, icon y 501-555, label y 574-590) and the leaf wallpaper. The Launcher draws NO scrim there in the normal state (its bottom gradient starts at y 811, the top one ends at 100 dp).
2. **Pixel source = two layers, composited exactly like SurfaceFlinger does (SRC_OVER, premultiplied):**
   * WALLPAPER layer: ONE real wallpaper-only crop, calibrated once per (wallpaper id, display size, wallpaper x offset step). The Launcher hides its root view for the capture, the system takes a screenshot (`GLOBAL_ACTION_TAKE_SCREENSHOT`, the lock calibration's own mechanism: MediaStore observer, owner check, crop = window + 24 px). One camera flash per wallpaper (Tj knew, BG2b).
   * LAUNCHER layer: `PixelCopy` of the Launcher's own window (same process: `LauncherAppState.getInstanceNoCreate().getModel().getCallback()`), the slot + margin only: exactly what the Launcher draws (GPU text, icon, shadows), with alpha. Fallback if PixelCopy fails: the patch stays away (card).
3. **It must follow the home screen's state.** A resting patch over a live UI is only correct while nothing under it changes, so it is ONLY visible when: custom mode, power mode `hide_screenshot`, home resumed and in state NORMAL, unlocked and screen on, no finger down for 700 ms (`TjHooks.onTouch` hides it in the same frame as the touch; ACTION_OUTSIDE is the second guard), no draw of the Launcher's view tree for 250 ms (a `ViewTreeObserver.OnDrawListener` on the DragLayer: a catch-all for scrolls, drags, folders, binds, press feedback), no floating view open, and a composite built AFTER the last change is ready. Any failure of any condition = the patch is away = `cardWanted()` is true = today's replacement card (unchanged behaviour, never worse than v0.8.8).
4. **Pages.** The wallpaper scrolls only with 2 or more pages (`WallpaperOffsetInterpolator`: x = 0 with one page, steps of 1/(max(4,pages)-1)). The wallpaper crop is keyed by the offset step; a page without a crop uses the card and is calibrated the first time it rests (one flash per distinct step).

## Decisions
* Switch `home_patch` (Launcher control, release ON, debug OFF like the other behaviour switches); the section `hp` sets it explicitly.
* `patchLockMode` already means "the cardless real-pixel patch" in 25 of its 34 uses; the home patch is a second kind of the same window (`patchHomeMode`), so the perfected open / close / fade / touch code is reused unchanged and every lock-specific rule (PIN pad, keyguard, lock calibration, `lockPatchAlways`) stays lock-only. With `patchHomeMode` false nothing changes on the lock screen (the lock sections v, ae, ps, pa, pt, x1, ad and the rest of the core run in the gate).
* `wallpaper` power mode (the old opaque MODEL patch + card on home) is untouched.
* The calibration screenshot stays in the gallery (Android 10 does not let the app delete it; same as the lock calibration).

## Open questions for Tj (asked in the reply, none blocks the build)
* How many home-screen pages does he use (more than one = one extra calibration per page position)?
* Which power-menu mode is selected (the report's `power_mode`)?

## Build order (each step committed + checkpointed, JVM tests for every pure rule)
1. Pure rules `HomePatchRules` (rest / quiet / away / key / offset step / calibration decision / SRC_OVER compose) + tests.
2. `HomeShot` store + `HomeLayer` (PixelCopy + compose).
3. Hooks: `TjHooks` (draw listener, touch, changed) + the Launcher fork registration.
4. Service integration (`patchTarget`, `addPatch`, `patchShow`, `onOutsideTouch`, `cardWanted`, calibration flow).
5. Launcher control (switch, status line, "Calibrate the home patch now") + tablet report lines.
6. Emulator section `hp`; `/code-review high`; tiered gate; ship (v0.8.9). The v0.8.8 shade bug (TASKS BH1e) is fixed in the same release.
