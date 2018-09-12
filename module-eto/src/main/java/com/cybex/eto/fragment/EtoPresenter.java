package com.cybex.eto.fragment;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.basemodule.utils.DateUtils;
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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
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

    public void refreshProjectStatusOk(final EtoProject etoProject){
        mCompositeDisposable.add(Flowable.interval(3, 3, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<EtoBaseResponse<EtoProjectStatus>>>() {
                    @Override
                    public Publisher<EtoBaseResponse<EtoProjectStatus>> apply(Long aLong) {
                        if(etoProject.getStatus().equals(EtoProject.Status.PRE) &&
                                DateUtils.timeDistance(System.currentTimeMillis(), DateUtils.formatToMillsETO(etoProject.getStart_at())) > 0){
                            return Flowable.create(new FlowableOnSubscribe<EtoBaseResponse<EtoProjectStatus>>() {
                                @Override
                                public void subscribe(FlowableEmitter<EtoBaseResponse<EtoProjectStatus>> e) throws Exception {
                                    e.onNext(new EtoBaseResponse<EtoProjectStatus>(0, null));
                                    e.onComplete();
                                }
                            }, BackpressureStrategy.DROP);
                        }
                        return RetrofitFactory.getInstance().apiEto().refreshProjectStatus(etoProject.getId());
                    }
                })
                .map(new Function<EtoBaseResponse<EtoProjectStatus>, EtoProject>() {
                    @Override
                    public EtoProject apply(EtoBaseResponse<EtoProjectStatus> etoBaseResponse) {
                        EtoProjectStatus etoProjectStatus = etoBaseResponse.getResult();
                        if(etoProjectStatus != null){
                            etoProject.setCurrent_percent(etoProjectStatus.getCurrent_percent());
                            etoProject.setCurrent_base_token_count(etoProjectStatus.getCurrent_base_token_count());
                            etoProject.setCurrent_user_count(etoProjectStatus.getCurrent_user_count());
                            etoProject.setStatus(etoProjectStatus.getStatus());
                            etoProject.setFinish_at(etoProjectStatus.getFinish_at());
                        }
                        return etoProject;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<EtoProject>() {
                    @Override
                    public void accept(EtoProject project) throws Exception {
                        getMvpView().onRefreshEtoProjectStatus(project);
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
                .getEtoBanner("mobile")
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
