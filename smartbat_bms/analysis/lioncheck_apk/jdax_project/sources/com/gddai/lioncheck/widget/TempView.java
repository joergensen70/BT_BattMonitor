package com.gddai.lioncheck.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.utils.BitmapUtils;

/* JADX INFO: loaded from: classes.dex */
public class TempView extends View {
    private Bitmap backBitmap;
    private int bgDrawableId;
    private int canvasH;
    private int canvasW;
    private boolean isClickable;
    private float progressValues;
    private Bitmap srcBitmap;
    private int srcDrawableId;

    public TempView(Context context) {
        this(context, null);
    }

    public TempView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TempView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.progressValues = 0.0f;
        this.isClickable = false;
        this.bgDrawableId = R.mipmap.tempe;
        this.srcDrawableId = R.mipmap.tempf;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvasW = getWidth();
        this.canvasH = getHeight();
        if (this.backBitmap == null || this.srcBitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmapDecodeResource = BitmapFactory.decodeResource(getResources(), this.bgDrawableId, options);
            Bitmap bitmapDecodeResource2 = BitmapFactory.decodeResource(getResources(), this.srcDrawableId, options);
            this.backBitmap = BitmapUtils.resizeBitmap(bitmapDecodeResource, this.canvasW, this.canvasH);
            this.srcBitmap = BitmapUtils.resizeBitmap(bitmapDecodeResource2, this.canvasW, this.canvasH);
            if (bitmapDecodeResource != null) {
                bitmapDecodeResource.recycle();
                System.gc();
            }
            if (bitmapDecodeResource2 != null) {
                bitmapDecodeResource2.recycle();
                System.gc();
            }
        }
        drawBg(canvas);
        drawSrc(canvas);
    }

    private void drawBg(Canvas canvas) {
        Bitmap bitmap = this.backBitmap;
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
    }

    private void drawSrc(Canvas canvas) {
        if (this.srcBitmap == null) {
            return;
        }
        canvas.save();
        int i = this.canvasH;
        canvas.clipRect(0.0f, ((i * 5) / 6) - (((this.progressValues * i) * 5.0f) / 6.0f), this.canvasW, i);
        canvas.drawBitmap(this.srcBitmap, 0.0f, 0.0f, (Paint) null);
        canvas.restore();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.isClickable) {
            setProgressValues((float) (((double) (((((int) motionEvent.getX()) * 5) / this.canvasW) + 1)) / 5.0d));
        }
        return true;
    }

    public void cancel() {
        Bitmap bitmap = this.backBitmap;
        if (bitmap != null) {
            bitmap.recycle();
            this.backBitmap = null;
        }
        Bitmap bitmap2 = this.srcBitmap;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.srcBitmap = null;
        }
        System.gc();
    }

    @Override // android.view.View
    public boolean isClickable() {
        return this.isClickable;
    }

    @Override // android.view.View
    public void setClickable(boolean z) {
        this.isClickable = z;
    }

    public float getProgressValues() {
        return this.progressValues;
    }

    public void setProgressValues(float f) {
        this.progressValues = f;
        invalidate();
    }

    public int getBgDrawableId() {
        return this.bgDrawableId;
    }

    public void setBgDrawableId(int i) {
        this.bgDrawableId = i;
    }

    public int getSrcDrawableId() {
        return this.srcDrawableId;
    }

    public void setSrcDrawableId(int i) {
        this.srcDrawableId = i;
        this.srcBitmap = null;
        invalidate();
    }
}
