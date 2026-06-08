package com.ys.module.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import com.ys.module.R;

/* JADX INFO: loaded from: classes.dex */
public class EditTextWithDel extends EditText {
    private static final String TAG = "EditTextWithDel";
    private Drawable imSearch;
    private Drawable imgAble;
    private Drawable imgInable;
    private Context mContext;

    public EditTextWithDel(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        init();
    }

    private void init() {
        this.imgInable = this.mContext.getResources().getDrawable(R.drawable.delete_gray);
        this.imgAble = this.mContext.getResources().getDrawable(R.drawable.delete);
        this.imSearch = this.mContext.getResources().getDrawable(R.drawable.search);
        addTextChangedListener(new TextWatcher() { // from class: com.ys.module.view.EditTextWithDel.1
            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                EditTextWithDel.this.setDrawable();
            }
        });
        setDrawable();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDrawable() {
        if (length() < 1) {
            setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, this.imgInable, (Drawable) null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, this.imgAble, (Drawable) null);
        }
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.imgAble != null && motionEvent.getAction() == 1) {
            int rawX = (int) motionEvent.getRawX();
            int rawY = (int) motionEvent.getRawY();
            Log.e(TAG, "eventX = " + rawX + "; eventY = " + rawY);
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - 50;
            if (rect.contains(rawX, rawY)) {
                setText("");
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
}
