# CHECKPOINT 515 — read me first, then TASKS.md

**Written:** 2026-11-06T18:50:28Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `45bb76a` (this checkpoint is the commit after it)

## Just done
review fixes in product code (patchShow: no fsync, never touchable when menu closed; tests 454/0, lint ok); delta gate dispatched on the new tip; AA1 (im) fails ALONE too (runs 228, 231) so checking v0.7.7 tag vs tip

## Do this next
read delta runs (x1,e1,x,v,ae | base,n,ps,last | o,ac,rb) + run 230 (e1,y2,b6,b4,n,n6,o,t,x old code) + im runs on v0.7.7 and tip; im fails on v0.7.7 too => known flake, else regression: look at what AA1 swipes need; then bump 0.8.0 code 25, ship.sh, full_build, record, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  766791a ckpt 514: gate read: G3 run 229 49/0; G2 run 228 79/1 (AA1 in im = known flake); G1 run 
  e9ac0e4 ckpt 513: final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
