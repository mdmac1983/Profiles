# CHECKPOINT 706 — read me first, then TASKS.md

**Written:** 2026-11-08T18:19:12Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `3e1c508` (this checkpoint is the commit after it)

## Just done
BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu press flickers the patch (warm it up in the dark), hide the QS pencil on the lock screen, make the custom launcher load from the lock screen as fast as possible; BJ1/BJ1e ticked (Tj: home screen power menu looks good now). No code yet

## Do this next
investigate in parallel: (a) first-press cause: what is one-time in the lock-screen open path (TjAccessibilityService fastMenuOpen/showStock/onDraw/setOpenLook, lockPrepare) + a film of the FIRST opening after a lock cycle; (b) pencil: schedulePencil/Mode.isCustom/coverWorld on the lock screen + section b6; (c) unlock-to-home timeline map (ReturnReset, settleHome, resetCover, UnlockMethods) with a read-only subagent

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
  43a5da9 ckpt 699: BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,
  783b81a ckpt 698: BJ1c: run #383 (id 42502655352, head 825ae3d) VERIFIED the fix: hf 10 PASS 0 F
  5afdc9d ckpt 697: BJ1c: /code-review high (8 findings) handled: fade summary moved out of the la
  825ae3d ckpt 696: BJ1c: run #382 PROVED the cause (home started no wait for a burst's first wind
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
