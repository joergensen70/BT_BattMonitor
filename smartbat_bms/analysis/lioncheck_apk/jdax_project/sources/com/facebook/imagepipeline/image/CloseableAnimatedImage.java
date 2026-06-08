package com.facebook.imagepipeline.image;

import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;

/* JADX INFO: loaded from: classes.dex */
public class CloseableAnimatedImage extends CloseableImage {
    private AnimatedImageResult mImageResult;

    @Override // com.facebook.imagepipeline.image.CloseableImage
    public boolean isStateful() {
        return true;
    }

    public CloseableAnimatedImage(AnimatedImageResult animatedImageResult) {
        this.mImageResult = animatedImageResult;
    }

    @Override // com.facebook.imagepipeline.image.ImageInfo
    public synchronized int getWidth() {
        return isClosed() ? 0 : this.mImageResult.getImage().getWidth();
    }

    @Override // com.facebook.imagepipeline.image.ImageInfo
    public synchronized int getHeight() {
        return isClosed() ? 0 : this.mImageResult.getImage().getHeight();
    }

    @Override // com.facebook.imagepipeline.image.CloseableImage, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        synchronized (this) {
            AnimatedImageResult animatedImageResult = this.mImageResult;
            if (animatedImageResult == null) {
                return;
            }
            this.mImageResult = null;
            animatedImageResult.dispose();
        }
    }

    @Override // com.facebook.imagepipeline.image.CloseableImage
    public synchronized boolean isClosed() {
        return this.mImageResult == null;
    }

    @Override // com.facebook.imagepipeline.image.CloseableImage
    public synchronized int getSizeInBytes() {
        return isClosed() ? 0 : this.mImageResult.getImage().getSizeInBytes();
    }

    public synchronized AnimatedImageResult getImageResult() {
        return this.mImageResult;
    }

    public synchronized AnimatedImage getImage() {
        return isClosed() ? null : this.mImageResult.getImage();
    }
}
