package com.gddai.lioncheck.sdk;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class MessageDTO implements Serializable {
    private static final long serialVersionUID = -6179718895675023989L;
    private String returnCode;
    private String returnMessage;
    protected LoginUsrInfo usr;

    public <ID extends Serializable> ID getId() {
        return null;
    }

    public Integer getVersion() {
        return null;
    }

    public String getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(String str) {
        this.returnCode = str;
    }

    public String getReturnMessage() {
        return this.returnMessage;
    }

    public void setReturnMessage(String str) {
        this.returnMessage = str;
    }

    public Long getReqUsrId() {
        LoginUsrInfo loginUsrInfo = this.usr;
        if (loginUsrInfo == null) {
            return null;
        }
        return loginUsrInfo.getUsrid();
    }

    public Long getReqPriUsrId() {
        LoginUsrInfo loginUsrInfo = this.usr;
        if (loginUsrInfo == null || loginUsrInfo.isAdmin()) {
            return null;
        }
        return this.usr.getUsrid();
    }

    public UserRoles getReqUsrRole() {
        LoginUsrInfo loginUsrInfo = this.usr;
        if (loginUsrInfo == null) {
            return null;
        }
        return loginUsrInfo.getUsrRole();
    }

    public String getReqLoginName() {
        LoginUsrInfo loginUsrInfo = this.usr;
        if (loginUsrInfo == null) {
            return null;
        }
        return loginUsrInfo.getLoginName();
    }

    public LoginUsrInfo getReqUsrInfo() {
        return this.usr;
    }

    public void setReqUsrInfo(LoginUsrInfo loginUsrInfo) {
        this.usr = loginUsrInfo;
    }
}
