package com.gddai.lioncheck.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.lidroid.xutils.util.LogUtils;
import com.ys.module.view.ActionSheetDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/* JADX INFO: loaded from: classes.dex */
public abstract class ImageSelActivity extends BaseActivity {
    public static final int PHOTO_REQUEST_CUT = 3;
    public static final int PHOTO_REQUEST_TAKEPHOTO = 1;
    public static final int PHOTO_REQUEST_TAKEPHOTO_CLIP = 2;
    public static final String TEMPLE_FILE = "crop.png";
    private String mTempleFile;

    protected void updateImage(String str) {
    }

    protected void showSelectImage(final boolean z) {
        new ActionSheetDialog(this).builder().setTitle("选项").setCancelable(true).setCanceledOnTouchOutside(true).addSheetItem("拍摄照片", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() { // from class: com.gddai.lioncheck.activity.base.ImageSelActivity.2
            @Override // com.ys.module.view.ActionSheetDialog.OnSheetItemClickListener
            public void onClick(int i) {
                ImageSelActivity.this.selImage(true, z);
            }
        }).addSheetItem("选取本地", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() { // from class: com.gddai.lioncheck.activity.base.ImageSelActivity.1
            @Override // com.ys.module.view.ActionSheetDialog.OnSheetItemClickListener
            public void onClick(int i) {
                ImageSelActivity.this.selImage(false, z);
            }
        }).show();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        Bundle extras;
        if (i == 1) {
            takePhone(intent, false);
            return;
        }
        if (i == 2) {
            takePhone(intent, true);
        } else {
            if (i != 3 || intent == null || (extras = intent.getExtras()) == null) {
                return;
            }
            updateImage(saveBitmap((Bitmap) extras.getParcelable(UriUtil.DATA_SCHEME)));
        }
    }

    private String saveBitmap(Bitmap bitmap) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/image");
            if (!file.exists()) {
                file.mkdir();
            }
            File file2 = new File(file, TEMPLE_FILE);
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 70, fileOutputStream)) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            LogUtils.e(file2.getAbsolutePath() + ", " + Environment.getExternalStorageDirectory() + "/image/crop.png");
            return file2.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void takePhone(Intent intent, boolean z) {
        Uri uriFromFile;
        LogUtils.e(this.mTempleFile);
        if (intent == null || intent.getData() == null) {
            uriFromFile = Uri.fromFile(new File(this.mTempleFile));
        } else {
            uriFromFile = intent.getData();
        }
        if (z) {
            startPhotoZoom(uriFromFile, GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION, this);
        } else {
            updateImage(this.mTempleFile);
        }
    }

    public String getPath(Uri uri) {
        Cursor cursorQuery = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        if (cursorQuery.moveToFirst()) {
            String string = cursorQuery.getString(cursorQuery.getColumnIndexOrThrow("_data"));
            cursorQuery.close();
            return string;
        }
        return "";
    }

    public void selImage(boolean z, boolean z2) {
        File file = new File(Environment.getExternalStorageDirectory() + "/small");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.mTempleFile = new File(file, getPhotoFile()).getAbsolutePath();
        int i = z2 ? 2 : 1;
        if (z) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra("output", Uri.fromFile(new File(this.mTempleFile)));
            startActivityForResult(intent, i);
            return;
        }
        startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), i);
    }

    public void startPhotoZoom(Uri uri, int i, Activity activity) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 10);
        intent.putExtra("aspectY", 10);
        intent.putExtra("outputX", i);
        intent.putExtra("outputY", i);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    public void deleteTempfile() {
        File templeFile = getTempleFile();
        if (templeFile.exists()) {
            templeFile.delete();
        }
    }

    public File getTempleFile() {
        return new File(Environment.getExternalStorageDirectory() + "/image", TEMPLE_FILE);
    }

    private String getPhotoFile() {
        return new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis())) + ".jpg";
    }
}
