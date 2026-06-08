package com.gddai.lioncheck.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.media.TransportMediator;
import android.util.AttributeSet;
import android.view.View;
import com.gddai.lioncheck.R;

/* JADX INFO: loaded from: classes.dex */
public class PositionScaleView extends View {
    private int canvasH;
    private int canvasW;
    private boolean isBitmapLoading;
    private float mDgree;
    private Paint paint;
    private Bitmap pointBitmap;

    public PositionScaleView(Context context) {
        this(context, null);
    }

    public PositionScaleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PositionScaleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDgree = 0.0f;
        this.isBitmapLoading = false;
        init();
    }

    private void init() {
        Paint paint = new Paint();
        this.paint = paint;
        paint.setAntiAlias(true);
        this.paint.setFilterBitmap(true);
    }

    /* JADX WARN: Not initialized variable reg: 3, insn: 0x00a3: MOVE (r1 I:??[OBJECT, ARRAY]) = (r3 I:??[OBJECT, ARRAY]), block:B:55:0x00a3 */
    private synchronized void loadBitmap() {
        Bitmap bitmap;
        Bitmap bitmap2;
        Bitmap bitmapDecodeResource;
        if (!this.isBitmapLoading && ((bitmap = this.pointBitmap) == null || bitmap.isRecycled())) {
            this.isBitmapLoading = true;
            Bitmap bitmap3 = null;
            try {
                try {
                    try {
                        try {
                        } catch (OutOfMemoryError unused) {
                            bitmapDecodeResource = null;
                        } catch (Throwable th) {
                            th = th;
                            if (bitmap3 != null && !bitmap3.isRecycled()) {
                                try {
                                    bitmap3.recycle();
                                } catch (Exception unused2) {
                                }
                            }
                            this.isBitmapLoading = false;
                            throw th;
                        }
                    } catch (Exception unused3) {
                    }
                } catch (Exception unused4) {
                    bitmapDecodeResource = null;
                }
                if (getResources() == null) {
                    this.isBitmapLoading = false;
                    return;
                }
                try {
                    getResources().getResourceName(R.mipmap.pt);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inPurgeable = true;
                    options.inInputShareable = true;
                    options.inJustDecodeBounds = false;
                    bitmapDecodeResource = BitmapFactory.decodeResource(getResources(), R.mipmap.pt, options);
                    if (bitmapDecodeResource != null) {
                        try {
                            if (!bitmapDecodeResource.isRecycled()) {
                                int i = this.canvasW;
                                int iMax = 155;
                                int iMax2 = 44;
                                if (i > 0 && this.canvasH > 0) {
                                    iMax2 = Math.max(1, (i * 44) / 495);
                                    iMax = Math.max(1, (this.canvasH * 155) / 495);
                                }
                                this.pointBitmap = Bitmap.createScaledBitmap(bitmapDecodeResource, iMax2, iMax, true);
                            }
                        } catch (Exception unused5) {
                            this.pointBitmap = null;
                            if (bitmapDecodeResource != null) {
                                bitmapDecodeResource.recycle();
                            }
                            this.isBitmapLoading = false;
                        } catch (OutOfMemoryError unused6) {
                            System.gc();
                            this.pointBitmap = null;
                            if (bitmapDecodeResource != null && !bitmapDecodeResource.isRecycled()) {
                                bitmapDecodeResource.recycle();
                            }
                            this.isBitmapLoading = false;
                        }
                    }
                    if (bitmapDecodeResource != null && !bitmapDecodeResource.isRecycled()) {
                        bitmapDecodeResource.recycle();
                    }
                    this.isBitmapLoading = false;
                } catch (Exception unused7) {
                    this.isBitmapLoading = false;
                    return;
                }
                this.pointBitmap = null;
                if (bitmapDecodeResource != null && !bitmapDecodeResource.isRecycled()) {
                    bitmapDecodeResource.recycle();
                }
                this.isBitmapLoading = false;
            } catch (Throwable th2) {
                th = th2;
                bitmap3 = bitmap2;
            }
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }
        this.canvasW = getWidth();
        int height = getHeight();
        this.canvasH = height;
        if (this.pointBitmap == null && this.canvasW > 0 && height > 0) {
            loadBitmap();
        }
        Bitmap bitmap = this.pointBitmap;
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        canvasPoint(canvas);
    }

    private void canvasPoint(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        canvas.save();
        canvas.translate(this.canvasW / 2, this.canvasH / 2);
        canvas.rotate(this.mDgree);
        Bitmap bitmap = this.pointBitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            try {
                canvas.drawBitmap(this.pointBitmap, (-r0.getWidth()) / 2, ((-this.pointBitmap.getHeight()) * TransportMediator.KEYCODE_MEDIA_RECORD) / 155, this.paint);
            } catch (Exception unused) {
            }
        }
        canvas.restore();
    }

    public float getmDgree() {
        return this.mDgree;
    }

    public void setmDgree(float f) {
        this.mDgree = f;
        invalidate();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        Bitmap bitmap = this.pointBitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            try {
                this.pointBitmap.recycle();
            } catch (Exception unused) {
            }
            this.pointBitmap = null;
        }
        super.onDetachedFromWindow();
    }
}
