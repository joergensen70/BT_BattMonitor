package com.facebook.datasource;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseDataSubscriber<T> implements DataSubscriber<T> {
    @Override // com.facebook.datasource.DataSubscriber
    public void onCancellation(DataSource<T> dataSource) {
    }

    protected abstract void onFailureImpl(DataSource<T> dataSource);

    protected abstract void onNewResultImpl(DataSource<T> dataSource);

    @Override // com.facebook.datasource.DataSubscriber
    public void onProgressUpdate(DataSource<T> dataSource) {
    }

    @Override // com.facebook.datasource.DataSubscriber
    public void onNewResult(DataSource<T> dataSource) {
        boolean zIsFinished = dataSource.isFinished();
        try {
            onNewResultImpl(dataSource);
        } finally {
            if (zIsFinished) {
                dataSource.close();
            }
        }
    }

    @Override // com.facebook.datasource.DataSubscriber
    public void onFailure(DataSource<T> dataSource) {
        try {
            onFailureImpl(dataSource);
        } finally {
            dataSource.close();
        }
    }
}
