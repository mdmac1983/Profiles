# CHECKPOINT 561 — read me first, then TASKS.md

**Written:** 2026-11-07T01:03:34Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `b925d62` (this checkpoint is the commit after it)

## Just done
pt phase C fixed (no extra lock); gate jobs: 128247261992 {n,o,t,x,ac,bs,lq}, 128247271172 {x1,e1,bl,ps,rb}

## Do this next
probe get_job_logs on those two job ids (404 = running); re-dispatched pt (new run after #271); then dispatch {base,v,pa,uf,last}, bump version, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  0d6e1fe ckpt 560: gate runs dispatched on 5e89113: #270 id 42321616646 = {n,o,t,x,ac,bs,lq}, #27
  5e89113 ckpt 559: gate prep: decode retry, Control switch also stops using a stored measurement,
  35e87c1 ckpt 558: HANDOFF 0g + CLAUDE.md lessons written; scrim decision uses the stored measure
  11b1655 ckpt 557: BA2 boxes ticked with run #265 proof; BA1 status written; guard hold bookkeepi
  13c6146 ckpt 556: BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (polic
  102c1f9 ckpt 555: BA2a-c coded: BatterySaverPolicy BA2 rules (47 JVM tests green), BatterySaverG
  041a233 ckpt 554: BA0-BA3 written into TASKS.md (Tj's 2026-11-07 request: patch end level 100 % 
  b429293 ckpt 553: v0.8.1 SHIPPED and recorded (Release v0.8.1, Build APK run 28 green, BUILDLOG 
  1a81bd9 ckpt 552: v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and androi
  7d85b50 ckpt 551: gated v0.8.1 (code 26) and pushed it: the handoff self-test, the full unit sui
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
