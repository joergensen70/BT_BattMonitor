package com.ys.module.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.lidroid.xutils.util.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import lib.lhh.fiv.library.FrescoController;

/* JADX INFO: loaded from: classes.dex */
public class BitmapUtils {
    public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Bitmap Bytes2Bimap(byte[] bArr) {
        if (bArr.length != 0) {
            return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
        }
        return null;
    }

    public static Bitmap getCropped2Bitmap(Bitmap bitmap) {
        if (bitmap.getWidth() != 100 || bitmap.getHeight() != 100) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return bitmapCreateBitmap;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int i, int i2) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float f = i / width;
            Matrix matrix = new Matrix();
            matrix.postScale(f, f);
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            bitmap.recycle();
            System.gc();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapCreateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            if (byteArrayOutputStream.toByteArray().length / 1024 > 20) {
                byteArrayOutputStream.reset();
                bitmapCreateBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            }
            LogUtils.e("�ռ�ͼƬ��С" + byteArrayOutputStream.toByteArray().length);
            try {
                Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, null);
                bitmapCreateBitmap.recycle();
                System.gc();
                return bitmapDecodeStream;
            } catch (Exception unused) {
            }
        }
        return null;
    }

    public static Bitmap resizeBitmap(float f, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        LogUtils.e("width" + width + "height" + height);
        float f2 = f / width;
        float f3 = f / height;
        if (f3 < f2) {
            f2 = f3;
        }
        Matrix matrix = new Matrix();
        if (f2 > 1.0f) {
            return bitmap;
        }
        matrix.postScale(f2, f2);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        System.gc();
        return bitmapCreateBitmap;
    }

    public static Bitmap resizeBitmaps(Bitmap bitmap, int i) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float f = i / height;
        Matrix matrix = new Matrix();
        matrix.postScale(f, f);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        System.gc();
        return bitmapCreateBitmap;
    }

    public static String saveBitmap(Bitmap bitmap, Activity activity) throws IOException {
        String str = "bit" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".png";
        File file = new File(Environment.getExternalStorageDirectory() + "/image");
        if (!file.exists()) {
            file.mkdir();
        }
        File file2 = new File(file, str);
        String str2 = Environment.getExternalStorageDirectory() + "/image/" + str;
        Log.e("taa", "filename=" + str2.toString());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 70, fileOutputStream)) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            activity.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse(FrescoController.FILE_PERFIX + str2)));
            Log.e("taa", "����ɹ�");
            return str2;
        } catch (FileNotFoundException e) {
            Log.e("taa", "�ļ��쳣");
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            Log.e("taa", "IO�쳣");
            e2.printStackTrace();
            return null;
        }
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int i) {
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        float f = i;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawRoundRect(rectF, f, f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return bitmapCreateBitmap;
    }

    public static Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        for (int i = 100; byteArrayOutputStream.toByteArray().length / 1024 > 50 && i > 12; i -= 12) {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
        }
        try {
            Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, null);
            bitmap.recycle();
            System.gc();
            return bitmapDecodeStream;
        } catch (Exception unused) {
            return null;
        }
    }

    public static Bitmap getPicFromBytes(byte[] bArr, BitmapFactory.Options options) {
        if (bArr == null) {
            return null;
        }
        if (options != null) {
            return BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        }
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }

    public static byte[] readStream(InputStream inputStream) throws Exception {
        byte[] bArr = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int i = inputStream.read(bArr);
            if (i != -1) {
                byteArrayOutputStream.write(bArr, 0, i);
            } else {
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                inputStream.close();
                return byteArray;
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x0034  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static android.graphics.Bitmap getimage(java.lang.String r6, int r7, int r8) {
        /*
            android.graphics.BitmapFactory$Options r0 = new android.graphics.BitmapFactory$Options
            r0.<init>()
            r1 = 1
            r0.inJustDecodeBounds = r1
            android.graphics.BitmapFactory.decodeFile(r6, r0)
            int r2 = r0.outWidth
            int r3 = r0.outHeight
            r4 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            if (r2 <= r3) goto L21
            if (r2 <= r8) goto L21
            int r7 = r0.outWidth
            double r2 = (double) r7
            double r2 = r2 * r4
            double r7 = (double) r8
            double r2 = r2 / r7
            double r7 = java.lang.Math.ceil(r2)
        L1f:
            int r7 = (int) r7
            goto L31
        L21:
            if (r2 >= r3) goto L30
            if (r3 <= r7) goto L30
            int r8 = r0.outHeight
            double r2 = (double) r8
            double r2 = r2 * r4
            double r7 = (double) r7
            double r2 = r2 / r7
            double r7 = java.lang.Math.ceil(r2)
            goto L1f
        L30:
            r7 = r1
        L31:
            if (r7 > 0) goto L34
            goto L35
        L34:
            r1 = r7
        L35:
            r0.inSampleSize = r1
            r7 = 0
            r0.inJustDecodeBounds = r7
            android.graphics.Bitmap r6 = android.graphics.BitmapFactory.decodeFile(r6, r0)
            android.graphics.Bitmap r6 = compressImage(r6)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.ys.module.utils.BitmapUtils.getimage(java.lang.String, int, int):android.graphics.Bitmap");
    }
}
