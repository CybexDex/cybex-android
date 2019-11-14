package com.cybexmobile.activity.setting;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.help.StoreLanguageHelper;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.AppVersion;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.provider.utils.SpUtil;
import com.cybexmobile.BuildConfig;
import com.cybexmobile.R;
import com.cybexmobile.activity.setting.enotes.SetCloudPasswordActivity;
import com.cybexmobile.activity.setting.help.HelpActivity;
import com.cybexmobile.activity.setting.language.ChooseLanguageActivity;
import com.cybexmobile.activity.setting.theme.ChooseThemeActivity;
import com.cybexmobile.activity.splash.SplashActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.dialog.CommonSelectDialog;
import com.cybexmobile.dialog.FrequencyModeDialog;
import com.cybexmobile.dialog.UnlockMethodSelectorDialog;
import com.cybexmobile.dialog.UnlockTimeSelectDialog;
import com.cybexmobile.shake.AntiShake;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_ORDINARY_MARKET;
import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_REAL_TIME_MARKET;
import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOAD_MODE;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_SELECTED_ITEM;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_UNLOCK_WALLET_PERIOD;
import static com.cybex.basemodule.constant.Constant.PREF_ADDRESS_TO_PUB_MAP;
import static com.cybex.basemodule.constant.Constant.PREF_IS_CARD_PASSWORD_SET;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_LOAD_MODE;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_PARAM_UNLOCK_BY_CARDS;
import static com.cybex.basemodule.constant.Constant.PREF_PASSWORD;
import static com.cybex.basemodule.constant.Constant.PREF_SERVER;
import static com.cybex.basemodule.constant.Constant.PREF_UNLOCK_WALLET_PERIOD;
import static com.cybex.basemodule.constant.Constant.SERVER_OFFICIAL;
import static com.cybex.basemodule.constant.Constant.SERVER_TEST;

public class SettingActivity extends BaseActivity implements FrequencyModeDialog.OnFrequencyModeSelectedListener,
        UnlockMethodSelectorDialog.OnUnlockMethodSelectedListener, UnlockTimeSelectDialog.OnUnlockWalletPeriodSelectedListener{

    @BindView(R.id.setting_layout_log_in_by_card)
    LinearLayout mLogInByEnotesLayout;
    @BindView(R.id.setting_tv_unlock_method)
    TextView mTvUnlockMethod;
    @BindView(R.id.setting_tv_set_cloud_password)
    TextView mTvCloudPasswordSet;
    @BindView(R.id.setting_tv_set_card_password)
    TextView mTvCardPasswordSet;
    @BindView(R.id.setting_tv_language)
    TextView mTvLanguage;
    @BindView(R.id.setting_tv_frequency)
    TextView mTvFrequency;
    @BindView(R.id.setting_tv_lock_time)
    TextView mTvLockTime;
    @BindView(R.id.setting_tv_version)
    TextView mTvVersion;
    @BindView(R.id.setting_tv_theme)
    TextView mTvTheme;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.log_out)
    Button mLogOutButton;
    @BindView(R.id.setting_tv_server)
    TextView mTvServer;
    @BindView(R.id.setting_cv_switch_server)
    CardView mCvSwitchServer;
    @BindView(R.id.setting_sc_switch_server)
    SwitchCompat mScSwitchServer;

    private SharedPreferences mSharedPreference;
    private Unbinder mUnbinder;
    private int mMode;
    private int mSelectedUnlockPeriod;
    private String mName;
    private boolean mIsCardPasswordSet;
    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
            if (fullAccountObject != null) {
                mAccountObject = fullAccountObject.account;
                mLogInByEnotesLayout.setVisibility(View.VISIBLE);
                displayDefaultUnlockSetting();
                displayPassWordSet();
                displayCardPasswordSet();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mUnbinder = ButterKnife.bind(this);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
        mName = mSharedPreference.getString(PREF_NAME, "");
        mMode = mSharedPreference.getInt(PREF_LOAD_MODE, FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI);
        mSelectedUnlockPeriod = mSharedPreference.getInt(PREF_UNLOCK_WALLET_PERIOD, 5);
        setSupportActionBar(mToolbar);
        if (isLoginFromENotes()) {
            Intent intent = new Intent(this, WebSocketService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);

        } else {
            mLogInByEnotesLayout.setVisibility(View.GONE);
        }
        displayLanguage();
        displayFrequency();
        displayUnlockWalletPeriod();
        displayTheme();
        displayVersionNumber();
        displayLogOutButton();
        displayServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChanged(Event.ConfigChanged event) {
        switch (event.getConfigName()) {
            case "THEME_CHANGED":
                recreate();
                break;
            case "EVENT_REFRESH_LANGUAGE":
                recreate();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        if(isLoginFromENotes() && mSharedPreference.getBoolean(PREF_IS_LOGIN_IN, false )) {
            unbindService(mConnection);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadFullAccount(Event.UpdateFullAccount event) {
        if (isLoginFromENotes() && (mAccountObject == null || mAccountObject.active.key_auths.size() != event.getFullAccount().account.active.key_auths.size())) {
            mAccountObject = event.getFullAccount().account;
            mLogInByEnotesLayout.setVisibility(View.VISIBLE);
            displayDefaultUnlockSetting();
            displayPassWordSet();
            displayCardPasswordSet();
        }
    }

    @OnClick(R.id.setting_tv_unlock_method)
    public void onUnlockMethodClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        if (mAccountObject.active.key_auths.size() > 1) {
            UnlockMethodSelectorDialog dialog = new UnlockMethodSelectorDialog();
            dialog.show(getSupportFragmentManager(), UnlockMethodSelectorDialog.class.getSimpleName());
            dialog.setOnUnlockMethodSelectedListener(this);
        }
    }

    @OnClick(R.id.setting_layout_set_cloud_password)
    public void onSetCloudPasswordClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        if (mAccountObject.active.key_auths.size() < 2) {
            Intent intent = new Intent(SettingActivity.this, SetCloudPasswordActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.setting_tv_set_card_password)
    public void onCardPasswordSetClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        if (!mIsCardPasswordSet) {
            Intent intent = new Intent(SettingActivity.this, ChooseLanguageActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.setting_layout_language)
    public void onLanguageClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        Intent intent = new Intent(SettingActivity.this, ChooseLanguageActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.setting_layout_version)
    public void onVersionClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        showLoadDialog();
        checkVersion();
    }

    @OnClick(R.id.setting_layout_theme)
    public void onThemeClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        Intent intent = new Intent(SettingActivity.this, ChooseThemeActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.setting_layout_help_feedback)
    public void onHelpFeedback(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.log_out)
    public void onLoginOutClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        if (isLoginFromENotes()) {
            unbindService(mConnection);
        }
        mSharedPreference.edit().putBoolean(PREF_IS_LOGIN_IN, false).apply();
        mSharedPreference.edit().putString(PREF_NAME, null).apply();
        mSharedPreference.edit().putString(PREF_PASSWORD, null).apply();
        mSharedPreference.edit().putBoolean(PREF_PARAM_UNLOCK_BY_CARDS, true).apply();
        SpUtil.putMap(this, PREF_ADDRESS_TO_PUB_MAP, new HashMap<>());
        SpUtil.putMap(this, "eNotesCardMap", new HashMap<>());
        setLoginFrom(false);
        setLoginPublicKey("");
        setPublicKeyFromCard(null);
        setLoginCybPublicKey("");
        BitsharesWalletWraper.getInstance().cancelLockWalletTime();
        /**
         * fix bug:CYM-558
         * 转账存在锁定期，在锁定期页面没有显示出来
         */
//        BitsharesWalletWraper.getInstance().clearAddressesForLockAsset();
        EventBus.getDefault().post(new Event.LoginOut());
        finish();
    }

    @OnClick(R.id.setting_layout_frequency)
    public void onFrequencyClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        FrequencyModeDialog dialog = new FrequencyModeDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_PARAM_LOAD_MODE, mMode);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), FrequencyModeDialog.class.getSimpleName());
        dialog.setOnFrequencyModeSelectedListener(this);
    }

    @OnClick(R.id.setting_layout_lock_time)
    public void onLockTimeClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        UnlockTimeSelectDialog dialog = new UnlockTimeSelectDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_PARAM_UNLOCK_WALLET_PERIOD, mSelectedUnlockPeriod);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), UnlockTimeSelectDialog.class.getSimpleName());
        dialog.setUnlockWalletPeriodSelectedListener(this);
    }

    @OnCheckedChanged(R.id.setting_sc_switch_server)
    public void onChangeServerClick(CompoundButton button, boolean isChecked) {
        if (!button.isPressed()) {
            return;
        }
        mTvServer.setText(getResources().getString(isChecked ?
                R.string.setting_official_server : R.string.setting_test_server));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString(PREF_SERVER, isChecked ? SERVER_OFFICIAL : SERVER_TEST).apply();
        onLoginOutClick(mLogOutButton);
        //重启app
        restartApp();
    }

    public void restartApp() {
        showLoadDialog();
        mTvServer.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getBaseContext(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getBaseContext().startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 2000);
    }

    private void displayDefaultUnlockSetting() {
        boolean isUnlockByEnotes = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_PARAM_UNLOCK_BY_CARDS, true);
        if (isUnlockByEnotes) {
            mTvUnlockMethod.setText(getResources().getString(R.string.setting_default_unlock_method_enotes));
        } else {
            if (mAccountObject.active.key_auths.size() > 1) {
                mTvUnlockMethod.setText(getResources().getString(R.string.setting_default_unlock_method_password));
            } else {
                mTvUnlockMethod.setText(getResources().getString(R.string.setting_default_unlock_method_enotes));
                mSharedPreference.edit().putBoolean(PREF_PARAM_UNLOCK_BY_CARDS, true).apply();
            }
        }
        if (mAccountObject.active.key_auths.size() > 1) {
            mTvUnlockMethod.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_arrow_forward_right_16_px), null);
        } else {
            mTvUnlockMethod.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    private void displayPassWordSet() {
        if (mAccountObject.active.key_auths.size() > 1) {
           mTvCloudPasswordSet.setText(getResources().getString(R.string.setting_has_already_set));
        } else {
            mTvCloudPasswordSet.setText(getResources().getString(R.string.setting_not_set));
            mTvCloudPasswordSet.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_arrow_forward_right_16_px), null);
        }
    }

    private void displayCardPasswordSet() {
        mIsCardPasswordSet = mSharedPreference.getBoolean(PREF_IS_CARD_PASSWORD_SET,false);
        if (mIsCardPasswordSet) {
            mTvCardPasswordSet.setText(getResources().getString(R.string.setting_has_already_set));
        } else {
            mTvCardPasswordSet.setText(getResources().getString(R.string.setting_not_set));
            mTvCardPasswordSet.setCompoundDrawables(null, null, getDrawable(R.drawable.ic_arrow_forward_right_16_px), null);
        }
    }

    private void displayLanguage() {
        String defaultLanguage = StoreLanguageHelper.getLanguageLocal(this);
        if (defaultLanguage != null) {
            if (defaultLanguage.equals("en")) {
                mTvLanguage.setText(getResources().getString(R.string.setting_language_english));
            } else if (defaultLanguage.equals("zh")) {
                mTvLanguage.setText(getResources().getString(R.string.setting_language_chinese));
            }

        }
    }

    private void displayFrequency() {
        if (mMode == FREQUENCY_MODE_ORDINARY_MARKET) {
            mTvFrequency.setText(getResources().getString(R.string.setting_frequency_ordinary_market));
        } else if (mMode == FREQUENCY_MODE_REAL_TIME_MARKET) {
            mTvFrequency.setText(getResources().getString(R.string.setting_frequency_real_time_market));
        } else if (mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI) {
            mTvFrequency.setText(getResources().getString(R.string.setting_frequency_real_time_market_only_wifi));
        }
    }

    private void displayUnlockWalletPeriod() {
        if (mSelectedUnlockPeriod == 5) {
            mTvLockTime.setText(getResources().getString(R.string.setting_lock_wallet_time_5min));
        } else if (mSelectedUnlockPeriod == 20) {
            mTvLockTime.setText(getResources().getString(R.string.setting_lock_wallet_time_20min));
        } else if (mSelectedUnlockPeriod == 60) {
            mTvLockTime.setText(getResources().getString(R.string.setting_lock_wallet_time_60min));
        }
    }

    private void displayTheme() {
        boolean isNight = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode", false);
        if (isNight) {
            mTvTheme.setText(getString(R.string.setting_theme_light));
        } else {
            mTvTheme.setText(getString(R.string.setting_theme_dark));
        }

    }

    private void displayVersionNumber() {
        String versionName = BuildConfig.VERSION_NAME;
        mTvVersion.setText(versionName);
    }

    private void displayLogOutButton() {
        mLogOutButton.setVisibility(mSharedPreference.getBoolean(PREF_IS_LOGIN_IN, false) ? View.VISIBLE : View.GONE);
    }

    private void displayServer() {
        String server = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_SERVER, SERVER_OFFICIAL);
        mCvSwitchServer.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        mScSwitchServer.setChecked(server.equals(SERVER_OFFICIAL));
        mTvServer.setText(getResources().getString(server.equals(SERVER_OFFICIAL) ? R.string.setting_official_server : R.string.setting_test_server));
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
                        if (appVersion.compareVersion(BuildConfig.VERSION_NAME)) {
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setCancelable(false)
                                    .setTitle(R.string.setting_version_update_available)
                                    .setMessage(R.string.setting_version_update_content)
                                    .setPositiveButton(R.string.setting_version_update_now, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appVersion.getUrl()));
                                            startActivity(browseIntent);
                                        }
                                    })
                                    .setNegativeButton(R.string.setting_version_update_next_time, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.setting_version_is_the_latest), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadDialog();
                    }

                    @Override
                    public void onComplete() {
                        hideLoadDialog();
                    }
                });
    }

    private String getSelectedPeriod(int period) {
        switch (period) {
            case 5:
                return getResources().getString(R.string.setting_lock_wallet_time_5min);
            case 20:
                return getResources().getString(R.string.setting_lock_wallet_time_20min);
            case 60:
                return getResources().getString(R.string.setting_lock_wallet_time_60min);
        }
        return getResources().getString(R.string.setting_lock_wallet_time_5min);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onFrequencyModeSelected(int mode) {
        if (mMode == mode) {
            return;
        }
        mMode = mode;
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putInt(PREF_LOAD_MODE, mMode);
        editor.apply();
        EventBus.getDefault().post(new Event.LoadModeChanged(mMode));
        displayFrequency();
    }

    @Override
    public void onUnlockMethodSelectedListener(boolean isUnlockByCard) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(PREF_PARAM_UNLOCK_BY_CARDS, isUnlockByCard);
        editor.apply();
        displayDefaultUnlockSetting();
    }

    @Override
    public void onUnlockWalletPeriodSelected(int period) {
        if (mSelectedUnlockPeriod == period) {
            return;
        }
        mSelectedUnlockPeriod = period;
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putInt(PREF_UNLOCK_WALLET_PERIOD, mSelectedUnlockPeriod);
        editor.apply();
        EventBus.getDefault().post(new Event.onChangeUnlockWalletPeriod(mSelectedUnlockPeriod));
        displayUnlockWalletPeriod();
    }
}
