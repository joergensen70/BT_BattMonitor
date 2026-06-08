package lib.lhh.fiv.library.zoomable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.facebook.common.internal.Preconditions;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.GenericDraweeView;
import lib.lhh.fiv.library.zoomable.ZoomableController;

/* JADX INFO: loaded from: classes.dex */
public class ZoomableDraweeView extends GenericDraweeView implements ZoomableController.Listener {
    private static final float HUGE_IMAGE_SCALE_FACTOR_THRESHOLD = 1.1f;
    private static final int TOUCH_TIME = 250;
    private final ControllerListener mControllerListener;
    public long mCurrDownTime;
    private DraweeController mHugeImageController;
    private final RectF mImageBounds;
    public View.OnClickListener mOnClickListener;
    private final RectF mViewBounds;
    private ZoomableController mZoomableController;

    public ZoomableDraweeView(Context context) {
        super(context);
        this.mImageBounds = new RectF();
        this.mViewBounds = new RectF();
        this.mControllerListener = new BaseControllerListener<Object>() { // from class: lib.lhh.fiv.library.zoomable.ZoomableDraweeView.1
            @Override // com.facebook.drawee.controller.BaseControllerListener, com.facebook.drawee.controller.ControllerListener
            public void onFinalImageSet(String str, Object obj, Animatable animatable) {
                ZoomableDraweeView.this.onFinalImageSet();
            }

            @Override // com.facebook.drawee.controller.BaseControllerListener, com.facebook.drawee.controller.ControllerListener
            public void onRelease(String str) {
                ZoomableDraweeView.this.onRelease();
            }
        };
        this.mZoomableController = DefaultZoomableController.newInstance();
        this.mCurrDownTime = 0L;
        init();
    }

    public ZoomableDraweeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mImageBounds = new RectF();
        this.mViewBounds = new RectF();
        this.mControllerListener = new BaseControllerListener<Object>() { // from class: lib.lhh.fiv.library.zoomable.ZoomableDraweeView.1
            @Override // com.facebook.drawee.controller.BaseControllerListener, com.facebook.drawee.controller.ControllerListener
            public void onFinalImageSet(String str, Object obj, Animatable animatable) {
                ZoomableDraweeView.this.onFinalImageSet();
            }

            @Override // com.facebook.drawee.controller.BaseControllerListener, com.facebook.drawee.controller.ControllerListener
            public void onRelease(String str) {
                ZoomableDraweeView.this.onRelease();
            }
        };
        this.mZoomableController = DefaultZoomableController.newInstance();
        this.mCurrDownTime = 0L;
        init();
    }

    public ZoomableDraweeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mImageBounds = new RectF();
        this.mViewBounds = new RectF();
        this.mControllerListener = new BaseControllerListener<Object>() { // from class: lib.lhh.fiv.library.zoomable.ZoomableDraweeView.1
            @Override // com.facebook.drawee.controller.BaseControllerListener, com.facebook.drawee.controller.ControllerListener
            public void onFinalImageSet(String str, Object obj, Animatable animatable) {
                ZoomableDraweeView.this.onFinalImageSet();
            }

            @Override // com.facebook.drawee.controller.BaseControllerListener, com.facebook.drawee.controller.ControllerListener
            public void onRelease(String str) {
                ZoomableDraweeView.this.onRelease();
            }
        };
        this.mZoomableController = DefaultZoomableController.newInstance();
        this.mCurrDownTime = 0L;
        init();
    }

    private void init() {
        this.mZoomableController.setListener(this);
    }

    public void setZoomableController(ZoomableController zoomableController) {
        Preconditions.checkNotNull(zoomableController);
        this.mZoomableController.setListener(null);
        this.mZoomableController = zoomableController;
        zoomableController.setListener(this);
    }

    @Override // com.facebook.drawee.view.DraweeView
    public void setController(DraweeController draweeController) {
        setControllers(draweeController, null);
    }

    private void setControllersInternal(DraweeController draweeController, DraweeController draweeController2) {
        removeControllerListener(getController());
        addControllerListener(draweeController);
        this.mHugeImageController = draweeController2;
        super.setController(draweeController);
    }

    public void setControllers(DraweeController draweeController, DraweeController draweeController2) {
        setControllersInternal(null, null);
        this.mZoomableController.setEnabled(false);
        setControllersInternal(draweeController, draweeController2);
    }

    private void maybeSetHugeImageController() {
        if (this.mHugeImageController == null || this.mZoomableController.getScaleFactor() <= HUGE_IMAGE_SCALE_FACTOR_THRESHOLD) {
            return;
        }
        setControllersInternal(this.mHugeImageController, null);
    }

    private void removeControllerListener(DraweeController draweeController) {
        if (draweeController instanceof AbstractDraweeController) {
            ((AbstractDraweeController) draweeController).removeControllerListener(this.mControllerListener);
        }
    }

    private void addControllerListener(DraweeController draweeController) {
        if (draweeController instanceof AbstractDraweeController) {
            ((AbstractDraweeController) draweeController).addControllerListener(this.mControllerListener);
        }
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        int iSave = canvas.save();
        canvas.concat(this.mZoomableController.getTransform());
        super.onDraw(canvas);
        canvas.restoreToCount(iSave);
    }

    public void setOnDraweeClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @Override // com.facebook.drawee.view.DraweeView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        View.OnClickListener onClickListener;
        if (motionEvent.getAction() == 0) {
            this.mCurrDownTime = motionEvent.getEventTime();
        }
        if (motionEvent.getAction() == 1 && motionEvent.getEventTime() - this.mCurrDownTime <= 250 && (onClickListener = this.mOnClickListener) != null) {
            onClickListener.onClick(this);
        }
        if (!this.mZoomableController.onTouchEvent(motionEvent)) {
            return super.onTouchEvent(motionEvent);
        }
        if (this.mZoomableController.getScaleFactor() > 1.0f) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }

    public void clearZoom() {
        ZoomableController zoomableController = this.mZoomableController;
        if (zoomableController == null || !(zoomableController instanceof DefaultZoomableController)) {
            return;
        }
        ((DefaultZoomableController) this.mZoomableController).zoomToImagePoint(1.0f, new PointF(getWidth() / 2, getHeight() / 2));
    }

    @Override // android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateZoomableControllerBounds();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinalImageSet() {
        if (this.mZoomableController.isEnabled()) {
            return;
        }
        updateZoomableControllerBounds();
        this.mZoomableController.setEnabled(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRelease() {
        this.mZoomableController.setEnabled(false);
    }

    @Override // lib.lhh.fiv.library.zoomable.ZoomableController.Listener
    public void onTransformChanged(Matrix matrix) {
        maybeSetHugeImageController();
        invalidate();
    }

    private void updateZoomableControllerBounds() {
        getHierarchy().getActualImageBounds(this.mImageBounds);
        this.mViewBounds.set(0.0f, 0.0f, getWidth(), getHeight());
        this.mZoomableController.setImageBounds(this.mImageBounds);
        this.mZoomableController.setViewBounds(this.mViewBounds);
    }
}
