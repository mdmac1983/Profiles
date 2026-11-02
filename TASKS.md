# TASKS — the job list. Tick `[x]` only when written, tested, committed (name the test).

Tj's request, 2026-11-02 (verbatim): "In the launcher repo, I will be building an Android 10 go launcher apk. I want
it to use github actions for as much as it can contribute, and save progress and checkpoints to github. Set up the
foundation for the launcher repo. I want there to be a comprehensive checkpoint, ladder, and save system so that if
Claude is interrupted by usage, another session can pick up the work without loss of progress. Look at the other
repos and their checkpoint systems for this purpose and adopt the best system but modify it for the launcher repo.
Install and use any skills and plug-ins needed to help this project. Install or adopt the tools needed to build apks
with github actions, just like the other repos do."

## Foundation (2026-11-02)
- [x] Checkpoint ladder: capture_inbox (rung 0), autosave (1), ckpt (2), ship (3), Actions (4) — `tools/test_handoff.sh`
- [x] Cold-resume briefing with interrupted-session and stranded-branch detection — `tools/test_handoff.sh`
- [x] Hook installer scoped to this repo, merge-safe next to novig/Mirage hooks — `tools/test_handoff.sh`
- [x] Secret scan on commit; key redaction in INBOX — `tools/test_handoff.sh`
- [x] Android project skeleton: HOME activity, `AppList` logic — `AppListTest` (`bash tools/gradle.sh testDebugUnitTest`)
- [x] Local debug build + lint green (2.5 MB debug APK)
- [x] Workflows: `android.yml` (gates / preview / signed release), `ci.yml` (PR tests + lint), Dependabot
- [x] Project skill `android-go-launcher`; kotlin-concurrency-and-flow copied
- [x] **Run `android.yml` once on GitHub** (done 2026-11-02: run 41369698320 green in ~2.5 min, artifact launcher-preview-apk 813 KB) (dispatch, `full_build=false` then `true`) and confirm the preview APK artifact
      (kept for history)
- [ ] Tj decides the OPEN items in BRIEF.md (UI toolkit, features, device, keystore)
- [x] Keystore created and committed under signing/ (Tj waived secrecy, 2026-11-02); GitHub signs
- [x] Tj approved `tools/MIRROR_MAIN` = on (2026-11-02): every push also fast-forwards `main`
- [ ] Install the preview APK on the Android 10 Go device; set it as default home app; report what happens

## Features — Tj's request, 2026-11-02 (second message, verbatim summary of the spec; screenshots saved in research/uploads/)
Device: Secure generic Alps 711 tb8168p1_bsp ("About tablet: 711"), MT8168B, Mali-G52 MC2, modified Android 10 Go (API 29),
security patch 2021-07-05, fingerprint alps/vnd_tb8168p1_bsp/tb8168p1_bsp:10/QP1A.190711.020/1719850017:user/release-keys,
Treble, kernel 4.14.141, secure boot, LOCKED bootloader, build sys_mssi_t_32_ago-user.2024070111, custom build alps-mp-q0.mp3-V4.160,
stock launcher Quickstep v10, 600x1024 screen, 3-button nav, storage 96% used (1.32 GB free), 32-bit userspace (mssi_t_32).
Tj: "Build a custom launcher and send it to me as an apk file", default launcher + boots with Android, identical in look/function to
stock Quickstep 10 (may adopt the Quickstep code; Claude may download and analyse it), default Android 10 Go layouts/fonts/app drawer,
screenshots attached (home grid 3 cols, folders Connect/Games, long-press popup Home settings / Wallpapers, Home settings page,
quick settings with only Auto-rotate tile). Modifications:
- [ ] L1 special permissions grantable via adb (WRITE_SECURE_SETTINGS etc.); WITHOUT them everything else works, gated features just stay unavailable
- [ ] L2 hide apps I select from the app drawer AND the drawer search; they reappear when I switch back to Quickstep
- [ ] L3 app drawer shows ONLY a clone Settings app that I can customise (working settings, hide chosen ones); stock Settings back in Quickstep
- [ ] L4 disable the side-key power menu while the custom launcher is active; restore in Quickstep
- [ ] L5 permanently strip all quick-settings tiles except Auto-rotate (persist even in Quickstep unless tiles can be swapped safely without adb;
      else keep auto-rotate permanently and consider an overlay for extra functions in Quickstep mode)
- [ ] L6 newly installed apps do not appear in the custom drawer; they appear in Quickstep
- [ ] L7 stock Android keyboard while custom launcher active; auto-switch to Microsoft SwiftKey when back on Quickstep
- [ ] L8 custom gesture to go back to Quickstep (custom launcher is default; Quickstep only appears via the gesture)
- [ ] L9 separate gesture / kill switch: close all open apps, clear all notifications, Bluetooth off if on, Wi-Fi off if on, return to custom launcher at once
- [ ] L10 SAFETY (overrides everything): before ANY release be highly confident it breaks nothing; failsafe that removes every modification and
      restores Android to its pre-launcher state. A friend's launcher glitched the lock screen (could not log in) and brought back all quick-settings tiles:
      that must never happen. Must be revertible if anything goes wrong.
- [ ] L11 deliver an APK (GitHub Release link when signed; preview artifact until the keystore exists)


## Build plan (2026-11-02) — do in order; checkpoint after each
- [x] P1 (compiles + packages 2026-11-02: launcher3/ imported, Gradle module, protos, plugin_core, manifest merged; see launcher3/PATCHES.md; NOT yet run on a device/emulator) Port: fetch pinned sources, build `plugin_core` from SystemUI plugin source, protos via Gradle, stub/reflect @hide APIs until `l3goWithoutQuickstep` compiles in a new Gradle module (one-Gradle-at-a-time; use CI for long builds)
- [ ] P2 Look check: emulator API 29 screenshots vs research/uploads/2026-11-02 (home 3 cols, drawer 5 cols translucent + Search apps, folders, popup, Home settings)
- [~] P3 (router, Mode, StockLauncher, CrashGuard, RestoreStore, Features, rotation detector + ControlActivity DONE with JVM tests 2026-11-02; still TODO: manifest/source guard test, adb scripts, emulator scenarios) Safety core: RestoreStore, crash-loop breaker, Quickstep pass-through mode, Restore action, manifest/source guard test (no admin/keyguard/overlay/lock settings), tools/adb-grant.sh + tools/adb-restore.sh
- [x] P4 CI emulator job (emulator.yml, manual): router/mode flip/crash breaker/uninstall/manifest checks all PASS 2026-11-02 (run 41468097179)
- [x] P5 Release v0.2.0 published 2026-11-02 (signed by GitHub; https://github.com/tjshea90/Launcher/releases/tag/v0.2.0). Tj has NOT yet tested it on the tablet. Release v0.2 "safe core":
- [ ] P6 L2, L6, L3 -> v0.3;  P7 L7, L5, L9 each default OFF -> v0.4;  P8 L4 experimental -> v0.5

## Tj's answers, 2026-11-02 (verbatim)
1) "I can send adb commands at a later time, but I want as much as possible to work without adb, with an option to add adb features later"
2) "I want a two finger clockwise gesture to return to quickstep, and either a small killswitch button on screen or a two finger counter clockwise gesture for the kill switch, or pressing both volume buttons at the same time"
3) "try the flicker power menu but only hide the screenshot button. Make this an option I can quickly turn off if it doesn't work"
4) "it can be signed by github if possible, security isn't important"
Decisions that follow (see DESIGN section 8): no-adb path first (accessibility + notification-listener are enabled in Settings, not adb);
return from Quickstep = launcher's own app icon + volume combo; power menu = hide ONLY the Screenshot entry (my reading of 3; ask if wrong);
keystore committed under signing/ (Tj waived secrecy), workflow signs with it.
- [ ] G1 two-finger clockwise rotate on custom home/drawer -> Quickstep mode (pass-through)
- [ ] G2 kill switch: small on-screen button, two-finger counter-clockwise, both volume keys (accessibility); each independently switchable
- [ ] G3 power menu: hide only the Screenshot entry (accessibility overlay on that node), quick on/off switch (also reachable in Quickstep mode)
- [ ] G4 keyboard (L7): no-adb fallback = on mode change show the system keyboard picker / notification; adb grant makes it automatic
- [x] G5 GitHub signs releases: keystore generated once, committed under signing/, android.yml uses it

## Tj's v0.2.0 test report, 2026-11-02 (verbatim summary) — photos in research/uploads/2026-11-02b/
"It installed fine. My display size is default. ... 1.png and 2.png are what the stock launcher looks like. 3.png and 4.png are the launcher you made.
It must look and function identically as the stock launcher. For the power menu, I want to hide the screenshot button (picture attached) completely, but if not
possible, disable the power menu completely, until I switch back to quickstep. The recents button works in the new launcher. Notice the grid layout and
transparency of the stock launcher."
Measured from the photos: 1024x600, **170 dpi**, **font scale 1.2**, 7.2 in, 32-bit ARMv7 (armeabi-v7a), MT8168B 4x2.0 GHz, Mali-G52.
Stock home = 3 columns x 4 rows, NO search bar, NO dock/hotseat, white labels; stock drawer = 5 columns, compact rows, TRANSLUCENT (wallpaper visible), "Search apps" bar;
v0.2.0 = 4x4 grid + search bar + dock, 4-column tall-row opaque drawer.  Recents button: WORKS (R3 resolved). Power menu has Power off / Restart / Screenshot.
- [ ] LK1 grid: home 3x4, no QSB, no hotseat; drawer 5 columns with the stock row height; icon/text sizes at 170 dpi + font scale 1.2
- [ ] LK2 drawer transparency like stock (wallpaper visible through it)
- [ ] LK3 emulator scenario at 600x1024, density 170, font_scale 1.2 for side-by-side comparison with the STOCK photos
- [ ] LK4 first-run home layout like his (folders Connect/Games, same app order) — default_workspace XML or label-based importer (stock DB is not readable without system permission)
- [x] R3 recents button works with our router as Home (Tj, v0.2.0)
- [x] G3b power menu: Screenshot hidden completely on the emulator (v0.2.1, opt-in accessibility); block-whole-menu mode also built; needs Tj device test
- [x] LK1/LK2 measured (2026-11-02): stock drawer = 5 columns, tile 55 px (=> Go icon 60 dp at 170 dpi), scrim = 50% white (least-squares over 2 stock screenshots, residual 8); applied in go/res device_profiles + styles (v0.2.1 candidate)
- [ ] G3b result so far: emulator has the same AOSP power menu; first accessibility run did nothing (service probably unbound by the test's force-stop; no logs). Re-running with diagnostics + rebind. Tj's extra question: can a NEW power menu replace the system one (answer: yes via accessibility cover/replace, same dependency; no non-accessibility route on a locked Android 10)
- [ ] LK5 OPEN QUESTION for Tj: does the stock home have a dock (can he drop icons at the bottom edge)? Decides 8 rows/no dock (pitch 113 px like stock) vs 7 rows + dock (pitch 121 px, current). Stock row pitch measured 113 px, mine 121.
- [ ] LK6 first-run home layout like his (folders Connect/Games + app order) — needs package names; label-based importer
- [ ] L2 hide apps / L6 hide new installs / L3 clone Settings (v0.3), G2 kill switch (button, CCW twist, both volume keys), L7 keyboard switch, L5 tiles guard

## Tj, 2026-11-02 (after v0.2.1 on the tablet), verbatim summary
"It has no dock, use 8 rows." / "Go back and look at the photos of the stock launcher. Remember these photos for reference at all times, even in a new Claude session." /
"Review the screenshots attached here for how the new launcher looks. See if they are identical." / "is there a way to completely hide the screenshot button and just make that whole section look identical to the wallpaper below it or make a new power menu with no screenshot button or gray space at all?"
- [x] REF stock photos stored permanently: research/reference/stock/INDEX.md (+ CLAUDE.md rule + resume.sh line)
- [x] LK5 (v0.2.2): no dock -> 8 rows, hotseat removed, no "^" caret on the home screen (match stock-home.png)
- [x] G3c (v0.2.2, emulator-proven incl. real Restart press) power menu: a TWO-BUTTON replacement card (Power off, Restart) exactly covering the system card, so no Screenshot slot and no gray gap; fail-open (if a tap cannot reach the real button, reveal the original menu)
- [x] BACK1 (v0.2.2, emulator-proven) (Tj, 2026-11-02: "When I do the gesture, it goes to quickstep, but I don't know how to go back to the new launcher. There needs to be a quick way to go back."): DOUBLE-PRESS HOME in Quickstep mode -> custom launcher (router sees every Home press; no permission). Also: Launcher control icon (exists), volume-combo via accessibility (later). Add a one-line hint when switching to Quickstep.
- [ ] G3d OPTIONAL experimental: wallpaper cut-out for the Screenshot slot (Tj asked; needs wallpaper read permission + alignment/dim matching; ship as a separate off-by-default mode)

## Tj, 2026-11-02 (after v0.2.2), verbatim
"Don't worry about the home layout, I will set the apps how i want." / "1) is there a faster way to cover the screenshot button so it doesn't show at all? 2) forget the kill switch button, but make it so when I double tap the home button to go back to the custom launcher, it automatically and immediately closes all apps and clears all notifications and turns off all connections such as Bluetooth or wifi so the notification panel only shows the battery symbol and clock, and automatically and immediately opens the solitaire app already on the tablet. Maybe this needs to be set in the launcher so it does all of this when the launcher opens 3) sometimes the gesture to return to quickstep is unresponsive because it instead drags the home screen left and right. This is a normal android behavior, but see if the gesture can be more responsive"
- [x] LK6 dropped: Tj arranges his own home layout
- [x] G2 dropped: no kill-switch button / CCW twist / volume combo; replaced by RESET1
- [x] COVER1 (v0.2.3, emulator-proven except the 2-finger twist, which needs the tablet) faster Screenshot cover (no visible flash): act on the first window event (no debounce), notificationTimeout 0, cached card rect shown before the node scan, no window animation
- [x] RESET1 (v0.2.3, emulator-proven except the 2-finger twist, which needs the tablet) on double-Home (Quickstep -> custom): close all apps (killBackgroundProcesses), clear notifications (NotificationListener access), Wi-Fi + Bluetooth off, then open Solitaire (found by label); master switch in Launcher control (default ON, one tap to turn off)
- [x] GEST1 (v0.2.3, emulator-proven except the 2-finger twist, which needs the tablet) more responsive two-finger CW twist: swallow the workspace scroll once two fingers are down, lower threshold, min finger distance

## Tj, 2026-11-02 (after v0.2.3), verbatim summary
"Now build the ability to hide apps from the app drawer. Any hidden apps should never show up in the app drawer search bar. Additionally, newly installed apps are automatically hidden from the app drawer, unless i go into the launcher settings and explicitly unhide apps one by one. The settings menu for the custom launcher should only be visible and accessible when the quickstep launcher is on. The goal is to 'lock' the user into the custom launcher, which looks and behaves completely stock, unless i do the gesture to switch to quickstep.
Then consider how you can build a cloned settings app which looks and behaves exactly identical to the stock Android 10 go settings ... I can customize it to remove settings and rearrange settings. Maybe download the actual settings app code just like you did with the actual quickstep launcher code?
Then consider even faster ways to hide the screenshot button. Can the accessibility mode prevent the power menu completely without hiding it? Can the accessibility feature make the alerted power menu show immediately on side key press (such as the time it usually takes for a quick button press to turn the screen off) while the original Android power menu takes a little longer because it requires a long side key press?"
- [ ] L2/L6 HIDE1: hidden apps never in drawer or drawer search; apps installed after the baseline are hidden until Tj allows them one by one (chooser in Launcher control)
- [x] LOCK1 (v0.3.0): Launcher control / hidden-apps chooser only visible+openable while Quickstep mode is on; our own icons never in the custom drawer
- [ ] L3 SET1 (consider, then build): curated "Settings" clone — see research/SETTINGS_CLONE_2026-11-02.md
- [ ] PWR2 research: can accessibility see the power key / prevent or pre-empt the power menu? (diagnostic in emulator, answer Tj)

## Tj's v0.2.3 report, 2026-11-02 (verbatim)
"The solitaire opened and the wifi turned off, but the opened apps did not close. The notification access feature says this feature is not available on this device. Maybe it can force stop apps or find another way to close apps, or at least hide the open apps from the recent apps menu. Can the launcher simply make it so it always shows only solitaire in the recent apps menu permanently?"
Findings: killBackgroundProcesses did not close his apps on the tablet; Go devices (low-RAM) do not offer Notification access, so the listener route cannot work there.
- [x] RESET2 (v0.3.0) rebuild the return reset on the ACCESSIBILITY helper (he already enables it): open the notification shade and press "Clear all"; open Recents and press "Clear all" (that removes the tasks AND closes the apps and empties Recents); then go home and open Solitaire; fallbacks: killBackgroundProcesses. Control shows what each step did last time. Drop the notification-access button/status.
- [ ] REC1 "always only Solitaire in Recents": system owns Recents; answer + optional auto-clear when Recents opens (accessibility) — decide after RESET2 is tested on the tablet
- [ ] REC1 (Tj, 2026-11-02: "If the custom launcher can permanently only show solitaire in the recent apps menu, this is even better than closing the apps") BUILD: every app launched from the custom launcher gets FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS except the target app (Solitaire) => Recents shows only Solitaire, permanently, instantly, no UI automation. Switch in Launcher control (default ON). Caveat: excluded apps keep running (not closeable via Recents Clear all), and are found again via the drawer.
- [x] SW1 (v0.3.0) (Tj, 2026-11-02: "Also add the option to do the clear all button for both the recent apps and the notifications ... put both methods in the launcher so I can see which one works better. I can switch between them in the launcher settings"): three independent switches in Launcher control (Quickstep mode only): (1) Recents shows only Solitaire [launch flag], (2) return reset presses Clear all in the notification shade, (3) return reset presses Clear all in Recents.
- [x] SOL1 (v0.3.0) (Tj, 2026-11-02: "The solitaire opening is a little glitch right now. It was in the recent apps but didn't open on the screen. Diagnose and ship the newest launcher version"): cause = reset pressed HOME then started Solitaire 450 ms later; on the slow tablet HOME lands after Solitaire so the launcher covers it. Fix: no HOME press, start Solitaire last, verify it is in front (accessibility) and re-start up to 3 times; report in Launcher control. Ship v0.3.0 with hide apps + lock + recents policy + switches.

## Tj's v0.3.0 report, 2026-11-02 (verbatim)
"So far it works well but : 1) make the gesture to go back to quickstep even more responsive 2) if possible, make the default keyboard while in the custom launcher the stock Android keyboard but switch to Microsoft SwiftKey when I switch to quickstep 3) can the closing apps and notifications be faster? The faster the better 4) sometimes the launcher app itself shows up in recent apps. I want the recent apps to only show solitaire 5) the default should be solitaire in the foreground when the custom launcher opens"
- [x] V1 (v0.3.1) gesture even more responsive (threshold lower, looser pinch tolerance, fire on the first qualifying samples)
- [x] V2 (v0.3.1) keyboard: stock IME in custom mode, SwiftKey in Quickstep (needs WRITE_SECURE_SETTINGS via adb; only switches to an IME that is already enabled; snapshot/restore; default OFF until granted)
- [x] V3 (v0.3.1) faster close/clear: poll for the Clear all buttons instead of fixed waits; start Solitaire right after the last press
- [x] V4 (v0.3.1) our Launcher activity must never be in Recents (it lost its HOME category when the router took over, so Android lists it): excludeFromRecents on Launcher + Launcher control + chooser
- [x] V5 (v0.3.1) Solitaire in the foreground whenever the custom launcher starts (cold start / boot) and on return from Quickstep (every Home press stays stock; ask Tj if he wants it on EVERY Home press)
