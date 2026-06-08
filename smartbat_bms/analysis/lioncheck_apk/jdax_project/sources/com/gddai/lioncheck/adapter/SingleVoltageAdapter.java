package com.gddai.lioncheck.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gddai.lioncheck.R;
import com.gddai.lioncheck.activity.base.BaseActivity;
import com.gddai.lioncheck.adapter.base.BaseRecylerAdapter;
import com.gddai.lioncheck.entity.VoltageEntity;
import com.gddai.lioncheck.service.BlueDataUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class SingleVoltageAdapter extends BaseRecylerAdapter<VoltageEntity> {
    private Context context;
    private int maxPosition;
    private long maxValue;
    private int minPosition;
    private long minValue;
    private long numV;
    private int position;

    public SingleVoltageAdapter(BaseActivity baseActivity) {
        super(baseActivity);
        this.minValue = 0L;
        this.maxValue = 0L;
        this.minPosition = 0;
        this.maxPosition = 0;
        this.position = 1;
        this.numV = 0L;
        this.context = baseActivity;
    }

    @Override // com.gddai.lioncheck.adapter.base.BaseRecylerAdapter, android.support.v7.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(createView(viewGroup, R.layout.item_single_valote));
    }

    @Override // com.gddai.lioncheck.adapter.base.BaseRecylerAdapter, android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        if (this.mList == null) {
            return 1;
        }
        return this.mList.size() + 1;
    }

    @Override // com.gddai.lioncheck.adapter.base.BaseRecylerAdapter, android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        ViewHolder viewHolder2 = (ViewHolder) viewHolder;
        if (i != 0) {
            VoltageEntity voltageEntity = (VoltageEntity) this.mList.get(i - 1);
            viewHolder2.tv_name.setText(this.context.getString(R.string.device_cell_text) + voltageEntity.getBatterySerial());
            viewHolder2.tv_voltage.setText(voltageEntity.getBatteryVolatage() + "");
            viewHolder2.tv_voltage_unit.setText("mV");
            return;
        }
        viewHolder2.tv_name.setText(this.context.getString(R.string.device_name_text));
        viewHolder2.tv_voltage.setText(this.context.getString(R.string.device_valtage_text));
        viewHolder2.tv_voltage_unit.setText(this.context.getString(R.string.device_unit_text));
    }

    public void logValue() {
        LogUtils.e("maxValue" + this.maxValue + "::minValue" + this.minValue);
    }

    /* JADX WARN: Type inference incomplete: some casts might be missing */
    public synchronized void insert(VoltageEntity voltageEntity) {
        if (voltageEntity.getBatterySerial() == this.position && (BlueDataUtils.totalSum - this.numV > 500 || BlueDataUtils.totalSum - this.numV < -500)) {
            this.numV += voltageEntity.getBatteryVolatage();
            this.position++;
        } else if (voltageEntity.getBatterySerial() >= this.position) {
            return;
        }
        if (this.mList == 0) {
            this.mList = new ArrayList();
            this.mList.add((T) voltageEntity);
            sortValue();
            notifyDataSetChanged();
        } else {
            for (int i = 0; i < this.mList.size(); i++) {
                if (((VoltageEntity) this.mList.get(i)).getBatterySerial() > voltageEntity.getBatterySerial()) {
                    this.mList.add(i, (T) voltageEntity);
                    sortValue();
                    notifyDataSetChanged();
                    return;
                } else {
                    if (((VoltageEntity) this.mList.get(i)).getBatterySerial() == voltageEntity.getBatterySerial()) {
                        this.mList.set(i, (T) voltageEntity);
                        sortValue();
                        notifyDataSetChanged();
                        return;
                    }
                }
            }
            this.mList.add((T) voltageEntity);
            sortValue();
            notifyDataSetChanged();
        }
    }

    public void sortValue() {
        if (this.mList == null || this.mList.size() <= 0) {
            return;
        }
        this.minValue = ((VoltageEntity) this.mList.get(0)).getBatteryVolatage();
        this.maxValue = ((VoltageEntity) this.mList.get(0)).getBatteryVolatage();
        this.minPosition = 0;
        this.maxPosition = 0;
        for (int i = 0; i < this.mList.size(); i++) {
            VoltageEntity voltageEntity = (VoltageEntity) this.mList.get(i);
            if (this.maxValue < voltageEntity.getBatteryVolatage()) {
                this.maxPosition = i;
                this.maxValue = voltageEntity.getBatteryVolatage();
            }
            if (this.minValue > voltageEntity.getBatteryVolatage()) {
                this.minPosition = i;
                this.minValue = voltageEntity.getBatteryVolatage();
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @ViewInject(R.id.tv_name)
        TextView tv_name;

        @ViewInject(R.id.tv_voltage)
        TextView tv_voltage;

        @ViewInject(R.id.tv_voltage_unit)
        TextView tv_voltage_unit;

        public ViewHolder(View view) {
            super(view);
            ViewUtils.inject(this, view);
        }
    }
}
