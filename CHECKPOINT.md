# CHECKPOINT 713 — read me first, then TASKS.md

**Written:** 2026-11-08T19:08:11Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `a1cf282` (this checkpoint is the commit after it)

## Just done
BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead of three times (recentsRoots + pressInRoots/anyTextInRoots/cardsInRoots; same searches, order and package rules); compiled. Baseline to beat: cs #393 id 42512174153 UNLOCKSUM 1865 ms after Enter (keyguard 399, cover gone +1466, reset 1429 = shade 328 + recents 1380). b6 #395 id 42513369857 (head 203a5b1) is the pencil diagnostics run

## Do this next
dispatch emulator.yml sections=cs on this head; compare UNLOCKSUM (and the reset report 'recents ... (+N ms)') with the baseline; CS-B2/CS-B4 must still pass; if no gain >= 300 ms or any CS check fails, revert the Recents change

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
