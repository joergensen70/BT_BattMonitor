package com.facebook.imagepipeline.producers;

import bolts.Continuation;
import bolts.Task;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.ImmutableMap;
import com.facebook.imagepipeline.cache.BufferedDiskCache;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.request.ImageRequest;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: loaded from: classes.dex */
public class DiskCacheProducer implements Producer<EncodedImage> {
    static final String PRODUCER_NAME = "DiskCacheProducer";
    static final String VALUE_FOUND = "cached_value_found";
    private final CacheKeyFactory mCacheKeyFactory;
    private final BufferedDiskCache mDefaultBufferedDiskCache;
    private final Producer<EncodedImage> mInputProducer;
    private final BufferedDiskCache mSmallImageBufferedDiskCache;

    public DiskCacheProducer(BufferedDiskCache bufferedDiskCache, BufferedDiskCache bufferedDiskCache2, CacheKeyFactory cacheKeyFactory, Producer<EncodedImage> producer) {
        this.mDefaultBufferedDiskCache = bufferedDiskCache;
        this.mSmallImageBufferedDiskCache = bufferedDiskCache2;
        this.mCacheKeyFactory = cacheKeyFactory;
        this.mInputProducer = producer;
    }

    @Override // com.facebook.imagepipeline.producers.Producer
    public void produceResults(final Consumer<EncodedImage> consumer, final ProducerContext producerContext) {
        ImageRequest imageRequest = producerContext.getImageRequest();
        if (!imageRequest.isDiskCacheEnabled()) {
            maybeStartInputProducer(consumer, consumer, producerContext);
            return;
        }
        final ProducerListener listener = producerContext.getListener();
        final String id = producerContext.getId();
        listener.onProducerStart(id, PRODUCER_NAME);
        final CacheKey encodedCacheKey = this.mCacheKeyFactory.getEncodedCacheKey(imageRequest);
        final BufferedDiskCache bufferedDiskCache = imageRequest.getImageType() == ImageRequest.ImageType.SMALL ? this.mSmallImageBufferedDiskCache : this.mDefaultBufferedDiskCache;
        Continuation continuation = new Continuation<EncodedImage, Void>() { // from class: com.facebook.imagepipeline.producers.DiskCacheProducer.1
            @Override // bolts.Continuation
            public Void then(Task<EncodedImage> task) throws Exception {
                if (task.isCancelled() || (task.isFaulted() && (task.getError() instanceof CancellationException))) {
                    listener.onProducerFinishWithCancellation(id, DiskCacheProducer.PRODUCER_NAME, null);
                    consumer.onCancellation();
                } else if (task.isFaulted()) {
                    listener.onProducerFinishWithFailure(id, DiskCacheProducer.PRODUCER_NAME, task.getError(), null);
                    DiskCacheProducer.this.maybeStartInputProducer(consumer, new DiskCacheConsumer(consumer, bufferedDiskCache, encodedCacheKey), producerContext);
                } else {
                    EncodedImage result = task.getResult();
                    if (result != null) {
                        ProducerListener producerListener = listener;
                        String str = id;
                        producerListener.onProducerFinishWithSuccess(str, DiskCacheProducer.PRODUCER_NAME, DiskCacheProducer.getExtraMap(producerListener, str, true));
                        consumer.onProgressUpdate(1.0f);
                        consumer.onNewResult(result, true);
                        result.close();
                    } else {
                        ProducerListener producerListener2 = listener;
                        String str2 = id;
                        producerListener2.onProducerFinishWithSuccess(str2, DiskCacheProducer.PRODUCER_NAME, DiskCacheProducer.getExtraMap(producerListener2, str2, false));
                        DiskCacheProducer.this.maybeStartInputProducer(consumer, new DiskCacheConsumer(consumer, bufferedDiskCache, encodedCacheKey), producerContext);
                    }
                }
                return null;
            }
        };
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        bufferedDiskCache.get(encodedCacheKey, atomicBoolean).continueWith(continuation);
        subscribeTaskForRequestCancellation(atomicBoolean, producerContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void maybeStartInputProducer(Consumer<EncodedImage> consumer, Consumer<EncodedImage> consumer2, ProducerContext producerContext) {
        if (producerContext.getLowestPermittedRequestLevel().getValue() >= ImageRequest.RequestLevel.DISK_CACHE.getValue()) {
            consumer.onNewResult(null, true);
        } else {
            this.mInputProducer.produceResults(consumer2, producerContext);
        }
    }

    static Map<String, String> getExtraMap(ProducerListener producerListener, String str, boolean z) {
        if (producerListener.requiresExtraMap(str)) {
            return ImmutableMap.of(VALUE_FOUND, String.valueOf(z));
        }
        return null;
    }

    private void subscribeTaskForRequestCancellation(final AtomicBoolean atomicBoolean, ProducerContext producerContext) {
        producerContext.addCallbacks(new BaseProducerContextCallbacks() { // from class: com.facebook.imagepipeline.producers.DiskCacheProducer.2
            @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
            public void onCancellationRequested() {
                atomicBoolean.set(true);
            }
        });
    }

    private class DiskCacheConsumer extends DelegatingConsumer<EncodedImage, EncodedImage> {
        private final BufferedDiskCache mCache;
        private final CacheKey mCacheKey;

        private DiskCacheConsumer(Consumer<EncodedImage> consumer, BufferedDiskCache bufferedDiskCache, CacheKey cacheKey) {
            super(consumer);
            this.mCache = bufferedDiskCache;
            this.mCacheKey = cacheKey;
        }

        @Override // com.facebook.imagepipeline.producers.BaseConsumer
        public void onNewResultImpl(EncodedImage encodedImage, boolean z) {
            if (encodedImage != null && z) {
                this.mCache.put(this.mCacheKey, encodedImage);
            }
            getConsumer().onNewResult(encodedImage, z);
        }
    }
}
