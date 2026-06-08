package com.squareup.okhttp;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public interface Interceptor {

    public interface Chain {
        Connection connection();

        Response proceed(Request request) throws IOException;

        Request request();
    }

    Response intercept(Chain chain) throws IOException;
}
