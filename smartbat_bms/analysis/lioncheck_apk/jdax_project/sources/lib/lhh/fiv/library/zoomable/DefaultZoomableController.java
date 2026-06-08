package lib.lhh.fiv.library.zoomable;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import lib.lhh.fiv.library.gestures.TransformGestureDetector;
import lib.lhh.fiv.library.zoomable.ZoomableController;

/* JADX INFO: loaded from: classes.dex */
public class DefaultZoomableController implements ZoomableController, TransformGestureDetector.Listener {
    private TransformGestureDetector mGestureDetector;
    private ZoomableController.Listener mListener = null;
    private boolean mIsEnabled = false;
    private boolean mIsRotationEnabled = false;
    private boolean mIsScaleEnabled = true;
    private boolean mIsTranslationEnabled = true;
    private float mMinScaleFactor = 1.0f;
    private float mMaxScaleFactor = Float.POSITIVE_INFINITY;
    private final RectF mViewBounds = new RectF();
    private final RectF mImageBounds = new RectF();
    private final RectF mTransformedImageBounds = new RectF();
    private final Matrix mPreviousTransform = new Matrix();
    private final Matrix mActiveTransform = new Matrix();
    private final Matrix mActiveTransformInverse = new Matrix();
    private final float[] mTempValues = new float[9];

    public DefaultZoomableController(TransformGestureDetector transformGestureDetector) {
        this.mGestureDetector = transformGestureDetector;
        transformGestureDetector.setListener(this);
    }

    public static DefaultZoomableController newInstance() {
        return new DefaultZoomableController(TransformGestureDetector.newInstance());
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public void setListener(ZoomableController.Listener listener) {
        this.mListener = listener;
    }

    public void reset() {
        this.mGestureDetector.reset();
        this.mPreviousTransform.reset();
        this.mActiveTransform.reset();
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public void setEnabled(boolean z) {
        this.mIsEnabled = z;
        if (z) {
            return;
        }
        reset();
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public void setRotationEnabled(boolean z) {
        this.mIsRotationEnabled = z;
    }

    public boolean isRotationEnabled() {
        return this.mIsRotationEnabled;
    }

    public void setScaleEnabled(boolean z) {
        this.mIsScaleEnabled = z;
    }

    public boolean isScaleEnabled() {
        return this.mIsScaleEnabled;
    }

    public void setTranslationEnabled(boolean z) {
        this.mIsTranslationEnabled = z;
    }

    public boolean isTranslationEnabled() {
        return this.mIsTranslationEnabled;
    }

    public RectF getImageBounds() {
        return this.mImageBounds;
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public void setImageBounds(RectF rectF) {
        this.mImageBounds.set(rectF);
    }

    public RectF getViewBounds() {
        return this.mViewBounds;
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public void setViewBounds(RectF rectF) {
        this.mViewBounds.set(rectF);
    }

    public float getMinScaleFactor() {
        return this.mMinScaleFactor;
    }

    public void setMinScaleFactor(float f) {
        this.mMinScaleFactor = f;
    }

    public float getMaxScaleFactor() {
        return this.mMaxScaleFactor;
    }

    public void setMaxScaleFactor(float f) {
        this.mMaxScaleFactor = f;
    }

    public PointF mapViewToImage(PointF pointF) {
        float[] fArr = this.mTempValues;
        fArr[0] = pointF.x;
        fArr[1] = pointF.y;
        this.mActiveTransform.invert(this.mActiveTransformInverse);
        this.mActiveTransformInverse.mapPoints(fArr, 0, fArr, 0, 1);
        mapAbsoluteToRelative(fArr, fArr, 1);
        return new PointF(fArr[0], fArr[1]);
    }

    public PointF mapImageToView(PointF pointF) {
        float[] fArr = this.mTempValues;
        fArr[0] = pointF.x;
        fArr[1] = pointF.y;
        mapRelativeToAbsolute(fArr, fArr, 1);
        this.mActiveTransform.mapPoints(fArr, 0, fArr, 0, 1);
        return new PointF(fArr[0], fArr[1]);
    }

    private void mapAbsoluteToRelative(float[] fArr, float[] fArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = i2 * 2;
            fArr[i3] = (fArr2[i3] - this.mImageBounds.left) / this.mImageBounds.width();
            int i4 = i3 + 1;
            fArr[i4] = (fArr2[i4] - this.mImageBounds.top) / this.mImageBounds.height();
        }
    }

    private void mapRelativeToAbsolute(float[] fArr, float[] fArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = i2 * 2;
            fArr[i3] = (fArr2[i3] * this.mImageBounds.width()) + this.mImageBounds.left;
            int i4 = i3 + 1;
            fArr[i4] = (fArr2[i4] * this.mImageBounds.height()) + this.mImageBounds.top;
        }
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public Matrix getTransform() {
        return this.mActiveTransform;
    }

    public void setTransform(Matrix matrix) {
        if (this.mGestureDetector.isGestureInProgress()) {
            this.mGestureDetector.reset();
        }
        this.mActiveTransform.set(matrix);
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mIsEnabled) {
            return this.mGestureDetector.onTouchEvent(motionEvent);
        }
        return false;
    }

    public void zoomToImagePoint(float f, PointF pointF) {
        if (this.mGestureDetector.isGestureInProgress()) {
            this.mGestureDetector.reset();
        }
        float fLimit = limit(f, this.mMinScaleFactor, this.mMaxScaleFactor);
        float[] fArr = this.mTempValues;
        fArr[0] = pointF.x;
        fArr[1] = pointF.y;
        mapRelativeToAbsolute(fArr, fArr, 1);
        this.mActiveTransform.setScale(fLimit, fLimit, fArr[0], fArr[1]);
        this.mActiveTransform.postTranslate(this.mViewBounds.centerX() - fArr[0], this.mViewBounds.centerY() - fArr[1]);
        limitTranslation();
    }

    @Override // lib.lhh.fiv.library.gestures.TransformGestureDetector.Listener
    public void onGestureBegin(TransformGestureDetector transformGestureDetector) {
        this.mPreviousTransform.set(this.mActiveTransform);
    }

    @Override // lib.lhh.fiv.library.gestures.TransformGestureDetector.Listener
    public void onGestureUpdate(TransformGestureDetector transformGestureDetector) {
        this.mActiveTransform.set(this.mPreviousTransform);
        if (this.mIsRotationEnabled) {
            this.mActiveTransform.postRotate(transformGestureDetector.getRotation() * 57.29578f, transformGestureDetector.getPivotX(), transformGestureDetector.getPivotY());
        }
        if (this.mIsScaleEnabled) {
            float scale = transformGestureDetector.getScale();
            this.mActiveTransform.postScale(scale, scale, transformGestureDetector.getPivotX(), transformGestureDetector.getPivotY());
        }
        limitScale(transformGestureDetector.getPivotX(), transformGestureDetector.getPivotY());
        if (this.mIsTranslationEnabled) {
            this.mActiveTransform.postTranslate(transformGestureDetector.getTranslationX(), transformGestureDetector.getTranslationY());
        }
        if (limitTranslation()) {
            this.mGestureDetector.restartGesture();
        }
        ZoomableController.Listener listener = this.mListener;
        if (listener != null) {
            listener.onTransformChanged(this.mActiveTransform);
        }
    }

    @Override // lib.lhh.fiv.library.gestures.TransformGestureDetector.Listener
    public void onGestureEnd(TransformGestureDetector transformGestureDetector) {
        this.mPreviousTransform.set(this.mActiveTransform);
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController
    public float getScaleFactor() {
        this.mActiveTransform.getValues(this.mTempValues);
        return this.mTempValues[0];
    }

    private void limitScale(float f, float f2) {
        float scaleFactor = getScaleFactor();
        float fLimit = limit(scaleFactor, this.mMinScaleFactor, this.mMaxScaleFactor);
        if (fLimit != scaleFactor) {
            float f3 = fLimit / scaleFactor;
            this.mActiveTransform.postScale(f3, f3, f, f2);
        }
    }

    private boolean limitTranslation() {
        RectF rectF = this.mTransformedImageBounds;
        rectF.set(this.mImageBounds);
        this.mActiveTransform.mapRect(rectF);
        float offset = getOffset(rectF.left, rectF.width(), this.mViewBounds.width());
        float offset2 = getOffset(rectF.top, rectF.height(), this.mViewBounds.height());
        if (offset == rectF.left && offset2 == rectF.top) {
            return false;
        }
        this.mActiveTransform.postTranslate(offset - rectF.left, offset2 - rectF.top);
        return true;
    }

    private float getOffset(float f, float f2, float f3) {
        float f4 = f3 - f2;
        return f4 > 0.0f ? f4 / 2.0f : limit(f, f4, 0.0f);
    }

    private float limit(float f, float f2, float f3) {
        return Math.min(Math.max(f2, f), f3);
    }
}
