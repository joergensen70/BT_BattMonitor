package com.gddai.lioncheck.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gddai.lioncheck.activity.base.BaseActivity;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseRecylerAdapter<T> extends RecyclerView.Adapter {
    protected BaseActivity mContext;
    protected LayoutInflater mInflater;
    protected List<T> mList;
    public OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Object obj, int i);
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i);

    public BaseRecylerAdapter(BaseActivity baseActivity) {
        this.mContext = baseActivity;
        this.mInflater = LayoutInflater.from(baseActivity);
    }

    protected View createView(ViewGroup viewGroup, int i) {
        return this.mInflater.inflate(i, viewGroup, false);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
        if (this.mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.gddai.lioncheck.adapter.base.BaseRecylerAdapter.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    BaseRecylerAdapter.this.mOnItemClickListener.onItemClick(viewHolder.itemView, BaseRecylerAdapter.this.mList.get(i), i);
                }
            });
        }
    }

    @Override // android.support.v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        List<T> list = this.mList;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public void setList(List<T> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public List<T> getList() {
        return this.mList;
    }

    public void addItem(T t) {
        if (this.mList == null) {
            this.mList = new ArrayList();
        }
        this.mList.add(t);
        notifyDataSetChanged();
    }

    public void clear() {
        if (this.mList == null) {
            this.mList = new ArrayList();
        }
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void setFormatText(TextView textView, int i, String str) {
        textView.setText(String.format(this.mContext.getString(i), str));
    }
}
