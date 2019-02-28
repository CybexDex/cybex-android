package com.cybexmobile.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class UnlockMethodSelectorDialog extends DialogFragment {
    @BindView(R.id.dialog_unlock_method_selector_tv_unlock_by_enotes)
    TextView mTvUnlockedByEnotes;
    @BindView(R.id.dialog_unlock_method_selector_tv_unlock_by_password)
    TextView mTvUnlockedByPassword;

    private Unbinder mUnbinder;

    private boolean isUnlockByCard;
    private OnUnlockMethodSelectedListener mOnUnlockMethodSelectedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog_Bottom);
        isUnlockByCard = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isUnlockByEnotes", true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dialog_unlock_method_selector, window.findViewById(android.R.id.content), false);
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
        if (isUnlockByCard) {
            mTvUnlockedByEnotes.setSelected(true);
        } else {
            mTvUnlockedByPassword.setSelected(true);
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
        if (mOnUnlockMethodSelectedListener != null) {
            mOnUnlockMethodSelectedListener.onUnlockMethodSelectedListener(isUnlockByCard);
        }
    }

    @OnClick({R.id.dialog_unlock_method_selector_tv_unlock_by_enotes, R.id.dialog_unlock_method_selector_tv_unlock_by_password, R.id.dialog_unlock_method_selector_button_cancel})
    public void onModeSelected(View view) {
        switch (view.getId()) {
            case R.id.dialog_unlock_method_selector_tv_unlock_by_enotes:
                isUnlockByCard = true;
                break;
            case R.id.dialog_unlock_method_selector_tv_unlock_by_password:
                isUnlockByCard = false;
            case R.id.dialog_unlock_method_selector_button_cancel:
                break;
        }
        this.dismiss();
    }

    public void setOnUnlockMethodSelectedListener(OnUnlockMethodSelectedListener unlockMethodSelectedListener) {
        mOnUnlockMethodSelectedListener = unlockMethodSelectedListener;
    }

    public interface OnUnlockMethodSelectedListener {
        void onUnlockMethodSelectedListener(boolean isUnlockByCard);
    }
}
