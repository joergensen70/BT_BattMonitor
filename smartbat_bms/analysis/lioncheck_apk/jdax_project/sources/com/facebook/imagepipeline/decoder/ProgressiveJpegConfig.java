package com.facebook.imagepipeline.decoder;

import com.facebook.imagepipeline.image.QualityInfo;

/* JADX INFO: loaded from: classes.dex */
public interface ProgressiveJpegConfig {
    int getNextScanNumberToDecode(int i);

    QualityInfo getQualityInfo(int i);
}
