package com.gddai.lioncheck.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.View;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.ys.module.title.TitleBar;

/* JADX INFO: loaded from: classes.dex */
public class CustomScanActivity extends BaseActivity implements DecoratedBarcodeView.TorchListener {
    private CaptureManager captureManager;

    @ViewInject(R.id.dbv_custom)
    private DecoratedBarcodeView mDBV;

    @ViewInject(R.id.tb_title)
    private TitleBar titleBar;

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init() {
    }

    @Override // com.journeyapps.barcodescanner.DecoratedBarcodeView.TorchListener
    public void onTorchOff() {
    }

    @Override // com.journeyapps.barcodescanner.DecoratedBarcodeView.TorchListener
    public void onTorchOn() {
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected int setLayoutId() {
        return R.layout.activity_custom_scan;
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init(Bundle bundle) {
        this.mDBV.setTorchListener(this);
        CaptureManager captureManager = new CaptureManager(this, this.mDBV);
        this.captureManager = captureManager;
        captureManager.initializeFromIntent(getIntent(), bundle);
        this.captureManager.decode();
        this.titleBar.setTitle(R.string.scan_text);
        this.titleBar.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        this.titleBar.setOnClickListener(new View.OnClickListener() { // from class: com.gddai.lioncheck.activity.CustomScanActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CustomScanActivity.this.finish();
            }
        });
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        this.captureManager.onPause();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        this.captureManager.onResume();
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.captureManager.onDestroy();
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle, PersistableBundle persistableBundle) {
        super.onSaveInstanceState(bundle, persistableBundle);
        this.captureManager.onSaveInstanceState(bundle);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return this.mDBV.onKeyDown(i, keyEvent) || super.onKeyDown(i, keyEvent);
    }
}
