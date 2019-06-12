package com.cybex.eto.activity.record;

import android.util.Log;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.basemodule.base.IMvpView;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoRecord;
import com.cybex.provider.http.entity.EtoRecordPage;
import com.cybex.provider.http.entity.NewEtoRecord;
import com.cybex.provider.http.response.EtoBaseResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

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
                .map(new Function<EtoBaseResponse<List<NewEtoRecord>>, List<NewEtoRecord>>() {
                    @Override
                    public List<NewEtoRecord> apply(EtoBaseResponse<List<NewEtoRecord>> etoRecordPageEtoBaseResponse) {
                        return etoRecordPageEtoBaseResponse.getResult();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<NewEtoRecord>>() {
                    @Override
                    public void accept(List<NewEtoRecord> etoRecords) throws Exception {
                        getMvpView().onLoadEtoRecords(mode, etoRecords);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("etoRecords", throwable.getMessage());
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
                }));
    }
}
