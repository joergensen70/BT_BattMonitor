package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/* JADX INFO: loaded from: classes.dex */
public class POJOPropertiesCollector {
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final AnnotatedClass _classDef;
    protected final MapperConfig<?> _config;
    protected final boolean _forSerialization;
    protected HashSet<String> _ignoredPropertyNames;
    protected LinkedHashMap<Object, AnnotatedMember> _injectables;
    protected final String _mutatorPrefix;
    protected final JavaType _type;
    protected final VisibilityChecker<?> _visibilityChecker;
    protected final LinkedHashMap<String, POJOPropertyBuilder> _properties = new LinkedHashMap<>();
    protected LinkedList<POJOPropertyBuilder> _creatorProperties = null;
    protected LinkedList<AnnotatedMember> _anyGetters = null;
    protected LinkedList<AnnotatedMethod> _anySetters = null;
    protected LinkedList<AnnotatedMethod> _jsonValueGetters = null;

    protected POJOPropertiesCollector(MapperConfig<?> mapperConfig, boolean z, JavaType javaType, AnnotatedClass annotatedClass, String str) {
        this._config = mapperConfig;
        this._forSerialization = z;
        this._type = javaType;
        this._classDef = annotatedClass;
        this._mutatorPrefix = str == null ? "set" : str;
        AnnotationIntrospector annotationIntrospector = mapperConfig.isAnnotationProcessingEnabled() ? mapperConfig.getAnnotationIntrospector() : null;
        this._annotationIntrospector = annotationIntrospector;
        if (annotationIntrospector == null) {
            this._visibilityChecker = mapperConfig.getDefaultVisibilityChecker();
        } else {
            this._visibilityChecker = annotationIntrospector.findAutoDetectVisibility(annotatedClass, mapperConfig.getDefaultVisibilityChecker());
        }
    }

    public MapperConfig<?> getConfig() {
        return this._config;
    }

    public JavaType getType() {
        return this._type;
    }

    public AnnotatedClass getClassDef() {
        return this._classDef;
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        return this._annotationIntrospector;
    }

    public List<BeanPropertyDefinition> getProperties() {
        return new ArrayList(this._properties.values());
    }

    public Map<Object, AnnotatedMember> getInjectables() {
        return this._injectables;
    }

    public AnnotatedMethod getJsonValueMethod() {
        LinkedList<AnnotatedMethod> linkedList = this._jsonValueGetters;
        if (linkedList == null) {
            return null;
        }
        if (linkedList.size() > 1) {
            reportProblem("Multiple value properties defined (" + this._jsonValueGetters.get(0) + " vs " + this._jsonValueGetters.get(1) + ")");
        }
        return this._jsonValueGetters.get(0);
    }

    public AnnotatedMember getAnyGetter() {
        LinkedList<AnnotatedMember> linkedList = this._anyGetters;
        if (linkedList == null) {
            return null;
        }
        if (linkedList.size() > 1) {
            reportProblem("Multiple 'any-getters' defined (" + this._anyGetters.get(0) + " vs " + this._anyGetters.get(1) + ")");
        }
        return this._anyGetters.getFirst();
    }

    public AnnotatedMethod getAnySetterMethod() {
        LinkedList<AnnotatedMethod> linkedList = this._anySetters;
        if (linkedList == null) {
            return null;
        }
        if (linkedList.size() > 1) {
            reportProblem("Multiple 'any-setters' defined (" + this._anySetters.get(0) + " vs " + this._anySetters.get(1) + ")");
        }
        return this._anySetters.getFirst();
    }

    public Set<String> getIgnoredPropertyNames() {
        return this._ignoredPropertyNames;
    }

    public ObjectIdInfo getObjectIdInfo() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        if (annotationIntrospector == null) {
            return null;
        }
        ObjectIdInfo objectIdInfoFindObjectIdInfo = annotationIntrospector.findObjectIdInfo(this._classDef);
        return objectIdInfoFindObjectIdInfo != null ? this._annotationIntrospector.findObjectReferenceInfo(this._classDef, objectIdInfoFindObjectIdInfo) : objectIdInfoFindObjectIdInfo;
    }

    public Class<?> findPOJOBuilderClass() {
        return this._annotationIntrospector.findPOJOBuilder(this._classDef);
    }

    protected Map<String, POJOPropertyBuilder> getPropertyMap() {
        return this._properties;
    }

    public POJOPropertiesCollector collect() {
        this._properties.clear();
        _addFields();
        _addMethods();
        _addCreators();
        _addInjectables();
        _removeUnwantedProperties();
        _renameProperties();
        PropertyNamingStrategy propertyNamingStrategy_findNamingStrategy = _findNamingStrategy();
        if (propertyNamingStrategy_findNamingStrategy != null) {
            _renameUsing(propertyNamingStrategy_findNamingStrategy);
        }
        Iterator<POJOPropertyBuilder> it = this._properties.values().iterator();
        while (it.hasNext()) {
            it.next().trimByVisibility();
        }
        Iterator<POJOPropertyBuilder> it2 = this._properties.values().iterator();
        while (it2.hasNext()) {
            it2.next().mergeAnnotations(this._forSerialization);
        }
        if (this._config.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)) {
            _renameWithWrappers();
        }
        _sortProperties();
        return this;
    }

    protected void _sortProperties() {
        boolean zBooleanValue;
        Map linkedHashMap;
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        Boolean boolFindSerializationSortAlphabetically = annotationIntrospector == null ? null : annotationIntrospector.findSerializationSortAlphabetically((Annotated) this._classDef);
        if (boolFindSerializationSortAlphabetically == null) {
            zBooleanValue = this._config.shouldSortPropertiesAlphabetically();
        } else {
            zBooleanValue = boolFindSerializationSortAlphabetically.booleanValue();
        }
        String[] strArrFindSerializationPropertyOrder = annotationIntrospector != null ? annotationIntrospector.findSerializationPropertyOrder(this._classDef) : null;
        if (!zBooleanValue && this._creatorProperties == null && strArrFindSerializationPropertyOrder == null) {
            return;
        }
        int size = this._properties.size();
        if (zBooleanValue) {
            linkedHashMap = new TreeMap();
        } else {
            linkedHashMap = new LinkedHashMap(size + size);
        }
        for (POJOPropertyBuilder pOJOPropertyBuilder : this._properties.values()) {
            linkedHashMap.put(pOJOPropertyBuilder.getName(), pOJOPropertyBuilder);
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap(size + size);
        if (strArrFindSerializationPropertyOrder != null) {
            for (String name : strArrFindSerializationPropertyOrder) {
                POJOPropertyBuilder pOJOPropertyBuilder2 = (POJOPropertyBuilder) linkedHashMap.get(name);
                if (pOJOPropertyBuilder2 == null) {
                    Iterator<POJOPropertyBuilder> it = this._properties.values().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        POJOPropertyBuilder next = it.next();
                        if (name.equals(next.getInternalName())) {
                            name = next.getName();
                            pOJOPropertyBuilder2 = next;
                            break;
                        }
                    }
                }
                if (pOJOPropertyBuilder2 != null) {
                    linkedHashMap2.put(name, pOJOPropertyBuilder2);
                }
            }
        }
        Collection<POJOPropertyBuilder> collectionValues = this._creatorProperties;
        if (collectionValues != null) {
            if (zBooleanValue) {
                TreeMap treeMap = new TreeMap();
                for (POJOPropertyBuilder pOJOPropertyBuilder3 : this._creatorProperties) {
                    treeMap.put(pOJOPropertyBuilder3.getName(), pOJOPropertyBuilder3);
                }
                collectionValues = treeMap.values();
            }
            for (POJOPropertyBuilder pOJOPropertyBuilder4 : collectionValues) {
                linkedHashMap2.put(pOJOPropertyBuilder4.getName(), pOJOPropertyBuilder4);
            }
        }
        linkedHashMap2.putAll(linkedHashMap);
        this._properties.clear();
        this._properties.putAll(linkedHashMap2);
    }

    protected void _addFields() {
        boolean z;
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        boolean z2 = (this._forSerialization || this._config.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)) ? false : true;
        for (AnnotatedField annotatedField : this._classDef.fields()) {
            PropertyName propertyName_propNameFromSimple = null;
            String strFindImplicitPropertyName = annotationIntrospector == null ? null : annotationIntrospector.findImplicitPropertyName(annotatedField);
            if (strFindImplicitPropertyName == null) {
                strFindImplicitPropertyName = annotatedField.getName();
            }
            if (annotationIntrospector != null) {
                if (this._forSerialization) {
                    propertyName_propNameFromSimple = annotationIntrospector.findNameForSerialization(annotatedField);
                } else {
                    propertyName_propNameFromSimple = annotationIntrospector.findNameForDeserialization(annotatedField);
                }
            }
            boolean z3 = propertyName_propNameFromSimple != null;
            if (z3 && propertyName_propNameFromSimple.isEmpty()) {
                propertyName_propNameFromSimple = _propNameFromSimple(strFindImplicitPropertyName);
                z = false;
            } else {
                z = z3;
            }
            PropertyName propertyName = propertyName_propNameFromSimple;
            boolean zIsFieldVisible = propertyName != null;
            if (!zIsFieldVisible) {
                zIsFieldVisible = this._visibilityChecker.isFieldVisible(annotatedField);
            }
            boolean z4 = zIsFieldVisible;
            boolean z5 = annotationIntrospector != null && annotationIntrospector.hasIgnoreMarker(annotatedField);
            if (!z2 || propertyName != null || z5 || !Modifier.isFinal(annotatedField.getModifiers())) {
                _property(strFindImplicitPropertyName).addField(annotatedField, propertyName, z, z4, z5);
            }
        }
    }

    protected void _addCreators() {
        if (this._annotationIntrospector != null) {
            Iterator<AnnotatedConstructor> it = this._classDef.getConstructors().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                AnnotatedConstructor next = it.next();
                if (this._creatorProperties == null) {
                    this._creatorProperties = new LinkedList<>();
                }
                int parameterCount = next.getParameterCount();
                for (int i = 0; i < parameterCount; i++) {
                    _addCreatorParam(next.getParameter(i));
                }
            }
            for (AnnotatedMethod annotatedMethod : this._classDef.getStaticMethods()) {
                if (this._creatorProperties == null) {
                    this._creatorProperties = new LinkedList<>();
                }
                int parameterCount2 = annotatedMethod.getParameterCount();
                for (int i2 = 0; i2 < parameterCount2; i2++) {
                    _addCreatorParam(annotatedMethod.getParameter(i2));
                }
            }
        }
    }

    protected void _addCreatorParam(AnnotatedParameter annotatedParameter) {
        String strFindImplicitPropertyName = this._annotationIntrospector.findImplicitPropertyName(annotatedParameter);
        if (strFindImplicitPropertyName == null) {
            strFindImplicitPropertyName = "";
        }
        PropertyName propertyNameFindNameForDeserialization = this._annotationIntrospector.findNameForDeserialization(annotatedParameter);
        boolean z = (propertyNameFindNameForDeserialization == null || propertyNameFindNameForDeserialization.isEmpty()) ? false : true;
        if (!z) {
            if (strFindImplicitPropertyName.isEmpty()) {
                return;
            } else {
                propertyNameFindNameForDeserialization = new PropertyName(strFindImplicitPropertyName);
            }
        }
        PropertyName propertyName = propertyNameFindNameForDeserialization;
        POJOPropertyBuilder pOJOPropertyBuilder_property = z ? _property(propertyName) : _property(strFindImplicitPropertyName);
        pOJOPropertyBuilder_property.addCtor(annotatedParameter, propertyName, z, true, false);
        this._creatorProperties.add(pOJOPropertyBuilder_property);
    }

    protected void _addMethods() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        for (AnnotatedMethod annotatedMethod : this._classDef.memberMethods()) {
            int parameterCount = annotatedMethod.getParameterCount();
            if (parameterCount == 0) {
                _addGetterMethod(annotatedMethod, annotationIntrospector);
            } else if (parameterCount == 1) {
                _addSetterMethod(annotatedMethod, annotationIntrospector);
            } else if (parameterCount == 2 && annotationIntrospector != null && annotationIntrospector.hasAnySetterAnnotation(annotatedMethod)) {
                if (this._anySetters == null) {
                    this._anySetters = new LinkedList<>();
                }
                this._anySetters.add(annotatedMethod);
            }
        }
    }

    protected void _addGetterMethod(AnnotatedMethod annotatedMethod, AnnotationIntrospector annotationIntrospector) {
        String strFindImplicitPropertyName;
        if (annotatedMethod.hasReturnType()) {
            if (annotationIntrospector != null) {
                if (annotationIntrospector.hasAnyGetterAnnotation(annotatedMethod)) {
                    if (this._anyGetters == null) {
                        this._anyGetters = new LinkedList<>();
                    }
                    this._anyGetters.add(annotatedMethod);
                    return;
                } else if (annotationIntrospector.hasAsValueAnnotation(annotatedMethod)) {
                    if (this._jsonValueGetters == null) {
                        this._jsonValueGetters = new LinkedList<>();
                    }
                    this._jsonValueGetters.add(annotatedMethod);
                    return;
                }
            }
            PropertyName propertyNameFindNameForSerialization = annotationIntrospector == null ? null : annotationIntrospector.findNameForSerialization(annotatedMethod);
            boolean zIsGetterVisible = true;
            boolean z = propertyNameFindNameForSerialization != null;
            if (!z) {
                strFindImplicitPropertyName = annotationIntrospector != null ? annotationIntrospector.findImplicitPropertyName(annotatedMethod) : null;
                if (strFindImplicitPropertyName == null) {
                    strFindImplicitPropertyName = BeanUtil.okNameForRegularGetter(annotatedMethod, annotatedMethod.getName());
                }
                if (strFindImplicitPropertyName == null) {
                    strFindImplicitPropertyName = BeanUtil.okNameForIsGetter(annotatedMethod, annotatedMethod.getName());
                    if (strFindImplicitPropertyName == null) {
                        return;
                    } else {
                        zIsGetterVisible = this._visibilityChecker.isIsGetterVisible(annotatedMethod);
                    }
                } else {
                    zIsGetterVisible = this._visibilityChecker.isGetterVisible(annotatedMethod);
                }
            } else {
                strFindImplicitPropertyName = annotationIntrospector != null ? annotationIntrospector.findImplicitPropertyName(annotatedMethod) : null;
                if (strFindImplicitPropertyName == null) {
                    strFindImplicitPropertyName = BeanUtil.okNameForGetter(annotatedMethod);
                }
                if (strFindImplicitPropertyName == null) {
                    strFindImplicitPropertyName = annotatedMethod.getName();
                }
                if (propertyNameFindNameForSerialization.isEmpty()) {
                    propertyNameFindNameForSerialization = _propNameFromSimple(strFindImplicitPropertyName);
                    z = false;
                }
            }
            _property(strFindImplicitPropertyName).addGetter(annotatedMethod, propertyNameFindNameForSerialization, z, zIsGetterVisible, annotationIntrospector != null ? annotationIntrospector.hasIgnoreMarker(annotatedMethod) : false);
        }
    }

    protected void _addSetterMethod(AnnotatedMethod annotatedMethod, AnnotationIntrospector annotationIntrospector) {
        String strFindImplicitPropertyName;
        PropertyName propertyNameFindNameForDeserialization = annotationIntrospector == null ? null : annotationIntrospector.findNameForDeserialization(annotatedMethod);
        boolean zIsSetterVisible = true;
        boolean z = propertyNameFindNameForDeserialization != null;
        if (!z) {
            strFindImplicitPropertyName = annotationIntrospector != null ? annotationIntrospector.findImplicitPropertyName(annotatedMethod) : null;
            if (strFindImplicitPropertyName == null) {
                strFindImplicitPropertyName = BeanUtil.okNameForMutator(annotatedMethod, this._mutatorPrefix);
            }
            if (strFindImplicitPropertyName == null) {
                return;
            } else {
                zIsSetterVisible = this._visibilityChecker.isSetterVisible(annotatedMethod);
            }
        } else {
            strFindImplicitPropertyName = annotationIntrospector != null ? annotationIntrospector.findImplicitPropertyName(annotatedMethod) : null;
            if (strFindImplicitPropertyName == null) {
                strFindImplicitPropertyName = BeanUtil.okNameForMutator(annotatedMethod, this._mutatorPrefix);
            }
            if (strFindImplicitPropertyName == null) {
                strFindImplicitPropertyName = annotatedMethod.getName();
            }
            if (propertyNameFindNameForDeserialization.isEmpty()) {
                propertyNameFindNameForDeserialization = _propNameFromSimple(strFindImplicitPropertyName);
                z = false;
            }
        }
        _property(strFindImplicitPropertyName).addSetter(annotatedMethod, propertyNameFindNameForDeserialization, z, zIsSetterVisible, annotationIntrospector != null ? annotationIntrospector.hasIgnoreMarker(annotatedMethod) : false);
    }

    protected void _addInjectables() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        if (annotationIntrospector == null) {
            return;
        }
        for (AnnotatedMember annotatedMember : this._classDef.fields()) {
            _doAddInjectable(annotationIntrospector.findInjectableValueId(annotatedMember), annotatedMember);
        }
        for (AnnotatedMethod annotatedMethod : this._classDef.memberMethods()) {
            if (annotatedMethod.getParameterCount() == 1) {
                _doAddInjectable(annotationIntrospector.findInjectableValueId(annotatedMethod), annotatedMethod);
            }
        }
    }

    protected void _doAddInjectable(Object obj, AnnotatedMember annotatedMember) {
        if (obj == null) {
            return;
        }
        if (this._injectables == null) {
            this._injectables = new LinkedHashMap<>();
        }
        if (this._injectables.put(obj, annotatedMember) == null) {
            return;
        }
        throw new IllegalArgumentException("Duplicate injectable value with id '" + String.valueOf(obj) + "' (of type " + obj.getClass().getName() + ")");
    }

    private PropertyName _propNameFromSimple(String str) {
        return PropertyName.construct(str, null);
    }

    protected void _removeUnwantedProperties() {
        Iterator<Map.Entry<String, POJOPropertyBuilder>> it = this._properties.entrySet().iterator();
        boolean z = !this._config.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS);
        while (it.hasNext()) {
            POJOPropertyBuilder value = it.next().getValue();
            if (!value.anyVisible()) {
                it.remove();
            } else {
                if (value.anyIgnorals()) {
                    if (!value.isExplicitlyIncluded()) {
                        it.remove();
                        _addIgnored(value.getName());
                    } else {
                        value.removeIgnored();
                        if (!this._forSerialization && !value.couldDeserialize()) {
                            _addIgnored(value.getName());
                        }
                    }
                }
                value.removeNonVisible(z);
            }
        }
    }

    private void _addIgnored(String str) {
        if (this._forSerialization) {
            return;
        }
        if (this._ignoredPropertyNames == null) {
            this._ignoredPropertyNames = new HashSet<>();
        }
        this._ignoredPropertyNames.add(str);
    }

    protected void _renameProperties() {
        Iterator<Map.Entry<String, POJOPropertyBuilder>> it = this._properties.entrySet().iterator();
        LinkedList<POJOPropertyBuilder> linkedList = null;
        while (it.hasNext()) {
            POJOPropertyBuilder value = it.next().getValue();
            Set<PropertyName> setFindExplicitNames = value.findExplicitNames();
            if (!setFindExplicitNames.isEmpty()) {
                it.remove();
                if (linkedList == null) {
                    linkedList = new LinkedList();
                }
                if (setFindExplicitNames.size() == 1) {
                    linkedList.add(value.withName(setFindExplicitNames.iterator().next()));
                } else {
                    linkedList.addAll(value.explode(setFindExplicitNames));
                }
            }
        }
        if (linkedList != null) {
            for (POJOPropertyBuilder pOJOPropertyBuilder : linkedList) {
                String name = pOJOPropertyBuilder.getName();
                POJOPropertyBuilder pOJOPropertyBuilder2 = this._properties.get(name);
                if (pOJOPropertyBuilder2 == null) {
                    this._properties.put(name, pOJOPropertyBuilder);
                } else {
                    pOJOPropertyBuilder2.addAll(pOJOPropertyBuilder);
                }
                _updateCreatorProperty(pOJOPropertyBuilder, this._creatorProperties);
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x00ab  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void _renameUsing(com.fasterxml.jackson.databind.PropertyNamingStrategy r9) {
        /*
            Method dump skipped, instruction units count: 218
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector._renameUsing(com.fasterxml.jackson.databind.PropertyNamingStrategy):void");
    }

    protected void _renameWithWrappers() {
        PropertyName propertyNameFindWrapperName;
        Iterator<Map.Entry<String, POJOPropertyBuilder>> it = this._properties.entrySet().iterator();
        LinkedList<POJOPropertyBuilder> linkedList = null;
        while (it.hasNext()) {
            POJOPropertyBuilder value = it.next().getValue();
            AnnotatedMember primaryMember = value.getPrimaryMember();
            if (primaryMember != null && (propertyNameFindWrapperName = this._annotationIntrospector.findWrapperName(primaryMember)) != null && propertyNameFindWrapperName.hasSimpleName() && !propertyNameFindWrapperName.equals(value.getFullName())) {
                if (linkedList == null) {
                    linkedList = new LinkedList();
                }
                linkedList.add(value.withName(propertyNameFindWrapperName));
                it.remove();
            }
        }
        if (linkedList != null) {
            for (POJOPropertyBuilder pOJOPropertyBuilder : linkedList) {
                String name = pOJOPropertyBuilder.getName();
                POJOPropertyBuilder pOJOPropertyBuilder2 = this._properties.get(name);
                if (pOJOPropertyBuilder2 == null) {
                    this._properties.put(name, pOJOPropertyBuilder);
                } else {
                    pOJOPropertyBuilder2.addAll(pOJOPropertyBuilder);
                }
            }
        }
    }

    protected void reportProblem(String str) {
        throw new IllegalArgumentException("Problem with definition of " + this._classDef + ": " + str);
    }

    protected POJOPropertyBuilder _property(PropertyName propertyName) {
        return _property(propertyName.getSimpleName());
    }

    protected POJOPropertyBuilder _property(String str) {
        POJOPropertyBuilder pOJOPropertyBuilder = this._properties.get(str);
        if (pOJOPropertyBuilder != null) {
            return pOJOPropertyBuilder;
        }
        POJOPropertyBuilder pOJOPropertyBuilder2 = new POJOPropertyBuilder(new PropertyName(str), this._annotationIntrospector, this._forSerialization);
        this._properties.put(str, pOJOPropertyBuilder2);
        return pOJOPropertyBuilder2;
    }

    private PropertyNamingStrategy _findNamingStrategy() {
        PropertyNamingStrategy propertyNamingStrategyNamingStrategyInstance;
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        Object objFindNamingStrategy = annotationIntrospector == null ? null : annotationIntrospector.findNamingStrategy(this._classDef);
        if (objFindNamingStrategy == null) {
            return this._config.getPropertyNamingStrategy();
        }
        if (objFindNamingStrategy instanceof PropertyNamingStrategy) {
            return (PropertyNamingStrategy) objFindNamingStrategy;
        }
        if (!(objFindNamingStrategy instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned PropertyNamingStrategy definition of type " + objFindNamingStrategy.getClass().getName() + "; expected type PropertyNamingStrategy or Class<PropertyNamingStrategy> instead");
        }
        Class<?> cls = (Class) objFindNamingStrategy;
        if (!PropertyNamingStrategy.class.isAssignableFrom(cls)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<PropertyNamingStrategy>");
        }
        HandlerInstantiator handlerInstantiator = this._config.getHandlerInstantiator();
        return (handlerInstantiator == null || (propertyNamingStrategyNamingStrategyInstance = handlerInstantiator.namingStrategyInstance(this._config, this._classDef, cls)) == null) ? (PropertyNamingStrategy) ClassUtil.createInstance(cls, this._config.canOverrideAccessModifiers()) : propertyNamingStrategyNamingStrategyInstance;
    }

    protected void _updateCreatorProperty(POJOPropertyBuilder pOJOPropertyBuilder, List<POJOPropertyBuilder> list) {
        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (list.get(i).getInternalName().equals(pOJOPropertyBuilder.getInternalName())) {
                    list.set(i, pOJOPropertyBuilder);
                    return;
                }
            }
        }
    }
}
