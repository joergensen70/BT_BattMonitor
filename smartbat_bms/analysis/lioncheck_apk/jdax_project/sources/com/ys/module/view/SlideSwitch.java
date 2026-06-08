package com.ys.module.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class SlideSwitch extends View {
    private static final int COLOR_THEME = Color.parseColor("#ff00ee00");
    private static final int RIM_SIZE = 6;
    public static final int SHAPE_CIRCLE = 2;
    public static final int SHAPE_RECT = 1;
    private int alpha;
    private Rect backRect;
    private int color_theme;
    private int diffX;
    private int eventLastX;
    private int eventStartX;
    private Rect frontRect;
    private int frontRect_left;
    private int frontRect_left_begin;
    private boolean isOpen;
    private SlideListener listener;
    private int max_left;
    private int min_left;
    private Paint paint;
    private int shape;

    public interface SlideListener {
        void close();

        void open();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    static /* synthetic */ int access$112(SlideSwitch slideSwitch, int i) {
        int i2 = slideSwitch.frontRect_left + i;
        slideSwitch.frontRect_left = i2;
        return i2;
    }

    static /* synthetic */ int access$120(SlideSwitch slideSwitch, int i) {
        int i2 = slideSwitch.frontRect_left - i;
        slideSwitch.frontRect_left = i2;
        return i2;
    }

    public SlideSwitch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.frontRect_left_begin = 6;
        this.diffX = 0;
        this.listener = null;
        this.paint = new Paint();
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.slideswitch);
        this.color_theme = typedArrayObtainStyledAttributes.getColor(R.styleable.slideswitch_themeColor, COLOR_THEME);
        this.isOpen = typedArrayObtainStyledAttributes.getBoolean(R.styleable.slideswitch_isOpen, false);
        this.shape = typedArrayObtainStyledAttributes.getInt(R.styleable.slideswitch_shape, 1);
        typedArrayObtainStyledAttributes.recycle();
    }

    public SlideSwitch(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SlideSwitch(Context context) {
        this(context, null);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int iMeasureDimension = measureDimension(280, i);
        int iMeasureDimension2 = measureDimension(140, i2);
        if (this.shape == 2 && iMeasureDimension < iMeasureDimension2) {
            iMeasureDimension = iMeasureDimension2 * 2;
        }
        setMeasuredDimension(iMeasureDimension, iMeasureDimension2);
        initDrawingVal();
    }

    public void initDrawingVal() {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        this.backRect = new Rect(0, 0, measuredWidth, measuredHeight);
        this.min_left = 6;
        if (this.shape == 1) {
            this.max_left = measuredWidth / 2;
        } else {
            this.max_left = (measuredWidth - (measuredHeight - 12)) - 6;
        }
        if (this.isOpen) {
            this.frontRect_left = this.max_left;
            this.alpha = 255;
        } else {
            this.frontRect_left = 6;
            this.alpha = 0;
        }
        this.frontRect_left_begin = this.frontRect_left;
    }

    public int measureDimension(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        return mode == 1073741824 ? size : mode == Integer.MIN_VALUE ? Math.min(i, size) : i;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.shape == 1) {
            this.paint.setColor(-7829368);
            canvas.drawRect(this.backRect, this.paint);
            this.paint.setColor(this.color_theme);
            this.paint.setAlpha(this.alpha);
            canvas.drawRect(this.backRect, this.paint);
            int i = this.frontRect_left;
            this.frontRect = new Rect(i, 6, ((getMeasuredWidth() / 2) + i) - 6, getMeasuredHeight() - 6);
            this.paint.setColor(-1);
            canvas.drawRect(this.frontRect, this.paint);
            return;
        }
        int iHeight = (this.backRect.height() / 2) - 6;
        this.paint.setColor(-7829368);
        float f = iHeight;
        canvas.drawRoundRect(new RectF(this.backRect), f, f, this.paint);
        this.paint.setColor(this.color_theme);
        this.paint.setAlpha(this.alpha);
        canvas.drawRoundRect(new RectF(this.backRect), f, f, this.paint);
        this.frontRect = new Rect(this.frontRect_left, 6, (this.backRect.height() + r3) - 12, this.backRect.height() - 6);
        this.paint.setColor(-1);
        canvas.drawRoundRect(new RectF(this.frontRect), f, f, this.paint);
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x003c  */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            int r0 = android.support.v4.view.MotionEventCompat.getActionMasked(r6)
            r1 = 1
            if (r0 == 0) goto L5d
            r2 = 3
            r3 = 2
            if (r0 == r1) goto L3c
            if (r0 == r3) goto L10
            if (r0 == r2) goto L3c
            goto L64
        L10:
            float r6 = r6.getRawX()
            int r6 = (int) r6
            r5.eventLastX = r6
            int r0 = r5.eventStartX
            int r6 = r6 - r0
            r5.diffX = r6
            int r0 = r5.frontRect_left_begin
            int r6 = r6 + r0
            int r0 = r5.max_left
            if (r6 <= r0) goto L24
            r6 = r0
        L24:
            int r2 = r5.min_left
            if (r6 >= r2) goto L29
            r6 = r2
        L29:
            if (r6 < r2) goto L64
            if (r6 > r0) goto L64
            r5.frontRect_left = r6
            r2 = 1132396544(0x437f0000, float:255.0)
            float r6 = (float) r6
            float r6 = r6 * r2
            float r0 = (float) r0
            float r6 = r6 / r0
            int r6 = (int) r6
            r5.alpha = r6
            r5.invalidateView()
            goto L64
        L3c:
            float r6 = r6.getRawX()
            int r0 = r5.eventStartX
            float r0 = (float) r0
            float r6 = r6 - r0
            int r6 = (int) r6
            int r0 = r5.frontRect_left
            r5.frontRect_left_begin = r0
            int r4 = r5.max_left
            int r4 = r4 / r3
            if (r0 <= r4) goto L50
            r0 = r1
            goto L51
        L50:
            r0 = 0
        L51:
            int r6 = java.lang.Math.abs(r6)
            if (r6 >= r2) goto L59
            r0 = r0 ^ 1
        L59:
            r5.moveToDest(r0)
            goto L64
        L5d:
            float r6 = r6.getRawX()
            int r6 = (int) r6
            r5.eventStartX = r6
        L64:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.ys.module.view.SlideSwitch.onTouchEvent(android.view.MotionEvent):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void setSlideListener(SlideListener slideListener) {
        this.listener = slideListener;
    }

    public void moveToDest(final boolean z) {
        final Handler handler = new Handler() { // from class: com.ys.module.view.SlideSwitch.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    SlideSwitch.this.listener.open();
                } else {
                    SlideSwitch.this.listener.close();
                }
            }
        };
        new Thread(new Runnable() { // from class: com.ys.module.view.SlideSwitch.2
            @Override // java.lang.Runnable
            public void run() {
                if (z) {
                    while (SlideSwitch.this.frontRect_left <= SlideSwitch.this.max_left) {
                        SlideSwitch.this.alpha = (int) ((r0.frontRect_left * 255.0f) / SlideSwitch.this.max_left);
                        SlideSwitch.this.invalidateView();
                        SlideSwitch.access$112(SlideSwitch.this, 3);
                        try {
                            Thread.sleep(3L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    SlideSwitch.this.alpha = 255;
                    SlideSwitch slideSwitch = SlideSwitch.this;
                    slideSwitch.frontRect_left = slideSwitch.max_left;
                    SlideSwitch.this.isOpen = true;
                    if (SlideSwitch.this.listener != null) {
                        handler.sendEmptyMessage(1);
                    }
                    SlideSwitch slideSwitch2 = SlideSwitch.this;
                    slideSwitch2.frontRect_left_begin = slideSwitch2.max_left;
                    return;
                }
                while (SlideSwitch.this.frontRect_left >= SlideSwitch.this.min_left) {
                    SlideSwitch.this.alpha = (int) ((r0.frontRect_left * 255.0f) / SlideSwitch.this.max_left);
                    SlideSwitch.this.invalidateView();
                    SlideSwitch.access$120(SlideSwitch.this, 3);
                    try {
                        Thread.sleep(3L);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
                SlideSwitch.this.alpha = 0;
                SlideSwitch slideSwitch3 = SlideSwitch.this;
                slideSwitch3.frontRect_left = slideSwitch3.min_left;
                SlideSwitch.this.isOpen = false;
                if (SlideSwitch.this.listener != null) {
                    handler.sendEmptyMessage(0);
                }
                SlideSwitch slideSwitch4 = SlideSwitch.this;
                slideSwitch4.frontRect_left_begin = slideSwitch4.min_left;
            }
        }).start();
    }

    public void setState(boolean z) {
        this.isOpen = z;
        initDrawingVal();
        invalidateView();
        SlideListener slideListener = this.listener;
        if (slideListener != null) {
            if (z) {
                slideListener.open();
            } else {
                slideListener.close();
            }
        }
    }

    public void setShapeType(int i) {
        this.shape = i;
    }
}
