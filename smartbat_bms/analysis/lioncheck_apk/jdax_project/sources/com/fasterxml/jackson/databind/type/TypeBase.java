package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public abstract class TypeBase extends JavaType implements JsonSerializable {
    private static final long serialVersionUID = -3581199092426900829L;
    volatile transient String _canonicalName;

    protected abstract String buildCanonicalName();

    @Override // com.fasterxml.jackson.databind.JavaType
    public abstract StringBuilder getErasedSignature(StringBuilder sb);

    @Override // com.fasterxml.jackson.databind.JavaType
    public abstract StringBuilder getGenericSignature(StringBuilder sb);

    @Deprecated
    protected TypeBase(Class<?> cls, int i, Object obj, Object obj2) {
        this(cls, i, obj, obj2, false);
    }

    protected TypeBase(Class<?> cls, int i, Object obj, Object obj2, boolean z) {
        super(cls, i, obj, obj2, z);
    }

    @Override // com.fasterxml.jackson.core.type.ResolvedType
    public String toCanonical() {
        String str = this._canonicalName;
        return str == null ? buildCanonicalName() : str;
    }

    @Override // com.fasterxml.jackson.databind.JavaType
    public <T> T getValueHandler() {
        return (T) this._valueHandler;
    }

    @Override // com.fasterxml.jackson.databind.JavaType
    public <T> T getTypeHandler() {
        return (T) this._typeHandler;
    }

    @Override // com.fasterxml.jackson.databind.JsonSerializable
    public void serializeWithType(JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
        typeSerializer.writeTypePrefixForScalar(this, jsonGenerator);
        serialize(jsonGenerator, serializerProvider);
        typeSerializer.writeTypeSuffixForScalar(this, jsonGenerator);
    }

    @Override // com.fasterxml.jackson.databind.JsonSerializable
    public void serialize(JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(toCanonical());
    }

    protected static StringBuilder _classSignature(Class<?> cls, StringBuilder sb, boolean z) {
        if (cls.isPrimitive()) {
            if (cls == Boolean.TYPE) {
                sb.append('Z');
                return sb;
            }
            if (cls == Byte.TYPE) {
                sb.append('B');
                return sb;
            }
            if (cls == Short.TYPE) {
                sb.append('S');
                return sb;
            }
            if (cls == Character.TYPE) {
                sb.append('C');
                return sb;
            }
            if (cls == Integer.TYPE) {
                sb.append('I');
                return sb;
            }
            if (cls == Long.TYPE) {
                sb.append('J');
                return sb;
            }
            if (cls == Float.TYPE) {
                sb.append('F');
                return sb;
            }
            if (cls == Double.TYPE) {
                sb.append('D');
                return sb;
            }
            if (cls == Void.TYPE) {
                sb.append('V');
                return sb;
            }
            throw new IllegalStateException("Unrecognized primitive type: " + cls.getName());
        }
        sb.append('L');
        String name = cls.getName();
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char cCharAt = name.charAt(i);
            if (cCharAt == '.') {
                cCharAt = '/';
            }
            sb.append(cCharAt);
        }
        if (z) {
            sb.append(';');
        }
        return sb;
    }
}
