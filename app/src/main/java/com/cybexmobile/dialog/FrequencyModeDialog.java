package com.cybexmobile.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cybexmobile.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_LOAD_MODE;
import static com.cybexmobile.utils.Constant.FREQUENCY_MODE_ORDINARY_MARKET;
import static com.cybexmobile.utils.Constant.FREQUENCY_MODE_REAL_TIME_MARKET;
import static com.cybexmobile.utils.Constant.FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI;

public class FrequencyModeDialog extends DialogFragment {

    @BindView(R.id.dialog_frequency_mode_select_tv_ordinary_market)
    TextView mTvOrdinaryMarket;
    @BindView(R.id.dialog_frequency_mode_select_tv_real_time_market)
    TextView mTvRealTimeMarket;
    @BindView(R.id.dialog_frequency_mode_select_tv_real_time_market_only_wifi)
    TextView mTvRealTimeMarketOnlyWifi;

    private Unbinder mUnbinder;

    private int mMode;
    private OnFrequencyModeSelectedListener mFrequencyModeSelectedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog_Bottom);
        mMode = getArguments().getInt(INTENT_PARAM_LOAD_MODE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dialog_frequency_mode_select, window.findViewById(android.R.id.content), false);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mMode == FREQUENCY_MODE_ORDINARY_MARKET){
            mTvOrdinaryMarket.setSelected(true);
        } else if(mMode == FREQUENCY_MODE_REAL_TIME_MARKET){
            mTvRealTimeMarket.setSelected(true);
        } else if (mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI){
            mTvRealTimeMarketOnlyWifi.setSelected(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mFrequencyModeSelectedListener != null){
            mFrequencyModeSelectedListener.onFrequencyModeSelected(mMode);
        }
    }

    @OnClick({R.id.dialog_frequency_mode_select_tv_ordinary_market, R.id.dialog_frequency_mode_select_tv_real_time_market,
            R.id.dialog_frequency_mode_select_tv_real_time_market_only_wifi, R.id.dialog_frequency_mode_select_tv_cancel})
    public void onModeSelected(View view){
        switch (view.getId()) {
            case R.id.dialog_frequency_mode_select_tv_ordinary_market:
                mMode = FREQUENCY_MODE_ORDINARY_MARKET;
                break;
            case R.id.dialog_frequency_mode_select_tv_real_time_market:
                mMode = FREQUENCY_MODE_REAL_TIME_MARKET;
                break;
            case R.id.dialog_frequency_mode_select_tv_real_time_market_only_wifi:
                mMode = FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI;
                break;
            case R.id.dialog_frequency_mode_select_tv_cancel:
                break;
        }
        this.dismiss();
    }

    public void setOnFrequencyModeSelectedListener(OnFrequencyModeSelectedListener loadModeSelectedListener){
        mFrequencyModeSelectedListener = loadModeSelectedListener;
    }

    public interface OnFrequencyModeSelectedListener {
        void onFrequencyModeSelected(int mode);
    }

}
