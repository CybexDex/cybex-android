package com.cybex.eto.fragment;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.response.EtoProjectResponse;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EtoPresenter<V extends EtoMvpView> extends BasePresenter<V> {

    @Inject
    public EtoPresenter() {
    }

    public void loadEtoProjects(){
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEto()
                .getEtoProjects(4, 0, "online")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<EtoProjectResponse>>() {
                    @Override
                    public void accept(List<EtoProjectResponse> etoProjectResponses) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));


    }
}
