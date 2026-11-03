# CHECKPOINT 176 — read me first, then TASKS.md

**Written:** 2026-11-03T23:47:28Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `ff3a7ce` (this checkpoint is the commit after it)

## Just done
Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-type launcher => Android clears the default home when the home process crashes (AppErrors.clearPackagePreferredForHomeActivities) => chooser on next Home. B1 is now an opt-in switch (RouterActivity.HOME_TYPE_KEY, default OFF, Control > Recents); scenario turns it on only for the B1 checks. B4/B6/Y1/Y2/W5/startup/Restart all PASS in run 33.

## Do this next
Dispatch emulator.yml; expect crash-breaker PASS again + B1 PASS; then ship v0.3.4; ask Tj the default for B1 (AskUserQuestion) and to resend the wallpaper (B5)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  35719db ckpt 175: Run 32 (eda1603): the emulator died at 23:11 right after the first reset step 
  eda1603 ckpt 174: B4 coded (RecentsDirty tracking, status-bar skip, black cover, instant Solitai
  02d9241 ckpt 173: B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
  78b22d1 ckpt 170: RELEASED v0.3.3 (run 41713996222 green, Release published, signed, 9.16 MB)
  5e3501b ckpt 169: v0.3.3 gated (code 9) and pushed (60c81ad on main); android.yml full_build dis
  60c81ad ckpt 168: pre-ship: v0.3.3: power-menu cover on the lock screen (Y1), faster double-Home
  66aa558 ckpt 167: Z1b: Tj's ORIGINAL leaf wallpaper (960x853, no number) replaces the AI-edited 
  46b7aa0 ckpt 166: Z1 closed: Tj found the original wallpaper himself
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
