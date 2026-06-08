package com.gddai.lioncheck.sdk;

/* JADX INFO: loaded from: classes.dex */
public abstract class RequestListener<T> {
    private String faileMsg;
    private boolean isLoading;
    private String loadingMsg;

    public void cancel() {
    }

    public void complete() {
    }

    public String failedMsg() {
        return null;
    }

    public String loadingMsg() {
        return null;
    }

    void onProgressUpdate() {
    }

    public void onStart() {
    }

    public abstract void onSuccess(T t);

    public RequestListener() {
        this.isLoading = true;
    }

    public RequestListener(boolean z) {
        this.isLoading = z;
    }

    public RequestListener(String str) {
        this.isLoading = true;
        this.faileMsg = str;
    }

    public boolean isLoading() {
        return this.isLoading;
    }
}
