package com.gddai.lioncheck.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gddai.lioncheck.Constans;
import com.gddai.lioncheck.MyApp;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.adapter.PageAdapter;
import com.gddai.lioncheck.fragment.DeviceOneFragment;
import com.gddai.lioncheck.fragment.DeviceTwoFragment;
import com.gddai.lioncheck.fragment.DevicefourFragment;
import com.gddai.lioncheck.service.BluStaValue;
import com.gddai.lioncheck.service.BlueDataUtils;
import com.gddai.lioncheck.service.BluetoothLeService;
import com.gddai.lioncheck.utils.EncryptUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ys.module.dialog.LoadingDialog;
import com.ys.module.toast.ToastTool;
import com.ys.module.view.AlertDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* JADX INFO: loaded from: classes.dex */
public class DeviceInfoActivity extends BaseActivity {

    @ViewInject(R.id.backIM)
    private ImageView backIM;
    private int cursorWidth;
    List<Fragment> fragments;
    boolean isJumped;

    @ViewInject(R.id.iv_limit)
    private ImageView iv_limit;
    private LocalBroadcastManager lbm;
    private LoadingDialog mLoadingDialog;

    @ViewInject(R.id.mianL)
    private LinearLayout mianL;
    MyReceiver receiver;

    @ViewInject(R.id.rightIM)
    private ImageView rightIM;

    @ViewInject(R.id.rightIM)
    private ImageView rightIm;
    Timer runTimer;
    List<byte[]> sendValueByte;
    TimerTask task;

    @ViewInject(R.id.titleTV)
    private TextView titleTV;

    @ViewInject(R.id.tvbg)
    private ImageView tvbg;

    @ViewInject(R.id.vp)
    private ViewPager vp;

    @ViewInject(R.id.weblink)
    private ImageView weblink;
    int cycleNumber = 0;
    int curretSend = 0;
    private boolean isRefrsh = false;
    private Animation animation = null;
    private int originalIndex = 0;
    private int curretPage = 0;
    public boolean isPageThressOne = false;
    private Handler handler = new Handler() { // from class: com.gddai.lioncheck.activity.DeviceInfoActivity.3
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            LogUtils.e(message.what + "msg.what");
            if (message.what == DeviceInfoActivity.this.curretSend - 1) {
                if (DeviceInfoActivity.this.cycleNumber <= 1) {
                    DeviceInfoActivity.this.cycleNumber++;
                    DeviceInfoActivity.this.curretSend = message.what;
                    DeviceInfoActivity.this.isRefrsh = false;
                    DeviceInfoActivity.this.initSend();
                    return;
                }
                DeviceInfoActivity.this.cycleNumber = 0;
                DeviceInfoActivity.this.isRefrsh = false;
                DeviceInfoActivity.this.initSend();
            }
        }
    };

    public void initCursor(int i) {
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected int setLayoutId() {
        return R.layout.activity_device_info;
    }

    @OnClick({R.id.weblink})
    private void showWeb(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("https://www.panther-batterien.de/"));
        intent.setAction("android.intent.action.VIEW");
        startActivity(intent);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init() {
        initClick();
        this.mLoadingDialog = new LoadingDialog(this);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.isRefrsh = false;
        this.isJumped = false;
        initReceiver();
        initData();
        initValue();
        initSend();
        BaseActivity.DI = this;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
    }

    private void initData() {
        this.titleTV.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Knockout-HTF30-JuniorWelterwt.otf"));
        if (BluStaValue.timeDevice != null && BluStaValue.timeDevice.getName() != null) {
            if (BluStaValue.timeDevice.getName().toLowerCase().contains("smartbat-a")) {
                String name = BluStaValue.timeDevice.getName();
                EncryptUtils.getType(name.substring(name.length() - 6, name.length()));
                BluStaValue.deviceType = 1;
            } else {
                if (BluStaValue.timeDevice.getName().toLowerCase().contains("smartbat-b")) {
                    String name2 = BluStaValue.timeDevice.getName();
                    EncryptUtils.getType(name2.substring(name2.length() - 6, name2.length()));
                } else {
                    EncryptUtils.resouce = 0;
                }
                BluStaValue.deviceType = 2;
            }
            this.titleTV.setText(BluStaValue.timeDevice.getName().replace("SmartBat-", Constans.BLE_NAME));
        } else {
            BluStaValue.deviceType = 2;
        }
        ArrayList arrayList = new ArrayList();
        this.fragments = arrayList;
        arrayList.add(new DeviceOneFragment());
        this.fragments.add(new DeviceTwoFragment());
        this.fragments.add(new DevicefourFragment());
        initCursor(this.fragments.size());
        this.vp.setAdapter(new PageAdapter(getSupportFragmentManager(), this.fragments));
        this.vp.setCurrentItem(0);
    }

    private void initValue() {
        BluStaValue.sendValueByte1.clear();
        BluStaValue.sendValueByte2.clear();
        BluStaValue.sendValueByte3.clear();
        if (BluStaValue.deviceType == 1) {
            BluStaValue.sendValueByte1.add("+RAA0202".getBytes());
            BluStaValue.sendValueByte1.add("+RAA0A03".getBytes());
            BluStaValue.sendValueByte1.add("+RAA0802".getBytes());
            BluStaValue.sendValueByte1.add("+RAA2C02".getBytes());
            BluStaValue.sendValueByte1.add("+RAA1002".getBytes());
            BluStaValue.sendValueByte2.add("+RAA1002".getBytes());
            BluStaValue.sendValueByte2.add("+RAA0A03".getBytes());
            BluStaValue.sendValueByte2.add("+RAA0802".getBytes());
            BluStaValue.sendValueByte2.add("+RAA0C02".getBytes());
            BluStaValue.sendValueByte2.add("+RAA0403".getBytes());
            BluStaValue.sendValueByte2.add("+RAA3C03".getBytes());
            BluStaValue.sendValueByte2.add("+RAA0603".getBytes());
            BluStaValue.sendValueByte2.add("+RAA1802".getBytes());
            BluStaValue.sendValueByte2.add("+RAA1A02".getBytes());
            BluStaValue.sendValueByte2.add("+RAA2802".getBytes());
            BluStaValue.sendValueByte2.add("+RAA4802".getBytes());
            BluStaValue.sendValueByte2.add("+RAA0202".getBytes());
        } else {
            BluStaValue.sendValueByte1.add("+R160D01".getBytes());
            BluStaValue.sendValueByte1.add("+R160A03".getBytes());
            BluStaValue.sendValueByte1.add("+R160902".getBytes());
            BluStaValue.sendValueByte1.add("+R161702".getBytes());
            BluStaValue.sendValueByte2.add("+R160A03".getBytes());
            BluStaValue.sendValueByte2.add("+R160902".getBytes());
            BluStaValue.sendValueByte2.add("+R160802".getBytes());
            BluStaValue.sendValueByte2.add("+R160F03".getBytes());
            BluStaValue.sendValueByte2.add("+R161803".getBytes());
            BluStaValue.sendValueByte2.add("+R161003".getBytes());
            BluStaValue.sendValueByte2.add("+R161202".getBytes());
            BluStaValue.sendValueByte2.add("+R161302".getBytes());
            BluStaValue.sendValueByte2.add("+R161C02".getBytes());
            BluStaValue.sendValueByte2.add("+R161B02".getBytes());
            BluStaValue.sendValueByte2.add("+R160D01".getBytes());
            BluStaValue.sendValueByte3.add("+R160D01".getBytes());
            BluStaValue.sendValueByte3.add("+R160902".getBytes());
            BluStaValue.sendValueByte3.add("+R163F02".getBytes());
            BluStaValue.sendValueByte3.add("+R163E02".getBytes());
            BluStaValue.sendValueByte3.add("+R163D02".getBytes());
            BluStaValue.sendValueByte3.add("+R163C02".getBytes());
            BluStaValue.sendValueByte3.add("+R163B02".getBytes());
            BluStaValue.sendValueByte3.add("+R163A02".getBytes());
            BluStaValue.sendValueByte3.add("+R163902".getBytes());
            BluStaValue.sendValueByte3.add("+R163802".getBytes());
            BluStaValue.sendValueByte3.add("+R163702".getBytes());
            BluStaValue.sendValueByte3.add("+R163602".getBytes());
            BluStaValue.sendValueByte3.add("+R163502".getBytes());
            BluStaValue.sendValueByte3.add("+R163402".getBytes());
            BluStaValue.sendValueByte3.add("+R163302".getBytes());
            BluStaValue.sendValueByte3.add("+R163202".getBytes());
            BluStaValue.sendValueByte3.add("+R163102".getBytes());
        }
        this.sendValueByte = BluStaValue.sendValueByte1;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        this.isRefrsh = true;
        this.lbm.unregisterReceiver(this.receiver);
    }

    private void initClick() {
        this.backIM.setOnClickListener(new View.OnClickListener() { // from class: com.gddai.lioncheck.activity.DeviceInfoActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DeviceInfoActivity.this.returnDeal();
            }
        });
        this.vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() { // from class: com.gddai.lioncheck.activity.DeviceInfoActivity.2
            @Override // android.support.v4.view.ViewPager.OnPageChangeListener
            public void onPageScrolled(int i, float f, int i2) {
            }

            @Override // android.support.v4.view.ViewPager.OnPageChangeListener
            public void onPageSelected(int i) {
                DeviceInfoActivity.this.animation = new TranslateAnimation(DeviceInfoActivity.this.originalIndex * DeviceInfoActivity.this.cursorWidth, DeviceInfoActivity.this.cursorWidth * i, 0.0f, 0.0f);
                DeviceInfoActivity.this.animation.setFillAfter(true);
                DeviceInfoActivity.this.animation.setDuration(300L);
                DeviceInfoActivity.this.iv_limit.startAnimation(DeviceInfoActivity.this.animation);
                DeviceInfoActivity.this.originalIndex = i;
                DeviceInfoActivity.this.titleTV.setVisibility(0);
                DeviceInfoActivity.this.tvbg.setVisibility(0);
                DeviceInfoActivity.this.rightIM.setVisibility(0);
                DeviceInfoActivity.this.weblink.setVisibility(4);
                if (i == 0) {
                    DeviceInfoActivity.this.isPageThressOne = false;
                    DeviceInfoActivity.this.iv_limit.setImageResource(R.mipmap.tpone);
                    DeviceInfoActivity.this.sendValueByte = BluStaValue.sendValueByte1;
                    DeviceInfoActivity.this.mianL.setBackgroundColor(Color.parseColor("#000000"));
                    if (BluStaValue.timeDevice == null) {
                        DeviceInfoActivity.this.titleTV.setText(Constans.BLE_NAME);
                        return;
                    } else {
                        DeviceInfoActivity.this.titleTV.setText(BluStaValue.timeDevice.getName().replace("SmartBat-", Constans.BLE_NAME));
                        return;
                    }
                }
                if (i != 1) {
                    if (i != 2) {
                        return;
                    }
                    DeviceInfoActivity.this.iv_limit.setImageResource(R.mipmap.tpthree);
                    DeviceInfoActivity.this.mianL.setBackgroundResource(R.mipmap.about);
                    DeviceInfoActivity.this.weblink.setVisibility(0);
                    DeviceInfoActivity.this.titleTV.setVisibility(4);
                    DeviceInfoActivity.this.tvbg.setVisibility(4);
                    DeviceInfoActivity.this.rightIM.setVisibility(4);
                    return;
                }
                DeviceInfoActivity.this.isPageThressOne = false;
                DeviceInfoActivity.this.iv_limit.setImageResource(R.mipmap.tptwo);
                DeviceInfoActivity.this.sendValueByte = BluStaValue.sendValueByte2;
                DeviceInfoActivity.this.mianL.setBackgroundColor(Color.parseColor("#000000"));
                if (BluStaValue.timeDevice == null) {
                    DeviceInfoActivity.this.titleTV.setText(Constans.BLE_NAME);
                } else {
                    DeviceInfoActivity.this.titleTV.setText(BluStaValue.timeDevice.getName().replace("SmartBat-", Constans.BLE_NAME));
                }
            }

            @Override // android.support.v4.view.ViewPager.OnPageChangeListener
            public void onPageScrollStateChanged(int i) {
                LogUtils.e("state" + i);
            }
        });
    }

    private void initReceiver() {
        this.lbm = LocalBroadcastManager.getInstance(this);
        this.receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluStaValue.ACTION_CONNECT);
        intentFilter.addAction(BluStaValue.ACTION_DISCONNECT);
        intentFilter.addAction(BluStaValue.ACTION_SINGLE_VOLTAGE);
        intentFilter.addAction(BluStaValue.ACTION_RECEIVER_VALUE);
        intentFilter.addAction(BluStaValue.ACTION_IMPROPER_DISCONNECT);
        this.lbm.registerReceiver(this.receiver, intentFilter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void initSend() {
        if (BluStaValue.timeDevice == null) {
            return;
        }
        if (this.isRefrsh) {
            return;
        }
        this.isRefrsh = true;
        if (this.curretSend >= this.sendValueByte.size()) {
            this.curretSend = 0;
        }
        List<byte[]> list = this.sendValueByte;
        if (list != null && list.size() > 0) {
            BluetoothLeService bluetoothLeService = MyApp.getmBluetoothLeService();
            List<byte[]> list2 = this.sendValueByte;
            int i = this.curretSend;
            this.curretSend = i + 1;
            bluetoothLeService.writeValue(list2.get(i));
        }
        LogUtils.e(this.curretSend + "curretSend");
        this.handler.sendEmptyMessageDelayed(this.curretSend, 150L);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void returnDeal() {
        new AlertDialog(this).builder().setMsg(getResources().getString(R.string.mode_exit_text)).setTitle(getResources().getString(R.string.disconnect_text)).setPositiveButton(getResources().getString(R.string.sure_en), new View.OnClickListener() { // from class: com.gddai.lioncheck.activity.DeviceInfoActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BlueDataUtils.totalSum = 0L;
                BluStaValue.deviceDisconnState = true;
                MyApp.getmBluetoothLeService().disconnect();
                DeviceInfoActivity.this.finish();
            }
        }).setNegativeButton(getResources().getString(R.string.cancel_en), null).show();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            isExit(i);
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    class MyReceiver extends BroadcastReceiver {
        MyReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            switch (action) {
                case "com.yscoco.bluetooth.ACTION_RSSI":
                    if (BluStaValue.timeDevice.getName() == null) {
                        return;
                    }
                    DeviceInfoActivity.this.titleTV.setText(BluStaValue.timeDevice.getName().replace("SmartBat", Constans.BLE_NAME));
                    return;
                case "com.yscoco.bluetooth.ACTION_CONNECT":
                    DeviceInfoActivity.this.mLoadingDialog.dismiss();
                    MyApp.getmBluetoothLeService().writeValue(DeviceInfoActivity.this.sendValueByte.get(0));
                    return;
                case "com.yscoco.bluetooth.ACTION_RECEIVER_VALUE":
                    if (intent.hasExtra("value")) {
                        String stringExtra = intent.getStringExtra("value");
                        String strSubstring = stringExtra.substring(4, 6);
                        LogUtils.e("接收到的数据是" + stringExtra);
                        if (DeviceInfoActivity.this.isPageThressOne) {
                            DeviceInfoActivity.this.curretSend = 2;
                            DeviceInfoActivity.this.cycleNumber = 0;
                            DeviceInfoActivity.this.isRefrsh = false;
                            DeviceInfoActivity.this.initSend();
                            return;
                        }
                        synchronized (DeviceInfoActivity.this) {
                            if ((DeviceInfoActivity.this.curretSend >= 0 && DeviceInfoActivity.this.curretSend <= DeviceInfoActivity.this.sendValueByte.size() && new String(DeviceInfoActivity.this.sendValueByte.get(DeviceInfoActivity.this.curretSend - 1)).toLowerCase().contains(strSubstring.toLowerCase())) || stringExtra.toLowerCase().contains("error".toLowerCase())) {
                                DeviceInfoActivity.this.cycleNumber = 0;
                                DeviceInfoActivity.this.isRefrsh = false;
                                DeviceInfoActivity.this.initSend();
                            } else {
                                DeviceInfoActivity.this.curretSend--;
                                DeviceInfoActivity.this.cycleNumber = 0;
                                DeviceInfoActivity.this.isRefrsh = false;
                                DeviceInfoActivity.this.initSend();
                            }
                            break;
                        }
                        return;
                    }
                    return;
                case "com.yscoco.bluetooth.ACTION_DISCONNECT":
                    DeviceInfoActivity.this.mLoadingDialog.dismiss();
                    ToastTool.showNormalShort(DeviceInfoActivity.this, R.string.connect_fail_text);
                    return;
                case "com.yscoco.bluetooth.ACTION_IMPROPER_DISCONNECT":
                    DeviceInfoActivity.this.mLoadingDialog.show(DeviceInfoActivity.this.getString(R.string.connectting_text));
                    return;
                default:
                    return;
            }
        }
    }

    public boolean isPageThressOne() {
        return this.isPageThressOne;
    }

    public void setIsPageThressOne(boolean z) {
        this.isPageThressOne = z;
    }
}
