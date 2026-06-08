package com.facebook.imagepipeline.animated.base;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;
import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.time.MonotonicClock;
import com.facebook.drawable.base.DrawableWithCaches;
import com.nineoldandroids.animation.ValueAnimator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes.dex */
public class AnimatedDrawable extends Drawable implements AnimatableDrawable, DrawableWithCaches {
    private static final int NO_FRAME = -1;
    private static final int POLL_FOR_RENDERED_FRAME_MS = 5;
    private static final Class<?> TAG = AnimatedDrawable.class;
    private static final long WATCH_DOG_TIMER_MIN_TIMEOUT_MS = 1000;
    private static final long WATCH_DOG_TIMER_POLL_INTERVAL_MS = 2000;
    private AnimatedDrawableCachingBackend mAnimatedDrawableBackend;
    private final AnimatedDrawableDiagnostics mAnimatedDrawableDiagnostics;
    private boolean mApplyTransformation;
    private final int mDurationMs;
    private final int mFrameCount;
    private boolean mHaveWatchdogScheduled;
    private boolean mInvalidateTaskScheduled;
    private boolean mIsRunning;
    private CloseableReference<Bitmap> mLastDrawnFrame;
    private volatile String mLogId;
    private final MonotonicClock mMonotonicClock;
    private int mPendingRenderedFrameMonotonicNumber;
    private int mPendingRenderedFrameNumber;
    private final ScheduledExecutorService mScheduledExecutorServiceForUiThread;
    private int mScheduledFrameMonotonicNumber;
    private int mScheduledFrameNumber;
    private long mStartTimeMs;
    private final int mTotalLoops;
    private final Paint mTransparentPaint;
    private boolean mWaitingForDraw;
    private final Paint mPaint = new Paint(6);
    private final Rect mDstRect = new Rect();
    private int mLastDrawnFrameNumber = -1;
    private int mLastDrawnFrameMonotonicNumber = -1;
    private long mLastInvalidateTimeMs = -1;
    private float mSx = 1.0f;
    private float mSy = 1.0f;
    private long mNextFrameTaskMs = -1;
    private final Runnable mStartTask = new Runnable() { // from class: com.facebook.imagepipeline.animated.base.AnimatedDrawable.1
        @Override // java.lang.Runnable
        public void run() {
            AnimatedDrawable.this.onStart();
        }
    };
    private final Runnable mNextFrameTask = new Runnable() { // from class: com.facebook.imagepipeline.animated.base.AnimatedDrawable.2
        @Override // java.lang.Runnable
        public void run() {
            FLog.v((Class<?>) AnimatedDrawable.TAG, "(%s) Next Frame Task", AnimatedDrawable.this.mLogId);
            AnimatedDrawable.this.onNextFrame();
        }
    };
    private final Runnable mInvalidateTask = new Runnable() { // from class: com.facebook.imagepipeline.animated.base.AnimatedDrawable.3
        @Override // java.lang.Runnable
        public void run() {
            FLog.v((Class<?>) AnimatedDrawable.TAG, "(%s) Invalidate Task", AnimatedDrawable.this.mLogId);
            AnimatedDrawable.this.mInvalidateTaskScheduled = false;
            AnimatedDrawable.this.doInvalidateSelf();
        }
    };
    private final Runnable mWatchdogTask = new Runnable() { // from class: com.facebook.imagepipeline.animated.base.AnimatedDrawable.4
        @Override // java.lang.Runnable
        public void run() {
            FLog.v((Class<?>) AnimatedDrawable.TAG, "(%s) Watchdog Task", AnimatedDrawable.this.mLogId);
            AnimatedDrawable.this.doWatchdogCheck();
        }
    };

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    public AnimatedDrawable(ScheduledExecutorService scheduledExecutorService, AnimatedDrawableCachingBackend animatedDrawableCachingBackend, AnimatedDrawableDiagnostics animatedDrawableDiagnostics, MonotonicClock monotonicClock) {
        this.mScheduledExecutorServiceForUiThread = scheduledExecutorService;
        this.mAnimatedDrawableBackend = animatedDrawableCachingBackend;
        this.mAnimatedDrawableDiagnostics = animatedDrawableDiagnostics;
        this.mMonotonicClock = monotonicClock;
        this.mDurationMs = animatedDrawableCachingBackend.getDurationMs();
        this.mFrameCount = this.mAnimatedDrawableBackend.getFrameCount();
        animatedDrawableDiagnostics.setBackend(this.mAnimatedDrawableBackend);
        this.mTotalLoops = this.mAnimatedDrawableBackend.getLoopCount();
        Paint paint = new Paint();
        this.mTransparentPaint = paint;
        paint.setColor(0);
        paint.setStyle(Paint.Style.FILL);
        resetToPreviewFrame();
    }

    private void resetToPreviewFrame() {
        int frameForPreview = this.mAnimatedDrawableBackend.getFrameForPreview();
        this.mScheduledFrameNumber = frameForPreview;
        this.mScheduledFrameMonotonicNumber = frameForPreview;
        this.mPendingRenderedFrameNumber = -1;
        this.mPendingRenderedFrameMonotonicNumber = -1;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        CloseableReference<Bitmap> closeableReference = this.mLastDrawnFrame;
        if (closeableReference != null) {
            closeableReference.close();
            this.mLastDrawnFrame = null;
        }
    }

    public void setLogId(String str) {
        this.mLogId = str;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mAnimatedDrawableBackend.getWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mAnimatedDrawableBackend.getHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
        doInvalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
        doInvalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        this.mApplyTransformation = true;
        CloseableReference<Bitmap> closeableReference = this.mLastDrawnFrame;
        if (closeableReference != null) {
            closeableReference.close();
            this.mLastDrawnFrame = null;
        }
        this.mLastDrawnFrameNumber = -1;
        this.mLastDrawnFrameMonotonicNumber = -1;
        this.mAnimatedDrawableBackend.dropCaches();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onStart() {
        if (this.mIsRunning) {
            this.mAnimatedDrawableDiagnostics.onStartMethodBegin();
            try {
                long jNow = this.mMonotonicClock.now();
                this.mStartTimeMs = jNow;
                this.mScheduledFrameNumber = 0;
                this.mScheduledFrameMonotonicNumber = 0;
                long durationMsForFrame = jNow + ((long) this.mAnimatedDrawableBackend.getDurationMsForFrame(0));
                scheduleSelf(this.mNextFrameTask, durationMsForFrame);
                this.mNextFrameTaskMs = durationMsForFrame;
                doInvalidateSelf();
            } finally {
                this.mAnimatedDrawableDiagnostics.onStartMethodEnd();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onNextFrame() {
        this.mNextFrameTaskMs = -1L;
        if (this.mIsRunning && this.mDurationMs != 0) {
            this.mAnimatedDrawableDiagnostics.onNextFrameMethodBegin();
            try {
                computeAndScheduleNextFrame(true);
            } finally {
                this.mAnimatedDrawableDiagnostics.onNextFrameMethodEnd();
            }
        }
    }

    private void computeAndScheduleNextFrame(boolean z) {
        if (this.mDurationMs == 0) {
            return;
        }
        long jNow = this.mMonotonicClock.now();
        long j = this.mStartTimeMs;
        int i = this.mDurationMs;
        int i2 = (int) ((jNow - j) / ((long) i));
        int i3 = this.mTotalLoops;
        if (i3 <= 0 || i2 < i3) {
            int i4 = (int) ((jNow - j) % ((long) i));
            int frameForTimestampMs = this.mAnimatedDrawableBackend.getFrameForTimestampMs(i4);
            boolean z2 = this.mScheduledFrameNumber != frameForTimestampMs;
            this.mScheduledFrameNumber = frameForTimestampMs;
            this.mScheduledFrameMonotonicNumber = (i2 * this.mFrameCount) + frameForTimestampMs;
            if (z) {
                if (z2) {
                    doInvalidateSelf();
                    return;
                }
                int timestampMsForFrame = (this.mAnimatedDrawableBackend.getTimestampMsForFrame(frameForTimestampMs) + this.mAnimatedDrawableBackend.getDurationMsForFrame(this.mScheduledFrameNumber)) - i4;
                int i5 = (this.mScheduledFrameNumber + 1) % this.mFrameCount;
                long j2 = jNow + ((long) timestampMsForFrame);
                long j3 = this.mNextFrameTaskMs;
                if (j3 == -1 || j3 > j2) {
                    FLog.v(TAG, "(%s) Next frame (%d) in %d ms", this.mLogId, Integer.valueOf(i5), Integer.valueOf(timestampMsForFrame));
                    unscheduleSelf(this.mNextFrameTask);
                    scheduleSelf(this.mNextFrameTask, j2);
                    this.mNextFrameTaskMs = j2;
                }
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean zRenderFrame;
        Canvas canvas2;
        CloseableReference<Bitmap> previewBitmap;
        CloseableReference<Bitmap> closeableReference;
        this.mAnimatedDrawableDiagnostics.onDrawMethodBegin();
        try {
            this.mWaitingForDraw = false;
            boolean z = true;
            if (this.mIsRunning && !this.mHaveWatchdogScheduled) {
                this.mScheduledExecutorServiceForUiThread.schedule(this.mWatchdogTask, WATCH_DOG_TIMER_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
                this.mHaveWatchdogScheduled = true;
            }
            if (this.mApplyTransformation) {
                this.mDstRect.set(getBounds());
                if (!this.mDstRect.isEmpty()) {
                    AnimatedDrawableCachingBackend animatedDrawableCachingBackendForNewBounds = this.mAnimatedDrawableBackend.forNewBounds(this.mDstRect);
                    AnimatedDrawableCachingBackend animatedDrawableCachingBackend = this.mAnimatedDrawableBackend;
                    if (animatedDrawableCachingBackendForNewBounds != animatedDrawableCachingBackend) {
                        animatedDrawableCachingBackend.dropCaches();
                        this.mAnimatedDrawableBackend = animatedDrawableCachingBackendForNewBounds;
                        this.mAnimatedDrawableDiagnostics.setBackend(animatedDrawableCachingBackendForNewBounds);
                    }
                    this.mSx = this.mDstRect.width() / this.mAnimatedDrawableBackend.getRenderedWidth();
                    this.mSy = this.mDstRect.height() / this.mAnimatedDrawableBackend.getRenderedHeight();
                    this.mApplyTransformation = false;
                }
            }
            if (!this.mDstRect.isEmpty()) {
                canvas.save();
                canvas.scale(this.mSx, this.mSy);
                int i = this.mPendingRenderedFrameNumber;
                if (i != -1) {
                    zRenderFrame = renderFrame(canvas, i, this.mPendingRenderedFrameMonotonicNumber);
                    if (zRenderFrame) {
                        FLog.v(TAG, "(%s) Rendered pending frame %d", this.mLogId, Integer.valueOf(this.mPendingRenderedFrameNumber));
                        this.mPendingRenderedFrameNumber = -1;
                        this.mPendingRenderedFrameMonotonicNumber = -1;
                    } else {
                        FLog.v(TAG, "(%s) Trying again later for pending %d", this.mLogId, Integer.valueOf(this.mPendingRenderedFrameNumber));
                        scheduleInvalidatePoll();
                    }
                } else {
                    zRenderFrame = false;
                }
                if (this.mPendingRenderedFrameNumber == -1) {
                    if (this.mIsRunning) {
                        computeAndScheduleNextFrame(false);
                    }
                    boolean zRenderFrame2 = renderFrame(canvas, this.mScheduledFrameNumber, this.mScheduledFrameMonotonicNumber);
                    zRenderFrame |= zRenderFrame2;
                    if (zRenderFrame2) {
                        FLog.v(TAG, "(%s) Rendered current frame %d", this.mLogId, Integer.valueOf(this.mScheduledFrameNumber));
                        if (this.mIsRunning) {
                            computeAndScheduleNextFrame(true);
                        }
                    } else {
                        FLog.v(TAG, "(%s) Trying again later for current %d", this.mLogId, Integer.valueOf(this.mScheduledFrameNumber));
                        this.mPendingRenderedFrameNumber = this.mScheduledFrameNumber;
                        this.mPendingRenderedFrameMonotonicNumber = this.mScheduledFrameMonotonicNumber;
                        scheduleInvalidatePoll();
                    }
                }
                if (!zRenderFrame && (closeableReference = this.mLastDrawnFrame) != null) {
                    canvas.drawBitmap(closeableReference.get(), 0.0f, 0.0f, this.mPaint);
                    FLog.v(TAG, "(%s) Rendered last known frame %d", this.mLogId, Integer.valueOf(this.mLastDrawnFrameNumber));
                    zRenderFrame = true;
                }
                if (zRenderFrame || (previewBitmap = this.mAnimatedDrawableBackend.getPreviewBitmap()) == null) {
                    z = zRenderFrame;
                } else {
                    canvas.drawBitmap(previewBitmap.get(), 0.0f, 0.0f, this.mPaint);
                    previewBitmap.close();
                    FLog.v(TAG, "(%s) Rendered preview frame", this.mLogId);
                }
                if (z) {
                    canvas2 = canvas;
                } else {
                    canvas2 = canvas;
                    canvas2.drawRect(0.0f, 0.0f, this.mDstRect.width(), this.mDstRect.height(), this.mTransparentPaint);
                    FLog.v(TAG, "(%s) Failed to draw a frame", this.mLogId);
                }
                canvas2.restore();
                this.mAnimatedDrawableDiagnostics.drawDebugOverlay(canvas2, this.mDstRect);
            }
        } finally {
            this.mAnimatedDrawableDiagnostics.onDrawMethodEnd();
        }
    }

    private void scheduleInvalidatePoll() {
        if (this.mInvalidateTaskScheduled) {
            return;
        }
        this.mInvalidateTaskScheduled = true;
        scheduleSelf(this.mInvalidateTask, 5L);
    }

    public boolean didLastDrawRender() {
        return this.mLastDrawnFrame != null;
    }

    private boolean renderFrame(Canvas canvas, int i, int i2) {
        int i3;
        CloseableReference<Bitmap> bitmapForFrame = this.mAnimatedDrawableBackend.getBitmapForFrame(i);
        if (bitmapForFrame == null) {
            return false;
        }
        canvas.drawBitmap(bitmapForFrame.get(), 0.0f, 0.0f, this.mPaint);
        CloseableReference<Bitmap> closeableReference = this.mLastDrawnFrame;
        if (closeableReference != null) {
            closeableReference.close();
        }
        if (this.mIsRunning && i2 > (i3 = this.mLastDrawnFrameMonotonicNumber)) {
            int i4 = (i2 - i3) - 1;
            this.mAnimatedDrawableDiagnostics.incrementDrawnFrames(1);
            this.mAnimatedDrawableDiagnostics.incrementDroppedFrames(i4);
            if (i4 > 0) {
                FLog.v(TAG, "(%s) Dropped %d frames", this.mLogId, Integer.valueOf(i4));
            }
        }
        this.mLastDrawnFrame = bitmapForFrame;
        this.mLastDrawnFrameNumber = i;
        this.mLastDrawnFrameMonotonicNumber = i2;
        FLog.v(TAG, "(%s) Drew frame %d", this.mLogId, Integer.valueOf(i));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doWatchdogCheck() {
        boolean z = false;
        this.mHaveWatchdogScheduled = false;
        if (this.mIsRunning) {
            long jNow = this.mMonotonicClock.now();
            boolean z2 = this.mWaitingForDraw && jNow - this.mLastInvalidateTimeMs > WATCH_DOG_TIMER_MIN_TIMEOUT_MS;
            long j = this.mNextFrameTaskMs;
            if (j != -1 && jNow - j > WATCH_DOG_TIMER_MIN_TIMEOUT_MS) {
                z = true;
            }
            if (z2 || z) {
                dropCaches();
                doInvalidateSelf();
            } else {
                this.mScheduledExecutorServiceForUiThread.schedule(this.mWatchdogTask, WATCH_DOG_TIMER_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
                this.mHaveWatchdogScheduled = true;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doInvalidateSelf() {
        this.mWaitingForDraw = true;
        this.mLastInvalidateTimeMs = this.mMonotonicClock.now();
        invalidateSelf();
    }

    boolean isWaitingForDraw() {
        return this.mWaitingForDraw;
    }

    boolean isWaitingForNextFrame() {
        return this.mNextFrameTaskMs != -1;
    }

    int getScheduledFrameNumber() {
        return this.mScheduledFrameNumber;
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        if (this.mDurationMs == 0 || this.mFrameCount <= 1) {
            return;
        }
        this.mIsRunning = true;
        scheduleSelf(this.mStartTask, this.mMonotonicClock.now());
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        this.mIsRunning = false;
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        return this.mIsRunning;
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onLevelChange(int i) {
        int frameForTimestampMs;
        if (this.mIsRunning || (frameForTimestampMs = this.mAnimatedDrawableBackend.getFrameForTimestampMs(i)) == this.mScheduledFrameNumber) {
            return false;
        }
        try {
            this.mScheduledFrameNumber = frameForTimestampMs;
            this.mScheduledFrameMonotonicNumber = frameForTimestampMs;
            doInvalidateSelf();
            return true;
        } catch (IllegalStateException unused) {
            return false;
        }
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatableDrawable
    public ValueAnimator createValueAnimator(int i) {
        ValueAnimator valueAnimatorCreateValueAnimator = createValueAnimator();
        valueAnimatorCreateValueAnimator.setRepeatCount(Math.max(i / this.mAnimatedDrawableBackend.getDurationMs(), 1));
        return valueAnimatorCreateValueAnimator;
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatableDrawable
    public ValueAnimator createValueAnimator() {
        int loopCount = this.mAnimatedDrawableBackend.getLoopCount();
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(0, this.mDurationMs);
        valueAnimator.setDuration(this.mDurationMs);
        if (loopCount == 0) {
            loopCount = -1;
        }
        valueAnimator.setRepeatCount(loopCount);
        valueAnimator.setRepeatMode(1);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(createAnimatorUpdateListener());
        return valueAnimator;
    }

    @Override // com.facebook.imagepipeline.animated.base.AnimatableDrawable
    public ValueAnimator.AnimatorUpdateListener createAnimatorUpdateListener() {
        return new ValueAnimator.AnimatorUpdateListener() { // from class: com.facebook.imagepipeline.animated.base.AnimatedDrawable.5
            @Override // com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AnimatedDrawable.this.setLevel(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        };
    }

    @Override // com.facebook.drawable.base.DrawableWithCaches
    public void dropCaches() {
        FLog.v(TAG, "(%s) Dropping caches", this.mLogId);
        CloseableReference<Bitmap> closeableReference = this.mLastDrawnFrame;
        if (closeableReference != null) {
            closeableReference.close();
            this.mLastDrawnFrame = null;
            this.mLastDrawnFrameNumber = -1;
            this.mLastDrawnFrameMonotonicNumber = -1;
        }
        this.mAnimatedDrawableBackend.dropCaches();
    }
}
