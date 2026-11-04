# CHECKPOINT 186 — read me first, then TASKS.md

**Written:** 2026-11-04T00:49:15Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `7ec7850` (this checkpoint is the commit after it)

## Just done
RELEASED v0.3.4 (Build APK green, Release published, signed, 9.17 MB; emulator run 35 = 48 PASS / 0 FAIL). v0.3.5 code is on the branch (C1, C2, C5), not yet emulator-proven.

## Do this next
Dispatch emulator.yml for v0.3.5, read results (C5 stock HOME-type stack, C2 declared permissions + pm grant DUMP), fix FAILs, tick C1/C2/C5 with proofs, ship v0.3.5. Tj owes: which ICS leaf wallpaper matches (C3)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  7046504 ckpt 185: v0.3.5 coded: C1 EditText for the lock number (+sanitize tests), C2 AdbGuideAc
  9af18f5 ckpt 184: v0.3.4 gated (code 10) and pushed; android.yml full_build dispatched on main. 
  fce25f5 ckpt 183: gated v0.3.4 (code 10) and pushed it: the handoff self-test, the full unit sui
  35c97ca ckpt 182: Research notes for C4/C5/C6/C7 in research/NOTES_2026-11-04.md
  94c2ea0 ckpt 181: C3: found the AOSP ICS leaf wallpapers (phone 960x800, tablet 1920x1280, andro
  9feddd9 ckpt 180: C2 docs: research/ADB_COMMANDS.txt (line by line, sections 0-10 incl. C7 brows
  5658cd2 ckpt 179: Recorded C1-C7 (Tj's seven requests of 2026-11-04). v0.3.4 still gating: emula
  10f0af9 ckpt 178: Run 34 (179cd9e, switch-off build): 48 PASS / 0 FAIL incl. B1 HOME-type stack 
  dfd13fa ckpt 177: Tj chose 'Hide completely: on' (AskUserQuestion): HOME-type launch is now the 
  179cd9e ckpt 176: Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-ty
```
