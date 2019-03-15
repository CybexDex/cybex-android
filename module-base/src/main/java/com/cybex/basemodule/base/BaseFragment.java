package com.cybex.basemodule.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.cybex.basemodule.R;
import com.cybex.basemodule.dialog.LoadDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.injection.component.BaseActivityComponent;
import com.cybex.basemodule.injection.component.DaggerBaseActivityComponent;
import com.cybex.basemodule.injection.module.BaseActivityModule;
import com.cybex.provider.utils.NetworkUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public abstract class BaseFragment extends Fragment{

    private static final String PARAM_NETWORK_AVAILABLE = "network_available";

    private LoadDialog mLoadDialog;

    public boolean mIsNetWorkAvailable = true;

    private BaseActivityComponent mBaseActivityComponent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            mIsNetWorkAvailable = savedInstanceState.getBoolean(PARAM_NETWORK_AVAILABLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PARAM_NETWORK_AVAILABLE, mIsNetWorkAvailable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkStateChanged(Event.NetWorkStateChanged event){
        mIsNetWorkAvailable = event.getState() != NetworkUtils.TYPE_NOT_CONNECTED;
        onNetWorkStateChanged(mIsNetWorkAvailable);
    }

    public BaseActivityComponent baseActivityComponent() {
        if (mBaseActivityComponent == null) {
            mBaseActivityComponent = DaggerBaseActivityComponent.builder()
                    .baseActivityModule(new BaseActivityModule(getActivity()))
                    .build();
        }
        return mBaseActivityComponent;
    }

    public abstract void onNetWorkStateChanged(boolean isAvailable);

    //show load dialog
    protected final void showLoadDialog() {
        this.showLoadDialog(false);

    }

    protected final void showLoadDialog(boolean isCancelable) {
        if (mLoadDialog == null) {
            mLoadDialog = new LoadDialog(getContext(), R.style.LoadDialog);
        }
        mLoadDialog.setCancelable(isCancelable);
        mLoadDialog.show();
    }

    //hide load dialog
    protected final void hideLoadDialog(){
        if(mLoadDialog != null && mLoadDialog.isShowing()){
            mLoadDialog.dismiss();
        }
    }

    protected boolean isLoginFromENotes() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("enotes", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("from", true);
    }

    protected String getLoginPublicKey() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("enotes", Context.MODE_PRIVATE);
        return sharedPreferences.getString("key", "");
    }

    protected void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

    }
}
