package android.support.v4.graphics;

import android.content.Context;
import android.graphics.Typeface;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.FontsContractCompat;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
class TypefaceCompatApi21Impl extends TypefaceCompatBaseImpl {
    private static final String TAG = "TypefaceCompatApi21Impl";

    TypefaceCompatApi21Impl() {
    }

    private File getFile(ParcelFileDescriptor parcelFileDescriptor) {
        try {
            String str = Os.readlink("/proc/self/fd/" + parcelFileDescriptor.getFd());
            if (OsConstants.S_ISREG(Os.stat(str).st_mode)) {
                return new File(str);
            }
        } catch (ErrnoException unused) {
        }
        return null;
    }

    @Override // android.support.v4.graphics.TypefaceCompatBaseImpl
    public Typeface createFromFontInfo(Context context, CancellationSignal cancellationSignal, FontsContractCompat.FontInfo[] fontInfoArr, int i) {
        Typeface typefaceCreateFromInputStream;
        if (fontInfoArr.length < 1) {
            return null;
        }
        FontsContractCompat.FontInfo fontInfoFindBestInfo = findBestInfo(fontInfoArr, i);
        try {
            ParcelFileDescriptor parcelFileDescriptorOpenFileDescriptor = context.getContentResolver().openFileDescriptor(fontInfoFindBestInfo.getUri(), "r", cancellationSignal);
            try {
                File file = getFile(parcelFileDescriptorOpenFileDescriptor);
                if (file == null || !file.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptorOpenFileDescriptor.getFileDescriptor());
                    try {
                        typefaceCreateFromInputStream = super.createFromInputStream(context, fileInputStream);
                        fileInputStream.close();
                        if (parcelFileDescriptorOpenFileDescriptor == null) {
                            return typefaceCreateFromInputStream;
                        }
                    } finally {
                    }
                } else {
                    typefaceCreateFromInputStream = Typeface.createFromFile(file);
                    if (parcelFileDescriptorOpenFileDescriptor != null) {
                    }
                    return typefaceCreateFromInputStream;
                }
                parcelFileDescriptorOpenFileDescriptor.close();
                return typefaceCreateFromInputStream;
            } finally {
            }
        } catch (IOException unused) {
            return null;
        }
    }
}
