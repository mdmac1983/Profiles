# CHECKPOINT 175 — read me first, then TASKS.md

**Written:** 2026-11-03T23:30:56Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `eda1603` (this checkpoint is the commit after it)

## Just done
Run 32 (eda1603): the emulator died at 23:11 right after the first reset step (qemu procs 0), so B1/B4/B6 checks never ran: flaky-emulator again (runs 26/27/32 died, 25/28/29/31 survived with the same step). Un-ticked B1/B4/B6 in TASKS (they were ticked before proof). Re-dispatched the emulator run.

## Do this next
Read the new emulator run: B1 (HOME-type stack, Recents list), B6 (TjPencilCover windows), B4 (recents=false, cover shown/removed), Y1/Y2; fix FAILs; re-tick B1/B4/B6 with the proving checks; ship v0.3.4; B5 still needs Tj to resend the wallpaper

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  eda1603 ckpt 174: B4 coded (RecentsDirty tracking, status-bar skip, black cover, instant Solitai
  02d9241 ckpt 173: B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
  78b22d1 ckpt 170: RELEASED v0.3.3 (run 41713996222 green, Release published, signed, 9.16 MB)
  5e3501b ckpt 169: v0.3.3 gated (code 9) and pushed (60c81ad on main); android.yml full_build dis
  60c81ad ckpt 168: pre-ship: v0.3.3: power-menu cover on the lock screen (Y1), faster double-Home
  66aa558 ckpt 167: Z1b: Tj's ORIGINAL leaf wallpaper (960x853, no number) replaces the AI-edited 
  46b7aa0 ckpt 166: Z1 closed: Tj found the original wallpaper himself
  0b3fc64 ckpt 165: A2: measured the correct number's opacity per pixel against the wallpaper abov
```
