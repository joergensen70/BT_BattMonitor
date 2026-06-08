package com.facebook.drawee.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes.dex */
public class ProgressBarDrawable extends Drawable {
    private final Paint mPaint = new Paint(1);
    private int mBackgroundColor = Integer.MIN_VALUE;
    private int mColor = -2147450625;
    private int mPadding = 10;
    private int mBarWidth = 20;
    private int mLevel = 0;
    private boolean mHideWhenZero = false;

    public void setColor(int i) {
        if (this.mColor != i) {
            this.mColor = i;
            invalidateSelf();
        }
    }

    public int getColor() {
        return this.mColor;
    }

    public void setBackgroundColor(int i) {
        if (this.mBackgroundColor != i) {
            this.mBackgroundColor = i;
            invalidateSelf();
        }
    }

    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public void setPadding(int i) {
        if (this.mPadding != i) {
            this.mPadding = i;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect rect) {
        int i = this.mPadding;
        rect.set(i, i, i, i);
        return this.mPadding != 0;
    }

    public void setBarWidth(int i) {
        if (this.mBarWidth != i) {
            this.mBarWidth = i;
            invalidateSelf();
        }
    }

    public int getBarWidth() {
        return this.mBarWidth;
    }

    public void setHideWhenZero(boolean z) {
        this.mHideWhenZero = z;
    }

    public boolean getHideWhenZero() {
        return this.mHideWhenZero;
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onLevelChange(int i) {
        this.mLevel = i;
        invalidateSelf();
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return DrawableUtils.getOpacityFromColor(this.mPaint.getColor());
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mHideWhenZero && this.mLevel == 0) {
            return;
        }
        drawBar(canvas, 10000, this.mBackgroundColor);
        drawBar(canvas, this.mLevel, this.mColor);
    }

    private void drawBar(Canvas canvas, int i, int i2) {
        Rect bounds = getBounds();
        int iWidth = ((bounds.width() - (this.mPadding * 2)) * i) / 10000;
        int i3 = bounds.left + this.mPadding;
        int i4 = (bounds.bottom - this.mPadding) - this.mBarWidth;
        this.mPaint.setColor(i2);
        canvas.drawRect(i3, i4, i3 + iWidth, i4 + this.mBarWidth, this.mPaint);
    }
}
