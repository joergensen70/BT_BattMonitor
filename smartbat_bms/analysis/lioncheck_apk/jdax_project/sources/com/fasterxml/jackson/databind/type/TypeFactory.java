package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.LRUMap;
import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public final class TypeFactory implements Serializable {
    private static final long serialVersionUID = 1;
    protected transient HierarchicType _cachedArrayListType;
    protected transient HierarchicType _cachedHashMapType;
    protected final TypeModifier[] _modifiers;
    protected final TypeParser _parser;
    protected final LRUMap<ClassKey, JavaType> _typeCache;
    private static final JavaType[] NO_TYPES = new JavaType[0];
    protected static final TypeFactory instance = new TypeFactory();
    protected static final SimpleType CORE_TYPE_STRING = new SimpleType(String.class);
    protected static final SimpleType CORE_TYPE_BOOL = new SimpleType(Boolean.TYPE);
    protected static final SimpleType CORE_TYPE_INT = new SimpleType(Integer.TYPE);
    protected static final SimpleType CORE_TYPE_LONG = new SimpleType(Long.TYPE);

    private TypeFactory() {
        this._typeCache = new LRUMap<>(16, 100);
        this._parser = new TypeParser(this);
        this._modifiers = null;
    }

    protected TypeFactory(TypeParser typeParser, TypeModifier[] typeModifierArr) {
        this._typeCache = new LRUMap<>(16, 100);
        this._parser = typeParser;
        this._modifiers = typeModifierArr;
    }

    public TypeFactory withModifier(TypeModifier typeModifier) {
        if (typeModifier == null) {
            return new TypeFactory(this._parser, this._modifiers);
        }
        TypeModifier[] typeModifierArr = this._modifiers;
        if (typeModifierArr == null) {
            return new TypeFactory(this._parser, new TypeModifier[]{typeModifier});
        }
        return new TypeFactory(this._parser, (TypeModifier[]) ArrayBuilders.insertInListNoDup(typeModifierArr, typeModifier));
    }

    public static TypeFactory defaultInstance() {
        return instance;
    }

    public void clearCache() {
        this._typeCache.clear();
    }

    public static JavaType unknownType() {
        return defaultInstance()._unknownType();
    }

    public static Class<?> rawClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        }
        return defaultInstance().constructType(type).getRawClass();
    }

    public JavaType constructSpecializedType(JavaType javaType, Class<?> cls) {
        if (javaType.getRawClass() == cls) {
            return javaType;
        }
        if ((javaType instanceof SimpleType) && (cls.isArray() || Map.class.isAssignableFrom(cls) || Collection.class.isAssignableFrom(cls))) {
            if (!javaType.getRawClass().isAssignableFrom(cls)) {
                throw new IllegalArgumentException("Class " + cls.getClass().getName() + " not subtype of " + javaType);
            }
            JavaType javaType_fromClass = _fromClass(cls, new TypeBindings(this, javaType.getRawClass()));
            Object valueHandler = javaType.getValueHandler();
            if (valueHandler != null) {
                javaType_fromClass = javaType_fromClass.withValueHandler(valueHandler);
            }
            Object typeHandler = javaType.getTypeHandler();
            return typeHandler != null ? javaType_fromClass.withTypeHandler(typeHandler) : javaType_fromClass;
        }
        return javaType.narrowBy(cls);
    }

    public JavaType constructFromCanonical(String str) throws IllegalArgumentException {
        return this._parser.parse(str);
    }

    public JavaType[] findTypeParameters(JavaType javaType, Class<?> cls) {
        Class<?> rawClass = javaType.getRawClass();
        if (rawClass == cls) {
            int iContainedTypeCount = javaType.containedTypeCount();
            if (iContainedTypeCount == 0) {
                return null;
            }
            JavaType[] javaTypeArr = new JavaType[iContainedTypeCount];
            for (int i = 0; i < iContainedTypeCount; i++) {
                javaTypeArr[i] = javaType.containedType(i);
            }
            return javaTypeArr;
        }
        return findTypeParameters(rawClass, cls, new TypeBindings(this, javaType));
    }

    public JavaType[] findTypeParameters(Class<?> cls, Class<?> cls2) {
        return findTypeParameters(cls, cls2, new TypeBindings(this, cls));
    }

    public JavaType[] findTypeParameters(Class<?> cls, Class<?> cls2, TypeBindings typeBindings) {
        HierarchicType hierarchicType_findSuperTypeChain = _findSuperTypeChain(cls, cls2);
        if (hierarchicType_findSuperTypeChain == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not a subtype of " + cls2.getName());
        }
        while (hierarchicType_findSuperTypeChain.getSuperType() != null) {
            hierarchicType_findSuperTypeChain = hierarchicType_findSuperTypeChain.getSuperType();
            Class<?> rawClass = hierarchicType_findSuperTypeChain.getRawClass();
            TypeBindings typeBindings2 = new TypeBindings(this, rawClass);
            if (hierarchicType_findSuperTypeChain.isGeneric()) {
                Type[] actualTypeArguments = hierarchicType_findSuperTypeChain.asGeneric().getActualTypeArguments();
                TypeVariable<Class<?>>[] typeParameters = rawClass.getTypeParameters();
                int length = actualTypeArguments.length;
                for (int i = 0; i < length; i++) {
                    typeBindings2.addBinding(typeParameters[i].getName(), _constructType(actualTypeArguments[i], typeBindings));
                }
            }
            typeBindings = typeBindings2;
        }
        if (hierarchicType_findSuperTypeChain.isGeneric()) {
            return typeBindings.typesAsArray();
        }
        return null;
    }

    public JavaType moreSpecificType(JavaType javaType, JavaType javaType2) {
        Class<?> rawClass;
        Class<?> rawClass2;
        return (javaType != null && (javaType2 == null || (rawClass = javaType.getRawClass()) == (rawClass2 = javaType2.getRawClass()) || !rawClass.isAssignableFrom(rawClass2))) ? javaType : javaType2;
    }

    public JavaType constructType(Type type) {
        return _constructType(type, null);
    }

    public JavaType constructType(Type type, TypeBindings typeBindings) {
        return _constructType(type, typeBindings);
    }

    public JavaType constructType(TypeReference<?> typeReference) {
        return _constructType(typeReference.getType(), null);
    }

    public JavaType constructType(Type type, Class<?> cls) {
        return _constructType(type, cls == null ? null : new TypeBindings(this, cls));
    }

    public JavaType constructType(Type type, JavaType javaType) {
        return _constructType(type, javaType == null ? null : new TypeBindings(this, javaType));
    }

    protected JavaType _constructType(Type type, TypeBindings typeBindings) {
        JavaType javaType_fromWildcard;
        if (type instanceof Class) {
            javaType_fromWildcard = _fromClass((Class) type, typeBindings);
        } else if (type instanceof ParameterizedType) {
            javaType_fromWildcard = _fromParamType((ParameterizedType) type, typeBindings);
        } else {
            if (type instanceof JavaType) {
                return (JavaType) type;
            }
            if (type instanceof GenericArrayType) {
                javaType_fromWildcard = _fromArrayType((GenericArrayType) type, typeBindings);
            } else if (type instanceof TypeVariable) {
                javaType_fromWildcard = _fromVariable((TypeVariable) type, typeBindings);
            } else if (type instanceof WildcardType) {
                javaType_fromWildcard = _fromWildcard((WildcardType) type, typeBindings);
            } else {
                throw new IllegalArgumentException("Unrecognized Type: " + (type == null ? "[null]" : type.toString()));
            }
        }
        if (this._modifiers != null && !javaType_fromWildcard.isContainerType()) {
            for (TypeModifier typeModifier : this._modifiers) {
                javaType_fromWildcard = typeModifier.modifyType(javaType_fromWildcard, type, typeBindings, this);
            }
        }
        return javaType_fromWildcard;
    }

    public ArrayType constructArrayType(Class<?> cls) {
        return ArrayType.construct(_constructType(cls, null), null, null);
    }

    public ArrayType constructArrayType(JavaType javaType) {
        return ArrayType.construct(javaType, null, null);
    }

    public CollectionType constructCollectionType(Class<? extends Collection> cls, Class<?> cls2) {
        return CollectionType.construct((Class<?>) cls, constructType(cls2));
    }

    public CollectionType constructCollectionType(Class<? extends Collection> cls, JavaType javaType) {
        return CollectionType.construct((Class<?>) cls, javaType);
    }

    public CollectionLikeType constructCollectionLikeType(Class<?> cls, Class<?> cls2) {
        return CollectionLikeType.construct(cls, constructType(cls2));
    }

    public CollectionLikeType constructCollectionLikeType(Class<?> cls, JavaType javaType) {
        return CollectionLikeType.construct(cls, javaType);
    }

    public MapType constructMapType(Class<? extends Map> cls, JavaType javaType, JavaType javaType2) {
        return MapType.construct((Class<?>) cls, javaType, javaType2);
    }

    public MapType constructMapType(Class<? extends Map> cls, Class<?> cls2, Class<?> cls3) {
        return MapType.construct((Class<?>) cls, constructType(cls2), constructType(cls3));
    }

    public MapLikeType constructMapLikeType(Class<?> cls, JavaType javaType, JavaType javaType2) {
        return MapLikeType.construct(cls, javaType, javaType2);
    }

    public MapLikeType constructMapLikeType(Class<?> cls, Class<?> cls2, Class<?> cls3) {
        return MapType.construct(cls, constructType(cls2), constructType(cls3));
    }

    public JavaType constructSimpleType(Class<?> cls, JavaType[] javaTypeArr) {
        TypeVariable<Class<?>>[] typeParameters = cls.getTypeParameters();
        if (typeParameters.length != javaTypeArr.length) {
            throw new IllegalArgumentException("Parameter type mismatch for " + cls.getName() + ": expected " + typeParameters.length + " parameters, was given " + javaTypeArr.length);
        }
        String[] strArr = new String[typeParameters.length];
        int length = typeParameters.length;
        for (int i = 0; i < length; i++) {
            strArr[i] = typeParameters[i].getName();
        }
        return new SimpleType(cls, strArr, javaTypeArr, null, null, false);
    }

    public JavaType uncheckedSimpleType(Class<?> cls) {
        return new SimpleType(cls);
    }

    public JavaType constructParametricType(Class<?> cls, Class<?>... clsArr) {
        int length = clsArr.length;
        JavaType[] javaTypeArr = new JavaType[length];
        for (int i = 0; i < length; i++) {
            javaTypeArr[i] = _fromClass(clsArr[i], null);
        }
        return constructParametricType(cls, javaTypeArr);
    }

    public JavaType constructParametricType(Class<?> cls, JavaType... javaTypeArr) {
        if (cls.isArray()) {
            if (javaTypeArr.length != 1) {
                throw new IllegalArgumentException("Need exactly 1 parameter type for arrays (" + cls.getName() + ")");
            }
            return constructArrayType(javaTypeArr[0]);
        }
        if (Map.class.isAssignableFrom(cls)) {
            if (javaTypeArr.length != 2) {
                throw new IllegalArgumentException("Need exactly 2 parameter types for Map types (" + cls.getName() + ")");
            }
            return constructMapType((Class<? extends Map>) cls, javaTypeArr[0], javaTypeArr[1]);
        }
        if (Collection.class.isAssignableFrom(cls)) {
            if (javaTypeArr.length != 1) {
                throw new IllegalArgumentException("Need exactly 1 parameter type for Collection types (" + cls.getName() + ")");
            }
            return constructCollectionType((Class<? extends Collection>) cls, javaTypeArr[0]);
        }
        return constructSimpleType(cls, javaTypeArr);
    }

    public CollectionType constructRawCollectionType(Class<? extends Collection> cls) {
        return CollectionType.construct((Class<?>) cls, unknownType());
    }

    public CollectionLikeType constructRawCollectionLikeType(Class<?> cls) {
        return CollectionLikeType.construct(cls, unknownType());
    }

    public MapType constructRawMapType(Class<? extends Map> cls) {
        return MapType.construct((Class<?>) cls, unknownType(), unknownType());
    }

    public MapLikeType constructRawMapLikeType(Class<?> cls) {
        return MapLikeType.construct(cls, unknownType(), unknownType());
    }

    protected JavaType _fromClass(Class<?> cls, TypeBindings typeBindings) {
        SimpleType simpleType;
        JavaType javaType_collectionType;
        if (cls == String.class) {
            return CORE_TYPE_STRING;
        }
        if (cls == Boolean.TYPE) {
            return CORE_TYPE_BOOL;
        }
        if (cls == Integer.TYPE) {
            return CORE_TYPE_INT;
        }
        if (cls == Long.TYPE) {
            return CORE_TYPE_LONG;
        }
        ClassKey classKey = new ClassKey(cls);
        JavaType javaType = this._typeCache.get(classKey);
        if (javaType != null) {
            return javaType;
        }
        if (cls.isArray()) {
            javaType_collectionType = ArrayType.construct(_constructType(cls.getComponentType(), null), null, null);
        } else {
            if (cls.isEnum()) {
                simpleType = new SimpleType(cls);
            } else if (Map.class.isAssignableFrom(cls)) {
                javaType_collectionType = _mapType(cls);
            } else if (Collection.class.isAssignableFrom(cls)) {
                javaType_collectionType = _collectionType(cls);
            } else {
                simpleType = new SimpleType(cls);
            }
            javaType_collectionType = simpleType;
        }
        this._typeCache.put(classKey, javaType_collectionType);
        return javaType_collectionType;
    }

    protected JavaType _fromParameterizedClass(Class<?> cls, List<JavaType> list) {
        if (cls.isArray()) {
            return ArrayType.construct(_constructType(cls.getComponentType(), null), null, null);
        }
        if (cls.isEnum()) {
            return new SimpleType(cls);
        }
        if (Map.class.isAssignableFrom(cls)) {
            if (list.size() > 0) {
                return MapType.construct(cls, list.get(0), list.size() >= 2 ? list.get(1) : _unknownType());
            }
            return _mapType(cls);
        }
        if (Collection.class.isAssignableFrom(cls)) {
            if (list.size() >= 1) {
                return CollectionType.construct(cls, list.get(0));
            }
            return _collectionType(cls);
        }
        if (list.size() == 0) {
            return new SimpleType(cls);
        }
        return constructSimpleType(cls, (JavaType[]) list.toArray(new JavaType[list.size()]));
    }

    protected JavaType _fromParamType(ParameterizedType parameterizedType, TypeBindings typeBindings) {
        JavaType[] javaTypeArr;
        Class<?> cls = (Class) parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        int length = actualTypeArguments == null ? 0 : actualTypeArguments.length;
        if (length == 0) {
            javaTypeArr = NO_TYPES;
        } else {
            JavaType[] javaTypeArr2 = new JavaType[length];
            for (int i = 0; i < length; i++) {
                javaTypeArr2[i] = _constructType(actualTypeArguments[i], typeBindings);
            }
            javaTypeArr = javaTypeArr2;
        }
        if (Map.class.isAssignableFrom(cls)) {
            JavaType[] javaTypeArrFindTypeParameters = findTypeParameters(constructSimpleType(cls, javaTypeArr), Map.class);
            if (javaTypeArrFindTypeParameters.length != 2) {
                throw new IllegalArgumentException("Could not find 2 type parameters for Map class " + cls.getName() + " (found " + javaTypeArrFindTypeParameters.length + ")");
            }
            return MapType.construct(cls, javaTypeArrFindTypeParameters[0], javaTypeArrFindTypeParameters[1]);
        }
        if (!Collection.class.isAssignableFrom(cls)) {
            if (length == 0) {
                return new SimpleType(cls);
            }
            return constructSimpleType(cls, javaTypeArr);
        }
        JavaType[] javaTypeArrFindTypeParameters2 = findTypeParameters(constructSimpleType(cls, javaTypeArr), Collection.class);
        if (javaTypeArrFindTypeParameters2.length != 1) {
            throw new IllegalArgumentException("Could not find 1 type parameter for Collection class " + cls.getName() + " (found " + javaTypeArrFindTypeParameters2.length + ")");
        }
        return CollectionType.construct(cls, javaTypeArrFindTypeParameters2[0]);
    }

    protected JavaType _fromArrayType(GenericArrayType genericArrayType, TypeBindings typeBindings) {
        return ArrayType.construct(_constructType(genericArrayType.getGenericComponentType(), typeBindings), null, null);
    }

    protected JavaType _fromVariable(TypeVariable<?> typeVariable, TypeBindings typeBindings) {
        if (typeBindings == null) {
            return _unknownType();
        }
        String name = typeVariable.getName();
        JavaType javaTypeFindType = typeBindings.findType(name);
        if (javaTypeFindType != null) {
            return javaTypeFindType;
        }
        Type[] bounds = typeVariable.getBounds();
        typeBindings._addPlaceholder(name);
        return _constructType(bounds[0], typeBindings);
    }

    protected JavaType _fromWildcard(WildcardType wildcardType, TypeBindings typeBindings) {
        return _constructType(wildcardType.getUpperBounds()[0], typeBindings);
    }

    private JavaType _mapType(Class<?> cls) {
        JavaType[] javaTypeArrFindTypeParameters = findTypeParameters(cls, Map.class);
        if (javaTypeArrFindTypeParameters == null) {
            return MapType.construct(cls, _unknownType(), _unknownType());
        }
        if (javaTypeArrFindTypeParameters.length != 2) {
            throw new IllegalArgumentException("Strange Map type " + cls.getName() + ": can not determine type parameters");
        }
        return MapType.construct(cls, javaTypeArrFindTypeParameters[0], javaTypeArrFindTypeParameters[1]);
    }

    private JavaType _collectionType(Class<?> cls) {
        JavaType[] javaTypeArrFindTypeParameters = findTypeParameters(cls, Collection.class);
        if (javaTypeArrFindTypeParameters == null) {
            return CollectionType.construct(cls, _unknownType());
        }
        if (javaTypeArrFindTypeParameters.length != 1) {
            throw new IllegalArgumentException("Strange Collection type " + cls.getName() + ": can not determine type parameters");
        }
        return CollectionType.construct(cls, javaTypeArrFindTypeParameters[0]);
    }

    protected JavaType _resolveVariableViaSubTypes(HierarchicType hierarchicType, String str, TypeBindings typeBindings) {
        if (hierarchicType != null && hierarchicType.isGeneric()) {
            TypeVariable<Class<?>>[] typeParameters = hierarchicType.getRawClass().getTypeParameters();
            int length = typeParameters.length;
            for (int i = 0; i < length; i++) {
                if (str.equals(typeParameters[i].getName())) {
                    Type type = hierarchicType.asGeneric().getActualTypeArguments()[i];
                    if (type instanceof TypeVariable) {
                        return _resolveVariableViaSubTypes(hierarchicType.getSubType(), ((TypeVariable) type).getName(), typeBindings);
                    }
                    return _constructType(type, typeBindings);
                }
            }
        }
        return _unknownType();
    }

    protected JavaType _unknownType() {
        return new SimpleType(Object.class);
    }

    protected HierarchicType _findSuperTypeChain(Class<?> cls, Class<?> cls2) {
        if (cls2.isInterface()) {
            return _findSuperInterfaceChain(cls, cls2);
        }
        return _findSuperClassChain(cls, cls2);
    }

    protected HierarchicType _findSuperClassChain(Type type, Class<?> cls) {
        HierarchicType hierarchicType_findSuperClassChain;
        HierarchicType hierarchicType = new HierarchicType(type);
        Class<?> rawClass = hierarchicType.getRawClass();
        if (rawClass == cls) {
            return hierarchicType;
        }
        Type genericSuperclass = rawClass.getGenericSuperclass();
        if (genericSuperclass == null || (hierarchicType_findSuperClassChain = _findSuperClassChain(genericSuperclass, cls)) == null) {
            return null;
        }
        hierarchicType_findSuperClassChain.setSubType(hierarchicType);
        hierarchicType.setSuperType(hierarchicType_findSuperClassChain);
        return hierarchicType;
    }

    protected HierarchicType _findSuperInterfaceChain(Type type, Class<?> cls) {
        HierarchicType hierarchicType = new HierarchicType(type);
        Class<?> rawClass = hierarchicType.getRawClass();
        if (rawClass == cls) {
            return new HierarchicType(type);
        }
        if (rawClass == HashMap.class && cls == Map.class) {
            return _hashMapSuperInterfaceChain(hierarchicType);
        }
        if (rawClass == ArrayList.class && cls == List.class) {
            return _arrayListSuperInterfaceChain(hierarchicType);
        }
        return _doFindSuperInterfaceChain(hierarchicType, cls);
    }

    protected HierarchicType _doFindSuperInterfaceChain(HierarchicType hierarchicType, Class<?> cls) {
        HierarchicType hierarchicType_findSuperInterfaceChain;
        Class<?> rawClass = hierarchicType.getRawClass();
        Type[] genericInterfaces = rawClass.getGenericInterfaces();
        if (genericInterfaces != null) {
            for (Type type : genericInterfaces) {
                HierarchicType hierarchicType_findSuperInterfaceChain2 = _findSuperInterfaceChain(type, cls);
                if (hierarchicType_findSuperInterfaceChain2 != null) {
                    hierarchicType_findSuperInterfaceChain2.setSubType(hierarchicType);
                    hierarchicType.setSuperType(hierarchicType_findSuperInterfaceChain2);
                    return hierarchicType;
                }
            }
        }
        Type genericSuperclass = rawClass.getGenericSuperclass();
        if (genericSuperclass == null || (hierarchicType_findSuperInterfaceChain = _findSuperInterfaceChain(genericSuperclass, cls)) == null) {
            return null;
        }
        hierarchicType_findSuperInterfaceChain.setSubType(hierarchicType);
        hierarchicType.setSuperType(hierarchicType_findSuperInterfaceChain);
        return hierarchicType;
    }

    protected synchronized HierarchicType _hashMapSuperInterfaceChain(HierarchicType hierarchicType) {
        if (this._cachedHashMapType == null) {
            HierarchicType hierarchicTypeDeepCloneWithoutSubtype = hierarchicType.deepCloneWithoutSubtype();
            _doFindSuperInterfaceChain(hierarchicTypeDeepCloneWithoutSubtype, Map.class);
            this._cachedHashMapType = hierarchicTypeDeepCloneWithoutSubtype.getSuperType();
        }
        HierarchicType hierarchicTypeDeepCloneWithoutSubtype2 = this._cachedHashMapType.deepCloneWithoutSubtype();
        hierarchicType.setSuperType(hierarchicTypeDeepCloneWithoutSubtype2);
        hierarchicTypeDeepCloneWithoutSubtype2.setSubType(hierarchicType);
        return hierarchicType;
    }

    protected synchronized HierarchicType _arrayListSuperInterfaceChain(HierarchicType hierarchicType) {
        if (this._cachedArrayListType == null) {
            HierarchicType hierarchicTypeDeepCloneWithoutSubtype = hierarchicType.deepCloneWithoutSubtype();
            _doFindSuperInterfaceChain(hierarchicTypeDeepCloneWithoutSubtype, List.class);
            this._cachedArrayListType = hierarchicTypeDeepCloneWithoutSubtype.getSuperType();
        }
        HierarchicType hierarchicTypeDeepCloneWithoutSubtype2 = this._cachedArrayListType.deepCloneWithoutSubtype();
        hierarchicType.setSuperType(hierarchicTypeDeepCloneWithoutSubtype2);
        hierarchicTypeDeepCloneWithoutSubtype2.setSubType(hierarchicType);
        return hierarchicType;
    }
}
