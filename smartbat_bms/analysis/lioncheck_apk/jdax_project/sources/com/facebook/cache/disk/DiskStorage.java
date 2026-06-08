package com.facebook.cache.disk;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.WriterCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public interface DiskStorage {

    public static class DiskDumpInfo {
        public List<DiskDumpInfoEntry> entries = new ArrayList();
        public Map<String, Integer> typeCounts = new HashMap();
    }

    public interface Entry {
        FileBinaryResource getResource();

        long getSize();

        long getTimestamp();
    }

    void clearAll() throws IOException;

    FileBinaryResource commit(String str, FileBinaryResource fileBinaryResource, Object obj) throws IOException;

    boolean contains(String str, Object obj) throws IOException;

    FileBinaryResource createTemporary(String str, Object obj) throws IOException;

    DiskDumpInfo getDumpInfo() throws IOException;

    Collection<Entry> getEntries() throws IOException;

    String getFilename(String str);

    FileBinaryResource getResource(String str, Object obj) throws IOException;

    boolean isEnabled();

    void purgeUnexpectedResources();

    long remove(Entry entry) throws IOException;

    long remove(String str) throws IOException;

    boolean touch(String str, Object obj) throws IOException;

    void updateResource(String str, FileBinaryResource fileBinaryResource, WriterCallback writerCallback, Object obj) throws IOException;

    public static class DiskDumpInfoEntry {
        public final String firstBits;
        public final String path;
        public final float size;
        public final String type;

        protected DiskDumpInfoEntry(String str, String str2, float f, String str3) {
            this.path = str;
            this.type = str2;
            this.size = f;
            this.firstBits = str3;
        }
    }
}
