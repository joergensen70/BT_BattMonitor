package com.facebook.cache.disk;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public interface DiskStorageSupplier {
    DiskStorage get() throws IOException;
}
