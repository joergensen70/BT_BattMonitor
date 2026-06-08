package com.facebook.common.soloader;

/* JADX INFO: loaded from: classes.dex */
public class SoLoaderShim {
    private static volatile Handler sHandler = new DefaultHandler();

    public interface Handler {
        void loadLibrary(String str);
    }

    public static class DefaultHandler implements Handler {
        @Override // com.facebook.common.soloader.SoLoaderShim.Handler
        public void loadLibrary(String str) {
            System.loadLibrary(str);
        }
    }

    public static void setHandler(Handler handler) {
        if (handler == null) {
            throw new NullPointerException("Handler cannot be null");
        }
        sHandler = handler;
    }

    public static void loadLibrary(String str) {
        sHandler.loadLibrary(str);
    }

    public static void setInTestMode() {
        setHandler(new Handler() { // from class: com.facebook.common.soloader.SoLoaderShim.1
            @Override // com.facebook.common.soloader.SoLoaderShim.Handler
            public void loadLibrary(String str) {
            }
        });
    }
}
