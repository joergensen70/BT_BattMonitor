package com.facebook.imagepipeline.producers;

import com.facebook.common.internal.Preconditions;
import com.facebook.common.logging.FLog;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.request.ImageRequest;

/* JADX INFO: loaded from: classes.dex */
public class DownsampleUtil {
    private static final int DEFAULT_SAMPLE_SIZE = 1;
    private static final float INTERVAL_ROUNDING = 0.33333334f;
    private static final float MAX_BITMAP_SIZE = 2048.0f;

    static int ratioToSampleSizeJPEG(float f) {
        if (f > 0.6666667f) {
            return 1;
        }
        int i = 2;
        while (true) {
            int i2 = i * 2;
            double d = 1.0d / ((double) i2);
            if (d + (0.3333333432674408d * d) <= f) {
                return i;
            }
            i = i2;
        }
    }

    static int roundToPowerOfTwo(int i) {
        int i2 = 1;
        while (i2 < i) {
            i2 *= 2;
        }
        return i2;
    }

    private DownsampleUtil() {
    }

    public static int determineSampleSize(ImageRequest imageRequest, EncodedImage encodedImage) {
        int iRatioToSampleSize;
        if (!EncodedImage.isMetaDataAvailable(encodedImage)) {
            return 1;
        }
        float fDetermineDownsampleRatio = determineDownsampleRatio(imageRequest, encodedImage);
        if (encodedImage.getImageFormat() == ImageFormat.JPEG) {
            iRatioToSampleSize = ratioToSampleSizeJPEG(fDetermineDownsampleRatio);
        } else {
            iRatioToSampleSize = ratioToSampleSize(fDetermineDownsampleRatio);
        }
        int iMax = Math.max(encodedImage.getHeight(), encodedImage.getWidth());
        while (iMax / iRatioToSampleSize > 2048.0f) {
            iRatioToSampleSize = encodedImage.getImageFormat() == ImageFormat.JPEG ? iRatioToSampleSize * 2 : iRatioToSampleSize + 1;
        }
        return iRatioToSampleSize;
    }

    static float determineDownsampleRatio(ImageRequest imageRequest, EncodedImage encodedImage) {
        Preconditions.checkArgument(EncodedImage.isMetaDataAvailable(encodedImage));
        ResizeOptions resizeOptions = imageRequest.getResizeOptions();
        if (resizeOptions == null || resizeOptions.height <= 0 || resizeOptions.width <= 0 || encodedImage.getWidth() == 0 || encodedImage.getHeight() == 0) {
            return 1.0f;
        }
        int rotationAngle = getRotationAngle(imageRequest, encodedImage);
        boolean z = rotationAngle == 90 || rotationAngle == 270;
        int height = z ? encodedImage.getHeight() : encodedImage.getWidth();
        int width = z ? encodedImage.getWidth() : encodedImage.getHeight();
        float f = resizeOptions.width / height;
        float f2 = resizeOptions.height / width;
        float fMax = Math.max(f, f2);
        FLog.v("DownsampleUtil", "Downsample - Specified size: %dx%d, image size: %dx%d ratio: %.1f x %.1f, ratio: %.3f for %s", Integer.valueOf(resizeOptions.width), Integer.valueOf(resizeOptions.height), Integer.valueOf(height), Integer.valueOf(width), Float.valueOf(f), Float.valueOf(f2), Float.valueOf(fMax), imageRequest.getSourceUri().toString());
        return fMax;
    }

    static int ratioToSampleSize(float f) {
        if (f > 0.6666667f) {
            return 1;
        }
        int i = 2;
        while (true) {
            double d = i;
            if ((1.0d / d) + ((1.0d / (Math.pow(d, 2.0d) - d)) * 0.3333333432674408d) <= f) {
                return i - 1;
            }
            i++;
        }
    }

    private static int getRotationAngle(ImageRequest imageRequest, EncodedImage encodedImage) {
        if (!imageRequest.getAutoRotateEnabled()) {
            return 0;
        }
        int rotationAngle = encodedImage.getRotationAngle();
        Preconditions.checkArgument(rotationAngle == 0 || rotationAngle == 90 || rotationAngle == 180 || rotationAngle == 270);
        return rotationAngle;
    }
}
