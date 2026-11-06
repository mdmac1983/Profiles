# CHECKPOINT 517 — read me first, then TASKS.md

**Written:** 2026-11-06T19:12:39Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `e5f28ea` (this checkpoint is the commit after it)

## Just done
FULL GATE GREEN for v0.8.0 (code 25): final-code runs 232 x1,e1,x,v,ae | 233 base,n,ps,last | 234 o,ac,rb | 236 im | 237 y2,b6,b4,n6,t; earlier-code runs for legacy,ka,ad,se,lq,ao,pf,rg,uf (delta-unaffected). AA1 (im) is a flake: passes on tip, fails on the v0.7.7 tag. android.yml full_build dispatched on the branch

## Do this next
find the android.yml run (list_workflow_runs status in_progress), wait for it, get_release_by_tag v0.8.0, then bash tools/record-release.sh v0.8.0 with the same note as ship.sh used (see BUILDLOG style), push, tell Tj with link + research/TABLET_CHECKLIST_v0.8.0.md; then tick AM1/AQ1/AR1/AS1 in TASKS.md

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e5f28ea ckpt 516: gated v0.8.0 (code 25) and pushed it: the handoff self-test, the full unit sui
  02e0419 ckpt 515: review fixes in product code (patchShow: no fsync, never touchable when menu c
  766791a ckpt 514: gate read: G3 run 229 49/0; G2 run 228 79/1 (AA1 in im = known flake); G1 run 
  e9ac0e4 ckpt 513: final gate in flight on 0888e76: G1 run 227 id 42269849507 (base,legacy,e1,y2,
  0888e76 ckpt 512: takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
```
