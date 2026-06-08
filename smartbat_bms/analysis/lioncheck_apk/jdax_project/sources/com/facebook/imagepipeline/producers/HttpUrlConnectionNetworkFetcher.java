package com.facebook.imagepipeline.producers;

import android.net.Uri;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: classes.dex */
public class HttpUrlConnectionNetworkFetcher extends BaseNetworkFetcher<FetchState> {
    private static final int NUM_NETWORK_THREADS = 3;
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(3);

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public FetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        return new FetchState(consumer, producerContext);
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public void fetch(final FetchState fetchState, final NetworkFetcher.Callback callback) {
        final Future<?> futureSubmit = this.mExecutorService.submit(new Runnable() { // from class: com.facebook.imagepipeline.producers.HttpUrlConnectionNetworkFetcher.1
            @Override // java.lang.Runnable
            public void run() throws Throwable {
                HttpURLConnection httpURLConnection;
                String scheme = fetchState.getUri().getScheme();
                String string = fetchState.getUri().toString();
                HttpURLConnection httpURLConnection2 = null;
                while (true) {
                    try {
                        try {
                            httpURLConnection = (HttpURLConnection) new URL(string).openConnection();
                            try {
                                String headerField = httpURLConnection.getHeaderField("Location");
                                String scheme2 = headerField == null ? null : Uri.parse(headerField).getScheme();
                                if (headerField == null || scheme2.equals(scheme)) {
                                    break;
                                }
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                }
                                httpURLConnection2 = httpURLConnection;
                                string = headerField;
                                scheme = scheme2;
                            } catch (Exception e) {
                                e = e;
                                httpURLConnection2 = httpURLConnection;
                                callback.onFailure(e);
                                if (httpURLConnection2 != null) {
                                    httpURLConnection2.disconnect();
                                    return;
                                }
                                return;
                            } catch (Throwable th) {
                                th = th;
                                httpURLConnection2 = httpURLConnection;
                                if (httpURLConnection2 != null) {
                                    httpURLConnection2.disconnect();
                                }
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (Exception e2) {
                        e = e2;
                    }
                }
                callback.onResponse(httpURLConnection.getInputStream(), -1);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        });
        fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() { // from class: com.facebook.imagepipeline.producers.HttpUrlConnectionNetworkFetcher.2
            @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
            public void onCancellationRequested() {
                if (futureSubmit.cancel(false)) {
                    callback.onCancellation();
                }
            }
        });
    }
}
