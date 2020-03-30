package io.enotes.sdk;

import android.util.Log;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.K256KeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.base58.B58;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.MainNetParams;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.junit.Test;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;

import java.math.BigInteger;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.card.TLVBox;
import io.enotes.sdk.utils.CardUtils;
import io.enotes.sdk.utils.LogUtils;

import static io.enotes.sdk.repository.card.Commands.TLVTag.Device_Certificate;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void createXrpRawTransaction() {
        String secret = "shwtqohe1FBZ9ErN8aDt7PxviRnVw";
        Payment payment = new Payment();

        // Put `as` AccountID field Account, `Object` o
        payment.as(AccountID.Account, "rP7JNBVPS31t1vroS3u9pPpywi5MYjyvkr");
        payment.as(AccountID.Destination, "r3fVM5zkC4TVmLnD5PeSYSNZQkDLT6bTeQ");
        payment.as(Amount.Amount, "20000000");
        payment.as(UInt32.Sequence, 10);
        payment.as(Amount.Fee, "10");

        // Try commenting out the Fee, you'll get STObject.FormatException
        SignedTransaction signed = payment.sign(secret);
        String toHexString = ByteUtil.toHexString(signed.signingData);
        toHexString.toUpperCase();
    }

    @Test
    public void testRippleAddress() {
        String secret = "shwtqohe1FBZ9ErN8aDt7PxviRnVw";
        IKeyPair keyPair = Seed.getKeyPair(secret);
        AccountID accountID = AccountID.fromKeyPair(keyPair);
        String address = accountID.address;
        String pubHex = keyPair.canonicalPubHex();


        Payment payment = new Payment();

        // Put `as` AccountID field Account, `Object` o
        payment.as(AccountID.Account, "rP7JNBVPS31t1vroS3u9pPpywi5MYjyvkr");
        payment.as(AccountID.Destination, "r3fVM5zkC4TVmLnD5PeSYSNZQkDLT6bTeQ");
        payment.as(Amount.Amount, "30000000");
        payment.as(UInt32.Sequence, 5);
        payment.as(Amount.Fee, "10");

        // Try commenting out the Fee, you'll get STObject.FormatException
        SignedTransaction signed = payment.sign(secret);
        String tx_blob = signed.tx_blob;
        tx_blob.toUpperCase();
    }

    private static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());

    @Test
    public void verifySignatureTwiceHash() throws CommandException {
        String s1 = org.ethereum.crypto.ECKey.HALF_CURVE_ORDER.toString();
        String s2 = org.ethereum.crypto.ECKey.CURVE.getN().toString(16);


        byte[] publicKey = ByteUtil.hexStringToBytes("04F1C073C702077AE7E6C4983B0EE8C5001AEF1E2D3D47451E96BA6A5C99338F4FBC95CF9988B9EFF8EDBC7B56798FAF61F8856B10BA3609AFF8532AE116A946DD");
        byte[] data = ByteUtil.hexStringToBytes("D9C9EB62FF5697B8D1937241037F219259D104DFDA6320A36925C7EAF10D1C3E");
        if (publicKey == null || publicKey.length == 0 || data == null || data.length == 0) {
            throw new CommandException(ErrorCode.INVALID_CARD, "Invalid cert _ verify manufacture cert fail");
        }
        BigInteger r = new BigInteger("E537207EF9D98C54D189F8968779BE78F8E224EEF441B8C59EA9E58E737C49DD", 16);
        BigInteger s = new BigInteger("E0E120715D01AAA73C352D25BD6CF6DD146EDE2C7040CA3ED1E75035DE564126", 16);
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
        boolean verifySignature = signer.verifySignature(db_hash, r, s);
    }

    @Test
    public void testHash() {
        String data = "0000001D043139246EF5EA05CC2159C580413A19F23ACD5EBE5591D2A524962D9E1A8CB5E0C840580440DF61286738994D197C780DFA00FDE6AF7DDCAF277663A7D7E6CF7E";
        String r="F900EB4FC75C672842572E1B18FD3FB1415A515E4458B9E115736F6D2A9B63B1";
        String s="81CE4ACD2385CAE437EE4ECCDD8D029119DCD51755953526ED1D2E0B4F4F16BD";
        byte[] hash = Sha256Hash.of(ByteUtil.hexStringToBytes(data)).getBytes();
        String hash1 = ByteUtil.toHexString(Sha256Hash.of(hash).getBytes());


        org.ethereum.crypto.ECKey.ECDSASignature sig = new ECKey.ECDSASignature(new BigInteger(r, 16), new BigInteger(s, 16)).toCanonicalised();
        String s1 = sig.s.toString(16);

        String a = "a";
    }

}