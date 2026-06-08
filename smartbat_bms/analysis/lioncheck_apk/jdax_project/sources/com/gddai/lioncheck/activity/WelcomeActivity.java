package com.gddai.lioncheck.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.ys.module.utils.ActivityCollectorUtils;

/* JADX INFO: loaded from: classes.dex */
public class WelcomeActivity extends BaseActivity {
    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected int setLayoutId() {
        return R.layout.activity_welcome_two;
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init() {
        if (Build.VERSION.SDK_INT >= 31) {
            requestPermission(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"});
        } else {
            requestPermission(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"});
        }
    }

    private void start() {
        new Handler().postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.WelcomeActivity.1
            @Override // java.lang.Runnable
            public void run() {
                WelcomeActivity.this.showActivity(BlueDeviceListActivity.class);
                WelcomeActivity.this.finish();
            }
        }, 3000L);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    public void okPermissions() {
        super.okPermissions();
        if (isBluetoothAvailable() && checkBluetoothPermissions()) {
            start();
        } else {
            start();
        }
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    public void notPermissions() {
        super.notPermissions();
        ActivityCollectorUtils.finishAll();
        finish();
    }

    private boolean isBluetoothAvailable() {
        try {
            BluetoothAdapter adapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
            if (adapter != null) {
                return adapter.isEnabled();
            }
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    private boolean checkBluetoothPermissions() {
        return Build.VERSION.SDK_INT >= 31 ? ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_SCAN") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_CONNECT") == 0 : ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_ADMIN") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0;
    }
}
