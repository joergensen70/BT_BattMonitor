package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class AnnotationIntrospectorPair extends AnnotationIntrospector implements Serializable {
    private static final long serialVersionUID = 1;
    protected final AnnotationIntrospector _primary;
    protected final AnnotationIntrospector _secondary;

    public AnnotationIntrospectorPair(AnnotationIntrospector annotationIntrospector, AnnotationIntrospector annotationIntrospector2) {
        this._primary = annotationIntrospector;
        this._secondary = annotationIntrospector2;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector, com.fasterxml.jackson.core.Versioned
    public Version version() {
        return this._primary.version();
    }

    public static AnnotationIntrospector create(AnnotationIntrospector annotationIntrospector, AnnotationIntrospector annotationIntrospector2) {
        return annotationIntrospector == null ? annotationIntrospector2 : annotationIntrospector2 == null ? annotationIntrospector : new AnnotationIntrospectorPair(annotationIntrospector, annotationIntrospector2);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Collection<AnnotationIntrospector> allIntrospectors() {
        return allIntrospectors(new ArrayList());
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Collection<AnnotationIntrospector> allIntrospectors(Collection<AnnotationIntrospector> collection) {
        this._primary.allIntrospectors(collection);
        this._secondary.allIntrospectors(collection);
        return collection;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public boolean isAnnotationBundle(Annotation annotation) {
        return this._primary.isAnnotationBundle(annotation) || this._secondary.isAnnotationBundle(annotation);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public PropertyName findRootName(AnnotatedClass annotatedClass) {
        PropertyName propertyNameFindRootName;
        PropertyName propertyNameFindRootName2 = this._primary.findRootName(annotatedClass);
        if (propertyNameFindRootName2 == null) {
            return this._secondary.findRootName(annotatedClass);
        }
        return (propertyNameFindRootName2.hasSimpleName() || (propertyNameFindRootName = this._secondary.findRootName(annotatedClass)) == null) ? propertyNameFindRootName2 : propertyNameFindRootName;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public String[] findPropertiesToIgnore(Annotated annotated) {
        String[] strArrFindPropertiesToIgnore = this._primary.findPropertiesToIgnore(annotated);
        return strArrFindPropertiesToIgnore == null ? this._secondary.findPropertiesToIgnore(annotated) : strArrFindPropertiesToIgnore;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Boolean findIgnoreUnknownProperties(AnnotatedClass annotatedClass) {
        Boolean boolFindIgnoreUnknownProperties = this._primary.findIgnoreUnknownProperties(annotatedClass);
        return boolFindIgnoreUnknownProperties == null ? this._secondary.findIgnoreUnknownProperties(annotatedClass) : boolFindIgnoreUnknownProperties;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Boolean isIgnorableType(AnnotatedClass annotatedClass) {
        Boolean boolIsIgnorableType = this._primary.isIgnorableType(annotatedClass);
        return boolIsIgnorableType == null ? this._secondary.isIgnorableType(annotatedClass) : boolIsIgnorableType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    @Deprecated
    public Object findFilterId(AnnotatedClass annotatedClass) {
        Object objFindFilterId = this._primary.findFilterId(annotatedClass);
        return objFindFilterId == null ? this._secondary.findFilterId(annotatedClass) : objFindFilterId;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findFilterId(Annotated annotated) {
        Object objFindFilterId = this._primary.findFilterId(annotated);
        return objFindFilterId == null ? this._secondary.findFilterId(annotated) : objFindFilterId;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findNamingStrategy(AnnotatedClass annotatedClass) {
        Object objFindNamingStrategy = this._primary.findNamingStrategy(annotatedClass);
        return objFindNamingStrategy == null ? this._secondary.findNamingStrategy(annotatedClass) : objFindNamingStrategy;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass annotatedClass, VisibilityChecker<?> visibilityChecker) {
        return this._primary.findAutoDetectVisibility(annotatedClass, this._secondary.findAutoDetectVisibility(annotatedClass, visibilityChecker));
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> mapperConfig, AnnotatedClass annotatedClass, JavaType javaType) {
        TypeResolverBuilder<?> typeResolverBuilderFindTypeResolver = this._primary.findTypeResolver(mapperConfig, annotatedClass, javaType);
        return typeResolverBuilderFindTypeResolver == null ? this._secondary.findTypeResolver(mapperConfig, annotatedClass, javaType) : typeResolverBuilderFindTypeResolver;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> mapperConfig, AnnotatedMember annotatedMember, JavaType javaType) {
        TypeResolverBuilder<?> typeResolverBuilderFindPropertyTypeResolver = this._primary.findPropertyTypeResolver(mapperConfig, annotatedMember, javaType);
        return typeResolverBuilderFindPropertyTypeResolver == null ? this._secondary.findPropertyTypeResolver(mapperConfig, annotatedMember, javaType) : typeResolverBuilderFindPropertyTypeResolver;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> mapperConfig, AnnotatedMember annotatedMember, JavaType javaType) {
        TypeResolverBuilder<?> typeResolverBuilderFindPropertyContentTypeResolver = this._primary.findPropertyContentTypeResolver(mapperConfig, annotatedMember, javaType);
        return typeResolverBuilderFindPropertyContentTypeResolver == null ? this._secondary.findPropertyContentTypeResolver(mapperConfig, annotatedMember, javaType) : typeResolverBuilderFindPropertyContentTypeResolver;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public List<NamedType> findSubtypes(Annotated annotated) {
        List<NamedType> listFindSubtypes = this._primary.findSubtypes(annotated);
        List<NamedType> listFindSubtypes2 = this._secondary.findSubtypes(annotated);
        if (listFindSubtypes == null || listFindSubtypes.isEmpty()) {
            return listFindSubtypes2;
        }
        if (listFindSubtypes2 == null || listFindSubtypes2.isEmpty()) {
            return listFindSubtypes;
        }
        ArrayList arrayList = new ArrayList(listFindSubtypes.size() + listFindSubtypes2.size());
        arrayList.addAll(listFindSubtypes);
        arrayList.addAll(listFindSubtypes2);
        return arrayList;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public String findTypeName(AnnotatedClass annotatedClass) {
        String strFindTypeName = this._primary.findTypeName(annotatedClass);
        return (strFindTypeName == null || strFindTypeName.length() == 0) ? this._secondary.findTypeName(annotatedClass) : strFindTypeName;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public AnnotationIntrospector.ReferenceProperty findReferenceType(AnnotatedMember annotatedMember) {
        AnnotationIntrospector.ReferenceProperty referencePropertyFindReferenceType = this._primary.findReferenceType(annotatedMember);
        return referencePropertyFindReferenceType == null ? this._secondary.findReferenceType(annotatedMember) : referencePropertyFindReferenceType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public NameTransformer findUnwrappingNameTransformer(AnnotatedMember annotatedMember) {
        NameTransformer nameTransformerFindUnwrappingNameTransformer = this._primary.findUnwrappingNameTransformer(annotatedMember);
        return nameTransformerFindUnwrappingNameTransformer == null ? this._secondary.findUnwrappingNameTransformer(annotatedMember) : nameTransformerFindUnwrappingNameTransformer;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findInjectableValueId(AnnotatedMember annotatedMember) {
        Object objFindInjectableValueId = this._primary.findInjectableValueId(annotatedMember);
        return objFindInjectableValueId == null ? this._secondary.findInjectableValueId(annotatedMember) : objFindInjectableValueId;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public boolean hasIgnoreMarker(AnnotatedMember annotatedMember) {
        return this._primary.hasIgnoreMarker(annotatedMember) || this._secondary.hasIgnoreMarker(annotatedMember);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Boolean hasRequiredMarker(AnnotatedMember annotatedMember) {
        Boolean boolHasRequiredMarker = this._primary.hasRequiredMarker(annotatedMember);
        return boolHasRequiredMarker == null ? this._secondary.hasRequiredMarker(annotatedMember) : boolHasRequiredMarker;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findSerializer(Annotated annotated) {
        Object objFindSerializer = this._primary.findSerializer(annotated);
        return _isExplicitClassOrOb(objFindSerializer, JsonSerializer.None.class) ? objFindSerializer : this._secondary.findSerializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findKeySerializer(Annotated annotated) {
        Object objFindKeySerializer = this._primary.findKeySerializer(annotated);
        return _isExplicitClassOrOb(objFindKeySerializer, JsonSerializer.None.class) ? objFindKeySerializer : this._secondary.findKeySerializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findContentSerializer(Annotated annotated) {
        Object objFindContentSerializer = this._primary.findContentSerializer(annotated);
        return _isExplicitClassOrOb(objFindContentSerializer, JsonSerializer.None.class) ? objFindContentSerializer : this._secondary.findContentSerializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findNullSerializer(Annotated annotated) {
        Object objFindNullSerializer = this._primary.findNullSerializer(annotated);
        return _isExplicitClassOrOb(objFindNullSerializer, JsonSerializer.None.class) ? objFindNullSerializer : this._secondary.findNullSerializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public JsonInclude.Include findSerializationInclusion(Annotated annotated, JsonInclude.Include include) {
        return this._primary.findSerializationInclusion(annotated, this._secondary.findSerializationInclusion(annotated, include));
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findSerializationType(Annotated annotated) {
        Class<?> clsFindSerializationType = this._primary.findSerializationType(annotated);
        return clsFindSerializationType == null ? this._secondary.findSerializationType(annotated) : clsFindSerializationType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findSerializationKeyType(Annotated annotated, JavaType javaType) {
        Class<?> clsFindSerializationKeyType = this._primary.findSerializationKeyType(annotated, javaType);
        return clsFindSerializationKeyType == null ? this._secondary.findSerializationKeyType(annotated, javaType) : clsFindSerializationKeyType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findSerializationContentType(Annotated annotated, JavaType javaType) {
        Class<?> clsFindSerializationContentType = this._primary.findSerializationContentType(annotated, javaType);
        return clsFindSerializationContentType == null ? this._secondary.findSerializationContentType(annotated, javaType) : clsFindSerializationContentType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public JsonSerialize.Typing findSerializationTyping(Annotated annotated) {
        JsonSerialize.Typing typingFindSerializationTyping = this._primary.findSerializationTyping(annotated);
        return typingFindSerializationTyping == null ? this._secondary.findSerializationTyping(annotated) : typingFindSerializationTyping;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findSerializationConverter(Annotated annotated) {
        Object objFindSerializationConverter = this._primary.findSerializationConverter(annotated);
        return objFindSerializationConverter == null ? this._secondary.findSerializationConverter(annotated) : objFindSerializationConverter;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findSerializationContentConverter(AnnotatedMember annotatedMember) {
        Object objFindSerializationContentConverter = this._primary.findSerializationContentConverter(annotatedMember);
        return objFindSerializationContentConverter == null ? this._secondary.findSerializationContentConverter(annotatedMember) : objFindSerializationContentConverter;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?>[] findViews(Annotated annotated) {
        Class<?>[] clsArrFindViews = this._primary.findViews(annotated);
        return clsArrFindViews == null ? this._secondary.findViews(annotated) : clsArrFindViews;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Boolean isTypeId(AnnotatedMember annotatedMember) {
        Boolean boolIsTypeId = this._primary.isTypeId(annotatedMember);
        return boolIsTypeId == null ? this._secondary.isTypeId(annotatedMember) : boolIsTypeId;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public ObjectIdInfo findObjectIdInfo(Annotated annotated) {
        ObjectIdInfo objectIdInfoFindObjectIdInfo = this._primary.findObjectIdInfo(annotated);
        return objectIdInfoFindObjectIdInfo == null ? this._secondary.findObjectIdInfo(annotated) : objectIdInfoFindObjectIdInfo;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public ObjectIdInfo findObjectReferenceInfo(Annotated annotated, ObjectIdInfo objectIdInfo) {
        return this._primary.findObjectReferenceInfo(annotated, this._secondary.findObjectReferenceInfo(annotated, objectIdInfo));
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public JsonFormat.Value findFormat(Annotated annotated) {
        JsonFormat.Value valueFindFormat = this._primary.findFormat(annotated);
        return valueFindFormat == null ? this._secondary.findFormat(annotated) : valueFindFormat;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public PropertyName findWrapperName(Annotated annotated) {
        PropertyName propertyNameFindWrapperName;
        PropertyName propertyNameFindWrapperName2 = this._primary.findWrapperName(annotated);
        if (propertyNameFindWrapperName2 == null) {
            return this._secondary.findWrapperName(annotated);
        }
        return (propertyNameFindWrapperName2 != PropertyName.USE_DEFAULT || (propertyNameFindWrapperName = this._secondary.findWrapperName(annotated)) == null) ? propertyNameFindWrapperName2 : propertyNameFindWrapperName;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public String findPropertyDescription(Annotated annotated) {
        String strFindPropertyDescription = this._primary.findPropertyDescription(annotated);
        return strFindPropertyDescription == null ? this._secondary.findPropertyDescription(annotated) : strFindPropertyDescription;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Integer findPropertyIndex(Annotated annotated) {
        Integer numFindPropertyIndex = this._primary.findPropertyIndex(annotated);
        return numFindPropertyIndex == null ? this._secondary.findPropertyIndex(annotated) : numFindPropertyIndex;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public String findImplicitPropertyName(AnnotatedMember annotatedMember) {
        String strFindImplicitPropertyName = this._primary.findImplicitPropertyName(annotatedMember);
        return strFindImplicitPropertyName == null ? this._secondary.findImplicitPropertyName(annotatedMember) : strFindImplicitPropertyName;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public String[] findSerializationPropertyOrder(AnnotatedClass annotatedClass) {
        String[] strArrFindSerializationPropertyOrder = this._primary.findSerializationPropertyOrder(annotatedClass);
        return strArrFindSerializationPropertyOrder == null ? this._secondary.findSerializationPropertyOrder(annotatedClass) : strArrFindSerializationPropertyOrder;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    @Deprecated
    public Boolean findSerializationSortAlphabetically(AnnotatedClass annotatedClass) {
        Boolean boolFindSerializationSortAlphabetically = this._primary.findSerializationSortAlphabetically(annotatedClass);
        return boolFindSerializationSortAlphabetically == null ? this._secondary.findSerializationSortAlphabetically(annotatedClass) : boolFindSerializationSortAlphabetically;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Boolean findSerializationSortAlphabetically(Annotated annotated) {
        Boolean boolFindSerializationSortAlphabetically = this._primary.findSerializationSortAlphabetically(annotated);
        return boolFindSerializationSortAlphabetically == null ? this._secondary.findSerializationSortAlphabetically(annotated) : boolFindSerializationSortAlphabetically;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public PropertyName findNameForSerialization(Annotated annotated) {
        PropertyName propertyNameFindNameForSerialization;
        PropertyName propertyNameFindNameForSerialization2 = this._primary.findNameForSerialization(annotated);
        if (propertyNameFindNameForSerialization2 == null) {
            return this._secondary.findNameForSerialization(annotated);
        }
        return (propertyNameFindNameForSerialization2 != PropertyName.USE_DEFAULT || (propertyNameFindNameForSerialization = this._secondary.findNameForSerialization(annotated)) == null) ? propertyNameFindNameForSerialization2 : propertyNameFindNameForSerialization;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public boolean hasAsValueAnnotation(AnnotatedMethod annotatedMethod) {
        return this._primary.hasAsValueAnnotation(annotatedMethod) || this._secondary.hasAsValueAnnotation(annotatedMethod);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public String findEnumValue(Enum<?> r2) {
        String strFindEnumValue = this._primary.findEnumValue(r2);
        return strFindEnumValue == null ? this._secondary.findEnumValue(r2) : strFindEnumValue;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findDeserializer(Annotated annotated) {
        Object objFindDeserializer = this._primary.findDeserializer(annotated);
        return _isExplicitClassOrOb(objFindDeserializer, JsonDeserializer.None.class) ? objFindDeserializer : this._secondary.findDeserializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findKeyDeserializer(Annotated annotated) {
        Object objFindKeyDeserializer = this._primary.findKeyDeserializer(annotated);
        return _isExplicitClassOrOb(objFindKeyDeserializer, KeyDeserializer.None.class) ? objFindKeyDeserializer : this._secondary.findKeyDeserializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findContentDeserializer(Annotated annotated) {
        Object objFindContentDeserializer = this._primary.findContentDeserializer(annotated);
        return _isExplicitClassOrOb(objFindContentDeserializer, JsonDeserializer.None.class) ? objFindContentDeserializer : this._secondary.findContentDeserializer(annotated);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findDeserializationType(Annotated annotated, JavaType javaType) {
        Class<?> clsFindDeserializationType = this._primary.findDeserializationType(annotated, javaType);
        return clsFindDeserializationType != null ? clsFindDeserializationType : this._secondary.findDeserializationType(annotated, javaType);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findDeserializationKeyType(Annotated annotated, JavaType javaType) {
        Class<?> clsFindDeserializationKeyType = this._primary.findDeserializationKeyType(annotated, javaType);
        return clsFindDeserializationKeyType == null ? this._secondary.findDeserializationKeyType(annotated, javaType) : clsFindDeserializationKeyType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findDeserializationContentType(Annotated annotated, JavaType javaType) {
        Class<?> clsFindDeserializationContentType = this._primary.findDeserializationContentType(annotated, javaType);
        return clsFindDeserializationContentType == null ? this._secondary.findDeserializationContentType(annotated, javaType) : clsFindDeserializationContentType;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findDeserializationConverter(Annotated annotated) {
        Object objFindDeserializationConverter = this._primary.findDeserializationConverter(annotated);
        return objFindDeserializationConverter == null ? this._secondary.findDeserializationConverter(annotated) : objFindDeserializationConverter;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findDeserializationContentConverter(AnnotatedMember annotatedMember) {
        Object objFindDeserializationContentConverter = this._primary.findDeserializationContentConverter(annotatedMember);
        return objFindDeserializationContentConverter == null ? this._secondary.findDeserializationContentConverter(annotatedMember) : objFindDeserializationContentConverter;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Object findValueInstantiator(AnnotatedClass annotatedClass) {
        Object objFindValueInstantiator = this._primary.findValueInstantiator(annotatedClass);
        return objFindValueInstantiator == null ? this._secondary.findValueInstantiator(annotatedClass) : objFindValueInstantiator;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public Class<?> findPOJOBuilder(AnnotatedClass annotatedClass) {
        Class<?> clsFindPOJOBuilder = this._primary.findPOJOBuilder(annotatedClass);
        return clsFindPOJOBuilder == null ? this._secondary.findPOJOBuilder(annotatedClass) : clsFindPOJOBuilder;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public JsonPOJOBuilder.Value findPOJOBuilderConfig(AnnotatedClass annotatedClass) {
        JsonPOJOBuilder.Value valueFindPOJOBuilderConfig = this._primary.findPOJOBuilderConfig(annotatedClass);
        return valueFindPOJOBuilderConfig == null ? this._secondary.findPOJOBuilderConfig(annotatedClass) : valueFindPOJOBuilderConfig;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public PropertyName findNameForDeserialization(Annotated annotated) {
        PropertyName propertyNameFindNameForDeserialization;
        PropertyName propertyNameFindNameForDeserialization2 = this._primary.findNameForDeserialization(annotated);
        if (propertyNameFindNameForDeserialization2 == null) {
            return this._secondary.findNameForDeserialization(annotated);
        }
        return (propertyNameFindNameForDeserialization2 != PropertyName.USE_DEFAULT || (propertyNameFindNameForDeserialization = this._secondary.findNameForDeserialization(annotated)) == null) ? propertyNameFindNameForDeserialization2 : propertyNameFindNameForDeserialization;
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public boolean hasAnySetterAnnotation(AnnotatedMethod annotatedMethod) {
        return this._primary.hasAnySetterAnnotation(annotatedMethod) || this._secondary.hasAnySetterAnnotation(annotatedMethod);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public boolean hasAnyGetterAnnotation(AnnotatedMethod annotatedMethod) {
        return this._primary.hasAnyGetterAnnotation(annotatedMethod) || this._secondary.hasAnyGetterAnnotation(annotatedMethod);
    }

    @Override // com.fasterxml.jackson.databind.AnnotationIntrospector
    public boolean hasCreatorAnnotation(Annotated annotated) {
        return this._primary.hasCreatorAnnotation(annotated) || this._secondary.hasCreatorAnnotation(annotated);
    }

    protected boolean _isExplicitClassOrOb(Object obj, Class<?> cls) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Class)) {
            return true;
        }
        Class<?> cls2 = (Class) obj;
        return (cls2 == cls || ClassUtil.isBogusClass(cls2)) ? false : true;
    }
}
