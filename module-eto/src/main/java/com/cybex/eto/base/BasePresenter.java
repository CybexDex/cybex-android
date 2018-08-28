package com.cybex.eto.base;

public class BasePresenter<V extends IMvpView> implements IPresenter<V> {

    private V mvpView;

    @Override
    public void attachView(V mvpView) {
        this.mvpView = mvpView;
    }

    @Override
    public void detachView() {
        this.mvpView = null;
    }

    @Override
    public boolean isAttached() {
        return mvpView != null;
    }

    @Override
    public V getMvpView(){
        return mvpView;
    }
}
