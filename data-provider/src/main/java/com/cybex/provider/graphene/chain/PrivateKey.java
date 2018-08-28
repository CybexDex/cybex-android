package com.cybex.provider.graphene.chain;

import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.crypto.Sha512Object;

import org.bitcoinj.core.ECKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;

import mrd.bitlib.crypto.HmacPRNG;
import mrd.bitlib.crypto.InMemoryPrivateKey;
import mrd.bitlib.crypto.RandomSource;
import mrd.bitlib.crypto.SignedMessage;
import mrd.bitlib.crypto.ec.EcTools;
import mrd.bitlib.crypto.ec.Parameters;
import mrd.bitlib.util.Sha256Hash;

public class PrivateKey {

    private byte[] key_data = new byte[32];
    public PrivateKey(byte key[]) {
        System.arraycopy(key, 0, key_data, 0, key_data.length);
    }

    public byte[] get_secret() {
        return key_data;
    }

    public static PrivateKey generate() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDsA", "SC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            return new PrivateKey(keyPair);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PublicKey get_public_key(boolean isCompressed) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            ECNamedCurveParameterSpec secp256k1 = org.spongycastle.jce.ECNamedCurveTable.getParameterSpec("secp256k1");
            org.spongycastle.jce.spec.ECPrivateKeySpec privSpec = new org.spongycastle.jce.spec.ECPrivateKeySpec(new BigInteger(1, key_data), secp256k1);
            KeyFactory keyFactory = KeyFactory.getInstance("EC","SC");

            byte[] keyBytes = new byte[33];
            System.arraycopy(key_data, 0, keyBytes, 1, 32);
            BigInteger privateKeys = new BigInteger(keyBytes);
            BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(privSpec);

            mrd.bitlib.crypto.ec.Point Q = EcTools.multiply(Parameters.G, privateKeys);

            //ECPoint ecPoint = ECKey.CURVE.getG().multiply(privateKeys);
            org.spongycastle.math.ec.ECPoint ecpubPoint = new org.spongycastle.math.ec.custom.sec.SecP256K1Curve().createPoint(Q.getX().toBigInteger(), Q.getY().toBigInteger());
            java.security.PublicKey publicKey = keyFactory.generatePublic(new org.spongycastle.jce.spec.ECPublicKeySpec(ecpubPoint, secp256k1));

            BCECPublicKey bcecPublicKey = (BCECPublicKey)publicKey;
            byte bytePublic[] = bcecPublicKey.getQ().getEncoded(isCompressed);

            return new PublicKey(bytePublic, isCompressed);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey(KeyPair ecKey){
        BCECPrivateKey privateKey = (BCECPrivateKey) ecKey.getPrivate();
        byte[] privateKeyGenerate = privateKey.getD().toByteArray();
        if (privateKeyGenerate.length == 33) {
            System.arraycopy(privateKeyGenerate, 1, key_data, 0, key_data.length);
        } else {
            System.arraycopy(privateKeyGenerate, 0, key_data, 0, key_data.length);
        }
    }

    public CompactSignature sign_compact(Sha256Object digest, boolean require_canonical ) {
        CompactSignature signature = null;
        try {
            final HmacPRNG prng = new HmacPRNG(key_data);
            RandomSource randomSource = new RandomSource() {
                @Override
                public void nextBytes(byte[] bytes) {
                    prng.nextBytes(bytes);
                }
            };

            while (true) {
                InMemoryPrivateKey inMemoryPrivateKey = new InMemoryPrivateKey(key_data);
                SignedMessage signedMessage = inMemoryPrivateKey.signHash(new Sha256Hash(digest.hash), randomSource);
                byte[] byteCompact = signedMessage.bitcoinEncodingOfSignature();
                signature = new CompactSignature(byteCompact);

                boolean bResult = PublicKey.is_canonical(signature);
                if (bResult == true) {
                    break;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return signature;
    }

    public static PrivateKey from_seed(String strSeed) {
        Sha256Object.encoder encoder = new Sha256Object.encoder();

        encoder.write(strSeed.getBytes(Charset.forName("UTF-8")));
        PrivateKey privateKey = new PrivateKey(encoder.result().hash);

        return privateKey;
    }

    public Sha512Object get_shared_secret(PublicKey publicKey) {
        ECKey ecPublicKey = ECKey.fromPublicOnly(publicKey.getKeyByte(true));
        ECKey ecPrivateKey = ECKey.fromPrivate(key_data);

        byte[] secret = ecPublicKey.getPubKeyPoint().multiply(ecPrivateKey.getPrivKey())
                .normalize().getXCoord().getEncoded();

        return Sha512Object.create_from_byte_array(secret, 0, secret.length);
    }
}
