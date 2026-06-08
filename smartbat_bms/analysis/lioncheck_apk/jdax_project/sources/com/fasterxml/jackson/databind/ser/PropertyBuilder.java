package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.NameTransformer;

/* JADX INFO: loaded from: classes.dex */
public class PropertyBuilder {
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final BeanDescription _beanDesc;
    protected final SerializationConfig _config;
    protected Object _defaultBean;
    protected final JsonInclude.Include _outputProps;

    public PropertyBuilder(SerializationConfig serializationConfig, BeanDescription beanDescription) {
        this._config = serializationConfig;
        this._beanDesc = beanDescription;
        this._outputProps = beanDescription.findSerializationInclusion(serializationConfig.getSerializationInclusion());
        this._annotationIntrospector = serializationConfig.getAnnotationIntrospector();
    }

    public Annotations getClassAnnotations() {
        return this._beanDesc.getClassAnnotations();
    }

    @Deprecated
    protected final BeanPropertyWriter buildWriter(BeanPropertyDefinition beanPropertyDefinition, JavaType javaType, JsonSerializer<?> jsonSerializer, TypeSerializer typeSerializer, TypeSerializer typeSerializer2, AnnotatedMember annotatedMember, boolean z) {
        throw new IllegalStateException();
    }

    protected BeanPropertyWriter buildWriter(SerializerProvider serializerProvider, BeanPropertyDefinition beanPropertyDefinition, JavaType javaType, JsonSerializer<?> jsonSerializer, TypeSerializer typeSerializer, TypeSerializer typeSerializer2, AnnotatedMember annotatedMember, boolean z) throws JsonMappingException {
        Object obj;
        boolean z2;
        JavaType javaTypeFindSerializationType = findSerializationType(annotatedMember, z, javaType);
        if (typeSerializer2 != null) {
            if (javaTypeFindSerializationType == null) {
                javaTypeFindSerializationType = javaType;
            }
            if (javaTypeFindSerializationType.getContentType() == null) {
                throw new IllegalStateException("Problem trying to create BeanPropertyWriter for property '" + beanPropertyDefinition.getName() + "' (of type " + this._beanDesc.getType() + "); serialization type " + javaTypeFindSerializationType + " has no content");
            }
            javaTypeFindSerializationType = javaTypeFindSerializationType.withContentTypeHandler(typeSerializer2);
            javaTypeFindSerializationType.getContentType();
        }
        JavaType javaType2 = javaTypeFindSerializationType;
        JsonInclude.Include includeFindSerializationInclusion = this._annotationIntrospector.findSerializationInclusion(annotatedMember, this._outputProps);
        Object defaultValue = null;
        boolean z3 = false;
        if (includeFindSerializationInclusion == null) {
            obj = defaultValue;
            z2 = z3;
        } else {
            int i = AnonymousClass1.$SwitchMap$com$fasterxml$jackson$annotation$JsonInclude$Include[includeFindSerializationInclusion.ordinal()];
            if (i == 1) {
                defaultValue = getDefaultValue(beanPropertyDefinition.getName(), annotatedMember);
                if (defaultValue != null) {
                    if (defaultValue.getClass().isArray()) {
                        defaultValue = ArrayBuilders.getArrayComparator(defaultValue);
                    }
                    obj = defaultValue;
                    z2 = z3;
                }
                obj = defaultValue;
                z2 = true;
            } else if (i == 2) {
                defaultValue = BeanPropertyWriter.MARKER_FOR_EMPTY;
                obj = defaultValue;
                z2 = true;
            } else {
                if (i != 3) {
                    if (i == 4) {
                    }
                    obj = defaultValue;
                    z2 = z3;
                } else {
                    z3 = true;
                }
                if (javaType.isContainerType() && !this._config.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)) {
                    defaultValue = BeanPropertyWriter.MARKER_FOR_EMPTY;
                }
                obj = defaultValue;
                z2 = z3;
            }
        }
        BeanPropertyWriter beanPropertyWriter = new BeanPropertyWriter(beanPropertyDefinition, annotatedMember, this._beanDesc.getClassAnnotations(), javaType, jsonSerializer, typeSerializer, javaType2, z2, obj);
        Object objFindNullSerializer = this._annotationIntrospector.findNullSerializer(annotatedMember);
        if (objFindNullSerializer != null) {
            beanPropertyWriter.assignNullSerializer(serializerProvider.serializerInstance(annotatedMember, objFindNullSerializer));
        }
        NameTransformer nameTransformerFindUnwrappingNameTransformer = this._annotationIntrospector.findUnwrappingNameTransformer(annotatedMember);
        return nameTransformerFindUnwrappingNameTransformer != null ? beanPropertyWriter.unwrappingWriter(nameTransformerFindUnwrappingNameTransformer) : beanPropertyWriter;
    }

    /* JADX INFO: renamed from: com.fasterxml.jackson.databind.ser.PropertyBuilder$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$fasterxml$jackson$annotation$JsonInclude$Include;

        static {
            int[] iArr = new int[JsonInclude.Include.values().length];
            $SwitchMap$com$fasterxml$jackson$annotation$JsonInclude$Include = iArr;
            try {
                iArr[JsonInclude.Include.NON_DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$annotation$JsonInclude$Include[JsonInclude.Include.NON_EMPTY.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$annotation$JsonInclude$Include[JsonInclude.Include.NON_NULL.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$annotation$JsonInclude$Include[JsonInclude.Include.ALWAYS.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    protected JavaType findSerializationType(Annotated annotated, boolean z, JavaType javaType) {
        JavaType javaTypeConstructSpecializedType;
        Class<?> clsFindSerializationType = this._annotationIntrospector.findSerializationType(annotated);
        if (clsFindSerializationType != null) {
            Class<?> rawClass = javaType.getRawClass();
            if (clsFindSerializationType.isAssignableFrom(rawClass)) {
                javaTypeConstructSpecializedType = javaType.widenBy(clsFindSerializationType);
            } else {
                if (!rawClass.isAssignableFrom(clsFindSerializationType)) {
                    throw new IllegalArgumentException("Illegal concrete-type annotation for method '" + annotated.getName() + "': class " + clsFindSerializationType.getName() + " not a super-type of (declared) class " + rawClass.getName());
                }
                javaTypeConstructSpecializedType = this._config.constructSpecializedType(javaType, clsFindSerializationType);
            }
            javaType = javaTypeConstructSpecializedType;
            z = true;
        }
        JavaType javaTypeModifySecondaryTypesByAnnotation = BasicSerializerFactory.modifySecondaryTypesByAnnotation(this._config, annotated, javaType);
        if (javaTypeModifySecondaryTypesByAnnotation != javaType) {
            javaType = javaTypeModifySecondaryTypesByAnnotation;
            z = true;
        }
        JsonSerialize.Typing typingFindSerializationTyping = this._annotationIntrospector.findSerializationTyping(annotated);
        if (typingFindSerializationTyping != null && typingFindSerializationTyping != JsonSerialize.Typing.DEFAULT_TYPING) {
            z = typingFindSerializationTyping == JsonSerialize.Typing.STATIC;
        }
        if (z) {
            return javaType;
        }
        return null;
    }

    protected Object getDefaultBean() {
        if (this._defaultBean == null) {
            Object objInstantiateBean = this._beanDesc.instantiateBean(this._config.canOverrideAccessModifiers());
            this._defaultBean = objInstantiateBean;
            if (objInstantiateBean == null) {
                throw new IllegalArgumentException("Class " + this._beanDesc.getClassInfo().getAnnotated().getName() + " has no default constructor; can not instantiate default bean value to support 'properties=JsonSerialize.Inclusion.NON_DEFAULT' annotation");
            }
        }
        return this._defaultBean;
    }

    protected Object getDefaultValue(String str, AnnotatedMember annotatedMember) {
        Object defaultBean = getDefaultBean();
        try {
            return annotatedMember.getValue(defaultBean);
        } catch (Exception e) {
            return _throwWrapped(e, str, defaultBean);
        }
    }

    protected Object _throwWrapped(Exception exc, String str, Object obj) {
        Throwable cause = exc;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof Error) {
            throw ((Error) cause);
        }
        if (cause instanceof RuntimeException) {
            throw ((RuntimeException) cause);
        }
        throw new IllegalArgumentException("Failed to get property '" + str + "' of default " + obj.getClass().getName() + " instance");
    }
}
