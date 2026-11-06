# CHECKPOINT 511 — read me first, then TASKS.md

**Written:** 2026-11-06T18:16:17Z · **tests:** 1 RED: test_handoff.sh (2 green)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `afbd108` (this checkpoint is the commit after it)

## Just done
ps red-crop control FIXED (test-only, tools/emulator_scenario.sh): bare rebind kept the old service instance (decision timestamp stayed 10:39:19, window count 3 before and after), so the red file was never read; ae restarts with am force-stop+rebind, ps now does too + prints pid before/after. Product diff f5e5278..HEAD is only 4 files +34/-7 (PatchShot.has, RecentsFilter 4 looks, restRecheck, fresh keyguard read). In flight on branch claude/new-session-8o8uj9: rg alone = run 225 (id 42268598636, job 128066972069), ps alone dispatched right after (find via list_workflow_runs in_progress). Local gradle (unit tests+lint) running in background, log in scratchpad gradle1.log. GATE PLAN NOTE: RS1 reset2 fails only when legacy runs without base before it (scenario order base,legacy,x1,...): regroup the full gate so base and legacy share a run (gate.sh --full splits them)

## Do this next
read run 225 (rg alone) and the ps run: get_job_logs tail 45 (404 = still running). rg alone red => real AP1 regression (read RecentsFilter.wanted/mayOpenRecents); rg green => sequence interference, regroup. ps control green => resting patch proven. Then full gate in 3 runs on the final product commit with base+legacy together, bump 0.8.0 code 25, ship.sh, android.yml full_build on the branch, get_release_by_tag v0.8.0, record-release.sh, tell Tj with checklist

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  9ff5695 ckpt 510: TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old ch
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
