# CHECKPOINT 559 — read me first, then TASKS.md

**Written:** 2026-11-07T01:00:27Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `ab47af8` (this checkpoint is the commit after it)

## Just done
gate prep: decode retry, Control switch also stops using a stored measurement, async hold bookkeeping; BA2 proven (#265 bl 21 PASS, #268 bs 13 PASS); #269 pt (phase C) still running

## Do this next
Gate runs dispatched on this commit: {n,o,t,x,ac,bs,lq} and {x1,e1,bl,ps,rb}; when #269 pt reports, dispatch {base,v,pa,pt,uf,last}; read all; tick BA1b-d; bump version (app/build.gradle.kts 27 / 0.8.2), ship.sh, android.yml full_build, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  35e87c1 ckpt 558: HANDOFF 0g + CLAUDE.md lessons written; scrim decision uses the stored measure
  11b1655 ckpt 557: BA2 boxes ticked with run #265 proof; BA1 status written; guard hold bookkeepi
  13c6146 ckpt 556: BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (polic
  102c1f9 ckpt 555: BA2a-c coded: BatterySaverPolicy BA2 rules (47 JVM tests green), BatterySaverG
  041a233 ckpt 554: BA0-BA3 written into TASKS.md (Tj's 2026-11-07 request: patch end level 100 % 
  b429293 ckpt 553: v0.8.1 SHIPPED and recorded (Release v0.8.1, Build APK run 28 green, BUILDLOG 
  1a81bd9 ckpt 552: v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and androi
  7d85b50 ckpt 551: gated v0.8.1 (code 26) and pushed it: the handoff self-test, the full unit sui
  b23199b ckpt 550: TASKS: 21 boxes ticked with proof (tools/tick_v081.py), AZ FINAL + AZ4 written
  2808072 ckpt 549: final gate: #260 {x1,e1,b6,o,bs,lq,ao,rb} PASS, #262 pt PASS, #259 {base,y2,t,
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
