package com.gddai.lioncheck.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.media.TransportMediator;
import android.view.View;
import com.gddai.lioncheck.R;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class LineChartView extends View {
    private Context context;
    private long endTime;
    float left;
    private Paint paint;
    private float rate;
    private float screenH;
    private float screenW;
    private long startTime;
    private int textSize;
    private float treeGapW;
    private float treeW;
    private String units;
    HashMap<Integer, Float> values;
    float x;
    private int[] xaxis;
    float y;
    private int[] yaxis;

    public LineChartView(Context context) {
        super(context);
        this.textSize = 10;
        this.rate = 0.925f;
        this.textSize = context.getResources().getDimensionPixelSize(R.dimen.text_size16);
    }

    public LineChartView(Context context, int[] iArr, int[] iArr2, HashMap<Integer, Float> map, String str) {
        this(context);
        this.context = context;
        Paint paint = new Paint();
        this.paint = paint;
        this.xaxis = iArr;
        this.yaxis = iArr2;
        this.values = map;
        this.units = str;
        paint.setAntiAlias(true);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.screenW = getWidth();
        float height = getHeight();
        this.screenH = height;
        float f = this.rate;
        this.y = height * f;
        float f2 = this.screenW;
        int[] iArr = this.xaxis;
        this.treeW = (((((f * 2.0f) - 1.0f) * f2) / iArr.length) * 9.0f) / 10.0f;
        this.treeGapW = ((((2.0f * f) - 1.0f) * f2) / iArr.length) / 10.0f;
        this.left = (1.0f - f) * f2;
        this.x = f2 * (1.0f - f);
        canvas.drawColor(Color.argb(0, 255, 255, 255));
        drawAxis(canvas);
        drawChart(canvas);
    }

    public void drawAxis(Canvas canvas) {
        this.paint.setColor(-1);
        this.paint.setStrokeWidth(4.0f);
        this.paint.setTypeface(Typeface.SERIF);
        this.paint.setTextSize(this.textSize);
        this.paint.setAntiAlias(true);
        float f = this.screenW;
        float f2 = this.rate;
        float f3 = this.screenH;
        canvas.drawLine((1.0f - f2) * f, f3 * f2, f, f3 * f2, this.paint);
        float f4 = this.screenW;
        float f5 = this.rate;
        float f6 = this.screenH;
        canvas.drawLine((1.0f - f5) * f4, f6 * f5, f4 * (1.0f - f5), (1.0f - f5) * f6, this.paint);
        this.paint.setStrokeWidth(2.0f);
        for (int i = 0; i < this.xaxis.length; i++) {
            this.paint.setColor(-1);
            canvas.drawText(this.xaxis[i] + "", this.x - (this.paint.measureText(this.xaxis[i] + "") / 2.0f), (this.screenH * this.rate) + this.textSize + 13.0f, this.paint);
            float f7 = this.x;
            float f8 = this.screenH;
            float f9 = this.rate;
            canvas.drawLine(f7, f8 * f9, f7, (f9 * f8) + 10.0f, this.paint);
            this.x += (this.screenW * ((this.rate * 2.0f) - 1.0f)) / this.xaxis.length;
        }
        for (int i2 = 0; i2 < this.yaxis.length; i2++) {
            this.paint.setColor(Color.argb(TransportMediator.KEYCODE_MEDIA_PAUSE, 255, 255, 255));
            canvas.drawText(this.yaxis[i2] + "", ((this.screenW * (1.0f - this.rate)) - this.paint.measureText(this.yaxis[i2] + "")) - 10.0f, this.y + (this.textSize / 2), this.paint);
            if (i2 != 0) {
                this.paint.setColor(Color.argb(TransportMediator.KEYCODE_MEDIA_PAUSE, 255, 255, 255));
                float f10 = this.screenW;
                float f11 = (1.0f - this.rate) * f10;
                float f12 = this.y;
                canvas.drawLine(f11, f12, f10, f12, this.paint);
            }
            this.y -= (this.screenH * ((this.rate * 2.0f) - 1.0f)) / this.yaxis.length;
        }
        this.paint.setColor(-1);
        float fMeasureText = this.paint.measureText(this.units + "");
        String str = this.units + "";
        float f13 = this.screenW;
        float f14 = this.rate;
        canvas.drawText(str, (f13 * (1.0f - f14)) - (fMeasureText / 2.0f), (this.screenH * (1.0f - f14)) - 5.0f, this.paint);
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x009e  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x00b3  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x00c5  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x00de  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void drawChart(android.graphics.Canvas r21) {
        /*
            Method dump skipped, instruction units count: 337
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gddai.lioncheck.widget.LineChartView.drawChart(android.graphics.Canvas):void");
    }

    public void addData(long j, float f) {
        this.endTime = j;
    }
}
