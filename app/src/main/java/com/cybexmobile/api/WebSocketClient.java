package com.cybexmobile.api;


import android.text.TextUtils;
import android.util.Log;

import com.cybexmobile.crypto.Sha256Object;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.FullNodeServerSelect;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.FullAccountObjectReply;
import com.cybexmobile.graphene.chain.GlobalConfigObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.LockUpAssetObject;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.market.MarketTrade;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.cybexmobile.constant.ErrorCode.ERROR_CONNECT_SERVER_INVALID;

public class WebSocketClient extends WebSocketListener {

    private static final String TAG = "WebSocketClient";

    //交易对比例
    private static final String CALL_GET_TICKER = "get_ticker";

    private volatile int _nDatabaseId = -1;
    private volatile int _nHistoryId = -1;
    private volatile int _nBroadcastId = -1;

    private static final String FLAG_DATABASE = "database";
    private static final String FLAG_HISTORY = "history";
    private static final String FLAG_BROADCAST = "broadcast";

    private OkHttpClient mOkHttpClient;
    private WebSocket mWebSocket;
    //websocket connect status
    private volatile int mConnectStatus = WEBSOCKET_CONNECT_CLOSED;
    private static int WEBSOCKET_CONNECT_FAIL = -2;
    private static int WEBSOCKET_CONNECT_CLOSED = -1;
    private static int WEBSOCKET_CONNECT_ING = 0;
    private static int WEBSOCKET_CONNECT_SUCCESS = 1;
    private static int WEBSOCKET_CONNECT_OK = 2;

    //call id
    private AtomicInteger mCallId = new AtomicInteger(1);
    private ConcurrentHashMap<Integer, ReplyProcessImpl> mHashMapIdToProcess = new ConcurrentHashMap<>();
    private List<DelayCall> delayCalls = null;
    //websocket node
    private List<String> mListNode = Arrays.asList(
            "wss://bitshares.openledger.info/ws",
            "wss://eu.openledger.info/ws",
            "wss://bit.btsabc.org/ws",
            "wss://bts.transwiser.com/ws",
            "wss://bitshares.dacplay.org/ws",
            "wss://bitshares-api.wancloud.io/ws",
            "wss://openledger.hk/ws",
            "wss://secure.freedomledger.com/ws",
            "wss://dexnode.net/ws",
            "wss://altcap.io/ws",
            "wss://bitshares.crypto.fans/ws"
    );

    public AtomicInteger getmCallId() {
        return mCallId;
    }

    public WebSocketClient(){
        delayCalls = Collections.synchronizedList(new LinkedList<>());
    }

    class WebsocketError {
        int code;
        String message;
        Object data;
    }

    class Call {
        int id;
        String method;
        List<Object> params;
    }

    class DelayCall{
        String flag;
        Call call;
        ReplyProcessImpl replyProcess;
        public DelayCall(String flag, Call call, ReplyProcessImpl replyProcess){
            this.flag = flag;
            this.call = call;
            this.replyProcess = replyProcess;
        }
    }

    public class Reply<T> {
        public String id;
        public String jsonrpc;
        public T result;
        public WebsocketError error;
    }

    class ReplyBase {
        int id;
        String jsonrpc;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.v(TAG, "onOpen: WebSocket is connected");
        mWebSocket = webSocket;
        mConnectStatus = WEBSOCKET_CONNECT_SUCCESS;
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
        Log.v(TAG, "onMessage: " + bytes.toString());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.d(TAG, "onMessage: " + text);
        try {
            Gson gson = new Gson();
            ReplyBase replyObjectBase = gson.fromJson(text, ReplyBase.class);
            IReplyProcess iReplyProcess = null;
            if (mHashMapIdToProcess.containsKey(replyObjectBase.id)) {
                iReplyProcess = mHashMapIdToProcess.get(replyObjectBase.id);
            }
            if (iReplyProcess != null) {
                iReplyProcess.processTextToObject(text);
                MessageCallback callback = iReplyProcess.getCallback();
                if(callback != null){
                    callback.onMessage(iReplyProcess.getReply());
                }
            } else {
                try {
                    JSONObject noticeObject = new JSONObject(text);
                    JSONArray params = noticeObject.getJSONArray("params");
                    int id = params.getInt(0);
                    EventBus.getDefault().post(String.valueOf(id));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        Log.v(TAG, "onFailure: WebSocket on failure", t);
        if(t instanceof SocketTimeoutException){
            //websocket连接超时
        }
        mConnectStatus = WEBSOCKET_CONNECT_FAIL;
        _nDatabaseId = -1;
        _nBroadcastId = -1;
        _nHistoryId = -1;
        mHashMapIdToProcess.clear();
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Log.v(TAG, "WebSocket is closing, code:" + code + " reason:" + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.v(TAG, "WebSocket is closed, code:" + code + " reason:" + reason);
        mConnectStatus = WEBSOCKET_CONNECT_CLOSED;
    }

    //连接WebSocket
    public void connect() {
        if(mConnectStatus == WEBSOCKET_CONNECT_ING
                || mConnectStatus == WEBSOCKET_CONNECT_SUCCESS
                || mConnectStatus == WEBSOCKET_CONNECT_OK){
            return;
        }
        FullNodeServerSelect fullNodeServerSelect = new FullNodeServerSelect();
        String strServer = fullNodeServerSelect.getServer();
        Log.v(TAG, strServer);
        if (TextUtils.isEmpty(strServer)) {
            mConnectStatus = ERROR_CONNECT_SERVER_INVALID;
            return;
        }
        Request request = new Request.Builder().url(strServer).build();
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newWebSocket(request, this);
        mConnectStatus = WEBSOCKET_CONNECT_ING;
    }

    //关闭WebSocket
    public void close() {
        mWebSocket.close(1000, "Close");
        mOkHttpClient = null;
        mWebSocket = null;
        mConnectStatus = WEBSOCKET_CONNECT_CLOSED;
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


    public void get_accounts(List<ObjectId<AccountObject>> listAccountObjectId, MessageCallback<Reply<List<AccountObject>>> callback) throws NetworkStatusException {
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

//    public List<OperationHistoryObject> get_account_history(ObjectId<AccountObject> accountId, int nLimit) throws NetworkStatusException {
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
//        ReplyProcessImpl<Reply<List<OperationHistoryObject>>> replyObject =
//                new ReplyProcessImpl<>(new TypeToken<Reply<List<OperationHistoryObject>>>(){}.getType());
//        Reply<List<OperationHistoryObject>> replyAccountHistory = sendForReply(callObject, replyObject);
//
//        return replyAccountHistory.result;
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

//    public dynamic_global_property_object get_dynamic_global_properties() throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_dynamic_global_properties");
//
//        callObject.params.add(new ArrayList<Object>());
//
//        ReplyProcessImpl<Reply<dynamic_global_property_object>> replyObjectProcess =
//                new ReplyProcessImpl<>(new TypeToken<Reply<dynamic_global_property_object>>(){}.getType());
//        Reply<dynamic_global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);
//
//        return replyObject.result;
//
//    }

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

        //return replyObject.result.get(0);
    }

    public void get_objects(List<String> objectIds, MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
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

        //return replyObject.result.get(0);
    }

//    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_block_header");
//        List<Object> listBlockNumber = new ArrayList<>();
//        listBlockNumber.add(nBlockNumber);
//        callObject.params.add(listBlockNumber);
//
//        ReplyProcessImpl<Reply<block_header>> replyObjectProcess =
//                new ReplyProcessImpl<>(new TypeToken<Reply<block_header>>(){}.getType());
//        Reply<block_header> replyObject = sendForReply(callObject, replyObjectProcess);
//
//        return replyObject.result;
//
//    }


//    public int broadcast_transaction(signed_transaction tx) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nBroadcastId);
//        callObject.params.add("broadcast_transaction");
//        List<Object> listTransaction = new ArrayList<>();
//        listTransaction.add(tx);
//        callObject.params.add(listTransaction);
//
//        ReplyProcessImpl<Reply<Object>> replyObjectProcess =
//                new ReplyProcessImpl<>(new TypeToken<Reply<Integer>>(){}.getType());
//        Reply<Object> replyObject = sendForReply(callObject, replyObjectProcess);
//        if (replyObject.error != null) {
//            return -1;
//        } else {
//            return 0;
//        }
//    }

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

    public void get_balance_objects(List<String> addresses, MessageCallback<Reply<List<LockUpAssetObject>>> callback) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_balance_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(addresses);
        callObject.params.add(listParams);

        ReplyProcessImpl<Reply<List<LockUpAssetObject>>> replyObjectProcess =
                new ReplyProcessImpl<>(new TypeToken<Reply<List<LockUpAssetObject>>>() {
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

//    public void set_subscribe_callback(boolean filter) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("set_subscribe_callback");
//
//        List<Object> listParams = new ArrayList<>();
//        listParams.add(callObject.id);
//        listParams.add(filter);
//        callObject.params.add(listParams);
//
//        ReplyProcessImpl<Reply<String>> replyObject =
//                new ReplyProcessImpl<>(new TypeToken<Reply<String>>() {
//                }.getType(), null);
//        sendForReplyImpl(FLAG_DATABASE, callObject, replyObject);
//    }

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



    private <T> void sendForReply(String flag, Call callObject, ReplyProcessImpl<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        if(mWebSocket != null && mConnectStatus == WEBSOCKET_CONNECT_OK){
            sendForReplyImpl(callObject, replyObjectProcess);
        }else {
            delayCalls.add(new DelayCall(flag, callObject, replyObjectProcess));
            connect();
        }
    }

    private <T> void sendForReplyImpl(Call call, ReplyProcessImpl<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(call);
        Log.v(TAG, strMessage);
        boolean result = mWebSocket.send(strMessage);
        if(result){
            mHashMapIdToProcess.put(call.id, replyObjectProcess);
        }
    }

    private void sendDelayForReply(){
        if(delayCalls == null || delayCalls.size() == 0){
            return;
        }
        Iterator<DelayCall> iterator = delayCalls.iterator();
        while (iterator.hasNext()){
            DelayCall delayCall = iterator.next();
            switch (delayCall.flag){
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

    private MessageCallback<Reply<Boolean>> loginCallback = new MessageCallback<Reply<Boolean>>() {
        @Override
        public void onMessage(Reply<Boolean> reply) {
            if(reply.result){
                try {
                    get_websocket_bitshares_api_id("database", databaseCallback);
                    get_websocket_bitshares_api_id("history", historyCallback);
                    get_websocket_bitshares_api_id("network_broadcast", broadcastCallback);
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
                mConnectStatus = WEBSOCKET_CONNECT_OK;
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
                mConnectStatus = WEBSOCKET_CONNECT_OK;
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
                mConnectStatus = WEBSOCKET_CONNECT_OK;
                sendDelayForReply();
            }
        }

        @Override
        public void onFailure() {
            _nBroadcastId = -1;
        }
    };

    public interface MessageCallback<T>{
        void onMessage(T reply);

        void onFailure();
    }

}

