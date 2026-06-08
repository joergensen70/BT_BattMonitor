package com.ys.module.utils;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ActivityCollectorUtils {
    public static List<Activity> activities = new ArrayList();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        List<Activity> list = activities;
        if (list == null || list.size() == 0) {
            return;
        }
        for (int size = activities.size() - 1; size >= 0; size--) {
            Activity activity = activities.get(size);
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
            activities.remove(size);
        }
    }
}
