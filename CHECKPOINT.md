# CHECKPOINT 287 — read me first, then TASKS.md

**Written:** 2026-11-05T01:19:50Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `41aadee` (this checkpoint is the commit after it)

## Just done
Added emulator research section rs (info lines only): R1 policy_control immersive.status on Android 10 (status bar, swipes, cmd statusbar, apps, lock screen untouched?), R2 development-level permissions list, R3 hide-a-task-but-keep-it-alive candidates (am stack movetask to the home stack, simulated secondary display). Read-only research, nothing in the app changed yet.

## Do this next
dispatch emulator.yml sections=rs on branch ccr-8b1c3755-1tp147 and read the INFO lines (get_job_logs tail ~135); meanwhile code AA2 (keep-alive foreground service: KeepAliveService + manifest FOREGROUND_SERVICE + switch + boot start + Restore stops it) then AA3 (REMOVETASKS protocol + TaskOrigin diff) then AA1 from the R1 numbers

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
  a0d9170 ckpt 285: Wrote research/REEXAMINE_2026-11-05.md re-examining every goal with fresh idea
  e403aa7 ckpt 284: Resumed on Opus; read IDEAS/PROFILES/NOTES research. Tj asked to re-examine ev
  f12b6ca ckpt 283: v0.6.0 RELEASED and recorded (android.yml run 18 green; the Release has the La
  246f6c6 ckpt 282: v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Se
  eb2e260 ckpt 281: gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit sui
  1e6ba2a ckpt 280: Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
