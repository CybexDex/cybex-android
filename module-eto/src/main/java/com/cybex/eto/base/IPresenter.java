package com.cybex.eto.base;

public interface IPresenter<V extends IMvpView> {

    void attachView(V mvpView);

    void detachView();

    boolean isAttached();

    V getMvpView();
}
