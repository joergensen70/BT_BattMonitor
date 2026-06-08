package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/* JADX INFO: loaded from: classes.dex */
@JacksonStdImpl
public class MapSerializer extends ContainerSerializer<Map<?, ?>> implements ContextualSerializer {
    protected static final JavaType UNSPECIFIED_TYPE = TypeFactory.unknownType();
    protected PropertySerializerMap _dynamicValueSerializers;
    protected final Object _filterId;
    protected final HashSet<String> _ignoredEntries;
    protected JsonSerializer<Object> _keySerializer;
    protected final JavaType _keyType;
    protected final BeanProperty _property;
    protected final boolean _sortKeys;
    protected JsonSerializer<Object> _valueSerializer;
    protected final JavaType _valueType;
    protected final boolean _valueTypeIsStatic;
    protected final TypeSerializer _valueTypeSerializer;

    protected MapSerializer(HashSet<String> hashSet, JavaType javaType, JavaType javaType2, boolean z, TypeSerializer typeSerializer, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2) {
        super(Map.class, false);
        this._ignoredEntries = hashSet;
        this._keyType = javaType;
        this._valueType = javaType2;
        this._valueTypeIsStatic = z;
        this._valueTypeSerializer = typeSerializer;
        this._keySerializer = jsonSerializer;
        this._valueSerializer = jsonSerializer2;
        this._dynamicValueSerializers = PropertySerializerMap.emptyMap();
        this._property = null;
        this._filterId = null;
        this._sortKeys = false;
    }

    protected MapSerializer(MapSerializer mapSerializer, BeanProperty beanProperty, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2, HashSet<String> hashSet) {
        super(Map.class, false);
        this._ignoredEntries = hashSet;
        this._keyType = mapSerializer._keyType;
        this._valueType = mapSerializer._valueType;
        this._valueTypeIsStatic = mapSerializer._valueTypeIsStatic;
        this._valueTypeSerializer = mapSerializer._valueTypeSerializer;
        this._keySerializer = jsonSerializer;
        this._valueSerializer = jsonSerializer2;
        this._dynamicValueSerializers = mapSerializer._dynamicValueSerializers;
        this._property = beanProperty;
        this._filterId = mapSerializer._filterId;
        this._sortKeys = mapSerializer._sortKeys;
    }

    protected MapSerializer(MapSerializer mapSerializer, TypeSerializer typeSerializer) {
        super(Map.class, false);
        this._ignoredEntries = mapSerializer._ignoredEntries;
        this._keyType = mapSerializer._keyType;
        this._valueType = mapSerializer._valueType;
        this._valueTypeIsStatic = mapSerializer._valueTypeIsStatic;
        this._valueTypeSerializer = typeSerializer;
        this._keySerializer = mapSerializer._keySerializer;
        this._valueSerializer = mapSerializer._valueSerializer;
        this._dynamicValueSerializers = mapSerializer._dynamicValueSerializers;
        this._property = mapSerializer._property;
        this._filterId = mapSerializer._filterId;
        this._sortKeys = mapSerializer._sortKeys;
    }

    protected MapSerializer(MapSerializer mapSerializer, Object obj, boolean z) {
        super(Map.class, false);
        this._ignoredEntries = mapSerializer._ignoredEntries;
        this._keyType = mapSerializer._keyType;
        this._valueType = mapSerializer._valueType;
        this._valueTypeIsStatic = mapSerializer._valueTypeIsStatic;
        this._valueTypeSerializer = mapSerializer._valueTypeSerializer;
        this._keySerializer = mapSerializer._keySerializer;
        this._valueSerializer = mapSerializer._valueSerializer;
        this._dynamicValueSerializers = mapSerializer._dynamicValueSerializers;
        this._property = mapSerializer._property;
        this._filterId = obj;
        this._sortKeys = z;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public MapSerializer _withValueTypeSerializer(TypeSerializer typeSerializer) {
        return new MapSerializer(this, typeSerializer);
    }

    @Deprecated
    public MapSerializer withResolved(BeanProperty beanProperty, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2, HashSet<String> hashSet) {
        return withResolved(beanProperty, jsonSerializer, jsonSerializer2, hashSet, this._sortKeys);
    }

    public MapSerializer withResolved(BeanProperty beanProperty, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2, HashSet<String> hashSet, boolean z) {
        MapSerializer mapSerializer = new MapSerializer(this, beanProperty, jsonSerializer, jsonSerializer2, hashSet);
        return z != mapSerializer._sortKeys ? new MapSerializer(mapSerializer, this._filterId, z) : mapSerializer;
    }

    public MapSerializer withFilterId(Object obj) {
        return this._filterId == obj ? this : new MapSerializer(this, obj, this._sortKeys);
    }

    @Deprecated
    public static MapSerializer construct(String[] strArr, JavaType javaType, boolean z, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer, JsonSerializer<Object> jsonSerializer2) {
        return construct(strArr, javaType, z, typeSerializer, jsonSerializer, jsonSerializer2, null);
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x0038  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x003d A[RETURN] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static com.fasterxml.jackson.databind.ser.std.MapSerializer construct(java.lang.String[] r8, com.fasterxml.jackson.databind.JavaType r9, boolean r10, com.fasterxml.jackson.databind.jsontype.TypeSerializer r11, com.fasterxml.jackson.databind.JsonSerializer<java.lang.Object> r12, com.fasterxml.jackson.databind.JsonSerializer<java.lang.Object> r13, java.lang.Object r14) {
        /*
            java.util.HashSet r1 = toSet(r8)
            if (r9 != 0) goto Lb
            com.fasterxml.jackson.databind.JavaType r8 = com.fasterxml.jackson.databind.ser.std.MapSerializer.UNSPECIFIED_TYPE
            r2 = r8
            r3 = r2
            goto L15
        Lb:
            com.fasterxml.jackson.databind.JavaType r8 = r9.getKeyType()
            com.fasterxml.jackson.databind.JavaType r9 = r9.getContentType()
            r2 = r8
            r3 = r9
        L15:
            r8 = 0
            if (r10 != 0) goto L23
            if (r3 == 0) goto L21
            boolean r9 = r3.isFinal()
            if (r9 == 0) goto L21
            r8 = 1
        L21:
            r10 = r8
            goto L2d
        L23:
            java.lang.Class r9 = r3.getRawClass()
            java.lang.Class<java.lang.Object> r0 = java.lang.Object.class
            if (r9 != r0) goto L2d
            r4 = r8
            goto L2e
        L2d:
            r4 = r10
        L2e:
            com.fasterxml.jackson.databind.ser.std.MapSerializer r0 = new com.fasterxml.jackson.databind.ser.std.MapSerializer
            r5 = r11
            r6 = r12
            r7 = r13
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            if (r14 == 0) goto L3d
            com.fasterxml.jackson.databind.ser.std.MapSerializer r8 = r0.withFilterId(r14)
            return r8
        L3d:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.ser.std.MapSerializer.construct(java.lang.String[], com.fasterxml.jackson.databind.JavaType, boolean, com.fasterxml.jackson.databind.jsontype.TypeSerializer, com.fasterxml.jackson.databind.JsonSerializer, com.fasterxml.jackson.databind.JsonSerializer, java.lang.Object):com.fasterxml.jackson.databind.ser.std.MapSerializer");
    }

    private static HashSet<String> toSet(String[] strArr) {
        if (strArr == null || strArr.length == 0) {
            return null;
        }
        HashSet<String> hashSet = new HashSet<>(strArr.length);
        for (String str : strArr) {
            hashSet.add(str);
        }
        return hashSet;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContextualSerializer
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        JsonSerializer<?> jsonSerializerSerializerInstance;
        JsonSerializer<?> jsonSerializerHandleSecondaryContextualization;
        Object objFindFilterId;
        AnnotationIntrospector annotationIntrospector = serializerProvider.getAnnotationIntrospector();
        JsonSerializer<Object> jsonSerializerSerializerInstance2 = null;
        AnnotatedMember member = beanProperty == null ? null : beanProperty.getMember();
        if (member == null || annotationIntrospector == null) {
            jsonSerializerSerializerInstance = null;
        } else {
            Object objFindKeySerializer = annotationIntrospector.findKeySerializer(member);
            jsonSerializerSerializerInstance = objFindKeySerializer != null ? serializerProvider.serializerInstance(member, objFindKeySerializer) : null;
            Object objFindContentSerializer = annotationIntrospector.findContentSerializer(member);
            if (objFindContentSerializer != null) {
                jsonSerializerSerializerInstance2 = serializerProvider.serializerInstance(member, objFindContentSerializer);
            }
        }
        if (jsonSerializerSerializerInstance2 == null) {
            jsonSerializerSerializerInstance2 = this._valueSerializer;
        }
        JsonSerializer<?> jsonSerializerFindConvertingContentSerializer = findConvertingContentSerializer(serializerProvider, beanProperty, jsonSerializerSerializerInstance2);
        if (jsonSerializerFindConvertingContentSerializer == null) {
            if ((this._valueTypeIsStatic && this._valueType.getRawClass() != Object.class) || hasContentTypeAnnotation(serializerProvider, beanProperty)) {
                jsonSerializerFindConvertingContentSerializer = serializerProvider.findValueSerializer(this._valueType, beanProperty);
            }
        } else {
            jsonSerializerFindConvertingContentSerializer = serializerProvider.handleSecondaryContextualization(jsonSerializerFindConvertingContentSerializer, beanProperty);
        }
        JsonSerializer<?> jsonSerializer = jsonSerializerFindConvertingContentSerializer;
        if (jsonSerializerSerializerInstance == null) {
            jsonSerializerSerializerInstance = this._keySerializer;
        }
        if (jsonSerializerSerializerInstance == null) {
            jsonSerializerHandleSecondaryContextualization = serializerProvider.findKeySerializer(this._keyType, beanProperty);
        } else {
            jsonSerializerHandleSecondaryContextualization = serializerProvider.handleSecondaryContextualization(jsonSerializerSerializerInstance, beanProperty);
        }
        JsonSerializer<?> jsonSerializer2 = jsonSerializerHandleSecondaryContextualization;
        HashSet<String> hashSet = this._ignoredEntries;
        boolean z = false;
        if (annotationIntrospector != null && member != null) {
            String[] strArrFindPropertiesToIgnore = annotationIntrospector.findPropertiesToIgnore(member);
            if (strArrFindPropertiesToIgnore != null) {
                hashSet = hashSet == null ? new HashSet<>() : new HashSet<>(hashSet);
                for (String str : strArrFindPropertiesToIgnore) {
                    hashSet.add(str);
                }
            }
            Boolean boolFindSerializationSortAlphabetically = annotationIntrospector.findSerializationSortAlphabetically(member);
            if (boolFindSerializationSortAlphabetically != null && boolFindSerializationSortAlphabetically.booleanValue()) {
                z = true;
            }
        }
        MapSerializer mapSerializerWithResolved = withResolved(beanProperty, jsonSerializer2, jsonSerializer, hashSet, z);
        return (beanProperty == null || (objFindFilterId = annotationIntrospector.findFilterId(beanProperty.getMember())) == null) ? mapSerializerWithResolved : mapSerializerWithResolved.withFilterId(objFindFilterId);
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public JavaType getContentType() {
        return this._valueType;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public JsonSerializer<?> getContentSerializer() {
        return this._valueSerializer;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer, com.fasterxml.jackson.databind.JsonSerializer
    public boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public boolean hasSingleElement(Map<?, ?> map) {
        return map.size() == 1;
    }

    public JsonSerializer<?> getKeySerializer() {
        return this._keySerializer;
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.JsonSerializer
    public void serialize(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (!map.isEmpty()) {
            Object obj = this._filterId;
            if (obj != null) {
                serializeFilteredFields(map, jsonGenerator, serializerProvider, findPropertyFilter(serializerProvider, obj, map));
                jsonGenerator.writeEndObject();
                return;
            }
            if (this._sortKeys || serializerProvider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
                map = _orderEntries(map);
            }
            JsonSerializer<Object> jsonSerializer = this._valueSerializer;
            if (jsonSerializer != null) {
                serializeFieldsUsing(map, jsonGenerator, serializerProvider, jsonSerializer);
            } else {
                serializeFields(map, jsonGenerator, serializerProvider);
            }
        }
        jsonGenerator.writeEndObject();
    }

    @Override // com.fasterxml.jackson.databind.JsonSerializer
    public void serializeWithType(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
        typeSerializer.writeTypePrefixForObject(map, jsonGenerator);
        if (!map.isEmpty()) {
            if (this._sortKeys || serializerProvider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
                map = _orderEntries(map);
            }
            JsonSerializer<Object> jsonSerializer = this._valueSerializer;
            if (jsonSerializer != null) {
                serializeFieldsUsing(map, jsonGenerator, serializerProvider, jsonSerializer);
            } else {
                serializeFields(map, jsonGenerator, serializerProvider);
            }
        }
        typeSerializer.writeTypeSuffixForObject(map, jsonGenerator);
    }

    public void serializeFields(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        JsonSerializer<Object> jsonSerializer_findAndAddDynamic;
        if (this._valueTypeSerializer != null) {
            serializeTypedFields(map, jsonGenerator, serializerProvider);
            return;
        }
        JsonSerializer<Object> jsonSerializer = this._keySerializer;
        HashSet<String> hashSet = this._ignoredEntries;
        boolean zIsEnabled = serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES);
        PropertySerializerMap propertySerializerMap = this._dynamicValueSerializers;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (key == null) {
                serializerProvider.findNullKeySerializer(this._keyType, this._property).serialize(null, jsonGenerator, serializerProvider);
            } else if (zIsEnabled || value != null) {
                if (hashSet == null || !hashSet.contains(key)) {
                    jsonSerializer.serialize(key, jsonGenerator, serializerProvider);
                }
            }
            if (value == null) {
                serializerProvider.defaultSerializeNull(jsonGenerator);
            } else {
                Class<?> cls = value.getClass();
                JsonSerializer<Object> jsonSerializerSerializerFor = propertySerializerMap.serializerFor(cls);
                if (jsonSerializerSerializerFor == null) {
                    if (this._valueType.hasGenericTypes()) {
                        jsonSerializer_findAndAddDynamic = _findAndAddDynamic(propertySerializerMap, serializerProvider.constructSpecializedType(this._valueType, cls), serializerProvider);
                    } else {
                        jsonSerializer_findAndAddDynamic = _findAndAddDynamic(propertySerializerMap, cls, serializerProvider);
                    }
                    jsonSerializerSerializerFor = jsonSerializer_findAndAddDynamic;
                    propertySerializerMap = this._dynamicValueSerializers;
                }
                try {
                    jsonSerializerSerializerFor.serialize(value, jsonGenerator, serializerProvider);
                } catch (Exception e) {
                    wrapAndThrow(serializerProvider, e, map, "" + key);
                }
            }
        }
    }

    protected void serializeFieldsUsing(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, JsonSerializer<Object> jsonSerializer) throws IOException {
        JsonSerializer<Object> jsonSerializer2 = this._keySerializer;
        HashSet<String> hashSet = this._ignoredEntries;
        TypeSerializer typeSerializer = this._valueTypeSerializer;
        boolean zIsEnabled = serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (key == null) {
                serializerProvider.findNullKeySerializer(this._keyType, this._property).serialize(null, jsonGenerator, serializerProvider);
            } else if (zIsEnabled || value != null) {
                if (hashSet == null || !hashSet.contains(key)) {
                    jsonSerializer2.serialize(key, jsonGenerator, serializerProvider);
                }
            }
            if (value == null) {
                serializerProvider.defaultSerializeNull(jsonGenerator);
            } else if (typeSerializer == null) {
                try {
                    jsonSerializer.serialize(value, jsonGenerator, serializerProvider);
                } catch (Exception e) {
                    wrapAndThrow(serializerProvider, e, map, "" + key);
                }
            } else {
                jsonSerializer.serializeWithType(value, jsonGenerator, serializerProvider, typeSerializer);
            }
        }
    }

    /* JADX WARN: Can't wrap try/catch for region: R(9:5|(2:35|7)(1:(2:37|(2:39|15)(2:42|40))(2:41|40))|(1:17)(2:18|(3:20|(1:22)(1:23)|24)(1:25))|26|32|27|43|40|3) */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x007c, code lost:
    
        r5 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x007d, code lost:
    
        wrapAndThrow(r13, r5, r11, "" + r6);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void serializeFilteredFields(java.util.Map<?, ?> r11, com.fasterxml.jackson.core.JsonGenerator r12, com.fasterxml.jackson.databind.SerializerProvider r13, com.fasterxml.jackson.databind.ser.PropertyFilter r14) throws java.io.IOException {
        /*
            r10 = this;
            java.util.HashSet<java.lang.String> r0 = r10._ignoredEntries
            com.fasterxml.jackson.databind.SerializationFeature r1 = com.fasterxml.jackson.databind.SerializationFeature.WRITE_NULL_MAP_VALUES
            boolean r1 = r13.isEnabled(r1)
            com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap r2 = r10._dynamicValueSerializers
            com.fasterxml.jackson.databind.ser.std.MapProperty r3 = new com.fasterxml.jackson.databind.ser.std.MapProperty
            com.fasterxml.jackson.databind.jsontype.TypeSerializer r4 = r10._valueTypeSerializer
            r3.<init>(r4)
            java.util.Set r4 = r11.entrySet()
            java.util.Iterator r4 = r4.iterator()
        L19:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L90
            java.lang.Object r5 = r4.next()
            java.util.Map$Entry r5 = (java.util.Map.Entry) r5
            java.lang.Object r6 = r5.getKey()
            java.lang.Object r5 = r5.getValue()
            if (r6 != 0) goto L38
            com.fasterxml.jackson.databind.JavaType r7 = r10._keyType
            com.fasterxml.jackson.databind.BeanProperty r8 = r10._property
            com.fasterxml.jackson.databind.JsonSerializer r7 = r13.findNullKeySerializer(r7, r8)
            goto L48
        L38:
            if (r1 != 0) goto L3d
            if (r5 != 0) goto L3d
            goto L19
        L3d:
            if (r0 == 0) goto L46
            boolean r7 = r0.contains(r6)
            if (r7 == 0) goto L46
            goto L19
        L46:
            com.fasterxml.jackson.databind.JsonSerializer<java.lang.Object> r7 = r10._keySerializer
        L48:
            if (r5 != 0) goto L4f
            com.fasterxml.jackson.databind.JsonSerializer r8 = r13.getDefaultNullValueSerializer()
            goto L75
        L4f:
            java.lang.Class r8 = r5.getClass()
            com.fasterxml.jackson.databind.JsonSerializer r9 = r2.serializerFor(r8)
            if (r9 != 0) goto L74
            com.fasterxml.jackson.databind.JavaType r9 = r10._valueType
            boolean r9 = r9.hasGenericTypes()
            if (r9 == 0) goto L6c
            com.fasterxml.jackson.databind.JavaType r9 = r10._valueType
            com.fasterxml.jackson.databind.JavaType r8 = r13.constructSpecializedType(r9, r8)
            com.fasterxml.jackson.databind.JsonSerializer r2 = r10._findAndAddDynamic(r2, r8, r13)
            goto L70
        L6c:
            com.fasterxml.jackson.databind.JsonSerializer r2 = r10._findAndAddDynamic(r2, r8, r13)
        L70:
            r8 = r2
            com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap r2 = r10._dynamicValueSerializers
            goto L75
        L74:
            r8 = r9
        L75:
            r3.reset(r6, r5, r7, r8)
            r14.serializeAsField(r11, r12, r13, r3)     // Catch: java.lang.Exception -> L7c
            goto L19
        L7c:
            r5 = move-exception
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = ""
            r7.<init>(r8)
            java.lang.StringBuilder r6 = r7.append(r6)
            java.lang.String r6 = r6.toString()
            r10.wrapAndThrow(r13, r5, r11, r6)
            goto L19
        L90:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.ser.std.MapSerializer.serializeFilteredFields(java.util.Map, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.ser.PropertyFilter):void");
    }

    protected void serializeTypedFields(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        JsonSerializer<Object> jsonSerializerFindValueSerializer;
        JsonSerializer<Object> jsonSerializer = this._keySerializer;
        HashSet<String> hashSet = this._ignoredEntries;
        boolean zIsEnabled = serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES);
        Class<?> cls = null;
        JsonSerializer<Object> jsonSerializer2 = null;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (key == null) {
                serializerProvider.findNullKeySerializer(this._keyType, this._property).serialize(null, jsonGenerator, serializerProvider);
            } else if (zIsEnabled || value != null) {
                if (hashSet == null || !hashSet.contains(key)) {
                    jsonSerializer.serialize(key, jsonGenerator, serializerProvider);
                }
            }
            if (value == null) {
                serializerProvider.defaultSerializeNull(jsonGenerator);
            } else {
                Class<?> cls2 = value.getClass();
                if (cls2 != cls) {
                    if (this._valueType.hasGenericTypes()) {
                        jsonSerializerFindValueSerializer = serializerProvider.findValueSerializer(serializerProvider.constructSpecializedType(this._valueType, cls2), this._property);
                    } else {
                        jsonSerializerFindValueSerializer = serializerProvider.findValueSerializer(cls2, this._property);
                    }
                    jsonSerializer2 = jsonSerializerFindValueSerializer;
                    cls = cls2;
                }
                try {
                    jsonSerializer2.serializeWithType(value, jsonGenerator, serializerProvider, this._valueTypeSerializer);
                } catch (Exception e) {
                    wrapAndThrow(serializerProvider, e, map, "" + key);
                }
            }
        }
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.jsonschema.SchemaAware
    public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
        return createSchemaNode("object", true);
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.JsonSerializer, com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
        JsonMapFormatVisitor jsonMapFormatVisitorExpectMapFormat = jsonFormatVisitorWrapper == null ? null : jsonFormatVisitorWrapper.expectMapFormat(javaType);
        if (jsonMapFormatVisitorExpectMapFormat != null) {
            jsonMapFormatVisitorExpectMapFormat.keyFormat(this._keySerializer, this._keyType);
            JsonSerializer<Object> jsonSerializer_findAndAddDynamic = this._valueSerializer;
            if (jsonSerializer_findAndAddDynamic == null) {
                jsonSerializer_findAndAddDynamic = _findAndAddDynamic(this._dynamicValueSerializers, this._valueType, jsonFormatVisitorWrapper.getProvider());
            }
            jsonMapFormatVisitorExpectMapFormat.valueFormat(jsonSerializer_findAndAddDynamic, this._valueType);
        }
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap propertySerializerMap, Class<?> cls, SerializerProvider serializerProvider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult serializerAndMapResultFindAndAddSecondarySerializer = propertySerializerMap.findAndAddSecondarySerializer(cls, serializerProvider, this._property);
        if (propertySerializerMap != serializerAndMapResultFindAndAddSecondarySerializer.map) {
            this._dynamicValueSerializers = serializerAndMapResultFindAndAddSecondarySerializer.map;
        }
        return serializerAndMapResultFindAndAddSecondarySerializer.serializer;
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap propertySerializerMap, JavaType javaType, SerializerProvider serializerProvider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult serializerAndMapResultFindAndAddSecondarySerializer = propertySerializerMap.findAndAddSecondarySerializer(javaType, serializerProvider, this._property);
        if (propertySerializerMap != serializerAndMapResultFindAndAddSecondarySerializer.map) {
            this._dynamicValueSerializers = serializerAndMapResultFindAndAddSecondarySerializer.map;
        }
        return serializerAndMapResultFindAndAddSecondarySerializer.serializer;
    }

    protected Map<?, ?> _orderEntries(Map<?, ?> map) {
        return map instanceof SortedMap ? map : new TreeMap(map);
    }
}
