package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.HashSet;

/* JADX INFO: loaded from: classes.dex */
public class BeanAsArrayDeserializer extends BeanDeserializerBase {
    private static final long serialVersionUID = 1;
    protected final BeanDeserializerBase _delegate;
    protected final SettableBeanProperty[] _orderedProperties;

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase
    protected BeanDeserializerBase asArrayDeserializer() {
        return this;
    }

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase
    public /* bridge */ /* synthetic */ BeanDeserializerBase withIgnorableProperties(HashSet hashSet) {
        return withIgnorableProperties((HashSet<String>) hashSet);
    }

    public BeanAsArrayDeserializer(BeanDeserializerBase beanDeserializerBase, SettableBeanProperty[] settableBeanPropertyArr) {
        super(beanDeserializerBase);
        this._delegate = beanDeserializerBase;
        this._orderedProperties = settableBeanPropertyArr;
    }

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase, com.fasterxml.jackson.databind.JsonDeserializer
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer nameTransformer) {
        return this._delegate.unwrappingDeserializer(nameTransformer);
    }

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase
    public BeanAsArrayDeserializer withObjectIdReader(ObjectIdReader objectIdReader) {
        return new BeanAsArrayDeserializer(this._delegate.withObjectIdReader(objectIdReader), this._orderedProperties);
    }

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase
    public BeanAsArrayDeserializer withIgnorableProperties(HashSet<String> hashSet) {
        return new BeanAsArrayDeserializer(this._delegate.withIgnorableProperties(hashSet), this._orderedProperties);
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            return _deserializeFromNonArray(jsonParser, deserializationContext);
        }
        if (!this._vanillaProcessing) {
            return _deserializeNonVanilla(jsonParser, deserializationContext);
        }
        Object objCreateUsingDefault = this._valueInstantiator.createUsingDefault(deserializationContext);
        SettableBeanProperty[] settableBeanPropertyArr = this._orderedProperties;
        int length = settableBeanPropertyArr.length;
        int i = 0;
        while (true) {
            if (jsonParser.nextToken() == JsonToken.END_ARRAY) {
                break;
            }
            if (i != length) {
                SettableBeanProperty settableBeanProperty = settableBeanPropertyArr[i];
                if (settableBeanProperty != null) {
                    try {
                        settableBeanProperty.deserializeAndSet(jsonParser, deserializationContext, objCreateUsingDefault);
                    } catch (Exception e) {
                        wrapAndThrow(e, objCreateUsingDefault, settableBeanProperty.getName(), deserializationContext);
                    }
                } else {
                    jsonParser.skipChildren();
                }
                i++;
            } else {
                if (!this._ignoreAllUnknown) {
                    throw deserializationContext.mappingException("Unexpected JSON values; expected at most " + length + " properties (in JSON Array)");
                }
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    jsonParser.skipChildren();
                }
            }
        }
        return objCreateUsingDefault;
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0026, code lost:
    
        return r8;
     */
    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.lang.Object deserialize(com.fasterxml.jackson.core.JsonParser r6, com.fasterxml.jackson.databind.DeserializationContext r7, java.lang.Object r8) throws java.io.IOException {
        /*
            r5 = this;
            com.fasterxml.jackson.databind.deser.impl.ValueInjector[] r0 = r5._injectables
            if (r0 == 0) goto L7
            r5.injectValues(r7, r8)
        L7:
            com.fasterxml.jackson.databind.deser.SettableBeanProperty[] r0 = r5._orderedProperties
            int r1 = r0.length
            r2 = 0
        Lb:
            com.fasterxml.jackson.core.JsonToken r3 = r6.nextToken()
            com.fasterxml.jackson.core.JsonToken r4 = com.fasterxml.jackson.core.JsonToken.END_ARRAY
            if (r3 != r4) goto L14
            goto L26
        L14:
            if (r2 != r1) goto L41
            boolean r0 = r5._ignoreAllUnknown
            if (r0 == 0) goto L27
        L1a:
            com.fasterxml.jackson.core.JsonToken r7 = r6.nextToken()
            com.fasterxml.jackson.core.JsonToken r0 = com.fasterxml.jackson.core.JsonToken.END_ARRAY
            if (r7 == r0) goto L26
            r6.skipChildren()
            goto L1a
        L26:
            return r8
        L27:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            java.lang.String r8 = "Unexpected JSON values; expected at most "
            r6.<init>(r8)
            java.lang.StringBuilder r6 = r6.append(r1)
            java.lang.String r8 = " properties (in JSON Array)"
            java.lang.StringBuilder r6 = r6.append(r8)
            java.lang.String r6 = r6.toString()
            com.fasterxml.jackson.databind.JsonMappingException r6 = r7.mappingException(r6)
            throw r6
        L41:
            r3 = r0[r2]
            if (r3 == 0) goto L52
            r3.deserializeAndSet(r6, r7, r8)     // Catch: java.lang.Exception -> L49
            goto L55
        L49:
            r4 = move-exception
            java.lang.String r3 = r3.getName()
            r5.wrapAndThrow(r4, r8, r3, r7)
            goto L55
        L52:
            r6.skipChildren()
        L55:
            int r2 = r2 + 1
            goto Lb
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.deser.impl.BeanAsArrayDeserializer.deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext, java.lang.Object):java.lang.Object");
    }

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase
    public Object deserializeFromObject(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return _deserializeFromNonArray(jsonParser, deserializationContext);
    }

    protected Object _deserializeNonVanilla(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (this._nonStandardCreation) {
            return _deserializeWithCreator(jsonParser, deserializationContext);
        }
        Object objCreateUsingDefault = this._valueInstantiator.createUsingDefault(deserializationContext);
        if (this._injectables != null) {
            injectValues(deserializationContext, objCreateUsingDefault);
        }
        Class<?> activeView = this._needViewProcesing ? deserializationContext.getActiveView() : null;
        SettableBeanProperty[] settableBeanPropertyArr = this._orderedProperties;
        int length = settableBeanPropertyArr.length;
        int i = 0;
        while (true) {
            if (jsonParser.nextToken() == JsonToken.END_ARRAY) {
                break;
            }
            if (i != length) {
                SettableBeanProperty settableBeanProperty = settableBeanPropertyArr[i];
                i++;
                if (settableBeanProperty != null && (activeView == null || settableBeanProperty.visibleInView(activeView))) {
                    try {
                        settableBeanProperty.deserializeAndSet(jsonParser, deserializationContext, objCreateUsingDefault);
                    } catch (Exception e) {
                        wrapAndThrow(e, objCreateUsingDefault, settableBeanProperty.getName(), deserializationContext);
                    }
                } else {
                    jsonParser.skipChildren();
                }
            } else {
                if (!this._ignoreAllUnknown) {
                    throw deserializationContext.mappingException("Unexpected JSON values; expected at most " + length + " properties (in JSON Array)");
                }
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    jsonParser.skipChildren();
                }
            }
        }
        return objCreateUsingDefault;
    }

    protected Object _deserializeWithCreator(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
        }
        if (this._propertyBasedCreator != null) {
            return _deserializeUsingPropertyBased(jsonParser, deserializationContext);
        }
        if (this._beanType.isAbstract()) {
            throw JsonMappingException.from(jsonParser, "Can not instantiate abstract type " + this._beanType + " (need to add/enable type information?)");
        }
        throw JsonMappingException.from(jsonParser, "No suitable constructor found for type " + this._beanType + ": can not instantiate from JSON object (need to add/enable type information?)");
    }

    @Override // com.fasterxml.jackson.databind.deser.BeanDeserializerBase
    protected final Object _deserializeUsingPropertyBased(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        PropertyBasedCreator propertyBasedCreator = this._propertyBasedCreator;
        PropertyValueBuffer propertyValueBufferStartBuilding = propertyBasedCreator.startBuilding(jsonParser, deserializationContext, this._objectIdReader);
        SettableBeanProperty[] settableBeanPropertyArr = this._orderedProperties;
        int length = settableBeanPropertyArr.length;
        int i = 0;
        Object objBuild = null;
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            SettableBeanProperty settableBeanProperty = i < length ? settableBeanPropertyArr[i] : null;
            if (settableBeanProperty == null) {
                jsonParser.skipChildren();
            } else if (objBuild != null) {
                try {
                    settableBeanProperty.deserializeAndSet(jsonParser, deserializationContext, objBuild);
                } catch (Exception e) {
                    wrapAndThrow(e, objBuild, settableBeanProperty.getName(), deserializationContext);
                }
            } else {
                String name = settableBeanProperty.getName();
                SettableBeanProperty settableBeanPropertyFindCreatorProperty = propertyBasedCreator.findCreatorProperty(name);
                if (settableBeanPropertyFindCreatorProperty != null) {
                    if (propertyValueBufferStartBuilding.assignParameter(settableBeanPropertyFindCreatorProperty.getCreatorIndex(), settableBeanPropertyFindCreatorProperty.deserialize(jsonParser, deserializationContext))) {
                        try {
                            objBuild = propertyBasedCreator.build(deserializationContext, propertyValueBufferStartBuilding);
                            if (objBuild.getClass() != this._beanType.getRawClass()) {
                                throw deserializationContext.mappingException("Can not support implicit polymorphic deserialization for POJOs-as-Arrays style: nominal type " + this._beanType.getRawClass().getName() + ", actual type " + objBuild.getClass().getName());
                            }
                        } catch (Exception e2) {
                            wrapAndThrow(e2, this._beanType.getRawClass(), name, deserializationContext);
                        }
                    } else {
                        continue;
                    }
                } else if (!propertyValueBufferStartBuilding.readIdProperty(name)) {
                    propertyValueBufferStartBuilding.bufferProperty(settableBeanProperty, settableBeanProperty.deserialize(jsonParser, deserializationContext));
                }
            }
            i++;
        }
        if (objBuild != null) {
            return objBuild;
        }
        try {
            return propertyBasedCreator.build(deserializationContext, propertyValueBufferStartBuilding);
        } catch (Exception e3) {
            wrapInstantiationProblem(e3, deserializationContext);
            return null;
        }
    }

    protected Object _deserializeFromNonArray(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        throw deserializationContext.mappingException("Can not deserialize a POJO (of type " + this._beanType.getRawClass().getName() + ") from non-Array representation (token: " + jsonParser.getCurrentToken() + "): type/property designed to be serialized as JSON Array");
    }
}
