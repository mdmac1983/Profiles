# CHECKPOINT 702 — read me first, then TASKS.md

**Written:** 2026-11-08T17:30:23Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `8e0b6c2` (this checkpoint is the commit after it)

## Just done
BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on FINAL code b0d29fd SUCCESS (34 PASS, fast path 10/10, median open_shape 0.066); #386 base,x1,ac,hf,rb = 71 PASS 3 FAIL, all three in hf phase A (HF0-HF2: no CARD line in a long chain after ac; the same phase passed in #387). hf phase A now waits for a quiet home, retries 3x, explains a miss; NEW: tap-close study (hfcl) because BACK-close films read close_max 0.5-1.16 in 5 of 10 films (a white Screenshot slot during the fade-out?), pt only judges the TAP close

## Do this next
dispatch emulator.yml sections=ac,hf on main (chain that failed + tap-close numbers); read hf INFO 'HF close by tap' + 'HF close by back'; decide: close defect real -> fix small + delta hp,hf; else ship v0.8.10 (bump versionCode 35 / 0.8.10 in app/build.gradle.kts, bash ship.sh note, android.yml full_build=true)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
  43a5da9 ckpt 699: BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,
  783b81a ckpt 698: BJ1c: run #383 (id 42502655352, head 825ae3d) VERIFIED the fix: hf 10 PASS 0 F
  5afdc9d ckpt 697: BJ1c: /code-review high (8 findings) handled: fade summary moved out of the la
  825ae3d ckpt 696: BJ1c: run #382 PROVED the cause (home started no wait for a burst's first wind
  22a8038 ckpt 695: BJ1b: per-fade frame statistics (first draw lateness, frames, slow frames, wor
  e321189 ckpt 694: BJ1b: emulator.yml run #382 id 42500697309 (sections=hf, head ae64eec) dispatc
  ae64eec ckpt 693: BJ1b: section hf (home fade film, 4 arms x 5 films) + debug-only switch home_b
  8c67d0a ckpt 692: BJ1 written into TASKS (Tj 16:21 UTC: home-screen patch looks right but the op
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
