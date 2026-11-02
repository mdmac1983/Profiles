package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;

import com.android.launcher3.util.ResourceBasedOverride;

public class AppFilter implements ResourceBasedOverride {

    private Context mTjContext;

    public static AppFilter newInstance(Context context) {
        AppFilter f = Overrides.getObject(AppFilter.class, context, R.string.app_filter_class);
        f.mTjContext = context.getApplicationContext();
        return f;
    }

    public boolean shouldShowApp(ComponentName app) {
        // Tj: hidden apps and apps installed after the baseline never appear in the drawer or its search (com.tj.launcher.HiddenApps).
        return mTjContext == null || com.tj.launcher.HiddenApps.isShown(mTjContext, app);
    }
}
