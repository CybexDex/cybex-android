package com.cybexmobile.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.fragment.AccountFragment;
import com.cybexmobile.fragment.data.WatchListData;
import com.cybexmobile.fragment.FaqFragment;
import com.cybexmobile.fragment.WatchLIstFragment;
import com.cybexmobile.helper.BottomNavigationViewHelper;
import com.cybexmobile.R;
import com.cybexmobile.market.MarketStat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class BottomNavigationActivity extends BaseActivity implements WatchLIstFragment.OnListFragmentInteractionListener, FaqFragment.OnFragmentInteractionListener,
        AccountFragment.OnAccountFragmentInteractionListener {

    private BottomNavigationView mBottomNavigationView;
    private static final String KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID = "KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID";

    private WatchLIstFragment mWatchListFragment;
    private FaqFragment mFaqFragment;
    private AccountFragment mAccountFragment;
    private TextView mTvTitle;
    private Toolbar mToolbar;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_watchlist:
                    mTvTitle.setText(R.string.title_watchlist);
                    showFragment(mWatchListFragment);
                    return true;
                case R.id.navigation_faq:
                    mTvTitle.setText(R.string.title_faq);
                    showFragment(mFaqFragment);
                    return true;
                case R.id.navigation_account:
                    mTvTitle.setText(R.string.title_account);
                    showFragment(mAccountFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_nav_button);
        initFragments(savedInstanceState);
        if (mWatchListFragment != null) {
            if (MarketStat.getInstance().getmWatchListDataListHashMap().size() == 0)
                MarketStat.getInstance().startRun(mWatchListFragment,"1.3.0","CYB");
        }
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mTvTitle = findViewById(R.id.tv_title);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        BottomNavigationViewHelper.removeShiftMode(mBottomNavigationView);
        mBottomNavigationView.setItemIconTintList(null);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBottomNavigationView.getLayoutParams();
//        layoutParams.setBehavior(new BottomNavigationBehavior());
        if (savedInstanceState != null) {
            int id = savedInstanceState.getInt(KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID, R.id.navigation_watchlist);
            switch (id) {
                case R.id.navigation_watchlist:
                    mTvTitle.setText(R.string.title_watchlist);
                    showFragment(mWatchListFragment);
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_faq:
                    mTvTitle.setText(R.string.title_faq);
                    showFragment(mFaqFragment);
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_account:
                    mTvTitle.setText(R.string.title_account);
                    showFragment(mAccountFragment);
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            showFragment(mWatchListFragment);
        }
        EventBus.getDefault().register(this);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        switch (string) {
            case "EVENT_REFRESH_LANGUAGE":
                recreate();
                break;
            case "get_message":
                // recreate();
                break;
            case "THEME_CHANGED":
                recreate();
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID, mBottomNavigationView.getSelectedItemId());
        FragmentManager fm = getSupportFragmentManager();
        if (mWatchListFragment.isAdded()) {
            fm.putFragment(outState, WatchLIstFragment.class.getSimpleName(), mWatchListFragment);
        }
        if (mFaqFragment.isAdded()) {
            fm.putFragment(outState, FaqFragment.class.getSimpleName(), mFaqFragment);
        }
        if (mAccountFragment.isAdded()) {
            fm.putFragment(outState, AccountFragment.class.getSimpleName(), mAccountFragment);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initFragments(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mWatchListFragment = new WatchLIstFragment();
            mFaqFragment = new FaqFragment();
            mAccountFragment = new AccountFragment();
        } else {
            mWatchListFragment = (WatchLIstFragment) fragmentManager.getFragment(savedInstanceState, WatchLIstFragment.class.getSimpleName());
            mFaqFragment = (FaqFragment) fragmentManager.getFragment(savedInstanceState, FaqFragment.class.getSimpleName());
            mAccountFragment = (AccountFragment) fragmentManager.getFragment(savedInstanceState, AccountFragment.class.getSimpleName());
        }

        if (!mWatchListFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.frame_container, mWatchListFragment, WatchLIstFragment.class.getSimpleName())
                    .commit();
        }
        if (!mFaqFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.frame_container, mFaqFragment, FaqFragment.class.getSimpleName())
                    .commit();
        }
        if (!mAccountFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.frame_container, mAccountFragment, AccountFragment.class.getSimpleName())
                    .commit();
        }

    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        if (fragment instanceof WatchLIstFragment) {
            fm.beginTransaction()
                    .show(mWatchListFragment)
                    .hide(mFaqFragment)
                    .hide(mAccountFragment)
                    .commit();

        } else if (fragment instanceof FaqFragment) {
            fm.beginTransaction()
                    .show(mFaqFragment)
                    .hide(mAccountFragment)
                    .hide(mWatchListFragment)
                    .commit();
        } else if (fragment instanceof AccountFragment) {
            fm.beginTransaction()
                    .show(mAccountFragment)
                    .hide(mFaqFragment)
                    .hide(mWatchListFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadFragment(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onListFragmentInteraction(WatchListData item, List<WatchListData> dataList, int position) {
        Intent intent = new Intent(BottomNavigationActivity.this, MarketsActivity.class);
        intent.putExtra("base", item.getBase());
        intent.putExtra("quote", item.getQuote());
        intent.putExtra("watchListData", item);
        intent.putExtra("id", position);
        startActivity(intent);
    }

    @Override
    public void onAccountFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
