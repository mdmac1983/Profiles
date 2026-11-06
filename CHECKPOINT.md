# CHECKPOINT 521 — read me first, then TASKS.md

**Written:** 2026-11-06T19:20:51Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `c49ac41` (this checkpoint is the commit after it)

## Just done
v0.8.0 released and recorded; AW1 audit of Tj's strict lock screen done (tiles = GAP on Tj's tablet: the mode flips only at the unlock without the shell helper; radios/notifications/number hold; Screenshot = gap until calibrated); full plan in TASKS AW1 RESULT

## Do this next
Tj answers AW5 (auto-calibrate / grey block / both); then implement AW2+AW3 in TjAccessibilityService (+TileProfile.listFor, LockQuiet cap), JVM tests, emulator lq/t checks with the shell helper killed, delta gate, ship v0.8.1; fresh chat recommended (this one is at Red)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  c49ac41 ckpt 520: v0.8.0 RELEASED (android.yml run 27 green, Release published 19:14Z, BUILDLOG 
  ebe0b6e ckpt 519: Tj's new request written into TASKS (AW1-AW6): lock screen from ANY launcher, 
  a83fd22 ckpt 518: Tj asked: does the lock screen from any launcher show only the one tile and no
  85d43cf ckpt 517: FULL GATE GREEN for v0.8.0 (code 25): final-code runs 232 x1,e1,x,v,ae | 233 b
  e5f28ea ckpt 516: gated v0.8.0 (code 25) and pushed it: the handoff self-test, the full unit sui
  02e0419 ckpt 515: review fixes in product code (patchShow: no fsync, never touchable when menu c
  766791a ckpt 514: gate read: G3 run 229 49/0; G2 run 228 79/1 (AA1 in im = known flake); G1 run 
  e9ac0e4 ckpt 513: final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
```
