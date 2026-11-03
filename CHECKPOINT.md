# CHECKPOINT 165 — read me first, then TASKS.md

**Written:** 2026-11-03T20:19:54Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `1167590` (this checkpoint is the commit after it)

## Just done
A2: measured the correct number's opacity per pixel against the wallpaper above it: peak alpha 0.72 (R .64 G .72 B .61 on the top bar, .68-.72 on diagonals); consistent with ~0.73 opacity at a 1.6px Light stroke -> ALPHA 186. CLAUDE.md: standing rule for interrupting messages. TASKS: A1, A2, Z1 (still open, blocked on network/file).

## Do this next
Wait for the emulator run on the final code; then ship v0.3.3. Reply to Tj answering ALL: A1 (adb later: yes, how), A2 (measurement), Z1 (still open: needs peakpx.com allowed or the original file)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  app/src/main/java/com/tj/launcher/LockNumber.java

## Last ten checkpoints
```
  f65af65 ckpt 164: Z1: WebSearch found no exact match; lead = Huawei P10 stock lockscreen leaf wa
  81ae879 ckpt 163: Y3b: lock-number ground truth from Tj's 7.png (192x18 at x205-396,y907-924, ~7
  62e14a1 ckpt 162: Recorded Z1 (find original leaf wallpaper)
  13bf78d ckpt 161: Bumped to 0.3.3 (code 9) + FIRST_INSTALL notes; emulator run dispatched with Y
  053ec79 ckpt 160: Run 29: W3b patch window present at the learned rect, pixel diff vs wallpaper-
  842bea1 ckpt 159: CORRECTION: the previous ckpt claimed Y1/Y2/Y3 emulator checks that had NOT be
  1306cf7 ckpt 158: Y2 built: AnimationSpeedup (animations off during reset, 4 restore failsafes),
  4305b7b ckpt 157: Y3 (number: size x1.063, Light weight 300, +1px x/y) and Y1 (cover on lock scr
  283857f ckpt 156: Recorded Y1 (cover on lock screen), Y2 (faster reset, skip empty shade), Y3 (n
  244acdd ckpt 155: W3b built: power mode 'wallpaper' = permanent wallpaper patch (WallpaperManage
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
