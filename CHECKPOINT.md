# CHECKPOINT 268 — read me first, then TASKS.md

**Written:** 2026-11-04T22:08:44Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `e9f9501` (this checkpoint is the commit after it)

## Just done
Tj explained 'the probe' = the adb commands that show Claude everything about the tablet (speed, users, UI/Settings code). That IS S1-S3/T1 (built, in v0.5.0). Added W1 (speed facts: dexopt state, am start -W, meminfo, oom_adj) and W2 (more code to pull) to TASKS

## Do this next
read run 69 (41918311572) at ~22:08 UTC; if 0 FAIL ship v0.5.0 (ship.sh, android.yml full_build=true main, get_release_by_tag, record-release); then U1/U2/V1/W1/W2

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  560038e ckpt 267: Tj sent 2 new requests (lock-screen launcher delay; lock-screen wallpaper-patc
  feaae74 ckpt 266: Final full run 68: 90 PASS / 0 FAIL, no native crash, but the job was CUT by t
  7b73448 ckpt 265: Run 67 (sections e1,y2,b6,b4,n,n6,o,t with fixes #2/#3 + ensurehome): ALL PASS
  92b77fc ckpt 264: Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BE
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
  457cb4a ckpt 259: Two full runs (59,60) each failed after ONE native SIGSEGV of our process in A
  943ee38 ckpt 258: Full run 59 (empty sections, 13bc686): 76 PASS / 19 FAIL, first at W5. Root ca
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
