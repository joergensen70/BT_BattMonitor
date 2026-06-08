package com.gddai.lioncheck.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.gddai.lioncheck.dbutils.SocEntityDBUtils;
import com.gddai.lioncheck.utils.EncryptUtils;
import com.lidroid.xutils.util.LogUtils;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class BluetoothLeService extends Service {
    public LocalBroadcastManager lbm;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    Timer rssiTimer;
    TimerTask task;
    private final IBinder mBinder = new LocalBinder();
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() { // from class: com.gddai.lioncheck.service.BluetoothLeService.2
        @Override // android.bluetooth.BluetoothGattCallback
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.i("blue", "onConnectionStateChange : status" + i + "newState" + i2);
            if (i2 == 2) {
                BluStaValue.deviceDisconnState = false;
                BluetoothLeService.this.mBluetoothGatt.discoverServices();
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BluStaValue.deviceDisconnState = false;
                return;
            }
            SocEntityDBUtils.deleteALL();
            BluetoothLeService.this.mBluetoothGatt.close();
            BluStaValue.deviceConnctState = false;
            if (!BluStaValue.deviceDisconnState && BluStaValue.deviceAddress.equals(bluetoothGatt.getDevice().getAddress())) {
                BluetoothLeService.this.new disconnNewConnTh().start();
                BluetoothLeService.this.lbm.sendBroadcast(new Intent(BluStaValue.ACTION_IMPROPER_DISCONNECT));
            }
            if (bluetoothGatt.getDevice().getAddress().equals(BluStaValue.deviceAddress)) {
                LogUtils.e("断开连接了");
                BluetoothLeService.this.lbm.sendBroadcast(new Intent(BluStaValue.ACTION_DISCONNECT));
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.i("blue", "onServicesDiscovered:" + i);
            if (i != 0) {
                BluetoothLeService.this.mBluetoothGatt.close();
                BluetoothLeService.this.mBluetoothGatt = null;
                BluetoothLeService bluetoothLeService = BluetoothLeService.this;
                bluetoothLeService.connect(bluetoothLeService.mBluetoothDeviceAddress);
            } else {
                BluStaValue.deviceConnctState = true;
                BluStaValue.timeDevice = bluetoothGatt.getDevice();
                BluetoothLeService.this.new NotifThread().start();
            }
            super.onServicesDiscovered(bluetoothGatt, i);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.e("blue", "onCharacteristicRead:" + i);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Log.e("blue", "onCharacteristicChanged:");
            String strTrim = bluetoothGattCharacteristic.getUuid().toString().trim();
            byte[] value = bluetoothGattCharacteristic.getValue();
            if (strTrim.equals(BluStaValue.CHA_UUID1)) {
                if (BluStaValue.deviceType == 1) {
                    BlueDataUtils.dealData(BluetoothLeService.this.lbm, value, BluetoothLeService.this);
                } else {
                    BlueDataUtils.dealDataTwo(BluetoothLeService.this.lbm, value, BluetoothLeService.this);
                }
                if (value.length > 3) {
                    try {
                        Intent intent = new Intent(BluStaValue.ACTION_RECEIVER_VALUE);
                        intent.putExtra("value", new String(EncryptUtils.encryCode(value)));
                        BluetoothLeService.this.lbm.sendBroadcast(intent);
                    } catch (Exception unused) {
                    }
                }
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.e("blue", "onCharacteristicWrite:");
            super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i, int i2) {
            super.onReadRemoteRssi(bluetoothGatt, i, i2);
            Intent intent = new Intent(BluStaValue.ACTION_RSSI);
            intent.putExtra("rssi", i);
            BluetoothLeService.this.lbm.sendBroadcast(intent);
        }
    };

    public BluetoothLeService getService1() {
        return this;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.lbm = LocalBroadcastManager.getInstance(this);
        this.task = new TimerTask() { // from class: com.gddai.lioncheck.service.BluetoothLeService.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                if (!BluStaValue.deviceConnctState || BluetoothLeService.this.mBluetoothGatt == null) {
                    return;
                }
                BluetoothLeService.this.mBluetoothGatt.readRemoteRssi();
            }
        };
        Timer timer = new Timer();
        this.rssiTimer = timer;
        TimerTask timerTask = this.task;
        if (timerTask != null) {
            timer.schedule(timerTask, 1000L, 1000L);
        }
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public boolean initialize() {
        if (this.mBluetoothManager == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            this.mBluetoothManager = bluetoothManager;
            if (bluetoothManager == null) {
                return false;
            }
        }
        BluetoothAdapter adapter = this.mBluetoothManager.getAdapter();
        this.mBluetoothAdapter = adapter;
        return adapter != null;
    }

    public boolean connect(String str) {
        BluetoothGatt bluetoothGatt;
        BluStaValue.deviceAddress = str;
        this.mBluetoothDeviceAddress = "";
        initialize();
        if (!this.mBluetoothAdapter.isEnabled()) {
            return false;
        }
        if (BluStaValue.deviceConnctState && this.mBluetoothGatt != null) {
            Log.e("error", "断开连接");
            this.mBluetoothGatt.disconnect();
            this.mBluetoothGatt = null;
        }
        if (this.mBluetoothAdapter == null || str == null) {
            return false;
        }
        String str2 = this.mBluetoothDeviceAddress;
        if (str2 != null && str.equals(str2) && (bluetoothGatt = this.mBluetoothGatt) != null) {
            return bluetoothGatt.connect();
        }
        BluetoothDevice remoteDevice = this.mBluetoothAdapter.getRemoteDevice(str.toUpperCase().trim());
        if (remoteDevice == null) {
            return false;
        }
        this.mBluetoothGatt = remoteDevice.connectGatt(this, false, this.mGattCallback);
        this.mBluetoothDeviceAddress = str;
        return true;
    }

    public void disconnect() {
        BluetoothGatt bluetoothGatt;
        if (this.mBluetoothAdapter == null || (bluetoothGatt = this.mBluetoothGatt) == null) {
            return;
        }
        bluetoothGatt.disconnect();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        this.mBluetoothGatt = null;
    }

    public synchronized boolean writeValue(byte[] bArr) {
        if (!BluStaValue.deviceConnctState) {
            return false;
        }
        return writeLlsAlertLevel(BluStaValue.SERVICE_UUID1, BluStaValue.CHA_UUID2, bArr);
    }

    public synchronized boolean writeLlsAlertLevel(String str, String str2, byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : bArr) {
            stringBuffer.append(String.format("%02X ", Byte.valueOf(b)).toString().trim());
        }
        Log.i("blue", "writeLlsAlertLevel : " + stringBuffer.toString() + "原始String值：" + new String(bArr).toString());
        byte[] bArrEncryCode = EncryptUtils.encryCode(bArr);
        BluetoothGattCharacteristic character = getCharacter(str, str2);
        if (character == null) {
            Log.e("error", "link loss Alert Level charateristic not found!");
            return false;
        }
        if (this.mBluetoothGatt == null) {
            return false;
        }
        character.getWriteType();
        character.setValue(bArrEncryCode);
        character.setWriteType(1);
        return this.mBluetoothGatt.writeCharacteristic(character);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startNotification(String str, String str2) {
        Log.i("blue", "startNotification : ");
        BluetoothGattCharacteristic character = getCharacter(str, str2);
        if (character != null) {
            Log.e("taa", "获取到了通知");
            int properties = character.getProperties();
            if ((properties | 2) > 0) {
                if (character != null) {
                    Log.e("error", "设置0为false");
                    setCharacteristicNotification(character, false);
                }
                readCharacteristic(str, str2);
                if ((properties | 16) > 0) {
                    Log.e("error", "继续清理1");
                    setCharacteristicNotification(character, true);
                }
            }
        }
    }

    public void readCharacteristic(String str, String str2) {
        BluetoothGattCharacteristic character;
        Log.i("blue", "readCharacteristic : ");
        if (this.mBluetoothAdapter == null || this.mBluetoothGatt == null || (character = getCharacter(str, str2)) == null) {
            return;
        }
        this.mBluetoothGatt.readCharacteristic(character);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        BluetoothGatt bluetoothGatt;
        Log.i("blue", "setCharacteristicNotification : ");
        if (this.mBluetoothAdapter == null || (bluetoothGatt = this.mBluetoothGatt) == null) {
            return;
        }
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString(BluStaValue.DES_UUID1));
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.mBluetoothGatt.writeDescriptor(descriptor);
    }

    public BluetoothGattCharacteristic getCharacter(String str, String str2) {
        BluetoothGattService service;
        Log.i("blue", "getCharacter : ");
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt == null || (service = bluetoothGatt.getService(UUID.fromString(str))) == null) {
            return null;
        }
        return service.getCharacteristic(UUID.fromString(str2));
    }

    class disconnNewConnTh extends Thread {
        disconnNewConnTh() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                boolean z = true;
                while (z) {
                    try {
                        Thread.sleep(1000L);
                        if (BluetoothLeService.this.mBluetoothAdapter.isEnabled()) {
                            if (BluetoothLeService.this.mBluetoothDeviceAddress != null && BluStaValue.deviceAddress.equals(BluetoothLeService.this.mBluetoothDeviceAddress)) {
                                if (BluetoothLeService.this.mBluetoothGatt != null) {
                                    BluetoothLeService.this.mBluetoothGatt.close();
                                    BluetoothLeService.this.mBluetoothGatt = null;
                                }
                                BluetoothLeService bluetoothLeService = BluetoothLeService.this;
                                bluetoothLeService.connect(bluetoothLeService.mBluetoothDeviceAddress);
                                z = false;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                return;
            }
        }
    }

    class NotifThread extends Thread {
        NotifThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            if (BluetoothLeService.this.mBluetoothAdapter.isEnabled()) {
                LogUtils.e("开启通知");
                BluetoothLeService.this.startNotification(BluStaValue.SERVICE_UUID1, BluStaValue.CHA_UUID1);
                try {
                    Thread.sleep(50L);
                    BluetoothLeService.this.lbm.sendBroadcast(new Intent(BluStaValue.ACTION_CONNECT));
                } catch (Exception unused) {
                }
            }
        }
    }
}
