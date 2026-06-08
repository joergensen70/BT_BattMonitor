package com.gddai.lioncheck.entity;

import com.gddai.lioncheck.entity.base.IEntity;
import com.lidroid.xutils.db.annotation.Column;

/* JADX INFO: loaded from: classes.dex */
public class HistoryEntity extends IEntity {

    @Column(column = "_address")
    private String address;

    @Column(column = "_date")
    private String date;

    @Column(column = "_lat")
    private double lat;

    @Column(column = "_lot")
    private double lot;

    @Column(column = "_userId")
    private String userId;

    @Override // java.lang.Comparable
    public int compareTo(IEntity iEntity) {
        return 0;
    }

    public HistoryEntity() {
    }

    public HistoryEntity(String str, String str2, String str3, double d, double d2) {
        this.userId = str;
        this.address = str3;
        this.date = str2;
        this.lat = d;
        this.lot = d2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String str) {
        this.address = str;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String str) {
        this.date = str;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double d) {
        this.lat = d;
    }

    public double getLot() {
        return this.lot;
    }

    public void setLot(double d) {
        this.lot = d;
    }
}
