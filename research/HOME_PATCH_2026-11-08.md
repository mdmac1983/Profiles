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

## BJ1 (2026-11-08 16:21 UTC): the open-menu FADE on the home screen
Tj, about v0.8.9 on the tablet (verbatim): "The home screen also must match the dimming just as the lock screen did. Right now the patch looks correct except the dimming speed makes the patch appear". This is the case BI1e had left open ("film it like pt phase D, do not guess").

**Method.** Emulator section `hf` (tools/emulator_scenario.sh): the custom home with a patterned wallpaper, the patch calibrated and resting, animations ON (the CI emulator runs with them off, and the stock scrim then appears in ONE frame, which hides the whole problem), `screenrecord` of 5 openings per treatment, `tools/pt_analyze.py` per film: `open_shape` = how well the patch's brightness follows the screen beside the menu, timing only (section `pt`'s yardstick for the lock screen: a film is good below 0.08, and 3 of 5 films must be). The first run compared three treatments of the burst's FIRST window event inside ONE build (a debug-only preference, since removed).

| treatment (run #382, head ae64eec) | patch opened by the fast path | median open_shape | films below 0.08 |
| :- | :- | :- | :- |
| v0.8.9 as released (filmed first and last) | 1 of 10 | 0.197 | 0 of 10 |
| the lock screen's wait for the dialog's own event; pencil NOT part of it (became the product) | 5 of 5 | 0.054 | 3 of 5 |
| everything waits, pencil too | 5 of 5 | 0.126 | 0 of 5 |

Verification of the product code (run #383, head 825ae3d, two groups of five): fast path 10 of 10, median 0.087 (0.087 / 0.092), first frame drawn 5-28 ms after the stock scrim began, 14-18 frames per fade, 0-2 later than 34 ms, worst gap 26-42 ms, animator scale 1.0.

**Cause (read from the code, then proven by the film).** The lock screen starts a wait on the burst's FIRST window event (`quietBurst` -> `deferWork(FAST_WAIT_MS)`) so that the stock dialog's own event (class `*ActionsDialog*`, 15-50 ms later) is handled first and opens the patch at once (`fastMenuOpen`). The home flavour skipped that wait on purpose (BI1: the quick-settings pencil must follow every SystemUI event within 40 ms, B6). So on the home the first event ran the node scan (56 ms) and the next event another (207 ms) that FOUND the menu itself: the patch opened by the SLOW path (`fade start lag=267 ms`), the dialog's own event waited 234 ms in the queue and the fast path declined ("already open"). The patch drew nothing while the thread was blocked, stayed at full brightness while the scrim went 984 -> 802 per mille (up to +24 % lighter than its surround), and dimmed only at the end of the stock fade: "the patch appears".

**Fix (v0.8.10).** `HomePatchRules.burstTreatment` (pure, JVM-tested) + the home branch of `quietBurst`: the first window event of a burst starts the lock screen's wait when the fast path will be taken (`fastDecline() == null`); the pencil is NOT part of the wait (it follows every event as before); the wait ends after `FAST_WAIT_MS` at the latest (fail-open: the scan then runs as it did). Regression guard: section `hf` check HF4 (fast path in >= 9 of 10 films; v0.8.9: 1) and HF5 (median open_shape of >= 9 read films below 0.14; v0.8.9: 0.197).

**What the film cannot say.** (1) Single films are noisy: the emulator's software renderer updates the stock scrim in ~100 ms steps while the small patch window updates every frame, so even a perfect patch reads 0.03-0.17 per film (the lock screen's own films read 0.017-0.096). Use the fast-path count and the median, never one film. (2) In the fast-path films the patch tends to be 40-60 ms AHEAD of the surround in the middle of the fade (5-9 % darker). There is no tablet evidence for that, so the lead (`PatchFade.LEAD_MS` 10 ms, Tj's 'perfect' on the lock screen) was NOT changed; `patch_fade_shift` ("Fade timing" in Launcher control) exists for a tablet that differs. (3) The animator scale: a stock fade at scale 0 is ONE frame and no 300 ms patch fade can match it. `AnimationSpeedup` holds the scales at 0 only in a Quickstep session (and behind the return reset's cover); the custom home runs real animations. v0.8.10 reports the scale of the last fade in the tablet report so this is read, not assumed.

**Tablet side.** The report line `last fade seen from the patch (BJ1)` (TjAccessibilityService.perfLine): home or lock, ms from the stock scrim's start to the patch's first frame, frames drawn, frames later than 34 ms after the one before, worst gap, animator scale. Debug builds also log `PATCH scan took N ms`, `PATCH pencil took N ms` and `PATCH fade end ...` (section hf prints them per film).
