# CHECKPOINT 289 — read me first, then TASKS.md

**Written:** 2026-11-05T01:42:49Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `b0d0e2a` (this checkpoint is the commit after it)

## Just done
AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/restore, opt-in, 7 JVM tests), AA2 fixed after run 79 (FGS floored at LOW importance -> transparent icon tj_ic_blank + status-bar scan ignores our title; ka assertion adjusted), AA3 (LISTTASKS/RMTASKS helper lines, realActivity task parser, TaskOrigin plan + session snapshot in Mode.set, hooked into runInvisible/cleanAtLock; 12+ JVM tests) all unit-green. Run 79 findings: with the a11y helper bound the launcher process is already adj 100 (survives kill-all), service makes it 50; the SHELL may set StatusBarManager disable flags (DISABLE_EXPAND really blocks the shade; DISABLE2_GLOBAL_ACTIONS made the stock power menu not appear); getevent can list the power-key device but sendevent is denied; am stack movetask was a typo (move-task); moving a stack to a second display does NOT hide it from Recents. New sections rt (AA3), im (AA1), rs3 (move-task, real power key via emulator console).

## Do this next
dispatch emulator.yml in parallel: sections=ka,y2,b4 (keep-alive regression: the notification-icon skip must still work) ; sections=rt ; sections=im,rs3. Read each with get_job_logs tail ~130. Then: AA4 doc (research/OPEN_BUT_HIDDEN_2026-11-05.md), AA5 guide audit (steps.txt = ADB_PHONE_COMMANDS.txt, section gc executing every guide command on the emulator, emergency undo lines incl. settings delete global policy_control), ship 0.7.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
  a0d9170 ckpt 285: Wrote research/REEXAMINE_2026-11-05.md re-examining every goal with fresh idea
  e403aa7 ckpt 284: Resumed on Opus; read IDEAS/PROFILES/NOTES research. Tj asked to re-examine ev
  f12b6ca ckpt 283: v0.6.0 RELEASED and recorded (android.yml run 18 green; the Release has the La
  246f6c6 ckpt 282: v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Se
  eb2e260 ckpt 281: gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit sui
  1e6ba2a ckpt 280: Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
```

(12 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
