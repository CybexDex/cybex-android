package com.cybex.eto.activity.attendETO;

import android.content.Context;
import android.preference.PreferenceManager;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoUserCurrentStatus;
import com.cybex.provider.http.response.EtoBaseResponse;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class AttendETOPresenter<V extends AttendETOView> extends BasePresenter<V> {

    @Inject
    public AttendETOPresenter() {

    }

    public String getUserName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME, "");
    }

    public void getUserCurrent(final String userName, final String projectId) {
        mCompositeDisposable.add(
                Flowable.interval(0, 3, TimeUnit.SECONDS)
                        .flatMap(new Function<Long, Publisher<EtoBaseResponse<EtoUserCurrentStatus>>>() {
                            @Override
                            public Publisher<EtoBaseResponse<EtoUserCurrentStatus>> apply(Long aLong) throws Exception {
                                return RetrofitFactory.getInstance().apiEto().getUserCurrent(userName, projectId);
                            }
                        })
                        .map(new Function<EtoBaseResponse<EtoUserCurrentStatus>, EtoUserCurrentStatus>() {
                            @Override
                            public EtoUserCurrentStatus apply(EtoBaseResponse<EtoUserCurrentStatus> etoUserCurrentStatusEtoBaseResponse) throws Exception {
                                return etoUserCurrentStatusEtoBaseResponse.getResult();
                            }
                        })
                        .retry()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<EtoUserCurrentStatus>() {
                            @Override
                            public void accept(EtoUserCurrentStatus etoUserCurrentStatus) throws Exception {
                                getMvpView().refreshUserSubscribedToken(etoUserCurrentStatus);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                getMvpView().onError();
                            }
                        })
        );
    }


}
