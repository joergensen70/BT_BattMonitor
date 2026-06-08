package com.fasterxml.jackson.databind.util;

import java.lang.annotation.Annotation;

/* JADX INFO: loaded from: classes.dex */
public interface Annotations {
    <A extends Annotation> A get(Class<A> cls);

    int size();
}
