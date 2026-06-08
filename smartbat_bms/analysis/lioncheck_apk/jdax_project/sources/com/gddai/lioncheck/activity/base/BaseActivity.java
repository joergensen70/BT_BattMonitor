package com.gddai.lioncheck.activity.base;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.DeviceInfoActivity;
import com.gddai.lioncheck.http.OkHttp;
import com.lidroid.xutils.ViewUtils;
import com.ys.module.toast.ToastTool;
import com.ys.module.utils.ActivityCollectorUtils;
import com.ys.module.utils.StatusBarUtil;
import java.io.Serializable;
import java.lang.reflect.Field;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseActivity extends FragmentActivity {
    public static DeviceInfoActivity DI;
    public static Typeface tfaceHnst;
    public static Typeface tfaceHnst2;
    private long mExitTime;
    private OkHttp mHttp;
    public int screenHeight;
    public int screenWidth;

    protected abstract void init();

    protected void init(Bundle bundle) {
    }

    public void notPermissions() {
    }

    public void okPermissions() {
    }

    protected abstract int setLayoutId();

    @Override // android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        getWindow().setSoftInputMode(34);
        setRequestedOrientation(1);
        super.onCreate(bundle);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.title_color), 20);
        ActivityCollectorUtils.addActivity(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.screenWidth = displayMetrics.widthPixels;
        this.screenHeight = displayMetrics.heightPixels;
        tfaceHnst = Typeface.createFromAsset(getResources().getAssets(), "fonts/HelveticaNeueLTStd-Cn.otf");
        tfaceHnst2 = Typeface.createFromAsset(getResources().getAssets(), "fonts/HelveticaNeueLTStd-HvEx.otf");
        try {
            Field declaredField = Typeface.class.getDeclaredField("SERIF");
            declaredField.setAccessible(true);
            declaredField.set(null, tfaceHnst);
            Field declaredField2 = Typeface.class.getDeclaredField("SANS_SERIF");
            declaredField2.setAccessible(true);
            declaredField2.set(null, tfaceHnst2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        }
        setContentView(setLayoutId());
        ViewUtils.inject(this);
        init();
        init(bundle);
    }

    public void showActivity(Class<?> cls) {
        showActivity(cls, (String) null);
    }

    public void showActivity(Class<?> cls, int i) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("value", i);
        startActivity(intent);
    }

    public void showActivity(Class<?> cls, String str) {
        Intent intent = new Intent(this, cls);
        if (str != null) {
            intent.putExtra("value", str);
        }
        startActivity(intent);
    }

    protected boolean isExit(int i) {
        if (i != 4) {
            return false;
        }
        if (System.currentTimeMillis() - this.mExitTime > 2000) {
            ToastTool.showNormalShort(this, "Again according to exit the APP !");
            this.mExitTime = System.currentTimeMillis();
            return true;
        }
        ActivityCollectorUtils.finishAll();
        return true;
    }

    public OkHttp getHttp() {
        if (this.mHttp == null) {
            this.mHttp = new OkHttp(this);
        }
        return this.mHttp;
    }

    public void showActivity(Class<?> cls, Serializable serializable) {
        Intent intent = new Intent(this, cls);
        if (serializable != null) {
            intent.putExtra("value", serializable);
        }
        startActivity(intent);
    }

    public Serializable getValue() {
        return getIntent().getSerializableExtra("value");
    }

    public void showActivityForResult(Class<?> cls, int i) {
        startActivityForResult(new Intent(this, cls), i);
    }

    public void showActivitySetResult(int i, String str) {
        Intent intent = new Intent();
        intent.putExtra(str + "", str);
        setResult(i, intent);
        finish();
    }

    public void showActivitySetResult(Class<?> cls, int i, Serializable serializable) {
        Intent intent = new Intent(this, cls);
        if (serializable != null) {
            intent.putExtra("value", serializable);
        }
        startActivityForResult(intent, i);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    public void requestPermission(String[] strArr) {
        for (String str : strArr) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                ActivityCompat.requestPermissions(this, strArr, 1);
                return;
            }
        }
        okPermissions();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 1) {
            return;
        }
        for (int i2 : iArr) {
            if (i2 != 0) {
                notPermissions();
                return;
            }
        }
        if (isFinishing()) {
            return;
        }
        okPermissions();
    }
}
