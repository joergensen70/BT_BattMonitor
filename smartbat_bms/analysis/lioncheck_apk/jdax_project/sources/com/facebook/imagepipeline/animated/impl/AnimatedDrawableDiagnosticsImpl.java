package com.facebook.imagepipeline.animated.impl;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableCachingBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics;
import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;

/* JADX INFO: loaded from: classes.dex */
public class AnimatedDrawableDiagnosticsImpl implements AnimatedDrawableDiagnostics {
    private static final Class<?> TAG = AnimatedDrawableDiagnostics.class;
    private AnimatedDrawableCachingBackend mAnimatedDrawableBackend;
    private final AnimatedDrawableUtil mAnimatedDrawableUtil;
    private final TextPaint mDebugTextPaint;
    private final DisplayMetrics mDisplayMetrics;
    private long mLastTimeStamp;
    private final RollingStat mDroppedFramesStat = new RollingStat();
    private final RollingStat mDrawnFrames = new RollingStat();
    private final StringBuilder sbTemp = new StringBuilder();

    public AnimatedDrawableDiagnosticsImpl(AnimatedDrawableUtil animatedDrawableUtil, DisplayMetrics displayMetrics) {
        this.mAnimatedDrawableUtil = animatedDrawableUtil;
        this.mDisplayMetrics = displayMetrics;
        TextPaint textPaint = new TextPaint();
        this.mDebugTextPaint = textPaint;
        textPaint.setColor(-16776961);
        textPaint.setTextSize(convertDpToPx(14));
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void setBackend(AnimatedDrawableCachingBackend animatedDrawableCachingBackend) {
        this.mAnimatedDrawableBackend = animatedDrawableCachingBackend;
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void onStartMethodBegin() {
        this.mLastTimeStamp = SystemClock.elapsedRealtime();
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void onStartMethodEnd() {
        long jElapsedRealtime = SystemClock.elapsedRealtime() - this.mLastTimeStamp;
        if (jElapsedRealtime > 3) {
            FLog.v(TAG, "onStart took %d", Long.valueOf(jElapsedRealtime));
        }
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void onNextFrameMethodBegin() {
        this.mLastTimeStamp = SystemClock.elapsedRealtime();
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void onNextFrameMethodEnd() {
        long jElapsedRealtime = SystemClock.elapsedRealtime() - this.mLastTimeStamp;
        if (jElapsedRealtime > 3) {
            FLog.v(TAG, "onNextFrame took %d", Long.valueOf(jElapsedRealtime));
        }
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void incrementDroppedFrames(int i) {
        this.mDroppedFramesStat.incrementStats(i);
        if (i > 0) {
            FLog.v(TAG, "Dropped %d frames", Integer.valueOf(i));
        }
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void incrementDrawnFrames(int i) {
        this.mDrawnFrames.incrementStats(i);
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void onDrawMethodBegin() {
        this.mLastTimeStamp = SystemClock.elapsedRealtime();
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void onDrawMethodEnd() {
        FLog.v(TAG, "draw took %d", Long.valueOf(SystemClock.elapsedRealtime() - this.mLastTimeStamp));
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics
    public void drawDebugOverlay(Canvas canvas, Rect rect) {
        int iMeasureText;
        int sum = this.mDroppedFramesStat.getSum(10);
        int sum2 = this.mDrawnFrames.getSum(10);
        int i = sum + sum2;
        int iConvertDpToPx = convertDpToPx(10);
        int iConvertDpToPx2 = convertDpToPx(20);
        int iConvertDpToPx3 = convertDpToPx(5);
        if (i > 0) {
            this.sbTemp.setLength(0);
            this.sbTemp.append((sum2 * 100) / i);
            this.sbTemp.append("%");
            StringBuilder sb = this.sbTemp;
            float f = iConvertDpToPx;
            canvas.drawText(sb, 0, sb.length(), f, iConvertDpToPx2, this.mDebugTextPaint);
            TextPaint textPaint = this.mDebugTextPaint;
            StringBuilder sb2 = this.sbTemp;
            iMeasureText = ((int) (f + textPaint.measureText(sb2, 0, sb2.length()))) + iConvertDpToPx3;
        } else {
            iMeasureText = iConvertDpToPx;
        }
        int memoryUsage = this.mAnimatedDrawableBackend.getMemoryUsage();
        this.sbTemp.setLength(0);
        this.mAnimatedDrawableUtil.appendMemoryString(this.sbTemp, memoryUsage);
        TextPaint textPaint2 = this.mDebugTextPaint;
        StringBuilder sb3 = this.sbTemp;
        float fMeasureText = textPaint2.measureText(sb3, 0, sb3.length());
        if (iMeasureText + fMeasureText > rect.width()) {
            iConvertDpToPx2 = (int) (iConvertDpToPx2 + this.mDebugTextPaint.getTextSize() + iConvertDpToPx3);
            iMeasureText = iConvertDpToPx;
        }
        StringBuilder sb4 = this.sbTemp;
        float f2 = iMeasureText;
        float f3 = iConvertDpToPx2;
        canvas.drawText(sb4, 0, sb4.length(), f2, f3, this.mDebugTextPaint);
        int i2 = ((int) (f2 + fMeasureText)) + iConvertDpToPx3;
        this.sbTemp.setLength(0);
        this.mAnimatedDrawableBackend.appendDebugOptionString(this.sbTemp);
        TextPaint textPaint3 = this.mDebugTextPaint;
        StringBuilder sb5 = this.sbTemp;
        if (i2 + textPaint3.measureText(sb5, 0, sb5.length()) > rect.width()) {
            iConvertDpToPx2 = (int) (f3 + this.mDebugTextPaint.getTextSize() + iConvertDpToPx3);
        } else {
            iConvertDpToPx = i2;
        }
        StringBuilder sb6 = this.sbTemp;
        canvas.drawText(sb6, 0, sb6.length(), iConvertDpToPx, iConvertDpToPx2, this.mDebugTextPaint);
    }

    private int convertDpToPx(int i) {
        return (int) TypedValue.applyDimension(1, i, this.mDisplayMetrics);
    }
}
