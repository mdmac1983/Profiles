# CHECKPOINT 504 — read me first, then TASKS.md

**Written:** 2026-11-06T09:46:37Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `ffa36a3` (this checkpoint is the commit after it)

## Just done
runs 213/214 read: base GREEN (Control page: every group header + Restore footer), ps: calibration + MAE 0 + open 0.002 + tap swallowed all pass again; the one FAIL is still the 'PATCH rest on' log (also with a log at window creation), so the resting state is unproven: replaced the log check by a POSITIVE CONTROL (swap the stored crop for a solid red one, restart the helper: the resting patch must then be plainly visible; the real crop is restored) and INFO of the patch log + window count

## Do this next
dispatch ps again (the new control), read it: if the red patch is NOT visible at rest after a helper restart, the resting patch does not come up (refreshLayers/patchTarget after loadLockWallpaper, lastBouncer, lockPatchAlways): fix TjAccessibilityService (patchShow / addPatch rest decision), rerun ps; then the FULL gate (bash tools/gate.sh 3e7fbb5 --full: 3 runs), bump version 0.8.0 code 25, ship, release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
