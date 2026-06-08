package com.facebook.imagepipeline.memory;

import com.facebook.common.internal.Preconditions;
import com.facebook.common.references.CloseableReference;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class NativePooledByteBufferOutputStream extends PooledByteBufferOutputStream {
    private CloseableReference<NativeMemoryChunk> mBufRef;
    private int mCount;
    private final NativeMemoryChunkPool mPool;

    public NativePooledByteBufferOutputStream(NativeMemoryChunkPool nativeMemoryChunkPool) {
        this(nativeMemoryChunkPool, nativeMemoryChunkPool.getMinBufferSize());
    }

    public NativePooledByteBufferOutputStream(NativeMemoryChunkPool nativeMemoryChunkPool, int i) {
        Preconditions.checkArgument(i > 0);
        NativeMemoryChunkPool nativeMemoryChunkPool2 = (NativeMemoryChunkPool) Preconditions.checkNotNull(nativeMemoryChunkPool);
        this.mPool = nativeMemoryChunkPool2;
        this.mCount = 0;
        this.mBufRef = CloseableReference.of(nativeMemoryChunkPool2.get(i), nativeMemoryChunkPool2);
    }

    @Override // com.facebook.imagepipeline.memory.PooledByteBufferOutputStream
    public NativePooledByteBuffer toByteBuffer() {
        ensureValid();
        return new NativePooledByteBuffer(this.mBufRef, this.mCount);
    }

    @Override // com.facebook.imagepipeline.memory.PooledByteBufferOutputStream
    public int size() {
        return this.mCount;
    }

    @Override // java.io.OutputStream
    public void write(int i) throws IOException {
        write(new byte[]{(byte) i});
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (i < 0 || i2 < 0 || i + i2 > bArr.length) {
            throw new ArrayIndexOutOfBoundsException("length=" + bArr.length + "; regionStart=" + i + "; regionLength=" + i2);
        }
        ensureValid();
        realloc(this.mCount + i2);
        this.mBufRef.get().write(this.mCount, bArr, i, i2);
        this.mCount += i2;
    }

    @Override // com.facebook.imagepipeline.memory.PooledByteBufferOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws Throwable {
        CloseableReference.closeSafely(this.mBufRef);
        this.mBufRef = null;
        this.mCount = -1;
        super.close();
    }

    void realloc(int i) {
        ensureValid();
        if (i <= this.mBufRef.get().getSize()) {
            return;
        }
        NativeMemoryChunk nativeMemoryChunk = this.mPool.get(i);
        this.mBufRef.get().copy(0, nativeMemoryChunk, 0, this.mCount);
        this.mBufRef.close();
        this.mBufRef = CloseableReference.of(nativeMemoryChunk, this.mPool);
    }

    private void ensureValid() {
        if (!CloseableReference.isValid(this.mBufRef)) {
            throw new InvalidStreamException();
        }
    }

    public static class InvalidStreamException extends RuntimeException {
        public InvalidStreamException() {
            super("OutputStream no longer valid");
        }
    }
}
