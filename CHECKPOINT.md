# CHECKPOINT 711 — read me first, then TASKS.md

**Written:** 2026-11-08T19:01:41Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `905e9da` (this checkpoint is the commit after it)

## Just done
BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles first=second; BK1b quickstep-mode lock still no cover AND no log line after the marker (log was cleared AFTER palock, so a cover shown earlier was invisible: test fixed, log now cleared before the lock cycle + every PENCIL line printed); product: pencil follow-up of a power-menu burst no longer searches (pencil took 102 ms at +536 in the film); FP3 is information now. cs #393 (id 42512174153) UNLOCKSUM: custom home on screen 1865 ms after Enter [keyguard 399; cover gone +1466; reset 1429 = shade 328 + recents 1380] mode quickstep

## Do this next
dispatch emulator.yml sections=b6 (pencil diagnostics), read BK1b PENCIL lines + cover window state; code-review high faf153d..HEAD; gate (gate.sh faf153d); ship v0.8.11; ask Tj the unlock questions (which unlock is slow, what is on screen, seconds, tablet report) and the curtain decision

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
