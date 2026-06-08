package com.nineoldandroids.animation;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.animation.AnimationUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class AnimatorInflater {
    private static final int AnimatorSet_ordering = 0;
    private static final int Animator_duration = 1;
    private static final int Animator_interpolator = 0;
    private static final int Animator_repeatCount = 3;
    private static final int Animator_repeatMode = 4;
    private static final int Animator_startOffset = 2;
    private static final int Animator_valueFrom = 5;
    private static final int Animator_valueTo = 6;
    private static final int Animator_valueType = 7;
    private static final int PropertyAnimator_propertyName = 0;
    private static final int TOGETHER = 0;
    private static final int VALUE_TYPE_FLOAT = 0;
    private static final int[] AnimatorSet = {R.attr.ordering};
    private static final int[] PropertyAnimator = {R.attr.propertyName};
    private static final int[] Animator = {R.attr.interpolator, R.attr.duration, R.attr.startOffset, R.attr.repeatCount, R.attr.repeatMode, R.attr.valueFrom, R.attr.valueTo, R.attr.valueType};

    public static Animator loadAnimator(Context context, int i) throws Resources.NotFoundException {
        XmlResourceParser animation = null;
        try {
            try {
                animation = context.getResources().getAnimation(i);
                return createAnimatorFromXml(context, animation);
            } catch (IOException e) {
                Resources.NotFoundException notFoundException = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(i));
                notFoundException.initCause(e);
                throw notFoundException;
            } catch (XmlPullParserException e2) {
                Resources.NotFoundException notFoundException2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(i));
                notFoundException2.initCause(e2);
                throw notFoundException2;
            }
        } finally {
            if (animation != null) {
                animation.close();
            }
        }
    }

    private static Animator createAnimatorFromXml(Context context, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        return createAnimatorFromXml(context, xmlPullParser, Xml.asAttributeSet(xmlPullParser), null, 0);
    }

    /* JADX WARN: Code restructure failed: missing block: B:30:0x008c, code lost:
    
        if (r12 == null) goto L40;
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x008e, code lost:
    
        if (r2 == null) goto L40;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x0090, code lost:
    
        r9 = new com.nineoldandroids.animation.Animator[r2.size()];
        r10 = r2.iterator();
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x009e, code lost:
    
        if (r10.hasNext() == false) goto L50;
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x00a0, code lost:
    
        r9[r6] = (com.nineoldandroids.animation.Animator) r10.next();
        r6 = r6 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x00ac, code lost:
    
        if (r13 != 0) goto L39;
     */
    /* JADX WARN: Code restructure failed: missing block: B:37:0x00ae, code lost:
    
        r12.playTogether(r9);
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x00b1, code lost:
    
        return r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x00b2, code lost:
    
        r12.playSequentially(r9);
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x00b5, code lost:
    
        return r3;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static com.nineoldandroids.animation.Animator createAnimatorFromXml(android.content.Context r9, org.xmlpull.v1.XmlPullParser r10, android.util.AttributeSet r11, com.nineoldandroids.animation.AnimatorSet r12, int r13) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            int r0 = r10.getDepth()
            r1 = 0
            r2 = r1
            r3 = r2
        L7:
            int r4 = r10.next()
            r5 = 3
            r6 = 0
            if (r4 != r5) goto L15
            int r5 = r10.getDepth()
            if (r5 <= r0) goto L8c
        L15:
            r5 = 1
            if (r4 == r5) goto L8c
            r5 = 2
            if (r4 == r5) goto L1c
            goto L7
        L1c:
            java.lang.String r3 = r10.getName()
            java.lang.String r4 = "objectAnimator"
            boolean r4 = r3.equals(r4)
            if (r4 == 0) goto L2d
            com.nineoldandroids.animation.ObjectAnimator r3 = loadObjectAnimator(r9, r11)
            goto L66
        L2d:
            java.lang.String r4 = "animator"
            boolean r4 = r3.equals(r4)
            if (r4 == 0) goto L3a
            com.nineoldandroids.animation.ValueAnimator r3 = loadAnimator(r9, r11, r1)
            goto L66
        L3a:
            java.lang.String r4 = "set"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L73
            com.nineoldandroids.animation.AnimatorSet r3 = new com.nineoldandroids.animation.AnimatorSet
            r3.<init>()
            int[] r4 = com.nineoldandroids.animation.AnimatorInflater.AnimatorSet
            android.content.res.TypedArray r4 = r9.obtainStyledAttributes(r11, r4)
            android.util.TypedValue r5 = new android.util.TypedValue
            r5.<init>()
            r4.getValue(r6, r5)
            int r7 = r5.type
            r8 = 16
            if (r7 != r8) goto L5d
            int r6 = r5.data
        L5d:
            r5 = r3
            com.nineoldandroids.animation.AnimatorSet r5 = (com.nineoldandroids.animation.AnimatorSet) r5
            createAnimatorFromXml(r9, r10, r11, r3, r6)
            r4.recycle()
        L66:
            if (r12 == 0) goto L7
            if (r2 != 0) goto L6f
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
        L6f:
            r2.add(r3)
            goto L7
        L73:
            java.lang.RuntimeException r9 = new java.lang.RuntimeException
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            java.lang.String r12 = "Unknown animator name: "
            r11.<init>(r12)
            java.lang.String r10 = r10.getName()
            java.lang.StringBuilder r10 = r11.append(r10)
            java.lang.String r10 = r10.toString()
            r9.<init>(r10)
            throw r9
        L8c:
            if (r12 == 0) goto Lb5
            if (r2 == 0) goto Lb5
            int r9 = r2.size()
            com.nineoldandroids.animation.Animator[] r9 = new com.nineoldandroids.animation.Animator[r9]
            java.util.Iterator r10 = r2.iterator()
        L9a:
            boolean r11 = r10.hasNext()
            if (r11 == 0) goto Lac
            java.lang.Object r11 = r10.next()
            com.nineoldandroids.animation.Animator r11 = (com.nineoldandroids.animation.Animator) r11
            int r0 = r6 + 1
            r9[r6] = r11
            r6 = r0
            goto L9a
        Lac:
            if (r13 != 0) goto Lb2
            r12.playTogether(r9)
            return r3
        Lb2:
            r12.playSequentially(r9)
        Lb5:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nineoldandroids.animation.AnimatorInflater.createAnimatorFromXml(android.content.Context, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, com.nineoldandroids.animation.AnimatorSet, int):com.nineoldandroids.animation.Animator");
    }

    private static ObjectAnimator loadObjectAnimator(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        loadAnimator(context, attributeSet, objectAnimator);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, PropertyAnimator);
        objectAnimator.setPropertyName(typedArrayObtainStyledAttributes.getString(0));
        typedArrayObtainStyledAttributes.recycle();
        return objectAnimator;
    }

    private static ValueAnimator loadAnimator(Context context, AttributeSet attributeSet, ValueAnimator valueAnimator) throws Resources.NotFoundException {
        int i;
        int i2;
        int i3;
        int color;
        int i4;
        int color2;
        int color3;
        float dimension;
        float dimension2;
        float dimension3;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, Animator);
        long j = typedArrayObtainStyledAttributes.getInt(1, 0);
        long j2 = typedArrayObtainStyledAttributes.getInt(2, 0);
        int i5 = typedArrayObtainStyledAttributes.getInt(7, 0);
        ValueAnimator valueAnimator2 = valueAnimator == null ? new ValueAnimator() : valueAnimator;
        int i6 = i5 == 0 ? 1 : 0;
        TypedValue typedValuePeekValue = typedArrayObtainStyledAttributes.peekValue(5);
        boolean z = typedValuePeekValue != null;
        int i7 = z ? typedValuePeekValue.type : 0;
        TypedValue typedValuePeekValue2 = typedArrayObtainStyledAttributes.peekValue(6);
        boolean z2 = typedValuePeekValue2 != null;
        if (z2) {
            i2 = typedValuePeekValue2.type;
            i = 0;
        } else {
            i = 0;
            i2 = 0;
        }
        if ((z && i7 >= 28 && i7 <= 31) || (z2 && i2 >= 28 && i2 <= 31)) {
            valueAnimator2.setEvaluator(new ArgbEvaluator());
            i6 = i;
        }
        if (i6 != 0) {
            if (z) {
                if (i7 == 5) {
                    dimension2 = typedArrayObtainStyledAttributes.getDimension(5, 0.0f);
                } else {
                    dimension2 = typedArrayObtainStyledAttributes.getFloat(5, 0.0f);
                }
                if (z2) {
                    if (i2 == 5) {
                        dimension3 = typedArrayObtainStyledAttributes.getDimension(6, 0.0f);
                    } else {
                        dimension3 = typedArrayObtainStyledAttributes.getFloat(6, 0.0f);
                    }
                    float[] fArr = new float[2];
                    fArr[i] = dimension2;
                    fArr[1] = dimension3;
                    valueAnimator2.setFloatValues(fArr);
                } else {
                    float[] fArr2 = new float[1];
                    fArr2[i] = dimension2;
                    valueAnimator2.setFloatValues(fArr2);
                }
            } else {
                if (i2 == 5) {
                    dimension = typedArrayObtainStyledAttributes.getDimension(6, 0.0f);
                } else {
                    dimension = typedArrayObtainStyledAttributes.getFloat(6, 0.0f);
                }
                float[] fArr3 = new float[1];
                fArr3[i] = dimension;
                valueAnimator2.setFloatValues(fArr3);
            }
            i3 = i;
        } else {
            if (z) {
                if (i7 == 5) {
                    color2 = (int) typedArrayObtainStyledAttributes.getDimension(5, 0.0f);
                    i4 = i;
                } else if (i7 >= 28 && i7 <= 31) {
                    i4 = i;
                    color2 = typedArrayObtainStyledAttributes.getColor(5, i4);
                } else {
                    i4 = i;
                    color2 = typedArrayObtainStyledAttributes.getInt(5, i4);
                }
                if (z2) {
                    if (i2 == 5) {
                        color3 = (int) typedArrayObtainStyledAttributes.getDimension(6, 0.0f);
                    } else if (i2 >= 28 && i2 <= 31) {
                        color3 = typedArrayObtainStyledAttributes.getColor(6, i4);
                    } else {
                        color3 = typedArrayObtainStyledAttributes.getInt(6, i4);
                    }
                    valueAnimator2.setIntValues(color2, color3);
                } else {
                    valueAnimator2.setIntValues(color2);
                }
            } else if (z2) {
                if (i2 == 5) {
                    color = (int) typedArrayObtainStyledAttributes.getDimension(6, 0.0f);
                    i3 = 0;
                } else if (i2 >= 28 && i2 <= 31) {
                    i3 = 0;
                    color = typedArrayObtainStyledAttributes.getColor(6, 0);
                } else {
                    i3 = 0;
                    color = typedArrayObtainStyledAttributes.getInt(6, 0);
                }
                valueAnimator2.setIntValues(color);
            }
            i3 = 0;
        }
        valueAnimator2.setDuration(j);
        valueAnimator2.setStartDelay(j2);
        if (typedArrayObtainStyledAttributes.hasValue(3)) {
            valueAnimator2.setRepeatCount(typedArrayObtainStyledAttributes.getInt(3, i3));
        }
        if (typedArrayObtainStyledAttributes.hasValue(4)) {
            valueAnimator2.setRepeatMode(typedArrayObtainStyledAttributes.getInt(4, 1));
        }
        int resourceId = typedArrayObtainStyledAttributes.getResourceId(i3, i3);
        if (resourceId > 0) {
            valueAnimator2.setInterpolator(AnimationUtils.loadInterpolator(context, resourceId));
        }
        typedArrayObtainStyledAttributes.recycle();
        return valueAnimator2;
    }
}
