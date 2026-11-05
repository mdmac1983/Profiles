# CHECKPOINT 379 — read me first, then TASKS.md

**Written:** 2026-11-05T22:56:01Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-0e021d44-iyicti` · **builds on:** `1079dbb` (this checkpoint is the commit after it)

## Just done
v0.7.2 gates green (A run 131, B run 133, unit 245, lint); android.yml run 21 (id 42125529945) building the signed release from 1079dbb

## Do this next
wait ~26 min (background timer to 23:23), then get_release_by_tag v0.7.2 + bash tools/record-release.sh v0.7.2 '<note in the ckpt 378 message>'; MEANWHILE the AC patch (research/ac-drafts/ac-work.patch) is applied to main WITHOUT a version bump and tested on CI (sections ac, v, n, o)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1079dbb ckpt 378: gated v0.7.2 (code 19) and pushed it: the handoff self-test, the full unit sui
  a51f04a ckpt 377: AC drafts saved in research/ac-drafts (BatterySaver*, LockLog, PowerMenuDump +
  c7cc995 ckpt 376: ka fuse check made conditional (emulator SystemUI never crashes: runs 130,132)
  782ca63 ckpt 375: AC design written (research/AC_DESIGN_2026-11-05.md); gate A run 131 + ka run 
  88e1fb2 ckpt 374: Tj sent 9 requests (AC1-AC9) written to TASKS.md
  90bdfef ckpt 373: REL072: code-review of v0.7.1..HEAD done (8 fixed, 2 declined); unit 245 green
  e860f7e ckpt 372: REL072: KA1+CBR1+UX1 coded, unit 245 green, lint ok; ka scenario extended, CI 
  ce42d62 ckpt 371: Tj asked to release the next version + will an update need adb (answer: no); R
  fa8132b ckpt 370: Tj: cable later; asked if he can use Launcher control now: yes, with cautions 
  c684404 ckpt 369: step 22: all permissions verified after restart (CBL1 closed); answer to 'ever
```
