# CHECKPOINT 710 — read me first, then TASKS.md

**Written:** 2026-11-08T18:52:35Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `8372c6c` (this checkpoint is the commit after it)

## Just done
BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up works (PATCH warm-up before the first press; fresh-process first fade 8 frames / worst gap 107 ms instead of 3 / 278; warm first = second opening), film number of the first opening is dominated by stock-scrim jitter so FP3 now judges the patch's own stats. BK1b: Quickstep-mode lock screen shows NO cover and NO service log line even with the fix; my check was vacuous (a warm window lingers): check now needs 'PENCIL cover shown' after the marker, plus diagnostics (stored mode, keyguard state, every log line). cs baseline (no helper, Quickstep -> plain PIN): reset took 1227 ms (shade +281, recents +1165 = 2 cards closed one by one), home in front +11 ms

## Do this next
dispatch emulator.yml sections=b6,fp,cs on main (b6/fp/cs in one run? no: b6,fp and cs separately): read BK1b quickstep diagnostics -> find why no cover in Quickstep stored mode; read CS-B UNLOCKSUM line

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
