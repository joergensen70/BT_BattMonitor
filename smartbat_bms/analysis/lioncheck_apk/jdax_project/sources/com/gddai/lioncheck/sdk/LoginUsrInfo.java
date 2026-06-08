package com.gddai.lioncheck.sdk;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class LoginUsrInfo implements Serializable {
    private static final long serialVersionUID = -1169500024324489806L;
    private AccountType accountType;
    private String avatar;
    private String birthday;
    private String deviceId;
    private String expireDate;
    private Genders gender;
    private String loginName;
    private LoginType loginType;
    private String mobile;
    private String nickName;
    private String token;
    private UserRoles usrRole;
    private Long usrid;

    public Long getUsrid() {
        return this.usrid;
    }

    public void setUsrid(Long l) {
        this.usrid = l;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String str) {
        this.nickName = str;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String str) {
        this.avatar = str;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String str) {
        this.token = str;
    }

    public UserRoles getUsrRole() {
        return this.usrRole;
    }

    public void setUsrRole(UserRoles userRoles) {
        this.usrRole = userRoles;
    }

    public Genders getGender() {
        return this.gender;
    }

    public void setGender(Genders genders) {
        this.gender = genders;
    }

    public AccountType getAccountType() {
        return this.accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public LoginType getLoginType() {
        return this.loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String str) {
        this.loginName = str;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String str) {
        this.mobile = str;
    }

    public boolean isMerchant() {
        UserRoles userRoles = this.usrRole;
        return userRoles != null && userRoles == UserRoles.MERCHANT;
    }

    public boolean isAdmin() {
        UserRoles userRoles = this.usrRole;
        if (userRoles != null) {
            return userRoles == UserRoles.ADMIN || this.usrRole == UserRoles.SUPERADMIN;
        }
        return false;
    }

    public boolean isMember() {
        UserRoles userRoles = this.usrRole;
        return userRoles == null || userRoles == UserRoles.MEMBER;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String str) {
        this.birthday = str;
    }

    public String getExpireDate() {
        return this.expireDate;
    }

    public void setExpireDate(String str) {
        this.expireDate = str;
    }
}
