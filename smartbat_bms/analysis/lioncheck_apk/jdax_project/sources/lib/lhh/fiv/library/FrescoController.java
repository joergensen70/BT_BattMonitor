package lib.lhh.fiv.library;

import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.imagepipeline.request.Postprocessor;

/* JADX INFO: loaded from: classes.dex */
public interface FrescoController {
    public static final String FILE_PERFIX = "file://";
    public static final String HTTPS_PERFIX = "https://";
    public static final String HTTP_PERFIX = "http://";

    void asCircle();

    void clearRoundingParams();

    void loadLocalImage(String str, int i);

    void loadView(String str, int i);

    void loadView(String str, String str2, int i);

    void setAnim(boolean z);

    void setBorder(int i, float f);

    void setCircle(int i);

    void setControllerListener(ControllerListener controllerListener);

    void setCornerRadius(float f);

    void setCornerRadius(float f, int i);

    void setPostProcessor(Postprocessor postprocessor);

    void setRoundingParmas(RoundingParams roundingParams);

    void setTapToRetryEnabled(boolean z);
}
