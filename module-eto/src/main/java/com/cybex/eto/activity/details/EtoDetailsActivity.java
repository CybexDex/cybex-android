package com.cybex.eto.activity.details;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.transform.CircleTransform;
import com.cybex.basemodule.utils.DateUtils;
import com.cybex.eto.R;
import com.cybex.eto.activity.attendETO.AttendETOActivity;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectStatus;
import com.cybex.provider.http.entity.EtoUserStatus;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.Locale;

import javax.inject.Inject;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ETO_PROJECT_DETAILS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.utils.DateUtils.PATTERN_yyyy_MM_dd_HH_mm_ss;

public class EtoDetailsActivity extends EtoBaseActivity implements EtoDetailsView {

    private static final int REQUEST_CODE_LOGIN = 1;

    @Inject
    EtoDetailsPresenter<EtoDetailsView> mEtoDetailsPresenter;
    EtoProject mEtoProject;
    private String mUserName;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    int mTime = 30;

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
    CheckBox mAgreementSelectionCheckbox;
    TextView mAppointmentStatusTv;
    Button mAppointmentButton;
    RelativeLayout mProjectWhiteListRl;
    ImageView mProjectDetailsExpandArrowIv;
    LinearLayout mProjectDetailsExpandView;
    TextView mProjectNameTv;
    TextView mProjectTokenNameTv;
    TextView mProjectEtoTimeTv;
    TextView mProjectEndAtTv;
    LinearLayout mProjectCybexStartLinerLayout;
    TextView mProjectCybexStartTv;
    TextView mProjectTokenReleasingTimeTv;
    TextView mProjectCurrencyTv;
    TextView mProjectExchangeRatioTv;
    ExpandableTextView mProjectIntroductionExpandTv;
    ImageView mProjectWebsiteExpandArrowIv;
    TextView mProjectOfficialWebsiteTv;
    TextView mProjectWhitepaperTv;
    LinearLayout mProjectDetailsTvLinearLayout;
    TextView mProjectProjectDetailsTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eto_details);
        etoActivityComponent().inject(this);
        mEtoDetailsPresenter.attachView(this);
        EventBus.getDefault().register(this);
        initViews();
        setSupportActionBar(mToolbar);
        setOnclickListener();
        mEtoProject = (EtoProject) getIntent().getSerializableExtra(INTENT_PARAM_ETO_PROJECT_DETAILS);
        mUserName = mEtoDetailsPresenter.getUserName(this);
        if (mEtoDetailsPresenter.isLogIn(this)) {
            mEtoDetailsPresenter.loadDetailsWithUserStatus(mEtoProject, mUserName);
        } else {
            showAgreementStatus(mEtoProject, null, false);
            showDetails(mEtoProject);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        mHandler = null;
        EventBus.getDefault().unregister(this);
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
        mAgreementSelectionCheckbox = findViewById(R.id.eto_select_agreement_check_box);
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
        mProjectCybexStartLinerLayout = findViewById(R.id.eto_details_circulation_on_cybex_linear_layout);
        mProjectTokenReleasingTimeTv = findViewById(R.id.eto_details_token_releasing_time_tv);
        mProjectCurrencyTv = findViewById(R.id.eto_details_currency_tv);
        mProjectExchangeRatioTv = findViewById(R.id.eto_details_exchange_ratio_tv);
        mProjectIntroductionExpandTv = findViewById(R.id.eto_details_expand_tv);
        mProjectWebsiteExpandArrowIv = findViewById(R.id.eto_details_project_website_expand_arrow_iv);
        mProjectOfficialWebsiteTv = findViewById(R.id.eto_details_official_website_tv);
        mProjectWhitepaperTv = findViewById(R.id.eto_details_white_paper_tv);
        mProjectDetailsTvLinearLayout = findViewById(R.id.eto_details_project_details_linear_layout);
        mProjectProjectDetailsTv = findViewById(R.id.eto_details_project_details_tv);
    }

    private void setOnclickListener() {
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

        mProjectWhiteListRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Locale.getDefault().getLanguage().equals("zh") ? mEtoProject.getAdds_whitelist() : mEtoProject.getAdds_whitelist__lang_en()));
                startActivity(browserIntent);
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
                String userName = data.getStringExtra(INTENT_PARAM_NAME);
                mAppointmentButton.setVisibility(View.INVISIBLE);
                mProjectAgreementLl.setVisibility(View.VISIBLE);
                mEtoDetailsPresenter.loadDetailsWithUserStatus(mEtoProject, userName);
            }
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLoadProjectDetailsAndUserStatus(EtoProject etoProject, EtoUserStatus etoUserStatus) {
        showAgreementStatus(etoProject, etoUserStatus, true);
        showDetails(etoProject);
    }

    @Override
    public void onRegisterSuccess(Dialog dialog) {
        dialog.dismiss();
        CybexDialog.showETOReserveSucessDialog(this, getResources().getString(R.string.ETO_details_dialog_message_success));
        mAppointmentButton.setVisibility(View.INVISIBLE);
        mProjectAgreementLl.setVisibility(View.VISIBLE);
        mEtoDetailsPresenter.loadDetailsWithUserStatus(mEtoProject, mUserName);
    }

    @Override
    public void onRegisterError(String message, LinearLayout layout, TextView textView, final Button button, final Dialog dialog) {
        layout.setVisibility(View.VISIBLE);
        textView.setText(message);
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int time = mTime;
                    mTime--;
                    if (time <= 0) {
                        mHandler.removeCallbacks(this);
                        button.setText(getResources().getString(R.string.dialog_text_confirm));
                        button.setEnabled(true);
                        mTime = 30;
                        mRunnable = null;
                    } else {
                        button.setText(time + "s");
                        button.setEnabled(false);
                        mHandler.postDelayed(this, 1000);
                    }
                }
            };
            mHandler.postDelayed(mRunnable, 1000);
        }
    }

    @Override
    public void onError() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshProjectStatus(Event.OnRefreshEtoProject refreshEtoProject) {
        EtoProjectStatus etoProjectStatus = refreshEtoProject.getEtoProjectStatus();
        if (!etoProjectStatus.getId().equals(mEtoProject.getId())) {
            return;
        }
        mEtoProject.setCurrent_percent(etoProjectStatus.getCurrent_percent());
        mEtoProject.setCurrent_base_token_count(etoProjectStatus.getCurrent_base_token_count());
        mEtoProject.setFinish_at(etoProjectStatus.getFinish_at());
        mEtoProject.setStatus(etoProjectStatus.getStatus());
        setProgress();
        showProjectTime(mEtoProject);
    }

    private void setProgress() {
        float progress = new BigDecimal(String.valueOf(mEtoProject.getCurrent_percent()))
                .multiply(new BigDecimal(String.valueOf(100))).floatValue();

        mProjectPb.setProgress((int) (progress));
        mProgressPercentTv.setText(String.format(Locale.US, "%.2f%%", progress));
    }

    private void showDetails(EtoProject etoProject) {
        showProjectStatusIcon(etoProject.getStatus());
        showProjectProfileIcon(etoProject);
        showProjectDetailsIntroductionAndWebsite(etoProject);
    }

    private void showProjectStatusIcon(String status) {
        switch (status) {
            case EtoProject.Status.PRE:
                if (Locale.getDefault().getLanguage().equals("zh")) {
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night_mode", false)) {
                        mStatusIv.setImageResource(R.drawable.img_coming_light_zh);
                    } else {
                        mStatusIv.setImageResource(R.drawable.img_coming_dark_zh);
                    }
                } else {
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night_mode", false)) {
                        mStatusIv.setImageResource(R.drawable.img_coming_light);
                    } else {
                        mStatusIv.setImageResource(R.drawable.img_coming_dark);
                    }
                }
                break;
            case EtoProject.Status.OK:
                if (Locale.getDefault().getLanguage().equals("zh")) {
                    mStatusIv.setImageResource(R.drawable.img_inprogress_zh);
                } else {
                    mStatusIv.setImageResource(R.drawable.img_inpprogress);
                }
                break;
            case EtoProject.Status.FINISH:
                if (Locale.getDefault().getLanguage().equals("zh")) {
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night_mode", false)) {
                        mStatusIv.setImageResource(R.drawable.img_finish_light_zh);
                    } else {
                        mStatusIv.setImageResource(R.drawable.img_finish_dark_zh);
                    }
                } else {
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night_mode", false)) {
                        mStatusIv.setImageResource(R.drawable.img_finish_light);
                    } else {
                        mStatusIv.setImageResource(R.drawable.img_finish_dark);
                    }
                }
                break;

        }
    }

    private void showProjectProfileIcon(EtoProject etoProject) {
        if (Locale.getDefault().getLanguage().equals("zh")) {
            Picasso.get().load(etoProject.getAdds_logo_mobile()).transform(new CircleTransform()).into(mProjectIconIv);
        } else {
            Picasso.get().load(etoProject.getAdds_logo_mobile__lang_en()).transform(new CircleTransform()).into(mProjectIconIv);
        }
    }

    private void showProjectDetailsIntroductionAndWebsite(EtoProject etoProject) {
        mProjectTitleTv.setText(etoProject.getName());
        setProgress();
        showProjectTime(etoProject);
        mProjectNameTv.setText(etoProject.getName());
        mProjectTokenNameTv.setText(etoProject.getToken_name());
        mProjectEtoTimeTv.setText(DateUtils.formatToDate(PATTERN_yyyy_MM_dd_HH_mm_ss, DateUtils.formatToMillsETO(etoProject.getStart_at())));
        mProjectEndAtTv.setText(DateUtils.formatToDate(PATTERN_yyyy_MM_dd_HH_mm_ss, DateUtils.formatToMillsETO(etoProject.getEnd_at())));
        if (etoProject.getLock_at() != null) {
            mProjectCybexStartTv.setText(DateUtils.formatToDate(PATTERN_yyyy_MM_dd_HH_mm_ss, DateUtils.formatToMillsETO(etoProject.getLock_at())));
        } else {
            mProjectCybexStartLinerLayout.setVisibility(View.GONE);
        }
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
            if (etoProject.getAdds_detail() != null) {
                mProjectProjectDetailsTv.setText(etoProject.getAdds_detail());
            } else {
                mProjectDetailsTvLinearLayout.setVisibility(View.GONE);
            }
        } else {
            mProjectIntroductionExpandTv.setText(etoProject.getAdds_advantage__lang_en());
            mProjectOfficialWebsiteTv.setText(etoProject.getAdds_website__lang_en());
            mProjectWhitepaperTv.setText(etoProject.getAdds_whitepaper__lang_en());
            if (etoProject.getAdds_detail() != null) {
                mProjectProjectDetailsTv.setText(etoProject.getAdds_detail__lang_en());
            } else {
                mProjectDetailsTvLinearLayout.setVisibility(View.GONE);
            }
        }
    }


    private void showProjectTime(EtoProject etoProject) {
        String status = etoProject.getStatus();
        if (status.equals(EtoProject.Status.PRE)) {
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_start_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), DateUtils.formatToMillsETO(etoProject.getStart_at())) / 1000), false));
        } else if (status.equals(EtoProject.Status.OK)) {
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_end_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(System.currentTimeMillis(), DateUtils.formatToMillsETO(etoProject.getEnd_at())) / 1000), false));
        } else if (status.equals(EtoProject.Status.FINISH)) {
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_finish_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at()) / 1000), true));
            mProjectPb.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress_full));
            mProgressPercentTv.setTextColor(getResources().getColor(R.color.font_color_white_dark));
        } else {
            mProjectTimeLabelTv.setText(getResources().getString(R.string.text_finish_of_distance));
            mProjectTimeTv.setText(parseTime((int) (DateUtils.timeDistance(etoProject.getStart_at(), etoProject.getFinish_at()) / 1000), true));
            mProjectPb.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress_full));
            mProgressPercentTv.setTextColor(getResources().getColor(R.color.font_color_white_dark));
        }
    }

    private String parseTime(int time, boolean isFinish){
        if(time <= 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        if(isFinish || time >= DateUtils.MINUTE_IN_SECOND){
            int day = time / DateUtils.DAY_IN_SECOND;
            sb.append(day).append(getResources().getString(R.string.text_day));
            int hours = (time % DateUtils.DAY_IN_SECOND) / DateUtils.HOUR_IN_SECOND;
            sb.append(hours).append(getResources().getString(R.string.text_hours));
            int minutes = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) / DateUtils.MINUTE_IN_SECOND;
            sb.append(minutes).append(getResources().getString(R.string.text_minutes));
            if(isFinish){
                int seconds = ((time % DateUtils.DAY_IN_SECOND) % DateUtils.HOUR_IN_SECOND) % DateUtils.MINUTE_IN_SECOND;
                sb.append(seconds).append(getResources().getString(R.string.text_seconds));
            }
        } else {
            sb.append(getResources().getString(R.string.text_less_than_minute));
        }
        return sb.toString();
    }

    private void showAgreementStatus(EtoProject etoProject, EtoUserStatus etoUserStatus, boolean isLogin) {
        if (isLogin) {
            String userKycStatus = etoUserStatus.getKyc_status();
            String userStatus = etoUserStatus.getStatus();
            //判断是否通过kyc
            if (userKycStatus.equals(EtoUserStatus.KycStatus.NOT_STARTED)) {
                mProjectAgreementLl.setVisibility(View.GONE);
                mAppointmentButton.setVisibility(View.VISIBLE);
                mAppointmentButton.setText(getResources().getString(R.string.ETO_details_kyc_verification));
                mAppointmentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://icoape.com"));
                        startActivity(browserIntent);
                    }
                });
            } else {
                //用户是否预约
                if (userStatus.equals(EtoUserStatus.Status.UNSTART)) {
                    //项目是否可预约
                    if (etoProject.isIs_user_in().equals("1")) {
                        showStatusUserNotMakeAppointment(etoProject);
                    } else {
                        if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
                            mProjectAppointmentRl.setVisibility(View.GONE);
                        } else {
                            mProjectAgreementLl.setVisibility(View.GONE);
                            mAppointmentButton.setVisibility(View.VISIBLE);
                            mAppointmentButton.setText(getResources().getString(R.string.ETO_details_stop_reserve));
                            mAppointmentButton.setEnabled(false);
                        }
                    }
                } else {
                    if (userStatus.equals(EtoUserStatus.Status.OK)) {
                        if (etoProject.isIs_user_in().equals("1")) {
                            showStatusUserPassVerifying(etoProject);
                        }
                    } else if (userStatus.equals(EtoUserStatus.Status.WAITING)) {
                        if (etoProject.isIs_user_in().equals("1")) {
                            if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
                                mProjectAppointmentRl.setVisibility(View.GONE);
                            } else {
                                mAgreementSelectionCheckbox.setBackground(getResources().getDrawable(R.drawable.ic_selected_agreement_grey));
                                mAgreementSelectionCheckbox.setChecked(true);
                                mAgreementSelectionCheckbox.setEnabled(false);
                                mAppointmentStatusTv.setVisibility(View.VISIBLE);
                                mAppointmentStatusTv.setText(getResources().getString(R.string.ETO_details_verifying));
                                mAppointmentStatusTv.setBackground(getResources().getDrawable(R.drawable.rect_board));
                            }
                        }
                    } else if (userStatus.equals(EtoUserStatus.Status.REJECT)) {
                        if (etoProject.isIs_user_in().equals("1")) {
                            if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
                                mProjectAppointmentRl.setVisibility(View.GONE);
                            } else {
                                mAgreementSelectionCheckbox.setBackground(getResources().getDrawable(R.drawable.ic_selected_agreement_grey));
                                mAgreementSelectionCheckbox.setChecked(true);
                                mAgreementSelectionCheckbox.setEnabled(false);
                                mAppointmentStatusTv.setVisibility(View.VISIBLE);
                                mAppointmentStatusTv.setText(getResources().getString(R.string.ETO_details_rejected));
                                mAppointmentStatusTv.setTextColor(getResources().getColor(R.color.font_color_white_dark));
                                mAppointmentStatusTv.setBackground(getResources().getDrawable(R.drawable.rect_board_grey));
                            }
                        }
                    }
                }
            }

        } else {
            if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
                mProjectAppointmentRl.setVisibility(View.GONE);
            } else {
                mProjectAgreementLl.setVisibility(View.GONE);
                mAppointmentButton.setVisibility(View.VISIBLE);
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

    private void showStatusUserNotMakeAppointment(final EtoProject etoProject) {
        if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
            mProjectAppointmentRl.setVisibility(View.GONE);
        } else {
            final Dialog dialog = new Dialog(EtoDetailsActivity.this);
            mAppointmentButton.setVisibility(View.VISIBLE);
            mAppointmentButton.setText(getResources().getString(R.string.ETO_details_reserve_now));
            mAppointmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mAgreementSelectionCheckbox.isChecked()) {
                        ToastMessage.showNotEnableDepositToastMessage(EtoDetailsActivity.this, getResources().getString(R.string.ETO_details_toast_message), R.drawable.ic_error_16px);
                    } else {
                        CybexDialog.showVerifyPinCodeETODialog(dialog, getResources().getString(R.string.ETO_details_dialog_invitation_code), new CybexDialog.ConfirmationDialogClickWithButtonTimerListener() {
                            @Override
                            public void onClick(final Dialog dialog, final Button button, EditText editText, TextView textView, LinearLayout linearLayout) {
                                String inputCode = editText.getText().toString().trim();
                                if (inputCode.isEmpty()) {
                                    linearLayout.setVisibility(View.VISIBLE);
                                    textView.setText(getResources().getString(R.string.ETO_details_dialog_no_invitation_code_error));
                                } else {
                                    linearLayout.setVisibility(View.GONE);
                                    mEtoDetailsPresenter.registerETO(mUserName, etoProject.getId(), inputCode, linearLayout, textView, button, dialog);
                                }
                            }
                        }, new CybexDialog.ConfirmationDialogCancelListener() {
                            @Override
                            public void onCancel(Dialog dialog) {

                            }
                        }, mHandler, mRunnable);
                    }
                }
            });
        }
    }

    private void showStatusUserPassVerifying(final EtoProject etoProject) {
        if (etoProject.getStatus().equals(EtoProject.Status.FINISH)) {
            mProjectAppointmentRl.setVisibility(View.GONE);
        } else if (etoProject.getStatus().equals(EtoProject.Status.PRE)) {
            mAgreementSelectionCheckbox.setBackground(getResources().getDrawable(R.drawable.ic_selected_agreement_grey));
            mAgreementSelectionCheckbox.setChecked(true);
            mAgreementSelectionCheckbox.setEnabled(false);
            mAppointmentStatusTv.setVisibility(View.VISIBLE);
            mAppointmentStatusTv.setText(getResources().getString(R.string.ETO_details_waiting_for_ETO));
            mAppointmentStatusTv.setBackground(getResources().getDrawable(R.drawable.rect_board));
        } else if (etoProject.getStatus().equals(EtoProject.Status.OK)) {
            mAgreementSelectionCheckbox.setBackground(getResources().getDrawable(R.drawable.ic_selected_agreement_grey));
            mAgreementSelectionCheckbox.setChecked(true);
            mAgreementSelectionCheckbox.setEnabled(false);
            mAppointmentButton.setVisibility(View.VISIBLE);
            mAppointmentButton.setText(getResources().getString(R.string.ETO_details_join_eto_now));
            mAppointmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EtoDetailsActivity.this, AttendETOActivity.class);
                    intent.putExtra(Constant.INTENT_PARAM_ETO_ATTEND_ETO, etoProject);
                    startActivity(intent);
                }
            });
        }
    }
}
