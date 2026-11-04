# CHECKPOINT 270 — read me first, then TASKS.md

**Written:** 2026-11-04T22:26:49Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `d62736b` (this checkpoint is the commit after it)

## Just done
V1+V2+U1+U2+W1+W2 coded: lock-screen wallpaper patch (invisible until menu opens, lock wallpaper via getWallpaperFile(FLAG_LOCK) else home wallpaper, centered) + card shortened to the stock item size (cardArea) with Control toggle; unlock stopwatch (UNLOCKSPEED) + Control note; COMPILE_SELF job (shell), adb-all.sh/.bat step 1b, SetupActivity item; probe: speed/memory/dexopt/overlay facts; APK list +5 packages; emulator section v (test lock wallpaper hook in SetupActivity guarded by allow_setup_autorun). Unit tests green

## Do this next
dispatch emulator.yml sections 'v' (and 'o' for the lock-screen unlock path, cheap): read results (artifact), fix; then bump 0.6.0 (code 16), ship (ship.sh, android.yml full_build=true main, get_release_by_tag, record-release); then HANDOFF prep (see TASKS 'handoff')

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/emulator.yml
     M CHECKPOINT.md

## Last ten checkpoints
```
  e5c1581 ckpt 269: gated v0.5.0 (code 15) and pushed it: the handoff self-test, the full unit sui
  19a29e4 ckpt 268: Tj explained 'the probe' = the adb commands that show Claude everything about 
  560038e ckpt 267: Tj sent 2 new requests (lock-screen launcher delay; lock-screen wallpaper-patc
  feaae74 ckpt 266: Final full run 68: 90 PASS / 0 FAIL, no native crash, but the job was CUT by t
  7b73448 ckpt 265: Run 67 (sections e1,y2,b6,b4,n,n6,o,t with fixes #2/#3 + ensurehome): ALL PASS
  92b77fc ckpt 264: Run 66 (short, fix #2): N2 still failed because onPad(false) clears the pad BE
  0b229ab ckpt 263: Run 65 (race fix #1): Enter click now arrives but classifies 'none' (keyguard 
  ee9d8c8 ckpt 262: Run 64 (sections e1,y2,b6,b4,n,n6): only N2 FAILS and it is a real race: the s
  ea69503 ckpt 261: N,N6 alone (run 62): PASS. In the full run N2's service log has NO 'UNLOCKPICK
  a61d580 ckpt 260: Run 61 (full, no native crash this time): 80 PASS / 6 FAIL, all in N sections 
```

(12 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
