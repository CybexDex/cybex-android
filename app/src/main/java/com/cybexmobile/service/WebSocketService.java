package com.cybexmobile.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.data.AssetsPairResponse;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.faucet.AssetsPair;
import com.cybexmobile.faucet.CnyResponse;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.FullAccountObjectReply;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.market.HistoryPrice;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.utils.PriceUtil;

import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";

    private List<AssetRmbPrice> mAssetRmbPrices;

    private volatile FullAccountObject mFullAccount;

    private Subscription mSubscription;

    private List<String> mNames = new ArrayList<>();

    private List<AssetObject> mAssetObjects = new ArrayList<>();

    private ConcurrentHashMap<String, List<AssetsPair>> mAssetsPairHashMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<WatchlistData>> mWatchlistHashMap = new ConcurrentHashMap<>();

    private Timer mTimer;

    private boolean mIsWebSocketAvailable;

    public class WebSocketBinder extends Binder{
        public WebSocketService getService(){
            return WebSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("WebSocketClient", "WebSocketService");
        //连接websocket
        BitsharesWalletWraper.getInstance().build_connect();
        //每10秒获取一次币Rmb价格
        getAssetsRmbPrice();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new WebSocketBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscription.cancel();
        mSubscription = null;
        mTimer.cancel();
        mTimer = null;
    }

    //加载行情数据
    public void loadWatchlistData(String baseAssetId){
        List<WatchlistData> watchlistDatas = mWatchlistHashMap.get(baseAssetId);
        if(watchlistDatas != null){
            EventBus.getDefault().post(new Event.UpdateWatchlists(watchlistDatas));
            return;
        }
        List<AssetsPair> assetsPairs = mAssetsPairHashMap.get(baseAssetId);
        if(assetsPairs != null && assetsPairs.size() > 0){
            loadHistoryPriceAndMarketTicker(assetsPairs);
            return;
        }
        loadAssetsPairData(baseAssetId);
    }

    public void subscribeAfterNetworkAvailable() {
        Iterator<Map.Entry<String, List<WatchlistData>>> entries = mWatchlistHashMap.entrySet().iterator();
        while (entries.hasNext()){
            List<WatchlistData> watchlists = entries.next().getValue();
            for (WatchlistData watchlistData : watchlists) {
                /**
                 * fix bug:CYM-249
                 * 重新订阅保持callId不变
                 */
                if(watchlistData.getSubscribeId() == 0){
                    AtomicInteger id = BitsharesWalletWraper.getInstance().get_call_id();
                    watchlistData.setSubscribeId(id.getAndIncrement());
                }
                subscribeToMarket(String.valueOf(watchlistData.getSubscribeId()), watchlistData.getBaseId(), watchlistData.getQuoteId());
            }
        }
    }

    //加载交易对数据
    private void loadAssetsPairData(String baseAsset){
        RetrofitFactory.getInstance()
                .api()
                .getAssetsPair(baseAsset)
                .map(new Function<AssetsPairResponse, List<AssetsPair>>() {
                    @Override
                    public List<AssetsPair> apply(AssetsPairResponse assetsPairResponse) {
                        List<AssetsPair> assetsPairs = new ArrayList<>();
                        if(assetsPairResponse.getData() != null && assetsPairResponse.getData().size() > 0){
                            for(String quote : assetsPairResponse.getData()){
                                assetsPairs.add(new AssetsPair(baseAsset, quote));
                            }
                        }
                        return assetsPairs;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AssetsPair>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<AssetsPair> assetsPairs) {
                        mAssetsPairHashMap.put(baseAsset, assetsPairs);
                        loadAssetObjectDatas(assetsPairs);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /**
     * 加载币信息
     * @param assetsPairs 交易对
     */
    private void loadAssetObjectDatas(List<AssetsPair> assetsPairs){
        List<String> assetsIds = new ArrayList<>();
        for(AssetsPair assetsPair : assetsPairs){
            if(!assetsIds.contains(assetsPair.getBase())){
                assetsIds.add(assetsPair.getBase());
            }
            assetsIds.add(assetsPair.getQuote());
        }
        try {
            BitsharesWalletWraper.getInstance().get_objects(assetsIds, mAssetMultiCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载币信息
     * @param baseAssetId
     * @param quoteAssetId
     */
    private void loadAssetObjectDatas(String  baseAssetId, String quoteAssetId){
        List<String> assetIds = new ArrayList<>();
        assetIds.add(baseAssetId);
        assetIds.add(quoteAssetId);
        try {
            BitsharesWalletWraper.getInstance().get_objects(assetIds, mAssetMultiCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载币信息
     * @param assetId 币id
     */
    private void loadAssetObjectData(String assetId){
        try {
            BitsharesWalletWraper.getInstance().get_objects(assetId, mAssetOneCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    //加载价格和交易历史
    private void loadHistoryPriceAndMarketTicker(List<AssetsPair> assetsPairs){
        for(AssetsPair assetsPair : assetsPairs){
            if(assetsPair.getBaseAsset() != null && assetsPair.getQuoteAsset() != null){
                loadMarketTicker(assetsPair.getBase(), assetsPair.getQuote());
                Date startDate = new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
                Date endDate = new Date(System.currentTimeMillis());
                loadHistoryPrice(assetsPair.getBaseAsset(), assetsPair.getQuoteAsset(), startDate, endDate);
            }
        }
    }

    public void updateHistoryPriceAndMarketTicker(AssetObject baseAsset, AssetObject quoteAsset) {
        loadMarketTicker(baseAsset.id.toString(), quoteAsset.id.toString());
        Date startDate = new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
        Date endDate = new Date(System.currentTimeMillis());
        loadHistoryPrice(baseAsset, quoteAsset, startDate, endDate);
    }

    private void loadHistoryPrice(AssetObject baseAsset, AssetObject quoteAsset, Date startDate, Date endDate){
        try {
            BitsharesWalletWraper.getInstance().get_market_history(baseAsset.id, quoteAsset.id, 3600, startDate, endDate, mHistoryPriceCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void loadMarketTicker(String base, String quote){
        try {
            BitsharesWalletWraper.getInstance().get_ticker(base, quote, mMarketTickerCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToMarket(String id, String base, String quote) {
        try {
            BitsharesWalletWraper.getInstance().subscribe_to_market(id ,base, quote, mSubscribeCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void loadPreHistoryPrice(ObjectId<AssetObject> baseAssetId, ObjectId<AssetObject> quoteAssetId, Date startDate, Date endDate){
        try {
            BitsharesWalletWraper.getInstance().get_market_history(baseAssetId, quoteAssetId, 3600, startDate, endDate, mPreHistoryPriceCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private WebSocketClient.MessageCallback mPreHistoryPriceCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<BucketObject>>>() {

        @Override
        public void onMessage(WebSocketClient.Reply<List<BucketObject>> reply) {
            List<BucketObject> buckets = reply.result;
            if(buckets == null || buckets.size() == 0){
                return;
            }
            BucketObject bucket = buckets.get(buckets.size() -1);
            WatchlistData watchlistData = getWatchlist(mWatchlistHashMap, buckets.get(0));
            if(watchlistData != null){
                watchlistData.addHistoryPrice(0, PriceUtil.priceFromBucket(watchlistData.getBaseAsset(), watchlistData.getQuoteAsset(), bucket));
            }
            EventBus.getDefault().post(new Event.UpdateWatchlist(watchlistData));
        }

        @Override
        public void onFailure() {

        }
    };

    //get market ticker callback
    private WebSocketClient.MessageCallback mMarketTickerCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>>() {

        @Override
        public void onMessage(WebSocketClient.Reply<MarketTicker> reply) {
            MarketTicker marketTicker = reply.result;
            if(marketTicker == null){
                return;
            }
            WatchlistData watchlistData = null;
            for(WatchlistData watchlist : mWatchlistHashMap.get(marketTicker.base)){
                if(watchlist.getQuoteId().equals(marketTicker.quote)){
                    watchlistData = watchlist;
                    break;
                }
            }
            if(watchlistData != null){
                watchlistData.setMarketTicker(marketTicker);
                EventBus.getDefault().post(new Event.UpdateWatchlist(watchlistData));
            }
        }

        @Override
        public void onFailure() {

        }
    };

    private WebSocketClient.MessageCallback mSubscribeCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<String>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<String> reply) {
            String id = reply.id;

        }

        @Override
        public void onFailure() {

        }
    };

    //get history price callback
    private WebSocketClient.MessageCallback mHistoryPriceCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<BucketObject>>>() {

        @Override
        public void onMessage(WebSocketClient.Reply<List<BucketObject>> reply) {
            List<BucketObject> buckets = reply.result;
            if(buckets == null || buckets.size() == 0){
                return;
            }
            if(buckets.get(0).key.open.getTime() > (System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS )){
                loadPreHistoryPrice(buckets.get(0).key.base, buckets.get(0).key.quote,
                        new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS - DateUtils.DAY_IN_MILLIS),
                        new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS));
            }
            WatchlistData watchlistData = getWatchlist(mWatchlistHashMap, buckets.get(0));
            if(watchlistData != null){
                List<HistoryPrice> historyPrices = new ArrayList<>();
                for(BucketObject bucket : buckets){
                    historyPrices.add(PriceUtil.priceFromBucket(watchlistData.getBaseAsset(), watchlistData.getQuoteAsset(), bucket));
                }
                watchlistData.setHistoryPrices(historyPrices);
            }
            EventBus.getDefault().post(new Event.UpdateWatchlist(watchlistData));
        }

        @Override
        public void onFailure() {

        }
    };

    private WatchlistData getWatchlist(Map<String, List<WatchlistData>> map, BucketObject bucket){
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
    private WebSocketClient.MessageCallback mAssetMultiCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AssetObject>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<AssetObject>> reply) {
            List<AssetObject> assetObjects = reply.result;
            if(assetObjects == null || assetObjects.size() == 0){
                return;
            }
            EventBus.getDefault().post(new Event.LoadAssets(assetObjects));
            mAssetObjects.addAll(assetObjects);
            if(assetObjects == null || assetObjects.size() == 0){
                return;
            }
            String baseAssetId = assetObjects.get(0).id.toString();
            List<AssetsPair> assetsPairs = mAssetsPairHashMap.get(baseAssetId);
            //币信息对应交易对
            for(AssetObject assetObject : assetObjects){
                for(AssetsPair assetsPair : assetsPairs){
                    if(assetsPair.getBase().equals(assetObject.id.toString())){
                        assetsPair.setBaseAsset(assetObject);
                    }
                    if(assetsPair.getQuote().equals(assetObject.id.toString())){
                        assetsPair.setQuoteAsset(assetObject);
                    }
                }
            }
            //创建交易对数据
            List<WatchlistData> watchlistData = new ArrayList<>();
            for(AssetsPair assetsPair : assetsPairs){
                watchlistData.add(new WatchlistData(assetsPair.getBaseAsset(), assetsPair.getQuoteAsset()));
            }
            mWatchlistHashMap.put(baseAssetId, watchlistData);
            //更新行情
            EventBus.getDefault().post(new Event.UpdateWatchlists(watchlistData));
            for (WatchlistData watchlistItem : watchlistData) {
                if(watchlistItem.getSubscribeId() == 0){
                    AtomicInteger id = BitsharesWalletWraper.getInstance().get_call_id();
                    watchlistItem.setSubscribeId(id.getAndIncrement());
                }
                subscribeToMarket(String.valueOf(watchlistItem.getSubscribeId()), watchlistItem.getBaseId(), watchlistItem.getQuoteId());
            }
            //加载价格和交易历史
            loadHistoryPriceAndMarketTicker(assetsPairs);


        }

        @Override
        public void onFailure() {

        }
    };

    //get asset object callback
    private WebSocketClient.MessageCallback mAssetOneCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AssetObject>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<AssetObject>> reply) {
            AssetObject assetObject = reply.result.get(0);
            mAssetObjects.add(assetObject);
            EventBus.getDefault().post(new Event.LoadAsset(assetObject));
        }

        @Override
        public void onFailure() {

        }
    };

    //get full account callback
    private WebSocketClient.MessageCallback mFullAccountCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FullAccountObjectReply>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<FullAccountObjectReply>> reply) {
            List<FullAccountObjectReply> fullAccountObjectReplies = reply.result;
            if(fullAccountObjectReplies == null || fullAccountObjectReplies.size() == 0){
                return;
            }
            mFullAccount = fullAccountObjectReplies.get(0).fullAccountObject;
            EventBus.getDefault().post(new Event.UpdateFullAccount(mFullAccount));
        }

        @Override
        public void onFailure() {

        }
    };

    public void callFullAccount(String name){
        //防止多次执行TimeTask
        if(mNames.size() != 0){
            return;
        }
        mNames.add(name);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    BitsharesWalletWraper.getInstance().get_full_accounts(mNames, true, mFullAccountCallback);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
        },  0, 3600*1000);
    }

    public FullAccountObject getFullAccount(boolean isLoginIn) {
        return isLoginIn ? mFullAccount : null;
    }

    public FullAccountObject getFullAccount(String name){
        if(mFullAccount != null){
            return mFullAccount;
        }
        if(!TextUtils.isEmpty(name)){
            callFullAccount(name);
        }
        return null;
    }

    public void cancelCallFullAccount(){
        mNames.clear();
        mTimer.cancel();
    }

    public void cancelRMBSubscription() {
        mSubscription.cancel();
        mSubscription = null;
    }

    public void getAssetsRmbPrice(){
        //防止多次执行
        if(mSubscription != null){
            return;
        }
        Flowable.interval(0, 5, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<CnyResponse>>() {
                    @Override
                    public Publisher<CnyResponse> apply(Long aLong) {
                        return RetrofitFactory.getInstance().api().getCny();
                    }
                })
                .map(new Function<CnyResponse, List<AssetRmbPrice>>() {
                    @Override
                    public List<AssetRmbPrice> apply(CnyResponse cnyResponse) {
                        return cnyResponse.getPrices() != null ? cnyResponse.getPrices() : new ArrayList<>();
                    }
                })
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<List<AssetRmbPrice>>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.v(TAG, "getAssetsRmbPrice: onSubscribe");
                        mSubscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(List<AssetRmbPrice> o) {
                        Log.v(TAG, "getAssetsRmbPrice: onNext");
                        mAssetRmbPrices = o;
                        EventBus.getDefault().post(new Event.UpdateRmbPrice(o));
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.v(TAG, "getAssetsRmbPrice:" + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.v(TAG, "getAssetsRmbPrice: onComplete");
                    }
                });

    }

    public List<AssetRmbPrice> getAssetRmbPrices(){
        return mAssetRmbPrices;
    }

    public AssetRmbPrice getAssetRmbPrice(String assetName){
        if(mAssetRmbPrices == null || mAssetRmbPrices.size() == 0){
            return null;
        }
        for(AssetRmbPrice assetRmbPrice : mAssetRmbPrices){
            if(assetName.equals(assetRmbPrice.getName())){
                return assetRmbPrice;
            }
        }
        return null;
    }

    public AssetObject getAssetObject(String assetId){
        for(AssetObject assetObject : mAssetObjects){
            if(assetObject.id.toString().equals(assetId)){
                return assetObject;
            }
        }
        loadAssetObjectData(assetId);
        return null;
    }

    public List<AssetObject> getAssetObjects(String baseAssetId, String quoteAssetId){
        List<AssetObject> assetObjects = new ArrayList<>();
        AssetObject baseAssetObject = null;
        AssetObject quoteAssetObject = null;
        for(AssetObject assetObject : mAssetObjects){
            if(assetObject.id.toString().equals(baseAssetId)){
                baseAssetObject = assetObject;
            }
            if(assetObject.id.toString().equals(quoteAssetId)){
                quoteAssetObject = assetObject;
            }
        }
        if(baseAssetObject != null && quoteAssetObject != null){
            assetObjects.add(baseAssetObject);
            assetObjects.add(quoteAssetObject);
            return assetObjects;
        }
        loadAssetObjectDatas(baseAssetId, quoteAssetId);
        return null;
    }

    /**
     * 清除用户缓存数据
     */
    public void clearAccountCache(){
        mFullAccount = null;
        mNames.clear();
    }
}
