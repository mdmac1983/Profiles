package com.tj.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class HomeDoublePressTest {
    @Test public void twoQuickPressesAreADoublePress() { assertTrue(HomeDoublePress.isDouble(1_000, 1_400)); }
    @Test public void slowSecondPressIsNot() { assertFalse(HomeDoublePress.isDouble(1_000, 2_000)); }
    @Test public void firstEverPressIsNot() { assertFalse(HomeDoublePress.isDouble(0, 1_000)); }
    @Test public void clockGoingBackwardsIsNot() { assertFalse(HomeDoublePress.isDouble(5_000, 4_000)); }
}
