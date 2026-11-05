# "Keep Quickstep's apps open but hidden" and "a truly seamless second profile": is either practical? (AA4, 2026-11-05)

Tj's question (TASKS AA4): *"a truly seamless second profile, keeping Quickstep apps open-but-hidden" may be possible with deeper adb; consider whether it is practical without breaking anything.*

**Short answer: no. Every way of hiding a live app from the shared Recents/shade that Android 10 allows a shell user to try either does nothing, is rejected by the system, or only hides things in a way the user can undo with one swipe. A second Android user is the only real "profile", and it is the one option that can end in a factory reset. What is already built (Quickstep's apps are closed when the screen locks, and only Quickstep's own tasks are removed by id) is the best version of this idea that exists on this Android, and it costs one thing: Quickstep's apps do not keep their state across a lock.**

## What "open but hidden" would have to do (three things at once)

1. Quickstep's app and its task stay alive while the custom launcher is in front.
2. The custom side never shows it: not in Recents, not in the notification shade, not in the status bar.
3. Back in Quickstep the app is exactly where it was.

Everything below was tried on the CI emulator (Android 10, API 29, x86, 600x1024; shell user, no root). Run numbers are the GitHub "Emulator check" run numbers.

## The candidates, measured

| # | Candidate | What was measured | Verdict |
| :- | :- | :- | :- |
| 1 | Move the app's task into the HOME stack (`am stack move-task <task> 0 true`) | The system refuses: `IllegalArgumentException: moveTaskToStack: Attempt to move task 8 to stack 0` (`ActivityTaskManagerService.moveTaskToStack`); the task stayed where it was (run 102, `rs3` a) | **Impossible.** The home stack only takes home-type activities. |
| 2 | Move the task to a second display | Moving a stack to a (simulated) second display did **not** hide it from Recents (run 79, `rs2` c2, recorded in checkpoint 289). On a real tablet a second display exists only as the developer "simulate secondary display" overlay, which draws a visible window. | **Does not hide.** |
| 3 | Switch Recents off with the status-bar flag `DISABLE_RECENT` (0x1000000) | The shell may set it (`getDisableFlags` read back `[16777216, 0]`), but the Recents key **still opened Quickstep's `RecentsActivity`** (run 101, `rs4` a). | **No effect** on Android 10. |
| 4 | A Do-Not-Disturb policy that hides the notification list (`SUPPRESSED_EFFECT_NOTIFICATION_LIST`) | With the policy set by the shell (Do-Not-Disturb access granted to it), `zen_mode=1` and every `SUPPRESSED_EFFECT_*` bit set including `NOTIFICATION_LIST`, the test notification was **still listed in the shade** (1 time, same as the baseline); after the probe ended and `zen_mode` was back to 0 it was still there (run 105, `rs4` b). | **No effect.** Notifications cannot be hidden from the shade this way on Android 10. |
| 5 | Remove only Quickstep's tasks, by task id, when the screen locks (**built**, AA3) | The helper lists Recents tasks, remembers the custom account's at the start of a Quickstep session and removes only the new ones at the lock. Works for apps that exist only in Quickstep (Gallery, Email gone, Calendar untouched: run 99). A shared app (open in both worlds) is the hard case, see "what is still open" below. | **Built; the closest to the goal**, but the app is closed, not hidden. |
| 6 | Hide the status bar in custom (`policy_control immersive.status=*`, **built**, AA1) | The bar is hidden, but **one swipe reveals it and a second swipe opens the shade**; `cmd statusbar expand-notifications` still works; the lock screen is untouched (run 78). | **A look option, not a seal.** A sealed shade would also remove the rotation tile Tj wants in custom. |
| 7 | Keep our launcher resident with a foreground service (**built**, AA2) | The process stays in memory: `oom_score_adj` 50 with the service (100 without), survives `am kill-all` (run 99, `ka`). | Keeps **our** launcher warm for a faster unlock. It does nothing for Quickstep's apps. |
| 8 | A second Android user (a real profile) | Not built and not measured: it needs `pm get-max-users` > 1, which only the tablet can answer (the OTG probe asks). The switch lands on the second user's own lock screen and takes seconds on an MT8168; every app is installed twice; the user switcher is visible. Creating and removing users by adb is also the classic way to end up needing a factory reset. | **Rejected on safety** (L10, AB1): the one option whose failure mode is data loss. |

## Why none of them can work (the structural reason)

* Recents is one list per Android user, kept by the system (`ActivityTaskManager`), and drawn by **Quickstep**, the stock launcher, which is the registered Recents component. Our custom launcher shares that list; it does not own one.
* A shell user can create, move and **remove** tasks. It cannot mark a *live* task "exclude from Recents": only the task's own app can do that (`AppTask.setExcludeFromRecents`). `removeTask` ends the task, it does not hide it.
* Changing which component draws Recents or what the shade shows needs a system overlay or a modified system image; the bootloader is locked (see REEXAMINE, goal 2).

So the "hidden" half of the idea has no lever on a stock Android 10 Go tablet, and the "open" half (state kept) is exactly what removing the task gives up.

## What is still open (and is the honest cost)

* **State.** When the screen locks, Quickstep's apps are closed. A half-written message or a game in progress in Quickstep is gone at the next lock. The only way to keep it is to stop cleaning, which brings Quickstep's cards and notifications back into the custom side. That trade-off is Tj's call; the switch for it is "Close the apps opened from Quickstep" in Launcher control.
* **Shared apps (AA3).** If the same app is open in both worlds, Android decides what happens to its task, not us. Measured with the real Contacts app (run 103, `rt`): the custom account's Contacts task (id 8) was **replaced by a new task (id 20) once Quickstep opened Contacts again**, so at the lock there was no custom-account task left to spare, only Quickstep's. The clean-up did what its rules say (removed the new task by id, did not force-stop the app), which is also what protects the privacy goal. Because a real app cannot be made to keep two tasks on demand, the emulator proof of "only Quickstep's task goes" uses a CI test app with a standard launch mode (section `rt`, checks AA3-S0..S3). In everyday use the common case is that Android simply reuses the one task: then nothing is "new", nothing is removed, and the custom account's Recents shows that app as Quickstep left it. Only an app with a notification is force-stopped (AB3).

## Recommendation

Keep what is built: close-at-lock plus remove-by-id (AA3), the one-tile list, the optional status-bar hiding (AA1) and the optional keep-alive (AA2). Do **not** build a second Android user. If a later tablet probe shows `pm get-max-users` > 1 and Tj still wants a hard split, treat it as its own project with an emergency path first, not as an adb tweak.
