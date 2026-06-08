package com.facebook.imagepipeline.memory;

import com.facebook.common.references.OOMSoftReference;
import java.util.LinkedList;

/* JADX INFO: loaded from: classes.dex */
class OOMSoftReferenceBucket<V> extends Bucket<V> {
    private LinkedList<OOMSoftReference<V>> mSpareReferences;

    public OOMSoftReferenceBucket(int i, int i2, int i3) {
        super(i, i2, i3);
        this.mSpareReferences = new LinkedList<>();
    }

    @Override // com.facebook.imagepipeline.memory.Bucket
    public V pop() {
        OOMSoftReference<V> oOMSoftReference = (OOMSoftReference) this.mFreeList.poll();
        V v = oOMSoftReference.get();
        oOMSoftReference.clear();
        this.mSpareReferences.add(oOMSoftReference);
        return v;
    }

    @Override // com.facebook.imagepipeline.memory.Bucket
    void addToFreeList(V v) {
        OOMSoftReference<V> oOMSoftReferencePoll = this.mSpareReferences.poll();
        if (oOMSoftReferencePoll == null) {
            oOMSoftReferencePoll = new OOMSoftReference<>();
        }
        oOMSoftReferencePoll.set(v);
        this.mFreeList.add(oOMSoftReferencePoll);
    }
}
