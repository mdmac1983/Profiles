# Launcher control: settings audit (AR1, 2026-11-06)

Tj: "Audit the settings in the launcher app to make sure they all work, they don't include contradictory settings or anything that would break, and improve the ui and organization for ease of use and intuitive design."

How the screen is built now: `ControlActivity` (opens only while Quickstep is on) shows a status card (Active launcher, Accessibility helper, adb permission, and the lock-screen patch state when it is in use),
warnings the launcher raised on its own (crash breaker, paused automation) and ten collapsible groups (the open / closed state is remembered per group; "n of m switches on" sits on every header).
`Restore everything` is a fixed footer outside the groups (the emulator section `ka` taps it by its text). Nothing was deleted: every switch of v0.7.7 is still there, regrouped.

## Where everything went (v0.7.7 section -> v0.8.0 group)

| v0.7.7 section | now |
| :- | :- |
| Launchers: Switch to custom / Choose apps | **Everyday** |
| Launchers: Keep Quickstep away unless a handshake | **Safety & secret ways to Quickstep** |
| Double-Home return: Open Solitaire automatically + app name | **Everyday** (it is not part of the return reset) |
| Double-Home return: the reset switches | **Return to the custom launcher** (sub-switches only when the master is on) |
| Recents (home-type, only-Solitaire) | **Recents & wallpaper** |
| Power menu | **Power menu (side key)** (lock-screen options only with a "hide Screenshot" option) |
| Lock screen (number) | **Lock screen** |
| Unlock into Quickstep (handshakes) | **Unlock into Quickstep (secret handshakes)** |
| Two profiles: gesture unlock back, prepare behind lock, Lock screen quiet | **Lock screen** |
| Two profiles: close Quickstep's apps at lock | **Return to the custom launcher** |
| Two profiles: radios, battery saver x3 | **Battery, radios & speed** |
| Two profiles: My photos, Recents filter | **Recents & wallpaper** |
| Two profiles: safety note | **Safety & secret ways to Quickstep** |
| Speed (keep-alive) | **Battery, radios & speed** |
| Invisible clean-up (adb shell helper) | **Advanced & diagnostics** |
| Back to the custom launcher / Secret ways / Master failsafe | **Return ...** / **Safety ...** |
| Quick settings, Keyboard | **Quick settings & keyboard** |
| Advanced (setup, guide) | **Advanced & diagnostics**; `Restore everything` = fixed footer |

## Contradictions and dead controls found, and what was done

1. "Lock screen: patch of my lock wallpaper + stock-size two-button menu" described the old card; with AM1 the menu stays stock. Reworded, and the switch (and its sub-switches) is shown only when the side-key option uses a cover; otherwise a note says it is unused.
2. "Run the return reset" claimed to be the master of everything in its section but "Open Solitaire automatically" also acts at boot and behind the lock screen: moved to Everyday. The remaining sub-switches are hidden while the master is off (a note says nothing is closed or cleared).
3. "Press Home twice" was drawn as a switch that snapped back on when tapped: now a plain line ("always on").
4. "Different tiles for each launcher": the two tile choosers did nothing when it was off: shown only when it is on.
5. "Last unlock" was printed three times: once now (Advanced).
6. Texts that said "above" / "below" across what are now different groups were pointed at the group names.
7. The wallpaper access button now says whether the permission is granted (the patch calibration needs it).
8. Defaults in the UI were compared with the defaults where the keys are read (PatchRest, ResumeTarget, ReturnReset cover, StrayHomeGuard, lock patch, startup app, pencil, picker, Recents filter, power card): no mismatch.

## Switches that overlap on purpose (not contradictions)

* Battery: "off in the custom launcher" + "on in Quickstep" act only when the launcher CHANGES; "Strict" additionally undoes changes behind their back. Strict needs the other two to mean anything.
* Recents: home-type (never listed), only-Solitaire (apps started from the custom launcher excluded), drawer-only filter (cards of non-drawer apps closed when Recents opens), return reset (Quickstep-opened apps closed at the return). They stack; with only-Solitaire on, drawer apps never reach Recents at all.
* Lock quiet (lock screen, every launcher) and "no Wi-Fi / Bluetooth in the custom launcher" (custom launcher always) are separate rules; a gesture unlock into Quickstep puts the radios back only if the first one's put-back switch is on.
* Power menu: the side-key option decides the HOME screen; the lock-screen patch / resting patch / own card switches decide the LOCK screen.

## Safe-by-default and restore

* Defaults ON (daily use): lock number, lock quiet (+ put back), prepare behind lock, gesture unlock back, return reset (+ notifications, Quickstep apps, cover, animations), battery off-in-custom and on-in-Quickstep, drawer-only Recents, picker hider, home-type, pencil cover, per-launcher tiles, stray-home guard, Konami key code, the lock patch and the resting patch (the patch only shows once calibrated).
* Defaults OFF: power menu option (Leave it alone), own card, Strict battery, only-Solitaire Recents, keep-alive, keyboard switch, status-bar hide, Solitaire hotspot.
* `Restore everything` switches every feature above off and puts the original system settings back (emulator section `rb` / `ka`); the crash breaker never wipes the user's switches (KA1 fuse).

## What proves what (emulator sections, see tools/gate-map.txt)

Lock number + patch: x1, v, ae, ps. Handshakes / plain PIN: ac, e1, o, uf. Battery: ac. Recents: rg, legacy, b4, pf. Picker: ao. Return reset: y2, b4, n6, pf, rt. Tiles: t. Pencil: b6. Keyboard: (adb only). Status bar: im. Keep-alive: ka. Secret entrances: se. Restart: rb. Control screen layout: base (every group header on the page; Restore everything reachable).
Not provable on an emulator (needs the tablet): the keyboard switch (SwiftKey), the real wallpaper set of the picker, MediaTek-specific behaviour of the battery tile.
