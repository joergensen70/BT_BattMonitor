package com.ys.module.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/* JADX INFO: loaded from: classes.dex */
public class AppUtils {
    public static boolean isExpire(String str, int i) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar2 = Calendar.getInstance();
        try {
            calendar2.setTime(simpleDateFormat.parse(str));
            calendar2.add(5, i);
            return calendar2.getTimeInMillis() < calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static String getIMEI(Context context) {
        String deviceId = ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
        return TextUtils.isEmpty(deviceId) ? "wew-Android" : deviceId;
    }
}
