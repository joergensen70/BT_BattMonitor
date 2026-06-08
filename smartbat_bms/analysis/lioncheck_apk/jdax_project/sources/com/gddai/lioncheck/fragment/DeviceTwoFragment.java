package com.gddai.lioncheck.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gddai.lioncheck.MyApp;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.LinearChatActivity;
import com.gddai.lioncheck.fragment.base.BaseFragment;
import com.gddai.lioncheck.service.BluStaValue;
import com.gddai.lioncheck.service.BlueDataUtils;
import com.gddai.lioncheck.widget.RmcView;
import com.gddai.lioncheck.widget.TempView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/* JADX INFO: loaded from: classes.dex */
public class DeviceTwoFragment extends BaseFragment {

    @ViewInject(R.id.cell1)
    private ViewGroup cell1;
    private float currentV;
    public LocalBroadcastManager lbm;
    MyReceiver receiver;

    @ViewInject(R.id.rmc_view)
    private RmcView rmc_view;

    @ViewInject(R.id.socInfoTv)
    private TextView socInfoTv;

    @ViewInject(R.id.temp_view)
    private TempView temp_view;

    @ViewInject(R.id.tv_atte)
    private TextView tv_atte;

    @ViewInject(R.id.tv_attf)
    private TextView tv_attf;

    @ViewInject(R.id.tv_f)
    private TextView tv_f;

    @ViewInject(R.id.tv_number)
    private TextView tv_number;

    @ViewInject(R.id.tv_rmc)
    private TextView tv_rmc;

    @ViewInject(R.id.tv_temperature)
    private TextView tv_temperature;
    private float voltage;

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected int setLayoutId() {
        return R.layout.fragment_device_two;
    }

    @OnClick({R.id.rmc_rl})
    private void SocChat(View view) {
        showActivity(LinearChatActivity.class, BluStaValue.ACTION_SOC);
    }

    @OnClick({R.id.tv_temp})
    private void temp(View view) {
        showActivity(LinearChatActivity.class, BluStaValue.ACTION_TEMPERATURE);
    }

    @OnClick({R.id.threena})
    private void SocView(View view) {
        showActivity(LinearChatActivity.class, BluStaValue.ACTION_SOC);
    }

    @OnClick({R.id.socInfoTv})
    private void socInfo(View view) {
        if (BluStaValue.deviceConnctState) {
            BlueDataUtils.totalSum = 0L;
            BluStaValue.deviceDisconnState = true;
            MyApp.getmBluetoothLeService().disconnect();
        }
        getActivity().finish();
    }

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected void init() {
        this.lbm = LocalBroadcastManager.getInstance(this.mActivity);
        if (this.mActivity.screenHeight > 1920) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.cell1.getLayoutParams();
            layoutParams.width = 40;
            layoutParams.height = 0;
            this.cell1.setLayoutParams(layoutParams);
        }
        Typeface typefaceCreateFromAsset = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeueLTStd-BdEx.otf");
        this.tv_f.setTypeface(typefaceCreateFromAsset);
        this.tv_temperature.setTypeface(typefaceCreateFromAsset);
        this.tv_rmc.setTypeface(typefaceCreateFromAsset);
        this.tv_attf.setTypeface(typefaceCreateFromAsset);
        this.tv_atte.setTypeface(typefaceCreateFromAsset);
        this.tv_number.setTypeface(typefaceCreateFromAsset);
        initReceiver();
        this.tv_f.setText(Html.fromHtml("<font color=\"#00ff00\">0</font>℉", 0));
        this.tv_temperature.setText(Html.fromHtml("<font color=\"#00ff00\">0</font>℃", 0));
        this.tv_rmc.setText(Html.fromHtml("<font color=\"#00ff00\">0</font>AH", 0));
    }

    private void initReceiver() {
        this.receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluStaValue.ACTION_TEMPERATURE);
        intentFilter.addAction(BluStaValue.ACTION_RMC);
        intentFilter.addAction(BluStaValue.ACTION_DCAP);
        intentFilter.addAction(BluStaValue.ACTION_FCC);
        intentFilter.addAction(BluStaValue.ACTION_ATTE);
        intentFilter.addAction(BluStaValue.ACTION_ATTF);
        intentFilter.addAction(BluStaValue.ACTION_NUMBER);
        intentFilter.addAction(BluStaValue.ACTION_DATE);
        intentFilter.addAction(BluStaValue.ACTION_SOC);
        this.lbm.registerReceiver(this.receiver, intentFilter);
    }

    @Override // android.support.v4.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.lbm.unregisterReceiver(this.receiver);
    }

    class MyReceiver extends BroadcastReceiver {
        MyReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            switch (action) {
                case "com.yscoco.bluetooth.ACTION_SOC":
                    int longExtra = (int) intent.getLongExtra("value", 0L);
                    DeviceTwoFragment.this.rmc_view.setProgressValues((float) (((double) longExtra) / 100.0d));
                    DeviceTwoFragment.this.tv_rmc.setText(Html.fromHtml("<font color=\"#00ff00\">" + longExtra + "</font>%", 0));
                    break;
                case "com.yscoco.bluetooth.ACTION_ATTE":
                    if (intent.getLongExtra("value", 0L) != 65535) {
                        DeviceTwoFragment.this.tv_atte.setText(Html.fromHtml(intent.getLongExtra("value", 0L) + "<font color=\"#ffffff\">Min</font>"));
                        break;
                    } else {
                        DeviceTwoFragment.this.tv_atte.setText(Html.fromHtml("NA<font color=\"#ffffff\">Min</font>"));
                        break;
                    }
                    break;
                case "com.yscoco.bluetooth.ACTION_ATTF":
                    if (intent.getLongExtra("value", 0L) != 65535) {
                        DeviceTwoFragment.this.tv_attf.setText(Html.fromHtml(intent.getLongExtra("value", 0L) + "<font color=\"#ffffff\">Min</font>"));
                        break;
                    } else {
                        DeviceTwoFragment.this.tv_attf.setText(Html.fromHtml("NA<font color=\"#ffffff\">Min</font>"));
                        break;
                    }
                    break;
                case "com.yscoco.bluetooth.ACTION_DATE":
                    intent.getLongExtra("value", 0L);
                    break;
                case "com.yscoco.bluetooth.ACTION_NUMBER":
                    String str = intent.getLongExtra("value", 100L) + "";
                    for (int length = str.length(); length < 5; length++) {
                        str = "0" + str;
                    }
                    DeviceTwoFragment.this.tv_number.setText(str);
                    break;
                case "com.yscoco.bluetooth.ACTION_TEMPERATURE":
                    float longExtra2 = (float) ((intent.getLongExtra("value", 100L) - 2731) / 10.0d);
                    DeviceTwoFragment.this.tv_f.setText(Html.fromHtml("<font color=\"#00ff00\">" + (((int) (((((double) longExtra2) * 1.8d) + 32.0d) * 10.0d)) / 10.0f) + "</font>℉", 0));
                    DeviceTwoFragment.this.tv_temperature.setText(Html.fromHtml("<font color=\"#00ff00\">" + longExtra2 + "</font>℃", 0));
                    DeviceTwoFragment.this.temp_view.setProgressValues((float) (((double) (longExtra2 + 40.0f)) / 160.0d));
                    break;
            }
        }
    }
}
