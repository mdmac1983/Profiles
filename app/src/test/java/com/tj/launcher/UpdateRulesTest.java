package com.tj.launcher;

import static org.junit.Assert.*;

import org.junit.Test;

public class UpdateRulesTest {
    private static final String JSON = "{\"url\":\"x\",\"tag_name\":\"v1.1\",\"name\":\"Profiles v1.1\",\"assets\":[" +
            "{\"name\":\"PhoneGuide-v1.1.apk\",\"browser_download_url\":\"https://github.com/mdmac1983/Profiles/releases/download/v1.1/PhoneGuide-v1.1.apk\"}," +
            "{\"name\":\"Profiles-v1.1.apk\",\"browser_download_url\":\"https://github.com/mdmac1983/Profiles/releases/download/v1.1/Profiles-v1.1.apk\"}]}";

    @Test public void versionsCompareByNumberNotByText() {
        assertTrue(UpdateRules.compare("1.1", "1.0") > 0);
        assertTrue("1.10 is newer than 1.9", UpdateRules.compare("1.10", "1.9") > 0);
        assertTrue("2.0 is newer than 1.9", UpdateRules.compare("v2.0", "1.9") > 0);
        assertEquals("a leading v and a missing .0 do not matter", 0, UpdateRules.compare("v1", "1.0"));
        assertTrue("1.0 is newer than the old 0.8.11", UpdateRules.compare("1.0", "0.8.11") > 0);
    }

    @Test public void onlyANewerTagIsAnUpdate() {
        assertTrue(UpdateRules.newer("v1.1", "1.0"));
        assertFalse("the same version is not an update", UpdateRules.newer("v1.0", "1.0"));
        assertFalse("an older release is never installed", UpdateRules.newer("v0.9", "1.0"));
        assertFalse("a tag without a number is never newer", UpdateRules.newer("latest", "1.0"));
        assertFalse(UpdateRules.newer(null, "1.0"));
    }

    @Test public void theAppApkIsPickedNotThePhoneGuideNorAPreview() {
        UpdateRules.Release r = UpdateRules.parse(JSON);
        assertNotNull(r);
        assertEquals("v1.1", r.tag);
        assertEquals("Profiles-v1.1.apk", r.apkName);
        assertEquals("https://github.com/mdmac1983/Profiles/releases/download/v1.1/Profiles-v1.1.apk", r.apkUrl);
        assertFalse(UpdateRules.isAppApk("PhoneGuide-v1.1.apk"));
        assertFalse(UpdateRules.isAppApk("Profiles-v1.1-preview.apk"));
        assertFalse(UpdateRules.isAppApk("Launcher-v0.8.11.apk"));
        assertTrue(UpdateRules.isAppApk("Profiles-v1.10.apk"));
    }

    @Test public void escapedSlashesAreRead() {
        assertNotNull(UpdateRules.parse(JSON.replace("/", "\\/")));
    }

    @Test public void anApkFromAnotherRepoIsRefused() {
        assertNull(UpdateRules.parse(JSON.replace("/PhoneGuide", "/x").replace("github.com/mdmac1983/Profiles/releases/download/v1.1/Profiles", "github.com/someone/Else/releases/download/v1.1/Profiles")));
    }

    @Test public void anAnswerWithoutTagOrApkGivesNothing() {
        assertNull(UpdateRules.parse(null));
        assertNull(UpdateRules.parse("{\"message\":\"Not Found\"}"));
        assertNull(UpdateRules.parse("{\"tag_name\":\"v1.1\",\"assets\":[]}"));
    }

    @Test public void theAutomaticCheckRunsAtMostEverySixHours() {
        long h = 60L * 60 * 1000;
        assertTrue("never checked", UpdateRules.autoCheckDue(0, 10 * h));
        assertFalse(UpdateRules.autoCheckDue(10 * h, 15 * h));
        assertTrue(UpdateRules.autoCheckDue(10 * h, 16 * h));
        assertTrue("the clock went back", UpdateRules.autoCheckDue(10 * h, 9 * h));
    }
}
