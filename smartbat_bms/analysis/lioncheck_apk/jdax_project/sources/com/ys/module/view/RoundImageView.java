package com.ys.module.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class RoundImageView extends ImageView {
    private static final int BODER_RADIUS_DEFAULT = 5;
    private static final String STATE_BORDER_RADIUS = "state_border_radius";
    private static final String STATE_INSTANCE = "state_instance";
    private static final String STATE_TYPE = "state_type";
    public static final int TYPE_CIRCLE = 0;
    public static final int TYPE_ROUND = 1;
    private Paint mBitmapPaint;
    private BitmapShader mBitmapShader;
    private int mBorderRadius;
    private Matrix mMatrix;
    private int mRadius;
    private RectF mRoundRect;
    private int mWidth;
    private int type;

    public RoundImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMatrix = new Matrix();
        Paint paint = new Paint();
        this.mBitmapPaint = paint;
        paint.setAntiAlias(true);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.RoundImageView);
        this.mBorderRadius = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius, (int) TypedValue.applyDimension(1, 5.0f, getResources().getDisplayMetrics()));
        this.type = typedArrayObtainStyledAttributes.getInt(R.styleable.RoundImageView_type, 1);
        typedArrayObtainStyledAttributes.recycle();
    }

    public RoundImageView(Context context) {
        this(context, null);
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.type == 0) {
            int iMin = Math.min(getMeasuredWidth(), getMeasuredHeight());
            this.mWidth = iMin;
            this.mRadius = iMin / 2;
            setMeasuredDimension(iMin, iMin);
        }
    }

    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        Bitmap bitmapDrawableToBitamp = drawableToBitamp(drawable);
        this.mBitmapShader = new BitmapShader(bitmapDrawableToBitamp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        int i = this.type;
        float fMax = 1.0f;
        if (i == 0) {
            fMax = (this.mWidth * 1.0f) / Math.min(bitmapDrawableToBitamp.getWidth(), bitmapDrawableToBitamp.getHeight());
        } else if (i == 1) {
            Log.e("TAG", "b'w = " + bitmapDrawableToBitamp.getWidth() + " , b'h = " + bitmapDrawableToBitamp.getHeight());
            if (bitmapDrawableToBitamp.getWidth() != getWidth() || bitmapDrawableToBitamp.getHeight() != getHeight()) {
                fMax = Math.max((getWidth() * 1.0f) / bitmapDrawableToBitamp.getWidth(), (getHeight() * 1.0f) / bitmapDrawableToBitamp.getHeight());
            }
        }
        this.mMatrix.setScale(fMax, fMax);
        this.mBitmapShader.setLocalMatrix(this.mMatrix);
        this.mBitmapPaint.setShader(this.mBitmapShader);
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        Log.e("TAG", "onDraw");
        if (getDrawable() == null) {
            return;
        }
        setUpShader();
        if (this.type == 1) {
            RectF rectF = this.mRoundRect;
            int i = this.mBorderRadius;
            canvas.drawRoundRect(rectF, i, i, this.mBitmapPaint);
        } else {
            int i2 = this.mRadius;
            canvas.drawCircle(i2, i2, i2, this.mBitmapPaint);
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (this.type == 1) {
            this.mRoundRect = new RectF(0.0f, 0.0f, i, i2);
        }
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        return bitmapCreateBitmap;
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putInt(STATE_TYPE, this.type);
        bundle.putInt(STATE_BORDER_RADIUS, this.mBorderRadius);
        return bundle;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
            this.type = bundle.getInt(STATE_TYPE);
            this.mBorderRadius = bundle.getInt(STATE_BORDER_RADIUS);
            return;
        }
        super.onRestoreInstanceState(parcelable);
    }

    public void setBorderRadius(int i) {
        int iDp2px = dp2px(i);
        if (this.mBorderRadius != iDp2px) {
            this.mBorderRadius = iDp2px;
            invalidate();
        }
    }

    public void setType(int i) {
        if (this.type != i) {
            this.type = i;
            if (i != 1 && i != 0) {
                this.type = 0;
            }
            requestLayout();
        }
    }

    public int dp2px(int i) {
        return (int) TypedValue.applyDimension(1, i, getResources().getDisplayMetrics());
    }
}
