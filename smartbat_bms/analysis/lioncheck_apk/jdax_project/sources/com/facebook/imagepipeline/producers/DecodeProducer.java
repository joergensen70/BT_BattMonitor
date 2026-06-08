package com.facebook.imagepipeline.producers;

import android.graphics.Bitmap;
import com.facebook.common.internal.ImmutableMap;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig;
import com.facebook.imagepipeline.decoder.ProgressiveJpegParser;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.memory.ByteArrayPool;
import com.facebook.imagepipeline.producers.JobScheduler;
import com.facebook.imagepipeline.request.ImageRequest;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class DecodeProducer implements Producer<CloseableReference<CloseableImage>> {
    private static final String BITMAP_SIZE_KEY = "bitmapSize";
    private static final String HAS_GOOD_QUALITY_KEY = "hasGoodQuality";
    private static final String IMAGE_TYPE_KEY = "imageType";
    private static final String IS_FINAL_KEY = "isFinal";
    public static final String PRODUCER_NAME = "DecodeProducer";
    private final ByteArrayPool mByteArrayPool;
    private final boolean mDownsampleEnabled;
    private final boolean mDownsampleEnabledForNetwork;
    private final Executor mExecutor;
    private final ImageDecoder mImageDecoder;
    private final Producer<EncodedImage> mInputProducer;
    private final ProgressiveJpegConfig mProgressiveJpegConfig;

    public DecodeProducer(ByteArrayPool byteArrayPool, Executor executor, ImageDecoder imageDecoder, ProgressiveJpegConfig progressiveJpegConfig, boolean z, boolean z2, Producer<EncodedImage> producer) {
        this.mByteArrayPool = (ByteArrayPool) Preconditions.checkNotNull(byteArrayPool);
        this.mExecutor = (Executor) Preconditions.checkNotNull(executor);
        this.mImageDecoder = (ImageDecoder) Preconditions.checkNotNull(imageDecoder);
        this.mProgressiveJpegConfig = (ProgressiveJpegConfig) Preconditions.checkNotNull(progressiveJpegConfig);
        this.mDownsampleEnabled = z;
        this.mDownsampleEnabledForNetwork = z2;
        this.mInputProducer = (Producer) Preconditions.checkNotNull(producer);
    }

    @Override // com.facebook.imagepipeline.producers.Producer
    public void produceResults(Consumer<CloseableReference<CloseableImage>> consumer, ProducerContext producerContext) {
        DecodeProducer decodeProducer;
        ProducerContext producerContext2;
        Consumer<EncodedImage> networkImagesProgressiveDecoder;
        if (!UriUtil.isNetworkUri(producerContext.getImageRequest().getSourceUri())) {
            networkImagesProgressiveDecoder = new LocalImagesProgressiveDecoder(consumer, producerContext);
            decodeProducer = this;
            producerContext2 = producerContext;
        } else {
            decodeProducer = this;
            producerContext2 = producerContext;
            networkImagesProgressiveDecoder = decodeProducer.new NetworkImagesProgressiveDecoder(consumer, producerContext2, new ProgressiveJpegParser(this.mByteArrayPool), this.mProgressiveJpegConfig);
        }
        decodeProducer.mInputProducer.produceResults(networkImagesProgressiveDecoder, producerContext2);
    }

    private abstract class ProgressiveDecoder extends DelegatingConsumer<EncodedImage, CloseableReference<CloseableImage>> {
        private final ImageDecodeOptions mImageDecodeOptions;
        private boolean mIsFinished;
        private final JobScheduler mJobScheduler;
        private final ProducerContext mProducerContext;
        private final ProducerListener mProducerListener;

        protected abstract int getIntermediateImageEndOffset(EncodedImage encodedImage);

        protected abstract QualityInfo getQualityInfo();

        public ProgressiveDecoder(Consumer<CloseableReference<CloseableImage>> consumer, final ProducerContext producerContext) {
            super(consumer);
            this.mProducerContext = producerContext;
            this.mProducerListener = producerContext.getListener();
            ImageDecodeOptions imageDecodeOptions = producerContext.getImageRequest().getImageDecodeOptions();
            this.mImageDecodeOptions = imageDecodeOptions;
            this.mIsFinished = false;
            this.mJobScheduler = new JobScheduler(DecodeProducer.this.mExecutor, new JobScheduler.JobRunnable() { // from class: com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder.1
                @Override // com.facebook.imagepipeline.producers.JobScheduler.JobRunnable
                public void run(EncodedImage encodedImage, boolean z) throws Throwable {
                    if (encodedImage != null) {
                        if (DecodeProducer.this.mDownsampleEnabled) {
                            ImageRequest imageRequest = producerContext.getImageRequest();
                            if (DecodeProducer.this.mDownsampleEnabledForNetwork || !UriUtil.isNetworkUri(imageRequest.getSourceUri())) {
                                encodedImage.setSampleSize(DownsampleUtil.determineSampleSize(imageRequest, encodedImage));
                            }
                        }
                        ProgressiveDecoder.this.doDecode(encodedImage, z);
                    }
                }
            }, imageDecodeOptions.minDecodeIntervalMs);
            producerContext.addCallbacks(new BaseProducerContextCallbacks() { // from class: com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder.2
                @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
                public void onIsIntermediateResultExpectedChanged() {
                    if (ProgressiveDecoder.this.mProducerContext.isIntermediateResultExpected()) {
                        ProgressiveDecoder.this.mJobScheduler.scheduleJob();
                    }
                }
            });
        }

        @Override // com.facebook.imagepipeline.producers.BaseConsumer
        public void onNewResultImpl(EncodedImage encodedImage, boolean z) {
            if (z && !EncodedImage.isValid(encodedImage)) {
                handleError(new NullPointerException("Encoded image is not valid."));
            } else if (updateDecodeJob(encodedImage, z)) {
                if (z || this.mProducerContext.isIntermediateResultExpected()) {
                    this.mJobScheduler.scheduleJob();
                }
            }
        }

        @Override // com.facebook.imagepipeline.producers.DelegatingConsumer, com.facebook.imagepipeline.producers.BaseConsumer
        public void onFailureImpl(Throwable th) {
            handleError(th);
        }

        @Override // com.facebook.imagepipeline.producers.DelegatingConsumer, com.facebook.imagepipeline.producers.BaseConsumer
        public void onCancellationImpl() {
            handleCancellation();
        }

        protected boolean updateDecodeJob(EncodedImage encodedImage, boolean z) {
            return this.mJobScheduler.updateJob(encodedImage, z);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void doDecode(EncodedImage encodedImage, boolean z) throws Throwable {
            Throwable th;
            long queuedTime;
            int size;
            QualityInfo qualityInfo;
            if (isFinished() || !EncodedImage.isValid(encodedImage)) {
                return;
            }
            try {
                queuedTime = this.mJobScheduler.getQueuedTime();
                if (z) {
                    try {
                        size = encodedImage.getSize();
                    } catch (Throwable th2) {
                        th = th2;
                        EncodedImage.closeSafely(encodedImage);
                        throw th;
                    }
                } else {
                    size = getIntermediateImageEndOffset(encodedImage);
                }
                qualityInfo = z ? ImmutableQualityInfo.FULL_QUALITY : getQualityInfo();
                this.mProducerListener.onProducerStart(this.mProducerContext.getId(), DecodeProducer.PRODUCER_NAME);
            } catch (Throwable th3) {
                th = th3;
            }
            try {
                try {
                    CloseableImage closeableImageDecodeImage = DecodeProducer.this.mImageDecoder.decodeImage(encodedImage, size, qualityInfo, this.mImageDecodeOptions);
                    this.mProducerListener.onProducerFinishWithSuccess(this.mProducerContext.getId(), DecodeProducer.PRODUCER_NAME, getExtraMap(closeableImageDecodeImage, queuedTime, qualityInfo, z));
                    handleResult(closeableImageDecodeImage, z);
                    EncodedImage.closeSafely(encodedImage);
                } catch (Exception e) {
                    this.mProducerListener.onProducerFinishWithFailure(this.mProducerContext.getId(), DecodeProducer.PRODUCER_NAME, e, getExtraMap(null, queuedTime, qualityInfo, z));
                    handleError(e);
                    EncodedImage.closeSafely(encodedImage);
                }
            } catch (Throwable th4) {
                th = th4;
                th = th;
                EncodedImage.closeSafely(encodedImage);
                throw th;
            }
        }

        private Map<String, String> getExtraMap(@Nullable CloseableImage closeableImage, long j, QualityInfo qualityInfo, boolean z) {
            if (!this.mProducerListener.requiresExtraMap(this.mProducerContext.getId())) {
                return null;
            }
            String strValueOf = String.valueOf(j);
            String strValueOf2 = String.valueOf(qualityInfo.isOfGoodEnoughQuality());
            String strValueOf3 = String.valueOf(z);
            String strValueOf4 = String.valueOf(this.mProducerContext.getImageRequest().getImageType());
            if (closeableImage instanceof CloseableStaticBitmap) {
                Bitmap underlyingBitmap = ((CloseableStaticBitmap) closeableImage).getUnderlyingBitmap();
                return ImmutableMap.of(DecodeProducer.BITMAP_SIZE_KEY, underlyingBitmap.getWidth() + "x" + underlyingBitmap.getHeight(), "queueTime", strValueOf, DecodeProducer.HAS_GOOD_QUALITY_KEY, strValueOf2, DecodeProducer.IS_FINAL_KEY, strValueOf3, DecodeProducer.IMAGE_TYPE_KEY, strValueOf4);
            }
            return ImmutableMap.of("queueTime", strValueOf, DecodeProducer.HAS_GOOD_QUALITY_KEY, strValueOf2, DecodeProducer.IS_FINAL_KEY, strValueOf3, DecodeProducer.IMAGE_TYPE_KEY, strValueOf4);
        }

        private synchronized boolean isFinished() {
            return this.mIsFinished;
        }

        private void maybeFinish(boolean z) {
            synchronized (this) {
                if (z) {
                    if (!this.mIsFinished) {
                        this.mIsFinished = true;
                        this.mJobScheduler.clearJob();
                    }
                }
            }
        }

        private void handleResult(CloseableImage closeableImage, boolean z) {
            CloseableReference<CloseableImage> closeableReferenceOf = CloseableReference.of(closeableImage);
            try {
                maybeFinish(z);
                getConsumer().onNewResult(closeableReferenceOf, z);
            } finally {
                CloseableReference.closeSafely(closeableReferenceOf);
            }
        }

        private void handleError(Throwable th) {
            maybeFinish(true);
            getConsumer().onFailure(th);
        }

        private void handleCancellation() {
            maybeFinish(true);
            getConsumer().onCancellation();
        }
    }

    private class LocalImagesProgressiveDecoder extends ProgressiveDecoder {
        public LocalImagesProgressiveDecoder(Consumer<CloseableReference<CloseableImage>> consumer, ProducerContext producerContext) {
            super(consumer, producerContext);
        }

        @Override // com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder
        protected synchronized boolean updateDecodeJob(EncodedImage encodedImage, boolean z) {
            if (!z) {
                return false;
            }
            return super.updateDecodeJob(encodedImage, z);
        }

        @Override // com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder
        protected int getIntermediateImageEndOffset(EncodedImage encodedImage) {
            return encodedImage.getSize();
        }

        @Override // com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder
        protected QualityInfo getQualityInfo() {
            return ImmutableQualityInfo.of(0, false, false);
        }
    }

    private class NetworkImagesProgressiveDecoder extends ProgressiveDecoder {
        private int mLastScheduledScanNumber;
        private final ProgressiveJpegConfig mProgressiveJpegConfig;
        private final ProgressiveJpegParser mProgressiveJpegParser;

        public NetworkImagesProgressiveDecoder(Consumer<CloseableReference<CloseableImage>> consumer, ProducerContext producerContext, ProgressiveJpegParser progressiveJpegParser, ProgressiveJpegConfig progressiveJpegConfig) {
            super(consumer, producerContext);
            this.mProgressiveJpegParser = (ProgressiveJpegParser) Preconditions.checkNotNull(progressiveJpegParser);
            this.mProgressiveJpegConfig = (ProgressiveJpegConfig) Preconditions.checkNotNull(progressiveJpegConfig);
            this.mLastScheduledScanNumber = 0;
        }

        @Override // com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder
        protected synchronized boolean updateDecodeJob(EncodedImage encodedImage, boolean z) {
            boolean zUpdateDecodeJob = super.updateDecodeJob(encodedImage, z);
            if (!z && EncodedImage.isValid(encodedImage)) {
                if (!this.mProgressiveJpegParser.parseMoreData(encodedImage)) {
                    return false;
                }
                int bestScanNumber = this.mProgressiveJpegParser.getBestScanNumber();
                int i = this.mLastScheduledScanNumber;
                if (bestScanNumber > i && bestScanNumber >= this.mProgressiveJpegConfig.getNextScanNumberToDecode(i)) {
                    this.mLastScheduledScanNumber = bestScanNumber;
                }
                return false;
            }
            return zUpdateDecodeJob;
        }

        @Override // com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder
        protected int getIntermediateImageEndOffset(EncodedImage encodedImage) {
            return this.mProgressiveJpegParser.getBestScanEndOffset();
        }

        @Override // com.facebook.imagepipeline.producers.DecodeProducer.ProgressiveDecoder
        protected QualityInfo getQualityInfo() {
            return this.mProgressiveJpegConfig.getQualityInfo(this.mProgressiveJpegParser.getBestScanNumber());
        }
    }
}
