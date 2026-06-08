package com.facebook.cache.disk;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheErrorLogger;
import com.facebook.cache.common.WriterCallback;
import com.facebook.cache.disk.DiskStorage;
import com.facebook.common.file.FileTree;
import com.facebook.common.file.FileTreeVisitor;
import com.facebook.common.file.FileUtils;
import com.facebook.common.internal.CountingOutputStream;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.time.Clock;
import com.facebook.common.time.SystemClock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes.dex */
public class DefaultDiskStorage implements DiskStorage {
    private static final String CONTENT_FILE_EXTENSION = ".cnt";
    private static final String DEFAULT_DISK_STORAGE_VERSION_PREFIX = "v2";
    private static final int SHARDING_BUCKET_COUNT = 100;
    private static final String TEMP_FILE_EXTENSION = ".tmp";
    private final CacheErrorLogger mCacheErrorLogger;
    private final Clock mClock;
    private final File mRootDirectory;
    private final File mVersionDirectory;
    private static final Class<?> TAG = DefaultDiskStorage.class;
    static final long TEMP_FILE_LIFETIME_MS = TimeUnit.MINUTES.toMillis(30);

    @Override // com.facebook.cache.disk.DiskStorage
    public boolean isEnabled() {
        return true;
    }

    public DefaultDiskStorage(File file, int i, CacheErrorLogger cacheErrorLogger) {
        Preconditions.checkNotNull(file);
        this.mRootDirectory = file;
        this.mVersionDirectory = new File(file, getVersionSubdirectoryName(i));
        this.mCacheErrorLogger = cacheErrorLogger;
        recreateDirectoryIfVersionChanges();
        this.mClock = SystemClock.get();
    }

    static String getVersionSubdirectoryName(int i) {
        return String.format(null, "%s.ols%d.%d", DEFAULT_DISK_STORAGE_VERSION_PREFIX, 100, Integer.valueOf(i));
    }

    private void recreateDirectoryIfVersionChanges() {
        if (this.mRootDirectory.exists()) {
            if (this.mVersionDirectory.exists()) {
                return;
            } else {
                FileTree.deleteRecursively(this.mRootDirectory);
            }
        }
        try {
            FileUtils.mkdirs(this.mVersionDirectory);
        } catch (FileUtils.CreateDirectoryException unused) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.WRITE_CREATE_DIR, TAG, "version directory could not be created: " + this.mVersionDirectory, null);
        }
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public void updateResource(String str, FileBinaryResource fileBinaryResource, WriterCallback writerCallback, Object obj) throws IOException {
        File file = fileBinaryResource.getFile();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                CountingOutputStream countingOutputStream = new CountingOutputStream(fileOutputStream);
                writerCallback.write(countingOutputStream);
                countingOutputStream.flush();
                long count = countingOutputStream.getCount();
                fileOutputStream.close();
                if (file.length() != count) {
                    throw new IncompleteFileException(count, file.length());
                }
            } catch (Throwable th) {
                fileOutputStream.close();
                throw th;
            }
        } catch (FileNotFoundException e) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.WRITE_UPDATE_FILE_NOT_FOUND, TAG, "updateResource", e);
            throw e;
        }
    }

    private static class IncompleteFileException extends IOException {
        public final long actual;
        public final long expected;

        public IncompleteFileException(long j, long j2) {
            super("File was not written completely. Expected: " + j + ", found: " + j2);
            this.expected = j;
            this.actual = j2;
        }
    }

    File getContentFileFor(String str) {
        return new File(getFilename(str));
    }

    private String getSubdirectoryPath(String str) {
        return this.mVersionDirectory + File.separator + String.valueOf(Math.abs(str.hashCode() % 100));
    }

    private File getSubdirectory(String str) {
        return new File(getSubdirectoryPath(str));
    }

    private class EntriesCollector implements FileTreeVisitor {
        private final List<DiskStorage.Entry> result;

        @Override // com.facebook.common.file.FileTreeVisitor
        public void postVisitDirectory(File file) {
        }

        @Override // com.facebook.common.file.FileTreeVisitor
        public void preVisitDirectory(File file) {
        }

        private EntriesCollector() {
            this.result = new ArrayList();
        }

        @Override // com.facebook.common.file.FileTreeVisitor
        public void visitFile(File file) {
            FileInfo shardFileInfo = DefaultDiskStorage.this.getShardFileInfo(file);
            if (shardFileInfo == null || shardFileInfo.type != FileType.CONTENT) {
                return;
            }
            this.result.add(new EntryImpl(file));
        }

        public List<DiskStorage.Entry> getEntries() {
            return Collections.unmodifiableList(this.result);
        }
    }

    private class PurgingVisitor implements FileTreeVisitor {
        private boolean insideBaseDirectory;

        private PurgingVisitor() {
        }

        @Override // com.facebook.common.file.FileTreeVisitor
        public void preVisitDirectory(File file) {
            if (this.insideBaseDirectory || !file.equals(DefaultDiskStorage.this.mVersionDirectory)) {
                return;
            }
            this.insideBaseDirectory = true;
        }

        @Override // com.facebook.common.file.FileTreeVisitor
        public void visitFile(File file) {
            if (this.insideBaseDirectory && isExpectedFile(file)) {
                return;
            }
            file.delete();
        }

        @Override // com.facebook.common.file.FileTreeVisitor
        public void postVisitDirectory(File file) {
            if (!DefaultDiskStorage.this.mRootDirectory.equals(file) && !this.insideBaseDirectory) {
                file.delete();
            }
            if (this.insideBaseDirectory && file.equals(DefaultDiskStorage.this.mVersionDirectory)) {
                this.insideBaseDirectory = false;
            }
        }

        private boolean isExpectedFile(File file) {
            FileInfo shardFileInfo = DefaultDiskStorage.this.getShardFileInfo(file);
            if (shardFileInfo == null) {
                return false;
            }
            if (shardFileInfo.type == FileType.TEMP) {
                return isRecentFile(file);
            }
            Preconditions.checkState(shardFileInfo.type == FileType.CONTENT);
            return true;
        }

        private boolean isRecentFile(File file) {
            return file.lastModified() > DefaultDiskStorage.this.mClock.now() - DefaultDiskStorage.TEMP_FILE_LIFETIME_MS;
        }
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public void purgeUnexpectedResources() {
        FileTree.walkFileTree(this.mRootDirectory, new PurgingVisitor());
    }

    private void mkdirs(File file, String str) throws IOException {
        try {
            FileUtils.mkdirs(file);
        } catch (FileUtils.CreateDirectoryException e) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.WRITE_CREATE_DIR, TAG, str, e);
            throw e;
        }
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public FileBinaryResource createTemporary(String str, Object obj) throws IOException {
        FileInfo fileInfo = new FileInfo(FileType.TEMP, str);
        File subdirectory = getSubdirectory(fileInfo.resourceId);
        if (!subdirectory.exists()) {
            mkdirs(subdirectory, "createTemporary");
        }
        try {
            return FileBinaryResource.createOrNull(fileInfo.createTempFile(subdirectory));
        } catch (IOException e) {
            this.mCacheErrorLogger.logError(CacheErrorLogger.CacheErrorCategory.WRITE_CREATE_TEMPFILE, TAG, "createTemporary", e);
            throw e;
        }
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public FileBinaryResource commit(String str, FileBinaryResource fileBinaryResource, Object obj) throws IOException {
        CacheErrorLogger.CacheErrorCategory cacheErrorCategory;
        File file = fileBinaryResource.getFile();
        File contentFileFor = getContentFileFor(str);
        try {
            FileUtils.rename(file, contentFileFor);
            if (contentFileFor.exists()) {
                contentFileFor.setLastModified(this.mClock.now());
            }
            return FileBinaryResource.createOrNull(contentFileFor);
        } catch (FileUtils.RenameException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cacheErrorCategory = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_OTHER;
            } else if (cause instanceof FileUtils.ParentDirNotFoundException) {
                cacheErrorCategory = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_TEMPFILE_PARENT_NOT_FOUND;
            } else if (cause instanceof FileNotFoundException) {
                cacheErrorCategory = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_TEMPFILE_NOT_FOUND;
            } else {
                cacheErrorCategory = CacheErrorLogger.CacheErrorCategory.WRITE_RENAME_FILE_OTHER;
            }
            this.mCacheErrorLogger.logError(cacheErrorCategory, TAG, "commit", e);
            throw e;
        }
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public FileBinaryResource getResource(String str, Object obj) {
        File contentFileFor = getContentFileFor(str);
        if (!contentFileFor.exists()) {
            return null;
        }
        contentFileFor.setLastModified(this.mClock.now());
        return FileBinaryResource.createOrNull(contentFileFor);
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public String getFilename(String str) {
        FileInfo fileInfo = new FileInfo(FileType.CONTENT, str);
        return fileInfo.toPath(getSubdirectoryPath(fileInfo.resourceId));
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public boolean contains(String str, Object obj) {
        return query(str, false);
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public boolean touch(String str, Object obj) {
        return query(str, true);
    }

    private boolean query(String str, boolean z) {
        File contentFileFor = getContentFileFor(str);
        boolean zExists = contentFileFor.exists();
        if (z && zExists) {
            contentFileFor.setLastModified(this.mClock.now());
        }
        return zExists;
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public long remove(DiskStorage.Entry entry) {
        return doRemove(((EntryImpl) entry).getResource().getFile());
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public long remove(String str) {
        return doRemove(getContentFileFor(str));
    }

    private long doRemove(File file) {
        if (!file.exists()) {
            return 0L;
        }
        long length = file.length();
        if (file.delete()) {
            return length;
        }
        return -1L;
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public void clearAll() {
        FileTree.deleteContents(this.mRootDirectory);
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public DiskStorage.DiskDumpInfo getDumpInfo() throws IOException {
        List<DiskStorage.Entry> entries = getEntries();
        DiskStorage.DiskDumpInfo diskDumpInfo = new DiskStorage.DiskDumpInfo();
        Iterator<DiskStorage.Entry> it = entries.iterator();
        while (it.hasNext()) {
            DiskStorage.DiskDumpInfoEntry diskDumpInfoEntryDumpCacheEntry = dumpCacheEntry(it.next());
            String str = diskDumpInfoEntryDumpCacheEntry.type;
            if (!diskDumpInfo.typeCounts.containsKey(str)) {
                diskDumpInfo.typeCounts.put(str, 0);
            }
            diskDumpInfo.typeCounts.put(str, Integer.valueOf(diskDumpInfo.typeCounts.get(str).intValue() + 1));
            diskDumpInfo.entries.add(diskDumpInfoEntryDumpCacheEntry);
        }
        return diskDumpInfo;
    }

    private DiskStorage.DiskDumpInfoEntry dumpCacheEntry(DiskStorage.Entry entry) throws IOException {
        String str;
        EntryImpl entryImpl = (EntryImpl) entry;
        byte[] bArr = entryImpl.getResource().read();
        String strTypeOfBytes = typeOfBytes(bArr);
        if (strTypeOfBytes.equals("undefined") && bArr.length >= 4) {
            str = String.format(null, "0x%02X 0x%02X 0x%02X 0x%02X", Byte.valueOf(bArr[0]), Byte.valueOf(bArr[1]), Byte.valueOf(bArr[2]), Byte.valueOf(bArr[3]));
        } else {
            str = "";
        }
        return new DiskStorage.DiskDumpInfoEntry(entryImpl.getResource().getFile().getPath(), strTypeOfBytes, entryImpl.getSize(), str);
    }

    private String typeOfBytes(byte[] bArr) {
        if (bArr.length >= 2) {
            byte b = bArr[0];
            if (b == -1 && bArr[1] == -40) {
                return "jpg";
            }
            if (b == -119 && bArr[1] == 80) {
                return "png";
            }
            if (b == 82 && bArr[1] == 73) {
                return "webp";
            }
            if (b == 71 && bArr[1] == 73) {
                return "gif";
            }
            return "undefined";
        }
        return "undefined";
    }

    @Override // com.facebook.cache.disk.DiskStorage
    public List<DiskStorage.Entry> getEntries() throws IOException {
        EntriesCollector entriesCollector = new EntriesCollector();
        FileTree.walkFileTree(this.mVersionDirectory, entriesCollector);
        return entriesCollector.getEntries();
    }

    class EntryImpl implements DiskStorage.Entry {
        private final FileBinaryResource resource;
        private long size;
        private long timestamp;

        private EntryImpl(File file) {
            Preconditions.checkNotNull(file);
            this.resource = FileBinaryResource.createOrNull(file);
            this.size = -1L;
            this.timestamp = -1L;
        }

        @Override // com.facebook.cache.disk.DiskStorage.Entry
        public long getTimestamp() {
            if (this.timestamp < 0) {
                this.timestamp = this.resource.getFile().lastModified();
            }
            return this.timestamp;
        }

        @Override // com.facebook.cache.disk.DiskStorage.Entry
        public FileBinaryResource getResource() {
            return this.resource;
        }

        @Override // com.facebook.cache.disk.DiskStorage.Entry
        public long getSize() {
            if (this.size < 0) {
                this.size = this.resource.size();
            }
            return this.size;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public FileInfo getShardFileInfo(File file) {
        FileInfo fileInfoFromFile = FileInfo.fromFile(file);
        if (fileInfoFromFile != null && getSubdirectory(fileInfoFromFile.resourceId).equals(file.getParentFile())) {
            return fileInfoFromFile;
        }
        return null;
    }

    private enum FileType {
        CONTENT(DefaultDiskStorage.CONTENT_FILE_EXTENSION),
        TEMP(DefaultDiskStorage.TEMP_FILE_EXTENSION);

        public final String extension;

        FileType(String str) {
            this.extension = str;
        }

        public static FileType fromExtension(String str) {
            if (DefaultDiskStorage.CONTENT_FILE_EXTENSION.equals(str)) {
                return CONTENT;
            }
            if (DefaultDiskStorage.TEMP_FILE_EXTENSION.equals(str)) {
                return TEMP;
            }
            return null;
        }
    }

    private static class FileInfo {
        public final String resourceId;
        public final FileType type;

        private FileInfo(FileType fileType, String str) {
            this.type = fileType;
            this.resourceId = str;
        }

        public String toString() {
            return this.type + "(" + this.resourceId + ")";
        }

        public String toPath(String str) {
            return str + File.separator + this.resourceId + this.type.extension;
        }

        public File createTempFile(File file) throws IOException {
            return File.createTempFile(this.resourceId + ".", DefaultDiskStorage.TEMP_FILE_EXTENSION, file);
        }

        public static FileInfo fromFile(File file) {
            FileType fileTypeFromExtension;
            String name = file.getName();
            int iLastIndexOf = name.lastIndexOf(46);
            if (iLastIndexOf <= 0 || (fileTypeFromExtension = FileType.fromExtension(name.substring(iLastIndexOf))) == null) {
                return null;
            }
            String strSubstring = name.substring(0, iLastIndexOf);
            if (fileTypeFromExtension.equals(FileType.TEMP)) {
                int iLastIndexOf2 = strSubstring.lastIndexOf(46);
                if (iLastIndexOf2 <= 0) {
                    return null;
                }
                strSubstring = strSubstring.substring(0, iLastIndexOf2);
            }
            return new FileInfo(fileTypeFromExtension, strSubstring);
        }
    }
}
