package com.cybexmobile.fragment.main;

import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.entity.Announce;
import com.cybex.provider.http.entity.CybexBanner;
import com.cybex.provider.http.entity.HotAssetPair;
import com.cybex.provider.http.entity.SubLink;

import java.util.List;

public interface CybexMainMvpView extends IMvpView {

    void onLoadAnnounces(List<Announce> announces);

    void onLoadHotAssetPairs(List<HotAssetPair> hotAssetPairs);

    void onLoadSubLinks(List<SubLink> subLinks);

    void onLoadBanners(List<CybexBanner> banners);
}
