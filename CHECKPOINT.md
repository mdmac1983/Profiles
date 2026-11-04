# CHECKPOINT 269 — read me first, then TASKS.md

**Written:** 2026-11-04T22:11:12Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `19a29e4` (this checkpoint is the commit after it)

## Just done
gated v0.5.0 (code 15) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.5.0 "v0.5.0: more lock-screen methods (drop-down, min one), bigger hidden-app toggles, lock-time prep (close Quickstep apps at lock), per-launcher tiles, opt-in native pencil/gear hide, accelerometer return (shake/knock), one-session adb setup (tools/adb-all.sh/.bat) + in-app Tablet setup & report, phone adb route, fixes: lost Enter tap at unlock"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  19a29e4 ckpt 268: Tj explained 'the probe' = the adb commands that show Claude everything about 
  560038e ckpt 267: Tj sent 2 new requests (lock-screen launcher delay; lock-screen wallpaper-patc
  feaae74 ckpt 266: Final full run 68: 90 PASS / 0 FAIL, no native crash, but the job was CUT by t
  7b73448 ckpt 265: Run 67 (sections e1,y2,b6,b4,n,n6,o,t with fixes #2/#3 + ensurehome): ALL PASS
  92b77fc ckpt 264: Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BE
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
```
