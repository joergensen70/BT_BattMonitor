package com.facebook.imagepipeline.producers;

import com.facebook.common.internal.Preconditions;

/* JADX INFO: loaded from: classes.dex */
public class ThreadHandoffProducer<T> implements Producer<T> {
    protected static final String PRODUCER_NAME = "BackgroundThreadHandoffProducer";
    private final Producer<T> mInputProducer;
    private final ThreadHandoffProducerQueue mThreadHandoffProducerQueue;

    public ThreadHandoffProducer(Producer<T> producer, ThreadHandoffProducerQueue threadHandoffProducerQueue) {
        this.mInputProducer = (Producer) Preconditions.checkNotNull(producer);
        this.mThreadHandoffProducerQueue = threadHandoffProducerQueue;
    }

    @Override // com.facebook.imagepipeline.producers.Producer
    public void produceResults(final Consumer<T> consumer, final ProducerContext producerContext) {
        final ProducerListener listener = producerContext.getListener();
        final String id = producerContext.getId();
        final StatefulProducerRunnable<T> statefulProducerRunnable = new StatefulProducerRunnable<T>(consumer, listener, PRODUCER_NAME, id) { // from class: com.facebook.imagepipeline.producers.ThreadHandoffProducer.1
            @Override // com.facebook.imagepipeline.producers.StatefulProducerRunnable, com.facebook.common.executors.StatefulRunnable
            protected void disposeResult(T t) {
            }

            @Override // com.facebook.common.executors.StatefulRunnable
            protected T getResult() throws Exception {
                return null;
            }

            @Override // com.facebook.imagepipeline.producers.StatefulProducerRunnable, com.facebook.common.executors.StatefulRunnable
            protected void onSuccess(T t) {
                listener.onProducerFinishWithSuccess(id, ThreadHandoffProducer.PRODUCER_NAME, null);
                ThreadHandoffProducer.this.mInputProducer.produceResults(consumer, producerContext);
            }
        };
        producerContext.addCallbacks(new BaseProducerContextCallbacks() { // from class: com.facebook.imagepipeline.producers.ThreadHandoffProducer.2
            @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
            public void onCancellationRequested() {
                statefulProducerRunnable.cancel();
                ThreadHandoffProducer.this.mThreadHandoffProducerQueue.remove(statefulProducerRunnable);
            }
        });
        this.mThreadHandoffProducerQueue.addToQueueOrExecute(statefulProducerRunnable);
    }
}
