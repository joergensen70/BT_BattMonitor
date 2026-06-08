package com.gddai.lioncheck.sdk;

/* JADX INFO: loaded from: classes.dex */
public enum VcodeType {
    LOGIN(10),
    REST_PWD_MOBILE(10),
    REST_PWD_EMAIL(10),
    UPDATE_CONF(10),
    BIND_MOBILE(10),
    BIND_EMAIL(120),
    REG_MOBILE(10),
    REG_EMAIL(120),
    REG_CAPTCHA(5);

    private int expire;

    VcodeType(int i) {
        this.expire = i;
    }

    public int getExpire() {
        return this.expire;
    }

    public void setExpire(int i) {
        this.expire = i;
    }
}
