package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class StdSubtypeResolver extends SubtypeResolver implements Serializable {
    private static final long serialVersionUID = 1;
    protected LinkedHashSet<NamedType> _registeredSubtypes;

    @Override // com.fasterxml.jackson.databind.jsontype.SubtypeResolver
    public void registerSubtypes(NamedType... namedTypeArr) {
        if (this._registeredSubtypes == null) {
            this._registeredSubtypes = new LinkedHashSet<>();
        }
        for (NamedType namedType : namedTypeArr) {
            this._registeredSubtypes.add(namedType);
        }
    }

    @Override // com.fasterxml.jackson.databind.jsontype.SubtypeResolver
    public void registerSubtypes(Class<?>... clsArr) {
        NamedType[] namedTypeArr = new NamedType[clsArr.length];
        int length = clsArr.length;
        for (int i = 0; i < length; i++) {
            namedTypeArr[i] = new NamedType(clsArr[i]);
        }
        registerSubtypes(namedTypeArr);
    }

    @Override // com.fasterxml.jackson.databind.jsontype.SubtypeResolver
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember annotatedMember, MapperConfig<?> mapperConfig, AnnotationIntrospector annotationIntrospector, JavaType javaType) {
        MapperConfig<?> mapperConfig2;
        AnnotationIntrospector annotationIntrospector2;
        Class<?> rawType = javaType == null ? annotatedMember.getRawType() : javaType.getRawClass();
        HashMap<NamedType, NamedType> map = new HashMap<>();
        LinkedHashSet<NamedType> linkedHashSet = this._registeredSubtypes;
        if (linkedHashSet != null) {
            for (NamedType namedType : linkedHashSet) {
                if (rawType.isAssignableFrom(namedType.getType())) {
                    mapperConfig2 = mapperConfig;
                    annotationIntrospector2 = annotationIntrospector;
                    _collectAndResolve(AnnotatedClass.constructWithoutSuperTypes(namedType.getType(), annotationIntrospector, mapperConfig), namedType, mapperConfig2, annotationIntrospector2, map);
                } else {
                    mapperConfig2 = mapperConfig;
                    annotationIntrospector2 = annotationIntrospector;
                }
                mapperConfig = mapperConfig2;
                annotationIntrospector = annotationIntrospector2;
            }
        }
        MapperConfig<?> mapperConfig3 = mapperConfig;
        AnnotationIntrospector annotationIntrospector3 = annotationIntrospector;
        List<NamedType> listFindSubtypes = annotationIntrospector3.findSubtypes(annotatedMember);
        if (listFindSubtypes != null) {
            for (NamedType namedType2 : listFindSubtypes) {
                _collectAndResolve(AnnotatedClass.constructWithoutSuperTypes(namedType2.getType(), annotationIntrospector3, mapperConfig3), namedType2, mapperConfig3, annotationIntrospector3, map);
            }
        }
        _collectAndResolve(AnnotatedClass.constructWithoutSuperTypes(rawType, annotationIntrospector3, mapperConfig3), new NamedType(rawType, null), mapperConfig3, annotationIntrospector3, map);
        return new ArrayList(map.values());
    }

    @Override // com.fasterxml.jackson.databind.jsontype.SubtypeResolver
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass annotatedClass, MapperConfig<?> mapperConfig, AnnotationIntrospector annotationIntrospector) {
        MapperConfig<?> mapperConfig2;
        AnnotationIntrospector annotationIntrospector2;
        HashMap<NamedType, NamedType> map = new HashMap<>();
        if (this._registeredSubtypes != null) {
            Class<?> rawType = annotatedClass.getRawType();
            for (NamedType namedType : this._registeredSubtypes) {
                if (rawType.isAssignableFrom(namedType.getType())) {
                    mapperConfig2 = mapperConfig;
                    annotationIntrospector2 = annotationIntrospector;
                    _collectAndResolve(AnnotatedClass.constructWithoutSuperTypes(namedType.getType(), annotationIntrospector, mapperConfig), namedType, mapperConfig2, annotationIntrospector2, map);
                } else {
                    mapperConfig2 = mapperConfig;
                    annotationIntrospector2 = annotationIntrospector;
                }
                mapperConfig = mapperConfig2;
                annotationIntrospector = annotationIntrospector2;
            }
        }
        _collectAndResolve(annotatedClass, new NamedType(annotatedClass.getRawType(), null), mapperConfig, annotationIntrospector, map);
        return new ArrayList(map.values());
    }

    protected void _collectAndResolve(AnnotatedClass annotatedClass, NamedType namedType, MapperConfig<?> mapperConfig, AnnotationIntrospector annotationIntrospector, HashMap<NamedType, NamedType> map) {
        String strFindTypeName;
        if (!namedType.hasName() && (strFindTypeName = annotationIntrospector.findTypeName(annotatedClass)) != null) {
            namedType = new NamedType(namedType.getType(), strFindTypeName);
        }
        if (map.containsKey(namedType)) {
            if (!namedType.hasName() || map.get(namedType).hasName()) {
                return;
            }
            map.put(namedType, namedType);
            return;
        }
        map.put(namedType, namedType);
        List<NamedType> listFindSubtypes = annotationIntrospector.findSubtypes(annotatedClass);
        if (listFindSubtypes == null || listFindSubtypes.isEmpty()) {
            return;
        }
        for (NamedType namedType2 : listFindSubtypes) {
            AnnotatedClass annotatedClassConstructWithoutSuperTypes = AnnotatedClass.constructWithoutSuperTypes(namedType2.getType(), annotationIntrospector, mapperConfig);
            MapperConfig<?> mapperConfig2 = mapperConfig;
            AnnotationIntrospector annotationIntrospector2 = annotationIntrospector;
            HashMap<NamedType, NamedType> map2 = map;
            _collectAndResolve(annotatedClassConstructWithoutSuperTypes, !namedType2.hasName() ? new NamedType(namedType2.getType(), annotationIntrospector.findTypeName(annotatedClassConstructWithoutSuperTypes)) : namedType2, mapperConfig2, annotationIntrospector2, map2);
            mapperConfig = mapperConfig2;
            annotationIntrospector = annotationIntrospector2;
            map = map2;
        }
    }
}
