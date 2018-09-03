package com.cybex.eto.fragment;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.response.EtoBaseResponse;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class EtoPresenter<V extends EtoMvpView> extends BasePresenter<V> {

    @Inject
    public EtoPresenter() {
    }

    public void loadEtoProjects(){
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEto()
                .getEtoProjects(4, 0, "online")
                .map(new Function<EtoBaseResponse<List<EtoProject>>, List<EtoProject>>() {
                    @Override
                    public List<EtoProject> apply(EtoBaseResponse<List<EtoProject>> etoBaseResponse) {
                        return etoBaseResponse.getResult();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<EtoProject>>() {
                    @Override
                    public void accept(List<EtoProject> etoProjects) throws Exception {
                        getMvpView().onLoadEtoProjects(etoProjects);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));
    }

    public void loadEtoBanner(){
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEto()
                .getEtoBanner()
                .map(new Function<EtoBaseResponse<List<EtoBanner>>, List<EtoBanner>>() {
                    @Override
                    public List<EtoBanner> apply(EtoBaseResponse<List<EtoBanner>> etoBaseResponse) {
                        return etoBaseResponse.getResult();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<EtoBanner>>() {
                    @Override
                    public void accept(List<EtoBanner> etoBanners) throws Exception {
                        getMvpView().onLoadEtoBanners(etoBanners);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));
    }
}
