package com.cybex.eto.activity.details;

import android.app.Dialog;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.EtoErrorMsgResponse;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoProjectUserDetails;
import com.cybex.provider.http.entity.EtoRegisterProjectRequest;
import com.cybex.provider.http.entity.EtoUserStatus;
import com.cybex.provider.http.response.EtoBaseResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;


public class EtoDetailsPresenter<V extends EtoDetailsView> extends BasePresenter<V> {

    private EtoProjectUserDetails mEtoProjectUserDetails;

    @Inject
    public EtoDetailsPresenter() {

    }

    public boolean isLogIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_LOGIN_IN, false);
    }

    public String getUserName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME, "");
    }

    public void loadDetailsWithUserStatus(final EtoProject etoProject, String userName) {
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEto()
                .getEtoUserStatus(userName, etoProject.getId())
                .map(new Function<EtoBaseResponse<EtoUserStatus>, EtoUserStatus>() {
                    @Override
                    public EtoUserStatus apply(EtoBaseResponse<EtoUserStatus> etoUserStatusEtoBaseResponse) throws Exception {
                        return etoUserStatusEtoBaseResponse.getResult();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<EtoUserStatus>() {
                    @Override
                    public void accept(EtoUserStatus etoUserStatus) throws Exception {
                        getMvpView().onLoadProjectDetailsAndUserStatus(etoProject, etoUserStatus);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getMvpView().onError();
                    }
                }));
    }

    public void registerETO(String userName, String projectId, String code, final LinearLayout errorLayout, final TextView errorTextView, final Button button, final Dialog dialog) {
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEto()
                .createETO(RequestBody.create(MediaType.parse("application/json"), getRegisterETORequest(userName, projectId, code)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<EtoBaseResponse<EtoErrorMsgResponse>>() {
                    @Override
                    public void accept(EtoBaseResponse<EtoErrorMsgResponse> stringEtoBaseResponse) throws Exception {
                        if (stringEtoBaseResponse.getCode() == 0) {
                            getMvpView().onRegisterSuccess(dialog);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof HttpException) {
                            String message;
                            ResponseBody body = ((HttpException) throwable).response().errorBody();
                            Gson gson = new Gson();
                            Type typeToken = new TypeToken<EtoBaseResponse<EtoErrorMsgResponse>>(){}.getType();
                            EtoBaseResponse<EtoErrorMsgResponse> etoErrorMsgResponse = gson.fromJson(body.string(), typeToken);
                            if (Locale.getDefault().getLanguage().equals("zh")) {
                                message = etoErrorMsgResponse.getResult().getZh();
                            } else {
                                message = etoErrorMsgResponse.getResult().getEn();
                            }
                            getMvpView().onRegisterError(message, errorLayout, errorTextView, button, dialog);

                        }
                    }
                })
        );
    }

    private String getRegisterETORequest(String userName, String projectId, String code) {
        EtoRegisterProjectRequest etoRegisterProjectRequest = new EtoRegisterProjectRequest();
        EtoRegisterProjectRequest.msg msg = new EtoRegisterProjectRequest.msg();
        msg.code = code;
        etoRegisterProjectRequest.project = projectId;
        etoRegisterProjectRequest.user = userName;
        etoRegisterProjectRequest.msg = msg;
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        return gson.toJson(etoRegisterProjectRequest);
    }


}
