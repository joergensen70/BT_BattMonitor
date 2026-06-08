package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class ExternalTypeHandler {
    private final HashMap<String, Integer> _nameToPropertyIndex;
    private final ExtTypedProperty[] _properties;
    private final TokenBuffer[] _tokens;
    private final String[] _typeIds;

    protected ExternalTypeHandler(ExtTypedProperty[] extTypedPropertyArr, HashMap<String, Integer> map, String[] strArr, TokenBuffer[] tokenBufferArr) {
        this._properties = extTypedPropertyArr;
        this._nameToPropertyIndex = map;
        this._typeIds = strArr;
        this._tokens = tokenBufferArr;
    }

    protected ExternalTypeHandler(ExternalTypeHandler externalTypeHandler) {
        ExtTypedProperty[] extTypedPropertyArr = externalTypeHandler._properties;
        this._properties = extTypedPropertyArr;
        this._nameToPropertyIndex = externalTypeHandler._nameToPropertyIndex;
        int length = extTypedPropertyArr.length;
        this._typeIds = new String[length];
        this._tokens = new TokenBuffer[length];
    }

    public ExternalTypeHandler start() {
        return new ExternalTypeHandler(this);
    }

    public boolean handleTypePropertyValue(JsonParser jsonParser, DeserializationContext deserializationContext, String str, Object obj) throws IOException {
        Integer num = this._nameToPropertyIndex.get(str);
        if (num == null) {
            return false;
        }
        int iIntValue = num.intValue();
        if (!this._properties[iIntValue].hasTypePropertyName(str)) {
            return false;
        }
        String text = jsonParser.getText();
        if (obj != null && this._tokens[iIntValue] != null) {
            _deserializeAndSet(jsonParser, deserializationContext, obj, iIntValue, text);
            this._tokens[iIntValue] = null;
            return true;
        }
        this._typeIds[iIntValue] = text;
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x002b, code lost:
    
        if (r7._tokens[r4] != null) goto L17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0040, code lost:
    
        if (r7._typeIds[r4] != null) goto L17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0042, code lost:
    
        r10 = r7._typeIds;
        r5 = r10[r4];
        r10[r4] = null;
        _deserializeAndSet(r8, r9, r11, r4, r5);
        r7._tokens[r4] = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:?, code lost:
    
        return true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean handlePropertyValue(com.fasterxml.jackson.core.JsonParser r8, com.fasterxml.jackson.databind.DeserializationContext r9, java.lang.String r10, java.lang.Object r11) throws java.io.IOException {
        /*
            r7 = this;
            java.util.HashMap<java.lang.String, java.lang.Integer> r0 = r7._nameToPropertyIndex
            java.lang.Object r0 = r0.get(r10)
            java.lang.Integer r0 = (java.lang.Integer) r0
            if (r0 != 0) goto Lc
            r8 = 0
            return r8
        Lc:
            int r4 = r0.intValue()
            com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler$ExtTypedProperty[] r0 = r7._properties
            r0 = r0[r4]
            boolean r10 = r0.hasTypePropertyName(r10)
            if (r10 == 0) goto L2e
            java.lang.String[] r10 = r7._typeIds
            java.lang.String r0 = r8.getText()
            r10[r4] = r0
            r8.skipChildren()
            if (r11 == 0) goto L55
            com.fasterxml.jackson.databind.util.TokenBuffer[] r10 = r7._tokens
            r10 = r10[r4]
            if (r10 == 0) goto L55
            goto L42
        L2e:
            com.fasterxml.jackson.databind.util.TokenBuffer r10 = new com.fasterxml.jackson.databind.util.TokenBuffer
            r10.<init>(r8)
            r10.copyCurrentStructure(r8)
            com.fasterxml.jackson.databind.util.TokenBuffer[] r0 = r7._tokens
            r0[r4] = r10
            if (r11 == 0) goto L55
            java.lang.String[] r10 = r7._typeIds
            r10 = r10[r4]
            if (r10 == 0) goto L55
        L42:
            java.lang.String[] r10 = r7._typeIds
            r5 = r10[r4]
            r6 = 0
            r10[r4] = r6
            r0 = r7
            r1 = r8
            r2 = r9
            r3 = r11
            r0._deserializeAndSet(r1, r2, r3, r4, r5)
            com.fasterxml.jackson.databind.util.TokenBuffer[] r8 = r0._tokens
            r8[r4] = r6
            goto L56
        L55:
            r0 = r7
        L56:
            r8 = 1
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.handlePropertyValue(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext, java.lang.String, java.lang.Object):boolean");
    }

    public Object complete(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj) throws IOException {
        JsonParser jsonParser2;
        DeserializationContext deserializationContext2;
        Object obj2;
        int length = this._properties.length;
        int i = 0;
        while (i < length) {
            String defaultTypeId = this._typeIds[i];
            if (defaultTypeId == null) {
                TokenBuffer tokenBuffer = this._tokens[i];
                if (tokenBuffer != null) {
                    JsonToken jsonTokenFirstToken = tokenBuffer.firstToken();
                    if (jsonTokenFirstToken != null && jsonTokenFirstToken.isScalarValue()) {
                        JsonParser jsonParserAsParser = tokenBuffer.asParser(jsonParser);
                        jsonParserAsParser.nextToken();
                        SettableBeanProperty property = this._properties[i].getProperty();
                        Object objDeserializeIfNatural = TypeDeserializer.deserializeIfNatural(jsonParserAsParser, deserializationContext, property.getType());
                        if (objDeserializeIfNatural != null) {
                            property.set(obj, objDeserializeIfNatural);
                        } else {
                            if (!this._properties[i].hasDefaultType()) {
                                throw deserializationContext.mappingException("Missing external type id property '" + this._properties[i].getTypePropertyName() + "'");
                            }
                            defaultTypeId = this._properties[i].getDefaultTypeId();
                        }
                    }
                }
                jsonParser2 = jsonParser;
                deserializationContext2 = deserializationContext;
                obj2 = obj;
                i++;
                jsonParser = jsonParser2;
                deserializationContext = deserializationContext2;
                obj = obj2;
            } else if (this._tokens[i] == null) {
                throw deserializationContext.mappingException("Missing property '" + this._properties[i].getProperty().getName() + "' for external type id '" + this._properties[i].getTypePropertyName());
            }
            jsonParser2 = jsonParser;
            deserializationContext2 = deserializationContext;
            obj2 = obj;
            _deserializeAndSet(jsonParser2, deserializationContext2, obj2, i, defaultTypeId);
            i++;
            jsonParser = jsonParser2;
            deserializationContext = deserializationContext2;
            obj = obj2;
        }
        return obj;
    }

    public Object complete(JsonParser jsonParser, DeserializationContext deserializationContext, PropertyValueBuffer propertyValueBuffer, PropertyBasedCreator propertyBasedCreator) throws IOException {
        int length = this._properties.length;
        Object[] objArr = new Object[length];
        for (int i = 0; i < length; i++) {
            String defaultTypeId = this._typeIds[i];
            if (defaultTypeId == null) {
                if (this._tokens[i] == null) {
                    continue;
                } else {
                    if (!this._properties[i].hasDefaultType()) {
                        throw deserializationContext.mappingException("Missing external type id property '" + this._properties[i].getTypePropertyName() + "'");
                    }
                    defaultTypeId = this._properties[i].getDefaultTypeId();
                }
            } else if (this._tokens[i] == null) {
                throw deserializationContext.mappingException("Missing property '" + this._properties[i].getProperty().getName() + "' for external type id '" + this._properties[i].getTypePropertyName());
            }
            objArr[i] = _deserialize(jsonParser, deserializationContext, i, defaultTypeId);
        }
        for (int i2 = 0; i2 < length; i2++) {
            SettableBeanProperty property = this._properties[i2].getProperty();
            if (propertyBasedCreator.findCreatorProperty(property.getName()) != null) {
                propertyValueBuffer.assignParameter(property.getCreatorIndex(), objArr[i2]);
            }
        }
        Object objBuild = propertyBasedCreator.build(deserializationContext, propertyValueBuffer);
        for (int i3 = 0; i3 < length; i3++) {
            SettableBeanProperty property2 = this._properties[i3].getProperty();
            if (propertyBasedCreator.findCreatorProperty(property2.getName()) == null) {
                property2.set(objBuild, objArr[i3]);
            }
        }
        return objBuild;
    }

    protected final Object _deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, int i, String str) throws IOException {
        TokenBuffer tokenBuffer = new TokenBuffer(jsonParser);
        tokenBuffer.writeStartArray();
        tokenBuffer.writeString(str);
        JsonParser jsonParserAsParser = this._tokens[i].asParser(jsonParser);
        jsonParserAsParser.nextToken();
        tokenBuffer.copyCurrentStructure(jsonParserAsParser);
        tokenBuffer.writeEndArray();
        JsonParser jsonParserAsParser2 = tokenBuffer.asParser(jsonParser);
        jsonParserAsParser2.nextToken();
        return this._properties[i].getProperty().deserialize(jsonParserAsParser2, deserializationContext);
    }

    protected final void _deserializeAndSet(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj, int i, String str) throws IOException {
        TokenBuffer tokenBuffer = new TokenBuffer(jsonParser);
        tokenBuffer.writeStartArray();
        tokenBuffer.writeString(str);
        JsonParser jsonParserAsParser = this._tokens[i].asParser(jsonParser);
        jsonParserAsParser.nextToken();
        tokenBuffer.copyCurrentStructure(jsonParserAsParser);
        tokenBuffer.writeEndArray();
        JsonParser jsonParserAsParser2 = tokenBuffer.asParser(jsonParser);
        jsonParserAsParser2.nextToken();
        this._properties[i].getProperty().deserializeAndSet(jsonParserAsParser2, deserializationContext, obj);
    }

    public static class Builder {
        private final ArrayList<ExtTypedProperty> _properties = new ArrayList<>();
        private final HashMap<String, Integer> _nameToPropertyIndex = new HashMap<>();

        public void addExternal(SettableBeanProperty settableBeanProperty, TypeDeserializer typeDeserializer) {
            Integer numValueOf = Integer.valueOf(this._properties.size());
            this._properties.add(new ExtTypedProperty(settableBeanProperty, typeDeserializer));
            this._nameToPropertyIndex.put(settableBeanProperty.getName(), numValueOf);
            this._nameToPropertyIndex.put(typeDeserializer.getPropertyName(), numValueOf);
        }

        public ExternalTypeHandler build() {
            ArrayList<ExtTypedProperty> arrayList = this._properties;
            return new ExternalTypeHandler((ExtTypedProperty[]) arrayList.toArray(new ExtTypedProperty[arrayList.size()]), this._nameToPropertyIndex, null, null);
        }
    }

    private static final class ExtTypedProperty {
        private final SettableBeanProperty _property;
        private final TypeDeserializer _typeDeserializer;
        private final String _typePropertyName;

        public ExtTypedProperty(SettableBeanProperty settableBeanProperty, TypeDeserializer typeDeserializer) {
            this._property = settableBeanProperty;
            this._typeDeserializer = typeDeserializer;
            this._typePropertyName = typeDeserializer.getPropertyName();
        }

        public boolean hasTypePropertyName(String str) {
            return str.equals(this._typePropertyName);
        }

        public boolean hasDefaultType() {
            return this._typeDeserializer.getDefaultImpl() != null;
        }

        public String getDefaultTypeId() {
            Class<?> defaultImpl = this._typeDeserializer.getDefaultImpl();
            if (defaultImpl == null) {
                return null;
            }
            return this._typeDeserializer.getTypeIdResolver().idFromValueAndType(null, defaultImpl);
        }

        public String getTypePropertyName() {
            return this._typePropertyName;
        }

        public SettableBeanProperty getProperty() {
            return this._property;
        }
    }
}
