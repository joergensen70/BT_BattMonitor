package com.fasterxml.jackson.databind.util;

import android.support.v7.widget.helper.ItemTouchHelper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.ClassKey;
import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class RootNameLookup implements Serializable {
    private static final long serialVersionUID = 1;
    protected transient LRUMap<ClassKey, PropertyName> _rootNames = new LRUMap<>(20, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);

    public PropertyName findRootName(JavaType javaType, MapperConfig<?> mapperConfig) {
        return findRootName(javaType.getRawClass(), mapperConfig);
    }

    public PropertyName findRootName(Class<?> cls, MapperConfig<?> mapperConfig) {
        ClassKey classKey = new ClassKey(cls);
        PropertyName propertyName = this._rootNames.get(classKey);
        if (propertyName != null) {
            return propertyName;
        }
        PropertyName propertyNameFindRootName = mapperConfig.getAnnotationIntrospector().findRootName(mapperConfig.introspectClassAnnotations(cls).getClassInfo());
        if (propertyNameFindRootName == null || !propertyNameFindRootName.hasSimpleName()) {
            propertyNameFindRootName = new PropertyName(cls.getSimpleName());
        }
        this._rootNames.put(classKey, propertyNameFindRootName);
        return propertyNameFindRootName;
    }

    protected Object readResolve() {
        return new RootNameLookup();
    }
}
