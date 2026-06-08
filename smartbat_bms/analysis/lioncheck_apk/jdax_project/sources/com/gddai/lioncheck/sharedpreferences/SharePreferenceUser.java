package com.gddai.lioncheck.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.gddai.lioncheck.Constans;
import com.gddai.lioncheck.sdk.LoginUsrInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import org.apache.commons.codec.binary.Base64;

/* JADX INFO: loaded from: classes.dex */
public class SharePreferenceUser {
    public static LoginUsrInfo readShareMember(Context context) {
        String string = context.getSharedPreferences(Constans.SHAREDPREFERENCES_FILENAME, 0).getString("member", "");
        if (string == "") {
            return null;
        }
        try {
            try {
                return (LoginUsrInfo) new ObjectInputStream(new ByteArrayInputStream(Base64.decodeBase64(string.getBytes()))).readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } catch (StreamCorruptedException e2) {
            e2.printStackTrace();
            return null;
        } catch (IOException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static void saveShareMember(Context context, LoginUsrInfo loginUsrInfo) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constans.SHAREDPREFERENCES_FILENAME, 0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(byteArrayOutputStream).writeObject(loginUsrInfo);
            String str = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
            SharedPreferences.Editor editorEdit = sharedPreferences.edit();
            editorEdit.putString("member", str);
            editorEdit.commit();
            Log.e("taa", "保存成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearAll(Context context) {
        saveShareMember(context, null);
    }
}
