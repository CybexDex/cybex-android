package com.cybexmobile.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.graphene.chain.Asset;
import com.cybex.provider.graphene.eva.EvaProject;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.response.CybexBaseResponse;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;


/**
 * A simple {@link Fragment} subclass.
 */
public class EvaFragment extends Fragment {
    private static final String ARG_EvaData = "evadata";
    private Unbinder mUnbinder;
    HashMap<String, String> params = new HashMap<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @BindView(R.id.eva_iv_icon)
    ImageView evaProjectIcon;

    @BindView(R.id.eva_projectname)
    TextView projectName;

    @BindView(R.id.eva_projectdesc)
    TextView projectDesc;

    @BindView(R.id.eva_score)
    TextView score;

    @BindView(R.id.eva_hypescore)
    TextView hyperScore;

    @BindView(R.id.eva_riskscore)
    TextView riskScore;

    @BindView(R.id.eva_expectation)
    TextView expectation;

    @BindView(R.id.eva_platform)
    TextView platform;

    @BindView(R.id.eva_industry)
    TextView industry;

    @BindView(R.id.eva_supply)
    TextView supply;

    @BindView(R.id.eva_tokenprice)
    TextView tokenPrice;

    @BindView(R.id.eva_country)
    TextView country;

    @BindView(R.id.eva_introduction)
    TextView introduction;

    public EvaFragment() {
        // Required empty public constructor
    }

    public static EvaFragment newInstance(WatchlistData watchListData) {
        EvaFragment fragment = new EvaFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EvaData, watchListData);
        fragment.setArguments(args);
        return fragment;
    }

    private void loadEvaProjectData() {
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .apiEva()
                .postEvaProjectInfo(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Consumer<CybexBaseResponse<EvaProject>>() {
            @Override
            public void accept(CybexBaseResponse<EvaProject> evaProjectCybexBaseResponse) throws Exception {
                if (evaProjectCybexBaseResponse.getCode() == 0) {
                    refreshView(evaProjectCybexBaseResponse.getData());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        }));
    }

    void refreshView(EvaProject data) {
        Picasso.get().load(data.getLogo()).into(evaProjectIcon);
        projectName.setText(data.getName());
        projectDesc.setText(data.getDescription());
        score.setText(data.getScore());
        hyperScore.setText(data.getHype_score());
        riskScore.setText(data.getRisk_score());
        expectation.setText(data.getInvestment_rating());

        if (TextUtils.isEmpty(data.getPlatform())) {
            ((FrameLayout)platform.getParent()).setVisibility(View.GONE);
        }
        else {
            platform.setText(data.getPlatform());
        }

        if (TextUtils.isEmpty(data.getIndustry())) {
            ((FrameLayout)industry.getParent()).setVisibility(View.GONE);
        }
        else {
            industry.setText(data.getIndustry());
        }

        if (TextUtils.isEmpty(data.getIco_token_supply())) {
            ((FrameLayout)supply.getParent()).setVisibility(View.GONE);
        }
        else {
            supply.setText(data.getIco_token_supply());
        }

        if (TextUtils.isEmpty(data.getToken_price_in_usd())) {
            ((FrameLayout)tokenPrice.getParent()).setVisibility(View.GONE);
        }
        else {
            tokenPrice.setText(data.getToken_price_in_usd());
        }

        if (TextUtils.isEmpty(data.getCountry())) {
            ((FrameLayout)country.getParent()).setVisibility(View.GONE);
        }
        else {
            country.setText(data.getCountry());
        }

        introduction.setText(data.getPremium());

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WatchlistData data = (WatchlistData) getArguments().get(ARG_EvaData);
        String quoteName = AssetUtil.parseSymbol(data.getQuoteSymbol());
        String projectName = AssetPairCache.getInstance().getEvaProjectNameFromToken(quoteName);
        params.put("name", projectName);
        params.put("token_name", quoteName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View evaView = inflater.inflate(R.layout.fragment_eva, container, false);
        mUnbinder = ButterKnife.bind(this, evaView);

        return evaView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadEvaProjectData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }
}
