package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PowerMenuRuleTest {
    private static final String H = Features.POWER_HIDE_SCREENSHOT, B = Features.POWER_BLOCK, O = Features.POWER_OFF;

    @Test public void offDoesNothing() { assertEquals(PowerMenuRule.NOTHING, PowerMenuRule.decide(O, true, false, true, true)); }
    @Test public void quickstepModeNeverTouchesTheMenu() { assertEquals(PowerMenuRule.NOTHING, PowerMenuRule.decide(B, false, false, true, true)); }
    @Test public void neverActsOnTheLockScreen() { assertEquals(PowerMenuRule.NOTHING, PowerMenuRule.decide(B, true, true, true, true)); }
    @Test public void noMenuNoAction() { assertEquals(PowerMenuRule.NOTHING, PowerMenuRule.decide(H, true, false, false, false)); }
    @Test public void hideCoversTheScreenshotEntry() { assertEquals(PowerMenuRule.COVER_SCREENSHOT, PowerMenuRule.decide(H, true, false, true, true)); }
    @Test public void hideFailsOpenWhenEntryNotFound() { assertEquals(PowerMenuRule.NOTHING, PowerMenuRule.decide(H, true, false, true, false)); }
    @Test public void blockClosesTheMenu() { assertEquals(PowerMenuRule.CLOSE_MENU, PowerMenuRule.decide(B, true, false, true, false)); }
    @Test public void wallpaperModeCoversLikeHideMode() { assertEquals(PowerMenuRule.COVER_SCREENSHOT, PowerMenuRule.decide(Features.POWER_WALLPAPER, true, false, true, true)); }
    @Test public void wallpaperModeNeverOnTheLockScreen() { assertEquals(PowerMenuRule.NOTHING, PowerMenuRule.decide(Features.POWER_WALLPAPER, true, true, true, true)); }
    @Test public void isCoverKnowsBothModes() { assertEquals(true, Features.isCover(Features.POWER_HIDE_SCREENSHOT) && Features.isCover(Features.POWER_WALLPAPER) && !Features.isCover(Features.POWER_BLOCK) && !Features.isCover(Features.POWER_OFF)); }
}
