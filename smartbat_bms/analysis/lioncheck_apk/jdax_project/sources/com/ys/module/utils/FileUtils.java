package com.ys.module.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

/* JADX INFO: loaded from: classes.dex */
public class FileUtils {
    public static void deleteFile(Context context, String str) {
        File file = new File(str);
        if (file.isFile() && file.exists()) {
            file.delete();
            scanFileAsync(context, str);
        }
    }

    public static void scanFileAsync(Context context, String str) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(new File(str)));
        context.sendBroadcast(intent);
    }
}
