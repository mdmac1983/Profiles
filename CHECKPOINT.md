# CHECKPOINT 695 — read me first, then TASKS.md

**Written:** 2026-11-08T16:38:52Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `189a838` (this checkpoint is the commit after it)

## Just done
BJ1b: per-fade frame statistics (first draw lateness, frames, slow frames, worst gap) logged as 'PATCH fade end' + in the tablet report perfLine; hf prints them per film. Run #382 (id 42500697309, head ae64eec) does NOT have them yet

## Do this next
wait for run 42500697309 (sections=hf, dispatched 16:35:47 UTC, expect ~16:52): read open_shape per arm a0/a1/a2/z0, scan/pen ms, events; then decide BJ1c

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e321189 ckpt 694: BJ1b: emulator.yml run #382 id 42500697309 (sections=hf, head ae64eec) dispatc
  ae64eec ckpt 693: BJ1b: section hf (home fade film, 4 arms x 5 films) + debug-only switch home_b
  8c67d0a ckpt 692: BJ1 written into TASKS (Tj 16:21 UTC: home-screen patch looks right but the op
  a0dca77 ckpt 691: TASKS BI1a ticked; v0.8.9 is released and recorded, nothing else open except T
  4283fdd ckpt 690: v0.8.9 RELEASED 08:05 UTC and RECORDED 08:12 (android.yml run 36 id 4246129231
  bf2e8cf ckpt 689: v0.8.9 RELEASE BUILD triggered 08:03 UTC: android.yml run 36 id 42461292313 on
  1ab06f6 ckpt 688: gated v0.8.9 (code 34) and pushed it: the handoff self-test, the full unit sui
  7a30b64 ckpt 687: EXPERIMENT 07:50 (temporary, debug-only): TjHooks.exp bit mask (Settings.Globa
  b3b55ce ckpt 686: v0.8.9 NOT SHIPPED: 8 CI runs in flight 07:13-07:22: delta gate #366 hp, #367 
  5a7e019 ckpt 685: v0.8.9: W5 (stock home redirect, section legacy) failed twice on main but base
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
