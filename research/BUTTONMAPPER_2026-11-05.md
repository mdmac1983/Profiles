# Button Mapper and the power key: is the pasted claim viable? (AB5, 2026-11-05)

**Short answer: no.** Button Mapper cannot take over the power (side) key, `WRITE_SECURE_SETTINGS` does not raise any "priority above the accessibility layer", and the "Custom Power Menu" it offers opens Android's own power menu, the one with Screenshot. The claim describes a mechanism that does not exist. A different route that *does* work on this kind of tablet was found and measured (section 4); it is not built yet and needs Tj's go-ahead.

## 1. What the pasted text claims, point by point

| Claim | Verdict | Why |
| :- | :- | :- |
| Button Mapper "hijacks the physical button before the stock system menu can render" | **False for the power key** | The app's own store text says it does not work with on-screen buttons **or the power button** (mirrors of the Play listing: [APKMirror, Button Mapper 3.17](https://www.apkmirror.com/?p=5425138), [APKMirror 3.14](https://www.apkmirror.com/?p=5424320), [BlueStacks listing](https://www.bluestacks.com/campaign/flar2.homebutton/ko)). It remaps keys that Android hands to an accessibility service's key filter (volume, camera, assistant, headset and some vendor keys). |
| `adb shell pm grant flar2.homebutton android.permission.WRITE_SECURE_SETTINGS` "elevates the app's priority above the standard accessibility layer" | **False** | Android has no key-handling priority tied to a permission. `WRITE_SECURE_SETTINGS` only lets an app write `Settings.Secure` values (for example to switch its own accessibility service on). In Button Mapper it unlocks the *adb-dependent* extras: working while the screen is off and simulating key codes ([same listings](https://www.apkmirror.com/?p=5425138): "does not work when the screen is off unless rooted or you run an adb command"). |
| "hooks deeply into the hardware button listeners" | **False** | There is no such hook for an app. Key events go: kernel -> `InputDispatcher` -> `PhoneWindowManager.interceptKeyBeforeQueueing` (inside system_server) -> only then the accessibility key filter and apps. The power key is handled in that first stage: it starts the long-press timer that opens the power menu and is not passed on. Our own helper recorded this earlier ("the side key is consumed before the key filter and never reaches a service", comments in `TjAccessibilityService`, "D3"); section 3 below re-measures it. |
| Long-press action "Custom Power Menu" | **Not custom** | The accessibility API has one power action, `GLOBAL_ACTION_POWER_DIALOG` (API 21): it opens the **system's** power dialog, the same one that has Screenshot. Button Mapper's "Power menu" action is that call. |
| "blocking the stock LineageOS menu" | **Unverified assumption** | Nothing shows the tablet runs LineageOS (it is an Alps MT8168, Android 10 Go). The phone guide has a card that prints `ro.lineage.version` (section 7); if it prints nothing, the ROM is not LineageOS. |

## 2. What the Android sources and the web say (and what they do not)

* The accessibility `onKeyEvent` hook lets a service consume a key or let it continue, but it "will not be able to inject key events or modify the observed ones" (AOSP commit text, mirrored e.g. in [/e/OS frameworks_base](https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/ca4dca12e5356657fad10149a076540b105d6e9e) and discussed in [this write-up](https://blog.joshsoftware.com/2018/03/28/android-accessibility-service-customization-for-keypress-event/)).
* `PhoneWindowManager.interceptKeyBeforeQueueing` has a `KEYCODE_POWER` branch of its own and runs in system_server before the event is queued ([commit logging that branch](https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/24201e6ec62a1d8ead600a1ad75fe41c23267844)).
* **Gap, said plainly:** no search result states outright that the accessibility key filter is bypassed for `KEYCODE_POWER`. The statement above is the reading of the code path plus our own measurement (next section), not a quotation.

## 3. Our own measurement (emulator, Android 10, section `rs3`, step b0)

RS3_KEYRAW_PLACEHOLDER

## 4. What would work: "Power menu v2" (not built; needs Tj's decision)

Measured on the emulator (`rs3` b2/b3): the **shell user may set `StatusBarManager.DISABLE2_GLOBAL_ACTIONS` (0x8)**. While the setting process lives, pressing the power key does **not** open the stock menu at all; when the process ends Android clears the flag and the stock menu is back. So:

1. the shell helper (already needed for the clean-up) holds the flag and reads the power key from the kernel input device with `getevent` (the shell can list it: `getevent -pl` shows `KEY_POWER`; whether it can *read* it on the tablet is the one thing still to prove, the guide has a card for the device name);
2. when the key has been held for the long-press time, the launcher shows its **own** card (Power off / Restart, no Screenshot), drawn as an overlay, and does the action itself (`svc power shutdown` / `svc power reboot` run by the helper; the shell holds `REBOOT`);
3. fail-open: if the helper dies the flag clears and the stock menu comes back; if our card does not appear within a short watchdog the helper clears the flag at once, so Tj can never end up with *no* power menu. A held power key still hard-resets the tablet as on any Android.

What it buys over today's design (the accessibility cover over the Screenshot entry): the stock menu is never drawn, so there is no flash and no timing race. What it costs: it works only while the helper runs (one pasted line per boot, like the clean-up), and it adds a second place that handles the power key. Today's cover keeps working without the helper and stays as the fallback.

## 5. Recommendation

* Do **not** install Button Mapper for this; it cannot do it and the permission grant would only add a risk surface.
* Keep the current cover. If the brief flash of the stock menu is still a problem on the tablet, say so and "Power menu v2" is the next build; before that, one probe line on the tablet (guide section 9, already listed) confirms the shell may set the flag there too.
* Open question for the tablet: `getprop ro.lineage.version` (guide section 7).

Sources: [APKMirror Button Mapper 3.17](https://www.apkmirror.com/?p=5425138) · [APKMirror 3.14](https://www.apkmirror.com/?p=5424320) · [BlueStacks listing](https://www.bluestacks.com/campaign/flar2.homebutton/ko) · [/e/OS frameworks_base commit](https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/ca4dca12e5356657fad10149a076540b105d6e9e) · [/e/OS PhoneWindowManager power-key commit](https://gitlab.e.foundation/e/os/android_frameworks_base/-/commit/24201e6ec62a1d8ead600a1ad75fe41c23267844) · [Josh Software, accessibility key events](https://blog.joshsoftware.com/2018/03/28/android-accessibility-service-customization-for-keypress-event/)
