package com.facebook.imagepipeline.animated.factory;

import android.graphics.Bitmap;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableBackendProvider;
import com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.gif.GifImage;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.webp.WebPImage;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class AnimatedImageFactory {
    private final AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    private final PlatformBitmapFactory mBitmapFactory;

    public AnimatedImageFactory(AnimatedDrawableBackendProvider animatedDrawableBackendProvider, PlatformBitmapFactory platformBitmapFactory) {
        this.mAnimatedDrawableBackendProvider = animatedDrawableBackendProvider;
        this.mBitmapFactory = platformBitmapFactory;
    }

    public CloseableImage decodeGif(EncodedImage encodedImage, ImageDecodeOptions imageDecodeOptions, Bitmap.Config config) {
        CloseableReference<PooledByteBuffer> byteBufferRef = encodedImage.getByteBufferRef();
        Preconditions.checkNotNull(byteBufferRef);
        try {
            Preconditions.checkState(!imageDecodeOptions.forceOldAnimationCode);
            PooledByteBuffer pooledByteBuffer = byteBufferRef.get();
            return getCloseableImage(imageDecodeOptions, GifImage.create(pooledByteBuffer.getNativePtr(), pooledByteBuffer.size()), config);
        } finally {
            CloseableReference.closeSafely(byteBufferRef);
        }
    }

    public CloseableImage decodeWebP(EncodedImage encodedImage, ImageDecodeOptions imageDecodeOptions, Bitmap.Config config) {
        CloseableReference<PooledByteBuffer> byteBufferRef = encodedImage.getByteBufferRef();
        Preconditions.checkNotNull(byteBufferRef);
        try {
            Preconditions.checkArgument(!imageDecodeOptions.forceOldAnimationCode);
            PooledByteBuffer pooledByteBuffer = byteBufferRef.get();
            return getCloseableImage(imageDecodeOptions, WebPImage.create(pooledByteBuffer.getNativePtr(), pooledByteBuffer.size()), config);
        } finally {
            CloseableReference.closeSafely(byteBufferRef);
        }
    }

    private CloseableAnimatedImage getCloseableImage(ImageDecodeOptions imageDecodeOptions, AnimatedImage animatedImage, Bitmap.Config config) throws Throwable {
        List<CloseableReference<Bitmap>> listDecodeAllFrames;
        CloseableReference<Bitmap> closeableReferenceCreatePreviewBitmap = null;
        try {
            int frameCount = imageDecodeOptions.useLastFrameForPreview ? animatedImage.getFrameCount() - 1 : 0;
            if (imageDecodeOptions.decodeAllFrames) {
                listDecodeAllFrames = decodeAllFrames(animatedImage, config);
                try {
                    closeableReferenceCreatePreviewBitmap = CloseableReference.cloneOrNull(listDecodeAllFrames.get(frameCount));
                } catch (Throwable th) {
                    th = th;
                    CloseableReference.closeSafely(closeableReferenceCreatePreviewBitmap);
                    CloseableReference.closeSafely(listDecodeAllFrames);
                    throw th;
                }
            } else {
                listDecodeAllFrames = null;
            }
            if (imageDecodeOptions.decodePreviewFrame && closeableReferenceCreatePreviewBitmap == null) {
                closeableReferenceCreatePreviewBitmap = createPreviewBitmap(animatedImage, config, frameCount);
            }
            CloseableAnimatedImage closeableAnimatedImage = new CloseableAnimatedImage(AnimatedImageResult.newBuilder(animatedImage).setPreviewBitmap(closeableReferenceCreatePreviewBitmap).setFrameForPreview(frameCount).setDecodedFrames(listDecodeAllFrames).build());
            CloseableReference.closeSafely(closeableReferenceCreatePreviewBitmap);
            CloseableReference.closeSafely(listDecodeAllFrames);
            return closeableAnimatedImage;
        } catch (Throwable th2) {
            th = th2;
            listDecodeAllFrames = null;
        }
    }

    private CloseableReference<Bitmap> createPreviewBitmap(AnimatedImage animatedImage, Bitmap.Config config, int i) {
        CloseableReference<Bitmap> closeableReferenceCreateBitmap = createBitmap(animatedImage.getWidth(), animatedImage.getHeight(), config);
        new AnimatedImageCompositor(this.mAnimatedDrawableBackendProvider.get(AnimatedImageResult.forAnimatedImage(animatedImage), null), new AnimatedImageCompositor.Callback() { // from class: com.facebook.imagepipeline.animated.factory.AnimatedImageFactory.1
            @Override // com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor.Callback
            public CloseableReference<Bitmap> getCachedBitmap(int i2) {
                return null;
            }

            @Override // com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor.Callback
            public void onIntermediateResult(int i2, Bitmap bitmap) {
            }
        }).renderFrame(i, closeableReferenceCreateBitmap.get());
        return closeableReferenceCreateBitmap;
    }

    private List<CloseableReference<Bitmap>> decodeAllFrames(AnimatedImage animatedImage, Bitmap.Config config) {
        final ArrayList arrayList = new ArrayList();
        AnimatedDrawableBackend animatedDrawableBackend = this.mAnimatedDrawableBackendProvider.get(AnimatedImageResult.forAnimatedImage(animatedImage), null);
        AnimatedImageCompositor animatedImageCompositor = new AnimatedImageCompositor(animatedDrawableBackend, new AnimatedImageCompositor.Callback() { // from class: com.facebook.imagepipeline.animated.factory.AnimatedImageFactory.2
            @Override // com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor.Callback
            public void onIntermediateResult(int i, Bitmap bitmap) {
            }

            @Override // com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor.Callback
            public CloseableReference<Bitmap> getCachedBitmap(int i) {
                return CloseableReference.cloneOrNull((CloseableReference) arrayList.get(i));
            }
        });
        for (int i = 0; i < animatedDrawableBackend.getFrameCount(); i++) {
            CloseableReference<Bitmap> closeableReferenceCreateBitmap = createBitmap(animatedDrawableBackend.getWidth(), animatedDrawableBackend.getHeight(), config);
            animatedImageCompositor.renderFrame(i, closeableReferenceCreateBitmap.get());
            arrayList.add(closeableReferenceCreateBitmap);
        }
        return arrayList;
    }

    private CloseableReference<Bitmap> createBitmap(int i, int i2, Bitmap.Config config) {
        CloseableReference<Bitmap> closeableReferenceCreateBitmap = this.mBitmapFactory.createBitmap(i, i2, config);
        closeableReferenceCreateBitmap.get().eraseColor(0);
        closeableReferenceCreateBitmap.get().setHasAlpha(true);
        return closeableReferenceCreateBitmap;
    }
}
