# CHECKPOINT 185 — read me first, then TASKS.md

**Written:** 2026-11-04T00:48:59Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `fabb694` (this checkpoint is the commit after it)

## Just done
v0.3.5 coded: C1 EditText for the lock number (+sanitize tests), C2 AdbGuideActivity + declared permissions (DUMP, CHANGE_CONFIGURATION, PACKAGE_USAGE_STATS, WRITE_SETTINGS, REORDER_TASKS, EXPAND_STATUS_BAR), C5 stock launcher via implicit HOME intent (setPackage). Version 0.3.5/code 11. Emulator checks C5/C2 added. Build + unit tests green. NOTHING shipped yet for v0.3.5.

## Do this next
get_release_by_tag v0.3.4 (build dispatched 00:47) -> record-release; then dispatch emulator.yml for v0.3.5 (C5 stock HOME-type stack, C2 declared permissions + pm grant DUMP), ship v0.3.5; Tj still owes: which ICS leaf wallpaper matches (C3)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  app/build.gradle.kts
    M  research/FIRST_INSTALL.md

## Last ten checkpoints
```
  9af18f5 ckpt 184: v0.3.4 gated (code 10) and pushed; android.yml full_build dispatched on main. 
  fce25f5 ckpt 183: gated v0.3.4 (code 10) and pushed it: the handoff self-test, the full unit sui
  35c97ca ckpt 182: Research notes for C4/C5/C6/C7 in research/NOTES_2026-11-04.md
  94c2ea0 ckpt 181: C3: found the AOSP ICS leaf wallpapers (phone 960x800, tablet 1920x1280, andro
  9feddd9 ckpt 180: C2 docs: research/ADB_COMMANDS.txt (line by line, sections 0-10 incl. C7 brows
  5658cd2 ckpt 179: Recorded C1-C7 (Tj's seven requests of 2026-11-04). v0.3.4 still gating: emula
  10f0af9 ckpt 178: Run 34 (179cd9e, switch-off build): 48 PASS / 0 FAIL incl. B1 HOME-type stack 
  dfd13fa ckpt 177: Tj chose 'Hide completely: on' (AskUserQuestion): HOME-type launch is now the 
  179cd9e ckpt 176: Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-ty
  35719db ckpt 175: Run 32 (eda1603): the emulator died at 23:11 right after the first reset step 
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
