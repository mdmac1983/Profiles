# Notifications in the custom launcher and on the lock screen (TASKS BH1, 2026-11-08)

Tj, 2026-11-08 02:29 UTC (verbatim): "When I unlocked to the custom launcher from the lock screen, there was a notification from a quickstep app. Turn all notifications off for both the lock screen and the custom launcher. The lock screen (no matter which launcher it was on) and the custom launcher should never show any notification at any time, unless it can safely and without delay or flicker only include notifications from apps that are in the custom launcher app drawer and not hidden"

**Short answer.** On this Go tablet nothing in the app can see or cancel another app's notification before SystemUI shows it, so "only the drawer apps' notifications, without flicker" is not possible; "none at all" is possible for the lock screen (already built) and for the POP-UP BANNER in the custom launcher (v0.8.8). The status-bar icon and the shade list of a notification that has arrived cannot be hidden by the app; they can only be cleared (at the start of a custom session) or hidden together with the whole status bar.

## What existed before v0.8.8
* **Lock screen = AK1 `LockQuiet`** (v0.7.x): `lock_screen_show_notifications = 0` and `lock_screen_allow_private_notifications = 0`, held all the time while its switch is on, from any launcher. Report line `lock screen quiet: ... lock_screen_show_notifications = <value>` shows whether the write took on the tablet (nothing measured there since 2026-11-05).
* **Custom launcher = a clean-up at the START of a custom session** (`ReturnReset`): shade "Clear all" behind the black cover, skipped when the status bar shows no notification icon; with the adb helper alive: force-stop of apps that have notifications (`NOTIFPKGS`). Nothing handled a notification that ARRIVES later.

## The levers, measured or read (what each hides, what it needs, verdict)
| Lever | Hides | Needs | Verdict |
| :- | :- | :- | :- |
| `lock_screen_show_notifications=0` | everything on the lock screen, icons included | adb grant (held) | BUILT (AK1) |
| `heads_up_notifications_enabled=0` (Settings.Global) | the pop-up banner only | adb grant (held) | **BUILT v0.8.8**; emulator run #340: 0 banners in the custom launcher, 1 in an unlocked Quickstep session, same notification |
| Notification listener (cancel / filter per app) | anything, per app | notification access | DEAD on Go: `canUseManagedServices` refuses (TASKS STEP 17) |
| Do-Not-Disturb policy, visual-suppression bits | the shade list did NOT disappear (run 105, `rs4` b); status-bar-icon and banner bits never measured | policy access, probably refused on Go too | not built |
| `policy_control immersive.status=*` (AA1, switch "hide the status bar in custom", default OFF) | the whole status bar, icons included; one swipe reveals it, a second opens the shade | adb grant | EXISTS as a look option; Tj's call |
| `cmd notification suspend_package <pkg>` (helper) | the CURRENT notifications of a package, without clearing them; new posts are shown | the adb helper alive (HLP1: unproven, dies in minutes) | a possible janitor, with a short icon flash; not built |
| real `pm suspend` (helper) | blocks posting, but kills the Recents card and can leave an app "paused" if the helper dies | helper | rejected (stranded state needs the cable) |
| StatusBarManager disable flags (helper) | notification icons / alerts / the shade | helper alive; they vanish the moment it dies | possible only with a persistent helper (HLP1 / idea D) |
| Accessibility event `TYPE_NOTIFICATION_STATE_CHANGED` | nothing, but it names the POSTING APP at post time | the helper (already enabled) | **BUILT v0.8.8** as "sightings" |

## What v0.8.8 does
1. **`NotifQuietPolicy` / `NotifQuietHold` / `NotifQuiet`**: pop-up banners are off whenever the custom launcher is in front, or the tablet is dark or locked in ANY launcher, or a plain PIN is still being decided; they are put back exactly as found in an unlocked Quickstep session. It is the exact complement of `AnimationPolicy` (BF1) and rides the same level-triggered `AnimationSpeedup.syncNow` (mode change, screen off / on, unlock decided, helper connect / unbind, process start, the 2 Hz heartbeat in Quickstep, Restore everything, the switch). The saved original lives in `Settings.Global tj_banners_original` (survives Clear data). A Quickstep session without the helper never holds (nothing could end the hold).
2. **`NotifSightings`**: which app posted, when, in which state, in the drawer or not (package only, never text), a ring buffer of 40 + lock-log lines + report line; plus a flag "something was posted since the last shade clear" that makes the clean-up open the shade even when no icon shows (a silent notification has no icon). Limits: a MINIMIZED notification sends no event; one extra shade step per batch of arrivals.
3. Switch "No pop-up notifications outside Quickstep" (Launcher control > Everyday, release ON, debug OFF) with a live line; report lines `pop-up banners (BH1 ...)` and `notifications seen (BH1 ...)`.

## Not done, and what it would take (Tj's choice; recommendation first)
* **Wait for the tablet's answer** (BH1c: where was the notification seen, which app; the report's `notifications seen` line). If it was the banner: done. If it was the status-bar icon or the shade: the next step is one of the following.
* **Turn on "hide the status bar in custom"** (exists): removes the icons; costs the clock / battery / Wi-Fi in custom. No code.
* **A helper janitor** (`suspend_package` on each sighting from a hidden app): hides it within about 100 ms of posting, with a short icon flash; works only while the helper lives (HLP1).
* **A persistent helper** (HLP1 / idea D, one cable session) would also allow the disable flags: no icons and no banners from the system itself, with the shade untouched.

## Where
`NotifQuietPolicy.java`, `NotifQuietHold.java`, `NotifQuiet.java`, `NotifSightings.java`; hooks in `AnimationSpeedup.syncNow` / `tick` / `Look`, `RestoreStore.restoreAll`, `ReturnReset.run` (shade step), `TjAccessibilityService.onAccessibilityEvent` (early return for the notification event), `tj_accessibility.xml` (event type), `ControlActivity`, `ReportState`. Tests: `NotifQuietPolicyTest` 16, `NotifQuietHoldTest` 15, `NotifSightingsTest` 10; emulator section `nq` (NQ0-NQ10, run #340 20 PASS on the first version). Code review (`/code-review high`, 8 findings, all handled): TASKS BH1d.
