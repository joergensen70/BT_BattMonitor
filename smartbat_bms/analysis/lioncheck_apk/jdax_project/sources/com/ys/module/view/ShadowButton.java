package com.ys.module.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Looper;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class ShadowButton extends Button {
    private int alpha;
    private Rect backRect;
    private boolean isPress;
    private boolean isStartThread;
    private boolean is_circle;
    private boolean is_round;
    private Callback mCallback;
    private GestureDetector mGestureDetector;
    private Paint paint;
    private int press_Color;
    int radius;
    private int unpress_Color;

    public interface Callback {
        void buttonClick();
    }

    public ShadowButton(Context context) {
        this(context, null);
    }

    public ShadowButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ShadowButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.unpress_Color = Color.parseColor("#FFFF6E37");
        this.press_Color = Color.parseColor("#FFFF5210");
        this.is_circle = false;
        this.is_round = true;
        this.isStartThread = true;
        this.isPress = true;
        this.alpha = 255;
        this.radius = 10;
        this.paint = new Paint();
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.shadow_button);
        this.press_Color = typedArrayObtainStyledAttributes.getColor(R.styleable.shadow_button_pressColor, this.press_Color);
        this.unpress_Color = typedArrayObtainStyledAttributes.getColor(R.styleable.shadow_button_unpressColor, this.unpress_Color);
        this.is_circle = typedArrayObtainStyledAttributes.getBoolean(R.styleable.shadow_button_isCircle, this.is_circle);
        this.is_round = typedArrayObtainStyledAttributes.getBoolean(R.styleable.shadow_button_isround, this.is_round);
        typedArrayObtainStyledAttributes.recycle();
        this.mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() { // from class: com.ys.module.view.ShadowButton.1
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                ShadowButton.this.clickEvent();
                return super.onSingleTapUp(motionEvent);
            }
        });
    }

    @Override // android.widget.TextView, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.backRect = new Rect(0, 0, measureDimension(280, i), measureDimension(140, i2));
    }

    public int measureDimension(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        return mode == 1073741824 ? size : mode == Integer.MIN_VALUE ? Math.min(i, size) : i;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.is_circle) {
            this.radius = this.backRect.height() / 2;
            this.paint.setColor(this.press_Color);
            RectF rectF = new RectF(this.backRect);
            int i = this.radius;
            canvas.drawRoundRect(rectF, i, i, this.paint);
            this.paint.setColor(this.unpress_Color);
            this.paint.setAlpha(this.alpha);
            RectF rectF2 = new RectF(this.backRect);
            int i2 = this.radius;
            canvas.drawRoundRect(rectF2, i2, i2, this.paint);
        } else if (this.is_round) {
            this.radius = 10;
            this.paint.setColor(this.press_Color);
            RectF rectF3 = new RectF(this.backRect);
            int i3 = this.radius;
            canvas.drawRoundRect(rectF3, i3, i3, this.paint);
            this.paint.setColor(this.unpress_Color);
            this.paint.setAlpha(this.alpha);
            RectF rectF4 = new RectF(this.backRect);
            int i4 = this.radius;
            canvas.drawRoundRect(rectF4, i4, i4, this.paint);
        } else {
            this.paint.setColor(this.press_Color);
            canvas.drawRect(this.backRect, this.paint);
            this.paint.setColor(this.unpress_Color);
            this.paint.setAlpha(this.alpha);
            canvas.drawRect(this.backRect, this.paint);
        }
        super.onDraw(canvas);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.mGestureDetector.onTouchEvent(motionEvent);
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (actionMasked == 0) {
            this.isPress = true;
            pressEffec();
        } else if (actionMasked == 1 || actionMasked == 3) {
            this.isPress = false;
            pressEffec();
        }
        return true;
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.ys.module.view.ShadowButton$2] */
    private void pressEffec() {
        if (this.isStartThread) {
            new Thread() { // from class: com.ys.module.view.ShadowButton.2
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    super.run();
                    while (ShadowButton.this.isJumpLoop()) {
                        try {
                            Thread.sleep(2L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ShadowButton.this.isStartThread = true;
                }
            }.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isJumpLoop() {
        boolean z = this.isPress;
        if (z && this.alpha <= 0) {
            return false;
        }
        if (!z && this.alpha >= 255) {
            return false;
        }
        if (z) {
            this.alpha -= 15;
        } else {
            this.alpha += 15;
        }
        invalidateView();
        return true;
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clickEvent() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.buttonClick();
        }
    }
}
