package com.gddai.lioncheck.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gddai.lioncheck.MyApp;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.adapter.BluDeviceAdapter;
import com.gddai.lioncheck.adapter.base.BaseRecylerAdapter;
import com.gddai.lioncheck.entity.BlueDevice;
import com.gddai.lioncheck.service.BluStaValue;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ys.module.dialog.LoadingDialog;
import com.ys.module.title.TitleBar;
import com.ys.module.toast.ToastTool;

/* JADX INFO: loaded from: classes.dex */
public class BlueDeviceActivity extends BaseActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluDeviceAdapter deviceManagerAdapter;
    public LocalBroadcastManager lbm;

    @ViewInject(R.id.ll_title_left)
    private LinearLayout ll_title_left;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHand;
    private LoadingDialog mLoadingDialog;

    @ViewInject(R.id.tb_title)
    private TitleBar mTitleBar;

    @ViewInject(R.id.device_manager_msg_tv)
    private TextView msgTv;

    @ViewInject(R.id.rlc_blue_device)
    private RecyclerView recyclerViewList;

    @ViewInject(R.id.refresh_search_deivce_iv)
    private ImageView refreshSearchDeivceIv;
    int value;
    private final int SCAN_DEVICE_START = 0;
    private final int SCAN_DEVICE_STOP = 1;
    private final int CONNECT_DEVICE_START = 2;
    private final int CONNECT_DEVICE_SUCC = 4;
    private final int CONNECT_DEVICE_FAIL = 5;
    private final int DEVICE_ADD = 6;
    BluetoothDevice mde = null;
    int rssiValue = -1;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() { // from class: com.gddai.lioncheck.activity.BlueDeviceActivity.3
        @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            if (bluetoothDevice.getName() == null || bluetoothDevice.getName().indexOf("SmartBat") == -1) {
                return;
            }
            synchronized (BlueDeviceActivity.this) {
                for (int i2 = 0; i2 < BlueDeviceActivity.this.deviceManagerAdapter.getItemCount(); i2++) {
                    if (bluetoothDevice.getAddress().equals(BlueDeviceActivity.this.deviceManagerAdapter.getList().get(i2).getDevice().getAddress())) {
                        BlueDeviceActivity.this.deviceManagerAdapter.getList().get(i2).setRssi(i);
                        BlueDeviceActivity.this.deviceManagerAdapter.notifyDataSetChanged();
                        return;
                    }
                }
                BlueDeviceActivity.this.mde = bluetoothDevice;
                BlueDeviceActivity.this.rssiValue = i;
                BlueDeviceActivity.this.handler.sendEmptyMessage(6);
            }
        }
    };
    Handler handler = new Handler() { // from class: com.gddai.lioncheck.activity.BlueDeviceActivity.4
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                BlueDeviceActivity.this.msgTv.setText("设备搜索中...");
                BlueDeviceActivity.this.refreshSearchDeivceIv.setSelected(true);
                LogUtils.e("--------start");
                return;
            }
            if (i == 1) {
                BlueDeviceActivity.this.msgTv.setText(R.string.change_connect_device_text);
                LogUtils.e("--------start");
                BlueDeviceActivity.this.refreshSearchDeivceIv.setSelected(false);
                return;
            }
            if (i == 2) {
                BlueDeviceActivity.this.mLoadingDialog.show(BlueDeviceActivity.this.getString(R.string.connectting_text));
                return;
            }
            if (i == 4) {
                BlueDeviceActivity.this.mLoadingDialog.dismiss();
                BlueDeviceActivity.this.deviceManagerAdapter.refresh();
                BlueDeviceActivity.this.showActivity(DeviceInfoActivity.class);
            } else if (i != 5) {
                if (i != 6) {
                    return;
                }
                BlueDeviceActivity.this.deviceManagerAdapter.addItem(new BlueDevice(BlueDeviceActivity.this.mde, BlueDeviceActivity.this.rssiValue));
            } else {
                BlueDeviceActivity.this.mLoadingDialog.dismiss();
                ToastTool.showNormalShort(BlueDeviceActivity.this, R.string.connect_fail_text);
                BlueDeviceActivity.this.deviceManagerAdapter.refresh();
            }
        }
    };
    BroadcastReceiver MyReceiver = new BroadcastReceiver() { // from class: com.gddai.lioncheck.activity.BlueDeviceActivity.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluStaValue.ACTION_CONNECT)) {
                BlueDeviceActivity.this.handler.sendEmptyMessage(4);
            }
            if (intent.getAction().equals(BluStaValue.ACTION_IMPROPER_DISCONNECT)) {
                BlueDeviceActivity.this.handler.sendEmptyMessage(5);
            }
            if (intent.getAction().equals(BluStaValue.ACTION_DISCONNECT)) {
                BlueDeviceActivity.this.handler.sendEmptyMessage(5);
            }
        }
    };
    BaseRecylerAdapter.OnItemClickListener mDeivceOnItemClickListener = new BaseRecylerAdapter.OnItemClickListener() { // from class: com.gddai.lioncheck.activity.BlueDeviceActivity.6
        @Override // com.gddai.lioncheck.adapter.base.BaseRecylerAdapter.OnItemClickListener
        public void onItemClick(View view, Object obj, int i) {
            BlueDevice blueDevice = (BlueDevice) obj;
            if (BluStaValue.deviceConnctState && BluStaValue.deviceAddress.equals(blueDevice.getDevice().getAddress())) {
                BluStaValue.deviceDisconnState = true;
                MyApp.getmBluetoothLeService().disconnect();
            } else {
                BlueDeviceActivity.this.handler.sendEmptyMessage(2);
                BluStaValue.deviceDisconnState = true;
                MyApp.getmBluetoothLeService().connect(blueDevice.getDevice().getAddress());
            }
        }
    };

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected int setLayoutId() {
        return R.layout.activity_blue_device;
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init() {
        this.mTitleBar.setTitle(R.string.app_name);
        this.ll_title_left.setVisibility(8);
        this.lbm = LocalBroadcastManager.getInstance(this);
        this.mLoadingDialog = new LoadingDialog(this);
        if (getIntent().hasExtra("value")) {
            this.value = getIntent().getIntExtra("value", -1);
        }
        this.recyclerViewList.setLayoutManager(new LinearLayoutManager(this, 1, false));
        BluDeviceAdapter bluDeviceAdapter = new BluDeviceAdapter(this);
        this.deviceManagerAdapter = bluDeviceAdapter;
        this.recyclerViewList.setAdapter(bluDeviceAdapter);
        this.deviceManagerAdapter.setOnItemClickListener(this.mDeivceOnItemClickListener);
        if (BluStaValue.deviceConnctState) {
            this.deviceManagerAdapter.addItem(new BlueDevice(BluStaValue.timeDevice, -10));
            this.deviceManagerAdapter.refresh();
        }
        initReceiver();
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluStaValue.ACTION_CONNECT);
        intentFilter.addAction(BluStaValue.ACTION_DISCONNECT);
        intentFilter.addAction(BluStaValue.ACTION_IMPROPER_DISCONNECT);
        this.lbm.registerReceiver(this.MyReceiver, intentFilter);
    }

    @OnClick({R.id.refresh_search_deivce_iv})
    private void refreshOnClick(View view) {
        if (!this.refreshSearchDeivceIv.isSelected()) {
            this.refreshSearchDeivceIv.setSelected(true);
            initStartScan();
        } else {
            scanLeDevice(false);
        }
    }

    private void initStartScan() {
        this.mHand = new Handler();
        new Handler().postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.BlueDeviceActivity.1
            @Override // java.lang.Runnable
            public void run() {
                BluetoothManager bluetoothManager = (BluetoothManager) BlueDeviceActivity.this.getSystemService("bluetooth");
                BlueDeviceActivity.this.mBluetoothAdapter = bluetoothManager.getAdapter();
                if (!BlueDeviceActivity.this.mBluetoothAdapter.isEnabled()) {
                    if (!BlueDeviceActivity.this.mBluetoothAdapter.isEnabled()) {
                        BlueDeviceActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
                        return;
                    } else {
                        BlueDeviceActivity.this.handler.sendEmptyMessage(0);
                        BlueDeviceActivity.this.scanLeDevice(true);
                        return;
                    }
                }
                BlueDeviceActivity.this.handler.sendEmptyMessage(0);
                BlueDeviceActivity.this.scanLeDevice(true);
            }
        }, 100L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scanLeDevice(boolean z) {
        if (z) {
            BluDeviceAdapter bluDeviceAdapter = this.deviceManagerAdapter;
            if (bluDeviceAdapter == null || bluDeviceAdapter == null || this.mHand == null) {
                return;
            }
            bluDeviceAdapter.clear();
            this.deviceManagerAdapter.notifyDataSetChanged();
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            this.mHand.postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.BlueDeviceActivity.2
                @Override // java.lang.Runnable
                public void run() {
                    BlueDeviceActivity.this.mBluetoothAdapter.stopLeScan(BlueDeviceActivity.this.mLeScanCallback);
                    BlueDeviceActivity.this.handler.sendEmptyMessage(1);
                }
            }, BluStaValue.SCAN_PERIOD);
            return;
        }
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(this.mLeScanCallback);
            this.handler.sendEmptyMessage(1);
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == -1) {
            scanLeDevice(true);
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        initStartScan();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.lbm.unregisterReceiver(this.MyReceiver);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            isExit(i);
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }
}
