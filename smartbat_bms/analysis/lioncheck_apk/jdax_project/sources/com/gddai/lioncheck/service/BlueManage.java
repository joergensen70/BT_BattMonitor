package com.gddai.lioncheck.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class BlueManage {
    public static void clearBond() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        Log.e("shuji", "devices.size" + bondedDevices.size());
        if (bondedDevices.isEmpty()) {
            return;
        }
        Iterator<BluetoothDevice> it = bondedDevices.iterator();
        while (it.hasNext()) {
            unpairDevice(it.next());
        }
    }

    private static void unpairDevice(BluetoothDevice bluetoothDevice) {
        try {
            bluetoothDevice.getClass().getMethod("removeBond", null).invoke(bluetoothDevice, null);
        } catch (Exception e) {
            Log.e("错误", e.getMessage());
        }
    }

    public static void refreshDeviceCache(BluetoothGatt bluetoothGatt) {
        try {
            Method method = bluetoothGatt.getClass().getMethod("refrsh", new Class[0]);
            if (method != null) {
                method.invoke(bluetoothGatt, new Object[0]);
            }
        } catch (Exception unused) {
            Log.e("", "A年exception occured while refreshing device");
        }
    }
}
