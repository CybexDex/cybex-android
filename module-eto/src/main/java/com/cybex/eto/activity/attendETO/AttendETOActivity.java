package com.cybex.eto.activity.attendETO;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cybex.eto.R;
import com.cybex.eto.base.EtoBaseActivity;

import javax.inject.Inject;

public class AttendETOActivity extends EtoBaseActivity implements AttendETOView {

    @Inject
    AttendETOPresenter<AttendETOView> mAttendETOPresenter;

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_eto);
        etoActivityComponent().inject(this);
        mAttendETOPresenter.attachView(this);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }
}
