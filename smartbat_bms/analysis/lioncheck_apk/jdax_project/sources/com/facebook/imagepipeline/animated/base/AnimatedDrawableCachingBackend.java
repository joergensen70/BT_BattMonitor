package com.facebook.imagepipeline.animated.base;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.facebook.common.references.CloseableReference;

/* JADX INFO: loaded from: classes.dex */
public interface AnimatedDrawableCachingBackend extends AnimatedDrawableBackend {
    void appendDebugOptionString(StringBuilder sb);

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend
    AnimatedDrawableCachingBackend forNewBounds(Rect rect);

    CloseableReference<Bitmap> getBitmapForFrame(int i);

    CloseableReference<Bitmap> getPreviewBitmap();
}
