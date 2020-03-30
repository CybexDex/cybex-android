package io.enotes.sdk.repository.card;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.enotes.sdk.repository.base.Resource;


/**
 * Interface definition that abstracts away the feature of a Card Reader.
 */
public interface ICardReader {
    /**
     * Nfc auto connect mode
     * <b>suggested</b>
     */
    int NFC_AUTO_MODE = 1;
    /**
     * Nfc manual connect mode
     */
    int NFC_MANUAL_MODE = 2;
    /**
     * Nfc manual/auto connect mode
     * If only one tag then auto connect. In fact it equals NFC_AUTO_MODE.
     */
    int NFC_MANUAL_AUTO_MODE = NFC_MANUAL_MODE ^ NFC_AUTO_MODE;
    /**
     * Ble auto connect mode
     */
    int BLE_AUTO_MODE = 4;
    /**
     * Ble manual connect mode
     */
    int BLE_MANUAL_MODE = 8;
    /**
     * Ble manual/auto connect mode
     * If only one device then auto connect.
     * <b>suggested</b>
     */
    int BLE_MANUAL_AUTO_MODE = BLE_MANUAL_MODE ^ BLE_AUTO_MODE;
    /**
     * Nfc + Ble auto connect mode
     */
    int BOTH_AUTO_MODE = NFC_AUTO_MODE ^ BLE_AUTO_MODE;
    /**
     * Nfc + Ble manually connect
     */
    int BOTH_MANUAL_MODE = NFC_MANUAL_MODE ^ BLE_MANUAL_MODE;
    /**
     * Nfc + Ble manually/auto connect mode
     * <b>suggested</b>
     */
    int BOTH_MANUAL_AUTO_MODE = NFC_MANUAL_AUTO_MODE ^ BLE_MANUAL_AUTO_MODE;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NFC_AUTO_MODE, NFC_MANUAL_MODE, NFC_MANUAL_AUTO_MODE, BLE_AUTO_MODE, BLE_MANUAL_MODE,
            BLE_MANUAL_AUTO_MODE, BOTH_AUTO_MODE, BOTH_MANUAL_MODE, BOTH_MANUAL_AUTO_MODE})
    @interface ReadMode {
    }

    /**
     * Whether the card is connected.
     *
     * @return
     */
    boolean isConnected();

    /**
     * Whether the card is present
     *
     * @return
     */
    boolean isPresent();

    /**
     * disconnect the card.
     */
    void disconnect();

    /**
     * parse the card and try to connect it.
     * <p>
     * Note: this may take a long time.
     *
     * @param reader
     */
    void parseAndConnect(@NonNull Reader reader);

    /**
     * prepare to read.
     * 1. Reset device if need
     * 2. select aid
     *
     * @param aid
     * @throws CommandException
     */
    void prepareToRead(String aid) throws CommandException;


    /**
     * Send Command with raw ISO-DEP data to the card and receive the response.
     * The response should be trimmed if it a valid response {@link Commands#isSuccessResponse(String)}
     *
     * @param command
     * @return
     * @throws CommandException if the response is not valid or connect error
     */
    @NonNull
    String transceive(@NonNull Command command) throws CommandException;

    /**
     * Send Command with raw ISO-7816 data to the card and receive the response.
     * The response should be trimmed if it a valid response {@link Commands#isSuccessResponse(String)}
     *
     * @param command
     * @return
     * @throws CommandException
     */
    @NonNull
    TLVBox transceive2TLV(@NonNull Command command) throws CommandException;

    /**
     * Set a ConnectedCallback for the reader.
     *
     * @param connectedCallback
     */
    void setConnectedCallback(@Nullable ConnectedCallback connectedCallback);

    interface ConnectedCallback {
        /**
         * A callback to be invoked when connected a card.
         */
        void onCardConnected(@NonNull Resource<ICardReader> reader);

        /**
         * A callback to be invoked when disconnected a card.
         */
        void onCardDisconnected(@NonNull Resource<ICardReader> reader);
    }
}
