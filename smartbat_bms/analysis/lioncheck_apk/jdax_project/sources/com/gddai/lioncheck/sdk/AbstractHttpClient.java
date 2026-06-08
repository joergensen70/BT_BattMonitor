package com.gddai.lioncheck.sdk;

import com.facebook.common.util.UriUtil;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import java.io.File;

/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractHttpClient {
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public abstract void get(String str, FormEncodingBuilder formEncodingBuilder, RequestListener<?> requestListener, Class<?>... clsArr);

    public abstract void post(String str, FormEncodingBuilder formEncodingBuilder, RequestListener<?> requestListener, Class<?>... clsArr);

    public abstract void post(String str, MultipartBuilder multipartBuilder, RequestListener<?> requestListener, Class<?>... clsArr);

    public void curApp(String str, RequestListener<UpdatedVersionDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "appId", "WewBar");
        post("/comm/curApp", formEncodingBuilder, requestListener, UpdatedVersionDTO.class);
    }

    public void feedback(String str, String str2, FeedbackType feedbackType, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, UriUtil.LOCAL_CONTENT_SCHEME, str);
        addParam(formEncodingBuilder, "contact", str2);
        addParam(formEncodingBuilder, "ftype", feedbackType);
        post("/comm/feedback", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void bindDevice(String str, String str2, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "deviceId", str);
        addParam(formEncodingBuilder, "provider", str2);
        post("/comm/bindDevice", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void register(String str, String str2, AccountType accountType, String str3, VcodeType vcodeType, String str4, RequestListener<DataMessageDTO<LoginUsrInfo>> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "deviceId", str);
        addParam(formEncodingBuilder, "loginName", str2);
        addParam(formEncodingBuilder, "accountType", accountType.name());
        addParam(formEncodingBuilder, "password", str3);
        addParam(formEncodingBuilder, "vcodeType", vcodeType.name());
        addParam(formEncodingBuilder, "vcode", str4);
        post("/user/register", formEncodingBuilder, requestListener, DataMessageDTO.class, LoginUsrInfo.class);
    }

    public void usrInfo(RequestListener<DataMessageDTO<LoginUsrInfo>> requestListener) {
        post("/user/usrInfo", new FormEncodingBuilder(), requestListener, DataMessageDTO.class, LoginUsrInfo.class);
    }

    public void loginVcode(String str, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "mobile", str);
        post("/user/loginVcode", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void bindMobileVcode(String str, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "mobile", str);
        post("/user/bindMobileVcode", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void regMobileVcode(String str, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "mobile", str);
        post("/user/regMobileVcode", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void restPwdMobileVcode(String str, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "mobile", str);
        post("/user/restPwdMobileVcode", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void socialLogin(String str, AccountType accountType, String str2, String str3, String str4, RequestListener<DataMessageDTO<LoginUsrInfo>> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "proId", str);
        addParam(formEncodingBuilder, "accountType", accountType.name());
        addParam(formEncodingBuilder, "socialId", str2);
        addParam(formEncodingBuilder, "nickName", str3);
        addParam(formEncodingBuilder, "avatar", str4);
        post("/user/socialLogin", formEncodingBuilder, requestListener, DataMessageDTO.class, LoginUsrInfo.class);
    }

    public void login(String str, String str2, String str3, RequestListener<DataMessageDTO<LoginUsrInfo>> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "deviceId", str);
        addParam(formEncodingBuilder, "loginName", str2);
        addParam(formEncodingBuilder, "password", str3);
        post("/user/login", formEncodingBuilder, requestListener, DataMessageDTO.class, LoginUsrInfo.class);
    }

    public void otpLogin(String str, String str2, String str3, RequestListener<DataMessageDTO<LoginUsrInfo>> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "deviceId", str);
        addParam(formEncodingBuilder, "mobile", str2);
        addParam(formEncodingBuilder, "vcode", str3);
        post("/user/otpLogin", formEncodingBuilder, requestListener, DataMessageDTO.class, LoginUsrInfo.class);
    }

    public void logout(RequestListener<MessageDTO> requestListener) {
        post("/user/logout", new FormEncodingBuilder(), requestListener, MessageDTO.class);
    }

    public void refreshToken(RequestListener<DataMessageDTO<LoginUsrInfo>> requestListener) {
        post("/user/refreshToken", new FormEncodingBuilder(), requestListener, DataMessageDTO.class, LoginUsrInfo.class);
    }

    public void updateUsrInfo(String str, String str2, Genders genders, String str3, RequestListener<DataMessageDTO<MessageDTO>> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "nickName", str);
        addParam(formEncodingBuilder, "avatar", str2);
        addParam(formEncodingBuilder, "gender", genders);
        addParam(formEncodingBuilder, "birthday", str3);
        post("/user/updateUsrInfo", formEncodingBuilder, requestListener, DataMessageDTO.class, MessageDTO.class);
    }

    public void upload(ResourceBusiness resourceBusiness, ResourceType resourceType, String str, RequestListener<ListMessageDTO<ResourceDTO>> requestListener) {
        File file = new File(str);
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM);
        multipartBuilder.addFormDataPart("business", resourceBusiness.name()).addFormDataPart("type", resourceType.name()).addFormDataPart("fstream", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, new File(str)));
        post("/comm/upload", multipartBuilder, requestListener, ListMessageDTO.class, ResourceDTO.class);
    }

    public void updatePwd(String str, String str2, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "password", str);
        addParam(formEncodingBuilder, "newPwd", str2);
        post("/user/updatePwd", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void forgotPwd(String str, VcodeType vcodeType, String str2, String str3, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "loginName", str);
        addParam(formEncodingBuilder, "vtype", vcodeType);
        addParam(formEncodingBuilder, "vcode", str2);
        addParam(formEncodingBuilder, "password", str3);
        post("/user/forgotPwd", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void bindMobile(String str, String str2, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "vcode", str);
        addParam(formEncodingBuilder, "mobile", str2);
        post("/user/bindMobile", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    public void uploadLog(String str, RequestListener<MessageDTO> requestListener) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        addParam(formEncodingBuilder, "log", str);
        post("/admin/comm/saveLogger", formEncodingBuilder, requestListener, MessageDTO.class);
    }

    private void addParam(FormEncodingBuilder formEncodingBuilder, String str, Object obj) {
        if (obj != null) {
            formEncodingBuilder.add(str, String.valueOf(obj));
        }
    }
}
