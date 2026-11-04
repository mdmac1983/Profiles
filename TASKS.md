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
- [x] Tj decides the OPEN items in BRIEF.md (UI toolkit, features, device, keystore)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] Keystore created and committed under signing/ (Tj waived secrecy, 2026-11-02); GitHub signs
- [x] Tj approved `tools/MIRROR_MAIN` = on (2026-11-02): every push also fast-forwards `main`
- [x] Install the preview APK on the Android 10 Go device; set it as default home app; report what happens  [DONE or superseded: see STATUS SNAPSHOT]

## Features — Tj's request, 2026-11-02 (second message, verbatim summary of the spec; screenshots saved in research/uploads/)
Device: Secure generic Alps 711 tb8168p1_bsp ("About tablet: 711"), MT8168B, Mali-G52 MC2, modified Android 10 Go (API 29),
security patch 2021-07-05, fingerprint alps/vnd_tb8168p1_bsp/tb8168p1_bsp:10/QP1A.190711.020/1719850017:user/release-keys,
Treble, kernel 4.14.141, secure boot, LOCKED bootloader, build sys_mssi_t_32_ago-user.2024070111, custom build alps-mp-q0.mp3-V4.160,
stock launcher Quickstep v10, 600x1024 screen, 3-button nav, storage 96% used (1.32 GB free), 32-bit userspace (mssi_t_32).
Tj: "Build a custom launcher and send it to me as an apk file", default launcher + boots with Android, identical in look/function to
stock Quickstep 10 (may adopt the Quickstep code; Claude may download and analyse it), default Android 10 Go layouts/fonts/app drawer,
screenshots attached (home grid 3 cols, folders Connect/Games, long-press popup Home settings / Wallpapers, Home settings page,
quick settings with only Auto-rotate tile). Modifications:
- [x] L1 special permissions grantable via adb (WRITE_SECURE_SETTINGS etc.); WITHOUT them everything else works, gated features just stay unavailable  [DONE or superseded: see STATUS SNAPSHOT]
- [x] L2 hide apps I select from the app drawer AND the drawer search; they reappear when I switch back to Quickstep  [DONE or superseded: see STATUS SNAPSHOT]
- [ ] L3 app drawer shows ONLY a clone Settings app that I can customise (working settings, hide chosen ones); stock Settings back in Quickstep
- [x] L4 disable the side-key power menu while the custom launcher is active; restore in Quickstep  [DONE or superseded: see STATUS SNAPSHOT]
- [ ] L5 permanently strip all quick-settings tiles except Auto-rotate (persist even in Quickstep unless tiles can be swapped safely without adb;
      else keep auto-rotate permanently and consider an overlay for extra functions in Quickstep mode)
- [x] L6 newly installed apps do not appear in the custom drawer; they appear in Quickstep  [DONE or superseded: see STATUS SNAPSHOT]
- [x] L7 stock Android keyboard while custom launcher active; auto-switch to Microsoft SwiftKey when back on Quickstep  [DONE or superseded: see STATUS SNAPSHOT]
- [x] L8 custom gesture to go back to Quickstep (custom launcher is default; Quickstep only appears via the gesture)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] L9 separate gesture / kill switch: close all open apps, clear all notifications, Bluetooth off if on, Wi-Fi off if on, return to custom launcher at once  [DONE or superseded: see STATUS SNAPSHOT]
- [ ] L10 SAFETY (overrides everything): before ANY release be highly confident it breaks nothing; failsafe that removes every modification and
      restores Android to its pre-launcher state. A friend's launcher glitched the lock screen (could not log in) and brought back all quick-settings tiles:
      that must never happen. Must be revertible if anything goes wrong.
- [x] L11 deliver an APK (GitHub Release link when signed; preview artifact until the keystore exists)  [DONE or superseded: see STATUS SNAPSHOT]


## Build plan (2026-11-02) — do in order; checkpoint after each
- [x] P1 (compiles + packages 2026-11-02: launcher3/ imported, Gradle module, protos, plugin_core, manifest merged; see launcher3/PATCHES.md; NOT yet run on a device/emulator) Port: fetch pinned sources, build `plugin_core` from SystemUI plugin source, protos via Gradle, stub/reflect @hide APIs until `l3goWithoutQuickstep` compiles in a new Gradle module (one-Gradle-at-a-time; use CI for long builds)
- [x] P2 Look check: emulator API 29 screenshots vs research/uploads/2026-11-02 (home 3 cols, drawer 5 cols translucent + Search apps, folders, popup, Home settings)  [DONE or superseded: see STATUS SNAPSHOT]
- [~] P3 (router, Mode, StockLauncher, CrashGuard, RestoreStore, Features, rotation detector + ControlActivity DONE with JVM tests 2026-11-02; still TODO: manifest/source guard test, adb scripts, emulator scenarios) Safety core: RestoreStore, crash-loop breaker, Quickstep pass-through mode, Restore action, manifest/source guard test (no admin/keyguard/overlay/lock settings), tools/adb-grant.sh + tools/adb-restore.sh
- [x] P4 CI emulator job (emulator.yml, manual): router/mode flip/crash breaker/uninstall/manifest checks all PASS 2026-11-02 (run 41468097179)
- [x] P5 Release v0.2.0 published 2026-11-02 (signed by GitHub; https://github.com/tjshea90/Launcher/releases/tag/v0.2.0). Tj has NOT yet tested it on the tablet. Release v0.2 "safe core":
- [x] P6 L2, L6, L3 -> v0.3;  P7 L7, L5, L9 each default OFF -> v0.4;  P8 L4 experimental -> v0.5  [DONE or superseded: see STATUS SNAPSHOT]

## Tj's answers, 2026-11-02 (verbatim)
1) "I can send adb commands at a later time, but I want as much as possible to work without adb, with an option to add adb features later"
2) "I want a two finger clockwise gesture to return to quickstep, and either a small killswitch button on screen or a two finger counter clockwise gesture for the kill switch, or pressing both volume buttons at the same time"
3) "try the flicker power menu but only hide the screenshot button. Make this an option I can quickly turn off if it doesn't work"
4) "it can be signed by github if possible, security isn't important"
Decisions that follow (see DESIGN section 8): no-adb path first (accessibility + notification-listener are enabled in Settings, not adb);
return from Quickstep = launcher's own app icon + volume combo; power menu = hide ONLY the Screenshot entry (my reading of 3; ask if wrong);
keystore committed under signing/ (Tj waived secrecy), workflow signs with it.
- [x] G1 two-finger clockwise rotate on custom home/drawer -> Quickstep mode (pass-through)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] G2 kill switch: small on-screen button, two-finger counter-clockwise, both volume keys (accessibility); each independently switchable  [DONE or superseded: see STATUS SNAPSHOT]
- [x] G3 power menu: hide only the Screenshot entry (accessibility overlay on that node), quick on/off switch (also reachable in Quickstep mode)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] G4 keyboard (L7): no-adb fallback = on mode change show the system keyboard picker / notification; adb grant makes it automatic  [DONE or superseded: see STATUS SNAPSHOT]
- [x] G5 GitHub signs releases: keystore generated once, committed under signing/, android.yml uses it

## Tj's v0.2.0 test report, 2026-11-02 (verbatim summary) — photos in research/uploads/2026-11-02b/
"It installed fine. My display size is default. ... 1.png and 2.png are what the stock launcher looks like. 3.png and 4.png are the launcher you made.
It must look and function identically as the stock launcher. For the power menu, I want to hide the screenshot button (picture attached) completely, but if not
possible, disable the power menu completely, until I switch back to quickstep. The recents button works in the new launcher. Notice the grid layout and
transparency of the stock launcher."
Measured from the photos: 1024x600, **170 dpi**, **font scale 1.2**, 7.2 in, 32-bit ARMv7 (armeabi-v7a), MT8168B 4x2.0 GHz, Mali-G52.
Stock home = 3 columns x 4 rows, NO search bar, NO dock/hotseat, white labels; stock drawer = 5 columns, compact rows, TRANSLUCENT (wallpaper visible), "Search apps" bar;
v0.2.0 = 4x4 grid + search bar + dock, 4-column tall-row opaque drawer.  Recents button: WORKS (R3 resolved). Power menu has Power off / Restart / Screenshot.
- [x] LK1 grid: home 3x4, no QSB, no hotseat; drawer 5 columns with the stock row height; icon/text sizes at 170 dpi + font scale 1.2  [DONE or superseded: see STATUS SNAPSHOT]
- [x] LK2 drawer transparency like stock (wallpaper visible through it)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] LK3 emulator scenario at 600x1024, density 170, font_scale 1.2 for side-by-side comparison with the STOCK photos  [DONE or superseded: see STATUS SNAPSHOT]
- [x] LK4 first-run home layout like his (folders Connect/Games, same app order) — default_workspace XML or label-based importer (stock DB is not readable without system permission)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] R3 recents button works with our router as Home (Tj, v0.2.0)
- [x] G3b power menu: Screenshot hidden completely on the emulator (v0.2.1, opt-in accessibility); block-whole-menu mode also built; needs Tj device test
- [x] LK1/LK2 measured (2026-11-02): stock drawer = 5 columns, tile 55 px (=> Go icon 60 dp at 170 dpi), scrim = 50% white (least-squares over 2 stock screenshots, residual 8); applied in go/res device_profiles + styles (v0.2.1 candidate)
- [x] G3b result so far: emulator has the same AOSP power menu; first accessibility run did nothing (service probably unbound by the test's force-stop; no logs). Re-running with diagnostics + rebind. Tj's extra question: can a NEW power menu replace the system one (answer: yes via accessibility cover/replace, same dependency; no non-accessibility route on a locked Android 10)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] LK5 OPEN QUESTION for Tj: does the stock home have a dock (can he drop icons at the bottom edge)? Decides 8 rows/no dock (pitch 113 px like stock) vs 7 rows + dock (pitch 121 px, current). Stock row pitch measured 113 px, mine 121.  [DONE or superseded: see STATUS SNAPSHOT]
- [x] LK6 first-run home layout like his (folders Connect/Games + app order) — needs package names; label-based importer  [DONE or superseded: see STATUS SNAPSHOT]
- [x] L2 hide apps / L6 hide new installs / L3 clone Settings (v0.3), G2 kill switch (button, CCW twist, both volume keys), L7 keyboard switch, L5 tiles guard  [DONE or superseded: see STATUS SNAPSHOT]

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
- [x] L2/L6 HIDE1: hidden apps never in drawer or drawer search; apps installed after the baseline are hidden until Tj allows them one by one (chooser in Launcher control)  [DONE or superseded: see STATUS SNAPSHOT]
- [x] LOCK1 (v0.3.0): Launcher control / hidden-apps chooser only visible+openable while Quickstep mode is on; our own icons never in the custom drawer
- [ ] L3 SET1 (consider, then build): curated "Settings" clone — see research/SETTINGS_CLONE_2026-11-02.md
- [ ] PWR2 research: can accessibility see the power key / prevent or pre-empt the power menu? (diagnostic in emulator, answer Tj)

## Tj's v0.2.3 report, 2026-11-02 (verbatim)
"The solitaire opened and the wifi turned off, but the opened apps did not close. The notification access feature says this feature is not available on this device. Maybe it can force stop apps or find another way to close apps, or at least hide the open apps from the recent apps menu. Can the launcher simply make it so it always shows only solitaire in the recent apps menu permanently?"
Findings: killBackgroundProcesses did not close his apps on the tablet; Go devices (low-RAM) do not offer Notification access, so the listener route cannot work there.
- [x] RESET2 (v0.3.0) rebuild the return reset on the ACCESSIBILITY helper (he already enables it): open the notification shade and press "Clear all"; open Recents and press "Clear all" (that removes the tasks AND closes the apps and empties Recents); then go home and open Solitaire; fallbacks: killBackgroundProcesses. Control shows what each step did last time. Drop the notification-access button/status.
- [x] REC1 "always only Solitaire in Recents": system owns Recents; answer + optional auto-clear when Recents opens (accessibility) — decide after RESET2 is tested on the tablet  [DONE or superseded: see STATUS SNAPSHOT]
- [x] REC1 (Tj, 2026-11-02: "If the custom launcher can permanently only show solitaire in the recent apps menu, this is even better than closing the apps") BUILD: every app launched from the custom launcher gets FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS except the target app (Solitaire) => Recents shows only Solitaire, permanently, instantly, no UI automation. Switch in Launcher control (default ON). Caveat: excluded apps keep running (not closeable via Recents Clear all), and are found again via the drawer.  [DONE or superseded: see STATUS SNAPSHOT]
- [x] SW1 (v0.3.0) (Tj, 2026-11-02: "Also add the option to do the clear all button for both the recent apps and the notifications ... put both methods in the launcher so I can see which one works better. I can switch between them in the launcher settings"): three independent switches in Launcher control (Quickstep mode only): (1) Recents shows only Solitaire [launch flag], (2) return reset presses Clear all in the notification shade, (3) return reset presses Clear all in Recents.
- [x] SOL1 (v0.3.0) (Tj, 2026-11-02: "The solitaire opening is a little glitch right now. It was in the recent apps but didn't open on the screen. Diagnose and ship the newest launcher version"): cause = reset pressed HOME then started Solitaire 450 ms later; on the slow tablet HOME lands after Solitaire so the launcher covers it. Fix: no HOME press, start Solitaire last, verify it is in front (accessibility) and re-start up to 3 times; report in Launcher control. Ship v0.3.0 with hide apps + lock + recents policy + switches.

## Tj's v0.3.0 report, 2026-11-02 (verbatim)
"So far it works well but : 1) make the gesture to go back to quickstep even more responsive 2) if possible, make the default keyboard while in the custom launcher the stock Android keyboard but switch to Microsoft SwiftKey when I switch to quickstep 3) can the closing apps and notifications be faster? The faster the better 4) sometimes the launcher app itself shows up in recent apps. I want the recent apps to only show solitaire 5) the default should be solitaire in the foreground when the custom launcher opens"
- [x] V1 (v0.3.1) gesture even more responsive (threshold lower, looser pinch tolerance, fire on the first qualifying samples)
- [x] V2 (v0.3.1) keyboard: stock IME in custom mode, SwiftKey in Quickstep (needs WRITE_SECURE_SETTINGS via adb; only switches to an IME that is already enabled; snapshot/restore; default OFF until granted)
- [x] V3 (v0.3.1) faster close/clear: poll for the Clear all buttons instead of fixed waits; start Solitaire right after the last press
- [x] V4 (v0.3.1) our Launcher activity must never be in Recents (it lost its HOME category when the router took over, so Android lists it): excludeFromRecents on Launcher + Launcher control + chooser
- [x] V5 (v0.3.1) Solitaire in the foreground whenever the custom launcher starts (cold start / boot) and on return from Quickstep (every Home press stays stock; ask Tj if he wants it on EVERY Home press)

## Tj's v0.3.1 report, 2026-11-03 (verbatim)
"1) make the double home button press to clear apps and notifications and open solitaire as fast as possible 2) Launcher still shows in the recent apps menu. I want it to only show solitaire and nothing else. If this can't be done, maybe a workaround by renaming the launcher solitaire and giving it the solitaire icon, but only if you can't keep launcher out of the recent apps list. 3) consider if it is possible to always have an accessibility overlay of the wallpaper over where the power menu is, so that if I press the side key the power menu is hidden under the overlay and just looks like the wallpaper. 4) consider if the accessibility modified power menu that hides the screenshot button can be any faster. Think outside the box like i did in number 3 above. Maybe as soon as I press the side key the modified power menu shows up immediately while waiting for the long side key press for the normal power menu 5) every once in a while if I'm messing around in android or close apps, it goes back to the quickstep launcher. I don't want this. the ONLY way that I should see the quickstep launcher and its home screen is by using the gesture. Otherwise, it should be permanently locked out from quickstep or its home screen at all times until I do the gesture"
- [x] W1 (v0.3.2; emulator run 28: reset2 PASS, per-step ms in Launcher control 'Last reset'; real speed is Tj's tablet to judge) double-Home reset as fast as possible: clear apps + notifications + open Solitaire (measure the steps; cut waits; parallelise)
- [x] W2 (v0.3.2; emulator run 28 'every task of ours carries the exclude flag' PASS; fallback TaskDescription hedge in) our Launcher/router must NEVER appear in Recents, only Solitaire; if truly impossible, fallback: rename launcher "solitaire" + Solitaire icon (only if proven impossible)
- [x] W3 (v0.3.2: answered in chat; permanent overlay not workable over apps; card is now pre-built invisible+untouchable) consider: always-present accessibility overlay of the wallpaper over the power-menu area, so the side-key menu is hidden under it
- [x] W4 (v0.3.2: pre-built card + side-key-DOWN early show; whether the key reaches the helper is only knowable on the tablet: Launcher control shows the counter) consider: faster modified power menu (e.g. show the replacement card the instant the side key is pressed, before the long-press menu) — think outside the box
- [x] W5 (v0.3.2; emulator run 28: 'stock home started without the gesture is sent back' + 'Quickstep mode left alone' PASS; StrayHomeGuardTest) Quickstep/stock home must NEVER appear except via the gesture (stray fall-backs to Quickstep while using Android / closing apps must stop)

## Tj, 2026-11-03 (after the W1-W5 message), verbatim
"Is it possible to add a serial number to the bottom of the lock screen that disappears when I swipe up to enter the pin? I'm having github actions build the apks and sign the releases. Figure out what the branches in the repo are for and fix it"
- [x] X1 research/answer (answered; Tj then authorised it: see X1a-c): serial number text at the bottom of the lock screen that disappears when swiping up to the PIN (conflicts with L10 "never touch the lock screen": needs Tj's decision, do not build unasked)
- [x] X2 (closed 5 unmergeable dependabot PRs, resume.sh fix, CLAUDE.md branch table; Tj switched the default branch to main 2026-11-03, confirmed) figure out what every branch in the repo is for and fix the mess (dependabot branches, session branches, main mirror); report what was done

## Tj, 2026-11-03 (answer to X1), verbatim
"Relax the lock screen rule, allow accessibility service to alter the lock screen. The lock screen should have the exact attached wallpaper, with the number on the bottom in identical font, size, placement, and color as the attached photo, but with this number: 71125011907547. When I pull up to enter the pin number, the number on the lock screen should disappear"
Photo saved: research/uploads/2026-11-03/lockscreen-wallpaper-with-number.png (old number 71125123100728 baked in at the bottom). L10 lock-screen rule is RELAXED by Tj for this feature ONLY (accessibility overlay on the lock screen; never touchable, never blocks the PIN pad).
- [x] X1a (v0.3.2: res/drawable-nodpi/tj_lock_wallpaper.jpg + Control button; button not exercised on the emulator) clean lock-screen wallpaper: the attached photo with the old number removed (so the number can disappear), shipped in the APK; set as lock wallpaper by an explicit button in Launcher control (never automatic)
- [x] X1b (v0.3.2; emulator run 28: overlay on lock screen PASS, gone at PIN pad PASS; LockNumberTest) accessibility overlay draws 71125011907547 at the bottom of the lock screen, same font/size/placement/colour as the photo; visible only on the lock screen, hidden as soon as the PIN/pattern/password bouncer appears and when unlocked / screen off
- [x] X1c (v0.3.2; LockNumberTest, emulator PIN cleared PASS; Restore turns it off) safety: overlay is NOT_TOUCHABLE + NOT_FOCUSABLE, any error removes it, switch in Launcher control, "Restore everything" turns it off; unit-tested rule + emulator check

## Tj, 2026-11-03 (refines W3), verbatim
"A permanent wallpaper overlay won't work, because it would sit on top of every app. This is ok because i don't use the custom launcher much. See if you can make a permanent overlay only on the custom launcher that looks identical to the same area of the wallpaper underneath so that it completely hides the power menu. Then when the side key is long pressed, the custom power menu shows on top"
- [x] W3b (v0.3.3; emulator: patch window present at the learned rect, region diff vs wallpaper-only 0.0 in runs 29 and 31, control: gone when mode off; x offset not exercised by the emulator wallpaper) new opt-in power mode "wallpaper cover": while the custom launcher is active, a permanent accessibility overlay over the power-menu card area shows the wallpaper pixels of that area (identical to what is underneath), so the real menu is never visible; when the menu opens (long press) our Power off / Restart card shows on top. Never on the lock screen. Prove alignment on the emulator by pixel diff (patch vs wallpaper without patch).

## Tj, 2026-11-03 (after v0.3.2 on the tablet), verbatim
Attached: "5.png" = the wallpaper photo with the old number (research/uploads/2026-11-03/5-reference-wallpaper-with-old-number.png); "6.png" = the tablet's lock screen with v0.3.2 (research/uploads/2026-11-03/6-tablet-lockscreen-v0.3.2.png).
"1) the accessibility service to hide the screenshot button must also work on the lock screen. Right now it isn't turned on for the lock screen.
2) make the double home tap to close apps/ notifications etc. as fast as possible, instant if possible. See if it can auto detect if there are notifications to close, and if there aren't, it can skip that step. Is there a faster way to close apps than the close recents? Maybe a command to immediately close all apps before opening solitaire as soon as the custom launcher opens?
3) the text for the serial number on the lock screen is very close but not perfect. The font size/type and transparency of the numbers are slightly different. It must look completely identical to the attached screenshot labeled 5.png (except the number is different, and ignore the clock and lock screen message). the current lock screen serial is labeled 6.png. find the differences and fix it"
- [x] Y1 (v0.3.3; emulator run 31: 'the power-menu cover also works on the lock screen' PASS; PowerMenuRuleTest) power-menu cover (hide Screenshot) must also work on the lock screen (custom mode)
- [x] Y2a (v0.3.3; status-bar icon scan, skips the shade when none; emulator: notification step 140 ms vs 342-466 before; 'skipped' branch only logged when the bar is empty, emulator always has the USB-debugging icon) double-Home reset: auto-detect whether there are notifications and skip the shade step when there are none
- [x] Y2b (v0.3.3; emulator run 31: animations off for the reset PASS, scales back to 1.0 PASS, Calendar opened PASS; needs the adb grant) double-Home reset: faster way to close apps / instant: research options (kill command at launcher start, animations off during the reset, etc.), build what is safe
- [x] Y3 (v0.3.3; look set from 7.png measurements: 192x18, Light, ~73 % white, baseline 925; LockNumberTest; only the tablet can confirm) lock-screen number must look identical to 5.png (font, size, transparency): measure both screenshots, find every difference, fix

## Tj, 2026-11-03, verbatim
"See if you can find a high quality version of the same exact Android leaf wallpaper and send it to me because the reference I'm sending was modified by ai"
- [x] Z1 closed by Tj (lead was: Huawei P10 stock lock screen "leaves macro" on peakpx.com; host blocked by the session network policy, Tj must allow it) find the original high-quality version of the green-leaf wallpaper (reference: research/uploads/2026-11-03/lockscreen-wallpaper-with-number.png) and send it to Tj; if found, it can replace the inpainted lock wallpaper in the APK
- [x] Y3b (Tj sent 7.png: the CORRECT number drawn under ours on the tablet) measured ground truth: bbox x205-396, y907-924 (192x18), white at ~71 % opacity, Light strokes; v0.3.3 changed to size 0.0429, Light, alpha 181, baseline 925/1024, x +1 px. Verify with a new tablet screenshot (research/uploads/2026-11-03/7-two-numbers-overlap-tablet.png is the reference)

## Tj, 2026-11-03 (three messages in one), verbatim
"If i use adb later, can i keep the launcher installed as is and simply add the adb permissions later for the more advanced features
I think the correct font is a little translucent. Measure it
If i send multiple messages interrupting you, always continue what you are already working on and simply add my new message as the next step. Don't ever forget messages. Respond to all of them. Such as this one i sent earlier: 'See if you can find a high quality version of the same exact Android leaf wallpaper and send it to me because the reference I'm sending was modified by ai'"
- [x] A1 answered: can the adb permissions be added later without reinstalling? (yes: see answer; explain which persist)
- [x] A2 (measured 0.72 peak, set 186/255) measure the translucency of the correct number precisely (7.png) and set it; report the number
- [x] Z1 CLOSED by Tj 2026-11-03 ("Forget the original wallpaper source. I found it"; if he sends the file, swap it in for res/drawable-nodpi/tj_lock_wallpaper.jpg) (was: Tj re-asked): original high-quality leaf wallpaper. Blocked: peakpx.com not allowed in the session network; ask Tj to allow it or send the file. Lead: Huawei P10 stock lockscreen leaf (peakpx.com/en/hd-wallpaper-desktop-keigo)
- [x] STANDING RULE (CLAUDE.md "Messages from Tj"): when Tj interrupts, keep doing the current work, queue his new message as the next step, never drop one, answer every one of them
- [x] Z1b (Tj sent the ORIGINAL leaf wallpaper 2026-11-03, 960x853, no number): it replaces the AI-edited/inpainted photo as app/src/main/res/drawable-nodpi/tj_lock_wallpaper.jpg; the Control button sets it as the LOCK wallpaper (Android centre-crops it to the screen). Original kept in research/uploads/2026-11-03/ORIGINAL-leaf-wallpaper-960x853.jpg

## Tj, 2026-11-03 (after v0.3.3), verbatim
"1) the "launcher" app still shows up as a recent app. It should never be listed as a recent app at any time. Maybe the launcher app should be renamed solitaire with the solitaire icon, unless you can figure out how to make it never show up in recents. Figure out the best way to hide or completely ban launcher from the recents list.
2) when I open the launcher settings menu, the settings are too long and there is no scroll feature so I can't access the launcher settings.
3) research if there is a way to keep apps open but they only appear at all if I do the gesture to return to quickstep. The only app the custom launcher should ever have open is solitaire, including in the foreground and recents. If this is impractical or can't be done, ignore this step.
4) make the double tap home to go to the custom launcher as fast as possible : 0 animation, only close apps if there are apps to close, only clear animations if there are animations to close, do as much of this in the background as possible behind solitaire. If the double press home could immediately show solitaire, and everything else happens invisibly in the background, that would be ideal. The faster the solitaire shows up and everything else hides, the better.
5) I sent the incorrect original wallpaper. Replace it with the attached wallpaper which is the original leaf wallpaper
6) is there a way to hide the "pencil" edit icon or disable it in the notification panel? If so, add it to the app."
- [x] B1 (v0.3.4: SECONDARY_HOME filter + implicit launch = HOME-type task; emulator checks 'Launcher task is in the HOME-type stack' and 'Recents from the custom home does not list com.tj.launcher'; hedge fixed) launcher must NEVER be listed in Recents: find the best way (hide/ban, or rename "solitaire" + Solitaire icon as fallback)
- [x] B2 (v0.3.4 ScrollView; no JVM test, look at the screenshot) Launcher control menu is too long and does not scroll: make it scroll
- [x] B3 (answered: impractical, Recents is system-owned; apps launched from the custom launcher are already excluded and keep running) research (ignore if impractical): keep apps open but invisible (foreground and Recents) unless Tj does the Quickstep gesture; custom launcher only ever has Solitaire open
- [x] B4 (v0.3.4: skip empty shade (status bar), skip Recents when clean (RecentsDirty, RecentsDirtyTest), black cover, animations off, Solitaire at once when nothing to clear; emulator checks B4) double-Home as fast as possible: 0 animation, only close apps if there are apps, only clear notifications if there are notifications, Solitaire shows IMMEDIATELY and the rest happens invisibly behind it
- [x] B5 folded into C3 (Tj found a different copy; ICS originals sent); was: replace the wallpaper with the NEW attached original leaf wallpaper. BLOCKED: no image arrived with the message (newest upload is the 960x853 one Tj now says is wrong); ask Tj to resend
- [x] B6 (v0.3.4: white touch-absorbing cover over android:id/edit; emulator checks B6) hide or disable the pencil (edit) icon in the notification panel (quick settings)
- B1 design (found in AOSP RecentTasks.isInVisibleRange): an EXCLUDED task is still listed when it is the most recent task (that is why "Launcher" showed when Recents was opened from the home screen); a HOME-type task is never listed. Fix = Launcher gets a MAIN+SECONDARY_HOME intent filter (not the HOME category, so the preferred home does not change) and the router starts it with an IMPLICIT SECONDARY_HOME intent => HOME-type task. Also fixes W5's root cause (the system's "resume home" now finds OUR home task).
- B1 FINDING (emulator run 33, crash-breaker test): a HOME-type Launcher has a cost. Android's AppErrors (Q) calls clearPackagePreferredForHomeActivities when the home process crashes => our default-home setting is cleared and the next Home press shows the "choose a Home app" chooser (the test ended on ResolverActivity). So the HOME-type launch is an OPT-IN switch (Launcher control > Recents, default OFF); without it the card (if it ever shows) is renamed "solitaire" + Solitaire icon. DECISION FOR TJ: default ON (never in Recents; a crash makes Android ask for the Home app again) or OFF (safe; a "solitaire"-looking card can show)?

## Tj's decision, 2026-11-04 (AskUserQuestion answer, verbatim): "Hide completely: on"
- [x] B1 default = ON: the launcher is a HOME-type task (never in Recents). Cost accepted: if the launcher process ever crashes, Android clears our default-home setting and the next Home press shows the "Choose a Home app" dialog. Switch in Launcher control > Recents; "Restore everything" turns it off.

## Tj, 2026-11-04 (while v0.3.4 is being verified), verbatim
"1) add a setting to the launcher settings which allows me to change the serial number on the bottom of the lock screen. keep the same font, color, position, and behavior but allow me to change the number.
2) make it easy for me to add the necessary adb permissions when i am ready to do the adb commands. for example, either do all the adb commands needed automatically by giving the app adb permissions if possible, or in the app tell me a step by step guide of what adb commands to enter. also give me the commands line by line in a txt file in this session so i can save it for later. include as many permissions as needed for all of the current features of this launcher, plus additional permissions which may be necessary for later added features to the launcher, as long as the permissions dont break or risk anything.
3) i found the wallpaper, but it is slightly different, maybe due to my screen dimensions or due to lineage having a slightly different copy. the wallpaper is from android 4 ics, named leaf or macro leave, but the one i found is slightly brighter and sharper than the stock one on this lineage based tablet. find the lineage version of android 4 green leaf wallpaper. this may also be due to how the os scales the photo for the screen dimensions.
4) research if it is possible for the notification panel settings button on both the lock screen and inside android to lead to the cloned modified settings that i am going to build.
5) make both "launcher" and "quickstep" never appear in the recents list at any time in any launcher. use adb if needed
6) research if it is possible to have separate notification panels and quick settings tiles depending on the active launcher
7) tell me the adb command to uninstall or disable the "browser" app (com.android.browser) from lineageos. i cant uninstall or disable it without adb. would it break anything if i disable or uninstall it?"
- [x] C1 (v0.3.5; LockNumberSanitizeTest; the EditText UI itself is untested on the emulator) setting in Launcher control to change the lock-screen number (same font/colour/position/behaviour)
- [x] C2 (v0.3.5; emulator run 36: manifest declares DUMP/CHANGE_CONFIGURATION/PACKAGE_USAGE_STATS, pm grant DUMP works; research/ADB_COMMANDS.txt sent to Tj; AdbGuideActivity UI untested on the emulator) easy adb setup: in-app step-by-step guide (+ copy button + grant status) AND a line-by-line txt file in this session; as many safe permissions as current + later features need
- [x] C3 CLOSED by Tj 2026-11-04 ("Forget the wallpaper. No longer needed.") find the LineageOS / Android 4 ICS "leaf / macro leaf" wallpaper as shipped on this tablet (Tj's copy is brighter/sharper; maybe scaling); B5 (his earlier resend request) is folded into this
- [x] C4 (answered in research/NOTES_2026-11-04.md; building it waits for SET1 and Tj's go) research: can the notification-panel settings (gear) button, on the lock screen and in Android, open the cloned Settings?
- [x] C5 (v0.3.5; emulator run 36: 'the stock launcher task started by the router is in the HOME-type stack') "Launcher" AND "Quickstep" never in Recents in any launcher (use adb if needed)
- [x] C6 (answered; tiles-per-mode needs the adb grant + Tj's go) research: separate notification panel / quick-settings tiles per active launcher
- [x] C7 (answered + research/ADB_COMMANDS.txt section 10) answer: adb command to disable/uninstall com.android.browser on LineageOS and what it would break

## Tj, 2026-11-04 (after v0.3.5), verbatim
"the permanent wallpaper overlay hiding the power menu worked, but it currently is overlaying the wrong section of the wallpaper, and it is also covering other apps.
1) figure out if there is a way to always have a permanent overlay of what is already showing on the screen at all times except the stock power menu, so the stock power menu is always hidden behind a clone of what is already on the screen at that time, whether wallpaper or an app. the wallpaper clone wont work if im in solitaire because it shows a patch of wallpaper over the solitaire app, plus any movement on the screen such as the app drawer animation reveals the wallpaper patch, the overlay must be a live copy of the screen below
2) improve and organize the ui of the launcher settings. right now i cant tell what is on or off. make toggles and improve the overall settings ui intuitively
3) think of any other ways accessibility service can be used to hide the power menu without the short delay, or if there is a way to make the delay so fast that i cant see the stock power menu
4) the disappearing pencil works, but is there a way to make it permanent or faster so the pencil never shows?
5) if possible, make it so any apps i open using the custom launcher can stay in the recents list as normal and disappear when closed(normal behavior), but any app that i open using quickstep launcher never shows up at any time in the custom launcher's recents list (they may stay in the recents list for quickstep). Basically, keep the two launchers' recents lists functional but separate"
- [x] D0 (v0.3.6: patch only on the settled home screen + nudges; alignment can only be judged on Tj's tablet) wallpaper-patch mode problems Tj reported: wrong section of the wallpaper + covers other apps (fix or retire; see D1)
- [x] D1 (answered in the reply: not possible on Android 10) research/answer: a permanent LIVE COPY of the screen under the stock power menu (wallpaper or app, follows animations)
- [x] D2 (v0.3.6; emulator screenshot 06-control.png shows sections + switches) settings UI redesign: real toggles, sections, clear on/off state
- [x] D3 (v0.3.6: no earlier signal exists on Android 10 (source-checked); latency measured and shown in Launcher control; emulator check 'card latency was measured and stored') research + build: faster / invisible-delay hiding of the stock power menu via accessibility (anything that beats the flash)
- [x] D4 (v0.3.6: warm pencil window; emulator 'B6/D4 hidden, kept warm'; a permanent cover is impossible) pencil: permanent or faster so it never shows
- [x] D5 (v0.3.6; AppOriginTest; REC1/B4/Y2 emulator checks pass; the one-by-one card dismissal is untestable on the emulator, fallback = Clear all) separate Recents per launcher: apps opened from the custom launcher stay in Recents normally; apps opened via Quickstep never show in the custom launcher's Recents (may stay for Quickstep)

## Tj, 2026-11-04 (interrupting D0-D5), verbatim
"Can I keep the one quick settings tile in the custom launcher but have three tiles in quickstep? Keep in mind I had to use adb to make it have only one tile in the first place because there is a minimum amount of tiles android requirement
If I send additional messages at any time, always finish the prior prompts I sent and queue the next messages in order"
- [x] D6 (v0.3.6; TileProfileTest; emulator 'D6: back in custom mode the tiles are just rotation') per-launcher quick-settings tiles: ONE tile (Auto-rotate) in custom mode, THREE tiles in Quickstep mode. Research answer: YES. The tile list is the secure setting `sysui_qs_tiles`; the minimum-tiles rule is only in SystemUI's tile EDITOR (that is why adb was needed), writing the setting directly bypasses it and SystemUI reloads it live. Needs the WRITE_SECURE_SETTINGS adb grant. Build: write "rotation" on custom, the chosen 3 on Quickstep (snapshot the original first, restore on 'Restore everything'). ASK Tj which three tiles for Quickstep (default suggestion wifi,bt,rotation).
- STANDING (already in CLAUDE.md "Messages from Tj"): finish the earlier prompts first, queue later messages in order, answer every one.

## Tj, 2026-11-04 (interrupting D5), verbatim
"The goal is for the custom launcher and quickstep to essentially be two different profiles of android, custom launcher is default and quickstep can only be accessed by gesture, otherwise the user is locked inside the custom launcher. Think of the best ways to achieve this, whether adb or accessibility or any other method"
- [x] D7 (answered; research/NOTES_2026-11-04.md) research/answer: the best ways to make custom launcher and Quickstep behave like two PROFILES of Android (custom = default, Quickstep only by gesture, otherwise locked in the custom launcher): compare adb, accessibility, secure settings, device-owner/profile options, etc.; recommend a design. (Related: D5 separate Recents, D6 per-mode tiles, W5 lock-out, B1 home-type, C4 gear -> cloned Settings.)

## Tj, 2026-11-04, verbatim
"Forget the wallpaper. No longer needed.
1) is it possible to have 2 different login pins which determine which launcher starts? For example, 1000 opens custom launcher, 1001 opens quickstep. Or, 1000 opens custom launcher, 10001 opens quickstep. Consider if this is possible by any means without root. I was thinking an accessibility service overlay which "watches" which numbers are typed, and if the extra number or different number is typed, it automatically loads a certain launcher when unlocking. For example, regular pin 1000 unlocks the tablet with custom launcher like normal, but if the accessibility service sees 10001, the extra digit triggers quickstep on unlock. If this isn't possible, is there anything else accessibility service can do which has the same effect? Such as typing the pin then doing a gesture before unlocking?"
- [x] C3/wallpaper: closed by Tj, drop everything wallpaper-related from the to-do (the bundled lock wallpaper and the optional patch mode stay as they are)
- [ ] E1 research + (if feasible) build: choose the launcher at unlock. Questions: (a) two different PINs / an extra digit (10001): possible without root? (b) can the accessibility service watch the PIN pad? (c) alternative: a gesture/token before unlocking that picks Quickstep, plain PIN = custom launcher.
