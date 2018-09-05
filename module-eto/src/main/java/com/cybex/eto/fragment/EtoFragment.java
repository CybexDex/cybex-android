package com.cybex.eto.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cybex.eto.R;
import com.cybex.eto.R2;
import com.cybex.eto.activity.record.EtoRecordActivity;
import com.cybex.eto.adapter.EtoRecyclerViewAdapter;
import com.cybex.eto.base.EtoBaseFragment;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.squareup.picasso.Picasso;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EtoFragment extends EtoBaseFragment implements EtoMvpView, Toolbar.OnMenuItemClickListener {

    @Inject
    EtoPresenter<EtoMvpView> mEtoPresenter;

    private RecyclerView mEtoRv;
    private Banner mBanner;
    private Toolbar mToolbar;

    private EtoRecyclerViewAdapter mEtoRecyclerViewAdapter;

    private Unbinder mUnbinder;

    public static EtoFragment getInstance(){
        EtoFragment etoFragment = new EtoFragment();
        return etoFragment;
    }

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
        initView(view);
        mUnbinder = ButterKnife.bind(this, view);
        mToolbar.inflateMenu(R.menu.menu_eto_record);
        mToolbar.setOnMenuItemClickListener(this);
        mEtoRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mEtoRecyclerViewAdapter = new EtoRecyclerViewAdapter(getContext(), new ArrayList<EtoProject>());
        mEtoRv.setAdapter(mEtoRecyclerViewAdapter);
        mBanner.setImageLoader(new PicassoImageLoader());
        return view;
    }

    private void initView(View view){
        mEtoRv = view.findViewById(R.id.eto_rv);
        mBanner = view.findViewById(R.id.eto_banner);
        mToolbar = view.findViewById(R.id.toolbar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEtoPresenter.loadEtoProjects();
        mEtoPresenter.loadEtoBanner();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEtoPresenter.detachView();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLoadEtoProjects(List<EtoProject> etoProjects) {
        mEtoRecyclerViewAdapter.setData(etoProjects);
    }

    @Override
    public void onLoadEtoBanners(List<EtoBanner> etoBanners) {
        if(etoBanners == null || etoBanners.size() == 0){
            return;
        }
        mBanner.setImages(etoBanners);
        mBanner.start();
    }

    @Override
    public void onError() {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.action_eto_record){
            Intent intent = new Intent(getContext(), EtoRecordActivity.class);
            startActivity(intent);
        }
        return false;
    }

    private class PicassoImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            EtoBanner etoBanner = (EtoBanner) path;
            if(Locale.getDefault().getLanguage().equals("zh")){
                Picasso.get().load(etoBanner.getAdds_banner()).into(imageView);
            } else {
                Picasso.get().load(etoBanner.getAdds_banner__lang_en()).into(imageView);
            }
        }
    }
}
