package com.gddai.lioncheck.utils;

/* JADX INFO: loaded from: classes.dex */
public class ProgessUtils {
    public static int getVigor(float f, float f2, float f3, int i, float f4) {
        float f5 = f2 + f3;
        for (int i2 = 0; i2 < i; i2++) {
            float f6 = f4 * f3;
            if (f >= f5 - f6 && f <= f6 + f5) {
                return i2;
            }
            f5 += (2.0f * f3) + f2;
        }
        return -1;
    }
}
