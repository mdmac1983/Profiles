# CHECKPOINT 321 — read me first, then TASKS.md

**Written:** 2026-11-05T06:56:29Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `05c10f9` (this checkpoint is the commit after it)

## Just done
dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, probably x) and run 120 (id 41983713997, probably g,gc)

## Do this next
read runs 119 + 120 (~4 min after 06:56Z): list_workflow_jobs resource_id=<run id> filter latest, then get_job_logs job_id tail 60; the RESULTS block says 'INFO sections:' which is which; fix any FAIL; then bump versionCode (v0.7.1 = code 18) + phoneguide version, ship.sh, android.yml full_build, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
  aa2b6de ckpt 316: PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending):
  b660de3 ckpt 315: PG1 done: phone guide redesigned for a short cable session. New order: PART 1 
  35fada6 ckpt 314: Tj: close G3d (done) + phone guide: simple, logical order for a short OTG sess
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
```
