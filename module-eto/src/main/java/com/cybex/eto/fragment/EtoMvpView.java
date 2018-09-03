package com.cybex.eto.fragment;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;

import java.util.List;

public interface EtoMvpView extends IMvpView {

    void onLoadEtoProjects(List<EtoProject> etoProjects);

    void onLoadEtoBanners(List<EtoBanner> etoBanners);
}
