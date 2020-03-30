package io.enotes.sdk.core;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.nfc.Tag;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.bitcoinj.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.core.interfaces.CardInterface;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.api.entity.response.simulate.BluetoothEntity;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.card.Command;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.card.Commands;
import io.enotes.sdk.repository.card.Reader;
import io.enotes.sdk.repository.card.TLVBox;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.repository.provider.CardProvider;
import io.enotes.sdk.utils.BtcRawTransaction;
import io.enotes.sdk.utils.EthRawTransaction;
import io.enotes.sdk.utils.Utils;
import io.enotes.sdk.utils.XrpRawTransaction;
import io.enotes.sdk.viewmodel.CardViewModel;

public class CardManager implements CardInterface {
    private CardProvider cardProvider;
    private @NonNull
    FragmentActivity fragmentActivity;
    private Callback readCardCallback;
    private Callback scanCardCallback;
    private Handler handler;

    public CardManager(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        handler = new Handler(fragmentActivity.getMainLooper());
        CardViewModel cardViewModel = ViewModelProviders.of(fragmentActivity).get(CardViewModel.class);
        cardProvider = cardViewModel.getCardProvider();
        initCallback();
    }

    public CardManager(Fragment fragment) {
        this.fragmentActivity = fragment.getActivity();
        handler = new Handler(fragmentActivity.getMainLooper());
        CardViewModel cardViewModel = ViewModelProviders.of(fragment).get(CardViewModel.class);
        cardProvider = cardViewModel.getCardProvider();
        initCallback();
    }

    private void initCallback() {
        if (cardProvider != null) {
            cardProvider.getCard().observe(fragmentActivity, (resource -> {
                if (resource.status == Status.SUCCESS) {
                }
                if (readCardCallback != null) {
                    readCardCallback.onCallBack(resource);
                }
            }));

            cardProvider.getReader().observe(fragmentActivity, (resource -> {
                if (scanCardCallback != null)
                    scanCardCallback.onCallBack(resource);
            }));
        }
    }

    @Override
    public void selectAid(String aid) {
        cardProvider.setTargetAID(aid);
    }

    @Override
    public void startBluetoothScan(@NonNull Callback<Reader> callback) {
        cardProvider.startScan();
        scanCardCallback = callback;
    }

    @Override
    public void stopBluetoothScan(Callback callback) {
        cardProvider.destroyScanner();
    }

    @Override
    public void connectBluetooth(BluetoothEntity bluetoothDevice) {
        cardProvider.parseAndConnect(new Reader().setDeviceInfo(bluetoothDevice));
    }

    @Override
    public void disconnectBluetooth() {
        cardProvider.disconnectCard();
    }

    @Override
    public void enableNfcReader(Activity activity) {
        cardProvider.enterForeground(activity);
    }

    @Override
    public void disableNfcReader(Activity activity) {
        cardProvider.enterBackground(activity);
    }

    @Override
    public void setReadCardCallback(Callback<Card> cardCallback) {
        readCardCallback = cardCallback;
    }

    @Override
    public boolean isConnected() {
        return cardProvider.isConnected();
    }

    @Override
    public boolean isPresent() {
        return cardProvider.isPresent();
    }

    @Override
    public void getBtcRawTransaction(Card card, String fees, String toAddress, List<EntUtxoEntity> unSpends, String omniValue, @NonNull Callback<String> callback) {
        new Thread(() -> {
            if (!cardProvider.isPresent() || cardProvider.getConnectedCard() == null || !cardProvider.getConnectedCard().getCurrencyPubKey().equals(card.getCurrencyPubKey())) {
                if (!ENotesSDK.config.debugForEmulatorCard) {
                    handler.post(() -> {
                        callback.onCallBack(Resource.error(ErrorCode.NOT_FIND_RIGHT_CARD, "not find right card when withdraw"));
                    });
                    return;
                }
            }
            BtcRawTransaction btcRawTransaction = new BtcRawTransaction();
            try {
                Transaction transaction = btcRawTransaction.createRawTransaction(card, cardProvider, Long.valueOf(fees), toAddress, 0, "", unSpends, omniValue);
                handler.post(() -> {
                    callback.onCallBack(Resource.success(ByteUtil.toHexString(transaction.bitcoinSerialize())));
                });
            } catch (CommandException e) {
                e.printStackTrace();
                handler.post(() -> {
                    callback.onCallBack(Resource.error(e.getCode(), e.getMessage()));
                });
            }
        }).start();

    }

    @Override
    public void getEthRawTransaction(Card card, String nonce, String estimateGas, String gasPrice, String toAddress, String value, byte[] data, Callback<String> callback) {
        new Thread(() -> {
            if (!cardProvider.isPresent() || cardProvider.getConnectedCard() == null || !cardProvider.getConnectedCard().getCurrencyPubKey().equals(card.getCurrencyPubKey())) {
                if (!ENotesSDK.config.debugForEmulatorCard) {
                    handler.post(() -> {
                        callback.onCallBack(Resource.error(ErrorCode.NOT_FIND_RIGHT_CARD, "not find right card when withdraw"));
                    });
                    return;
                }
            }
            EthRawTransaction ethRawTransaction = new EthRawTransaction();
            try {
                BigInteger toValue;
                if (value.equals("0")) {
                    toValue = new BigInteger(value);
                } else {
                    toValue = new BigInteger(value).subtract((new BigInteger(gasPrice).multiply(new BigInteger(estimateGas))));
                }
                String rawTransaction = ethRawTransaction.getRawTransaction(card, cardProvider, ByteUtil.bigIntegerToBytes(new BigInteger(nonce)), ByteUtil.bigIntegerToBytes(new BigInteger(gasPrice)), ByteUtil.bigIntegerToBytes(new BigInteger(estimateGas)), ByteUtil.hexStringToBytes(toAddress), ByteUtil.bigIntegerToBytes(toValue), data);
                handler.post(() -> {
                    callback.onCallBack(Resource.success(rawTransaction));
                });
            } catch (CommandException e) {
                e.printStackTrace();
                handler.post(() -> {
                    callback.onCallBack(Resource.error(e.getCode(), e.getMessage()));
                });
            }
        }).start();

    }

    @Override
    public void getXrpRawTransaction(Card card, String toAddress, String amount, int sequence, String fee, long destinationTag, Callback<String> callback) {
        new Thread(() -> {
            if (!cardProvider.isPresent() || cardProvider.getConnectedCard() == null || !cardProvider.getConnectedCard().getCurrencyPubKey().equals(card.getCurrencyPubKey())) {
                if (!ENotesSDK.config.debugForEmulatorCard) {
                    handler.post(() -> {
                        callback.onCallBack(Resource.error(ErrorCode.NOT_FIND_RIGHT_CARD, "not find right card when withdraw"));
                    });
                    return;
                }
            }
            XrpRawTransaction xrpRawTransaction = new XrpRawTransaction();
            BigInteger toValue;
            if (amount.equals("0")) {
                toValue = new BigInteger(amount);
            } else {
                toValue = (new BigInteger(amount).subtract((new BigInteger("20000000"))).subtract(new BigInteger(fee)));// ripple account must remain 20xrp for balance
            }
            if (toValue.compareTo(new BigInteger("0")) <= 0) {
                handler.post(() -> {
                    callback.onCallBack(Resource.error(ErrorCode.NET_ERROR, "No balance available"));
                });
                return;
            }
            try {
                String rawTransaction = xrpRawTransaction.createRawTransaction(card, cardProvider, toAddress, toValue.toString(), sequence, fee, destinationTag);
                handler.post(() -> {
                    callback.onCallBack(Resource.success(rawTransaction));
                });
            } catch (CommandException e) {
                e.printStackTrace();
                handler.post(() -> {
                    callback.onCallBack(Resource.error(e.getCode(), e.getMessage()));
                });
            }
        }).start();
    }

    @Override
    public String transmitApdu(@NonNull Command command) throws CommandException {
        return cardProvider.transceive(command);
    }

    @Override
    public void parseNfcTag(Tag tag) {
        cardProvider.parseAndConnect(new Reader().setTag(tag));
    }

    @Override
    public int getTransactionPinStatus() throws CommandException {
        byte[] bytes = ByteUtil.hexStringToBytes(transmitApdu(Commands.getFreezeStatus()));
        TLVBox tlvBox = TLVBox.parse(bytes, 0, bytes.length);
        return new BigInteger(tlvBox.getStringValue(Commands.TLVTag.Transaction_Freeze_Status), 16).intValue();
    }

    @Override
    public int getDisableTransactionPinTries() throws CommandException {
        byte[] bytes = ByteUtil.hexStringToBytes(transmitApdu(Commands.getReadUnfreezeTries()));
        TLVBox tlvBox = TLVBox.parse(bytes, 0, bytes.length);
        return new BigInteger(tlvBox.getStringValue(Commands.TLVTag.Transaction_Freeze_Tries), 16).intValue();
    }

    @Override
    public boolean enableTransactionPin(String pin) throws CommandException {
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Transaction_Freeze_Pin, pin.getBytes());
        transmitApdu(Commands.freezeTx(tlvBox.serialize()));
        return true;
    }

    @Override
    public boolean disableTransactionPin(String pin) throws CommandException {
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Transaction_Freeze_Pin, pin.getBytes());
        transmitApdu(Commands.unFreezeTx(tlvBox.serialize()));
        return true;
    }

    @Override
    public boolean verifyTransactionPin(String pin) throws CommandException {
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Transaction_Freeze_Pin, pin.getBytes());
        transmitApdu(Commands.verifyTxPin(tlvBox.serialize()));
        return true;
    }

    @Override
    public boolean updateTransactionPin(String oldPin, String newPin) throws CommandException {
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Transaction_Freeze_Pin, oldPin.getBytes());
        transmitApdu(Commands.verifyTxPin(tlvBox.serialize()));

        TLVBox tlvBox1 = new TLVBox();
        tlvBox1.putBytesValue(Commands.TLVTag.Transaction_Freeze_Pin, newPin.getBytes());
        transmitApdu(Commands.updateTxPin(tlvBox1.serialize()));
        return true;
    }

    @Override
    public EntSignature doSign(byte[] hash, Card card) throws CommandException {
        TLVBox tlvBox = new TLVBox();
        tlvBox.putBytesValue(Commands.TLVTag.Transaction_Hash, hash);
        try {
            byte[] bytes = ByteUtil.hexStringToBytes(transmitApdu(Commands.signTX(tlvBox.serialize())));
            TLVBox signatureTLV = TLVBox.parse(bytes, 0, bytes.length);
            String signature = signatureTLV.getStringValue(Commands.TLVTag.Transaction_signature);
            if (signature.length() != 128) {
                throw new CommandException(ErrorCode.INVALID_CARD, "please_right_card");
            }
            String r = signature.substring(0, 64);
            String s = signature.substring(64);
            ECKey.ECDSASignature sig = new ECKey.ECDSASignature(new BigInteger(r, 16), new BigInteger(s, 16)).toCanonicalised();
            int recId = -1;
            byte[] thisKey = card.getEthECKey().getPubKey();

            for (int i = 0; i < 4; ++i) {
                byte[] k = ECKey.recoverPubBytesFromSignature(i, sig, hash);
                if (k != null && Arrays.equals(k, thisKey)) {
                    recId = i;
                    break;
                }
            }
            return new EntSignature(sig.r.toString(16), sig.s.toString(16), recId);
        } catch (CommandException e) {
            throw e;
        }
    }
}
