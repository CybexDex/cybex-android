package io.enotes.sdk.constant;

public class ErrorCode {
    /**
     * not error
     */
    public static final int NOT_ERROR = 0;
    /**
     * bluetooth disconnect
     */
    public static final int BLUETOOTH_DISCONNECT = 100;
    /**
     * nfc disconnect
     */
    public static final int NFC_DISCONNECTED = 101;
    /**
     * read cert or verify sign error
     */
    public static final int INVALID_CARD = 102;
    /**
     * card not on the phone or on the card reader
     */
    public static final int NOT_FIND_CARD = 103;
    /**
     * include 404 、 500 、 timeout...
     */
    public static final int NET_ERROR = 104;

    /**
     * net unavailable
     */
    public static final int NET_UNAVAILABLE = 105;

    /**
     * bluetooth unable
     */
    public static final int BLUETOOTH_UNABLE = 106;

    /**
     * call cert public key error
     */
    public static final int CALL_CERT_PUB_KEY_ERROR = 107;

    /**
     * sdk self error
     */
    public static final int SDK_ERROR = 108;

    /**
     * not find right card when withdraw
     */
    public static final int NOT_FIND_RIGHT_CARD = 109;

    /**
     * not support it, need update app or sdk
     */
    public static final int NOT_SUPPORT_CARD = 110;
}
