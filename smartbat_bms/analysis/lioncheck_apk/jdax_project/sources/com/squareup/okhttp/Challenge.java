package com.squareup.okhttp;

import com.squareup.okhttp.internal.Util;

/* JADX INFO: loaded from: classes.dex */
public final class Challenge {
    private final String realm;
    private final String scheme;

    public Challenge(String str, String str2) {
        this.scheme = str;
        this.realm = str2;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getRealm() {
        return this.realm;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Challenge)) {
            return false;
        }
        Challenge challenge = (Challenge) obj;
        return Util.equal(this.scheme, challenge.scheme) && Util.equal(this.realm, challenge.realm);
    }

    public int hashCode() {
        String str = this.realm;
        int iHashCode = (899 + (str != null ? str.hashCode() : 0)) * 31;
        String str2 = this.scheme;
        return iHashCode + (str2 != null ? str2.hashCode() : 0);
    }

    public String toString() {
        return this.scheme + " realm=\"" + this.realm + "\"";
    }
}
