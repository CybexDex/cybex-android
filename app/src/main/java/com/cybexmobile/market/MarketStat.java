package com.cybexmobile.market;


import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.DateUtils;
import android.util.Log;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.RetrofitApi;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchListData;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.Price;
import com.cybexmobile.graphene.chain.Utils;
import com.cybexmobile.manager.ThreadPoolManager;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.Utils;
import com.google.gson.internal.LinkedTreeMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MarketStat {
    private static final String TAG = "MarketStat";
    private static final long DEFAULT_BUCKET_SECS = TimeUnit.MINUTES.toSeconds(5);
    private static final long BUCKETS_SECS_HOUR = 3600;
    private static final long BUCKETS_SECS_DAY = 86400;
    private static MarketStat INSTANCE = null;
    private BitsharesWalletWraper wraper = BitsharesWalletWraper.getInstance();

    public static final int STAT_MARKET_HISTORY = 0x01;
    public static final int STAT_MARKET_FILL_ORDER_HISTORY = 0x03;
    public static final int STAT_MARKET_TICKER = 0x02;
    public static final int STAT_MARKET_ORDER_BOOK = 0x04;
    public static final int STAT_MARKET_OPEN_ORDER = 0x08;
    public static final int STAT_MARKET_ALL = 0xffff;

    private List<WatchListData> mWatchListDateList = new ArrayList<WatchListData>();
    private HashMap<String, List<WatchListData>> mWatchListDataListHashMap = new HashMap<>();
    private HashMap<String, Integer> mRMBValueHashMap = new HashMap<>();

    private HashMap<String, Subscription> subscriptionHashMap = new HashMap<>();
    private HashMap<String, List<List<String>>> mCoinListHashMap = new HashMap<>();
    private static boolean isDeserializerRegistered = false;
    private List<List<String>> mCoinPairList = new ArrayList<>();

    private MarketStat() {
        if (!isDeserializerRegistered) {
            isDeserializerRegistered = true;
        }
        EventBus.getDefault().register(this);

    }

    public static synchronized MarketStat getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MarketStat();
        }
        return (INSTANCE);
    }

    public interface getResultListener {
        void getResultListener(HashMap<String,List<WatchListData>> watchListData, String baseAsset);
    }

    public interface startFirstActivityListener {
        void startToRunFirstActivity();
    }

    public interface callBackListener {
        void continueGetWebSocketConnect();
    }

    private Handler mHandler = new Handler();

    private ThreadPoolManager executorService = ThreadPoolManager.getInstance();


    public void getWebSocketConnect(final startFirstActivityListener startFirstActivityListener) {

        if (mCoinListHashMap != null && mCoinListHashMap.size() != 0) {
            startFirstActivityListener.startToRunFirstActivity();
        } else {
            getCoinPairConfiguration(new callBackListener() {
                @Override
                public void continueGetWebSocketConnect() {
//                    Log.e("shefeng", mCoinListHashMap.get().toString());
                    wraper.build_connect();
                    if (startFirstActivityListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                startFirstActivityListener.startToRunFirstActivity();
                            }
                        });
                    }
                }
            }, "1.3.0");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        switch (string) {
            case "onReconnect":
                getWebSocketConnect(null);
                //startRun(null);
                break;
        }

    }

    public void getCoinPairConfiguration(final callBackListener listener, final String baseAsset) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://app.cybex.io/market_list?base=" + baseAsset)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(myResponse);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    List<List<String>> coinPairArrayList = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        final List<String> coinPairList = new ArrayList<>();
                        coinPairList.add(baseAsset);
                        coinPairList.add(jsonArray.getString(i));
                        coinPairArrayList.add(coinPairList);
                    }
                    mCoinListHashMap.put(baseAsset, coinPairArrayList);
                    if (listener != null) {
                        listener.continueGetWebSocketConnect();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void startRun(final getResultListener listener, final String baseAsset, String tabSymbol) {
        //mWatchListDateList.clear();
        if (mWatchListDataListHashMap.get(baseAsset) != null) {
            mWatchListDataListHashMap.get(baseAsset).clear();
        }
        //Log.e("coinPair", mCoinPairList.toString());
        mWatchListDataListHashMap.put(baseAsset, new ArrayList<WatchListData>());
         for (int i = 0; i < mCoinListHashMap.get(baseAsset).size(); i++) {
            String base = mCoinListHashMap.get(baseAsset).get(i).get(0);
            String quote = mCoinListHashMap.get(baseAsset).get(i).get(1);
            executorService.execute(new Task(base, quote, listener, tabSymbol));
        }

        if (mCoinListHashMap.get(baseAsset).size() == 0) {
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     listener.getResultListener(mWatchListDataListHashMap, baseAsset);
                 }
             });
        }

    }

    class Task implements Runnable {
        private String mQuote;
        private String mBase;
        private getResultListener mListener;
        private String mTabSymbol;

        public Task(String base, String quote, getResultListener listener, String tabSymbol) {
            super();
            mQuote = quote;
            mBase = base;
            mListener = listener;
            mTabSymbol = tabSymbol;
        }

        @Override
        public void run() {
            try {
                String subscribeId = wraper.subscribe_to_market(mBase, mQuote);
                mWatchListDataListHashMap.get(mBase).add(getWatchLIstData(mBase, mQuote, subscribeId, mTabSymbol));
                if (mWatchListDataListHashMap.get(mBase).size() == mCoinListHashMap.get(mBase).size()) {
                    Collections.sort(mWatchListDataListHashMap.get(mBase), new Comparator<WatchListData>() {
                        @Override
                        public int compare(WatchListData o1, WatchListData o2) {
                            return o1.getVol() > o2.getVol() ? -1 : 1;
                        }
                    });

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.getResultListener(mWatchListDataListHashMap, mBase);
                            }
                        }
                    });
                }
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }

        }
    }

//    public MarketStat() {
//        if (!isDeserializerRegistered) {
//            isDeserializerRegistered = true;
////            GlobalConfigObject.getInstance().getGsonBuilder().registerTypeAdapter(
////                    FullAccountObject.class, new FullAccountObject.deserializer());
//        }
//    }

    public void subscribe(String base, String quote, int stats, long intervalMillis, long bucketsDuration,
                          OnMarketStatUpdateListener l) {
        subscribe(base, quote, bucketsDuration, stats, intervalMillis, l);
    }

    public void subscribe(String base, String quote, long bucketSize, int stats,
                          long intervalMillis, OnMarketStatUpdateListener l) {
        unsubscribe(base, quote);
        Subscription subscription =
                new Subscription(base, quote, bucketSize, stats, intervalMillis, l);
        subscriptionHashMap.put(makeMarketName(base, quote), subscription);
    }

    public void unsubscribe(String base, String quote) {
        String market = makeMarketName(base, quote);
        Subscription subscription = subscriptionHashMap.get(market);
        if (subscription != null) {
            subscriptionHashMap.remove(market);
            subscription.cancel();
        }
    }

    public void updateImmediately(String base, String quote) {
        String market = makeMarketName(base, quote);
        Subscription subscription = subscriptionHashMap.get(market);
        if (subscription != null) {
            subscription.updateImmediately();
        }
    }

    private static String makeMarketName(String base, String quote) {
        return String.format("%s_%s", base.toLowerCase(), quote.toLowerCase());
    }

    public static class HistoryPrice {
        public double high;
        public double low;
        public double open;
        public double close;
        public double volume;
        public double quoteVolume;
        public Date date;
    }

    public static class Stat {
        public HistoryPrice[] prices;
        public MarketTicker ticker;
        public Date latestTradeDate;
        public OrderBook orderBook;
        public List<MarketTrade> marketTradeList;
        public List<OpenOrder> openOrders;
    }

    public interface OnMarketStatUpdateListener {
        void onMarketStatUpdate(Stat stat);
    }

    private class Subscription implements Runnable {
        private String base;
        private String quote;
        private long bucketSecs = BUCKETS_SECS_HOUR;
        private int stats;
        private long intervalMillis;
        private OnMarketStatUpdateListener listener;
        private AssetObject baseAsset;
        private AssetObject quoteAsset;

        private BitsharesWalletWraper wraper = BitsharesWalletWraper.getInstance();
        private Handler handler = new Handler();
        private Handler handler2 = new Handler();
        private Handler statHandler;
        private HandlerThread statThread;
        private AtomicBoolean isCancelled = new AtomicBoolean(false);

        private Subscription(String base, String quote, long bucketSecs, int stats,
                             long intervalMillis, OnMarketStatUpdateListener l) {
            this.base = base;
            this.quote = quote;
            this.bucketSecs = bucketSecs;
            this.stats = stats;
            this.intervalMillis = intervalMillis;
            this.listener = l;
            this.statThread = new HandlerThread(makeMarketName(base, quote));
            this.statThread.start();
            this.statHandler = new Handler(this.statThread.getLooper());
            this.statHandler.post(this);
        }

        private void cancel() {
            isCancelled.set(true);
            statHandler.getLooper().quit();
        }

        private void updateImmediately() {
            statHandler.post(this);
        }

        @Override
        public void run() {
            final Stat marketStat = new Stat();
            if (getAssets()) {

                if ((stats & STAT_MARKET_HISTORY) != 0) {
                    marketStat.prices = getMarketHistory();
                    marketStat.orderBook = getOrderBook();
                    try {
                        List<HashMap<String, Object>> hashMapList = wraper.get_fill_order_history(baseAsset.id, quoteAsset.id, 40);
                        marketStat.marketTradeList = getListFromMap(hashMapList, base, quote);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }

                if (stats == STAT_MARKET_FILL_ORDER_HISTORY) {
                    try {
                        List<HashMap<String, Object>> hashMapList = wraper.get_fill_order_history(baseAsset.id, quoteAsset.id, 40);
                        marketStat.marketTradeList = getListFromMap(hashMapList, base, quote);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }
                if ((stats & STAT_MARKET_TICKER) != 0) {
                    try {
                        marketStat.ticker = wraper.get_ticker(base, quote);
//                        Date start = new Date(System.currentTimeMillis());
//                        Date end = new Date(
//                                System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
//                        List<MarketTrade> trades =
//                                wraper.get_trade_history(base, quote, start, end, 1);
//                        if (trades == null || trades.isEmpty()) {
//                            end = new Date(0);
//                            trades = wraper.get_trade_history(base, quote, start, end, 1);
//                        }
//                        if (trades != null && trades.size() > 0) {
//                            marketStat.latestTradeDate = trades.get(0).date;
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if ((stats & STAT_MARKET_ORDER_BOOK) != 0) {
                    marketStat.orderBook = getOrderBook();
                    handler2.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onMarketStatUpdate(marketStat);
                        }
                    });

                }
                if ((stats & STAT_MARKET_OPEN_ORDER) != 0) {
                    marketStat.openOrders = getOpenOrders();
                }
                if (isCancelled.get()) {
                    return;
                }
//                try {
//                    wraper.set_subscribe_market(true);
//                    wraper.subscribe_to_market(baseAsset.id, quoteAsset.id);
//                } catch (NetworkStatusException e) {
//                    e.printStackTrace();
//                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onMarketStatUpdate(marketStat);
                    }
                });
//                statHandler.postDelayed(this, intervalMillis);
            } else if (!isCancelled.get()) {
                statHandler.postDelayed(this, 500);
            }
        }

        private boolean getAssets() {
            if (baseAsset != null && quoteAsset != null) {
                return true;
            }
            try {
                baseAsset = wraper.lookup_asset_symbols(base);
                quoteAsset = wraper.lookup_asset_symbols(quote);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private List<MarketTrade> getListFromMap(List<HashMap<String, Object>> hashMaplist, String base, String quote) {
            List<MarketTrade> list = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.US);
            for (int i = 0; i < hashMaplist.size(); i += 2) {

                MarketTrade marketTrade = new MarketTrade();
                LinkedTreeMap op = (LinkedTreeMap) hashMaplist.get(i).get("op");
                LinkedTreeMap pays = (LinkedTreeMap) op.get("pays");
                LinkedTreeMap receives = (LinkedTreeMap) op.get("receives");
                String date = (String) hashMaplist.get(i).get("time");


                try {
                    Date converted = simpleDateFormat.parse(date);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(converted);
                    cal.add(Calendar.HOUR_OF_DAY, 8);
                    marketTrade.date = simpleDateFormat1.format(cal.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                marketTrade.base = base;
                marketTrade.quote = quote;
                String paysAmount = String.format("%s", pays.get("amount"));
                String receiveAmount = String .format("%s", receives.get("amount"));

                if (pays.get("asset_id").equals(baseAsset.id.toString())) {
                    marketTrade.baseAmount = Double.parseDouble(paysAmount) / Math.pow(10, baseAsset.precision);

                    marketTrade.quoteAmount = Double.parseDouble(receiveAmount) / Math.pow(10, quoteAsset.precision);
                    marketTrade.price = marketTrade.baseAmount / marketTrade.quoteAmount;
                    marketTrade.showRed = "showRed";
                } else {
                    marketTrade.quoteAmount = Double.parseDouble(paysAmount) / Math.pow(10, quoteAsset.precision);
                    marketTrade.baseAmount = Double.parseDouble(receiveAmount) / Math.pow(10, baseAsset.precision);

                    marketTrade.price = marketTrade.baseAmount / marketTrade.quoteAmount;
                    marketTrade.showRed = "showGreen";
                }

                list.add(marketTrade);

            }
            return list;
        }

        private HistoryPrice[] getMarketHistory() {
            // 服务器每次最多返回200个bucket对象
            final int maxBucketCount = 200;
            Date startDate1 = new Date(
                    System.currentTimeMillis() - bucketSecs * maxBucketCount * 1000);
            Date startDate2 = new Date(
                    System.currentTimeMillis() - bucketSecs * maxBucketCount * 2000);

//            Date startDate = new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
            Date startDate = new Date(0);
            Date endDate = new Date(System.currentTimeMillis());
//            List<BucketObject> buckets1 = getMarketHistory(startDate2, startDate1);
            List<BucketObject> buckets2 = getMarketHistory(startDate1, endDate);
//            int numBuckets = (buckets1 != null ? buckets1.size() : 0) +
//                    (buckets2 != null ? buckets2.size() : 0);
            int numBuckets = buckets2 != null ? buckets2.size() : 0;

            HistoryPrice[] prices = new HistoryPrice[numBuckets];
            HistoryPrice[] prices1 = new HistoryPrice[24];
            int priceIndex = 0;
//            if (buckets1 != null) {
//                for (int i = 0; i < buckets1.size(); i++) {
//                    BucketObject bucket = buckets1.get(i);
//                    prices[priceIndex++] = priceFromBucket(bucket);
//                }
//            }
            if (buckets2 != null) {
                for (int i = 0; i < buckets2.size(); i++) {
                    BucketObject bucket = buckets2.get(i);
                    long hours = bucket.key.open.getTime();
                    prices[priceIndex++] = priceFromBucket(bucket);
                }
            }
            return prices;
        }


        private List<BucketObject> getMarketHistory(Date start, Date end) {
            try {
                return wraper.get_market_history(
                        baseAsset.id, quoteAsset.id, (int) bucketSecs, start, end);
            } catch (Exception e) {
                return null;
            }
        }

        private HistoryPrice priceFromBucket(BucketObject bucket) {
            HistoryPrice price = new HistoryPrice();
            price.date = bucket.key.open;
            if (bucket.key.quote.equals(quoteAsset.id)) {
                price.high = Utils.get_asset_price(bucket.high_base, baseAsset,
                        bucket.high_quote, quoteAsset);
                price.low = Utils.get_asset_price(bucket.low_base, baseAsset,
                        bucket.low_quote, quoteAsset);
                price.open = Utils.get_asset_price(bucket.open_base, baseAsset,
                        bucket.open_quote, quoteAsset);
                price.close = Utils.get_asset_price(bucket.close_base, baseAsset,
                        bucket.close_quote, quoteAsset);
                price.volume = Utils.get_asset_amount(bucket.quote_volume, quoteAsset);
            } else {
                price.low = Utils.get_asset_price(bucket.high_quote, baseAsset,
                        bucket.high_base, quoteAsset);
                price.high = Utils.get_asset_price(bucket.low_quote, baseAsset,
                        bucket.low_base, quoteAsset);
                price.open = Utils.get_asset_price(bucket.open_quote, baseAsset,
                        bucket.open_base, quoteAsset);
                price.close = Utils.get_asset_price(bucket.close_quote, baseAsset,
                        bucket.close_base, quoteAsset);
                price.volume = Utils.get_asset_amount(bucket.base_volume, quoteAsset);
            }
            if (price.low == 0) {
                price.low = findMin(price.open, price.close);
            }
            if (price.high == Double.NaN || price.high == Double.POSITIVE_INFINITY) {
                price.high = findMax(price.open, price.close);
            }
            if (price.close == Double.POSITIVE_INFINITY || price.close == 0) {
                price.close = price.open;
            }
            if (price.open == Double.POSITIVE_INFINITY || price.open == 0) {
                price.open = price.close;
            }
            if (price.high > 1.3 * ((price.open + price.close) / 2)) {
                price.high = findMax(price.open, price.close);
            }
            if (price.low < 0.7 * ((price.open + price.close) / 2)) {
                price.low = findMin(price.open, price.close);
            }
            return price;
        }

        private OrderBook getOrderBook() {
            try {
                List<LimitOrderObject> orders =
                        wraper.get_limit_orders(baseAsset.id, quoteAsset.id, 200);
                if (orders != null) {
                    OrderBook orderBook = new OrderBook();
                    orderBook.base = baseAsset.symbol;
                    orderBook.quote = quoteAsset.symbol;
                    orderBook.bids = new ArrayList<>();
                    orderBook.asks = new ArrayList<>();
                    for (int i = 0; i < orders.size(); i++) {
                        LimitOrderObject o = orders.get(i);
                        if (o.sell_price.base.asset_id.equals(baseAsset.id)) {
                            Order ord = new Order();
                            ord.price = priceToReal(o.sell_price);
                            ord.quote = ((double) o.for_sale * (double) o.sell_price.quote.amount)
                                    / (double) o.sell_price.base.amount
                                    / Math.pow(10, quoteAsset.precision);
                            ord.base = o.for_sale / Math.pow(10, baseAsset.precision);
                            orderBook.bids.add(ord);
                        } else {
                            Order ord = new Order();
                            ord.price = priceToReal(o.sell_price);
                            ord.quote = o.for_sale / Math.pow(10, quoteAsset.precision);
                            ord.base = (double) o.for_sale * (double) o.sell_price.quote.amount
                                    / o.sell_price.base.amount
                                    / Math.pow(10, baseAsset.precision);
                            orderBook.asks.add(ord);
                        }
                    }
                    Collections.sort(orderBook.bids, new Comparator<Order>() {
                        @Override
                        public int compare(Order o1, Order o2) {
                            return (o1.price - o2.price) < 0 ? 1 : -1;
                        }
                    });
                    Collections.sort(orderBook.asks, new Comparator<Order>() {
                        @Override
                        public int compare(Order o1, Order o2) {
                            return (o1.price - o2.price) < 0 ? -1 : 1;
                        }
                    });
                    return orderBook;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private List<OpenOrder> getOpenOrders() {
            try {
                List<AccountObject> accounts = wraper.list_my_accounts();
                if (accounts == null || accounts.isEmpty()) {
                    return null;
                }
                List<String> names = new ArrayList<>(accounts.size());
                for (AccountObject o : accounts) {
                    names.add(o.name);
                }
                List<FullAccountObject> fullAccounts;
                try {
                    fullAccounts = wraper.get_full_accounts(names, false);
                    if (fullAccounts == null || fullAccounts.isEmpty()) {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
                List<OpenOrder> openOrders = new ArrayList<>();
                for (int i = 0; i < fullAccounts.size(); i++) {
                    FullAccountObject a = fullAccounts.get(i);
                    for (int j = 0; j < a.limit_orders.size(); j++) {
                        LimitOrderObject o = a.limit_orders.get(j);
                        if (!o.sell_price.base.asset_id.equals(baseAsset.id) &&
                                !o.sell_price.base.asset_id.equals(quoteAsset.id)) {
                            continue;
                        }
                        if (!o.sell_price.quote.asset_id.equals(baseAsset.id) &&
                                !o.sell_price.quote.asset_id.equals(quoteAsset.id)) {
                            continue;
                        }
                        OpenOrder order = new OpenOrder();
                        order.limitOrder = o;
                        order.base = baseAsset;
                        order.quote = quoteAsset;
                        order.price = priceToReal(o.sell_price);
                        openOrders.add(order);
                    }
                }
                return openOrders;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private double assetToReal(Asset a, long p) {
            return (double) a.amount / Math.pow(10, p);
        }

        private double priceToReal(Price p) {
            if (p.base.asset_id.equals(baseAsset.id)) {
                return assetToReal(p.base, baseAsset.precision)
                        / assetToReal(p.quote, quoteAsset.precision);
            } else {
                return assetToReal(p.quote, baseAsset.precision)
                        / assetToReal(p.base, quoteAsset.precision);
            }
        }
    }

    public WatchListData getWatchLIstData(String base, String quote, String subscribeId, String tabSymbol) {
        retrofit2.Call<ResponseBody> call = RetrofitApi.getCnyInterface().getCny();
        try {
            retrofit2.Response<ResponseBody> response = call.execute();
            String responseString = response.body().string();
            Log.e("rmb", responseString);

            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray prices = jsonObject.getJSONArray("prices");
            double rmb = 0;
            for(int i = 0; i < prices.length(); i++) {
                JSONObject price = prices.getJSONObject(i);
                if (price.getString("name").equals(tabSymbol)) {
                    rmb = price.getDouble("value");
                    break;
                }
            }
            AssetObject baseAssetLocal = wraper.get_objects(base);
            AssetObject quoteAssetLocal = wraper.get_objects(quote);
            MarketTicker marketTicker;
            List<HistoryPrice> historyPriceList = requestFor24HoursMarketHistory(baseAssetLocal, quoteAssetLocal);
            marketTicker = wraper.get_ticker(base, quote);
            return CalculateWatchListData(baseAssetLocal, quoteAssetLocal, historyPriceList, marketTicker, subscribeId, rmb);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<HistoryPrice> requestFor24HoursMarketHistory(AssetObject baseAsset, AssetObject quoteAsset) {
        List<HistoryPrice> historyPriceList = new ArrayList<>();
        Date startDate = new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
        Date endDate = new Date(System.currentTimeMillis());
        List<BucketObject> buckets = getMarketHistory(baseAsset, quoteAsset, startDate, endDate);
        BucketObject previousBucketObject = null;
        if (buckets != null) {
            for (int i = 0; i < buckets.size(); i++) {
                if (buckets.get(i).key.open.getTime() > startDate.getTime() - 3600 && i == 0) {
                    Date newStartDate = new Date(startDate.getTime() - DateUtils.DAY_IN_MILLIS);
                    List<BucketObject> previousBuckets = getMarketHistory(baseAsset, quoteAsset, newStartDate, startDate);
                    if (previousBuckets != null && previousBuckets.size() != 0) {
                        previousBucketObject = previousBuckets.get(previousBuckets.size() - 1);
                    }
                }
                historyPriceList.add(priceFromBucket(baseAsset, quoteAsset, buckets.get(i)));
            }
            if (previousBucketObject != null) {
                historyPriceList.add(0, priceFromBucket(baseAsset, quoteAsset, previousBucketObject));
            }
        }
        return historyPriceList;
    }

    private HistoryPrice generateNewElement(HistoryPrice price) {
        double temp = 0.f;
        temp = price.open;
        price.open = price.close;
        price.close = temp;
        price.volume = 0;
        return price;
    }


    private WatchListData CalculateWatchListData(AssetObject base, AssetObject quote, List<HistoryPrice> historyPriceList, MarketTicker marketTicker, String subscribeId, double rmb) {
        WatchListData watchListData = new WatchListData();
        watchListData.setBaseId(base.id.toString());
        watchListData.setQuoteId(quote.id.toString());
        watchListData.setSubscribeId(subscribeId);
        Log.e("id", base.id.toString() + " " + quote.id.toString());
        watchListData.setBase(base.symbol);
        watchListData.setQuote(quote.symbol);
        watchListData.setRmbPrice(rmb);
        if (historyPriceList != null && historyPriceList.size() != 0) {
            watchListData.setHigh(getHighFromPriceList(historyPriceList));
            watchListData.setLow(getLowFromPriceList(historyPriceList));
            watchListData.setCurrentPrice(getCurrentPriceFromPriceList(historyPriceList));
            watchListData.setVol(marketTicker.base_volume);
            watchListData.setQuoteVol(marketTicker.quote_volume);
            watchListData.setChange(getChangeFromPriceList(historyPriceList));
        }
        return watchListData;
    }

    private double getHighFromPriceList(List<HistoryPrice> historyPriceList) {
        double max = 0;
        for (HistoryPrice historyPrice : historyPriceList) {
            max = Math.max(historyPrice.high, max);
        }
        return max;
    }

    private double getLowFromPriceList(List<HistoryPrice> historyPriceList) {
        double min = historyPriceList.get(0).low;
        for (HistoryPrice historyPrice : historyPriceList) {
            min = Math.min(historyPrice.low, min);
        }
        return min;
    }

    private double getCurrentPriceFromPriceList(List<HistoryPrice> historyPriceList) {
        double currentPrice;
        currentPrice = historyPriceList.get(historyPriceList.size() - 1).close;
        return currentPrice;
    }

    private double getVolFromPriceList(List<HistoryPrice> historyPriceList) {
        double vol = 0;
        for (HistoryPrice historyPrice : historyPriceList) {
            vol += historyPrice.volume;
        }
        return vol;
    }

    private double getQuoteVolFromPriceList(List<HistoryPrice> historyPriceList) {
        double vol = 0;
        for (HistoryPrice historyPrice : historyPriceList) {
            vol += historyPrice.quoteVolume;
        }
        return vol;
    }


    private String getChangeFromPriceList(List<HistoryPrice> historyPriceList) {
        String change;
        double open = historyPriceList.get(0).open;
        double close = historyPriceList.get(historyPriceList.size() - 1).close;
        change = String.valueOf((close - open) / open);
        return change;
    }

    private List<BucketObject> getMarketHistory(AssetObject baseAsset, AssetObject quoteAsset, Date startDate, Date end) {
        try {
            return wraper.get_market_history(
                    baseAsset.id, quoteAsset.id, 3600, startDate, end);
        } catch (Exception e) {
            return null;
        }
    }

    private HistoryPrice priceFromBucket(AssetObject baseAsset, AssetObject quoteAsset, BucketObject bucket) {
        HistoryPrice price = new HistoryPrice();
        price.date = bucket.key.open;
        if (bucket.key.quote.equals(quoteAsset.id)) {
            price.high = Utils.get_asset_price(bucket.high_base, baseAsset,
                    bucket.high_quote, quoteAsset);
            price.low = Utils.get_asset_price(bucket.low_base, baseAsset,
                    bucket.low_quote, quoteAsset);
            price.open = Utils.get_asset_price(bucket.open_base, baseAsset,
                    bucket.open_quote, quoteAsset);
            price.close = Utils.get_asset_price(bucket.close_base, baseAsset,
                    bucket.close_quote, quoteAsset);
            price.volume = Utils.get_asset_amount(bucket.base_volume, baseAsset);
            price.quoteVolume = Utils.get_asset_amount(bucket.quote_volume, baseAsset);
        } else {
            price.low = Utils.get_asset_price(bucket.high_quote, baseAsset,
                    bucket.high_base, quoteAsset);
            price.high = Utils.get_asset_price(bucket.low_quote, baseAsset,
                    bucket.low_base, quoteAsset);
            price.open = Utils.get_asset_price(bucket.open_quote, baseAsset,
                    bucket.open_base, quoteAsset);
            price.close = Utils.get_asset_price(bucket.close_quote, baseAsset,
                    bucket.close_base, quoteAsset);
            price.volume = Utils.get_asset_amount(bucket.base_volume, quoteAsset);
            price.quoteVolume = Utils.get_asset_amount(bucket.quote_volume, baseAsset);
        }
        if (price.low == 0) {
            price.low = findMin(price.open, price.close);
        }
        if (price.high == Double.NaN || price.high == Double.POSITIVE_INFINITY) {
            price.high = findMax(price.open, price.close);
        }
        if (price.close == Double.POSITIVE_INFINITY || price.close == 0) {
            price.close = price.open;
        }
        if (price.open == Double.POSITIVE_INFINITY || price.open == 0) {
            price.open = price.close;
        }
        if (price.high > 1.3 * ((price.open + price.close) / 2)) {
            price.high = findMax(price.open, price.close);
        }
        if (price.low < 0.7 * ((price.open + price.close) / 2)) {
            price.low = findMin(price.open, price.close);
        }
        return price;
    }

    private static double findMax(double a, double b) {
        if (a != Double.POSITIVE_INFINITY && b != Double.POSITIVE_INFINITY) {
            return Math.max(a, b);
        } else if (a == Double.POSITIVE_INFINITY) {
            return b;
        } else {
            return a;
        }
    }

    private static double findMin(double a, double b) {
        if (a != 0 && b != 0) {
            return Math.min(a, b);
        } else if (a == 0) {
            return b;
        } else {
            return a;
        }
    }

    public List<WatchListData> getWatchListDataList() {
        return mWatchListDateList;
    }

    public HashMap<String, List<WatchListData>> getmWatchListDataListHashMap() {
        return mWatchListDataListHashMap;
    }

    public HashMap<String, List<List<String>>> getmCoinListHashMap() {
        return mCoinListHashMap;
    }

    public List<List<String>> getCoinList() {
        return mCoinPairList;
    }
}
