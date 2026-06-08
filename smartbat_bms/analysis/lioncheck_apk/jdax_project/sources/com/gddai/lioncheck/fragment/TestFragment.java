package com.gddai.lioncheck.fragment;

import com.gddai.lioncheck.R;
import com.gddai.lioncheck.fragment.base.BaseFragment;
import com.lidroid.xutils.util.LogUtils;

/* JADX INFO: loaded from: classes.dex */
public class TestFragment extends BaseFragment {
    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected void init() {
    }

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected int setLayoutId() {
        return R.layout.fragment_test;
    }

    @Override // android.support.v4.app.Fragment
    public void onResume() {
        super.onResume();
        LogUtils.e("TestFragment  onResume");
    }

    @Override // android.support.v4.app.Fragment
    public void onPause() {
        super.onPause();
        LogUtils.e("TestFragment  onPause");
    }

    @Override // android.support.v4.app.Fragment
    public void onStop() {
        super.onStop();
        LogUtils.e("TestFragment  onStop");
    }

    @Override // android.support.v4.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("TestFragment  onDestroy");
    }
}
