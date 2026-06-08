package com.facebook.imagepipeline.backends.okhttp;

import android.os.Looper;
import android.os.SystemClock;
import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
public class OkHttpNetworkFetcher extends BaseNetworkFetcher<OkHttpNetworkFetchState> {
    private static final String FETCH_TIME = "fetch_time";
    private static final String IMAGE_SIZE = "image_size";
    private static final String QUEUE_TIME = "queue_time";
    private static final String TAG = "OkHttpNetworkFetchProducer";
    private static final String TOTAL_TIME = "total_time";
    private Executor mCancellationExecutor;
    private final OkHttpClient mOkHttpClient;

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public /* bridge */ /* synthetic */ FetchState createFetchState(Consumer consumer, ProducerContext producerContext) {
        return createFetchState((Consumer<EncodedImage>) consumer, producerContext);
    }

    public static class OkHttpNetworkFetchState extends FetchState {
        public long fetchCompleteTime;
        public long responseTime;
        public long submitTime;

        public OkHttpNetworkFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }

    public OkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        this.mOkHttpClient = okHttpClient;
        this.mCancellationExecutor = okHttpClient.getDispatcher().getExecutorService();
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public OkHttpNetworkFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        return new OkHttpNetworkFetchState(consumer, producerContext);
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public void fetch(final OkHttpNetworkFetchState okHttpNetworkFetchState, final NetworkFetcher.Callback callback) {
        okHttpNetworkFetchState.submitTime = SystemClock.elapsedRealtime();
        final Call callNewCall = this.mOkHttpClient.newCall(new Request.Builder().cacheControl(new CacheControl.Builder().noStore().build()).url(okHttpNetworkFetchState.getUri().toString()).get().build());
        okHttpNetworkFetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() { // from class: com.facebook.imagepipeline.backends.okhttp.OkHttpNetworkFetcher.1
            @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
            public void onCancellationRequested() {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    OkHttpNetworkFetcher.this.mCancellationExecutor.execute(new Runnable() { // from class: com.facebook.imagepipeline.backends.okhttp.OkHttpNetworkFetcher.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            callNewCall.cancel();
                        }
                    });
                } else {
                    callNewCall.cancel();
                }
            }
        });
        callNewCall.enqueue(new Callback() { // from class: com.facebook.imagepipeline.backends.okhttp.OkHttpNetworkFetcher.2
            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Type inference failed for: r8v1, types: [com.squareup.okhttp.ResponseBody] */
            /* JADX WARN: Type inference failed for: r8v10 */
            /* JADX WARN: Type inference failed for: r8v3 */
            /* JADX WARN: Type inference failed for: r8v4, types: [com.squareup.okhttp.ResponseBody] */
            /* JADX WARN: Type inference failed for: r8v9 */
            @Override // com.squareup.okhttp.Callback
            public void onResponse(Response response) {
                okHttpNetworkFetchState.responseTime = SystemClock.elapsedRealtime();
                if (!response.isSuccessful()) {
                    OkHttpNetworkFetcher.this.handleException(callNewCall, new IOException("Unexpected HTTP code " + response), callback);
                    return;
                }
                ?? Body = response.body();
                try {
                    try {
                        try {
                            long jContentLength = Body.contentLength();
                            if (jContentLength < 0) {
                                jContentLength = 0;
                            }
                            callback.onResponse(Body.byteStream(), (int) jContentLength);
                            Body.close();
                        } catch (Exception e) {
                            OkHttpNetworkFetcher.this.handleException(callNewCall, e, callback);
                            Body.close();
                            Body = Body;
                        }
                    } catch (Throwable th) {
                        try {
                            Body.close();
                        } catch (Exception e2) {
                            FLog.w(OkHttpNetworkFetcher.TAG, "Exception when closing response body", e2);
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    FLog.w(OkHttpNetworkFetcher.TAG, "Exception when closing response body", e3);
                    Body = e3;
                }
            }

            @Override // com.squareup.okhttp.Callback
            public void onFailure(Request request, IOException iOException) {
                OkHttpNetworkFetcher.this.handleException(callNewCall, iOException, callback);
            }
        });
    }

    @Override // com.facebook.imagepipeline.producers.BaseNetworkFetcher, com.facebook.imagepipeline.producers.NetworkFetcher
    public void onFetchCompletion(OkHttpNetworkFetchState okHttpNetworkFetchState, int i) {
        okHttpNetworkFetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override // com.facebook.imagepipeline.producers.BaseNetworkFetcher, com.facebook.imagepipeline.producers.NetworkFetcher
    public Map<String, String> getExtraMap(OkHttpNetworkFetchState okHttpNetworkFetchState, int i) {
        HashMap map = new HashMap(4);
        map.put(QUEUE_TIME, Long.toString(okHttpNetworkFetchState.responseTime - okHttpNetworkFetchState.submitTime));
        map.put(FETCH_TIME, Long.toString(okHttpNetworkFetchState.fetchCompleteTime - okHttpNetworkFetchState.responseTime));
        map.put(TOTAL_TIME, Long.toString(okHttpNetworkFetchState.fetchCompleteTime - okHttpNetworkFetchState.submitTime));
        map.put(IMAGE_SIZE, Integer.toString(i));
        return map;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleException(Call call, Exception exc, NetworkFetcher.Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(exc);
        }
    }
}
