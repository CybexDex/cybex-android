package io.enotes.sdk.repository.card;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.enotes.sdk.BuildConfig;
import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.core.ENotesSDK;
import io.enotes.sdk.repository.ProviderFactory;
import io.enotes.sdk.repository.api.ApiResponse;
import io.enotes.sdk.repository.api.RetrofitFactory;
import io.enotes.sdk.repository.api.SimulateCardService;
import io.enotes.sdk.repository.api.entity.ResponseEntity;
import io.enotes.sdk.repository.api.entity.response.simulate.ApduEntity;
import io.enotes.sdk.repository.api.entity.response.simulate.BluetoothEntity;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.repository.db.entity.Cert;
import io.enotes.sdk.repository.db.entity.Mfr;
import io.enotes.sdk.repository.provider.ApiProvider;
import io.enotes.sdk.utils.CardUtils;
import io.enotes.sdk.utils.LogUtils;
import io.enotes.sdk.utils.ReaderUtils;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

import static io.enotes.sdk.constant.ErrorCode.BLUETOOTH_DISCONNECT;
import static io.enotes.sdk.constant.ErrorCode.NOT_FIND_CARD;
import static io.enotes.sdk.constant.Status.BLUETOOTH_PARSING;
import static io.enotes.sdk.constant.Status.BLUETOOTH_SCAN_FINISH;
import static io.enotes.sdk.constant.Status.ERROR;
import static io.enotes.sdk.constant.Status.NFC_CONNECTED;
import static io.enotes.sdk.constant.Status.SUCCESS;
import static io.enotes.sdk.repository.db.entity.Card.STATUS_SAFE;
import static io.enotes.sdk.repository.db.entity.Card.STATUS_UNSAFE;
import static io.enotes.sdk.utils.SignatureUtils.blockChainPrvChallenge;
import static io.enotes.sdk.utils.SignatureUtils.devicePrvChallenge;
import static io.enotes.sdk.utils.SignatureUtils.verifySignatureTwiceHash;


/**
 * A helper class to scan and connect card
 */
public class CardScannerReader implements ICardScanner, ICardReader, ICardScanner.ScanCallback, ICardReader.ConnectedCallback {
    private static final String TAG = CardScannerReader.class.getSimpleName();
    private Context mContext;
    private String mTargetAID;
    private int mScanMode;
    private int mReadMode;
    private List<ScannerReader> mScannerReaders = new ArrayList<>();
    private ScannerReader mCurrentScannerReader;
    private ScannerReader mNfcScannerReader;
    private ScannerReader mBleScannerReader;
    private ICardScanner.ScanCallback mScanCallback;
    private ICardReader.ConnectedCallback mConnectedCallback;
    private MutableLiveData<Resource<Card>> mCard = new MutableLiveData<>();
    private MediatorLiveData<Resource<Reader>> mReader = new MediatorLiveData<>();
    private Handler handler = new Handler();
    private Card connectedCard;
    private SimulateCardService simulateCardService;
    private long cardIdForSimulate;

    public static class Builder {
        private Context mContext;
        private String mTargetAID;
        private int mScanMode = 0;
        private int mReadMode = 0;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        public Builder setTargetAID(@NonNull String aid) {
            mTargetAID = aid;
            return this;
        }

        public Builder setScanMode(@ScanMode int scanMode) {
            mScanMode = scanMode;
            return this;
        }

        public Builder setReadMode(@ReadMode int readMode) {
            mReadMode = readMode;
            return this;
        }

        public CardScannerReader build() {
            if (mTargetAID == null) mTargetAID = Reader.DEFAULT_TARGET_AID;
            if ((mScanMode & NFC_MODE) != 0 && ((mReadMode & (NFC_AUTO_MODE ^ NFC_MANUAL_MODE)) == 0)) {
                throw new IllegalArgumentException("Please set NFC read mode!");
            }
            if ((mScanMode & BLE_MODE) != 0 && ((mReadMode & (BLE_AUTO_MODE ^ BLE_MANUAL_MODE)) == 0)) {
                throw new IllegalArgumentException("Please set BLE read mode!");
            }
            return new CardScannerReader(mContext, mTargetAID, mScanMode, mReadMode);
        }
    }

    private CardScannerReader(@NonNull Context context, String targetAID, int scanMode, int readMode) {
        mContext = context.getApplicationContext();
        setTargetAID(targetAID);
        setScanReadMode(scanMode, readMode);
    }

    public void setTargetAID(@NonNull String aid) {
        mTargetAID = aid;
    }

    public String getTargetAID() {
        return mTargetAID;
    }

    /**
     * Notice：It will remove and destroy old scanners and readers if they are not in the new scanMode.
     * New scanners and readers may be added if there was no related scanner and reader before.
     * Device should have nfc/ble feature to support related scan mode, otherwise it does nothing.
     *
     * @param scanMode
     * @param readMode
     */
    public void setScanReadMode(@ScanMode int scanMode, @ReadMode int readMode) {
        if ((scanMode & NFC_MODE) != 0 && ((readMode & (NFC_AUTO_MODE ^ NFC_MANUAL_MODE)) == 0)) {
            throw new IllegalArgumentException("Invalid NFC scan/read mode!");
        }
        if ((scanMode & BLE_MODE) != 0 && ((readMode & (BLE_AUTO_MODE ^ BLE_MANUAL_MODE)) == 0)) {
            throw new IllegalArgumentException("Invalid BLE scan/read mode!");
        }
        if (scanMode == mScanMode && readMode == mReadMode) {
            return;
        }
        boolean canNfc = ((scanMode & NFC_MODE) != 0) && ReaderUtils.supportNfc(mContext);
        boolean canBle = ((scanMode & BLE_MODE) != 0) && ReaderUtils.supportBluetooth(mContext);
        if (!canNfc && mNfcScannerReader != null) {
            release(mNfcScannerReader);
            mNfcScannerReader = null;
        }
        if (!canBle && mBleScannerReader != null) {
            release(mBleScannerReader);
            mBleScannerReader = null;
        }
        mScanMode = scanMode;
        mReadMode = readMode;
        if (canNfc && mNfcScannerReader == null) {
            mNfcScannerReader = ScannerReader.build(new NfcCardDetector(this),
                    new NfcCardReader(this));
            mScannerReaders.add(mNfcScannerReader);
        }
        if (canBle && mBleScannerReader == null) {
            mBleScannerReader = ScannerReader.build(new BleCardScanner(mContext, this),
                    new BleCardReader(mContext, this));
            mScannerReaders.add(mBleScannerReader);
        }
    }

    private void release(ScannerReader scannerReader) {
        scannerReader.mCardReader.disconnect();
        scannerReader.mCardScanner.destroy();
        scannerReader.mCardReader.setConnectedCallback(null);
        scannerReader.mCardScanner.setScanCallback(null);
        mScannerReaders.remove(scannerReader);
        if (mCurrentScannerReader == scannerReader) mCurrentScannerReader = null;
    }

    @NonNull
    public LiveData<Resource<Card>> getCard() {
        return mCard;
    }

    @NonNull
    public LiveData<Resource<Reader>> getReader() {
        return mReader;
    }

    @Override
    public void onCardScanned(@NonNull Resource<Reader> card) {
        switch (card.status) {
            case SUCCESS:
                // Usually UI observer display the card result and pass the card back by #parseAndConnect if it's manual mode.
                boolean isNfcCard = card.data.getTag() != null;
                boolean isBleCard = card.data.getDeviceInfo() != null;
                boolean isOneBleCard = true;
                // pass to reader, card with tag or devices if it's auto read mode
                boolean autoConnectNfc = isNfcCard && ((mReadMode & NFC_AUTO_MODE) != 0);
                boolean autoConnectBle = isBleCard && (
                        (((mReadMode & BLE_AUTO_MODE) != 0) && ((mReadMode & BLE_MANUAL_MODE) == 0))//auto mode
                                ||
                                (((mReadMode & BLE_AUTO_MODE) != 0) && ((mReadMode & BLE_MANUAL_MODE) != 0)
                                        && isOneBleCard)// manual auto mode
                );
                if (autoConnectNfc || autoConnectBle) {
                    if (card.data.getDeviceInfo() == null)
                        parseAndConnect(card.data);
                    else
                        mReader.postValue(card);
                }
                break;
            case ERROR:
                // here is always of the callback of ble scanner
                mReader.postValue(Resource.error(card.errorCode, card.message));
                break;
            case BLUETOOTH_SCAN_FINISH:
                mReader.postValue(Resource.bluetoothScanFinish(card.message));
                break;
        }
        if (mScanCallback != null) mScanCallback.onCardScanned(card);
    }

    @Override
    public void enterForeground(Activity activity) {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            scannerReader.mCardScanner.enterForeground(activity);
        }
    }

    @Override
    public void enterBackground(Activity activity) {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            scannerReader.mCardScanner.enterBackground(activity);
        }
    }

    // only for ble
    @Override
    public void startScan() {
        if (ENotesSDK.config.debugForEmulatorCard) {
            simulateCardService = RetrofitFactory.getSimulateCardService(mContext);
            mReader.addSource(simulateCardService.getBluetoothList(), (responseEntityApiResponse -> {
                if (responseEntityApiResponse.isSuccessful()) {
                    if (responseEntityApiResponse.body.getCode() == 0 && responseEntityApiResponse.body.getData() != null) {
                        List<BluetoothEntity> bList = responseEntityApiResponse.body.getData();
                        new Thread(() -> {
                            for (BluetoothEntity entity : bList) {
                                Reader reader = new Reader();
                                reader.setDeviceInfo(entity);
                                handler.post(() -> {
                                    mReader.postValue(Resource.success(reader));
                                });
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.post(() -> {
                                mReader.postValue(Resource.bluetoothScanFinish(""));
                            });
                        }).start();
                    }
                } else {
                    mReader.postValue(Resource.error(ErrorCode.NET_ERROR, "can not find server"));
                }
            }));
            return;
        }
//        mReader.postValue(Resource.start("start scan"));
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            if (scannerReader == mBleScannerReader) {
                scannerReader.mCardReader.disconnect();
                scannerReader.mCardScanner.startScan();
            }
        }
    }

    @Override
    public void destroy() {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            scannerReader.mCardScanner.destroy();
        }
    }

    @Override
    public void deliveryCard(@NonNull Reader reader) {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            scannerReader.mCardScanner.deliveryCard(reader);
        }
    }

    @Override
    public void setScanCallback(@Nullable ScanCallback scanCallback) {
        mScanCallback = scanCallback;
    }

    @Override
    public boolean isConnected() {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            if (scannerReader.mCardReader.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPresent() {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            if (scannerReader.mCardReader.isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void disconnect() {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            scannerReader.mCardReader.disconnect();
        }
    }

    @Override
    public void parseAndConnect(@NonNull Reader reader) {
        List<ScannerReader> scannerReaders = mScannerReaders;
        for (ScannerReader scannerReader : scannerReaders) {
            if (ENotesSDK.config.debugForEmulatorCard) {
                if (scannerReader.mCardReader instanceof BleCardReader && reader.getDeviceInfo() != null) {

                    new Thread(() -> {
                        try {
                            ApiResponse<ResponseEntity<BluetoothEntity>> cardEntity = ApiProvider.getValue(simulateCardService.connectBluetooth(reader.getDeviceInfo().getAddress()));
                            if (cardEntity.isSuccessful() && cardEntity.body.getCode() == 0) {
                                cardIdForSimulate = cardEntity.body.getData().getId();
                                mCurrentScannerReader = scannerReader;
                                detectCard();
                                parseCardFinish();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();


                    continue;
                }
            }
            // connect in parallel
            Completable.fromAction(() -> scannerReader.mCardReader.parseAndConnect(reader))
                    .subscribeOn(Schedulers.newThread()).subscribe();
        }
    }

    @Override
    public void prepareToRead(String aid) throws CommandException {
        ScannerReader scannerReader = mCurrentScannerReader;
        if (scannerReader != null) scannerReader.mCardReader.prepareToRead(aid);
    }

    @NonNull
    @Override
    public String transceive(@NonNull Command command) throws CommandException {
        ScannerReader scannerReader = mCurrentScannerReader;
        if (scannerReader != null) {
            String transceive = scannerReader.mCardReader.transceive(command);
            return transceive;
        }
        throw new CommandException(ErrorCode.NFC_DISCONNECTED, "tag_connection_lost");
    }

    @NonNull
    public TLVBox transceive2TLV(@NonNull Command command) throws CommandException {
        if (ENotesSDK.config.debugForEmulatorCard) {
            try {
                ApiResponse<ResponseEntity<ApduEntity>> entity = ApiProvider.getValue(simulateCardService.transceiveApdu(cardIdForSimulate, command.getCmdStr()));
                if (entity.isSuccessful() && entity.body.getCode() == 0) {
                    String apduString = entity.body.getData().getResult();
                    byte[] bytes = ByteUtil.hexStringToBytes(apduString.substring(0, apduString.length() - 4));
                    return TLVBox.parse(bytes, 0, bytes.length);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new CommandException(ErrorCode.NFC_DISCONNECTED, "tag_connection_lost");
        }
        ScannerReader scannerReader = mCurrentScannerReader;
        if (scannerReader != null) {
            TLVBox transceive = scannerReader.mCardReader.transceive2TLV(command);
            return transceive;
        }
        throw new CommandException(ErrorCode.NFC_DISCONNECTED, "tag_connection_lost");
    }

    @Override
    public void setConnectedCallback(@Nullable ConnectedCallback connectedCallback) {
        mConnectedCallback = connectedCallback;
    }

    @Override
    public void onCardConnected(@NonNull Resource<ICardReader> reader) {
        LogUtils.i(TAG, "onCardConnected status:" + reader.errorCode);
        switch (reader.status) {
            case NFC_CONNECTED:
                mCard.postValue(Resource.nfcConnected("nfc_start"));
                break;
            case SUCCESS:
                // only keep the latest connect currently
                LogUtils.d(TAG, "onCardConnected: " + reader.data);
                List<ScannerReader> scannerReaders = mScannerReaders;
                for (ScannerReader scannerReader : scannerReaders) {
                    // multi-thread issue?
                    if (scannerReader.mCardReader != reader.data) {
                        scannerReader.mCardReader.disconnect();
                    } else {
                        mCurrentScannerReader = scannerReader;
                    }
                }
                // need a thread for bluetooth sync
                new Thread(() -> {
                    detectCard();
                    parseCardFinish();
                }).start();
                break;
            case ERROR:
                connectedCard = null;
                mCard.postValue(Resource.error(reader.errorCode, reader.message));
                break;
            case BLUETOOTH_PARSING:
                connectedCard = null;
                mCard.postValue(Resource.bluetoothParsingCard(reader.message));
                break;
            case BLUETOOTH_DISCONNECT:
                connectedCard = null;
                mCard.postValue(Resource.error(ErrorCode.BLUETOOTH_DISCONNECT, reader.message));
                parseCardFinish();
                break;
        }
        if (mConnectedCallback != null) mConnectedCallback.onCardConnected(reader);
    }

    @Override
    public void onCardDisconnected(@NonNull Resource<ICardReader> reader) {
        LogUtils.i(TAG, "onCardDisconnected status:" + reader.errorCode);
        if (reader.status == Status.ERROR) {
            connectedCard = null;
            mCard.postValue(Resource.error(reader.errorCode, reader.message));
            parseCardFinish();
        }
        if (mConnectedCallback != null) mConnectedCallback.onCardDisconnected(reader);
    }

    private void parseCardFinish() {
        handler.postDelayed(() -> {
            mCard.postValue(Resource.cardParseFinish());
        }, 500);
    }

    /**
     * Get card from current connected card device.
     */
    @WorkerThread
    public void detectCard() {
        long startTime = System.currentTimeMillis();
        Card card = new Card();
        try {
            LogUtils.d(TAG, "detectCoin prepareToRead mCurrentScannerReader:" + mCurrentScannerReader);
            if (!ENotesSDK.config.debugForEmulatorCard) {
                prepareToRead(mTargetAID);
            }
            if (mTargetAID != Reader.DEFAULT_TARGET_AID) {
                mCard.postValue(Resource.success(card));
                return;
            }
            readApduVersion();
            card.setCert(readCert());
            card.setCurrencyPubKey(readCurrencyPubKey());
            setCurrencyAddress(card);
            card.setStatus(readStatus());
            read1_2_0VersionData(card);
            checkDevicePrv(ByteUtil.hexStringToBytes(card.getCert().getPublicKey()));
            checkBlockChainPrv(CardUtils.isBTC(card.getCert().getBlockChain()) ? card.getBitCoinECKey().getPubKey() : card.getEthECKey().getPubKey());
            connectedCard = card;
            //differentiate success type
            if (mCurrentScannerReader.mCardReader instanceof NfcCardReader)
                mCard.postValue(Resource.success(card));
            else
                mCard.postValue(Resource.success(card, "bluetooth"));
        } catch (CommandException e) {
            LogUtils.d(TAG, "detectCoin exception: " + e.getMessage());
            if (e.getMessage().contains("Tag was lost")) {
                mCard.postValue(Resource.error(ErrorCode.NFC_DISCONNECTED, e.getMessage()));
            } else
                mCard.postValue(Resource.error(e.getCode(), e.getMessage()));
        } finally {
            LogUtils.d(TAG, "checkCard spend time: " + (System.currentTimeMillis() - startTime));
        }
    }

    private void readApduVersion() throws CommandException {
        String version = new String(transceive2TLV(Command.newCmd().setDesc("apdu version").setCmdStr("00CA0012")).getBytesValue(Commands.TLVTag.Apdu_Protocol_Version));
        if (version.compareTo(Constant.APDU.APDU_VERSION) > 0 && !ENotesSDK.config.debugCard) {
            throw new CommandException(ErrorCode.NOT_SUPPORT_CARD, "Not Support");
        }
    }

    private Cert readCert() throws CommandException {
        LogUtils.d(TAG, "read cert");
        StringBuffer certSB = new StringBuffer();
        boolean flag = true;
        int p1 = 0;
        Command command = Commands.getCertificate();
        while (flag) {
            String stringValue = transceive(command.setCmdStr("00CA0" + p1 + "30"));
            if (TextUtils.isEmpty(stringValue) || stringValue.length() < 510) {//if cert.bytes = 253,need read cert apdu again
                flag = false;
            } else {
                p1++;
            }
            certSB.append(stringValue);
        }
        byte[] bytes = ByteUtil.hexStringToBytes(certSB.toString());
        TLVBox tlvBox = TLVBox.parse(bytes, 0, bytes.length);
        String certHex = tlvBox.getStringValue(Commands.TLVTag.Device_Certificate);
        LogUtils.d(TAG, "certHex=" + certHex);
        Cert cert;
        try {
            cert = Cert.fromHex(certHex);
            if (cert == null || cert.getPublicKey() == null) {
                throw new CommandException(ErrorCode.INVALID_CARD, "No cert");
            }
            if (cert.getCertVersion() > Constant.APDU.CERT_VERSION && !ENotesSDK.config.debugCard) {
                throw new CommandException(ErrorCode.NOT_SUPPORT_CARD, "Not Support");
            }
//            if (!cert.getBlockChain().equals(Constant.BlockChain.BITCOIN) && !cert.getBlockChain().equals(Constant.BlockChain.ETHEREUM) && !cert.getBlockChain().equals(Constant.BlockChain.BITCOIN_CASH) && !cert.getBlockChain().equals(Constant.BlockChain.RIPPLE)) {
//                throw new CommandException(ErrorCode.NOT_SUPPORT_CARD, "Not Support");
//            }
            LogUtils.i(TAG, cert.toString());
            //verify manufacture cert
            verifyCert(cert);

        } catch (IllegalArgumentException ex) {
            throw new CommandException("Invalid cert", ex);
        }

        return cert;
    }

    /**
     * verify cert by contract public key
     *
     * @param cert
     * @throws CommandException
     */
    private void verifyCert(Cert cert) throws CommandException {
        LogUtils.i(TAG, "verify cert start");
        boolean testCard;
        if (cert.getSerialNumber() != null && (cert.getSerialNumber().toLowerCase().startsWith("test-") || cert.getSerialNumber().toLowerCase().startsWith("demo-"))) {
            testCard = true;
        } else {
            testCard = false;
        }
        Mfr mfr = ProviderFactory.getInstance(mContext).getApiProvider()
                .callCertPubKey(testCard ? Constant.ContractAddress.ABI_KOVAN_ADDRESS : Constant.ContractAddress.ABI_ADDRESS, cert.getVendorName(), cert.getBatch(), testCard);
        if (mfr == null) {
            throw new CommandException(ErrorCode.CALL_CERT_PUB_KEY_ERROR, "call pub key");
        }
        LogUtils.i(TAG, "call_publicKey: " + mfr.getPublicKey());
        try {
            if (!verifySignatureTwiceHash(ByteUtil.hexStringToBytes(mfr.getPublicKey()), cert.getTbsCertificate(), cert.getR(), cert.getS())) {
                throw new CommandException(ErrorCode.INVALID_CARD, "Invalid cert _ verify manufacture cert fail");
            }
        } catch (Exception e) {
            throw new CommandException(ErrorCode.INVALID_CARD, "Invalid cert _ verify manufacture cert fail");
        }
        LogUtils.i(TAG, "verify cert success");
    }

    /**
     * verify device private key
     *
     * @param publicKey
     * @throws CommandException
     */
    private void checkDevicePrv(byte[] publicKey) throws CommandException {
        LogUtils.d(TAG, "Device private key challenge start");
        devicePrvChallenge(this, publicKey);
        LogUtils.d(TAG, "Device private key challenge success");
    }

    /**
     * get block chain public key
     *
     * @return
     * @throws CommandException
     */
    private String readCurrencyPubKey() throws CommandException {
        LogUtils.d(TAG, "read currency pub key");
        String pubKey = transceive2TLV(Commands.getCurrencyPubKey()).getStringValue(Commands.TLVTag.BlockChain_PublicKey);
        if (TextUtils.isEmpty(pubKey))
            throw new CommandException(ErrorCode.INVALID_CARD, "wrong public key format");
        if (pubKey.length() == 130 && pubKey.startsWith("04"))
            return pubKey;
        else if (pubKey.length() == 128)
            return "04" + pubKey;
        else if (pubKey.length() == 66 && (pubKey.startsWith("02") || pubKey.startsWith("03")))
            return pubKey;
        throw new CommandException(ErrorCode.INVALID_CARD, "wrong public key format");

    }

    private void read1_2_0VersionData(Card card) {
        LogUtils.d(TAG, "read1_2_0VersionData");
        try {
            String account = transceive2TLV(Command.newCmd().setDesc("READ_ACCOUNT").setCmdStr("00CA0032")).getUtf8StringValue(Commands.TLVTag.Account);
            card.setAccount(account);
        } catch (CommandException e) {

        }


    }

    /**
     * set card address fof btc or eth
     *
     * @param card
     * @throws CommandException
     */
    private void setCurrencyAddress(Card card) throws CommandException {
        LogUtils.d(TAG, "calculate address");
        card.setAddress(CardUtils.getAddress(card, card.getCert().getBlockChain()));
        if (card.getAddress() == null) {
            throw new CommandException(ErrorCode.INVALID_CARD, "No valid currency public key");
        }
    }

    /**
     * read card status
     *
     * @return
     * @throws CommandException
     */
    private int readStatus() throws CommandException {
        LogUtils.d(TAG, "read status");
        String result = transceive2TLV(Commands.getTxSignCounter()).getStringValue(Commands.TLVTag.Transaction_Signature_Counter);
        if (result != null) {
            return new BigInteger(result,16).intValue();
        }
        throw new CommandException(ErrorCode.INVALID_CARD, "Fail to read status");
    }

    /**
     * verify block chain private key
     *
     * @param publicKey
     * @throws CommandException
     */
    private void checkBlockChainPrv(byte[] publicKey) throws CommandException {
        LogUtils.d(TAG, "block chain private key challenge start");
        blockChainPrvChallenge(this, publicKey);
        LogUtils.d(TAG, "block chain private key challenge success");
    }

    public Card getConnectedCard() {
        return connectedCard;
    }

    static class ScannerReader {
        ICardScanner mCardScanner;
        ICardReader mCardReader;

        static ScannerReader build(ICardScanner scanner, ICardReader reader) {
            ScannerReader scannerReader = new ScannerReader();
            scannerReader.mCardScanner = scanner;
            scannerReader.mCardReader = reader;
            return scannerReader;
        }
    }

}