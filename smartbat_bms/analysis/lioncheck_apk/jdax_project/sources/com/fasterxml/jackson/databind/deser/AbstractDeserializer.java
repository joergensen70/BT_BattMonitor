package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class AbstractDeserializer extends JsonDeserializer<Object> implements Serializable {
    private static final long serialVersionUID = -3010349050434697698L;
    protected final boolean _acceptBoolean;
    protected final boolean _acceptDouble;
    protected final boolean _acceptInt;
    protected final boolean _acceptString;
    protected final Map<String, SettableBeanProperty> _backRefProperties;
    protected final JavaType _baseType;
    protected final ObjectIdReader _objectIdReader;

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public boolean isCachable() {
        return true;
    }

    public AbstractDeserializer(BeanDeserializerBuilder beanDeserializerBuilder, BeanDescription beanDescription, Map<String, SettableBeanProperty> map) {
        JavaType type = beanDescription.getType();
        this._baseType = type;
        this._objectIdReader = beanDeserializerBuilder.getObjectIdReader();
        this._backRefProperties = map;
        Class<?> rawClass = type.getRawClass();
        this._acceptString = rawClass.isAssignableFrom(String.class);
        this._acceptBoolean = rawClass == Boolean.TYPE || rawClass.isAssignableFrom(Boolean.class);
        this._acceptInt = rawClass == Integer.TYPE || rawClass.isAssignableFrom(Integer.class);
        this._acceptDouble = rawClass == Double.TYPE || rawClass.isAssignableFrom(Double.class);
    }

    protected AbstractDeserializer(BeanDescription beanDescription) {
        JavaType type = beanDescription.getType();
        this._baseType = type;
        this._objectIdReader = null;
        this._backRefProperties = null;
        Class<?> rawClass = type.getRawClass();
        this._acceptString = rawClass.isAssignableFrom(String.class);
        this._acceptBoolean = rawClass == Boolean.TYPE || rawClass.isAssignableFrom(Boolean.class);
        this._acceptInt = rawClass == Integer.TYPE || rawClass.isAssignableFrom(Integer.class);
        this._acceptDouble = rawClass == Double.TYPE || rawClass.isAssignableFrom(Double.class);
    }

    public static AbstractDeserializer constructForNonPOJO(BeanDescription beanDescription) {
        return new AbstractDeserializer(beanDescription);
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public Class<?> handledType() {
        return this._baseType.getRawClass();
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public ObjectIdReader getObjectIdReader() {
        return this._objectIdReader;
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public SettableBeanProperty findBackReference(String str) {
        Map<String, SettableBeanProperty> map = this._backRefProperties;
        if (map == null) {
            return null;
        }
        return map.get(str);
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public Object deserializeWithType(JsonParser jsonParser, DeserializationContext deserializationContext, TypeDeserializer typeDeserializer) throws IOException {
        JsonToken currentToken;
        if (this._objectIdReader != null && (currentToken = jsonParser.getCurrentToken()) != null && currentToken.isScalarValue()) {
            return _deserializeFromObjectId(jsonParser, deserializationContext);
        }
        Object obj_deserializeIfNatural = _deserializeIfNatural(jsonParser, deserializationContext);
        return obj_deserializeIfNatural != null ? obj_deserializeIfNatural : typeDeserializer.deserializeTypedFromObject(jsonParser, deserializationContext);
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        throw deserializationContext.instantiationException(this._baseType.getRawClass(), "abstract types either need to be mapped to concrete types, have custom deserializer, or be instantiated with additional type information");
    }

    protected Object _deserializeIfNatural(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (!currentToken.isScalarValue()) {
            return null;
        }
        if (currentToken == JsonToken.VALUE_STRING) {
            if (this._acceptString) {
                return jsonParser.getText();
            }
            return null;
        }
        if (currentToken == JsonToken.VALUE_NUMBER_INT) {
            if (this._acceptInt) {
                return Integer.valueOf(jsonParser.getIntValue());
            }
            return null;
        }
        if (currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
            if (this._acceptDouble) {
                return Double.valueOf(jsonParser.getDoubleValue());
            }
            return null;
        }
        if (currentToken == JsonToken.VALUE_TRUE) {
            if (this._acceptBoolean) {
                return Boolean.TRUE;
            }
            return null;
        }
        if (currentToken == JsonToken.VALUE_FALSE && this._acceptBoolean) {
            return Boolean.FALSE;
        }
        return null;
    }

    protected Object _deserializeFromObjectId(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Object objectReference = this._objectIdReader.readObjectReference(jsonParser, deserializationContext);
        ReadableObjectId readableObjectIdFindObjectId = deserializationContext.findObjectId(objectReference, this._objectIdReader.generator, this._objectIdReader.resolver);
        Object objResolve = readableObjectIdFindObjectId.resolve();
        if (objResolve != null) {
            return objResolve;
        }
        throw new UnresolvedForwardReference("Could not resolve Object Id [" + objectReference + "] -- unresolved forward-reference?", jsonParser.getCurrentLocation(), readableObjectIdFindObjectId);
    }
}
