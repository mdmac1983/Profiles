# CHECKPOINT 509 — read me first, then TASKS.md

**Written:** 2026-11-06T10:33:11Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `3298a5d` (this checkpoint is the commit after it)

## Just done
runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true vis=true (the patch DOES rest after calibration); the red-crop control fails most likely because run-as cannot read /data/local/tmp (SELinux): now injected via base64 and its size printed; run 220 (base,b6,n6,ka,ao,rg,uf): base/b6/n6/ka/ao + AR1 grouping green; rg failed in sequence (it passes alone: run 204) = leftover crash-breaker state / slow Recents cards: crash prefs cleared at its start, RecentsFilter now looks again at 0.6/1.3/2.5/4 s, 8 s settle; uf UF2 failed because the stock RECENTS screen is resumed behind the reset's black cover: the check now only forbids the stock HOME and the Quickstep app and requires the last resume to be ours

## Do this next
dispatch ps,ae,x1,last and v,legacy,rg,uf on the final commit; read them and run 221 (t,ac,se,rt,im,pf,rb on f5e5278); if all green: bump 0.8.0 code 25, ship.sh, android.yml full_build on the branch, get_release_by_tag v0.8.0, record-release.sh, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
