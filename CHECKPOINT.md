# CHECKPOINT 508 — read me first, then TASKS.md

**Written:** 2026-11-06T10:21:55Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `7e8dff4` (this checkpoint is the commit after it)

## Just done
ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys lines), the status says 'patch window wanted at [485,339][596,642]', the card rect is known, but the red crop is invisible at rest: the window is created but hidden (logcat is useless here: the ring buffer of the busy emulator drops the lines). Candidates: a stale 250 ms keyguard cache in lockPatchAlways(), a first answer too early. Fix: lockPatchAlways reads the keyguard FRESH, a created lock patch window re-decides its resting state after 400 ms and 1.6 s (restRecheck), and the decision inputs are kept in the preference patch_rest_info (printed in the ps INFO, allow-listed in the report); PatchShot.has added

## Do this next
dispatch ps: the positive control must pass (red crop visible at rest) and the real crop MAE stay 0; if still hidden read 'resting decision' in the INFO; then ae,x1,v,legacy,last on the final commit; read runs 219/220/221 (sections e1..lq, base..uf, t..rb on f5e5278); then ship v0.8.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
