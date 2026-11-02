# CHECKPOINT 58 — read me first, then TASKS.md

**Written:** 2026-11-02T18:01:28Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `43e136c` (this checkpoint is the commit after it)

## Just done
Fixed power-menu service feedback loop (ignore own events, debounce, no re-layout of unchanged cover, zero work when off); emulator adb calls bounded by timeout; scenario capped at 1000 s.

## Do this next
Dispatch emulator.yml; download screenshots (01-home, 03-drawer, 10-13 power menu) and compare with STOCK photos in research/uploads/2026-11-02b; report grid/transparency differences and power-menu result; answer Tj's question about a custom power menu.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1f2e3e9 ckpt 55: Power-menu helper: TjAccessibilityService (cover Screenshot entry / block menu,
  c3955b4 ckpt 52: Look pass 1: grid 5x7 + 5-icon dock (derived from stock photo geometry), no hom
  8495c8f ckpt 49: Saved Tj's v0.2.0 report + 8 photos (density 170, font scale 1.2, stock 3x4 hom
  271eb4a ckpt 47: Released v0.2.0 (signed by GitHub Actions, 8.6 MB), recorded in BUILDLOG.
  0efe959 ckpt 45: gated v0.2.0 (code 2) and pushed it: the handoff self-test, the full unit suite
  921312e ckpt 44: Emulator safety scenario fully green. Prepared v0.2.0 (code 2): R8 off, FIRST_I
  54a4b86 ckpt 43: Emulator scenario harness fixes (force-stop after editing prefs; home between c
  eff4fa4 ckpt 41: Emulator scenario now asserts safety: manifest has no admin/overlay/keyguard/bo
  b772b25 ckpt 40: P3 core: RouterActivity (only HOME activity, fail-open to stock), Mode, StockLa
  799512e ckpt 35: Wrote DESIGN section 8: no-adb feature map, router-activity pass-through design
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
