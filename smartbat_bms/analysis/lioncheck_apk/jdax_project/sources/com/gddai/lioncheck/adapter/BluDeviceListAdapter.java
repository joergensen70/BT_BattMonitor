package com.gddai.lioncheck.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.adapter.base.ArrayListAdapter;
import com.gddai.lioncheck.entity.BlueDevice;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BluDeviceListAdapter extends ArrayListAdapter<BlueDevice> {
    int selectId;
    List<ViewHolder> selectViewH;
    ViewHolder viewHolder;

    public BluDeviceListAdapter(Activity activity) {
        super(activity);
        this.selectId = -1;
        this.selectViewH = null;
    }

    /* JADX WARN: Type inference incomplete: some casts might be missing */
    @Override // com.gddai.lioncheck.adapter.base.ArrayListAdapter
    public void addItem(BlueDevice blueDevice) {
        this.mList.add((T) blueDevice);
    }

    @Override // com.gddai.lioncheck.adapter.base.ArrayListAdapter, android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(this.mContext).inflate(R.layout.item_blue_device, (ViewGroup) null);
            ViewHolder viewHolder = new ViewHolder(view);
            this.viewHolder = viewHolder;
            view.setTag(viewHolder);
        } else {
            this.viewHolder = (ViewHolder) view.getTag();
        }
        if (i == this.selectId) {
            this.viewHolder.rl.setBackground(this.mContext.getResources().getDrawable(R.mipmap.devicebgf));
        }
        this.viewHolder.tv_device_name.setTypeface(Typeface.createFromAsset(this.mContext.getAssets(), "fonts/HelveticaNeueLTStd-BdEx.otf"));
        BlueDevice blueDevice = (BlueDevice) this.mList.get(i);
        if (blueDevice.getDevice().getName() != null) {
            this.viewHolder.tv_device_name.setText(blueDevice.getDevice().getName().replace("SmartBat", "LionCheck"));
        } else {
            this.viewHolder.tv_device_name.setText(blueDevice.getDevice().getName());
        }
        this.viewHolder.tv_device_mac.setText(blueDevice.getDevice().getAddress());
        return view;
    }

    public void setSelectedPosition(int i) {
        this.selectId = i;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @ViewInject(R.id.rl)
        RelativeLayout rl;

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
