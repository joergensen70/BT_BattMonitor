package com.gddai.lioncheck.http;

import android.content.Context;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.sdk.AbstractHttpClient;
import com.gddai.lioncheck.sdk.LoginUsrInfo;
import com.gddai.lioncheck.sdk.RequestListener;
import com.gddai.lioncheck.sharedpreferences.SharePreferenceUser;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.ys.module.Config;

/* JADX INFO: loaded from: classes.dex */
public class OkHttp extends AbstractHttpClient {
    private static OkHttpClient sHttp;
    protected Context context;
    protected BaseActivity mContext;

    @Override // com.gddai.lioncheck.sdk.AbstractHttpClient
    public void get(String str, FormEncodingBuilder formEncodingBuilder, RequestListener<?> requestListener, Class<?>... clsArr) {
    }

    public OkHttp(BaseActivity baseActivity) {
        this.mContext = baseActivity;
    }

    public OkHttp(Context context) {
    }

    @Override // com.gddai.lioncheck.sdk.AbstractHttpClient
    public void post(String str, FormEncodingBuilder formEncodingBuilder, RequestListener<?> requestListener, Class<?>... clsArr) {
        LoginUsrInfo shareMember = SharePreferenceUser.readShareMember(this.mContext);
        if (shareMember != null) {
            formEncodingBuilder.add("token", shareMember.getToken());
        }
        post(str, formEncodingBuilder.build(), requestListener, clsArr);
    }

    @Override // com.gddai.lioncheck.sdk.AbstractHttpClient
    public void post(String str, MultipartBuilder multipartBuilder, RequestListener<?> requestListener, Class<?>... clsArr) {
        LoginUsrInfo shareMember = SharePreferenceUser.readShareMember(this.mContext);
        if (shareMember != null) {
            multipartBuilder.addFormDataPart("token", shareMember.getToken());
        }
        post(str, multipartBuilder.build(), requestListener, clsArr);
    }

    public void post(String str, RequestBody requestBody, RequestListener<?> requestListener, Class<?>... clsArr) {
        getHttp().newCall(new Request.Builder().url(Config.URL_ROOT + str).post(requestBody).build()).enqueue(new ResponseInfo(this.mContext, requestListener, clsArr));
    }

    public static OkHttpClient getHttp() {
        if (sHttp == null) {
            sHttp = new OkHttpClient();
        }
        return sHttp;
    }
}
