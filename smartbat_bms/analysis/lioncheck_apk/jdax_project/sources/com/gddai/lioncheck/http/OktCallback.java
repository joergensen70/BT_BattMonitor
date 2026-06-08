package com.gddai.lioncheck.http;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.sdk.MessageDTO;
import com.gddai.lioncheck.sdk.RequestListener;
import com.lidroid.xutils.util.LogUtils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.ys.module.dialog.LoadingDialog;
import com.ys.module.toast.ToastTool;
import com.ys.module.utils.StringUtils;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public abstract class OktCallback implements Callback {
    public static final int ERROR = 1002;
    public static final int SHOW_INFO = 1003;
    public static final int SUCCESS = 1001;
    public static ObjectMapper mMapper;
    private RequestListener mCallBack;
    protected Context mContext;
    private String mFaileMsg;
    private Handler mHandler = new Handler() { // from class: com.gddai.lioncheck.http.OktCallback.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            ReturnCodeType returnCodeType;
            MessageDTO messageDTO;
            super.handleMessage(message);
            if (message.obj instanceof Bundle) {
                Bundle bundle = (Bundle) message.obj;
                returnCodeType = (ReturnCodeType) bundle.getSerializable("type");
                messageDTO = (MessageDTO) bundle.getSerializable("dto");
            } else {
                returnCodeType = null;
                messageDTO = null;
            }
            switch (message.what) {
                case 1001:
                    OktCallback.this.mCallBack.onSuccess(messageDTO);
                    break;
                case 1002:
                    if (!StringUtils.isEmpty(message.obj.toString())) {
                        ToastTool.showNormalShort(OktCallback.this.mContext, message.obj.toString());
                    }
                    break;
                case 1003:
                    OktCallback.this.responseInfo(returnCodeType, messageDTO);
                    break;
            }
        }
    };
    private JavaType mJavaType;
    private LoadingDialog mLoadingDialog;
    private String mLoadingMsg;

    public abstract void responseInfo(ReturnCodeType returnCodeType, MessageDTO messageDTO);

    /* JADX WARN: Multi-variable type inference failed */
    public OktCallback(Context context, RequestListener requestListener, Class... clsArr) {
        this.mContext = context;
        this.mCallBack = requestListener;
        if (clsArr.length == 1) {
            this.mJavaType = getMapper().getTypeFactory().constructType(clsArr[0]);
        } else {
            this.mJavaType = getMapper().getTypeFactory().constructParametricType((Class<?>) clsArr[0], (Class<?>[]) new Class[]{clsArr[1]});
        }
        onStart();
    }

    public static ObjectMapper getMapper() {
        if (mMapper == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            mMapper = objectMapper;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mMapper;
    }

    @Override // com.squareup.okhttp.Callback
    public void onResponse(Response response) {
        try {
            if (this.mCallBack != null) {
                try {
                    try {
                        String strString = response.body().string();
                        LogUtils.e(strString);
                        MessageDTO messageDTO = (MessageDTO) getMapper().readValue(strString, this.mJavaType);
                        ReturnCodeType type = ReturnCodeType.getType(messageDTO.getReturnCode());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("type", type);
                        bundle.putSerializable("dto", messageDTO);
                        if (type == ReturnCodeType.SUCCEE) {
                            this.mHandler.obtainMessage(1001, bundle).sendToTarget();
                        } else {
                            this.mHandler.obtainMessage(1003, bundle).sendToTarget();
                        }
                        complate();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                        LogUtils.e("解析错误");
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        LogUtils.e("解析错误");
                    }
                } catch (JsonParseException e3) {
                    e3.printStackTrace();
                    LogUtils.e("解析错误");
                }
            }
        } finally {
            complate();
        }
    }

    @Override // com.squareup.okhttp.Callback
    public void onFailure(Request request, IOException iOException) {
        complate();
        String string = !StringUtils.isEmpty(this.mFaileMsg) ? this.mFaileMsg : null;
        if (iOException != null) {
            string = this.mContext.getString(R.string.net_conn_fail);
        }
        this.mHandler.obtainMessage(1002, string).sendToTarget();
        iOException.printStackTrace();
    }

    public void onStart() {
        RequestListener requestListener = this.mCallBack;
        if (requestListener != null) {
            this.mFaileMsg = requestListener.failedMsg();
            this.mLoadingMsg = this.mCallBack.loadingMsg();
            if (this.mCallBack.isLoading()) {
                showLoading();
            }
        }
    }

    private void cancelDialog() {
        LoadingDialog loadingDialog = this.mLoadingDialog;
        if (loadingDialog != null) {
            loadingDialog.cancel();
            this.mLoadingDialog = null;
        }
    }

    private void showLoading() {
        if (this.mLoadingDialog == null) {
            this.mLoadingDialog = new LoadingDialog(this.mContext);
        }
        this.mLoadingDialog.show(this.mLoadingMsg);
    }

    private void complate() {
        cancelDialog();
        this.mCallBack.complete();
    }
}
