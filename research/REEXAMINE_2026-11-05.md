# Re-examining every goal with fresh eyes (Tj, 2026-11-05)

Read with PROFILES_2026-11-04.md, IDEAS_2026-11-04.md, NOTES_2026-11-04.md. This doc only adds what is
**NEW** (not in those), or re-ranks. Tags: **[NEW]** idea not raised before · **[DONE]** already shipped ·
**[RULED-OUT]** proven impossible here · **[PROBE]** needs one read from your tablet to decide.
Nothing here is built yet (L10: no unasked changes). The list ends with "what I'd build first" for your yes/no.

## The one thing that unlocks the most: run the probe once
Almost every "best-case, no-delay, no-compromise" answer below is gated on facts only your tablet has. One OTG
session with the PhoneGuide app (or `tools/adb-all.sh`) answers all of these at once:
- `settings get secure power_menu_actions` + the `lineagesettings` provider → **[PROBE]** if LineageOS exposes it,
  the Screenshot button is removed **natively, zero delay, no overlay** (goal 2 fully solved).
- `pm get-max-users` → **[PROBE]** whether a real second Android user (true profile separation) is even possible.
- `getprop persist.adb.tcp.port` / `service.adb.tcp.port` + whether "ADB over network" is a persisted Developer
  Option → **[PROBE]** whether the launcher can be its **own adb helper after every reboot with no PC** (the single
  biggest restart-survival win — see goal 9).
- `settings get global device_demo_mode` round-trip → confirms the native pencil+gear removal (goal 4).
- pull the Settings APK → exact look for the clone (goal 5).

So: the highest-value next action is not code, it's one probe run. Everything else is ranked assuming no new adb.

---

## Goal 1 — two launchers as two "profiles"
**[DONE]** layered one-user design: router is the only Home, unlock handshake picks the mode, per-mode tiles /
keyboard / Recents / power menu, invisible cleanup at lock via the helper. **[RULED-OUT]** seamless second user
(switch lands on its own lock screen, seconds on an MT8168).

- **[NEW] Seal the custom side with `policy_control` immersive.** Writing `Settings.Global.policy_control =
  immersive.status=*` while custom is active hides the **status bar**, so the Quickstep notification shade cannot be
  pulled down in custom **at all** — no shared shade, no Quickstep notifications, no pencil, no gear, by construction
  instead of by covering. Keep 3-button nav (use `immersive.status`, not `immersive.full`). One secure setting,
  snapshot+restore like the tiles, **survives reboot**, needs the WRITE_SECURE_SETTINGS grant. This is the closest
  thing to a real "walled profile" without a second user, and it retires the pencil/gear/shade problems in one line.
  Cost: custom loses its own clock/battery in the status bar (plain, which you said you want). Worth an emulator test.
- **[NEW] Direct-Boot-aware handshake** — see goal 9; makes the "two profiles" split exist on the very first boot PIN
  screen, which today it does not.
- **[NEW, high-risk] Device owner for ONE narrow win:** `setPersistentPreferredActivity` sets a default Home that a
  launcher crash cannot clear (fixes the "Choose a Home app" dialog, the one accepted cost of the HOME-type task). I do
  **not** recommend full lock-task kiosk, only this single call, and only `testOnly` so `dpm remove-active-admin` always
  backs out. Flagging it because it is the clean fix to the crash cost; your call whether the risk is worth it.

## Goal 2 — hidden Screenshot, no delay
**[DONE]** two-button card over the real menu (works, ~50–150 ms to appear). **[RULED-OUT]** input monitor
(MONITOR_INPUT refused), RRO overlay of `config_globalActionsList` (locked bootloader), any earlier signal than the
menu window (PhoneWindowManager sends nothing before it).

- **[PROBE → likely NEW win] LineageOS `power_menu_actions`.** If present, drop `screenshot` from that secure/lineage
  value and the real menu renders with Power off + Restart only, **no card, no delay, nothing to hide**. If the helper
  is running the launcher can write it itself per mode. This is the single best outcome for this goal and it is one
  probe read away. Elevated to top priority.
- **[NEW] If no Lineage setting: block the menu, offer our own power actions.** Keep the side-key long-press from
  producing the stock menu in custom (our card already covers it), and add real "Power off" / "Restart" actions that
  call the helper (`reboot`, `svc power shutdown`). Then there is no stock menu to race at all in custom. Costs the
  stock menu in custom (you only want Power off + Restart there anyway). No-adb path keeps today's card.

## Goal 3 — fastest launcher startup (keep in RAM / prep ahead)
**[DONE]** pre-arm custom home + Solitaire behind the keyguard at screen-off; HOME-type resume; `compile -m speed`
(U2); cleanup done at lock so a plain unlock has no work.

- **[NEW] Keep the launcher process resident with a minimal foreground service.** On a 1.3 GB Go tablet the launcher
  can be killed under memory pressure → the next unlock is a cold start. A tiny foreground service (raised oom_adj) +
  the battery whitelist you already grant the helper keeps it in RAM, so unlock is a surface flip, not a process
  start. Cost: one persistent silent notification — and in custom the shade is hidden/cleared anyway, so you never see
  it. This is the most direct answer to "kept in ram / preloaded." Worth building + measuring ("Last unlock" already
  logs the ms).
- **[NEW] Pin Solitaire + custom home into memory at SCREEN_OFF, not at wake.** The pre-arm already starts them behind
  the keyguard; combined with the keep-alive service above, both are warm before you ever press the button. The only
  remaining unlock cost is Android's own keyguard-dismiss animation, which is not ours to remove without adb.
- Honest ceiling: with the helper running + process kept warm, unlock is already "a normal unlock." Truly instant
  (no keyguard animation) is the adb/`policy_control` territory, not an app's to take.

## Goal 4 — rotation-only tile, hidden pencil, custom Settings clone
**[DONE]** `sysui_qs_tiles` per mode (needs grant); pencil cover (reactive); Settings-clone plan (front door + guard).
- **[DONE/PROBE] `device_demo_mode=1` removes pencil AND gear natively** (emulator run 55) — no cover, no delay.
  Confirm on tablet; if good it replaces the reactive pencil cover entirely.
- **[NEW] If `policy_control` immersive is adopted (goal 1), the pencil/gear problem disappears** without demo mode,
  because the shade is unreachable in custom. Two independent routes to the same no-delay result; pick whichever your
  tablet likes.
- Settings clone: plan unchanged (front door that opens the REAL pages, guard bounces disallowed ones). **[NEW] cut
  the guard's flash** by having the front door only ever deep-link allowed pages, so the guard is a rare safety net,
  not the main mechanism. Send screenshots + the pulled Settings APK when ready.

## Goal 6 — editable tiles in Quickstep, permanent single tile in custom
**[DONE]** per-mode `sysui_qs_tiles`; the min-tile rule lives only in SystemUI's editor, so writing the list directly
bypasses it; the custom guard re-asserts one tile. `sysui_qs_tiles` **survives reboot** on its own; the launcher
re-asserts on every mode switch and on boot, so an accidental change in Quickstep never leaks into custom.
- **[NEW] re-assert the custom tile list on BOOT explicitly** (not only on mode switch) so a restart that lands in
  custom is already one-tile before the shade can be opened. Cheap addition to the boot path.

## Goal 7 — only custom-opened apps in custom Recents
**[DONE]** custom launches excluded except Solitaire; launcher is HOME-type (never listed); Quickstep-origin apps
tracked (AppOrigin) and cleared on return.
- **[NEW] With the helper: remove Quickstep-origin tasks by task-id, invisibly.** AppOrigin already knows which tasks
  are Quickstep's; the helper can `am` remove exactly those task-ids at lock — no "Clear all" UI, no cover, instant,
  and custom's own Recents is untouched. This is the crispest version of "two separate Recents" short of a second
  user, and it reuses tracking that already exists.

## Goal 9 — survive a tablet restart
**[DONE/table in PROFILES N7]** app, default Home, every switch, mode, tile list, keyboard choice and all `pm grant`
permissions survive. Two things do not, and both have **[NEW]** angles:
- **The adb helper dies at every boot.** **[PROBE] self-adb over TCP to localhost:** if the tablet persists "ADB over
  network," the launcher can speak the adb protocol to 127.0.0.1 itself after boot (one "Allow" tap the first time) and
  re-start its own helper — no PC, ever again. This single capability makes *every* adb feature reboot-proof and is the
  highest-leverage unknown. Needs the probe to confirm the port/persistence.
- **Accessibility can't run before the first unlock (FBE).** So the lock-number, handshakes and Konami code don't exist
  on the *boot* PIN screen. **[NEW] Direct-Boot-aware component:** mark the handshake watcher `directBootAware` and keep
  its prefs in device-protected storage; Android 10 Direct Boot then lets it start at `LOCKED_BOOT_COMPLETED`, before
  the first unlock. Caveat: AccessibilityManager may still defer binding a11y services until unlock even when
  directBootAware — this is exactly the kind of thing the emulator can test cheaply before promising it. New avenue,
  worth one experiment.

---

## What I'd build first (your yes/no — nothing starts without it)
1. **Probe run** (you, one OTG session): unlocks goals 2, 9, 5 and the max-users question. Highest value, no code.
2. **[NEW] `policy_control` immersive.status in custom** (emulator-test first): seals the shade → fixes shade/pencil/
   gear/Quickstep-notifications in one reboot-surviving setting. Needs the grant you already plan.
3. **[NEW] keep-alive foreground service**: launcher stays in RAM → fastest unlock. Measurable.
4. **[NEW] helper task-id Recents removal** (if you run the helper): crisp per-launcher Recents, invisible.
5. **[NEW] Direct-Boot handshake experiment** (emulator): would make the profile split exist on the first boot screen.
6. Re-assert custom tiles on boot; cut the Settings-guard flash — small, do alongside the above.

Lower / already covered: second Android user (not seamless), device owner (risk; only the one crash-proof-home call is
tempting), live screen mirror (impossible on Android 10), keeping Quickstep apps open-but-hidden (not seamless — PROFILES §6).
