package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import java.io.Serializable;
import java.lang.Enum;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class EnumResolver<T extends Enum<T>> implements Serializable {
    private static final long serialVersionUID = 1;
    protected final Class<T> _enumClass;
    protected final T[] _enums;
    protected final HashMap<String, T> _enumsById;

    protected EnumResolver(Class<T> cls, T[] tArr, HashMap<String, T> map) {
        this._enumClass = cls;
        this._enums = tArr;
        this._enumsById = map;
    }

    public static <ET extends Enum<ET>> EnumResolver<ET> constructFor(Class<ET> cls, AnnotationIntrospector annotationIntrospector) {
        ET[] enumConstants = cls.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalArgumentException("No enum constants for class " + cls.getName());
        }
        HashMap map = new HashMap();
        for (ET et : enumConstants) {
            map.put(annotationIntrospector.findEnumValue(et), et);
        }
        return new EnumResolver<>(cls, enumConstants, map);
    }

    public static <ET extends Enum<ET>> EnumResolver<ET> constructUsingToString(Class<ET> cls) {
        ET[] enumConstants = cls.getEnumConstants();
        HashMap map = new HashMap();
        int length = enumConstants.length;
        while (true) {
            length--;
            if (length >= 0) {
                ET et = enumConstants[length];
                map.put(et.toString(), et);
            } else {
                return new EnumResolver<>(cls, enumConstants, map);
            }
        }
    }

    public static <ET extends Enum<ET>> EnumResolver<ET> constructUsingMethod(Class<ET> cls, Method method) {
        ET[] enumConstants = cls.getEnumConstants();
        HashMap map = new HashMap();
        int length = enumConstants.length;
        while (true) {
            length--;
            if (length >= 0) {
                ET et = enumConstants[length];
                try {
                    Object objInvoke = method.invoke(et, new Object[0]);
                    if (objInvoke != null) {
                        map.put(objInvoke.toString(), et);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to access @JsonValue of Enum value " + et + ": " + e.getMessage());
                }
            } else {
                return new EnumResolver<>(cls, enumConstants, map);
            }
        }
    }

    public static EnumResolver<?> constructUnsafe(Class<?> cls, AnnotationIntrospector annotationIntrospector) {
        return constructFor(cls, annotationIntrospector);
    }

    public static EnumResolver<?> constructUnsafeUsingToString(Class<?> cls) {
        return constructUsingToString(cls);
    }

    public static EnumResolver<?> constructUnsafeUsingMethod(Class<?> cls, Method method) {
        return constructUsingMethod(cls, method);
    }

    public T findEnum(String str) {
        return this._enumsById.get(str);
    }

    public T getEnum(int i) {
        if (i < 0) {
            return null;
        }
        T[] tArr = this._enums;
        if (i >= tArr.length) {
            return null;
        }
        return tArr[i];
    }

    public List<T> getEnums() {
        ArrayList arrayList = new ArrayList(this._enums.length);
        for (T t : this._enums) {
            arrayList.add(t);
        }
        return arrayList;
    }

    public Class<T> getEnumClass() {
        return this._enumClass;
    }

    public int lastValidIndex() {
        return this._enums.length - 1;
    }
}
