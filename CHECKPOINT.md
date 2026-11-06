# CHECKPOINT 510 — read me first, then TASKS.md

**Written:** 2026-11-06T18:08:00Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-8o8uj9` · **builds on:** `024ede6` (this checkpoint is the commit after it)

## Just done
TAKEOVER by branch claude/new-session-8o8uj9 (ff to 7a9vpi tip 024ede6; old chat died 'Prompt is too long'; GitHub quiet, v0.7.7 latest release+recorded). Read the two UNREAD runs on 024ede6: run 223 (ps,ae,x1,last) 29P/1F = only 'PS B: the patch really rests there' (red-crop positive control MAE 0 although crop injection now works 327 B and decision rest=true vis=true; real crop MAE 0, open-menu MAE 0.014, tap swallowed, ae A wrong-model patch IS visible at rest MAE 0.37, ae B photo MAE 3.6e-07 all pass); run 224 (v,legacy,rg,uf) 36P/2F = reset2 (notif count 3->3, RESET result=1) and AP1 (guard ON: calendar=2 still in Recents). Green on f5e5278 (older product code): run 219 e1,y2,b4,n,o,x,ad,lq and run 221 t,ac,se,rt,im,pf,rb

## Do this next
1) read TjAccessibilityService addPatch/lockPatchAlways/restRecheck + ps scenario red-crop control: real bug (no resting window) or invalid control (crop cached in memory)? 2) dispatch rg alone and legacy alone on HEAD (emulator.yml, ref claude/new-session-8o8uj9, sections rg / legacy) to tell flake from regression; 3) then full gate on the final product commit (bash tools/gate.sh 3e7fbb5 --full), bump 0.8.0 code 25, ship.sh, android.yml full_build, get_release_by_tag v0.8.0, record-release.sh, tell Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  024ede6 ckpt 509: runs 219 (e1,y2,b4,n,o,x,ad,lq) GREEN; run 222 ps: resting decision rest=true 
  0ac789f ckpt 508: ps run 218: after a helper restart + lock the patch window EXISTS (3 dumpsys l
  f5e5278 ckpt 507: ps run 217: the helper log is empty because the calibration happened BEFORE th
  14ad080 ckpt 506: ps run 216: positive control still fails (red crop NOT visible at rest) and th
  274f0b7 ckpt 505: ps positive control (run 215) FAILED: with a solid RED crop the resting patch 
  994ba9b ckpt 504: runs 213/214 read: base GREEN (Control page: every group header + Restore foot
  24420bb ckpt 503: runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resu
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
```
