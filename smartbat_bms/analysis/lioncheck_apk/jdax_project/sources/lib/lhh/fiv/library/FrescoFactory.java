package lib.lhh.fiv.library;

import android.net.Uri;
import android.text.TextUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/* JADX INFO: loaded from: classes.dex */
public class FrescoFactory {
    public static DraweeController buildDraweeController(BaseFrescoImageView baseFrescoImageView) {
        return Fresco.newDraweeControllerBuilder().setImageRequest(baseFrescoImageView.getImageRequest()).setAutoPlayAnimations(baseFrescoImageView.isAnim()).setTapToRetryEnabled(baseFrescoImageView.getTapToRetryEnabled()).setLowResImageRequest(baseFrescoImageView.getLowImageRequest()).setControllerListener(baseFrescoImageView.getControllerListener()).setOldController(baseFrescoImageView.getDraweeController()).build();
    }

    public static ImageRequest buildImageRequestWithResource(BaseFrescoImageView baseFrescoImageView) {
        return ImageRequestBuilder.newBuilderWithResourceId(baseFrescoImageView.getDefaultResID()).setPostprocessor(baseFrescoImageView.getPostProcessor()).setLocalThumbnailPreviewsEnabled(true).build();
    }

    public static ImageRequest buildImageRequestWithSource(BaseFrescoImageView baseFrescoImageView) {
        String thumbnailUrl;
        if (TextUtils.isEmpty(baseFrescoImageView.getThumbnailUrl())) {
            thumbnailUrl = baseFrescoImageView.getThumbnailPath();
        } else {
            thumbnailUrl = baseFrescoImageView.getThumbnailUrl();
        }
        return ImageRequestBuilder.newBuilderWithSource(Uri.parse(thumbnailUrl)).setPostprocessor(baseFrescoImageView.getPostProcessor()).setLocalThumbnailPreviewsEnabled(true).build();
    }

    public static ImageRequest buildLowImageRequest(BaseFrescoImageView baseFrescoImageView) {
        if (TextUtils.isEmpty(baseFrescoImageView.getLowThumbnailUrl())) {
            return null;
        }
        return ImageRequest.fromUri(Uri.parse(baseFrescoImageView.getLowThumbnailUrl()));
    }
}
