package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public final class AnnotatedClass extends Annotated {
    private static final AnnotationMap[] NO_ANNOTATION_MAPS = new AnnotationMap[0];
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final Class<?> _class;
    protected AnnotationMap _classAnnotations;
    protected List<AnnotatedConstructor> _constructors;
    protected List<AnnotatedMethod> _creatorMethods;
    protected boolean _creatorsResolved = false;
    protected AnnotatedConstructor _defaultConstructor;
    protected List<AnnotatedField> _fields;
    protected AnnotatedMethodMap _memberMethods;
    protected final ClassIntrospector.MixInResolver _mixInResolver;
    protected final Class<?> _primaryMixIn;
    protected final List<Class<?>> _superTypes;

    private AnnotatedClass(Class<?> cls, List<Class<?>> list, AnnotationIntrospector annotationIntrospector, ClassIntrospector.MixInResolver mixInResolver, AnnotationMap annotationMap) {
        this._class = cls;
        this._superTypes = list;
        this._annotationIntrospector = annotationIntrospector;
        this._mixInResolver = mixInResolver;
        this._primaryMixIn = mixInResolver == null ? null : mixInResolver.findMixInClassFor(cls);
        this._classAnnotations = annotationMap;
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public AnnotatedClass withAnnotations(AnnotationMap annotationMap) {
        return new AnnotatedClass(this._class, this._superTypes, this._annotationIntrospector, this._mixInResolver, annotationMap);
    }

    public static AnnotatedClass construct(Class<?> cls, AnnotationIntrospector annotationIntrospector, ClassIntrospector.MixInResolver mixInResolver) {
        return new AnnotatedClass(cls, ClassUtil.findSuperTypes(cls, null), annotationIntrospector, mixInResolver, null);
    }

    public static AnnotatedClass constructWithoutSuperTypes(Class<?> cls, AnnotationIntrospector annotationIntrospector, ClassIntrospector.MixInResolver mixInResolver) {
        return new AnnotatedClass(cls, Collections.emptyList(), annotationIntrospector, mixInResolver, null);
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public Class<?> getAnnotated() {
        return this._class;
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public int getModifiers() {
        return this._class.getModifiers();
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public String getName() {
        return this._class.getName();
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public <A extends Annotation> A getAnnotation(Class<A> cls) {
        if (this._classAnnotations == null) {
            resolveClassAnnotations();
        }
        return (A) this._classAnnotations.get(cls);
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public Type getGenericType() {
        return this._class;
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public Class<?> getRawType() {
        return this._class;
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    public Iterable<Annotation> annotations() {
        if (this._classAnnotations == null) {
            resolveClassAnnotations();
        }
        return this._classAnnotations.annotations();
    }

    @Override // com.fasterxml.jackson.databind.introspect.Annotated
    protected AnnotationMap getAllAnnotations() {
        if (this._classAnnotations == null) {
            resolveClassAnnotations();
        }
        return this._classAnnotations;
    }

    public Annotations getAnnotations() {
        if (this._classAnnotations == null) {
            resolveClassAnnotations();
        }
        return this._classAnnotations;
    }

    public boolean hasAnnotations() {
        if (this._classAnnotations == null) {
            resolveClassAnnotations();
        }
        return this._classAnnotations.size() > 0;
    }

    public AnnotatedConstructor getDefaultConstructor() {
        if (!this._creatorsResolved) {
            resolveCreators();
        }
        return this._defaultConstructor;
    }

    public List<AnnotatedConstructor> getConstructors() {
        if (!this._creatorsResolved) {
            resolveCreators();
        }
        return this._constructors;
    }

    public List<AnnotatedMethod> getStaticMethods() {
        if (!this._creatorsResolved) {
            resolveCreators();
        }
        return this._creatorMethods;
    }

    public Iterable<AnnotatedMethod> memberMethods() {
        if (this._memberMethods == null) {
            resolveMemberMethods();
        }
        return this._memberMethods;
    }

    public int getMemberMethodCount() {
        if (this._memberMethods == null) {
            resolveMemberMethods();
        }
        return this._memberMethods.size();
    }

    public AnnotatedMethod findMethod(String str, Class<?>[] clsArr) {
        if (this._memberMethods == null) {
            resolveMemberMethods();
        }
        return this._memberMethods.find(str, clsArr);
    }

    public int getFieldCount() {
        if (this._fields == null) {
            resolveFields();
        }
        return this._fields.size();
    }

    public Iterable<AnnotatedField> fields() {
        if (this._fields == null) {
            resolveFields();
        }
        return this._fields;
    }

    private void resolveClassAnnotations() {
        AnnotationMap annotationMap = new AnnotationMap();
        this._classAnnotations = annotationMap;
        if (this._annotationIntrospector != null) {
            Class<?> cls = this._primaryMixIn;
            if (cls != null) {
                _addClassMixIns(annotationMap, this._class, cls);
            }
            _addAnnotationsIfNotPresent(this._classAnnotations, this._class.getDeclaredAnnotations());
            for (Class<?> cls2 : this._superTypes) {
                _addClassMixIns(this._classAnnotations, cls2);
                _addAnnotationsIfNotPresent(this._classAnnotations, cls2.getDeclaredAnnotations());
            }
            _addClassMixIns(this._classAnnotations, Object.class);
        }
    }

    private void resolveCreators() {
        Constructor<?>[] declaredConstructors = this._class.getDeclaredConstructors();
        ArrayList arrayList = null;
        ArrayList arrayList2 = null;
        for (Constructor<?> constructor : declaredConstructors) {
            if (constructor.getParameterTypes().length == 0) {
                this._defaultConstructor = _constructConstructor(constructor, true);
            } else {
                if (arrayList2 == null) {
                    arrayList2 = new ArrayList(Math.max(10, declaredConstructors.length));
                }
                arrayList2.add(_constructConstructor(constructor, false));
            }
        }
        if (arrayList2 == null) {
            this._constructors = Collections.emptyList();
        } else {
            this._constructors = arrayList2;
        }
        if (this._primaryMixIn != null && (this._defaultConstructor != null || !this._constructors.isEmpty())) {
            _addConstructorMixIns(this._primaryMixIn);
        }
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        if (annotationIntrospector != null) {
            AnnotatedConstructor annotatedConstructor = this._defaultConstructor;
            if (annotatedConstructor != null && annotationIntrospector.hasIgnoreMarker(annotatedConstructor)) {
                this._defaultConstructor = null;
            }
            List<AnnotatedConstructor> list = this._constructors;
            if (list != null) {
                int size = list.size();
                while (true) {
                    size--;
                    if (size < 0) {
                        break;
                    } else if (this._annotationIntrospector.hasIgnoreMarker(this._constructors.get(size))) {
                        this._constructors.remove(size);
                    }
                }
            }
        }
        for (Method method : _findClassMethods(this._class)) {
            if (Modifier.isStatic(method.getModifiers())) {
                if (arrayList == null) {
                    arrayList = new ArrayList(8);
                }
                arrayList.add(_constructCreatorMethod(method));
            }
        }
        if (arrayList == null) {
            this._creatorMethods = Collections.emptyList();
        } else {
            this._creatorMethods = arrayList;
            Class<?> cls = this._primaryMixIn;
            if (cls != null) {
                _addFactoryMixIns(cls);
            }
            if (this._annotationIntrospector != null) {
                int size2 = this._creatorMethods.size();
                while (true) {
                    size2--;
                    if (size2 < 0) {
                        break;
                    } else if (this._annotationIntrospector.hasIgnoreMarker(this._creatorMethods.get(size2))) {
                        this._creatorMethods.remove(size2);
                    }
                }
            }
        }
        this._creatorsResolved = true;
    }

    private void resolveMemberMethods() {
        Class<?> clsFindMixInClassFor;
        this._memberMethods = new AnnotatedMethodMap();
        AnnotatedMethodMap annotatedMethodMap = new AnnotatedMethodMap();
        _addMemberMethods(this._class, this._memberMethods, this._primaryMixIn, annotatedMethodMap);
        for (Class<?> cls : this._superTypes) {
            ClassIntrospector.MixInResolver mixInResolver = this._mixInResolver;
            _addMemberMethods(cls, this._memberMethods, mixInResolver == null ? null : mixInResolver.findMixInClassFor(cls), annotatedMethodMap);
        }
        ClassIntrospector.MixInResolver mixInResolver2 = this._mixInResolver;
        if (mixInResolver2 != null && (clsFindMixInClassFor = mixInResolver2.findMixInClassFor(Object.class)) != null) {
            _addMethodMixIns(this._class, this._memberMethods, clsFindMixInClassFor, annotatedMethodMap);
        }
        if (this._annotationIntrospector == null || annotatedMethodMap.isEmpty()) {
            return;
        }
        for (AnnotatedMethod annotatedMethod : annotatedMethodMap) {
            try {
                Method declaredMethod = Object.class.getDeclaredMethod(annotatedMethod.getName(), annotatedMethod.getRawParameterTypes());
                if (declaredMethod != null) {
                    AnnotatedMethod annotatedMethod_constructMethod = _constructMethod(declaredMethod);
                    _addMixOvers(annotatedMethod.getAnnotated(), annotatedMethod_constructMethod, false);
                    this._memberMethods.add(annotatedMethod_constructMethod);
                }
            } catch (Exception unused) {
            }
        }
    }

    private void resolveFields() {
        Map<String, AnnotatedField> map_findFields = _findFields(this._class, null);
        if (map_findFields == null || map_findFields.size() == 0) {
            this._fields = Collections.emptyList();
            return;
        }
        ArrayList arrayList = new ArrayList(map_findFields.size());
        this._fields = arrayList;
        arrayList.addAll(map_findFields.values());
    }

    protected void _addClassMixIns(AnnotationMap annotationMap, Class<?> cls) {
        ClassIntrospector.MixInResolver mixInResolver = this._mixInResolver;
        if (mixInResolver != null) {
            _addClassMixIns(annotationMap, cls, mixInResolver.findMixInClassFor(cls));
        }
    }

    protected void _addClassMixIns(AnnotationMap annotationMap, Class<?> cls, Class<?> cls2) {
        if (cls2 == null) {
            return;
        }
        _addAnnotationsIfNotPresent(annotationMap, cls2.getDeclaredAnnotations());
        Iterator<Class<?>> it = ClassUtil.findSuperTypes(cls2, cls).iterator();
        while (it.hasNext()) {
            _addAnnotationsIfNotPresent(annotationMap, it.next().getDeclaredAnnotations());
        }
    }

    protected void _addConstructorMixIns(Class<?> cls) {
        List<AnnotatedConstructor> list = this._constructors;
        int size = list == null ? 0 : list.size();
        MemberKey[] memberKeyArr = null;
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                AnnotatedConstructor annotatedConstructor = this._defaultConstructor;
                if (annotatedConstructor != null) {
                    _addMixOvers(constructor, annotatedConstructor, false);
                }
            } else {
                if (memberKeyArr == null) {
                    memberKeyArr = new MemberKey[size];
                    for (int i = 0; i < size; i++) {
                        memberKeyArr[i] = new MemberKey(this._constructors.get(i).getAnnotated());
                    }
                }
                MemberKey memberKey = new MemberKey(constructor);
                int i2 = 0;
                while (true) {
                    if (i2 >= size) {
                        break;
                    }
                    if (memberKey.equals(memberKeyArr[i2])) {
                        _addMixOvers(constructor, this._constructors.get(i2), true);
                        break;
                    }
                    i2++;
                }
            }
        }
    }

    protected void _addFactoryMixIns(Class<?> cls) {
        int size = this._creatorMethods.size();
        MemberKey[] memberKeyArr = null;
        for (Method method : cls.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length != 0) {
                if (memberKeyArr == null) {
                    memberKeyArr = new MemberKey[size];
                    for (int i = 0; i < size; i++) {
                        memberKeyArr[i] = new MemberKey(this._creatorMethods.get(i).getAnnotated());
                    }
                }
                MemberKey memberKey = new MemberKey(method);
                int i2 = 0;
                while (true) {
                    if (i2 >= size) {
                        break;
                    }
                    if (memberKey.equals(memberKeyArr[i2])) {
                        _addMixOvers(method, this._creatorMethods.get(i2), true);
                        break;
                    }
                    i2++;
                }
            }
        }
    }

    protected void _addMemberMethods(Class<?> cls, AnnotatedMethodMap annotatedMethodMap, Class<?> cls2, AnnotatedMethodMap annotatedMethodMap2) {
        if (cls2 != null) {
            _addMethodMixIns(cls, annotatedMethodMap, cls2, annotatedMethodMap2);
        }
        if (cls == null) {
            return;
        }
        for (Method method : _findClassMethods(cls)) {
            if (_isIncludableMemberMethod(method)) {
                AnnotatedMethod annotatedMethodFind = annotatedMethodMap.find(method);
                if (annotatedMethodFind == null) {
                    AnnotatedMethod annotatedMethod_constructMethod = _constructMethod(method);
                    annotatedMethodMap.add(annotatedMethod_constructMethod);
                    AnnotatedMethod annotatedMethodRemove = annotatedMethodMap2.remove(method);
                    if (annotatedMethodRemove != null) {
                        _addMixOvers(annotatedMethodRemove.getAnnotated(), annotatedMethod_constructMethod, false);
                    }
                } else {
                    _addMixUnders(method, annotatedMethodFind);
                    if (annotatedMethodFind.getDeclaringClass().isInterface() && !method.getDeclaringClass().isInterface()) {
                        annotatedMethodMap.add(annotatedMethodFind.withMethod(method));
                    }
                }
            }
        }
    }

    protected void _addMethodMixIns(Class<?> cls, AnnotatedMethodMap annotatedMethodMap, Class<?> cls2, AnnotatedMethodMap annotatedMethodMap2) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(cls2);
        ClassUtil.findSuperTypes(cls2, cls, arrayList);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            for (Method method : ((Class) it.next()).getDeclaredMethods()) {
                if (_isIncludableMemberMethod(method)) {
                    AnnotatedMethod annotatedMethodFind = annotatedMethodMap.find(method);
                    if (annotatedMethodFind != null) {
                        _addMixUnders(method, annotatedMethodFind);
                    } else {
                        AnnotatedMethod annotatedMethodFind2 = annotatedMethodMap2.find(method);
                        if (annotatedMethodFind2 != null) {
                            _addMixUnders(method, annotatedMethodFind2);
                        } else {
                            annotatedMethodMap2.add(_constructMethod(method));
                        }
                    }
                }
            }
        }
    }

    protected Map<String, AnnotatedField> _findFields(Class<?> cls, Map<String, AnnotatedField> map) {
        Class<?> clsFindMixInClassFor;
        Class<? super Object> superclass = cls.getSuperclass();
        if (superclass != null) {
            map = _findFields(superclass, map);
            for (Field field : cls.getDeclaredFields()) {
                if (_isIncludableField(field)) {
                    if (map == null) {
                        map = new LinkedHashMap<>();
                    }
                    map.put(field.getName(), _constructField(field));
                }
            }
            ClassIntrospector.MixInResolver mixInResolver = this._mixInResolver;
            if (mixInResolver != null && (clsFindMixInClassFor = mixInResolver.findMixInClassFor(cls)) != null) {
                _addFieldMixIns(superclass, clsFindMixInClassFor, map);
            }
        }
        return map;
    }

    protected void _addFieldMixIns(Class<?> cls, Class<?> cls2, Map<String, AnnotatedField> map) {
        AnnotatedField annotatedField;
        ArrayList arrayList = new ArrayList();
        arrayList.add(cls2);
        ClassUtil.findSuperTypes(cls2, cls, arrayList);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            for (Field field : ((Class) it.next()).getDeclaredFields()) {
                if (_isIncludableField(field) && (annotatedField = map.get(field.getName())) != null) {
                    _addOrOverrideAnnotations(annotatedField, field.getDeclaredAnnotations());
                }
            }
        }
    }

    protected AnnotatedMethod _constructMethod(Method method) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedMethod(method, _emptyAnnotationMap(), null);
        }
        return new AnnotatedMethod(method, _collectRelevantAnnotations(method.getDeclaredAnnotations()), null);
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x006f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected com.fasterxml.jackson.databind.introspect.AnnotatedConstructor _constructConstructor(java.lang.reflect.Constructor<?> r8, boolean r9) {
        /*
            r7 = this;
            com.fasterxml.jackson.databind.AnnotationIntrospector r0 = r7._annotationIntrospector
            if (r0 != 0) goto L17
            com.fasterxml.jackson.databind.introspect.AnnotatedConstructor r9 = new com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
            com.fasterxml.jackson.databind.introspect.AnnotationMap r0 = r7._emptyAnnotationMap()
            java.lang.Class[] r1 = r8.getParameterTypes()
            int r1 = r1.length
            com.fasterxml.jackson.databind.introspect.AnnotationMap[] r1 = r7._emptyAnnotationMaps(r1)
            r9.<init>(r8, r0, r1)
            return r9
        L17:
            r0 = 0
            if (r9 == 0) goto L28
            com.fasterxml.jackson.databind.introspect.AnnotatedConstructor r9 = new com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
            java.lang.annotation.Annotation[] r1 = r8.getDeclaredAnnotations()
            com.fasterxml.jackson.databind.introspect.AnnotationMap r1 = r7._collectRelevantAnnotations(r1)
            r9.<init>(r8, r1, r0)
            return r9
        L28:
            java.lang.annotation.Annotation[][] r9 = r8.getParameterAnnotations()
            java.lang.Class[] r1 = r8.getParameterTypes()
            int r1 = r1.length
            int r2 = r9.length
            if (r1 == r2) goto La7
            java.lang.Class r2 = r8.getDeclaringClass()
            boolean r3 = r2.isEnum()
            r4 = 0
            if (r3 == 0) goto L54
            int r3 = r9.length
            r5 = 2
            int r3 = r3 + r5
            if (r1 != r3) goto L54
            int r0 = r9.length
            int r0 = r0 + r5
            java.lang.annotation.Annotation[][] r0 = new java.lang.annotation.Annotation[r0][]
            int r2 = r9.length
            java.lang.System.arraycopy(r9, r4, r0, r5, r2)
            com.fasterxml.jackson.databind.introspect.AnnotationMap[] r9 = r7._collectRelevantAnnotations(r0)
        L50:
            r6 = r0
            r0 = r9
            r9 = r6
            goto L6c
        L54:
            boolean r2 = r2.isMemberClass()
            if (r2 == 0) goto L6c
            int r2 = r9.length
            r3 = 1
            int r2 = r2 + r3
            if (r1 != r2) goto L6c
            int r0 = r9.length
            int r0 = r0 + r3
            java.lang.annotation.Annotation[][] r0 = new java.lang.annotation.Annotation[r0][]
            int r2 = r9.length
            java.lang.System.arraycopy(r9, r4, r0, r3, r2)
            com.fasterxml.jackson.databind.introspect.AnnotationMap[] r9 = r7._collectRelevantAnnotations(r0)
            goto L50
        L6c:
            if (r0 == 0) goto L6f
            goto Lab
        L6f:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "Internal error: constructor for "
            r2.<init>(r3)
            java.lang.Class r8 = r8.getDeclaringClass()
            java.lang.String r8 = r8.getName()
            java.lang.StringBuilder r8 = r2.append(r8)
            java.lang.String r2 = " has mismatch: "
            java.lang.StringBuilder r8 = r8.append(r2)
            java.lang.StringBuilder r8 = r8.append(r1)
            java.lang.String r1 = " parameters; "
            java.lang.StringBuilder r8 = r8.append(r1)
            int r9 = r9.length
            java.lang.StringBuilder r8 = r8.append(r9)
            java.lang.String r9 = " sets of annotations"
            java.lang.StringBuilder r8 = r8.append(r9)
            java.lang.String r8 = r8.toString()
            r0.<init>(r8)
            throw r0
        La7:
            com.fasterxml.jackson.databind.introspect.AnnotationMap[] r0 = r7._collectRelevantAnnotations(r9)
        Lab:
            com.fasterxml.jackson.databind.introspect.AnnotatedConstructor r9 = new com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
            java.lang.annotation.Annotation[] r1 = r8.getDeclaredAnnotations()
            com.fasterxml.jackson.databind.introspect.AnnotationMap r1 = r7._collectRelevantAnnotations(r1)
            r9.<init>(r8, r1, r0)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.introspect.AnnotatedClass._constructConstructor(java.lang.reflect.Constructor, boolean):com.fasterxml.jackson.databind.introspect.AnnotatedConstructor");
    }

    protected AnnotatedMethod _constructCreatorMethod(Method method) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedMethod(method, _emptyAnnotationMap(), _emptyAnnotationMaps(method.getParameterTypes().length));
        }
        return new AnnotatedMethod(method, _collectRelevantAnnotations(method.getDeclaredAnnotations()), _collectRelevantAnnotations(method.getParameterAnnotations()));
    }

    protected AnnotatedField _constructField(Field field) {
        if (this._annotationIntrospector == null) {
            return new AnnotatedField(field, _emptyAnnotationMap());
        }
        return new AnnotatedField(field, _collectRelevantAnnotations(field.getDeclaredAnnotations()));
    }

    private AnnotationMap _emptyAnnotationMap() {
        return new AnnotationMap();
    }

    private AnnotationMap[] _emptyAnnotationMaps(int i) {
        if (i == 0) {
            return NO_ANNOTATION_MAPS;
        }
        AnnotationMap[] annotationMapArr = new AnnotationMap[i];
        for (int i2 = 0; i2 < i; i2++) {
            annotationMapArr[i2] = _emptyAnnotationMap();
        }
        return annotationMapArr;
    }

    protected boolean _isIncludableMemberMethod(Method method) {
        return (Modifier.isStatic(method.getModifiers()) || method.isSynthetic() || method.isBridge() || method.getParameterTypes().length > 2) ? false : true;
    }

    private boolean _isIncludableField(Field field) {
        if (field.isSynthetic()) {
            return false;
        }
        int modifiers = field.getModifiers();
        return (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) ? false : true;
    }

    protected AnnotationMap[] _collectRelevantAnnotations(Annotation[][] annotationArr) {
        int length = annotationArr.length;
        AnnotationMap[] annotationMapArr = new AnnotationMap[length];
        for (int i = 0; i < length; i++) {
            annotationMapArr[i] = _collectRelevantAnnotations(annotationArr[i]);
        }
        return annotationMapArr;
    }

    protected AnnotationMap _collectRelevantAnnotations(Annotation[] annotationArr) {
        AnnotationMap annotationMap = new AnnotationMap();
        _addAnnotationsIfNotPresent(annotationMap, annotationArr);
        return annotationMap;
    }

    private void _addAnnotationsIfNotPresent(AnnotationMap annotationMap, Annotation[] annotationArr) {
        if (annotationArr != null) {
            LinkedList linkedList = null;
            for (Annotation annotation : annotationArr) {
                if (_isAnnotationBundle(annotation)) {
                    if (linkedList == null) {
                        linkedList = new LinkedList();
                    }
                    linkedList.add(annotation.annotationType().getDeclaredAnnotations());
                } else {
                    annotationMap.addIfNotPresent(annotation);
                }
            }
            if (linkedList != null) {
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    _addAnnotationsIfNotPresent(annotationMap, (Annotation[]) it.next());
                }
            }
        }
    }

    private void _addAnnotationsIfNotPresent(AnnotatedMember annotatedMember, Annotation[] annotationArr) {
        if (annotationArr != null) {
            LinkedList linkedList = null;
            for (Annotation annotation : annotationArr) {
                if (_isAnnotationBundle(annotation)) {
                    if (linkedList == null) {
                        linkedList = new LinkedList();
                    }
                    linkedList.add(annotation.annotationType().getDeclaredAnnotations());
                } else {
                    annotatedMember.addIfNotPresent(annotation);
                }
            }
            if (linkedList != null) {
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    _addAnnotationsIfNotPresent(annotatedMember, (Annotation[]) it.next());
                }
            }
        }
    }

    private void _addOrOverrideAnnotations(AnnotatedMember annotatedMember, Annotation[] annotationArr) {
        if (annotationArr != null) {
            LinkedList linkedList = null;
            for (Annotation annotation : annotationArr) {
                if (_isAnnotationBundle(annotation)) {
                    if (linkedList == null) {
                        linkedList = new LinkedList();
                    }
                    linkedList.add(annotation.annotationType().getDeclaredAnnotations());
                } else {
                    annotatedMember.addOrOverride(annotation);
                }
            }
            if (linkedList != null) {
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    _addOrOverrideAnnotations(annotatedMember, (Annotation[]) it.next());
                }
            }
        }
    }

    protected void _addMixOvers(Constructor<?> constructor, AnnotatedConstructor annotatedConstructor, boolean z) {
        _addOrOverrideAnnotations(annotatedConstructor, constructor.getDeclaredAnnotations());
        if (z) {
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            int length = parameterAnnotations.length;
            for (int i = 0; i < length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    annotatedConstructor.addOrOverrideParam(i, annotation);
                }
            }
        }
    }

    protected void _addMixOvers(Method method, AnnotatedMethod annotatedMethod, boolean z) {
        _addOrOverrideAnnotations(annotatedMethod, method.getDeclaredAnnotations());
        if (z) {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            int length = parameterAnnotations.length;
            for (int i = 0; i < length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    annotatedMethod.addOrOverrideParam(i, annotation);
                }
            }
        }
    }

    protected void _addMixUnders(Method method, AnnotatedMethod annotatedMethod) {
        _addAnnotationsIfNotPresent(annotatedMethod, method.getDeclaredAnnotations());
    }

    private final boolean _isAnnotationBundle(Annotation annotation) {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        return annotationIntrospector != null && annotationIntrospector.isAnnotationBundle(annotation);
    }

    protected Method[] _findClassMethods(Class<?> cls) {
        try {
            return cls.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader == null) {
                throw e;
            }
            try {
                return contextClassLoader.loadClass(cls.getName()).getDeclaredMethods();
            } catch (ClassNotFoundException unused) {
                throw e;
            }
        }
    }

    public String toString() {
        return "[AnnotedClass " + this._class.getName() + "]";
    }
}
