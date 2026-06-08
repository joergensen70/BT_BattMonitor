package com.lidroid.xutils.view;

/* JADX INFO: loaded from: classes.dex */
public class ViewInjectInfo {
    public int parentId;
    public Object value;

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ViewInjectInfo)) {
            return false;
        }
        ViewInjectInfo viewInjectInfo = (ViewInjectInfo) obj;
        if (this.parentId != viewInjectInfo.parentId) {
            return false;
        }
        Object obj2 = this.value;
        if (obj2 == null) {
            return viewInjectInfo.value == null;
        }
        return obj2.equals(viewInjectInfo.value);
    }

    public int hashCode() {
        return (this.value.hashCode() * 31) + this.parentId;
    }
}
