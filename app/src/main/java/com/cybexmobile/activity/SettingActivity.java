package com.cybexmobile.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cybexmobile.BuildConfig;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.AppVersion;
import com.cybexmobile.event.Event;
import com.cybexmobile.helper.StoreLanguageHelper;
import com.cybexmobile.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SettingActivity extends BaseActivity {

    private CardView mLanguageSettingView, mThemeSettingView, mSettingVersionView;
    private Button mLogOutButton;
    private SharedPreferences mSharedPreference;
    private Toolbar mToolbar;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
        initViews();
        onClickListener();
        displayLanguage();
        displayTheme();
        displayVersionNumber();
        displayLogOutButton();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        switch (string) {
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
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {
        mLanguageSettingView = findViewById(R.id.setting_language);
        mThemeSettingView = findViewById(R.id.setting_theme);
        mSettingVersionView = findViewById(R.id.setting_version);
        mLogOutButton = findViewById(R.id.log_out);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void onClickListener() {
        mLanguageSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ChooseLanguageActivity.class);
                startActivity(intent);
            }
        });

        mThemeSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ChooseThemeActivity.class);
                startActivity(intent);
            }
        });

        mSettingVersionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadDialog();
                checkVersion();
            }
        });

        mLogOutButton.setOnClickListener(v -> {
            mSharedPreference.edit().putBoolean("isLoggedIn", false).apply();
            mSharedPreference.edit().putString("name", null).apply();
            mSharedPreference.edit().putString("password", null).apply();
            EventBus.getDefault().post(new Event.LoginOut());
            finish();
        });
    }

    private void displayLanguage() {
        String defaultLanguage = StoreLanguageHelper.getLanguageLocal(this);
        TextView textView = mLanguageSettingView.findViewById(R.id.setting_language_type);
        if (defaultLanguage != null) {
            if (defaultLanguage.equals("en")) {
                textView.setText(getResources().getString(R.string.setting_language_english));
            } else if (defaultLanguage.equals("zh")) {
                textView.setText(getResources().getString(R.string.setting_language_chinese));
            }

        }
    }

    private void displayTheme() {
        boolean isNight = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode", false);
        TextView textView = (TextView) mThemeSettingView.findViewById(R.id.setting_theme_content);
        if (isNight) {
            textView.setText(getString(R.string.setting_theme_light));
        } else {
            textView.setText(getString(R.string.setting_theme_dark));
        }

    }

    private void displayVersionNumber() {
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNumber = mSettingVersionView.findViewById(R.id.setting_version_content);
        versionNumber.setText(versionName);
    }

    private void displayLogOutButton() {
        if (mSharedPreference.getBoolean("isLoggedIn", false)) {
            mLogOutButton.setVisibility(View.VISIBLE);
        } else {
            mLogOutButton.setVisibility(View.GONE);
        }
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

}
