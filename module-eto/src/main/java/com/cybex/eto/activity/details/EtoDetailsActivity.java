package com.cybex.eto.activity.details;

import android.app.Activity;
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

import com.alibaba.android.arouter.launcher.ARouter;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.eto.R;
import com.cybex.eto.activity.attendETO.AttendETOActivity;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectUserDetails;
import com.cybex.provider.http.entity.EtoUserStatus;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import javax.inject.Inject;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ETO_PROJECT_DETAILS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOGIN_IN;

public class EtoDetailsActivity extends EtoBaseActivity implements EtoDetailsView {

    private static final int REQUEST_CODE_LOGIN = 1;

    @Inject
    EtoDetailsPresenter<EtoDetailsView> mEtoDetailsPresenter;

    Toolbar mToolbar;
    ImageView mStatusIv;
    ImageView mProjectIconIv;
    TextView mProjectTitleTv;
    ProgressBar mProjectPb;
    TextView mProgressPercentTv;
    TextView mProjectTimeLabelTv;
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
        EtoProject etoProject = (EtoProject) getIntent().getSerializableExtra(INTENT_PARAM_ETO_PROJECT_DETAILS);
        if (mEtoDetailsPresenter.isLogIn(this)) {
            String userName = mEtoDetailsPresenter.getUserName(this);
            mEtoDetailsPresenter.loadDetailsWithUserStatus(etoProject, userName);
        } else {
            showAgreementStatus(etoProject, null, false);
            showDetails(etoProject);
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
        mProjectTimeLabelTv = findViewById(R.id.eto_details_time_label_tv);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(INTENT_PARAM_LOGIN_IN, false)) {

            }
        }
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
    public void onLoadProjectDetailsAndUserStatus(EtoProject etoProject, EtoUserStatus etoUserStatus) {
        showAgreementStatus(etoProject, etoUserStatus, true);
    }

    @Override
    public void onError() {

    }

    private void showDetails(EtoProject etoProject) {
        showProjectStatusIcon(etoProject.getStatus());
        showProjectProfileIcon(etoProject);
        showProjectDetailsIntroductionAndWebsite(etoProject);
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

    private void showProjectProfileIcon(EtoProject etoProject) {
        if (Locale.getDefault().getLanguage().equals("zh")) {
            Picasso.get().load(etoProject.getAdds_logo()).into(mProjectIconIv);
        } else {
            Picasso.get().load(etoProject.getAdds_logo__lang_en()).into(mProjectIconIv);
        }
    }

    private void showProjectDetailsIntroductionAndWebsite(EtoProject etoProject) {
        mProjectTitleTv.setText(etoProject.getName());
        if (etoProject.getCurrent_percent() == 1f) {
            mProjectPb.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress_full));
        }
        mProjectPb.setProgress((int) (etoProject.getCurrent_percent() * 100));
        mProgressPercentTv.setText(String.format("%s%%", etoProject.getCurrent_percent() * 100));
        showProjectTime(etoProject);
        mProjectNameTv.setText(etoProject.getName());
        mProjectTokenNameTv.setText(etoProject.getToken_name());
        mProjectEtoTimeTv.setText(etoProject.getStart_at());
        mProjectEndAtTv.setText(etoProject.getEnd_at());
        mProjectCybexStartTv.setText(etoProject.getEnd_at());
        if (etoProject.getOffer_at() != null) {
            mProjectTokenReleasingTimeTv.setText(etoProject.getOffer_at());
        } else {
            mProjectTokenReleasingTimeTv.setText(getResources().getString(R.string.ETO_details_text_token_releasing_immediately));
        }
        mProjectCurrencyTv.setText(etoProject.getBase_token_name());
        mProjectExchangeRatioTv.setText(String.format(getResources().getString(R.string.ETO_details_text_currency_ratio), etoProject.getBase_token_name(), etoProject.getRate(), etoProject.getToken_name()));
        if (Locale.getDefault().getLanguage().equals("zh")) {
            mProjectIntroductionExpandTv.setText(etoProject.getAdds_advantage());
            mProjectOfficialWebsiteTv.setText(etoProject.getAdds_website());
            mProjectWhitepaperTv.setText(etoProject.getAdds_whitepaper());
        } else {
            mProjectIntroductionExpandTv.setText(etoProject.getAdds_advantage__lang_en());
            mProjectOfficialWebsiteTv.setText(etoProject.getAdds_website__lane_en());
            mProjectWhitepaperTv.setText(etoProject.getAdds_whitepaper__lane_en());
        }
    }


    private void showProjectTime(EtoProject etoProject) {
        String status = etoProject.getStatus();
        if(status.equals(EtoProject.Status.PRE)){
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_start_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), etoProject.getStart_at())/1000)));
        } else if(status.equals(EtoProject.Status.OK)){
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_end_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), etoProject.getEnd_at())/1000)));
        } else if(status.equals(EtoProject.Status.FINISH)){
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_finish_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at())/1000)));
        } else {
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_finish_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at())/1000)));
        }
    }

    private String parseTime(int time){
        if(time <= 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int day = time / DateUtils.DAY_IN_SECOND;
        if(day > 0){
            sb.append(day).append(getResources().getString(R.string.text_day));
        }
        int hours = (time % DateUtils.DAY_IN_SECOND) / DateUtils.HOUR_IN_SECOND;
        if(hours > 0){
            sb.append(hours).append(getResources().getString(R.string.text_hours));
        }
        int minutes = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) / DateUtils.MINUTE_IN_SECOND;
        if(minutes > 0){
            sb.append(minutes).append(getResources().getString(R.string.text_minutes));
        }
        int seconds = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) % DateUtils.MINUTE_IN_SECOND;
        if(seconds > 0){
            sb.append(seconds).append(getResources().getString(R.string.text_seconds));
        }
        return sb.toString();
    }

    private void showAgreementStatus(EtoProject etoProject, EtoUserStatus etoUserStatus, boolean isLogin) {
        if (isLogin) {
            String userKycStatus = etoUserStatus.getKyc_status();
            if (userKycStatus.equals(EtoUserStatus.KycStatus.NOT_STARTED)) {
                mProjectAgreementLl.setVisibility(View.GONE);
            }

        } else {
            if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
                mProjectAppointmentRl.setVisibility(View.GONE);
            } else {
                mProjectAgreementLl.setVisibility(View.GONE);
                mAppointmentButton.setText(getResources().getString(R.string.action_sign_in));
                mAppointmentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ARouter.getInstance().build("/login/loginActivity").navigation(EtoDetailsActivity.this, REQUEST_CODE_LOGIN);
                    }
                });
            }
        }
    }
}
