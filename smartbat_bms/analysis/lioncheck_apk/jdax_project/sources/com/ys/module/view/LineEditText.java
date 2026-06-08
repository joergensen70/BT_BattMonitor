package com.ys.module.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class LineEditText extends EditText {
    private Context context;
    private Paint mPaint;
    private int paintColor;
    private float paintSize;

    public LineEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.paintSize = 2.0f;
        this.context = context;
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int color = this.context.getResources().getColor(R.color.box_border_color);
        this.paintColor = color;
        this.mPaint.setColor(color);
        float dimension = this.context.getResources().getDimension(R.dimen.DIMEN_1PX);
        this.paintSize = dimension;
        this.mPaint.setStrokeWidth(dimension);
        canvas.drawLine(0.0f, getHeight() - 5, getWidth() - 1, getHeight() - 5, this.mPaint);
    }
}
