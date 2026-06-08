package com.facebook.imageutils;

import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
class StreamProcessor {
    StreamProcessor() {
    }

    public static int readPackedInt(InputStream inputStream, int i, boolean z) throws IOException {
        int i2;
        int i3 = 0;
        for (int i4 = 0; i4 < i; i4++) {
            int i5 = inputStream.read();
            if (i5 == -1) {
                throw new IOException("no more bytes");
            }
            if (z) {
                i2 = (i5 & 255) << (i4 * 8);
            } else {
                i3 <<= 8;
                i2 = i5 & 255;
            }
            i3 |= i2;
        }
        return i3;
    }
}
