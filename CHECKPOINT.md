# CHECKPOINT 173 — read me first, then TASKS.md

**Written:** 2026-11-03T23:05:39Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `03e929b` (this checkpoint is the commit after it)

## Just done
B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'), covers it with a white touch-absorbing window (panel is 255 white in Tj's photo), removes it when the panel closes; switch in Control (default ON). B1 hedge fixed (TaskDescription on every resume). Build+tests green.

## Do this next
B4 (instant Solitaire in the router, then background kill, skip empty shade/Recents steps), B3 answer, emulator checks for B1 (type=home stack, recents list), B6 (TjPencilCover window after 'cmd statusbar expand-settings'), then run emulator + ship v0.3.4; ask Tj to resend the wallpaper (B5)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  app/src/main/java/com/tj/launcher/ControlActivity.java
    M  app/src/main/java/com/tj/launcher/TjAccessibilityService.java

## Last ten checkpoints
```
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
  78b22d1 ckpt 170: RELEASED v0.3.3 (run 41713996222 green, Release published, signed, 9.16 MB)
  5e3501b ckpt 169: v0.3.3 gated (code 9) and pushed (60c81ad on main); android.yml full_build dis
  60c81ad ckpt 168: pre-ship: v0.3.3: power-menu cover on the lock screen (Y1), faster double-Home
  66aa558 ckpt 167: Z1b: Tj's ORIGINAL leaf wallpaper (960x853, no number) replaces the AI-edited 
  46b7aa0 ckpt 166: Z1 closed: Tj found the original wallpaper himself
  0b3fc64 ckpt 165: A2: measured the correct number's opacity per pixel against the wallpaper abov
  f65af65 ckpt 164: Z1: WebSearch found no exact match; lead = Huawei P10 stock lockscreen leaf wa
  81ae879 ckpt 163: Y3b: lock-number ground truth from Tj's 7.png (192x18 at x205-396,y907-924, ~7
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
