# The faint line under the hidden card = the stock panel's shadow (BB1, 2026-11-07)

**Symptom (Tj, v0.8.2, 05:17 UTC):** "It is almost perfect but there is still a very slight tinted line about where the bottom of the card would be. Like a faded tint." (After v0.8.2 the square itself was invisible: BA1 worked, see `SCRIM_MEASUREMENT_2026-11-07.md`.)

## What it is
The lock-screen patch hides the stock menu's third slot (Screenshot). The stock panel (3 entries) casts an **elevation shadow downward** from its real, hidden bottom edge. The patch's opaque core reached only `EDGE_DP` = 2 dp (2 px) past that edge and then **faded out over `FEATHER_DP` = 8 dp (9 px)** (`PatchRest.slotWindow`, `TjAccessibilityService.buildPatchMasks`), so the shadow showed through the fringe and below the window: a soft dark band right under the patch.

## Evidence (Tj's two v0.8.2 photos, 2026-11-07 00:20 / 00:21 tablet time; the three older photos he re-attached were the v0.8.1 ones and were ignored)
Raw row profile of the columns inside the patch, across its bottom edge (device px below the card's visible bottom, luminance 0-255):

| photo | before the edge | at / after the edge | reading |
| :- | :- | :- | :- |
| 00:21 | 67.3 (d 105) | 64.2 (108), 59.5 (111), **57.3 (114)**, 57.8, 58.4, 58.8 (123) | a 6 to 10 level drop, recovering over ~15 px; the control strip beside the card smooth |
| 00:20 | 79.9 (d 114) | 77.5 (117), 73.3 (120), **69.3 (123)**, then flat | the same band |

* The patch **interior** is right: at its left edge the step is only +1 to +2 levels (+1.6 raw; it was -4.6 on v0.8.1), and the interior follows the wallpaper's own gradient. The shadow is cast downward, so the side shadow is weak.
* `tools/patch_photo_step.py` read these photos as "patch LIGHTER by 5 to 7.5": that sign is an artefact of the dark band (it pulls the below-edge fit down). Do not read it as an overshoot.
* The camera's bloom around the bright card hides any side shadow in the photos (the wallpaper next to the card reads +3 to +12 % brighter within 8 device px), which is why the emulator, not the photos, has to measure the shadow's shape.

## The fix (BB1b, v0.8.3)
* The opaque core now reaches further DOWN (`PatchRest.EXTRA_BOTTOM_DP`, hidden pref `patch_extra_dp`, 0 = the v0.8.2 geometry); the bottom fringe width is the hidden pref `patch_bfeather_dp`. The sides keep their margin.
* The stored real-pixel crop is the window + `PatchShot.MARGIN_PX` (24 px) on every side and `PatchShot.covers` is inclusive, so the window may grow downward only within that margin minus a 3 px slack (`PatchRest.clampExtra`, JVM-tested): existing calibrations stay valid, no new screenshot. A deeper extension needs a larger margin AND a recalibration of the stored crop.

## The probe (how to read it)
`tools/pb_probe.py` (self-test: `python3 -I tools/pb_probe.py --selftest`), run by emulator section `pt` phase D on screenshots of the SAME lock screen: menu closed (bare), menu open with the lock patch OFF (the stock panel's shadow), and with it ON in several geometries. The stock scrim is fitted per run (`shown = s * before + o`), so the emulator's scrim level does not matter. Output lines `PBMETRIC` (`shadow_max` = deepest darkening below the panel in levels, `shadow_ext` = farthest px where it is still >= 1 level; `line_max` / `line_at` = the largest deviation of the patch run in the zone 0..60 px below the panel; `core_mean` = the mean deviation inside the slot, ~0 = the patch matches) and `PBPROFILE` (the deviation per distance).
