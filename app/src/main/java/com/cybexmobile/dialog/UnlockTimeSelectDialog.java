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
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_UNLOCK_WALLET_PERIOD;

public class UnlockTimeSelectDialog extends DialogFragment {
    @BindView(R.id.dialog_unlock_period_select_tv_5_min)
    TextView mTv5min;
    @BindView(R.id.dialog_unlock_period_select_tv_20_min)
    TextView mTv20min;
    @BindView(R.id.dialog_unlock_period_select_tv_60_min)
    TextView mTv60min;

    private Unbinder mUnbinder;

    private int mPeriod;
    private OnUnlockWalletPeriodSelectedListener mUnlockWalletPeriodSelectedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog_Bottom);
        mPeriod = getArguments().getInt(INTENT_PARAM_UNLOCK_WALLET_PERIOD, 5);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dialog_unlock_wallet_period_selector, window.findViewById(android.R.id.content), false);
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
        if (mPeriod == 5) {
            mTv5min.setSelected(true);
        } else if (mPeriod == 20) {
            mTv20min.setSelected(true);
        } else if (mPeriod == 60) {
            mTv60min.setSelected(true);
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
        if(mUnlockWalletPeriodSelectedListener != null){
            mUnlockWalletPeriodSelectedListener.onUnlockWalletPeriodSelected(mPeriod);
        }
    }

    @OnClick({R.id.dialog_unlock_period_select_tv_5_min, R.id.dialog_unlock_period_select_tv_20_min,
            R.id.dialog_unlock_period_select_tv_60_min, R.id.dialog_unlock_period_select_tv_cancel})
    public void onModeSelected(View view){
        switch (view.getId()) {
            case R.id.dialog_unlock_period_select_tv_5_min:
                mPeriod = 5;
                break;
            case R.id.dialog_unlock_period_select_tv_20_min:
                mPeriod = 20;
                break;
            case R.id.dialog_unlock_period_select_tv_60_min:
                mPeriod = 60;
                break;
            case R.id.dialog_unlock_period_select_tv_cancel:
                break;
        }
        this.dismiss();
    }

    public void setUnlockWalletPeriodSelectedListener(OnUnlockWalletPeriodSelectedListener unlockWalletPeriodSelectedListener){
        mUnlockWalletPeriodSelectedListener = unlockWalletPeriodSelectedListener;
    }

    public interface OnUnlockWalletPeriodSelectedListener {
        void onUnlockWalletPeriodSelected(int mode);
    }
}
