package com.cybex.provider.websocket;


import android.content.Context;
import android.util.Log;

import com.cybex.provider.constant.ErrorCode;
import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.crypto.Aes;
import com.cybex.provider.crypto.Sha512Object;
import com.cybex.provider.fc.io.BaseEncoder;
import com.cybex.provider.fc.io.DataStreamEncoder;
import com.cybex.provider.fc.io.DataStreamSizeEncoder;
import com.cybex.provider.fc.io.RawType;
import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.Asset;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.Authority;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybex.provider.graphene.chain.BucketObject;
import com.cybex.provider.graphene.chain.CompactSignature;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObjectReply;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.graphene.chain.LimitOrderObject;
import com.cybex.provider.graphene.chain.LockAssetObject;
import com.cybex.provider.graphene.chain.MemoData;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.PrivateKey;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.graphene.chain.Types;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.provider.utils.MyUtils;
import com.google.common.primitives.UnsignedInteger;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import io.enotes.sdk.core.CardManager;
import io.enotes.sdk.repository.db.entity.Card;

public class WalletApi {

    class wallet_object {
        Sha256Object chain_id;
        List<AccountObject> my_accounts = new ArrayList<>();
        ByteBuffer cipher_keys;
        HashMap<ObjectId<AccountObject>, List<Types.public_key_type>> extra_keys = new HashMap<>();
        String ws_server = "";
        String ws_user = "";
        String ws_password = "";

        public void update_account(AccountObject accountObject) {
            boolean bUpdated = false;
            for (int i = 0; i < my_accounts.size(); ++i) {
                if (my_accounts.get(i).id == accountObject.id) {
                    my_accounts.remove(i);
                    my_accounts.add(accountObject);
                    bUpdated = true;
                    break;
                }
            }

            if (bUpdated == false) {
                my_accounts.add(accountObject);
            }
        }
    }

    DynamicGlobalPropertyObject mDynamicGlobalPropertyObject;
    private WebSocketClient mWebSocketClient = new WebSocketClient();
    private wallet_object mWalletObject;
    private boolean mbLogin = false;
    private HashMap<Types.public_key_type, Types.private_key_type> mHashMapPub2Priv = new HashMap<>();
    private Types.private_key_type mMemoPrivateKey;
    private Sha512Object mCheckSum = new Sha512Object();
    private String unCompressedOwnerKey;
    private Context mContext;
    static class plain_keys {
        Map<Types.public_key_type, String> keys;
        Sha512Object checksum;

        public void write_to_encoder(BaseEncoder encoder) {
            RawType rawType = new RawType();

            rawType.pack(encoder, UnsignedInteger.fromIntBits(keys.size()));
            for (Map.Entry<Types.public_key_type, String> entry : keys.entrySet()) {
                encoder.write(entry.getKey().key_data);

                byte[] byteValue = entry.getValue().getBytes();
                rawType.pack(encoder, UnsignedInteger.fromIntBits(byteValue.length));
                encoder.write(byteValue);
            }
            encoder.write(checksum.hash);
        }

        public static plain_keys from_input_stream(InputStream inputStream) {
            plain_keys keysResult = new plain_keys();
            keysResult.keys = new HashMap<>();
            keysResult.checksum = new Sha512Object();

            RawType rawType = new RawType();
            UnsignedInteger size = rawType.unpack(inputStream);
            try {
                for (int i = 0; i < size.longValue(); ++i) {
                    Types.public_key_type publicKeyType = new Types.public_key_type();
                    inputStream.read(publicKeyType.key_data);

                    UnsignedInteger strSize = rawType.unpack(inputStream);
                    byte[] byteBuffer = new byte[strSize.intValue()];
                    inputStream.read(byteBuffer);

                    String strPrivateKey = new String(byteBuffer);

                    keysResult.keys.put(publicKeyType, strPrivateKey);
                }

                inputStream.read(keysResult.checksum.hash);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return keysResult;
        }


    }

    public WalletApi() {
        mMemoPrivateKey = new Types.private_key_type(PrivateKey.from_seed("cybex-testactivecybextest123456"));
    }

    public WalletApi(Context context) {
        mContext = context;
    }

    public void initialize() {
        mWebSocketClient.connect();
        if (mWalletObject == null) {
            mWalletObject = new wallet_object();
        }
    }

    public void disconnect() {
        mWebSocketClient.disconnect();
        mWalletObject = null;
        mCheckSum = null;
        mbLogin = false;
        mHashMapPub2Priv.clear();
    }

    public int reset() {
        mWebSocketClient.close();
        return 0;
    }

    public boolean is_locked() {
        if (mWalletObject.cipher_keys.array().length > 0 &&
                mCheckSum.equals(new Sha512Object())) {
            return true;
        }
        return false;
    }

    private void encrypt_keys() {
        plain_keys data = new plain_keys();
        data.keys = new HashMap<>();
        for (Map.Entry<Types.public_key_type, Types.private_key_type> entry : mHashMapPub2Priv.entrySet()) {
            data.keys.put(entry.getKey(), entry.getValue().toString());
        }
        data.checksum = mCheckSum;

        DataStreamSizeEncoder sizeEncoder = new DataStreamSizeEncoder();
        data.write_to_encoder(sizeEncoder);
        DataStreamEncoder encoder = new DataStreamEncoder(sizeEncoder.getSize());
        data.write_to_encoder(encoder);

        byte[] byteKey = new byte[32];
        System.arraycopy(mCheckSum.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(mCheckSum.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteResult = Aes.encrypt(byteKey, ivBytes, encoder.getData());

        mWalletObject.cipher_keys = byteResult;
    }

    public int lock() {
        encrypt_keys();

        mCheckSum = new Sha512Object();
        mHashMapPub2Priv.clear();

        return 0;
    }

    public int unlock(String strPassword) {
        assert(strPassword.length() > 0);
        Sha512Object passwordHash = Sha512Object.create_from_string(strPassword);
        byte[] byteKey = new byte[32];
        System.arraycopy(passwordHash.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(passwordHash.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteDecrypt = Aes.decrypt(byteKey, ivBytes, mWalletObject.cipher_keys.array());
        if (byteDecrypt == null || byteDecrypt.array().length == 0) {
            return -1;
        }

        plain_keys dataResult = plain_keys.from_input_stream(
                new ByteArrayInputStream(byteDecrypt.array())
        );

        for (Map.Entry<Types.public_key_type, String> entry : dataResult.keys.entrySet()) {
            Types.private_key_type privateKeyType = new Types.private_key_type(entry.getValue());
            mHashMapPub2Priv.put(entry.getKey(), privateKeyType);
        }

        mCheckSum = passwordHash;
        if (passwordHash.equals(dataResult.checksum)) {
            return 0;
        } else {
            return -1;
        }
    }

    public boolean is_new() {
        if (mWalletObject == null || mWalletObject.cipher_keys == null) {
            return true;
        }

        return (mWalletObject.cipher_keys.array().length == 0 &&
                mCheckSum.equals(new Sha512Object()));
    }

    public int load_wallet_file(String strFileName) {
        if (mWalletObject != null) {
            return 0;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(strFileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
            mWalletObject = gson.fromJson(inputStreamReader, wallet_object.class);
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return -1;
        } catch (JsonIOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int save_wallet_file(String strFileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(strFileName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();

            outputStreamWriter.write(gson.toJson(mWalletObject));
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int set_password(String strPassword) {
        mCheckSum = Sha512Object.create_from_string(strPassword);
        return 0;
    }

    /*public synchronized int login(String strUserName, String strPassowrd) throws IOException {
        if (mbLogin) {
            return 0;
        }

        if (mWalletObject != null) {
            mbLogin = mWebSocketClient.login(
                    mWalletObject.ws_user,
                    mWalletObject.ws_password
            );
        } else {
            mWalletObject = new wallet_object();
            mbLogin = mWebSocketClient.login(
                    strUserName,
                    strPassowrd
            );
        }

        if (mbLogin) {
            mWebSocketClient.get_database_api_id();
            mWebSocketClient.get_history_api_id();
            mWebSocketClient.get_broadcast_api_id();


            mWalletObject.ws_user = strUserName;
            mWalletObject.ws_password = strPassowrd;
            Sha256Object sha256Object = mWebSocketClient.get_chain_id();
            if (mWalletObject.chain_id != null &&
                    mWalletObject.chain_id.equals(sha256Object) == false) {
                return -1; // 之前的chain_id与当前的chain_id不一致
            }
            mWalletObject.chain_id = sha256Object;
        }

        return 0;
    }*/

    public List<AccountObject> list_my_accounts() {
        List<AccountObject> accountObjectList = new ArrayList<>();
        if (mWalletObject != null) {
            accountObjectList.addAll(mWalletObject.my_accounts);
        }
        return accountObjectList;
    }

    public void get_accounts(List<String> accountIds,
                             MessageCallback<Reply<List<AccountObject>>> callback) throws NetworkStatusException {
        mWebSocketClient.get_accounts(accountIds, callback);
    }

//    public void lookup_account_names(String strAccountName, WebSocketClient.MessageCallback callback) throws NetworkStatusException {
//        mWebSocketClient.lookup_account_names(strAccountName, callback);
//    }

    public void get_account_by_name(String strAccountName,
                                    MessageCallback<Reply<AccountObject>> callback) throws NetworkStatusException {
        mWebSocketClient.get_account_by_name(strAccountName, callback);
    }

//    public List<Asset> list_account_balance(ObjectId<AccountObject> accountId) throws NetworkStatusException {
//        return mWebSocketClient.list_account_balances(accountId);
//    }

//    public void get_account_history(ObjectId<AccountObject> accountId,
//                                    int nLimit,
//                                    MessageCallback<Reply<List<AccountHistoryObject>>> callback) throws NetworkStatusException {
//        mWebSocketClient.get_account_history(accountId, nLimit, callback);
//    }

    public void get_block(int callId,
                          int blockNumber,
                          MessageCallback<Reply<BlockHeader>> callback) throws NetworkStatusException {
        mWebSocketClient.get_block(callId, blockNumber, callback);
    }

    public void get_block_header(int blockNumber, MessageCallback<Reply<BlockHeader>> callback) throws NetworkStatusException {
        mWebSocketClient.get_block_header(blockNumber, callback);
    }

//    public List<AssetObject> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
//        return mWebSocketClient.list_assets(strLowerBound, nLimit);
//    }
//    public List<AssetObject> get_assets(List<ObjectId<AssetObject>> listAssetObjectId) throws NetworkStatusException {
//        return mWebSocketClient.get_assets(listAssetObjectId);
//    }

    public void lookup_asset_symbols(String strAssetSymbol,
                                     MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWebSocketClient.lookup_asset_symbols(strAssetSymbol, callback);
    }

    public void get_objects(String objectId,
                            MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        Set<String> objectIds = new HashSet<>();
        objectIds.add(objectId);
        get_objects(objectIds, callback);
    }

    public void get_objects(Set<String> objectIds,
                            MessageCallback<Reply<List<AssetObject>>> callback) throws NetworkStatusException {
        mWebSocketClient.get_objects(objectIds, callback);
    }

//    public int import_brain_key(String strAccountNameOrId, String strBrainKey) throws NetworkStatusException {
//        AccountObject accountObject = get_account(strAccountNameOrId);
//        if (accountObject == null) {
//            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
//        }
//
//        Map<Types.public_key_type, Types.private_key_type> mapPublic2Private = new HashMap<>();
//        for (int i = 0; i < 10; ++i) {
//            BrainKey brainKey = new BrainKey(strBrainKey, i);
//            ECKey ecKey = brainKey.getPrivateKey();
//            PrivateKey privateKey = new PrivateKey(ecKey.getPrivKeyBytes());
//            Types.private_key_type privateKeyType = new Types.private_key_type(privateKey);
//            Types.public_key_type publicKeyType = new Types.public_key_type(privateKey.get_public_key());
//
//            if (accountObject.active.is_public_key_type_exist(publicKeyType) == false &&
//                    accountObject.owner.is_public_key_type_exist(publicKeyType) == false &&
//                    accountObject.options.memo_key.compare(publicKeyType) == false) {
//                continue;
//            }
//            mapPublic2Private.put(publicKeyType, privateKeyType);
//        }
//
//        if (mapPublic2Private.isEmpty() == true) {
//            return ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
//        }
//
//        mWalletObject.update_account(accountObject);
//
//        List<Types.public_key_type> listPublicKeyType = new ArrayList<>();
//        listPublicKeyType.addAll(mapPublic2Private.keySet());
//
//        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
//        mHashMapPub2Priv.putAll(mapPublic2Private);
//
//        encrypt_keys();
//
//        return 0;
//    }

//    public int import_key(String account_name_or_id,
//                          String wif_key) throws NetworkStatusException {
//        assert (is_locked() == false && is_new() == false);
//
//        Types.private_key_type privateKeyType = new Types.private_key_type(wif_key);
//
//        PublicKey publicKey = privateKeyType.getPrivateKey().get_public_key();
//        Types.public_key_type publicKeyType = new Types.public_key_type(publicKey);
//
//        AccountObject accountObject = get_account(account_name_or_id);
//        if (accountObject == null) {
//            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
//        }
//
//        /*List<AccountObject> listAccountObject = lookup_account_names(account_name_or_id);
//        // 进行publicKey的比对
//        if (listAccountObject.isEmpty()) {
//            return -1;
//        }
//
//        AccountObject accountObject = listAccountObject.get(0);*/
//        if (accountObject.active.is_public_key_type_exist(publicKeyType) == false &&
//                accountObject.owner.is_public_key_type_exist(publicKeyType) == false &&
//                accountObject.options.memo_key.compare(publicKeyType) == false) {
//            return -1;
//        }
//
//        mWalletObject.update_account(accountObject);
//
//        List<Types.public_key_type> listPublicKeyType = new ArrayList<>();
//        listPublicKeyType.add(publicKeyType);
//
//        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
//        mHashMapPub2Priv.put(publicKeyType, privateKeyType);
//
//        encrypt_keys();
//
//        // 保存至文件
//
//        return 0;
//    }

//    public int import_keys(String account_name_or_id,
//                           String wif_key_1,
//                           String wif_key_2) throws NetworkStatusException {
//        assert (is_locked() == false && is_new() == false);
//
//        Types.private_key_type privateKeyType1 = new Types.private_key_type(wif_key_1);
//        Types.private_key_type privateKeyType2 = new Types.private_key_type(wif_key_2);
//
//        PublicKey publicKey1 = privateKeyType1.getPrivateKey().get_public_key();
//        PublicKey publicKey2 = privateKeyType1.getPrivateKey().get_public_key();
//        Types.public_key_type publicKeyType1 = new Types.public_key_type(publicKey1);
//        Types.public_key_type publicKeyType2 = new Types.public_key_type(publicKey2);
//
//        AccountObject accountObject = get_account(account_name_or_id);
//        if (accountObject == null) {
//            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
//        }
//
//        /*List<AccountObject> listAccountObject = lookup_account_names(account_name_or_id);
//        // 进行publicKey的比对
//        if (listAccountObject.isEmpty()) {
//            return -1;
//        }
//
//        AccountObject accountObject = listAccountObject.get(0);*/
//        if (accountObject.active.is_public_key_type_exist(publicKeyType1) == false &&
//                accountObject.owner.is_public_key_type_exist(publicKeyType1) == false &&
//                accountObject.options.memo_key.compare(publicKeyType1) == false) {
//            return -1;
//        }
//
//        if (accountObject.active.is_public_key_type_exist(publicKeyType2) == false &&
//                accountObject.owner.is_public_key_type_exist(publicKeyType2) == false &&
//                accountObject.options.memo_key.compare(publicKeyType2) == false) {
//            return -1;
//        }
//
//
//
//        mWalletObject.update_account(accountObject);
//
//        List<Types.public_key_type> listPublicKeyType = new ArrayList<>();
//        listPublicKeyType.add(publicKeyType1);
//        listPublicKeyType.add(publicKeyType2);
//
//        mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
//        mHashMapPub2Priv.put(publicKeyType1, privateKeyType1);
//        mHashMapPub2Priv.put(publicKeyType2, privateKeyType2);
//
//        encrypt_keys();
//
//        // 保存至文件
//        return 0;
//    }

    public int import_account_password(AccountObject accountObject, String strAccountName, String strPassword) {
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }
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
        String ownerAddress = publicOwnerKeyType.getAddress();
        String memoAddress = publicMemoKeyType.getAddress();
        String PTSAddress = publicActiveKeyType.getPTSAddress(publicActiveKeyType.key_data);
        String ownerPtsAddress = publicOwnerKeyType.getPTSAddress(publicOwnerKeyType.key_data);
        String memoPtsAddress = publicMemoKeyType.getPTSAddress(publicMemoKeyType.key_data);
        String uncompressedPts = publicActiveKeyTypeUnCompressed.getPTSAddress(publicActiveKeyTypeUnCompressed.key_data_uncompressed);
        unCompressedOwnerKey = publicOwnerKeyTypeUnCompressed.getPTSAddress(publicOwnerKeyTypeUnCompressed.key_data_uncompressed);
        String unCompressedMemo = publicMemoKeyTypeUnCompressed.getPTSAddress(publicMemoKeyTypeUnCompressed.key_data_uncompressed);
        Log.v("Address", address);
        Log.v("OwnerAddress", ownerAddress);
        Log.v("ActivePTSAddress", PTSAddress);
        Log.v("OwnerPtsAddress", ownerPtsAddress);
        Log.v("MemoAddress", memoAddress);
        Log.v("MemoPTSAddress", memoPtsAddress);
        Log.v("uncompressedActive", uncompressedPts);
        Log.v("uncompressedOwner", unCompressedOwnerKey);
        Log.v("uncompressedMemo", unCompressedMemo);

        if (!accountObject.active.is_public_key_type_exist(publicActiveKeyType)&&
            !accountObject.active.is_public_key_type_exist(publicOwnerKeyType) &&
            !accountObject.owner.is_public_key_type_exist(publicActiveKeyType)&&
            !accountObject.owner.is_public_key_type_exist(publicOwnerKeyType)){
            return ErrorCode.ERROR_PASSWORD_INVALID;
        }

        List<Types.public_key_type> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicActiveKeyType);
        listPublicKeyType.add(publicOwnerKeyType);
        listPublicKeyType.add(publicMemoKeyType);
        if (mWalletObject != null) {
            mWalletObject.update_account(accountObject);
            mWalletObject.extra_keys.put(accountObject.id, listPublicKeyType);
            encrypt_keys();
        }
        mHashMapPub2Priv.put(publicActiveKeyType, new Types.private_key_type(privateActiveKey));
        mHashMapPub2Priv.put(publicOwnerKeyType, new Types.private_key_type(privateOwnerKey));
        mHashMapPub2Priv.put(publicMemoKeyType, new Types.private_key_type(privateMemoKey));

        return 0;
    }

//    public Asset transfer_calculate_fee(String strAmount,
//                                        String strAssetSymbol,
//                                        String strMemo) throws NetworkStatusException {
//        ObjectId<AssetObject> assetObjectId = ObjectId.create_from_string(strAssetSymbol);
//        AssetObject assetObject = null;
//        if (assetObjectId == null) {
//            assetObject = lookup_asset_symbols(strAssetSymbol);
//        } else {
//            List<ObjectId<AssetObject>> listAssetObjectId = new ArrayList<>();
//            listAssetObjectId.add(assetObjectId);
//            assetObject = get_assets(listAssetObjectId).get(0);
//        }
//
//        Operations.transfer_operation transferOperation = new Operations.transfer_operation();
//        transferOperation.from = new ObjectId<AccountObject>(0, AccountObject.class);//accountObjectFrom.id;
//        transferOperation.to = new ObjectId<AccountObject>(0, AccountObject.class);
//        transferOperation.amount = assetObject.amount_from_string(strAmount);
//        transferOperation.extensions = new HashSet<>();
//        /*if (TextUtils.isEmpty(strMemo) == false) {
//
//        }*/
//
//        Operations.operation_type operationType = new Operations.operation_type();
//        operationType.nOperationType = 0;
//        operationType.operationContent = transferOperation;
//
//        signed_transaction tx = new signed_transaction();
//        tx.Operations = new ArrayList<>();
//        tx.Operations.add(operationType);
//        tx.extensions = new HashSet<>();
//        set_operation_fees(tx, get_global_properties().parameters.current_fees);
//
//        return transferOperation.fee;
//    }

    public Operations.transfer_operation getTransferOperation(ObjectId<AccountObject> from,
                                                              ObjectId<AccountObject> to,
                                                              ObjectId<AssetObject> transferAssetId,
                                                              long feeAmount,
                                                              ObjectId<AssetObject> feeAssetId,
                                                              long transferAmount,
                                                              String memo,
                                                              Types.public_key_type fromMemoKey,
                                                              Types.public_key_type toMemoKey) {

        Operations.transfer_operation transferOperation = new Operations.transfer_operation();
        transferOperation.from = from;
        transferOperation.to = to;
        transferOperation.fee = new Asset(feeAmount, feeAssetId);
        transferOperation.amount = new Asset(transferAmount, transferAssetId);
        transferOperation.extensions = new HashSet<>();
        if(memo != null && memo.length() > 0){
            transferOperation.memo = new MemoData();
            transferOperation.memo.from = fromMemoKey;
            transferOperation.memo.to = toMemoKey;
            Types.private_key_type  privateKeyType = mMemoPrivateKey;//使用随意一个私钥来避免空指针问题
            transferOperation.memo.set_message(
                    privateKeyType.getPrivateKey(),
                    toMemoKey.getPublicKey(),
                    memo,
                    0);
            transferOperation.memo.get_message(
                    privateKeyType.getPrivateKey(),
                    toMemoKey.getPublicKey());
        }
        return transferOperation;
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
        Operations.transfer_operation transferOperation = new Operations.transfer_operation();
        transferOperation.from = from;
        transferOperation.to = to;
        transferOperation.fee = new Asset(feeAmount, feeAssetId);
        transferOperation.amount = new Asset(transferAmount, transferAssetId);
        transferOperation.vesting_period = vesting_period;
        transferOperation.public_key_type = toActiveKey;
        List<Object> lockTimeTransferObject = new ArrayList<>();
        lockTimeTransferObject.add(type);
        HashMap<String, Object> object = new HashMap<>();
        object.put("vesting_period", vesting_period);
        object.put("public_key", toActiveKey.toString());
        lockTimeTransferObject.add(object);
        HashSet<List<Object>> extensions = new HashSet<>();
        extensions.add(lockTimeTransferObject);
        transferOperation.extensions =extensions;


        if(memo != null && memo.length() > 0){
            transferOperation.memo = new MemoData();
            transferOperation.memo.from = fromMemoKey;
            transferOperation.memo.to = toMemoKey;
            Types.private_key_type  privateKeyType = mMemoPrivateKey;
            transferOperation.memo.set_message(
                    privateKeyType.getPrivateKey(),
                    toMemoKey.getPublicKey(),
                    memo,
                    0);
            transferOperation.memo.get_message(
                    privateKeyType.getPrivateKey(),
                    toMemoKey.getPublicKey());
        }
        return transferOperation;

    }

    public Operations.limit_order_cancel_operation getLimitOrderCancelOperation(ObjectId<AccountObject> accountId,
                                                                                ObjectId<AssetObject> assetFeeId,
                                                                                ObjectId<LimitOrder> limitOrderId,
                                                                                long amountFee){
        Operations.limit_order_cancel_operation operation = new Operations.limit_order_cancel_operation();
        operation.fee = new Asset(amountFee, assetFeeId);
        operation.fee_paying_account = accountId;
        operation.order = limitOrderId;
        operation.extensions = new HashSet<>();
        return operation;
    }

    public Operations.cancel_all_operation getLimitOrderCancelAllOperation(ObjectId<AssetObject> assetFeeId,
                                                                            long amountFee,
                                                                            ObjectId<AccountObject> accountId,
                                                                            ObjectId<AssetObject> receiveAssetId,
                                                                            ObjectId<AssetObject> sellAssetId
                                                                            ) {
        Operations.cancel_all_operation operation = new Operations.cancel_all_operation();
        operation.fee = new Asset(amountFee, assetFeeId);
        operation.seller = accountId;
        operation.receive_asset_id = receiveAssetId;
        operation.sell_asset_id = sellAssetId;
        operation.extensions = new HashSet<>();
        return operation;
    }

    public Operations.limit_order_create_operation getLimitOrderCreateOperation(ObjectId<AccountObject> accountId,
                                                                                ObjectId<AssetObject> assetFeeId,
                                                                                ObjectId<AssetObject> assetSellId,
                                                                                ObjectId<AssetObject> assetReceiveId,
                                                                                long amountFee,
                                                                                long amountSell,
                                                                                long amountReceive){
        Operations.limit_order_create_operation operation = new Operations.limit_order_create_operation();
        operation.fee = new Asset(amountFee, assetFeeId);
        operation.seller = accountId;
        operation.amount_to_sell = new Asset(amountSell, assetSellId);
        operation.min_to_receive = new Asset(amountReceive, assetReceiveId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 5);
        operation.expiration = calendar.getTime();
        operation.fill_or_kill = false;
        operation.extensions = new HashSet<>();
        return operation;
    }

    public Operations.withdraw_deposit_history_operation getWithdrawDepositOperation(String accountName, int offset, int size, String fundType, String asset, Date expiration) {
        Operations.withdraw_deposit_history_operation operation = new Operations.withdraw_deposit_history_operation();
        operation.accountName = accountName;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 5);
        operation.expirationDate = expiration;
        operation.expiration = expiration.getTime() / 1000;
        operation.fundType = fundType;
        operation.asset = asset;
        operation.size = size;
        operation.sizeInteger = UnsignedInteger.valueOf(size);
        operation.offset = offset;
        operation.offsetInteger = UnsignedInteger.valueOf(offset);
        Log.e("expiration", String.valueOf(calendar.getTime().getTime()));
        return operation;
    }

    public Operations.balance_claim_operation getBalanceClaimOperation(long fee,
                                                                       ObjectId<AssetObject> feeAssetId,
                                                                       ObjectId<AccountObject> depositToAccount,
                                                                       ObjectId<LockAssetObject> balanceToClaim,
                                                                       Types.public_key_type balanceOwnerKey,
                                                                       long totalClaimedAmount,
                                                                       ObjectId<AssetObject> totalClaimedAmountId) {
        Operations.balance_claim_operation operation = new Operations.balance_claim_operation();
        operation.fee = new Asset(fee, feeAssetId);
        operation.deposit_to_account = depositToAccount;
        operation.balance_to_claim = balanceToClaim;
        operation.balance_owner_key = balanceOwnerKey;
        operation.total_claimed = new Asset(totalClaimedAmount, totalClaimedAmountId);
        return operation;
    }

    public Operations.account_update_operation getAccountUpdateOperation(ObjectId<AssetObject> feeAssetId,
                                                                         long fee,
                                                                         ObjectId<AccountObject> accountId,
                                                                         Authority authority,
                                                                         Types.public_key_type public_key_type
                                                                         ) {
        Operations.account_update_operation account_update_operation = new Operations.account_update_operation();
        account_update_operation.fee = new Asset(fee, feeAssetId);
        account_update_operation.account = accountId;
        account_update_operation.active = authority;
        account_update_operation.owner = null;
        account_update_operation.new_options = null;
        account_update_operation.extensions = new HashSet<>();
        return account_update_operation;
    }

    public SignedTransaction getSignedTransaction(AccountObject accountObject, Operations.base_operation operation, int operationId, DynamicGlobalPropertyObject dynamicGlobalPropertyObject) {
        SignedTransaction signedTransaction = new SignedTransaction();
        Operations.operation_type operationType = new Operations.operation_type();
        operationType.nOperationType = operationId;
        operationType.operationContent = operation;
        signedTransaction.operationTypes = new ArrayList<>();
        signedTransaction.operationTypes.add(operationType);
        signedTransaction.operations = new ArrayList<>();
        List<Object> listInOperations = new ArrayList<>();
        listInOperations.add(operationId);
        listInOperations.add(operation);
        signedTransaction.operations.add(listInOperations);
        signedTransaction.extensions = new HashSet<>();

        signedTransaction.set_reference_block(dynamicGlobalPropertyObject.head_block_id);

        Date dateObject = dynamicGlobalPropertyObject.time;
        Calendar calender = Calendar.getInstance();
        calender.setTime(dateObject);
        calender.add(Calendar.SECOND, 30);
        dateObject = calender.getTime();

        signedTransaction.set_expiration(dateObject);
        Types.private_key_type privateKey = null;

        for (Types.public_key_type public_key_type : accountObject.active.get_keys()) {
            privateKey = mHashMapPub2Priv.get(public_key_type);
            if (privateKey != null) {
                signedTransaction.sign(privateKey, mWebSocketClient.getmChainIdObject());
                break;
            }
        }
        if (privateKey == null) {
            for (Types.public_key_type public_key_type : accountObject.owner.get_keys()) {
                privateKey = mHashMapPub2Priv.get(public_key_type);
                if (privateKey != null) {
                    signedTransaction.sign(privateKey, mWebSocketClient.getmChainIdObject());
                    break;
                }
            }
        }
        return signedTransaction;
    }

    public SignedTransaction getSignedTransactionForTicket(AccountObject accountObject, Operations.base_operation operation, int operationId, BlockHeader blockHeader) throws ParseException {
        SignedTransaction signedTransaction = new SignedTransaction();
        Operations.operation_type operationType = new Operations.operation_type();
        operationType.nOperationType = operationId;
        operationType.operationContent = operation;
        signedTransaction.operationTypes = new ArrayList<>();
        signedTransaction.operationTypes.add(operationType);
        signedTransaction.operations = new ArrayList<>();
        List<Object> listInOperations = new ArrayList<>();
        listInOperations.add(operationId);
        listInOperations.add(operation);
        signedTransaction.operations.add(listInOperations);
        signedTransaction.extensions = new HashSet<>();

        signedTransaction.set_reference_block(blockHeader.previous);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dateObject = simpleDateFormat.parse(blockHeader.timestamp);

        Calendar calender = Calendar.getInstance();
        calender.setTime(dateObject);
        calender.add(Calendar.SECOND, 30);
        dateObject = calender.getTime();

        signedTransaction.set_expiration(dateObject);
        Types.private_key_type privateKey = null;

        if (operation instanceof Operations.balance_claim_operation) {
            privateKey = mHashMapPub2Priv.get(((Operations.balance_claim_operation) operation).balance_owner_key);
            signedTransaction.sign(privateKey, mWebSocketClient.getmChainIdObject());
        }


        if (privateKey == null) {
            for (Types.public_key_type public_key_type : accountObject.active.get_keys()) {
                privateKey = mHashMapPub2Priv.get(public_key_type);
                if (privateKey != null) {
                    signedTransaction.sign(privateKey, mWebSocketClient.getmChainIdObject());
                    break;
                }
            }
        }
        if (privateKey == null) {
            for (Types.public_key_type public_key_type : accountObject.owner.get_keys()) {
                privateKey = mHashMapPub2Priv.get(public_key_type);
                if (privateKey != null) {
                    signedTransaction.sign(privateKey, mWebSocketClient.getmChainIdObject());
                    break;
                }
            }
        }
        return signedTransaction;
    }

    public SignedTransaction getSignedTransactionByENotes(CardManager cardManager, Card card, AccountObject accountObject, Operations.base_operation operation, int operationId, DynamicGlobalPropertyObject dynamicGlobalPropertyObject) {
        SignedTransaction signedTransaction = new SignedTransaction();
        Operations.operation_type operationType = new Operations.operation_type();
        operationType.nOperationType = operationId;
        operationType.operationContent = operation;
        signedTransaction.operationTypes = new ArrayList<>();
        signedTransaction.operationTypes.add(operationType);
        signedTransaction.operations = new ArrayList<>();
        List<Object> listInOperations = new ArrayList<>();
        listInOperations.add(operationId);
        listInOperations.add(operation);
        signedTransaction.operations.add(listInOperations);
        signedTransaction.extensions = new HashSet<>();

        signedTransaction.set_reference_block(dynamicGlobalPropertyObject.head_block_id);

        Date dateObject = dynamicGlobalPropertyObject.time;
        Calendar calender = Calendar.getInstance();
        calender.setTime(dateObject);
        calender.add(Calendar.SECOND, 30);
        dateObject = calender.getTime();

        signedTransaction.set_expiration(dateObject);
        Types.private_key_type privateKey = null;
        signedTransaction.signByENotes(cardManager, card, privateKey, mWebSocketClient.getmChainIdObject());
        return signedTransaction;
    }

    public String getChatMessageSignature(AccountObject accountObject, String message) {
        Sha256Object.encoder encoder = new Sha256Object.encoder();
        if(message != null){
            byte[] assetByte = message.getBytes();
            encoder.write(assetByte);
        }
        Types.private_key_type privateKey;
        CompactSignature signature;
        for (Types.public_key_type public_key_type : accountObject.active.get_keys()) {
            privateKey = mHashMapPub2Priv.get(public_key_type);
            if (privateKey != null) {
                signature = privateKey.getPrivateKey().sign_compact(encoder.result(), true);
                return MyUtils.bytesToHex(signature.data);
            }
        }

        for (Types.public_key_type public_key_type : accountObject.owner.get_keys()) {
            privateKey = mHashMapPub2Priv.get(public_key_type);
            if (privateKey != null) {
                signature = privateKey.getPrivateKey().sign_compact(encoder.result(), true);
                return MyUtils.bytesToHex(signature.data);
            }
        }

        return null;
    }

    public String getWithdrawDepositSignature(AccountObject accountObject, Operations.base_operation operation) {
        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.operation = operation;
        Types.private_key_type privateKey;
        for (Types.public_key_type public_key_type : accountObject.active.get_keys()) {
             privateKey = mHashMapPub2Priv.get(public_key_type);
            if (privateKey != null) {
                return signedTransaction.sign(privateKey);
            }
        }

        for (Types.public_key_type public_key_type : accountObject.owner.get_keys()) {
            privateKey = mHashMapPub2Priv.get(public_key_type);
            if (privateKey != null) {
                return signedTransaction.sign(privateKey);
            }
        }
        return null;
    }

    public String getMemoMessage(MemoData memoData) {
        try {
            if (memoData != null) {
                Types.public_key_type memoKeyFrom = new Types.public_key_type(memoData.from.toString());
                Types.public_key_type memoKeyTo = new Types.public_key_type(memoData.to.toString());
                if (mHashMapPub2Priv.get(memoKeyFrom) != null) {
                    return memoData.get_message(mHashMapPub2Priv.get(memoKeyFrom).getPrivateKey(), memoKeyTo.getPublicKey(), memoData.message, memoData.nonce);
                } else if (mHashMapPub2Priv.get(memoKeyTo) != null) {
                    return memoData.get_message(mHashMapPub2Priv.get(memoKeyTo).getPrivateKey(), memoKeyFrom.getPublicKey(), memoData.message, memoData.nonce);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }



//    public signed_transaction transfer(String strFrom,
//                                       String strTo,
//                                       String strAmount,
//                                       String strAssetSymbol,
//                                       String strMemo) throws NetworkStatusException {
//
//        ObjectId<AssetObject> assetObjectId = ObjectId.create_from_string(strAssetSymbol);
//        AssetObject assetObject = null;
//        if (assetObjectId == null) {
//            assetObject = lookup_asset_symbols(strAssetSymbol);
//        } else {
//            List<ObjectId<AssetObject>> listAssetObjectId = new ArrayList<>();
//            listAssetObjectId.add(assetObjectId);
//            assetObject = get_assets(listAssetObjectId).get(0);
//        }
//
//        AccountObject accountObjectFrom = get_account(strFrom);
//        AccountObject accountObjectTo = get_account(strTo);
//        if (accountObjectTo == null) {
//            return null;
//        }
//
//        Operations.transfer_operation transferOperation = new Operations.transfer_operation();
//        transferOperation.from = accountObjectFrom.id;
//        transferOperation.to = accountObjectTo.id;
//        transferOperation.amount = assetObject.amount_from_string(strAmount);
//        transferOperation.extensions = new HashSet<>();
//        if (TextUtils.isEmpty(strMemo) == false) {
//            transferOperation.memo = new memo_data();
//            transferOperation.memo.from = accountObjectFrom.options.memo_key;
//            transferOperation.memo.to = accountObjectTo.options.memo_key;
//
//            Types.private_key_type privateKeyType = mHashMapPub2Priv.get(accountObjectFrom.options.memo_key);
//            if (privateKeyType == null) {
//                // // TODO: 07/09/2017 获取失败的问题
//            }
//            transferOperation.memo.set_message(
//                    privateKeyType.getPrivateKey(),
//                    accountObjectTo.options.memo_key.getPublicKey(),
//                    strMemo,
//                    0
//            );
//            transferOperation.memo.get_message(
//                    privateKeyType.getPrivateKey(),
//                    accountObjectTo.options.memo_key.getPublicKey()
//            );
//        }
//
//        Operations.operation_type operationType = new Operations.operation_type();
//        operationType.nOperationType = Operations.ID_TRANSER_OPERATION;
//        operationType.operationContent = transferOperation;
//
//        signed_transaction tx = new signed_transaction();
//        tx.Operations = new ArrayList<>();
//        tx.Operations.add(operationType);
//        tx.extensions = new HashSet<>();
//        set_operation_fees(tx, get_global_properties().parameters.current_fees);
//
//
//        //// TODO: 07/09/2017 tx.validate();
//        return sign_transaction(tx);
//    }

//    public Asset calculate_sell_asset_fee(String amountToSell, AssetObject assetToSell,
//                                          String minToReceive, AssetObject assetToReceive,
//                                          global_property_object globalPropertyObject) {
//        Operations.limit_order_create_operation op = new Operations.limit_order_create_operation();
//        op.amount_to_sell = assetToSell.amount_from_string(amountToSell);
//        op.min_to_receive = assetToReceive.amount_from_string(minToReceive);
//
//        Operations.operation_type operationType = new Operations.operation_type();
//        operationType.nOperationType = Operations.ID_CREATE_LIMIT_ORDER_OPERATION;
//        operationType.operationContent = op;
//
//        signed_transaction tx = new signed_transaction();
//        tx.Operations = new ArrayList<>();
//        tx.Operations.add(operationType);
//
//        tx.extensions = new HashSet<>();
//        set_operation_fees(tx, globalPropertyObject.parameters.current_fees);
//
//        return op.fee;
//    }

//    public Asset calculate_sell_fee(AssetObject assetToSell, AssetObject assetToReceive,
//                                    double rate, double amount,
//                                    global_property_object globalPropertyObject) {
//        return calculate_sell_asset_fee(Double.toString(amount), assetToSell,
//                Double.toString(rate * amount), assetToReceive, globalPropertyObject);
//    }

//    public Asset calculate_buy_fee(AssetObject assetToReceive, AssetObject assetToSell,
//                                   double rate, double amount,
//                                   global_property_object globalPropertyObject) {
//        return calculate_sell_asset_fee(Double.toString(rate * amount), assetToSell,
//                Double.toString(amount), assetToReceive, globalPropertyObject);
//    }

//    public signed_transaction sell_asset(String amountToSell, String symbolToSell,
//                                         String minToReceive, String symbolToReceive,
//                                         int timeoutSecs, boolean fillOrKill)
//            throws NetworkStatusException {
//        // 这是用于出售的帐号
//        AccountObject accountObject = list_my_accounts().get(0);
//        Operations.limit_order_create_operation op = new Operations.limit_order_create_operation();
//        op.seller = accountObject.id;
//
//        // 填充数据
//        op.amount_to_sell = lookup_asset_symbols(symbolToSell).amount_from_string(amountToSell);
//        op.min_to_receive = lookup_asset_symbols(symbolToReceive).amount_from_string(minToReceive);
//        if (timeoutSecs > 0) {
//            op.expiration = new Date(
//                    System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSecs));
//        } else {
//            op.expiration = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365));
//        }
//        op.fill_or_kill = fillOrKill;
//        op.extensions = new HashSet<>();
//
//        Operations.operation_type operationType = new Operations.operation_type();
//        operationType.nOperationType = Operations.ID_CREATE_LIMIT_ORDER_OPERATION;
//        operationType.operationContent = op;
//
//        signed_transaction tx = new signed_transaction();
//        tx.Operations = new ArrayList<>();
//        tx.Operations.add(operationType);
//
//        tx.extensions = new HashSet<>();
//        set_operation_fees(tx, get_global_properties().parameters.current_fees);
//
//        return sign_transaction(tx);
//    }

//    /**
//     * @param symbolToSell 卖出的货币符号
//     * @param symbolToReceive 买入的货币符号
//     * @param rate 多少个<t>symbolToReceive</t>可以兑换一个<t>symbolToSell</t>
//     * @param amount 要卖出多少个<t>symbolToSell</t>
//     * @throws NetworkStatusException
//     */
//    public signed_transaction sell(String symbolToSell, String symbolToReceive, double rate,
//                                   double amount) throws NetworkStatusException {
//        return sell_asset(Double.toString(amount), symbolToSell, Double.toString(rate * amount),
//                symbolToReceive, 0, false);
//    }
//
//    public signed_transaction sell(String symbolToSell, String symbolToReceive, double rate,
//                                   double amount, int timeoutSecs) throws NetworkStatusException {
//        return sell_asset(Double.toString(amount), symbolToSell, Double.toString(rate * amount),
//                symbolToReceive, timeoutSecs, false);
//    }

//    /**
//     * @param symbolToReceive 买入的货币符号
//     * @param symbolToSell 卖出的货币符号
//     * @param rate 多少个<t>symbolToSell</t>可以兑换一个<t>symbolToReceive</t>
//     * @param amount 要买入多少个<t>symbolToReceive</t>
//     * @throws NetworkStatusException
//     */
//    public signed_transaction buy(String symbolToReceive, String symbolToSell, double rate,
//                                  double amount) throws NetworkStatusException {
//        return sell_asset(Double.toString(rate * amount), symbolToSell, Double.toString(amount),
//                symbolToReceive, 0, false);
//    }

//    public signed_transaction buy(String symbolToReceive, String symbolToSell, double rate,
//                                  double amount, int timeoutSecs) throws NetworkStatusException {
//        return sell_asset(Double.toString(rate * amount), symbolToSell, Double.toString(amount),
//                symbolToReceive, timeoutSecs, false);
//    }

//    public signed_transaction cancel_order(ObjectId<LimitOrderObject> id)
//            throws NetworkStatusException {
//        Operations.limit_order_cancel_operation op = new Operations.limit_order_cancel_operation();
//        op.fee_paying_account = mWebSocketClient.get_limit_order(id).seller;
//        op.order = id;
//        op.extensions = new HashSet<>();
//
//        Operations.operation_type operationType = new Operations.operation_type();
//        operationType.nOperationType = Operations.ID_CANCEL_LMMIT_ORDER_OPERATION;
//        operationType.operationContent = op;
//
//        signed_transaction tx = new signed_transaction();
//        tx.Operations = new ArrayList<>();
//        tx.Operations.add(operationType);
//
//        tx.extensions = new HashSet<>();
//        set_operation_fees(tx, get_global_properties().parameters.current_fees);
//
//        return sign_transaction(tx);
//    }

//    public global_property_object get_global_properties() throws NetworkStatusException {
//        return mWebSocketClient.get_global_properties();
//    }

//    public void create_account_with_private_key(PrivateKey privateOwnerKey,
//                                                String strAccountName,
//                                                String strPassword,
//                                                String strRegistar,
//                                                String strReferrer) throws NetworkStatusException {
//        int nActiveKeyIndex = find_first_unused_derived_key_index(privateOwnerKey);
//
//        String strWifKey = new Types.private_key_type(privateOwnerKey).toString();
//        PrivateKey privateActiveKey = derive_private_key(strWifKey, nActiveKeyIndex);
//
//        strWifKey = new Types.private_key_type(privateActiveKey).toString();
//        int nMemoKeyIndex = find_first_unused_derived_key_index(privateActiveKey);
//        PrivateKey privateMemoKey = derive_private_key(strWifKey, nMemoKeyIndex);
//
//        Types.public_key_type publicOwnerKey = new Types.public_key_type(privateOwnerKey.get_public_key());
//        Types.public_key_type publicActiveKey = new Types.public_key_type(privateActiveKey.get_public_key());
//        Types.public_key_type publicMemoKey = new Types.public_key_type(privateMemoKey.get_public_key());
//
//        Operations.account_create_operation operation = new Operations.account_create_operation();
//        operation.name = strAccountName;
//        operation.options.memo_key = publicMemoKey;
//        operation.active = new Authority(1, publicActiveKey, 1);
//        operation.owner = new Authority(1, publicOwnerKey, 1);
//
//        AccountObject accountRegistrar = get_account(strRegistar);
//        AccountObject accountReferr = get_account(strReferrer);
//
//        operation.referrer = accountReferr.id;
//        operation.registrar = accountRegistrar.id;
//        operation.referrer_percent = accountReferr.referrer_rewards_percentage;
//
//        global_property_object globalPropertyObject = mWebSocketClient.get_global_properties();
//        dynamic_global_property_object dynamicGlobalPropertyObject = mWebSocketClient.get_dynamic_global_properties();
//
//    }

//    private SignedTransaction sign_transaction(SignedTransaction tx) throws NetworkStatusException {
//        // // TODO: 07/09/2017 这里的set应出问题
//        SignedTransaction.required_authorities requiresAuthorities = tx.get_required_authorities();
//
//        Set<ObjectId<AccountObject>> req_active_approvals = new HashSet<>();
//        req_active_approvals.addAll(requiresAuthorities.active);
//
//        Set<ObjectId<AccountObject>> req_owner_approvals = new HashSet<>();
//        req_owner_approvals.addAll(requiresAuthorities.owner);
//
//
//        for (Authority authorityObject : requiresAuthorities.other) {
//            for (ObjectId<AccountObject> accountObjectId : authorityObject.account_auths.keySet()) {
//                req_active_approvals.add(accountObjectId);
//            }
//        }
//
//        Set<ObjectId<AccountObject>> accountObjectAll = new HashSet<>();
//        accountObjectAll.addAll(req_active_approvals);
//        accountObjectAll.addAll(req_owner_approvals);
//
//
//        List<ObjectId<AccountObject>> listAccountObjectId = new ArrayList<>();
//        listAccountObjectId.addAll(accountObjectAll);
//
//        List<account_object> listAccountObject = get_accounts(listAccountObjectId);
//        HashMap<object_id<account_object>, account_object> hashMapIdToObject = new HashMap<>();
//        for (account_object accountObject : listAccountObject) {
//            hashMapIdToObject.put(accountObject.id, accountObject);
//        }
//
//        HashSet<types.public_key_type> approving_key_set = new HashSet<>();
//        for (object_id<account_object> accountObjectId : req_active_approvals) {
//            account_object accountObject = hashMapIdToObject.get(accountObjectId);
//            approving_key_set.addAll(accountObject.active.get_keys());
//        }
//
//        for (object_id<account_object> accountObjectId : req_owner_approvals) {
//            account_object accountObject = hashMapIdToObject.get(accountObjectId);
//            approving_key_set.addAll(accountObject.owner.get_keys());
//        }
//
//        for (authority authorityObject : requiresAuthorities.other) {
//            for (types.public_key_type publicKeyType : authorityObject.get_keys()) {
//                approving_key_set.add(publicKeyType);
//            }
//        }
//
//        // // TODO: 07/09/2017 被简化了
//        DynamicGlobalPropertyObject dynamicGlobalPropertyObject = get_dynamic_global_properties();
//        tx.set_reference_block(dynamicGlobalPropertyObject.head_block_id);
//
//        Date dateObject = dynamicGlobalPropertyObject.time;
//        Calendar calender = Calendar.getInstance();
//        calender.setTime(dateObject);
//        calender.add(Calendar.SECOND, 30);
//
//        dateObject = calender.getTime();
//
//        tx.set_expiration(dateObject);
//
//        for (types.public_key_type pulicKeyType : approving_key_set) {
//            types.private_key_type privateKey = mHashMapPub2Priv.get(pulicKeyType);
//            if (privateKey != null) {
//                tx.sign(privateKey, mWalletObject.chain_id);
//            }
//        }
//
//        // 发出tx，进行广播，这里也涉及到序列化
//        mWebSocketClient.broadcast_transaction(tx);
//
//    }

    public void get_market_history(ObjectId<AssetObject> baseAssetId,
                                   ObjectId<AssetObject> quoteAssetId,
                                   int nBucket,
                                   Date dateStart,
                                   Date dateEnd,
                                   MessageCallback<Reply<List<BucketObject>>> callback) throws NetworkStatusException {
        mWebSocketClient.get_market_history(baseAssetId, quoteAssetId, nBucket, dateStart, dateEnd, callback);
    }

    public void get_dynamic_global_properties(MessageCallback<Reply<DynamicGlobalPropertyObject>> callback) throws NetworkStatusException {
        mWebSocketClient.get_dynamic_global_properties(callback);
    }

//    private void set_operation_fees(signed_transaction tx, fee_schedule feeSchedule) {
//        for (Operations.operation_type operationType : tx.Operations) {
//            feeSchedule.set_fee(operationType, Price.unit_price(new ObjectId<AssetObject>(0, AssetObject.class)));
//        }
//    }


//    private PrivateKey derive_private_key(String strWifKey, int nSeqNumber) {
//        String strData = strWifKey + " " + nSeqNumber;
//        byte[] bytesBuffer = strData.getBytes();
//        SHA512Digest digest = new SHA512Digest();
//        digest.update(bytesBuffer, 0, bytesBuffer.length);
//
//        byte[] out = new byte[64];
//        digest.doFinal(out, 0);
//
//        SHA256Digest digest256 = new SHA256Digest();
//        byte[] outSeed = new byte[32];
//        digest256.update(out, 0, out.length);
//        digest.doFinal(outSeed, 0);
//
//        return new PrivateKey(outSeed);
//    }

//    private int find_first_unused_derived_key_index(PrivateKey privateKey) {
//        int first_unused_index = 0;
//        int number_of_consecutive_unused_keys = 0;
//
//        String strWifKey = new Types.private_key_type(privateKey).toString();
//        for (int key_index = 0; ; ++key_index) {
//            PrivateKey derivedPrivateKey = derive_private_key(strWifKey, key_index);
//            Types.public_key_type publicKeyType = new Types.public_key_type(derivedPrivateKey.get_public_key());
//
//            if (mHashMapPub2Priv.containsKey(publicKeyType) == false) {
//                if (number_of_consecutive_unused_keys != 0)
//                {
//                    ++number_of_consecutive_unused_keys;
//                    if (number_of_consecutive_unused_keys > 5)
//                        return first_unused_index;
//                }
//                else
//                {
//                    first_unused_index = key_index;
//                    number_of_consecutive_unused_keys = 1;
//                }
//            } else {
//                first_unused_index = 0;
//                number_of_consecutive_unused_keys = 0;
//            }
//        }
//    }

//    public String decrypt_memo_message(memo_data memoData) {
//        assert(is_locked() == false);
//        String strMessage = null;
//
//        Types.private_key_type privateKeyType = mHashMapPub2Priv.get(memoData.to);
//        if (privateKeyType != null) {
//            strMessage = memoData.get_message(privateKeyType.getPrivateKey(), memoData.from.getPublicKey());
//        } else {
//            privateKeyType = mHashMapPub2Priv.get(memoData.from);
//            if (privateKeyType != null) {
//                strMessage = memoData.get_message(privateKeyType.getPrivateKey(), memoData.to.getPublicKey());
//            }
//        }
//
//        return strMessage;
//    }

    public void subscribe_to_market(String id,
                                    String base,
                                    String quote,
                                    MessageCallback<Reply<String>> callback) throws NetworkStatusException {
        mWebSocketClient.subscribe_to_market(id, base, quote, callback);
    }

    public AtomicInteger getCallId() {
        return mWebSocketClient.getCallId();
    }

//    public void set_subscribe_market(boolean filter) throws NetworkStatusException {
//        mWebSocketClient.set_subscribe_callback(filter);
//    }

    public void get_ticker(String base,
                           String quote,
                           MessageCallback<Reply<MarketTicker>> callback) throws NetworkStatusException {
        mWebSocketClient.get_ticker(base, quote, callback);
    }

//    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
//            throws NetworkStatusException {
//        return mWebSocketClient.get_trade_history(base, quote, start, end, limit);
//    }

    public void get_fill_order_history(ObjectId<AssetObject> base,
                                       ObjectId<AssetObject> quote,
                                       int limit,
                                       MessageCallback<Reply<List<HashMap<String, Object>>>> callback) throws NetworkStatusException {
        mWebSocketClient.get_fill_order_history(base, quote,limit, callback);
    }

    public void get_limit_orders(ObjectId<AssetObject> base,
                                 ObjectId<AssetObject> quote,
                                 int limit,
                                 MessageCallback<Reply<List<LimitOrderObject>>> callback) throws NetworkStatusException {
        mWebSocketClient.get_limit_orders(base, quote, limit, callback);
    }

    public void get_balance_objects(List<String> addresses,
                                    MessageCallback<Reply<List<LockAssetObject>>> callback) throws NetworkStatusException {
        mWebSocketClient.get_balance_objects(addresses, callback);
    }

    public void get_full_accounts(List<String> names,
                                  boolean subscribe,
                                  MessageCallback<Reply<List<FullAccountObjectReply>>> callback)
            throws NetworkStatusException {
        mWebSocketClient.get_full_accounts(names, subscribe, callback);
    }

    public void get_required_fees(String assetId,
                                  int operationId,
                                  Operations.base_operation operation,
                                  MessageCallback<Reply<List<FeeAmountObject>>> callback)
            throws NetworkStatusException {
        mWebSocketClient.get_required_fees(assetId, operationId, operation, callback);
    }

    public void broadcast_transaction_with_callback(SignedTransaction signedTransaction,
                                                    MessageCallback<Reply<String>> callback) throws NetworkStatusException {
        mWebSocketClient.broadcast_transaction_with_callback(signedTransaction, callback);
    }

}
