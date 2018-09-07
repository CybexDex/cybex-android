package com.cybex.eto.activity.record;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoRecord;
import com.cybex.provider.http.entity.EtoRecordPage;
import com.cybex.provider.http.response.EtoBaseResponse;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class EtoRecordPresenter<V extends EtoRecordMvpView> extends BasePresenter<V> {

    public static final int LOAD_REFRESH = 1;
    public static final int LOAD_MORE = 2;

    @Inject
    public EtoRecordPresenter(){

    }

    public void loadEtoRecords(final int mode, String account, int page, int limit){
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEto()
                .getEtoRecords(account, page, limit)
                .map(new Function<EtoBaseResponse<EtoRecordPage>, List<EtoRecord>>() {
                    @Override
                    public List<EtoRecord> apply(EtoBaseResponse<EtoRecordPage> etoRecordPageEtoBaseResponse) {
                        return etoRecordPageEtoBaseResponse.getResult().getData();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<EtoRecord>>() {
                    @Override
                    public void accept(List<EtoRecord> etoRecords) throws Exception {
                        getMvpView().onLoadEtoRecords(mode, etoRecords);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));
    }
}
