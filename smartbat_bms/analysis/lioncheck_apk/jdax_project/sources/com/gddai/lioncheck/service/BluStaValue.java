package com.gddai.lioncheck.service;

import android.bluetooth.BluetoothDevice;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BluStaValue {
    public static final String ACTION_ATTE = "com.yscoco.bluetooth.ACTION_ATTE";
    public static final String ACTION_ATTF = "com.yscoco.bluetooth.ACTION_ATTF";
    public static final String ACTION_CONNECT = "com.yscoco.bluetooth.ACTION_CONNECT";
    public static final String ACTION_CURRENT = "com.yscoco.bluetooth.ACTION_CURRENT";
    public static final String ACTION_CYCLE = "com.yscoco.bluetooth.ACTION_CYCLE";
    public static final String ACTION_DATE = "com.yscoco.bluetooth.ACTION_DATE";
    public static final String ACTION_DCAP = "com.yscoco.bluetooth.ACTION_DCAP";
    public static final String ACTION_DISCONNECT = "com.yscoco.bluetooth.ACTION_DISCONNECT";
    public static final String ACTION_FCC = "com.yscoco.bluetooth.ACTION_FCC";
    public static final String ACTION_IMPROPER_DISCONNECT = "com.yscoco.bluetooth.ACTION_IMPROPER_DISCONNECT";
    public static final String ACTION_NUMBER = "com.yscoco.bluetooth.ACTION_NUMBER";
    public static final String ACTION_RECEIVER_VALUE = "com.yscoco.bluetooth.ACTION_RECEIVER_VALUE";
    public static final String ACTION_RMC = "com.yscoco.bluetooth.ACTION_RMC";
    public static final String ACTION_RSSI = "com.yscoco.bluetooth.ACTION_RSSI";
    public static final String ACTION_SINGLE_VOLTAGE = "com.yscoco.bluetooth.ACTION_SINGLE_VOLTAGE";
    public static final String ACTION_SOC = "com.yscoco.bluetooth.ACTION_SOC";
    public static final String ACTION_TEMPERATURE = "com.yscoco.bluetooth.ACTION_TEMPERATURE";
    public static final String ACTION_VOLTAGE = "com.yscoco.bluetooth.ACTION_VOLTAGE";
    public static final String CHA_UUID1 = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static final String CHA_UUID2 = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static final String DES_UUID1 = "00002902-0000-1000-8000-00805f9b34fb";
    public static final long SCAN_PERIOD = 15000;
    public static final String SERVICE_UUID1 = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String deviceAddress = "";
    public static boolean deviceConnctState = false;
    public static boolean deviceDisconnState = false;
    public static BluetoothDevice timeDevice;
    public static List<byte[]> sendValueByte1 = new ArrayList();
    public static List<byte[]> sendValueByte2 = new ArrayList();
    public static List<byte[]> sendValueByte3 = new ArrayList();
    public static int deviceType = 2;
}
