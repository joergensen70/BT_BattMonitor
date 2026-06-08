package com.gddai.lioncheck.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class PageAdapter extends FragmentStatePagerAdapter {
    private FragmentManager fm;
    private List<Fragment> fragments;

    @Override // android.support.v4.view.PagerAdapter
    public int getItemPosition(Object obj) {
        return -2;
    }

    public PageAdapter(FragmentManager fragmentManager, List<Fragment> list) {
        super(fragmentManager);
        this.fm = fragmentManager;
        this.fragments = list;
    }

    public void reload(List<Fragment> list) {
        this.fragments = list;
        notifyDataSetChanged();
    }

    @Override // android.support.v4.view.PagerAdapter
    public int getCount() {
        return this.fragments.size();
    }

    @Override // android.support.v4.app.FragmentStatePagerAdapter
    public Fragment getItem(int i) {
        return this.fragments.get(i);
    }

    @Override // android.support.v4.app.FragmentStatePagerAdapter, android.support.v4.view.PagerAdapter
    public Fragment instantiateItem(ViewGroup viewGroup, int i) {
        Fragment fragment = (Fragment) super.instantiateItem(viewGroup, i);
        this.fm.beginTransaction().show(fragment).commit();
        return fragment;
    }

    @Override // android.support.v4.app.FragmentStatePagerAdapter, android.support.v4.view.PagerAdapter
    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        if (i >= getCount()) {
            return;
        }
        this.fm.beginTransaction().hide(this.fragments.get(i)).commit();
    }
}
