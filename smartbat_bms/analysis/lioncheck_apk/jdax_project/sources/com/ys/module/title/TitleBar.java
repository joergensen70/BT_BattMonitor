package com.ys.module.title;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class TitleBar extends RelativeLayout {
    private Activity activity;
    private ImageView iv_title_left;
    private LinearLayout ll_title_left;
    private TextView mBackBtn;
    private Callback mCallback;
    private TextView mRightBtn;
    private ImageView mRightImage;
    private TextView mTitle;
    private ImageView mTitleImg;
    private int type;

    public interface Callback {
        void buttonClick(View view);
    }

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TitleBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.activity = (Activity) context;
        init();
    }

    private void init() {
        View viewInflate = View.inflate(getContext(), R.layout.title_bar, this);
        this.ll_title_left = (LinearLayout) viewInflate.findViewById(R.id.ll_title_left);
        this.mBackBtn = (TextView) viewInflate.findViewById(R.id.back_button);
        this.mRightBtn = (TextView) viewInflate.findViewById(R.id.right_btn);
        this.iv_title_left = (ImageView) viewInflate.findViewById(R.id.iv_title_left);
        this.mRightImage = (ImageView) viewInflate.findViewById(R.id.right_image);
        this.mTitle = (TextView) viewInflate.findViewById(R.id.title_name);
        this.mTitleImg = (ImageView) viewInflate.findViewById(R.id.title_img);
        this.ll_title_left.setOnClickListener(new View.OnClickListener() { // from class: com.ys.module.title.TitleBar.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                TitleBar.this.buttonClick(view);
            }
        });
    }

    public void setTitle(int i) {
        this.mTitle.setVisibility(0);
        this.mTitle.setText(i);
    }

    public void setTitle(String str) {
        this.mTitle.setVisibility(0);
        this.mTitle.setText(str);
    }

    public void setLeftBtnText(int i) {
        setLeftBtnText(i, false);
    }

    public void setLeftBtnText(int i, boolean z) {
        this.mBackBtn.setVisibility(0);
        if (z) {
            return;
        }
        this.mBackBtn.setText("");
    }

    public void setRightBtnText(int i) {
        this.mRightBtn.setVisibility(0);
        this.mRightBtn.setText(i);
    }

    public void setLeftImage(int i) {
        this.iv_title_left.setImageResource(i);
    }

    public void setRightImage(int i) {
        this.mRightImage.setVisibility(0);
        this.mRightImage.setImageResource(i);
    }

    public void buttonClick(View view) {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.buttonClick(view);
        } else {
            this.activity.finish();
        }
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setTitleBackground(int i) {
        this.mTitleImg.setVisibility(0);
        this.mTitleImg.setImageResource(i);
    }
}
