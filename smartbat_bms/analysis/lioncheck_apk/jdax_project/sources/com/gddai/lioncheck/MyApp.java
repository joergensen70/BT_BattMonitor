package com.gddai.lioncheck;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Process;
import android.util.DisplayMetrics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.gddai.lioncheck.dbutils.MyDBUtil;
import com.gddai.lioncheck.service.BluetoothLeService;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.util.LogUtils;
import com.squareup.okhttp.OkHttpClient;
import java.lang.Thread;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class MyApp extends Application {
    public static String TEST_IMAGE;
    private static MyApp instance;
    private static BluetoothLeService mBluetoothLeService;
    public static DbUtils mDbUtils;
    public static final ServiceConnection mServiceConnection = new ServiceConnection() { // from class: com.gddai.lioncheck.MyApp.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                BluetoothLeService unused = MyApp.mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
                MyApp.mBluetoothLeService.initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            BluetoothLeService unused = MyApp.mBluetoothLeService = null;
        }
    };
    private SharedPreferences sharedPreferences;
    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() { // from class: com.gddai.lioncheck.MyApp.3
        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            Intent intent = new Intent(MyApp.this, (Class<?>) CrashActivity.class);
            intent.setFlags(268435456);
            intent.putExtra("exception", th);
            MyApp.this.startActivity(intent);
            Process.killProcess(Process.myPid());
        }
    };

    public static BluetoothLeService getmBluetoothLeService() {
        return mBluetoothLeService;
    }

    public static void setmBluetoothLeService(BluetoothLeService bluetoothLeService) {
        getApplication();
        mBluetoothLeService = bluetoothLeService;
    }

    public static MyApp getApplication() {
        return instance;
    }

    public MyApp() {
        instance = this;
    }

    public static MyApp getInstance() {
        return instance;
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        instance = this;
        initOKFresco();
        initBlueTooth();
        DbUtils dbUtils = MyDBUtil.getdbInstance(this);
        mDbUtils = dbUtils;
        dbUtils.configDebug(true);
        closeLog();
        SharedPreferences sharedPreferences = getSharedPreferences("lang", 0);
        this.sharedPreferences = sharedPreferences;
        changeLanguage(sharedPreferences.getInt("lag", 1));
        Thread.setDefaultUncaughtExceptionHandler(this.uncaughtExceptionHandler);
    }

    public static void initBlueTooth() {
        getApplication().bindService(new Intent(getApplication(), (Class<?>) BluetoothLeService.class), mServiceConnection, 1);
    }

    public static DbUtils getDbUtils() {
        return mDbUtils;
    }

    private void closeLog() {
        LogUtils.allowD = false;
        LogUtils.allowE = false;
        LogUtils.allowV = false;
        LogUtils.allowI = false;
        LogUtils.allowW = false;
    }

    private void initOKFresco() {
        Fresco.initialize(this, OkHttpImagePipelineConfigFactory.newBuilder(this, new OkHttpClient()).setProgressiveJpegConfig(new ProgressiveJpegConfig() { // from class: com.gddai.lioncheck.MyApp.2
            @Override // com.facebook.imagepipeline.decoder.ProgressiveJpegConfig
            public int getNextScanNumberToDecode(int i) {
                return i + 2;
            }

            @Override // com.facebook.imagepipeline.decoder.ProgressiveJpegConfig
            public QualityInfo getQualityInfo(int i) {
                return ImmutableQualityInfo.of(i, i >= 5, false);
            }
        }).build());
    }

    private void changeLanguage(int i) {
        Locale locale;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Configuration configuration = getResources().getConfiguration();
        if (i == 1) {
            locale = Locale.getDefault();
        } else {
            locale = i != 2 ? null : Locale.GERMANY;
        }
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, displayMetrics);
    }
}
