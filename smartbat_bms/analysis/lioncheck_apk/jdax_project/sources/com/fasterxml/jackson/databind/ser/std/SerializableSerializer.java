package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/* JADX INFO: loaded from: classes.dex */
@JacksonStdImpl
public class SerializableSerializer extends StdSerializer<JsonSerializable> {
    public static final SerializableSerializer instance = new SerializableSerializer();
    private static final AtomicReference<ObjectMapper> _mapperReference = new AtomicReference<>();

    protected SerializableSerializer() {
        super(JsonSerializable.class);
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.JsonSerializer
    public void serialize(JsonSerializable jsonSerializable, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonSerializable.serialize(jsonGenerator, serializerProvider);
    }

    @Override // com.fasterxml.jackson.databind.JsonSerializer
    public final void serializeWithType(JsonSerializable jsonSerializable, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
        jsonSerializable.serializeWithType(jsonGenerator, serializerProvider, typeSerializer);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x0042  */
    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.jsonschema.SchemaAware
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public com.fasterxml.jackson.databind.JsonNode getSchema(com.fasterxml.jackson.databind.SerializerProvider r6, java.lang.reflect.Type r7) throws com.fasterxml.jackson.databind.JsonMappingException {
        /*
            r5 = this;
            com.fasterxml.jackson.databind.node.ObjectNode r6 = r5.createObjectNode()
            r0 = 0
            if (r7 == 0) goto L42
            java.lang.Class r7 = com.fasterxml.jackson.databind.type.TypeFactory.rawClass(r7)
            java.lang.Class<com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema> r1 = com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema.class
            boolean r1 = r7.isAnnotationPresent(r1)
            if (r1 == 0) goto L42
            java.lang.Class<com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema> r1 = com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema.class
            java.lang.annotation.Annotation r7 = r7.getAnnotation(r1)
            com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema r7 = (com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema) r7
            java.lang.String r1 = r7.schemaType()
            java.lang.String r2 = r7.schemaObjectPropertiesDefinition()
            java.lang.String r3 = "##irrelevant"
            boolean r2 = r3.equals(r2)
            if (r2 != 0) goto L30
            java.lang.String r2 = r7.schemaObjectPropertiesDefinition()
            goto L31
        L30:
            r2 = r0
        L31:
            java.lang.String r4 = r7.schemaItemDefinition()
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L3f
            java.lang.String r0 = r7.schemaItemDefinition()
        L3f:
            r7 = r0
            r0 = r2
            goto L45
        L42:
            java.lang.String r1 = "any"
            r7 = r0
        L45:
            java.lang.String r2 = "type"
            r6.put(r2, r1)
            if (r0 == 0) goto L62
            java.lang.String r1 = "properties"
            com.fasterxml.jackson.databind.ObjectMapper r2 = _getObjectMapper()     // Catch: java.io.IOException -> L5a
            com.fasterxml.jackson.databind.JsonNode r0 = r2.readTree(r0)     // Catch: java.io.IOException -> L5a
            r6.put(r1, r0)     // Catch: java.io.IOException -> L5a
            goto L62
        L5a:
            com.fasterxml.jackson.databind.JsonMappingException r6 = new com.fasterxml.jackson.databind.JsonMappingException
            java.lang.String r7 = "Failed to parse @JsonSerializableSchema.schemaObjectPropertiesDefinition value"
            r6.<init>(r7)
            throw r6
        L62:
            if (r7 == 0) goto L7a
            java.lang.String r0 = "items"
            com.fasterxml.jackson.databind.ObjectMapper r1 = _getObjectMapper()     // Catch: java.io.IOException -> L72
            com.fasterxml.jackson.databind.JsonNode r7 = r1.readTree(r7)     // Catch: java.io.IOException -> L72
            r6.put(r0, r7)     // Catch: java.io.IOException -> L72
            return r6
        L72:
            com.fasterxml.jackson.databind.JsonMappingException r6 = new com.fasterxml.jackson.databind.JsonMappingException
            java.lang.String r7 = "Failed to parse @JsonSerializableSchema.schemaItemDefinition value"
            r6.<init>(r7)
            throw r6
        L7a:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.ser.std.SerializableSerializer.getSchema(com.fasterxml.jackson.databind.SerializerProvider, java.lang.reflect.Type):com.fasterxml.jackson.databind.JsonNode");
    }

    private static final synchronized ObjectMapper _getObjectMapper() {
        ObjectMapper objectMapper;
        AtomicReference<ObjectMapper> atomicReference = _mapperReference;
        objectMapper = atomicReference.get();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            atomicReference.set(objectMapper);
        }
        return objectMapper;
    }

    @Override // com.fasterxml.jackson.databind.ser.std.StdSerializer, com.fasterxml.jackson.databind.JsonSerializer, com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
        jsonFormatVisitorWrapper.expectAnyFormat(javaType);
    }
}
