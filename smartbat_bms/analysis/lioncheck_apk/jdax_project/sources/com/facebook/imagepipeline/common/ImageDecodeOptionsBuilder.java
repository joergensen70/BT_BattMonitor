package com.facebook.imagepipeline.common;

/* JADX INFO: loaded from: classes.dex */
public class ImageDecodeOptionsBuilder {
    private boolean mDecodeAllFrames;
    private boolean mDecodePreviewFrame;
    private boolean mForceOldAnimationCode;
    private boolean mUseLastFrameForPreview;
    private int mMinDecodeIntervalMs = 100;
    private int mBackgroundColor = -1;

    ImageDecodeOptionsBuilder() {
    }

    public ImageDecodeOptionsBuilder setFrom(ImageDecodeOptions imageDecodeOptions) {
        this.mBackgroundColor = imageDecodeOptions.backgroundColor;
        this.mForceOldAnimationCode = imageDecodeOptions.forceOldAnimationCode;
        this.mDecodePreviewFrame = imageDecodeOptions.decodePreviewFrame;
        this.mUseLastFrameForPreview = imageDecodeOptions.useLastFrameForPreview;
        this.mDecodeAllFrames = imageDecodeOptions.decodeAllFrames;
        return this;
    }

    public ImageDecodeOptionsBuilder setMinDecodeIntervalMs(int i) {
        this.mMinDecodeIntervalMs = i;
        return this;
    }

    public int getMinDecodeIntervalMs() {
        return this.mMinDecodeIntervalMs;
    }

    public ImageDecodeOptionsBuilder setBackgroundColor(int i) {
        this.mBackgroundColor = i;
        return this;
    }

    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public ImageDecodeOptionsBuilder setForceOldAnimationCode(boolean z) {
        this.mForceOldAnimationCode = z;
        return this;
    }

    public boolean getForceOldAnimationCode() {
        return this.mForceOldAnimationCode;
    }

    public ImageDecodeOptionsBuilder setDecodePreviewFrame(boolean z) {
        this.mDecodePreviewFrame = z;
        return this;
    }

    public boolean getDecodePreviewFrame() {
        return this.mDecodePreviewFrame;
    }

    public boolean getUseLastFrameForPreview() {
        return this.mUseLastFrameForPreview;
    }

    public ImageDecodeOptionsBuilder setUseLastFrameForPreview(boolean z) {
        this.mUseLastFrameForPreview = z;
        return this;
    }

    public boolean getDecodeAllFrames() {
        return this.mDecodeAllFrames;
    }

    public ImageDecodeOptionsBuilder setDecodeAllFrames(boolean z) {
        this.mDecodeAllFrames = z;
        return this;
    }

    public ImageDecodeOptions build() {
        return new ImageDecodeOptions(this);
    }
}
