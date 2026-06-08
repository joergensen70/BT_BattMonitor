package com.facebook.imagepipeline.image;

import android.graphics.Bitmap;

/* JADX INFO: loaded from: classes.dex */
public abstract class CloseableBitmap extends CloseableImage {
    public abstract Bitmap getUnderlyingBitmap();
}
