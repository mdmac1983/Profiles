# CHECKPOINT 708 — read me first, then TASKS.md

**Written:** 2026-11-08T18:42:22Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `1660d2d` (this checkpoint is the commit after it)

## Just done
BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window at 1/255 alpha in the first resting frame (run #389 measured the fresh-process first opening at open_shape 0.194, 3 frames, gap 278 ms vs warm 0.04-0.06) + 'FIRST fade since this process started' in the report; (b) schedulePencil uses coverWorld() (lock screen counts as custom world) and quietWork's follow-up may start the search; b6 now runs the lock phase in both stored modes; fp checks FP3/FP4

## Do this next
dispatch emulator.yml sections=b6,fp on main (verification, previous run #389 id 42509822696 was the baseline): expect BK1b quickstep PASS, FP3 coldA open_shape < 0.12, FP4 warm-up line; then Part C (unlock-to-home): add the single 'PIN accepted -> home visible' figure to the lock log/report and measure with section cs

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
  bc58bb3 ckpt 705: v0.8.10 (code 35) RELEASED 17:51 UTC and RECORDED 17:52 (android.yml run 37 id
  2eaf030 ckpt 704: v0.8.10 (code 35) RELEASE BUILD triggered 17:49:40 UTC: android.yml full_build
  faf153d ckpt 703: gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit su
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
  43a5da9 ckpt 699: BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,
  783b81a ckpt 698: BJ1c: run #383 (id 42502655352, head 825ae3d) VERIFIED the fix: hf 10 PASS 0 F
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
