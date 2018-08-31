package com.cybex.basemodule.base;

import io.reactivex.disposables.CompositeDisposable;

public class BasePresenter<V extends IMvpView> implements IPresenter<V> {

    private V mvpView;
    protected CompositeDisposable mCompositeDisposable;

    @Override
    public void attachView(V mvpView) {
        this.mvpView = mvpView;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void detachView() {
        this.mvpView = null;
        mCompositeDisposable.clear();
        mCompositeDisposable = null;
    }

    @Override
    public boolean isAttached() {
        return mvpView != null;
    }

    @Override
    public V getMvpView(){
        return mvpView;
    }

    public void checkAttached() {
        if (!isAttached()) {
            throw new MvpViewNotAttachedException();
        }
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before requesting data to the Presenter");
        }
    }
}
