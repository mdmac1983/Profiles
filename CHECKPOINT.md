# CHECKPOINT 512 — read me first, then TASKS.md

**Written:** 2026-11-06T18:20:40Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `e496985` (this checkpoint is the commit after it)

## Just done
takeover done: ps control fixed and PROVEN (run 226 15/0), rg alone green (run 225), local unit tests 454 + lint green, resume briefing trimmed to the 20 newest open tasks; details in TASKS.md AT1-AT5

## Do this next
final gate dispatched on this tip in 3 runs: G1 base,legacy,e1,y2,b6,b4,n,n6,o,t,x | G2 ka,ac,ad,se,rt,im | G3 lq,ao,pf,rb; read each (list_workflow_runs status in_progress for ids, list_workflow_jobs, get_job_logs tail 80); green (reset2 only if RS1) -> bump app/build.gradle 0.8.0 code 25 -> bash ship.sh note -> android.yml full_build on the branch -> get_release_by_tag v0.8.0 -> tools/record-release.sh -> tell Tj with checklist; MIRROR_MAIN stays off, ask Tj before moving main

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  130c89a ckpt 511: ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
