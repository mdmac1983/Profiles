package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TileProfileTest {
    @Test public void keepsAValidList() { assertEquals("wifi,bt,rotation", TileProfile.sanitize("wifi,bt,rotation", "x")); }
    @Test public void trimsAndDropsEmptyEntries() { assertEquals("wifi,rotation", TileProfile.sanitize(" wifi , ,rotation,", "x")); }
    @Test public void dropsOddCharacters() { assertEquals("wifi,bt", TileProfile.sanitize("wi;fi,b'''t", "x").replace("wifi", "wifi")); }
    @Test public void emptyFallsBack() { assertEquals("rotation", TileProfile.sanitize("", "rotation")); assertEquals("rotation", TileProfile.sanitize(null, "rotation")); assertEquals("rotation", TileProfile.sanitize(",,;;", "rotation")); }
    @Test public void customTileSpecsSurvive() { assertEquals("custom(com.a.b/.C)", TileProfile.sanitize("custom(com.a.b/.C)", "x")); }
}
