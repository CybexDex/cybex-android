package com.cybexmobile.api;


import android.util.Log;

import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BlockHeader;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.DynamicGlobalPropertyObject;
import com.cybexmobile.graphene.chain.FeeAmountObject;
import com.cybexmobile.graphene.chain.FullAccountObjectReply;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.LockUpAssetObject;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.graphene.chain.AccountHistoryObject;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.graphene.chain.PrivateKey;
import com.cybexmobile.graphene.chain.SignedTransaction;
import com.cybexmobile.graphene.chain.Types;
import com.cybexmobile.market.MarketTicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BitsharesWalletWraper {

    private static BitsharesWalletWraper bitsharesWalletWraper = new BitsharesWalletWraper();
    private WalletApi mWalletApi = new WalletApi();
    private Map<ObjectId<AccountObject>, AccountObject> mMapAccountId2Object = new ConcurrentHashMap<>();
    private Map<ObjectId<AccountObject>, List<Asset>> mMapAccountId2Asset = new ConcurrentHashMap<>();
    private Map<ObjectId<AssetObject>, AssetObject> mMapAssetId2Object = new ConcurrentHashMap<>();
    private String mstrWalletFilePath;
    private List<ObjectId<AssetObject>> mObjectList = new ArrayList<>();
    private List<String> addressList = new ArrayList<>();
    private String password;

    private BitshareData mBitshareData;

    private BitsharesWalletWraper() {
        //mstrWalletFilePath = BitsharesApplication.getInstance().getFilesDir().getPath();
        mstrWalletFilePath += "/wallet.json";
    }

    public static BitsharesWalletWraper getInstance() {
        return bitsharesWalletWraper;
    }

    public void reset() {
        mWalletApi.reset();
//        mWalletApi = new WalletApi();
        mMapAccountId2Object.clear();
        mMapAccountId2Asset.clear();
        mMapAssetId2Object.clear();

        File file = new File(mstrWalletFilePath);
        file.delete();
    }

    public AccountObject get_account() {
        List<AccountObject> listAccount = mWalletApi.list_my_accounts();
        if (listAccount == null || listAccount.isEmpty()) {
            return null;
        }

        return listAccount.get(0);
    }

    public boolean is_new() {
        return mWalletApi.is_new();
    }

    public boolean is_locked() {
//        return mWalletApi.is_locked();
        return password == null;
    }

    public int load_wallet_file() {
        return mWalletApi.load_wallet_file(mstrWalletFilePath);
    }

    private int save_wallet_file() {
        return mWalletApi.save_wallet_file(mstrWalletFilePath);
    }

    public void build_connect() {
        mWalletApi.initialize();
    }


    public List<AccountObject> list_my_accounts() {
        return mWalletApi.list_my_accounts();
    }

//    public int import_key(String strAccountNameOrId,
//                          String strPassword,
//                          String strPrivateKey) {
//
//        mWalletApi.set_password(strPassword);
//
//        try {
//            int nRet = mWalletApi.import_key(strAccountNameOrId, strPrivateKey);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//        save_wallet_file();
//
//        for (AccountObject accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//    }

//    public int import_keys(String strAccountNameOrId,
//                           String strPassword,
//                           String strPrivateKey1,
//                           String strPrivateKey2) {
//
//        mWalletApi.set_password(strPassword);
//
//        try {
//            int nRet = mWalletApi.import_keys(strAccountNameOrId, strPrivateKey1, strPrivateKey2);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//        save_wallet_file();
//
//        for (AccountObject accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//    }

//    public int import_brain_key(String strAccountNameOrId,
//                                String strPassword,
//                                String strBrainKey) {
//        mWalletApi.set_password(strPassword);
//        try {
//            int nRet = mWalletApi.import_brain_key(strAccountNameOrId, strBrainKey);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return ErrorCode.ERROR_IMPORT_NETWORK_FAIL;
//        }
//
//        save_wallet_file();
//
//        for (AccountObject accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//    }

//    public int import_file_bin(String strPassword,
//                               String strFilePath) {
//        File file = new File(strFilePath);
//        if (file.exists() == false) {
//            return ErrorCode.ERROR_FILE_NOT_FOUND;
//        }
//
//        int nSize = (int)file.length();
//
//        final byte[] byteContent = new byte[nSize];
//
//        FileInputStream fileInputStream;
//        try {
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(byteContent, 0, byteContent.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return ErrorCode.ERROR_FILE_NOT_FOUND;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ErrorCode.ERROR_FILE_READ_FAIL;
//        }
//
//        WalletBackup walletBackup = FileBin.deserializeWalletBackup(byteContent, strPassword);
//        if (walletBackup == null) {
//            return ErrorCode.ERROR_FILE_BIN_PASSWORD_INVALID;
//        }
//
//        String strBrainKey = walletBackup.getWallet(0).decryptBrainKey(strPassword);
//        //LinkedAccount linkedAccount = walletBackup.getLinkedAccounts()[0];
//
//        int nRet = ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
//        for (LinkedAccount linkedAccount : walletBackup.getLinkedAccounts()) {
//            nRet = import_brain_key(linkedAccount.getName(), strPassword, strBrainKey);
//            if (nRet == 0) {
//                break;
//            }
//        }
//
//        return nRet;
//    }

    public int import_account_password(AccountObject accountObject, String strAccountName, String strPassword) {
        mWalletApi.set_password(strPassword);
        int nRet = mWalletApi.import_account_password(accountObject, strAccountName, strPassword);
        if (nRet == 0) {
//            save_wallet_file();
            for (AccountObject account : list_my_accounts()) {
                mMapAccountId2Object.put(account.id, account);
            }
            password = strPassword;
            lockWallet();
        }
        return nRet;

    }

    public int unlock(String strPassword) {
        return mWalletApi.unlock(strPassword);
    }

    public int lock() {
        return mWalletApi.lock();
    }

    public void lockWallet() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                password = null;
            }
        }, 360 * 1000, 360 * 1000);
    }

//    public List<Asset> list_balances(boolean bRefresh) throws NetworkStatusException {
//        List<Asset> listAllAsset = new ArrayList<>();
//        for (AccountObject accountObject : list_my_accounts()) {
//            List<Asset> listAsset = list_account_balance(accountObject.id, bRefresh);
//
//            listAllAsset.addAll(listAsset);
//        }
//
//        return listAllAsset;
//    }

//    public List<Asset> list_account_balance(ObjectId<AccountObject> accountObjectId,
//                                            boolean bRefresh) throws NetworkStatusException {
//        List<Asset> listAsset = mMapAccountId2Asset.get(accountObjectId);
//        if (bRefresh || listAsset == null) {
//            listAsset = mWalletApi.list_account_balance(accountObjectId);
//            mMapAccountId2Asset.put(accountObjectId, listAsset);
//        }
//
//        return listAsset;
//    }

//    public List<AccountHistoryObject> get_history(boolean bRefresh) throws NetworkStatusException {
//        List<AccountHistoryObject> listAllHistoryObject = new ArrayList<>();
//        for (AccountObject accountObject : list_my_accounts()) {
//            List<AccountHistoryObject> listHistoryObject = get_account_history(
//                    accountObject.id,
//                    100,
//                    bRefresh
//            );
//
//            listAllHistoryObject.addAll(listHistoryObject);
//        }
//
//        return listAllHistoryObject;
//    }

    public void get_account_history(ObjectId<AccountObject> accountObjectId,
                                    int nLimit,
                                    WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AccountHistoryObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_account_history(accountObjectId, nLimit, callback);
    }

//    public List<AssetObject> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
//        return mWalletApi.list_assets(strLowerBound, nLimit);
//    }

//    public Map<ObjectId<AssetObject>, AssetObject> get_assets(List<ObjectId<AssetObject>> listAssetObjectId) throws NetworkStatusException {
//        Map<ObjectId<AssetObject>, AssetObject> mapId2Object = new HashMap<>();
//
//        List<ObjectId<AssetObject>> listRequestId = new ArrayList<>();
//        for (ObjectId<AssetObject> objectId : listAssetObjectId) {
//            AssetObject assetObject = mMapAssetId2Object.get(objectId);
//            if (assetObject != null) {
//                mapId2Object.put(objectId, assetObject);
//            } else {
//                listRequestId.add(objectId);
//            }
//        }
//
//        if (listRequestId.isEmpty() == false) {
//            List<AssetObject> listAssetObject = mWalletApi.get_assets(listRequestId);
//            for (AssetObject assetObject : listAssetObject) {
//                mapId2Object.put(assetObject.id, assetObject);
//                mMapAssetId2Object.put(assetObject.id, assetObject);
//            }
//        }
//
//        return mapId2Object;
//    }

    public void lookup_asset_symbols(String strAssetSymbol, WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.lookup_asset_symbols(strAssetSymbol, callback);
    }

    //get asset detail
    public void get_objects(String objectId, WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_objects(objectId, callback);
    }

    public void get_objects(List<String> objectIds, WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_objects(objectIds, callback);
    }

//    public Map<ObjectId<AccountObject>, AccountObject> get_accounts(List<ObjectId<AccountObject>> listAccountObjectId) throws NetworkStatusException {
//        Map<ObjectId<AccountObject>, AccountObject> mapId2Object = new HashMap<>();
//
//        List<ObjectId<AccountObject>> listRequestId = new ArrayList<>();
//        for (ObjectId<AccountObject> objectId : listAccountObjectId) {
//            AccountObject accountObject = mMapAccountId2Object.get(objectId);
//            if (accountObject != null) {
//                mapId2Object.put(objectId, accountObject);
//            } else {
//                listRequestId.add(objectId);
//            }
//        }
//
//        if (listRequestId.isEmpty() == false) {
//            List<AccountObject> listAccountObject = mWalletApi.get_accounts(listRequestId);
//            for (AccountObject accountObject : listAccountObject) {
//                mapId2Object.put(accountObject.id, accountObject);
//                mMapAccountId2Object.put(accountObject.id, accountObject);
//            }
//        }
//
//        return mapId2Object;
//    }

    public void get_block(int callId, int blockNumber, WebSocketClient.MessageCallback<WebSocketClient.Reply<BlockHeader>> callback) throws NetworkStatusException {
        mWalletApi.get_block(callId, blockNumber, callback);
    }

    public Operations.transfer_operation getTransferOperation(ObjectId<AccountObject> from,
                                                              ObjectId<AccountObject> to,
                                                              AssetObject assetObject,
                                                              long feeAmount,
                                                              String feeAssetId,
                                                              String amount,
                                                              String memo,
                                                              Types.public_key_type fromMemoKey,
                                                              Types.public_key_type toMemokey) {
        return mWalletApi.getTransferOperation(from, to, assetObject, feeAmount, feeAssetId, amount, memo, fromMemoKey, toMemokey);
    }

    public Operations.limit_order_create_operation getLimitOrderCreateOperation(ObjectId<AccountObject> accountId,
                                                                                ObjectId<AssetObject> assetFeeId,
                                                                                ObjectId<AssetObject> assetSellId,
                                                                                ObjectId<AssetObject> assetReceiveId,
                                                                                long amountFee,
                                                                                long amountSell,
                                                                                long amountReceive){
        return mWalletApi.getLimitOrderCreateOperation(accountId, assetFeeId, assetSellId, assetReceiveId, amountFee, amountSell, amountReceive);
    }

    public Operations.limit_order_cancel_operation getLimitOrderCancelOperation(ObjectId<AccountObject> accountId,
                                                                                ObjectId<AssetObject> assetFeeId,
                                                                                ObjectId<LimitOrderObject> limitOrderId,
                                                                                long amountFee){
        return mWalletApi.getLimitOrderCancelOperation(accountId, assetFeeId, limitOrderId, amountFee);
    }

    public SignedTransaction getSignedTransaction(AccountObject accountObject, Operations.base_operation operation, int operationId, DynamicGlobalPropertyObject dynamicGlobalPropertyObject) {

        return mWalletApi.getSignedTransaction(accountObject, operation, operationId, dynamicGlobalPropertyObject);
    }

//    public signed_transaction transfer(String strFrom,
//                                       String strTo,
//                                       String strAmount,
//                                       String strAssetSymbol,
//                                       String strMemo) throws NetworkStatusException {
//        signed_transaction signedTransaction = mWalletApi.transfer(
//                strFrom,
//                strTo,
//                strAmount,
//                strAssetSymbol,
//                strMemo
//        );
//        return signedTransaction;
//    }

//    public BitshareData prepare_data_to_display(boolean bRefresh) {
//        try {
//            List<Asset> listBalances = BitsharesWalletWraper.getInstance().list_balances(bRefresh);
//
//            List<AccountHistoryObject> operationHistoryObjectList = BitsharesWalletWraper.getInstance().get_history(bRefresh);
//            HashSet<ObjectId<AccountObject>> hashSetObjectId = new HashSet<ObjectId<AccountObject>>();
//            HashSet<ObjectId<AssetObject>> hashSetAssetObject = new HashSet<ObjectId<AssetObject>>();
//
//            List<Pair<AccountHistoryObject, Date>> listHistoryObjectTime = new ArrayList<Pair<AccountHistoryObject, Date>>();
//            for (AccountHistoryObject historyObject : operationHistoryObjectList) {
//                block_header blockHeader = BitsharesWalletWraper.getInstance().get_block_header(historyObject.block_num);
//                listHistoryObjectTime.add(new Pair<>(historyObject, blockHeader.timestamp));
//                if (historyObject.op.nOperationType <= Operations.ID_CREATE_ACCOUNT_OPERATION) {
//                    Operations.base_operation operation = (Operations.base_operation)historyObject.op.operationContent;
//                    hashSetObjectId.addAll(operation.get_account_id_list());
//                    hashSetAssetObject.addAll(operation.get_asset_id_list());
//                }
//            }
//
//            // 保证默认数据一直存在
//            hashSetAssetObject.add(new ObjectId<AssetObject>(0, AssetObject.class));
//
//            //// TODO: 06/09/2017 这里需要优化到一次调用
//
//            for (Asset assetBalances : listBalances) {
//                hashSetAssetObject.add(assetBalances.asset_id);
//            }
//
//            List<ObjectId<AccountObject>> listAccountObjectId = new ArrayList<ObjectId<AccountObject>>();
//            listAccountObjectId.addAll(hashSetObjectId);
//            Map<ObjectId<AccountObject>, AccountObject> mapId2AccountObject =
//                    BitsharesWalletWraper.getInstance().get_accounts(listAccountObjectId);
//
//
//            List<ObjectId<AssetObject>> listAssetObjectId = new ArrayList<ObjectId<AssetObject>>();
//            listAssetObjectId.addAll(hashSetAssetObject);
//
//            // 生成id 2 asset_object映身
//            Map<ObjectId<AssetObject>, AssetObject> mapId2AssetObject =
//                    BitsharesWalletWraper.getInstance().get_assets(listAssetObjectId);
//
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
//            String strCurrencySetting = prefs.getString("currency_setting", "USD");
//
//            AssetObject currencyObject = mWalletApi.list_assets(strCurrencySetting, 1).get(0);
//            mapId2AssetObject.put(currencyObject.id, currencyObject);
//
//            hashSetAssetObject.add(currencyObject.id);
//
//            listAssetObjectId.clear();
//            listAssetObjectId.addAll(hashSetAssetObject);
//
//            Map<ObjectId<AssetObject>, BucketObject> mapAssetId2Bucket = get_market_histories_base(listAssetObjectId);
//
//            mBitshareData = new BitshareData();
//            mBitshareData.assetObjectCurrency = currencyObject;
//            mBitshareData.listBalances = listBalances;
//            mBitshareData.listHistoryObject = listHistoryObjectTime;
//            mBitshareData.mapId2AssetObject = mapId2AssetObject;
//            //mBitshareData.mapId2AccountObject = mapId2AccountObject;
//            mBitshareData.mapAssetId2Bucket = mapAssetId2Bucket;
//
//            return mBitshareData;
//
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    // 获取对于基础货币的所有市场价格
//    public Map<ObjectId<AssetObject>, BucketObject> get_market_histories_base(List<ObjectId<AssetObject>> listAssetObjectId) throws NetworkStatusException {
//        dynamic_global_property_object dynamicGlobalPropertyObject = mWalletApi.get_dynamic_global_properties();
//
//        Date dateObject = dynamicGlobalPropertyObject.time;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(dateObject);
//        calendar.add(Calendar.HOUR, -12);
//
//        Date dateObjectStart = calendar.getTime();
//
//        calendar.setTime(dateObject);
//        calendar.add(Calendar.SECOND, 30);
//
//        Date dateObjectEnd = calendar.getTime();
//
//        Map<ObjectId<AssetObject>, BucketObject> mapId2BucketObject = new HashMap<>();
//
//        ObjectId<AssetObject> assetObjectBase = new ObjectId<AssetObject>(0, AssetObject.class);
//        for (ObjectId<AssetObject> objectId : listAssetObjectId) {
//            if (objectId.equals(assetObjectBase)) {
//                continue;
//            }
//            List<BucketObject> listBucketObject = mWalletApi.get_market_history(
//                    objectId,
//                    assetObjectBase,
//                    3600,
//                    dateObjectStart,
//                    dateObjectEnd
//            );
//
//            if (listBucketObject.isEmpty() == false) {
//                BucketObject bucketObject = listBucketObject.get(listBucketObject.size() - 1);
//                mapId2BucketObject.put(objectId, bucketObject);
//            }
//        }
//
//        return mapId2BucketObject;
//    }

    public void get_market_history(ObjectId<AssetObject> baseAssetId,
                                   ObjectId<AssetObject> quoteAssetId,
                                   int nBucket, Date dateStart, Date dateEnd,
                                   WebSocketClient.MessageCallback<WebSocketClient.Reply<List<BucketObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_market_history(baseAssetId, quoteAssetId, nBucket, dateStart, dateEnd, callback);
    }

    public void subscribe_to_market(String id, String base, String quote, WebSocketClient.MessageCallback<WebSocketClient.Reply<String>> callback) throws NetworkStatusException {
        mWalletApi.subscribe_to_market(id, base, quote, callback);
    }

    public AtomicInteger get_call_id() {
        return mWalletApi.getCallId();
    }

//    public void set_subscribe_market(boolean filter) throws NetworkStatusException {
//        mWalletApi.set_subscribe_market(filter);
//    }

    //
    public void get_ticker(String base, String quote, WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>> callback) throws NetworkStatusException {
        mWalletApi.get_ticker(base, quote, callback);
    }

//    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
//            throws NetworkStatusException {
//        return mWalletApi.get_trade_history(base, quote, start, end, limit);
//    }

    public void get_fill_order_history(ObjectId<AssetObject> base,
                                       ObjectId<AssetObject> quote,
                                       int limit, WebSocketClient.MessageCallback<WebSocketClient.Reply<List<HashMap<String, Object>>>> callback) throws NetworkStatusException {
        mWalletApi.get_fill_order_history(base, quote, limit, callback);
    }

    public void get_limit_orders(ObjectId<AssetObject> base,
                                 ObjectId<AssetObject> quote,
                                 int limit, WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LimitOrderObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_limit_orders(base, quote, limit, callback);
    }

    public void get_balance_objects(List<String> addresses, WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LockUpAssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_balance_objects(addresses, callback);
    }

//    public signed_transaction sell_asset(String amountToSell, String symbolToSell,
//                                         String minToReceive, String symbolToReceive,
//                                         int timeoutSecs, boolean fillOrKill)
//            throws NetworkStatusException {
//        return mWalletApi.sell_asset(amountToSell, symbolToSell, minToReceive, symbolToReceive,
//                timeoutSecs, fillOrKill);
//    }

//    public Asset calculate_sell_fee(AssetObject assetToSell, AssetObject assetToReceive,
//                                    double rate, double amount,
//                                    global_property_object globalPropertyObject) {
//        return mWalletApi.calculate_sell_fee(assetToSell, assetToReceive, rate, amount,
//                globalPropertyObject);
//    }

//    public Asset calculate_buy_fee(AssetObject assetToReceive, AssetObject assetToSell,
//                                   double rate, double amount,
//                                   global_property_object globalPropertyObject) {
//        return mWalletApi.calculate_buy_fee(assetToReceive, assetToSell, rate, amount,
//                globalPropertyObject);
//    }

//    public signed_transaction sell(String base, String quote, double rate, double amount)
//            throws NetworkStatusException {
//        return mWalletApi.sell(base, quote, rate, amount);
//    }
//
//    public signed_transaction sell(String base, String quote, double rate, double amount,
//                                   int timeoutSecs) throws NetworkStatusException {
//        return mWalletApi.sell(base, quote, rate, amount, timeoutSecs);
//    }
//
//    public signed_transaction buy(String base, String quote, double rate, double amount)
//            throws NetworkStatusException {
//        return mWalletApi.buy(base, quote, rate, amount);
//    }
//
//    public signed_transaction buy(String base, String quote, double rate, double amount,
//                                  int timeoutSecs) throws NetworkStatusException {
//        return mWalletApi.buy(base, quote, rate, amount, timeoutSecs);
//    }

//    public BitshareData getBitshareData() {
//        return mBitshareData;
//    }

    public void get_account_object(String strAccount, WebSocketClient.MessageCallback<WebSocketClient.Reply<AccountObject>> callback) throws NetworkStatusException {
        mWalletApi.get_account(strAccount, callback);
    }

//    public Asset transfer_calculate_fee(String strAmount,
//                                        String strAssetSymbol,
//                                        String strMemo) throws NetworkStatusException {
//        return mWalletApi.transfer_calculate_fee(strAmount, strAssetSymbol, strMemo);
//    }

//    public String get_plain_text_message(memo_data memoData) {
//        return mWalletApi.decrypt_memo_message(memoData);
//    }

    public void get_full_accounts(List<String> names, boolean subscribe,
                                  WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FullAccountObjectReply>>> callback) throws NetworkStatusException {
        mWalletApi.get_full_accounts(names, subscribe, callback);
    }

    public void get_required_fees(String assetId, int operationId, Operations.base_operation operation,
                                  WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FeeAmountObject>>> callback)
            throws NetworkStatusException {
        mWalletApi.get_required_fees(assetId, operationId, operation, callback);
    }

    public void broadcast_transaction_with_callback(SignedTransaction signedTransaction, WebSocketClient.MessageCallback<WebSocketClient.Reply<String>> callback) throws NetworkStatusException {
        mWalletApi.broadcast_transaction_with_callback(signedTransaction, callback);
    }


    //Todo: add asset_object_to_id_map
//    public List<ObjectId<AssetObject>> getObjectList() {
//        if (mObjectList.size() == 0) {
//            mFullAccountObjects.get(0).balances
//        }
//        return mObjectList;
//    }

//    public signed_transaction cancel_order(ObjectId<LimitOrderObject> id)
//            throws NetworkStatusException {
//        return mWalletApi.cancel_order(id);
//    }
//

    public void get_dynamic_global_properties(WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>> callback) throws NetworkStatusException {
        mWalletApi.get_dynamic_global_properties(callback);
    }

    private List<String> getAddressesForLockAsset(String strAccountName, String strPassword) {
        PrivateKey privateActiveKey = PrivateKey.from_seed(strAccountName + "active" + strPassword);
        PrivateKey privateOwnerKey = PrivateKey.from_seed(strAccountName + "owner" + strPassword);
        PrivateKey privateMemoKey = PrivateKey.from_seed(strAccountName + "memo" + strPassword);

        Types.public_key_type publicActiveKeyType = new Types.public_key_type(privateActiveKey.get_public_key(true), true);
        Types.public_key_type publicOwnerKeyType = new Types.public_key_type(privateOwnerKey.get_public_key(true), true);
        Types.public_key_type publicMemoKeyType = new Types.public_key_type(privateMemoKey.get_public_key(true), true);

        Types.public_key_type publicActiveKeyTypeUnCompressed = new Types.public_key_type(privateActiveKey.get_public_key(false), false);
        Types.public_key_type publicOwnerKeyTypeUnCompressed = new Types.public_key_type(privateOwnerKey.get_public_key(false), false);
        Types.public_key_type publicMemoKeyTypeUnCompressed = new Types.public_key_type(privateMemoKey.get_public_key(false), false);

        String address = publicActiveKeyType.getAddress();
        addressList.add(address);
        String ownerAddress = publicOwnerKeyType.getAddress();
        addressList.add(ownerAddress);
        String memoAddress = publicMemoKeyType.getAddress();
        addressList.add(memoAddress);
        String PTSAddress = publicActiveKeyType.getPTSAddress(publicActiveKeyType.key_data);
        addressList.add(PTSAddress);
        String owerPtsAddress = publicOwnerKeyType.getPTSAddress(publicOwnerKeyType.key_data);
        addressList.add(owerPtsAddress);
        String memoPtsAddress = publicMemoKeyType.getPTSAddress(publicMemoKeyType.key_data);
        addressList.add(memoPtsAddress);
        String uncompresedPts = publicActiveKeyTypeUnCompressed.getPTSAddress(publicActiveKeyTypeUnCompressed.key_data_uncompressed);
        addressList.add(uncompresedPts);
        String unCompressedOwnerKey = publicOwnerKeyTypeUnCompressed.getPTSAddress(publicOwnerKeyTypeUnCompressed.key_data_uncompressed);
        addressList.add(unCompressedOwnerKey);
        String unCompressedMemo = publicMemoKeyTypeUnCompressed.getPTSAddress(publicMemoKeyTypeUnCompressed.key_data_uncompressed);
        addressList.add(unCompressedMemo);
        Log.e("Address", address);
        Log.e("OwnerAddress", ownerAddress);
        Log.e("ActivePTSAddress", PTSAddress);
        Log.e("OwnerPtsAddress", owerPtsAddress);
        Log.e("MemoAddress", memoAddress);
        Log.e("MemoPTSAddress", memoPtsAddress);
        Log.e("uncompressedActive", uncompresedPts);
        Log.e("uncompressedOwner", unCompressedOwnerKey);
        Log.e("uncompressedMemo", unCompressedMemo);
        return addressList;
    }

    public List<String> getAddressList(String userName, String passWord) {
        if (addressList.size() != 0) {
            return addressList;
        } else {
            return getAddressesForLockAsset(userName, passWord);
        }
    }

    public String getPassword() {
        return password;
    }
}
