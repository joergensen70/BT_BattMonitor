package lib.lhh.fiv.library;

import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.Postprocessor;

/* JADX INFO: loaded from: classes.dex */
public interface BaseFrescoImageView {
    ControllerListener getControllerListener();

    int getDefaultResID();

    DraweeController getDraweeController();

    ImageRequest getImageRequest();

    ImageRequest getLowImageRequest();

    String getLowThumbnailUrl();

    Postprocessor getPostProcessor();

    RoundingParams getRoundingParams();

    boolean getTapToRetryEnabled();

    String getThumbnailPath();

    String getThumbnailUrl();

    boolean isAnim();
}
