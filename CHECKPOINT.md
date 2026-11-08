# CHECKPOINT 709 — read me first, then TASKS.md

**Written:** 2026-11-08T18:46:40Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `2ebe681` (this checkpoint is the commit after it)

## Just done
BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key unlock_sum + Control note + intake row; stamps: Enter tap, keyguard gone, Launcher resume, first frame, black cover up/gone); compiled. TASKS has the measured BK1a result and the BK1b root cause. Runs in flight: #390 id 42511333451 (b6,fp verification of the warm-up + pencil fix, head e415cd0), #391 id 42511417413 (cs baseline of the no-helper unlock)

## Do this next
read #390 (FP3/FP4, BK1b quickstep mode) and #391 (CS-B info lines: 'Last reset' step ms, HOME line); then code review + gate + ship v0.8.11 and ask Tj the unlock questions

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
  43a5da9 ckpt 699: BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
