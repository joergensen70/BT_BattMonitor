package com.gddai.lioncheck.dbutils;

import com.gddai.lioncheck.MyApp;
import com.gddai.lioncheck.entity.SocEntity;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class SocEntityDBUtils {
    public static void insertDevice(float f) {
        try {
            MyApp.mDbUtils.saveOrUpdate(new SocEntity());
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void insertDevice(List<SocEntity> list) {
        try {
            MyApp.mDbUtils.createTableIfNotExist(SocEntity.class);
            MyApp.mDbUtils.saveAll(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<SocEntity> findAll() {
        try {
            if (MyApp.mDbUtils.tableIsExist(SocEntity.class)) {
                return (ArrayList) MyApp.mDbUtils.findAll(Selector.from(SocEntity.class).orderBy("_time", false));
            }
            return null;
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteALL() {
        try {
            if (MyApp.mDbUtils.tableIsExist(SocEntity.class)) {
                MyApp.mDbUtils.delete(SocEntity.class);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String str) {
        try {
            if (MyApp.mDbUtils.tableIsExist(SocEntity.class)) {
                MyApp.mDbUtils.delete(SocEntity.class, WhereBuilder.b("_userId", "=", str));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
