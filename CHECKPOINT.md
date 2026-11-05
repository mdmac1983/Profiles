# CHECKPOINT 326 — read me first, then TASKS.md

**Written:** 2026-11-05T07:09:58Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `55b9791` (this checkpoint is the commit after it)

## Just done
code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against the code; fixing the genuine ones before release; CANCELLED full gate run 122 (it tested code about to change)

## Do this next
apply in order, unit test each, then ship.sh + FULL emulator gate on the final code: (a) ShellProtocol.keyTestLines: separate caps for device-header lines (24) and KEY_ events (40) so many input devices cannot push KEY_POWER out (+unit test with 30 devices); (b) SetupActivity.keyTest: AtomicBoolean re-entrancy guard + build() on failure too; (c) GuideActivity prefix switch: refresh commandViews text via shown(commandTexts) instead of rebuilding the page; (d) TabletReport.ownLog: destroy the logcat child in finally; (e) Features.all package-private; tools only: (f) tablet_intake.py log error regex accept E/ as well as E (logcat -v time), IPv6 redaction, prefer the highest ' (N)' report; (g) uinode.py require --contains, exit 2 on bad --nth, stderr on parse failure. Declined: skippable-after-open (design). Run 121 (x, id 41984771050) still running: read it for the setup-screen + PARSER MISMATCH checks

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  55b9791 ckpt 325: dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  3029356 ckpt 321: dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, prob
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
  aa2b6de ckpt 316: PG2/PG3 part 1 (tablet side, code + unit tests green, emulator proof pending):
```
