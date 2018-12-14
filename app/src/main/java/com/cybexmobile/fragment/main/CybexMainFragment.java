package com.cybexmobile.fragment.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.help.StoreLanguageHelper;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.eto.utils.PicassoImageLoader;
import com.cybex.provider.http.entity.Announce;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybex.provider.http.entity.CybexBanner;
import com.cybex.provider.http.entity.HotAssetPair;
import com.cybex.provider.http.entity.SubLink;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;

import com.cybex.basemodule.base.BaseFragment;
import com.cybexmobile.activity.web.WebActivity;
import com.cybexmobile.adapter.HotAssetPairRecyclerViewAdapter;
import com.cybexmobile.adapter.SubLinkRecyclerViewAdapter;
import com.cybexmobile.adapter.TopGainerRecyclerViewAdapter;
import com.cybexmobile.fragment.WatchlistFragment;
import com.cybexmobile.helper.GridSpacingItemDecoration;
import com.cybexmobile.injection.base.AppBaseFragment;
import com.cybexmobile.intent.IntentFactory;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class CybexMainFragment extends AppBaseFragment implements CybexMainMvpView, View.OnClickListener,
        OnBannerListener, SubLinkRecyclerViewAdapter.OnItemClickListener{

    @Inject
    CybexMainPresenter<CybexMainMvpView> mPresenter;

    @BindView(R.id.fragment_cybex_main_banner)
    Banner mBanner;
    @BindView(R.id.fragment_cybex_main_rv_hot_pair)
    RecyclerView mRvHotPair;
    @BindView(R.id.fragment_cybex_main_rv_sub_link)
    RecyclerView mRvSubLink;
    @BindView(R.id.fragment_cybex_main_vf_notice)
    ViewFlipper mVfNotice;
    @BindView(R.id.fragment_cybex_main_ll_notice)
    LinearLayout mLayoutNotice;
    @BindView(R.id.fragment_cybex_main_tv_current_block)
    TextView mTvCurrentBlock;
    @BindView(R.id.fragment_cybex_main_tv_last_block)
    TextView mTvLastBlock;
    @BindView(R.id.fragment_cybex_main_tv_average_confirmation_time)
    TextView mTvAverageConfirmationTime;
    @BindView(R.id.fragment_cybex_main_tv_trx_block)
    TextView mTvTrxBlock;
    @BindView(R.id.fragment_cybex_main_rv_top_gainers)
    RecyclerView mRvTopGainers;

    private Unbinder mUnbinder;

    private List<CybexBanner> mCybexBanners;
    private List<SubLink> mSubLinks;
    private List<WatchlistData> mHotWatchlistData;
    private List<WatchlistData> mAllWatchlistData;
    private boolean mIsLoginIn;
    private String mName;

    private SubLinkRecyclerViewAdapter mSubLinkRecyclerViewAdapter;
    private HotAssetPairRecyclerViewAdapter mHotAssetPairRecyclerViewAdapter;
    private TopGainerRecyclerViewAdapter mTopGainerRecyclerViewAdapter;
    private SharedPreferences mPreferences;

    private WebSocketService mWebSocketService;

    private WatchlistFragment.OnListFragmentInteractionListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appActivityComponent().inject(this);
        mPresenter.attachView(this);
        EventBus.getDefault().register(this);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIsLoginIn = mPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = mPreferences.getString(PREF_NAME, "");
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cybex_main, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        GridLayoutManager layoutManager_hotPair = new GridLayoutManager(getContext(), 3);
        GridLayoutManager layoutManager_subLink = new GridLayoutManager(getContext(), 2);
        mRvHotPair.setLayoutManager(layoutManager_hotPair);
        mRvSubLink.setLayoutManager(layoutManager_subLink);
        mRvSubLink.addItemDecoration(new GridSpacingItemDecoration(2, getResources().getDimensionPixelSize(R.dimen.margin_8), false));
        mRvTopGainers.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvTopGainers.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mBanner.isAutoPlay(true);
        mBanner.setDelayTime(3000);
        mBanner.setOnBannerListener(this);
        mBanner.setBannerAnimation(Transformer.ZoomOutSlide);
        mBanner.setImageLoader(new PicassoImageLoader());
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        mVfNotice.setInAnimation(getContext(), R.anim.in_from_bottom);
        mVfNotice.setOutAnimation(getContext(), R.anim.out_to_top);
        mVfNotice.setFlipInterval(5000);
        mVfNotice.setAutoStart(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadAnnounces(StoreLanguageHelper.getLanguageLocal(getContext()));
        mPresenter.loadSubLinks(StoreLanguageHelper.getLanguageLocal(getContext()), getContext());
        mPresenter.loadHotAssetPairs();
        mPresenter.loadBanners(StoreLanguageHelper.getLanguageLocal(getContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        EventBus.getDefault().unregister(this);
        getContext().unbindService(mConnection);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WatchlistFragment.OnListFragmentInteractionListener) {
            mListener = (WatchlistFragment.OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoadAnnounces(List<Announce> announces) {
        if(announces == null || announces.size() == 0){
            mLayoutNotice.setVisibility(View.GONE);
            return;
        }
        for(Announce announce : announces){
            TextView textView = new TextView(getContext());
            textView.setText(announce.getTitle());
            textView.setSingleLine();
            textView.setTextColor(getResources().getColor(R.color.font_color_white_dark));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTag(announce);
            textView.setOnClickListener(this);
            mVfNotice.addView(textView);
        }
    }

    @Override
    public void onLoadHotAssetPairs(List<HotAssetPair> hotAssetPairs) {
        mWebSocketService.loadHotWatchlistData(hotAssetPairs);
    }

    @Override
    public void onLoadSubLinks(List<SubLink> subLinks) {
        mSubLinks = subLinks;
        mSubLinkRecyclerViewAdapter = new SubLinkRecyclerViewAdapter(getContext(), mSubLinks);
        mSubLinkRecyclerViewAdapter.setOnItemClickListener(this);
        mRvSubLink.setAdapter(mSubLinkRecyclerViewAdapter);
    }

    @Override
    public void onLoadBanners(List<CybexBanner> banners) {
        mCybexBanners = banners;
        mBanner.setImages(mCybexBanners);
        mBanner.start();
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof Announce){
            Announce announce = (Announce) v.getTag();
            new IntentFactory()
                    .action(announce.getUrl())
                    .checkLogin(mIsLoginIn)
                    .intent(getContext());
        }
    }

    @Override
    public void OnBannerClick(int position) {
        new IntentFactory()
                .action(mCybexBanners.get(position).getLink())
                .checkLogin(mIsLoginIn)
                .intent(getContext());
    }

    @Override
    public void onItemClick(SubLink subLink) {
        if (subLink.getLink().equals("cybexapp://game") && !mPreferences.getBoolean(Constant.PREF_GAME_INVITATION, false)) {
            CybexDialog.showVerifyPinCodeETODialog(new Dialog(getActivity()), getActivity().getResources().getString(R.string.ETO_details_dialog_invitation_code), new CybexDialog.ConfirmationDialogClickWithButtonTimerListener() {
                @Override
                public void onClick(Dialog dialog, Button button, EditText editText, TextView textView) {
                    String inputCode = editText.getText().toString().trim();
                    textView.setVisibility(View.VISIBLE);
                    if (inputCode.isEmpty()) {
                        textView.setText(getResources().getString(com.cybex.eto.R.string.ETO_details_dialog_no_invitation_code_error));
                    } else {
                        List<String> invitationCodeList = Arrays.asList(getActivity().getResources().getStringArray(R.array.game_invitation_code));
                        if (invitationCodeList.contains(inputCode)) {
                            mPreferences.edit().putBoolean(Constant.PREF_GAME_INVITATION, true).apply();
                            new IntentFactory()
                                    .action(subLink.getLink())
                                    .checkLogin(mIsLoginIn)
                                    .intent(getContext());
                            dialog.dismiss();
                        } else {
                            textView.setText(getResources().getString(R.string.text_invitation_code_wrong));
                        }
                    }

                }
            }, new CybexDialog.ConfirmationDialogCancelListener() {
                @Override
                public void onCancel(Dialog dialog) {

                }
            }, null, null);
        } else {
            new IntentFactory()
                    .action(subLink.getLink())
                    .checkLogin(mIsLoginIn)
                    .intent(getContext());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateHotWatchlistDate(Event.UpdateHotWatchlists event){
        mAllWatchlistData = event.getAllWatchlists();
        mHotWatchlistData = event.getHotWatchlists();
        mHotAssetPairRecyclerViewAdapter = new HotAssetPairRecyclerViewAdapter(getContext(), mHotWatchlistData, mListener);
        mRvHotPair.setAdapter(mHotAssetPairRecyclerViewAdapter);
        Collections.sort(mAllWatchlistData, new Comparator<WatchlistData>() {
            @Override
            public int compare(WatchlistData o1, WatchlistData o2) {
                return o1.getChange() > o2.getChange() ? -1 : 1;
            }
        });
        mTopGainerRecyclerViewAdapter = new TopGainerRecyclerViewAdapter(getContext(), mAllWatchlistData, mListener);
        mRvTopGainers.setAdapter(mTopGainerRecyclerViewAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
        for (AssetRmbPrice rmbPrice : assetRmbPrices) {
            if(mAllWatchlistData != null && mAllWatchlistData.size() > 0){
                for(WatchlistData watchlistData : mAllWatchlistData){
                    if (!rmbPrice.getName().equals(AssetUtil.parseSymbol(watchlistData.getBaseSymbol()))) {
                        continue;
                    }
                    if (rmbPrice.getName().equals(AssetUtil.parseSymbol(watchlistData.getQuoteSymbol()))) {
                        watchlistData.setQuoteRmbPrice(rmbPrice.getValue());
                    }
                    watchlistData.setRmbPrice(rmbPrice.getValue());
                }
            }
            if(mHotWatchlistData != null && mHotWatchlistData.size() > 0){
                for(WatchlistData watchlistData : mHotWatchlistData){
                    if (!rmbPrice.getName().equals(AssetUtil.parseSymbol(watchlistData.getBaseSymbol()))) {
                        continue;
                    }
                    if (rmbPrice.getName().equals(AssetUtil.parseSymbol(watchlistData.getQuoteSymbol()))) {
                        watchlistData.setQuoteRmbPrice(rmbPrice.getValue());
                    }
                    watchlistData.setRmbPrice(rmbPrice.getValue());
                }
            }
        }
        if (mHotAssetPairRecyclerViewAdapter != null) {
            mHotAssetPairRecyclerViewAdapter.notifyDataSetChanged();
        }
        if (mAllWatchlistData != null) {
            Collections.sort(mAllWatchlistData, new Comparator<WatchlistData>() {
                @Override
                public int compare(WatchlistData o1, WatchlistData o2) {
                    return o1.getChange() > o2.getChange() ? -1 : 1;
                }
            });
        }
        if (mTopGainerRecyclerViewAdapter != null) {
            mTopGainerRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event) {
        mIsLoginIn = false;
        mName = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginIn(Event.LoginIn event){
        mName = event.getName();
        mIsLoginIn = true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

}
