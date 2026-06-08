package com.facebook.cache.disk;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheErrorLogger;
import com.facebook.cache.common.CacheEventListener;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.WriterCallback;
import com.facebook.cache.disk.DiskStorage;
import com.facebook.common.disk.DiskTrimmable;
import com.facebook.common.disk.DiskTrimmableRegistry;
import com.facebook.common.logging.FLog;
import com.facebook.common.statfs.StatFsHelper;
import com.facebook.common.time.Clock;
import com.facebook.common.time.SystemClock;
import com.facebook.common.util.SecureHashUtil;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class DiskStorageCache implements FileCache, DiskTrimmable {
    public static final int START_OF_VERSIONING = 1;
    private static final double TRIMMING_LOWER_BOUND = 0.02d;
    private static final long UNINITIALIZED = -1;
    private final CacheErrorLogger mCacheErrorLogger;
    private final CacheEventListener mCacheEventListener;
    private long mCacheSizeLimit;
    private final long mCacheSizeLimitMinimum;
    private final Clock mClock;
    private final long mDefaultCacheSizeLimit;
    private final EntryEvictionComparatorSupplier mEntryEvictionComparatorSupplier;
    private final long mLowDiskSpaceCacheSizeLimit;
    private final DiskStorageSupplier mStorageSupplier;
    private static final Class<?> TAG = DiskStorageCache.class;
    private static final long FUTURE_TIMESTAMP_THRESHOLD_MS = TimeUnit.HOURS.toMillis(2);
    private static final long FILECACHE_SIZE_UPDATE_PERIOD_MS = TimeUnit.MINUTES.toMillis(30);
    private final Object mLock = new Object();
    private final StatFsHelper mStatFsHelper = StatFsHelper.getInstance();
    private long mCacheSizeLastUpdateTime = -1;
    private final CacheStats mCacheStats = new CacheStats();

    static class CacheStats {
        private boolean mInitialized = false;
        private long mSize = -1;
        private long mCount = -1;

        CacheStats() {
        }

        public synchronized boolean isInitialized() {
            return this.mInitialized;
        }

        public synchronized void reset() {
            this.mInitialized = false;
            this.mCount = -1L;
            this.mSize = -1L;
        }

        public synchronized void set(long j, long j2) {
            this.mCount = j2;
            this.mSize = j;
            this.mInitialized = true;
        }

        public synchronized void increment(long j, long j2) {
            if (this.mInitialized) {
                this.mSize += j;
                this.mCount += j2;
            }
        }

        public synchronized long getSize() {
            return this.mSize;
        }

        public synchronized long getCount() {
            return this.mCount;
        }
    }

    public static class Params {
        public final long mCacheSizeLimitMinimum;
        public final long mDefaultCacheSizeLimit;
        public final long mLowDiskSpaceCacheSizeLimit;

        public Params(long j, long j2, long j3) {
            this.mCacheSizeLimitMinimum = j;
            this.mLowDiskSpaceCacheSizeLimit = j2;
            this.mDefaultCacheSizeLimit = j3;
        }
    }

    public DiskStorageCache(DiskStorageSupplier diskStorageSupplier, EntryEvictionComparatorSupplier entryEvictionComparatorSupplier, Params params, CacheEventListener cacheEventListener, CacheErrorLogger cacheErrorLogger, @Nullable DiskTrimmableRegistry diskTrimmableRegistry) {
        this.mLowDiskSpaceCacheSizeLimit = params.mLowDiskSpaceCacheSizeLimit;
        this.mDefaultCacheSizeLimit = params.mDefaultCacheSizeLimit;
        this.mCacheSizeLimit = params.mDefaultCacheSizeLimit;
        this.mStorageSupplier = diskStorageSupplier;
        this.mEntryEvictionComparatorSupplier = entryEvictionComparatorSupplier;
        this.mCacheEventListener = cacheEventListener;
        this.mCacheSizeLimitMinimum = params.mCacheSizeLimitMinimum;
        this.mCacheErrorLogger = cacheErrorLogger;
        if (diskTrimmableRegistry != null) {
            diskTrimmableRegistry.registerDiskTrimmable(this);
        }
        this.mClock = SystemClock.get();
    }

    @Override // com.facebook.cache.disk.FileCache
    public DiskStorage.DiskDumpInfo getDumpInfo() throws IOException {
        return this.mStorageSupplier.get().getDumpInfo();
    }

    @Override // com.facebook.cache.disk.FileCache
    public boolean isEnabled() {
        try {
            return this.mStorageSupplier.get().isEnabled();
        } catch (IOException unused) {
            return false;
        }
    }

    @Override // com.facebook.cache.disk.FileCache
    public BinaryResource getResource(CacheKey cacheKey) {
        FileBinaryResource resource;
        try {
            synchronized (this.mLock) {
                resource = this.mStorageSupplier.get().getResource(getResourceId(cacheKey), cacheKey);
                if (resource == null) {
                    this.mCacheEventListener.onMiss();
                } else {
                    this.mCacheEventListener.onHit();
                }
            }
            return resource;
        } catch (IOException e) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.GENERIC_IO, TAG, "getResource", e);
            this.mCacheEventListener.onReadException();
            return null;
        }
    }

    @Override // com.facebook.cache.disk.FileCache
    public boolean probe(CacheKey cacheKey) {
        boolean z;
        try {
            synchronized (this.mLock) {
                z = this.mStorageSupplier.get().touch(getResourceId(cacheKey), cacheKey);
            }
            return z;
        } catch (IOException unused) {
            this.mCacheEventListener.onReadException();
            return false;
        }
    }

    private FileBinaryResource createTemporaryResource(String str, CacheKey cacheKey) throws IOException {
        maybeEvictFilesInCacheDir();
        return this.mStorageSupplier.get().createTemporary(str, cacheKey);
    }

    private void deleteTemporaryResource(FileBinaryResource fileBinaryResource) {
        File file = fileBinaryResource.getFile();
        if (file.exists()) {
            Class<?> cls = TAG;
            FLog.e(cls, "Temp file still on disk: %s ", file);
            if (file.delete()) {
                return;
            }
            FLog.e(cls, "Failed to delete temp file: %s", file);
        }
    }

    private FileBinaryResource commitResource(String str, CacheKey cacheKey, FileBinaryResource fileBinaryResource) throws IOException {
        FileBinaryResource fileBinaryResourceCommit;
        synchronized (this.mLock) {
            fileBinaryResourceCommit = this.mStorageSupplier.get().commit(str, fileBinaryResource, cacheKey);
            this.mCacheStats.increment(fileBinaryResourceCommit.size(), 1L);
        }
        return fileBinaryResourceCommit;
    }

    @Override // com.facebook.cache.disk.FileCache
    public BinaryResource insert(CacheKey cacheKey, WriterCallback writerCallback) throws IOException {
        this.mCacheEventListener.onWriteAttempt();
        String resourceId = getResourceId(cacheKey);
        try {
            FileBinaryResource fileBinaryResourceCreateTemporaryResource = createTemporaryResource(resourceId, cacheKey);
            try {
                this.mStorageSupplier.get().updateResource(resourceId, fileBinaryResourceCreateTemporaryResource, writerCallback, cacheKey);
                return commitResource(resourceId, cacheKey, fileBinaryResourceCreateTemporaryResource);
            } finally {
                deleteTemporaryResource(fileBinaryResourceCreateTemporaryResource);
            }
        } catch (IOException e) {
            this.mCacheEventListener.onWriteException();
            FLog.e(TAG, "Failed inserting a file into the cache", e);
            throw e;
        }
    }

    @Override // com.facebook.cache.disk.FileCache
    public void remove(CacheKey cacheKey) {
        synchronized (this.mLock) {
            try {
                this.mStorageSupplier.get().remove(getResourceId(cacheKey));
            } catch (IOException e) {
                this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.DELETE_FILE, TAG, "delete: " + e.getMessage(), e);
            }
        }
    }

    @Override // com.facebook.cache.disk.FileCache
    public long clearOldEntries(long j) {
        long j2;
        long jMax;
        synchronized (this.mLock) {
            try {
                long jNow = this.mClock.now();
                DiskStorage diskStorage = this.mStorageSupplier.get();
                int i = 0;
                long j3 = 0;
                jMax = 0;
                for (DiskStorage.Entry entry : diskStorage.getEntries()) {
                    try {
                        long jMax2 = Math.max(1L, Math.abs(jNow - entry.getTimestamp()));
                        if (jMax2 >= j) {
                            long jRemove = diskStorage.remove(entry);
                            if (jRemove > 0) {
                                i++;
                                j3 += jRemove;
                            }
                        } else {
                            jMax = Math.max(jMax, jMax2);
                        }
                    } catch (IOException e) {
                        e = e;
                        j2 = jMax;
                        this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.EVICTION, TAG, "clearOldEntries: " + e.getMessage(), e);
                        jMax = j2;
                    }
                }
                diskStorage.purgeUnexpectedResources();
                if (i > 0) {
                    maybeUpdateFileCacheSize();
                    this.mCacheStats.increment(-j3, -i);
                    reportEviction(CacheEventListener.EvictionReason.CONTENT_STALE, i, j3);
                }
            } catch (IOException e2) {
                e = e2;
                j2 = 0;
            }
        }
        return jMax;
    }

    private void reportEviction(CacheEventListener.EvictionReason evictionReason, int i, long j) {
        this.mCacheEventListener.onEviction(evictionReason, i, j);
    }

    private void maybeEvictFilesInCacheDir() throws IOException {
        synchronized (this.mLock) {
            boolean zMaybeUpdateFileCacheSize = maybeUpdateFileCacheSize();
            updateFileCacheSizeLimit();
            long size = this.mCacheStats.getSize();
            if (size > this.mCacheSizeLimit && !zMaybeUpdateFileCacheSize) {
                this.mCacheStats.reset();
                maybeUpdateFileCacheSize();
            }
            long j = this.mCacheSizeLimit;
            if (size > j) {
                evictAboveSize((j * 9) / 10, CacheEventListener.EvictionReason.CACHE_FULL);
            }
        }
    }

    private void evictAboveSize(long j, CacheEventListener.EvictionReason evictionReason) throws IOException {
        DiskStorage diskStorage = this.mStorageSupplier.get();
        try {
            Collection<DiskStorage.Entry> sortedEntries = getSortedEntries(diskStorage.getEntries());
            long size = this.mCacheStats.getSize() - j;
            int i = 0;
            long j2 = 0;
            for (DiskStorage.Entry entry : sortedEntries) {
                if (j2 > size) {
                    break;
                }
                long jRemove = diskStorage.remove(entry);
                if (jRemove > 0) {
                    i++;
                    j2 += jRemove;
                }
            }
            this.mCacheStats.increment(-j2, -i);
            diskStorage.purgeUnexpectedResources();
            reportEviction(evictionReason, i, j2);
        } catch (IOException e) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.EVICTION, TAG, "evictAboveSize: " + e.getMessage(), e);
            throw e;
        }
    }

    private Collection<DiskStorage.Entry> getSortedEntries(Collection<DiskStorage.Entry> collection) {
        long jNow = this.mClock.now() + FUTURE_TIMESTAMP_THRESHOLD_MS;
        ArrayList arrayList = new ArrayList(collection.size());
        ArrayList arrayList2 = new ArrayList(collection.size());
        for (DiskStorage.Entry entry : collection) {
            if (entry.getTimestamp() > jNow) {
                arrayList.add(entry);
            } else {
                arrayList2.add(entry);
            }
        }
        Collections.sort(arrayList2, this.mEntryEvictionComparatorSupplier.get());
        arrayList.addAll(arrayList2);
        return arrayList;
    }

    private void updateFileCacheSizeLimit() {
        if (this.mStatFsHelper.testLowDiskSpace(StatFsHelper.StorageType.INTERNAL, this.mDefaultCacheSizeLimit - this.mCacheStats.getSize())) {
            this.mCacheSizeLimit = this.mLowDiskSpaceCacheSizeLimit;
        } else {
            this.mCacheSizeLimit = this.mDefaultCacheSizeLimit;
        }
    }

    @Override // com.facebook.cache.disk.FileCache
    public long getSize() {
        return this.mCacheStats.getSize();
    }

    @Override // com.facebook.cache.disk.FileCache
    public void clearAll() {
        synchronized (this.mLock) {
            try {
                this.mStorageSupplier.get().clearAll();
            } catch (IOException e) {
                this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.EVICTION, TAG, "clearAll: " + e.getMessage(), e);
            }
            this.mCacheStats.reset();
        }
    }

    @Override // com.facebook.cache.disk.FileCache
    public boolean hasKey(CacheKey cacheKey) {
        try {
            return this.mStorageSupplier.get().contains(getResourceId(cacheKey), cacheKey);
        } catch (IOException unused) {
            return false;
        }
    }

    @Override // com.facebook.common.disk.DiskTrimmable
    public void trimToMinimum() {
        synchronized (this.mLock) {
            maybeUpdateFileCacheSize();
            long size = this.mCacheStats.getSize();
            long j = this.mCacheSizeLimitMinimum;
            if (j > 0 && size > 0 && size >= j) {
                double d = 1.0d - (j / size);
                if (d > TRIMMING_LOWER_BOUND) {
                    trimBy(d);
                }
            }
        }
    }

    @Override // com.facebook.common.disk.DiskTrimmable
    public void trimToNothing() {
        clearAll();
    }

    private void trimBy(double d) {
        synchronized (this.mLock) {
            try {
                this.mCacheStats.reset();
                maybeUpdateFileCacheSize();
                long size = this.mCacheStats.getSize();
                evictAboveSize(size - ((long) (d * size)), CacheEventListener.EvictionReason.CACHE_MANAGER_TRIMMED);
            } catch (IOException e) {
                this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.EVICTION, TAG, "trimBy: " + e.getMessage(), e);
            }
        }
    }

    private boolean maybeUpdateFileCacheSize() {
        long jElapsedRealtime = android.os.SystemClock.elapsedRealtime();
        if (this.mCacheStats.isInitialized()) {
            long j = this.mCacheSizeLastUpdateTime;
            if (j != -1 && jElapsedRealtime - j <= FILECACHE_SIZE_UPDATE_PERIOD_MS) {
                return false;
            }
        }
        calcFileCacheSize();
        this.mCacheSizeLastUpdateTime = jElapsedRealtime;
        return true;
    }

    private void calcFileCacheSize() {
        long j;
        long jNow = this.mClock.now();
        long j2 = FUTURE_TIMESTAMP_THRESHOLD_MS + jNow;
        try {
            long size = 0;
            boolean z = false;
            int size2 = 0;
            long jMax = -1;
            int i = 0;
            int i2 = 0;
            for (DiskStorage.Entry entry : this.mStorageSupplier.get().getEntries()) {
                i++;
                size += entry.getSize();
                if (entry.getTimestamp() > j2) {
                    i2++;
                    j = jNow;
                    size2 = (int) (((long) size2) + entry.getSize());
                    z = true;
                    jMax = Math.max(entry.getTimestamp() - j, jMax);
                } else {
                    j = jNow;
                }
                jNow = j;
            }
            if (z) {
                this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.READ_INVALID_ENTRY, TAG, "Future timestamp found in " + i2 + " files , with a total size of " + size2 + " bytes, and a maximum time delta of " + jMax + "ms", null);
            }
            this.mCacheStats.set(size, i);
        } catch (IOException e) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.GENERIC_IO, TAG, "calcFileCacheSize: " + e.getMessage(), e);
        }
    }

    String getResourceId(CacheKey cacheKey) {
        try {
            return SecureHashUtil.makeSHA1HashBase64(cacheKey.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
