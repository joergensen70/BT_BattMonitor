package com.facebook.imagepipeline.cache;

import bolts.Task;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.WriterCallback;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.memory.PooledByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: loaded from: classes.dex */
public class BufferedDiskCache {
    private static final Class<?> TAG = BufferedDiskCache.class;
    private final FileCache mFileCache;
    private final ImageCacheStatsTracker mImageCacheStatsTracker;
    private final PooledByteBufferFactory mPooledByteBufferFactory;
    private final PooledByteStreams mPooledByteStreams;
    private final Executor mReadExecutor;
    private final StagingArea mStagingArea = StagingArea.getInstance();
    private final Executor mWriteExecutor;

    public BufferedDiskCache(FileCache fileCache, PooledByteBufferFactory pooledByteBufferFactory, PooledByteStreams pooledByteStreams, Executor executor, Executor executor2, ImageCacheStatsTracker imageCacheStatsTracker) {
        this.mFileCache = fileCache;
        this.mPooledByteBufferFactory = pooledByteBufferFactory;
        this.mPooledByteStreams = pooledByteStreams;
        this.mReadExecutor = executor;
        this.mWriteExecutor = executor2;
        this.mImageCacheStatsTracker = imageCacheStatsTracker;
    }

    public Task<Boolean> contains(final CacheKey cacheKey) {
        Preconditions.checkNotNull(cacheKey);
        EncodedImage encodedImage = this.mStagingArea.get(cacheKey);
        if (encodedImage != null) {
            encodedImage.close();
            FLog.v(TAG, "Found image for %s in staging area", cacheKey.toString());
            this.mImageCacheStatsTracker.onStagingAreaHit();
            return Task.forResult(true);
        }
        try {
            return Task.call(new Callable<Boolean>() { // from class: com.facebook.imagepipeline.cache.BufferedDiskCache.1
                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.concurrent.Callable
                public Boolean call() throws Exception {
                    EncodedImage encodedImage2 = BufferedDiskCache.this.mStagingArea.get(cacheKey);
                    if (encodedImage2 == null) {
                        FLog.v((Class<?>) BufferedDiskCache.TAG, "Did not find image for %s in staging area", cacheKey.toString());
                        BufferedDiskCache.this.mImageCacheStatsTracker.onStagingAreaMiss();
                        try {
                            return Boolean.valueOf(BufferedDiskCache.this.mFileCache.hasKey(cacheKey));
                        } catch (Exception unused) {
                            return false;
                        }
                    }
                    encodedImage2.close();
                    FLog.v((Class<?>) BufferedDiskCache.TAG, "Found image for %s in staging area", cacheKey.toString());
                    BufferedDiskCache.this.mImageCacheStatsTracker.onStagingAreaHit();
                    return true;
                }
            }, this.mReadExecutor);
        } catch (Exception e) {
            FLog.w(TAG, e, "Failed to schedule disk-cache read for %s", cacheKey.toString());
            return Task.forError(e);
        }
    }

    public Task<EncodedImage> get(final CacheKey cacheKey, final AtomicBoolean atomicBoolean) {
        Preconditions.checkNotNull(cacheKey);
        Preconditions.checkNotNull(atomicBoolean);
        EncodedImage encodedImage = this.mStagingArea.get(cacheKey);
        if (encodedImage != null) {
            FLog.v(TAG, "Found image for %s in staging area", cacheKey.toString());
            this.mImageCacheStatsTracker.onStagingAreaHit();
            return Task.forResult(encodedImage);
        }
        try {
            return Task.call(new Callable<EncodedImage>() { // from class: com.facebook.imagepipeline.cache.BufferedDiskCache.2
                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.concurrent.Callable
                public EncodedImage call() throws Exception {
                    if (!atomicBoolean.get()) {
                        EncodedImage encodedImage2 = BufferedDiskCache.this.mStagingArea.get(cacheKey);
                        if (encodedImage2 != null) {
                            FLog.v((Class<?>) BufferedDiskCache.TAG, "Found image for %s in staging area", cacheKey.toString());
                            BufferedDiskCache.this.mImageCacheStatsTracker.onStagingAreaHit();
                        } else {
                            FLog.v((Class<?>) BufferedDiskCache.TAG, "Did not find image for %s in staging area", cacheKey.toString());
                            BufferedDiskCache.this.mImageCacheStatsTracker.onStagingAreaMiss();
                            try {
                                CloseableReference closeableReferenceOf = CloseableReference.of(BufferedDiskCache.this.readFromDiskCache(cacheKey));
                                try {
                                    encodedImage2 = new EncodedImage((CloseableReference<PooledByteBuffer>) closeableReferenceOf);
                                } finally {
                                    CloseableReference.closeSafely((CloseableReference<?>) closeableReferenceOf);
                                }
                            } catch (Exception unused) {
                                return null;
                            }
                        }
                        if (!Thread.interrupted()) {
                            return encodedImage2;
                        }
                        FLog.v((Class<?>) BufferedDiskCache.TAG, "Host thread was interrupted, decreasing reference count");
                        if (encodedImage2 != null) {
                            encodedImage2.close();
                        }
                        throw new InterruptedException();
                    }
                    throw new CancellationException();
                }
            }, this.mReadExecutor);
        } catch (Exception e) {
            FLog.w(TAG, e, "Failed to schedule disk-cache read for %s", cacheKey.toString());
            return Task.forError(e);
        }
    }

    public void put(final CacheKey cacheKey, EncodedImage encodedImage) {
        Preconditions.checkNotNull(cacheKey);
        Preconditions.checkArgument(EncodedImage.isValid(encodedImage));
        this.mStagingArea.put(cacheKey, encodedImage);
        final EncodedImage encodedImageCloneOrNull = EncodedImage.cloneOrNull(encodedImage);
        try {
            this.mWriteExecutor.execute(new Runnable() { // from class: com.facebook.imagepipeline.cache.BufferedDiskCache.3
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        BufferedDiskCache.this.writeToDiskCache(cacheKey, encodedImageCloneOrNull);
                    } finally {
                        BufferedDiskCache.this.mStagingArea.remove(cacheKey, encodedImageCloneOrNull);
                        EncodedImage.closeSafely(encodedImageCloneOrNull);
                    }
                }
            });
        } catch (Exception e) {
            FLog.w(TAG, e, "Failed to schedule disk-cache write for %s", cacheKey.toString());
            this.mStagingArea.remove(cacheKey, encodedImage);
            EncodedImage.closeSafely(encodedImageCloneOrNull);
        }
    }

    public Task<Void> remove(final CacheKey cacheKey) {
        Preconditions.checkNotNull(cacheKey);
        this.mStagingArea.remove(cacheKey);
        try {
            return Task.call(new Callable<Void>() { // from class: com.facebook.imagepipeline.cache.BufferedDiskCache.4
                @Override // java.util.concurrent.Callable
                public Void call() throws Exception {
                    BufferedDiskCache.this.mStagingArea.remove(cacheKey);
                    BufferedDiskCache.this.mFileCache.remove(cacheKey);
                    return null;
                }
            }, this.mWriteExecutor);
        } catch (Exception e) {
            FLog.w(TAG, e, "Failed to schedule disk-cache remove for %s", cacheKey.toString());
            return Task.forError(e);
        }
    }

    public Task<Void> clearAll() {
        this.mStagingArea.clearAll();
        try {
            return Task.call(new Callable<Void>() { // from class: com.facebook.imagepipeline.cache.BufferedDiskCache.5
                @Override // java.util.concurrent.Callable
                public Void call() throws Exception {
                    BufferedDiskCache.this.mStagingArea.clearAll();
                    BufferedDiskCache.this.mFileCache.clearAll();
                    return null;
                }
            }, this.mWriteExecutor);
        } catch (Exception e) {
            FLog.w(TAG, e, "Failed to schedule disk-cache clear", new Object[0]);
            return Task.forError(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public PooledByteBuffer readFromDiskCache(CacheKey cacheKey) throws IOException {
        try {
            Class<?> cls = TAG;
            FLog.v(cls, "Disk cache read for %s", cacheKey.toString());
            BinaryResource resource = this.mFileCache.getResource(cacheKey);
            if (resource == null) {
                FLog.v(cls, "Disk cache miss for %s", cacheKey.toString());
                this.mImageCacheStatsTracker.onDiskCacheMiss();
                return null;
            }
            FLog.v(cls, "Found entry in disk cache for %s", cacheKey.toString());
            this.mImageCacheStatsTracker.onDiskCacheHit();
            InputStream inputStreamOpenStream = resource.openStream();
            try {
                PooledByteBuffer pooledByteBufferNewByteBuffer = this.mPooledByteBufferFactory.newByteBuffer(inputStreamOpenStream, (int) resource.size());
                inputStreamOpenStream.close();
                FLog.v(cls, "Successful read from disk cache for %s", cacheKey.toString());
                return pooledByteBufferNewByteBuffer;
            } catch (Throwable th) {
                inputStreamOpenStream.close();
                throw th;
            }
        } catch (IOException e) {
            FLog.w(TAG, e, "Exception reading from cache for %s", cacheKey.toString());
            this.mImageCacheStatsTracker.onDiskCacheGetFail();
            throw e;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeToDiskCache(CacheKey cacheKey, final EncodedImage encodedImage) {
        Class<?> cls = TAG;
        FLog.v(cls, "About to write to disk-cache for key %s", cacheKey.toString());
        try {
            this.mFileCache.insert(cacheKey, new WriterCallback() { // from class: com.facebook.imagepipeline.cache.BufferedDiskCache.6
                @Override // com.facebook.cache.common.WriterCallback
                public void write(OutputStream outputStream) throws IOException {
                    BufferedDiskCache.this.mPooledByteStreams.copy(encodedImage.getInputStream(), outputStream);
                }
            });
            FLog.v(cls, "Successful disk-cache write for key %s", cacheKey.toString());
        } catch (IOException e) {
            FLog.w(TAG, e, "Failed to write to disk-cache for key %s", cacheKey.toString());
        }
    }
}
