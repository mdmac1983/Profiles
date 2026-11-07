# CHECKPOINT 556 — read me first, then TASKS.md

**Written:** 2026-11-07T00:48:34Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `32f0699` (this checkpoint is the commit after it)

## Just done
BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (policy + sampling, tests), service stage (bare frame -> helper opens the stock menu -> second frame -> BACK), setOpenLook draws the measured ColorMatrix, Control switch/button/status, report line, pt phase C; BA2 proven on emulator: section bl run #265 21 PASS 0 FAIL (mState=2 after restore, Settings 'Turn off now' works, plug-in = no restore); full unit suite + lint green

## Do this next
Dispatch emulator pt (phase C) on this branch; read RESULTS; run #266 (bs,lq,ac) still running: read it; then tick BA2 boxes with proof, write TABLET_CHECKLIST_v0.8.2, ship v0.8.2

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  102c1f9 ckpt 555: BA2a-c coded: BatterySaverPolicy BA2 rules (47 JVM tests green), BatterySaverG
  041a233 ckpt 554: BA0-BA3 written into TASKS.md (Tj's 2026-11-07 request: patch end level 100 % 
  b429293 ckpt 553: v0.8.1 SHIPPED and recorded (Release v0.8.1, Build APK run 28 green, BUILDLOG 
  1a81bd9 ckpt 552: v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and androi
  7d85b50 ckpt 551: gated v0.8.1 (code 26) and pushed it: the handoff self-test, the full unit sui
  b23199b ckpt 550: TASKS: 21 boxes ticked with proof (tools/tick_v081.py), AZ FINAL + AZ4 written
  2808072 ckpt 549: final gate: #260 {x1,e1,b6,o,bs,lq,ao,rb} PASS, #262 pt PASS, #259 {base,y2,t,
  0919477 ckpt 548: final delta gate dispatched on ckpt 547 (sha 04e6cd1, 23:11 UTC): #259 id 4231
  04e6cd1 ckpt 547: FINAL CODE for v0.8.1 (versionCode 26): v+ps run #258 22 PASS / 1 FAIL (PS C s
  92016c2 ckpt 546: dispatched on ckpt 545 (23:01 UTC): ps #254 id 42308896343, ae #255 id 4230890
```

(23 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
