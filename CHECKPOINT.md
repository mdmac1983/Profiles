# CHECKPOINT 282 — read me first, then TASKS.md

**Written:** 2026-11-04T23:12:45Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `eb2e260` (this checkpoint is the commit after it)

## Just done
v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Section v green (run 76: 0 FAIL, patch is a photo of the lock screen, freed slot MAE 4e-7), section g green (run 75). TASKS ticked: U1 U2 V1 V2 V3 V4 Y2 Y3; V5 (menu backdrop dim around the patch) and U3 (needs tablet numbers) open. android.yml full_build=true dispatched on main; emulator section x (run 77, proves W1/W2 probe) running.

## Do this next
1) find the android.yml run (actions_list list_workflow_runs android.yml, branch main) and wait for green; 2) get_release_by_tag v0.6.0 must list Launcher-v0.6.0.apk AND PhoneGuide-v0.6.0.apk (if the run fails at :phoneguide steps drop those lines in android.yml and release the launcher alone); 3) bash tools/record-release.sh v0.6.0 '...' ONLY after green; 4) read emulator run 77 (section x) via get_job_logs tail 135: S1/S2/T1 checks; tick W1 W2 Y4 in TASKS.md; 5) tell Tj the Release link + how to use PhoneGuide (HANDOFF section 4) + that he can switch accounts; V5 is the next job

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  eb2e260 ckpt 281: gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit sui
  1e6ba2a ckpt 280: Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
  f2fdff4 ckpt 276: Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
  e9b3365 ckpt 272: HANDOFF.md written (single entry point for a no-context Claude account; linked
```
