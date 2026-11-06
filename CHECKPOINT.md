# CHECKPOINT 519 — read me first, then TASKS.md

**Written:** 2026-11-06T19:17:37Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `8057792` (this checkpoint is the commit after it)

## Just done
Tj's new request written into TASKS (AW1-AW6): lock screen from ANY launcher, always: rotation tile only, no Wi-Fi/Bluetooth/notifications, number always at the bottom, power menu always hiding Screenshot. v0.8.0 build still in flight (android.yml run 42277575772)

## Do this next
1) confirm Release v0.8.0 (get_release_by_tag) + tools/record-release.sh + tell Tj; 2) AW1 audit of the four rules (read-only), write results under AW1, then v0.8.1 plan

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  a83fd22 ckpt 518: Tj asked: does the lock screen from any launcher show only the one tile and no
  85d43cf ckpt 517: FULL GATE GREEN for v0.8.0 (code 25): final-code runs 232 x1,e1,x,v,ae | 233 b
  e5f28ea ckpt 516: gated v0.8.0 (code 25) and pushed it: the handoff self-test, the full unit sui
  02e0419 ckpt 515: review fixes in product code (patchShow: no fsync, never touchable when menu c
  766791a ckpt 514: gate read: G3 run 229 49/0; G2 run 228 79/1 (AA1 in im = known flake); G1 run 
  e9ac0e4 ckpt 513: final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
