package com.facebook.drawee.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.facebook.drawee.R;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class GenericDraweeView extends DraweeView<GenericDraweeHierarchy> {
    public GenericDraweeView(Context context, GenericDraweeHierarchy genericDraweeHierarchy) {
        super(context);
        setHierarchy(genericDraweeHierarchy);
    }

    public GenericDraweeView(Context context) {
        super(context);
        inflateHierarchy(context, null);
    }

    public GenericDraweeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        inflateHierarchy(context, attributeSet);
    }

    public GenericDraweeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        inflateHierarchy(context, attributeSet);
    }

    public GenericDraweeView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        inflateHierarchy(context, attributeSet);
    }

    /* JADX WARN: Finally extract failed */
    private void inflateHierarchy(Context context, @Nullable AttributeSet attributeSet) {
        Resources resources;
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        int i;
        int i2;
        int i3;
        int resourceId;
        int resourceId2;
        boolean z5;
        int resourceId3;
        int resourceId4;
        int resourceId5;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int resourceId6;
        int i10;
        boolean z6;
        boolean z7;
        Resources resources2 = context.getResources();
        ScalingUtils.ScaleType scaleTypeFromXml = GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
        ScalingUtils.ScaleType scaleTypeFromXml2 = GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
        ScalingUtils.ScaleType scaleTypeFromXml3 = GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
        ScalingUtils.ScaleType scaleTypeFromXml4 = GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
        ScalingUtils.ScaleType scaleTypeFromXml5 = GenericDraweeHierarchyBuilder.DEFAULT_ACTUAL_IMAGE_SCALE_TYPE;
        int i11 = GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION;
        if (attributeSet != null) {
            TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.GenericDraweeView);
            try {
                int indexCount = typedArrayObtainStyledAttributes.getIndexCount();
                resources = resources2;
                boolean z8 = true;
                boolean z9 = true;
                z3 = true;
                z4 = true;
                int resourceId7 = 0;
                resourceId = 0;
                resourceId2 = 0;
                resourceId3 = 0;
                resourceId4 = 0;
                resourceId5 = 0;
                int i12 = 0;
                int integer = 0;
                int i13 = 0;
                boolean z10 = false;
                int dimensionPixelSize = 0;
                int color = 0;
                int dimensionPixelSize2 = 0;
                int color2 = 0;
                int dimensionPixelSize3 = 0;
                while (i12 < indexCount) {
                    int i14 = indexCount;
                    int index = typedArrayObtainStyledAttributes.getIndex(i12);
                    int i15 = i12;
                    if (index == R.styleable.GenericDraweeView_actualImageScaleType) {
                        scaleTypeFromXml5 = getScaleTypeFromXml(typedArrayObtainStyledAttributes, R.styleable.GenericDraweeView_actualImageScaleType, scaleTypeFromXml5);
                    } else if (index == R.styleable.GenericDraweeView_placeholderImage) {
                        resourceId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_placeholderImage, resourceId);
                    } else if (index == R.styleable.GenericDraweeView_pressedStateOverlayImage) {
                        resourceId2 = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_pressedStateOverlayImage, resourceId2);
                    } else if (index == R.styleable.GenericDraweeView_progressBarImage) {
                        resourceId4 = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_progressBarImage, resourceId4);
                    } else if (index == R.styleable.GenericDraweeView_fadeDuration) {
                        i11 = typedArrayObtainStyledAttributes.getInt(R.styleable.GenericDraweeView_fadeDuration, i11);
                    } else {
                        if (index == R.styleable.GenericDraweeView_viewAspectRatio) {
                            setAspectRatio(typedArrayObtainStyledAttributes.getFloat(R.styleable.GenericDraweeView_viewAspectRatio, getAspectRatio()));
                            resourceId6 = i13;
                            i10 = resourceId7;
                        } else if (index == R.styleable.GenericDraweeView_placeholderImageScaleType) {
                            scaleTypeFromXml = getScaleTypeFromXml(typedArrayObtainStyledAttributes, R.styleable.GenericDraweeView_placeholderImageScaleType, scaleTypeFromXml);
                        } else if (index == R.styleable.GenericDraweeView_retryImage) {
                            resourceId7 = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_retryImage, resourceId7);
                        } else if (index == R.styleable.GenericDraweeView_retryImageScaleType) {
                            scaleTypeFromXml2 = getScaleTypeFromXml(typedArrayObtainStyledAttributes, R.styleable.GenericDraweeView_retryImageScaleType, scaleTypeFromXml2);
                        } else if (index == R.styleable.GenericDraweeView_failureImage) {
                            resourceId5 = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_failureImage, resourceId5);
                        } else if (index == R.styleable.GenericDraweeView_failureImageScaleType) {
                            scaleTypeFromXml3 = getScaleTypeFromXml(typedArrayObtainStyledAttributes, R.styleable.GenericDraweeView_failureImageScaleType, scaleTypeFromXml3);
                        } else if (index == R.styleable.GenericDraweeView_progressBarImageScaleType) {
                            scaleTypeFromXml4 = getScaleTypeFromXml(typedArrayObtainStyledAttributes, R.styleable.GenericDraweeView_progressBarImageScaleType, scaleTypeFromXml4);
                        } else if (index == R.styleable.GenericDraweeView_progressBarAutoRotateInterval) {
                            integer = typedArrayObtainStyledAttributes.getInteger(R.styleable.GenericDraweeView_progressBarAutoRotateInterval, 0);
                        } else if (index == R.styleable.GenericDraweeView_backgroundImage) {
                            resourceId3 = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_backgroundImage, resourceId3);
                        } else {
                            if (index == R.styleable.GenericDraweeView_overlayImage) {
                                resourceId6 = typedArrayObtainStyledAttributes.getResourceId(R.styleable.GenericDraweeView_overlayImage, i13);
                                i10 = resourceId7;
                            } else {
                                resourceId6 = i13;
                                i10 = resourceId7;
                                if (index == R.styleable.GenericDraweeView_roundAsCircle) {
                                    z10 = typedArrayObtainStyledAttributes.getBoolean(R.styleable.GenericDraweeView_roundAsCircle, z10);
                                } else if (index == R.styleable.GenericDraweeView_roundedCornerRadius) {
                                    dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.GenericDraweeView_roundedCornerRadius, dimensionPixelSize);
                                } else {
                                    if (index == R.styleable.GenericDraweeView_roundTopLeft) {
                                        z7 = z9;
                                        z4 = typedArrayObtainStyledAttributes.getBoolean(R.styleable.GenericDraweeView_roundTopLeft, z4);
                                    } else if (index == R.styleable.GenericDraweeView_roundTopRight) {
                                        z7 = z9;
                                        z3 = typedArrayObtainStyledAttributes.getBoolean(R.styleable.GenericDraweeView_roundTopRight, z3);
                                    } else if (index == R.styleable.GenericDraweeView_roundBottomRight) {
                                        z7 = typedArrayObtainStyledAttributes.getBoolean(R.styleable.GenericDraweeView_roundBottomRight, z9);
                                    } else if (index == R.styleable.GenericDraweeView_roundBottomLeft) {
                                        z6 = typedArrayObtainStyledAttributes.getBoolean(R.styleable.GenericDraweeView_roundBottomLeft, z8);
                                        z7 = z9;
                                        z8 = z6;
                                        z9 = z7;
                                        resourceId7 = i10;
                                        indexCount = i14;
                                        i13 = resourceId6;
                                        i12 = i15 + 1;
                                    } else if (index == R.styleable.GenericDraweeView_roundWithOverlayColor) {
                                        color = typedArrayObtainStyledAttributes.getColor(R.styleable.GenericDraweeView_roundWithOverlayColor, color);
                                    } else if (index == R.styleable.GenericDraweeView_roundingBorderWidth) {
                                        dimensionPixelSize2 = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.GenericDraweeView_roundingBorderWidth, dimensionPixelSize2);
                                    } else if (index == R.styleable.GenericDraweeView_roundingBorderColor) {
                                        color2 = typedArrayObtainStyledAttributes.getColor(R.styleable.GenericDraweeView_roundingBorderColor, color2);
                                    } else if (index == R.styleable.GenericDraweeView_roundingBorderPadding) {
                                        dimensionPixelSize3 = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.GenericDraweeView_roundingBorderPadding, dimensionPixelSize3);
                                    }
                                    z6 = z8;
                                    z8 = z6;
                                    z9 = z7;
                                    resourceId7 = i10;
                                    indexCount = i14;
                                    i13 = resourceId6;
                                    i12 = i15 + 1;
                                }
                            }
                            z6 = z8;
                            z7 = z9;
                            z8 = z6;
                            z9 = z7;
                            resourceId7 = i10;
                            indexCount = i14;
                            i13 = resourceId6;
                            i12 = i15 + 1;
                        }
                        z6 = z8;
                        dimensionPixelSize3 = dimensionPixelSize3;
                        z7 = z9;
                        z8 = z6;
                        z9 = z7;
                        resourceId7 = i10;
                        indexCount = i14;
                        i13 = resourceId6;
                        i12 = i15 + 1;
                    }
                    z6 = z8;
                    resourceId6 = i13;
                    i10 = resourceId7;
                    z7 = z9;
                    z8 = z6;
                    z9 = z7;
                    resourceId7 = i10;
                    indexCount = i14;
                    i13 = resourceId6;
                    i12 = i15 + 1;
                }
                i4 = i13;
                int i16 = resourceId7;
                typedArrayObtainStyledAttributes.recycle();
                z = z9;
                i8 = dimensionPixelSize3;
                i3 = integer;
                i2 = i16;
                z5 = z10;
                i5 = color;
                i6 = dimensionPixelSize2;
                i7 = color2;
                z2 = z8;
                i = dimensionPixelSize;
            } catch (Throwable th) {
                typedArrayObtainStyledAttributes.recycle();
                throw th;
            }
        } else {
            resources = resources2;
            z = true;
            z2 = true;
            z3 = true;
            z4 = true;
            i = 0;
            i2 = 0;
            i3 = 0;
            resourceId = 0;
            resourceId2 = 0;
            z5 = false;
            resourceId3 = 0;
            resourceId4 = 0;
            resourceId5 = 0;
            i4 = 0;
            i5 = 0;
            i6 = 0;
            i7 = 0;
            i8 = 0;
        }
        boolean z11 = z5;
        Resources resources3 = resources;
        GenericDraweeHierarchyBuilder genericDraweeHierarchyBuilder = new GenericDraweeHierarchyBuilder(resources3);
        genericDraweeHierarchyBuilder.setFadeDuration(i11);
        if (resourceId > 0) {
            genericDraweeHierarchyBuilder.setPlaceholderImage(resources3.getDrawable(resourceId), scaleTypeFromXml);
        }
        if (i2 > 0) {
            genericDraweeHierarchyBuilder.setRetryImage(resources3.getDrawable(i2), scaleTypeFromXml2);
        }
        if (resourceId5 > 0) {
            genericDraweeHierarchyBuilder.setFailureImage(resources3.getDrawable(resourceId5), scaleTypeFromXml3);
        }
        if (resourceId4 > 0) {
            Drawable drawable = resources3.getDrawable(resourceId4);
            if (i3 > 0) {
                drawable = new AutoRotateDrawable(drawable, i3);
            }
            genericDraweeHierarchyBuilder.setProgressBarImage(drawable, scaleTypeFromXml4);
        }
        if (resourceId3 > 0) {
            genericDraweeHierarchyBuilder.setBackground(resources3.getDrawable(resourceId3));
        }
        if (i4 > 0) {
            genericDraweeHierarchyBuilder.setOverlay(resources3.getDrawable(i4));
        }
        if (resourceId2 > 0) {
            genericDraweeHierarchyBuilder.setPressedStateOverlay(getResources().getDrawable(resourceId2));
        }
        genericDraweeHierarchyBuilder.setActualImageScaleType(scaleTypeFromXml5);
        if (z11 || i > 0) {
            RoundingParams roundingParams = new RoundingParams();
            roundingParams.setRoundAsCircle(z11);
            if (i > 0) {
                int i17 = i;
                roundingParams.setCornersRadii(z4 ? i17 : 0.0f, z3 ? i17 : 0.0f, z ? i17 : 0.0f, z2 ? i17 : 0.0f);
            }
            int i18 = i5;
            if (i18 != 0) {
                roundingParams.setOverlayColor(i18);
            }
            int i19 = i7;
            if (i19 != 0 && (i9 = i6) > 0) {
                roundingParams.setBorder(i19, i9);
            }
            int i20 = i8;
            if (i20 != 0) {
                roundingParams.setPadding(i20);
            }
            genericDraweeHierarchyBuilder.setRoundingParams(roundingParams);
        }
        setHierarchy(genericDraweeHierarchyBuilder.build());
    }

    private static ScalingUtils.ScaleType getScaleTypeFromXml(TypedArray typedArray, int i, ScalingUtils.ScaleType scaleType) {
        int i2 = typedArray.getInt(i, -1);
        return i2 < 0 ? scaleType : ScalingUtils.ScaleType.values()[i2];
    }
}
