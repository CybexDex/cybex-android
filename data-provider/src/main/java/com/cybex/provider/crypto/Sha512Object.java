package com.cybex.provider.crypto;

import com.google.common.io.BaseEncoding;

import org.spongycastle.crypto.digests.SHA512Digest;

import java.util.Arrays;

public class Sha512Object {

    public byte[] hash = new byte[64];

    @Override
    public String toString() {
        BaseEncoding encoding = BaseEncoding.base16().lowerCase();
        return encoding.encode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        Sha512Object sha512Object = (Sha512Object)obj;
        return Arrays.equals(hash, sha512Object.hash);
    }

    public static Sha512Object create_from_string(String strContent) {
        SHA512Digest digest = new SHA512Digest();
        byte[] bytePassword = strContent.getBytes();
        digest.update(bytePassword, 0, bytePassword.length);

        byte[] byteHash = new byte[64];
        digest.doFinal(byteHash, 0);

        Sha512Object sha512Object = new Sha512Object();
        System.arraycopy(byteHash, 0, sha512Object.hash, 0, byteHash.length);

        return sha512Object;
    }

    public static Sha512Object create_from_byte_array(byte[] byteArray, int offset, int length) {
        SHA512Digest digest = new SHA512Digest();
        digest.update(byteArray, offset, length);

        byte[] byteHash = new byte[64];
        digest.doFinal(byteHash, 0);

        Sha512Object sha512Object = new Sha512Object();
        System.arraycopy(byteHash, 0, sha512Object.hash, 0, byteHash.length);

        return sha512Object;
    }


}
