package com.gddai.lioncheck.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.DeviceInfoActivity;
import com.gddai.lioncheck.adapter.SingleVoltageAdapter;
import com.gddai.lioncheck.entity.VoltageEntity;
import com.gddai.lioncheck.fragment.base.BaseFragment;
import com.gddai.lioncheck.service.BluStaValue;
import com.lidroid.xutils.view.annotation.ViewInject;

/* JADX INFO: loaded from: classes.dex */
public class DeviceThreeFragment extends BaseFragment {
    DeviceInfoActivity activity;
    SingleVoltageAdapter adapter;
    public LocalBroadcastManager lbm;

    @ViewInject(R.id.rlc_single_rlv)
    private RecyclerView mRecyclerView;
    ViewGroup.LayoutParams params;
    MyReceiver receiver;

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected int setLayoutId() {
        return R.layout.fragment_device_three;
    }

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected void init() {
        this.activity = (DeviceInfoActivity) this.mActivity;
        this.lbm = LocalBroadcastManager.getInstance(this.mActivity);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.mActivity, 1, false));
        SingleVoltageAdapter singleVoltageAdapter = new SingleVoltageAdapter(this.mActivity);
        this.adapter = singleVoltageAdapter;
        this.mRecyclerView.setAdapter(singleVoltageAdapter);
        initReceiver();
    }

    private void initReceiver() {
        this.receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluStaValue.ACTION_SINGLE_VOLTAGE);
        this.lbm.registerReceiver(this.receiver, intentFilter);
    }

    @Override // android.support.v4.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.lbm.unregisterReceiver(this.receiver);
    }

    class MyReceiver extends BroadcastReceiver {
        MyReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            if (action.equals(BluStaValue.ACTION_SINGLE_VOLTAGE)) {
                if (intent.getIntExtra("single", 0) == 1) {
                    DeviceThreeFragment.this.activity.setIsPageThressOne(false);
                }
                if (DeviceThreeFragment.this.activity.isPageThressOne) {
                    return;
                }
                long longExtra = intent.getLongExtra("value", 0L);
                if (longExtra != 0) {
                    DeviceThreeFragment.this.adapter.insert(new VoltageEntity(intent.getIntExtra("single", 1), longExtra));
                    DeviceThreeFragment.this.adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
