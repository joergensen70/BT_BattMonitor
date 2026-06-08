package com.lidroid.xutils.view;

import android.content.Context;
import android.view.animation.AnimationUtils;

/* JADX INFO: loaded from: classes.dex */
public class ResLoader {

    /* JADX INFO: renamed from: com.lidroid.xutils.view.ResLoader$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$lidroid$xutils$view$ResType;

        static {
            int[] iArr = new int[ResType.values().length];
            $SwitchMap$com$lidroid$xutils$view$ResType = iArr;
            try {
                iArr[ResType.Animation.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Boolean.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Color.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.ColorStateList.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Dimension.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.DimensionPixelOffset.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.DimensionPixelSize.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Drawable.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Integer.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.IntArray.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Movie.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.String.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.StringArray.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Text.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.TextArray.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$lidroid$xutils$view$ResType[ResType.Xml.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
        }
    }

    public static Object loadRes(ResType resType, Context context, int i) {
        if (context == null || i < 1) {
            return null;
        }
        switch (AnonymousClass1.$SwitchMap$com$lidroid$xutils$view$ResType[resType.ordinal()]) {
            case 1:
                return AnimationUtils.loadAnimation(context, i);
            case 2:
                return Boolean.valueOf(context.getResources().getBoolean(i));
            case 3:
                return Integer.valueOf(context.getResources().getColor(i));
            case 4:
                return context.getResources().getColorStateList(i);
            case 5:
                return Float.valueOf(context.getResources().getDimension(i));
            case 6:
                return Integer.valueOf(context.getResources().getDimensionPixelOffset(i));
            case 7:
                return Integer.valueOf(context.getResources().getDimensionPixelSize(i));
            case 8:
                return context.getResources().getDrawable(i);
            case 9:
                return Integer.valueOf(context.getResources().getInteger(i));
            case 10:
                return context.getResources().getIntArray(i);
            case 11:
                return context.getResources().getMovie(i);
            case 12:
                return context.getResources().getString(i);
            case 13:
                return context.getResources().getStringArray(i);
            case 14:
                return context.getResources().getText(i);
            case 15:
                return context.getResources().getTextArray(i);
            case 16:
                return context.getResources().getXml(i);
            default:
                return null;
        }
    }
}
