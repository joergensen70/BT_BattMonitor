package com.gddai.lioncheck.utils;

import com.lidroid.xutils.util.LogUtils;

/* JADX INFO: loaded from: classes.dex */
public class EncryptUtils {
    public static int[] encryptByte = {2, 5, 4, 3, 1, 4, 1, 6, 8, 3, 7, 2, 5, 8, 9, 3};
    public static int resouce;

    public static int getEncyKey(int i, String str) {
        int i2 = 0;
        while (str != null && str.length() != 0) {
            i2 += encryptByte[Integer.valueOf(str.substring(0, 1), 16).intValue() & 15];
            str = str.substring(1);
        }
        return i == 1 ? i2 + 5 : i2 + 8;
    }

    public static void getType(String str) {
        LogUtils.e("截取的数据为" + str);
        String strSubstring = str.substring(0, 1);
        String hexString = Integer.toHexString(Integer.valueOf(str.substring(1, str.length())).intValue());
        while (hexString.length() < 4) {
            hexString = "0" + hexString;
        }
        LogUtils.e("数据为" + hexString);
        if (strSubstring.toUpperCase().equals("A")) {
            resouce = getEncyKey(1, hexString);
        } else {
            resouce = getEncyKey(2, hexString);
        }
        LogUtils.e("加密字符串为" + resouce);
    }

    public static byte[] encryCode(byte[] bArr) {
        byte[] bArr2 = new byte[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr2[i] = (byte) ((bArr[i] & 255) ^ resouce);
        }
        return bArr2;
    }
}
