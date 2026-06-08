package com.facebook.imagepipeline.animated.impl;

import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;

/* JADX INFO: loaded from: classes.dex */
class WhatToKeepCachedArray {
    private final boolean[] mData;

    WhatToKeepCachedArray(int i) {
        this.mData = new boolean[i];
    }

    boolean get(int i) {
        return this.mData[i];
    }

    void setAll(boolean z) {
        int i = 0;
        while (true) {
            boolean[] zArr = this.mData;
            if (i >= zArr.length) {
                return;
            }
            zArr[i] = z;
            i++;
        }
    }

    void removeOutsideRange(int i, int i2) {
        for (int i3 = 0; i3 < this.mData.length; i3++) {
            if (AnimatedDrawableUtil.isOutsideRange(i, i2, i3)) {
                this.mData[i3] = false;
            }
        }
    }

    void set(int i, boolean z) {
        this.mData[i] = z;
    }
}
