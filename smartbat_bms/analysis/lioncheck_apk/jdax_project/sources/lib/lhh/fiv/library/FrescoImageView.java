package lib.lhh.fiv.library;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.Postprocessor;

/* JADX INFO: loaded from: classes.dex */
public class FrescoImageView extends SimpleDraweeView implements FrescoController, BaseFrescoImageView {
    private boolean mAnim;
    private DraweeController mController;
    private ControllerListener mControllerListener;
    private int mDefaultResID;
    private ImageRequest mLowResRequest;
    private String mLowThumbnailUrl;
    private Postprocessor mPostProcessor;
    private ImageRequest mRequest;
    private boolean mTapToRetry;
    private String mThumbnailPath;
    private String mThumbnailUrl;

    public FrescoImageView(Context context, GenericDraweeHierarchy genericDraweeHierarchy) {
        super(context, genericDraweeHierarchy);
        this.mThumbnailUrl = null;
        this.mLowThumbnailUrl = null;
        this.mDefaultResID = 0;
        this.mAnim = true;
        this.mThumbnailPath = null;
        this.mTapToRetry = false;
    }

    public FrescoImageView(Context context) {
        super(context);
        this.mThumbnailUrl = null;
        this.mLowThumbnailUrl = null;
        this.mDefaultResID = 0;
        this.mAnim = true;
        this.mThumbnailPath = null;
        this.mTapToRetry = false;
    }

    public FrescoImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mThumbnailUrl = null;
        this.mLowThumbnailUrl = null;
        this.mDefaultResID = 0;
        this.mAnim = true;
        this.mThumbnailPath = null;
        this.mTapToRetry = false;
    }

    public FrescoImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mThumbnailUrl = null;
        this.mLowThumbnailUrl = null;
        this.mDefaultResID = 0;
        this.mAnim = true;
        this.mThumbnailPath = null;
        this.mTapToRetry = false;
    }

    private void setResourceController() {
        this.mRequest = FrescoFactory.buildImageRequestWithResource(this);
        DraweeController draweeControllerBuildDraweeController = FrescoFactory.buildDraweeController(this);
        this.mController = draweeControllerBuildDraweeController;
        setController(draweeControllerBuildDraweeController);
    }

    private void setSourceController() {
        this.mRequest = FrescoFactory.buildImageRequestWithSource(this);
        this.mLowResRequest = FrescoFactory.buildLowImageRequest(this);
        DraweeController draweeControllerBuildDraweeController = FrescoFactory.buildDraweeController(this);
        this.mController = draweeControllerBuildDraweeController;
        setController(draweeControllerBuildDraweeController);
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public int getDefaultResID() {
        return this.mDefaultResID;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void loadView(String str, String str2, int i) {
        try {
            this.mThumbnailPath = null;
            this.mThumbnailUrl = str2;
            this.mLowThumbnailUrl = str2;
            this.mDefaultResID = i;
            if (!TextUtils.isEmpty(str2) && (this.mThumbnailUrl.startsWith(FrescoController.HTTP_PERFIX) || this.mThumbnailUrl.startsWith(FrescoController.HTTPS_PERFIX))) {
                getHierarchy().setPlaceholderImage(i);
                setSourceController();
            } else {
                getHierarchy().setPlaceholderImage(i);
                setResourceController();
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void loadView(String str, int i) {
        loadView(null, str, i);
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void loadLocalImage(String str, int i) {
        try {
            this.mThumbnailPath = str;
            this.mDefaultResID = i;
            this.mThumbnailUrl = null;
            this.mLowThumbnailUrl = null;
            getHierarchy().setPlaceholderImage(this.mDefaultResID);
            if (TextUtils.isEmpty(this.mThumbnailPath)) {
                setResourceController();
                return;
            }
            if (!this.mThumbnailPath.startsWith(FrescoController.FILE_PERFIX)) {
                this.mThumbnailPath = FrescoController.FILE_PERFIX + this.mThumbnailPath;
            }
            setSourceController();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public Postprocessor getPostProcessor() {
        return this.mPostProcessor;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setPostProcessor(Postprocessor postprocessor) {
        this.mPostProcessor = postprocessor;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public String getThumbnailUrl() {
        return this.mThumbnailUrl;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public String getLowThumbnailUrl() {
        return this.mLowThumbnailUrl;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public String getThumbnailPath() {
        return this.mThumbnailPath;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public boolean getTapToRetryEnabled() {
        return this.mTapToRetry;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void asCircle() {
        setRoundingParmas(getRoundingParams().setRoundAsCircle(true));
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setBorder(int i, float f) {
        setRoundingParmas(getRoundingParams().setBorder(i, f));
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void clearRoundingParams() {
        setRoundingParmas(null);
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public ControllerListener getControllerListener() {
        return this.mControllerListener;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public DraweeController getDraweeController() {
        return getController();
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public ImageRequest getLowImageRequest() {
        return this.mLowResRequest;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public ImageRequest getImageRequest() {
        return this.mRequest;
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public RoundingParams getRoundingParams() {
        RoundingParams roundingParams = getHierarchy().getRoundingParams();
        return roundingParams == null ? new RoundingParams() : roundingParams;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setRoundingParmas(RoundingParams roundingParams) {
        getHierarchy().setRoundingParams(roundingParams);
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setControllerListener(ControllerListener controllerListener) {
        this.mControllerListener = controllerListener;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setCircle(int i) {
        setRoundingParmas(getRoundingParams().setRoundAsCircle(true).setRoundingMethod(RoundingParams.RoundingMethod.OVERLAY_COLOR).setOverlayColor(i));
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setCornerRadius(float f) {
        setRoundingParmas(getRoundingParams().setCornersRadius(f));
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setCornerRadius(float f, int i) {
        setRoundingParmas(getRoundingParams().setCornersRadius(f).setRoundingMethod(RoundingParams.RoundingMethod.OVERLAY_COLOR).setOverlayColor(i));
    }

    @Override // lib.lhh.fiv.library.BaseFrescoImageView
    public boolean isAnim() {
        return this.mAnim;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setAnim(boolean z) {
        this.mAnim = z;
    }

    @Override // lib.lhh.fiv.library.FrescoController
    public void setTapToRetryEnabled(boolean z) {
        this.mTapToRetry = z;
    }
}
