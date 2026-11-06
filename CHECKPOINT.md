# CHECKPOINT 507 — read me first, then TASKS.md

**Written:** 2026-11-06T10:11:16Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `aef491a` (this checkpoint is the commit after it)

## Just done
ps run 217: the helper log is empty because the calibration happened BEFORE the scenario's logcat -c (so the earlier 'no rest on log' proved nothing); the red-crop positive control still fails after a helper restart + lock cycle = no resting patch window is created at lock-screen show (only the menu open creates it, via evaluateScan -> refreshLayers); printing wallpaper_patch_status + card rect + window count, and patchTarget now says when the lock wallpaper cannot be read; dispatching the sections that do not depend on the patch code now (22 of the 28) while this is fixed

## Do this next
read the ps diagnostics (status line), fix why the lock-screen resting patch window is not created at wake; rerun ps, x1, ae, v, legacy, last; then ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
