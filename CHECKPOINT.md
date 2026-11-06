# CHECKPOINT 505 — read me first, then TASKS.md

**Written:** 2026-11-06T09:54:28Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `07cb1c8` (this checkpoint is the commit after it)

## Just done
ps positive control (run 215) FAILED: with a solid RED crop the resting patch is NOT visible at rest (MAE 0, no 'PATCH rest on' log, patch window exists), so after calibration / after a helper restart the patch window stays hidden until the menu opens (an earlier MAE 0 was trivial). Added a log of every input of the resting decision at window creation (PATCH window created: resting always switch wanted calibrating shot bouncer overlayShown) and print it in the ps INFO; rerunning ps

## Do this next
read the ps run (list newest emulator.yml dispatch; list_workflow_jobs + get_job_logs tail 44): the INFO line 'patch log lines' names which input is false; fix it in TjAccessibilityService (lockPatchAlways / patchShow / addPatch); rerun ps until the positive control passes (red crop visible at rest) AND the real crop MAE stays 0; then the FULL gate (bash tools/gate.sh 3e7fbb5 --full), bump 0.8.0 code 25, ship, release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
