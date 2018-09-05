package com.cybex.eto.activity.details;

import android.content.Context;
import android.preference.PreferenceManager;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectUserDetails;
import com.cybex.provider.http.entity.EtoUserStatus;
import com.cybex.provider.http.response.EtoBaseResponse;

import javax.inject.Inject;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;


public class EtoDetailsPresenter<V extends EtoDetailsView> extends BasePresenter<V> {

    private EtoProjectUserDetails mEtoProjectUserDetails;

    @Inject
    public EtoDetailsPresenter() {

    }

    public boolean isLogIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_LOGIN_IN, false);
    }

    public void loadDetailsData() {

        mCompositeDisposable.add(RetrofitFactory.getInstance()
                    .apiEto()
                    .getEtoProjectDetails("1053")
                    .concatMap(new Function<EtoBaseResponse<EtoProject>, ObservableSource<EtoBaseResponse<EtoUserStatus>>>() {
                        @Override
                        public ObservableSource<EtoBaseResponse<EtoUserStatus>> apply(EtoBaseResponse<EtoProject> etoProjectEtoBaseResponse) throws Exception {
                            mEtoProjectUserDetails = new EtoProjectUserDetails();
                            mEtoProjectUserDetails.setEtoProject(etoProjectEtoBaseResponse.getResult());
                            return RetrofitFactory.getInstance()
                                    .apiEto()
                                    .getEtoUserStatus("name", "1053");
                        }
                    })
                    .map(new Function<EtoBaseResponse<EtoUserStatus>, EtoProjectUserDetails>() {
                        @Override
                        public EtoProjectUserDetails apply(EtoBaseResponse<EtoUserStatus> etoUserStatusEtoBaseResponse) throws Exception {
                            mEtoProjectUserDetails.setEtoUserStatus(etoUserStatusEtoBaseResponse.getResult());
                            return mEtoProjectUserDetails;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<EtoProjectUserDetails>() {
                        @Override
                        public void accept(EtoProjectUserDetails etoProjectUserDetails) throws Exception {
                            getMvpView().onLoadProjectDetails(etoProjectUserDetails);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            getMvpView().onError();
                        }
                    }));
    }

    public void loadDetailsDataWithoutLogin(String projectId) {
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                        .apiEto()
                        .getEtoProjectDetails(projectId)
                        .map(new Function<EtoBaseResponse<EtoProject>, EtoProject>() {
                            @Override
                            public EtoProject apply(EtoBaseResponse<EtoProject> etoProjectEtoBaseResponse) throws Exception {
                                return etoProjectEtoBaseResponse.getResult();
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<EtoProject>() {
                            @Override
                            public void accept(EtoProject etoProject) throws Exception {
                                getMvpView().onLoadProjectDetailsWithoutLogin(etoProject);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                getMvpView().onError();
                            }
                        }));
    }


}
