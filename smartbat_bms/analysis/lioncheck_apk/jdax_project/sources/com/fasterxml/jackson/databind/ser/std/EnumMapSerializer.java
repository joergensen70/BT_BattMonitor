package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.EnumValues;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
@JacksonStdImpl
@Deprecated
public class EnumMapSerializer extends ContainerSerializer<EnumMap<? extends Enum<?>, ?>> implements ContextualSerializer {
    protected final EnumValues _keyEnums;
    protected final BeanProperty _property;
    protected final boolean _staticTyping;
    protected final JsonSerializer<Object> _valueSerializer;
    protected final JavaType _valueType;
    protected final TypeSerializer _valueTypeSerializer;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public EnumMapSerializer(JavaType javaType, boolean z, EnumValues enumValues, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer) {
        super(EnumMap.class, false);
        boolean z2 = false;
        this._property = null;
        if (z || (javaType != null && javaType.isFinal())) {
            z2 = true;
        }
        this._staticTyping = z2;
        this._valueType = javaType;
        this._keyEnums = enumValues;
        this._valueTypeSerializer = typeSerializer;
        this._valueSerializer = jsonSerializer;
    }

    public EnumMapSerializer(EnumMapSerializer enumMapSerializer, BeanProperty beanProperty, JsonSerializer<?> jsonSerializer) {
        super(enumMapSerializer);
        this._property = beanProperty;
        this._staticTyping = enumMapSerializer._staticTyping;
        this._valueType = enumMapSerializer._valueType;
        this._keyEnums = enumMapSerializer._keyEnums;
        this._valueTypeSerializer = enumMapSerializer._valueTypeSerializer;
        this._valueSerializer = jsonSerializer;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public EnumMapSerializer _withValueTypeSerializer(TypeSerializer typeSerializer) {
        return new EnumMapSerializer(this._valueType, this._staticTyping, this._keyEnums, typeSerializer, this._valueSerializer);
    }

    public EnumMapSerializer withValueSerializer(BeanProperty beanProperty, JsonSerializer<?> jsonSerializer) {
        return (this._property == beanProperty && jsonSerializer == this._valueSerializer) ? this : new EnumMapSerializer(this, beanProperty, jsonSerializer);
    }

    @Override // com.fasterxml.jackson.databind.ser.ContextualSerializer
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        AnnotatedMember member;
        Object objFindContentSerializer;
        JsonSerializer<Object> jsonSerializerSerializerInstance = (beanProperty == null || (member = beanProperty.getMember()) == null || (objFindContentSerializer = serializerProvider.getAnnotationIntrospector().findContentSerializer(member)) == null) ? null : serializerProvider.serializerInstance(member, objFindContentSerializer);
        if (jsonSerializerSerializerInstance == null) {
            jsonSerializerSerializerInstance = this._valueSerializer;
        }
        JsonSerializer<?> jsonSerializerFindConvertingContentSerializer = findConvertingContentSerializer(serializerProvider, beanProperty, jsonSerializerSerializerInstance);
        if (jsonSerializerFindConvertingContentSerializer == null) {
            if (this._staticTyping) {
                return withValueSerializer(beanProperty, serializerProvider.findValueSerializer(this._valueType, beanProperty));
            }
        } else {
            jsonSerializerFindConvertingContentSerializer = serializerProvider.handleSecondaryContextualization(jsonSerializerFindConvertingContentSerializer, beanProperty);
        }
        return jsonSerializerFindConvertingContentSerializer != this._valueSerializer ? withValueSerializer(beanProperty, jsonSerializerFindConvertingContentSerializer) : this;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public JavaType getContentType() {
        return this._valueType;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public JsonSerializer<?> getContentSerializer() {
        return this._valueSerializer;
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer, com.fasterxml.jackson.databind.JsonSerializer
    public boolean isEmpty(EnumMap<? extends Enum<?>, ?> enumMap) {
        return enumMap == null || enumMap.isEmpty();
    }

    @Override // com.fasterxml.jackson.databind.ser.ContainerSerializer
    public boolean hasSingleElement(EnumMap<? extends Enum<?>, ?> enumMap) {
        return enumMap.size() == 1;
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.JsonSerializer
    public void serialize(EnumMap<? extends Enum<?>, ?> enumMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (!enumMap.isEmpty()) {
            serializeContents(enumMap, jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndObject();
    }

    @Override // com.fasterxml.jackson.databind.JsonSerializer
    public void serializeWithType(EnumMap<? extends Enum<?>, ?> enumMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
        typeSerializer.writeTypePrefixForObject(enumMap, jsonGenerator);
        if (!enumMap.isEmpty()) {
            serializeContents(enumMap, jsonGenerator, serializerProvider);
        }
        typeSerializer.writeTypeSuffixForObject(enumMap, jsonGenerator);
    }

    protected void serializeContents(EnumMap<? extends Enum<?>, ?> enumMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        JsonSerializer<Object> jsonSerializer = this._valueSerializer;
        if (jsonSerializer != null) {
            serializeContentsUsing(enumMap, jsonGenerator, serializerProvider, jsonSerializer);
            return;
        }
        EnumValues enumValues = this._keyEnums;
        boolean zIsEnabled = serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES);
        TypeSerializer typeSerializer = this._valueTypeSerializer;
        Iterator it = enumMap.entrySet().iterator();
        Class<?> cls = null;
        JsonSerializer<Object> jsonSerializerFindValueSerializer = null;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object value = entry.getValue();
            if (zIsEnabled || value != null) {
                Enum<?> r8 = (Enum) entry.getKey();
                if (enumValues == null) {
                    enumValues = ((EnumSerializer) ((StdSerializer) serializerProvider.findValueSerializer(r8.getDeclaringClass(), this._property))).getEnumValues();
                }
                jsonGenerator.writeFieldName(enumValues.serializedValueFor(r8));
                if (value == null) {
                    serializerProvider.defaultSerializeNull(jsonGenerator);
                } else {
                    Class<?> cls2 = value.getClass();
                    if (cls2 != cls) {
                        jsonSerializerFindValueSerializer = serializerProvider.findValueSerializer(cls2, this._property);
                        cls = cls2;
                    }
                    if (typeSerializer == null) {
                        try {
                            jsonSerializerFindValueSerializer.serialize(value, jsonGenerator, serializerProvider);
                        } catch (Exception e) {
                            wrapAndThrow(serializerProvider, e, enumMap, ((Enum) entry.getKey()).name());
                        }
                    } else {
                        jsonSerializerFindValueSerializer.serializeWithType(value, jsonGenerator, serializerProvider, typeSerializer);
                    }
                }
            }
        }
    }

    protected void serializeContentsUsing(EnumMap<? extends Enum<?>, ?> enumMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, JsonSerializer<Object> jsonSerializer) throws IOException {
        EnumValues enumValues = this._keyEnums;
        boolean zIsEnabled = serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES);
        TypeSerializer typeSerializer = this._valueTypeSerializer;
        Iterator it = enumMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object value = entry.getValue();
            if (zIsEnabled || value != null) {
                Enum<?> r6 = (Enum) entry.getKey();
                if (enumValues == null) {
                    enumValues = ((EnumSerializer) ((StdSerializer) serializerProvider.findValueSerializer(r6.getDeclaringClass(), this._property))).getEnumValues();
                }
                jsonGenerator.writeFieldName(enumValues.serializedValueFor(r6));
                if (value == null) {
                    serializerProvider.defaultSerializeNull(jsonGenerator);
                } else if (typeSerializer == null) {
                    try {
                        jsonSerializer.serialize(value, jsonGenerator, serializerProvider);
                    } catch (Exception e) {
                        wrapAndThrow(serializerProvider, e, enumMap, ((Enum) entry.getKey()).name());
                    }
                } else {
                    jsonSerializer.serializeWithType(value, jsonGenerator, serializerProvider, typeSerializer);
                }
            }
        }
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.jsonschema.SchemaAware
    public JsonNode getSchema(SerializerProvider serializerProvider, Type type) throws JsonMappingException {
        ObjectNode objectNodeCreateSchemaNode = createSchemaNode("object", true);
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length == 2) {
                JavaType javaTypeConstructType = serializerProvider.constructType(actualTypeArguments[0]);
                JavaType javaTypeConstructType2 = serializerProvider.constructType(actualTypeArguments[1]);
                ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                for (Enum<?> r5 : (Enum[]) javaTypeConstructType.getRawClass().getEnumConstants()) {
                    JsonFormatVisitable jsonFormatVisitableFindValueSerializer = serializerProvider.findValueSerializer(javaTypeConstructType2.getRawClass(), this._property);
                    objectNode.put(serializerProvider.getConfig().getAnnotationIntrospector().findEnumValue(r5), jsonFormatVisitableFindValueSerializer instanceof SchemaAware ? ((SchemaAware) jsonFormatVisitableFindValueSerializer).getSchema(serializerProvider, null) : JsonSchema.getDefaultSchemaNode());
                }
                objectNodeCreateSchemaNode.put("properties", objectNode);
            }
        }
        return objectNodeCreateSchemaNode;
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.JsonSerializer, com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
        JsonObjectFormatVisitor jsonObjectFormatVisitorExpectObjectFormat;
        if (jsonFormatVisitorWrapper == null || (jsonObjectFormatVisitorExpectObjectFormat = jsonFormatVisitorWrapper.expectObjectFormat(javaType)) == null) {
            return;
        }
        JavaType javaTypeContainedType = javaType.containedType(1);
        JsonSerializer<Object> jsonSerializerFindValueSerializer = this._valueSerializer;
        if (jsonSerializerFindValueSerializer == null && javaTypeContainedType != null) {
            jsonSerializerFindValueSerializer = jsonFormatVisitorWrapper.getProvider().findValueSerializer(javaTypeContainedType, this._property);
        }
        if (javaTypeContainedType == null) {
            javaTypeContainedType = jsonFormatVisitorWrapper.getProvider().constructType(Object.class);
        }
        EnumValues enumValues = this._keyEnums;
        if (enumValues == null) {
            JavaType javaTypeContainedType2 = javaType.containedType(0);
            if (javaTypeContainedType2 == null) {
                throw new IllegalStateException("Can not resolve Enum type of EnumMap: " + javaType);
            }
            JsonSerializer<Object> jsonSerializerFindValueSerializer2 = jsonFormatVisitorWrapper.getProvider().findValueSerializer(javaTypeContainedType2, this._property);
            if (!(jsonSerializerFindValueSerializer2 instanceof EnumSerializer)) {
                throw new IllegalStateException("Can not resolve Enum type of EnumMap: " + javaType);
            }
            enumValues = ((EnumSerializer) jsonSerializerFindValueSerializer2).getEnumValues();
        }
        Iterator it = enumValues.internalMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String value = ((SerializableString) entry.getValue()).getValue();
            if (jsonSerializerFindValueSerializer == null) {
                jsonSerializerFindValueSerializer = jsonFormatVisitorWrapper.getProvider().findValueSerializer(entry.getKey().getClass(), this._property);
            }
            jsonObjectFormatVisitorExpectObjectFormat.property(value, jsonSerializerFindValueSerializer, javaTypeContainedType);
        }
    }
}
