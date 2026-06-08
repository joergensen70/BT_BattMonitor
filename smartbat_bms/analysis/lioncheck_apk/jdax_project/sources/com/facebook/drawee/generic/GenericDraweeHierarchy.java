package com.facebook.drawee.generic;

import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import com.facebook.common.internal.Preconditions;
import com.facebook.drawee.drawable.DrawableParent;
import com.facebook.drawee.drawable.FadeDrawable;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.drawable.MatrixDrawable;
import com.facebook.drawee.drawable.ScaleTypeDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.SettableDraweeHierarchy;
import java.util.Iterator;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class GenericDraweeHierarchy implements SettableDraweeHierarchy {
    private final int mActualImageIndex;
    private final ForwardingDrawable mActualImageWrapper;
    private final Drawable mEmptyActualImageDrawable;
    private final FadeDrawable mFadeDrawable;
    private final int mFailureImageIndex;
    private final int mPlaceholderImageIndex;
    private final int mProgressBarImageIndex;
    private final Resources mResources;
    private final int mRetryImageIndex;
    private RoundingParams mRoundingParams;
    private final RootDrawable mTopLevelDrawable;

    GenericDraweeHierarchy(GenericDraweeHierarchyBuilder genericDraweeHierarchyBuilder) {
        int i = 0;
        ColorDrawable colorDrawable = new ColorDrawable(0);
        this.mEmptyActualImageDrawable = colorDrawable;
        this.mResources = genericDraweeHierarchyBuilder.getResources();
        this.mRoundingParams = genericDraweeHierarchyBuilder.getRoundingParams();
        this.mActualImageWrapper = new ForwardingDrawable(colorDrawable);
        int size = genericDraweeHierarchyBuilder.getBackgrounds() != null ? genericDraweeHierarchyBuilder.getBackgrounds().size() : 0;
        int size2 = (genericDraweeHierarchyBuilder.getOverlays() != null ? genericDraweeHierarchyBuilder.getOverlays().size() : 0) + (genericDraweeHierarchyBuilder.getPressedStateOverlay() != null ? 1 : 0);
        this.mPlaceholderImageIndex = size;
        this.mActualImageIndex = size + 1;
        this.mProgressBarImageIndex = size + 2;
        this.mRetryImageIndex = size + 3;
        int i2 = size + 5;
        this.mFailureImageIndex = size + 4;
        Drawable[] drawableArr = new Drawable[i2 + size2];
        if (size > 0) {
            Iterator<Drawable> it = genericDraweeHierarchyBuilder.getBackgrounds().iterator();
            int i3 = 0;
            while (it.hasNext()) {
                drawableArr[i3] = buildBranch(it.next(), null);
                i3++;
            }
        }
        drawableArr[this.mPlaceholderImageIndex] = buildBranch(genericDraweeHierarchyBuilder.getPlaceholderImage(), genericDraweeHierarchyBuilder.getPlaceholderImageScaleType());
        drawableArr[this.mActualImageIndex] = buildActualImageBranch(this.mActualImageWrapper, genericDraweeHierarchyBuilder.getActualImageScaleType(), genericDraweeHierarchyBuilder.getActualImageFocusPoint(), genericDraweeHierarchyBuilder.getActualImageMatrix(), genericDraweeHierarchyBuilder.getActualImageColorFilter());
        drawableArr[this.mProgressBarImageIndex] = buildBranch(genericDraweeHierarchyBuilder.getProgressBarImage(), genericDraweeHierarchyBuilder.getProgressBarImageScaleType());
        drawableArr[this.mRetryImageIndex] = buildBranch(genericDraweeHierarchyBuilder.getRetryImage(), genericDraweeHierarchyBuilder.getRetryImageScaleType());
        drawableArr[this.mFailureImageIndex] = buildBranch(genericDraweeHierarchyBuilder.getFailureImage(), genericDraweeHierarchyBuilder.getFailureImageScaleType());
        if (size2 > 0) {
            if (genericDraweeHierarchyBuilder.getOverlays() != null) {
                Iterator<Drawable> it2 = genericDraweeHierarchyBuilder.getOverlays().iterator();
                while (it2.hasNext()) {
                    drawableArr[i + i2] = buildBranch(it2.next(), null);
                    i++;
                }
            }
            if (genericDraweeHierarchyBuilder.getPressedStateOverlay() != null) {
                drawableArr[i2 + i] = buildBranch(genericDraweeHierarchyBuilder.getPressedStateOverlay(), null);
            }
        }
        FadeDrawable fadeDrawable = new FadeDrawable(drawableArr);
        this.mFadeDrawable = fadeDrawable;
        fadeDrawable.setTransitionDuration(genericDraweeHierarchyBuilder.getFadeDuration());
        RootDrawable rootDrawable = new RootDrawable(WrappingUtils.maybeWrapWithRoundedOverlayColor(fadeDrawable, this.mRoundingParams));
        this.mTopLevelDrawable = rootDrawable;
        rootDrawable.mutate();
        resetFade();
    }

    @Nullable
    private Drawable buildActualImageBranch(Drawable drawable, @Nullable ScalingUtils.ScaleType scaleType, @Nullable PointF pointF, @Nullable Matrix matrix, @Nullable ColorFilter colorFilter) {
        drawable.setColorFilter(colorFilter);
        return WrappingUtils.maybeWrapWithMatrix(WrappingUtils.maybeWrapWithScaleType(drawable, scaleType, pointF), matrix);
    }

    @Nullable
    private Drawable buildBranch(@Nullable Drawable drawable, @Nullable ScalingUtils.ScaleType scaleType) {
        return WrappingUtils.maybeWrapWithScaleType(WrappingUtils.maybeApplyLeafRounding(drawable, this.mRoundingParams, this.mResources), scaleType);
    }

    private void resetActualImages() {
        this.mActualImageWrapper.setDrawable(this.mEmptyActualImageDrawable);
    }

    private void resetFade() {
        FadeDrawable fadeDrawable = this.mFadeDrawable;
        if (fadeDrawable != null) {
            fadeDrawable.beginBatchMode();
            this.mFadeDrawable.fadeInAllLayers();
            fadeOutBranches();
            fadeInLayer(this.mPlaceholderImageIndex);
            this.mFadeDrawable.finishTransitionImmediately();
            this.mFadeDrawable.endBatchMode();
        }
    }

    private void fadeOutBranches() {
        fadeOutLayer(this.mPlaceholderImageIndex);
        fadeOutLayer(this.mActualImageIndex);
        fadeOutLayer(this.mProgressBarImageIndex);
        fadeOutLayer(this.mRetryImageIndex);
        fadeOutLayer(this.mFailureImageIndex);
    }

    private void fadeInLayer(int i) {
        if (i >= 0) {
            this.mFadeDrawable.fadeInLayer(i);
        }
    }

    private void fadeOutLayer(int i) {
        if (i >= 0) {
            this.mFadeDrawable.fadeOutLayer(i);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void setProgress(float f) {
        Drawable drawable = getLayerParentDrawable(this.mProgressBarImageIndex).getDrawable();
        if (drawable == 0) {
            return;
        }
        if (f >= 0.999f) {
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).stop();
            }
            fadeOutLayer(this.mProgressBarImageIndex);
        } else {
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
            fadeInLayer(this.mProgressBarImageIndex);
        }
        drawable.setLevel(Math.round(f * 10000.0f));
    }

    @Override // com.facebook.drawee.interfaces.DraweeHierarchy
    public Drawable getTopLevelDrawable() {
        return this.mTopLevelDrawable;
    }

    @Override // com.facebook.drawee.interfaces.SettableDraweeHierarchy
    public void reset() {
        resetActualImages();
        resetFade();
    }

    @Override // com.facebook.drawee.interfaces.SettableDraweeHierarchy
    public void setImage(Drawable drawable, float f, boolean z) {
        Drawable drawableMaybeApplyLeafRounding = WrappingUtils.maybeApplyLeafRounding(drawable, this.mRoundingParams, this.mResources);
        drawableMaybeApplyLeafRounding.mutate();
        this.mActualImageWrapper.setDrawable(drawableMaybeApplyLeafRounding);
        this.mFadeDrawable.beginBatchMode();
        fadeOutBranches();
        fadeInLayer(this.mActualImageIndex);
        setProgress(f);
        if (z) {
            this.mFadeDrawable.finishTransitionImmediately();
        }
        this.mFadeDrawable.endBatchMode();
    }

    @Override // com.facebook.drawee.interfaces.SettableDraweeHierarchy
    public void setProgress(float f, boolean z) {
        this.mFadeDrawable.beginBatchMode();
        setProgress(f);
        if (z) {
            this.mFadeDrawable.finishTransitionImmediately();
        }
        this.mFadeDrawable.endBatchMode();
    }

    @Override // com.facebook.drawee.interfaces.SettableDraweeHierarchy
    public void setFailure(Throwable th) {
        this.mFadeDrawable.beginBatchMode();
        fadeOutBranches();
        if (this.mFadeDrawable.getDrawable(this.mFailureImageIndex) != null) {
            fadeInLayer(this.mFailureImageIndex);
        } else {
            fadeInLayer(this.mPlaceholderImageIndex);
        }
        this.mFadeDrawable.endBatchMode();
    }

    @Override // com.facebook.drawee.interfaces.SettableDraweeHierarchy
    public void setRetry(Throwable th) {
        this.mFadeDrawable.beginBatchMode();
        fadeOutBranches();
        if (this.mFadeDrawable.getDrawable(this.mRetryImageIndex) != null) {
            fadeInLayer(this.mRetryImageIndex);
        } else {
            fadeInLayer(this.mPlaceholderImageIndex);
        }
        this.mFadeDrawable.endBatchMode();
    }

    @Override // com.facebook.drawee.interfaces.SettableDraweeHierarchy
    public void setControllerOverlay(@Nullable Drawable drawable) {
        this.mTopLevelDrawable.setControllerOverlay(drawable);
    }

    private DrawableParent getLayerParentDrawable(int i) {
        DrawableParent drawableParentForIndex = this.mFadeDrawable.getDrawableParentForIndex(i);
        if (drawableParentForIndex.getDrawable() instanceof MatrixDrawable) {
            drawableParentForIndex = (MatrixDrawable) drawableParentForIndex.getDrawable();
        }
        return drawableParentForIndex.getDrawable() instanceof ScaleTypeDrawable ? (ScaleTypeDrawable) drawableParentForIndex.getDrawable() : drawableParentForIndex;
    }

    private void setLayerChildDrawable(int i, @Nullable Drawable drawable) {
        if (drawable == null) {
            this.mFadeDrawable.setDrawable(i, null);
        } else {
            getLayerParentDrawable(i).setDrawable(WrappingUtils.maybeApplyLeafRounding(drawable, this.mRoundingParams, this.mResources));
        }
    }

    private ScaleTypeDrawable getLayerScaleTypeDrawable(int i) {
        DrawableParent layerParentDrawable = getLayerParentDrawable(i);
        if (layerParentDrawable instanceof ScaleTypeDrawable) {
            return (ScaleTypeDrawable) layerParentDrawable;
        }
        return WrappingUtils.wrapChildWithScaleType(layerParentDrawable, ScalingUtils.ScaleType.FIT_XY);
    }

    public void setFadeDuration(int i) {
        this.mFadeDrawable.setTransitionDuration(i);
    }

    public void setActualImageFocusPoint(PointF pointF) {
        Preconditions.checkNotNull(pointF);
        getLayerScaleTypeDrawable(this.mActualImageIndex).setFocusPoint(pointF);
    }

    public void setActualImageScaleType(ScalingUtils.ScaleType scaleType) {
        Preconditions.checkNotNull(scaleType);
        getLayerScaleTypeDrawable(this.mActualImageIndex).setScaleType(scaleType);
    }

    public void setActualImageColorFilter(ColorFilter colorFilter) {
        this.mActualImageWrapper.setColorFilter(colorFilter);
    }

    public void getActualImageBounds(RectF rectF) {
        this.mActualImageWrapper.getTransformedBounds(rectF);
    }

    public void setPlaceholderImage(@Nullable Drawable drawable) {
        setLayerChildDrawable(this.mPlaceholderImageIndex, drawable);
    }

    public void setPlaceholderImage(Drawable drawable, ScalingUtils.ScaleType scaleType) {
        setLayerChildDrawable(this.mPlaceholderImageIndex, drawable);
        getLayerScaleTypeDrawable(this.mPlaceholderImageIndex).setScaleType(scaleType);
    }

    public void setPlaceholderImageFocusPoint(PointF pointF) {
        Preconditions.checkNotNull(pointF);
        getLayerScaleTypeDrawable(this.mPlaceholderImageIndex).setFocusPoint(pointF);
    }

    public void setPlaceholderImage(int i) {
        setPlaceholderImage(this.mResources.getDrawable(i));
    }

    public void setFailureImage(@Nullable Drawable drawable) {
        setLayerChildDrawable(this.mFailureImageIndex, drawable);
    }

    public void setFailureImage(Drawable drawable, ScalingUtils.ScaleType scaleType) {
        setLayerChildDrawable(this.mFailureImageIndex, drawable);
        getLayerScaleTypeDrawable(this.mFailureImageIndex).setScaleType(scaleType);
    }

    public void setRetryImage(@Nullable Drawable drawable) {
        setLayerChildDrawable(this.mRetryImageIndex, drawable);
    }

    public void setRetryImage(Drawable drawable, ScalingUtils.ScaleType scaleType) {
        setLayerChildDrawable(this.mRetryImageIndex, drawable);
        getLayerScaleTypeDrawable(this.mRetryImageIndex).setScaleType(scaleType);
    }

    public void setProgressBarImage(@Nullable Drawable drawable) {
        setLayerChildDrawable(this.mProgressBarImageIndex, drawable);
    }

    public void setProgressBarImage(Drawable drawable, ScalingUtils.ScaleType scaleType) {
        setLayerChildDrawable(this.mProgressBarImageIndex, drawable);
        getLayerScaleTypeDrawable(this.mProgressBarImageIndex).setScaleType(scaleType);
    }

    public void setRoundingParams(RoundingParams roundingParams) {
        this.mRoundingParams = roundingParams;
        WrappingUtils.updateOverlayColorRounding(this.mTopLevelDrawable, roundingParams);
        for (int i = 0; i < this.mFadeDrawable.getNumberOfLayers(); i++) {
            WrappingUtils.updateLeafRounding(getLayerParentDrawable(i), this.mRoundingParams, this.mResources);
        }
    }

    public RoundingParams getRoundingParams() {
        return this.mRoundingParams;
    }
}
