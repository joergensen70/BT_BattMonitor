package com.facebook.imagepipeline.memory;

import com.facebook.common.internal.Preconditions;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
class Bucket<V> {
    private static final String TAG = "com.facebook.imagepipeline.common.Bucket";
    final Queue mFreeList;
    private int mInUseLength;
    public final int mItemSize;
    public final int mMaxLength;

    public Bucket(int i, int i2, int i3) {
        Preconditions.checkState(i > 0);
        Preconditions.checkState(i2 >= 0);
        Preconditions.checkState(i3 >= 0);
        this.mItemSize = i;
        this.mMaxLength = i2;
        this.mFreeList = new LinkedList();
        this.mInUseLength = i3;
    }

    public boolean isMaxLengthExceeded() {
        return this.mInUseLength + getFreeListSize() > this.mMaxLength;
    }

    int getFreeListSize() {
        return this.mFreeList.size();
    }

    @Nullable
    public V get() {
        V vPop = pop();
        if (vPop != null) {
            this.mInUseLength++;
        }
        return vPop;
    }

    @Nullable
    public V pop() {
        return (V) this.mFreeList.poll();
    }

    public void incrementInUseCount() {
        this.mInUseLength++;
    }

    public void release(V v) {
        Preconditions.checkNotNull(v);
        Preconditions.checkState(this.mInUseLength > 0);
        this.mInUseLength--;
        addToFreeList(v);
    }

    void addToFreeList(V v) {
        this.mFreeList.add(v);
    }

    public void decrementInUseCount() {
        Preconditions.checkState(this.mInUseLength > 0);
        this.mInUseLength--;
    }

    public int getInUseCount() {
        return this.mInUseLength;
    }
}
