package android.support.v4.os;

import android.os.Trace;

/* JADX INFO: loaded from: classes.dex */
public final class TraceCompat {
    public static void beginSection(String str) {
        Trace.beginSection(str);
    }

    public static void endSection() {
        Trace.endSection();
    }

    private TraceCompat() {
    }
}
