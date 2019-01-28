package com.cybex.provider.graphene.chain;

import android.util.Log;

import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.fc.io.BaseEncoder;
import com.cybex.provider.fc.io.RawType;
import com.cybex.provider.utils.MyUtils;
import com.google.common.primitives.UnsignedInteger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.bitcoinj.wallet.Wallet;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.cybex.provider.graphene.chain.Config.GRAPHENE_BLOCKCHAIN_PRECISION;

public class Operations {
    public static final int ID_TRANSER_OPERATION = 0;
    public static final int ID_CREATE_LIMIT_ORDER_OPERATION = 1;
    public static final int ID_CANCEL_LMMIT_ORDER_OPERATION = 2;
    public static final int ID_UPDATE_LMMIT_ORDER_OPERATION = 3;
    public static final int ID_FILL_LMMIT_ORDER_OPERATION = 4;
    public static final int ID_CREATE_ACCOUNT_OPERATION = 5;
    public static final int ID_WITHDRAW_DEPOSIT_OPERATION = 6;
    public static final int ID_BALANCE_CLAIM_OPERATION = 37;

    public static operation_id_map operations_map = new operation_id_map();

    public static class operation_id_map {
        private HashMap<Integer, Type> mHashId2Operation = new HashMap<>();
        private HashMap<Integer, Type> mHashId2OperationFee = new HashMap<>();

        public operation_id_map() {

            mHashId2Operation.put(ID_TRANSER_OPERATION, transfer_operation.class);
            mHashId2Operation.put(ID_CREATE_LIMIT_ORDER_OPERATION, limit_order_create_operation.class);
            mHashId2Operation.put(ID_CANCEL_LMMIT_ORDER_OPERATION, limit_order_cancel_operation.class);
            mHashId2Operation.put(ID_UPDATE_LMMIT_ORDER_OPERATION, call_order_update_operation.class);
            mHashId2Operation.put(ID_FILL_LMMIT_ORDER_OPERATION, fill_order_operation.class);
            mHashId2Operation.put(ID_CREATE_ACCOUNT_OPERATION, account_create_operation.class);
            mHashId2Operation.put(ID_WITHDRAW_DEPOSIT_OPERATION, withdraw_deposit_history_operation.class);
            mHashId2Operation.put(ID_BALANCE_CLAIM_OPERATION, balance_claim_operation.class);

            mHashId2OperationFee.put(ID_TRANSER_OPERATION, transfer_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_CREATE_LIMIT_ORDER_OPERATION, limit_order_create_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_CANCEL_LMMIT_ORDER_OPERATION, limit_order_cancel_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_UPDATE_LMMIT_ORDER_OPERATION, call_order_update_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_FILL_LMMIT_ORDER_OPERATION, fill_order_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_CREATE_ACCOUNT_OPERATION, account_create_operation.fee_parameters_type.class);
            mHashId2OperationFee.put(ID_WITHDRAW_DEPOSIT_OPERATION, withdraw_deposit_history_operation.class);
        }

        public Type getOperationObjectById(int nId) {
            return mHashId2Operation.get(nId);
        }

        public Type getOperationFeeObjectById(int nId) {
            return mHashId2OperationFee.get(nId);
        }
    }

    public static class operation_type {
        public int nOperationType;
        public Object operationContent;

        public static class operation_type_deserializer implements JsonDeserializer<operation_type> {
            @Override
            public operation_type deserialize(JsonElement json,
                                              Type typeOfT,
                                              JsonDeserializationContext context) throws JsonParseException {
                operation_type operationType = new operation_type();
                JsonArray jsonArray = json.getAsJsonArray();

                operationType.nOperationType = jsonArray.get(0).getAsInt();
                Type type = operations_map.getOperationObjectById(operationType.nOperationType);


                if (type != null) {
                    operationType.operationContent = context.deserialize(jsonArray.get(1), type);
                } else {
                    operationType.operationContent = context.deserialize(jsonArray.get(1), Object.class);
                }

                return operationType;
            }
        }

        public static class operation_type_serializer implements JsonSerializer<operation_type> {

            @Override
            public JsonElement serialize(operation_type src, Type typeOfSrc, JsonSerializationContext context) {
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(src.nOperationType);
                Type type = operations_map.getOperationObjectById(src.nOperationType);

                assert (type != null);
                jsonArray.add(context.serialize(src.operationContent, type));

                return jsonArray;
            }
        }
    }

    ;

    public interface base_operation {
        List<Authority> get_required_authorities();

        List<ObjectId<AccountObject>> get_required_active_authorities();

        List<ObjectId<AccountObject>> get_required_owner_authorities();

        void write_to_encoder(BaseEncoder baseEncoder);

        long calculate_fee(Object objectFeeParameter);

        void set_fee(Asset fee);

        ObjectId<AccountObject> fee_payer();

        List<ObjectId<AccountObject>> get_account_id_list();

        List<ObjectId<AssetObject>> get_asset_id_list();
    }


    public static class transfer_operation implements base_operation, Serializable {
        public static class fee_parameters_type {
            long fee = 20 * GRAPHENE_BLOCKCHAIN_PRECISION;
            long price_per_kbyte = 10 * GRAPHENE_BLOCKCHAIN_PRECISION; /// only required for large memos.
        }

        ;

        public Asset fee;
        public ObjectId<AccountObject> from;
        public ObjectId<AccountObject> to;
        public Asset amount;
        public MemoData memo;
        //public extensions_type   extensions;
        public transient Types.public_key_type public_key_type;
        public transient long vesting_period;
        public Set<Set<Object>> extensions;

        @Override
        public List<Authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            List<ObjectId<AccountObject>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {
            RawType rawObject = new RawType();
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            //baseEncoder.write(rawObject.get_byte_array(fee.asset_id.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));
            //baseEncoder.write(rawObject.get_byte_array(from.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(from.get_instance()));
            //baseEncoder.write(rawObject.get_byte_array(to.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(to.get_instance()));
            baseEncoder.write(rawObject.get_byte_array(amount.amount));
            //baseEncoder.write(rawObject.get_byte_array(amount.asset_id.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(amount.asset_id.get_instance()));
            baseEncoder.write(rawObject.get_byte(memo != null));
            if (memo != null) {
                baseEncoder.write(memo.from.key_data);
                baseEncoder.write(memo.to.key_data);
                baseEncoder.write(rawObject.get_byte_array(memo.unsignedNonce));
                byte[] byteMessage = memo.messageBuffer.array();
                rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(byteMessage.length));
                baseEncoder.write(byteMessage);
            }

            //baseEncoder.write(rawObject.get_byte_array(extensions.size()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(1));
            baseEncoder.write(rawObject.get_byte_array(vesting_period));
            baseEncoder.write(public_key_type.key_data);
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert (fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type) objectFeeParameter;


            return calculate_fee(feeParametersType);
        }

        @Override
        public void set_fee(Asset assetFee) {
            fee = assetFee;
        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return from;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            List<ObjectId<AccountObject>> listAccountId = new ArrayList<>();
            listAccountId.add(from);
            listAccountId.add(to);
            return listAccountId;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            List<ObjectId<AssetObject>> listAssetId = new ArrayList<>();
            listAssetId.add(amount.asset_id);
            return listAssetId;
        }

        public long calculate_fee(fee_parameters_type feeParametersType) {
            long lFee = feeParametersType.fee;
            if (memo != null) {
                // 计算数据价格
                Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                BigInteger nSize = BigInteger.valueOf(gson.toJson(memo).length());
                BigInteger nPrice = BigInteger.valueOf(feeParametersType.price_per_kbyte);
                BigInteger nKbyte = BigInteger.valueOf(1024);
                BigInteger nAmount = nPrice.multiply(nSize).divide(nKbyte);

                lFee += nAmount.longValue();
            }

            return lFee;
        }
    }

    public static class limit_order_create_operation implements base_operation {
        static class fee_parameters_type {
            long fee = 5 * GRAPHENE_BLOCKCHAIN_PRECISION;
        }

        public Asset fee;
        public ObjectId<AccountObject> seller;
        public Asset amount_to_sell;
        public Asset min_to_receive;

        /// The order will be removed from the books if not filled by expiration
        /// Upon expiration, all unsold asset will be returned to seller
        public Date expiration; // = time_point_sec::maximum();

        /// If this flag is set the entire order must be filled or the operation is rejected
        public boolean fill_or_kill = false;
        public Set<Types.void_t> extensions;

        @Override
        public List<Authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            List<ObjectId<AccountObject>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {
            RawType rawObject = new RawType();

            // fee
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));

            // seller
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(seller.get_instance()));

            // amount_to_sell
            baseEncoder.write(rawObject.get_byte_array(amount_to_sell.amount));
            rawObject.pack(baseEncoder,
                    UnsignedInteger.fromIntBits(amount_to_sell.asset_id.get_instance()));

            // min_to_receive
            baseEncoder.write(rawObject.get_byte_array(min_to_receive.amount));
            rawObject.pack(baseEncoder,
                    UnsignedInteger.fromIntBits(min_to_receive.asset_id.get_instance()));

            // expiration
            baseEncoder.write(rawObject.get_byte_array(expiration));

            // fill_or_kill
            baseEncoder.write(rawObject.get_byte(fill_or_kill));

            // extensions
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert (fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type) objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(Asset fee) {
            this.fee = fee;
        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return seller;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            List<ObjectId<AccountObject>> listAccountId = new ArrayList<>();
            listAccountId.add(seller);
            return listAccountId;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            List<ObjectId<AssetObject>> listAssetId = new ArrayList<>();
            listAssetId.add(amount_to_sell.asset_id);
            listAssetId.add(min_to_receive.asset_id);
            return listAssetId;
        }
    }

    public static class limit_order_cancel_operation implements base_operation {
        class fee_parameters_type {
            long fee = 0;
        }

        ;

        public Asset fee;
        public ObjectId<LimitOrder> order;
        /**
         * must be order->seller
         */
        public ObjectId<AccountObject> fee_paying_account;
        public Set<Types.void_t> extensions;

        @Override
        public List<Authority> get_required_authorities() {
            return new ArrayList<>();
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            List<ObjectId<AccountObject>> activeList = new ArrayList<>();
            activeList.add(fee_payer());
            return activeList;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return new ArrayList<>();
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {
            RawType rawObject = new RawType();

            // fee
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));

            // fee_paying_account
            rawObject.pack(baseEncoder,
                    UnsignedInteger.fromIntBits(fee_paying_account.get_instance()));

            // order
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(order.get_instance()));

            // extensions
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(extensions.size()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            assert (fee_parameters_type.class.isInstance(objectFeeParameter));
            fee_parameters_type feeParametersType = (fee_parameters_type) objectFeeParameter;
            return feeParametersType.fee;
        }

        @Override
        public void set_fee(Asset fee) {
            this.fee = fee;
        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return fee_paying_account;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            List<ObjectId<AccountObject>> listAccountId = new ArrayList<>();
            listAccountId.add(fee_paying_account);
            return listAccountId;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            List<ObjectId<AssetObject>> listAssetId = new ArrayList<>();
            return listAssetId;
        }
    }

    public static class call_order_update_operation implements base_operation {
        /**
         * this is slightly more expensive than limit orders, this pricing impacts prediction markets
         */
        class fee_parameters_type {
            long fee = 20 * GRAPHENE_BLOCKCHAIN_PRECISION;
        }

        ;

        Asset fee;
        ObjectId<AccountObject> funding_account; ///< pays fee, collateral, and cover
        Asset delta_collateral; ///< the amount of collateral to add to the margin position
        Asset delta_debt; ///< the amount of the debt to be paid off, may be negative to issue new debt
        Set<Types.void_t> extensions;

        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(Asset fee) {

        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return funding_account;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            List<ObjectId<AccountObject>> listAccountId = new ArrayList<>();
            listAccountId.add(funding_account);
            return listAccountId;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            List<ObjectId<AssetObject>> listAssetId = new ArrayList<>();
            listAssetId.add(delta_collateral.asset_id);
            listAssetId.add(delta_debt.asset_id);
            return listAssetId;
        }
    }

    public static class fill_order_operation implements base_operation {
        class fee_parameters_type {
        }

        public ObjectId order_id;
        public ObjectId<AccountObject> account_id;
        public Asset pays;
        public Asset receives;
        public Asset fee; // paid by receiving account

        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(Asset fee) {

        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return account_id;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            List<ObjectId<AccountObject>> listAccountId = new ArrayList<>();
            listAccountId.add(account_id);

            return listAccountId;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            List<ObjectId<AssetObject>> listAssetId = new ArrayList<>();
            listAssetId.add(pays.asset_id);
            listAssetId.add(receives.asset_id);

            return listAssetId;
        }

    }

    public static class account_create_operation implements base_operation {
        class fee_parameters_type {
            long basic_fee = 5 * GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            long premium_fee = 2000 * GRAPHENE_BLOCKCHAIN_PRECISION; ///< the cost to register the cheapest non-free account
            int price_per_kbyte = GRAPHENE_BLOCKCHAIN_PRECISION;
        }

        public Asset fee;
        public ObjectId<AccountObject> registrar;
        public ObjectId<AccountObject> referrer;
        public int referrer_percent;
        public String name;
        public Authority owner;
        public Authority active;
        public Types.account_options options;


        public long calculate_fee(fee_parameters_type feeParametersType) {
            long lFeeRequired = feeParametersType.basic_fee;
            if (Utils.is_cheap_name(name) == false) {
                lFeeRequired = feeParametersType.premium_fee;
            }

            // // TODO: 07/09/2017  未完成
            return 0;

        }

        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(Asset fee) {

        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return registrar;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            List<ObjectId<AccountObject>> listAccountId = new ArrayList<>();
            listAccountId.add(registrar);
            listAccountId.add(referrer);

            return listAccountId;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            List<ObjectId<AssetObject>> listAssetId = new ArrayList<>();
            return listAssetId;
        }

    }

    public static class withdraw_deposit_history_operation implements base_operation {
        public String accountName;
        public String asset;
        public String fundType;
        transient public UnsignedInteger sizeInteger;
        transient public UnsignedInteger offsetInteger;
        public long size;
        public long offset;
        transient public Date expirationDate;
        public long expiration;




        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {
            RawType rawObject = new RawType();
            byte[] accountNameByte = accountName.getBytes();
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(accountNameByte.length));
            baseEncoder.write(accountNameByte);
            baseEncoder.write(rawObject.get_byte(asset != null));
            if (asset != null) {
                byte[] assetByte = asset.getBytes();
                rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(assetByte.length));
                baseEncoder.write(assetByte);
            }
            baseEncoder.write(rawObject.get_byte(fundType != null));
            if (fundType != null) {
                byte[] fundTypeByte = fundType.getBytes();
                rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fundTypeByte.length));
                baseEncoder.write(fundTypeByte);
            }

            baseEncoder.write(rawObject.get_byte(sizeInteger != null));
            if (sizeInteger != null) {
                baseEncoder.write(rawObject.get_byte_array(sizeInteger.intValue()));
            }
//            if (size != 0) {
//            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(size));
//            }
            baseEncoder.write(rawObject.get_byte(offsetInteger != null));
            if (offsetInteger != null) {
                baseEncoder.write(rawObject.get_byte_array(offsetInteger.intValue()));
            }
//            if (offset != 0) {
//            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(offset));
//            }
            baseEncoder.write(rawObject.get_byte_array(expirationDate));

        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(Asset fee) {

        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            return null;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            return null;
        }

        public void sign(Types.private_key_type privateKey) {
            Sha256Object.encoder enc = new Sha256Object.encoder();
            this.write_to_encoder(enc);
            byte[] signature = privateKey.getPrivateKey().sign_compact(enc.result(), true).data;
            Log.e("signWithdraw", MyUtils.bytesToHex(signature));
        }
    }

    public static class balance_claim_operation implements base_operation {
        public Asset fee;
        public ObjectId<AccountObject> deposit_to_account;
        public ObjectId<LockAssetObject> balance_to_claim;
        public Types.public_key_type balance_owner_key;
        public Asset total_claimed;

        @Override
        public List<Authority> get_required_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_active_authorities() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_required_owner_authorities() {
            return null;
        }

        @Override
        public void write_to_encoder(BaseEncoder baseEncoder) {
            RawType rawObject = new RawType();
            baseEncoder.write(rawObject.get_byte_array(fee.amount));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(fee.asset_id.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(deposit_to_account.get_instance()));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(balance_to_claim.get_instance()));
            baseEncoder.write(balance_owner_key.key_data);
            baseEncoder.write(rawObject.get_byte_array(total_claimed.amount));
            rawObject.pack(baseEncoder, UnsignedInteger.fromIntBits(total_claimed.asset_id.get_instance()));
        }

        @Override
        public long calculate_fee(Object objectFeeParameter) {
            return 0;
        }

        @Override
        public void set_fee(Asset fee) {

        }

        @Override
        public ObjectId<AccountObject> fee_payer() {
            return null;
        }

        @Override
        public List<ObjectId<AccountObject>> get_account_id_list() {
            return null;
        }

        @Override
        public List<ObjectId<AssetObject>> get_asset_id_list() {
            return null;
        }
    }
}
