package com.facebook.imagepipeline.producers;

import com.facebook.common.executors.StatefulRunnable;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public abstract class StatefulProducerRunnable<T> extends StatefulRunnable<T> {
    private final Consumer<T> mConsumer;
    private final ProducerListener mProducerListener;
    private final String mProducerName;
    private final String mRequestId;

    @Override // com.facebook.common.executors.StatefulRunnable
    protected abstract void disposeResult(T t);

    protected Map<String, String> getExtraMapOnCancellation() {
        return null;
    }

    protected Map<String, String> getExtraMapOnFailure(Exception exc) {
        return null;
    }

    protected Map<String, String> getExtraMapOnSuccess(T t) {
        return null;
    }

    public StatefulProducerRunnable(Consumer<T> consumer, ProducerListener producerListener, String str, String str2) {
        this.mConsumer = consumer;
        this.mProducerListener = producerListener;
        this.mProducerName = str;
        this.mRequestId = str2;
        producerListener.onProducerStart(str2, str);
    }

    @Override // com.facebook.common.executors.StatefulRunnable
    protected void onSuccess(T t) {
        ProducerListener producerListener = this.mProducerListener;
        String str = this.mRequestId;
        producerListener.onProducerFinishWithSuccess(str, this.mProducerName, producerListener.requiresExtraMap(str) ? getExtraMapOnSuccess(t) : null);
        this.mConsumer.onNewResult(t, true);
    }

    @Override // com.facebook.common.executors.StatefulRunnable
    protected void onFailure(Exception exc) {
        ProducerListener producerListener = this.mProducerListener;
        String str = this.mRequestId;
        producerListener.onProducerFinishWithFailure(str, this.mProducerName, exc, producerListener.requiresExtraMap(str) ? getExtraMapOnFailure(exc) : null);
        this.mConsumer.onFailure(exc);
    }

    @Override // com.facebook.common.executors.StatefulRunnable
    protected void onCancellation() {
        ProducerListener producerListener = this.mProducerListener;
        String str = this.mRequestId;
        producerListener.onProducerFinishWithCancellation(str, this.mProducerName, producerListener.requiresExtraMap(str) ? getExtraMapOnCancellation() : null);
        this.mConsumer.onCancellation();
    }
}
