package com.gddai.lioncheck.entity;

import com.gddai.lioncheck.entity.base.IEntity;
import com.lidroid.xutils.db.annotation.Column;

/* JADX INFO: loaded from: classes.dex */
public class SocEntity extends IEntity {

    @Column(column = "_time")
    private long time;

    @Column(column = "_value")
    private float value;

    @Override // java.lang.Comparable
    public int compareTo(IEntity iEntity) {
        return 0;
    }

    public SocEntity() {
    }

    public SocEntity(long j, float f) {
        this();
        this.time = j;
        this.value = f;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long j) {
        this.time = j;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float f) {
        this.value = f;
    }
}
