package com.cybexmobile.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.cybexmobile.widget.ViewPagerForScrollView;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragmentPageAdapter extends FragmentPagerAdapter {
    private List<Fragment> mListFragment = new ArrayList<>();
    private int mCurrentPosition = -1;

    public OrderHistoryFragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment) {
        mListFragment.add(fragment);
        notifyDataSetChanged();
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

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            ViewPagerForScrollView pager = (ViewPagerForScrollView) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment);
            }
        }
    }


}
