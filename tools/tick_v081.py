#!/usr/bin/env python3
"""Final v0.8.1 ticks in TASKS.md. Run from the repo root AFTER the final gate is green.
Ticks `- [ ] ID` lines (top level or indented) and appends the proof. Items with sub-boxes keep their sub-boxes as written.
"""
import re, sys
P = "TASKS.md"
T = open(P).read().split("\n")

PROOF = {
    "AU2": "v0.8.1: JVM AutoCalibrationTest (owner/menu/PIN-pad/unlock refusal) + emulator pa PA 3 (a screenshot with the menu open is refused)",
    "AU3": "v0.8.1: automatic calibration (AW5) means an uncalibrated lock screen calibrates itself on the next plain lock screen; emulator pa",
    "AU4": "v0.8.1: PatchShot.covers + PatchShot.invalidate (a stored crop that no longer covers the window is forgotten, so it is calibrated again); JVM PatchShotTest",
    "AU6": "v0.8.1: cancelPatchWork in onDestroy/onUnbind; onInterrupt ends the calibration window; code review",
    "AU7": "v0.8.1: PatchShot.summary checks the signature; Control chip uses valid/never; JVM",
    "AU8": "v0.8.1: Control wording corrected",
    "AW4": "v0.8.1: emulator lq 'LQ/AW4 the lock number is drawn on the plain lock screen locked from Quickstep' PASS",
    "AW5": "v0.8.1: AutoCalibration + switch patch_auto_cal (default ON); emulator pa PASS (MAE 2e-07); gate",
    "AX1": "v0.8.1: root cause found and fixed, see AZ2 (research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md); emulator bs 13 PASS; TABLET: Tj to try",
    "AZ3": "v0.8.1: 50 old boxes ticked/closed with evidence on 2026-11-06",
    "AZ1a": "v0.8.1: PatchFade (LEAD_MS 10) + PatchFadeTest",
    "AZ1b": "v0.8.1: fast path on the dialog's own window event, quietBurst/deferWork, touchable flag off the critical frame, async recordLatency, hold confirmation (ckpt 536-545)",
    "AZ1d": "v0.8.1: emulator section pt (tools/pt_analyze.py, animations ON): worst mismatch while opening 2-6 % vs 26 % for the old jump (runs 249, 253)",
    "ACC1": "v0.8.1: HelperStatus + Control chip 'enabled but NOT connected'; JVM HelperStatusTest",
    "UX1": "v0.8.1: SetupActivity explains the grey button when the helper is not running",
}
SUPERSEDED = ["AC3", "AC-C", "AG1", "AG2", "AN1b", "AN1"]
done, missing = [], []
for key, note in list(PROOF.items()) + [(k, "superseded by AZ2 (v0.8.1: the real cause was Android's battery saver state machine going out of step after the launcher's direct writes; the launcher no longer switches the saver ON)") for k in SUPERSEDED]:
    pat = re.compile(r"^(\s*)- \[ \] " + re.escape(key) + r"\b")
    hit = False
    for i, l in enumerate(T):
        if pat.match(l):
            T[i] = l.replace("- [ ] " + key, "- [x] " + key, 1) + "  [" + note + "]"
            hit = True
            done.append(key)
            break
    if not hit:
        missing.append(key)
open(P, "w").write("\n".join(T))
print("ticked:", " ".join(done))
print("not found / already ticked:", " ".join(missing))
