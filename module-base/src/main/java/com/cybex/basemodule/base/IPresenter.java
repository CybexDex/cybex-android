package com.cybex.basemodule.base;

public interface IPresenter<V extends IMvpView> {

    void attachView(V mvpView);

    void detachView();

    boolean isAttached();

    V getMvpView();
}
