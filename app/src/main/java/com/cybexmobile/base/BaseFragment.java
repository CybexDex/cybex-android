package com.cybexmobile.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.cybexmobile.R;
import com.cybexmobile.dialog.LoadDialog;

public class BaseFragment extends Fragment{

    private LoadDialog mLoadDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
