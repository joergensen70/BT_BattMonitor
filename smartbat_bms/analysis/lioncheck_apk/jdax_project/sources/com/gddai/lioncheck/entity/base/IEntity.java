package com.gddai.lioncheck.entity.base;

import com.lidroid.xutils.db.annotation.Id;
import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public abstract class IEntity implements Comparable<IEntity>, Serializable {

    @Id(column = "_id")
    private int id;

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }
}
