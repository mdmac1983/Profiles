package com.tj.testnotifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.TextView;

/** CI-only: posts one notification each time it is opened. Its title carries the package name, so a screen dump shows which copy (quick / ctl) it came from. */
public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView t = new TextView(this);
        t.setText("Notifier test app " + getPackageName());
        t.setTextSize(20);
        setContentView(t);
        if (getIntent().getBooleanExtra("quiet", false)) return;   // the task-clean-up test (section rt) needs a shared app that opens WITHOUT a notification
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("t", "Test", NotificationManager.IMPORTANCE_DEFAULT));
        nm.notify(1, new Notification.Builder(this, "t")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("NOTIFIER " + getPackageName())
                .setContentText("posted when the app was opened")
                .build());
    }
}
