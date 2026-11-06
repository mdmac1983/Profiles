# CHECKPOINT 518 — read me first, then TASKS.md

**Written:** 2026-11-06T19:15:33Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `4cfa80a` (this checkpoint is the commit after it)

## Just done
Tj asked: does the lock screen from any launcher show only the one tile and no wifi/bluetooth/notifications no matter what (TASKS AV1 answered from code, AV2 = decision for Tj). Release v0.8.0 build in flight: android.yml run 42277575772 on e5f28ea

## Do this next
when the build ends: get_release_by_tag v0.8.0, bash tools/record-release.sh v0.8.0 with the note ship.sh printed (also in the ckpt 516 commit message), push, tell Tj the link + research/TABLET_CHECKLIST_v0.8.0.md + AV2 + AF2 questions

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  85d43cf ckpt 517: FULL GATE GREEN for v0.8.0 (code 25): final-code runs 232 x1,e1,x,v,ae | 233 b
  e5f28ea ckpt 516: gated v0.8.0 (code 25) and pushed it: the handoff self-test, the full unit sui
  02e0419 ckpt 515: review fixes in product code (patchShow: no fsync, never touchable when menu c
  766791a ckpt 514: gate read: G3 run 229 49/0; G2 run 228 79/1 (AA1 in im = known flake); G1 run 
  e9ac0e4 ckpt 513: final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
