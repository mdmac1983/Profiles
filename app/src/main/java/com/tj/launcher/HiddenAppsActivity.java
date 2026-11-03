package com.tj.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chooses which apps the CUSTOM launcher shows. Reachable only while the stock launcher (Quickstep) is on (Tj: "settings ... only visible and accessible when the quickstep launcher is on").
 * Checked = shown in the custom drawer and its search. Apps installed after the first run start unchecked ("new") until Tj checks them.
 */
public class HiddenAppsActivity extends Activity {
    private static final class Row { String pkg; String label; Drawable icon; }

    private final List<Row> rows = new ArrayList<>();
    private Set<String> shown = new HashSet<>();

    @Override protected void onResume() { super.onResume(); RecentsGuard.excludeOurTasks(this); RecentsGuard.describeAsTarget(this); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Mode.isCustom(this)) { finish(); return; }   // locked into the custom launcher: this screen only exists in Quickstep mode
        PackageManager pm = getPackageManager();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            Row r = new Row();
            r.pkg = pkg; r.label = String.valueOf(ri.loadLabel(pm)); r.icon = ri.loadIcon(pm);
            rows.add(r);
        }
        Collections.sort(rows, new Comparator<Row>() { @Override public int compare(Row a, Row c) { return a.label.compareToIgnoreCase(c.label); } });
        for (Row r : rows) if (HiddenApps.isPackageShown(this, r.pkg)) shown.add(r.pkg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        TextView head = new TextView(this);
        head.setText("Apps shown in the custom launcher\\nChecked = shown in the drawer and its search. Newly installed apps start unchecked until you check them.");
        head.setTextColor(Color.DKGRAY);
        head.setPadding(32, 48, 32, 24);
        root.addView(head);
        ListView list = new ListView(this);
        list.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return rows.size(); }
            @Override public Object getItem(int i) { return rows.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup parent) {
                final Row r = rows.get(i);
                LinearLayout row = new LinearLayout(HiddenAppsActivity.this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(32, 12, 32, 12);
                ImageView iv = new ImageView(HiddenAppsActivity.this);
                iv.setImageDrawable(r.icon);
                row.addView(iv, new LinearLayout.LayoutParams(96, 96));
                TextView tv = new TextView(HiddenAppsActivity.this);
                tv.setText(r.label);
                tv.setTextSize(16);
                tv.setTextColor(Color.BLACK);
                tv.setPadding(24, 0, 0, 0);
                row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                final CheckBox cb = new CheckBox(HiddenAppsActivity.this);
                cb.setChecked(shown.contains(r.pkg));
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        boolean on = cb.isChecked();
                        if (on) shown.add(r.pkg); else shown.remove(r.pkg);
                        HiddenApps.setShown(HiddenAppsActivity.this, r.pkg, on);
                        HiddenApps.reloadLauncher(HiddenAppsActivity.this);
                    }
                });
                row.addView(cb);
                return row;
            }
        });
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }
}
