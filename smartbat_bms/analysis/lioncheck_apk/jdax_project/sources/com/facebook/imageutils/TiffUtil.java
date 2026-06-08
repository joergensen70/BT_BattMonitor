package com.facebook.imageutils;

import com.facebook.common.logging.FLog;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
class TiffUtil {
    private static final Class<?> TAG = TiffUtil.class;
    public static final int TIFF_BYTE_ORDER_BIG_END = 1296891946;
    public static final int TIFF_BYTE_ORDER_LITTLE_END = 1229531648;
    public static final int TIFF_TAG_ORIENTATION = 274;
    public static final int TIFF_TYPE_SHORT = 3;

    TiffUtil() {
    }

    public static int getAutoRotateAngleFromOrientation(int i) {
        if (i == 1) {
            return 0;
        }
        if (i == 3) {
            return 180;
        }
        if (i == 6) {
            return 90;
        }
        if (i == 8) {
            return 270;
        }
        FLog.i(TAG, "Unsupported orientation");
        return 0;
    }

    public static int readOrientationFromTIFF(InputStream inputStream, int i) throws IOException {
        TiffHeader tiffHeader = new TiffHeader();
        int tiffHeader2 = readTiffHeader(inputStream, i, tiffHeader);
        int i2 = tiffHeader.firstIfdOffset - 8;
        if (tiffHeader2 == 0 || i2 > tiffHeader2) {
            return 0;
        }
        inputStream.skip(i2);
        return getOrientationFromTiffEntry(inputStream, moveToTiffEntryWithTag(inputStream, tiffHeader2 - i2, tiffHeader.isLittleEndian, TIFF_TAG_ORIENTATION), tiffHeader.isLittleEndian);
    }

    private static class TiffHeader {
        int byteOrder;
        int firstIfdOffset;
        boolean isLittleEndian;

        private TiffHeader() {
        }
    }

    private static int readTiffHeader(InputStream inputStream, int i, TiffHeader tiffHeader) throws IOException {
        if (i <= 8) {
            return 0;
        }
        tiffHeader.byteOrder = StreamProcessor.readPackedInt(inputStream, 4, false);
        if (tiffHeader.byteOrder != 1229531648 && tiffHeader.byteOrder != 1296891946) {
            FLog.e(TAG, "Invalid TIFF header");
            return 0;
        }
        tiffHeader.isLittleEndian = tiffHeader.byteOrder == 1229531648;
        tiffHeader.firstIfdOffset = StreamProcessor.readPackedInt(inputStream, 4, tiffHeader.isLittleEndian);
        int i2 = i - 8;
        if (tiffHeader.firstIfdOffset >= 8 && tiffHeader.firstIfdOffset - 8 <= i2) {
            return i2;
        }
        FLog.e(TAG, "Invalid offset");
        return 0;
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x0027, code lost:
    
        return 0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static int moveToTiffEntryWithTag(java.io.InputStream r6, int r7, boolean r8, int r9) throws java.io.IOException {
        /*
            r0 = 14
            r1 = 0
            if (r7 >= r0) goto L6
            return r1
        L6:
            r0 = 2
            int r2 = com.facebook.imageutils.StreamProcessor.readPackedInt(r6, r0, r8)
            int r7 = r7 + (-2)
        Ld:
            int r3 = r2 + (-1)
            if (r2 <= 0) goto L27
            r2 = 12
            if (r7 < r2) goto L27
            int r2 = com.facebook.imageutils.StreamProcessor.readPackedInt(r6, r0, r8)
            int r4 = r7 + (-2)
            if (r2 != r9) goto L1e
            return r4
        L1e:
            r4 = 10
            r6.skip(r4)
            int r7 = r7 + (-12)
            r2 = r3
            goto Ld
        L27:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.imageutils.TiffUtil.moveToTiffEntryWithTag(java.io.InputStream, int, boolean, int):int");
    }

    private static int getOrientationFromTiffEntry(InputStream inputStream, int i, boolean z) throws IOException {
        if (i < 10 || StreamProcessor.readPackedInt(inputStream, 2, z) != 3 || StreamProcessor.readPackedInt(inputStream, 4, z) != 1) {
            return 0;
        }
        int packedInt = StreamProcessor.readPackedInt(inputStream, 2, z);
        StreamProcessor.readPackedInt(inputStream, 2, z);
        return packedInt;
    }
}
