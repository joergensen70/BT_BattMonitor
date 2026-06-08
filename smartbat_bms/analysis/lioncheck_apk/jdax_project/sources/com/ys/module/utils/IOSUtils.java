package com.ys.module.utils;

import android.app.Activity;
import com.ys.module.view.ActionSheetDialog;

/* JADX INFO: loaded from: classes.dex */
public class IOSUtils {
    public static void initPhoto(final Activity activity) {
        new ActionSheetDialog(activity).builder().setTitle("选项").setCancelable(true).setCanceledOnTouchOutside(true).addSheetItem("拍摄照片", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() { // from class: com.ys.module.utils.IOSUtils.2
            @Override // com.ys.module.view.ActionSheetDialog.OnSheetItemClickListener
            public void onClick(int i) {
                ImageSelUtils.selImage(activity, true);
            }
        }).addSheetItem("选取本地", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() { // from class: com.ys.module.utils.IOSUtils.1
            @Override // com.ys.module.view.ActionSheetDialog.OnSheetItemClickListener
            public void onClick(int i) {
                ImageSelUtils.selImage(activity, false);
            }
        }).show();
    }
}
