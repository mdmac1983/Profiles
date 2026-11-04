# CHECKPOINT 179 — read me first, then TASKS.md

**Written:** 2026-11-04T00:35:00Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/install-hooks-script-chvlig` · **builds on:** `c189a6a` (this checkpoint is the commit after it)

## Just done
Recorded C1-C7 (Tj's seven requests of 2026-11-04). v0.3.4 still gating: emulator run 35 in progress; NO app-code changes until it ships (docs/research only).

## Do this next
Wait for run 35 (41747631730) -> ship v0.3.4. Meanwhile (docs only): write research/ADB_COMMANDS.txt (C2), research notes for C3/C4/C6/C7; AFTER shipping: C1 (EditText for lock number), C2 in-app guide, C5 (RouterActivity.startStock via implicit HOME intent with setPackage(stock) so Quickstep is a HOME-type task)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
    M  TASKS.md

## Last ten checkpoints
```
  10f0af9 ckpt 178: Run 34 (179cd9e, switch-off build): 48 PASS / 0 FAIL incl. B1 HOME-type stack 
  dfd13fa ckpt 177: Tj chose 'Hide completely: on' (AskUserQuestion): HOME-type launch is now the 
  179cd9e ckpt 176: Run 33 (eda1603): 46 PASS, 2 FAIL = crash-breaker tests, caused by B1: HOME-ty
  35719db ckpt 175: Run 32 (eda1603): the emulator died at 23:11 right after the first reset step 
  eda1603 ckpt 174: B4 coded (RecentsDirty tracking, status-bar skip, black cover, instant Solitai
  02d9241 ckpt 173: B6 coded: a11y finds the QS pencil (android:id/edit, 'Edit order of settings.'
  528c5ae ckpt 172: B1 coded: Launcher has a MAIN+SECONDARY_HOME filter; RouterActivity starts it 
  98f544a ckpt 171: Recorded B1-B6 (Tj's six requests after v0.3.3). B5 blocked: wallpaper image d
  78b22d1 ckpt 170: RELEASED v0.3.3 (run 41713996222 green, Release published, signed, 9.16 MB)
  5e3501b ckpt 169: v0.3.3 gated (code 9) and pushed (60c81ad on main); android.yml full_build dis
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
