package com.ys.module.toast;

import android.content.Context;
import android.widget.Toast;

/* JADX INFO: loaded from: classes.dex */
public class ToastTool {
    public static void showNormalShort(Context context, String str) {
        Toast.makeText(context, str, 0).show();
    }

    public static void showNormalShort(Context context, int i) {
        Toast.makeText(context, i, 0).show();
    }

    public static void showNormalLong(Context context, String str) {
        Toast.makeText(context, str, 1).show();
    }

    public static void showNormalLong(Context context, int i) {
        Toast.makeText(context, i, 1).show();
    }
}
