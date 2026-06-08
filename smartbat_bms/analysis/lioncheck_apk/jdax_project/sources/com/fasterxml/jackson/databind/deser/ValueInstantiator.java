package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public abstract class ValueInstantiator {
    public boolean canCreateFromBoolean() {
        return false;
    }

    public boolean canCreateFromDouble() {
        return false;
    }

    public boolean canCreateFromInt() {
        return false;
    }

    public boolean canCreateFromLong() {
        return false;
    }

    public boolean canCreateFromObjectWith() {
        return false;
    }

    public boolean canCreateFromString() {
        return false;
    }

    public boolean canCreateUsingDelegate() {
        return false;
    }

    public AnnotatedWithParams getDefaultCreator() {
        return null;
    }

    public AnnotatedWithParams getDelegateCreator() {
        return null;
    }

    public JavaType getDelegateType(DeserializationConfig deserializationConfig) {
        return null;
    }

    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig deserializationConfig) {
        return null;
    }

    public AnnotatedParameter getIncompleteParameter() {
        return null;
    }

    public abstract String getValueTypeDesc();

    public AnnotatedWithParams getWithArgsCreator() {
        return null;
    }

    public boolean canInstantiate() {
        return canCreateUsingDefault() || canCreateUsingDelegate() || canCreateFromObjectWith() || canCreateFromString() || canCreateFromInt() || canCreateFromLong() || canCreateFromDouble() || canCreateFromBoolean();
    }

    public boolean canCreateUsingDefault() {
        return getDefaultCreator() != null;
    }

    public Object createUsingDefault(DeserializationContext deserializationContext) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + "; no default creator found");
    }

    public Object createFromObjectWith(DeserializationContext deserializationContext, Object[] objArr) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " with arguments");
    }

    public Object createUsingDelegate(DeserializationContext deserializationContext, Object obj) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " using delegate");
    }

    public Object createFromString(DeserializationContext deserializationContext, String str) throws IOException {
        return _createFromStringFallbacks(deserializationContext, str);
    }

    public Object createFromInt(DeserializationContext deserializationContext, int i) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " from Integer number (" + i + ", int)");
    }

    public Object createFromLong(DeserializationContext deserializationContext, long j) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " from Integer number (" + j + ", long)");
    }

    public Object createFromDouble(DeserializationContext deserializationContext, double d) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " from Floating-point number (" + d + ", double)");
    }

    public Object createFromBoolean(DeserializationContext deserializationContext, boolean z) throws IOException {
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " from Boolean value (" + z + ")");
    }

    protected Object _createFromStringFallbacks(DeserializationContext deserializationContext, String str) throws IOException {
        if (canCreateFromBoolean()) {
            String strTrim = str.trim();
            if ("true".equals(strTrim)) {
                return createFromBoolean(deserializationContext, true);
            }
            if ("false".equals(strTrim)) {
                return createFromBoolean(deserializationContext, false);
            }
        }
        if (str.length() == 0 && deserializationContext.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
            return null;
        }
        throw deserializationContext.mappingException("Can not instantiate value of type " + getValueTypeDesc() + " from String value ('" + str + "'); no single-String constructor/factory method");
    }
}
