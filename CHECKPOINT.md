# CHECKPOINT 763 — read me first, then TASKS.md

**Written:** 2026-11-09T03:54:10Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/kind-pasteur-ccqmmc` · **builds on:** `c7310bd` (this checkpoint is the commit after it)

## Just done
gated v1.1 (code 38) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v1.1 "v1.1: profiles Owner/FBOP/Rental, handshake->profile, 8 new handshakes, Rental restrictions, per-profile layout, gestures"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c7310bd ckpt 762: pre-ship: v1.1: profiles Owner/FBOP/Rental, handshake->profile, 8 new handshak
  d5a034e ckpt 761: gate round 2: run17 only CU5 (known first-frame flake), run18 GE2 (lock action
  1cbd88b ckpt 760: gate v1.1 round 1 (32ed08f): run10 CS-A3/A4 (known flake), run11 UF1/UF2 (plai
  49a4fd9 ckpt 759: v1.4 gestures built: GestureRules(+test), TjHooks gestureTouch/runGesture, per
  47f68da ckpt 758: v1.3 layout built (ProfileLayout, LauncherProvider switch, PR1b); per-profile 
  272a245 ckpt 757: v1.2 work (Rental restrictions, BT1c) built: RentalRules(+test), drawer hiding
  1f90cf8 ckpt 756: v1.1 = profiles + the 8 new handshakes (BT1b folded in): UnlockMethods RHYTHM/
  df36d74 ckpt 755: v1.1 built: ProfileRules(+test)/Profiles, per-profile apps/tiles/status bar/ra
  c870ebf ckpt 754: BT1 written: build v1.1..v1.5
  aa7af8f ckpt 753: v1.0 RELEASED + RECORDED (android.yml #2 id 42548773723, 9ebe111; https://gith
```
