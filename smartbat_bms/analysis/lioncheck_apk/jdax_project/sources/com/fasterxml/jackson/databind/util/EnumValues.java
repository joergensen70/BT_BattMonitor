package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public final class EnumValues {
    private final Class<Enum<?>> _enumClass;
    private final EnumMap<?, SerializableString> _values;

    private EnumValues(Class<Enum<?>> cls, Map<Enum<?>, SerializableString> map) {
        this._enumClass = cls;
        this._values = new EnumMap<>(map);
    }

    public static EnumValues construct(SerializationConfig serializationConfig, Class<Enum<?>> cls) {
        if (serializationConfig.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
            return constructFromToString(serializationConfig, cls);
        }
        return constructFromName(serializationConfig, cls);
    }

    public static EnumValues constructFromName(MapperConfig<?> mapperConfig, Class<Enum<?>> cls) {
        Enum<?>[] enumArr = (Enum[]) ClassUtil.findEnumType(cls).getEnumConstants();
        if (enumArr != null) {
            HashMap map = new HashMap();
            for (Enum<?> r4 : enumArr) {
                map.put(r4, mapperConfig.compileString(mapperConfig.getAnnotationIntrospector().findEnumValue(r4)));
            }
            return new EnumValues(cls, map);
        }
        throw new IllegalArgumentException("Can not determine enum constants for Class " + cls.getName());
    }

    public static EnumValues constructFromToString(MapperConfig<?> mapperConfig, Class<Enum<?>> cls) {
        Enum[] enumArr = (Enum[]) ClassUtil.findEnumType(cls).getEnumConstants();
        if (enumArr != null) {
            HashMap map = new HashMap();
            for (Enum r4 : enumArr) {
                map.put(r4, mapperConfig.compileString(r4.toString()));
            }
            return new EnumValues(cls, map);
        }
        throw new IllegalArgumentException("Can not determine enum constants for Class " + cls.getName());
    }

    public SerializableString serializedValueFor(Enum<?> r2) {
        return this._values.get(r2);
    }

    public Collection<SerializableString> values() {
        return this._values.values();
    }

    public EnumMap<?, SerializableString> internalMap() {
        return this._values;
    }

    public Class<Enum<?>> getEnumClass() {
        return this._enumClass;
    }
}
