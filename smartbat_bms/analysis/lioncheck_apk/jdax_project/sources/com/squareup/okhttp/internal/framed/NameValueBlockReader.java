package com.squareup.okhttp.internal.framed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.ForwardingSource;
import okio.InflaterSource;
import okio.Okio;

/* JADX INFO: loaded from: classes.dex */
class NameValueBlockReader {
    private int compressedLimit;
    private final InflaterSource inflaterSource;
    private final BufferedSource source;

    public NameValueBlockReader(BufferedSource bufferedSource) {
        InflaterSource inflaterSource = new InflaterSource(new ForwardingSource(bufferedSource) { // from class: com.squareup.okhttp.internal.framed.NameValueBlockReader.1
            @Override // okio.ForwardingSource, okio.Source
            public long read(Buffer buffer, long j) throws IOException {
                if (NameValueBlockReader.this.compressedLimit == 0) {
                    return -1L;
                }
                long j2 = super.read(buffer, Math.min(j, NameValueBlockReader.this.compressedLimit));
                if (j2 == -1) {
                    return -1L;
                }
                NameValueBlockReader nameValueBlockReader = NameValueBlockReader.this;
                nameValueBlockReader.compressedLimit = (int) (((long) nameValueBlockReader.compressedLimit) - j2);
                return j2;
            }
        }, new Inflater() { // from class: com.squareup.okhttp.internal.framed.NameValueBlockReader.2
            @Override // java.util.zip.Inflater
            public int inflate(byte[] bArr, int i, int i2) throws DataFormatException {
                int iInflate = super.inflate(bArr, i, i2);
                if (iInflate != 0 || !needsDictionary()) {
                    return iInflate;
                }
                setDictionary(Spdy3.DICTIONARY);
                return super.inflate(bArr, i, i2);
            }
        });
        this.inflaterSource = inflaterSource;
        this.source = Okio.buffer(inflaterSource);
    }

    public List<Header> readNameValueBlock(int i) throws IOException {
        this.compressedLimit += i;
        int i2 = this.source.readInt();
        if (i2 < 0) {
            throw new IOException("numberOfPairs < 0: " + i2);
        }
        if (i2 > 1024) {
            throw new IOException("numberOfPairs > 1024: " + i2);
        }
        ArrayList arrayList = new ArrayList(i2);
        for (int i3 = 0; i3 < i2; i3++) {
            ByteString asciiLowercase = readByteString().toAsciiLowercase();
            ByteString byteString = readByteString();
            if (asciiLowercase.size() == 0) {
                throw new IOException("name.size == 0");
            }
            arrayList.add(new Header(asciiLowercase, byteString));
        }
        doneReading();
        return arrayList;
    }

    private ByteString readByteString() throws IOException {
        return this.source.readByteString(this.source.readInt());
    }

    private void doneReading() throws IOException {
        if (this.compressedLimit > 0) {
            this.inflaterSource.refill();
            if (this.compressedLimit != 0) {
                throw new IOException("compressedLimit > 0: " + this.compressedLimit);
            }
        }
    }

    public void close() throws IOException {
        this.source.close();
    }
}
