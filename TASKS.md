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
- [ ] P4 CI emulator job (reactivecircus/android-emulator-runner, API 29): install, set Home, drawer, mode flip, forced crashes, Restore byte-identical
- [ ] P5 Release v0.2 "safe core" (needs keystore): Release link + residual-risk note + first-install checklist
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
