package io.enotes.sdk.utils;

import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.STObject;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.BytesSink;
import com.ripple.core.serialized.MultiSink;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.utils.HashUtils;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

import java.util.Arrays;

import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.repository.provider.CardProvider;

public class XrpRawTransaction {
    private static final String TAG = "XrpRawTransaction";
    public Transaction txn;
    public Hash256 hash;
    public byte[] signingData;
    public byte[] previousSigningData;
    public String tx_blob;

    public String createRawTransaction(Card card, CardProvider cardProvider, String toAddress, String amount, int sequence, String fee, long destinationTag) throws CommandException {
        Payment payment = new Payment();
        payment.as(AccountID.Account, card.getAddress());
        payment.as(AccountID.Destination, toAddress);
        payment.as(Amount.Amount, amount);
        payment.as(UInt32.Sequence, sequence);
        payment.as(Amount.Fee, fee);
        this.txn = (Transaction) STObject.translate.fromBytes(payment.toBytes());
        Blob pubKey = new Blob(card.getBitCoinECKey().getPubKeyPoint().getEncoded(true));

        this.txn.signingPubKey(pubKey);
        this.txn.setCanonicalSignatureFlag();
        if (destinationTag > 0)
            this.txn.put(UInt32.DestinationTag, new UInt32(destinationTag));
        this.txn.checkFormat();
        this.signingData = this.txn.signingData();

        byte[] sha512 = HashUtils.halfSha512(signingData);
        LogUtils.i(TAG, "payment.prettyJSON:" + txn.prettyJSON());
        LogUtils.i(TAG, "txn.tx:" + ByteUtil.toHexString(txn.signingData()));
        String signature = null;
        if (this.previousSigningData == null || !Arrays.equals(this.signingData, this.previousSigningData)) {
            try {
                signature = cardProvider.verifyCoinAndSignTx(card, sha512);
                signature = ByteUtil.toHexString(SignatureUtils.str2BtcSignature(signature).encodeToDER());
                LogUtils.i(TAG, "signature->" + signature);
                txn.txnSignature(new Blob(ByteUtil.hexStringToBytes(signature)));
                BytesList blob = new BytesList();
                HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);
                this.txn.toBytesSink(new MultiSink(new BytesSink[]{blob, id}));
                this.tx_blob = blob.bytesHex();
                this.hash = id.finish();
            } catch (CommandException e) {
                e.printStackTrace();
                this.previousSigningData = null;
                throw e;
            }
            this.previousSigningData = this.signingData;
            LogUtils.i(TAG, "txn.prettyJSON:" + txn.prettyJSON());
            LogUtils.i(TAG, "txn.raw:" + hash.toHex());
            return tx_blob;
        }
        return null;
    }
}
