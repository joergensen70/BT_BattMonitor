package android.support.v4.os;

import android.content.res.Configuration;

/* JADX INFO: loaded from: classes.dex */
public final class ConfigurationCompat {
    private ConfigurationCompat() {
    }

    public static LocaleListCompat getLocales(Configuration configuration) {
        return LocaleListCompat.wrap(configuration.getLocales());
    }
}
