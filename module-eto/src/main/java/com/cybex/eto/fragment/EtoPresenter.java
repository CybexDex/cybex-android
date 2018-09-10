package com.cybex.eto.fragment;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectStatus;
import com.cybex.provider.http.response.CnyResponse;
import com.cybex.provider.http.response.EtoBaseResponse;

import org.reactivestreams.Publisher;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_ORDINARY_MARKET;
import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI;
import static com.cybex.provider.utils.NetworkUtils.TYPE_MOBILE;

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

    public void refreshProjectStatus(final String projectId){
        mCompositeDisposable.add(Flowable.interval(3, 3, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<EtoBaseResponse<EtoProjectStatus>>>() {
                    @Override
                    public Publisher<EtoBaseResponse<EtoProjectStatus>> apply(Long aLong) {
                        return RetrofitFactory.getInstance().apiEto().refreshProjectStatus(projectId);
                    }
                })
                .map(new Function<EtoBaseResponse<EtoProjectStatus>, EtoProjectStatus>() {
                    @Override
                    public EtoProjectStatus apply(EtoBaseResponse<EtoProjectStatus> etoBaseResponse) {
                        EtoProjectStatus etoProjectStatus = etoBaseResponse.getResult();
                        etoProjectStatus.setId(projectId);
                        return etoProjectStatus;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<EtoProjectStatus>() {
                    @Override
                    public void accept(EtoProjectStatus etoProjectStatus) throws Exception {
                        getMvpView().onRefreshEtoProjectStatus(etoProjectStatus);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

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
