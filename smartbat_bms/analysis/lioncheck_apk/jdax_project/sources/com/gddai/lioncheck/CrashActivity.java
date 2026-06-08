package com.gddai.lioncheck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/* JADX INFO: loaded from: classes.dex */
public class CrashActivity extends Activity {
    TextView tv_ExceptionTrace;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(android.R.style.Theme.Black.NoTitleBar);
        initWidgets();
        Intent intent = getIntent();
        if (intent != null) {
            Throwable th = (Throwable) intent.getSerializableExtra("exception");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            th.printStackTrace(printWriter);
            this.tv_ExceptionTrace.setText(stringWriter.toString());
            try {
                stringWriter.close();
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initWidgets() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        Button button = new Button(this);
        button.setText("Close");
        button.setOnClickListener(new View.OnClickListener() { // from class: com.gddai.lioncheck.CrashActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Process.killProcess(Process.myPid());
            }
        });
        TextView textView = new TextView(this);
        this.tv_ExceptionTrace = textView;
        textView.setVerticalScrollBarEnabled(true);
        this.tv_ExceptionTrace.setMovementMethod(ScrollingMovementMethod.getInstance());
        linearLayout.addView(button, -1, -2);
        linearLayout.addView(this.tv_ExceptionTrace, -2, -1);
        setContentView(linearLayout, new ViewGroup.LayoutParams(-1, -1));
    }

    public void onClick(View view) {
        Process.killProcess(Process.myPid());
    }
}
