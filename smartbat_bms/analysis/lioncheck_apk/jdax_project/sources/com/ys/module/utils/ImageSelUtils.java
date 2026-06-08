package com.ys.module.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import com.facebook.common.util.UriUtil;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* JADX INFO: loaded from: classes.dex */
public class ImageSelUtils {
    public static final int PHOTO_REQUEST_CUT = 3;
    public static final int PHOTO_REQUEST_GALLERY = 2;
    public static final int PHOTO_REQUEST_TAKEPHOTO = 1;
    public static Uri imageUriFromCamera;
    public static String tempFile;

    public static void selImage(Activity activity, boolean z) {
        File file = new File(Environment.getExternalStorageDirectory() + "/cfdm");
        if (!file.exists()) {
            file.mkdir();
        }
        tempFile = new File(Environment.getExternalStorageDirectory() + "/cfdm", getPhotoFileName()).getAbsolutePath();
        if (z) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra("output", Uri.fromFile(new File(tempFile)));
            activity.startActivityForResult(intent, 1);
        } else {
            Intent intent2 = new Intent("android.intent.action.PICK", (Uri) null);
            intent2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            activity.startActivityForResult(intent2, 2);
        }
    }

    public static void startPhotoZoom(Uri uri, int i, Activity activity) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 10);
        intent.putExtra("aspectY", 10);
        intent.putExtra("outputX", i);
        intent.putExtra("outputY", i);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, 3);
    }

    public static String setPicToView(Intent intent, Activity activity) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }
        try {
            return BitmapUtils.saveBitmap((Bitmap) extras.getParcelable(UriUtil.DATA_SCHEME), activity);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String setPicToView(ImageView imageView, Intent intent, Activity activity) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }
        Bitmap bitmap = (Bitmap) extras.getParcelable(UriUtil.DATA_SCHEME);
        imageView.setImageBitmap(bitmap);
        try {
            return BitmapUtils.saveBitmap(bitmap, activity);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap setPicToBitmap(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            return (Bitmap) extras.getParcelable(UriUtil.DATA_SCHEME);
        }
        return null;
    }

    private static String getPhotoFileName() {
        return new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis())) + ".jpg";
    }
}
