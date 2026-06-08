package com.ys.module.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class AlertDialog {
    private Button btn_neg;
    private Button btn_pos;
    private Context context;
    private Dialog dialog;
    private Display display;
    private ImageView img_line;
    private LinearLayout lLayout_bg;
    private TextView txt_msg;
    private TextView txt_title;
    private boolean showTitle = false;
    private boolean showMsg = false;
    private boolean showPosBtn = false;
    private boolean showNegBtn = false;
    private Window window = null;

    public AlertDialog(Context context) {
        this.context = context;
        this.display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
    }

    public AlertDialog builder() {
        View viewInflate = LayoutInflater.from(this.context).inflate(R.layout.view_alertdialog, (ViewGroup) null);
        this.lLayout_bg = (LinearLayout) viewInflate.findViewById(R.id.lLayout_bg);
        TextView textView = (TextView) viewInflate.findViewById(R.id.txt_title);
        this.txt_title = textView;
        textView.setVisibility(8);
        TextView textView2 = (TextView) viewInflate.findViewById(R.id.txt_msg);
        this.txt_msg = textView2;
        textView2.setVisibility(8);
        Button button = (Button) viewInflate.findViewById(R.id.btn_neg);
        this.btn_neg = button;
        button.setVisibility(8);
        Button button2 = (Button) viewInflate.findViewById(R.id.btn_pos);
        this.btn_pos = button2;
        button2.setVisibility(8);
        ImageView imageView = (ImageView) viewInflate.findViewById(R.id.img_line);
        this.img_line = imageView;
        imageView.setVisibility(8);
        Dialog dialog = new Dialog(this.context, R.style.AlertDialogStyle);
        this.dialog = dialog;
        dialog.setContentView(viewInflate);
        this.lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (((double) this.display.getWidth()) * 0.85d), -2));
        Window window = this.dialog.getWindow();
        this.window = window;
        window.setWindowAnimations(R.style.ActionSheetDialogAnimationxx);
        return this;
    }

    public AlertDialog setTitle(String str) {
        this.showTitle = true;
        if ("".equals(str)) {
            this.txt_title.setText("标题");
            return this;
        }
        this.txt_title.setText(str);
        return this;
    }

    public AlertDialog setMsg(String str) {
        this.showMsg = true;
        if ("".equals(str)) {
            this.txt_msg.setText("内容");
            return this;
        }
        this.txt_msg.setText(str);
        return this;
    }

    public AlertDialog setCancelable(boolean z) {
        this.dialog.setCancelable(z);
        return this;
    }

    public AlertDialog setPositiveButton(int i, String str, final View.OnClickListener onClickListener) {
        this.showPosBtn = true;
        this.btn_pos.setTextColor(i);
        if ("".equals(str)) {
            this.btn_pos.setText(R.string.comfirm_text);
        } else {
            this.btn_pos.setText(str);
        }
        this.btn_pos.setOnClickListener(new View.OnClickListener() { // from class: com.ys.module.view.AlertDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                View.OnClickListener onClickListener2 = onClickListener;
                if (onClickListener2 != null) {
                    onClickListener2.onClick(view);
                }
                AlertDialog.this.dialog.dismiss();
            }
        });
        return this;
    }

    public AlertDialog setPositiveButton(String str, final View.OnClickListener onClickListener) {
        this.showPosBtn = true;
        if ("".equals(str) || str == null) {
            this.btn_pos.setText(R.string.comfirm_text);
        } else {
            this.btn_pos.setText(str);
        }
        this.btn_pos.setOnClickListener(new View.OnClickListener() { // from class: com.ys.module.view.AlertDialog.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                View.OnClickListener onClickListener2 = onClickListener;
                if (onClickListener2 != null) {
                    onClickListener2.onClick(view);
                }
                AlertDialog.this.dialog.dismiss();
            }
        });
        return this;
    }

    public AlertDialog setNegativeButton(int i, String str, final View.OnClickListener onClickListener) {
        this.showNegBtn = true;
        this.btn_neg.setTextColor(i);
        if ("".equals(str) || str == null) {
            this.btn_neg.setText(R.string.cancel_text);
        } else {
            this.btn_neg.setText(str);
        }
        this.btn_neg.setOnClickListener(new View.OnClickListener() { // from class: com.ys.module.view.AlertDialog.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                View.OnClickListener onClickListener2 = onClickListener;
                if (onClickListener2 != null) {
                    onClickListener2.onClick(view);
                }
                AlertDialog.this.dialog.dismiss();
            }
        });
        return this;
    }

    public AlertDialog setNegativeButton(String str, final View.OnClickListener onClickListener) {
        this.showNegBtn = true;
        if ("".equals(str) || str == null) {
            this.btn_neg.setText(R.string.cancel_text);
        } else {
            this.btn_neg.setText(str);
        }
        this.btn_neg.setOnClickListener(new View.OnClickListener() { // from class: com.ys.module.view.AlertDialog.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                View.OnClickListener onClickListener2 = onClickListener;
                if (onClickListener2 != null) {
                    onClickListener2.onClick(view);
                }
                AlertDialog.this.dialog.dismiss();
            }
        });
        return this;
    }

    private void setLayout() {
        if (!this.showTitle && !this.showMsg) {
            this.txt_title.setText("提示");
            this.txt_title.setVisibility(0);
        }
        if (this.showTitle) {
            this.txt_title.setVisibility(0);
        }
        if (this.showMsg) {
            this.txt_msg.setVisibility(0);
        }
        if (!this.showPosBtn && !this.showNegBtn) {
            this.btn_pos.setText("确定");
            this.btn_pos.setVisibility(0);
            this.btn_pos.setBackgroundResource(R.drawable.alertdialog_single_selector);
            this.btn_pos.setOnClickListener(new View.OnClickListener() { // from class: com.ys.module.view.AlertDialog.5
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    AlertDialog.this.dialog.dismiss();
                }
            });
        }
        if (this.showPosBtn && this.showNegBtn) {
            this.btn_pos.setVisibility(0);
            this.btn_pos.setBackgroundResource(R.drawable.alertdialog_right_selector);
            this.btn_neg.setVisibility(0);
            this.btn_neg.setBackgroundResource(R.drawable.alertdialog_left_selector);
            this.img_line.setVisibility(0);
        }
        if (this.showPosBtn && !this.showNegBtn) {
            this.btn_pos.setVisibility(0);
            this.btn_pos.setBackgroundResource(R.drawable.alertdialog_single_selector);
        }
        if (this.showPosBtn || !this.showNegBtn) {
            return;
        }
        this.btn_neg.setVisibility(0);
        this.btn_neg.setBackgroundResource(R.drawable.alertdialog_single_selector);
    }

    public void show() {
        setLayout();
        this.dialog.show();
    }
}
