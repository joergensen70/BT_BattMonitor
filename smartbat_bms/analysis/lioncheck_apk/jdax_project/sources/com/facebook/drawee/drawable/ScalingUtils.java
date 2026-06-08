package com.facebook.drawee.drawable;

import android.graphics.Matrix;
import android.graphics.Rect;

/* JADX INFO: loaded from: classes.dex */
public class ScalingUtils {

    public enum ScaleType {
        FIT_XY,
        FIT_START,
        FIT_CENTER,
        FIT_END,
        CENTER,
        CENTER_INSIDE,
        CENTER_CROP,
        FOCUS_CROP
    }

    public static Matrix getTransform(Matrix matrix, Rect rect, int i, int i2, float f, float f2, ScaleType scaleType) {
        float f3;
        float f4;
        float fMax;
        float fMax2;
        int iWidth = rect.width();
        float f5 = iWidth;
        float f6 = i;
        float f7 = f5 / f6;
        float fHeight = rect.height();
        float f8 = i2;
        float f9 = fHeight / f8;
        switch (AnonymousClass1.$SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[scaleType.ordinal()]) {
            case 1:
                float f10 = rect.left;
                float f11 = rect.top;
                matrix.setScale(f7, f9);
                matrix.postTranslate((int) (f10 + 0.5f), (int) (f11 + 0.5f));
                return matrix;
            case 2:
                float fMin = Math.min(f7, f9);
                float f12 = rect.left;
                float f13 = rect.top;
                matrix.setScale(fMin, fMin);
                matrix.postTranslate((int) (f12 + 0.5f), (int) (f13 + 0.5f));
                return matrix;
            case 3:
                float fMin2 = Math.min(f7, f9);
                float f14 = rect.left + ((f5 - (f6 * fMin2)) * 0.5f);
                float f15 = rect.top + ((fHeight - (f8 * fMin2)) * 0.5f);
                matrix.setScale(fMin2, fMin2);
                matrix.postTranslate((int) (f14 + 0.5f), (int) (f15 + 0.5f));
                return matrix;
            case 4:
                float fMin3 = Math.min(f7, f9);
                float f16 = rect.left + (f5 - (f6 * fMin3));
                float f17 = rect.top + (fHeight - (f8 * fMin3));
                matrix.setScale(fMin3, fMin3);
                matrix.postTranslate((int) (f16 + 0.5f), (int) (f17 + 0.5f));
                return matrix;
            case 5:
                matrix.setTranslate((int) (rect.left + ((iWidth - i) * 0.5f) + 0.5f), (int) (rect.top + ((r3 - i2) * 0.5f) + 0.5f));
                return matrix;
            case 6:
                float fMin4 = Math.min(Math.min(f7, f9), 1.0f);
                float f18 = rect.left + ((f5 - (f6 * fMin4)) * 0.5f);
                float f19 = rect.top + ((fHeight - (f8 * fMin4)) * 0.5f);
                matrix.setScale(fMin4, fMin4);
                matrix.postTranslate((int) (f18 + 0.5f), (int) (f19 + 0.5f));
                return matrix;
            case 7:
                if (f9 > f7) {
                    f3 = rect.left + ((f5 - (f6 * f9)) * 0.5f);
                    f4 = rect.top;
                    f7 = f9;
                } else {
                    f3 = rect.left;
                    f4 = rect.top + ((fHeight - (f8 * f7)) * 0.5f);
                }
                matrix.setScale(f7, f7);
                matrix.postTranslate((int) (f3 + 0.5f), (int) (f4 + 0.5f));
                return matrix;
            case 8:
                if (f9 > f7) {
                    float f20 = f6 * f9;
                    fMax = rect.left + Math.max(Math.min((f5 * 0.5f) - (f20 * f), 0.0f), f5 - f20);
                    fMax2 = rect.top;
                    f7 = f9;
                } else {
                    fMax = rect.left;
                    float f21 = f8 * f7;
                    fMax2 = rect.top + Math.max(Math.min((fHeight * 0.5f) - (f21 * f2), 0.0f), fHeight - f21);
                }
                matrix.setScale(f7, f7);
                matrix.postTranslate((int) (fMax + 0.5f), (int) (fMax2 + 0.5f));
                return matrix;
            default:
                throw new UnsupportedOperationException("Unsupported scale type: " + scaleType);
        }
    }

    /* JADX INFO: renamed from: com.facebook.drawee.drawable.ScalingUtils$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType;

        static {
            int[] iArr = new int[ScaleType.values().length];
            $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType = iArr;
            try {
                iArr[ScaleType.FIT_XY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.FIT_START.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.FIT_CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.FIT_END.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.CENTER.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.CENTER_INSIDE.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.CENTER_CROP.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$facebook$drawee$drawable$ScalingUtils$ScaleType[ScaleType.FOCUS_CROP.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
        }
    }
}
