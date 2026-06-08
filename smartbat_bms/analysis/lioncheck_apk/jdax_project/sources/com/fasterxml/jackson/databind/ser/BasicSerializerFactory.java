package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ext.OptionalHandlerFactory;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;
import com.fasterxml.jackson.databind.ser.impl.IteratorSerializer;
import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;
import com.fasterxml.jackson.databind.ser.std.BooleanSerializer;
import com.fasterxml.jackson.databind.ser.std.ByteBufferSerializer;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import com.fasterxml.jackson.databind.ser.std.EnumSetSerializer;
import com.fasterxml.jackson.databind.ser.std.InetAddressSerializer;
import com.fasterxml.jackson.databind.ser.std.InetSocketAddressSerializer;
import com.fasterxml.jackson.databind.ser.std.IterableSerializer;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.databind.ser.std.ObjectArraySerializer;
import com.fasterxml.jackson.databind.ser.std.SerializableSerializer;
import com.fasterxml.jackson.databind.ser.std.SqlDateSerializer;
import com.fasterxml.jackson.databind.ser.std.SqlTimeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdArraySerializers;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.ser.std.TimeZoneSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.TokenBufferSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.TimeZone;

/* JADX INFO: loaded from: classes.dex */
public abstract class BasicSerializerFactory extends SerializerFactory implements Serializable {
    protected static final HashMap<String, JsonSerializer<?>> _concrete;
    protected static final HashMap<String, Class<? extends JsonSerializer<?>>> _concreteLazy;
    protected final SerializerFactoryConfig _factoryConfig;

    @Override // com.fasterxml.jackson.databind.ser.SerializerFactory
    public abstract JsonSerializer<Object> createSerializer(SerializerProvider serializerProvider, JavaType javaType) throws JsonMappingException;

    protected abstract Iterable<Serializers> customSerializers();

    public abstract SerializerFactory withConfig(SerializerFactoryConfig serializerFactoryConfig);

    static {
        HashMap<String, JsonSerializer<?>> map = new HashMap<>();
        _concrete = map;
        HashMap<String, Class<? extends JsonSerializer<?>>> map2 = new HashMap<>();
        _concreteLazy = map2;
        map.put(String.class.getName(), new StringSerializer());
        ToStringSerializer toStringSerializer = ToStringSerializer.instance;
        map.put(StringBuffer.class.getName(), toStringSerializer);
        map.put(StringBuilder.class.getName(), toStringSerializer);
        map.put(Character.class.getName(), toStringSerializer);
        map.put(Character.TYPE.getName(), toStringSerializer);
        NumberSerializers.addAll(map);
        map.put(Boolean.TYPE.getName(), new BooleanSerializer(true));
        map.put(Boolean.class.getName(), new BooleanSerializer(false));
        NumberSerializer numberSerializer = NumberSerializer.instance;
        map.put(BigInteger.class.getName(), numberSerializer);
        map.put(BigDecimal.class.getName(), numberSerializer);
        map.put(Calendar.class.getName(), CalendarSerializer.instance);
        DateSerializer dateSerializer = DateSerializer.instance;
        map.put(Date.class.getName(), dateSerializer);
        map.put(Timestamp.class.getName(), dateSerializer);
        map2.put(java.sql.Date.class.getName(), SqlDateSerializer.class);
        map2.put(Time.class.getName(), SqlTimeSerializer.class);
        for (Map.Entry<Class<?>, Object> entry : StdJdkSerializers.all()) {
            Object value = entry.getValue();
            if (value instanceof JsonSerializer) {
                _concrete.put(entry.getKey().getName(), (JsonSerializer) value);
            } else if (value instanceof Class) {
                _concreteLazy.put(entry.getKey().getName(), (Class) value);
            } else {
                throw new IllegalStateException("Internal error: unrecognized value of type " + entry.getClass().getName());
            }
        }
        _concreteLazy.put(TokenBuffer.class.getName(), TokenBufferSerializer.class);
    }

    protected BasicSerializerFactory(SerializerFactoryConfig serializerFactoryConfig) {
        this._factoryConfig = serializerFactoryConfig == null ? new SerializerFactoryConfig() : serializerFactoryConfig;
    }

    public SerializerFactoryConfig getFactoryConfig() {
        return this._factoryConfig;
    }

    @Override // com.fasterxml.jackson.databind.ser.SerializerFactory
    public final SerializerFactory withAdditionalSerializers(Serializers serializers) {
        return withConfig(this._factoryConfig.withAdditionalSerializers(serializers));
    }

    @Override // com.fasterxml.jackson.databind.ser.SerializerFactory
    public final SerializerFactory withAdditionalKeySerializers(Serializers serializers) {
        return withConfig(this._factoryConfig.withAdditionalKeySerializers(serializers));
    }

    @Override // com.fasterxml.jackson.databind.ser.SerializerFactory
    public final SerializerFactory withSerializerModifier(BeanSerializerModifier beanSerializerModifier) {
        return withConfig(this._factoryConfig.withSerializerModifier(beanSerializerModifier));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.fasterxml.jackson.databind.ser.SerializerFactory
    public JsonSerializer<Object> createKeySerializer(SerializationConfig serializationConfig, JavaType javaType, JsonSerializer<Object> jsonSerializer) {
        BeanDescription beanDescriptionIntrospectClassAnnotations = serializationConfig.introspectClassAnnotations(javaType.getRawClass());
        JsonSerializer<?> jsonSerializerFindSerializer = null;
        if (this._factoryConfig.hasKeySerializers()) {
            Iterator<Serializers> it = this._factoryConfig.keySerializers().iterator();
            while (it.hasNext() && (jsonSerializerFindSerializer = it.next().findSerializer(serializationConfig, javaType, beanDescriptionIntrospectClassAnnotations)) == null) {
            }
        }
        if (jsonSerializerFindSerializer != null) {
            jsonSerializer = jsonSerializerFindSerializer;
        } else if (jsonSerializer == null) {
            jsonSerializer = StdKeySerializers.getStdKeySerializer(javaType);
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it2 = this._factoryConfig.serializerModifiers().iterator();
            while (it2.hasNext()) {
                jsonSerializer = it2.next().modifyKeySerializer(serializationConfig, javaType, beanDescriptionIntrospectClassAnnotations, jsonSerializer);
            }
        }
        return jsonSerializer;
    }

    @Override // com.fasterxml.jackson.databind.ser.SerializerFactory
    public TypeSerializer createTypeSerializer(SerializationConfig serializationConfig, JavaType javaType) {
        Collection<NamedType> collectionCollectAndResolveSubtypes;
        AnnotatedClass classInfo = serializationConfig.introspectClassAnnotations(javaType.getRawClass()).getClassInfo();
        AnnotationIntrospector annotationIntrospector = serializationConfig.getAnnotationIntrospector();
        TypeResolverBuilder<?> typeResolverBuilderFindTypeResolver = annotationIntrospector.findTypeResolver(serializationConfig, classInfo, javaType);
        if (typeResolverBuilderFindTypeResolver == null) {
            typeResolverBuilderFindTypeResolver = serializationConfig.getDefaultTyper(javaType);
            collectionCollectAndResolveSubtypes = null;
        } else {
            collectionCollectAndResolveSubtypes = serializationConfig.getSubtypeResolver().collectAndResolveSubtypes(classInfo, serializationConfig, annotationIntrospector);
        }
        if (typeResolverBuilderFindTypeResolver == null) {
            return null;
        }
        return typeResolverBuilderFindTypeResolver.buildTypeSerializer(serializationConfig, javaType, collectionCollectAndResolveSubtypes);
    }

    protected final JsonSerializer<?> findSerializerByLookup(JavaType javaType, SerializationConfig serializationConfig, BeanDescription beanDescription, boolean z) {
        Class<? extends JsonSerializer<?>> cls;
        String name = javaType.getRawClass().getName();
        JsonSerializer<?> jsonSerializer = _concrete.get(name);
        if (jsonSerializer != null || (cls = _concreteLazy.get(name)) == null) {
            return jsonSerializer;
        }
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate standard serializer (of type " + cls.getName() + "): " + e.getMessage(), e);
        }
    }

    protected final JsonSerializer<?> findSerializerByAnnotations(SerializerProvider serializerProvider, JavaType javaType, BeanDescription beanDescription) throws JsonMappingException {
        if (JsonSerializable.class.isAssignableFrom(javaType.getRawClass())) {
            return SerializableSerializer.instance;
        }
        AnnotatedMethod annotatedMethodFindJsonValueMethod = beanDescription.findJsonValueMethod();
        if (annotatedMethodFindJsonValueMethod == null) {
            return null;
        }
        Method annotated = annotatedMethodFindJsonValueMethod.getAnnotated();
        if (serializerProvider.canOverrideAccessModifiers()) {
            ClassUtil.checkAndFixAccess(annotated);
        }
        return new JsonValueSerializer(annotated, findSerializerFromAnnotation(serializerProvider, annotatedMethodFindJsonValueMethod));
    }

    protected final JsonSerializer<?> findSerializerByPrimaryType(SerializerProvider serializerProvider, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        Class<?> rawClass = javaType.getRawClass();
        JsonSerializer<?> jsonSerializerFindOptionalStdSerializer = findOptionalStdSerializer(serializerProvider, javaType, beanDescription, z);
        if (jsonSerializerFindOptionalStdSerializer != null) {
            return jsonSerializerFindOptionalStdSerializer;
        }
        if (Calendar.class.isAssignableFrom(rawClass)) {
            return CalendarSerializer.instance;
        }
        if (Date.class.isAssignableFrom(rawClass)) {
            return DateSerializer.instance;
        }
        if (ByteBuffer.class.isAssignableFrom(rawClass)) {
            return new ByteBufferSerializer();
        }
        if (InetAddress.class.isAssignableFrom(rawClass)) {
            return new InetAddressSerializer();
        }
        if (InetSocketAddress.class.isAssignableFrom(rawClass)) {
            return new InetSocketAddressSerializer();
        }
        if (TimeZone.class.isAssignableFrom(rawClass)) {
            return new TimeZoneSerializer();
        }
        if (Charset.class.isAssignableFrom(rawClass)) {
            return ToStringSerializer.instance;
        }
        if (Number.class.isAssignableFrom(rawClass)) {
            JsonFormat.Value valueFindExpectedFormat = beanDescription.findExpectedFormat(null);
            if (valueFindExpectedFormat != null) {
                int i = AnonymousClass1.$SwitchMap$com$fasterxml$jackson$annotation$JsonFormat$Shape[valueFindExpectedFormat.getShape().ordinal()];
                if (i == 1) {
                    return ToStringSerializer.instance;
                }
                if (i == 2 || i == 3) {
                    return null;
                }
            }
            return NumberSerializer.instance;
        }
        if (Enum.class.isAssignableFrom(rawClass)) {
            return buildEnumSerializer(serializerProvider.getConfig(), javaType, beanDescription);
        }
        return null;
    }

    /* JADX INFO: renamed from: com.fasterxml.jackson.databind.ser.BasicSerializerFactory$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$fasterxml$jackson$annotation$JsonFormat$Shape;

        static {
            int[] iArr = new int[JsonFormat.Shape.values().length];
            $SwitchMap$com$fasterxml$jackson$annotation$JsonFormat$Shape = iArr;
            try {
                iArr[JsonFormat.Shape.STRING.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$annotation$JsonFormat$Shape[JsonFormat.Shape.OBJECT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$annotation$JsonFormat$Shape[JsonFormat.Shape.ARRAY.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    protected JsonSerializer<?> findOptionalStdSerializer(SerializerProvider serializerProvider, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        return OptionalHandlerFactory.instance.findSerializer(serializerProvider.getConfig(), javaType, beanDescription);
    }

    protected final JsonSerializer<?> findSerializerByAddonType(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        Class<?> rawClass = javaType.getRawClass();
        if (Iterator.class.isAssignableFrom(rawClass)) {
            return buildIteratorSerializer(serializationConfig, javaType, beanDescription, z);
        }
        if (Iterable.class.isAssignableFrom(rawClass)) {
            return buildIterableSerializer(serializationConfig, javaType, beanDescription, z);
        }
        if (CharSequence.class.isAssignableFrom(rawClass)) {
            return ToStringSerializer.instance;
        }
        return null;
    }

    protected JsonSerializer<Object> findSerializerFromAnnotation(SerializerProvider serializerProvider, Annotated annotated) throws JsonMappingException {
        Object objFindSerializer = serializerProvider.getAnnotationIntrospector().findSerializer(annotated);
        if (objFindSerializer == null) {
            return null;
        }
        return findConvertingSerializer(serializerProvider, annotated, serializerProvider.serializerInstance(annotated, objFindSerializer));
    }

    protected JsonSerializer<?> findConvertingSerializer(SerializerProvider serializerProvider, Annotated annotated, JsonSerializer<?> jsonSerializer) throws JsonMappingException {
        Converter<Object, Object> converterFindConverter = findConverter(serializerProvider, annotated);
        return converterFindConverter == null ? jsonSerializer : new StdDelegatingSerializer(converterFindConverter, converterFindConverter.getOutputType(serializerProvider.getTypeFactory()), jsonSerializer);
    }

    protected Converter<Object, Object> findConverter(SerializerProvider serializerProvider, Annotated annotated) throws JsonMappingException {
        Object objFindSerializationConverter = serializerProvider.getAnnotationIntrospector().findSerializationConverter(annotated);
        if (objFindSerializationConverter == null) {
            return null;
        }
        return serializerProvider.converterInstance(annotated, objFindSerializationConverter);
    }

    protected JsonSerializer<?> buildContainerSerializer(SerializerProvider serializerProvider, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        SerializationConfig config = serializerProvider.getConfig();
        if (!z && javaType.useStaticType() && (!javaType.isContainerType() || javaType.getContentType().getRawClass() != Object.class)) {
            z = true;
        }
        TypeSerializer typeSerializerCreateTypeSerializer = createTypeSerializer(config, javaType.getContentType());
        if (typeSerializerCreateTypeSerializer != null) {
            z = false;
        }
        boolean z2 = z;
        JsonSerializer<Object> jsonSerializer_findContentSerializer = _findContentSerializer(serializerProvider, beanDescription.getClassInfo());
        if (javaType.isMapLikeType()) {
            MapLikeType mapLikeType = (MapLikeType) javaType;
            JsonSerializer<Object> jsonSerializer_findKeySerializer = _findKeySerializer(serializerProvider, beanDescription.getClassInfo());
            if (mapLikeType.isTrueMapType()) {
                return buildMapSerializer(config, (MapType) mapLikeType, beanDescription, z2, jsonSerializer_findKeySerializer, typeSerializerCreateTypeSerializer, jsonSerializer_findContentSerializer);
            }
            Iterator<Serializers> it = customSerializers().iterator();
            while (it.hasNext()) {
                JsonSerializer<?> jsonSerializerFindMapLikeSerializer = it.next().findMapLikeSerializer(config, mapLikeType, beanDescription, jsonSerializer_findKeySerializer, typeSerializerCreateTypeSerializer, jsonSerializer_findContentSerializer);
                if (jsonSerializerFindMapLikeSerializer != null) {
                    if (this._factoryConfig.hasSerializerModifiers()) {
                        Iterator<BeanSerializerModifier> it2 = this._factoryConfig.serializerModifiers().iterator();
                        while (it2.hasNext()) {
                            jsonSerializerFindMapLikeSerializer = it2.next().modifyMapLikeSerializer(config, mapLikeType, beanDescription, jsonSerializerFindMapLikeSerializer);
                        }
                    }
                    return jsonSerializerFindMapLikeSerializer;
                }
            }
            return null;
        }
        if (javaType.isCollectionLikeType()) {
            CollectionLikeType collectionLikeType = (CollectionLikeType) javaType;
            if (collectionLikeType.isTrueCollectionType()) {
                return buildCollectionSerializer(config, (CollectionType) collectionLikeType, beanDescription, z2, typeSerializerCreateTypeSerializer, jsonSerializer_findContentSerializer);
            }
            Iterator<Serializers> it3 = customSerializers().iterator();
            while (it3.hasNext()) {
                TypeSerializer typeSerializer = typeSerializerCreateTypeSerializer;
                JsonSerializer<?> jsonSerializerFindCollectionLikeSerializer = it3.next().findCollectionLikeSerializer(config, collectionLikeType, beanDescription, typeSerializer, jsonSerializer_findContentSerializer);
                typeSerializerCreateTypeSerializer = typeSerializer;
                if (jsonSerializerFindCollectionLikeSerializer != null) {
                    if (this._factoryConfig.hasSerializerModifiers()) {
                        Iterator<BeanSerializerModifier> it4 = this._factoryConfig.serializerModifiers().iterator();
                        while (it4.hasNext()) {
                            jsonSerializerFindCollectionLikeSerializer = it4.next().modifyCollectionLikeSerializer(config, collectionLikeType, beanDescription, jsonSerializerFindCollectionLikeSerializer);
                        }
                    }
                    return jsonSerializerFindCollectionLikeSerializer;
                }
            }
            return null;
        }
        if (javaType.isArrayType()) {
            return buildArraySerializer(config, (ArrayType) javaType, beanDescription, z2, typeSerializerCreateTypeSerializer, jsonSerializer_findContentSerializer);
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:40:0x0094  */
    /* JADX WARN: Type inference failed for: r11v1 */
    /* JADX WARN: Type inference failed for: r11v11 */
    /* JADX WARN: Type inference failed for: r11v12 */
    /* JADX WARN: Type inference failed for: r11v4, types: [com.fasterxml.jackson.databind.ser.BeanSerializerModifier] */
    /* JADX WARN: Type inference failed for: r3v2, types: [com.fasterxml.jackson.databind.ser.Serializers] */
    /* JADX WARN: Type inference failed for: r5v0 */
    /* JADX WARN: Type inference failed for: r5v1, types: [com.fasterxml.jackson.databind.type.CollectionType] */
    /* JADX WARN: Type inference failed for: r5v2, types: [com.fasterxml.jackson.databind.type.CollectionType] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected com.fasterxml.jackson.databind.JsonSerializer<?> buildCollectionSerializer(com.fasterxml.jackson.databind.SerializationConfig r10, com.fasterxml.jackson.databind.type.CollectionType r11, com.fasterxml.jackson.databind.BeanDescription r12, boolean r13, com.fasterxml.jackson.databind.jsontype.TypeSerializer r14, com.fasterxml.jackson.databind.JsonSerializer<java.lang.Object> r15) throws com.fasterxml.jackson.databind.JsonMappingException {
        /*
            r9 = this;
            java.lang.Iterable r0 = r9.customSerializers()
            java.util.Iterator r0 = r0.iterator()
            r1 = 0
            r2 = r1
        La:
            boolean r3 = r0.hasNext()
            if (r3 == 0) goto L29
            java.lang.Object r2 = r0.next()
            r3 = r2
            com.fasterxml.jackson.databind.ser.Serializers r3 = (com.fasterxml.jackson.databind.ser.Serializers) r3
            r4 = r10
            r5 = r11
            r6 = r12
            r7 = r14
            r8 = r15
            com.fasterxml.jackson.databind.JsonSerializer r2 = r3.findCollectionSerializer(r4, r5, r6, r7, r8)
            if (r2 == 0) goto L23
            goto L2e
        L23:
            r10 = r4
            r11 = r5
            r12 = r6
            r14 = r7
            r15 = r8
            goto La
        L29:
            r4 = r10
            r5 = r11
            r6 = r12
            r7 = r14
            r8 = r15
        L2e:
            if (r2 != 0) goto L9c
            com.fasterxml.jackson.annotation.JsonFormat$Value r10 = r6.findExpectedFormat(r1)
            if (r10 == 0) goto L3f
            com.fasterxml.jackson.annotation.JsonFormat$Shape r10 = r10.getShape()
            com.fasterxml.jackson.annotation.JsonFormat$Shape r11 = com.fasterxml.jackson.annotation.JsonFormat.Shape.OBJECT
            if (r10 != r11) goto L3f
            return r1
        L3f:
            java.lang.Class r10 = r5.getRawClass()
            java.lang.Class<java.util.EnumSet> r11 = java.util.EnumSet.class
            boolean r11 = r11.isAssignableFrom(r10)
            if (r11 == 0) goto L5c
            com.fasterxml.jackson.databind.JavaType r10 = r5.getContentType()
            boolean r11 = r10.isEnumType()
            if (r11 != 0) goto L56
            goto L57
        L56:
            r1 = r10
        L57:
            com.fasterxml.jackson.databind.JsonSerializer r2 = r9.buildEnumSetSerializer(r1)
            goto L9c
        L5c:
            com.fasterxml.jackson.databind.JavaType r11 = r5.getContentType()
            java.lang.Class r11 = r11.getRawClass()
            boolean r10 = r9.isIndexedList(r10)
            if (r10 == 0) goto L83
            java.lang.Class<java.lang.String> r10 = java.lang.String.class
            if (r11 != r10) goto L79
            if (r8 == 0) goto L76
            boolean r10 = com.fasterxml.jackson.databind.util.ClassUtil.isJacksonStdImpl(r8)
            if (r10 == 0) goto L92
        L76:
            com.fasterxml.jackson.databind.ser.impl.IndexedStringListSerializer r10 = com.fasterxml.jackson.databind.ser.impl.IndexedStringListSerializer.instance
            goto L81
        L79:
            com.fasterxml.jackson.databind.JavaType r10 = r5.getContentType()
            com.fasterxml.jackson.databind.ser.ContainerSerializer r10 = r9.buildIndexedListSerializer(r10, r13, r7, r8)
        L81:
            r2 = r10
            goto L92
        L83:
            java.lang.Class<java.lang.String> r10 = java.lang.String.class
            if (r11 != r10) goto L92
            if (r8 == 0) goto L8f
            boolean r10 = com.fasterxml.jackson.databind.util.ClassUtil.isJacksonStdImpl(r8)
            if (r10 == 0) goto L92
        L8f:
            com.fasterxml.jackson.databind.ser.impl.StringCollectionSerializer r10 = com.fasterxml.jackson.databind.ser.impl.StringCollectionSerializer.instance
            goto L81
        L92:
            if (r2 != 0) goto L9c
            com.fasterxml.jackson.databind.JavaType r10 = r5.getContentType()
            com.fasterxml.jackson.databind.ser.ContainerSerializer r2 = r9.buildCollectionSerializer(r10, r13, r7, r8)
        L9c:
            com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig r10 = r9._factoryConfig
            boolean r10 = r10.hasSerializerModifiers()
            if (r10 == 0) goto Lbf
            com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig r10 = r9._factoryConfig
            java.lang.Iterable r10 = r10.serializerModifiers()
            java.util.Iterator r10 = r10.iterator()
        Lae:
            boolean r11 = r10.hasNext()
            if (r11 == 0) goto Lbf
            java.lang.Object r11 = r10.next()
            com.fasterxml.jackson.databind.ser.BeanSerializerModifier r11 = (com.fasterxml.jackson.databind.ser.BeanSerializerModifier) r11
            com.fasterxml.jackson.databind.JsonSerializer r2 = r11.modifyCollectionSerializer(r4, r5, r6, r2)
            goto Lae
        Lbf:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.ser.BasicSerializerFactory.buildCollectionSerializer(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.type.CollectionType, com.fasterxml.jackson.databind.BeanDescription, boolean, com.fasterxml.jackson.databind.jsontype.TypeSerializer, com.fasterxml.jackson.databind.JsonSerializer):com.fasterxml.jackson.databind.JsonSerializer");
    }

    protected boolean isIndexedList(Class<?> cls) {
        return RandomAccess.class.isAssignableFrom(cls);
    }

    public ContainerSerializer<?> buildIndexedListSerializer(JavaType javaType, boolean z, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) {
        return new IndexedListSerializer(javaType, z, typeSerializer, null, jsonSerializer);
    }

    public ContainerSerializer<?> buildCollectionSerializer(JavaType javaType, boolean z, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) {
        return new CollectionSerializer(javaType, z, typeSerializer, null, jsonSerializer);
    }

    public JsonSerializer<?> buildEnumSetSerializer(JavaType javaType) {
        return new EnumSetSerializer(javaType, null);
    }

    protected JsonSerializer<?> buildMapSerializer(SerializationConfig serializationConfig, MapType mapType, BeanDescription beanDescription, boolean z, JsonSerializer<Object> jsonSerializer, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer2) throws JsonMappingException {
        SerializationConfig serializationConfig2;
        BeanDescription beanDescription2;
        Iterator<Serializers> it = customSerializers().iterator();
        JsonSerializer<?> jsonSerializerModifyMapSerializer = null;
        while (true) {
            if (!it.hasNext()) {
                serializationConfig2 = serializationConfig;
                beanDescription2 = beanDescription;
                break;
            }
            serializationConfig2 = serializationConfig;
            beanDescription2 = beanDescription;
            jsonSerializerModifyMapSerializer = it.next().findMapSerializer(serializationConfig2, mapType, beanDescription2, jsonSerializer, typeSerializer, jsonSerializer2);
            if (jsonSerializerModifyMapSerializer != null) {
                break;
            }
        }
        if (jsonSerializerModifyMapSerializer == null) {
            jsonSerializerModifyMapSerializer = MapSerializer.construct(serializationConfig2.getAnnotationIntrospector().findPropertiesToIgnore(beanDescription2.getClassInfo()), mapType, z, typeSerializer, jsonSerializer, jsonSerializer2, findFilterId(serializationConfig2, beanDescription2));
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it2 = this._factoryConfig.serializerModifiers().iterator();
            while (it2.hasNext()) {
                jsonSerializerModifyMapSerializer = it2.next().modifyMapSerializer(serializationConfig2, mapType, beanDescription2, jsonSerializerModifyMapSerializer);
            }
        }
        return jsonSerializerModifyMapSerializer;
    }

    protected JsonSerializer<?> buildArraySerializer(SerializationConfig serializationConfig, ArrayType arrayType, BeanDescription beanDescription, boolean z, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) throws JsonMappingException {
        SerializationConfig serializationConfig2;
        ArrayType arrayType2;
        BeanDescription beanDescription2;
        TypeSerializer typeSerializer2;
        JsonSerializer<Object> jsonSerializer2;
        Iterator<Serializers> it = customSerializers().iterator();
        JsonSerializer<?> jsonSerializerModifyArraySerializer = null;
        while (true) {
            if (!it.hasNext()) {
                serializationConfig2 = serializationConfig;
                arrayType2 = arrayType;
                beanDescription2 = beanDescription;
                typeSerializer2 = typeSerializer;
                jsonSerializer2 = jsonSerializer;
                break;
            }
            serializationConfig2 = serializationConfig;
            arrayType2 = arrayType;
            beanDescription2 = beanDescription;
            typeSerializer2 = typeSerializer;
            jsonSerializer2 = jsonSerializer;
            jsonSerializerModifyArraySerializer = it.next().findArraySerializer(serializationConfig2, arrayType2, beanDescription2, typeSerializer2, jsonSerializer2);
            if (jsonSerializerModifyArraySerializer != null) {
                break;
            }
            serializationConfig = serializationConfig2;
            arrayType = arrayType2;
            beanDescription = beanDescription2;
            typeSerializer = typeSerializer2;
            jsonSerializer = jsonSerializer2;
        }
        if (jsonSerializerModifyArraySerializer == null) {
            Class<?> rawClass = arrayType2.getRawClass();
            if (jsonSerializer2 == null || ClassUtil.isJacksonStdImpl(jsonSerializer2)) {
                if (String[].class == rawClass) {
                    jsonSerializerModifyArraySerializer = StringArraySerializer.instance;
                } else {
                    jsonSerializerModifyArraySerializer = StdArraySerializers.findStandardImpl(rawClass);
                }
            }
            if (jsonSerializerModifyArraySerializer == null) {
                jsonSerializerModifyArraySerializer = new ObjectArraySerializer(arrayType2.getContentType(), z, typeSerializer2, jsonSerializer2);
            }
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it2 = this._factoryConfig.serializerModifiers().iterator();
            while (it2.hasNext()) {
                jsonSerializerModifyArraySerializer = it2.next().modifyArraySerializer(serializationConfig2, arrayType2, beanDescription2, jsonSerializerModifyArraySerializer);
            }
        }
        return jsonSerializerModifyArraySerializer;
    }

    protected JsonSerializer<?> buildIteratorSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        JavaType javaTypeContainedType = javaType.containedType(0);
        if (javaTypeContainedType == null) {
            javaTypeContainedType = TypeFactory.unknownType();
        }
        return new IteratorSerializer(javaTypeContainedType, z, createTypeSerializer(serializationConfig, javaTypeContainedType), (BeanProperty) null);
    }

    protected JsonSerializer<?> buildIterableSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        JavaType javaTypeContainedType = javaType.containedType(0);
        if (javaTypeContainedType == null) {
            javaTypeContainedType = TypeFactory.unknownType();
        }
        return new IterableSerializer(javaTypeContainedType, z, createTypeSerializer(serializationConfig, javaTypeContainedType), (BeanProperty) null);
    }

    protected JsonSerializer<?> buildEnumSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription) throws JsonMappingException {
        JsonFormat.Value valueFindExpectedFormat = beanDescription.findExpectedFormat(null);
        if (valueFindExpectedFormat != null && valueFindExpectedFormat.getShape() == JsonFormat.Shape.OBJECT) {
            ((BasicBeanDescription) beanDescription).removeProperty("declaringClass");
            return null;
        }
        JsonSerializer<?> jsonSerializerConstruct = EnumSerializer.construct(javaType.getRawClass(), serializationConfig, beanDescription, valueFindExpectedFormat);
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it = this._factoryConfig.serializerModifiers().iterator();
            while (it.hasNext()) {
                jsonSerializerConstruct = it.next().modifyEnumSerializer(serializationConfig, javaType, beanDescription, jsonSerializerConstruct);
            }
        }
        return jsonSerializerConstruct;
    }

    protected <T extends JavaType> T modifyTypeByAnnotation(SerializationConfig serializationConfig, Annotated annotated, T t) {
        Class<?> clsFindSerializationType = serializationConfig.getAnnotationIntrospector().findSerializationType(annotated);
        if (clsFindSerializationType != null) {
            try {
                t = (T) t.widenBy(clsFindSerializationType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Failed to widen type " + t + " with concrete-type annotation (value " + clsFindSerializationType.getName() + "), method '" + annotated.getName() + "': " + e.getMessage());
            }
        }
        return (T) modifySecondaryTypesByAnnotation(serializationConfig, annotated, t);
    }

    protected static <T extends JavaType> T modifySecondaryTypesByAnnotation(SerializationConfig serializationConfig, Annotated annotated, T t) {
        AnnotationIntrospector annotationIntrospector = serializationConfig.getAnnotationIntrospector();
        if (t.isContainerType()) {
            Class<?> clsFindSerializationKeyType = annotationIntrospector.findSerializationKeyType(annotated, t.getKeyType());
            if (clsFindSerializationKeyType != null) {
                if (!(t instanceof MapType)) {
                    throw new IllegalArgumentException("Illegal key-type annotation: type " + t + " is not a Map type");
                }
                try {
                    t = (T) ((MapType) t).widenKey(clsFindSerializationKeyType);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Failed to narrow key type " + t + " with key-type annotation (" + clsFindSerializationKeyType.getName() + "): " + e.getMessage());
                }
            }
            Class<?> clsFindSerializationContentType = annotationIntrospector.findSerializationContentType(annotated, t.getContentType());
            if (clsFindSerializationContentType != null) {
                try {
                    return (T) t.widenContentsBy(clsFindSerializationContentType);
                } catch (IllegalArgumentException e2) {
                    throw new IllegalArgumentException("Failed to narrow content type " + t + " with content-type annotation (" + clsFindSerializationContentType.getName() + "): " + e2.getMessage());
                }
            }
        }
        return t;
    }

    protected JsonSerializer<Object> _findKeySerializer(SerializerProvider serializerProvider, Annotated annotated) throws JsonMappingException {
        Object objFindKeySerializer = serializerProvider.getAnnotationIntrospector().findKeySerializer(annotated);
        if (objFindKeySerializer != null) {
            return serializerProvider.serializerInstance(annotated, objFindKeySerializer);
        }
        return null;
    }

    protected JsonSerializer<Object> _findContentSerializer(SerializerProvider serializerProvider, Annotated annotated) throws JsonMappingException {
        Object objFindContentSerializer = serializerProvider.getAnnotationIntrospector().findContentSerializer(annotated);
        if (objFindContentSerializer != null) {
            return serializerProvider.serializerInstance(annotated, objFindContentSerializer);
        }
        return null;
    }

    protected Object findFilterId(SerializationConfig serializationConfig, BeanDescription beanDescription) {
        return serializationConfig.getAnnotationIntrospector().findFilterId((Annotated) beanDescription.getClassInfo());
    }

    protected boolean usesStaticTyping(SerializationConfig serializationConfig, BeanDescription beanDescription, TypeSerializer typeSerializer) {
        if (typeSerializer != null) {
            return false;
        }
        JsonSerialize.Typing typingFindSerializationTyping = serializationConfig.getAnnotationIntrospector().findSerializationTyping(beanDescription.getClassInfo());
        if (typingFindSerializationTyping == null || typingFindSerializationTyping == JsonSerialize.Typing.DEFAULT_TYPING) {
            return serializationConfig.isEnabled(MapperFeature.USE_STATIC_TYPING);
        }
        return typingFindSerializationTyping == JsonSerialize.Typing.STATIC;
    }

    protected Class<?> _verifyAsClass(Object obj, String str, Class<?> cls) {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector." + str + "() returned value of type " + obj.getClass().getName() + ": expected type JsonSerializer or Class<JsonSerializer> instead");
        }
        Class<?> cls2 = (Class) obj;
        if (cls2 == cls || ClassUtil.isBogusClass(cls2)) {
            return null;
        }
        return cls2;
    }
}
