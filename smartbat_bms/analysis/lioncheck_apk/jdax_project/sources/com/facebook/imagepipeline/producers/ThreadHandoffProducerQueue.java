package com.facebook.imagepipeline.producers;

import com.facebook.common.internal.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
public class ThreadHandoffProducerQueue {
    private final Executor mExecutor;
    private boolean mQueueing = false;
    private final ArrayList<Runnable> mRunnableList = new ArrayList<>();

    public ThreadHandoffProducerQueue(Executor executor) {
        this.mExecutor = (Executor) Preconditions.checkNotNull(executor);
    }

    public synchronized void addToQueueOrExecute(Runnable runnable) {
        if (this.mQueueing) {
            this.mRunnableList.add(runnable);
        } else {
            this.mExecutor.execute(runnable);
        }
    }

    public synchronized void startQueueing() {
        this.mQueueing = true;
    }

    public synchronized void stopQueuing() {
        this.mQueueing = false;
        execInQueue();
    }

    private void execInQueue() {
        Iterator<Runnable> it = this.mRunnableList.iterator();
        while (it.hasNext()) {
            this.mExecutor.execute(it.next());
        }
        this.mRunnableList.clear();
    }

    public void remove(Runnable runnable) {
        this.mRunnableList.remove(runnable);
    }

    public synchronized boolean isQueueing() {
        return this.mQueueing;
    }
}
