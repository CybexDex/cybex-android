package com.cybex.basemodule;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alibaba.android.arouter.utils.MapUtils;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.Asset;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.Authority;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybex.provider.graphene.chain.BucketObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.FullAccountObjectReply;
import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.graphene.chain.LimitOrderObject;
import com.cybex.provider.graphene.chain.LockAssetObject;
import com.cybex.provider.graphene.chain.MemoData;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.PrivateKey;
import com.cybex.provider.graphene.chain.PublicKey;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.graphene.chain.Types;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.provider.utils.SpUtil;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.WalletApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.enotes.sdk.core.CardManager;
import io.enotes.sdk.repository.db.entity.Card;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

public class BitsharesWalletWraper {

    private static BitsharesWalletWraper bitsharesWalletWraper = new BitsharesWalletWraper();
    private WalletApi mWalletApi = new WalletApi();
    private Map<ObjectId<AccountObject>, AccountObject> mMapAccountId2Object = new ConcurrentHashMap<>();
    private Map<ObjectId<AccountObject>, List<Asset>> mMapAccountId2Asset = new ConcurrentHashMap<>();
    private Map<ObjectId<AssetObject>, AssetObject> mMapAssetId2Object = new ConcurrentHashMap<>();
    private Map<String, Types.public_key_type > mMapAddress2PublicKey = new ConcurrentHashMap<>();
    private String mstrWalletFilePath;
    private List<ObjectId<AssetObject>> mObjectList = new ArrayList<>();
    private List<String> addressList = new ArrayList<>();
    private List<String> addressListFromPublicKey = new ArrayList<>();
    private String password;
    private Disposable lockWalletDisposable;
    private int mUnlockWalletPeriod;

    private BitsharesWalletWraper() {
        //mstrWalletFilePath = BitsharesApplication.getInstance().getFilesDir().getPath();
        mstrWalletFilePath += "/wallet.json";
        mUnlockWalletPeriod = PreferenceManager.getDefaultSharedPreferences(BaseActivity.getContext()).getInt(Constant.PREF_UNLOCK_WALLET_PERIOD, 1);
        EventBus.getDefault().register(this);
    }

    public static BitsharesWalletWraper getInstance() {
        return bitsharesWalletWraper;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLockWalletPeriod(Event.onChangeUnlockWalletPeriod event) {
        mUnlockWalletPeriod = event.getPeriod();
        cancelLockWalletTime();
        startLockWalletTimer();
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
        return mWalletApi.is_locked();
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

    public void disconnect() {
        mWalletApi.disconnect();
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

    public String getUserKey(String userName, String password) {
        return mWalletApi.getUserKey(userName, password);
    }

    public int import_account_password(AccountObject accountObject, String strAccountName, String strPassword) {
        mWalletApi.set_password(strPassword);
        int nRet = mWalletApi.import_account_password(accountObject, strAccountName, strPassword);
        if (nRet == 0) {
            for (AccountObject account : list_my_accounts()) {
                mMapAccountId2Object.put(account.id, account);
            }
            SpUtil.putMap(BaseActivity.getContext(), Constant.PREF_ADDRESS_TO_PUB_MAP, mWalletApi.getMapAddress2Pub());
            password = strPassword;
            unlock(strPassword);
            startLockWalletTimer();
        }
        return nRet;

    }

    public int unlock(String strPassword) {
        return mWalletApi.unlock(strPassword);
    }

    public int lock() {
        return mWalletApi.lock();
    }

    private void startLockWalletTimer() {
        lockWalletDisposable = Flowable.intervalRange(0, 0, mUnlockWalletPeriod, 0, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(this::lock)
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        password = null;
                    }
                })
                .subscribe();
    }


    public void cancelLockWalletTime() {
        if (lockWalletDisposable != null && !lockWalletDisposable.isDisposed()) {
            lockWalletDisposable.dispose();
        }
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

//    public void get_account_history(ObjectId<AccountObject> accountObjectId,
//                                    int nLimit,
//                                    MessageCallback<Reply<List<AccountHistoryObject>>> callback) throws NetworkStatusException {
//        mWalletApi.get_account_history(accountObjectId, nLimit, callback);
//    }

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

    public void lookup_asset_symbols(String strAssetSymbol,
                                     MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.lookup_asset_symbols(strAssetSymbol, callback);
    }

    //get asset detail
    public void get_objects(String objectId,
                            MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_objects(objectId, callback);
    }

    public void get_objects(Set<String> objectIds,
                            MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_objects(objectIds, callback);
    }

    public void get_accounts(List<String> accountIds,
                             MessageCallback<Reply<List<AccountObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_accounts(accountIds, callback);
    }

    public void get_block(int callId, int blockNumber,
                          MessageCallback<Reply<BlockHeader>> callback) throws NetworkStatusException {
        mWalletApi.get_block(callId, blockNumber, callback);
    }

    public void get_block_header(int blockNumber, MessageCallback<Reply<BlockHeader>> callback) throws NetworkStatusException {
        mWalletApi.get_block_header(blockNumber, callback);
    }

    public void get_recent_transaction_by_id(String transactionId, MessageCallback<Reply<Object>> callback) throws NetworkStatusException {
        mWalletApi.get_recent_transaction_by_id(transactionId, callback);
    }

    public Operations.transfer_operation getTransferOperation(ObjectId<AccountObject> from,
                                                              ObjectId<AccountObject> to,
                                                              ObjectId<AssetObject> transferAssetId,
                                                              long feeAmount,
                                                              ObjectId<AssetObject> feeAssetId,
                                                              long transferAmount,
                                                              String memo,
                                                              Types.public_key_type fromMemoKey,
                                                              Types.public_key_type toMemoKey) {
        return mWalletApi.getTransferOperation(from, to, transferAssetId, feeAmount, feeAssetId, transferAmount, memo, fromMemoKey, toMemoKey);
    }

    public Operations.transfer_operation getTransferOperationWithLockTime(ObjectId<AccountObject> from,
                                                                          ObjectId<AccountObject> to,
                                                                          ObjectId<AssetObject> transferAssetId,
                                                                          long feeAmount,
                                                                          ObjectId<AssetObject> feeAssetId,
                                                                          long transferAmount,
                                                                          String memo,
                                                                          Types.public_key_type fromMemoKey,
                                                                          Types.public_key_type toMemoKey,
                                                                          Types.public_key_type toActiveKey,
                                                                          long vesting_period,
                                                                          int type) {
        return mWalletApi.getTransferOperationWithLockTime(from, to, transferAssetId, feeAmount, feeAssetId, transferAmount, memo, fromMemoKey, toMemoKey, toActiveKey, vesting_period, type);

    }

    public Operations.limit_order_create_operation getLimitOrderCreateOperation(ObjectId<AccountObject> accountId,
                                                                                ObjectId<AssetObject> assetFeeId,
                                                                                ObjectId<AssetObject> assetSellId,
                                                                                ObjectId<AssetObject> assetReceiveId,
                                                                                long amountFee,
                                                                                long amountSell,
                                                                                long amountReceive) {
        return mWalletApi.getLimitOrderCreateOperation(accountId, assetFeeId, assetSellId, assetReceiveId, amountFee, amountSell, amountReceive);
    }

    public Operations.limit_order_cancel_operation getLimitOrderCancelOperation(ObjectId<AccountObject> accountId,
                                                                                ObjectId<AssetObject> assetFeeId,
                                                                                ObjectId<LimitOrder> limitOrderId,
                                                                                long amountFee) {
        return mWalletApi.getLimitOrderCancelOperation(accountId, assetFeeId, limitOrderId, amountFee);
    }

    public Operations.cancel_all_operation getLimitOrderCancelAllOperation(ObjectId<AssetObject> assetFeeId,
                                                                           long amountFee,
                                                                           ObjectId<AccountObject> sellerId,
                                                                           ObjectId<AssetObject> receiveAssetId,
                                                                           ObjectId<AssetObject> sellAssetId) {
        return mWalletApi.getLimitOrderCancelAllOperation(assetFeeId, amountFee, sellerId, receiveAssetId, sellAssetId);
    }

    public Operations.withdraw_deposit_history_operation getWithdrawDepositOperation(String accountName, int offset, int size, String fundType, String asset, Date expiration) {
        return mWalletApi.getWithdrawDepositOperation(accountName, offset, size, fundType, asset, expiration);
    }

    public Operations.gateway_login_operation getGatewayLoginOperation(String user, Date expiration) {
        return mWalletApi.getGatewayLoginOperation(user, expiration);
    }

    public Operations.balance_claim_operation getBalanceClaimOperation(long fee,
                                                                       ObjectId<AssetObject> feeAssetId,
                                                                       ObjectId<AccountObject> depositToAccount,
                                                                       ObjectId<LockAssetObject> balanceToClaim,
                                                                       Types.public_key_type balanceOwnerKey,
                                                                       long totalClaimedAmount,
                                                                       ObjectId<AssetObject> totalClaimedAmountId) {
        return mWalletApi.getBalanceClaimOperation(fee, feeAssetId, depositToAccount, balanceToClaim, balanceOwnerKey, totalClaimedAmount, totalClaimedAmountId);
    }

    public Operations.exchange_participate_operation getParticipateOperatin(long fee,
                                                                            ObjectId<AssetObject> feeAssetId,
                                                                            long amount,
                                                                            ObjectId<AssetObject> amountAssetId,
                                                                            ObjectId exchangePayId,
                                                                            ObjectId<AccountObject> payerId) {
        return mWalletApi.getExchangeOperation(fee, feeAssetId, amount, amountAssetId, exchangePayId, payerId);
    }

    public Operations.account_update_operation getAccountUpdateOperation(ObjectId<AssetObject> feeAssetId,
                                                                         long fee,
                                                                         ObjectId<AccountObject> accountId,
                                                                         Authority authority,
                                                                         Types.account_options account_options,
                                                                         Types.public_key_type public_key_type) {
        return mWalletApi.getAccountUpdateOperation(feeAssetId, fee, accountId, authority, account_options, public_key_type);
    }

    public SignedTransaction getSignedTransaction(AccountObject accountObject, Operations.base_operation operation, int operationId, DynamicGlobalPropertyObject dynamicGlobalPropertyObject) {

        return mWalletApi.getSignedTransaction(accountObject, operation, operationId, dynamicGlobalPropertyObject);
    }

    public SignedTransaction getSignedTransactinForTickets(AccountObject accountObject, Operations.base_operation operation, int operationId, BlockHeader blockHeader) throws ParseException {
        return mWalletApi.getSignedTransactionForTicket(accountObject, operation, operationId, blockHeader);
    }

    public SignedTransaction getSignedTransactionByENotesForTicket(CardManager cardManager, Card card, AccountObject accountObject, Operations.base_operation operation, int operationId, BlockHeader blockHeader ) throws ParseException {
        return mWalletApi.getSignedTransactionByENotesForTicket(cardManager, card, accountObject, operation, operationId, blockHeader);
    }

    public String getChatMessageSignature(AccountObject accountObject, String message){
        return mWalletApi.getChatMessageSignature(accountObject, message);
    }

    public String getWithdrawDepositSignature(AccountObject accountObject, Operations.base_operation operation) {
        return mWalletApi.getWithdrawDepositSignature(accountObject, operation);
    }

    public SignedTransaction getSignedTransactionByENotes(CardManager cardManager, Card card, AccountObject accountObject, Operations.base_operation operation, int operationId, DynamicGlobalPropertyObject dynamicGlobalPropertyObject) {

        return mWalletApi.getSignedTransactionByENotes(cardManager, card, accountObject, operation, operationId, dynamicGlobalPropertyObject);
    }

    public String getMemoMessage(MemoData memoData) {
        return mWalletApi.getMemoMessage(memoData);
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
                                   int nBucket,
                                   Date dateStart,
                                   Date dateEnd,
                                   MessageCallback<Reply<List<BucketObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_market_history(baseAssetId, quoteAssetId, nBucket, dateStart, dateEnd, callback);
    }

    public void subscribe_to_market(String id,
                                    String base,
                                    String quote,
                                    MessageCallback<Reply<String>> callback) throws NetworkStatusException {
        mWalletApi.subscribe_to_market(id, base, quote, callback);
    }

    public AtomicInteger get_call_id() {
        return mWalletApi.getCallId();
    }

//    public void set_subscribe_market(boolean filter) throws NetworkStatusException {
//        mWalletApi.set_subscribe_market(filter);
//    }

    //
    public void get_ticker(String base,
                           String quote,
                           MessageCallback<Reply<MarketTicker>> callback) throws NetworkStatusException {
        mWalletApi.get_ticker(base, quote, callback);
    }

//    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
//            throws NetworkStatusException {
//        return mWalletApi.get_trade_history(base, quote, start, end, limit);
//    }

    public void get_fill_order_history(ObjectId<AssetObject> base,
                                       ObjectId<AssetObject> quote,
                                       int limit,
                                       MessageCallback<Reply<List<HashMap<String, Object>>>> callback) throws NetworkStatusException {
        mWalletApi.get_fill_order_history(base, quote, limit, callback);
    }

    public void get_limit_orders(ObjectId<AssetObject> base,
                                 ObjectId<AssetObject> quote,
                                 int limit,
                                 MessageCallback<Reply<List<LimitOrderObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_limit_orders(base, quote, limit, callback);
    }

    public void get_balance_objects(List<String> addresses,
                                    MessageCallback<Reply<List<LockAssetObject>>> callback) throws NetworkStatusException {
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

    public void get_account_object(String strAccount,
                                   MessageCallback<Reply<AccountObject>> callback) throws NetworkStatusException {
        mWalletApi.get_account_by_name(strAccount, callback);
    }

//    public Asset transfer_calculate_fee(String strAmount,
//                                        String strAssetSymbol,
//                                        String strMemo) throws NetworkStatusException {
//        return mWalletApi.transfer_calculate_fee(strAmount, strAssetSymbol, strMemo);
//    }

//    public String get_plain_text_message(memo_data memoData) {
//        return mWalletApi.decrypt_memo_message(memoData);
//    }

    public void get_full_accounts(List<String> names,
                                  boolean subscribe,
                                  MessageCallback<Reply<List<FullAccountObjectReply>>> callback) throws NetworkStatusException {
        mWalletApi.get_full_accounts(names, subscribe, callback);
    }

    public void get_required_fees(String assetId,
                                  int operationId,
                                  Operations.base_operation operation,
                                  MessageCallback<Reply<List<FeeAmountObject>>> callback)
            throws NetworkStatusException {
        mWalletApi.get_required_fees(assetId, operationId, operation, callback);
    }

    public void get_key_references(Set<String> pubKeys,
                            MessageCallback<Reply<List<List<String>>>> callback) throws NetworkStatusException {
        mWalletApi.get_key_references(pubKeys, callback);
    }

    public void get_account_objects(Set<String> objectIds,
                            MessageCallback<Reply<List<AccountObject>>> callback) throws NetworkStatusException {
        mWalletApi.get_account_objects(objectIds, callback);
    }

    public void broadcast_transaction_with_callback(SignedTransaction signedTransaction,
                                                    MessageCallback<Reply<String>> callback) throws NetworkStatusException {
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

    public void get_dynamic_global_properties(MessageCallback<Reply<DynamicGlobalPropertyObject>> callback) throws NetworkStatusException {
        mWalletApi.get_dynamic_global_properties(callback);
    }

    public String getPassword() {
        return password;
    }

}
