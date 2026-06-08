package com.facebook.imagepipeline.producers;

import com.facebook.common.internal.Closeables;
import com.facebook.common.internal.Supplier;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
public abstract class LocalFetchProducer implements Producer<EncodedImage> {
    private final boolean mDecodeFileDescriptorEnabledForKitKat = false;
    private final Executor mExecutor;
    private final PooledByteBufferFactory mPooledByteBufferFactory;

    protected abstract EncodedImage getEncodedImage(ImageRequest imageRequest) throws IOException;

    protected abstract String getProducerName();

    protected LocalFetchProducer(Executor executor, PooledByteBufferFactory pooledByteBufferFactory, boolean z) {
        this.mExecutor = executor;
        this.mPooledByteBufferFactory = pooledByteBufferFactory;
    }

    @Override // com.facebook.imagepipeline.producers.Producer
    public void produceResults(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        ProducerListener listener = producerContext.getListener();
        String id = producerContext.getId();
        final ImageRequest imageRequest = producerContext.getImageRequest();
        final StatefulProducerRunnable<EncodedImage> statefulProducerRunnable = new StatefulProducerRunnable<EncodedImage>(consumer, listener, getProducerName(), id) { // from class: com.facebook.imagepipeline.producers.LocalFetchProducer.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.facebook.common.executors.StatefulRunnable
            public EncodedImage getResult() throws Exception {
                EncodedImage encodedImage = LocalFetchProducer.this.getEncodedImage(imageRequest);
                if (encodedImage == null) {
                    return null;
                }
                encodedImage.parseMetaData();
                return encodedImage;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.facebook.imagepipeline.producers.StatefulProducerRunnable, com.facebook.common.executors.StatefulRunnable
            public void disposeResult(EncodedImage encodedImage) {
                EncodedImage.closeSafely(encodedImage);
            }
        };
        producerContext.addCallbacks(new BaseProducerContextCallbacks() { // from class: com.facebook.imagepipeline.producers.LocalFetchProducer.2
            @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
            public void onCancellationRequested() {
                statefulProducerRunnable.cancel();
            }
        });
        this.mExecutor.execute(statefulProducerRunnable);
    }

    protected EncodedImage getByteBufferBackedEncodedImage(InputStream inputStream, int i) throws IOException {
        CloseableReference closeableReferenceOf;
        CloseableReference closeableReference = null;
        try {
            if (i < 0) {
                closeableReferenceOf = CloseableReference.of(this.mPooledByteBufferFactory.newByteBuffer(inputStream));
            } else {
                closeableReferenceOf = CloseableReference.of(this.mPooledByteBufferFactory.newByteBuffer(inputStream, i));
            }
            closeableReference = closeableReferenceOf;
            return new EncodedImage((CloseableReference<PooledByteBuffer>) closeableReference);
        } finally {
            Closeables.closeQuietly(inputStream);
            CloseableReference.closeSafely((CloseableReference<?>) closeableReference);
        }
    }

    protected EncodedImage getEncodedImage(InputStream inputStream, int i) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        long jMaxMemory = runtime.maxMemory();
        long jMin = Math.min(jMaxMemory - (runtime.totalMemory() - runtime.freeMemory()), 8388608L);
        if (this.mDecodeFileDescriptorEnabledForKitKat && (inputStream instanceof FileInputStream) && jMaxMemory >= jMin * 64) {
            return getInputStreamBackedEncodedImage(new File(inputStream.toString()), i);
        }
        return getByteBufferBackedEncodedImage(inputStream, i);
    }

    protected EncodedImage getInputStreamBackedEncodedImage(final File file, int i) throws IOException {
        return new EncodedImage(new Supplier<FileInputStream>() { // from class: com.facebook.imagepipeline.producers.LocalFetchProducer.3
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // com.facebook.common.internal.Supplier
            public FileInputStream get() {
                try {
                    return new FileInputStream(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, i);
    }
}
