package com.cybex.eto.activity.details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybex.eto.R;
import com.cybex.eto.activity.attendETO.AttendETOActivity;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectUserDetails;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import javax.inject.Inject;

public class EtoDetailsActivity extends EtoBaseActivity implements EtoDetailsView {

    @Inject
    EtoDetailsPresenter<EtoDetailsView> mEtoDetailsPresenter;

    Toolbar mToolbar;
    ImageView mStatusIv;
    ImageView mProjectIconIv;
    TextView mProjectTitleTv;
    ProgressBar mProjectPb;
    TextView mProgressPercentTv;
    TextView mProjectTimeTv;
    RelativeLayout mProjectAppointmentRl;
    LinearLayout mProjectAgreementLl;
    ImageView mAgreementSelectionIv;
    TextView mAppointmentStatusTv;
    Button mAppointmentButton;
    RelativeLayout mProjectWhiteListRl;
    ImageView mProjectDetailsExpandArrowIv;
    LinearLayout mProjectDetailsExpandView;
    TextView mProjectNameTv;
    TextView mProjectTokenNameTv;
    TextView mProjectEtoTimeTv;
    TextView mProjectEndAtTv;
    TextView mProjectCybexStartTv;
    TextView mProjectTokenReleasingTimeTv;
    TextView mProjectCurrencyTv;
    TextView mProjectExchangeRatioTv;
    ExpandableTextView mProjectIntroductionExpandTv;
    ImageView mProjectWebsiteExpandArrowIv;
    TextView mProjectOfficialWebsiteTv;
    TextView mProjectWhitepaperTv;
    TextView mProjectProjectDetailsTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eto_details);
        etoActivityComponent().inject(this);
        mEtoDetailsPresenter.attachView(this);
        initViews();
        setSupportActionBar(mToolbar);
        setOnclickListener();
        if (mEtoDetailsPresenter.isLogIn(this)) {
            mEtoDetailsPresenter.loadDetailsData();
        } else {
            mEtoDetailsPresenter.loadDetailsDataWithoutLogin("1053");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        mStatusIv = findViewById(R.id.eto_details_project_detail_status_image);
        mProjectIconIv = findViewById(R.id.eto_details_project_icon);
        mProjectTitleTv = findViewById(R.id.eto_details_project_name_title);
        mProjectPb = findViewById(R.id.eto_details_progress_bar);
        mProgressPercentTv = findViewById(R.id.eto_details_progress_percentage);
        mProjectTimeTv = findViewById(R.id.eto_details_project_time);
        mProjectAppointmentRl = findViewById(R.id.eto_details_project_appointment_status_layout);
        mProjectAgreementLl = findViewById(R.id.eto_agreement_layout);
        mAgreementSelectionIv = findViewById(R.id.eto_select_agreement_image_view);
        mAppointmentStatusTv = findViewById(R.id.eto_details_project_appointment_status_tv);
        mAppointmentButton = findViewById(R.id.eto_details_project_appointment_button);
        mProjectWhiteListRl = findViewById(R.id.eto_details_white_list_layout);
        mProjectDetailsExpandArrowIv = findViewById(R.id.eto_details_project_details_expand_arrow_iv);
        mProjectDetailsExpandView = findViewById(R.id.eto_details_project_details_expand_view);
        mProjectNameTv = findViewById(R.id.eto_details_project_name_tv);
        mProjectTokenNameTv = findViewById(R.id.eto_details_token_name_tv);
        mProjectEtoTimeTv = findViewById(R.id.eto_details_eto_time_tv);
        mProjectEndAtTv = findViewById(R.id.eto_details_end_at_tv);
        mProjectCybexStartTv = findViewById(R.id.eto_details_circulation_on_cybex_tv);
        mProjectTokenReleasingTimeTv = findViewById(R.id.eto_details_token_releasing_time_tv);
        mProjectCurrencyTv = findViewById(R.id.eto_details_currency_tv);
        mProjectExchangeRatioTv = findViewById(R.id.eto_details_exchange_ratio_tv);
        mProjectIntroductionExpandTv = findViewById(R.id.eto_details_expand_tv);
        mProjectWebsiteExpandArrowIv = findViewById(R.id.eto_details_project_website_expand_arrow_iv);
        mProjectOfficialWebsiteTv = findViewById(R.id.eto_details_official_website_tv);
        mProjectWhitepaperTv = findViewById(R.id.eto_details_white_paper_tv);
        mProjectProjectDetailsTv = findViewById(R.id.eto_details_project_details_tv);
    }

    private void setOnclickListener() {
        mAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EtoDetailsActivity.this, AttendETOActivity.class);
                startActivity(intent);
            }
        });

        mProjectDetailsExpandArrowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProjectDetailsExpandView.getVisibility() == View.VISIBLE) {
                    mProjectDetailsExpandView.setVisibility(View.GONE);
                    mProjectDetailsExpandArrowIv.setImageResource(R.drawable.ic_down_24_px);
                } else {
                    mProjectDetailsExpandView.setVisibility(View.VISIBLE);
                    mProjectDetailsExpandArrowIv.setImageResource(R.drawable.ic_up_arrow_24_px);
                }
            }
        });
        mProjectIntroductionExpandTv.setText("Herdius is a forward-looking cross-chain interaction solution. Herdius USES a private key to get through all the blockchain. All it needs is a Herdius account and a matching Herdius wallet. Without specific tokens, users can use all kinds of blockchain. Centralize the application, touch each kind of ecosystem.Herdius is a forward-looking cross-chain interaction solution. Herdius USES a private key to get through all the blockchain. All it needs is a Herdius account and a matching Herdius wallet. Without specific tokens, users can use all kinds of blockchain. Centralize the application, touch each kind of ecosystem.Herdius is a forward-looking cross-chain interaction solution. ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eto_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.eto_details_share) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLoadProjectDetails(EtoProjectUserDetails etoProjectUserDetails) {
    }

    @Override
    public void onLoadProjectDetailsWithoutLogin(EtoProject etoProject) {
        showProjectStatusIcon(etoProject.getStatus());
    }

    @Override
    public void onError() {

    }

    private void showProjectStatusIcon(String status) {
        switch (status) {
            case "pre":
                mStatusIv.setImageResource(R.drawable.img_coming_light);
                break;
            case "ok":
                mStatusIv.setImageResource(R.drawable.img_inpprogress);
                break;
            case "finish":
                mStatusIv.setImageResource(R.drawable.img_finished_light);

        }
    }
}
