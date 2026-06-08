package com.gddai.lioncheck.http;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public enum ReturnCodeType implements Serializable {
    SUCCEE("0000"),
    TOKEN("0002"),
    RANGEFAIL("0101"),
    FAIL("9999"),
    RECOIDERROR("0001"),
    PSWDERROR("0003"),
    NOTEXIST("0014"),
    EXIST("0015"),
    ILLEGAL("9999"),
    FINSH("6666"),
    DIMISSION("0018"),
    RESIGNIN("9995"),
    UNKNOW("1111");

    private String nCode;

    ReturnCodeType(String str) {
        this.nCode = str;
    }

    @Override // java.lang.Enum
    public String toString() {
        return String.valueOf(this.nCode);
    }

    public static ReturnCodeType getType(String str) {
        str.hashCode();
        switch (str) {
            case "0000":
                return SUCCEE;
            case "0001":
                return RECOIDERROR;
            case "0002":
                return TOKEN;
            case "0003":
                return PSWDERROR;
            case "0014":
                return NOTEXIST;
            case "0015":
                return EXIST;
            case "0018":
                return DIMISSION;
            case "0101":
                return RANGEFAIL;
            case "6666":
                return FINSH;
            case "9995":
                return RESIGNIN;
            case "9999":
                return FAIL;
            default:
                return UNKNOW;
        }
    }
}
