package com.gddai.lioncheck.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;
import com.gddai.lioncheck.utils.EncryptUtils;

/* JADX INFO: loaded from: classes.dex */
public class BlueDataUtils {
    public static long ratio;
    public static long totalSum;

    public static void dealData(LocalBroadcastManager localBroadcastManager, byte[] bArr, BluetoothLeService bluetoothLeService) {
        String str;
        Intent intent;
        str = new String(EncryptUtils.encryCode(bArr));
        String strSubstring = str.substring(0, 6);
        intent = new Intent();
        strSubstring.hashCode();
        switch (strSubstring) {
            case "+RD,02":
                intent.setAction(BluStaValue.ACTION_SOC);
                intent.putExtra("value", Integer.valueOf(str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,04":
                intent.setAction(BluStaValue.ACTION_RMC);
                ratio = Integer.valueOf(str.substring(10, 12), 16).intValue();
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue() * Integer.valueOf(str.substring(10, 12), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,06":
                intent.setAction(BluStaValue.ACTION_FCC);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue() * Integer.valueOf(str.substring(10, 12), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,08":
                intent.setAction(BluStaValue.ACTION_VOLTAGE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,0A":
                ratio = Integer.valueOf(str.substring(10, 12), 16).intValue();
                break;
            case "+RD,0C":
                intent.setAction(BluStaValue.ACTION_TEMPERATURE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,10":
                if (ratio != 0) {
                    intent.setAction(BluStaValue.ACTION_CURRENT);
                    long jIntValue = Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue();
                    if (jIntValue > PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID) {
                        jIntValue -= 65535;
                    }
                    intent.putExtra("value", jIntValue * ratio);
                    localBroadcastManager.sendBroadcast(intent);
                    break;
                }
                break;
            case "+RD,18":
                intent.setAction(BluStaValue.ACTION_ATTE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,1A":
                intent.setAction(BluStaValue.ACTION_ATTF);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,28":
                intent.setAction(BluStaValue.ACTION_NUMBER);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,2C":
                intent.setAction(BluStaValue.ACTION_CYCLE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,3C":
                intent.setAction(BluStaValue.ACTION_DCAP);
                ratio = Integer.valueOf(str.substring(10, 12), 16).intValue();
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue() * Integer.valueOf(str.substring(10, 12), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,48":
                intent.setAction(BluStaValue.ACTION_DATE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
        }
    }

    public static void dealDataTwo(LocalBroadcastManager localBroadcastManager, byte[] bArr, BluetoothLeService bluetoothLeService) {
        String strSubstring;
        Intent intent;
        String str = new String(EncryptUtils.encryCode(bArr));
        if (str.toUpperCase().contains("ERR")) {
            return;
        }
        strSubstring = str.substring(0, 6);
        intent = new Intent();
        strSubstring.hashCode();
        switch (strSubstring) {
            case "+RD,08":
                intent.setAction(BluStaValue.ACTION_TEMPERATURE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,09":
                intent.setAction(BluStaValue.ACTION_VOLTAGE);
                long jIntValue = Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue();
                totalSum = jIntValue;
                intent.putExtra("value", jIntValue);
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,0A":
                intent.setAction(BluStaValue.ACTION_CURRENT);
                long jIntValue2 = Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue();
                if (jIntValue2 > PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID) {
                    jIntValue2 -= 65535;
                }
                intent.putExtra("value", jIntValue2 * ((long) Integer.valueOf(str.substring(10, 12), 16).intValue()));
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,0D":
                intent.setAction(BluStaValue.ACTION_SOC);
                intent.putExtra("value", Integer.valueOf(str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,0F":
                intent.setAction(BluStaValue.ACTION_RMC);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue() * Integer.valueOf(str.substring(10, 12), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,10":
                intent.setAction(BluStaValue.ACTION_FCC);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue() * Integer.valueOf(str.substring(10, 12), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,12":
                intent.setAction(BluStaValue.ACTION_ATTE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,13":
                intent.setAction(BluStaValue.ACTION_ATTF);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,17":
                intent.setAction(BluStaValue.ACTION_CYCLE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,18":
                intent.setAction(BluStaValue.ACTION_DCAP);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue() * Integer.valueOf(str.substring(10, 12), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,1B":
                intent.setAction(BluStaValue.ACTION_DATE);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            case "+RD,1C":
                intent.setAction(BluStaValue.ACTION_NUMBER);
                intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                localBroadcastManager.sendBroadcast(intent);
                break;
            default:
                if (strSubstring.contains("+RD,3")) {
                    intent.setAction(BluStaValue.ACTION_SINGLE_VOLTAGE);
                    intent.putExtra("single", 16 - Integer.valueOf(str.substring(5, 6), 16).intValue());
                    intent.putExtra("value", Integer.valueOf(str.substring(8, 10) + str.substring(6, 8), 16).intValue());
                    localBroadcastManager.sendBroadcast(intent);
                    break;
                }
                break;
        }
    }
}
