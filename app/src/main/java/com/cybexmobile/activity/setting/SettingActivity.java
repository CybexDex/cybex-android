package com.cybexmobile.activity.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.help.StoreLanguageHelper;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.AppVersion;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybexmobile.BuildConfig;
import com.cybexmobile.R;
import com.cybexmobile.activity.setting.help.HelpActivity;
import com.cybexmobile.activity.setting.language.ChooseLanguageActivity;
import com.cybexmobile.activity.setting.theme.ChooseThemeActivity;
import com.cybexmobile.activity.splash.SplashActivity;
import com.cybexmobile.dialog.FrequencyModeDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOAD_MODE;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_LOAD_MODE;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_PASSWORD;
import static com.cybex.basemodule.constant.Constant.PREF_SERVER;
import static com.cybex.basemodule.constant.Constant.SERVER_OFFICIAL;
import static com.cybex.basemodule.constant.Constant.SERVER_TEST;

public class SettingActivity extends BaseActivity implements FrequencyModeDialog.OnFrequencyModeSelectedListener {

    @BindView(R.id.setting_tv_language)
    TextView mTvLanguage;
    @BindView(R.id.setting_tv_frequency)
    TextView mTvFrequency;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mUnbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
        mMode = mSharedPreference.getInt(PREF_LOAD_MODE, FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI);
        setSupportActionBar(mToolbar);
        displayLanguage();
        displayFrequency();
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
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.setting_layout_language)
    public void onLanguageClick(View view){
        Intent intent = new Intent(SettingActivity.this, ChooseLanguageActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.setting_layout_version)
    public void onVersionClick(View view){
        showLoadDialog();
        checkVersion();
    }

    @OnClick(R.id.setting_layout_theme)
    public void onThemeClick(View view){
        Intent intent = new Intent(SettingActivity.this, ChooseThemeActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.setting_layout_help_feedback)
    public void onHelpFeedback(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.log_out)
    public void onLoginOutClick(){
        mSharedPreference.edit().putBoolean(PREF_IS_LOGIN_IN, false).apply();
        mSharedPreference.edit().putString(PREF_NAME, null).apply();
        mSharedPreference.edit().putString(PREF_PASSWORD, null).apply();
        /**
         * fix bug:CYM-558
         * 转账存在锁定期，在锁定期页面没有显示出来
         */
        BitsharesWalletWraper.getInstance().clearAddressesForLockAsset();
        EventBus.getDefault().post(new Event.LoginOut());
        finish();
    }

    @OnClick(R.id.setting_layout_frequency)
    public void onFrequencyClick(View view){
        FrequencyModeDialog dialog = new FrequencyModeDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_PARAM_LOAD_MODE, mMode);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), FrequencyModeDialog.class.getSimpleName());
        dialog.setOnFrequencyModeSelectedListener(this);
    }

    @OnCheckedChanged(R.id.setting_sc_switch_server)
    public void onChangeServerClick(CompoundButton button, boolean isChecked){
        if(!button.isPressed()){
            return;
        }
        mTvServer.setText(getResources().getString(isChecked ?
                R.string.setting_official_server : R.string.setting_test_server));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString(PREF_SERVER, isChecked ? SERVER_OFFICIAL : SERVER_TEST).apply();
        onLoginOutClick();
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

    private void displayFrequency(){
        if(mMode == FREQUENCY_MODE_ORDINARY_MARKET){
            mTvFrequency.setText(getResources().getString(R.string.setting_frequency_ordinary_market));
        } else if(mMode == FREQUENCY_MODE_REAL_TIME_MARKET){
            mTvFrequency.setText(getResources().getString(R.string.setting_frequency_real_time_market));
        } else if(mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI){
            mTvFrequency.setText(getResources().getString(R.string.setting_frequency_real_time_market_only_wifi));
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
                        if(appVersion.compareVersion(BuildConfig.VERSION_NAME)){
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

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onFrequencyModeSelected(int mode) {
        if(mMode == mode){
            return;
        }
        mMode = mode;
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putInt(PREF_LOAD_MODE, mMode);
        editor.apply();
        EventBus.getDefault().post(new Event.LoadModeChanged(mMode));
        displayFrequency();
    }
}
