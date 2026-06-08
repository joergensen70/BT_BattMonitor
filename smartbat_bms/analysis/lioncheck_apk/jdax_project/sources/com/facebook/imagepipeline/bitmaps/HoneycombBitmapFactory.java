package com.facebook.imagepipeline.bitmaps;

import android.graphics.Bitmap;
import com.facebook.common.references.CloseableReference;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.platform.PlatformDecoder;

/* JADX INFO: loaded from: classes.dex */
public class HoneycombBitmapFactory extends PlatformBitmapFactory {
    private final EmptyJpegGenerator mJpegGenerator;
    private final PlatformDecoder mPurgeableDecoder;

    public HoneycombBitmapFactory(EmptyJpegGenerator emptyJpegGenerator, PlatformDecoder platformDecoder) {
        this.mJpegGenerator = emptyJpegGenerator;
        this.mPurgeableDecoder = platformDecoder;
    }

    @Override // com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory
    public CloseableReference<Bitmap> createBitmap(int i, int i2, Bitmap.Config config) throws Throwable {
        CloseableReference<PooledByteBuffer> closeableReferenceGenerate = this.mJpegGenerator.generate((short) i, (short) i2);
        try {
            EncodedImage encodedImage = new EncodedImage(closeableReferenceGenerate);
            encodedImage.setImageFormat(ImageFormat.JPEG);
            try {
                CloseableReference<Bitmap> closeableReferenceDecodeJPEGFromEncodedImage = this.mPurgeableDecoder.decodeJPEGFromEncodedImage(encodedImage, config, closeableReferenceGenerate.get().size());
                closeableReferenceDecodeJPEGFromEncodedImage.get().eraseColor(0);
                return closeableReferenceDecodeJPEGFromEncodedImage;
            } finally {
                EncodedImage.closeSafely(encodedImage);
            }
        } finally {
            closeableReferenceGenerate.close();
        }
    }
}
