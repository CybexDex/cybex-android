package com.cybex.eto.activity.details;

import android.app.Dialog;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectUserDetails;
import com.cybex.provider.http.entity.EtoUserStatus;

public interface EtoDetailsView extends IMvpView {
    void onLoadProjectDetailsAndUserStatus(EtoProject etoProject, EtoUserStatus etoUserStatus);
    void onRegisterError(String message, TextView textView, Button button, Dialog dialog);
    void onRegisterSuccess(Dialog dialog);
    void onLoadEtoProject(EtoProject etoProject);
}
