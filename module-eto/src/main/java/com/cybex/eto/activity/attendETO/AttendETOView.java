package com.cybex.eto.activity.attendETO;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.EtoUserCurrentStatus;

public interface AttendETOView extends IMvpView {
    void refreshUserSubscribedToken(EtoUserCurrentStatus etoUserCurrentStatus);

}
