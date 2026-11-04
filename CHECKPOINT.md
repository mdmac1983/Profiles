# CHECKPOINT 180 — read me first, then TASKS.md

**Written:** 2026-11-04T00:36:02Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `d1f5903` (this checkpoint is the commit after it)

## Just done
C2 docs: research/ADB_COMMANDS.txt (line by line, sections 0-10 incl. C7 browser disable) + tools/adb-setup.sh. App-side (manifest declares DUMP/CHANGE_CONFIGURATION/GET_USAGE_STATS/WRITE_SETTINGS + in-app guide) comes with v0.3.5 AFTER v0.3.4 ships.

## Do this next
Check emulator run 35; ship v0.3.4; then v0.3.5: C1, C2 app side, C5, plus research answers C3/C4/C6

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    A  tools/adb-setup.sh

## Last ten checkpoints
```
  5658cd2 ckpt 179: Recorded C1-C7 (Tj's seven requests of 2026-11-04). v0.3.4 still gating: emula
  10f0af9 ckpt 178: Run 34 (179cd9e, switch-off build): 48 PASS / 0 FAIL incl. B1 HOME-type stack 
  dfd13fa ckpt 177: Tj chose 'Hide completely: on' (AskUserQuestion): HOME-type launch is now the 
  179cd9e ckpt 176: Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-ty
  35719db ckpt 175: Run 32 (eda1603): the emulator died at 23:11 right after the first reset step 
  eda1603 ckpt 174: B4 coded (RecentsDirty tracking, status-bar skip, black cover, instant Solitai
  02d9241 ckpt 173: B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
  78b22d1 ckpt 170: RELEASED v0.3.3 (run 41713996222 green, Release published, signed, 9.16 MB)
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
