package com.cybexmobile.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.cybexmobile.BuildConfig;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.AppVersion;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.AccountFragment;
import com.cybexmobile.fragment.ExchangeFragment;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.fragment.WatchlistFragment;
import com.cybexmobile.helper.BottomNavigationViewHelper;
import com.cybexmobile.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.cybexmobile.activity.MarketsActivity.RESULT_CODE_BACK;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACTION;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;

public class BottomNavigationActivity extends BaseActivity implements WatchlistFragment.OnListFragmentInteractionListener,
        AccountFragment.OnAccountFragmentInteractionListener {

    private BottomNavigationView mBottomNavigationView;
    private static final String KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID = "KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID";
    private static final int REQUEST_CODE_BACK = 1;

    private WatchlistFragment mWatchListFragment;
    private AccountFragment mAccountFragment;
    private ExchangeFragment mExchangeFragment;
    private Context mContext;

    private String mAction;
    private WatchlistData mWatchlistData;

    private long mLastExitTime;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            showFragment(item.getItemId());
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EventBus.getDefault().register(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_nav_button);
        mBottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(mBottomNavigationView);
        mBottomNavigationView.setItemIconTintList(null);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initFragment(savedInstanceState);
        checkVersion();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChanged(Event.ConfigChanged event) {
        switch (event.getConfigName()) {
            case "EVENT_REFRESH_LANGUAGE":
                recreate();
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
        if (mWatchListFragment != null && mWatchListFragment.isAdded()) {
            fm.putFragment(outState, WatchlistFragment.class.getSimpleName(), mWatchListFragment);
        }
        if (mExchangeFragment != null && mExchangeFragment.isAdded()) {
            fm.putFragment(outState, ExchangeFragment.class.getSimpleName(), mExchangeFragment);
        }
        if (mAccountFragment != null && mAccountFragment.isAdded()) {
            fm.putFragment(outState, AccountFragment.class.getSimpleName(), mAccountFragment);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //register after recreate that activity
        recreate();
    }

    private void initFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int selectedId = R.id.navigation_watchlist;
        if (savedInstanceState != null) {
            mWatchListFragment = (WatchlistFragment) fragmentManager.getFragment(savedInstanceState, WatchlistFragment.class.getSimpleName());
            mExchangeFragment = (ExchangeFragment) fragmentManager.getFragment(savedInstanceState, ExchangeFragment.class.getSimpleName());
            mAccountFragment = (AccountFragment) fragmentManager.getFragment(savedInstanceState, AccountFragment.class.getSimpleName());
            selectedId = savedInstanceState.getInt(KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID, R.id.navigation_watchlist);
        }
        showFragment(selectedId);
    }

    /**
     * 延迟加载fragment
     *
     * @param resId
     */
    private void showFragment(int resId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mWatchListFragment != null && mWatchListFragment.isAdded()) {
            transaction.hide(mWatchListFragment);
        }
        if (mExchangeFragment != null && mExchangeFragment.isAdded()) {
            transaction.hide(mExchangeFragment);
        }
        if (mAccountFragment != null && mAccountFragment.isAdded()) {
            transaction.hide(mAccountFragment);
        }
        switch (resId) {
            case R.id.navigation_watchlist:
                if (mWatchListFragment == null) {
                    mWatchListFragment = new WatchlistFragment();
                }
                if (mWatchListFragment.isAdded()) {
                    transaction.show(mWatchListFragment);
                } else {
                    transaction.add(R.id.frame_container, mWatchListFragment, WatchlistFragment.class.getSimpleName());
                }
                break;
            case R.id.navigation_exchange:
                if (mExchangeFragment == null) {
                    mExchangeFragment = ExchangeFragment.getInstance(mAction, mWatchlistData);
                }
                if (mExchangeFragment.isAdded()) {
                    transaction.show(mExchangeFragment);
                } else {
                    transaction.add(R.id.frame_container, mExchangeFragment, ExchangeFragment.class.getSimpleName());
                }
                break;
            case R.id.navigation_account:
                if (mAccountFragment == null) {
                    mAccountFragment = new AccountFragment();
                }
                if (mAccountFragment.isAdded()) {
                    transaction.show(mAccountFragment);
                } else {
                    transaction.add(R.id.frame_container, mAccountFragment, AccountFragment.class.getSimpleName());
                }
                break;
        }
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastExitTime > 2 * 1000) {
                mLastExitTime = currentTime;
                Toast.makeText(this, getResources().getString(R.string.text_press_again_to_exit), Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BACK && resultCode == RESULT_CODE_BACK) {
            if (data != null) {
                mAction = data.getStringExtra(INTENT_PARAM_ACTION);
                mWatchlistData = (WatchlistData) data.getSerializableExtra(INTENT_PARAM_WATCHLIST);
            }
            mBottomNavigationView.setSelectedItemId(R.id.navigation_exchange);
        }
    }

    @Override
    public void onListFragmentInteraction(WatchlistData item, List<WatchlistData> dataList, int position) {
        Intent intent = new Intent(BottomNavigationActivity.this, MarketsActivity.class);
        intent.putExtra(INTENT_PARAM_WATCHLIST, item);
        intent.putExtra("id", position);
        startActivityForResult(intent, REQUEST_CODE_BACK);
    }

    @Override
    public void onAccountFragmentInteraction(Uri uri) {

    }

    private void checkVersion() {
        RetrofitFactory.getInstance()
                .api()
                .checkAppUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AppVersion>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AppVersion appVersion) {
                        if (appVersion.compareVersion("1.0.1")) {
                            if (appVersion.isForceUpdate(BuildConfig.VERSION_NAME)) {
                                if (Locale.getDefault().getLanguage().equals("zh")) {
                                    CybexDialog.showVersionUpdateDialogForced(BottomNavigationActivity.this, appVersion.getCnUpdateInfo(), new CybexDialog.ConfirmationDialogClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            goToUpdate(appVersion.getUrl());
                                        }
                                    });
                                } else {
                                    CybexDialog.showVersionUpdateDialogForced(BottomNavigationActivity.this, appVersion.getEnUpdateInfo(), new CybexDialog.ConfirmationDialogClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            goToUpdate(appVersion.getUrl());
                                        }
                                    });
                                }
                            } else {
                                if (Locale.getDefault().getLanguage().equals("zh")) {
                                    CybexDialog.showVersionUpdateDialog(BottomNavigationActivity.this, appVersion.getCnUpdateInfo(), new CybexDialog.ConfirmationDialogClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            goToUpdate(appVersion.getUrl());
                                        }
                                    });
                                } else {
                                    CybexDialog.showVersionUpdateDialog(BottomNavigationActivity.this, appVersion.getEnUpdateInfo(), new CybexDialog.ConfirmationDialogClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            goToUpdate(appVersion.getUrl());
                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void goToUpdate(String url) {
        Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browseIntent);
    }

}
