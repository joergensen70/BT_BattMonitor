package com.ys.module.utils;

import android.os.CountDownTimer;
import android.widget.Button;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class MyCountTimer extends CountDownTimer {
    public static final int TIME_COUNT = 60000;
    private Button btn;
    private int endStrRid;
    private int normalColor;
    private int timingColor;

    public MyCountTimer(long j, long j2, Button button, int i) {
        super(j, j2);
        this.btn = button;
        this.endStrRid = i;
    }

    public MyCountTimer(Button button, int i) {
        super(60000L, 1000L);
        this.btn = button;
        this.endStrRid = i;
    }

    public MyCountTimer(Button button) {
        super(60000L, 1000L);
        this.btn = button;
        this.endStrRid = R.string.get_code_text;
    }

    public MyCountTimer(Button button, int i, int i2) {
        this(button);
        this.normalColor = i;
        this.timingColor = i2;
    }

    @Override // android.os.CountDownTimer
    public void onFinish() {
        int i = this.normalColor;
        if (i > 0) {
            this.btn.setTextColor(i);
        }
        this.btn.setText(this.endStrRid);
        this.btn.setEnabled(true);
    }

    @Override // android.os.CountDownTimer
    public void onTick(long j) {
        int i = this.timingColor;
        if (i > 0) {
            this.btn.setTextColor(i);
        }
        this.btn.setEnabled(false);
        this.btn.setText((j / 1000) + "秒");
    }
}
