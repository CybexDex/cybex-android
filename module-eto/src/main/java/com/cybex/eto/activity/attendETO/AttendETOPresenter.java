package com.cybex.eto.activity.attendETO;

import android.content.Context;
import android.preference.PreferenceManager;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoUserCurrentStatus;
import com.cybex.provider.http.response.EtoBaseResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.reactivestreams.Publisher;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

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
                                String message = throwable.getMessage();
                                if (throwable instanceof HttpException) {
                                    ResponseBody responseBody = ((HttpException) throwable).response().errorBody();
                                    Gson gson = new Gson();
                                    Type typeToken = new TypeToken<EtoBaseResponse<String>>(){}.getType();
                                    EtoBaseResponse<String> response = gson.fromJson(responseBody.string(), typeToken);
                                    message = response.getResult();
                                }
                                getMvpView().onNoUserError(message);
                            }
                        })
        );
    }


}
