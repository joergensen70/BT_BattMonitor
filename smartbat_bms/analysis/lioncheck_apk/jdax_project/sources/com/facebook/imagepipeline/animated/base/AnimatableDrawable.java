package com.facebook.imagepipeline.animated.base;

import android.graphics.drawable.Animatable;
import com.nineoldandroids.animation.ValueAnimator;

/* JADX INFO: loaded from: classes.dex */
public interface AnimatableDrawable extends Animatable {
    ValueAnimator.AnimatorUpdateListener createAnimatorUpdateListener();

    ValueAnimator createValueAnimator();

    ValueAnimator createValueAnimator(int i);
}
