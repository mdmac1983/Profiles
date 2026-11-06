# Android 10 source excerpts that decide AZ1 (power-menu fade) and AZ2 (battery saver) — 2026-11-06

Source: AOSP tag `android-10.0.0_r9` (https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-10.0.0_r9/...),
Apache License 2.0, Copyright The Android Open Source Project. Excerpts only, kept here so a new session does not have to fetch them again
(Claude's Bash hook blocks `curl`; the sanctioned route is the `web-fetch` agent). Indentation was flattened by the fetch tool; the tokens are verbatim.
Tj's tablet runs MediaTek's Android 10 (Alps 711, MtkSystemUI): MediaTek may have changed details, so everything below is "AOSP says"; what the tablet does must still be measured.

## 1. AZ2 — why the battery saver cannot be turned off (or on) after the launcher wrote `Settings.Global.low_power`

File: `services/core/java/com/android/server/power/batterysaver/BatterySaverStateMachine.java` (the class is driven by `PowerManagerService`).

The state machine keeps its OWN state `mState` (STATE_OFF, STATE_MANUAL_ON, STATE_AUTOMATIC_ON, STATE_OFF_AUTOMATIC_SNOOZED, STATE_PENDING_STICKY_ON).
A request from the tile or the Settings button arrives as `PowerManager.setPowerSaveModeEnabled(boolean)` -> `PowerManagerService.setLowPowerModeInternal` (returns TRUE unless the device is plugged in)
-> `BatterySaverStateMachine.setBatterySaverEnabledManually(enabled)` -> `updateStateLocked(true, enabled)`:

```java
private void updateStateLocked(boolean manual, boolean enable) {
    if (!manual && !(mBootCompleted && mSettingsLoaded && mBatteryStatusSet)) { return; }
    switch (mState) {
        case STATE_OFF: {
            if (!mIsPowered) {
                if (manual) {
                    if (!enable) {
                        Slog.e(TAG, "Tried to disable BS when it's already OFF");
                        return;                                   // <-- IGNORED, no error to the caller
                    }
                    enableBatterySaverLocked(/*enable*/ true, /*manual*/ true, BatterySaverController.REASON_MANUAL_ON);
                    hideStickyDisabledNotification();
                    mState = STATE_MANUAL_ON;
                } else if (isAutomaticModeActiveLocked() && isInAutomaticLowZoneLocked()) {
                    enableBatterySaverLocked(true, false, BatterySaverController.REASON_PERCENTAGE_AUTOMATIC_ON);
                    hideStickyDisabledNotification();
                    mState = STATE_AUTOMATIC_ON;
                } else if (isDynamicModeActiveLocked() && isInDynamicLowZoneLocked()) {
                    enableBatterySaverLocked(true, false, BatterySaverController.REASON_DYNAMIC_POWER_SAVINGS_AUTOMATIC_ON);
                    hideStickyDisabledNotification();
                    mState = STATE_AUTOMATIC_ON;
                }
            }
            break;
        }
        case STATE_MANUAL_ON: {
            if (manual) {
                if (enable) {
                    Slog.e(TAG, "Tried to enable BS when it's already MANUAL_ON");
                    return;                                       // <-- IGNORED
                }
                enableBatterySaverLocked(false, true, BatterySaverController.REASON_MANUAL_OFF);
                mState = STATE_OFF;
            } else if (mIsPowered) {                              // the ONLY non-manual exit of MANUAL_ON is being plugged in
                enableBatterySaverLocked(false, false, BatterySaverController.REASON_PLUGGED_IN);
                if (mSettingBatterySaverEnabledSticky && !mBatterySaverStickyBehaviourDisabled) { mState = STATE_PENDING_STICKY_ON; } else { mState = STATE_OFF; }
            }
            break;
        }
        case STATE_AUTOMATIC_ON: {
            if (mIsPowered) { enableBatterySaverLocked(false, false, REASON_PLUGGED_IN); mState = STATE_OFF; }
            else if (manual) {
                if (enable) { Slog.e(TAG, "Tried to enable BS when it's already AUTO_ON"); return; }   // <-- IGNORED
                enableBatterySaverLocked(false, true, REASON_MANUAL_OFF);
                mState = STATE_OFF_AUTOMATIC_SNOOZED;
            } else if (isAutomaticModeActiveLocked() && !isInAutomaticLowZoneLocked()) { enableBatterySaverLocked(false, false, REASON_PERCENTAGE_AUTOMATIC_OFF); mState = STATE_OFF; }
            /* ... dynamic mode branches ... */
            break;
        }
        case STATE_OFF_AUTOMATIC_SNOOZED: {
            if (manual) {
                if (!enable) { Slog.e(TAG, "Tried to disable BS when it's already AUTO_SNOOZED"); return; }
                enableBatterySaverLocked(true, true, REASON_MANUAL_ON); mState = STATE_MANUAL_ON;
            } else if (mIsPowered || (isAutomaticModeActiveLocked() && !isInAutomaticLowZoneLocked()) || /* ... */) { mState = STATE_OFF; }
            break;
        }
        case STATE_PENDING_STICKY_ON: { /* manual changes are refused; unplugged -> re-enable (REASON_STICKY_RESTORE) */ }
    }
}
```

A WRITE of `Settings.Global.low_power` by an app (what `BatterySaverGuard` does with the adb-granted WRITE_SECURE_SETTINGS) takes a different path:
a ContentObserver calls `refreshSettingsLocked()` -> `setSettingsLocked(...)`:

```java
        if (enabledChanged) {
            final String reason = batterySaverEnabled ? "Global.low_power changed to 1" : "Global.low_power changed to 0";
            enableBatterySaverLocked(/*enable=*/ batterySaverEnabled, /*manual=*/ true, BatterySaverController.REASON_SETTING_CHANGED, reason);
        } else {
            doAutoBatterySaverLocked();
        }
```
`enableBatterySaverLocked` really switches the saver on/off (and sets `low_power_sticky`), **but nothing in it, or in `setSettingsLocked`, ever assigns `mState`.**
So after a direct write the saver and `mState` disagree:

| what wrote | state before | after the write | what the NEXT tile / Settings tap does |
| :- | :- | :- | :- |
| launcher writes `low_power=1` (AG1, "ON in Quickstep") | STATE_OFF | saver ON, mState OFF | tap/Settings "Turn off now": `Tried to disable BS when it's already OFF` -> nothing happens, UI says it worked |
| launcher writes `low_power=0` (AC3, "OFF in custom") after Tj turned it on in Quickstep | STATE_MANUAL_ON | saver OFF, mState MANUAL_ON | tap "Turn on": `Tried to enable BS when it's already MANUAL_ON` -> nothing happens |
| same, after an automatic switch-on at low battery | STATE_AUTOMATIC_ON | saver OFF, mState AUTOMATIC_ON | tap "Turn on": `Tried to enable BS when it's already AUTO_ON` |

Only being plugged in (MANUAL_ON/AUTOMATIC_ON -> OFF) or a reboot (`onBootCompleted` writes `low_power=0` and sets mState) heals it: "the tile sometimes works".
This is exactly Tj's report history: 2026-11-05 "turn it off in custom" (AC3, v0.7.3) -> 2026-11-06 "battery saver doesn't work in Quickstep", "tile mostly unresponsive", "sometimes works",
and (20:40) "in the full Settings I turn it off, it says turned off, it never does".
`setPowerSaveModeEnabled` returns true even when the state machine ignored the request, so Settings (`BatterySaverButtonPreferenceController.setChecked` -> `BatterySaverUtils.setPowerSaveMode(ctx, false, false)`) and the tile (`BatterySaverTile.handleClick` -> `BatteryControllerImpl.setPowerSaveMode` -> same util) both look successful.

Other facts used by the fix:
* `BatterySaverUtils.setPowerSaveMode(context, enable, needFirstTimeWarning)`: when `enable` and the request was accepted it increments `Settings.Secure.low_power_manual_activation_count` (a tap on "turn ON" is therefore observable even if the state machine ignores it). The tile passes `needFirstTimeWarning=true` (first time: a confirmation dialog, `low_power_warning_acknowledged` stays 0 until confirmed); the Settings button passes false.
* `enableBatterySaverLocked`: `if (enable && mIsPowered) return;` (cannot turn ON while charging); `onBootCompleted`: `putGlobalSetting(LOW_POWER_MODE, 0)` (a reboot always starts with the saver off).
* `setBatterySaverEnabledManually` needs `POWER_SAVER`/`DEVICE_POWER` (signature|privileged): an adb-granted app cannot call it. `WRITE_SECURE_SETTINGS` can only write the setting (the desync path above).

## 2. AZ1 — what the stock power menu does to the lock screen when it opens

File: `packages/SystemUI/src/com/android/systemui/globalactions/GlobalActionsDialog.java`, inner class `ActionsDialog`:

```java
super(context, com.android.systemui.R.style.Theme_SystemUI_Dialog_GlobalActions);   // parent Theme.DeviceDefault.Light.NoActionBar.Fullscreen, windowIsFloating=true
...
window.setLayout(MATCH_PARENT, MATCH_PARENT);
window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);               // NO window dim: the darkening is the dialog's own background
window.addFlags(FLAG_LAYOUT_IN_SCREEN | FLAG_NOT_TOUCH_MODAL | FLAG_LAYOUT_INSET_DECOR | FLAG_SHOW_WHEN_LOCKED | FLAG_WATCH_OUTSIDE_TOUCH | FLAG_HARDWARE_ACCELERATED);
window.setType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY);
...
private void initializeLayout() {
    ...
    if (mBackgroundDrawable == null) {
        mBackgroundDrawable = new ScrimDrawable();
        mScrimAlpha = ScrimController.GRADIENT_SCRIM_ALPHA;                   // 0.2f
    }
    getWindow().setBackgroundDrawable(mBackgroundDrawable);
}
protected void onStart() { ... GradientColors colors = mColorExtractor.getNeutralColors(); updateColors(colors, false /* animate */); }
private void updateColors(GradientColors colors, boolean animate) { ((ScrimDrawable) mBackgroundDrawable).setColor(colors.getMainColor(), animate); ... }

@Override public void show() {
    super.show();
    mShowing = true;
    mHadTopUi = mStatusBarWindowController.getForceHasTopUi();
    mStatusBarWindowController.setForceHasTopUi(true);
    mBackgroundDrawable.setAlpha(0);
    mGlobalActionsLayout.setTranslationX(mGlobalActionsLayout.getAnimationOffsetX());
    mGlobalActionsLayout.setTranslationY(mGlobalActionsLayout.getAnimationOffsetY());
    mGlobalActionsLayout.setAlpha(0);
    mGlobalActionsLayout.animate()
            .alpha(1).translationX(0).translationY(0)
            .setDuration(300)
            .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)                   // cubic-bezier(0.4, 0, 0.2, 1)
            .setUpdateListener(animation -> {
                int alpha = (int) ((Float) animation.getAnimatedValue() * mScrimAlpha * 255);
                mBackgroundDrawable.setAlpha(alpha);                          // scrim alpha = f(t) * 0.2
            })
            .start();
}
@Override public void dismiss() {
    if (!mShowing) return;
    mShowing = false;
    ... .alpha(0) ... .setDuration(300).withEndAction(this::completeDismiss)
        .setInterpolator(new LogAccelerateInterpolator())                       // slow start, fast end
        .setUpdateListener(animation -> { int alpha = (int) ((1f - (Float) animation.getAnimatedValue()) * mScrimAlpha * 255); mBackgroundDrawable.setAlpha(alpha); })
        .start();
}
```
`ScrimController.GRADIENT_SCRIM_ALPHA = 0.2f` (there is NO `BUSY_SCRIM_ALPHA` in this tag). `ScrimDrawable.draw`: `mPaint.setColor(mMainColor); mPaint.setAlpha(mAlpha); canvas.drawRect(getBounds(), mPaint);` — a flat colour at alpha, i.e. `out = (1 - a) * wallpaper + a * C` with a = f(t) * 0.2 and C = the neutral colour of the colour extractor (black-ish on the AOSP emulator: measured keep factor 0.791 / 0.791 / 0.789; possibly different on MtkSystemUI).

Consequences for the lock-screen patch (our accessibility overlay sits ABOVE this dialog, so the dialog's scrim never touches it):
* the surrounding lock screen darkens along `0.2 * FAST_OUT_SLOW_IN(t / 300 ms)`: 50 % of the way at 105 ms, 78 % at 152 ms, 97 % at 236 ms; the dismiss runs 300 ms back with a slow-start curve (the scrim is still ~90 % at half time).
* a patch that jumps to the final dim at the moment it is told "the menu is open" is darker than its surround for the first ~200 ms (and lighter before the jump): that is the flicker.
* the fix is to follow the same curve from the same start: start time = the accessibility event time of the new dialog window (same `uptimeMillis` clock), start fraction = curve(lag / 300).
* the dim is `out = s * U + b` in general (s = 1 - a, b = a * C); only a black C makes it a pure brightness factor.
