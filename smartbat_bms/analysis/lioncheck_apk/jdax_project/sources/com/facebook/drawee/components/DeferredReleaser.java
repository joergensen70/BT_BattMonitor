package com.facebook.drawee.components;

import android.os.Handler;
import android.os.Looper;
import com.facebook.common.internal.Preconditions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class DeferredReleaser {
    private static DeferredReleaser sInstance;
    private final Runnable releaseRunnable = new Runnable() { // from class: com.facebook.drawee.components.DeferredReleaser.1
        @Override // java.lang.Runnable
        public void run() {
            DeferredReleaser.ensureOnUiThread();
            Iterator it = DeferredReleaser.this.mPendingReleasables.iterator();
            while (it.hasNext()) {
                ((Releasable) it.next()).release();
            }
            DeferredReleaser.this.mPendingReleasables.clear();
        }
    };
    private final Set<Releasable> mPendingReleasables = new HashSet();
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    public interface Releasable {
        void release();
    }

    public static synchronized DeferredReleaser getInstance() {
        if (sInstance == null) {
            sInstance = new DeferredReleaser();
        }
        return sInstance;
    }

    public void scheduleDeferredRelease(Releasable releasable) {
        ensureOnUiThread();
        if (this.mPendingReleasables.add(releasable) && this.mPendingReleasables.size() == 1) {
            this.mUiHandler.post(this.releaseRunnable);
        }
    }

    public void cancelDeferredRelease(Releasable releasable) {
        ensureOnUiThread();
        this.mPendingReleasables.remove(releasable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void ensureOnUiThread() {
        Preconditions.checkState(Looper.getMainLooper().getThread() == Thread.currentThread());
    }
}
