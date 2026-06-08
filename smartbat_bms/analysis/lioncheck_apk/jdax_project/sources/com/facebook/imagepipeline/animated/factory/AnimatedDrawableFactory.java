package com.facebook.imagepipeline.animated.factory;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import com.facebook.common.time.MonotonicClock;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableOptions;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableBackendProvider;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableCachingBackendImpl;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableCachingBackendImplProvider;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableDiagnosticsImpl;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableDiagnosticsNoop;
import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;
import java.util.concurrent.ScheduledExecutorService;

/* JADX INFO: loaded from: classes.dex */
public class AnimatedDrawableFactory {
    private final AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    private final AnimatedDrawableCachingBackendImplProvider mAnimatedDrawableCachingBackendProvider;
    private final AnimatedDrawableUtil mAnimatedDrawableUtil;
    private final MonotonicClock mMonotonicClock = new MonotonicClock() { // from class: com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory.1
        @Override // com.facebook.common.time.MonotonicClock
        public long now() {
            return SystemClock.uptimeMillis();
        }
    };
    private final Resources mResources;
    private final ScheduledExecutorService mScheduledExecutorServiceForUiThread;

    public AnimatedDrawableFactory(AnimatedDrawableBackendProvider animatedDrawableBackendProvider, AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendImplProvider, AnimatedDrawableUtil animatedDrawableUtil, ScheduledExecutorService scheduledExecutorService, Resources resources) {
        this.mAnimatedDrawableBackendProvider = animatedDrawableBackendProvider;
        this.mAnimatedDrawableCachingBackendProvider = animatedDrawableCachingBackendImplProvider;
        this.mAnimatedDrawableUtil = animatedDrawableUtil;
        this.mScheduledExecutorServiceForUiThread = scheduledExecutorService;
        this.mResources = resources;
    }

    public AnimatedDrawable create(AnimatedImageResult animatedImageResult) {
        return create(animatedImageResult, AnimatedDrawableOptions.DEFAULTS);
    }

    public AnimatedDrawable create(AnimatedImageResult animatedImageResult, AnimatedDrawableOptions animatedDrawableOptions) {
        AnimatedImage image = animatedImageResult.getImage();
        return createAnimatedDrawable(animatedDrawableOptions, this.mAnimatedDrawableBackendProvider.get(animatedImageResult, new Rect(0, 0, image.getWidth(), image.getHeight())));
    }

    private AnimatedDrawable createAnimatedDrawable(AnimatedDrawableOptions animatedDrawableOptions, AnimatedDrawableBackend animatedDrawableBackend) {
        AnimatedDrawableDiagnostics animatedDrawableDiagnosticsNoop;
        DisplayMetrics displayMetrics = this.mResources.getDisplayMetrics();
        AnimatedDrawableCachingBackendImpl animatedDrawableCachingBackendImpl = this.mAnimatedDrawableCachingBackendProvider.get(animatedDrawableBackend, animatedDrawableOptions);
        if (animatedDrawableOptions.enableDebugging) {
            animatedDrawableDiagnosticsNoop = new AnimatedDrawableDiagnosticsImpl(this.mAnimatedDrawableUtil, displayMetrics);
        } else {
            animatedDrawableDiagnosticsNoop = AnimatedDrawableDiagnosticsNoop.getInstance();
        }
        return new AnimatedDrawable(this.mScheduledExecutorServiceForUiThread, animatedDrawableCachingBackendImpl, animatedDrawableDiagnosticsNoop, this.mMonotonicClock);
    }
}
