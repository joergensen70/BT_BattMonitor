package com.facebook.imagepipeline.memory;

import com.facebook.common.internal.Throwables;
import java.io.IOException;
import java.io.OutputStream;

/* JADX INFO: loaded from: classes.dex */
public abstract class PooledByteBufferOutputStream extends OutputStream {
    public abstract int size();

    public abstract PooledByteBuffer toByteBuffer();

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws Throwable {
        try {
            super.close();
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }
}
