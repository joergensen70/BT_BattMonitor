package com.gddai.lioncheck.utils;

import com.lidroid.xutils.util.LogUtils;

/* JADX INFO: loaded from: classes.dex */
public class CircularRingUtils {
    public static boolean isCircleCentra(float f, float f2, float f3, float f4, float f5) {
        float f6 = f - f3;
        float f7 = f2 - f3;
        float f8 = f4 - f5;
        return (f6 * f6) + (f7 * f7) < f8 * f8;
    }

    public static boolean isRingCentra(float f, float f2, float f3, float f4, float f5) {
        float f6 = f - f3;
        float f7 = f2 - f3;
        float f8 = (f6 * f6) + (f7 * f7);
        float f9 = f4 - f5;
        if (f8 <= f9 * f9) {
            return false;
        }
        float f10 = f4 + f5;
        return f8 <= f10 * f10;
    }

    public static float getSweepAngle(float f, float f2, float f3) {
        float f4 = f2 - f3;
        float fAsin = (float) ((Math.asin(((double) (f - f3)) / Math.sqrt((r4 * r4) + (f4 * f4))) * 360.0d) / 6.283185307179586d);
        LogUtils.e("sweepAngle" + fAsin);
        return fAsin > 0.0f ? f2 < f3 ? fAsin : 180.0f - fAsin : f2 > f3 ? 180.0f - fAsin : fAsin + 360.0f;
    }
}
