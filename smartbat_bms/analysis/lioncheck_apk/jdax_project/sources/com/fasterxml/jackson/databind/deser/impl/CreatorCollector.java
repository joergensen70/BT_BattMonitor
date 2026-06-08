package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class CreatorCollector {
    protected final BeanDescription _beanDesc;
    protected AnnotatedWithParams _booleanCreator;
    protected final boolean _canFixAccess;
    protected AnnotatedWithParams _defaultConstructor;
    protected CreatorProperty[] _delegateArgs;
    protected AnnotatedWithParams _delegateCreator;
    protected AnnotatedWithParams _doubleCreator;
    protected AnnotatedParameter _incompleteParameter;
    protected AnnotatedWithParams _intCreator;
    protected AnnotatedWithParams _longCreator;
    protected CreatorProperty[] _propertyBasedArgs = null;
    protected AnnotatedWithParams _propertyBasedCreator;
    protected AnnotatedWithParams _stringCreator;

    public CreatorCollector(BeanDescription beanDescription, boolean z) {
        this._beanDesc = beanDescription;
        this._canFixAccess = z;
    }

    public ValueInstantiator constructValueInstantiator(DeserializationConfig deserializationConfig) {
        JavaType javaTypeResolveType;
        boolean z = false;
        boolean z2 = this._delegateCreator == null;
        if (z2) {
            javaTypeResolveType = null;
        } else {
            CreatorProperty[] creatorPropertyArr = this._delegateArgs;
            if (creatorPropertyArr != null) {
                int length = creatorPropertyArr.length;
                int i = 0;
                while (i < length) {
                    if (this._delegateArgs[i] == null) {
                        break;
                    }
                    i++;
                }
                i = 0;
                javaTypeResolveType = this._beanDesc.bindingsForBeanType().resolveType(this._delegateCreator.getGenericParameterType(i));
            } else {
                i = 0;
                javaTypeResolveType = this._beanDesc.bindingsForBeanType().resolveType(this._delegateCreator.getGenericParameterType(i));
            }
        }
        JavaType javaType = javaTypeResolveType;
        JavaType type = this._beanDesc.getType();
        if (this._propertyBasedCreator == null && this._delegateCreator == null && this._stringCreator == null && this._longCreator == null && this._doubleCreator == null && this._booleanCreator == null) {
            z = true;
        }
        if (z2 & z) {
            Class<?> rawClass = type.getRawClass();
            if (rawClass == Collection.class || rawClass == List.class || rawClass == ArrayList.class) {
                return new Vanilla(1);
            }
            if (rawClass == Map.class || rawClass == LinkedHashMap.class) {
                return new Vanilla(2);
            }
            if (rawClass == HashMap.class) {
                return new Vanilla(3);
            }
        }
        StdValueInstantiator stdValueInstantiator = new StdValueInstantiator(deserializationConfig, type);
        stdValueInstantiator.configureFromObjectSettings(this._defaultConstructor, this._delegateCreator, javaType, this._delegateArgs, this._propertyBasedCreator, this._propertyBasedArgs);
        stdValueInstantiator.configureFromStringCreator(this._stringCreator);
        stdValueInstantiator.configureFromIntCreator(this._intCreator);
        stdValueInstantiator.configureFromLongCreator(this._longCreator);
        stdValueInstantiator.configureFromDoubleCreator(this._doubleCreator);
        stdValueInstantiator.configureFromBooleanCreator(this._booleanCreator);
        stdValueInstantiator.configureIncompleteParameter(this._incompleteParameter);
        return stdValueInstantiator;
    }

    public void setDefaultCreator(AnnotatedWithParams annotatedWithParams) {
        this._defaultConstructor = (AnnotatedWithParams) _fixAccess(annotatedWithParams);
    }

    public void addStringCreator(AnnotatedWithParams annotatedWithParams) {
        this._stringCreator = verifyNonDup(annotatedWithParams, this._stringCreator, "String");
    }

    public void addIntCreator(AnnotatedWithParams annotatedWithParams) {
        this._intCreator = verifyNonDup(annotatedWithParams, this._intCreator, "int");
    }

    public void addLongCreator(AnnotatedWithParams annotatedWithParams) {
        this._longCreator = verifyNonDup(annotatedWithParams, this._longCreator, "long");
    }

    public void addDoubleCreator(AnnotatedWithParams annotatedWithParams) {
        this._doubleCreator = verifyNonDup(annotatedWithParams, this._doubleCreator, "double");
    }

    public void addBooleanCreator(AnnotatedWithParams annotatedWithParams) {
        this._booleanCreator = verifyNonDup(annotatedWithParams, this._booleanCreator, "boolean");
    }

    public void addDelegatingCreator(AnnotatedWithParams annotatedWithParams, CreatorProperty[] creatorPropertyArr) {
        this._delegateCreator = verifyNonDup(annotatedWithParams, this._delegateCreator, "delegate");
        this._delegateArgs = creatorPropertyArr;
    }

    public void addPropertyCreator(AnnotatedWithParams annotatedWithParams, CreatorProperty[] creatorPropertyArr) {
        Integer num;
        this._propertyBasedCreator = verifyNonDup(annotatedWithParams, this._propertyBasedCreator, "property-based");
        if (creatorPropertyArr.length > 1) {
            HashMap map = new HashMap();
            int length = creatorPropertyArr.length;
            for (int i = 0; i < length; i++) {
                String name = creatorPropertyArr[i].getName();
                if ((name.length() != 0 || creatorPropertyArr[i].getInjectableValueId() == null) && (num = (Integer) map.put(name, Integer.valueOf(i))) != null) {
                    throw new IllegalArgumentException("Duplicate creator property \"" + name + "\" (index " + num + " vs " + i + ")");
                }
            }
        }
        this._propertyBasedArgs = creatorPropertyArr;
    }

    public void addIncompeteParameter(AnnotatedParameter annotatedParameter) {
        if (this._incompleteParameter == null) {
            this._incompleteParameter = annotatedParameter;
        }
    }

    public boolean hasDefaultCreator() {
        return this._defaultConstructor != null;
    }

    private <T extends AnnotatedMember> T _fixAccess(T t) {
        if (t != null && this._canFixAccess) {
            ClassUtil.checkAndFixAccess((Member) t.getAnnotated());
        }
        return t;
    }

    protected AnnotatedWithParams verifyNonDup(AnnotatedWithParams annotatedWithParams, AnnotatedWithParams annotatedWithParams2, String str) {
        if (annotatedWithParams2 != null && annotatedWithParams2.getClass() == annotatedWithParams.getClass()) {
            throw new IllegalArgumentException("Conflicting " + str + " creators: already had " + annotatedWithParams2 + ", encountered " + annotatedWithParams);
        }
        return (AnnotatedWithParams) _fixAccess(annotatedWithParams);
    }

    protected static final class Vanilla extends ValueInstantiator implements Serializable {
        public static final int TYPE_COLLECTION = 1;
        public static final int TYPE_HASH_MAP = 3;
        public static final int TYPE_MAP = 2;
        private static final long serialVersionUID = 1;
        private final int _type;

        @Override // com.fasterxml.jackson.databind.deser.ValueInstantiator
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override // com.fasterxml.jackson.databind.deser.ValueInstantiator
        public boolean canInstantiate() {
            return true;
        }

        public Vanilla(int i) {
            this._type = i;
        }

        @Override // com.fasterxml.jackson.databind.deser.ValueInstantiator
        public String getValueTypeDesc() {
            int i = this._type;
            if (i == 1) {
                return ArrayList.class.getName();
            }
            if (i == 2) {
                return LinkedHashMap.class.getName();
            }
            if (i == 3) {
                return HashMap.class.getName();
            }
            return Object.class.getName();
        }

        @Override // com.fasterxml.jackson.databind.deser.ValueInstantiator
        public Object createUsingDefault(DeserializationContext deserializationContext) throws IOException {
            int i = this._type;
            if (i == 1) {
                return new ArrayList();
            }
            if (i == 2) {
                return new LinkedHashMap();
            }
            if (i == 3) {
                return new HashMap();
            }
            throw new IllegalStateException("Unknown type " + this._type);
        }
    }
}
