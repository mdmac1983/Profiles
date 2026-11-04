# CHECKPOINT 181 — read me first, then TASKS.md

**Written:** 2026-11-04T00:38:11Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `9feddd9` (this checkpoint is the commit after it)

## Just done
C3: found the AOSP ICS leaf wallpapers (phone 960x800, tablet 1920x1280, android-4.0.4_r2.1 Launcher2) and saved them in research/reference/ics-leaf; Tj's file is a different (edited) version; LineageOS copy not reachable (github blocked); stock-photo match inconclusive

## Do this next
Send both ICS originals to Tj, ask which one looks like the lock screen on the tablet (or let him pick); then wait for run 35 and ship v0.3.4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    A  research/reference/ics-leaf/AOSP-ICS-4.0.4-wallpaper_leaf-phone-960x800.jpg
    A  research/reference/ics-leaf/AOSP-ICS-4.0.4-wallpaper_leaf-tablet-1920x1280.jpg
    A  research/reference/ics-leaf/README.txt

## Last ten checkpoints
```
  9feddd9 ckpt 180: C2 docs: research/ADB_COMMANDS.txt (line by line, sections 0-10 incl. C7 brows
  5658cd2 ckpt 179: Recorded C1-C7 (Tj's seven requests of 2026-11-04). v0.3.4 still gating: emula
  10f0af9 ckpt 178: Run 34 (179cd9e, switch-off build): 48 PASS / 0 FAIL incl. B1 HOME-type stack 
  dfd13fa ckpt 177: Tj chose 'Hide completely: on' (AskUserQuestion): HOME-type launch is now the 
  179cd9e ckpt 176: Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-ty
  35719db ckpt 175: Run 32 (eda1603): the emulator died at 23:11 right after the first reset step 
  eda1603 ckpt 174: B4 coded (RecentsDirty tracking, status-bar skip, black cover, instant Solitai
  02d9241 ckpt 173: B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
```
