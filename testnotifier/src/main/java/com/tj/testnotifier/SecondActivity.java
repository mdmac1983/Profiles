package com.tj.testnotifier;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * CI-only: a second entry into the same app that lives in its OWN task (its task affinity differs from MainActivity's, see the manifest). Section rt uses it to get "one app, two tasks": the custom
 * account's task (MainActivity) and Quickstep's (this one). Android's Recents drops an OLDER task with the same affinity or intent when a new one is added (run 106: 8 -> 22, 12 -> 24), so two
 * tasks of one package can only stay side by side in Recents if their affinities differ. Posts no notification.
 */
public class SecondActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView t = new TextView(this);
        t.setText("Second task of " + getPackageName());
        t.setTextSize(20);
        setContentView(t);
    }
}
