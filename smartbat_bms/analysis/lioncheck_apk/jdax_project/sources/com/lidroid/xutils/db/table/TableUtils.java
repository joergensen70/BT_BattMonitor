package com.lidroid.xutils.db.table;

import android.text.TextUtils;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.util.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: classes.dex */
public class TableUtils {
    private static ConcurrentHashMap<String, HashMap<String, Column>> entityColumnsMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Id> entityIdMap = new ConcurrentHashMap<>();

    private TableUtils() {
    }

    public static String getTableName(Class<?> cls) {
        com.lidroid.xutils.db.annotation.Table table = (com.lidroid.xutils.db.annotation.Table) cls.getAnnotation(com.lidroid.xutils.db.annotation.Table.class);
        if (table == null || TextUtils.isEmpty(table.name())) {
            return cls.getName().replace('.', '_');
        }
        return table.name();
    }

    public static String getExecAfterTableCreated(Class<?> cls) {
        com.lidroid.xutils.db.annotation.Table table = (com.lidroid.xutils.db.annotation.Table) cls.getAnnotation(com.lidroid.xutils.db.annotation.Table.class);
        if (table != null) {
            return table.execAfterTableCreated();
        }
        return null;
    }

    static synchronized HashMap<String, Column> getColumnMap(Class<?> cls) {
        if (entityColumnsMap.containsKey(cls.getName())) {
            return entityColumnsMap.get(cls.getName());
        }
        HashMap<String, Column> map = new HashMap<>();
        addColumns2Map(cls, getPrimaryKeyFieldName(cls), map);
        entityColumnsMap.put(cls.getName(), map);
        return map;
    }

    private static void addColumns2Map(Class<?> cls, String str, HashMap<String, Column> map) {
        if (Object.class.equals(cls)) {
            return;
        }
        try {
            for (Field field : cls.getDeclaredFields()) {
                if (!ColumnUtils.isTransient(field) && !Modifier.isStatic(field.getModifiers())) {
                    if (ColumnConverterFactory.isSupportColumnConverter(field.getType())) {
                        if (!field.getName().equals(str)) {
                            Column column = new Column(cls, field);
                            if (!map.containsKey(column.getColumnName())) {
                                map.put(column.getColumnName(), column);
                            }
                        }
                    } else if (ColumnUtils.isForeign(field)) {
                        Foreign foreign = new Foreign(cls, field);
                        if (!map.containsKey(foreign.getColumnName())) {
                            map.put(foreign.getColumnName(), foreign);
                        }
                    } else if (ColumnUtils.isFinder(field)) {
                        Finder finder = new Finder(cls, field);
                        if (!map.containsKey(finder.getColumnName())) {
                            map.put(finder.getColumnName(), finder);
                        }
                    }
                }
            }
            if (Object.class.equals(cls.getSuperclass())) {
                return;
            }
            addColumns2Map(cls.getSuperclass(), str, map);
        } catch (Throwable th) {
            LogUtils.e(th.getMessage(), th);
        }
    }

    static Column getColumnOrId(Class<?> cls, String str) {
        if (getPrimaryKeyColumnName(cls).equals(str)) {
            return getId(cls);
        }
        return getColumnMap(cls).get(str);
    }

    static synchronized Id getId(Class<?> cls) {
        if (Object.class.equals(cls)) {
            throw new RuntimeException("field 'id' not found");
        }
        if (entityIdMap.containsKey(cls.getName())) {
            return entityIdMap.get(cls.getName());
        }
        Field[] declaredFields = cls.getDeclaredFields();
        Field field = null;
        if (declaredFields != null) {
            int length = declaredFields.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Field field2 = declaredFields[i];
                if (field2.getAnnotation(com.lidroid.xutils.db.annotation.Id.class) != null) {
                    field = field2;
                    break;
                }
                i++;
            }
            if (field == null) {
                for (Field field3 : declaredFields) {
                    if (!"id".equals(field3.getName()) && !"_id".equals(field3.getName())) {
                    }
                    field = field3;
                    break;
                }
            }
        }
        if (field == null) {
            return getId(cls.getSuperclass());
        }
        Id id = new Id(cls, field);
        entityIdMap.put(cls.getName(), id);
        return id;
    }

    private static String getPrimaryKeyFieldName(Class<?> cls) {
        Id id = getId(cls);
        if (id == null) {
            return null;
        }
        return id.getColumnField().getName();
    }

    private static String getPrimaryKeyColumnName(Class<?> cls) {
        Id id = getId(cls);
        if (id == null) {
            return null;
        }
        return id.getColumnName();
    }
}
