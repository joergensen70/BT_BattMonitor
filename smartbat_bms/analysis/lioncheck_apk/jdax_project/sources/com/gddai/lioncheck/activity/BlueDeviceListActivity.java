package com.gddai.lioncheck.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.gddai.lioncheck.MyApp;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.adapter.BluDeviceListAdapter;
import com.gddai.lioncheck.entity.BlueDevice;
import com.gddai.lioncheck.service.BluStaValue;
import com.gddai.lioncheck.service.BlueDataUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ys.module.dialog.LoadingDialog;
import com.ys.module.swip.SwipyRefreshLayout;
import com.ys.module.swip.SwipyRefreshLayoutDirection;
import com.ys.module.toast.ToastTool;
import java.util.ArrayList;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class BlueDeviceListActivity extends BaseActivity {
    private static final int REQUEST_ENABLE_BT = 1;

    @ViewInject(R.id.backIM)
    private ImageView backIM;
    private BluDeviceListAdapter deviceManagerAdapter;
    boolean hasP;
    public LocalBroadcastManager lbm;

    @ViewInject(R.id.linkstatus)
    private ImageView linkstatus;

    @ViewInject(R.id.ll_search)
    private LinearLayout ll_search;
    LocationManager lm;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHand;
    private LoadingDialog mLoadingDialog;
    boolean ok;

    @ViewInject(R.id.rlc_blue_device)
    private ListView recyclerViewList;
    private SharedPreferences sharedPreferences;

    @ViewInject(R.id.swipe_ly)
    private SwipyRefreshLayout swipe_ly;
    int value;
    private final int SCAN_DEVICE_START = 0;
    private final int SCAN_DEVICE_STOP = 1;
    private final int CONNECT_DEVICE_START = 2;
    private final int CONNECT_DEVICE_SUCC = 4;
    private final int CONNECT_DEVICE_FAIL = 5;
    private final int DEVICE_ADD = 6;
    private boolean isAdd = false;
    private boolean isScan = false;
    private final int REQUEST_PERMISSION_CODE = 1001;
    BluetoothDevice mde = null;
    int rssiValue = -1;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.5
        @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            if (bluetoothDevice != null) {
                Log.i("0930", "" + bluetoothDevice.getName());
            }
            if (BlueDeviceListActivity.this.isAdd) {
                return;
            }
            BlueDeviceListActivity.this.isAdd = true;
            if (bluetoothDevice != null && bluetoothDevice.getName() != null && bluetoothDevice.getName().indexOf("SmartBat") != -1 && BlueDeviceListActivity.this.isSearch(bluetoothDevice.getName())) {
                synchronized (BlueDeviceListActivity.this) {
                    for (int i2 = 0; i2 < BlueDeviceListActivity.this.deviceManagerAdapter.getList().size(); i2++) {
                        if (bluetoothDevice.getAddress().equals(BlueDeviceListActivity.this.deviceManagerAdapter.getList().get(i2).getDevice().getAddress())) {
                            BlueDeviceListActivity.this.deviceManagerAdapter.getList().get(i2).setDevice(bluetoothDevice);
                            BlueDeviceListActivity.this.deviceManagerAdapter.getList().get(i2).setRssi(i);
                            BlueDeviceListActivity.this.handler.sendEmptyMessage(12);
                            BlueDeviceListActivity.this.isAdd = false;
                            return;
                        }
                    }
                    BlueDeviceListActivity.this.mde = bluetoothDevice;
                    BlueDeviceListActivity.this.rssiValue = i;
                    BlueDeviceListActivity.this.handler.sendEmptyMessage(6);
                    return;
                }
            }
            if (bluetoothDevice.getName() == null || bluetoothDevice.getName().indexOf("Solarpack") == -1) {
                BlueDeviceListActivity.this.isAdd = false;
                return;
            }
            synchronized (BlueDeviceListActivity.this) {
                for (int i3 = 0; i3 < BlueDeviceListActivity.this.deviceManagerAdapter.getList().size(); i3++) {
                    if (bluetoothDevice.getAddress().equals(BlueDeviceListActivity.this.deviceManagerAdapter.getList().get(i3).getDevice().getAddress())) {
                        BlueDeviceListActivity.this.deviceManagerAdapter.getList().get(i3).setDevice(bluetoothDevice);
                        BlueDeviceListActivity.this.deviceManagerAdapter.getList().get(i3).setRssi(i);
                        BlueDeviceListActivity.this.handler.sendEmptyMessage(12);
                        BlueDeviceListActivity.this.isAdd = false;
                        return;
                    }
                }
                BlueDeviceListActivity.this.mde = bluetoothDevice;
                BlueDeviceListActivity.this.rssiValue = i;
                BlueDeviceListActivity.this.handler.sendEmptyMessage(6);
            }
        }
    };
    Handler handler = new Handler() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.6
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                BlueDeviceListActivity.this.linkstatus.setVisibility(0);
                LogUtils.e("--------start");
                return;
            }
            if (i == 1) {
                BlueDeviceListActivity.this.linkstatus.setVisibility(8);
                LogUtils.e("--------start");
                return;
            }
            if (i == 2) {
                BlueDeviceListActivity.this.mLoadingDialog.show(BlueDeviceListActivity.this.getString(R.string.connectting_text));
                return;
            }
            if (i == 4) {
                BlueDeviceListActivity.this.handler.sendEmptyMessageDelayed(13, 1000L);
                return;
            }
            if (i == 5) {
                BlueDeviceListActivity.this.mLoadingDialog.dismiss();
                ToastTool.showNormalShort(BlueDeviceListActivity.this, R.string.connect_fail_text);
                BlueDeviceListActivity.this.deviceManagerAdapter.refresh();
                return;
            }
            if (i == 6) {
                if (BlueDeviceListActivity.this.mde != null) {
                    BlueDeviceListActivity.this.deviceManagerAdapter.addItem(new BlueDevice(BlueDeviceListActivity.this.mde, BlueDeviceListActivity.this.rssiValue));
                    BlueDeviceListActivity.this.deviceManagerAdapter.refresh();
                }
                BlueDeviceListActivity.this.isAdd = false;
                return;
            }
            if (i == 12) {
                BlueDeviceListActivity.this.deviceManagerAdapter.notifyDataSetChanged();
                return;
            }
            if (i != 13) {
                return;
            }
            BlueDeviceListActivity.this.mLoadingDialog.dismiss();
            BlueDeviceListActivity.this.deviceManagerAdapter.refresh();
            if (BluStaValue.deviceConnctState) {
                BlueDataUtils.ratio = 0L;
                BlueDeviceListActivity.this.showActivity(DeviceInfoActivity.class);
            }
        }
    };
    BroadcastReceiver MyReceiver = new BroadcastReceiver() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluStaValue.ACTION_CONNECT)) {
                BlueDeviceListActivity.this.handler.sendEmptyMessage(4);
            }
            if (intent.getAction().equals(BluStaValue.ACTION_IMPROPER_DISCONNECT)) {
                BlueDeviceListActivity.this.handler.sendEmptyMessage(5);
            }
            if (intent.getAction().equals(BluStaValue.ACTION_DISCONNECT)) {
                BlueDeviceListActivity.this.handler.sendEmptyMessage(5);
            }
        }
    };
    TextWatcher textWatcher = new TextWatcher() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.8
        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable editable) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            BlueDeviceListActivity.this.search();
        }
    };

    private void initClick() {
    }

    @OnClick({R.id.refresh_search_deivce_iv})
    private void refreshOnClick(View view) {
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected int setLayoutId() {
        return R.layout.activity_blue_device_two;
    }

    private void initPermission() {
        ArrayList arrayList = new ArrayList();
        if (Build.VERSION.SDK_INT >= 31) {
            arrayList.add("android.permission.BLUETOOTH_SCAN");
            arrayList.add("android.permission.BLUETOOTH_ADVERTISE");
            arrayList.add("android.permission.BLUETOOTH_CONNECT");
        } else {
            arrayList.add("android.permission.ACCESS_COARSE_LOCATION");
            arrayList.add("android.permission.ACCESS_FINE_LOCATION");
        }
        ActivityCompat.requestPermissions(this, (String[]) arrayList.toArray(new String[0]), 1001);
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity
    protected void init() {
        this.lbm = LocalBroadcastManager.getInstance(this);
        this.mLoadingDialog = new LoadingDialog(this);
        if (getIntent().hasExtra("value")) {
            this.value = getIntent().getIntExtra("value", -1);
        }
        this.sharedPreferences = getSharedPreferences("lang", 0);
        getResources().getString(R.string.device_cell_text).equals("Cell");
        BluDeviceListAdapter bluDeviceListAdapter = new BluDeviceListAdapter(this);
        this.deviceManagerAdapter = bluDeviceListAdapter;
        this.recyclerViewList.setAdapter((ListAdapter) bluDeviceListAdapter);
        if (BluStaValue.deviceConnctState) {
            this.deviceManagerAdapter.addItem(new BlueDevice(BluStaValue.timeDevice, -10));
            this.deviceManagerAdapter.refresh();
        }
        initReceiver();
        this.swipe_ly.setDirection(SwipyRefreshLayoutDirection.TOP);
        this.swipe_ly.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.1
            @Override // com.ys.module.swip.SwipyRefreshLayout.OnRefreshListener
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (AnonymousClass9.$SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection[swipyRefreshLayoutDirection.ordinal()] != 1) {
                    return;
                }
                if (!BlueDeviceListActivity.this.isScan) {
                    BlueDeviceListActivity.this.initStartScan();
                }
                new Handler().postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        BlueDeviceListActivity.this.swipe_ly.setRefreshing(false);
                    }
                }, 1500L);
            }
        });
        LocationManager locationManager = (LocationManager) getSystemService("location");
        this.lm = locationManager;
        boolean zIsProviderEnabled = locationManager.isProviderEnabled("gps");
        this.ok = zIsProviderEnabled;
        if (zIsProviderEnabled) {
            if (checkBluetoothPermissions()) {
                initStartScan();
            } else {
                initPermission();
            }
        } else {
            Intent intent = new Intent();
            intent.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
            startActivityForResult(intent, 1315);
        }
        clearRecycleClick();
        initClick();
    }

    /* JADX INFO: renamed from: com.gddai.lioncheck.activity.BlueDeviceListActivity$9, reason: invalid class name */
    static /* synthetic */ class AnonymousClass9 {
        static final /* synthetic */ int[] $SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection;

        static {
            int[] iArr = new int[SwipyRefreshLayoutDirection.values().length];
            $SwitchMap$com$ys$module$swip$SwipyRefreshLayoutDirection = iArr;
            try {
                iArr[SwipyRefreshLayoutDirection.TOP.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    @Override // com.gddai.lioncheck.activity.base.BaseActivity, android.support.v4.app.FragmentActivity, android.app.Activity, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 1001) {
            return;
        }
        for (int i2 : iArr) {
            if (i2 != 0) {
                ToastTool.showNormalShort(this, "蓝牙权限被拒绝，无法搜索设备");
                return;
            }
        }
        initStartScan();
    }

    private void clearRecycleClick() {
        this.recyclerViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.2
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                BlueDeviceListActivity.this.deviceManagerAdapter.setSelectedPosition(i);
                BlueDevice blueDevice = (BlueDevice) adapterView.getAdapter().getItem(i);
                BluStaValue.deviceDisconnState = true;
                MyApp.getmBluetoothLeService().disconnect();
                MyApp.getmBluetoothLeService().connect(blueDevice.getDevice().getAddress());
                BlueDeviceListActivity.this.handler.sendEmptyMessage(2);
            }
        });
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluStaValue.ACTION_CONNECT);
        intentFilter.addAction(BluStaValue.ACTION_DISCONNECT);
        intentFilter.addAction(BluStaValue.ACTION_IMPROPER_DISCONNECT);
        this.lbm.registerReceiver(this.MyReceiver, intentFilter);
    }

    @OnClick({R.id.right_image})
    private void refresh(View view) {
        initStartScan();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initStartScan() {
        if (!checkBluetoothPermissions()) {
            initPermission();
        } else {
            this.mHand = new Handler();
            new Handler().postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.3
                @Override // java.lang.Runnable
                public void run() {
                    BlueDeviceListActivity.this.mBluetoothAdapter = ((BluetoothManager) BlueDeviceListActivity.this.getSystemService("bluetooth")).getAdapter();
                    Log.i("0930", "mBluetoothAdapter:" + BlueDeviceListActivity.this.mBluetoothAdapter.isEnabled());
                    if (!BlueDeviceListActivity.this.mBluetoothAdapter.isEnabled()) {
                        if (!BlueDeviceListActivity.this.mBluetoothAdapter.isEnabled()) {
                            BlueDeviceListActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
                            return;
                        } else {
                            BlueDeviceListActivity.this.handler.sendEmptyMessage(0);
                            BlueDeviceListActivity.this.scanLeDevice(true);
                            return;
                        }
                    }
                    BlueDeviceListActivity.this.handler.sendEmptyMessage(0);
                    BlueDeviceListActivity.this.scanLeDevice(true);
                }
            }, 100L);
        }
    }

    private boolean checkBluetoothPermissions() {
        return Build.VERSION.SDK_INT >= 31 ? ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_SCAN") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_CONNECT") == 0 : ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_ADMIN") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scanLeDevice(boolean z) {
        Log.i("0930", "ok:" + this.ok);
        if (this.ok) {
            this.isAdd = false;
            if (z) {
                BluDeviceListAdapter bluDeviceListAdapter = this.deviceManagerAdapter;
                if (bluDeviceListAdapter == null || bluDeviceListAdapter == null || this.mHand == null) {
                    return;
                }
                bluDeviceListAdapter.clearItem();
                this.isScan = true;
                this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
                this.mHand.postDelayed(new Runnable() { // from class: com.gddai.lioncheck.activity.BlueDeviceListActivity.4
                    @Override // java.lang.Runnable
                    public void run() {
                        BlueDeviceListActivity.this.isScan = false;
                        BlueDeviceListActivity.this.mBluetoothAdapter.stopLeScan(BlueDeviceListActivity.this.mLeScanCallback);
                        BlueDeviceListActivity.this.handler.sendEmptyMessage(1);
                    }
                }, BluStaValue.SCAN_PERIOD);
                return;
            }
            BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
            if (bluetoothAdapter != null) {
                this.isScan = false;
                bluetoothAdapter.stopLeScan(this.mLeScanCallback);
                this.handler.sendEmptyMessage(1);
            }
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1315) {
            LocationManager locationManager = (LocationManager) getSystemService("location");
            this.lm = locationManager;
            boolean zIsProviderEnabled = locationManager.isProviderEnabled("gps");
            this.ok = zIsProviderEnabled;
            if (zIsProviderEnabled) {
                if (checkBluetoothPermissions()) {
                    initStartScan();
                } else {
                    initPermission();
                }
            }
        } else if (i2 == -1) {
            scanLeDevice(true);
        }
        IntentResult activityResult = IntentIntegrator.parseActivityResult(i, i2, intent);
        if (activityResult != null) {
            if (activityResult.getContents() == null) {
                LogUtils.e("tagCancelled");
            } else {
                LogUtils.e("tagScanned" + activityResult.getContents());
            }
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        if (checkBluetoothPermissions()) {
            initStartScan();
        } else {
            initPermission();
        }
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

    private void setSerach() {
        if (this.ll_search.getVisibility() == 8) {
            this.ll_search.setVisibility(0);
        } else {
            this.ll_search.setVisibility(8);
        }
    }

    private void changeLanguage(int i) {
        Locale locale;
        this.sharedPreferences.edit().putInt("lag", i).commit();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Configuration configuration = getResources().getConfiguration();
        if (i == 1) {
            locale = Locale.getDefault();
        } else {
            locale = i != 2 ? null : Locale.GERMANY;
        }
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, displayMetrics);
        Intent intent = new Intent(this, (Class<?>) BlueDeviceListActivity.class);
        intent.setFlags(268468224);
        startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void search() {
        this.deviceManagerAdapter.getList().clear();
        this.deviceManagerAdapter.notifyDataSetChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSearch(String str) {
        if (this.ll_search.getVisibility() == 8) {
            return true;
        }
        if (str.length() < 10) {
            return false;
        }
        Long.valueOf(str.substring(10)).longValue();
        return true;
    }
}
