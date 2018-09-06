package com.cybex.eto.activity.details;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectUserDetails;
import com.cybex.provider.http.entity.EtoUserStatus;

public interface EtoDetailsView extends IMvpView {
    void onLoadProjectDetails(EtoProjectUserDetails etoProjectUserDetails);
    void onLoadProjectDetailsWithoutLogin(EtoProject etoProject);
    void onLoadProjectDetailsAndUserStatus(EtoProject etoProject, EtoUserStatus etoUserStatus);
}
