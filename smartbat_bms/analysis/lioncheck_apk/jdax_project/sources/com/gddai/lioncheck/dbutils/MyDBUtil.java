package com.gddai.lioncheck.dbutils;

import android.content.Context;
import com.lidroid.xutils.DbUtils;

/* JADX INFO: loaded from: classes.dex */
public class MyDBUtil {
    public static synchronized DbUtils getdbInstance(Context context) {
        return DbUtils.create(context, "clockdb.db", 1, new DbUtils.DbUpgradeListener() { // from class: com.gddai.lioncheck.dbutils.MyDBUtil.1
            @Override // com.lidroid.xutils.DbUtils.DbUpgradeListener
            public void onUpgrade(DbUtils dbUtils, int i, int i2) {
            }
        });
    }
}
