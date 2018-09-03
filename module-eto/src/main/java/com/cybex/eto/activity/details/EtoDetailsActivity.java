package com.cybex.eto.activity.details;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.eto.R;
import com.cybex.eto.base.EtoBaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EtoDetailsActivity extends EtoBaseActivity implements EtoDetailsView {

    private Unbinder mUnbinder;

    @Inject
    EtoDetailsPresenter<EtoDetailsView> mEtoDetailsPresenter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eto_details);
        mUnbinder = ButterKnife.bind(this);
        etoActivityComponent().inject(this);
        mEtoDetailsPresenter.attachView(this);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eto_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eto_details_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }
}
