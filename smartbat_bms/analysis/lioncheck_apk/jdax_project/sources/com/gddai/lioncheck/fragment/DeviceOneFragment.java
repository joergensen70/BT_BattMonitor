package com.gddai.lioncheck.fragment;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.LinearChatActivity;
import com.gddai.lioncheck.fragment.base.BaseFragment;
import com.gddai.lioncheck.service.BluStaValue;
import com.gddai.lioncheck.widget.PositionScaleView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/* JADX INFO: loaded from: classes.dex */
public class DeviceOneFragment extends BaseFragment {

    @ViewInject(R.id.cell1)
    private ViewGroup cell1;

    @ViewInject(R.id.cell2)
    private ViewGroup cell2;

    @ViewInject(R.id.cell3)
    private ViewGroup cell3;
    public LocalBroadcastManager lbm;

    @ViewInject(R.id.pslv_current)
    private PositionScaleView pslv_current;

    @ViewInject(R.id.pslv_voltage)
    private PositionScaleView pslv_voltage;
    MyReceiver receiver;

    @ViewInject(R.id.rl_ampere_bg)
    private RelativeLayout rl_ampere_bg;

    @ViewInject(R.id.rl_volt_bg)
    private RelativeLayout rl_volt_bg;

    @ViewInject(R.id.tv_current)
    private TextView tv_current;

    @ViewInject(R.id.tv_voltage)
    private TextView tv_voltage;

    private void initLayout() {
    }

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected int setLayoutId() {
        return R.layout.fragment_device_one;
    }

    @Override // com.gddai.lioncheck.fragment.base.BaseFragment
    protected void init() {
        this.lbm = LocalBroadcastManager.getInstance(this.mActivity);
        this.pslv_voltage.setmDgree(-135.0f);
        initLayout();
        initReceiver();
        if (this.mActivity.screenHeight > 1920) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.cell1.getLayoutParams();
            layoutParams.width = 40;
            layoutParams.height = 20;
            this.cell1.setLayoutParams(layoutParams);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.cell2.getLayoutParams();
            layoutParams2.width = 40;
            layoutParams2.height = 20;
            this.cell2.setLayoutParams(layoutParams2);
            LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.cell3.getLayoutParams();
            layoutParams3.width = 40;
            layoutParams3.height = 30;
            this.cell3.setLayoutParams(layoutParams3);
        }
    }

    @OnClick({R.id.t1btn})
    private void current(View view) {
        showActivity(LinearChatActivity.class, BluStaValue.ACTION_CURRENT);
    }

    @OnClick({R.id.t2btn})
    private void voltage(View view) {
        showActivity(LinearChatActivity.class, BluStaValue.ACTION_VOLTAGE);
    }

    private void initReceiver() {
        this.receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluStaValue.ACTION_SOC);
        intentFilter.addAction(BluStaValue.ACTION_CURRENT);
        intentFilter.addAction(BluStaValue.ACTION_VOLTAGE);
        intentFilter.addAction(BluStaValue.ACTION_CYCLE);
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

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
        /* JADX WARN: Removed duplicated region for block: B:17:0x0037  */
        @Override // android.content.BroadcastReceiver
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void onReceive(android.content.Context r13, android.content.Intent r14) {
            /*
                Method dump skipped, instruction units count: 438
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: com.gddai.lioncheck.fragment.DeviceOneFragment.MyReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }
}
