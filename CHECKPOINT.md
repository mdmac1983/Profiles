# CHECKPOINT 288 — read me first, then TASKS.md

**Written:** 2026-11-05T01:31:19Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `c58cf0f` (this checkpoint is the commit after it)

## Just done
AA2 built: KeepAliveService (foreground, min-importance silent notification) + KeepAlivePolicy (JVM test green) + manifest FOREGROUND_SERVICE + started by Router and the a11y helper + Control switch (Speed) + Restore stops it. Research run 78 results: policy_control immersive.status=* hides the status bar but ONE swipe only reveals it and a SECOND swipe opens the shade (frame 976), cmd statusbar expand works, lock screen untouched (MAE 0.002); R3 parse bug (apps with custom task affinity e.g. android.task.calendar) so movetask/display tests skipped; dev-permission list of 22 captured. New sections ka (AA2 proof) and rs2 (shell may set StatusBarManager disable flags? raw power key via getevent/sendevent? hide-a-task candidates with a correct parser) + ShellProbe research class.

## Do this next
dispatch emulator.yml sections=ka,rs2 on branch ccr-8b1c3755-1tp147; read INFO/PASS/FAIL lines (get_job_logs tail ~110); then AA3 (RMTASKS/LISTTASKS helper + TaskOrigin diff, realActivity-based parser) and AA1 shaped by the R1 + rs2 numbers; then AA4 doc, AA5 audit

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M tools/emulator_scenario.sh

## Last ten checkpoints
```
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
  a0d9170 ckpt 285: Wrote research/REEXAMINE_2026-11-05.md re-examining every goal with fresh idea
  e403aa7 ckpt 284: Resumed on Opus; read IDEAS/PROFILES/NOTES research. Tj asked to re-examine ev
  f12b6ca ckpt 283: v0.6.0 RELEASED and recorded (android.yml run 18 green; the Release has the La
  246f6c6 ckpt 282: v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Se
  eb2e260 ckpt 281: gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit sui
  1e6ba2a ckpt 280: Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
```

(6 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
