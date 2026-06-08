package com.google.zxing.client.result;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public final class ExpandedProductResultParser extends ResultParser {
    @Override // com.google.zxing.client.result.ResultParser
    public ExpandedProductParsedResult parse(Result result) {
        String strFindValue;
        int length;
        HashMap map;
        if (result.getBarcodeFormat() != BarcodeFormat.RSS_EXPANDED) {
            return null;
        }
        String massagedText = getMassagedText(result);
        HashMap map2 = new HashMap();
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String str7 = null;
        String str8 = null;
        String str9 = null;
        String strSubstring = null;
        String strSubstring2 = null;
        String strSubstring3 = null;
        String strSubstring4 = null;
        int i = 0;
        while (i < massagedText.length()) {
            String strFindAIvalue = findAIvalue(i, massagedText);
            if (strFindAIvalue == null) {
                return null;
            }
            int length2 = i + strFindAIvalue.length() + 2;
            strFindValue = findValue(length2, massagedText);
            length = length2 + strFindValue.length();
            strFindAIvalue.hashCode();
            switch (strFindAIvalue) {
                case "00":
                    str2 = strFindValue;
                    i = length;
                    break;
                case "01":
                    str = strFindValue;
                    i = length;
                    break;
                case "10":
                    str3 = strFindValue;
                    i = length;
                    break;
                case "11":
                    str4 = strFindValue;
                    i = length;
                    break;
                case "13":
                    str5 = strFindValue;
                    i = length;
                    break;
                case "15":
                    str6 = strFindValue;
                    i = length;
                    break;
                case "17":
                    str7 = strFindValue;
                    i = length;
                    break;
                case "3100":
                case "3101":
                case "3102":
                case "3103":
                case "3104":
                case "3105":
                case "3106":
                case "3107":
                case "3108":
                case "3109":
                    map = map2;
                    strSubstring = strFindAIvalue.substring(3);
                    str9 = ExpandedProductParsedResult.KILOGRAM;
                    str8 = strFindValue;
                    map2 = map;
                    i = length;
                    break;
                case "3200":
                case "3201":
                case "3202":
                case "3203":
                case "3204":
                case "3205":
                case "3206":
                case "3207":
                case "3208":
                case "3209":
                    map = map2;
                    strSubstring = strFindAIvalue.substring(3);
                    str9 = ExpandedProductParsedResult.POUND;
                    str8 = strFindValue;
                    map2 = map;
                    i = length;
                    break;
                case "3920":
                case "3921":
                case "3922":
                case "3923":
                    map = map2;
                    strSubstring3 = strFindAIvalue.substring(3);
                    strSubstring2 = strFindValue;
                    map2 = map;
                    i = length;
                    break;
                case "3930":
                case "3931":
                case "3932":
                case "3933":
                    if (strFindValue.length() >= 4) {
                        strSubstring2 = strFindValue.substring(3);
                        map = map2;
                        strSubstring4 = strFindValue.substring(0, 3);
                        strSubstring3 = strFindAIvalue.substring(3);
                        map2 = map;
                        i = length;
                        break;
                    } else {
                        break;
                    }
                    break;
                default:
                    map2.put(strFindAIvalue, strFindValue);
                    i = length;
                    break;
            }
            return null;
        }
        return new ExpandedProductParsedResult(massagedText, str, str2, str3, str4, str5, str6, str7, str8, str9, strSubstring, strSubstring2, strSubstring3, strSubstring4, map2);
    }

    private static String findAIvalue(int i, String str) {
        if (str.charAt(i) != '(') {
            return null;
        }
        String strSubstring = str.substring(i + 1);
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < strSubstring.length(); i2++) {
            char cCharAt = strSubstring.charAt(i2);
            if (cCharAt == ')') {
                return sb.toString();
            }
            if (cCharAt < '0' || cCharAt > '9') {
                return null;
            }
            sb.append(cCharAt);
        }
        return sb.toString();
    }

    private static String findValue(int i, String str) {
        StringBuilder sb = new StringBuilder();
        String strSubstring = str.substring(i);
        for (int i2 = 0; i2 < strSubstring.length(); i2++) {
            char cCharAt = strSubstring.charAt(i2);
            if (cCharAt == '(') {
                if (findAIvalue(i2, strSubstring) != null) {
                    break;
                }
                sb.append('(');
            } else {
                sb.append(cCharAt);
            }
        }
        return sb.toString();
    }
}
