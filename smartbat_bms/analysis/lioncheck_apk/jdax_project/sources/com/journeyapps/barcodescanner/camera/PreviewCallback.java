package com.journeyapps.barcodescanner.camera;

import com.journeyapps.barcodescanner.SourceData;

/* JADX INFO: loaded from: classes.dex */
public interface PreviewCallback {
    void onPreview(SourceData sourceData);

    void onPreviewError(Exception exc);
}
