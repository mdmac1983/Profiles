package com.tj.launcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BS1a (user, 2026-11-09: "add in app updates"): the pure rules of the in-app updater (no Android classes, JVM-tested). The app reads the newest Release of the
 * owner's repo, picks its {@code Profiles-vX.apk} and installs it when its version is newer than the installed one. Versions go 1.0, 1.1, 1.2 ... (BS1b).
 * Android itself refuses an APK signed with another key, and {@link Updater} checks the signature before it even offers the install.
 */
public final class UpdateRules {
    public static final String REPO = "mdmac1983/Profiles";
    public static final String LATEST_API = "https://api.github.com/repos/" + REPO + "/releases/latest";
    public static final String RELEASES_PAGE = "https://github.com/" + REPO + "/releases/latest";
    /** Opening Profiles settings checks by itself at most this often (the button checks at once). */
    public static final long AUTO_CHECK_EVERY_MS = 6L * 60 * 60 * 1000;

    private UpdateRules() {}

    /** The numbers of a version name: "v1.2" -> [1, 2]; "0.8.11" -> [0, 8, 11]; anything without a digit -> empty. */
    static int[] parts(String v) {
        if (v == null) return new int[0];
        Matcher m = Pattern.compile("\\d+").matcher(v);
        java.util.ArrayList<Integer> l = new java.util.ArrayList<>();
        while (m.find()) { try { l.add(Integer.parseInt(m.group())); } catch (NumberFormatException e) { l.add(Integer.MAX_VALUE); } }
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = l.get(i);
        return r;
    }

    /** Negative, zero or positive as a is older, equal or newer than b (missing parts count as 0: "1.0" == "1"). */
    public static int compare(String a, String b) {
        int[] x = parts(a), y = parts(b);
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? x[i] : 0, q = i < y.length ? y[i] : 0;
            if (p != q) return p < q ? -1 : 1;
        }
        return 0;
    }

    /** Is the release's tag a NEWER version than the installed one? A tag with no number is never newer. */
    public static boolean newer(String remoteTag, String installed) {
        return parts(remoteTag).length > 0 && compare(remoteTag, installed) > 0;
    }

    /** The release file to install: {@code Profiles-v<version>.apk}, never a preview, never the phone guide. */
    public static boolean isAppApk(String fileName) {
        return fileName != null && fileName.matches("Profiles-v[0-9][0-9A-Za-z._-]*\\.apk") && !fileName.contains("preview");
    }

    public static final class Release {
        public final String tag, apkName, apkUrl;
        Release(String tag, String apkName, String apkUrl) { this.tag = tag; this.apkName = apkName; this.apkUrl = apkUrl; }
    }

    /** Reads GitHub's "latest release" JSON: its tag and the URL of its app APK. Null when it is not such an answer or holds no app APK. */
    public static Release parse(String json) {
        if (json == null) return null;
        String j = json.replace("\\/", "/");
        Matcher t = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").matcher(j);
        if (!t.find()) return null;
        Matcher u = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"(https://[^\"]+)\"").matcher(j);
        while (u.find()) {
            String url = u.group(1);
            String name = url.substring(url.lastIndexOf('/') + 1);
            if (isAppApk(name) && url.startsWith("https://github.com/" + REPO + "/releases/download/")) return new Release(t.group(1), name, url);
        }
        return null;
    }

    /** Should opening the settings screen check by itself? Never twice within {@link #AUTO_CHECK_EVERY_MS}; a clock that went back counts as due. */
    public static boolean autoCheckDue(long lastCheckMs, long nowMs) {
        return lastCheckMs <= 0 || nowMs < lastCheckMs || nowMs - lastCheckMs >= AUTO_CHECK_EVERY_MS;
    }
}
