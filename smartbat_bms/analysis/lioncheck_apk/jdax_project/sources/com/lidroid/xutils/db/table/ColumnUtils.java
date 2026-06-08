package com.lidroid.xutils.db.table;

import android.text.TextUtils;
import com.lidroid.xutils.db.annotation.Check;
import com.lidroid.xutils.db.annotation.NotNull;
import com.lidroid.xutils.db.annotation.Transient;
import com.lidroid.xutils.db.annotation.Unique;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.FinderLazyLoader;
import com.lidroid.xutils.db.sqlite.ForeignLazyLoader;
import com.lidroid.xutils.util.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ColumnUtils {
    private static final HashSet<String> DB_PRIMITIVE_TYPES;

    private ColumnUtils() {
    }

    static {
        HashSet<String> hashSet = new HashSet<>(14);
        DB_PRIMITIVE_TYPES = hashSet;
        hashSet.add(Integer.TYPE.getName());
        hashSet.add(Long.TYPE.getName());
        hashSet.add(Short.TYPE.getName());
        hashSet.add(Byte.TYPE.getName());
        hashSet.add(Float.TYPE.getName());
        hashSet.add(Double.TYPE.getName());
        hashSet.add(Integer.class.getName());
        hashSet.add(Long.class.getName());
        hashSet.add(Short.class.getName());
        hashSet.add(Byte.class.getName());
        hashSet.add(Float.class.getName());
        hashSet.add(Double.class.getName());
        hashSet.add(String.class.getName());
        hashSet.add(byte[].class.getName());
    }

    public static boolean isDbPrimitiveType(Class<?> cls) {
        return DB_PRIMITIVE_TYPES.contains(cls.getName());
    }

    public static Method getColumnGetMethod(Class<?> cls, Field field) {
        String name = field.getName();
        Method booleanColumnGetMethod = field.getType() == Boolean.TYPE ? getBooleanColumnGetMethod(cls, name) : null;
        if (booleanColumnGetMethod == null) {
            String str = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            try {
                booleanColumnGetMethod = cls.getDeclaredMethod(str, new Class[0]);
            } catch (NoSuchMethodException unused) {
                LogUtils.d(str + " not exist");
            }
        }
        return (booleanColumnGetMethod != null || Object.class.equals(cls.getSuperclass())) ? booleanColumnGetMethod : getColumnGetMethod(cls.getSuperclass(), field);
    }

    public static Method getColumnSetMethod(Class<?> cls, Field field) {
        String name = field.getName();
        Method booleanColumnSetMethod = field.getType() == Boolean.TYPE ? getBooleanColumnSetMethod(cls, field) : null;
        if (booleanColumnSetMethod == null) {
            String str = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            try {
                booleanColumnSetMethod = cls.getDeclaredMethod(str, field.getType());
            } catch (NoSuchMethodException unused) {
                LogUtils.d(str + " not exist");
            }
        }
        return (booleanColumnSetMethod != null || Object.class.equals(cls.getSuperclass())) ? booleanColumnSetMethod : getColumnSetMethod(cls.getSuperclass(), field);
    }

    public static String getColumnNameByField(Field field) {
        com.lidroid.xutils.db.annotation.Column column = (com.lidroid.xutils.db.annotation.Column) field.getAnnotation(com.lidroid.xutils.db.annotation.Column.class);
        if (column != null && !TextUtils.isEmpty(column.column())) {
            return column.column();
        }
        com.lidroid.xutils.db.annotation.Id id = (com.lidroid.xutils.db.annotation.Id) field.getAnnotation(com.lidroid.xutils.db.annotation.Id.class);
        if (id != null && !TextUtils.isEmpty(id.column())) {
            return id.column();
        }
        com.lidroid.xutils.db.annotation.Foreign foreign = (com.lidroid.xutils.db.annotation.Foreign) field.getAnnotation(com.lidroid.xutils.db.annotation.Foreign.class);
        if (foreign != null && !TextUtils.isEmpty(foreign.column())) {
            return foreign.column();
        }
        if (((com.lidroid.xutils.db.annotation.Finder) field.getAnnotation(com.lidroid.xutils.db.annotation.Finder.class)) != null) {
            return field.getName();
        }
        return field.getName();
    }

    public static String getForeignColumnNameByField(Field field) {
        com.lidroid.xutils.db.annotation.Foreign foreign = (com.lidroid.xutils.db.annotation.Foreign) field.getAnnotation(com.lidroid.xutils.db.annotation.Foreign.class);
        if (foreign != null) {
            return foreign.foreign();
        }
        return field.getName();
    }

    public static String getColumnDefaultValue(Field field) {
        com.lidroid.xutils.db.annotation.Column column = (com.lidroid.xutils.db.annotation.Column) field.getAnnotation(com.lidroid.xutils.db.annotation.Column.class);
        if (column == null || TextUtils.isEmpty(column.defaultValue())) {
            return null;
        }
        return column.defaultValue();
    }

    public static boolean isTransient(Field field) {
        return field.getAnnotation(Transient.class) != null;
    }

    public static boolean isForeign(Field field) {
        return field.getAnnotation(com.lidroid.xutils.db.annotation.Foreign.class) != null;
    }

    public static boolean isFinder(Field field) {
        return field.getAnnotation(com.lidroid.xutils.db.annotation.Finder.class) != null;
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(Unique.class) != null;
    }

    public static boolean isNotNull(Field field) {
        return field.getAnnotation(NotNull.class) != null;
    }

    public static String getCheck(Field field) {
        Check check = (Check) field.getAnnotation(Check.class);
        if (check != null) {
            return check.value();
        }
        return null;
    }

    public static Class<?> getForeignEntityType(Foreign foreign) {
        Class<?> type = foreign.getColumnField().getType();
        return (type.equals(ForeignLazyLoader.class) || type.equals(List.class)) ? (Class) ((ParameterizedType) foreign.getColumnField().getGenericType()).getActualTypeArguments()[0] : type;
    }

    public static Class<?> getFinderTargetEntityType(Finder finder) {
        Class<?> type = finder.getColumnField().getType();
        return (type.equals(FinderLazyLoader.class) || type.equals(List.class)) ? (Class) ((ParameterizedType) finder.getColumnField().getGenericType()).getActualTypeArguments()[0] : type;
    }

    public static Object convert2DbColumnValueIfNeeded(Object obj) {
        ColumnConverter columnConverter;
        if (obj == null) {
            return obj;
        }
        Class<?> cls = obj.getClass();
        return (isDbPrimitiveType(cls) || (columnConverter = ColumnConverterFactory.getColumnConverter(cls)) == null) ? obj : columnConverter.fieldValue2ColumnValue(obj);
    }

    private static boolean isStartWithIs(String str) {
        return str != null && str.startsWith("is");
    }

    private static Method getBooleanColumnGetMethod(Class<?> cls, String str) {
        String str2 = "is" + str.substring(0, 1).toUpperCase() + str.substring(1);
        if (!isStartWithIs(str)) {
            str = str2;
        }
        try {
            return cls.getDeclaredMethod(str, new Class[0]);
        } catch (NoSuchMethodException unused) {
            LogUtils.d(str + " not exist");
            return null;
        }
    }

    private static Method getBooleanColumnSetMethod(Class<?> cls, Field field) {
        String str;
        String name = field.getName();
        if (isStartWithIs(field.getName())) {
            str = "set" + name.substring(2, 3).toUpperCase() + name.substring(3);
        } else {
            str = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        try {
            return cls.getDeclaredMethod(str, field.getType());
        } catch (NoSuchMethodException unused) {
            LogUtils.d(str + " not exist");
            return null;
        }
    }
}
