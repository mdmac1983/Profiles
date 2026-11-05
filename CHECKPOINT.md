# CHECKPOINT 323 — read me first, then TASKS.md

**Written:** 2026-11-05T07:02:48Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `97b98f9` (this checkpoint is the commit after it)

## Just done
ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT RELEASED: run 119 (x) found 2 open items; run 120 (g,gc) is fully green (18 PASS: tick, next-step, restart all fine)

## Do this next
DO NOT trigger android.yml yet (ship.sh's printed next-step is superseded). (1) run 119 FAIL 'PARSER MISMATCH': notification sample has NotificationRecord lines but the launcher reader found no app: read TabletReport.java:152 + ShellProtocol.parseNotifPackages + ResetPolicy.shouldKill to see if the emulator's records are system-only (false positive of the intake rule in tools/tablet_intake.py:233) or a real reader bug; (2) FAIL 'setup screen numbered steps': all 5 labels exist in SetupActivity.java:174-193, the 8 blind 500px swipes (emulator_scenario.sh ~line 774) probably fling past a button: scan in 300px steps until the page stops changing, and print which strings were missing; then re-run x, then FULL emulator run (empty sections) as the release gate (L10), then android.yml full_build=true, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  3029356 ckpt 321: dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, prob
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
  aa2b6de ckpt 316: PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending):
  b660de3 ckpt 315: PG1 done: phone guide redesigned for a short cable session. New order: PART 1 
  35fada6 ckpt 314: Tj: close G3d (done) + phone guide: simple, logical order for a short OTG sess
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
```
