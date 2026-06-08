package lib.lhh.fiv.library.gestures;

import android.view.MotionEvent;

/* JADX INFO: loaded from: classes.dex */
public class MultiPointerGestureDetector {
    private static final int MAX_POINTERS = 2;
    private int mCount;
    private boolean mGestureInProgress;
    private final int[] mId = new int[2];
    private final float[] mStartX = new float[2];
    private final float[] mStartY = new float[2];
    private final float[] mCurrentX = new float[2];
    private final float[] mCurrentY = new float[2];
    private Listener mListener = null;

    public interface Listener {
        void onGestureBegin(MultiPointerGestureDetector multiPointerGestureDetector);

        void onGestureEnd(MultiPointerGestureDetector multiPointerGestureDetector);

        void onGestureUpdate(MultiPointerGestureDetector multiPointerGestureDetector);
    }

    protected boolean shouldStartGesture() {
        return true;
    }

    public MultiPointerGestureDetector() {
        reset();
    }

    public static MultiPointerGestureDetector newInstance() {
        return new MultiPointerGestureDetector();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void reset() {
        this.mGestureInProgress = false;
        this.mCount = 0;
        for (int i = 0; i < 2; i++) {
            this.mId[i] = -1;
        }
    }

    private void startGesture() {
        if (this.mGestureInProgress) {
            return;
        }
        this.mGestureInProgress = true;
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onGestureBegin(this);
        }
    }

    private void stopGesture() {
        if (this.mGestureInProgress) {
            this.mGestureInProgress = false;
            Listener listener = this.mListener;
            if (listener != null) {
                listener.onGestureEnd(this);
            }
        }
    }

    private int getPressedPointerIndex(MotionEvent motionEvent, int i) {
        int pointerCount = motionEvent.getPointerCount();
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        if ((actionMasked == 1 || actionMasked == 6) && i >= actionIndex) {
            i++;
        }
        if (i < pointerCount) {
            return i;
        }
        return -1;
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x0059  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onTouchEvent(android.view.MotionEvent r10) {
        /*
            r9 = this;
            int r0 = r10.getActionMasked()
            r1 = -1
            r2 = 0
            r3 = 2
            r4 = 1
            if (r0 == 0) goto L59
            if (r0 == r4) goto L59
            if (r0 == r3) goto L21
            r5 = 3
            if (r0 == r5) goto L19
            r5 = 5
            if (r0 == r5) goto L59
            r5 = 6
            if (r0 == r5) goto L59
            goto L9b
        L19:
            r9.stopGesture()
            r9.reset()
            goto L9b
        L21:
            if (r2 >= r3) goto L40
            int[] r0 = r9.mId
            r0 = r0[r2]
            int r0 = r10.findPointerIndex(r0)
            if (r0 == r1) goto L3d
            float[] r5 = r9.mCurrentX
            float r6 = r10.getX(r0)
            r5[r2] = r6
            float[] r5 = r9.mCurrentY
            float r0 = r10.getY(r0)
            r5[r2] = r0
        L3d:
            int r2 = r2 + 1
            goto L21
        L40:
            boolean r10 = r9.mGestureInProgress
            if (r10 != 0) goto L4d
            boolean r10 = r9.shouldStartGesture()
            if (r10 == 0) goto L4d
            r9.startGesture()
        L4d:
            boolean r10 = r9.mGestureInProgress
            if (r10 == 0) goto L9b
            lib.lhh.fiv.library.gestures.MultiPointerGestureDetector$Listener r10 = r9.mListener
            if (r10 == 0) goto L9b
            r10.onGestureUpdate(r9)
            goto L9b
        L59:
            boolean r0 = r9.mGestureInProgress
            r9.stopGesture()
            r9.reset()
        L61:
            if (r2 >= r3) goto L92
            int r5 = r9.getPressedPointerIndex(r10, r2)
            if (r5 != r1) goto L6a
            goto L92
        L6a:
            int[] r6 = r9.mId
            int r7 = r10.getPointerId(r5)
            r6[r2] = r7
            float[] r6 = r9.mCurrentX
            float[] r7 = r9.mStartX
            float r8 = r10.getX(r5)
            r7[r2] = r8
            r6[r2] = r8
            float[] r6 = r9.mCurrentY
            float[] r7 = r9.mStartY
            float r5 = r10.getY(r5)
            r7[r2] = r5
            r6[r2] = r5
            int r5 = r9.mCount
            int r5 = r5 + r4
            r9.mCount = r5
            int r2 = r2 + 1
            goto L61
        L92:
            if (r0 == 0) goto L9b
            int r10 = r9.mCount
            if (r10 <= 0) goto L9b
            r9.startGesture()
        L9b:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: lib.lhh.fiv.library.gestures.MultiPointerGestureDetector.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void restartGesture() {
        if (this.mGestureInProgress) {
            stopGesture();
            for (int i = 0; i < 2; i++) {
                this.mStartX[i] = this.mCurrentX[i];
                this.mStartY[i] = this.mCurrentY[i];
            }
            startGesture();
        }
    }

    public boolean isGestureInProgress() {
        return this.mGestureInProgress;
    }

    public int getCount() {
        return this.mCount;
    }

    public float[] getStartX() {
        return this.mStartX;
    }

    public float[] getStartY() {
        return this.mStartY;
    }

    public float[] getCurrentX() {
        return this.mCurrentX;
    }

    public float[] getCurrentY() {
        return this.mCurrentY;
    }
}
