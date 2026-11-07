# CHECKPOINT 562 — read me first, then TASKS.md

**Written:** 2026-11-07T01:05:29Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `747eebd` (this checkpoint is the commit after it)

## Just done
waiting: pt #272 job 128248073003, gates 128247261992 + 128247271172, code review agent running

## Do this next
1) get_job_logs tail 80 on the three jobs (404=still running). 2) pt: BA1 checks must pass (measurement MEASURED, plausible scale, dialog closed, end level); if not read INFO PT/BA1 lines. 3) review agent findings: fix real ones, re-run affected sections. 4) dispatch {base,v,pa,uf,last} (+pt if code changed) on the final commit. 5) tick BA1b-d + AZ1c, bump app/build.gradle.kts to 27 / 0.8.2, ship.sh, android.yml full_build, record-release, tell Tj (TABLET_CHECKLIST_v0.8.2.md)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  747eebd ckpt 561: pt phase C fixed (no extra lock); gate jobs: 128247261992 {n,o,t,x,ac,bs,lq}, 
  0d6e1fe ckpt 560: gate runs dispatched on 5e89113: #270 id 42321616646 = {n,o,t,x,ac,bs,lq}, #27
  5e89113 ckpt 559: gate prep: decode retry, Control switch also stops using a stored measurement,
  35e87c1 ckpt 558: HANDOFF 0g + CLAUDE.md lessons written; scrim decision uses the stored measure
  11b1655 ckpt 557: BA2 boxes ticked with run #265 proof; BA1 status written; guard hold bookkeepi
  13c6146 ckpt 556: BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (polic
  102c1f9 ckpt 555: BA2a-c coded: BatterySaverPolicy BA2 rules (47 JVM tests green), BatterySaverG
  041a233 ckpt 554: BA0-BA3 written into TASKS.md (Tj's 2026-11-07 request: patch end level 100 % 
  b429293 ckpt 553: v0.8.1 SHIPPED and recorded (Release v0.8.1, Build APK run 28 green, BUILDLOG 
  1a81bd9 ckpt 552: v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and androi
```
