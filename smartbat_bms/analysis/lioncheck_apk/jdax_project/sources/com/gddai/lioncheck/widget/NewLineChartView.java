package com.gddai.lioncheck.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.media.TransportMediator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import com.gddai.lioncheck.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class NewLineChartView extends View {
    private Context context;
    private long endTime;
    private boolean isRefrsh;
    float left;
    private Paint paint;
    private float rate;
    private float screenH;
    private float screenW;
    private long startTime;
    private Paint textPaint;
    private int textSize;
    private List<Float> timeValue;
    private float treeGapW;
    private float treeW;
    private String units;
    float x;
    private int xRate;
    float y;
    private int[] yaxis;

    public NewLineChartView(Context context) {
        super(context);
        this.startTime = 0L;
        this.endTime = 20L;
        this.timeValue = new ArrayList();
        this.xRate = 20;
        this.textSize = 10;
        this.rate = 0.925f;
        this.isRefrsh = true;
        this.textSize = context.getResources().getDimensionPixelSize(R.dimen.text_size18);
    }

    public NewLineChartView(Context context, int[] iArr, String str) {
        this(context);
        this.context = context;
        this.paint = new Paint();
        Paint paint = new Paint();
        this.textPaint = paint;
        paint.setTextSize(52.0f);
        this.textPaint.setColor(Color.parseColor("#ffffff"));
        this.yaxis = iArr;
        this.units = str;
        this.paint.setAntiAlias(true);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.screenW = getWidth();
        float height = getHeight();
        this.screenH = height;
        float f = this.rate;
        this.y = height * f;
        float f2 = this.screenW;
        int i = this.xRate;
        this.treeW = (((((f * 2.0f) - 1.0f) * f2) / i) * 9.0f) / 10.0f;
        this.treeGapW = ((((2.0f * f) - 1.0f) * f2) / i) / 10.0f;
        this.left = (1.0f - f) * f2;
        this.x = f2 * (1.0f - f);
        canvas.drawColor(Color.argb(0, 255, 255, 255));
        drawAxis(canvas);
        this.isRefrsh = false;
        drawChart(canvas);
        this.isRefrsh = true;
    }

    public void drawAxis(Canvas canvas) {
        this.paint.setColor(-1);
        this.paint.setStrokeWidth(4.0f);
        this.paint.setTypeface(Typeface.DEFAULT);
        this.paint.setTextSize(this.textSize);
        this.paint.setAntiAlias(true);
        this.paint.setDither(true);
        float f = this.screenW;
        float f2 = this.rate;
        float f3 = this.screenH;
        canvas.drawLine((1.0f - f2) * f, f3 * f2, (f / 2.0f) - 30.0f, f2 * f3, this.paint);
        float f4 = this.screenW;
        float f5 = this.screenH;
        float f6 = this.rate;
        canvas.drawLine((f4 / 2.0f) + 30.0f, f5 * f6, f4 * f6, f5 * f6, this.paint);
        float f7 = this.screenW;
        float f8 = this.rate;
        float f9 = this.screenH;
        canvas.drawLine((1.0f - f8) * f7, f9 * f8, f7 * (1.0f - f8), (1.0f - f8) * f9, this.paint);
        this.paint.setStrokeWidth(2.0f);
        for (int i = 0; i < this.yaxis.length; i++) {
            this.paint.setColor(Color.argb(255, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION, 204, 204));
            canvas.drawText(this.yaxis[i] + "", (this.screenW / 2.0f) - (this.paint.measureText(this.yaxis[i] + "") / 2.0f), this.y + (this.textSize / 2), this.paint);
            if (i != 0) {
                this.paint.setColor(Color.argb(TransportMediator.KEYCODE_MEDIA_PAUSE, 255, 255, 255));
                float f10 = this.screenW;
                float f11 = (1.0f - this.rate) * f10;
                float f12 = this.y;
                canvas.drawLine(f11, f12, (f10 / 2.0f) - 30.0f, f12, this.paint);
                float f13 = this.screenW;
                float f14 = this.y;
                canvas.drawLine((f13 / 2.0f) + 30.0f, f14, this.rate * f13, f14, this.paint);
            }
            this.y -= (this.screenH * ((this.rate * 2.0f) - 1.0f)) / this.yaxis.length;
        }
        float fMeasureText = this.textPaint.measureText(this.units + "");
        String str = this.units + "";
        float f15 = this.screenW;
        float f16 = this.rate;
        canvas.drawText(str, (f15 * (1.0f - f16)) - (fMeasureText / 2.0f), (this.screenH * (1.0f - f16)) - 5.0f, this.textPaint);
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x00a5  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x009b A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void drawChart(android.graphics.Canvas r13) {
        /*
            Method dump skipped, instruction units count: 204
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gddai.lioncheck.widget.NewLineChartView.drawChart(android.graphics.Canvas):void");
    }

    public void addData(float f) {
        if (this.isRefrsh) {
            this.timeValue.add(Float.valueOf(f));
            invalidate();
        }
    }

    public String getUnits() {
        return this.units;
    }

    public void setUnits(String str) {
        this.units = str;
        if (this.isRefrsh) {
            invalidate();
        }
    }
}
