package lib.lhh.fiv.library.gestures;

import android.view.MotionEvent;
import lib.lhh.fiv.library.gestures.MultiPointerGestureDetector;

/* JADX INFO: loaded from: classes.dex */
public class TransformGestureDetector implements MultiPointerGestureDetector.Listener {
    private final MultiPointerGestureDetector mDetector;
    private Listener mListener = null;

    public interface Listener {
        void onGestureBegin(TransformGestureDetector transformGestureDetector);

        void onGestureEnd(TransformGestureDetector transformGestureDetector);

        void onGestureUpdate(TransformGestureDetector transformGestureDetector);
    }

    public TransformGestureDetector(MultiPointerGestureDetector multiPointerGestureDetector) {
        this.mDetector = multiPointerGestureDetector;
        multiPointerGestureDetector.setListener(this);
    }

    public static TransformGestureDetector newInstance() {
        return new TransformGestureDetector(MultiPointerGestureDetector.newInstance());
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void reset() {
        this.mDetector.reset();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mDetector.onTouchEvent(motionEvent);
    }

    @Override // lib.lhh.fiv.library.gestures.MultiPointerGestureDetector.Listener
    public void onGestureBegin(MultiPointerGestureDetector multiPointerGestureDetector) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onGestureBegin(this);
        }
    }

    @Override // lib.lhh.fiv.library.gestures.MultiPointerGestureDetector.Listener
    public void onGestureUpdate(MultiPointerGestureDetector multiPointerGestureDetector) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onGestureUpdate(this);
        }
    }

    @Override // lib.lhh.fiv.library.gestures.MultiPointerGestureDetector.Listener
    public void onGestureEnd(MultiPointerGestureDetector multiPointerGestureDetector) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onGestureEnd(this);
        }
    }

    private float calcAverage(float[] fArr, int i) {
        float f = 0.0f;
        for (int i2 = 0; i2 < i; i2++) {
            f += fArr[i2];
        }
        if (i > 0) {
            return f / i;
        }
        return 0.0f;
    }

    public void restartGesture() {
        this.mDetector.restartGesture();
    }

    public boolean isGestureInProgress() {
        return this.mDetector.isGestureInProgress();
    }

    public float getPivotX() {
        return calcAverage(this.mDetector.getStartX(), this.mDetector.getCount());
    }

    public float getPivotY() {
        return calcAverage(this.mDetector.getStartY(), this.mDetector.getCount());
    }

    public float getTranslationX() {
        return calcAverage(this.mDetector.getCurrentX(), this.mDetector.getCount()) - calcAverage(this.mDetector.getStartX(), this.mDetector.getCount());
    }

    public float getTranslationY() {
        return calcAverage(this.mDetector.getCurrentY(), this.mDetector.getCount()) - calcAverage(this.mDetector.getStartY(), this.mDetector.getCount());
    }

    public float getScale() {
        if (this.mDetector.getCount() < 2) {
            return 1.0f;
        }
        float f = this.mDetector.getStartX()[1] - this.mDetector.getStartX()[0];
        float f2 = this.mDetector.getStartY()[1] - this.mDetector.getStartY()[0];
        return ((float) Math.hypot(this.mDetector.getCurrentX()[1] - this.mDetector.getCurrentX()[0], this.mDetector.getCurrentY()[1] - this.mDetector.getCurrentY()[0])) / ((float) Math.hypot(f, f2));
    }

    public float getRotation() {
        if (this.mDetector.getCount() < 2) {
            return 0.0f;
        }
        float f = this.mDetector.getStartX()[1] - this.mDetector.getStartX()[0];
        float f2 = this.mDetector.getStartY()[1] - this.mDetector.getStartY()[0];
        float f3 = this.mDetector.getCurrentX()[1] - this.mDetector.getCurrentX()[0];
        return ((float) Math.atan2(this.mDetector.getCurrentY()[1] - this.mDetector.getCurrentY()[0], f3)) - ((float) Math.atan2(f2, f));
    }
}
