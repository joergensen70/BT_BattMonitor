package com.gddai.lioncheck.entity;

import android.bluetooth.BluetoothDevice;

/* JADX INFO: loaded from: classes.dex */
public class BlueDevice {
    private BluetoothDevice device;
    private int rssi;

    public BlueDevice(BluetoothDevice bluetoothDevice, int i) {
        this.device = bluetoothDevice;
        this.rssi = i;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public void setDevice(BluetoothDevice bluetoothDevice) {
        this.device = bluetoothDevice;
    }

    public int getRssi() {
        return this.rssi;
    }

    public void setRssi(int i) {
        this.rssi = i;
    }
}
