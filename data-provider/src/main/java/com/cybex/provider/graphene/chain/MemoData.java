package com.cybex.provider.graphene.chain;

import com.cybex.provider.crypto.Aes;
import com.cybex.provider.crypto.Sha224Object;
import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.crypto.Sha512Object;
import com.cybex.provider.fc.io.RawType;
import com.cybex.provider.utils.MyUtils;
import com.google.common.primitives.UnsignedLong;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MemoData implements Serializable{

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    public static class memo_message {
        public int checksum;
        public String text;
        public memo_message(int nChecksum, String strText) {
            checksum = nChecksum;
            text = strText;
        }

        public ByteBuffer serialize() {
            RawType rawType = new RawType();
            byte[] bytesChecksum = rawType.get_byte_array(checksum);
            byte[] bytesContent = text.getBytes(Charset.forName("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(bytesContent.length + 4);
            byteBuffer.put(bytesChecksum);
            byteBuffer.put(bytesContent);

            return byteBuffer;
        }

        public static memo_message deserialize(ByteBuffer byteBuffer) {
            byte[] byteSerial = byteBuffer.array();
            String strBuffer = new String(byteSerial, 4, byteSerial.length - 4, Charset.forName("UTF-8"));

            byte[] byteChecksum = new byte[4];
            System.arraycopy(byteSerial, 0, byteChecksum, 0, byteChecksum.length);

            RawType rawType = new RawType();
            return new memo_message(rawType.byte_array_to_int(byteChecksum), strBuffer);
        }
    }

    public Types.public_key_type from;
    public Types.public_key_type to;
    /**
     * 64 bit unsignedNonce format:
     * [  8 bits | 56 bits   ]
     * [ entropy | timestamp ]
     * Timestamp is number of microseconds since the epoch
     * Entropy is a byte taken from the hash of a new private key
     *
     * This format is not mandated or verified; it is chosen to ensure uniqueness of key-IV pairs only. This should
     * be unique with high probability as long as the generating host has a high-resolution clock OR a strong source
     * of entropy for generating private keys.
     */
    transient UnsignedLong unsignedNonce = UnsignedLong.ZERO;

    public String nonce;
    /**
     * This field contains the AES encrypted packed @ref memo_message
     */
    //vector<char> messageBuffer;
    transient ByteBuffer messageBuffer;

    public String message;

    /// @note custom_nonce is for debugging only; do not set to a nonzero value in production
    public void set_message(PrivateKey privateKey,
                            PublicKey publicKey,
                            String strMsg,
                            long lCustomNonce) {
        if (lCustomNonce == 0) {
            byte[] byteSecret = PrivateKey.generate().get_secret();
            Sha224Object sha224Object = Sha224Object.create_from_byte_array(
                    byteSecret,
                    0,
                    byteSecret.length
            );

            byte[] byteEntropy = new byte[4];
            System.arraycopy(sha224Object.hash, 0, byteEntropy, 0, byteEntropy.length);

            RawType rawType = new RawType();
            long lEntropy = rawType.byte_array_to_int(byteEntropy);
            lEntropy <<= 32;
            lEntropy &= 0xff00000000000000l;
            unsignedNonce = UnsignedLong.fromLongBits((System.currentTimeMillis() & 0x00ffffffffffffffl) | lEntropy);
            nonce = unsignedNonce.toString();
        } else {
            unsignedNonce = UnsignedLong.valueOf(lCustomNonce);
            nonce = unsignedNonce.toString();
        }
        Sha512Object sha512Object = privateKey.get_shared_secret(publicKey);
        String strNoncePlusSecret = unsignedNonce.toString() + sha512Object.toString();
        sha512Object = Sha512Object.create_from_string(strNoncePlusSecret);

        Sha256Object sha256Object = Sha256Object.create_from_string(strMsg);
        byte[] byteChecksum = new byte[4];
        System.arraycopy(sha256Object.hash, 0, byteChecksum, 0, byteChecksum.length);

        RawType rawType = new RawType();
        int nChecksum = rawType.byte_array_to_int(byteChecksum);

        ByteBuffer byteBufferText = new memo_message(nChecksum, strMsg).serialize();
        // aes加密

        byte[] byteKey = new byte[32];
        System.arraycopy(sha512Object.hash, 0, byteKey, 0, byteKey.length);

        byte[] ivBytes = new byte[16];
        System.arraycopy(sha512Object.hash, 32, ivBytes, 0, ivBytes.length);

        messageBuffer = Aes.encrypt(byteKey, ivBytes, byteBufferText.array());
        message = bytesToHex(messageBuffer.array());
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


    public String get_message(PrivateKey privateKey, PublicKey publicKey) {
        Sha512Object sha512Object = privateKey.get_shared_secret(publicKey);
        String strNoncePlusSecret = unsignedNonce.toString() + sha512Object.toString();
        sha512Object = Sha512Object.create_from_string(strNoncePlusSecret);

        byte[] byteKey = new byte[32];
        System.arraycopy(sha512Object.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(sha512Object.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteDecrypt = Aes.decrypt(byteKey, ivBytes, messageBuffer.array());
        memo_message memoMessage = memo_message.deserialize(byteDecrypt);

        Sha256Object messageHash = Sha256Object.create_from_string(memoMessage.text);
        byte[] byteChecksum = new byte[4];
        System.arraycopy(messageHash.hash, 0, byteChecksum, 0, byteChecksum.length);

        RawType rawType = new RawType();
        int nChecksum = rawType.byte_array_to_int(byteChecksum);
        if (nChecksum == memoMessage.checksum) {
            return memoMessage.text;
        }

        return "";
    }

    public String get_message(PrivateKey privateKey, PublicKey publicKey, String message, String nonce) {
        Sha512Object sha512Object = privateKey.get_shared_secret(publicKey);
        String strNoncePlusSecret = nonce + sha512Object.toString();
        sha512Object = Sha512Object.create_from_string(strNoncePlusSecret);

        byte[] byteKey = new byte[32];
        System.arraycopy(sha512Object.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(sha512Object.hash, 32, ivBytes, 0, ivBytes.length);

        byte[] messageByte = MyUtils.hexToBytes(message);
        ByteBuffer byteDecrypt = Aes.decrypt(byteKey, ivBytes, messageByte);
        memo_message memoMessage = memo_message.deserialize(byteDecrypt);

        Sha256Object messageHash = Sha256Object.create_from_string(memoMessage.text);
        byte[] byteChecksum = new byte[4];
        System.arraycopy(messageHash.hash, 0, byteChecksum, 0, byteChecksum.length);

        RawType rawType = new RawType();
        int nChecksum = rawType.byte_array_to_int(byteChecksum);
        if (nChecksum == memoMessage.checksum) {
            return memoMessage.text;
        }

        return "";
    }
}
