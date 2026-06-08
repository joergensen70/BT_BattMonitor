package com.ys.module.utils;

import com.lidroid.xutils.util.LogUtils;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class DateUtils {
    public static String getDate() {
        String str = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        LogUtils.e("date" + str);
        return str;
    }

    public static String getDatetwo() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String TimeInMillsTrasToDateOne(Long l, int i) {
        SimpleDateFormat simpleDateFormat;
        if (l == null) {
            return "";
        }
        Calendar calendar = null;
        if (i == 1) {
            simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        } else if (i == 2) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        } else if (i == 3) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        } else if (i == 4) {
            simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        } else {
            simpleDateFormat = i != 5 ? null : new SimpleDateFormat("HH:mm");
        }
        if (l != null) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(l.longValue());
        }
        return simpleDateFormat.format(calendar.getTime());
    }

    public static Long DateTransTimeInMills(int i, int i2, int i3) {
        try {
            return Long.valueOf(new SimpleDateFormat("yyyy-MM-dd").parse(i + "-" + i2 + "-" + i3).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Long strTransTimeInMinlls(String str) {
        try {
            return Long.valueOf(new SimpleDateFormat("yyyy-MM-dd").parse(str).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isBetweenDate(String str, String str2) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            return simpleDateFormat.parse(str).before(simpleDateFormat.parse(str2));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isBeforeNow(String str, int i) {
        SimpleDateFormat simpleDateFormat;
        if (i == 1) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        } else {
            simpleDateFormat = i != 2 ? null : new SimpleDateFormat("yyyy-MM-dd");
        }
        try {
            return simpleDateFormat.parse(str).after(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean compareTo(String str, String str2) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            return simpleDateFormat.parse(str).compareTo(simpleDateFormat.parse(str2)) == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getAge(Date date) {
        return new DecimalFormat("#").format((((new Date().getTime() - date.getTime()) / 86400000) + 1) / 365.0f);
    }

    public static String getAge(String str) {
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date2 = new Date();
        try {
            date = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        return new DecimalFormat("#").format((((date2.getTime() - date.getTime()) / 86400000) + 1) / 365.0f);
    }

    public static Date transformDate(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (Exception unused) {
            return null;
        }
    }
}
