# Animations off inside the Quickstep session only (TASKS BF1, written 2026-11-08)

Tj, 2026-11-07 23:36 UTC (verbatim): "Since this app already has adb permissions and accessibility access, is it possible for the Quickstep launcher to automatically turn off all animations (this is an accessibility setting). When I turn off animations it makes the tablet seem faster. But the animations must be turned back on automatically and immediately on both the lock screen and in the custom launcher. Animations should only ever be turned off inside quickstep launcher"

## What the "accessibility setting" is
"Remove animations" (Settings > Accessibility) writes three `Settings.Global` values to 0: `window_animation_scale`, `transition_animation_scale`, `animator_duration_scale` (AOSP `DisableAnimationsPreferenceController`). The launcher already holds the one-time adb grant `WRITE_SECURE_SETTINGS`, so it can write the same three values. `AnimationSpeedup` already did this for the double-Home reset (Y2b, a few seconds, put back exactly). BF1 adds a hold that lasts a whole Quickstep session.
"Inside Quickstep" = the Quickstep session (the stock home AND the apps opened from it: opening / closing an app is where the animation time is). If Tj meant only the stock home screen, that is a change of `AnimationPolicy.why` only (TASKS BF1j).

## The rule (`AnimationPolicy.why`, one function for the decision AND the words in the log / Control)
Animations are OFF only while all of these hold, in this order of "reason given when not": switch on, adb grant, accessibility helper connected, not paused (crash breaker / Restore everything), stored mode = Quickstep, screen on, lock screen NOT up, unlock choice already applied. Everything else = ON, put back to the values found before.
* The lock screen counts as the custom world (same idea as `TileProfile.effectiveMode`). Real animations are REQUIRED there: the lock-screen patch follows the stock power menu's 300 ms fade (`PatchFade`, Tj: "the timing is perfect").
* "Unlock not decided yet": after a PLAIN PIN the stored mode still says Quickstep for a few ms (`applyUnlockChoiceBody` flips it to custom AFTER `USER_PRESENT`). Without this fact the animations would blink off in the custom launcher right after every plain unlock that ends a Quickstep session. `AnimationSpeedup.lockCycle` is set at screen off and cleared by `onUnlockDecided` (or by a wake with no lock screen, or a 5 s valve).

## The holds (`AnimationSpeedup`)
Two reasons as a bit mask in prefs `tj_anim` (`holds`): RESET (the return reset, `begin` / `restoreLater` / `releaseReset`) and QUICKSTEP. The first reason saves the originals (disk first, then the zeros), the last one ending puts them back. A scale is put back only if it is still at OUR zero and the original was not zero itself (a value Tj sets in Developer options meanwhile is his; his own permanent "animation off" is never touched). A reason that joins an existing hold rewrites nothing.

## Triggers (level-triggered: every one re-reads the facts, none trusts the last event)
`Mode.set` (first thing after the commit) | SCREEN_OFF (inline, first, in the lock receiver) | SCREEN_ON (posted after the patch work) | `applyUnlockChoice` finally (`onUnlockDecided`, plus looks at +400 ms and +1.5 s because Android reports the keyguard gone a moment later) | helper connected / unbound / destroyed | start of the main process (`reconcileAsync`: at once with the helper assumed present, again after 2.5 s with the helper as it is) | window-change heartbeat (`tick`, at most 2 per second, nothing unless Quickstep AND switch on) | `RestoreStore.restoreAll` (switch false + closing sync) | the Control switch.

## Residual risks (stated honestly)
* A process killed mid-session leaves the scales at 0 until Android restarts it (seconds; the accessibility service is rebound with BIND_AUTO_CREATE). If the lock screen comes up in that gap it runs without animations until the restart; the restart then restores at once (emulator AN9b).
* Uninstalling the launcher in the middle of a Quickstep session leaves the scales at 0 (no uninstall hook). That is what Tj does by hand anyway.
* The stock Quickstep UI, the power menu and notifications in Quickstep run animation-free: intended.
* Not testable on the emulator: how much faster the real tablet feels; the real keyguard-gone timing (the two follow-up looks and the heartbeat cover it).

## Proof
JVM: `AnimationPolicyTest` (19). Emulator: section `an` (`tools/emulator_scenario.sh`, gate-map `an`): custom = untouched; Quickstep unlocked = 0 0 0; screen off = back within 2.5 s; lock screen = real; plain PIN = custom and NO new `ANIM OFF` line on the way; gesture = off after the unlock; double Home = real again; originals exact (0.5) and a hand-changed scale stays (1.5); kill mid-session = restored by the next start; killed + lock screen + restart = restored at once (log trigger `process start`); switch off = nothing changes. Debug builds default the switch OFF (like `clean_start`), the section turns it on, so the older sections are untouched.
Tablet: `research/TABLET_CHECKLIST_v0.8.6.md`.
