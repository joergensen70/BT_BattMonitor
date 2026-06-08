package com.fasterxml.jackson.databind.deser.std;

import android.support.v4.media.TransportMediator;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class UUIDDeserializer extends FromStringDeserializer<UUID> {
    static final int[] HEX_DIGITS;
    private static final long serialVersionUID = 1;

    static {
        int[] iArr = new int[TransportMediator.KEYCODE_MEDIA_PAUSE];
        HEX_DIGITS = iArr;
        Arrays.fill(iArr, -1);
        for (int i = 0; i < 10; i++) {
            HEX_DIGITS[i + 48] = i;
        }
        for (int i2 = 0; i2 < 6; i2++) {
            int[] iArr2 = HEX_DIGITS;
            int i3 = i2 + 10;
            iArr2[i2 + 97] = i3;
            iArr2[i2 + 65] = i3;
        }
    }

    public UUIDDeserializer() {
        super(UUID.class);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
    public UUID _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
        if (str.length() != 36) {
            if (str.length() == 24) {
                return _fromBytes(Base64Variants.getDefaultVariant().decode(str), deserializationContext);
            }
            _badFormat(str);
        }
        if (str.charAt(8) != '-' || str.charAt(13) != '-' || str.charAt(18) != '-' || str.charAt(23) != '-') {
            _badFormat(str);
        }
        return new UUID((((long) intFromChars(str, 0)) << 32) + ((((long) shortFromChars(str, 9)) << 16) | ((long) shortFromChars(str, 14))), ((((long) intFromChars(str, 28)) << 32) >>> 32) | (((long) (shortFromChars(str, 24) | (shortFromChars(str, 19) << 16))) << 32));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
    public UUID _deserializeEmbedded(Object obj, DeserializationContext deserializationContext) throws IOException {
        if (obj instanceof byte[]) {
            return _fromBytes((byte[]) obj, deserializationContext);
        }
        super._deserializeEmbedded(obj, deserializationContext);
        return null;
    }

    private void _badFormat(String str) {
        throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
    }

    static int intFromChars(String str, int i) {
        return (byteFromChars(str, i) << 24) + (byteFromChars(str, i + 2) << 16) + (byteFromChars(str, i + 4) << 8) + byteFromChars(str, i + 6);
    }

    static int shortFromChars(String str, int i) {
        return (byteFromChars(str, i) << 8) + byteFromChars(str, i + 2);
    }

    static int byteFromChars(String str, int i) {
        char cCharAt = str.charAt(i);
        int i2 = i + 1;
        char cCharAt2 = str.charAt(i2);
        if (cCharAt <= 127 && cCharAt2 <= 127) {
            int[] iArr = HEX_DIGITS;
            int i3 = iArr[cCharAt2] | (iArr[cCharAt] << 4);
            if (i3 >= 0) {
                return i3;
            }
        }
        if (cCharAt > 127 || HEX_DIGITS[cCharAt] < 0) {
            return _badChar(str, i, cCharAt);
        }
        return _badChar(str, i2, cCharAt2);
    }

    static int _badChar(String str, int i, char c) {
        throw new NumberFormatException("Non-hex character '" + c + "', not valid character for a UUID String' (value 0x" + Integer.toHexString(c) + ") for UUID String \"" + str + "\"");
    }

    private UUID _fromBytes(byte[] bArr, DeserializationContext deserializationContext) throws IOException {
        if (bArr.length != 16) {
            deserializationContext.mappingException("Can only construct UUIDs from byte[16]; got " + bArr.length + " bytes");
        }
        return new UUID(_long(bArr, 0), _long(bArr, 8));
    }

    private static long _long(byte[] bArr, int i) {
        return ((((long) _int(bArr, i + 4)) << 32) >>> 32) | (((long) _int(bArr, i)) << 32);
    }

    private static int _int(byte[] bArr, int i) {
        return (bArr[i + 3] & 255) | (bArr[i] << 24) | ((bArr[i + 1] & 255) << 16) | ((bArr[i + 2] & 255) << 8);
    }
}
