package com.cybex.eto.fragment;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectStatus;

import java.util.List;

public interface EtoMvpView extends IMvpView {

    void onLoadEtoProjects(List<EtoProject> etoProjects);

    void onLoadEtoBanners(List<EtoBanner> etoBanners);

    void onRefreshEtoProjectStatus(EtoProject etoProject);

}
