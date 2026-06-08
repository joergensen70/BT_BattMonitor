package com.facebook.imagepipeline.core;

import android.net.Uri;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.media.MediaUtils;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.producers.AddImageTransformMetaDataProducer;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.RemoveImageTransformMetaDataProducer;
import com.facebook.imagepipeline.producers.ThreadHandoffProducerQueue;
import com.facebook.imagepipeline.producers.ThrottlingProducer;
import com.facebook.imagepipeline.request.ImageRequest;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class ProducerSequenceFactory {
    private static final int MAX_SIMULTANEOUS_FILE_FETCH_AND_RESIZE = 5;
    Producer<EncodedImage> mBackgroundNetworkFetchToEncodedMemorySequence;
    private Producer<EncodedImage> mCommonNetworkFetchToEncodedMemorySequence;
    Producer<CloseableReference<CloseableImage>> mDataFetchSequence;
    private final boolean mDownsampleEnabled;
    Producer<CloseableReference<PooledByteBuffer>> mEncodedImageProducerSequence;
    Producer<CloseableReference<CloseableImage>> mLocalAssetFetchSequence;
    Producer<CloseableReference<CloseableImage>> mLocalContentUriFetchSequence;
    Producer<CloseableReference<CloseableImage>> mLocalImageFileFetchSequence;
    Producer<CloseableReference<CloseableImage>> mLocalResourceFetchSequence;
    Producer<CloseableReference<CloseableImage>> mLocalVideoFileFetchSequence;
    Producer<CloseableReference<CloseableImage>> mNetworkFetchSequence;
    Producer<Void> mNetworkFetchToEncodedMemoryPrefetchSequence;
    private final NetworkFetcher mNetworkFetcher;
    private final ProducerFactory mProducerFactory;
    private final boolean mResizeAndRotateEnabledForNetwork;
    private final ThreadHandoffProducerQueue mThreadHandoffProducerQueue;
    private final boolean mWebpSupportEnabled;
    Map<Producer<CloseableReference<CloseableImage>>, Producer<CloseableReference<CloseableImage>>> mPostprocessorSequences = new HashMap();
    Map<Producer<CloseableReference<CloseableImage>>, Producer<Void>> mCloseableImagePrefetchSequences = new HashMap();

    public ProducerSequenceFactory(ProducerFactory producerFactory, NetworkFetcher networkFetcher, boolean z, boolean z2, boolean z3, ThreadHandoffProducerQueue threadHandoffProducerQueue) {
        this.mProducerFactory = producerFactory;
        this.mNetworkFetcher = networkFetcher;
        this.mResizeAndRotateEnabledForNetwork = z;
        this.mDownsampleEnabled = z2;
        this.mWebpSupportEnabled = z3;
        this.mThreadHandoffProducerQueue = threadHandoffProducerQueue;
    }

    public Producer<CloseableReference<PooledByteBuffer>> getEncodedImageProducerSequence(ImageRequest imageRequest) {
        validateEncodedImageRequest(imageRequest);
        synchronized (this) {
            if (this.mEncodedImageProducerSequence == null) {
                this.mEncodedImageProducerSequence = new RemoveImageTransformMetaDataProducer(getBackgroundNetworkFetchToEncodedMemorySequence());
            }
        }
        return this.mEncodedImageProducerSequence;
    }

    public Producer<Void> getEncodedImagePrefetchProducerSequence(ImageRequest imageRequest) {
        validateEncodedImageRequest(imageRequest);
        return getNetworkFetchToEncodedMemoryPrefetchSequence();
    }

    private static void validateEncodedImageRequest(ImageRequest imageRequest) {
        Preconditions.checkNotNull(imageRequest);
        Preconditions.checkArgument(UriUtil.isNetworkUri(imageRequest.getSourceUri()));
        Preconditions.checkArgument(imageRequest.getLowestPermittedRequestLevel().getValue() <= ImageRequest.RequestLevel.ENCODED_MEMORY_CACHE.getValue());
    }

    public Producer<CloseableReference<CloseableImage>> getDecodedImageProducerSequence(ImageRequest imageRequest) {
        Producer<CloseableReference<CloseableImage>> basicDecodedImageSequence = getBasicDecodedImageSequence(imageRequest);
        return imageRequest.getPostprocessor() != null ? getPostprocessorSequence(basicDecodedImageSequence) : basicDecodedImageSequence;
    }

    public Producer<Void> getDecodedImagePrefetchProducerSequence(ImageRequest imageRequest) {
        return getDecodedImagePrefetchSequence(getBasicDecodedImageSequence(imageRequest));
    }

    private Producer<CloseableReference<CloseableImage>> getBasicDecodedImageSequence(ImageRequest imageRequest) {
        Preconditions.checkNotNull(imageRequest);
        Uri sourceUri = imageRequest.getSourceUri();
        Preconditions.checkNotNull(sourceUri, "Uri is null.");
        if (UriUtil.isNetworkUri(sourceUri)) {
            return getNetworkFetchSequence();
        }
        if (UriUtil.isLocalFileUri(sourceUri)) {
            if (MediaUtils.isVideo(MediaUtils.extractMime(sourceUri.getPath()))) {
                return getLocalVideoFileFetchSequence();
            }
            return getLocalImageFileFetchSequence();
        }
        if (UriUtil.isLocalContentUri(sourceUri)) {
            return getLocalContentUriFetchSequence();
        }
        if (UriUtil.isLocalAssetUri(sourceUri)) {
            return getLocalAssetFetchSequence();
        }
        if (UriUtil.isLocalResourceUri(sourceUri)) {
            return getLocalResourceFetchSequence();
        }
        if (UriUtil.isDataUri(sourceUri)) {
            return getDataFetchSequence();
        }
        String string = sourceUri.toString();
        if (string.length() > 30) {
            string = string.substring(0, 30) + "...";
        }
        throw new RuntimeException("Unsupported uri scheme! Uri is: " + string);
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getNetworkFetchSequence() {
        if (this.mNetworkFetchSequence == null) {
            this.mNetworkFetchSequence = newBitmapCacheGetToDecodeSequence(getCommonNetworkFetchToEncodedMemorySequence());
        }
        return this.mNetworkFetchSequence;
    }

    private synchronized Producer<EncodedImage> getBackgroundNetworkFetchToEncodedMemorySequence() {
        if (this.mBackgroundNetworkFetchToEncodedMemorySequence == null) {
            this.mBackgroundNetworkFetchToEncodedMemorySequence = this.mProducerFactory.newBackgroundThreadHandoffProducer(getCommonNetworkFetchToEncodedMemorySequence(), this.mThreadHandoffProducerQueue);
        }
        return this.mBackgroundNetworkFetchToEncodedMemorySequence;
    }

    private synchronized Producer<Void> getNetworkFetchToEncodedMemoryPrefetchSequence() {
        if (this.mNetworkFetchToEncodedMemoryPrefetchSequence == null) {
            this.mNetworkFetchToEncodedMemoryPrefetchSequence = ProducerFactory.newSwallowResultProducer(getBackgroundNetworkFetchToEncodedMemorySequence());
        }
        return this.mNetworkFetchToEncodedMemoryPrefetchSequence;
    }

    private synchronized Producer<EncodedImage> getCommonNetworkFetchToEncodedMemorySequence() {
        if (this.mCommonNetworkFetchToEncodedMemorySequence == null) {
            AddImageTransformMetaDataProducer addImageTransformMetaDataProducerNewAddImageTransformMetaDataProducer = ProducerFactory.newAddImageTransformMetaDataProducer(newEncodedCacheMultiplexToTranscodeSequence(this.mProducerFactory.newNetworkFetchProducer(this.mNetworkFetcher)));
            this.mCommonNetworkFetchToEncodedMemorySequence = addImageTransformMetaDataProducerNewAddImageTransformMetaDataProducer;
            if (this.mResizeAndRotateEnabledForNetwork && !this.mDownsampleEnabled) {
                this.mCommonNetworkFetchToEncodedMemorySequence = this.mProducerFactory.newResizeAndRotateProducer(addImageTransformMetaDataProducerNewAddImageTransformMetaDataProducer);
            }
        }
        return this.mCommonNetworkFetchToEncodedMemorySequence;
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getLocalImageFileFetchSequence() {
        if (this.mLocalImageFileFetchSequence == null) {
            this.mLocalImageFileFetchSequence = newBitmapCacheGetToLocalTransformSequence(this.mProducerFactory.newLocalFileFetchProducer());
        }
        return this.mLocalImageFileFetchSequence;
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getLocalVideoFileFetchSequence() {
        if (this.mLocalVideoFileFetchSequence == null) {
            this.mLocalVideoFileFetchSequence = newBitmapCacheGetToBitmapCacheSequence(this.mProducerFactory.newLocalVideoThumbnailProducer());
        }
        return this.mLocalVideoFileFetchSequence;
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getLocalContentUriFetchSequence() {
        if (this.mLocalContentUriFetchSequence == null) {
            this.mLocalContentUriFetchSequence = newBitmapCacheGetToLocalTransformSequence(this.mProducerFactory.newContentUriFetchProducer());
        }
        return this.mLocalContentUriFetchSequence;
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getLocalResourceFetchSequence() {
        if (this.mLocalResourceFetchSequence == null) {
            this.mLocalResourceFetchSequence = newBitmapCacheGetToLocalTransformSequence(this.mProducerFactory.newLocalResourceFetchProducer());
        }
        return this.mLocalResourceFetchSequence;
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getLocalAssetFetchSequence() {
        if (this.mLocalAssetFetchSequence == null) {
            this.mLocalAssetFetchSequence = newBitmapCacheGetToLocalTransformSequence(this.mProducerFactory.newLocalAssetFetchProducer());
        }
        return this.mLocalAssetFetchSequence;
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getDataFetchSequence() {
        if (this.mDataFetchSequence == null) {
            Producer<EncodedImage> producerNewAddImageTransformMetaDataProducer = ProducerFactory.newAddImageTransformMetaDataProducer(this.mProducerFactory.newDataFetchProducer());
            if (!this.mDownsampleEnabled) {
                producerNewAddImageTransformMetaDataProducer = this.mProducerFactory.newResizeAndRotateProducer(producerNewAddImageTransformMetaDataProducer);
            }
            this.mDataFetchSequence = newBitmapCacheGetToDecodeSequence(producerNewAddImageTransformMetaDataProducer);
        }
        return this.mDataFetchSequence;
    }

    private Producer<CloseableReference<CloseableImage>> newBitmapCacheGetToLocalTransformSequence(Producer<EncodedImage> producer) {
        return newBitmapCacheGetToDecodeSequence(newLocalTransformationsSequence(newEncodedCacheMultiplexToTranscodeSequence(producer)));
    }

    private Producer<CloseableReference<CloseableImage>> newBitmapCacheGetToDecodeSequence(Producer<EncodedImage> producer) {
        return newBitmapCacheGetToBitmapCacheSequence(this.mProducerFactory.newDecodeProducer(producer));
    }

    private Producer<EncodedImage> newEncodedCacheMultiplexToTranscodeSequence(Producer<EncodedImage> producer) {
        return this.mProducerFactory.newEncodedCacheKeyMultiplexProducer(this.mProducerFactory.newEncodedMemoryCacheProducer(this.mProducerFactory.newDiskCacheProducer(producer)));
    }

    private Producer<CloseableReference<CloseableImage>> newBitmapCacheGetToBitmapCacheSequence(Producer<CloseableReference<CloseableImage>> producer) {
        return this.mProducerFactory.newBitmapMemoryCacheGetProducer(this.mProducerFactory.newBackgroundThreadHandoffProducer(this.mProducerFactory.newBitmapMemoryCacheKeyMultiplexProducer(this.mProducerFactory.newBitmapMemoryCacheProducer(producer)), this.mThreadHandoffProducerQueue));
    }

    private Producer<EncodedImage> newLocalTransformationsSequence(Producer<EncodedImage> producer) {
        Producer<EncodedImage> producerNewAddImageTransformMetaDataProducer = ProducerFactory.newAddImageTransformMetaDataProducer(producer);
        if (!this.mDownsampleEnabled) {
            producerNewAddImageTransformMetaDataProducer = this.mProducerFactory.newResizeAndRotateProducer(producerNewAddImageTransformMetaDataProducer);
        }
        ThrottlingProducer throttlingProducerNewThrottlingProducer = this.mProducerFactory.newThrottlingProducer(5, producerNewAddImageTransformMetaDataProducer);
        Producer<EncodedImage> producerNewLocalExifThumbnailProducer = this.mProducerFactory.newLocalExifThumbnailProducer();
        if (!this.mDownsampleEnabled) {
            producerNewLocalExifThumbnailProducer = this.mProducerFactory.newResizeAndRotateProducer(producerNewLocalExifThumbnailProducer);
        }
        return ProducerFactory.newBranchOnSeparateImagesProducer(producerNewLocalExifThumbnailProducer, throttlingProducerNewThrottlingProducer);
    }

    private synchronized Producer<CloseableReference<CloseableImage>> getPostprocessorSequence(Producer<CloseableReference<CloseableImage>> producer) {
        if (!this.mPostprocessorSequences.containsKey(producer)) {
            this.mPostprocessorSequences.put(producer, this.mProducerFactory.newPostprocessorBitmapMemoryCacheProducer(this.mProducerFactory.newPostprocessorProducer(producer)));
        }
        return this.mPostprocessorSequences.get(producer);
    }

    private synchronized Producer<Void> getDecodedImagePrefetchSequence(Producer<CloseableReference<CloseableImage>> producer) {
        if (!this.mCloseableImagePrefetchSequences.containsKey(producer)) {
            this.mCloseableImagePrefetchSequences.put(producer, ProducerFactory.newSwallowResultProducer(producer));
        }
        return this.mCloseableImagePrefetchSequences.get(producer);
    }
}
