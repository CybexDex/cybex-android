package io.enotes.sdk.repository.card;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import org.ethereum.util.ByteUtil;

import java.security.SecureRandom;
import java.util.Arrays;


/**
 * APDU Commands
 */

public class Commands {
    //AID "0102030405060708090000"
    private static final String SELECT_AID = "00A40400";
    //1.验证私钥(随机数签名)/Sign Challenge by Cryptocurrency Private Key
    //APDU = 0x003B0000 + 随机数长度(1字节) + 随机数(8字节)
    //RAPDU = 0x + 签名(长度不定DER编码) + 9000
    private static final String CHECK_CURRENCY_PRV = "0088540022";
    //2.交易签名/Sign Transaction by Cryptocurrency Private Key
    //APDU = 0x003B0100 +  交易哈希长度(1字节) + 交易哈希(32字节)
    //RAPDU = 0x + 签名(长度不定DER编码) + 9000
    private static final String SIGN_TX = "00A0540022";
    //3.读取公钥/Get Cryptocurrency Public Key
    //APDU = 0x003C0000
    //RAPDU = 0x + 公钥(65字节DER编码) + 9000
    private static final Command READ_CURRENCY_PUB = Command.newCmd().setDesc("READ_CURRENCY_PUB").setCmdStr("00CA0055");
    //4.读取状态/Get Transaction Sign Counter
    //APDU = 0x003D0000
    //RAPDU = 0x + 是否已产生密钥对(1字节) + 随机数签名次数(2字节) + 交易签名次数(2字节) + 9000
    private static final Command READ_STATUS = Command.newCmd().setDesc("READ_STATUS").setCmdStr("00CA0090");
    //5.读取证书/Get Card Certificate
    //APDU = 00CA0030
    //RAPDU = 0x + card Certificate(DER) + 9000
    private static final Command READ_CERT = Command.newCmd().setDesc("READ_CERT").setCmdStr("00CA0030");
    //6.验证实体币/证书私钥(随机数签名)/Sign Challenge by Card ID Private Key
    //APDU = 0x00200000 + 随机数长度(1字节) + 随机数(8字节)
    //RAPDU = 0x + 签名(长度不定DER编码) + 9000
    private static final String CHECK_CARD_PRV = "0088520022";

    private static final String freezeTx = "0028940008";

    private static final String unFreezeTx = "0026940008";

    private static final String verifyTransactionPin = "0020930008";
    private static final String updateTransactionPin = "0024930008";

    private static final Command READ_FREEZE_STATUS = Command.newCmd().setDesc("READ_FREEZE_STATUS").setCmdStr("00CA0094");
    private static final Command READ_UNFREEZE_TRIES = Command.newCmd().setDesc("READ_FREEZE_STATUS").setCmdStr("00CA0095");

    //正确码
    private static final String NORMAL_CODE = "9000";

    private static SecureRandom sSecureRandom;

    /**
     * SELECT AID command. This command indicates which service a reader is
     * interested in communicating with.
     *
     * @param aidHex
     * @return
     */
    @NonNull
    public static Command selectAID(@NonNull String aidHex) {
        return Command.newCmd().setDesc("SELECT_AID").setCmdStr(SELECT_AID + String.format("%02X", aidHex.length() / 2) + aidHex);
    }

    /**
     * Sign Challenge by Crypto Currency Private Key.
     *
     * @param randTlv
     * @return
     */
    @NonNull
    public static Command signRandByCurrencyPriKey(byte[] randTlv) {
        return Command.newCmd().setDesc("CHECK_CURRENCY_PRV").setCmdByte(concat(ByteUtil.hexStringToBytes(CHECK_CURRENCY_PRV), randTlv));
    }

    /**
     * Sign Tx hash by Crypto Currency Private Key.
     *
     * @param txHash
     * @return
     */
    @NonNull
    public static Command signTX(@Size(32) byte[] txHash) {
        return Command.newCmd().setDesc("SIGN_TX").setCmdByte(concat(ByteUtil.hexStringToBytes(SIGN_TX), txHash));
    }

    /**
     * Get Crypto Currency public Key
     *
     * @return
     */
    @NonNull
    public static Command getCurrencyPubKey() {
        return READ_CURRENCY_PUB;
    }

    /**
     * Get Transaction Sign Counter
     *
     * @return
     */
    @NonNull
    public static Command getTxSignCounter() {
        return READ_STATUS;
    }

    /**
     * Get Card Certificate
     *
     * @return
     */
    public static Command getCertificate() {
        return READ_CERT;
    }

    public static Command getFreezeStatus() {
        return READ_FREEZE_STATUS;
    }

    public static Command getReadUnfreezeTries() {
        return READ_UNFREEZE_TRIES;
    }

    public static Command freezeTx(byte[] pin) {
        return Command.newCmd().setDesc("FREEZE_TX").setCmdByte(concat(ByteUtil.hexStringToBytes(freezeTx), pin));
    }

    public static Command unFreezeTx(byte[] pin) {
        return Command.newCmd().setDesc("UNFREEZE_TX").setCmdByte(concat(ByteUtil.hexStringToBytes(unFreezeTx), pin));
    }

    public static Command verifyTxPin(byte[] pin) {
        return Command.newCmd().setDesc("verifyTxPin").setCmdByte(concat(ByteUtil.hexStringToBytes(verifyTransactionPin), pin));
    }

    public static Command updateTxPin(byte[] pin) {
        return Command.newCmd().setDesc("updateTxPin").setCmdByte(concat(ByteUtil.hexStringToBytes(updateTransactionPin), pin));
    }

    /**
     * Sign Challenge by Card Private Key.
     *
     * @param randTlv
     * @return
     */
    @NonNull
    public static Command signRandByCardPriKey(byte[] randTlv) {
        return Command.newCmd().setDesc("CHECK_CARD_PRV").setCmdByte(concat(ByteUtil.hexStringToBytes(CHECK_CARD_PRV), randTlv));
    }

    /**
     * Check whether it's a successful response.
     *
     * @param response
     * @return
     */
    public static boolean isSuccessResponse(String response) {
        if (response == null) return false;
        return response.endsWith(NORMAL_CODE);
    }

    /**
     * Get a random long number.
     *
     * @return
     */
    public static long nextRandomLong() {
        if (sSecureRandom == null) {
            sSecureRandom = new SecureRandom();
        }
        return Math.abs(sSecureRandom.nextLong());
    }

    public static byte[] nextRandomBytes() {
        if (sSecureRandom == null) {
            sSecureRandom = new SecureRandom();
        }
        return sSecureRandom.generateSeed(32);
    }

    private static byte[] concat(byte[] byte1, byte[] byte2) {
        byte[] result = Arrays.copyOf(byte1, byte1.length + byte2.length);
        System.arraycopy(byte2, 0, result, byte1.length, byte2.length);
        return result;
    }

    public static class TLVTag {
        public static final int Software_Author = 0x10;
        public static final int Software_Version = 0x11;
        public static final int Apdu_Protocol_Version = 0x12;
        public static final int Secure_Channel_Protocol = 0x13;
        public static final int Device_Certificate = 0x30;
        public static final int Account = 0x32;
        public static final int Master_PublicKey = 0x57;
        public static final int BlockChain_PublicKey = 0x55;
        public static final int Challenge = 0x70;
        public static final int Salt = 0x71;
        public static final int Verification_Signature = 0x73;
        public static final int Transaction_Signature_Counter = 0x90;
        public static final int Transaction_Hash = 0x91;
        public static final int Transaction_signature = 0x92;
        public static final int Transaction_Freeze_Pin = 0x93;
        public static final int Transaction_Freeze_Status = 0x94;
        public static final int Transaction_Freeze_Tries = 0x95;
    }
}
