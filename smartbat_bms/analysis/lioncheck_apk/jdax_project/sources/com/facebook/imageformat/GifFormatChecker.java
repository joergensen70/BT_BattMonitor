package com.facebook.imageformat;

import com.facebook.common.internal.Preconditions;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public final class GifFormatChecker {
    private static final int FRAME_HEADER_SIZE = 10;
    private static final byte[] FRAME_HEADER_START = {0, 33, -7, 4};
    private static final byte[] FRAME_HEADER_END_1 = {0, 44};
    private static final byte[] FRAME_HEADER_END_2 = {0, 33};

    private GifFormatChecker() {
    }

    public static boolean isAnimated(InputStream inputStream) {
        byte[] bArr = new byte[10];
        try {
            inputStream.read(bArr, 0, 10);
            int i = 0;
            int i2 = 0;
            while (inputStream.read(bArr, i, 1) > 0) {
                int i3 = i + 1;
                if (circularBufferMatchesBytePattern(bArr, i3, FRAME_HEADER_START)) {
                    int i4 = i + 9;
                    if ((circularBufferMatchesBytePattern(bArr, i4, FRAME_HEADER_END_1) || circularBufferMatchesBytePattern(bArr, i4, FRAME_HEADER_END_2)) && (i2 = i2 + 1) > 1) {
                        return true;
                    }
                }
                i = i3 % 10;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean circularBufferMatchesBytePattern(byte[] bArr, int i, byte[] bArr2) {
        Preconditions.checkNotNull(bArr);
        Preconditions.checkNotNull(bArr2);
        Preconditions.checkArgument(i >= 0);
        if (bArr2.length > bArr.length) {
            return false;
        }
        for (int i2 = 0; i2 < bArr2.length; i2++) {
            if (bArr[(i2 + i) % bArr.length] != bArr2[i2]) {
                return false;
            }
        }
        return true;
    }
}
