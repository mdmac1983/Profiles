# CHECKPOINT 692 — read me first, then TASKS.md

**Written:** 2026-11-08T16:22:56Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-dda19ce3-8dl2cy` · **builds on:** `1ce317a` (this checkpoint is the commit after it)

## Just done
BJ1 written into TASKS (Tj 16:21 UTC: home-screen patch looks right but the open-menu dimming speed makes the patch appear = BI1e happened); no code yet

## Do this next
read PatchFade + the home fast-open path in TjAccessibilityService / HomeLayer vs the lock path, then read how section pt phase D films the lock fade (tools/emulator_scenario.sh) to build the home variant (BJ1b)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  a0dca77 ckpt 691: TASKS BI1a ticked; v0.8.9 is released and recorded, nothing else open except T
  4283fdd ckpt 690: v0.8.9 RELEASED 08:05 UTC and RECORDED 08:12 (android.yml run 36 id 4246129231
  bf2e8cf ckpt 689: v0.8.9 RELEASE BUILD triggered 08:03 UTC: android.yml run 36 id 42461292313 on
  1ab06f6 ckpt 688: gated v0.8.9 (code 34) and pushed it: the handoff self-test, the full unit sui
  7a30b64 ckpt 687: EXPERIMENT 07:50 (temporary, debug-only): TjHooks.exp bit mask (Settings.Globa
  b3b55ce ckpt 686: v0.8.9 NOT SHIPPED: 8 CI runs in flight 07:13-07:22: delta gate #366 hp, #367 
  5a7e019 ckpt 685: v0.8.9: W5 (stock home redirect, section legacy) failed twice on main but base
  db1c63e ckpt 684: v0.8.9 DELTA GATE 07:14 on 97905e3 (product delta since 9c40e5c = the foreign-
  e5d3cb9 ckpt 683: v0.8.9 gate rerun map 06:58: #359 pt alone 27 PASS 1 FAIL (BC2 closetap) so #3
  f619254 ckpt 682: v0.8.9 GATE READ 06:40: #356 (b6,n,n6,v,pa,rg,cs2,hp,pf,last) 89 PASS 2 FAIL =
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
