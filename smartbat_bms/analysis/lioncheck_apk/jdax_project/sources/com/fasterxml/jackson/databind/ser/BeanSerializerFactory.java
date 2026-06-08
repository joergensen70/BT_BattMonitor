package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.FilteredBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BeanSerializerFactory extends BasicSerializerFactory implements Serializable {
    public static final BeanSerializerFactory instance = new BeanSerializerFactory(null);
    private static final long serialVersionUID = 1;

    protected BeanSerializerFactory(SerializerFactoryConfig serializerFactoryConfig) {
        super(serializerFactoryConfig);
    }

    @Override // com.fasterxml.jackson.databind.ser.BasicSerializerFactory
    public SerializerFactory withConfig(SerializerFactoryConfig serializerFactoryConfig) {
        if (this._factoryConfig == serializerFactoryConfig) {
            return this;
        }
        if (getClass() != BeanSerializerFactory.class) {
            throw new IllegalStateException("Subtype of BeanSerializerFactory (" + getClass().getName() + ") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with additional serializer definitions");
        }
        return new BeanSerializerFactory(serializerFactoryConfig);
    }

    @Override // com.fasterxml.jackson.databind.ser.BasicSerializerFactory
    protected Iterable<Serializers> customSerializers() {
        return this._factoryConfig.serializers();
    }

    @Override // com.fasterxml.jackson.databind.ser.BasicSerializerFactory, com.fasterxml.jackson.databind.ser.SerializerFactory
    public JsonSerializer<Object> createSerializer(SerializerProvider serializerProvider, JavaType javaType) throws JsonMappingException {
        boolean z;
        SerializationConfig config = serializerProvider.getConfig();
        BeanDescription beanDescriptionIntrospect = config.introspect(javaType);
        JsonSerializer<?> jsonSerializerFindSerializerFromAnnotation = findSerializerFromAnnotation(serializerProvider, beanDescriptionIntrospect.getClassInfo());
        if (jsonSerializerFindSerializerFromAnnotation != null) {
            return jsonSerializerFindSerializerFromAnnotation;
        }
        JavaType javaTypeModifyTypeByAnnotation = modifyTypeByAnnotation(config, beanDescriptionIntrospect.getClassInfo(), javaType);
        if (javaTypeModifyTypeByAnnotation == javaType) {
            z = false;
        } else {
            if (!javaTypeModifyTypeByAnnotation.hasRawClass(javaType.getRawClass())) {
                beanDescriptionIntrospect = config.introspect(javaTypeModifyTypeByAnnotation);
            }
            z = true;
        }
        Converter<Object, Object> converterFindSerializationConverter = beanDescriptionIntrospect.findSerializationConverter();
        if (converterFindSerializationConverter == null) {
            return _createSerializer2(serializerProvider, javaTypeModifyTypeByAnnotation, beanDescriptionIntrospect, z);
        }
        JavaType outputType = converterFindSerializationConverter.getOutputType(serializerProvider.getTypeFactory());
        if (!outputType.hasRawClass(javaTypeModifyTypeByAnnotation.getRawClass())) {
            beanDescriptionIntrospect = config.introspect(outputType);
            jsonSerializerFindSerializerFromAnnotation = findSerializerFromAnnotation(serializerProvider, beanDescriptionIntrospect.getClassInfo());
        }
        if (jsonSerializerFindSerializerFromAnnotation == null) {
            jsonSerializerFindSerializerFromAnnotation = _createSerializer2(serializerProvider, outputType, beanDescriptionIntrospect, true);
        }
        return new StdDelegatingSerializer(converterFindSerializationConverter, outputType, jsonSerializerFindSerializerFromAnnotation);
    }

    protected JsonSerializer<?> _createSerializer2(SerializerProvider serializerProvider, JavaType javaType, BeanDescription beanDescription, boolean z) throws JsonMappingException {
        JsonSerializer<?> jsonSerializerFindSerializerByAnnotations = findSerializerByAnnotations(serializerProvider, javaType, beanDescription);
        if (jsonSerializerFindSerializerByAnnotations != null) {
            return jsonSerializerFindSerializerByAnnotations;
        }
        SerializationConfig config = serializerProvider.getConfig();
        if (javaType.isContainerType()) {
            if (!z) {
                z = usesStaticTyping(config, beanDescription, null);
            }
            jsonSerializerFindSerializerByAnnotations = buildContainerSerializer(serializerProvider, javaType, beanDescription, z);
            if (jsonSerializerFindSerializerByAnnotations != null) {
                return jsonSerializerFindSerializerByAnnotations;
            }
        } else {
            Iterator<Serializers> it = customSerializers().iterator();
            while (it.hasNext() && (jsonSerializerFindSerializerByAnnotations = it.next().findSerializer(config, javaType, beanDescription)) == null) {
            }
        }
        if (jsonSerializerFindSerializerByAnnotations == null && (jsonSerializerFindSerializerByAnnotations = findSerializerByLookup(javaType, config, beanDescription, z)) == null && (jsonSerializerFindSerializerByAnnotations = findSerializerByPrimaryType(serializerProvider, javaType, beanDescription, z)) == null && (jsonSerializerFindSerializerByAnnotations = findBeanSerializer(serializerProvider, javaType, beanDescription)) == null) {
            jsonSerializerFindSerializerByAnnotations = findSerializerByAddonType(config, javaType, beanDescription, z);
        }
        if (jsonSerializerFindSerializerByAnnotations != null && this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it2 = this._factoryConfig.serializerModifiers().iterator();
            while (it2.hasNext()) {
                jsonSerializerFindSerializerByAnnotations = it2.next().modifySerializer(config, beanDescription, jsonSerializerFindSerializerByAnnotations);
            }
        }
        return jsonSerializerFindSerializerByAnnotations;
    }

    public JsonSerializer<Object> findBeanSerializer(SerializerProvider serializerProvider, JavaType javaType, BeanDescription beanDescription) throws JsonMappingException {
        if (isPotentialBeanType(javaType.getRawClass()) || javaType.isEnumType()) {
            return constructBeanSerializer(serializerProvider, beanDescription);
        }
        return null;
    }

    public TypeSerializer findPropertyTypeSerializer(JavaType javaType, SerializationConfig serializationConfig, AnnotatedMember annotatedMember) throws JsonMappingException {
        AnnotationIntrospector annotationIntrospector = serializationConfig.getAnnotationIntrospector();
        TypeResolverBuilder<?> typeResolverBuilderFindPropertyTypeResolver = annotationIntrospector.findPropertyTypeResolver(serializationConfig, annotatedMember, javaType);
        if (typeResolverBuilderFindPropertyTypeResolver == null) {
            return createTypeSerializer(serializationConfig, javaType);
        }
        return typeResolverBuilderFindPropertyTypeResolver.buildTypeSerializer(serializationConfig, javaType, serializationConfig.getSubtypeResolver().collectAndResolveSubtypes(annotatedMember, serializationConfig, annotationIntrospector, javaType));
    }

    public TypeSerializer findPropertyContentTypeSerializer(JavaType javaType, SerializationConfig serializationConfig, AnnotatedMember annotatedMember) throws JsonMappingException {
        JavaType contentType = javaType.getContentType();
        AnnotationIntrospector annotationIntrospector = serializationConfig.getAnnotationIntrospector();
        TypeResolverBuilder<?> typeResolverBuilderFindPropertyContentTypeResolver = annotationIntrospector.findPropertyContentTypeResolver(serializationConfig, annotatedMember, javaType);
        if (typeResolverBuilderFindPropertyContentTypeResolver == null) {
            return createTypeSerializer(serializationConfig, contentType);
        }
        return typeResolverBuilderFindPropertyContentTypeResolver.buildTypeSerializer(serializationConfig, contentType, serializationConfig.getSubtypeResolver().collectAndResolveSubtypes(annotatedMember, serializationConfig, annotationIntrospector, contentType));
    }

    protected JsonSerializer<Object> constructBeanSerializer(SerializerProvider serializerProvider, BeanDescription beanDescription) throws JsonMappingException {
        if (beanDescription.getBeanClass() == Object.class) {
            return serializerProvider.getUnknownTypeSerializer(Object.class);
        }
        SerializationConfig config = serializerProvider.getConfig();
        BeanSerializerBuilder beanSerializerBuilderConstructBeanSerializerBuilder = constructBeanSerializerBuilder(beanDescription);
        beanSerializerBuilderConstructBeanSerializerBuilder.setConfig(config);
        List<BeanPropertyWriter> listFindBeanProperties = findBeanProperties(serializerProvider, beanDescription, beanSerializerBuilderConstructBeanSerializerBuilder);
        if (listFindBeanProperties == null) {
            listFindBeanProperties = new ArrayList<>();
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it = this._factoryConfig.serializerModifiers().iterator();
            while (it.hasNext()) {
                listFindBeanProperties = it.next().changeProperties(config, beanDescription, listFindBeanProperties);
            }
        }
        List<BeanPropertyWriter> listFilterBeanProperties = filterBeanProperties(config, beanDescription, listFindBeanProperties);
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it2 = this._factoryConfig.serializerModifiers().iterator();
            while (it2.hasNext()) {
                listFilterBeanProperties = it2.next().orderProperties(config, beanDescription, listFilterBeanProperties);
            }
        }
        beanSerializerBuilderConstructBeanSerializerBuilder.setObjectIdWriter(constructObjectIdHandler(serializerProvider, beanDescription, listFilterBeanProperties));
        beanSerializerBuilderConstructBeanSerializerBuilder.setProperties(listFilterBeanProperties);
        beanSerializerBuilderConstructBeanSerializerBuilder.setFilterId(findFilterId(config, beanDescription));
        AnnotatedMember annotatedMemberFindAnyGetter = beanDescription.findAnyGetter();
        if (annotatedMemberFindAnyGetter != null) {
            if (config.canOverrideAccessModifiers()) {
                annotatedMemberFindAnyGetter.fixAccess();
            }
            JavaType type = annotatedMemberFindAnyGetter.getType(beanDescription.bindingsForBeanType());
            boolean zIsEnabled = config.isEnabled(MapperFeature.USE_STATIC_TYPING);
            JavaType contentType = type.getContentType();
            beanSerializerBuilderConstructBeanSerializerBuilder.setAnyGetter(new AnyGetterWriter(new BeanProperty.Std(new PropertyName(annotatedMemberFindAnyGetter.getName()), contentType, (PropertyName) null, beanDescription.getClassAnnotations(), annotatedMemberFindAnyGetter, PropertyMetadata.STD_OPTIONAL), annotatedMemberFindAnyGetter, MapSerializer.construct(null, type, zIsEnabled, createTypeSerializer(config, contentType), null, null, null)));
        }
        processViews(config, beanSerializerBuilderConstructBeanSerializerBuilder);
        if (this._factoryConfig.hasSerializerModifiers()) {
            Iterator<BeanSerializerModifier> it3 = this._factoryConfig.serializerModifiers().iterator();
            while (it3.hasNext()) {
                beanSerializerBuilderConstructBeanSerializerBuilder = it3.next().updateBuilder(config, beanDescription, beanSerializerBuilderConstructBeanSerializerBuilder);
            }
        }
        JsonSerializer<?> jsonSerializerBuild = beanSerializerBuilderConstructBeanSerializerBuilder.build();
        return (jsonSerializerBuild == null && beanDescription.hasKnownClassAnnotations()) ? beanSerializerBuilderConstructBeanSerializerBuilder.createDummy() : jsonSerializerBuild;
    }

    protected ObjectIdWriter constructObjectIdHandler(SerializerProvider serializerProvider, BeanDescription beanDescription, List<BeanPropertyWriter> list) throws JsonMappingException {
        ObjectIdInfo objectIdInfo = beanDescription.getObjectIdInfo();
        if (objectIdInfo == null) {
            return null;
        }
        Class<? extends ObjectIdGenerator<?>> generatorType = objectIdInfo.getGeneratorType();
        if (generatorType == ObjectIdGenerators.PropertyGenerator.class) {
            String simpleName = objectIdInfo.getPropertyName().getSimpleName();
            int size = list.size();
            for (int i = 0; i != size; i++) {
                BeanPropertyWriter beanPropertyWriter = list.get(i);
                if (simpleName.equals(beanPropertyWriter.getName())) {
                    if (i > 0) {
                        list.remove(i);
                        list.add(0, beanPropertyWriter);
                    }
                    return ObjectIdWriter.construct(beanPropertyWriter.getType(), (PropertyName) null, new PropertyBasedObjectIdGenerator(objectIdInfo, beanPropertyWriter), objectIdInfo.getAlwaysAsId());
                }
            }
            throw new IllegalArgumentException("Invalid Object Id definition for " + beanDescription.getBeanClass().getName() + ": can not find property with name '" + simpleName + "'");
        }
        return ObjectIdWriter.construct(serializerProvider.getTypeFactory().findTypeParameters(serializerProvider.constructType(generatorType), ObjectIdGenerator.class)[0], objectIdInfo.getPropertyName(), serializerProvider.objectIdGeneratorInstance(beanDescription.getClassInfo(), objectIdInfo), objectIdInfo.getAlwaysAsId());
    }

    protected BeanPropertyWriter constructFilteredBeanWriter(BeanPropertyWriter beanPropertyWriter, Class<?>[] clsArr) {
        return FilteredBeanPropertyWriter.constructViewBased(beanPropertyWriter, clsArr);
    }

    protected PropertyBuilder constructPropertyBuilder(SerializationConfig serializationConfig, BeanDescription beanDescription) {
        return new PropertyBuilder(serializationConfig, beanDescription);
    }

    protected BeanSerializerBuilder constructBeanSerializerBuilder(BeanDescription beanDescription) {
        return new BeanSerializerBuilder(beanDescription);
    }

    protected boolean isPotentialBeanType(Class<?> cls) {
        return ClassUtil.canBeABeanType(cls) == null && !ClassUtil.isProxyType(cls);
    }

    protected List<BeanPropertyWriter> findBeanProperties(SerializerProvider serializerProvider, BeanDescription beanDescription, BeanSerializerBuilder beanSerializerBuilder) throws JsonMappingException {
        SerializerProvider serializerProvider2;
        List<BeanPropertyDefinition> listFindProperties = beanDescription.findProperties();
        SerializationConfig config = serializerProvider.getConfig();
        removeIgnorableTypes(config, beanDescription, listFindProperties);
        if (config.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS)) {
            removeSetterlessGetters(config, beanDescription, listFindProperties);
        }
        if (listFindProperties.isEmpty()) {
            return null;
        }
        boolean zUsesStaticTyping = usesStaticTyping(config, beanDescription, null);
        PropertyBuilder propertyBuilderConstructPropertyBuilder = constructPropertyBuilder(config, beanDescription);
        ArrayList arrayList = new ArrayList(listFindProperties.size());
        TypeBindings typeBindingsBindingsForBeanType = beanDescription.bindingsForBeanType();
        for (BeanPropertyDefinition beanPropertyDefinition : listFindProperties) {
            AnnotatedMember accessor = beanPropertyDefinition.getAccessor();
            if (!beanPropertyDefinition.isTypeId()) {
                AnnotationIntrospector.ReferenceProperty referencePropertyFindReferenceType = beanPropertyDefinition.findReferenceType();
                if (referencePropertyFindReferenceType == null || !referencePropertyFindReferenceType.isBackReference()) {
                    if (accessor instanceof AnnotatedMethod) {
                        serializerProvider2 = serializerProvider;
                        arrayList.add(_constructWriter(serializerProvider2, beanPropertyDefinition, typeBindingsBindingsForBeanType, propertyBuilderConstructPropertyBuilder, zUsesStaticTyping, (AnnotatedMethod) accessor));
                    } else {
                        serializerProvider2 = serializerProvider;
                        arrayList.add(_constructWriter(serializerProvider2, beanPropertyDefinition, typeBindingsBindingsForBeanType, propertyBuilderConstructPropertyBuilder, zUsesStaticTyping, (AnnotatedField) accessor));
                    }
                    serializerProvider = serializerProvider2;
                }
            } else if (accessor != null) {
                if (config.canOverrideAccessModifiers()) {
                    accessor.fixAccess();
                }
                beanSerializerBuilder.setTypeId(accessor);
            }
        }
        return arrayList;
    }

    protected List<BeanPropertyWriter> filterBeanProperties(SerializationConfig serializationConfig, BeanDescription beanDescription, List<BeanPropertyWriter> list) {
        String[] strArrFindPropertiesToIgnore = serializationConfig.getAnnotationIntrospector().findPropertiesToIgnore(beanDescription.getClassInfo());
        if (strArrFindPropertiesToIgnore != null && strArrFindPropertiesToIgnore.length > 0) {
            HashSet hashSetArrayToSet = ArrayBuilders.arrayToSet(strArrFindPropertiesToIgnore);
            Iterator<BeanPropertyWriter> it = list.iterator();
            while (it.hasNext()) {
                if (hashSetArrayToSet.contains(it.next().getName())) {
                    it.remove();
                }
            }
        }
        return list;
    }

    protected void processViews(SerializationConfig serializationConfig, BeanSerializerBuilder beanSerializerBuilder) {
        List<BeanPropertyWriter> properties = beanSerializerBuilder.getProperties();
        boolean zIsEnabled = serializationConfig.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION);
        int size = properties.size();
        BeanPropertyWriter[] beanPropertyWriterArr = new BeanPropertyWriter[size];
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            BeanPropertyWriter beanPropertyWriter = properties.get(i2);
            Class<?>[] views = beanPropertyWriter.getViews();
            if (views != null) {
                i++;
                beanPropertyWriterArr[i2] = constructFilteredBeanWriter(beanPropertyWriter, views);
            } else if (zIsEnabled) {
                beanPropertyWriterArr[i2] = beanPropertyWriter;
            }
        }
        if (zIsEnabled && i == 0) {
            return;
        }
        beanSerializerBuilder.setFilteredProperties(beanPropertyWriterArr);
    }

    protected void removeIgnorableTypes(SerializationConfig serializationConfig, BeanDescription beanDescription, List<BeanPropertyDefinition> list) {
        AnnotationIntrospector annotationIntrospector = serializationConfig.getAnnotationIntrospector();
        HashMap map = new HashMap();
        Iterator<BeanPropertyDefinition> it = list.iterator();
        while (it.hasNext()) {
            AnnotatedMember accessor = it.next().getAccessor();
            if (accessor == null) {
                it.remove();
            } else {
                Class<?> rawType = accessor.getRawType();
                Boolean boolIsIgnorableType = (Boolean) map.get(rawType);
                if (boolIsIgnorableType == null) {
                    boolIsIgnorableType = annotationIntrospector.isIgnorableType(serializationConfig.introspectClassAnnotations(rawType).getClassInfo());
                    if (boolIsIgnorableType == null) {
                        boolIsIgnorableType = Boolean.FALSE;
                    }
                    map.put(rawType, boolIsIgnorableType);
                }
                if (boolIsIgnorableType.booleanValue()) {
                    it.remove();
                }
            }
        }
    }

    protected void removeSetterlessGetters(SerializationConfig serializationConfig, BeanDescription beanDescription, List<BeanPropertyDefinition> list) {
        Iterator<BeanPropertyDefinition> it = list.iterator();
        while (it.hasNext()) {
            BeanPropertyDefinition next = it.next();
            if (!next.couldDeserialize() && !next.isExplicitlyIncluded()) {
                it.remove();
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected BeanPropertyWriter _constructWriter(SerializerProvider serializerProvider, BeanPropertyDefinition beanPropertyDefinition, TypeBindings typeBindings, PropertyBuilder propertyBuilder, boolean z, AnnotatedMember annotatedMember) throws JsonMappingException {
        PropertyName fullName = beanPropertyDefinition.getFullName();
        if (serializerProvider.canOverrideAccessModifiers()) {
            annotatedMember.fixAccess();
        }
        JavaType type = annotatedMember.getType(typeBindings);
        BeanProperty.Std std = new BeanProperty.Std(fullName, type, beanPropertyDefinition.getWrapperName(), propertyBuilder.getClassAnnotations(), annotatedMember, beanPropertyDefinition.getMetadata());
        JsonSerializer<Object> jsonSerializerFindSerializerFromAnnotation = findSerializerFromAnnotation(serializerProvider, annotatedMember);
        if (jsonSerializerFindSerializerFromAnnotation instanceof ResolvableSerializer) {
            ((ResolvableSerializer) jsonSerializerFindSerializerFromAnnotation).resolve(serializerProvider);
        }
        return propertyBuilder.buildWriter(serializerProvider, beanPropertyDefinition, type, serializerProvider.handlePrimaryContextualization(jsonSerializerFindSerializerFromAnnotation, std), findPropertyTypeSerializer(type, serializerProvider.getConfig(), annotatedMember), (ClassUtil.isCollectionMapOrArray(type.getRawClass()) || type.isCollectionLikeType() || type.isMapLikeType()) ? findPropertyContentTypeSerializer(type, serializerProvider.getConfig(), annotatedMember) : null, annotatedMember, z);
    }
}
