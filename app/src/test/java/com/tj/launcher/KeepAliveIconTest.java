package com.tj.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Test;

/**
 * KA1 (v0.7.2): the keep-alive notification's small icon was an EMPTY vector drawable (res/drawable/tj_ic_blank.xml). The real tablet's MediaTek SystemUI could not turn it into an icon
 * ("Couldn't create icon"), and Android then crashed the launcher three times in seven seconds. The icon is now an ordinary fully transparent PNG in every common density.
 * These tests read the resource files themselves, so nobody can put an empty vector back unnoticed (the emulator's AOSP SystemUI accepts both, so only a unit test can pin this).
 */
public class KeepAliveIconTest {
    private static File res() {
        for (String p : new String[]{"src/main/res", "app/src/main/res"}) { File f = new File(p); if (f.isDirectory()) return f; }
        throw new AssertionError("res directory not found from " + new File(".").getAbsolutePath());
    }

    @Test public void thereIsNoVectorOrXmlVersionOfTheIconAnyMore() {
        File[] dirs = res().listFiles();
        assertTrue(dirs != null && dirs.length > 0);
        for (File d : dirs) {
            if (!d.getName().startsWith("drawable")) continue;
            assertFalse(d + " must not hold tj_ic_blank.xml (an empty vector crash-looped the tablet)", new File(d, "tj_ic_blank.xml").exists());
        }
    }

    @Test public void everyCommonDensityHasARealPngOfTheRightSize() throws IOException {
        String[][] want = {{"mdpi", "24"}, {"hdpi", "36"}, {"xhdpi", "48"}, {"xxhdpi", "72"}, {"xxxhdpi", "96"}};
        for (String[] w : want) {
            File f = new File(res(), "drawable-" + w[0] + "/tj_ic_blank.png");
            assertTrue(f + " is missing", f.isFile());
            byte[] b = new byte[24];
            try (FileInputStream in = new FileInputStream(f)) { assertEquals(24, in.read(b)); }
            assertEquals("PNG signature of " + f, 0x89, b[0] & 0xff);
            assertEquals('P', b[1]); assertEquals('N', b[2]); assertEquals('G', b[3]);
            int width = ((b[16] & 0xff) << 24) | ((b[17] & 0xff) << 16) | ((b[18] & 0xff) << 8) | (b[19] & 0xff);
            int height = ((b[20] & 0xff) << 24) | ((b[21] & 0xff) << 16) | ((b[22] & 0xff) << 8) | (b[23] & 0xff);
            assertEquals("width of " + f, Integer.parseInt(w[1]), width);
            assertEquals("height of " + f, Integer.parseInt(w[1]), height);
        }
    }
}
