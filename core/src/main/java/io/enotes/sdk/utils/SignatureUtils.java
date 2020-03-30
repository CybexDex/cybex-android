package io.enotes.sdk.utils;

import android.support.annotation.NonNull;

import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;

import java.math.BigInteger;
import java.util.Arrays;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.card.Commands;
import io.enotes.sdk.repository.card.ICardReader;
import io.enotes.sdk.repository.card.TLVBox;


public class SignatureUtils {
    private static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());

    /**
     * Convert str signature to org.ethereum.crypto.ECKey.ECDSASignature
     *
     * @param ecKey
     * @param messageHash
     * @param strSignature
     * @return
     */
    public static ECKey.ECDSASignature str2EthSignature(int network, @NonNull ECKey ecKey, @NonNull byte[] messageHash, @NonNull String strSignature) {
        if (strSignature.length() != 128) {
            throw new RuntimeException("signature illegal");
        }
        String r = strSignature.substring(0, 64);
        String s = strSignature.substring(64);
        ECKey.ECDSASignature sig = new ECKey.ECDSASignature(new BigInteger(r, 16), new BigInteger(s, 16)).toCanonicalised();
        int recId = -1;
        byte[] thisKey = ecKey.getPubKey();

        for (int i = 0; i < 4; ++i) {
            byte[] k = ECKey.recoverPubBytesFromSignature(i, sig, messageHash);
            if (k != null && Arrays.equals(k, thisKey)) {
                recId = i;
                break;
            }
        }

        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        } else {
            sig.v = (byte) (recId + 27);
            return sig;
        }
    }

    /**
     * Convert der signature to org.bitcoinj.core.ECKey.ECDSASignature
     *
     * @param derSignature
     * @return
     */
    public static org.bitcoinj.core.ECKey.ECDSASignature der2BtcSignature(@NonNull String derSignature) {
        return org.bitcoinj.core.ECKey.ECDSASignature.decodeFromDER(ByteUtil.hexStringToBytes(derSignature)).toCanonicalised();
    }

    /**
     * Convert str signature to org.bitcoinj.core.ECKey.ECDSASignature
     *
     * @param strSignature
     * @return
     */
    public static org.bitcoinj.core.ECKey.ECDSASignature str2BtcSignature(@NonNull String strSignature) {
        if (strSignature.length() != 128) {
            throw new RuntimeException("signature illegal");
        }
        String r = strSignature.substring(0, 64);
        String s = strSignature.substring(64);
        return new org.bitcoinj.core.ECKey.ECDSASignature(new BigInteger(r, 16), new BigInteger(s, 16)).toCanonicalised();
    }

    /**
     * device private key Challenge
     *
     * @param cardReader
     * @param publicKey
     * @return true if challenge successfully, otherwise throw a CommandException
     * @throws CommandException
     */
    public static boolean devicePrvChallenge(@NonNull ICardReader cardReader, @NonNull byte[] publicKey) throws CommandException {
        byte[] rl = Commands.nextRandomBytes();
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Challenge, rl);
        TLVBox signatureAndSalt = cardReader.transceive2TLV(Commands.signRandByCardPriKey(tlvBox.serialize()));
        String signature = signatureAndSalt.getStringValue(Commands.TLVTag.Verification_Signature);
        String salt = signatureAndSalt.getStringValue(Commands.TLVTag.Salt);
        if (salt == null) return false;
        String rlStr = ByteUtil.toHexString(rl);
        String rlAndSalt = rlStr + salt;
        byte[] data = ByteUtil.hexStringToBytes(rlAndSalt);
        LogUtils.i("sign", "publicKey=" + ByteUtil.toHexString(publicKey) + "\ndata=" + ByteUtil.toHexString(data) + "\nsig=" + signature);
        if (signature.length() != 128) {
            throw new CommandException(ErrorCode.INVALID_CARD,"Device private key Challenge is failed");
        }
        String r = signature.substring(0, 64);
        String s = signature.substring(64);
        if (!verifySignatureTwiceHash(publicKey, data, new BigInteger(r, 16), new BigInteger(s, 16))) {
            throw new CommandException(ErrorCode.INVALID_CARD,"Device private key Challenge is failed");
        }
        return true;
    }

    /**
     * block chain private key Challenge
     *
     * @param cardReader
     * @param publicKey
     * @return true if challenge successfully, otherwise throw a CommandException
     * @throws CommandException
     */
    public static boolean blockChainPrvChallenge(@NonNull ICardReader cardReader, @NonNull byte[] publicKey) throws CommandException {
        byte[] rl = Commands.nextRandomBytes();
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Challenge, rl);
        TLVBox signatureAndSalt = cardReader.transceive2TLV(Commands.signRandByCurrencyPriKey(tlvBox.serialize()));
        String signature = signatureAndSalt.getStringValue(Commands.TLVTag.Verification_Signature);
        String salt = signatureAndSalt.getStringValue(Commands.TLVTag.Salt);
        byte[] data = ByteUtil.hexStringToBytes(ByteUtil.toHexString(rl) + salt);
        LogUtils.i("Signature", "data=" + ByteUtil.toHexString(data) + "\nsig=" + signature + "\npubKey=" + ByteUtil.toHexString(publicKey));
        if (signature.length() != 128) {
            throw new CommandException(ErrorCode.INVALID_CARD,"Block chain private key Challenge is failed");
        }
        String r = signature.substring(0, 64);
        String s = signature.substring(64);
        if (!verifySignatureTwiceHash(publicKey, data, new BigInteger(r, 16), new BigInteger(s, 16))) {
            throw new CommandException(ErrorCode.INVALID_CARD,"Block chain private key Challenge is failed");
        }
        return true;
    }

    /**
     * verify sign data for sha1-256 twice
     *
     * @param publicKey
     * @param data
     * @param r
     * @param s
     * @return
     * @throws CommandException
     */
    public static boolean verifySignatureTwiceHash(byte[] publicKey, byte[] data, BigInteger r, BigInteger s) throws CommandException {
        if (publicKey == null || publicKey.length == 0 || data == null || data.length == 0) {
            throw new CommandException(ErrorCode.INVALID_CARD,"Invalid cert _ verify manufacture cert fail");
        }
        SHA256Digest s_SHA256Digest = new SHA256Digest();
        s_SHA256Digest.update(data, 0, data.length);
        byte hash[] = new byte[32];
        s_SHA256Digest.doFinal(hash, 0);

        s_SHA256Digest.reset();

        s_SHA256Digest.update(hash, 0, hash.length);
        byte db_hash[] = new byte[32];
        s_SHA256Digest.doFinal(db_hash, 0);

        ECDSASigner signer = new ECDSASigner();
        signer.init(false, new ECPublicKeyParameters(curve.getCurve().decodePoint(publicKey), domain));
        if (!signer.verifySignature(db_hash, r, s)) {
            return false;
        }
        return true;
    }

    /**
     * verify sign data for no hash
     *
     * @param publicKey
     * @param data
     * @param r
     * @param s
     * @return
     * @throws CommandException
     */
    public static boolean verifySignatureNoHash(byte[] publicKey, byte[] data, BigInteger r, BigInteger s) throws CommandException {
        ECDSASigner signer = new ECDSASigner();
        signer.init(false, new ECPublicKeyParameters(curve.getCurve().decodePoint(publicKey), domain));
        if (!signer.verifySignature(data, r, s)) {
            return false;
        }
        return true;
    }
}
