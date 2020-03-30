package io.enotes.sdk.core.interfaces;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.nfc.Tag;
import android.support.annotation.NonNull;

import java.util.List;

import io.enotes.sdk.core.Callback;
import io.enotes.sdk.core.EntSignature;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.api.entity.response.simulate.BluetoothEntity;
import io.enotes.sdk.repository.card.Command;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.card.Commands;
import io.enotes.sdk.repository.card.Reader;
import io.enotes.sdk.repository.card.TLVBox;
import io.enotes.sdk.repository.db.entity.Card;

public interface CardInterface {

    /**
     * select cardlet application id
     */
    void selectAid(String aid);
    /**
     * start scan bluetooth devices
     *
     */
    void startBluetoothScan(Callback<Reader> callback);

    /**
     * stop scan bluetooth devices
     *
     */
    void stopBluetoothScan(Callback callback);

    /**
     * connect bluetooth device,you can get connect statu from [setReadCardCallback](#setReadCardCallback)
     *
     */
    void connectBluetooth(BluetoothEntity bluetoothDevice);

    /**
     * disconnect bluetooth device,you can get connect statu from [setReadCardCallback](#setReadCardCallback)
     */
    void disconnectBluetooth();

    /**
     * Limit the NFC controller to reader mode while this Activity is in the foreground ,
     * you can get read status from [setReadCardCallback](#setReadCardCallback),support you set on BaseActivity onResume().
     *
     */
    void enableNfcReader(Activity activity);

    /**
     * Restore the NFC adapter to normal mode of operation,support you set on BaseActivity onPause().
     *
     */
    void disableNfcReader(Activity activity);

    /**
     * after read cert and verify sign by bluetooth or nfc , will get card object.
     *
     */
    void setReadCardCallback(Callback<Card> cardCallback);

    /**
     * judge whether bluetooth is connected
     *
     */
    boolean isConnected();

    /**
     * judge whether card is clinging to phone or bluetooth
     *
     */
    boolean isPresent();

    /**
     * get btc raw transaction
     *
     */
    void getBtcRawTransaction(Card card, String fees, String toAddress, List<EntUtxoEntity> unSpends, String omniValue, Callback<String> callback);

    /**
     * get eth raw transaction
     *
     */
    void getEthRawTransaction(Card card, String nonce, String estimateGas, String gasPrice, String toAddress, String value, byte[] data, Callback<String> callback);

    void getXrpRawTransaction(Card card,String toAddress, String amount, int sequence, String fee, long destinationTag, Callback<String> callback);
    /**
     * Send Command with raw ISO-DEP data to the card and receive the response.
     * The response should be trimmed if it a valid response {@link Commands#isSuccessResponse(String)}
     */
    String transmitApdu(@NonNull Command command) throws CommandException;

    /**
     * parse nfc tag from outside the application
     */
    void parseNfcTag(Tag tag);

    int getTransactionPinStatus() throws CommandException;
    int getDisableTransactionPinTries() throws CommandException;
    boolean enableTransactionPin(String pin) throws CommandException;
    boolean disableTransactionPin(String pin) throws CommandException;
    boolean verifyTransactionPin(String pin) throws CommandException;
    boolean updateTransactionPin(String oldPin, String newPin) throws CommandException;
    EntSignature doSign(byte[] hash, Card card)throws CommandException ;
}
