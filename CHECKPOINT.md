# CHECKPOINT 477 — read me first, then TASKS.md

**Written:** 2026-11-06T07:29:04Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `b2f7208` (this checkpoint is the commit after it)

## Just done
Tj: stock menu stays stock, patch always in place; photo shows the model patch plainly visible; AM1 design recorded

## Do this next
v0.7.6 first: read gate runs 189/190/191 (~07:30) + reviewers a7e7c5fb1cbb98059 / a716b835fb97dc18c, fix, delta re-gate, ship (ref = branch). THEN v0.7.7 = AM1 in TASKS: power_card switch default OFF (cardless), calibration from a real screenshot (MediaStore, GLOBAL_ACTION_TAKE_SCREENSHOT optional), per-rotation crop stored, resting patch only when calibrated

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  de3d688 ckpt 476: v0.7.6 gate dispatched on 43d6cb4: emulator.yml runs 189 (id 42179486444) lega
  43d6cb4 ckpt 475: version 0.7.6 (code 23) bumped for the gate; unit tests + lint green
  1cbf45c ckpt 474: tools/photo_rectify.py (tested, refuses the repo), HANDOFF state refreshed, CL
  106465d ckpt 473: v0.7.6: patch device profile (tb8168 shift -90 px, +8 %), wallpaper geometry r
  8e6db5b ckpt 472: v0.7.6 WIP: number constants + nudges, card layout, slot-only soft patch; AL1 
  9040fe4 ckpt 471: v0.7.6 WIP: review fixes done (CrashGuard order, saver cap, RESET log, card pr
  013c849 ckpt 470: v0.7.5 RELEASED + RECORDED (run 24, from c0e044d on the branch); v0.7.6 WIP: A
  0cd8ac2 ckpt 469: v0.7.5 build dispatched: android.yml run 24 (id 42176728625) on c0e044d, branc
  c0e044d ckpt 468: intake redacts Recents card titles (v0.7.5 review privacy item); reviews clear
  753c633 ckpt 467: gated v0.7.5 (code 22) and pushed it: the handoff self-test, the full unit sui
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
