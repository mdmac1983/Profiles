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
import android.widget.HorizontalScrollView;
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
 * The phone-side guide. The list lives in assets/steps.txt (the same file is copied to research/ADB_PHONE_COMMANDS.txt); {@link GuideParser} documents the format.
 * Tj, 2026-11-05: "simple and intuitive and in a logical order in case I have limited time with the OTG cable". So: a short plan at the top, jump buttons per part, a button that goes to the next step
 * not done yet, the one card that matters marked, and the by-hand fallbacks / read-only questions / undo cards closed until tapped.
 */
public class GuideActivity extends Activity {
    private static final String A11Y = "com.tj.launcher/com.tj.launcher.TjAccessibilityService";
    private static final int BLUE = Color.parseColor("#0B57D0"), MUST_BG = Color.parseColor("#FFF4D6");
    private SharedPreferences prefs;
    private float dp;
    private boolean withPrefix;
    private ScrollView scroll;
    private LinearLayout page;
    private final List<TextView> commandViews = new ArrayList<>();
    private final List<String> commandTexts = new ArrayList<>();
    /** One entry per card in file order, for the "next step" button. */
    private final List<View> cardViews = new ArrayList<>();
    private final List<CheckBox> cardDone = new ArrayList<>();
    private final List<Boolean> cardSkippable = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("guide", MODE_PRIVATE);
        dp = getResources().getDisplayMetrics().density;
        withPrefix = prefs.getBoolean("prefix", false);
        scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#F4F6F8"));
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(px(14), px(30), px(14), px(60));
        scroll.addView(page, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
        build();
    }

    private int px(int v) { return Math.round(v * dp); }

    private TextView text(String t, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private LinearLayout card(boolean must) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundColor(must ? MUST_BG : Color.WHITE);
        c.setPadding(px(12), px(10), px(12), px(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = px(10);
        c.setLayoutParams(lp);
        return c;
    }

    private String shown(String cmd) { return withPrefix ? "adb shell " + cmd : cmd; }

    private void build() {
        page.removeAllViews();
        commandViews.clear(); commandTexts.clear(); cardViews.clear(); cardDone.clear(); cardSkippable.clear();
        GuideParser.Guide guide;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(getAssets().open("steps.txt"), "UTF-8"));
            guide = GuideParser.parse(r);
            r.close();
        } catch (Exception e) {
            page.addView(text("Could not read the list: " + e, 14, Color.RED, false));
            return;
        }

        page.addView(text("Tablet adb guide", 26, Color.BLACK, true));
        page.addView(text("Do the parts in order. One card = one thing: press Copy, paste it into the adb app on this phone, press Enter, read the reply, tick Done. Lines marked with a star are the ones that matter if you are short of time.", 15, Color.DKGRAY, false));

        LinearLayout how = card(false);
        how.addView(text("Your plan", 16, Color.BLACK, true));
        StringBuilder plan = new StringBuilder();
        for (String p : guide.plan) { if (plan.length() > 0) plan.append("\n\n"); plan.append(p); }
        if (plan.length() > 0) how.addView(text(plan.toString(), 14, Color.DKGRAY, false));
        TextView small = text("The ADB OTG shell already talks to the tablet, so the lines have NO \"adb\" and NO \"adb shell\" in front. If your app says \"not found\" or does nothing, switch this on: every line then starts with \"adb shell \". Nothing here deletes anything or touches the lock screen, accounts or Wi-Fi; every change has an undo at the bottom.", 13, Color.GRAY, false);
        small.setPadding(0, px(8), 0, 0);
        how.addView(small);
        Switch sw = new Switch(this);
        sw.setText("Show lines with the \"adb shell \" prefix");
        sw.setChecked(withPrefix);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean on) {
                withPrefix = on; prefs.edit().putBoolean("prefix", on).apply();
                refreshCommands();   // only the command boxes change: rebuilding the page would throw away the scroll position, the parts Tj opened and anything he typed into a card (the Copy buttons read the switch when pressed)
            }
        });
        how.addView(sw);
        page.addView(how);

        Button next = new Button(this);
        next.setText("Take me to my next step");
        next.setAllCaps(false);
        next.setTextSize(17);
        next.setMinHeight(px(52));
        next.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { goToNextStep(); } });
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = px(10);
        page.addView(next, nlp);

        // jump buttons: one per group (their headers are filled in below, the click looks them up)
        final List<View> headers = new ArrayList<>(), bodies = new ArrayList<>();
        final List<TextView> headerTexts = new ArrayList<>();
        LinearLayout jumpRow = new LinearLayout(this);
        jumpRow.setOrientation(LinearLayout.HORIZONTAL);
        HorizontalScrollView jump = new HorizontalScrollView(this);
        jump.setHorizontalScrollBarEnabled(false);
        jump.addView(jumpRow);
        page.addView(jump);
        for (int g = 0; g < guide.groups.size(); g++) {
            final int gi = g;
            Button jb = new Button(this);
            jb.setText(guide.groups.get(g).label());
            jb.setAllCaps(false);
            jb.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openGroup(headers.get(gi), bodies.get(gi), headerTexts.get(gi), true); } });
            jumpRow.addView(jb);
        }

        int n = 0;
        for (final GuideParser.Group group : guide.groups) {
            final TextView h = text(group.title, 19, BLUE, true);
            h.setPadding(0, px(22), 0, 0);
            page.addView(h);
            final LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            page.addView(body, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            headers.add(h); bodies.add(body); headerTexts.add(h);
            for (GuideParser.Card c : group.cards) addCard(body, ++n, c, group.collapsed);
            if (group.collapsed) {
                h.setText("▸ " + group.title + "  (tap to open)");
                body.setVisibility(View.GONE);
                h.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
                    boolean show = body.getVisibility() != View.VISIBLE;
                    h.setText((show ? "▾ " : "▸ ") + group.title + (show ? "" : "  (tap to open)"));
                    body.setVisibility(show ? View.VISIBLE : View.GONE);
                } });
            }
        }
    }

    private void openGroup(View header, final View body, TextView headerText, boolean scrollTo) {
        if (body.getVisibility() != View.VISIBLE) {
            body.setVisibility(View.VISIBLE);
            String t = headerText.getText().toString().replace("▸ ", "").replace("  (tap to open)", "");
            headerText.setText("▾ " + t);
        }
        final View target = header;
        if (scrollTo) scroll.post(new Runnable() { @Override public void run() { scroll.smoothScrollTo(0, Math.max(0, target.getTop() - px(8))); } });
    }

    /** Scrolls to the first card in the open parts that is not ticked yet (optional cards are skipped). */
    private void goToNextStep() {
        for (int i = 0; i < cardViews.size(); i++) {
            if (cardViews.get(i).getVisibility() != View.VISIBLE || !isOnScreenGroup(cardViews.get(i))) continue;
            if (cardDone.get(i).isChecked() || cardSkippable.get(i)) continue;
            final View target = cardViews.get(i);
            scroll.post(new Runnable() { @Override public void run() { scroll.smoothScrollTo(0, Math.max(0, topInPage(target) - px(8))); } });
            return;
        }
        Toast.makeText(this, "Every step in the parts above is ticked. Well done.", Toast.LENGTH_LONG).show();
    }

    private boolean isOnScreenGroup(View card) { View p = (View) card.getParent(); return p == null || p.getVisibility() == View.VISIBLE; }

    private int topInPage(View v) {
        int top = 0;
        View cur = v;
        while (cur != null && cur != page) { top += cur.getTop(); cur = (View) cur.getParent(); }
        return top;
    }

    private void addCard(LinearLayout into, int n, GuideParser.Card cd, boolean inClosedGroup) {
        final String key = "done_" + n + "_" + cd.title;
        LinearLayout c = card(cd.must);
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = text((cd.must ? "★ " : "") + n + ".  " + cd.title, 16, Color.BLACK, true);
        head.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        CheckBox done = new CheckBox(this);
        done.setText("Done");
        done.setChecked(prefs.getBoolean(key, false));
        done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean on) { prefs.edit().putBoolean(key, on).apply(); }
        });
        head.addView(done);
        c.addView(head);
        if (cd.why != null) c.addView(text(cd.why, 14, Color.DKGRAY, false));
        if ("a11y".equals(cd.special)) addA11y(c);
        for (String cmd : cd.cmds) addCommand(c, cmd, "Copy this line");
        for (String alt : cd.alts) { c.addView(text("If that did nothing, use this instead:", 13, Color.DKGRAY, true)); addCommand(c, alt, "Copy the other form"); }
        if (cd.note != null) { TextView nt = text(cd.note, 13, Color.parseColor("#7A4E00"), false); nt.setPadding(0, px(8), 0, 0); c.addView(nt); }
        for (String u : cd.undos) { TextView ut = text("Undo: " + u, 13, Color.GRAY, false); ut.setPadding(0, px(6), 0, 0); c.addView(ut); }
        into.addView(c);
        cardViews.add(c); cardDone.add(done); cardSkippable.add(cd.optional() || inClosedGroup);
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
        c.addView(text("Type the reply to the card above here (leave empty if it said null):", 13, Color.DKGRAY, true));
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

    private void copyLine(String s) {
        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("adb line", s));
        Toast.makeText(this, "Copied. Paste it into the adb app and press Enter.", Toast.LENGTH_SHORT).show();
    }
}
