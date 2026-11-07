# CHECKPOINT 554 — read me first, then TASKS.md

**Written:** 2026-11-07T00:23:28Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `106acac` (this checkpoint is the commit after it)

## Just done
BA0-BA3 written into TASKS.md (Tj's 2026-11-07 request: patch end level 100 % via an on-tablet scrim measurement; battery saver off on the lock screen with a guarded restore in Quickstep)

## Do this next
Start BA2a/BA2b (smaller, self-contained): pure rules in BatterySaverPolicy + tests, then BatterySaverGuard.onLock/onUnlock; then BA1a ScrimFit + tests; then the service stage

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b429293 ckpt 553: v0.8.1 SHIPPED and recorded (Release v0.8.1, Build APK run 28 green, BUILDLOG 
  1a81bd9 ckpt 552: v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and androi
  7d85b50 ckpt 551: gated v0.8.1 (code 26) and pushed it: the handoff self-test, the full unit sui
  b23199b ckpt 550: TASKS: 21 boxes ticked with proof (tools/tick_v081.py), AZ FINAL + AZ4 written
  2808072 ckpt 549: final gate: #260 {x1,e1,b6,o,bs,lq,ao,rb} PASS, #262 pt PASS, #259 {base,y2,t,
  0919477 ckpt 548: final delta gate dispatched on ckpt 547 (sha 04e6cd1, 23:11 UTC): #259 id 4231
  04e6cd1 ckpt 547: FINAL CODE for v0.8.1 (versionCode 26): v+ps run #258 22 PASS / 1 FAIL (PS C s
  92016c2 ckpt 546: dispatched on ckpt 545 (23:01 UTC): ps #254 id 42308896343, ae #255 id 4230890
  1d45987 ckpt 545: gate round 1 (ckpt 540 code) read: pt #249 9 PASS; ps B + pa PA2 + ae AE3 fail
  3cb5281 ckpt 544: PatchFadeTest adjusted for LEAD_MS 10 (unit suite + lint green locally)
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
