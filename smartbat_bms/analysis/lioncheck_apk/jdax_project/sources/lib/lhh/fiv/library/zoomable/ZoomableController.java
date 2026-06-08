package lib.lhh.fiv.library.zoomable;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;

/* JADX INFO: loaded from: classes.dex */
public interface ZoomableController {

    public interface Listener {
        void onTransformChanged(Matrix matrix);
    }

    float getScaleFactor();

    Matrix getTransform();

    boolean isEnabled();

    boolean onTouchEvent(MotionEvent motionEvent);

    void setEnabled(boolean z);

    void setImageBounds(RectF rectF);

    void setListener(Listener listener);

    void setViewBounds(RectF rectF);
}
