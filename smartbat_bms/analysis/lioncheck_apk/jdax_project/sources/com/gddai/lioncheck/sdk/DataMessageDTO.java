package com.gddai.lioncheck.sdk;

/* JADX INFO: loaded from: classes.dex */
public class DataMessageDTO<E> extends MessageDTO {
    private static final long serialVersionUID = 7499049188052671251L;
    private E data;

    public E getData() {
        return this.data;
    }

    public void setData(E e) {
        this.data = e;
    }
}
