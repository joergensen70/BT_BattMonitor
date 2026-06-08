package com.ys.module.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.LinearLayout;

/* JADX INFO: loaded from: classes.dex */
public class StatusBarUtil {
    private static int calculateStatusColor(int i, int i2) {
        float f = 1.0f - (i2 / 255.0f);
        return ((int) (((double) ((i & 255) * f)) + 0.5d)) | (((int) (((double) (((i >> 16) & 255) * f)) + 0.5d)) << 16) | ViewCompat.MEASURED_STATE_MASK | (((int) (((double) (((i >> 8) & 255) * f)) + 0.5d)) << 8);
    }

    public static void setColor(Activity activity, int i, int i2) {
        activity.getWindow().addFlags(Integer.MIN_VALUE);
        activity.getWindow().clearFlags(67108864);
        activity.getWindow().setStatusBarColor(calculateStatusColor(i, i2));
    }

    private static View createStatusBarView(Activity activity, int i) {
        View view = new View(activity);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, getStatusBarHeight(activity)));
        view.setBackgroundColor(i);
        return view;
    }

    private static View createStatusBarView(Activity activity, int i, int i2) {
        View view = new View(activity);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, getStatusBarHeight(activity)));
        view.setBackgroundColor(calculateStatusColor(i, i2));
        return view;
    }

    private static int getStatusBarHeight(Context context) {
        return context.getResources().getDimensionPixelSize(context.getResources().getIdentifier("status_bar_height", "dimen", "android"));
    }
}
