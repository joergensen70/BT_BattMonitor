package com.facebook.imagepipeline.core;

import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
public interface ExecutorSupplier {
    Executor forBackgroundTasks();

    Executor forDecode();

    Executor forLightweightBackgroundTasks();

    Executor forLocalStorageRead();

    Executor forLocalStorageWrite();
}
