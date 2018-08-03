package com.cybexmobile.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.cybexmobile.R;
import com.cybexmobile.dialog.LoadDialog;
import com.cybexmobile.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

import static com.cybexmobile.utils.NetworkUtils.TYPE_NOT_CONNECTED;

public abstract class BaseFragment extends Fragment{

    private static final String PARAM_NETWORK_AVAILABLE = "network_available";

    private LoadDialog mLoadDialog;

    public boolean mIsNetWorkAvailable = true;

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
        mIsNetWorkAvailable = event.getState() != TYPE_NOT_CONNECTED;
        onNetWorkStateChanged(mIsNetWorkAvailable);
    }

    public abstract void onNetWorkStateChanged(boolean isAvailable);

    //show load dialog
    protected final void showLoadDialog(){
        if(mLoadDialog == null){
            mLoadDialog = new LoadDialog(getContext(), R.style.LoadDialog);
            mLoadDialog.setCancelable(false);
        }
        mLoadDialog.show();
    }

    //hide load dialog
    protected final void hideLoadDialog(){
        if(mLoadDialog != null && mLoadDialog.isShowing()){
            mLoadDialog.dismiss();
        }
    }
}
