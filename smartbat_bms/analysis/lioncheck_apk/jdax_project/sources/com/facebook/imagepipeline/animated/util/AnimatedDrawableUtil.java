package com.facebook.imagepipeline.animated.util;

import android.graphics.Bitmap;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class AnimatedDrawableUtil {
    private static final int FRAME_DURATION_MS_FOR_MIN = 100;
    private static final int MIN_FRAME_DURATION_MS = 11;

    public static boolean isOutsideRange(int i, int i2, int i3) {
        if (i == -1 || i2 == -1) {
            return true;
        }
        return i <= i2 ? i3 < i || i3 > i2 : i3 < i && i3 > i2;
    }

    public void appendMemoryString(StringBuilder sb, int i) {
        if (i < 1024) {
            sb.append(i);
            sb.append("KB");
            return;
        }
        sb.append(i / 1024);
        sb.append(".");
        sb.append((i % 1024) / 100);
        sb.append("MB");
    }

    public void fixFrameDurations(int[] iArr) {
        for (int i = 0; i < iArr.length; i++) {
            if (iArr[i] < 11) {
                iArr[i] = 100;
            }
        }
    }

    public int getTotalDurationFromFrameDurations(int[] iArr) {
        int i = 0;
        for (int i2 : iArr) {
            i += i2;
        }
        return i;
    }

    public int[] getFrameTimeStampsFromDurations(int[] iArr) {
        int[] iArr2 = new int[iArr.length];
        int i = 0;
        for (int i2 = 0; i2 < iArr.length; i2++) {
            iArr2[i2] = i;
            i += iArr[i2];
        }
        return iArr2;
    }

    public int getFrameForTimestampMs(int[] iArr, int i) {
        int iBinarySearch = Arrays.binarySearch(iArr, i);
        return iBinarySearch < 0 ? (-iBinarySearch) - 2 : iBinarySearch;
    }

    public int getSizeOfBitmap(Bitmap bitmap) {
        return bitmap.getAllocationByteCount();
    }
}
