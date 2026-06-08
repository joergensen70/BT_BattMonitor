package com.facebook.imagepipeline.common;

import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH;

    public static Priority getHigherPriority(@Nullable Priority priority, @Nullable Priority priority2) {
        return (priority != null && (priority2 == null || priority.ordinal() > priority2.ordinal())) ? priority : priority2;
    }
}
