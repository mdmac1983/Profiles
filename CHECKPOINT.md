# CHECKPOINT 712 — read me first, then TASKS.md

**Written:** 2026-11-08T19:06:42Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `327f938` (this checkpoint is the commit after it)

## Just done
BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/resumed before the keyguard broadcast counted, wording; event-less pencil search REVERTED; warm-up per look kind and only counted after a real draw; BK1b panel-on-screen is a check; TASKS no duplicate ids). Run #394 b6 (id 42512916730): custom mode cover 78 ms after the panel opened; QUICKSTEP mode: no PENCIL line at all although the panel is open and the pencil on screen: adding a debug-only 'PENCIL skipped: custom= locked=' line + keyguard policy dump to see whether the tablet was really locked in that phase

## Do this next
dispatch emulator.yml sections=b6, read the 'PENCIL skipped' line / keyguard state in the BK1b quickstep INFO lines

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
