package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class TypeBindings {
    private static final JavaType[] NO_TYPES = new JavaType[0];
    public static final JavaType UNBOUND = new SimpleType(Object.class);
    protected Map<String, JavaType> _bindings;
    protected final Class<?> _contextClass;
    protected final JavaType _contextType;
    private final TypeBindings _parentBindings;
    protected HashSet<String> _placeholders;
    protected final TypeFactory _typeFactory;

    public TypeBindings(TypeFactory typeFactory, Class<?> cls) {
        this(typeFactory, null, cls, null);
    }

    public TypeBindings(TypeFactory typeFactory, JavaType javaType) {
        this(typeFactory, null, javaType.getRawClass(), javaType);
    }

    public TypeBindings childInstance() {
        return new TypeBindings(this._typeFactory, this, this._contextClass, this._contextType);
    }

    private TypeBindings(TypeFactory typeFactory, TypeBindings typeBindings, Class<?> cls, JavaType javaType) {
        this._typeFactory = typeFactory;
        this._parentBindings = typeBindings;
        this._contextClass = cls;
        this._contextType = javaType;
    }

    public JavaType resolveType(Class<?> cls) {
        return this._typeFactory._constructType(cls, this);
    }

    public JavaType resolveType(Type type) {
        return this._typeFactory._constructType(type, this);
    }

    public int getBindingCount() {
        if (this._bindings == null) {
            _resolve();
        }
        return this._bindings.size();
    }

    public JavaType findType(String str) {
        String name;
        if (this._bindings == null) {
            _resolve();
        }
        JavaType javaType = this._bindings.get(str);
        if (javaType != null) {
            return javaType;
        }
        HashSet<String> hashSet = this._placeholders;
        if (hashSet != null && hashSet.contains(str)) {
            return UNBOUND;
        }
        TypeBindings typeBindings = this._parentBindings;
        if (typeBindings != null) {
            return typeBindings.findType(str);
        }
        Class<?> cls = this._contextClass;
        if (cls != null && cls.getEnclosingClass() != null && !Modifier.isStatic(this._contextClass.getModifiers())) {
            return UNBOUND;
        }
        Class<?> cls2 = this._contextClass;
        if (cls2 != null) {
            name = cls2.getName();
        } else {
            JavaType javaType2 = this._contextType;
            if (javaType2 != null) {
                name = javaType2.toString();
            } else {
                name = "UNKNOWN";
            }
        }
        throw new IllegalArgumentException("Type variable '" + str + "' can not be resolved (with context of class " + name + ")");
    }

    public void addBinding(String str, JavaType javaType) {
        Map<String, JavaType> map = this._bindings;
        if (map == null || map.size() == 0) {
            this._bindings = new LinkedHashMap();
        }
        this._bindings.put(str, javaType);
    }

    public JavaType[] typesAsArray() {
        if (this._bindings == null) {
            _resolve();
        }
        if (this._bindings.size() == 0) {
            return NO_TYPES;
        }
        return (JavaType[]) this._bindings.values().toArray(new JavaType[this._bindings.size()]);
    }

    protected void _resolve() {
        int iContainedTypeCount;
        _resolveBindings(this._contextClass);
        JavaType javaType = this._contextType;
        if (javaType != null && (iContainedTypeCount = javaType.containedTypeCount()) > 0) {
            for (int i = 0; i < iContainedTypeCount; i++) {
                addBinding(this._contextType.containedTypeName(i), this._contextType.containedType(i));
            }
        }
        if (this._bindings == null) {
            this._bindings = Collections.emptyMap();
        }
    }

    public void _addPlaceholder(String str) {
        if (this._placeholders == null) {
            this._placeholders = new HashSet<>();
        }
        this._placeholders.add(str);
    }

    protected void _resolveBindings(Type type) {
        Class<?> cls;
        if (type == null) {
            return;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                Class cls2 = (Class) parameterizedType.getRawType();
                TypeVariable[] typeParameters = cls2.getTypeParameters();
                if (typeParameters.length != actualTypeArguments.length) {
                    throw new IllegalArgumentException("Strange parametrized type (in class " + cls2.getName() + "): number of type arguments != number of type parameters (" + actualTypeArguments.length + " vs " + typeParameters.length + ")");
                }
                int length = actualTypeArguments.length;
                for (int i = 0; i < length; i++) {
                    String name = typeParameters[i].getName();
                    Map<String, JavaType> map = this._bindings;
                    if (map == null) {
                        this._bindings = new LinkedHashMap();
                    } else {
                        if (map.containsKey(name)) {
                        }
                    }
                    _addPlaceholder(name);
                    this._bindings.put(name, this._typeFactory._constructType(actualTypeArguments[i], this));
                }
            }
            cls = (Class) parameterizedType.getRawType();
        } else {
            if (!(type instanceof Class)) {
                return;
            }
            cls = (Class) type;
            Class<?> declaringClass = cls.getDeclaringClass();
            if (declaringClass != null && !declaringClass.isAssignableFrom(cls)) {
                _resolveBindings(cls.getDeclaringClass());
            }
            TypeVariable<Class<?>>[] typeParameters2 = cls.getTypeParameters();
            if (typeParameters2 != null && typeParameters2.length > 0) {
                JavaType javaType = this._contextType;
                JavaType[] javaTypeArrFindTypeParameters = (javaType == null || !cls.isAssignableFrom(javaType.getRawClass())) ? null : this._typeFactory.findTypeParameters(this._contextType, cls);
                for (int i2 = 0; i2 < typeParameters2.length; i2++) {
                    TypeVariable<Class<?>> typeVariable = typeParameters2[i2];
                    String name2 = typeVariable.getName();
                    Type type2 = typeVariable.getBounds()[0];
                    if (type2 != null) {
                        Map<String, JavaType> map2 = this._bindings;
                        if (map2 == null) {
                            this._bindings = new LinkedHashMap();
                        } else if (map2.containsKey(name2)) {
                        }
                        _addPlaceholder(name2);
                        if (javaTypeArrFindTypeParameters != null && javaTypeArrFindTypeParameters.length > i2) {
                            this._bindings.put(name2, javaTypeArrFindTypeParameters[i2]);
                        } else {
                            this._bindings.put(name2, this._typeFactory._constructType(type2, this));
                        }
                    }
                }
            }
        }
        _resolveBindings(cls.getGenericSuperclass());
        for (Type type3 : cls.getGenericInterfaces()) {
            _resolveBindings(type3);
        }
    }

    public String toString() {
        if (this._bindings == null) {
            _resolve();
        }
        StringBuilder sb = new StringBuilder("[TypeBindings for ");
        JavaType javaType = this._contextType;
        if (javaType != null) {
            sb.append(javaType.toString());
        } else {
            sb.append(this._contextClass.getName());
        }
        sb.append(": ").append(this._bindings).append("]");
        return sb.toString();
    }
}
