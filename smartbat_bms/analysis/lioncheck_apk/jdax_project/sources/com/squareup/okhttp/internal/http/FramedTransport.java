package com.squareup.okhttp.internal.http;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.framed.ErrorCode;
import com.squareup.okhttp.internal.framed.FramedConnection;
import com.squareup.okhttp.internal.framed.FramedStream;
import com.squareup.okhttp.internal.framed.Header;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okio.ByteString;
import okio.Okio;
import okio.Sink;

/* JADX INFO: loaded from: classes.dex */
public final class FramedTransport implements Transport {
    private static final ByteString CONNECTION;
    private static final ByteString ENCODING;
    private static final ByteString HOST;
    private static final List<ByteString> HTTP_2_SKIPPED_REQUEST_HEADERS;
    private static final List<ByteString> HTTP_2_SKIPPED_RESPONSE_HEADERS;
    private static final ByteString KEEP_ALIVE;
    private static final ByteString PROXY_CONNECTION;
    private static final List<ByteString> SPDY_3_SKIPPED_REQUEST_HEADERS;
    private static final List<ByteString> SPDY_3_SKIPPED_RESPONSE_HEADERS;
    private static final ByteString TE;
    private static final ByteString TRANSFER_ENCODING;
    private static final ByteString UPGRADE;
    private final FramedConnection framedConnection;
    private final HttpEngine httpEngine;
    private FramedStream stream;

    @Override // com.squareup.okhttp.internal.http.Transport
    public boolean canReuseConnection() {
        return true;
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public void releaseConnectionOnIdle() {
    }

    static {
        ByteString byteStringEncodeUtf8 = ByteString.encodeUtf8("connection");
        CONNECTION = byteStringEncodeUtf8;
        ByteString byteStringEncodeUtf82 = ByteString.encodeUtf8("host");
        HOST = byteStringEncodeUtf82;
        ByteString byteStringEncodeUtf83 = ByteString.encodeUtf8("keep-alive");
        KEEP_ALIVE = byteStringEncodeUtf83;
        ByteString byteStringEncodeUtf84 = ByteString.encodeUtf8("proxy-connection");
        PROXY_CONNECTION = byteStringEncodeUtf84;
        ByteString byteStringEncodeUtf85 = ByteString.encodeUtf8("transfer-encoding");
        TRANSFER_ENCODING = byteStringEncodeUtf85;
        ByteString byteStringEncodeUtf86 = ByteString.encodeUtf8("te");
        TE = byteStringEncodeUtf86;
        ByteString byteStringEncodeUtf87 = ByteString.encodeUtf8("encoding");
        ENCODING = byteStringEncodeUtf87;
        ByteString byteStringEncodeUtf88 = ByteString.encodeUtf8("upgrade");
        UPGRADE = byteStringEncodeUtf88;
        SPDY_3_SKIPPED_REQUEST_HEADERS = Util.immutableList(byteStringEncodeUtf8, byteStringEncodeUtf82, byteStringEncodeUtf83, byteStringEncodeUtf84, byteStringEncodeUtf85, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
        SPDY_3_SKIPPED_RESPONSE_HEADERS = Util.immutableList(byteStringEncodeUtf8, byteStringEncodeUtf82, byteStringEncodeUtf83, byteStringEncodeUtf84, byteStringEncodeUtf85);
        HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList(byteStringEncodeUtf8, byteStringEncodeUtf82, byteStringEncodeUtf83, byteStringEncodeUtf84, byteStringEncodeUtf86, byteStringEncodeUtf85, byteStringEncodeUtf87, byteStringEncodeUtf88, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
        HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList(byteStringEncodeUtf8, byteStringEncodeUtf82, byteStringEncodeUtf83, byteStringEncodeUtf84, byteStringEncodeUtf86, byteStringEncodeUtf85, byteStringEncodeUtf87, byteStringEncodeUtf88);
    }

    public FramedTransport(HttpEngine httpEngine, FramedConnection framedConnection) {
        this.httpEngine = httpEngine;
        this.framedConnection = framedConnection;
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public Sink createRequestBody(Request request, long j) throws IOException {
        return this.stream.getSink();
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public void writeRequestHeaders(Request request) throws IOException {
        List<Header> listSpdy3HeadersList;
        if (this.stream != null) {
            return;
        }
        this.httpEngine.writingRequestHeaders();
        boolean zPermitsRequestBody = this.httpEngine.permitsRequestBody(request);
        if (this.framedConnection.getProtocol() == Protocol.HTTP_2) {
            listSpdy3HeadersList = http2HeadersList(request);
        } else {
            listSpdy3HeadersList = spdy3HeadersList(request);
        }
        FramedStream framedStreamNewStream = this.framedConnection.newStream(listSpdy3HeadersList, zPermitsRequestBody, true);
        this.stream = framedStreamNewStream;
        framedStreamNewStream.readTimeout().timeout(this.httpEngine.client.getReadTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public void writeRequestBody(RetryableSink retryableSink) throws IOException {
        retryableSink.writeToSocket(this.stream.getSink());
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public Response.Builder readResponseHeaders() throws IOException {
        if (this.framedConnection.getProtocol() == Protocol.HTTP_2) {
            return readHttp2HeadersList(this.stream.getResponseHeaders());
        }
        return readSpdy3HeadersList(this.stream.getResponseHeaders());
    }

    public static List<Header> spdy3HeadersList(Request request) {
        Headers headers = request.headers();
        ArrayList arrayList = new ArrayList(headers.size() + 5);
        arrayList.add(new Header(Header.TARGET_METHOD, request.method()));
        arrayList.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        arrayList.add(new Header(Header.VERSION, "HTTP/1.1"));
        arrayList.add(new Header(Header.TARGET_HOST, Util.hostHeader(request.httpUrl())));
        arrayList.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString byteStringEncodeUtf8 = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!SPDY_3_SKIPPED_REQUEST_HEADERS.contains(byteStringEncodeUtf8)) {
                String strValue = headers.value(i);
                if (linkedHashSet.add(byteStringEncodeUtf8)) {
                    arrayList.add(new Header(byteStringEncodeUtf8, strValue));
                } else {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= arrayList.size()) {
                            break;
                        }
                        if (((Header) arrayList.get(i2)).name.equals(byteStringEncodeUtf8)) {
                            arrayList.set(i2, new Header(byteStringEncodeUtf8, joinOnNull(((Header) arrayList.get(i2)).value.utf8(), strValue)));
                            break;
                        }
                        i2++;
                    }
                }
            }
        }
        return arrayList;
    }

    private static String joinOnNull(String str, String str2) {
        return str + (char) 0 + str2;
    }

    public static List<Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        ArrayList arrayList = new ArrayList(headers.size() + 4);
        arrayList.add(new Header(Header.TARGET_METHOD, request.method()));
        arrayList.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        arrayList.add(new Header(Header.TARGET_AUTHORITY, Util.hostHeader(request.httpUrl())));
        arrayList.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString byteStringEncodeUtf8 = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(byteStringEncodeUtf8)) {
                arrayList.add(new Header(byteStringEncodeUtf8, headers.value(i)));
            }
        }
        return arrayList;
    }

    public static Response.Builder readSpdy3HeadersList(List<Header> list) throws IOException {
        Headers.Builder builder = new Headers.Builder();
        builder.set(OkHeaders.SELECTED_PROTOCOL, Protocol.SPDY_3.toString());
        int size = list.size();
        String str = null;
        String str2 = "HTTP/1.1";
        for (int i = 0; i < size; i++) {
            ByteString byteString = list.get(i).name;
            String strUtf8 = list.get(i).value.utf8();
            int i2 = 0;
            while (i2 < strUtf8.length()) {
                int iIndexOf = strUtf8.indexOf(0, i2);
                if (iIndexOf == -1) {
                    iIndexOf = strUtf8.length();
                }
                String strSubstring = strUtf8.substring(i2, iIndexOf);
                if (byteString.equals(Header.RESPONSE_STATUS)) {
                    str = strSubstring;
                } else if (byteString.equals(Header.VERSION)) {
                    str2 = strSubstring;
                } else if (!SPDY_3_SKIPPED_RESPONSE_HEADERS.contains(byteString)) {
                    builder.add(byteString.utf8(), strSubstring);
                }
                i2 = iIndexOf + 1;
            }
        }
        if (str == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        StatusLine statusLine = StatusLine.parse(str2 + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + str);
        return new Response.Builder().protocol(Protocol.SPDY_3).code(statusLine.code).message(statusLine.message).headers(builder.build());
    }

    public static Response.Builder readHttp2HeadersList(List<Header> list) throws IOException {
        Headers.Builder builder = new Headers.Builder();
        builder.set(OkHeaders.SELECTED_PROTOCOL, Protocol.HTTP_2.toString());
        int size = list.size();
        String str = null;
        for (int i = 0; i < size; i++) {
            ByteString byteString = list.get(i).name;
            String strUtf8 = list.get(i).value.utf8();
            if (byteString.equals(Header.RESPONSE_STATUS)) {
                str = strUtf8;
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(byteString)) {
                builder.add(byteString.utf8(), strUtf8);
            }
        }
        if (str == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        StatusLine statusLine = StatusLine.parse("HTTP/1.1 " + str);
        return new Response.Builder().protocol(Protocol.HTTP_2).code(statusLine.code).message(statusLine.message).headers(builder.build());
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(this.stream.getSource()));
    }

    @Override // com.squareup.okhttp.internal.http.Transport
    public void disconnect(HttpEngine httpEngine) throws IOException {
        FramedStream framedStream = this.stream;
        if (framedStream != null) {
            framedStream.close(ErrorCode.CANCEL);
        }
    }
}
