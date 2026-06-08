package com.gddai.lioncheck.sdk;

/* JADX INFO: loaded from: classes.dex */
public class UpdatedVersionDTO extends MessageDTO {
    private static final long serialVersionUID = -6565687036413144695L;
    private String appId;
    private Boolean curVer;
    private Boolean forceVer;
    private String verContent;
    private String verName;
    private Integer verNo;
    private String verUrl;

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String str) {
        this.appId = str;
    }

    public Integer getVerNo() {
        return this.verNo;
    }

    public void setVerNo(Integer num) {
        this.verNo = num;
    }

    public String getVerName() {
        return this.verName;
    }

    public void setVerName(String str) {
        this.verName = str;
    }

    public String getVerContent() {
        return this.verContent;
    }

    public void setVerContent(String str) {
        this.verContent = str;
    }

    public String getVerUrl() {
        return this.verUrl;
    }

    public void setVerUrl(String str) {
        this.verUrl = str;
    }

    public Boolean getCurVer() {
        return this.curVer;
    }

    public void setCurVer(Boolean bool) {
        this.curVer = bool;
    }

    public Boolean getForceVer() {
        return this.forceVer;
    }

    public void setForceVer(Boolean bool) {
        this.forceVer = bool;
    }
}
