package com.facebook.imagepipeline.cache;

/* JADX INFO: loaded from: classes.dex */
public interface MemoryCacheTracker {
    void onCacheHit();

    void onCacheMiss();

    void onCachePut();
}
