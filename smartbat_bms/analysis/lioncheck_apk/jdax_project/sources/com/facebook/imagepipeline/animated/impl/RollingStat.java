package com.facebook.imagepipeline.animated.impl;

import android.os.SystemClock;

/* JADX INFO: loaded from: classes.dex */
class RollingStat {
    private static final int WINDOWS = 60;
    private final short[] mStat = new short[60];

    void incrementStats(int i) {
        long jUptimeMillis = SystemClock.uptimeMillis() / 1000;
        int i2 = (int) (jUptimeMillis % 60);
        int i3 = (int) ((jUptimeMillis / 60) & 255);
        short[] sArr = this.mStat;
        short s = sArr[i2];
        int i4 = s & 255;
        if (i3 == ((s >> 8) & 255)) {
            i += i4;
        }
        sArr[i2] = (short) (i | (i3 << 8));
    }

    int getSum(int i) {
        long jUptimeMillis = SystemClock.uptimeMillis() / 1000;
        int i2 = (int) ((jUptimeMillis - ((long) i)) % 60);
        int i3 = (int) ((jUptimeMillis / 60) & 255);
        int i4 = 0;
        for (int i5 = 0; i5 < i; i5++) {
            short s = this.mStat[(i2 + i5) % 60];
            int i6 = s & 255;
            if (((s >> 8) & 255) == i3) {
                i4 += i6;
            }
        }
        return i4;
    }
}
