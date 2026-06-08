package com.gddai.lioncheck.entity;

import com.squareup.okhttp.internal.Platform;

/* JADX INFO: loaded from: classes.dex */
public class ShareEntity {
    private String content;
    private Platform pf;
    private String title;
    private String titleUrl;
    private String url;

    public Platform getPf() {
        return this.pf;
    }

    public void setPf(Platform platform) {
        this.pf = platform;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String str) {
        this.content = str;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public String getTitleUrl() {
        return this.titleUrl;
    }

    public void setTitleUrl(String str) {
        this.titleUrl = str;
    }
}
