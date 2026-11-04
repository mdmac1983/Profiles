# CHECKPOINT 177 — read me first, then TASKS.md

**Written:** 2026-11-04T00:32:41Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `179cd9e` (this checkpoint is the commit after it)

## Just done
Tj chose 'Hide completely: on' (AskUserQuestion): HOME-type launch is now the DEFAULT (RouterActivity.HOME_TYPE_KEY default true), Restore everything turns it off, crash-breaker scenario block runs with the switch off. Build+tests green.

## Do this next
Dispatch emulator.yml (this auto-cancels the previous run), read results: crash breaker PASS + B1/B4/B6/Y1/Y2 PASS; then ship v0.3.4 (ship.sh, android.yml full_build=true main, record-release). Tell Tj: B5 wallpaper still needs the image

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md
    M  app/src/main/java/com/tj/launcher/ControlActivity.java
    M  app/src/main/java/com/tj/launcher/RestoreStore.java
    M  app/src/main/java/com/tj/launcher/RouterActivity.java
    M  research/FIRST_INSTALL.md
    M  tools/emulator_scenario.sh

## Last ten checkpoints
```
  179cd9e ckpt 176: Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-ty
  35719db ckpt 175: Run 32 (eda1603): the emulator died at 23:11 right after the first reset step 
  eda1603 ckpt 174: B4 coded (RecentsDirty tracking, status-bar skip, black cover, instant Solitai
  02d9241 ckpt 173: B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
  78b22d1 ckpt 170: RELEASED v0.3.3 (run 41713996222 green, Release published, signed, 9.16 MB)
  5e3501b ckpt 169: v0.3.3 gated (code 9) and pushed (60c81ad on main); android.yml full_build dis
  60c81ad ckpt 168: pre-ship: v0.3.3: power-menu cover on the lock screen (Y1), faster double-Home
  66aa558 ckpt 167: Z1b: Tj's ORIGINAL leaf wallpaper (960x853, no number) replaces the AI-edited 
```
