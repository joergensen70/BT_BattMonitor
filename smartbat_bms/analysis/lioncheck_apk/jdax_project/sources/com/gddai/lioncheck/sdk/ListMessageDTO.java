package com.gddai.lioncheck.sdk;

import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ListMessageDTO<E> extends MessageDTO {
    private static final long serialVersionUID = 3821068611680729123L;
    private List<E> list;

    public List<E> getList() {
        return this.list;
    }

    public void setList(List<E> list) {
        this.list = list;
    }
}
