package com.facebook.imagepipeline.producers;

/* JADX INFO: loaded from: classes.dex */
public interface Producer<T> {
    void produceResults(Consumer<T> consumer, ProducerContext producerContext);
}
