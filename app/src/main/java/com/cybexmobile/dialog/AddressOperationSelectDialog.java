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

import com.cybexmobile.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddressOperationSelectDialog extends DialogFragment {

    public static final int OPETATION_CANCEL = 0;//取消
    public static final int OPERATION_COPY = 1;//复制
    public static final int OPERATION_DELETE = 2;//删除

    private int mSelectedOpetation = OPETATION_CANCEL;

    private OnAddressOperationSelectedListener mAddressOperationSelectedListener;

    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog_Bottom);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dialog_address_operation_select, window.findViewById(android.R.id.content), false);
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
        if(mAddressOperationSelectedListener != null){
            mAddressOperationSelectedListener.onAddressOperationSelected(mSelectedOpetation);
        }
    }

    @OnClick({R.id.dialog_address_operation_select_tv_copy, R.id.dialog_address_operation_select_tv_delete,
            R.id.dialog_address_operation_select_tv_cancel})
    public void onModeSelected(View view){
        switch (view.getId()) {
            case R.id.dialog_address_operation_select_tv_copy:
                mSelectedOpetation = OPERATION_COPY;
                break;
            case R.id.dialog_address_operation_select_tv_delete:
                mSelectedOpetation = OPERATION_DELETE;
                break;
            case R.id.dialog_address_operation_select_tv_cancel:
                mSelectedOpetation = OPETATION_CANCEL;
                break;
        }
        this.dismiss();
    }

    public void setOnAddressOperationSelectedListener(OnAddressOperationSelectedListener addressOperationSelectedListener){
        mAddressOperationSelectedListener = addressOperationSelectedListener;
    }

    public interface OnAddressOperationSelectedListener {
        void onAddressOperationSelected(int operation);
    }
}
