package com.gddai.lioncheck.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.v4.internal.view.SupportMenu;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.utils.CircularRingUtils;

/* JADX INFO: loaded from: classes.dex */
public class CircleProgressView extends View {
    progessCallBack callBack;
    float centre;
    private float currentProgess;
    private int defaultValue;
    private boolean isCircle;
    private int max;
    Paint paint;
    int radius;
    private int roundProgressColor;
    int roundWidth;
    private float sweepAngle;
    private boolean touchable;

    public interface progessCallBack {
        void progess(float f);
    }

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CircleProgressView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.max = 15;
        this.currentProgess = 0.0f;
        this.sweepAngle = 75.0f;
        this.radius = 0;
        this.isCircle = false;
        this.paint = new Paint();
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.RoundProgressBar);
        this.roundProgressColor = typedArrayObtainStyledAttributes.getColor(2, getResources().getColor(R.color.circle_ring_color));
        this.touchable = typedArrayObtainStyledAttributes.getBoolean(9, false);
        this.max = typedArrayObtainStyledAttributes.getInteger(1, 100);
        this.defaultValue = typedArrayObtainStyledAttributes.getInteger(1, 0);
        setSweepAngle((r3 * 360) / this.max);
        typedArrayObtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.radius == 0) {
            this.centre = (float) ((((double) getWidth()) * 394.0d) / 788.0d);
            this.roundWidth = (getWidth() * 29) / 394;
            this.radius = (int) ((this.centre - (r0 / 2)) - 8.0f);
        }
        this.paint.setColor(-1);
        this.paint.setShader(null);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeCap(Paint.Cap.ROUND);
        this.paint.setStrokeWidth(this.roundWidth);
        this.paint.setAntiAlias(true);
        float f = this.centre;
        canvas.drawCircle(f, f, this.radius, this.paint);
        float f2 = this.centre;
        int i = this.radius;
        RectF rectF = new RectF(f2 - i, f2 - i, i + f2, f2 + i);
        if (this.currentProgess <= 0.1d) {
            int[] iArr = {SupportMenu.CATEGORY_MASK, SupportMenu.CATEGORY_MASK};
            float f3 = this.centre;
            this.paint.setShader(new SweepGradient(f3, f3, iArr, (float[]) null));
        } else {
            float f4 = this.centre;
            this.paint.setShader(new SweepGradient(f4, f4, new int[]{-14483706, -14483706}, (float[]) null));
        }
        float f5 = this.sweepAngle;
        canvas.drawArc(rectF, 90.0f - (f5 / 2.0f), f5, false, this.paint);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        int action = motionEvent.getAction();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (action == 0 || action == 2) {
            if (CircularRingUtils.isCircleCentra(x, y, this.centre, this.radius, this.roundWidth)) {
                this.isCircle = true;
            } else {
                this.isCircle = false;
                if (this.touchable && CircularRingUtils.isRingCentra(x, y, this.centre, this.radius, this.roundWidth / 2)) {
                    setSweepAngle(CircularRingUtils.getSweepAngle(x, y, this.centre));
                    progessCallBack progesscallback = this.callBack;
                    if (progesscallback != null) {
                        progesscallback.progess(this.currentProgess);
                    }
                }
            }
        }
        return true;
    }

    public int getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(int i) {
        this.defaultValue = i;
        setSweepAngle((i * 360) / this.max);
    }

    public boolean isTouchable() {
        return this.touchable;
    }

    public void setTouchable(boolean z) {
        this.touchable = z;
    }

    public float getSweepAngle() {
        return this.sweepAngle;
    }

    public void setSweepAngle(float f) {
        this.sweepAngle = f;
        this.currentProgess = (float) ((((double) f) * 1.0d) / 360.0d);
        invalidate();
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int i) {
        this.max = i;
    }

    public float getCurrentProgess() {
        return this.currentProgess;
    }

    public void setCurrentProgess(float f) {
        if (f < 0.0f) {
            f = 0.0f;
        }
        if (f > 1.0f) {
            f = 1.0f;
        }
        setSweepAngle(f * 360.0f);
    }

    public boolean isCircle() {
        return this.isCircle;
    }

    public void setIsCircle(boolean z) {
        this.isCircle = z;
    }

    public progessCallBack getCallBack() {
        return this.callBack;
    }

    public void setCallBack(progessCallBack progesscallback) {
        this.callBack = progesscallback;
    }
}
