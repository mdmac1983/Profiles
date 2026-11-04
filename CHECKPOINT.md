# CHECKPOINT 278 — read me first, then TASKS.md

**Written:** 2026-11-04T22:45:24Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `6bad342` (this checkpoint is the commit after it)

## Just done
CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact state. v0.6.0 code (U1/U2/V1/V3/V4/W1/W2 + phoneguide module + android.yml PhoneGuide steps) is committed and pushed on claude/admiring-gates-1ut5m3 but NOT released and the version is NOT bumped (still 0.5.0/15). Last fix (TRANSLUCENT lock patch so it is invisible when the menu is closed) is unproven: emulator sections v and g were dispatched ~22:43 UTC.

## Do this next
1) find + read emulator runs v and g (created>2026-11-04T22:40:00Z; actions_get get_workflow_run; artifact; check 81/82 pngs + results.txt FAIL lines); fix if needed; 2) bump app/build.gradle.kts to versionCode 16 / 0.6.0; 3) bash ship.sh; android.yml full_build=true on main; get_release_by_tag v0.6.0 must list Launcher-v0.6.0.apk and PhoneGuide-v0.6.0.apk (if android.yml fails on :phoneguide, drop those lines and release the launcher alone); 4) bash tools/record-release.sh v0.6.0; 5) tick TASKS U1-U3 V1-V4 W1 W2 Y2-Y4 with proofs; 6) tell Tj the link + how to use the PhoneGuide app

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M HANDOFF.md

## Last ten checkpoints
```
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
  f2fdff4 ckpt 276: Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
  953311b ckpt 271: fixed section v helper (vfeat sets lock_patch + power_mode)
  45e6c46 ckpt 270: V1+V2+U1+U2+W1+W2 coded: lock-screen wallpaper patch (invisible until menu ope
  e5c1581 ckpt 269: gated v0.5.0 (code 15) and pushed it: the handoff self-test, the full unit sui
  19a29e4 ckpt 268: Tj explained 'the probe' = the adb commands that show Claude everything about 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
