package android.support.v4.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.Preconditions;
import android.util.TypedValue;

/* JADX INFO: loaded from: classes.dex */
public final class ResourcesCompat {
    private static final String TAG = "ResourcesCompat";

    public static Drawable getDrawable(Resources resources, int i, Resources.Theme theme) throws Resources.NotFoundException {
        return resources.getDrawable(i, theme);
    }

    public static Drawable getDrawableForDensity(Resources resources, int i, int i2, Resources.Theme theme) throws Resources.NotFoundException {
        return resources.getDrawableForDensity(i, i2, theme);
    }

    public static int getColor(Resources resources, int i, Resources.Theme theme) throws Resources.NotFoundException {
        return resources.getColor(i, theme);
    }

    public static ColorStateList getColorStateList(Resources resources, int i, Resources.Theme theme) throws Resources.NotFoundException {
        return resources.getColorStateList(i, theme);
    }

    public static Typeface getFont(Context context, int i) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, i, new TypedValue(), 0, null, null, false);
    }

    public static abstract class FontCallback {
        public abstract void onFontRetrievalFailed(int i);

        public abstract void onFontRetrieved(Typeface typeface);

        public final void callbackSuccessAsync(final Typeface typeface, Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() { // from class: android.support.v4.content.res.ResourcesCompat.FontCallback.1
                @Override // java.lang.Runnable
                public void run() {
                    FontCallback.this.onFontRetrieved(typeface);
                }
            });
        }

        public final void callbackFailAsync(final int i, Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() { // from class: android.support.v4.content.res.ResourcesCompat.FontCallback.2
                @Override // java.lang.Runnable
                public void run() {
                    FontCallback.this.onFontRetrievalFailed(i);
                }
            });
        }
    }

    public static void getFont(Context context, int i, FontCallback fontCallback, Handler handler) throws Resources.NotFoundException {
        Preconditions.checkNotNull(fontCallback);
        if (context.isRestricted()) {
            fontCallback.callbackFailAsync(-4, handler);
        } else {
            loadFont(context, i, new TypedValue(), 0, fontCallback, handler, false);
        }
    }

    public static Typeface getFont(Context context, int i, TypedValue typedValue, int i2, FontCallback fontCallback) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, i, typedValue, i2, fontCallback, null, true);
    }

    private static Typeface loadFont(Context context, int i, TypedValue typedValue, int i2, FontCallback fontCallback, Handler handler, boolean z) {
        Resources resources = context.getResources();
        resources.getValue(i, typedValue, true);
        Typeface typefaceLoadFont = loadFont(context, resources, typedValue, i, i2, fontCallback, handler, z);
        if (typefaceLoadFont == null && fontCallback == null) {
            throw new Resources.NotFoundException("Font resource ID #0x" + Integer.toHexString(i) + " could not be retrieved.");
        }
        return typefaceLoadFont;
    }

    /* JADX WARN: Removed duplicated region for block: B:36:0x0093  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static android.graphics.Typeface loadFont(android.content.Context r11, android.content.res.Resources r12, android.util.TypedValue r13, int r14, int r15, android.support.v4.content.res.ResourcesCompat.FontCallback r16, android.os.Handler r17, boolean r18) {
        /*
            r5 = r16
            r6 = r17
            java.lang.String r8 = "ResourcesCompat"
            java.lang.CharSequence r0 = r13.string
            if (r0 == 0) goto L97
            java.lang.CharSequence r13 = r13.string
            java.lang.String r13 = r13.toString()
            java.lang.String r0 = "res/"
            boolean r0 = r13.startsWith(r0)
            r9 = 0
            r10 = -3
            if (r0 != 0) goto L20
            if (r5 == 0) goto L1f
            r5.callbackFailAsync(r10, r6)
        L1f:
            return r9
        L20:
            android.graphics.Typeface r0 = android.support.v4.graphics.TypefaceCompat.findFromCache(r12, r14, r15)
            if (r0 == 0) goto L2c
            if (r5 == 0) goto L2b
            r5.callbackSuccessAsync(r0, r6)
        L2b:
            return r0
        L2c:
            java.lang.String r0 = r13.toLowerCase()     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            java.lang.String r1 = ".xml"
            boolean r0 = r0.endsWith(r1)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            if (r0 == 0) goto L58
            android.content.res.XmlResourceParser r0 = r12.getXml(r14)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            android.support.v4.content.res.FontResourcesParserCompat$FamilyResourceEntry r1 = android.support.v4.content.res.FontResourcesParserCompat.parse(r0, r12)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            if (r1 != 0) goto L4d
            java.lang.String r11 = "Failed to find font-family tag"
            android.util.Log.e(r8, r11)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            if (r5 == 0) goto L4c
            r5.callbackFailAsync(r10, r6)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
        L4c:
            return r9
        L4d:
            r0 = r11
            r2 = r12
            r3 = r14
            r4 = r15
            r7 = r18
            android.graphics.Typeface r11 = android.support.v4.graphics.TypefaceCompat.createFromResourcesFamilyXml(r0, r1, r2, r3, r4, r5, r6, r7)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            return r11
        L58:
            android.graphics.Typeface r11 = android.support.v4.graphics.TypefaceCompat.createFromResourcesFontFile(r11, r12, r14, r13, r15)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            if (r5 == 0) goto L67
            if (r11 == 0) goto L64
            r5.callbackSuccessAsync(r11, r6)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
            return r11
        L64:
            r5.callbackFailAsync(r10, r6)     // Catch: java.io.IOException -> L68 org.xmlpull.v1.XmlPullParserException -> L7d
        L67:
            return r11
        L68:
            r0 = move-exception
            r11 = r0
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            java.lang.String r14 = "Failed to read xml resource "
            r12.<init>(r14)
            java.lang.StringBuilder r12 = r12.append(r13)
            java.lang.String r12 = r12.toString()
            android.util.Log.e(r8, r12, r11)
            goto L91
        L7d:
            r0 = move-exception
            r11 = r0
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            java.lang.String r14 = "Failed to parse xml resource "
            r12.<init>(r14)
            java.lang.StringBuilder r12 = r12.append(r13)
            java.lang.String r12 = r12.toString()
            android.util.Log.e(r8, r12, r11)
        L91:
            if (r5 == 0) goto L96
            r5.callbackFailAsync(r10, r6)
        L96:
            return r9
        L97:
            android.content.res.Resources$NotFoundException r11 = new android.content.res.Resources$NotFoundException
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r1 = "Resource \""
            r0.<init>(r1)
            java.lang.String r12 = r12.getResourceName(r14)
            java.lang.StringBuilder r12 = r0.append(r12)
            java.lang.String r0 = "\" ("
            java.lang.StringBuilder r12 = r12.append(r0)
            java.lang.String r14 = java.lang.Integer.toHexString(r14)
            java.lang.StringBuilder r12 = r12.append(r14)
            java.lang.String r14 = ") is not a Font: "
            java.lang.StringBuilder r12 = r12.append(r14)
            java.lang.StringBuilder r12 = r12.append(r13)
            java.lang.String r12 = r12.toString()
            r11.<init>(r12)
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.content.res.ResourcesCompat.loadFont(android.content.Context, android.content.res.Resources, android.util.TypedValue, int, int, android.support.v4.content.res.ResourcesCompat$FontCallback, android.os.Handler, boolean):android.graphics.Typeface");
    }

    private ResourcesCompat() {
    }
}
