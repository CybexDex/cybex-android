package io.enotes.sdk.repository.provider;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ethereum.util.ByteUtil;

import java.math.BigInteger;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.repository.base.BaseManager;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.card.CardScannerReader;
import io.enotes.sdk.repository.card.Command;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.card.Commands;
import io.enotes.sdk.repository.card.ICardReader;
import io.enotes.sdk.repository.card.ICardScanner;
import io.enotes.sdk.repository.card.Reader;
import io.enotes.sdk.repository.card.TLVBox;
import io.enotes.sdk.repository.db.entity.Card;

import static io.enotes.sdk.utils.SignatureUtils.verifySignatureNoHash;


public class CardProvider implements BaseManager {
    private CardScannerReader mCardScannerReader;

    public CardProvider(Context context) {
        mCardScannerReader = new CardScannerReader.Builder(context).build();
    }

    /**
     * Set the target aid, default is {@link Reader#DEFAULT_TARGET_AID}
     *
     * @param aid
     */
    public void setTargetAID(@NonNull String aid) {
        mCardScannerReader.setTargetAID(aid);
    }

    /**
     * Notice：It will remove and destroy old scanners and readers if they are not in the new scanMode.
     * New scanners and readers may be added if there was no related scanner and reader before.
     *
     * @param scanMode
     * @param readMode
     */
    public void setScanReadMode(@ICardScanner.ScanMode int scanMode, @ICardReader.ReadMode int readMode) {
        mCardScannerReader.setScanReadMode(scanMode, readMode);
    }

    /**
     * Start to search coin.
     * Scanned {@link Reader} and {@link Card} (for AUTO CONNECT MODE) can be
     * gotten by {@link #getReader()} and {@link #getCard()} (for AUTO CONNECT MODE)
     * <p>
     * Notice: NFC mode is passive
     */
    @MainThread
    public void startScan() {
        mCardScannerReader.startScan();
    }


    /**
     * For NFC Mode: Limit the NFC controller to reader mode while this Activity is in the foreground.
     *
     * @param activity
     * Use {@link NfcAdapter#enableReaderMode(Activity, NfcAdapter.ReaderCallback, int, Bundle)}
     */
    @MainThread
    public void enterForeground(Activity activity) {
        mCardScannerReader.enterForeground(activity);
    }

    /**
     * For NFC Mode: Restore the NFC adapter to normal mode of operation
     *
     * @param activity
     * Use {@link NfcAdapter#disableReaderMode(Activity)}
     */
    @MainThread
    public void enterBackground(Activity activity) {
        mCardScannerReader.enterBackground(activity);
    }

    /**
     * Release resources of scanner
     */
    public void destroyScanner() {
        mCardScannerReader.destroy();
    }

    /**
     * Disconnect the card.
     * To connect the card again, it has to move away and move back the card for NFC mode and
     * scan and connect the device again for BLE mode usually.
     */
    public void disconnectCard() {
        mCardScannerReader.disconnect();
    }

    /**
     * Parse the card and try to connect it.
     * Usually it's for the manual read mode, the card is selected by the user.
     * Connect result can be gotten by {@link #getCard()}
     *
     * @param reader
     */
    public void parseAndConnect(@NonNull Reader reader) {
        mCardScannerReader.parseAndConnect(reader);
    }

    /**
     * Whether it is connected to any card or not.
     *
     * @return
     */
    public boolean isConnected() {
        return mCardScannerReader.isConnected();
    }

    /**
     * Whether card is present
     *
     * @return
     */
    public boolean isPresent() {
        return mCardScannerReader.isPresent();
    }

    /**
     * For some situations, new command should follow select aid command.
     * So call this function before transceive new command if need.
     *
     * @throws CommandException
     */
    public void prepareToTransceive() throws CommandException {
        mCardScannerReader.prepareToRead(mCardScannerReader.getTargetAID());
    }

    /**
     * Send Command with raw ISO-DEP data to the card and receive the response.
     * The response should be trimmed if it a valid response {@link Commands#isSuccessResponse(String)}
     *
     * @param command
     * @return
     * @throws CommandException if the response is not valid or connect error
     */
    @Nullable
    public String transceive(@NonNull Command command) throws CommandException {
        return mCardScannerReader.transceive(command);
    }

    /**
     * Send Command with raw ISO-8316 data to the card and receive the response.
     * The response should be trimmed if it a valid response {@link Commands#isSuccessResponse(String)}
     *
     * @param command
     * @return
     * @throws CommandException if the response is not valid or connect error
     */
    @Nullable
    public TLVBox transceive2TLV(@NonNull Command command) throws CommandException {
        return mCardScannerReader.transceive2TLV(command);
    }

    /**
     * Verify the card by card/certification private key challenge and sign the txHash by currency private key.
     * If the card/certification private key challenge is passed and the currency private key signature is verified,
     * then return the der format signature.
     *
     * @param card
     * @param txHash
     * @return
     * @throws CommandException
     */
    public String verifyCoinAndSignTx(@NonNull Card card, @NonNull byte[] txHash) throws CommandException {
        try {
            TLVBox tlvBox = new TLVBox();
            tlvBox.putBytesValue(Commands.TLVTag.Transaction_Hash, txHash);
            TLVBox signatureTLV = transceive2TLV(Commands.signTX(tlvBox.serialize()));
            String signature = signatureTLV.getStringValue(Commands.TLVTag.Transaction_signature);
            if (signature.length() != 128) {
                throw new CommandException(ErrorCode.INVALID_CARD, "please_right_card");
            }
            String r = signature.substring(0, 64);
            String s = signature.substring(64);
            if (verifySignatureNoHash(ByteUtil.hexStringToBytes(card.getCurrencyPubKey()), txHash, new BigInteger(r, 16), new BigInteger(s, 16))) {
                return signature;
            }
        } catch (CommandException e) {
            throw new CommandException(ErrorCode.INVALID_CARD, "please_right_card");
        } catch (Exception e) {
            throw new CommandException(ErrorCode.INVALID_CARD, "please_right_card");
        }
        return null;
    }


    /**
     * Get connected coin card currently.
     * <p>
     * The {@link Resource} data {@link Card} will be nonnull, but it may be a error {@link Resource} if fail to connect.
     *
     * @return
     */
    @NonNull
    public LiveData<Resource<Card>> getCard() {
        return mCardScannerReader.getCard();
    }

    /**
     * Get scanned devices or detected tag, wrapped by Card
     * <p>
     * The {@link Resource} data {@link Reader}  will be nonnull, but it may be a error {@link Resource} if fail to scan.
     *
     * @return
     */
    @NonNull
    public LiveData<Resource<Reader>> getReader() {
        return mCardScannerReader.getReader();
    }

    public Card getConnectedCard(){
        return mCardScannerReader.getConnectedCard();
    }
}
