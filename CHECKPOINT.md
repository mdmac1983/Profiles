# CHECKPOINT 292 — read me first, then TASKS.md

**Written:** 2026-11-05T02:10:03Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `de48005` (this checkpoint is the commit after it)

## Just done
AB1: A11YON now single-quotes the merged accessibility list (a $ in another service's class name was expanded by the shell) + tests; AB3: NOTIFPKGS helper line (ShellProtocol.parseNotifPackages, ShellServer, ShellHelper.notifPackages) and TaskOrigin.plan(notified): a shared app that has a notification is force-stopped instead of spared, tests; AB2: pencil flag read only for SystemUI events; ReturnReset target cache. All unit tests green

## Do this next
AB3: write the CI-only testnotifier module (posts a notification when opened) + emulator.yml build step + section pf in tools/emulator_scenario.sh (tiles guard, one swipe, pencil cover, no wifi/bt, Quickstep-app notifications gone incl. shared app, control notification stays); then AB1 SAFETY doc, AB2 EFFICIENCY doc, re-run sections ka rt rs3 rs4 gc pf + full run, AB4 guide, AB5 doc, ship 0.7.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M app/src/main/java/com/tj/launcher/TjAccessibilityService.java

## Last ten checkpoints
```
  e6c72f1 ckpt 291: AB2 efficiency (code, compiled, not yet unit-tested): EventNoise (drops other 
  2fc12ca ckpt 290: Tj's new message recorded as AB1-AB5: safety audit (no factory reset, ever), e
  da09334 ckpt 289: AA1 (ImmersiveProfile: policy_control immersive.status=* per mode, merge/resto
  11306b2 ckpt 288: AA2 built: KeepAliveService (foreground, min-importance silent notification) +
  cbf34e0 ckpt 287: Added emulator research section rs (info lines only): R1 policy_control immers
  b62dbca ckpt 286: Tj: build #2 (policy_control immersive), #3 (keep-alive service), #4 (helper t
  a0d9170 ckpt 285: Wrote research/REEXAMINE_2026-11-05.md re-examining every goal with fresh idea
  e403aa7 ckpt 284: Resumed on Opus; read IDEAS/PROFILES/NOTES research. Tj asked to re-examine ev
  f12b6ca ckpt 283: v0.6.0 RELEASED and recorded (android.yml run 18 green; the Release has the La
  246f6c6 ckpt 282: v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Se
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
