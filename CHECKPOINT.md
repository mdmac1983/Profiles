# CHECKPOINT 327 — read me first, then TASKS.md

**Written:** 2026-11-05T07:15:53Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `c94b357` (this checkpoint is the commit after it)

## Just done
review fixes in: keyTestLines separate caps (many input devices cannot drop KEY_POWER), key-test button guard, guide prefix switch refreshes the boxes only, logcat child destroyed, Features.all package-private; tools: E/ log errors, IPv6 redaction, newest report wins, uinode strict args; scenario reopens the setup screen before scanning (autorun left Settings in front). unit 244 green, lint ok, intake 57, uinode 20

## Do this next
dispatch x + FULL emulator gate on main at this commit; read x first (setup-screen scan must now find all 5 numbered steps); full gate ~25 min; then bash ship.sh "<v0.7.1 note>" (its gate is already green on this code), android.yml full_build=true, get_release_by_tag v0.7.1, record-release v0.7.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b185fe1 ckpt 326: code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against th
  55b9791 ckpt 325: dispatched on main at 7ee440a: run 121 (id 41984771050) = x, run 122 (id 41984
  7ee440a ckpt 324: x findings fixed in tools: the intake no longer flags a notification-reader mi
  d8a171b ckpt 323: ship.sh gates passed for v0.7.1 (code 18, unit+lint green, pushed) but NOT REL
  97b98f9 ckpt 322: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  3029356 ckpt 321: dispatched emulator sections on main at 05c10f9: run 119 (id 41983710605, prob
  05c10f9 ckpt 320: finished the cut-off change: KEYTEST-LINES (getevent silent is not 'key not pr
  181f88b ckpt 319: PG3: short summary as text (TabletReport.summary + setup button 2b + share she
  cc68086 ckpt 318: STEP D code: emulator sections g (new guide order, collapse, tick, next-step) 
  6454345 ckpt 317: PG3 intake done: tools/tablet-intake.sh + tablet_intake.py (redact, credential
```

(20 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
