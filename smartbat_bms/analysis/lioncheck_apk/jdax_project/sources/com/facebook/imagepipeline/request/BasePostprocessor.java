package com.facebook.imagepipeline.request;

import android.graphics.Bitmap;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public abstract class BasePostprocessor implements Postprocessor {
    @Override // com.facebook.imagepipeline.request.Postprocessor
    @Nullable
    public CacheKey getPostprocessorCacheKey() {
        return null;
    }

    public void process(Bitmap bitmap) {
    }

    @Override // com.facebook.imagepipeline.request.Postprocessor
    public String getName() {
        return "Unknown postprocessor";
    }

    @Override // com.facebook.imagepipeline.request.Postprocessor
    public CloseableReference<Bitmap> process(Bitmap bitmap, PlatformBitmapFactory platformBitmapFactory) {
        CloseableReference<Bitmap> closeableReferenceCreateBitmap = platformBitmapFactory.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        try {
            process(closeableReferenceCreateBitmap.get(), bitmap);
            return CloseableReference.cloneOrNull(closeableReferenceCreateBitmap);
        } finally {
            CloseableReference.closeSafely(closeableReferenceCreateBitmap);
        }
    }

    public void process(Bitmap bitmap, Bitmap bitmap2) {
        Bitmaps.copyBitmap(bitmap, bitmap2);
        process(bitmap);
    }
}
