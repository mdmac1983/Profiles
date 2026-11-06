# CHECKPOINT 506 — read me first, then TASKS.md

**Written:** 2026-11-06T10:04:09Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `f2d8ce3` (this checkpoint is the commit after it)

## Just done
ps run 216: positive control still fails (red crop NOT visible at rest) and the helper-log capture is EMPTY even for PATCHSHOT, so the log-based reasoning is unreliable; now printing the helper's PATCH log lines straight from logcat (all lines since boot, and again after the helper restart) in the ps INFO

## Do this next
dispatch ps, read it: if the 'PATCH window created: resting=.. [always switch wanted calibrating shot bouncer overlayShown]' line is there, the false input is the culprit; if there is NO PATCH line at all the resting patch window is never created at rest (scheduleLayerRefresh / patchTarget / cachedCard / loadLockWallpaper) and only the menu open creates it: then create it at SCREEN_ON when locked + calibrated

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
