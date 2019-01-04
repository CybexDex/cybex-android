package com.cybex.basemodule.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybex.provider.http.entity.HotAssetPair;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.utils.NetworkUtils;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybex.provider.http.response.AssetsPairResponse;
import com.cybex.provider.http.response.AssetsPairToppingResponse;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.http.response.CnyResponse;
import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybex.provider.graphene.chain.BucketObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.FullAccountObjectReply;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.market.HistoryPrice;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function5;
import io.reactivex.functions.Function6;
import io.reactivex.functions.Function7;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_USDT;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_USDT;
import static com.cybex.provider.utils.NetworkUtils.TYPE_MOBILE;
import static com.cybex.provider.utils.NetworkUtils.TYPE_NOT_CONNECTED;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_ETH;
import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_ORDINARY_MARKET;
import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_REAL_TIME_MARKET;
import static com.cybex.basemodule.constant.Constant.FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI;
import static com.cybex.basemodule.constant.Constant.PREF_LOAD_MODE;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";

    private List<AssetRmbPrice> mAssetRmbPrices;

    private volatile FullAccountObject mFullAccount;

    private Disposable mDisposable;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String mName;
    private int mMode;//网络加载模式
    private int mNetworkState;

    private volatile List<AssetObject> mAssetObjects = new ArrayList<>();
    private List<String> mAssetWhiteList = new ArrayList<>();

    private ConcurrentHashMap<String, List<AssetsPair>> mAssetsPairHashMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<WatchlistData>> mWatchlistHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AccountObject> mAccountHashMap = new ConcurrentHashMap<>();
    private List<FeeAmountObject> mLimitOrderCreateFees = null;
    private List<FeeAmountObject> mLimitOrderCancelFees = null;
    private List<HotAssetPair> mHotAssetPair = null;
    private JsonObject mAssetPairsConfig;
    //当前行情tab页
    private volatile String mCurrentBaseAssetId;

    private boolean mIsWebSocketAvailable;

    private ScheduledExecutorService mScheduled = Executors.newScheduledThreadPool(2);
    private WatchlistWorker mWatchlistWorker;
    private FullAccountWorker mFullAccountWorker;
    private ScheduledFuture mWatchlistFuture;
    private ScheduledFuture mFullAccountFuture;

    public class WebSocketBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(PREF_LOAD_MODE, FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI);
        mName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mNetworkState = NetworkUtils.getConnectivityStatus(this);
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, throwable.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("WebSocketClient", "WebSocketService");
        //连接websocket
        BitsharesWalletWraper.getInstance().build_connect();
        loadAssetsRmbPrice();
        startFullAccountWorkerSchedule();
        loadEvaProjectNames();
        return START_NOT_STICKY;
    }

    private void loadEvaProjectNames() {
        compositeDisposable.add(RetrofitFactory.getInstance().api().getEvaProjectNames()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Consumer<JsonObject>() {
                    @Override
                    public void accept(JsonObject jsonObject) throws Exception {
                        Gson gson = new Gson();
                        Map<String, String> result = gson.fromJson(jsonObject, new TypeToken<Map<String, String>>(){}.getType());
                        AssetPairCache.getInstance().setEvaProjectNames(result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new WebSocketBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbindService");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e( TAG, "OnDestroyService");
        cancelRMBSubscription();
        cancelWatchlistWorkerSchedule();
        cancelFullAccountWorkerSchedule();
        shutdownSchedule();
        BitsharesWalletWraper.getInstance().disconnect();
        EventBus.getDefault().unregister(this);
    }

    //加载行情数据
    public void loadWatchlistData(String baseAssetId) {
        mCurrentBaseAssetId = baseAssetId;
        if(mNetworkState == TYPE_NOT_CONNECTED){
            return;
        }
        List<WatchlistData> watchlistDatas = mWatchlistHashMap.get(baseAssetId);
        if (watchlistDatas != null) {
            EventBus.getDefault().post(new Event.UpdateWatchlists(baseAssetId, watchlistDatas));
            return;
        }
        loadAllAssetsPairData();
    }

    /**
     * 加载热点行情数据
     * @param hotAssetPairs
     */
    public void loadHotWatchlistData(List<HotAssetPair> hotAssetPairs){
        if(hotAssetPairs == null || hotAssetPairs.size() == 0 || mNetworkState == TYPE_NOT_CONNECTED){
            return;
        }
        mHotAssetPair = hotAssetPairs;
        List<WatchlistData> allWatchlistDatas = getAllWatchlistData();
        if(allWatchlistDatas != null){
            List<WatchlistData> hotWatchlistDatas = getHotWatchlistData(hotAssetPairs, allWatchlistDatas);
            EventBus.getDefault().post(new Event.UpdateHotWatchlists(hotWatchlistDatas, allWatchlistDatas));
            return;
        }
        loadAllAssetsPairData();
    }

    private List<WatchlistData> getHotWatchlistData(List<HotAssetPair> hotAssetPairs, List<WatchlistData> allWatchlistDatas){
        if(hotAssetPairs == null || hotAssetPairs.size() == 0 ||
                allWatchlistDatas == null || allWatchlistDatas.size() == 0){
            return null;
        }
        List<WatchlistData> watchlistDatas = new ArrayList<>();
        for(HotAssetPair hotAssetPair : hotAssetPairs){
            for(WatchlistData watchlistData : allWatchlistDatas){
                if(!watchlistData.getQuoteId().equals(hotAssetPair.getQuote()) ||
                        !watchlistData.getBaseId().equals(hotAssetPair.getBase())){
                    continue;
                }
                watchlistDatas.add(watchlistData);
            }
        }
        return watchlistDatas;
    }

    public List<WatchlistData> getAllWatchlistData() {
        if (mWatchlistHashMap.isEmpty()) {
            return null;
        }
        List<WatchlistData> watchlistDatas = new ArrayList<>();
        for (Map.Entry<String, List<WatchlistData>> entry : mWatchlistHashMap.entrySet()) {
            watchlistDatas.addAll(entry.getValue());
        }
        return watchlistDatas;
    }

    public void loadLimitOrderCreateFee(String assetId, int operationId, Operations.base_operation operation){
        if(mLimitOrderCreateFees == null){
            mLimitOrderCreateFees = Collections.synchronizedList(new ArrayList<FeeAmountObject>());
        }
        if(mLimitOrderCreateFees.size() > 0){
            for(FeeAmountObject fee : mLimitOrderCreateFees){
                if(!assetId.equals(fee.asset_id)){
                   continue;
                }
                EventBus.getDefault().post(new Event.LoadRequiredFee(fee));
                return;
            }
        }
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(assetId, operationId, operation, mLimitOrderCreateFeeCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    public void loadLimitOrderCancelFee(String assetId, int operationId, Operations.base_operation operation){
        if(mLimitOrderCancelFees == null){
            mLimitOrderCancelFees = Collections.synchronizedList(new ArrayList<FeeAmountObject>());
        }
        if(mLimitOrderCancelFees.size() > 0){
            for(FeeAmountObject fee : mLimitOrderCancelFees){
                if(!assetId.equals(fee.asset_id)){
                    continue;
                }
                EventBus.getDefault().post(new Event.LoadRequiredCancelFee(fee));
                return;
            }
        }
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(assetId, operationId, operation, mLimitOrderCancelFeesCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadModeChanged(Event.LoadModeChanged event){
        int mode = event.getMode();
        if(mMode == FREQUENCY_MODE_ORDINARY_MARKET && mode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI &&
                mNetworkState == TYPE_MOBILE){
            mMode = event.getMode();
            return;
        }
        if(mMode == FREQUENCY_MODE_REAL_TIME_MARKET && mode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI &&
                mNetworkState == NetworkUtils.TYPE_WIFI){
            mMode = event.getMode();
            return;
        }
        if(mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI && mode == FREQUENCY_MODE_ORDINARY_MARKET &&
                mNetworkState == TYPE_MOBILE){
            mMode = event.getMode();
            return;
        }
        if(mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI && mode == FREQUENCY_MODE_REAL_TIME_MARKET &&
                mNetworkState == NetworkUtils.TYPE_WIFI){
            mMode = event.getMode();
            return;
        }
        mMode = event.getMode();
        //先关闭当前任务
        cancelRMBSubscription();
        cancelFullAccountWorkerSchedule();
        cancelWatchlistWorkerSchedule();
        //重启任务
        startWatchlistWorkerSchedule();
        startFullAccountWorkerSchedule();
        loadAssetsRmbPrice();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogOut(Event.LoginOut event) {
        clearAccountCache();
        cancelFullAccountWorkerSchedule();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(Event.LoginIn event){
        mName = event.getName();
        startFullAccountWorkerSchedule();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkStateChanged(Event.NetWorkStateChanged event){
        if(mNetworkState == event.getState()){
            return;
        }
        if(mMode != FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI){
            mNetworkState = event.getState();
            return;
        }
        mNetworkState = event.getState();
        //先关闭当前任务
        cancelRMBSubscription();
        cancelFullAccountWorkerSchedule();
        cancelWatchlistWorkerSchedule();
        //重启任务
        startFullAccountWorkerSchedule();
        loadAssetsRmbPrice();

        if(mWatchlistHashMap.isEmpty()){
            loadWatchlistData(mCurrentBaseAssetId == null ? ASSET_ID_CYB : mCurrentBaseAssetId);
        } else {
            startWatchlistWorkerSchedule();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void isOnBackground(Event.IsOnBackground isOnBackground) {
        if (isOnBackground.isOnBackground()) {
            Log.e(TAG, "background");
            BitsharesWalletWraper.getInstance().reset();
            cancelWatchlistWorkerSchedule();
            cancelFullAccountWorkerSchedule();
            cancelRMBSubscription();
        } else {
            Log.e(TAG, "foreground");
            BitsharesWalletWraper.getInstance().build_connect();
            startWatchlistWorkerSchedule();
            startFullAccountWorkerSchedule();
            loadAssetsRmbPrice();
        }
    }

    /**
     * 加载置顶交易对
     * 加载行情交易对
     * 加载交易对配置信息
     * 加载币种白名单
     */
    private void loadAllAssetsPairData(){
        Observable.zip(loadToppingAssetsPair(), loadAssetsPairData(ASSET_ID_ETH), loadAssetsPairData(ASSET_ID_CYB),
                loadAssetsPairData(ASSET_ID_USDT), loadAssetsPairData(ASSET_ID_BTC), loadPairsConfig(), loadAssetWhiteList(),
                new Function7<List<AssetsPairToppingResponse>, Map<String,List<AssetsPair>>,
                                                        Map<String,List<AssetsPair>>, Map<String,List<AssetsPair>>,
                                                        Map<String,List<AssetsPair>>, JsonObject,
                                                        List<String>, Map<String,List<AssetsPair>>>() {
                    @Override
                    public Map<String,List<AssetsPair>> apply(List<AssetsPairToppingResponse> assetsPairToppingResponses,
                                                              Map<String, List<AssetsPair>> assetsPairs1,
                                                              Map<String, List<AssetsPair>> assetsPairs2,
                                                              Map<String, List<AssetsPair>> assetsPairs3,
                                                              Map<String, List<AssetsPair>> assetsPairs4,
                                                              JsonObject assetPairsConfig,
                                                              List<String> assetWhites) {
                        mAssetWhiteList.addAll(assetWhites);
                        mAssetPairsConfig = assetPairsConfig;
                        assetsPairs1.putAll(assetsPairs2);
                        assetsPairs1.putAll(assetsPairs3);
                        assetsPairs1.putAll(assetsPairs4);
                        if(assetsPairToppingResponses != null && assetsPairToppingResponses.size() > 0){
                            for(AssetsPairToppingResponse toppingResponse : assetsPairToppingResponses){
                                List<String> quotes = toppingResponse.getQuotes();
                                List<AssetsPair> assetsPairs = assetsPairs1.get(toppingResponse.getBase());
                                if(assetsPairs == null){
                                    continue;
                                }
                                for(int i=0; i<quotes.size(); i++){
                                    for(AssetsPair assetsPair : assetsPairs){
                                        if(quotes.get(i).equals(assetsPair.getQuote())){
                                            assetsPair.setOrder(quotes.size() - i);
                                        }
                                    }
                                }
                            }
                        }
                        return assetsPairs1;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map<String,List<AssetsPair>>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Map<String,List<AssetsPair>> assetsPairMap) {
                    mAssetsPairHashMap.putAll(assetsPairMap);
                    Set<String> assetsIds = new HashSet<>();
                    for (Map.Entry<String, List<AssetsPair>> entry : mAssetsPairHashMap.entrySet()){
                        List<AssetsPair> assetsPairs = entry.getValue();
                        for(AssetsPair assetsPair : assetsPairs){
                            assetsIds.add(assetsPair.getBase());
                            assetsIds.add(assetsPair.getQuote());
                        }
                        assetsIds.addAll(mAssetWhiteList);
                    }
                    loadAllAssetObjectData(assetsIds);

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
        });
    }

    private Observable<JsonObject> loadPairsConfig() {
         return RetrofitFactory.getInstance()
                .api()
                .getAssetPairsConfig();
    }

    //加载交易对数据
    private Observable<Map<String, List<AssetsPair>>> loadAssetsPairData(final String baseAsset) {
        return RetrofitFactory.getInstance()
                .api()
                .getAssetsPair(baseAsset)
                .map(new Function<AssetsPairResponse, Map<String, List<AssetsPair>>>() {
                    @Override
                    public Map<String, List<AssetsPair>> apply(AssetsPairResponse assetsPairResponse) {
                        Map<String, List<AssetsPair>> assetsPairMap = new HashMap<>();
                        List<AssetsPair> assetsPairs = new ArrayList<>();
                        if (assetsPairResponse.getData() != null && assetsPairResponse.getData().size() > 0) {
                            for (String quote : assetsPairResponse.getData()) {
                                if(!TextUtils.isEmpty(quote)){
                                    assetsPairs.add(new AssetsPair(baseAsset, quote));
                                }
                            }
                        }
                        assetsPairMap.put(baseAsset, assetsPairs);
                        return assetsPairMap;
                    }
                });
    }

    //加载置顶交易对
    private Observable<List<AssetsPairToppingResponse>> loadToppingAssetsPair() {
        return RetrofitFactory.getInstance()
                .api()
                .getAssetsPairTopping();
    }

    /**
     * 加载币信息
     *
     * @param assetsIds 币种id
     */
    private void loadAllAssetObjectData(Set<String> assetsIds){
        try {
            BitsharesWalletWraper.getInstance().get_objects(assetsIds, mAssetMultiCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }



    /**
     * 加载币信息
     *
     * @param assetId 币id
     */
    private void loadAssetObjectData(String assetId) {
        try {
            BitsharesWalletWraper.getInstance().get_objects(assetId, mAssetOneCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    //加载指定交易对的价格
    private void loadMarketTicker(List<AssetsPair> assetsPairs) {
        for (AssetsPair assetsPair : assetsPairs) {
            if (assetsPair.getBaseAsset() != null && assetsPair.getQuoteAsset() != null) {
                loadMarketTicker(assetsPair.getBase(), assetsPair.getQuote());
            }
        }
    }

    //加载所有交易对的价格
    private void loadMarketTickers() {
        for(Map.Entry<String, List<AssetsPair>> entry : mAssetsPairHashMap.entrySet()){
            for (AssetsPair assetsPair : entry.getValue()) {
                if (assetsPair.getBaseAsset() != null && assetsPair.getQuoteAsset() != null) {
                    loadMarketTicker(assetsPair.getBase(), assetsPair.getQuote());
                }
            }
        }
    }

    private void shutdownSchedule(){
        mScheduled.shutdownNow();
    }

    private void startWatchlistWorkerSchedule(){
        if(mNetworkState == TYPE_NOT_CONNECTED){
            return;
        }
        if(mWatchlistFuture != null && !mWatchlistFuture.isCancelled()){
            mWatchlistFuture.cancel(true);
        }
        if (mScheduled.isShutdown() || mScheduled.isTerminated()) {
            mScheduled = Executors.newScheduledThreadPool(2);
        }
        if(mWatchlistWorker == null){
            mWatchlistWorker = new WatchlistWorker();
        }
        mWatchlistFuture = mScheduled.scheduleAtFixedRate(mWatchlistWorker, 0,
                mMode == FREQUENCY_MODE_ORDINARY_MARKET ||
                    (mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI &&
                        mNetworkState == TYPE_MOBILE) ? 6 : 3, TimeUnit.SECONDS);
    }

    private void cancelWatchlistWorkerSchedule(){
        if(mWatchlistFuture == null){
            return;
        }
        if(mWatchlistFuture.isCancelled()){
            return;
        }
        mWatchlistFuture.cancel(true);
        mWatchlistFuture = null;
    }

    private void startFullAccountWorkerSchedule(){
        if(mNetworkState == TYPE_NOT_CONNECTED){
            return;
        }
        if(TextUtils.isEmpty(mName)){
            return;
        }
        if(mFullAccountFuture != null && !mFullAccountFuture.isCancelled()){
            mFullAccountFuture.cancel(true);
        }
        if (mScheduled.isShutdown() || mScheduled.isTerminated()) {
            mScheduled = Executors.newScheduledThreadPool(2);
        }
        if(mFullAccountWorker == null){
            mFullAccountWorker = new FullAccountWorker();
        }
        mFullAccountFuture = mScheduled.scheduleAtFixedRate(mFullAccountWorker, 0,
                mMode == FREQUENCY_MODE_ORDINARY_MARKET ||
                    (mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI &&
                        mNetworkState == TYPE_MOBILE) ? 6 : 3, TimeUnit.SECONDS);
    }

    private void cancelFullAccountWorkerSchedule(){
        if(mFullAccountFuture == null){
            return;
        }
        if(mFullAccountFuture.isCancelled()){
            return;
        }
        mFullAccountFuture.cancel(true);
        mFullAccountFuture = null;
    }

    private void loadMarketTicker(String base, String quote) {
        try {
            BitsharesWalletWraper.getInstance().get_ticker(base, quote, mMarketTickerCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    public void loadAccountHistory(ObjectId<AccountObject> accountObjectId, int nLimit){
        try {
            BitsharesWalletWraper.getInstance().get_account_history(accountObjectId, nLimit, mAccountHistoryCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    public void loadBlock(int callId, int blockNumber){
        try {
            BitsharesWalletWraper.getInstance().get_block(callId, blockNumber, mBlockCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    public void loadAccountObject(String accountId){
        if(mAccountHashMap.containsKey(accountId)){
            EventBus.getDefault().post(new Event.LoadAccountObject(mAccountHashMap.get(accountId)));
            return;
        }
        List<String> accountIds = new ArrayList<>();
        accountIds.add(accountId);
        try {
            BitsharesWalletWraper.getInstance().get_accounts(accountIds, mAccountObjectCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private MessageCallback mLimitOrderCreateFeeCallback = new MessageCallback<Reply<List<FeeAmountObject>>>() {
        @Override
        public void onMessage(Reply<List<FeeAmountObject>> reply) {
            List<FeeAmountObject> feeAmountObjects = reply.result;
            if(feeAmountObjects == null || feeAmountObjects.size() == 0 || feeAmountObjects.get(0) == null){
                return;
            }
            mLimitOrderCreateFees.add(feeAmountObjects.get(0));
            EventBus.getDefault().post(new Event.LoadRequiredFee(feeAmountObjects.get(0)));
        }

        @Override
        public void onFailure() {

        }
    };

    private MessageCallback mLimitOrderCancelFeesCallback = new MessageCallback<Reply<List<FeeAmountObject>>>() {
        @Override
        public void onMessage(Reply<List<FeeAmountObject>> reply) {
            List<FeeAmountObject> feeAmountObjects = reply.result;
            if(feeAmountObjects == null || feeAmountObjects.size() == 0 || feeAmountObjects.get(0) == null){
                return;
            }
            mLimitOrderCancelFees.add(feeAmountObjects.get(0));
            EventBus.getDefault().post(new Event.LoadRequiredCancelFee(feeAmountObjects.get(0)));
        }

        @Override
        public void onFailure() {

        }
    };

    private MessageCallback mBlockCallback = new MessageCallback<Reply<BlockHeader>>() {
        @Override
        public void onMessage(Reply<BlockHeader> reply) {
            BlockHeader block = reply.result;
            EventBus.getDefault().post(new Event.LoadBlock(Integer.parseInt(reply.id), block));
        }

        @Override
        public void onFailure() {

        }
    };

    private MessageCallback mAccountHistoryCallback = new MessageCallback<Reply<List<AccountHistoryObject>>>() {

        @Override
        public void onMessage(Reply<List<AccountHistoryObject>> reply) {
            List<AccountHistoryObject> accountHistoryObjects = reply.result;
            if(accountHistoryObjects == null || accountHistoryObjects.size() == 0){
                return;
            }
            EventBus.getDefault().post(new Event.LoadAccountHistory(accountHistoryObjects));
        }

        @Override
        public void onFailure() {

        }
    };

    //get market ticker callback
    private MessageCallback mMarketTickerCallback = new MessageCallback<Reply<MarketTicker>>() {

        @Override
        public void onMessage(Reply<MarketTicker> reply) {
            MarketTicker marketTicker = reply.result;
            if (marketTicker == null) {
                return;
            }
            WatchlistData watchlistData = null;
            for (WatchlistData watchlist : mWatchlistHashMap.get(marketTicker.base)) {
                if (watchlist.getQuoteId().equals(marketTicker.quote)) {
                    watchlistData = watchlist;
                    break;
                }
            }
            if (watchlistData != null) {
                watchlistData.setMarketTicker(marketTicker);
                //EventBus.getDefault().post(new Event.UpdateWatchlist(watchlistData));
            }
        }

        @Override
        public void onFailure() {

        }
    };

    private MessageCallback mAccountObjectCallback = new MessageCallback<Reply<List<AccountObject>>>() {

        @Override
        public void onMessage(Reply<List<AccountObject>> reply) {
            AccountObject accountObject = reply.result.get(0);
            mAccountHashMap.put(accountObject.id.toString(), accountObject);
            EventBus.getDefault().post(new Event.LoadAccountObject(accountObject));
        }

        @Override
        public void onFailure() {

        }
    };


    private WatchlistData getWatchlist(Map<String, List<WatchlistData>> map, BucketObject bucket) {
        List<WatchlistData> baseWatchlistDatas = map.get(bucket.key.base.toString());
        List<WatchlistData> quoteWatchlistDatas = map.get(bucket.key.quote.toString());

        if (baseWatchlistDatas != null) {
            for (WatchlistData watchlist : baseWatchlistDatas) {
                if (watchlist.getQuoteId().equals(bucket.key.quote.toString())) {
                    return watchlist;
                }
            }

            for (WatchlistData watchlist : quoteWatchlistDatas) {
                if (watchlist.getQuoteId().equals(bucket.key.base.toString())) {
                    return watchlist;
                }
            }

        } else {
            for (WatchlistData watchlist : quoteWatchlistDatas) {
                if (watchlist.getQuoteId().equals(bucket.key.base.toString())) {
                    return watchlist;
                }
            }

            for (WatchlistData watchlist : baseWatchlistDatas) {
                if (watchlist.getQuoteId().equals(bucket.key.quote.toString())) {
                    return watchlist;
                }
            }
        }
        return null;
    }

    //get asset object callback
    private MessageCallback mAssetMultiCallback = new MessageCallback<Reply<List<AssetObject>>>() {
        @Override
        public void onMessage(Reply<List<AssetObject>> reply) {
            List<AssetObject> assetObjects = reply.result;
            if (assetObjects == null || assetObjects.size() == 0) {
                return;
            }
            EventBus.getDefault().post(new Event.LoadAssets(assetObjects));
            mAssetObjects.addAll(assetObjects);
            //币信息对应交易对
            for(Map.Entry<String, List<AssetsPair>> entry : mAssetsPairHashMap.entrySet()){
                List<AssetsPair> assetsPairs = entry.getValue();
                if(assetsPairs == null || assetsPairs.size() == 0){
                    continue;
                }
                for (AssetObject assetObject : assetObjects) {
                    for (AssetsPair assetsPair : assetsPairs) {
                        if (assetsPair.getBase().equals(assetObject.id.toString())) {
                            assetsPair.setBaseAsset(assetObject);
                        }
                        if (assetsPair.getQuote().equals(assetObject.id.toString())) {
                            assetsPair.setQuoteAsset(assetObject);
                        }
                    }
                }
            }
            Gson gson = new GsonBuilder().create();
            //创建交易对数据
            for(Map.Entry<String, List<AssetsPair>> entry : mAssetsPairHashMap.entrySet()){
                List<AssetsPair> assetsPairs = entry.getValue();
                if(assetsPairs == null || assetsPairs.size() == 0){
                    continue;
                }
                List<WatchlistData> watchlistData = new ArrayList<>();
                for (AssetsPair assetsPair : assetsPairs) {
                    JsonElement jsonElement = mAssetPairsConfig.get(AssetUtil.parseSymbol(assetsPair.getBaseAsset().symbol));
                    if(jsonElement != null) {
                        JsonElement element = jsonElement.getAsJsonObject().get(AssetUtil.parseSymbol(assetsPair.getQuoteAsset().symbol));
                        if(element != null) {
                            assetsPair.setConfig(gson.fromJson(element.getAsJsonObject().get("book").getAsJsonObject(), AssetsPair.Config.class));
                        }
                    }
                    WatchlistData watchlist = new WatchlistData(assetsPair.getBaseAsset(), assetsPair.getQuoteAsset());
                    AtomicInteger id = BitsharesWalletWraper.getInstance().get_call_id();
                    watchlist.setSubscribeId(id.getAndIncrement());
                    watchlist.setOrder(assetsPair.getOrder());
                    watchlist.setAssetPairConfig(assetsPair.getConfig());
                    watchlistData.add(watchlist);
                }
                mWatchlistHashMap.put(entry.getKey(), watchlistData);
            }
            //缓存交易对
            AssetPairCache.getInstance().setAssetPairCache(mAssetsPairHashMap);
            if(!TextUtils.isEmpty(mCurrentBaseAssetId)){
                //更新行情
                List<WatchlistData> watchlistData = mWatchlistHashMap.get(mCurrentBaseAssetId);
                EventBus.getDefault().post(new Event.UpdateWatchlists(mCurrentBaseAssetId, watchlistData));
            }
            //更新热点行情
            List<WatchlistData> allWatchlistDatas = getAllWatchlistData();
            List<WatchlistData> hotWatchlistDatas = getHotWatchlistData(mHotAssetPair, allWatchlistDatas);
            EventBus.getDefault().post(new Event.UpdateHotWatchlists(hotWatchlistDatas, allWatchlistDatas));
            //开启周期性任务加载所有Tab下的所有交易对数据
            startWatchlistWorkerSchedule();
        }

        @Override
        public void onFailure() {

        }
    };

    //get asset object callback
    private MessageCallback mAssetOneCallback = new MessageCallback<Reply<List<AssetObject>>>() {
        @Override
        public void onMessage(Reply<List<AssetObject>> reply) {
            AssetObject assetObject = reply.result.get(0);
            mAssetObjects.add(assetObject);
            EventBus.getDefault().post(new Event.LoadAsset(assetObject));
        }

        @Override
        public void onFailure() {

        }
    };

    //get full account callback
    private MessageCallback mFullAccountCallback = new MessageCallback<Reply<List<FullAccountObjectReply>>>() {
        @Override
        public void onMessage(Reply<List<FullAccountObjectReply>> reply) {
            List<FullAccountObjectReply> fullAccountObjectReplies = reply.result;
            if (fullAccountObjectReplies == null || fullAccountObjectReplies.size() == 0) {
                return;
            }
            mFullAccount = fullAccountObjectReplies.get(0).fullAccountObject;
            EventBus.getDefault().post(new Event.UpdateFullAccount(mFullAccount));
        }

        @Override
        public void onFailure() {

        }
    };

    public void loadFullAccount(String name) {
        List<String> names = new ArrayList<>();
        names.add(name);
        try {
            BitsharesWalletWraper.getInstance().get_full_accounts(names, true, mFullAccountCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    public FullAccountObject getFullAccount(String name) {
        mName = name;
        if (mFullAccount != null) {
            return mFullAccount;
        }
        if (!TextUtils.isEmpty(name) && mAssetsPairHashMap != null) {
            startFullAccountWorkerSchedule();
        }
        return null;
    }

    public void cancelRMBSubscription() {
        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
            mDisposable = null;
        }
    }

    public Observable<List<String>> loadAssetWhiteList() {
        return RetrofitFactory.getInstance()
                .api()
                .getAssetWhiteList();
    }

    public void loadAssetsRmbPrice() {
        if(mNetworkState == TYPE_NOT_CONNECTED){
            return;
        }
        //防止多次执行
        if (mDisposable != null) {
            return;
        }
        /**
         * fix bug:CYM-446
         * 人民币价格请求失败立马重新请求
         */
        mDisposable = Flowable.interval(0,
                mMode == FREQUENCY_MODE_ORDINARY_MARKET ||
                    (mMode == FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI &&
                        mNetworkState == TYPE_MOBILE) ? 6 : 3, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<CnyResponse>>() {
                    @Override
                    public Publisher<CnyResponse> apply(Long aLong) {
                        return RetrofitFactory.getInstance().api().getCny();
                    }
                })
                .map(new Function<CnyResponse, List<AssetRmbPrice>>() {
                    @Override
                    public List<AssetRmbPrice> apply(CnyResponse cnyResponse) {
                        return cnyResponse.getPrices() != null ? cnyResponse.getPrices() : new ArrayList<AssetRmbPrice>();
                    }
                })
                .retry()
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<AssetRmbPrice>>() {
                    @Override
                    public void accept(List<AssetRmbPrice> assetRmbPrices) throws Exception {
                        Log.v(TAG, "getAssetsRmbPrice: success");
                        mAssetRmbPrices = assetRmbPrices;
                        EventBus.getDefault().post(new Event.UpdateRmbPrice(assetRmbPrices));
                        //初始化交易界面 默认交易对CYB/ETH
                        WatchlistData watchlistData = getWatchlist(ASSET_ID_ETH, ASSET_ID_CYB);
                        if (watchlistData != null) {
                            EventBus.getDefault().post(new Event.InitExchangeWatchlist(watchlistData));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.v(TAG, "getAssetsRmbPrice:" + throwable.getMessage());
                        mDisposable.dispose();
                        mDisposable = null;
                        loadAssetsRmbPrice();
                    }
                });
    }

    public List<String> getAssetWhiteList() {
        return mAssetWhiteList;
    }

    public List<AssetObject> getAssetObjectsList() {
        return mAssetObjects;
    }

    public List<AssetRmbPrice> getAssetRmbPrices() {
        return mAssetRmbPrices;
    }

    public AssetRmbPrice getAssetRmbPrice(String assetName) {
        if (mAssetRmbPrices == null || mAssetRmbPrices.size() == 0) {
            return null;
        }
        for (AssetRmbPrice assetRmbPrice : mAssetRmbPrices) {
            if (assetName.equals(assetRmbPrice.getName())) {
                return assetRmbPrice;
            }
        }
        return null;
    }

    public AssetObject getAssetObject(String assetId) {
        for (AssetObject assetObject : mAssetObjects) {
            if (assetObject.id.toString().equals(assetId)) {
                return assetObject;
            }
        }
        loadAssetObjectData(assetId);
        return null;
    }

    public AssetObject getAssetObjectBySymbol(String assetSymbol) {
        for (AssetObject assetObject : mAssetObjects) {
            if (assetObject.symbol.equals(assetSymbol)) {
                return assetObject;
            }
        }
        loadAssetObjectData(assetSymbol);
        return null;
    }

    public List<AssetObject> getAssetObjects(String baseAssetId, String quoteAssetId) {
        List<AssetObject> assetObjects = new ArrayList<>();
        AssetObject baseAssetObject = null;
        AssetObject quoteAssetObject = null;
        for (AssetObject assetObject : mAssetObjects) {
            if (assetObject.id.toString().equals(baseAssetId)) {
                baseAssetObject = assetObject;
            }
            if (assetObject.id.toString().equals(quoteAssetId)) {
                quoteAssetObject = assetObject;
            }
        }
        if (baseAssetObject != null && quoteAssetObject != null) {
            assetObjects.add(baseAssetObject);
            assetObjects.add(quoteAssetObject);
            return assetObjects;
        }
        return null;
    }

    /**
     * 清除用户缓存数据
     */
    public void clearAccountCache() {
        mFullAccount = null;
        mName = null;
    }

    /**
     * 获取交易对WatchlistData
     * @param baseId
     * @param quoteId
     * @return
     */
    public WatchlistData getWatchlist(String baseId, String quoteId){
        if(mWatchlistHashMap == null || mWatchlistHashMap.isEmpty()){
            return null;
        }
        List<WatchlistData> watchlists = mWatchlistHashMap.get(baseId);
        if(watchlists == null || watchlists.size() == 0){
            return null;
        }
        for(WatchlistData watchlist : watchlists){
            AssetObject quoteAsset = watchlist.getQuoteAsset();
            if(quoteAsset != null && quoteAsset.id.toString().equals(quoteId)){
                return watchlist;
            }
        }
        return null;
    }

    /**
     * 获取所有交易对信息
     */
    public Map<String, List<AssetsPair>> getAssetPairHashMap(){
        return mAssetsPairHashMap;
    }

    public void setRMBPriceForQuoteAssetAsBaseInOtherTab(WatchlistData watchlistData) {
        switch (watchlistData.getQuoteId()) {
            case ASSET_ID_ETH:
                watchlistData.setQuoteRmbPrice(getAssetRmbPrice(ASSET_SYMBOL_ETH).getValue());
                break;
            case ASSET_ID_BTC:
                watchlistData.setQuoteRmbPrice(getAssetRmbPrice(ASSET_SYMBOL_BTC).getValue());
                break;
            case ASSET_ID_CYB:
                watchlistData.setQuoteRmbPrice(getAssetRmbPrice(ASSET_SYMBOL_CYB).getValue());
                break;
            case ASSET_ID_USDT:
                watchlistData.setQuoteRmbPrice(getAssetRmbPrice(ASSET_SYMBOL_USDT).getValue());
                break;
            default:
                watchlistData.setQuoteRmbPrice(0);
        }
    }

    private class WatchlistWorker implements Runnable {

        @Override
        public void run() {
            loadMarketTickers();
        }
    }

    private class FullAccountWorker implements Runnable {

        @Override
        public void run() {
            loadFullAccount(mName);
        }
    }

}
