package com.cybexmobile.fragment.main;

import android.content.Context;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.Announce;
import com.cybex.provider.http.entity.CybexBanner;
import com.cybex.provider.http.entity.HotAssetPair;
import com.cybex.provider.http.entity.SubLink;
import com.cybex.provider.http.response.CybexBaseResponse;
import com.cybexmobile.R;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CybexMainPresenter<T extends CybexMainMvpView> extends BasePresenter<T>{

    @Inject
    public CybexMainPresenter(){

    }

    /**
     * 加载公告
     * @param lang
     */
    public void loadAnnounces(String lang){
        mCompositeDisposable.add(RetrofitFactory
                .getInstance()
                .apiMain()
                .getAnnounces(lang)
                .retry()
                .map(new Function<CybexBaseResponse<List<Announce>>, List<Announce>>() {
                    @Override
                    public List<Announce> apply(CybexBaseResponse<List<Announce>> baseResponse) {
                        return baseResponse.getData();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Announce>>() {
                    @Override
                    public void accept(List<Announce> announces) throws Exception {
                        getMvpView().onLoadAnnounces(announces);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));
    }

    public void loadHotAssetPairs(){
        mCompositeDisposable.add(RetrofitFactory
                .getInstance()
                .apiMain()
                .getHotAssetPairs()
                .retry()
                .map(new Function<CybexBaseResponse<List<HotAssetPair>>, List<HotAssetPair>>() {
                    @Override
                    public List<HotAssetPair> apply(CybexBaseResponse<List<HotAssetPair>> baseResponse) {
                        return baseResponse.getData();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<HotAssetPair>>() {
                    @Override
                    public void accept(List<HotAssetPair> hotAssetPairs) throws Exception {
                        getMvpView().onLoadHotAssetPairs(hotAssetPairs);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));

    }

    public void loadSubLinks(String lang, String env, Context context){
        mCompositeDisposable.add(RetrofitFactory
                .getInstance()
                .apiMain()
                .getSubLinks(lang, env)
                .retry()
                .map(new Function<CybexBaseResponse<List<SubLink>>, List<SubLink>>() {
                    @Override
                    public List<SubLink> apply(CybexBaseResponse<List<SubLink>> baseResponse) {
                        List<SubLink> subLinks = baseResponse.getData();
                        Iterator<SubLink> iterator = subLinks.iterator();
                        while (iterator.hasNext()) {
                            SubLink subLink = iterator.next();
                            if (subLink.getStatus().equals(SubLink.Status.OFFLINE)) {
                                iterator.remove();
                            }
                        }
                        return subLinks;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<SubLink>>() {
                    @Override
                    public void accept(List<SubLink> subLinks) throws Exception {
                        getMvpView().onLoadSubLinks(subLinks);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));

    }

    public void loadBanners(String lang){
        mCompositeDisposable.add(RetrofitFactory
                .getInstance()
                .apiMain()
                .getBanners(lang)
                .retry()
                .map(new Function<CybexBaseResponse<List<CybexBanner>>, List<CybexBanner>>() {
                    @Override
                    public List<CybexBanner> apply(CybexBaseResponse<List<CybexBanner>> baseResponse) {
                        List<CybexBanner> banners = baseResponse.getData();
                        Iterator<CybexBanner> iterator = banners.iterator();
                        while (iterator.hasNext()) {
                            CybexBanner banner = iterator.next();
                            if (banner.getStatus().equals(CybexBanner.Status.OFFLINE)) {
                                iterator.remove();
                            }
                        }
                        return baseResponse.getData();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CybexBanner>>() {
                    @Override
                    public void accept(List<CybexBanner> banners) throws Exception {
                        getMvpView().onLoadBanners(banners);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));

    }

}
