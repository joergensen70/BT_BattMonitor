package com.google.zxing.integration.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureActivity;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class IntentIntegrator {
    public static int REQUEST_CODE = 49374;
    private static final String TAG = "IntentIntegrator";
    private final Activity activity;
    private Class<?> captureActivity;
    private Collection<String> desiredBarcodeFormats;
    private Fragment fragment;
    private final Map<String, Object> moreExtras = new HashMap(3);
    private android.support.v4.app.Fragment supportFragment;
    public static final Collection<String> PRODUCT_CODE_TYPES = list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "RSS_14");
    public static final Collection<String> ONE_D_CODE_TYPES = list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "CODE_39", "CODE_93", "CODE_128", "ITF", "RSS_14", "RSS_EXPANDED");
    public static final Collection<String> QR_CODE_TYPES = Collections.singleton("QR_CODE");
    public static final Collection<String> DATA_MATRIX_TYPES = Collections.singleton("DATA_MATRIX");
    public static final Collection<String> ALL_CODE_TYPES = null;

    protected Class<?> getDefaultCaptureActivity() {
        return CaptureActivity.class;
    }

    public IntentIntegrator(Activity activity) {
        this.activity = activity;
    }

    public Class<?> getCaptureActivity() {
        if (this.captureActivity == null) {
            this.captureActivity = getDefaultCaptureActivity();
        }
        return this.captureActivity;
    }

    public IntentIntegrator setCaptureActivity(Class<?> cls) {
        this.captureActivity = cls;
        return this;
    }

    public static IntentIntegrator forSupportFragment(android.support.v4.app.Fragment fragment) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(fragment.getActivity());
        intentIntegrator.supportFragment = fragment;
        return intentIntegrator;
    }

    public static IntentIntegrator forFragment(Fragment fragment) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(fragment.getActivity());
        intentIntegrator.fragment = fragment;
        return intentIntegrator;
    }

    public Map<String, ?> getMoreExtras() {
        return this.moreExtras;
    }

    public final IntentIntegrator addExtra(String str, Object obj) {
        this.moreExtras.put(str, obj);
        return this;
    }

    public final IntentIntegrator setPrompt(String str) {
        if (str != null) {
            addExtra(Intents.Scan.PROMPT_MESSAGE, str);
        }
        return this;
    }

    public IntentIntegrator setOrientationLocked(boolean z) {
        addExtra(Intents.Scan.ORIENTATION_LOCKED, Boolean.valueOf(z));
        return this;
    }

    public IntentIntegrator setCameraId(int i) {
        if (i >= 0) {
            addExtra(Intents.Scan.CAMERA_ID, Integer.valueOf(i));
        }
        return this;
    }

    public IntentIntegrator setBeepEnabled(boolean z) {
        addExtra(Intents.Scan.BEEP_ENABLED, Boolean.valueOf(z));
        return this;
    }

    public IntentIntegrator setBarcodeImageEnabled(boolean z) {
        addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, Boolean.valueOf(z));
        return this;
    }

    public IntentIntegrator setDesiredBarcodeFormats(Collection<String> collection) {
        this.desiredBarcodeFormats = collection;
        return this;
    }

    public final void initiateScan() {
        startActivityForResult(createScanIntent(), REQUEST_CODE);
    }

    public IntentIntegrator setTimeout(long j) {
        addExtra(Intents.Scan.TIMEOUT, Long.valueOf(j));
        return this;
    }

    public Intent createScanIntent() {
        Intent intent = new Intent(this.activity, getCaptureActivity());
        intent.setAction(Intents.Scan.ACTION);
        if (this.desiredBarcodeFormats != null) {
            StringBuilder sb = new StringBuilder();
            for (String str : this.desiredBarcodeFormats) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(str);
            }
            intent.putExtra(Intents.Scan.FORMATS, sb.toString());
        }
        intent.addFlags(67108864);
        intent.addFlags(524288);
        attachMoreExtras(intent);
        return intent;
    }

    public final void initiateScan(Collection<String> collection) {
        setDesiredBarcodeFormats(collection);
        initiateScan();
    }

    protected void startActivityForResult(Intent intent, int i) {
        if (this.fragment != null) {
            this.fragment.startActivityForResult(intent, i);
            return;
        }
        android.support.v4.app.Fragment fragment = this.supportFragment;
        if (fragment != null) {
            fragment.startActivityForResult(intent, i);
        } else {
            this.activity.startActivityForResult(intent, i);
        }
    }

    protected void startActivity(Intent intent) {
        if (this.fragment != null) {
            this.fragment.startActivity(intent);
            return;
        }
        android.support.v4.app.Fragment fragment = this.supportFragment;
        if (fragment != null) {
            fragment.startActivity(intent);
        } else {
            this.activity.startActivity(intent);
        }
    }

    public static IntentResult parseActivityResult(int i, int i2, Intent intent) {
        if (i != REQUEST_CODE) {
            return null;
        }
        if (i2 == -1) {
            String stringExtra = intent.getStringExtra(Intents.Scan.RESULT);
            String stringExtra2 = intent.getStringExtra(Intents.Scan.RESULT_FORMAT);
            byte[] byteArrayExtra = intent.getByteArrayExtra(Intents.Scan.RESULT_BYTES);
            int intExtra = intent.getIntExtra(Intents.Scan.RESULT_ORIENTATION, Integer.MIN_VALUE);
            return new IntentResult(stringExtra, stringExtra2, byteArrayExtra, intExtra != Integer.MIN_VALUE ? Integer.valueOf(intExtra) : null, intent.getStringExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL), intent.getStringExtra(Intents.Scan.RESULT_BARCODE_IMAGE_PATH));
        }
        return new IntentResult();
    }

    private static List<String> list(String... strArr) {
        return Collections.unmodifiableList(Arrays.asList(strArr));
    }

    private void attachMoreExtras(Intent intent) {
        for (Map.Entry<String, Object> entry : this.moreExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (Double) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (Float) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else {
                intent.putExtra(key, value.toString());
            }
        }
    }
}
