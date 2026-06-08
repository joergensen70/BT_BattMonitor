package com.ys.module.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import com.ys.module.R;
import com.ys.module.utils.StringUtils;

/* JADX INFO: loaded from: classes.dex */
public class LoadingDialog extends Dialog {
    private TextView mProgressText;

    public LoadingDialog(Context context, int i) {
        super(context, i);
        init();
    }

    public LoadingDialog(Context context) {
        super(context, R.style.MyAlertDialog);
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_loading);
        this.mProgressText = (TextView) findViewById(R.id.progress_text);
    }

    public void show(String str) {
        StringUtils.isEmpty(str);
        this.mProgressText.setText(str);
        show();
    }
}
