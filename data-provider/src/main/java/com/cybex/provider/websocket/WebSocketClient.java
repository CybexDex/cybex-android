package com.cybex.provider.websocket;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.Asset;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybex.provider.graphene.chain.BucketObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObjectReply;
import com.cybex.provider.graphene.chain.FullNodeServerSelect;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.LimitOrderObject;
import com.cybex.provider.graphene.chain.LockAssetObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.provider.graphene.chain.MarketTrade;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient extends WebSocketListener {

    private static final String TAG = "WebSocketClient";

    private static final int WHAT_MESSAGE = 10000001;

    //交易对比例
    private static final String CALL_GET_TICKER = "get_ticker";

    private volatile int _nDatabaseId = -1;
    private volatile int _nHistoryId = -1;
    private volatile int _nBroadcastId = -1;

    private static final String FLAG_DATABASE = "database";
    private static final String FLAG_HISTORY = "history";
    private static final String FLAG_BROADCAST = "broadcast";

    private OkHttpClient mOkHttpClient;
    private volatile WebSocket mWebSocket;
    //websocket connect status
    private volatile WebSocketStatus mConnectStatus = WebSocketStatus.DEFAULT;

    //call id
    private final AtomicInteger mCallId = new AtomicInteger(1);
    private final JsonParser mJsonParser = new JsonParser();

    private ConcurrentHashMap<Integer, ReplyProcessImpl> mHashMapIdToProcess = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<DelayCall> delayCalls = new ConcurrentLinkedQueue<>();
    private Sha256Object mChainIdObject;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what != WHAT_MESSAGE){
                return;
            }
            IReplyProcess iReplyProcess = (IReplyProcess) msg.obj;
            MessageCallback callback = iReplyProcess.getCallback();
            if(callback != null){
                callback.onMessage(iReplyProcess.getReply());
                iReplyProcess.release();
            }
        }
    };

    public AtomicInteger getCallId() {
        return mCallId;
    }

    public WebSocketClient() {
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.v(TAG, "onOpen: WebSocket is connected" );
        mWebSocket = webSocket;
        mConnectStatus = WebSocketStatus.OPENED;
        try {
            //websocke连接成功, send login
            login("", "", loginCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.v(TAG, String.format("onMessage: %s", bytes.toString()));
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.v(TAG, String.format("onMessage: %s", text));
        try {
            if (mJsonParser.parse(text).getAsJsonObject().get("id") == null) {
                Log.v("lsf", String.format("onMessage: %s", text));
                return;
            }
            int callId = mJsonParser.parse(text).getAsJsonObject().get("id").getAsInt();
            IReplyProcess iReplyProcess = mHashMapIdToProcess.remove(callId);
            if (iReplyProcess != null) {
                iReplyProcess.processTextToObject(text);
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_MESSAGE, iReplyProcess));
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        Log.v(TAG, "onFailure: WebSocket on failure", t);
        mConnectStatus = WebSocketStatus.FAILURE;
        _nDatabaseId = -1;
        _nBroadcastId = -1;
        _nHistoryId = -1;
        mHashMapIdToProcess.clear();
        try {
            Thread.sleep(3*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Log.v(TAG, "WebSocket is closing, code:" + code + " reason:" + reason);
        mConnectStatus = WebSocketStatus.CLOSING;
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.v(TAG, "WebSocket is closed, code:" + code + " reason:" + reason);
        mConnectStatus = WebSocketStatus.CLOSED;
    }

    //连接WebSocket
    public void connect() {
        if(mConnectStatus == WebSocketStatus.OPENING
                || mConnectStatus == WebSocketStatus.OPENED
                || mConnectStatus == WebSocketStatus.LOGIN){
            return;
        }
        String strServer = FullNodeServerSelect.getInstance().getServer();
        Log.v(TAG, strServer);
        if (TextUtils.isEmpty(strServer)) {
            //无效地址
            return;
        }
        Request request = new Request.Builder().url(strServer).build();
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newWebSocket(request, this);
        mConnectStatus = WebSocketStatus.OPENING;
    }

    public void disconnect() {
        this.close();
        mHandler.removeCallbacksAndMessages(null);
    }

    //关闭WebSocket
    public void close() {
        if(mWebSocket != null){
            mWebSocket.close(1000, "Close");
            mWebSocket = null;
        }
        mOkHttpClient = null;
        mConnectStatus = WebSocketStatus.CLOSING;
        mHashMapIdToProcess.clear();
        _nDatabaseId = -1;
        _nBroadcastId = -1;
        _nHistoryId = -1;
    }

    private void login(String strUserName, String strPassword, MessageCallback callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add("login");

        List<Object> listLoginParams = new ArrayList<>();
        listLoginParams.add(strUserName);
        listLoginParams.add(strPassword);
        callObject.params.add(listLoginParams);

        ReplyProcessImpl<Reply<Boolean>> replyObject = new ReplyProcessImpl<>(new TypeToken<Reply<Boolean>>() {}.getType(), callback);
        sendForReplyImpl(callObject, replyObject);
    }

    private void get_websocket_bitshares_api_id(String strApiName, MessageCallback<Reply<Integer>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add(strApiName);

        List<Object> listDatabaseParams = new ArrayList<>();
        callObject.params.add(listDatabaseParams);

        ReplyProcessImpl<Reply<Integer>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<Integer>>() {
                }.getType(), callback);
        sendForReplyImpl(callObject, replyObject);
    }

    public void get_chain_id(MessageCallback<Reply<Sha256Object>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_chain_id");

        List<Object> listDatabaseParams = new ArrayList<>();
        callObject.params.add(listDatabaseParams);

        ReplyProcessImpl<Reply<Sha256Object>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<Sha256Object>>(){}.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void lookup_account_names(String strAccountName, MessageCallback<Reply<List<AccountObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_account_names");

        List<Object> listAccountNames = new ArrayList<>();
        listAccountNames.add(strAccountName);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountNames);
        callObject.params.add(listAccountNamesParams);

        ReplyProcessImpl<Reply<List<AccountObject>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<AccountObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void get_account_by_name(String strAccountName, MessageCallback<Reply<AccountObject>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_account_by_name");

        List<Object> listAccountNameParams = new ArrayList<>();
        listAccountNameParams.add(strAccountName);

        callObject.params.add(listAccountNameParams);

        ReplyProcessImpl<Reply<AccountObject>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<AccountObject>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void get_account_by_id(){

    }


    public void get_accounts(List<String> listAccountObjectId, MessageCallback<Reply<List<AccountObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_accounts");

        List<Object> listAccountIds = new ArrayList<>();
        listAccountIds.add(listAccountObjectId);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountIds);

        callObject.params.add(listAccountIds);
        ReplyProcessImpl<Reply<List<AccountObject>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<AccountObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void list_account_balances(ObjectId<AccountObject> accountId, MessageCallback<Reply<List<Asset>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_account_balances");

        List<Object> listAccountBalancesParam = new ArrayList<>();
        listAccountBalancesParam.add(accountId);
        listAccountBalancesParam.add(new ArrayList<Object>());
        callObject.params.add(listAccountBalancesParam);


        ReplyProcessImpl<Reply<List<Asset>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<Asset>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

//    public void get_account_history(ObjectId<AccountObject> accountId, int nLimit, MessageCallback<Reply<List<AccountHistoryObject>>> callback) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nHistoryId);
//        callObject.params.add("get_account_history");
//
//        List<Object> listAccountHistoryParam = new ArrayList<>();
//        listAccountHistoryParam.add(accountId);
//        listAccountHistoryParam.add("1.11.0");
//        listAccountHistoryParam.add(nLimit);
//        listAccountHistoryParam.add("1.11.0");
//        callObject.params.add(listAccountHistoryParam);
//
//        ReplyProcessImpl<Reply<List<AccountHistoryObject>>> replyObject =
//                new ReplyProcessImpl<>(new TypeToken<Reply<List<AccountHistoryObject>>>(){}.getType(), callback);
//        sendForReply(FLAG_HISTORY, callObject, replyObject);
//    }

//    public global_property_object get_global_properties() throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_global_properties");
//
//        callObject.params.add(new ArrayList<>());
//
//        ReplyProcessImpl<Reply<global_property_object>> replyObjectProcess =
//                new ReplyProcessImpl<>(new TypeToken<Reply<global_property_object>>(){}.getType());
//        Reply<global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);
//
//        return replyObject.result;
//    }

    public void get_dynamic_global_properties(MessageCallback<Reply<DynamicGlobalPropertyObject>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_dynamic_global_properties");

        callObject.params.add(new ArrayList<Object>());

        ReplyProcessImpl<Reply<DynamicGlobalPropertyObject>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<DynamicGlobalPropertyObject>>(){}.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);

    }

    public void list_assets(String strLowerBound, int nLimit, MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("list_assets");

        List<Object> listAssetsParam = new ArrayList<>();
        listAssetsParam.add(strLowerBound);
        listAssetsParam.add(nLimit);
        callObject.params.add(listAssetsParam);

        ReplyProcessImpl<Reply<List<AssetObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<AssetObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void get_assets(List<ObjectId<AssetObject>> listAssetObjectId, MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_assets");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listObjectId = new ArrayList<>();
        listObjectId.addAll(listAssetObjectId);

        listAssetsParam.add(listObjectId);
        callObject.params.add(listAssetsParam);

        ReplyProcessImpl<Reply<List<AssetObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<AssetObject>>>() {
                }.getType(), callback);
         sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void lookup_asset_symbols(String strAssetSymbol, MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_asset_symbols");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listAssetSysmbols = new ArrayList<>();
        listAssetSysmbols.add(strAssetSymbol);

        listAssetsParam.add(listAssetSysmbols);
        callObject.params.add(listAssetsParam);

        ReplyProcessImpl<Reply<List<AssetObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<AssetObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void get_objects(Set<String> objectIds, MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_objects");

        List<Object> listObjectParams = new ArrayList<>();
        listObjectParams.add(objectIds);
        callObject.params.add(listObjectParams);
        ReplyProcessImpl<Reply<List<AssetObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<AssetObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void get_block(int callId, int blockNumber, MessageCallback<Reply<BlockHeader>> callback) throws NetworkStatusException {
        Call call = new Call();
        call.id = callId;
        call.method = "call";
        call.params = new ArrayList<>();
        call.params.add(_nDatabaseId);
        call.params.add("get_block");
        List<Object> blockData = new ArrayList<>();
        blockData.add(blockNumber);
        call.params.add(blockData);
        ReplyProcessImpl<Reply<BlockHeader>> replyReplyProcess =
            new ReplyProcessImpl<>(new TypeToken<Reply<BlockHeader>>(){}.getType(), callback);
        sendForReply(FLAG_DATABASE, call, replyReplyProcess);
    }

    public void get_block_header(int blockNumber, MessageCallback<Reply<BlockHeader>> callback) throws NetworkStatusException {
        Call call = new Call();
        call.id = mCallId.getAndIncrement();
        call.method = "call";
        call.params = new ArrayList<>();
        call.params.add(_nDatabaseId);
        call.params.add("get_block_header");
        List<Object> blockData = new ArrayList<>();
        blockData.add(blockNumber);
        call.params.add(blockData);
        ReplyProcessImpl<Reply<BlockHeader>> replyReplyProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<BlockHeader>>(){}.getType(), callback);
        sendForReply(FLAG_DATABASE, call, replyReplyProcess);
    }

    public void get_recent_transaction_by_id(String transactionId, MessageCallback<Reply<Object>> callback) throws NetworkStatusException {
        Call call = new Call();
        call.id = mCallId.getAndIncrement();
        call.method = "call";
        call.params = new ArrayList<>();
        call.params.add(_nDatabaseId);
        call.params.add("get_recent_transaction_by_id");
        List<Object> blockData = new ArrayList<>();
        blockData.add(transactionId);
        call.params.add(blockData);
        ReplyProcessImpl<Reply<Object>> replyReplyProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<Object>>(){}.getType(), callback);
        sendForReply(FLAG_DATABASE, call, replyReplyProcess);
    }

    public void broadcast_transaction_with_callback(SignedTransaction tx, MessageCallback<Reply<String>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nBroadcastId);
        callObject.params.add("broadcast_transaction_with_callback");
        List<Object> listTransaction = new ArrayList<>();
        listTransaction.add(callObject.id);
        listTransaction.add(tx);
        callObject.params.add(listTransaction);

        ReplyProcessImpl<Reply<String>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<String>>(){}.getType(), callback);
        sendForReply(FLAG_BROADCAST, callObject, replyObjectProcess);
    }

    public void get_market_history(ObjectId<AssetObject> baseAssetId,
                                                 ObjectId<AssetObject> quoteAssetId,
                                                 int nBucket,
                                                 Date dateStart,
                                                 Date dateEnd, MessageCallback<Reply<List<BucketObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_market_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(baseAssetId);
        listParams.add(quoteAssetId);
        listParams.add(nBucket);
        listParams.add(dateStart);
        listParams.add(dateEnd);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<BucketObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<BucketObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_HISTORY, callObject, replyObjectProcess);
    }


    public void get_limit_orders(ObjectId<AssetObject> base,
                                 ObjectId<AssetObject> quote,
                                 int limit, MessageCallback<Reply<List<LimitOrderObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_limit_orders");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<LimitOrderObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<LimitOrderObject>>>() {
                }.getType(), callback);
         sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void get_balance_objects(List<String> addresses, MessageCallback<Reply<List<LockAssetObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_balance_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(addresses);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<LockAssetObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<LockAssetObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void subscribe_to_market(String id, String base, String quote, MessageCallback<Reply<String>> callback)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = Integer.parseInt(id);
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("subscribe_to_market");

        List<Object> listParams = new ArrayList<>();
        listParams.add(callObject.id);
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<String>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<String>>() {
                }.getType(), callback);
         sendForReply(FLAG_DATABASE, callObject, replyObjectProcess);
    }

    public void get_ticker(String base, String quote, MessageCallback<Reply<MarketTicker>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add(CALL_GET_TICKER);

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<MarketTicker>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<MarketTicker>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void get_trade_history(String base, String quote, Date start, Date end, int limit,
                                               MessageCallback<Reply<List<MarketTrade>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_trade_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(start);
        listParams.add(end);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<MarketTrade>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<MarketTrade>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void get_fill_order_history(ObjectId<AssetObject> assetObjectId1,
                                                                ObjectId<AssetObject> assetObjectId2, int limit,
                                                                MessageCallback<Reply<List<HashMap<String, Object>>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_fill_order_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(assetObjectId1);
        listParams.add(assetObjectId2);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<HashMap<String, Object>>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<HashMap<String, Object>>>>() {
                }.getType(), callback);
        sendForReply(FLAG_HISTORY, callObject, replyObject);
    }

    public void get_full_accounts(List<String> names, boolean subscribe,
                                                     MessageCallback<Reply<List<FullAccountObjectReply>>> callback)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_full_accounts");

        List<Object> listParams = new ArrayList<>();
        listParams.add(names);
        listParams.add(subscribe);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<FullAccountObjectReply>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<FullAccountObjectReply>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);
    }

    public void get_required_fees(String assetId, int operationId, Operations.base_operation operation,
                                  MessageCallback<Reply<List<FeeAmountObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_required_fees");

        List<Object> operationParams1 = new ArrayList<>();
        List<Object> operationParams2 = new ArrayList<>();
        List<Object> operationParams3 = new ArrayList<>();

        operationParams3.add(operationId);
        operationParams3.add(operation);

        operationParams2.add(operationParams3);
        operationParams1.add(operationParams2);
        operationParams1.add(assetId);
        callObject.params.add(operationParams1);

        ReplyProcessImpl<Reply<List<FeeAmountObject>>> replyObject =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<FeeAmountObject>>>() {
                }.getType(), callback);
        sendForReply(FLAG_DATABASE, callObject, replyObject);

    }



    private <T> void sendForReply(String flag, Call callObject, ReplyProcessImpl<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        if(mWebSocket != null && mConnectStatus == WebSocketStatus.LOGIN){
            sendForReplyImpl(callObject, replyObjectProcess);
        }else {
            DelayCall<T> delayCall = new DelayCall<T>(flag, callObject, replyObjectProcess);
            addDelayCall(delayCall);
            if (mConnectStatus != WebSocketStatus.CLOSING || mConnectStatus != WebSocketStatus.CLOSED) {
                connect();
            }
        }
    }

    private <T> void addDelayCall(DelayCall<T> delayCall) {
        if (delayCalls.size() == 0) {
            delayCalls.add(delayCall);
            return;
        }
        boolean hasCall = false;
        for(DelayCall call : delayCalls) {
            if (call.call.params.get(1).equals(delayCall.call.params.get(1))) {
                hasCall = true;
                break;
            }
        }
        if (!hasCall) {
            delayCalls.add(delayCall);
        }
    }

    private <T> void sendForReplyImpl(Call call, ReplyProcessImpl<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(call);
        Log.v(TAG, String.format("call: %s", strMessage));
        boolean result = mWebSocket.send(strMessage);
        if(result){
            mHashMapIdToProcess.put(call.id, replyObjectProcess);
        }
    }

    private void sendDelayForReply() {
        if (delayCalls == null || delayCalls.size() == 0) {
            return;
        }

        Iterator<DelayCall> iterator = delayCalls.iterator();
        while (iterator.hasNext()) {
            DelayCall delayCall = iterator.next();
            switch (delayCall.flag) {
                case FLAG_DATABASE:
                    delayCall.call.params.set(0, _nDatabaseId);
                    break;
                case FLAG_HISTORY:
                    delayCall.call.params.set(0, _nHistoryId);
                    break;
                case FLAG_BROADCAST:
                    delayCall.call.params.set(0, _nDatabaseId);
                    break;
            }
            try {
                iterator.remove();
                sendForReply(delayCall.flag, delayCall.call, delayCall.replyProcess);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }

    }

    private MessageCallback<Reply<Sha256Object>> chainIdCallback = new MessageCallback<Reply<Sha256Object>>() {
        @Override
        public void onMessage(Reply<Sha256Object> reply) {
            mChainIdObject = reply.result;
        }

        @Override
        public void onFailure() {

        }
    };

    public Sha256Object getmChainIdObject() {
        return mChainIdObject;
    }

    private MessageCallback<Reply<Boolean>> loginCallback = new MessageCallback<Reply<Boolean>>() {
        @Override
        public void onMessage(Reply<Boolean> reply) {
            if(reply.result){
                try {
                    get_websocket_bitshares_api_id("database", databaseCallback);
                    get_websocket_bitshares_api_id("history", historyCallback);
                    get_websocket_bitshares_api_id("network_broadcast", broadcastCallback);
                    get_chain_id(chainIdCallback);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure() {

        }
    };

    private MessageCallback<Reply<Integer>> databaseCallback = new MessageCallback<Reply<Integer>>() {
        @Override
        public void onMessage(Reply<Integer> reply) {
            _nDatabaseId = reply.result;
            if(_nDatabaseId != -1 && _nBroadcastId != -1 && _nHistoryId != -1){
                mConnectStatus = WebSocketStatus.LOGIN;
                sendDelayForReply();
            }
        }

        @Override
        public void onFailure() {
            _nDatabaseId = -1;
        }
    };

    private MessageCallback<Reply<Integer>> historyCallback = new MessageCallback<Reply<Integer>>() {
        @Override
        public void onMessage(Reply<Integer> reply) {
            _nHistoryId = reply.result;
            if(_nDatabaseId != -1 && _nBroadcastId != -1 && _nHistoryId != -1){
                mConnectStatus = WebSocketStatus.LOGIN;
                sendDelayForReply();
            }
        }

        @Override
        public void onFailure() {
            _nHistoryId = -1;
        }
    };

    private MessageCallback<Reply<Integer>> broadcastCallback = new MessageCallback<Reply<Integer>>() {
        @Override
        public void onMessage(Reply<Integer> reply) {
            _nBroadcastId = reply.result;
            if(_nDatabaseId != -1 && _nBroadcastId != -1 && _nHistoryId != -1){
                mConnectStatus = WebSocketStatus.LOGIN;
                sendDelayForReply();
            }
        }

        @Override
        public void onFailure() {
            _nBroadcastId = -1;
        }
    };


}

