package com.cybexmobile.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragmentPageAdapter extends FragmentPagerAdapter {
    private List<Fragment> mListFragment = new ArrayList<>();
    private final List<String> mListFragmentTitle = new ArrayList<>();
    int position;

    public OrderHistoryFragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String strTitle) {
        mListFragment.add(fragment);
        mListFragmentTitle.add(strTitle.toLowerCase());

        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mListFragmentTitle.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mListFragment.get(position);
    }

    @Override
    public int getCount() {
        return mListFragment.size();
    }

    @Override
    public long getItemId(int position) {
        return mListFragment.get(position).hashCode();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
    }
}
