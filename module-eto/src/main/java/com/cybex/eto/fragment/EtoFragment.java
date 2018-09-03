package com.cybex.eto.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cybex.eto.R;
import com.cybex.eto.R2;
import com.cybex.eto.adapter.EtoRecyclerViewAdapter;
import com.cybex.eto.base.EtoBaseFragment;
import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.squareup.picasso.Picasso;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EtoFragment extends EtoBaseFragment implements EtoMvpView {

    @Inject
    EtoPresenter<EtoMvpView> mEtoPresenter;

    @BindView(R2.id.eto_rv)
    RecyclerView mEtoRv;
    @BindView(R2.id.eto_banner)
    Banner mBanner;

    private EtoRecyclerViewAdapter mEtoRecyclerViewAdapter;

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
        mEtoRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mEtoRecyclerViewAdapter = new EtoRecyclerViewAdapter(getContext(), new ArrayList<EtoProject>());
        mEtoRv.setAdapter(mEtoRecyclerViewAdapter);
        mBanner.setImageLoader(new PicassoImageLoader());
        return view;
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
        List<String> urls = new ArrayList<>();
        for(EtoBanner banner : etoBanners){
            urls.add(banner.getAdds_banner());
        }
        mBanner.setImages(urls);
        mBanner.start();
    }

    @Override
    public void onError() {

    }

    private class PicassoImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Picasso.get()
                    .load((String) path)
                    .into(imageView);
        }
    }
}
