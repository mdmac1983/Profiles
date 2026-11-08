# CHECKPOINT 703 — read me first, then TASKS.md

**Written:** 2026-11-08T17:49:16Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `08fe4c2` (this checkpoint is the commit after it)

## Just done
gated v0.8.10 (code 35) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.10 "on the unlocked home screen the patch over the hidden Screenshot button now dims in step with the stock power menu, like on the lock screen: it used to stay bright for the whole fade and drop at the end (measured on the emulator, the right path ran in 1 of 10 openings, now 10 of 10); the tablet report shows how the last fade looked from the patch's side (first frame, late frames, animation scale)"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  2864951 ckpt 702: BJ1 GATE READ: #384 b6,n,o,hp,last SUCCESS; #385 e1,pt SUCCESS; #387 hp,hf on 
  b0d29fd ckpt 701: BJ1: one more guard (code re-read): fastMenuOpen ends a home-flavour wait star
  fcee0dc ckpt 700: BJ1: docs written (HOME_PATCH design doc section with the measured table, TABL
  43a5da9 ckpt 699: BJ1 GATE on head 783b81a dispatched 17:11 UTC: run #384 id 42503687941 = b6,n,
  783b81a ckpt 698: BJ1c: run #383 (id 42502655352, head 825ae3d) VERIFIED the fix: hf 10 PASS 0 F
  5afdc9d ckpt 697: BJ1c: /code-review high (8 findings) handled: fade summary moved out of the la
  825ae3d ckpt 696: BJ1c: run #382 PROVED the cause (home started no wait for a burst's first wind
  22a8038 ckpt 695: BJ1b: per-fade frame statistics (first draw lateness, frames, slow frames, wor
  e321189 ckpt 694: BJ1b: emulator.yml run #382 id 42500697309 (sections=hf, head ae64eec) dispatc
  ae64eec ckpt 693: BJ1b: section hf (home fade film, 4 arms x 5 films) + debug-only switch home_b
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
