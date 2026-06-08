package com.facebook.imagepipeline.producers;

import android.os.SystemClock;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.ByteArrayPool;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.memory.PooledByteBufferOutputStream;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import java.io.InputStream;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class NetworkFetchProducer implements Producer<EncodedImage> {
    public static final String INTERMEDIATE_RESULT_PRODUCER_EVENT = "intermediate_result";
    static final String PRODUCER_NAME = "NetworkFetchProducer";
    private static final int READ_SIZE = 16384;
    static final long TIME_BETWEEN_PARTIAL_RESULTS_MS = 100;
    private final ByteArrayPool mByteArrayPool;
    private final NetworkFetcher mNetworkFetcher;
    private final PooledByteBufferFactory mPooledByteBufferFactory;

    public NetworkFetchProducer(PooledByteBufferFactory pooledByteBufferFactory, ByteArrayPool byteArrayPool, NetworkFetcher networkFetcher) {
        this.mPooledByteBufferFactory = pooledByteBufferFactory;
        this.mByteArrayPool = byteArrayPool;
        this.mNetworkFetcher = networkFetcher;
    }

    @Override // com.facebook.imagepipeline.producers.Producer
    public void produceResults(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        producerContext.getListener().onProducerStart(producerContext.getId(), PRODUCER_NAME);
        final FetchState fetchStateCreateFetchState = this.mNetworkFetcher.createFetchState(consumer, producerContext);
        this.mNetworkFetcher.fetch(fetchStateCreateFetchState, new NetworkFetcher.Callback() { // from class: com.facebook.imagepipeline.producers.NetworkFetchProducer.1
            @Override // com.facebook.imagepipeline.producers.NetworkFetcher.Callback
            public void onResponse(InputStream inputStream, int i) throws Throwable {
                NetworkFetchProducer.this.onResponse(fetchStateCreateFetchState, inputStream, i);
            }

            @Override // com.facebook.imagepipeline.producers.NetworkFetcher.Callback
            public void onFailure(Throwable th) {
                NetworkFetchProducer.this.onFailure(fetchStateCreateFetchState, th);
            }

            @Override // com.facebook.imagepipeline.producers.NetworkFetcher.Callback
            public void onCancellation() {
                NetworkFetchProducer.this.onCancellation(fetchStateCreateFetchState);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onResponse(FetchState fetchState, InputStream inputStream, int i) throws Throwable {
        PooledByteBufferOutputStream pooledByteBufferOutputStreamNewOutputStream;
        if (i > 0) {
            pooledByteBufferOutputStreamNewOutputStream = this.mPooledByteBufferFactory.newOutputStream(i);
        } else {
            pooledByteBufferOutputStreamNewOutputStream = this.mPooledByteBufferFactory.newOutputStream();
        }
        byte[] bArr = this.mByteArrayPool.get(16384);
        while (true) {
            try {
                int i2 = inputStream.read(bArr);
                if (i2 < 0) {
                    this.mNetworkFetcher.onFetchCompletion(fetchState, pooledByteBufferOutputStreamNewOutputStream.size());
                    handleFinalResult(pooledByteBufferOutputStreamNewOutputStream, fetchState);
                    return;
                } else if (i2 > 0) {
                    pooledByteBufferOutputStreamNewOutputStream.write(bArr, 0, i2);
                    maybeHandleIntermediateResult(pooledByteBufferOutputStreamNewOutputStream, fetchState);
                    fetchState.getConsumer().onProgressUpdate(calculateProgress(pooledByteBufferOutputStreamNewOutputStream.size(), i));
                }
            } finally {
                this.mByteArrayPool.release(bArr);
                pooledByteBufferOutputStreamNewOutputStream.close();
            }
        }
    }

    private static float calculateProgress(int i, int i2) {
        return i2 > 0 ? i / i2 : 1.0f - ((float) Math.exp(((double) (-i)) / 50000.0d));
    }

    private void maybeHandleIntermediateResult(PooledByteBufferOutputStream pooledByteBufferOutputStream, FetchState fetchState) throws Throwable {
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        if (!shouldPropagateIntermediateResults(fetchState) || jElapsedRealtime - fetchState.getLastIntermediateResultTimeMs() < TIME_BETWEEN_PARTIAL_RESULTS_MS) {
            return;
        }
        fetchState.setLastIntermediateResultTimeMs(jElapsedRealtime);
        fetchState.getListener().onProducerEvent(fetchState.getId(), PRODUCER_NAME, INTERMEDIATE_RESULT_PRODUCER_EVENT);
        notifyConsumer(pooledByteBufferOutputStream, false, fetchState.getConsumer());
    }

    private void handleFinalResult(PooledByteBufferOutputStream pooledByteBufferOutputStream, FetchState fetchState) throws Throwable {
        fetchState.getListener().onProducerFinishWithSuccess(fetchState.getId(), PRODUCER_NAME, getExtraMap(fetchState, pooledByteBufferOutputStream.size()));
        notifyConsumer(pooledByteBufferOutputStream, true, fetchState.getConsumer());
    }

    private void notifyConsumer(PooledByteBufferOutputStream pooledByteBufferOutputStream, boolean z, Consumer<EncodedImage> consumer) throws Throwable {
        EncodedImage encodedImage;
        CloseableReference closeableReferenceOf = CloseableReference.of(pooledByteBufferOutputStream.toByteBuffer());
        EncodedImage encodedImage2 = null;
        try {
            encodedImage = new EncodedImage((CloseableReference<PooledByteBuffer>) closeableReferenceOf);
        } catch (Throwable th) {
            th = th;
        }
        try {
            encodedImage.parseMetaData();
            consumer.onNewResult(encodedImage, z);
            EncodedImage.closeSafely(encodedImage);
            CloseableReference.closeSafely((CloseableReference<?>) closeableReferenceOf);
        } catch (Throwable th2) {
            th = th2;
            encodedImage2 = encodedImage;
            EncodedImage.closeSafely(encodedImage2);
            CloseableReference.closeSafely((CloseableReference<?>) closeableReferenceOf);
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFailure(FetchState fetchState, Throwable th) {
        fetchState.getListener().onProducerFinishWithFailure(fetchState.getId(), PRODUCER_NAME, th, null);
        fetchState.getConsumer().onFailure(th);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCancellation(FetchState fetchState) {
        fetchState.getListener().onProducerFinishWithCancellation(fetchState.getId(), PRODUCER_NAME, null);
        fetchState.getConsumer().onCancellation();
    }

    private boolean shouldPropagateIntermediateResults(FetchState fetchState) {
        if (fetchState.getContext().getImageRequest().getProgressiveRenderingEnabled()) {
            return this.mNetworkFetcher.shouldPropagate(fetchState);
        }
        return false;
    }

    @Nullable
    private Map<String, String> getExtraMap(FetchState fetchState, int i) {
        if (fetchState.getListener().requiresExtraMap(fetchState.getId())) {
            return this.mNetworkFetcher.getExtraMap(fetchState, i);
        }
        return null;
    }
}
