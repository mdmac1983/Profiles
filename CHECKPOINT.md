# CHECKPOINT 475 — read me first, then TASKS.md

**Written:** 2026-11-06T07:13:38Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `e2a1460` (this checkpoint is the commit after it)

## Just done
version 0.7.6 (code 23) bumped for the gate; unit tests + lint green

## Do this next
dispatch the v0.7.6 emulator gate: bash tools/gate.sh c0e044d -> 3 emulator.yml runs on ref claude/new-session-7a9vpi; read RESULTS blocks; collect reviewers; fix; delta re-gate; ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
  013c849 ckpt 470: v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: A
  0cd8ac2 ckpt 469: v0.7.5 build dispatched: android.yml run 24 (id 42176728625) on c0e044d, branc
  c0e044d ckpt 468: intake redacts Recents card titles (v0.7.5 review privacy item); reviews clear
  753c633 ckpt 467: gated v0.7.5 (code 22) and pushed it: the handoff self-test, the full unit sui
  307f1a7 ckpt 466: Tj sent 3 side-by-side photos (reference vs ours); card size already matches, 
  5516218 ckpt 465: photo findings: reference number ~15% bigger + heavier; agents running
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
