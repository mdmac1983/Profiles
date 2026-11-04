# CHECKPOINT 283 — read me first, then TASKS.md

**Written:** 2026-11-04T23:19:57Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-8b1c3755-1tp147` · **builds on:** `9ea88cd` (this checkpoint is the commit after it)

## Just done
v0.6.0 RELEASED and recorded (android.yml run 18 green; the Release has the Launcher and PhoneGuide APKs; sections v, g, x green). HANDOFF 0b rewritten for the released state, TASKS ticked (U1 U2 V1-V4 W1 W2 Y2-Y5). Open: V5 (stock menu dims the area around the photo patch), U3 (tablet numbers).

## Do this next
Tj installs v0.6.0 and reports back. Next job if he reports a seam or asks: V5 in TASKS.md (design in HANDOFF 0b): second CAL_SHOT with the menu open, per-channel linear fit over the strip beside the patch, ColorFilter on the photo, JVM test of the fit, emulator check on the freed-slot mean colour; ship as 0.6.1

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  246f6c6 ckpt 282: v0.6.0 (code 16) gated with ship.sh and pushed (main == branch == eb2e260). Se
  eb2e260 ckpt 281: gated v0.6.0 (code 16) and pushed it: the handoff self-test, the full unit sui
  1e6ba2a ckpt 280: Run 74 (v): U1/U2/V1/V2-closed PASS; g (run 75) PASS. V4/V2-freed FAIL because
  e91b6a5 ckpt 279: Runs 72 (v) and 73 (g) read from job logs (artifact curl is blocked by the sim
  445eb1a ckpt 278: CUT-OFF SAVE (usage nearly out): HANDOFF.md section 0b written with exact stat
  80e19d2 ckpt 277: HANDOFF.md: phone guide row, Tj's next step, open requests list; ADB_PHONE.txt
  f2fdff4 ckpt 276: Run 71 (v): card area OK ([485,357][596,511] = 2 stock items vs full rect 303 
  498ad58 ckpt 275: Tj asked for a PHONE-side adb guide app (module phoneguide) with copy buttons,
  2d6e5f9 ckpt 274: V3 (patch = learned rect + 12dp margin, card = top two items), V4 (PatchCalibr
  aa72200 ckpt 273: Tj: patch must cover the whole 3-button menu, card = top two buttons only; pat
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
