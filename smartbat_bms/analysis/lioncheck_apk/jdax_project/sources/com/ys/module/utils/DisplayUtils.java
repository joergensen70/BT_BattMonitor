package com.ys.module.utils;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import com.lidroid.xutils.util.LogUtils;

/* JADX INFO: loaded from: classes.dex */
public class DisplayUtils {
    public static void print(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        LogUtils.e("w:" + defaultDisplay.getWidth() + ", h:" + defaultDisplay.getHeight());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LogUtils.e("w:" + (displayMetrics.widthPixels * displayMetrics.density) + ", h:" + (displayMetrics.heightPixels * displayMetrics.density) + ",d:" + displayMetrics.densityDpi + "::" + displayMetrics.density);
    }

    public static int dp2px(float f, Activity activity) {
        return (int) TypedValue.applyDimension(1, f, activity.getResources().getDisplayMetrics());
    }
}
