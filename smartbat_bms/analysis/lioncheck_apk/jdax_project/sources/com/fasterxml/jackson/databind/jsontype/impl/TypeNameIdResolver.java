package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import java.util.Collection;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class TypeNameIdResolver extends TypeIdResolverBase {
    protected final MapperConfig<?> _config;
    protected final HashMap<String, JavaType> _idToType;
    protected final HashMap<String, String> _typeToId;

    protected TypeNameIdResolver(MapperConfig<?> mapperConfig, JavaType javaType, HashMap<String, String> map, HashMap<String, JavaType> map2) {
        super(javaType, mapperConfig.getTypeFactory());
        this._config = mapperConfig;
        this._typeToId = map;
        this._idToType = map2;
    }

    public static TypeNameIdResolver construct(MapperConfig<?> mapperConfig, JavaType javaType, Collection<NamedType> collection, boolean z, boolean z2) {
        JavaType javaType2;
        if (z == z2) {
            throw new IllegalArgumentException();
        }
        HashMap map = z ? new HashMap() : null;
        HashMap map2 = z2 ? new HashMap() : null;
        if (collection != null) {
            for (NamedType namedType : collection) {
                Class<?> type = namedType.getType();
                String name = namedType.hasName() ? namedType.getName() : _defaultTypeId(type);
                if (z) {
                    map.put(type.getName(), name);
                }
                if (z2 && ((javaType2 = (JavaType) map2.get(name)) == null || !type.isAssignableFrom(javaType2.getRawClass()))) {
                    map2.put(name, mapperConfig.constructType(type));
                }
            }
        }
        return new TypeNameIdResolver(mapperConfig, javaType, map, map2);
    }

    @Override // com.fasterxml.jackson.databind.jsontype.TypeIdResolver
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    @Override // com.fasterxml.jackson.databind.jsontype.TypeIdResolver
    public String idFromValue(Object obj) {
        String str_defaultTypeId;
        Class<?> rawClass = this._typeFactory.constructType(obj.getClass()).getRawClass();
        String name = rawClass.getName();
        synchronized (this._typeToId) {
            str_defaultTypeId = this._typeToId.get(name);
            if (str_defaultTypeId == null) {
                if (this._config.isAnnotationProcessingEnabled()) {
                    str_defaultTypeId = this._config.getAnnotationIntrospector().findTypeName(this._config.introspectClassAnnotations(rawClass).getClassInfo());
                }
                if (str_defaultTypeId == null) {
                    str_defaultTypeId = _defaultTypeId(rawClass);
                }
                this._typeToId.put(name, str_defaultTypeId);
            }
        }
        return str_defaultTypeId;
    }

    @Override // com.fasterxml.jackson.databind.jsontype.TypeIdResolver
    public String idFromValueAndType(Object obj, Class<?> cls) {
        if (obj == null) {
            return null;
        }
        return idFromValue(obj);
    }

    @Override // com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase, com.fasterxml.jackson.databind.jsontype.TypeIdResolver
    @Deprecated
    public JavaType typeFromId(String str) {
        return _typeFromId(str);
    }

    @Override // com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
    public JavaType typeFromId(DatabindContext databindContext, String str) {
        return _typeFromId(str);
    }

    protected JavaType _typeFromId(String str) {
        return this._idToType.get(str);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(getClass().getName());
        sb.append("; id-to-type=").append(this._idToType);
        sb.append(']');
        return sb.toString();
    }

    protected static String _defaultTypeId(Class<?> cls) {
        String name = cls.getName();
        int iLastIndexOf = name.lastIndexOf(46);
        return iLastIndexOf < 0 ? name : name.substring(iLastIndexOf + 1);
    }
}
