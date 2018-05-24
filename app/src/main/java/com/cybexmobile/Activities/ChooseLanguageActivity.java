package com.cybexmobile.Activities;

import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybexmobile.HelperClass.ActionBarTitleHelper;
import com.cybexmobile.HelperClass.StoreLanguageHelper;
import com.cybexmobile.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.Locale;

public class ChooseLanguageActivity extends AppCompatActivity {

    ActionBar mActionBar;
    RelativeLayout mChineseLanguageLayout;
    RelativeLayout mEnglishLanguageLayout;
    TextView mTextViewEnglish;
    TextView mTextViewChinese;
    ImageView mImageViewEnglish;
    ImageView mImageViewChinese;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateResources(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);
        ActionBarTitleHelper.centeredActionBarTitle(this);
        setBackbutton();
        mChineseLanguageLayout = (RelativeLayout) findViewById(R.id.language_select_chinese);
        mEnglishLanguageLayout = (RelativeLayout) findViewById(R.id.language_select_english);
        mTextViewEnglish = (TextView) findViewById(R.id.language_select_text_view_english);
        mTextViewChinese = (TextView) findViewById(R.id.language_select_text_view_chinese);
        mImageViewEnglish = (ImageView) findViewById(R.id.language_select_image_view_english);
        mImageViewChinese = (ImageView) findViewById(R.id.language_select_image_view_chinese);
        clickChineseChange();
        clickEnglishChange();
        getDefaultLanguage();
        EventBus.getDefault().register(this);
    }

    private void setBackbutton() {
        ImageView backButton;
        TextView mTitile;
        if (getSupportActionBar() != null) {
            backButton = (ImageView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_arrow_back_button);
            mTitile = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.actionbar_title);
            backButton.setVisibility(View.VISIBLE);
            mTitile.setText(getResources().getString(R.string.title_setting));
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Context updateResources(Context context) {
        String language = StoreLanguageHelper.getLanguageLocal(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        switch (string) {
            case "EVENT_REFRESH_LANGUAGE":
                updateResources(getBaseContext());
                recreate();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void clickChineseChange() {
        mChineseLanguageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewChinese.setTextColor(getResources().getColor(R.color.primary_orange));
                mImageViewChinese.setVisibility(View.VISIBLE);
                mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_color_white));
                mImageViewEnglish.setVisibility(View.GONE);
                StoreLanguageHelper.setLanguageLocal(ChooseLanguageActivity.this, "zh");
                EventBus.getDefault().post("EVENT_REFRESH_LANGUAGE");

            }
        });
    }

    private void clickEnglishChange() {
        mEnglishLanguageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_orange));
                mImageViewEnglish.setVisibility(View.VISIBLE);
                mTextViewChinese.setTextColor(getResources().getColor(R.color.primary_color_white));
                mImageViewChinese.setVisibility(View.GONE);
                StoreLanguageHelper.setLanguageLocal(ChooseLanguageActivity.this, "en");
                EventBus.getDefault().post("EVENT_REFRESH_LANGUAGE");
            }
        });
    }

    private void getDefaultLanguage() {
        String defaultLanguage = StoreLanguageHelper.getLanguageLocal(this);
        if (!defaultLanguage.equals("")) {
            if (defaultLanguage.equals("en")) {
                mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_orange));
                mImageViewEnglish.setVisibility(View.VISIBLE);
            } else if (defaultLanguage.equals("zh")) {
                mTextViewChinese.setTextColor(getResources().getColor(R.color.primary_orange));
                mImageViewChinese.setVisibility(View.VISIBLE);
            }
        } else {
            mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_orange));
            mImageViewEnglish.setVisibility(View.VISIBLE);
        }
    }
}
