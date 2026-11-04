package com.tj.launcher;

import android.app.Activity;
import android.os.Bundle;

/** N4: the "Back to custom" icon (an app icon that acts as the kill switch) and the target of the quick-settings tile. Invisible: switches and finishes. Does nothing in the custom launcher. */
public class BackToCustomActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            if (!Mode.isCustom(this) && ReturnMethods.on(this, ReturnMethods.Method.ICON)) ProfileSession.returnToCustom(this, "icon");
        } catch (Throwable t) { android.util.Log.e("TjBack", "return failed", t); }
        finish();
        overridePendingTransition(0, 0);
    }
}
