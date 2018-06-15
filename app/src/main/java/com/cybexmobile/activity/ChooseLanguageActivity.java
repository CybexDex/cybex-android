package com.cybexmobile.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.event.Event;
import com.cybexmobile.helper.StoreLanguageHelper;
import com.cybexmobile.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChooseLanguageActivity extends BaseActivity {

    RelativeLayout mChineseLanguageLayout;
    RelativeLayout mEnglishLanguageLayout;
    TextView mTextViewEnglish;
    TextView mTextViewChinese;
    ImageView mImageViewEnglish;
    ImageView mImageViewChinese;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);
        mChineseLanguageLayout = (RelativeLayout) findViewById(R.id.language_select_chinese);
        mEnglishLanguageLayout = (RelativeLayout) findViewById(R.id.language_select_english);
        mTextViewEnglish = (TextView) findViewById(R.id.language_select_text_view_english);
        mTextViewChinese = (TextView) findViewById(R.id.language_select_text_view_chinese);
        mImageViewEnglish = (ImageView) findViewById(R.id.language_select_image_view_english);
        mImageViewChinese = (ImageView) findViewById(R.id.language_select_image_view_chinese);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        clickChineseChange();
        clickEnglishChange();
        getDefaultLanguage();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChanged(Event.ConfigChanged event) {
        switch (event.getConfigName()) {
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

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private void clickChineseChange() {
        mChineseLanguageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewChinese.setTextColor(getResources().getColor(R.color.primary_color_orange));
                mImageViewChinese.setVisibility(View.VISIBLE);
                mTextViewEnglish.setTextColor(getResources().getColor(R.color.font_color_white_dark));
                mImageViewEnglish.setVisibility(View.GONE);
                StoreLanguageHelper.setLanguageLocal(ChooseLanguageActivity.this, "zh");
                EventBus.getDefault().post(new Event.ConfigChanged("EVENT_REFRESH_LANGUAGE"));

            }
        });
    }

    private void clickEnglishChange() {
        mEnglishLanguageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_color_orange));
                mImageViewEnglish.setVisibility(View.VISIBLE);
                mTextViewChinese.setTextColor(getResources().getColor(R.color.font_color_white_dark));
                mImageViewChinese.setVisibility(View.GONE);
                StoreLanguageHelper.setLanguageLocal(ChooseLanguageActivity.this, "en");
                EventBus.getDefault().post(new Event.ConfigChanged("EVENT_REFRESH_LANGUAGE"));
            }
        });
    }

    private void getDefaultLanguage() {
        String defaultLanguage = StoreLanguageHelper.getLanguageLocal(this);
        if (!defaultLanguage.equals("")) {
            if (defaultLanguage.equals("en")) {
                mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_color_orange));
                mImageViewEnglish.setVisibility(View.VISIBLE);
            } else if (defaultLanguage.equals("zh")) {
                mTextViewChinese.setTextColor(getResources().getColor(R.color.primary_color_orange));
                mImageViewChinese.setVisibility(View.VISIBLE);
            }
        } else {
            mTextViewEnglish.setTextColor(getResources().getColor(R.color.primary_color_orange));
            mImageViewEnglish.setVisibility(View.VISIBLE);
        }
    }
}
