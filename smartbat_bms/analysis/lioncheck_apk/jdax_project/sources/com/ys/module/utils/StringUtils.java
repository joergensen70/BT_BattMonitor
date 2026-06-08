package com.ys.module.utils;

import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class StringUtils {
    private static final Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private static final Pattern phone = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

    public static boolean isEmpty(CharSequence charSequence) {
        if (charSequence != null && !"".equals(charSequence)) {
            for (int i = 0; i < charSequence.length(); i++) {
                char cCharAt = charSequence.charAt(i);
                if (cCharAt != ' ' && cCharAt != '\t' && cCharAt != '\r' && cCharAt != '\n') {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isPhone(CharSequence charSequence) {
        if (isEmpty(charSequence)) {
            return false;
        }
        return phone.matcher(charSequence).matches();
    }

    public static boolean isEmail(CharSequence charSequence) {
        if (isEmpty(charSequence)) {
            return false;
        }
        return emailer.matcher(charSequence).matches();
    }

    public static boolean isNumeric(String str) {
        return Pattern.compile("[0-9]*").matcher(str).matches();
    }

    public static int toInt(String str, int i) {
        try {
            return Integer.parseInt(str);
        } catch (Exception unused) {
            return i;
        }
    }

    public static String getID(String str) {
        if (str == null) {
            return "";
        }
        if (str.length() == 15) {
            return str.substring(0, 8) + "****" + str.substring(12, str.length());
        }
        if (str.length() != 18) {
            return "";
        }
        return str.substring(0, 10) + "****" + str.substring(14, str.length());
    }
}
