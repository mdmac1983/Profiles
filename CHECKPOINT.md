# CHECKPOINT 558 — read me first, then TASKS.md

**Written:** 2026-11-07T00:56:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `a224036` (this checkpoint is the commit after it)

## Just done
HANDOFF 0g + CLAUDE.md lessons written; scrim decision uses the stored measurement; guard hold bookkeeping async

## Do this next
READ runs: #268 bs id 42320933665 (BS4 fix) and #269 pt id 42321050689 (phase C: measurement + end level). If pt phase C fails read the pt-scrim-log lines (INFO PT/BA1) and fix; then freeze code, bump app/build.gradle.kts to versionCode 27 / versionName 0.8.2, run tools/gate.sh 7d85b50 (3 parallel runs), ship.sh, android.yml full_build, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  11b1655 ckpt 557: BA2 boxes ticked with run #265 proof; BA1 status written; guard hold bookkeepi
  13c6146 ckpt 556: BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (polic
  102c1f9 ckpt 555: BA2a-c coded: BatterySaverPolicy BA2 rules (47 JVM tests green), BatterySaverG
  041a233 ckpt 554: BA0-BA3 written into TASKS.md (Tj's 2026-11-07 request: patch end level 100 % 
  b429293 ckpt 553: v0.8.1 SHIPPED and recorded (Release v0.8.1, Build APK run 28 green, BUILDLOG 
  1a81bd9 ckpt 552: v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and androi
  7d85b50 ckpt 551: gated v0.8.1 (code 26) and pushed it: the handoff self-test, the full unit sui
  b23199b ckpt 550: TASKS: 21 boxes ticked with proof (tools/tick_v081.py), AZ FINAL + AZ4 written
  2808072 ckpt 549: final gate: #260 {x1,e1,b6,o,bs,lq,ao,rb} PASS, #262 pt PASS, #259 {base,y2,t,
  0919477 ckpt 548: final delta gate dispatched on ckpt 547 (sha 04e6cd1, 23:11 UTC): #259 id 4231
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
