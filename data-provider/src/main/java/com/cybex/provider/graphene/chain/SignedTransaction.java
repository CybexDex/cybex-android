package com.cybex.provider.graphene.chain;

import android.util.Log;

import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.utils.MyUtils;

import org.bitcoinj.core.ECKey;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.core.CardManager;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.card.Commands;
import io.enotes.sdk.repository.card.TLVBox;
import io.enotes.sdk.repository.db.entity.Card;
import mrd.bitlib.crypto.Signature;
import mrd.bitlib.crypto.SignedMessage;
import mrd.bitlib.util.Sha256Hash;

public class SignedTransaction extends Transaction {
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    transient List<CompactSignature> SignaturesBuffer = new ArrayList<>();
    List<String> signatures = new ArrayList<>();

    public void sign(Types.private_key_type privateKeyType, Sha256Object chain_id) {
        Sha256Object digest = sig_digest(chain_id);
        SignaturesBuffer.add(privateKeyType.getPrivateKey().sign_compact(digest, true));
        signatures.add(bytesToHex(privateKeyType.getPrivateKey().sign_compact(digest, true).data));
    }

    public void signByENotes(CardManager cardManager, Card card, Types.private_key_type privateKeyType, Sha256Object chain_id) {
        Sha256Object digest = sig_digest(chain_id);
        Log.e("sha256Digest", MyUtils.bytesToHex(digest.hash));
        CompactSignature compactSignature = sign_compactByENotes(cardManager, card, digest, true);
        SignaturesBuffer.add(compactSignature);
        if (compactSignature != null)
        signatures.add(bytesToHex(compactSignature.data));
    }

    public String sign(Types.private_key_type privateKeyType) {
        Sha256Object digest = sig_digest();
        SignaturesBuffer.add(privateKeyType.getPrivateKey().sign_compact(digest, true));
        signatures.add(bytesToHex(privateKeyType.getPrivateKey().sign_compact(digest, true).data));
        Log.e("withdraw_deposit_hash", signatures.get(0));
        return signatures.get(0);
    }

    public CompactSignature sign_compactByENotes(CardManager cardManager, Card card, Sha256Object digest, boolean require_canonical) {
        CompactSignature signature = null;
        try {


            while (true) {
                Sha256Hash a = new Sha256Hash(digest.hash);
                Log.e("sha256hash",a.toHex());
                SignedMessage signedMessage = signHashByENotes(cardManager, card, new Sha256Hash(digest.hash));
                byte[] byteCompact = signedMessage.bitcoinEncodingOfSignature();
                signature = new CompactSignature(byteCompact);

                boolean bResult = PublicKey.is_canonical(signature);
                if (bResult == true) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return signature;
    }

    public SignedMessage signHashByENotes(CardManager cardManager, Card card, Sha256Hash hashToSign) {
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Transaction_Hash, hashToSign.getBytes());

        try {
            byte[] bytes = ByteUtil.hexStringToBytes(cardManager.transmitApdu(Commands.signTX(tlvBox.serialize())));
            TLVBox signatureTLV = TLVBox.parse(bytes, 0, bytes.length);
            String signature = signatureTLV.getStringValue(Commands.TLVTag.Transaction_signature);
            if (signature.length() != 128) {
                throw new CommandException(ErrorCode.INVALID_CARD, "please_right_card");
            }
            String r = signature.substring(0, 64);
            String s = signature.substring(64);
            ECKey.ECDSASignature signature1 = new ECKey.ECDSASignature(new BigInteger(r, 16), new BigInteger(s, 16)).toCanonicalised();
            Signature sig = new Signature(signature1.r,signature1.s);

            // Now we have to work backwards to figure out the recId needed to recover the signature.
            mrd.bitlib.crypto.PublicKey targetPubKey = new mrd.bitlib.crypto.PublicKey(card.getBitCoinECKey().getPubKeyPoint().getEncoded(true));
            boolean compressed = targetPubKey.isCompressed();
            int recId = -1;
            for (int i = 0; i < 4; i++) {

                mrd.bitlib.crypto.PublicKey k = SignedMessage.recoverFromSignature(i, sig, hashToSign, compressed);
                if (k != null && targetPubKey.equals(k)) {
                    recId = i;
                    break;
                }
            }
            return SignedMessage.from(sig, targetPubKey, recId);
        } catch (CommandException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
