package com.cybexmobile.api;


import com.cybexmobile.constant.ErrorCode;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.faucet.CreateAccountException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.graphene.chain.OperationHistoryObject;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.market.MarketTrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitsharesWalletWraper {

    private static BitsharesWalletWraper bitsharesWalletWraper = new BitsharesWalletWraper();
    private WalletApi mWalletApi = new WalletApi();
    private Map<ObjectId<AccountObject>, AccountObject> mMapAccountId2Object = new ConcurrentHashMap<>();
    private Map<ObjectId<AccountObject>, List<Asset>> mMapAccountId2Asset = new ConcurrentHashMap<>();
    private Map<ObjectId<AccountObject>, List<OperationHistoryObject>> mMapAccountId2History = new ConcurrentHashMap<>();
    private Map<ObjectId<AssetObject>, AssetObject> mMapAssetId2Object = new ConcurrentHashMap<>();
    private String mstrWalletFilePath;
    private List<FullAccountObject> mFullAccountObjects = new ArrayList<>();
    private List<ObjectId<AssetObject>> mObjectList = new ArrayList<>();

    private int mnStatus = STATUS_INVALID;

    private static final int STATUS_INVALID = -1;
    private static final int STATUS_INITIALIZED = 0;

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
        mWalletApi = new WalletApi();
        mMapAccountId2Object.clear();
        ;
        mMapAccountId2Asset.clear();
        ;
        mMapAccountId2History.clear();
        mMapAssetId2Object.clear();
        ;

        File file = new File(mstrWalletFilePath);
        file.delete();

        mnStatus = STATUS_INVALID;
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

    public  boolean is_locked() {
        return mWalletApi.is_locked();
    }

    public int load_wallet_file() {
        return mWalletApi.load_wallet_file(mstrWalletFilePath);
    }

    private int save_wallet_file() {
        return mWalletApi.save_wallet_file(mstrWalletFilePath);
    }

    public synchronized int build_connect() {
        if (mnStatus == STATUS_INITIALIZED) {
            return 0;
        }

        int nRet = mWalletApi.initialize();
        if (nRet != 0) {
            return nRet;
        }

        mnStatus = STATUS_INITIALIZED;
        return 0;
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

    public int import_account_password(String strAccountName,
                                       String strPassword) {
        mWalletApi.set_password(strPassword);
        try {
            int nRet = mWalletApi.import_account_password(strAccountName, strPassword);
            if (nRet != 0) {
                return nRet;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return -1;
        }

        save_wallet_file();

        for (AccountObject accountObject : list_my_accounts()) {
            mMapAccountId2Object.put(accountObject.id, accountObject);
        }

        return 0;

    }

//    public int unlock(String strPassword) {
//        return mWalletApi.unlock(strPassword);
//    }

//    public int lock() {
//        return mWalletApi.lock();
//    }

    public List<Asset> list_balances(boolean bRefresh) throws NetworkStatusException {
        List<Asset> listAllAsset = new ArrayList<>();
        for (AccountObject accountObject : list_my_accounts()) {
            List<Asset> listAsset = list_account_balance(accountObject.id, bRefresh);

            listAllAsset.addAll(listAsset);
        }

        return listAllAsset;
    }

    public List<Asset> list_account_balance(ObjectId<AccountObject> accountObjectId,
                                            boolean bRefresh) throws NetworkStatusException {
        List<Asset> listAsset = mMapAccountId2Asset.get(accountObjectId);
        if (bRefresh || listAsset == null) {
            listAsset = mWalletApi.list_account_balance(accountObjectId);
            mMapAccountId2Asset.put(accountObjectId, listAsset);
        }

        return listAsset;
    }

//    public List<OperationHistoryObject> get_history(boolean bRefresh) throws NetworkStatusException {
//        List<OperationHistoryObject> listAllHistoryObject = new ArrayList<>();
//        for (AccountObject accountObject : list_my_accounts()) {
//            List<OperationHistoryObject> listHistoryObject = get_account_history(
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

//    public List<OperationHistoryObject> get_account_history(ObjectId<AccountObject> accountObjectId,
//                                                              int nLimit,
//                                                              boolean bRefresh) throws NetworkStatusException {
//        List<OperationHistoryObject> listHistoryObject = mMapAccountId2History.get(accountObjectId);
//        if (listHistoryObject == null || bRefresh) {
//            listHistoryObject = mWalletApi.get_account_history(accountObjectId, nLimit);
//            mMapAccountId2History.put(accountObjectId, listHistoryObject);
//        }
//        return listHistoryObject;
//    }

    public List<AssetObject> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
        return mWalletApi.list_assets(strLowerBound, nLimit);
    }

    public Map<ObjectId<AssetObject>, AssetObject> get_assets(List<ObjectId<AssetObject>> listAssetObjectId) throws NetworkStatusException {
        Map<ObjectId<AssetObject>, AssetObject> mapId2Object = new HashMap<>();

        List<ObjectId<AssetObject>> listRequestId = new ArrayList<>();
        for (ObjectId<AssetObject> objectId : listAssetObjectId) {
            AssetObject assetObject = mMapAssetId2Object.get(objectId);
            if (assetObject != null) {
                mapId2Object.put(objectId, assetObject);
            } else {
                listRequestId.add(objectId);
            }
        }

        if (listRequestId.isEmpty() == false) {
            List<AssetObject> listAssetObject = mWalletApi.get_assets(listRequestId);
            for (AssetObject assetObject : listAssetObject) {
                mapId2Object.put(assetObject.id, assetObject);
                mMapAssetId2Object.put(assetObject.id, assetObject);
            }
        }

        return mapId2Object;
    }

    public AssetObject lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        return mWalletApi.lookup_asset_symbols(strAssetSymbol);
    }

    public AssetObject get_objects(String objectId) throws NetworkStatusException {
        return mWalletApi.get_objects(objectId);
    }

    public Map<ObjectId<AccountObject>, AccountObject> get_accounts(List<ObjectId<AccountObject>> listAccountObjectId) throws NetworkStatusException {
        Map<ObjectId<AccountObject>, AccountObject> mapId2Object = new HashMap<>();

        List<ObjectId<AccountObject>> listRequestId = new ArrayList<>();
        for (ObjectId<AccountObject> objectId : listAccountObjectId) {
            AccountObject accountObject = mMapAccountId2Object.get(objectId);
            if (accountObject != null) {
                mapId2Object.put(objectId, accountObject);
            } else {
                listRequestId.add(objectId);
            }
        }

        if (listRequestId.isEmpty() == false) {
            List<AccountObject> listAccountObject = mWalletApi.get_accounts(listRequestId);
            for (AccountObject accountObject : listAccountObject) {
                mapId2Object.put(accountObject.id, accountObject);
                mMapAccountId2Object.put(accountObject.id, accountObject);
            }
        }

        return mapId2Object;
    }

//    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
//        return mWalletApi.get_block_header(nBlockNumber);
//    }

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
//            List<OperationHistoryObject> operationHistoryObjectList = BitsharesWalletWraper.getInstance().get_history(bRefresh);
//            HashSet<ObjectId<AccountObject>> hashSetObjectId = new HashSet<ObjectId<AccountObject>>();
//            HashSet<ObjectId<AssetObject>> hashSetAssetObject = new HashSet<ObjectId<AssetObject>>();
//
//            List<Pair<OperationHistoryObject, Date>> listHistoryObjectTime = new ArrayList<Pair<OperationHistoryObject, Date>>();
//            for (OperationHistoryObject historyObject : operationHistoryObjectList) {
//                block_header blockHeader = BitsharesWalletWraper.getInstance().get_block_header(historyObject.block_num);
//                listHistoryObjectTime.add(new Pair<>(historyObject, blockHeader.timestamp));
//                if (historyObject.op.nOperationType <= operations.ID_CREATE_ACCOUNT_OPERATION) {
//                    operations.base_operation operation = (operations.base_operation)historyObject.op.operationContent;
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

    public List<BucketObject> get_market_history(ObjectId<AssetObject> assetObjectId1,
                                                 ObjectId<AssetObject> assetObjectId2,
                                                 int nBucket, Date dateStart,
                                                 Date dateEnd) throws NetworkStatusException {
        return mWalletApi.get_market_history(
                assetObjectId1, assetObjectId2, nBucket, dateStart, dateEnd);
    }

    public String subscribe_to_market(String base, String quote) throws NetworkStatusException {
        return mWalletApi.subscribe_to_market(base, quote);
    }

    public void set_subscribe_market(boolean filter) throws NetworkStatusException {
        mWalletApi.set_subscribe_market(filter);
    }

    public MarketTicker get_ticker(String base, String quote) throws NetworkStatusException {
        return mWalletApi.get_ticker(base, quote);
    }

    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
            throws NetworkStatusException {
        return mWalletApi.get_trade_history(base, quote, start, end, limit);
    }

    public List<HashMap<String, Object>> get_fill_order_history(ObjectId<AssetObject> base,
                                                                ObjectId<AssetObject> quote,
                                                                int limit) throws NetworkStatusException {
        return mWalletApi.get_fill_order_history(base, quote, limit);
    }

    public List<LimitOrderObject> get_limit_orders(ObjectId<AssetObject> base,
                                                   ObjectId<AssetObject> quote,
                                                   int limit) throws NetworkStatusException {
        return mWalletApi.get_limit_orders(base, quote, limit);
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

    public BitshareData getBitshareData() {
        return mBitshareData;
    }

    public AccountObject get_account_object(String strAccount) throws NetworkStatusException {
        return mWalletApi.get_account(strAccount);
    }

//    public Asset transfer_calculate_fee(String strAmount,
//                                        String strAssetSymbol,
//                                        String strMemo) throws NetworkStatusException {
//        return mWalletApi.transfer_calculate_fee(strAmount, strAssetSymbol, strMemo);
//    }

//    public String get_plain_text_message(memo_data memoData) {
//        return mWalletApi.decrypt_memo_message(memoData);
//    }

    public List<FullAccountObject> get_full_accounts(List<String> names, boolean subscribe)
            throws NetworkStatusException {
        mFullAccountObjects.addAll(mWalletApi.get_full_accounts(names, subscribe));
        return mFullAccountObjects;
    }

    public List<FullAccountObject> getMyFullAccountInstance() {
        return mFullAccountObjects;
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
//    public global_property_object get_global_properties() throws NetworkStatusException {
//        return mWalletApi.get_global_properties();
//    }

    public int create_account_with_password(String strAccountName,
                                            String strPassword, String pinCode, String capId) throws CreateAccountException {
        try {
            return mWalletApi.create_account_with_password(strAccountName, strPassword, pinCode, capId);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            return ErrorCode.ERROR_NETWORK_FAIL;
        }
    }
}
