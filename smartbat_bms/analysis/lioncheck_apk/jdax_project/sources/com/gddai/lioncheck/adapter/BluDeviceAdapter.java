package com.gddai.lioncheck.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.adapter.base.BaseRecylerAdapter;
import com.gddai.lioncheck.entity.BlueDevice;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/* JADX INFO: loaded from: classes.dex */
public class BluDeviceAdapter extends BaseRecylerAdapter<BlueDevice> {
    public BluDeviceAdapter(BaseActivity baseActivity) {
        super(baseActivity);
    }

    @Override // com.gddai.lioncheck.adapter.base.BaseRecylerAdapter, android.support.v7.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(createView(viewGroup, R.layout.item_blue_device));
    }

    @Override // com.gddai.lioncheck.adapter.base.BaseRecylerAdapter, android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        ViewHolder viewHolder2 = (ViewHolder) viewHolder;
        viewHolder2.tv_device_name.setText(((BlueDevice) this.mList.get(i)).getDevice().getName());
        viewHolder2.tv_device_mac.setText(((BlueDevice) this.mList.get(i)).getDevice().getAddress());
        viewHolder2.tv_device_rssi.setText(Html.fromHtml("<font color='#1AFFE7'>rssi:&nbsp;</font>" + ((BlueDevice) this.mList.get(i)).getRssi() + ""));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @ViewInject(R.id.tv_device_mac)
        TextView tv_device_mac;

        @ViewInject(R.id.tv_device_name)
        TextView tv_device_name;

        @ViewInject(R.id.tv_device_rssi)
        TextView tv_device_rssi;

        public ViewHolder(View view) {
            super(view);
            ViewUtils.inject(this, view);
        }
    }

    public void refresh() {
        notifyDataSetChanged();
    }
}
