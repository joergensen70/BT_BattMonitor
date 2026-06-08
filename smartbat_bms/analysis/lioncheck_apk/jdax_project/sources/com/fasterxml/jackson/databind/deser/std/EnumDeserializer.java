package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.EnumResolver;
import java.io.IOException;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: classes.dex */
public class EnumDeserializer extends StdScalarDeserializer<Enum<?>> {
    private static final long serialVersionUID = -5893263645879532318L;
    protected final EnumResolver<?> _resolver;

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public boolean isCachable() {
        return true;
    }

    public EnumDeserializer(EnumResolver<?> enumResolver) {
        super((Class<?>) Enum.class);
        this._resolver = enumResolver;
    }

    public static JsonDeserializer<?> deserializerForCreator(DeserializationConfig deserializationConfig, Class<?> cls, AnnotatedMethod annotatedMethod) {
        Class cls2;
        Class<?> rawParameterType = annotatedMethod.getRawParameterType(0);
        if (rawParameterType == String.class) {
            cls2 = null;
        } else if (rawParameterType == Integer.TYPE || rawParameterType == Integer.class) {
            cls2 = Integer.class;
        } else if (rawParameterType == Long.TYPE || rawParameterType == Long.class) {
            cls2 = Long.class;
        } else {
            throw new IllegalArgumentException("Parameter #0 type for factory method (" + annotatedMethod + ") not suitable, must be java.lang.String or int/Integer/long/Long");
        }
        if (deserializationConfig.canOverrideAccessModifiers()) {
            ClassUtil.checkAndFixAccess(annotatedMethod.getMember());
        }
        return new FactoryBasedDeserializer(cls, annotatedMethod, cls2);
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public Enum<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken == JsonToken.VALUE_STRING || currentToken == JsonToken.FIELD_NAME) {
            String text = jsonParser.getText();
            Enum<?> enumFindEnum = this._resolver.findEnum(text);
            return enumFindEnum == null ? _deserializeAltString(jsonParser, deserializationContext, text) : enumFindEnum;
        }
        if (currentToken == JsonToken.VALUE_NUMBER_INT) {
            if (deserializationContext.isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)) {
                throw deserializationContext.mappingException("Not allowed to deserialize Enum value out of JSON number (disable DeserializationConfig.DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS to allow)");
            }
            int intValue = jsonParser.getIntValue();
            Enum<?> r0 = this._resolver.getEnum(intValue);
            if (r0 != null || deserializationContext.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
                return r0;
            }
            throw deserializationContext.weirdNumberException(Integer.valueOf(intValue), this._resolver.getEnumClass(), "index value outside legal index range [0.." + this._resolver.lastValidIndex() + "]");
        }
        return _deserializeOther(jsonParser, deserializationContext);
    }

    private final Enum<?> _deserializeAltString(JsonParser jsonParser, DeserializationContext deserializationContext, String str) throws IOException {
        String strTrim = str.trim();
        if (strTrim.length() == 0) {
            if (deserializationContext.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                return null;
            }
        } else {
            char cCharAt = strTrim.charAt(0);
            if (cCharAt >= '0' && cCharAt <= '9') {
                try {
                    Enum<?> r5 = this._resolver.getEnum(Integer.parseInt(strTrim));
                    if (r5 != null) {
                        return r5;
                    }
                } catch (NumberFormatException unused) {
                }
            }
        }
        if (deserializationContext.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            return null;
        }
        throw deserializationContext.weirdStringException(strTrim, this._resolver.getEnumClass(), "value not one of declared Enum instance names: " + this._resolver.getEnums());
    }

    private final Enum<?> _deserializeOther(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY && deserializationContext.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            jsonParser.nextToken();
            Enum<?> enumDeserialize = deserialize(jsonParser, deserializationContext);
            if (jsonParser.nextToken() == JsonToken.END_ARRAY) {
                return enumDeserialize;
            }
            throw deserializationContext.wrongTokenException(jsonParser, JsonToken.END_ARRAY, "Attempted to unwrap single value array for single '" + this._resolver.getEnumClass().getName() + "' value but there was more than a single value in the array");
        }
        throw deserializationContext.mappingException(this._resolver.getEnumClass());
    }

    protected static class FactoryBasedDeserializer extends StdScalarDeserializer<Object> {
        private static final long serialVersionUID = -7775129435872564122L;
        protected final Class<?> _enumClass;
        protected final Method _factory;
        protected final Class<?> _inputType;

        public FactoryBasedDeserializer(Class<?> cls, AnnotatedMethod annotatedMethod, Class<?> cls2) {
            super((Class<?>) Enum.class);
            this._enumClass = cls;
            this._factory = annotatedMethod.getAnnotated();
            this._inputType = cls2;
        }

        @Override // com.fasterxml.jackson.databind.JsonDeserializer
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            Object objValueOf;
            Class<?> cls = this._inputType;
            if (cls == null) {
                objValueOf = jsonParser.getText();
            } else if (cls == Integer.class) {
                objValueOf = Integer.valueOf(jsonParser.getValueAsInt());
            } else if (cls == Long.class) {
                objValueOf = Long.valueOf(jsonParser.getValueAsLong());
            } else {
                throw deserializationContext.mappingException(this._enumClass);
            }
            try {
                return this._factory.invoke(this._enumClass, objValueOf);
            } catch (Exception e) {
                Throwable rootCause = ClassUtil.getRootCause(e);
                if (rootCause instanceof IOException) {
                    throw ((IOException) rootCause);
                }
                throw deserializationContext.instantiationException(this._enumClass, rootCause);
            }
        }
    }
}
