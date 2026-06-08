package com.gddai.lioncheck.http;

import android.widget.Toast;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.sdk.MessageDTO;
import com.gddai.lioncheck.sdk.RequestListener;
import com.ys.module.toast.ToastTool;

/* JADX INFO: loaded from: classes.dex */
public class ResponseInfo extends OktCallback {
    public ResponseInfo(BaseActivity baseActivity, RequestListener requestListener, Class<?>... clsArr) {
        super(baseActivity, requestListener, clsArr);
    }

    /* JADX INFO: renamed from: com.gddai.lioncheck.http.ResponseInfo$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType;

        static {
            int[] iArr = new int[ReturnCodeType.values().length];
            $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType = iArr;
            try {
                iArr[ReturnCodeType.PSWDERROR.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.FAIL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.RECOIDERROR.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.NOTEXIST.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.EXIST.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.DIMISSION.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.RESIGNIN.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.TOKEN.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[ReturnCodeType.FINSH.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
        }
    }

    @Override // com.gddai.lioncheck.http.OktCallback
    public void responseInfo(ReturnCodeType returnCodeType, MessageDTO messageDTO) {
        switch (AnonymousClass1.$SwitchMap$com$gddai$lioncheck$http$ReturnCodeType[returnCodeType.ordinal()]) {
            case 1:
                Toast.makeText(this.mContext, R.string.return_pswd_error_error, 0).show();
                break;
            case 2:
                Toast.makeText(this.mContext, R.string.return_fail_text, 0).show();
                break;
            case 3:
                Toast.makeText(this.mContext, R.string.return_code_text, 0).show();
                break;
            case 4:
                Toast.makeText(this.mContext, R.string.return_not_exist_text, 0).show();
                break;
            case 5:
                Toast.makeText(this.mContext, R.string.return_exist_text, 0).show();
                break;
            case 6:
                boolean z = this.mContext instanceof BaseActivity;
                break;
            case 7:
                ToastTool.showNormalShort(this.mContext, messageDTO.getReturnMessage());
                break;
            case 8:
                boolean z2 = this.mContext instanceof BaseActivity;
                break;
            case 9:
                break;
            default:
                ToastTool.showNormalShort(this.mContext, messageDTO.getReturnMessage());
                break;
        }
    }
}
