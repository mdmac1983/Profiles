# Audit: hidden apps in the custom Recents, the slow unlock into custom, the patch flicker (BE1-BE3, 2026-11-07)

Tj, 20:17 UTC (v0.8.4 installed): the patch + power menu are "99% perfect"; a very quick patch flicker now and then when pulling up for the PIN, and ONE Screenshot flash;
audit the custom Recents ("at no time" a hidden app); drop Solitaire if "no apps at all" + "completely clear the recent apps list every time the custom launcher starts" is better;
make the custom launcher start from the lock screen "immediate or as close as possible", prepare invisibly while locked / screen off, Quickstep may be a little slower.
(Earlier, same topic: BC5/BC5b patch hide at the pull-up, BC4 screenshots, BD1 hidden apps in Recents, BD2 slow unlock with a long black screen.)

Nothing here was measured on the real tablet (no report was sent). Everything below is read from the code at 931884a (v0.8.4) plus the emulator numbers in `PERF_AUDIT.md`.

## 1. How a hidden app can reach the custom Recents (every route, and what stops it TODAY)

Android has ONE Recents list (one user). The custom launcher does not own it: the stock Quickstep `RecentsActivity` draws it. So "keep hidden apps out" can only mean "remove their TASKS before the screen is drawn".

| # | Route | What stops it today | Hole |
| :- | :- | :- | :- |
| R1 | An app opened in a Quickstep session (handshake unlock), then lock + plain PIN | `ReturnReset.cleanAtLock` (helper running: force-stop + `RMTASKS` by id, `TaskOrigin.finish`); no helper: `ReturnReset.run` at the unlock presses Close/Clear all in the real Recents behind a black cover (`closeLabels` = apps in `AppOrigin.pending`) | origin tracking is a HEURISTIC: an app whose window event was missed, or whose task already existed when the session began (`TaskOrigin.baseFor`), is "the custom account's" for ever; `doRecents = recents && (!tracked || !pendPkgs.isEmpty())` skips the Recents step when `pending` is empty; **BD1 (Telegram + Claude) is this hole** |
| R2 | A hidden app started in custom mode (notification tap, "Open with", share target, an alarm, an incoming-call screen) | nothing at all until Recents opens | the task stays in Recents for ever |
| R3 | A hidden app left from BEFORE v0.7.x / a restart (Android restores the persisted task list after a boot) | `RecentsFilter` (AP1) closes the card AFTER the Recents screen opened | the card is visible for ~0.15-0.7 s (first look 150 ms, then one card per 70 ms), and AP1 only matches the card by LABEL |
| R4 | The helper was not running at the lock (it is usually not: it needs one adb line after every boot) | the on-screen path (R1) | slow, and heuristic (see R1) |
| R5 | The Solitaire stand-in itself | by design the only decoy; its start costs a cold process start on a 2 GB device | the black cover is only lifted when Solitaire is FOREGROUND (`scheduleVerify`) = the long black screen of BD2 |

AP1 (`RecentsFilter`) is a second line, not a guarantee: it reacts to the Recents window, it works by label, it is visible.

## 2. Where the unlock time goes (no helper, a plain PIN after a Quickstep session = Tj's daily case)

`TjAccessibilityService.coverAtEnter` raises a black full-screen cover at the Enter tap when `ReturnReset.mayHaveWork`. After USER_PRESENT `ReturnReset.run` (visible path):
1. `AnimationSpeedup.begin` (settings writes), 2. shade: icon scan, `GLOBAL_ACTION_NOTIFICATIONS`, poll "Clear all", press, BACK (~0.5-1 s), 3. Recents: `GLOBAL_ACTION_RECENTS`, wait for the stock RecentsActivity, find cards, close them one at a time (~0.5-1.5 s),
4. `openApp`: start Solitaire (COLD on a 2 GB Go device: 1-3 s) and verify it three times at 450-500 ms, 5. only then `hideResetCover`.
`research/PERF_AUDIT.md` measured 3.3 s of this on the (much faster) emulator ("UF2"). The cover therefore stays up for the whole chain AND for a cold Solitaire start. With the helper (`cleanAtLock`) the unlock has nothing left to do: that path is already instant.

Facts that bound what is possible:
* The keyguard forbids Recents and the shade, so the on-screen clean-up cannot happen while locked or with the screen off. Without the helper something visible (the cover) must exist for as long as the automation runs.
* Task removal by id needs the shell uid (`IActivityTaskManager.removeTask`, the helper). `killBackgroundProcesses` kills processes, not Recents cards.
* The helper dies with every reboot; a self-restart needs adb over TCP (`service.adb.tcp.port` is empty on the tablet).

## 3. Decisions (Claude, 2026-11-07; Tj delegated the "is it better" question)

D1. **Drop the Solitaire stand-in by default ("clean start").** It was the cause of the cold-start black screen, it is a second task in Recents, and the custom home alone is enough under the keyguard (the lock-screen pre-arm starts the custom home; `windowIsLockLeftover` already ignores what the unlock reveals). Switch `clean_start` (default ON) in Launcher control; OFF restores the old Solitaire behaviour exactly.
D2. **"Completely clear the Recents list every time the custom launcher starts"** = at the START of a custom session: (a) a Quickstep session ends (plain unlock / double Home / return methods / boot), (b) nothing else. NOT at every unlock of a custom-only session and NOT at every process restart (the OS kills our process under memory pressure on this device: clearing then would close the apps Tj is using). Helper: every task except the protected ones, by id, plus a force-stop of their apps (notifications go with them). No helper: Recents "Clear all" (no label matching, nothing to mis-match) behind the cover.
D3. **A membership rule replaces origin tracking as the guarantee**: in custom mode every Recents task whose package is not in the custom drawer (`HiddenApps.isPackageShown`) is removed, whatever its origin: `RecentsSweep` (pure plan, JVM-tested). Triggers (helper running, invisible): at the lock, after a plain unlock, when the stock Recents window opens (before AP1 looks), when a non-drawer app leaves the foreground. Never the foreground task, never the protected packages (`ResetPolicy.shouldKill`).
D4. **No helper = best effort and honest**: AP1 gets a faster first look and closes every matching card in one pass; the long-term fix for "usually not running" is a persistent helper (not possible on a locked Android 10 without adb after a boot).
D5. **Quickstep may be slower, custom must not**: the pre-arm at the lock puts ONLY the custom home in front (no Solitaire); the handshake path is unchanged (~260 ms).

## 4. The patch (BE1)

Observed by Tj: (a) a very quick flicker "every once in a while" when pulling up for the PIN; (b) once, the Screenshot button for a split second.
Code reading (`TjAccessibilityService`):
* P1. The PIN hide (BC5) is event-driven: the first sign is an accessibility event of the keyguard's PIN view (a content-change / window event that SystemUI sends after the bouncer is shown) or the scan (~120 ms slower). The stock bouncer scrim (ScrimState.BOUNCER, ~220 ms) starts at the first MOVE of the drag. So the patch (a still picture of the PLAIN lock screen) is visible against a darkening wallpaper from the start of the scrim until the event arrives = the flicker; "every once in a while" = event latency varies with SystemUI load. FIX: hide on the touch itself: the resting patch window gets `FLAG_WATCH_OUTSIDE_TOUCH` and hides on `ACTION_OUTSIDE` (delivered at DOWN, before any MOVE, to a visible window in front of the touched one even when it is `NOT_TOUCHABLE`).
* P2. After the PIN hide the resting patch stays away for 2.5 s (`pinHiddenUntilMs`). `fastMenuOpen` then DECLINES (`fastDecline`: "the patch is not resting"), so a power menu opened in that window (side key right after dismissing the PIN pad, or a quick screen off / on) goes through the slow scan path = the Screenshot slot shows for the scan latency (100-300 ms). FIX: the fast path accepts "away only because of a time guard" and clears the guard.
* P3. Patch creation on a new lock session happens in the SCREEN_ON broadcast handler, AFTER `lockEval` (a PIN-pad scan: dozens of cross-process node searches), and `patchTarget()` decodes the full lock wallpaper (`loadLockWallpaper`) even when the stored real-pixel crop makes it unnecessary. A menu held open right at wake can beat the window. FIX: create the patch window while the screen is off (`lockPrepare`), do not require the wallpaper decode when the stored crop exists, create the patch before the scans on wake.
* P4. The re-show at +2.6 s runs `patchShow(overlayShown)` without asking whether the PIN pad is still up (`lastBouncer` is a scan result and can be false for a moment during the pad's animation). FIX: the return needs `!lastBouncer` AND a calm period after the last sign.
* Diagnostics for next time: `LockLog` lines "PATCH" for every hide (which sign, ms after the touch), every return, and for a menu that opens while the patch is not ready (state of every guard).

## 5. What waits on Tj / what cannot be done without him

* The tablet report right after a slow unlock (`tools/tablet-intake.sh`): to confirm how long the cover really stays up and whether the helper runs.
* Whether he accepts "Recents completely cleared at the start of every custom session" (D2); the switch exists.
* The helper: one adb line after every boot gives the instant unlock; without it the unlock after a Quickstep session still needs the cover for ~1-2 s (after this change; was 3-5 s).
