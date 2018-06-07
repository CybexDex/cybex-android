package com.cybexmobile.market;


import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.DateUtils;
import android.util.Log;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.Price;
import com.cybexmobile.graphene.chain.Utils;
import com.cybexmobile.manager.ThreadPoolManager;
import com.cybexmobile.utils.MathUtil;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
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
import okhttp3.ResponseBody;

public class MarketStat {
    private static final String TAG = "MarketStat";
    private static final long DEFAULT_BUCKET_SECS = TimeUnit.MINUTES.toSeconds(5);
    private static final long BUCKETS_SECS_HOUR = 3600;
    private static final long BUCKETS_SECS_DAY = 86400;
    private static MarketStat INSTANCE = null;
    private BitsharesWalletWraper wraper = BitsharesWalletWraper.getInstance();

    public static final int STAT_MARKET_HISTORY = 0x01;
    public static final int STAT_MARKET_TICKER = 0x02;
    public static final int STAT_MARKET_OPEN_ORDER = 0x08;
    public static final int STAT_MARKET_ALL = 0xffff;

    private HashMap<String, Double> mRmbListHashMap = new HashMap<>();
    private static boolean isDeserializerRegistered = false;

    private MarketStat() {
        if (!isDeserializerRegistered) {
            isDeserializerRegistered = true;
        }
    }

    public static synchronized MarketStat getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MarketStat();
        }
        return (INSTANCE);
    }

    private static String makeMarketName(String base, String quote) {
        return String.format("%s_%s", base.toLowerCase(), quote.toLowerCase());
    }

    public static class Stat {
        public HistoryPrice[] prices;
        public MarketTicker ticker;
        public Date latestTradeDate;
        public OrderBook orderBook;
        public List<MarketTrade> marketTradeList;
        public List<OpenOrder> openOrders;
    }

    public double getRMBPriceFromHashMap(String symbol) {
        if (mRmbListHashMap.get(symbol) == null) {
            return 0;//getRmbPrice(symbol);
        } else {
            return mRmbListHashMap.get(symbol);
        }
    }
}
