package com.gddai.lioncheck.fragment.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.http.OkHttp;
import com.lidroid.xutils.ViewUtils;
import com.ys.module.dialog.LoadingDialog;
import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseFragment extends Fragment {
    private int fragmentId;
    protected BaseActivity mActivity;
    protected FragmentManager mFragmentManager;
    protected LoadingDialog mLoadingDialog;
    protected View mView;

    protected abstract void init();

    protected abstract int setLayoutId();

    @Override // android.support.v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (BaseActivity) activity;
    }

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = this.mView;
        if (view != null) {
            ViewGroup viewGroup2 = (ViewGroup) view.getParent();
            if (viewGroup2 != null) {
                viewGroup2.removeView(this.mView);
            }
        } else {
            View viewInflate = layoutInflater.inflate(setLayoutId(), viewGroup, false);
            this.mView = viewInflate;
            ViewUtils.inject(this, viewInflate);
            init();
        }
        return this.mView;
    }

    public OkHttp getHttp() {
        return this.mActivity.getHttp();
    }

    public void showActivity(Class<?> cls) {
        this.mActivity.showActivity(cls);
    }

    public void showActivity(Class<?> cls, Serializable serializable) {
        this.mActivity.showActivity(cls, serializable);
    }
}
