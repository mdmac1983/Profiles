# CHECKPOINT 396 — read me first, then TASKS.md

**Written:** 2026-11-06T01:43:50Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-9ab65fa5-vp812y` · **builds on:** `a7bf0d0` (this checkpoint is the commit after it)

## Just done
Tj 7-item message recorded; found the other session already shipped v0.7.2 and is gating v0.7.3; this branch rebased on main with MIRROR_MAIN off

## Do this next
analyse main's code for AD4 (secret entrances: FailsafeKeys/TapCode/updateHotspot, Control section 'Master failsafe') and AD5/AD1b/AD7a (TjAccessibilityService secretAccepted/applyUnlockChoice, ProfileSession, power cover only in custom?); design v0.7.4 in research/AD_DESIGN_2026-11-06.md; do NOT dispatch android.yml for v0.7.3 (other session owns it); reply to Tj with the map + plan

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  951e369 ckpt 395: round 3: rt Calendar survives with the fix (run 157 partial: task 34 present a
  7823e42 ckpt 394: gate round 3 dispatched 01:17 on 63f84b7 (v,ac,rt + A + B); docs: root cause o
  63f84b7 ckpt 393: ROOT CAUSE of gate-B rt: helper recentTaskIds matched ANY line so the OLDEST R
  c31fe3f ckpt 392: gate round 2: B run 147 = 107 PASS 1 FAIL (AA3 Calendar target task gone after
  771e441 ckpt 391: ship.sh gates passed for v0.7.3 (handoff, units 299, lint); emulator gate roun
  c027bd7 ckpt 390: gated v0.7.3 (code 20) and pushed it: the handoff self-test, the full unit sui
  c78a967 ckpt 389: final gate round 2 on 6729d90 dispatched 00:23 (A + B); pf fix verified alone 
  bcbaa95 ckpt 388: pf root cause fixed: app under lock screen no longer counted as Quickstep-open
  403ef01 ckpt 387: pf/rt regression hunt: pf alone fails (ctl in AppOrigin.quickstep before lock;
  64af798 ckpt 386: pf regression: ctl closed at lock (runs 139/141); DIRTY log + pf diagnostics a
```
