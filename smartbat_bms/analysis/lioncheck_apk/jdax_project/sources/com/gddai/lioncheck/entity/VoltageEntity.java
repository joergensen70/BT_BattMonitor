package com.gddai.lioncheck.entity;

/* JADX INFO: loaded from: classes.dex */
public class VoltageEntity {
    private int batterySerial;
    private long batteryVolatage;

    public VoltageEntity(int i, long j) {
        this.batterySerial = i;
        this.batteryVolatage = j;
    }

    public int getBatterySerial() {
        return this.batterySerial;
    }

    public void setBatterySerial(int i) {
        this.batterySerial = i;
    }

    public long getBatteryVolatage() {
        return this.batteryVolatage;
    }

    public void setBatteryVolatage(long j) {
        this.batteryVolatage = j;
    }
}
