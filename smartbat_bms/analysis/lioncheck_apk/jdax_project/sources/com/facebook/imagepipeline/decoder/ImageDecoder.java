package com.facebook.imagepipeline.decoder;

import android.graphics.Bitmap;
import com.facebook.common.internal.Closeables;
import com.facebook.common.references.CloseableReference;
import com.facebook.imageformat.GifFormatChecker;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imageformat.ImageFormatChecker;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.platform.PlatformDecoder;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class ImageDecoder {
    private final AnimatedImageFactory mAnimatedImageFactory;
    private final Bitmap.Config mBitmapConfig;
    private final PlatformDecoder mPlatformDecoder;

    public ImageDecoder(AnimatedImageFactory animatedImageFactory, PlatformDecoder platformDecoder, Bitmap.Config config) {
        this.mAnimatedImageFactory = animatedImageFactory;
        this.mBitmapConfig = config;
        this.mPlatformDecoder = platformDecoder;
    }

    public CloseableImage decodeImage(EncodedImage encodedImage, int i, QualityInfo qualityInfo, ImageDecodeOptions imageDecodeOptions) {
        ImageFormat imageFormat = encodedImage.getImageFormat();
        if (imageFormat == null || imageFormat == ImageFormat.UNKNOWN) {
            imageFormat = ImageFormatChecker.getImageFormat_WrapIOException(encodedImage.getInputStream());
        }
        int i2 = AnonymousClass1.$SwitchMap$com$facebook$imageformat$ImageFormat[imageFormat.ordinal()];
        if (i2 == 1) {
            throw new IllegalArgumentException("unknown image format");
        }
        if (i2 == 2) {
            return decodeJpeg(encodedImage, i, qualityInfo);
        }
        if (i2 == 3) {
            return decodeGif(encodedImage, imageDecodeOptions);
        }
        if (i2 == 4) {
            return decodeAnimatedWebp(encodedImage, imageDecodeOptions);
        }
        return decodeStaticImage(encodedImage);
    }

    /* JADX INFO: renamed from: com.facebook.imagepipeline.decoder.ImageDecoder$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$facebook$imageformat$ImageFormat;

        static {
            int[] iArr = new int[ImageFormat.values().length];
            $SwitchMap$com$facebook$imageformat$ImageFormat = iArr;
            try {
                iArr[ImageFormat.UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$facebook$imageformat$ImageFormat[ImageFormat.JPEG.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$facebook$imageformat$ImageFormat[ImageFormat.GIF.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$facebook$imageformat$ImageFormat[ImageFormat.WEBP_ANIMATED.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public CloseableImage decodeGif(EncodedImage encodedImage, ImageDecodeOptions imageDecodeOptions) {
        InputStream inputStream = encodedImage.getInputStream();
        if (inputStream == null) {
            return null;
        }
        try {
            if (GifFormatChecker.isAnimated(inputStream)) {
                return this.mAnimatedImageFactory.decodeGif(encodedImage, imageDecodeOptions, this.mBitmapConfig);
            }
            return decodeStaticImage(encodedImage);
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    public CloseableStaticBitmap decodeStaticImage(EncodedImage encodedImage) {
        CloseableReference<Bitmap> closeableReferenceDecodeFromEncodedImage = this.mPlatformDecoder.decodeFromEncodedImage(encodedImage, this.mBitmapConfig);
        try {
            return new CloseableStaticBitmap(closeableReferenceDecodeFromEncodedImage, ImmutableQualityInfo.FULL_QUALITY, encodedImage.getRotationAngle());
        } finally {
            closeableReferenceDecodeFromEncodedImage.close();
        }
    }

    public CloseableStaticBitmap decodeJpeg(EncodedImage encodedImage, int i, QualityInfo qualityInfo) {
        CloseableReference<Bitmap> closeableReferenceDecodeJPEGFromEncodedImage = this.mPlatformDecoder.decodeJPEGFromEncodedImage(encodedImage, this.mBitmapConfig, i);
        try {
            return new CloseableStaticBitmap(closeableReferenceDecodeJPEGFromEncodedImage, qualityInfo, encodedImage.getRotationAngle());
        } finally {
            closeableReferenceDecodeJPEGFromEncodedImage.close();
        }
    }

    public CloseableImage decodeAnimatedWebp(EncodedImage encodedImage, ImageDecodeOptions imageDecodeOptions) {
        return this.mAnimatedImageFactory.decodeWebP(encodedImage, imageDecodeOptions, this.mBitmapConfig);
    }
}
