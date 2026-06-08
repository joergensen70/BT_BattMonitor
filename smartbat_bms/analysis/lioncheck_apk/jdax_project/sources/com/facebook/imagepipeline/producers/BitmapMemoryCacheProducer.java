package com.facebook.imagepipeline.producers;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.ImmutableMap;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;

/* JADX INFO: loaded from: classes.dex */
public class BitmapMemoryCacheProducer implements Producer<CloseableReference<CloseableImage>> {
    static final String PRODUCER_NAME = "BitmapMemoryCacheProducer";
    static final String VALUE_FOUND = "cached_value_found";
    private final CacheKeyFactory mCacheKeyFactory;
    private final Producer<CloseableReference<CloseableImage>> mInputProducer;
    private final MemoryCache<CacheKey, CloseableImage> mMemoryCache;

    public BitmapMemoryCacheProducer(MemoryCache<CacheKey, CloseableImage> memoryCache, CacheKeyFactory cacheKeyFactory, Producer<CloseableReference<CloseableImage>> producer) {
        this.mMemoryCache = memoryCache;
        this.mCacheKeyFactory = cacheKeyFactory;
        this.mInputProducer = producer;
    }

    @Override // com.facebook.imagepipeline.producers.Producer
    public void produceResults(Consumer<CloseableReference<CloseableImage>> consumer, ProducerContext producerContext) {
        ProducerListener listener = producerContext.getListener();
        String id = producerContext.getId();
        listener.onProducerStart(id, getProducerName());
        CacheKey bitmapCacheKey = this.mCacheKeyFactory.getBitmapCacheKey(producerContext.getImageRequest());
        CloseableReference<CloseableImage> closeableReference = this.mMemoryCache.get(bitmapCacheKey);
        if (closeableReference != null) {
            boolean zIsOfFullQuality = closeableReference.get().getQualityInfo().isOfFullQuality();
            if (zIsOfFullQuality) {
                listener.onProducerFinishWithSuccess(id, getProducerName(), listener.requiresExtraMap(id) ? ImmutableMap.of(VALUE_FOUND, "true") : null);
                consumer.onProgressUpdate(1.0f);
            }
            consumer.onNewResult(closeableReference, zIsOfFullQuality);
            closeableReference.close();
            if (zIsOfFullQuality) {
                return;
            }
        }
        if (producerContext.getLowestPermittedRequestLevel().getValue() >= ImageRequest.RequestLevel.BITMAP_MEMORY_CACHE.getValue()) {
            listener.onProducerFinishWithSuccess(id, getProducerName(), listener.requiresExtraMap(id) ? ImmutableMap.of(VALUE_FOUND, "false") : null);
            consumer.onNewResult(null, true);
        } else {
            Consumer<CloseableReference<CloseableImage>> consumerWrapConsumer = wrapConsumer(consumer, bitmapCacheKey);
            listener.onProducerFinishWithSuccess(id, getProducerName(), listener.requiresExtraMap(id) ? ImmutableMap.of(VALUE_FOUND, "false") : null);
            this.mInputProducer.produceResults(consumerWrapConsumer, producerContext);
        }
    }

    protected Consumer<CloseableReference<CloseableImage>> wrapConsumer(Consumer<CloseableReference<CloseableImage>> consumer, final CacheKey cacheKey) {
        return new DelegatingConsumer<CloseableReference<CloseableImage>, CloseableReference<CloseableImage>>(consumer) { // from class: com.facebook.imagepipeline.producers.BitmapMemoryCacheProducer.1
            @Override // com.facebook.imagepipeline.producers.BaseConsumer
            public void onNewResultImpl(CloseableReference<CloseableImage> closeableReference, boolean z) {
                CloseableReference<CloseableImage> closeableReference2;
                if (closeableReference == null) {
                    if (z) {
                        getConsumer().onNewResult(null, true);
                        return;
                    }
                    return;
                }
                if (!closeableReference.get().isStateful()) {
                    if (!z && (closeableReference2 = BitmapMemoryCacheProducer.this.mMemoryCache.get(cacheKey)) != null) {
                        try {
                            QualityInfo qualityInfo = closeableReference.get().getQualityInfo();
                            QualityInfo qualityInfo2 = closeableReference2.get().getQualityInfo();
                            if (qualityInfo2.isOfFullQuality() || qualityInfo2.getQuality() >= qualityInfo.getQuality()) {
                                getConsumer().onNewResult(closeableReference2, false);
                                return;
                            }
                        } finally {
                            CloseableReference.closeSafely(closeableReference2);
                        }
                    }
                    CloseableReference<CloseableImage> closeableReferenceCache = BitmapMemoryCacheProducer.this.mMemoryCache.cache(cacheKey, closeableReference);
                    if (z) {
                        try {
                            getConsumer().onProgressUpdate(1.0f);
                        } finally {
                            CloseableReference.closeSafely(closeableReferenceCache);
                        }
                    }
                    Consumer<CloseableReference<CloseableImage>> consumer2 = getConsumer();
                    if (closeableReferenceCache != null) {
                        closeableReference = closeableReferenceCache;
                    }
                    consumer2.onNewResult(closeableReference, z);
                    return;
                }
                getConsumer().onNewResult(closeableReference, z);
            }
        };
    }

    protected String getProducerName() {
        return PRODUCER_NAME;
    }
}
