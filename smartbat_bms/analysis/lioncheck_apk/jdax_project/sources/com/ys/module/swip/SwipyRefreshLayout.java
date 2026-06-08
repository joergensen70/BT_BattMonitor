package com.ys.module.swip;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/* JADX INFO: loaded from: classes.dex */
public class SwipyRefreshLayout extends ViewGroup {
    private static final int ALPHA_ANIMATION_DURATION = 300;
    private static final int ANIMATE_TO_START_DURATION = 200;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    private static final int CIRCLE_BG_LIGHT = -328966;
    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_DIAMETER_LARGE = 56;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0f;
    public static final int DEFAULT = 1;
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private static final float DRAG_RATE = 0.5f;
    private static final int INVALID_POINTER = -1;
    public static final int LARGE = 0;
    private static final int[] LAYOUT_ATTRS = {R.attr.enabled};
    private static final String LOG_TAG = "SwipyRefreshLayout";
    private static final int MAX_ALPHA = 255;
    private static final float MAX_PROGRESS_ANGLE = 0.8f;
    private static final int SCALE_DOWN_DURATION = 150;
    private static final int STARTING_PROGRESS_ALPHA = 76;
    private int mActivePointerId;
    private Animation mAlphaMaxAnimation;
    private Animation mAlphaStartAnimation;
    private final Animation mAnimateToCorrectPosition;
    private final Animation mAnimateToStartPosition;
    private boolean mBothDirection;
    private int mCircleHeight;
    private CircleImageView mCircleView;
    private int mCircleViewIndex;
    private int mCircleWidth;
    private int mCurrentTargetOffsetTop;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private SwipyRefreshLayoutDirection mDirection;
    protected int mFrom;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private OnRefreshListener mListener;
    private int mMediumAnimationDuration;
    private boolean mNotify;
    private boolean mOriginalOffsetCalculated;
    protected int mOriginalOffsetTop;
    private MaterialProgressDrawable mProgress;
    private Animation.AnimationListener mRefreshListener;
    private boolean mRefreshing;
    private boolean mReturningToStart;
    private boolean mScale;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mScaleDownToStartAnimation;
    private float mSpinnerFinalOffset;
    private float mStartingScale;
    private View mTarget;
    private float mTotalDragDistance;
    private int mTouchSlop;
    private boolean mUsingCustomStart;

    public interface OnRefreshListener {
        void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection);
    }

    private boolean isAlphaUsedForScale() {
        return false;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setColorViewAlpha(int i) {
        this.mCircleView.getBackground().setAlpha(i);
        this.mProgress.setAlpha(i);
    }

    public void setSize(int i) {
        if (i == 0 || i == 1) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (i == 0) {
                int i2 = (int) (displayMetrics.density * 56.0f);
                this.mCircleWidth = i2;
                this.mCircleHeight = i2;
            } else {
                int i3 = (int) (displayMetrics.density * 40.0f);
                this.mCircleWidth = i3;
                this.mCircleHeight = i3;
            }
            this.mCircleView.setImageDrawable(null);
            this.mProgress.updateSizes(i);
            this.mCircleView.setImageDrawable(this.mProgress);
        }
    }

    public SwipyRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipyRefreshLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRefreshing = false;
        this.mTotalDragDistance = -1.0f;
        this.mOriginalOffsetCalculated = false;
        this.mActivePointerId = -1;
        this.mCircleViewIndex = -1;
        this.mRefreshListener = new Animation.AnimationListener() { // from class: com.ys.module.swip.SwipyRefreshLayout.1
            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                if (SwipyRefreshLayout.this.mRefreshing) {
                    SwipyRefreshLayout.this.mProgress.setAlpha(255);
                    SwipyRefreshLayout.this.mProgress.start();
                    if (SwipyRefreshLayout.this.mNotify && SwipyRefreshLayout.this.mListener != null) {
                        SwipyRefreshLayout.this.mListener.onRefresh(SwipyRefreshLayout.this.mDirection);
                    }
                } else {
                    SwipyRefreshLayout.this.mProgress.stop();
                    SwipyRefreshLayout.this.mCircleView.setVisibility(8);
                    SwipyRefreshLayout.this.setColorViewAlpha(255);
                    if (SwipyRefreshLayout.this.mScale) {
                        SwipyRefreshLayout.this.setAnimationProgress(0.0f);
                    } else {
                        SwipyRefreshLayout swipyRefreshLayout = SwipyRefreshLayout.this;
                        swipyRefreshLayout.setTargetOffsetTopAndBottom(swipyRefreshLayout.mOriginalOffsetTop - SwipyRefreshLayout.this.mCurrentTargetOffsetTop, true);
                    }
                }
                SwipyRefreshLayout swipyRefreshLayout2 = SwipyRefreshLayout.this;
                swipyRefreshLayout2.mCurrentTargetOffsetTop = swipyRefreshLayout2.mCircleView.getTop();
            }
        };
        this.mAnimateToCorrectPosition = new Animation() { // from class: com.ys.module.swip.SwipyRefreshLayout.6
            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                float fAbs;
                int measuredHeight;
                if (!SwipyRefreshLayout.this.mUsingCustomStart) {
                    if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[SwipyRefreshLayout.this.mDirection.ordinal()] != 1) {
                        fAbs = SwipyRefreshLayout.this.mSpinnerFinalOffset - Math.abs(SwipyRefreshLayout.this.mOriginalOffsetTop);
                    } else {
                        measuredHeight = SwipyRefreshLayout.this.getMeasuredHeight() - ((int) SwipyRefreshLayout.this.mSpinnerFinalOffset);
                        SwipyRefreshLayout.this.setTargetOffsetTopAndBottom((SwipyRefreshLayout.this.mFrom + ((int) ((measuredHeight - SwipyRefreshLayout.this.mFrom) * f))) - SwipyRefreshLayout.this.mCircleView.getTop(), false);
                    }
                } else {
                    fAbs = SwipyRefreshLayout.this.mSpinnerFinalOffset;
                }
                measuredHeight = (int) fAbs;
                SwipyRefreshLayout.this.setTargetOffsetTopAndBottom((SwipyRefreshLayout.this.mFrom + ((int) ((measuredHeight - SwipyRefreshLayout.this.mFrom) * f))) - SwipyRefreshLayout.this.mCircleView.getTop(), false);
            }
        };
        this.mAnimateToStartPosition = new Animation() { // from class: com.ys.module.swip.SwipyRefreshLayout.7
            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                SwipyRefreshLayout.this.moveToStart(f);
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMediumAnimationDuration = getResources().getInteger(R.integer.config_mediumAnimTime);
        setWillNotDraw(false);
        this.mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, LAYOUT_ATTRS);
        setEnabled(typedArrayObtainStyledAttributes.getBoolean(0, true));
        typedArrayObtainStyledAttributes.recycle();
        TypedArray typedArrayObtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, com.ys.module.R.styleable.SwipyRefreshLayout);
        SwipyRefreshLayoutDirection fromInt = SwipyRefreshLayoutDirection.getFromInt(typedArrayObtainStyledAttributes2.getInt(com.ys.module.R.styleable.SwipyRefreshLayout_direction, 2));
        if (fromInt != SwipyRefreshLayoutDirection.BOTH) {
            this.mDirection = fromInt;
            this.mBothDirection = false;
        } else {
            this.mDirection = SwipyRefreshLayoutDirection.TOP;
            this.mBothDirection = true;
        }
        typedArrayObtainStyledAttributes2.recycle();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mCircleWidth = (int) (displayMetrics.density * 40.0f);
        this.mCircleHeight = (int) (displayMetrics.density * 40.0f);
        createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        float f = displayMetrics.density * 64.0f;
        this.mSpinnerFinalOffset = f;
        this.mTotalDragDistance = f;
    }

    @Override // android.view.ViewGroup
    protected int getChildDrawingOrder(int i, int i2) {
        int i3 = this.mCircleViewIndex;
        return i3 < 0 ? i2 : i2 == i + (-1) ? i3 : i2 >= i3 ? i2 + 1 : i2;
    }

    private void createProgressView() {
        this.mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, 20.0f);
        MaterialProgressDrawable materialProgressDrawable = new MaterialProgressDrawable(getContext(), this);
        this.mProgress = materialProgressDrawable;
        materialProgressDrawable.setBackgroundColor(CIRCLE_BG_LIGHT);
        this.mCircleView.setImageDrawable(this.mProgress);
        this.mCircleView.setVisibility(8);
        addView(this.mCircleView);
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
    }

    public void setRefreshing(boolean z) {
        float f;
        if (z && this.mRefreshing != z) {
            this.mRefreshing = z;
            if (!this.mUsingCustomStart) {
                f = this.mSpinnerFinalOffset + this.mOriginalOffsetTop;
            } else {
                f = this.mSpinnerFinalOffset;
            }
            setTargetOffsetTopAndBottom(((int) f) - this.mCurrentTargetOffsetTop, true);
            this.mNotify = false;
            startScaleUpAnimation(this.mRefreshListener);
            return;
        }
        setRefreshing(z, false);
    }

    private void startScaleUpAnimation(Animation.AnimationListener animationListener) {
        this.mCircleView.setVisibility(0);
        this.mProgress.setAlpha(255);
        Animation animation = new Animation() { // from class: com.ys.module.swip.SwipyRefreshLayout.2
            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                SwipyRefreshLayout.this.setAnimationProgress(f);
            }
        };
        this.mScaleAnimation = animation;
        animation.setDuration(this.mMediumAnimationDuration);
        if (animationListener != null) {
            this.mCircleView.setAnimationListener(animationListener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleAnimation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setAnimationProgress(float f) {
        if (isAlphaUsedForScale()) {
            setColorViewAlpha((int) (f * 255.0f));
        } else {
            ViewCompat.setScaleX(this.mCircleView, f);
            ViewCompat.setScaleY(this.mCircleView, f);
        }
    }

    private void setRefreshing(boolean z, boolean z2) {
        if (this.mRefreshing != z) {
            this.mNotify = z2;
            ensureTarget();
            this.mRefreshing = z;
            if (z) {
                animateOffsetToCorrectPosition(this.mCurrentTargetOffsetTop, this.mRefreshListener);
            } else {
                startScaleDownAnimation(this.mRefreshListener);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startScaleDownAnimation(Animation.AnimationListener animationListener) {
        Animation animation = new Animation() { // from class: com.ys.module.swip.SwipyRefreshLayout.3
            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                SwipyRefreshLayout.this.setAnimationProgress(1.0f - f);
            }
        };
        this.mScaleDownAnimation = animation;
        animation.setDuration(150L);
        this.mCircleView.setAnimationListener(animationListener);
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation() {
        this.mAlphaStartAnimation = startAlphaAnimation(this.mProgress.getAlpha(), 76);
    }

    private void startProgressAlphaMaxAnimation() {
        this.mAlphaMaxAnimation = startAlphaAnimation(this.mProgress.getAlpha(), 255);
    }

    private Animation startAlphaAnimation(final int i, final int i2) {
        if (this.mScale && isAlphaUsedForScale()) {
            return null;
        }
        Animation animation = new Animation() { // from class: com.ys.module.swip.SwipyRefreshLayout.4
            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                SwipyRefreshLayout.this.mProgress.setAlpha((int) (i + ((i2 - r0) * f)));
            }
        };
        animation.setDuration(300L);
        this.mCircleView.setAnimationListener(null);
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(animation);
        return animation;
    }

    public void setProgressBackgroundColor(int i) {
        this.mCircleView.setBackgroundColor(i);
        this.mProgress.setBackgroundColor(getResources().getColor(i));
    }

    @Deprecated
    public void setColorScheme(int... iArr) {
        setColorSchemeResources(iArr);
    }

    public void setColorSchemeResources(int... iArr) {
        Resources resources = getResources();
        int[] iArr2 = new int[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            iArr2[i] = resources.getColor(iArr[i]);
        }
        setColorSchemeColors(iArr2);
    }

    public void setColorSchemeColors(int... iArr) {
        ensureTarget();
        this.mProgress.setColorSchemeColors(iArr);
    }

    public boolean isRefreshing() {
        return this.mRefreshing;
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                if (!childAt.equals(this.mCircleView)) {
                    this.mTarget = childAt;
                    return;
                }
            }
        }
    }

    public void setDistanceToTriggerSync(int i) {
        this.mTotalDragDistance = i;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (this.mTarget == null) {
            ensureTarget();
        }
        View view = this.mTarget;
        if (view == null) {
            return;
        }
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        view.layout(paddingLeft, paddingTop, ((measuredWidth - getPaddingLeft()) - getPaddingRight()) + paddingLeft, ((measuredHeight - getPaddingTop()) - getPaddingBottom()) + paddingTop);
        int measuredWidth2 = this.mCircleView.getMeasuredWidth();
        int measuredHeight2 = this.mCircleView.getMeasuredHeight();
        int i5 = measuredWidth / 2;
        int i6 = measuredWidth2 / 2;
        int i7 = this.mCurrentTargetOffsetTop;
        this.mCircleView.layout(i5 - i6, i7, i5 + i6, measuredHeight2 + i7);
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mTarget == null) {
            ensureTarget();
        }
        View view = this.mTarget;
        if (view == null) {
            return;
        }
        view.measure(View.MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), View.MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
        this.mCircleView.measure(View.MeasureSpec.makeMeasureSpec(this.mCircleWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mCircleHeight, 1073741824));
        if (!this.mUsingCustomStart && !this.mOriginalOffsetCalculated) {
            this.mOriginalOffsetCalculated = true;
            if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[this.mDirection.ordinal()] == 1) {
                int measuredHeight = getMeasuredHeight() - this.mCircleView.getMeasuredHeight();
                this.mOriginalOffsetTop = measuredHeight;
                this.mCurrentTargetOffsetTop = measuredHeight;
            } else {
                int i3 = -this.mCircleView.getMeasuredHeight();
                this.mOriginalOffsetTop = i3;
                this.mCurrentTargetOffsetTop = i3;
            }
        }
        this.mCircleViewIndex = -1;
        for (int i4 = 0; i4 < getChildCount(); i4++) {
            if (getChildAt(i4) == this.mCircleView) {
                this.mCircleViewIndex = i4;
                return;
            }
        }
    }

    /* JADX INFO: renamed from: com.ys.module.swip.SwipyRefreshLayout$9, reason: invalid class name */
    static /* synthetic */ class AnonymousClass9 {
        static final /* synthetic */ int[] $SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection;

        static {
            int[] iArr = new int[SwipyRefreshLayoutDirection.values().length];
            $SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection = iArr;
            try {
                iArr[SwipyRefreshLayoutDirection.BOTTOM.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[SwipyRefreshLayoutDirection.TOP.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public boolean canChildScrollUp() {
        return ViewCompat.canScrollVertically(this.mTarget, -1);
    }

    public boolean canChildScrollDown() {
        return ViewCompat.canScrollVertically(this.mTarget, 1);
    }

    /* JADX WARN: Removed duplicated region for block: B:41:0x0067  */
    @Override // android.view.ViewGroup
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onInterceptTouchEvent(android.view.MotionEvent r7) {
        /*
            Method dump skipped, instruction units count: 248
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.ys.module.swip.SwipyRefreshLayout.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    private float getMotionEventY(MotionEvent motionEvent, int i) {
        int iFindPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, i);
        if (iFindPointerIndex < 0) {
            return -1.0f;
        }
        return MotionEventCompat.getY(motionEvent, iFindPointerIndex);
    }

    private boolean isAnimationRunning(Animation animation) {
        return (animation == null || !animation.hasStarted() || animation.hasEnded()) ? false : true;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float f;
        float f2;
        int i;
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (this.mReturningToStart && actionMasked == 0) {
            this.mReturningToStart = false;
        }
        if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[this.mDirection.ordinal()] == 1) {
            if (!isEnabled() || this.mReturningToStart || canChildScrollDown() || this.mRefreshing) {
                return false;
            }
        } else if (!isEnabled() || this.mReturningToStart || canChildScrollUp() || this.mRefreshing) {
            return false;
        }
        if (actionMasked == 0) {
            this.mActivePointerId = MotionEventCompat.getPointerId(motionEvent, 0);
            this.mIsBeingDragged = false;
        } else {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int iFindPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mActivePointerId);
                    if (iFindPointerIndex < 0) {
                        return false;
                    }
                    float y = MotionEventCompat.getY(motionEvent, iFindPointerIndex);
                    if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[this.mDirection.ordinal()] == 1) {
                        f2 = (this.mInitialMotionY - y) * DRAG_RATE;
                    } else {
                        f2 = (y - this.mInitialMotionY) * DRAG_RATE;
                    }
                    if (this.mIsBeingDragged) {
                        this.mProgress.showArrow(true);
                        float f3 = f2 / this.mTotalDragDistance;
                        if (f3 < 0.0f) {
                            return false;
                        }
                        float fMin = Math.min(1.0f, Math.abs(f3));
                        float fMax = (((float) Math.max(((double) fMin) - 0.4d, 0.0d)) * 5.0f) / 3.0f;
                        float fAbs = Math.abs(f2) - this.mTotalDragDistance;
                        float f4 = this.mUsingCustomStart ? this.mSpinnerFinalOffset - this.mOriginalOffsetTop : this.mSpinnerFinalOffset;
                        double dMax = Math.max(0.0f, Math.min(fAbs, f4 * DECELERATE_INTERPOLATION_FACTOR) / f4) / 4.0f;
                        float fPow = ((float) (dMax - Math.pow(dMax, 2.0d))) * DECELERATE_INTERPOLATION_FACTOR;
                        float f5 = f4 * fPow * DECELERATE_INTERPOLATION_FACTOR;
                        if (this.mDirection == SwipyRefreshLayoutDirection.TOP) {
                            i = this.mOriginalOffsetTop + ((int) ((f4 * fMin) + f5));
                        } else {
                            i = this.mOriginalOffsetTop - ((int) ((f4 * fMin) + f5));
                        }
                        if (this.mCircleView.getVisibility() != 0) {
                            this.mCircleView.setVisibility(0);
                        }
                        if (!this.mScale) {
                            ViewCompat.setScaleX(this.mCircleView, 1.0f);
                            ViewCompat.setScaleY(this.mCircleView, 1.0f);
                        }
                        float f6 = this.mTotalDragDistance;
                        if (f2 < f6) {
                            if (this.mScale) {
                                setAnimationProgress(f2 / f6);
                            }
                            if (this.mProgress.getAlpha() > 76 && !isAnimationRunning(this.mAlphaStartAnimation)) {
                                startProgressAlphaStartAnimation();
                            }
                            this.mProgress.setStartEndTrim(0.0f, Math.min(MAX_PROGRESS_ANGLE, fMax * MAX_PROGRESS_ANGLE));
                            this.mProgress.setArrowScale(Math.min(1.0f, fMax));
                        } else if (this.mProgress.getAlpha() < 255 && !isAnimationRunning(this.mAlphaMaxAnimation)) {
                            startProgressAlphaMaxAnimation();
                        }
                        this.mProgress.setProgressRotation((((fMax * 0.4f) - 0.25f) + (fPow * DECELERATE_INTERPOLATION_FACTOR)) * DRAG_RATE);
                        setTargetOffsetTopAndBottom(i - this.mCurrentTargetOffsetTop, true);
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 5) {
                        this.mActivePointerId = MotionEventCompat.getPointerId(motionEvent, MotionEventCompat.getActionIndex(motionEvent));
                    } else if (actionMasked == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            int i2 = this.mActivePointerId;
            if (i2 == -1) {
                return false;
            }
            float y2 = MotionEventCompat.getY(motionEvent, MotionEventCompat.findPointerIndex(motionEvent, i2));
            if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[this.mDirection.ordinal()] == 1) {
                f = (this.mInitialMotionY - y2) * DRAG_RATE;
            } else {
                f = (y2 - this.mInitialMotionY) * DRAG_RATE;
            }
            this.mIsBeingDragged = false;
            if (f > this.mTotalDragDistance) {
                setRefreshing(true, true);
            } else {
                this.mRefreshing = false;
                this.mProgress.setStartEndTrim(0.0f, 0.0f);
                animateOffsetToStartPosition(this.mCurrentTargetOffsetTop, !this.mScale ? new Animation.AnimationListener() { // from class: com.ys.module.swip.SwipyRefreshLayout.5
                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        if (SwipyRefreshLayout.this.mScale) {
                            return;
                        }
                        SwipyRefreshLayout.this.startScaleDownAnimation(null);
                    }
                } : null);
                this.mProgress.showArrow(false);
            }
            this.mActivePointerId = -1;
            return false;
        }
        return true;
    }

    private void animateOffsetToCorrectPosition(int i, Animation.AnimationListener animationListener) {
        this.mFrom = i;
        this.mAnimateToCorrectPosition.reset();
        this.mAnimateToCorrectPosition.setDuration(200L);
        this.mAnimateToCorrectPosition.setInterpolator(this.mDecelerateInterpolator);
        if (animationListener != null) {
            this.mCircleView.setAnimationListener(animationListener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int i, Animation.AnimationListener animationListener) {
        if (this.mScale) {
            startScaleDownReturnToStartAnimation(i, animationListener);
            return;
        }
        this.mFrom = i;
        this.mAnimateToStartPosition.reset();
        this.mAnimateToStartPosition.setDuration(200L);
        this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
        if (animationListener != null) {
            this.mCircleView.setAnimationListener(animationListener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToStartPosition);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void moveToStart(float f) {
        setTargetOffsetTopAndBottom((this.mFrom + ((int) ((this.mOriginalOffsetTop - r0) * f))) - this.mCircleView.getTop(), false);
    }

    private void startScaleDownReturnToStartAnimation(int i, Animation.AnimationListener animationListener) {
        this.mFrom = i;
        if (isAlphaUsedForScale()) {
            this.mStartingScale = this.mProgress.getAlpha();
        } else {
            this.mStartingScale = ViewCompat.getScaleX(this.mCircleView);
        }
        Animation animation = new Animation() { // from class: com.ys.module.swip.SwipyRefreshLayout.8
            @Override // android.view.animation.Animation
            public void applyTransformation(float f, Transformation transformation) {
                SwipyRefreshLayout.this.setAnimationProgress(SwipyRefreshLayout.this.mStartingScale + ((-SwipyRefreshLayout.this.mStartingScale) * f));
                SwipyRefreshLayout.this.moveToStart(f);
            }
        };
        this.mScaleDownToStartAnimation = animation;
        animation.setDuration(150L);
        if (animationListener != null) {
            this.mCircleView.setAnimationListener(animationListener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownToStartAnimation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTargetOffsetTopAndBottom(int i, boolean z) {
        this.mCircleView.bringToFront();
        this.mCircleView.offsetTopAndBottom(i);
        this.mCurrentTargetOffsetTop = this.mCircleView.getTop();
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        if (MotionEventCompat.getPointerId(motionEvent, actionIndex) == this.mActivePointerId) {
            this.mActivePointerId = MotionEventCompat.getPointerId(motionEvent, actionIndex == 0 ? 1 : 0);
        }
    }

    public SwipyRefreshLayoutDirection getDirection() {
        return this.mBothDirection ? SwipyRefreshLayoutDirection.BOTH : this.mDirection;
    }

    public void setDirection(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
        if (swipyRefreshLayoutDirection == SwipyRefreshLayoutDirection.BOTH) {
            this.mBothDirection = true;
        } else {
            this.mBothDirection = false;
            this.mDirection = swipyRefreshLayoutDirection;
        }
        if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[this.mDirection.ordinal()] == 1) {
            int measuredHeight = getMeasuredHeight() - this.mCircleView.getMeasuredHeight();
            this.mOriginalOffsetTop = measuredHeight;
            this.mCurrentTargetOffsetTop = measuredHeight;
        } else {
            int i = -this.mCircleView.getMeasuredHeight();
            this.mOriginalOffsetTop = i;
            this.mCurrentTargetOffsetTop = i;
        }
    }

    private void setRawDirection(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
        if (this.mDirection == swipyRefreshLayoutDirection) {
            return;
        }
        this.mDirection = swipyRefreshLayoutDirection;
        if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[this.mDirection.ordinal()] == 1) {
            int measuredHeight = getMeasuredHeight() - this.mCircleView.getMeasuredHeight();
            this.mOriginalOffsetTop = measuredHeight;
            this.mCurrentTargetOffsetTop = measuredHeight;
        } else {
            int i = -this.mCircleView.getMeasuredHeight();
            this.mOriginalOffsetTop = i;
            this.mCurrentTargetOffsetTop = i;
        }
    }
}
