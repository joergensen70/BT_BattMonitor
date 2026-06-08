package com.squareup.okhttp.internal.framed;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public interface IncomingStreamHandler {
    public static final IncomingStreamHandler REFUSE_INCOMING_STREAMS = new IncomingStreamHandler() { // from class: com.squareup.okhttp.internal.framed.IncomingStreamHandler.1
        @Override // com.squareup.okhttp.internal.framed.IncomingStreamHandler
        public void receive(FramedStream framedStream) throws IOException {
            framedStream.close(ErrorCode.REFUSED_STREAM);
        }
    };

    void receive(FramedStream framedStream) throws IOException;
}
