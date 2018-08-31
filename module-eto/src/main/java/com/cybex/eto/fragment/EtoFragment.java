package com.cybex.eto.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybex.eto.R;
import com.cybex.eto.base.EtoBaseFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EtoFragment extends EtoBaseFragment implements EtoMvpView {

    @Inject
    EtoPresenter<EtoMvpView> mEtoPresenter;

    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        etoActivityComponent().inject(this);
        mEtoPresenter.attachView(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eto, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEtoPresenter.loadEtoProjects();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
