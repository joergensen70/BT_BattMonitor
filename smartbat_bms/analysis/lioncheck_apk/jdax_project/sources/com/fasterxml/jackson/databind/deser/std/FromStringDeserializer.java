package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public abstract class FromStringDeserializer<T> extends StdScalarDeserializer<T> {
    protected abstract T _deserialize(String str, DeserializationContext deserializationContext) throws IOException;

    protected T _deserializeFromEmptyString() throws IOException {
        return null;
    }

    public static Class<?>[] types() {
        return new Class[]{File.class, URL.class, URI.class, Class.class, JavaType.class, Currency.class, Pattern.class, Locale.class, Charset.class, TimeZone.class, InetAddress.class, InetSocketAddress.class};
    }

    protected FromStringDeserializer(Class<?> cls) {
        super(cls);
    }

    public static Std findDeserializer(Class<?> cls) {
        int i;
        if (cls == File.class) {
            i = 1;
        } else if (cls == URL.class) {
            i = 2;
        } else if (cls == URI.class) {
            i = 3;
        } else if (cls == Class.class) {
            i = 4;
        } else if (cls == JavaType.class) {
            i = 5;
        } else if (cls == Currency.class) {
            i = 6;
        } else if (cls == Pattern.class) {
            i = 7;
        } else if (cls == Locale.class) {
            i = 8;
        } else if (cls == Charset.class) {
            i = 9;
        } else if (cls == TimeZone.class) {
            i = 10;
        } else if (cls == InetAddress.class) {
            i = 11;
        } else {
            if (cls != InetSocketAddress.class) {
                return null;
            }
            i = 12;
        }
        return new Std(cls, i);
    }

    @Override // com.fasterxml.jackson.databind.JsonDeserializer
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String str;
        String message;
        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY && deserializationContext.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            jsonParser.nextToken();
            T tDeserialize = deserialize(jsonParser, deserializationContext);
            if (jsonParser.nextToken() == JsonToken.END_ARRAY) {
                return tDeserialize;
            }
            throw deserializationContext.wrongTokenException(jsonParser, JsonToken.END_ARRAY, "Attempted to unwrap single value array for single '" + this._valueClass.getName() + "' value but there was more than a single value in the array");
        }
        String valueAsString = jsonParser.getValueAsString();
        IllegalArgumentException e = null;
        if (valueAsString != null) {
            if (valueAsString.length() != 0) {
                String strTrim = valueAsString.trim();
                if (strTrim.length() != 0) {
                    try {
                        T t_deserialize = _deserialize(strTrim, deserializationContext);
                        if (t_deserialize != null) {
                            return t_deserialize;
                        }
                    } catch (IllegalArgumentException e2) {
                        e = e2;
                    }
                    if (e != null && (message = e.getMessage()) != null) {
                        str = "not a valid textual representation, problem: " + message;
                    } else {
                        str = "not a valid textual representation";
                    }
                    JsonMappingException jsonMappingExceptionWeirdStringException = deserializationContext.weirdStringException(strTrim, this._valueClass, str);
                    if (e != null) {
                        jsonMappingExceptionWeirdStringException.initCause(e);
                        throw jsonMappingExceptionWeirdStringException;
                    }
                    throw jsonMappingExceptionWeirdStringException;
                }
            }
            return _deserializeFromEmptyString();
        }
        if (jsonParser.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
            T t = (T) jsonParser.getEmbeddedObject();
            if (t == null) {
                return null;
            }
            return this._valueClass.isAssignableFrom(t.getClass()) ? t : _deserializeEmbedded(t, deserializationContext);
        }
        throw deserializationContext.mappingException(this._valueClass);
    }

    protected T _deserializeEmbedded(Object obj, DeserializationContext deserializationContext) throws IOException {
        throw deserializationContext.mappingException("Don't know how to convert embedded Object of type " + obj.getClass().getName() + " into " + this._valueClass.getName());
    }

    public static class Std extends FromStringDeserializer<Object> {
        public static final int STD_CHARSET = 9;
        public static final int STD_CLASS = 4;
        public static final int STD_CURRENCY = 6;
        public static final int STD_FILE = 1;
        public static final int STD_INET_ADDRESS = 11;
        public static final int STD_INET_SOCKET_ADDRESS = 12;
        public static final int STD_JAVA_TYPE = 5;
        public static final int STD_LOCALE = 8;
        public static final int STD_PATTERN = 7;
        public static final int STD_TIME_ZONE = 10;
        public static final int STD_URI = 3;
        public static final int STD_URL = 2;
        private static final long serialVersionUID = 1;
        protected final int _kind;

        protected Std(Class<?> cls, int i) {
            super(cls);
            this._kind = i;
        }

        @Override // com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
        protected Object _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
            switch (this._kind) {
                case 1:
                    return new File(str);
                case 2:
                    return new URL(str);
                case 3:
                    return URI.create(str);
                case 4:
                    try {
                        return deserializationContext.findClass(str);
                    } catch (Exception e) {
                        throw deserializationContext.instantiationException(this._valueClass, ClassUtil.getRootCause(e));
                    }
                case 5:
                    return deserializationContext.getTypeFactory().constructFromCanonical(str);
                case 6:
                    return Currency.getInstance(str);
                case 7:
                    return Pattern.compile(str);
                case 8:
                    int iIndexOf = str.indexOf(95);
                    if (iIndexOf < 0) {
                        return new Locale(str);
                    }
                    String strSubstring = str.substring(0, iIndexOf);
                    String strSubstring2 = str.substring(iIndexOf + 1);
                    int iIndexOf2 = strSubstring2.indexOf(95);
                    if (iIndexOf2 < 0) {
                        return new Locale(strSubstring, strSubstring2);
                    }
                    return new Locale(strSubstring, strSubstring2.substring(0, iIndexOf2), strSubstring2.substring(iIndexOf2 + 1));
                case 9:
                    return Charset.forName(str);
                case 10:
                    return TimeZone.getTimeZone(str);
                case 11:
                    return InetAddress.getByName(str);
                case 12:
                    if (str.startsWith("[")) {
                        int iLastIndexOf = str.lastIndexOf(93);
                        if (iLastIndexOf == -1) {
                            throw new InvalidFormatException("Bracketed IPv6 address must contain closing bracket", str, InetSocketAddress.class);
                        }
                        int iIndexOf3 = str.indexOf(58, iLastIndexOf);
                        return new InetSocketAddress(str.substring(0, iLastIndexOf + 1), iIndexOf3 > -1 ? Integer.parseInt(str.substring(iIndexOf3 + 1)) : 0);
                    }
                    int iIndexOf4 = str.indexOf(58);
                    if (iIndexOf4 >= 0) {
                        int i = iIndexOf4 + 1;
                        if (str.indexOf(58, i) < 0) {
                            return new InetSocketAddress(str.substring(0, iIndexOf4), Integer.parseInt(str.substring(i)));
                        }
                    }
                    return new InetSocketAddress(str, 0);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override // com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
        protected Object _deserializeFromEmptyString() throws IOException {
            if (this._kind == 3) {
                return URI.create("");
            }
            return super._deserializeFromEmptyString();
        }
    }
}
