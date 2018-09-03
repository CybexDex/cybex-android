package com.cybex.eto.activity.attendETO;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cybex.eto.R;
import com.cybex.eto.base.EtoBaseActivity;

import javax.inject.Inject;

public class AttendETOActivity extends EtoBaseActivity implements AttendETOView {

    @Inject
    AttendETOPresenter mAttendETOPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_eto);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }
}
