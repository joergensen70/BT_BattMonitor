package bolts;

import android.content.Context;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import bolts.AppLink;
import bolts.Task;
import com.facebook.common.util.UriUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class WebViewAppLinkResolver implements AppLinkResolver {
    private static final String KEY_AL_VALUE = "value";
    private static final String KEY_ANDROID = "android";
    private static final String KEY_APP_NAME = "app_name";
    private static final String KEY_CLASS = "class";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_SHOULD_FALLBACK = "should_fallback";
    private static final String KEY_URL = "url";
    private static final String KEY_WEB = "web";
    private static final String KEY_WEB_URL = "url";
    private static final String META_TAG_PREFIX = "al";
    private static final String PREFER_HEADER = "Prefer-Html-Meta-Tags";
    private static final String TAG_EXTRACTION_JAVASCRIPT = "javascript:boltsWebViewAppLinkResolverResult.setValue((function() {  var metaTags = document.getElementsByTagName('meta');  var results = [];  for (var i = 0; i < metaTags.length; i++) {    var property = metaTags[i].getAttribute('property');    if (property && property.substring(0, 'al:'.length) === 'al:') {      var tag = { \"property\": metaTags[i].getAttribute('property') };      if (metaTags[i].hasAttribute('content')) {        tag['content'] = metaTags[i].getAttribute('content');      }      results.push(tag);    }  }  return JSON.stringify(results);})())";
    private final Context context;

    public WebViewAppLinkResolver(Context context) {
        this.context = context;
    }

    @Override // bolts.AppLinkResolver
    public Task<AppLink> getAppLinkFromUrlInBackground(final Uri uri) {
        final Capture capture = new Capture();
        final Capture capture2 = new Capture();
        return Task.callInBackground(new Callable<Void>() { // from class: bolts.WebViewAppLinkResolver.3
            @Override // java.util.concurrent.Callable
            public Void call() throws Exception {
                URL url = new URL(uri.toString());
                URLConnection uRLConnectionOpenConnection = null;
                while (url != null) {
                    uRLConnectionOpenConnection = url.openConnection();
                    boolean z = uRLConnectionOpenConnection instanceof HttpURLConnection;
                    if (z) {
                        ((HttpURLConnection) uRLConnectionOpenConnection).setInstanceFollowRedirects(true);
                    }
                    uRLConnectionOpenConnection.setRequestProperty(WebViewAppLinkResolver.PREFER_HEADER, WebViewAppLinkResolver.META_TAG_PREFIX);
                    uRLConnectionOpenConnection.connect();
                    if (z) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) uRLConnectionOpenConnection;
                        if (httpURLConnection.getResponseCode() >= 300 && httpURLConnection.getResponseCode() < 400) {
                            URL url2 = new URL(httpURLConnection.getHeaderField("Location"));
                            httpURLConnection.disconnect();
                            url = url2;
                        }
                    }
                    url = null;
                }
                try {
                    capture.set(WebViewAppLinkResolver.readFromConnection(uRLConnectionOpenConnection));
                    capture2.set(uRLConnectionOpenConnection.getContentType());
                    return null;
                } finally {
                    if (uRLConnectionOpenConnection instanceof HttpURLConnection) {
                        ((HttpURLConnection) uRLConnectionOpenConnection).disconnect();
                    }
                }
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONArray>>() { // from class: bolts.WebViewAppLinkResolver.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // bolts.Continuation
            public Task<JSONArray> then(Task<Void> task) throws Exception {
                final Task.TaskCompletionSource taskCompletionSourceCreate = Task.create();
                WebView webView = new WebView(WebViewAppLinkResolver.this.context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setNetworkAvailable(false);
                webView.setWebViewClient(new WebViewClient() { // from class: bolts.WebViewAppLinkResolver.2.1
                    private boolean loaded = false;

                    private void runJavaScript(WebView webView2) {
                        if (this.loaded) {
                            return;
                        }
                        this.loaded = true;
                        webView2.loadUrl(WebViewAppLinkResolver.TAG_EXTRACTION_JAVASCRIPT);
                    }

                    @Override // android.webkit.WebViewClient
                    public void onPageFinished(WebView webView2, String str) {
                        super.onPageFinished(webView2, str);
                        runJavaScript(webView2);
                    }

                    @Override // android.webkit.WebViewClient
                    public void onLoadResource(WebView webView2, String str) {
                        super.onLoadResource(webView2, str);
                        runJavaScript(webView2);
                    }
                });
                webView.addJavascriptInterface(new Object() { // from class: bolts.WebViewAppLinkResolver.2.2
                    @JavascriptInterface
                    public void setValue(String str) {
                        try {
                            taskCompletionSourceCreate.trySetResult(new JSONArray(str));
                        } catch (JSONException e) {
                            taskCompletionSourceCreate.trySetError(e);
                        }
                    }
                }, "boltsWebViewAppLinkResolverResult");
                webView.loadDataWithBaseURL(uri.toString(), (String) capture.get(), capture2.get() != null ? ((String) capture2.get()).split(";")[0] : null, null, null);
                return taskCompletionSourceCreate.getTask();
            }
        }, Task.UI_THREAD_EXECUTOR).onSuccess(new Continuation<JSONArray, AppLink>() { // from class: bolts.WebViewAppLinkResolver.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // bolts.Continuation
            public AppLink then(Task<JSONArray> task) throws Exception {
                return WebViewAppLinkResolver.makeAppLinkFromAlData(WebViewAppLinkResolver.parseAlData(task.getResult()), uri);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Map<String, Object> parseAlData(JSONArray jSONArray) throws JSONException {
        HashMap map = new HashMap();
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            String[] strArrSplit = jSONObject.getString("property").split(":");
            if (strArrSplit[0].equals(META_TAG_PREFIX)) {
                Map map2 = map;
                int i2 = 1;
                while (true) {
                    if (i2 >= strArrSplit.length) {
                        break;
                    }
                    List arrayList = (List) map2.get(strArrSplit[i2]);
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                        map2.put(strArrSplit[i2], arrayList);
                    }
                    Map map3 = arrayList.size() > 0 ? (Map) arrayList.get(arrayList.size() - 1) : null;
                    if (map3 == null || i2 == strArrSplit.length - 1) {
                        map2 = new HashMap();
                        arrayList.add(map2);
                    } else {
                        map2 = map3;
                    }
                    i2++;
                }
                if (jSONObject.has(UriUtil.LOCAL_CONTENT_SCHEME)) {
                    if (jSONObject.isNull(UriUtil.LOCAL_CONTENT_SCHEME)) {
                        map2.put(KEY_AL_VALUE, null);
                    } else {
                        map2.put(KEY_AL_VALUE, jSONObject.getString(UriUtil.LOCAL_CONTENT_SCHEME));
                    }
                }
            }
        }
        return map;
    }

    private static List<Map<String, Object>> getAlList(Map<String, Object> map, String str) {
        List<Map<String, Object>> list = (List) map.get(str);
        return list == null ? Collections.emptyList() : list;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static AppLink makeAppLinkFromAlData(Map<String, Object> map, Uri uri) {
        Uri uriTryCreateUrl;
        ArrayList arrayList = new ArrayList();
        List listEmptyList = (List) map.get(KEY_ANDROID);
        if (listEmptyList == null) {
            listEmptyList = Collections.emptyList();
        }
        Iterator it = listEmptyList.iterator();
        while (true) {
            int i = 0;
            if (!it.hasNext()) {
                break;
            }
            Map map2 = (Map) it.next();
            List<Map<String, Object>> alList = getAlList(map2, "url");
            List<Map<String, Object>> alList2 = getAlList(map2, KEY_PACKAGE);
            List<Map<String, Object>> alList3 = getAlList(map2, KEY_CLASS);
            List<Map<String, Object>> alList4 = getAlList(map2, KEY_APP_NAME);
            int iMax = Math.max(alList.size(), Math.max(alList2.size(), Math.max(alList3.size(), alList4.size())));
            while (i < iMax) {
                arrayList.add(new AppLink.Target((String) (alList2.size() > i ? alList2.get(i).get(KEY_AL_VALUE) : null), (String) (alList3.size() > i ? alList3.get(i).get(KEY_AL_VALUE) : null), tryCreateUrl((String) (alList.size() > i ? alList.get(i).get(KEY_AL_VALUE) : null)), (String) (alList4.size() > i ? alList4.get(i).get(KEY_AL_VALUE) : null)));
                i++;
            }
        }
        List list = (List) map.get(KEY_WEB);
        if (list == null || list.size() <= 0) {
            uriTryCreateUrl = uri;
        } else {
            Map map3 = (Map) list.get(0);
            List list2 = (List) map3.get("url");
            List list3 = (List) map3.get(KEY_SHOULD_FALLBACK);
            uriTryCreateUrl = (list3 == null || list3.size() <= 0 || !Arrays.asList("no", "false", "0").contains(((String) ((Map) list3.get(0)).get(KEY_AL_VALUE)).toLowerCase())) ? uri : null;
            if (uriTryCreateUrl != null && list2 != null && list2.size() > 0) {
                uriTryCreateUrl = tryCreateUrl((String) ((Map) list2.get(0)).get(KEY_AL_VALUE));
            }
        }
        return new AppLink(uri, arrayList, uriTryCreateUrl);
    }

    private static Uri tryCreateUrl(String str) {
        if (str == null) {
            return null;
        }
        return Uri.parse(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String readFromConnection(URLConnection uRLConnection) throws IOException {
        InputStream inputStream;
        int i;
        if (uRLConnection instanceof HttpURLConnection) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) uRLConnection;
            try {
                inputStream = uRLConnection.getInputStream();
            } catch (Exception unused) {
                inputStream = httpURLConnection.getErrorStream();
            }
        } else {
            inputStream = uRLConnection.getInputStream();
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                int i2 = inputStream.read(bArr);
                i = 0;
                if (i2 == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, i2);
            }
            String contentEncoding = uRLConnection.getContentEncoding();
            if (contentEncoding == null) {
                String[] strArrSplit = uRLConnection.getContentType().split(";");
                int length = strArrSplit.length;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String strTrim = strArrSplit[i].trim();
                    if (strTrim.startsWith("charset=")) {
                        contentEncoding = strTrim.substring("charset=".length());
                        break;
                    }
                    i++;
                }
                if (contentEncoding == null) {
                    contentEncoding = "UTF-8";
                }
            }
            return new String(byteArrayOutputStream.toByteArray(), contentEncoding);
        } finally {
            inputStream.close();
        }
    }
}
