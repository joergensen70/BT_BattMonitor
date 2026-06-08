package com.gddai.lioncheck.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gddai.lioncheck.Constans;
import com.gddai.lioncheck.MyApp;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.service.BluStaValue;
import com.gddai.lioncheck.service.BlueDataUtils;
import com.gddai.lioncheck.widget.NewLineChartView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ys.module.view.AlertDialog;

/* JADX INFO: loaded from: classes.dex */
public class LinearChatActivity extends BaseActivity {

    @ViewInject(R.id.backtp)
    private ImageView backtp;

    @ViewInject(R.id.imtitle)
    private ImageView imtitle;
    private boolean isSend;
    private LocalBroadcastManager lbm;

    @ViewInject(R.id.ll_container)
    private LinearLayout ll_container;
    MyReceiver receiver;

    @ViewInject(R.id.titleTV)
    private TextView titleTV;

    @ViewInject(R.id.tv_value)
    private TextView tv_value;
    private String unit;
    NewLineChartView view;
    int[] yaxis;
    String action = "";
    byte[] sends = null;

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected int setLayoutId() {
        return R.layout.activity_line_chart;
    }

    @OnClick({R.id.backIM})
    private void back(View view) {
        returnDeal();
        if (BaseActivity.DI != null) {
            BaseActivity.DI.finish();
        }
    }

    private void returnDeal() {
        new AlertDialog(this).builder().setMsg(getResources().getString(R.string.mode_exit_text)).setTitle(getResources().getString(R.string.disconnect_text)).setPositiveButton(getResources().getString(R.string.sure_en), new View.OnClickListener() { // from class: com.gddai.lioncheck.activity.LinearChatActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BlueDataUtils.totalSum = 0L;
                BluStaValue.deviceDisconnState = true;
                MyApp.getmBluetoothLeService().disconnect();
                LinearChatActivity.this.finish();
            }
        }).setNegativeButton(getResources().getString(R.string.cancel_en), null).show();
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init() {
        if (getIntent().hasExtra("value")) {
            this.action = getIntent().getStringExtra("value");
        }
        this.titleTV.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Knockout-HTF30-JuniorWelterwt.otf"));
        this.tv_value.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTStd-BdEx.otf"));
        if (BluStaValue.timeDevice != null && BluStaValue.timeDevice.getName() != null) {
            this.titleTV.setText(BluStaValue.timeDevice.getName().replace("SmartBat-", Constans.BLE_NAME));
        }
        this.backtp.setOnClickListener(new View.OnClickListener() { // from class: com.gddai.lioncheck.activity.LinearChatActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                LinearChatActivity.this.finish();
            }
        });
        initReceiver();
        initData();
        this.view = new NewLineChartView(this, this.yaxis, this.unit);
        this.ll_container.removeAllViews();
        this.ll_container.addView(this.view);
    }

    private void initData() {
        String str = this.action;
        str.hashCode();
        switch (str) {
            case "com.yscoco.bluetooth.ACTION_SOC":
                this.imtitle.setImageResource(R.mipmap.threeline);
                this.yaxis = new int[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
                this.unit = "%";
                if (BluStaValue.deviceType == 1) {
                    this.sends = "+RAA0202".getBytes();
                    break;
                } else {
                    this.sends = "+R160D01".getBytes();
                    break;
                }
                break;
            case "com.yscoco.bluetooth.ACTION_VOLTAGE":
                this.imtitle.setImageResource(R.mipmap.twoline);
                this.yaxis = new int[]{0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20};
                this.unit = "V";
                if (BluStaValue.deviceType == 1) {
                    this.sends = "+RAA0802".getBytes();
                    break;
                } else {
                    this.sends = "+R160902".getBytes();
                    break;
                }
                break;
            case "com.yscoco.bluetooth.ACTION_CURRENT":
                this.imtitle.setImageResource(R.mipmap.oneline);
                this.yaxis = new int[]{-250, -200, -150, -100, -50, 0, 50, 100, 150, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION};
                this.unit = "A";
                if (BluStaValue.deviceType == 1) {
                    this.sends = "+RAA1002".getBytes();
                    break;
                } else {
                    this.sends = "+R160A03".getBytes();
                    break;
                }
                break;
            case "com.yscoco.bluetooth.ACTION_TEMPERATURE":
                this.yaxis = new int[]{-40, -30, -20, -10, 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};
                this.unit = "℃";
                if (BluStaValue.deviceType == 1) {
                    this.sends = "+RAA0C02".getBytes();
                    break;
                } else {
                    this.sends = "+R160802".getBytes();
                    break;
                }
                break;
        }
        this.isSend = true;
        initSend();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initSend() {
        if (this.isSend) {
            new Handler().postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.LinearChatActivity.3
                @Override // java.lang.Runnable
                public void run() {
                    LinearChatActivity.this.initSend();
                    MyApp.getmBluetoothLeService().writeValue(LinearChatActivity.this.sends);
                }
            }, 1000L);
        }
    }

    private void initReceiver() {
        this.lbm = LocalBroadcastManager.getInstance(this);
        this.receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        String str = this.action;
        if (str != null) {
            intentFilter.addAction(str);
        }
        this.lbm.registerReceiver(this.receiver, intentFilter);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.isSend = false;
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
                    LinearChatActivity.this.view.addData(intent.getLongExtra("value", 0L));
                    LinearChatActivity.this.tv_value.setText(intent.getLongExtra("value", 0L) + "%");
                    break;
                case "com.yscoco.bluetooth.ACTION_VOLTAGE":
                    LinearChatActivity.this.view.addData((float) (intent.getLongExtra("value", 0L) / 1000.0d));
                    LinearChatActivity.this.tv_value.setText(Html.fromHtml(((float) (intent.getLongExtra("value", 0L) / 1000.0d)) + "<font color='#ffffff'>V</font>"));
                    break;
                case "com.yscoco.bluetooth.ACTION_RSSI":
                    if (BluStaValue.timeDevice.getName() != null) {
                        BluStaValue.timeDevice.getName().replace("SmartBat", Constans.BLE_NAME);
                        break;
                    }
                    break;
                case "com.yscoco.bluetooth.ACTION_CURRENT":
                    float longExtra = (float) (intent.getLongExtra("value", 0L) / 1000.0d);
                    LinearChatActivity.this.view.addData(longExtra);
                    LinearChatActivity.this.tv_value.setText(Html.fromHtml(longExtra + "<font color='#ffffff'>A</font>"));
                    break;
                case "com.yscoco.bluetooth.ACTION_TEMPERATURE":
                    LinearChatActivity.this.view.addData((float) ((intent.getLongExtra("value", 100L) - 2731) / 10.0d));
                    LinearChatActivity.this.tv_value.setText(Html.fromHtml(((float) ((intent.getLongExtra("value", 100L) - 2731) / 10.0d)) + "<font color='#ffffff'>℃</font>"));
                    break;
            }
        }
    }
}
