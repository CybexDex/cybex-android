package com.cybex.provider.crypto;

import com.google.common.io.BaseEncoding;

import org.spongycastle.crypto.digests.SHA224Digest;

import java.util.Arrays;

public class Sha224Object {

    public byte[] hash = new byte[28];

    @Override
    public String toString() {
        BaseEncoding encoding = BaseEncoding.base16().lowerCase();
        return encoding.encode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        Sha224Object sha224Object = (Sha224Object) obj;
        return Arrays.equals(hash, sha224Object.hash);
    }

    public static Sha224Object create_from_byte_array(byte[] byteArray, int offset, int length) {
        SHA224Digest digest = new SHA224Digest();
        digest.update(byteArray, offset, length);

        byte[] byteHash = new byte[28];
        digest.doFinal(byteHash, 0);

        Sha224Object sha224Object = new Sha224Object();
        System.arraycopy(byteHash, 0, sha224Object.hash, 0, byteHash.length);

        return sha224Object;
    }
}
