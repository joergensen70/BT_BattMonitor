package com.gddai.lioncheck.adapter.base;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class ArrayListAdapter<T> extends BaseAdapter {
    protected Activity mContext;
    protected ArrayList<T> mList = new ArrayList<>();

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public abstract View getView(int i, View view, ViewGroup viewGroup);

    public ArrayListAdapter(Activity activity) {
        this.mContext = activity;
    }

    public void setList(ArrayList<T> arrayList) {
        this.mList = arrayList;
        notifyDataSetChanged();
    }

    public void addItem(T t) {
        this.mList.add(t);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<T> arrayList) {
        Iterator<T> it = arrayList.iterator();
        while (it.hasNext()) {
            this.mList.add(it.next());
        }
        notifyDataSetChanged();
    }

    public void deleteItem(T t) {
        this.mList.remove(t);
        notifyDataSetChanged();
    }

    @Override // android.widget.Adapter
    public int getCount() {
        ArrayList<T> arrayList = this.mList;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        ArrayList<T> arrayList = this.mList;
        if (arrayList != null) {
            return arrayList.get(i);
        }
        return null;
    }

    public List<T> getList() {
        if (this.mList == null) {
            this.mList = new ArrayList<>();
        }
        return this.mList;
    }

    public void clearItem() {
        if (this.mList == null) {
            this.mList = new ArrayList<>();
        }
        this.mList.clear();
        notifyDataSetChanged();
    }
}
