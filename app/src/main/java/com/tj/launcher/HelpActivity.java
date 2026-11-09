package com.tj.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;

/**
 * BS1c (user, 2026-11-09: "in the bottom of settings, add user guide and features"): one plain, scrollable text page. The text lives in res/raw
 * (user_guide.txt, features.txt) so it can be edited without touching code. A line starting with "# " is a heading. Opened only from Profiles settings.
 */
public class HelpActivity extends Activity {
    public static final String EXTRA_PAGE = "page";
    public static final String GUIDE = "guide", FEATURES = "features";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Mode.isCustom(this)) { finish(); return; }   // like Profiles settings: Owner (Quickstep) only
        float dp = getResources().getDisplayMetrics().density;
        boolean features = FEATURES.equals(getIntent().getStringExtra(EXTRA_PAGE));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        int p = Math.round(16 * dp);
        page.setPadding(p, Math.round(36 * dp), p, Math.round(64 * dp));
        String text = read(features ? com.android.launcher3.R.raw.features : com.android.launcher3.R.raw.user_guide);
        for (String line : text.split("\n", -1)) {
            TextView v = new TextView(this);
            boolean head = line.startsWith("# ");
            v.setText(head ? line.substring(2) : line);
            v.setTextColor(head ? Color.parseColor("#1A73E8") : Color.parseColor("#202124"));
            v.setTextSize(head ? 20 : 15);
            if (head) { v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, Math.round(18 * dp), 0, Math.round(6 * dp)); }
            page.addView(v);
        }
        scroll.addView(page);
        setContentView(scroll);
    }

    private String read(int res) {
        try (InputStream in = getResources().openRawResource(res)) {
            java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
            return o.toString("UTF-8");
        } catch (Throwable t) { return "(this page could not be loaded)"; }
    }
}
