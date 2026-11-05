# CHECKPOINT 320 — read me first, then TASKS.md

**Written:** 2026-11-05T06:55:40Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `7c9e4f7` (this checkpoint is the commit after it)

## Just done
finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pressed') + intake takes a single file under any name (unit 230 green, intake 49/49) and wired tools/uinode.py (test_uinode 17/17, in test_handoff) into the g tick checks; runs 117/118 read: x failed only on the intake file name, g on a blind swipe + stale-dump taps

## Do this next
dispatch emulator.yml sections 'x' and 'g,gc' on main at the new HEAD (both together), read RESULTS via list_workflow_jobs + get_job_logs tail 60; fix what fails; then ship.sh, android.yml full_build, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
  aa2b6de ckpt 316: PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending):
  b660de3 ckpt 315: PG1 done: phone guide redesigned for a short cable session. New order: PART 1 
  35fada6 ckpt 314: Tj: close G3d (done) + phone guide: simple, logical order for a short OTG sess
  77726ab ckpt 313: Tj asked what Power menu v2 and the standing items are: answered in chat; his 
  3de52f1 ckpt 312: v0.7.0 RELEASED: https://github.com/tjshea90/Launcher/releases/tag/v0.7.0 (Lau
```

(12 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
