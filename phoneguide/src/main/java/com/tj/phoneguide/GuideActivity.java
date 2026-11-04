package com.tj.phoneguide;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The phone-side guide. The list lives in assets/steps.txt (the same file is copied to research/ADB_PHONE_COMMANDS.txt).
 * Format: "## Group title" starts a group; "@ Title" starts a card; then "why:", "cmd:" (the line to type), "alt:" (a second line if the first does nothing),
 * "undo:", "note:"; "special: a11y" turns the card into the accessibility-merge helper. Lines starting with "#" (single) are comments.
 */
public class GuideActivity extends Activity {
    private static final String A11Y = "com.tj.launcher/com.tj.launcher.TjAccessibilityService";
    private SharedPreferences prefs;
    private float dp;
    private boolean withPrefix;
    private LinearLayout page;
    private final List<TextView> commandViews = new ArrayList<>();
    private final List<String> commandTexts = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("guide", MODE_PRIVATE);
        dp = getResources().getDisplayMetrics().density;
        withPrefix = prefs.getBoolean("prefix", false);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.parseColor("#F4F6F8"));
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(px(14), px(30), px(14), px(60));
        sv.addView(page, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
        build();
    }

    private int px(int v) { return Math.round(v * dp); }

    private TextView text(String t, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundColor(Color.WHITE);
        c.setPadding(px(12), px(10), px(12), px(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = px(10);
        c.setLayoutParams(lp);
        return c;
    }

    private String shown(String cmd) { return withPrefix ? "adb shell " + cmd : cmd; }

    private void build() {
        page.removeAllViews();
        commandViews.clear(); commandTexts.clear();
        page.addView(text("Tablet adb guide", 26, Color.BLACK, true));
        page.addView(text("Run these ON THE PHONE, in the shell of your ADB OTG app, to set up the TABLET. One card = one line: press Copy, paste it into the app, press Enter, read the reply, tick Done.", 15, Color.DKGRAY, false));

        LinearLayout how = card();
        how.addView(text("How this works", 16, Color.BLACK, true));
        how.addView(text("The ADB OTG shell already talks to the tablet, so the lines have NO \"adb\" and NO \"adb shell\" in front. If your app says \"not found\" or does nothing, switch the box below on: every line then starts with \"adb shell \".\n\nNothing here deletes anything, changes the lock screen, touches accounts or Wi-Fi. Every change has an undo at the bottom.", 14, Color.DKGRAY, false));
        Switch sw = new Switch(this);
        sw.setText("Show lines with the \"adb shell \" prefix");
        sw.setChecked(withPrefix);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean on) { withPrefix = on; prefs.edit().putBoolean("prefix", on).apply(); page.post(new Runnable() { @Override public void run() { build(); } }); }
        });
        how.addView(sw);
        page.addView(how);

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(getAssets().open("steps.txt"), "UTF-8"));
            String line, title = null, why = null, undo = null, note = null, special = null;
            List<String> cmds = new ArrayList<>(), alts = new ArrayList<>();
            int n = 0;
            while (true) {
                line = r.readLine();
                boolean flush = line == null || line.startsWith("@ ") || line.startsWith("## ");
                if (flush && title != null) { addCard(++n, title, why, cmds, alts, undo, note, special); }
                if (flush) { title = why = undo = note = special = null; cmds.clear(); alts.clear(); }
                if (line == null) break;
                if (line.startsWith("## ")) { TextView h = text(line.substring(3), 19, Color.parseColor("#0B57D0"), true); h.setPadding(0, px(22), 0, 0); page.addView(h); }
                else if (line.startsWith("@ ")) title = line.substring(2).trim();
                else if (line.startsWith("why:")) why = line.substring(4).trim();
                else if (line.startsWith("cmd:")) cmds.add(line.substring(4).trim());
                else if (line.startsWith("alt:")) alts.add(line.substring(4).trim());
                else if (line.startsWith("undo:")) undo = line.substring(5).trim();
                else if (line.startsWith("note:")) note = line.substring(5).trim();
                else if (line.startsWith("special:")) special = line.substring(8).trim();
            }
            r.close();
        } catch (Exception e) {
            page.addView(text("Could not read the list: " + e, 14, Color.RED, false));
        }
    }

    private void addCard(int n, String title, String why, List<String> cmds, List<String> alts, String undo, String note, String special) {
        final String key = "done_" + n + "_" + title;
        LinearLayout c = card();
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = text(n + ".  " + title, 16, Color.BLACK, true);
        head.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        CheckBox done = new CheckBox(this);
        done.setText("Done");
        done.setChecked(prefs.getBoolean(key, false));
        done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean on) { prefs.edit().putBoolean(key, on).apply(); }
        });
        head.addView(done);
        c.addView(head);
        if (why != null) c.addView(text(why, 14, Color.DKGRAY, false));
        if ("a11y".equals(special)) addA11y(c);
        for (String cmd : cmds) addCommand(c, cmd, "Copy this line");
        for (String alt : alts) { c.addView(text("If that did nothing, use this instead:", 13, Color.DKGRAY, true)); addCommand(c, alt, "Copy the other form"); }
        if (note != null) { TextView nt = text(note, 13, Color.parseColor("#7A4E00"), false); nt.setPadding(0, px(8), 0, 0); c.addView(nt); }
        if (undo != null) { TextView u = text("Undo: " + undo, 13, Color.GRAY, false); u.setPadding(0, px(6), 0, 0); c.addView(u); }
        page.addView(c);
    }

    private void addCommand(LinearLayout c, final String cmd, String buttonLabel) {
        final TextView box = text(shown(cmd), 14, Color.BLACK, false);
        box.setTypeface(Typeface.MONOSPACE);
        box.setBackgroundColor(Color.parseColor("#EEF1F4"));
        box.setPadding(px(8), px(8), px(8), px(8));
        box.setTextIsSelectable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = px(8);
        c.addView(box, lp);
        commandViews.add(box); commandTexts.add(cmd);
        final Button copy = new Button(this);
        copy.setText(buttonLabel);
        copy.setAllCaps(false);
        copy.setTextSize(17);
        copy.setMinHeight(px(52));
        copy.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { copyLine(shown(cmd)); }
        });
        c.addView(copy, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /** The accessibility step: the new list must KEEP any service that is already on, so the line is built from what the tablet printed. */
    private void addA11y(LinearLayout c) {
        c.addView(text("Type the reply to step 1 here (leave empty if it said null):", 13, Color.DKGRAY, true));
        final EditText in = new EditText(this);
        in.setSingleLine(true);
        in.setHint("e.g. null   or   com.example/.SomeService");
        c.addView(in);
        final TextView out = text("", 14, Color.BLACK, false);
        out.setTypeface(Typeface.MONOSPACE);
        out.setBackgroundColor(Color.parseColor("#EEF1F4"));
        out.setPadding(px(8), px(8), px(8), px(8));
        out.setTextIsSelectable(true);
        c.addView(out);
        final Runnable update = new Runnable() { @Override public void run() { out.setText(shown(a11yLine(in.getText().toString()))); } };
        in.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b2, int c2) { }
            @Override public void onTextChanged(CharSequence s, int a, int b2, int c2) { }
            @Override public void afterTextChanged(Editable e) { update.run(); }
        });
        update.run();
        Button copy = new Button(this);
        copy.setText("Copy the line above");
        copy.setAllCaps(false);
        copy.setTextSize(17);
        copy.setMinHeight(px(52));
        copy.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { copyLine(shown(a11yLine(in.getText().toString()))); } });
        c.addView(copy);
    }

    static String a11yLine(String printed) { return A11yLine.build(printed); }

    private void refreshCommands() {
        for (int i = 0; i < commandViews.size(); i++) commandViews.get(i).setText(shown(commandTexts.get(i)));
    }

    private void copyLine(String s) {
        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("adb line", s));
        Toast.makeText(this, "Copied. Paste it into the adb app and press Enter.", Toast.LENGTH_SHORT).show();
    }
}
