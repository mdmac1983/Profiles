package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/** AH3 (v0.7.6): the per-device correction of the lock-screen wallpaper patch applies to Tj's MediaTek tablet only. */
public class PatchProfileTest {
    @Test public void theTabletShiftsTheModelLeftByFifteenPercentOfTheWidth() {
        assertEquals(-0.15f, PatchProfile.lockShiftPerWidth("tb8168p1_bsp"), 0.0001f);
        assertEquals(-90, PatchProfile.lockShiftPx("tb8168p1_bsp", 600));
        assertEquals(-154, PatchProfile.lockShiftPx("tb8168p1_bsp", 1024));   // a landscape display scales with its width
    }
    @Test public void theTabletGetsAMildBrightnessLift() { assertEquals(8, PatchProfile.lockDimPct("tb8168p1_bsp")); }
    @Test public void everyOtherDeviceKeepsTheGenericModel() {
        for (String d : new String[] { "generic_x86", "emulator64_x86_64", "sdk_gphone_x86", "", "walleye", null }) {
            assertEquals(d, 0f, PatchProfile.lockShiftPerWidth(d), 0f);
            assertEquals(d, 0, PatchProfile.lockDimPct(d));
            assertEquals(d, 0, PatchProfile.lockShiftPx(d, 600));
        }
    }
    @Test public void theMatchIsCaseInsensitive() { assertEquals(8, PatchProfile.lockDimPct("TB8168P1_BSP")); }
}
