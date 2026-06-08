package com.gddai.lioncheck.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import com.gddai.lioncheck.R;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.ViewfinderView;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class CustomViewfinderView extends ViewfinderView {
    public int[] colors;
    public int laserLinePosition;
    public LinearGradient linearGradient;
    public float[] position;

    public CustomViewfinderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.laserLinePosition = 0;
        this.position = new float[]{0.0f, 0.5f, 1.0f};
        this.colors = new int[]{-16344602, -16344602, -16344602};
    }

    @Override // com.journeyapps.barcodescanner.ViewfinderView, android.view.View
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (this.framingRect == null || this.previewFramingRect == null) {
            return;
        }
        Rect rect = this.framingRect;
        Rect rect2 = this.previewFramingRect;
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        this.paint.setColor(getResources().getColor(R.color.title_color));
        canvas.drawRect(rect.left, rect.top, rect.left + 70, rect.top + 10, this.paint);
        canvas.drawRect(rect.left, rect.top, rect.left + 10, rect.top + 70, this.paint);
        canvas.drawRect(rect.right - 70, rect.top, rect.right, rect.top + 10, this.paint);
        canvas.drawRect(rect.right - 10, rect.top, rect.right, rect.top + 70, this.paint);
        canvas.drawRect(rect.left, rect.bottom - 10, rect.left + 70, rect.bottom, this.paint);
        canvas.drawRect(rect.left, rect.bottom - 70, rect.left + 10, rect.bottom, this.paint);
        canvas.drawRect(rect.right - 70, rect.bottom - 10, rect.right, rect.bottom, this.paint);
        canvas.drawRect(rect.right - 10, rect.bottom - 70, rect.right, rect.bottom, this.paint);
        this.paint.setColor(this.resultBitmap != null ? this.resultColor : this.maskColor);
        float f = width;
        canvas.drawRect(0.0f, 0.0f, f, rect.top, this.paint);
        canvas.drawRect(0.0f, rect.top, rect.left, rect.bottom + 1, this.paint);
        canvas.drawRect(rect.right + 1, rect.top, f, rect.bottom + 1, this.paint);
        canvas.drawRect(0.0f, rect.bottom + 1, f, height, this.paint);
        if (this.resultBitmap != null) {
            this.paint.setAlpha(160);
            canvas.drawBitmap(this.resultBitmap, (Rect) null, rect, this.paint);
            return;
        }
        int iHeight = rect.height() / 2;
        int i = rect.top;
        int i2 = this.laserLinePosition + 5;
        this.laserLinePosition = i2;
        if (i2 > rect.height()) {
            this.laserLinePosition = 0;
        }
        this.linearGradient = new LinearGradient(rect.left + 1, rect.top + this.laserLinePosition, rect.right - 1, rect.top + 10 + this.laserLinePosition, this.colors, this.position, Shader.TileMode.CLAMP);
        this.paint.setShader(this.linearGradient);
        canvas.drawRect(rect.left + 1, rect.top + this.laserLinePosition, rect.right - 1, rect.top + 10 + this.laserLinePosition, this.paint);
        this.paint.setShader(null);
        float fWidth = rect.width() / rect2.width();
        float fHeight = rect.height() / rect2.height();
        List<ResultPoint> list = this.possibleResultPoints;
        List<ResultPoint> list2 = this.lastPossibleResultPoints;
        int i3 = rect.left;
        int i4 = rect.top;
        if (list.isEmpty()) {
            this.lastPossibleResultPoints = null;
        } else {
            this.possibleResultPoints = new ArrayList(5);
            this.lastPossibleResultPoints = list;
            this.paint.setAlpha(160);
            this.paint.setColor(this.resultPointColor);
            for (ResultPoint resultPoint : list) {
                canvas.drawCircle(((int) (resultPoint.getX() * fWidth)) + i3, ((int) (resultPoint.getY() * fHeight)) + i4, 6.0f, this.paint);
            }
        }
        if (list2 != null) {
            this.paint.setAlpha(80);
            this.paint.setColor(this.resultPointColor);
            for (ResultPoint resultPoint2 : list2) {
                canvas.drawCircle(((int) (resultPoint2.getX() * fWidth)) + i3, ((int) (resultPoint2.getY() * fHeight)) + i4, 3.0f, this.paint);
            }
        }
        postInvalidateDelayed(16L, rect.left, rect.top, rect.right, rect.bottom);
    }
}
