package io.enotes.sdk.repository.card;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;

import org.ethereum.util.ByteUtil;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.LogUtils;


/**
 * Bluetooth Card Reader
 * <p>
 * Note: it parses one by one if there are many devices currently.
 * And it can connect to one device at most at the same time.
 * This behavior can be changed if need.
 */
public class BleCardReader implements ICardReader {
    private static final String TAG = BleCardReader.class.getSimpleName();
    private AtomicBoolean mIsConnected = new AtomicBoolean(false);
    private ConnectedCallback mConnectedCallback;
    private String mDeviceAddress = "";

    /* Detected reader. */
    private BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;

    private ProgressDialog mProgressDialog;

    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;
    private int mConnectState = BluetoothReader.STATE_DISCONNECTED;
    private Context context;
    private String apduResponse;
    private int apduError;
    private Object lock = new Object();
    /* Default master key. */
    private static final String DEFAULT_3901_MASTER_KEY = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF";
    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";
    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00,
            0x40, 0x01};
    private static final byte[] AUTO_POLLING_STOP = {(byte) 0xE0, 0x00, 0x00,
            0x40, 0x00};
    private String authenticationKey;
    private Handler handler;
    private String uuId;
    private int currentCardStatus;

    public BleCardReader(@NonNull Context context, ConnectedCallback connectedCallback) {
        this.context = context;
        handler = new Handler(context.getMainLooper());
        setConnectedCallback(connectedCallback);
        setConnectListener();
    }


    @Override
    public void setConnectedCallback(@Nullable ConnectedCallback connectedCallback) {
        this.mConnectedCallback = connectedCallback;
    }

    @Override
    public boolean isConnected() {
        return mIsConnected.get();
    }

    @Override
    public boolean isPresent() {
        return currentCardStatus == BluetoothReader.CARD_STATUS_PRESENT && !TextUtils.isEmpty(uuId);
    }

    @Override
    public void disconnect() {
        disconnectReader();
    }

    @Override
    public void parseAndConnect(@NonNull Reader reader) {
        LogUtils.i(TAG, "parseAndConnect");
        if (reader.getDeviceInfo() != null) {
            if (mConnectState == BluetoothProfile.STATE_CONNECTED && !TextUtils.isEmpty(uuId) && mDeviceAddress.equals(reader.getDeviceInfo().getAddress())) {
                if (mConnectedCallback != null) {
                    mConnectedCallback.onCardConnected(Resource.success(BleCardReader.this));
                    mConnectedCallback.onCardConnected(Resource.bluetoothParsingCard("connected"));
                }
                return;
            } else if (mConnectState != BluetoothProfile.STATE_CONNECTING && !mDeviceAddress.equals(reader.getDeviceInfo().getAddress())) {
                disconnectReader();
            }
            mConnectedCallback.onCardConnected(Resource.bluetoothParsingCard("connecting"));
            mDeviceAddress = reader.getDeviceInfo().getAddress();
            onParseDevices(context);
        }
    }

    @Override
    public void prepareToRead(String aid) throws CommandException {
        LogUtils.i(TAG, "prepareToRead");
        try {
            mConnectedCallback.onCardConnected(Resource.bluetoothParsingCard("connected"));
            String uuid = readUUID();
            LogUtils.d(TAG, "UUID: " + uuid);
            Command command = Commands.selectAID(aid);
            transceive(command);
        } catch (Exception e) {
            // fail to reset, do not disconnect(in fact disconnect has no use, maybe the device and sdk has some issues)
            // can prepareToRead again if the device is still connected.
            //disconnect();
            throw new CommandException(ErrorCode.BLUETOOTH_DISCONNECT,"Tag was lost");
        }
    }

    @NonNull
    @Override
    public String transceive(@NonNull Command command) throws CommandException {
        LogUtils.d(TAG, "execute " + command + " start");
        try {
            if (!mBluetoothReader.transmitApdu(ByteUtil.hexStringToBytes(command.getCmdStr()))) {
                throw new CommandException(ErrorCode.INVALID_CARD,"transceive apdu fial: " + apduResponse + " for " + command);
            }
        } catch (Exception e) {
            throw new CommandException(ErrorCode.INVALID_CARD,"transceive apdu fial: " + apduResponse + " for " + command);
        }
        LogUtils.d("command", "execute " + command + " end");

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (apduError > 0) {
            throw new CommandException(ErrorCode.BLUETOOTH_DISCONNECT,"Tag was lost");
        }
        LogUtils.i(TAG, command + "\nresponse=" + apduResponse);
        if (!Commands.isSuccessResponse(apduResponse)) {
            LogUtils.d("command", "execute " + command + " invalid response");
            throw new CommandException(ErrorCode.INVALID_CARD,"invalid response: " + apduResponse + " for " + command);
        }
        return apduResponse.substring(0, apduResponse.length() - 4);

    }

    @NonNull
    @Override
    public TLVBox transceive2TLV(@NonNull Command command) throws CommandException {
        byte[] bytes = ByteUtil.hexStringToBytes(transceive(command));
        return TLVBox.parse(bytes, 0, bytes.length);
    }

    /**
     * read UUID
     *
     * @return
     */
    private String readUUID() {
        return uuId;
    }


    private void onParseDevices(Context context) {
        LogUtils.i(TAG, "onParseDevices");
        connectReader(context);
    }

    /*
     * Create a GATT connection with the reader. And detect the connected reader
     * once service list is available.
     */
    private boolean connectReader(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            LogUtils.d(TAG, "Unable to initialize BluetoothManager.");
            setonCardDisconnected("Unable to initialize BluetoothManager.");
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return false;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            LogUtils.d(TAG, "Unable to obtain a BluetoothAdapter.");
            setonCardDisconnected("Unable to obtain a BluetoothAdapter.");
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return false;
        }

        /*
         * Connect Device.
         */
        /* Clear old GATT connection. */
        if (mBluetoothGatt != null) {
            LogUtils.d(TAG, "Clear old GATT connection");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        /* Create a new connection. */
        final BluetoothDevice device = bluetoothAdapter
                .getRemoteDevice(mDeviceAddress);

        if (device == null) {
            LogUtils.d(TAG, "Device not found. Unable to connect.");
            setonCardDisconnected("Device not found. Unable to connect.");
            return false;
        }

        /* Connect to GATT server. */
        updateConnectionState(BluetoothReader.STATE_CONNECTING);
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        return true;
    }

    /* Start the process to enable the reader's notifications. */
    private void activateReader(BluetoothReader reader) {
        if (reader == null) {
            setonCardDisconnected("BluetoothReader is null");
            return;
        }
        if (reader instanceof Acr3901us1Reader) {
            /* Start pairing to the reader. */
            ((Acr3901us1Reader) mBluetoothReader).startBonding();
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            /* Enable notification. */
            mBluetoothReader.enableNotification(true);
        }
    }

    private void setConnectListener() {
        if (mGattCallback == null)
            mGattCallback = new BluetoothReaderGattCallback();
        mGattCallback
                .setOnConnectionStateChangeListener((gatt, state, newState) -> {
                    LogUtils.i(TAG, "onConnectionStateChange: " + "\nstate=" + state + "\nnewState=" + newState);
                    if (state != BluetoothGatt.GATT_SUCCESS) {
                        mConnectState = BluetoothReader.STATE_DISCONNECTED;
                        mIsConnected.set(false);
                        setonCardDisconnected("nowStat Ble connection lost");
                        return;
                    }

                    updateConnectionState(newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        /* Detect the connected reader. */
                        mIsConnected.set(true);
                        if (mBluetoothReaderManager != null) {
                            mBluetoothReaderManager.detectReader(
                                    gatt, mGattCallback);
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mBluetoothReader = null;
                        mIsConnected.set(false);
                        /*
                         * Release resources occupied by Bluetooth
                         * GATT client.
                         */
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.close();
                            mBluetoothGatt = null;
                        }
                        setonCardDisconnected("Ble connection lost");
                    }


                });
        /* Initialize mBluetoothReaderManager. */
        if (mBluetoothReaderManager == null)
            mBluetoothReaderManager = new BluetoothReaderManager();
        /* Register BluetoothReaderManager's listeners */
        mBluetoothReaderManager
                .setOnReaderDetectionListener(reader -> {
                    LogUtils.i(TAG, "setOnReaderDetectionListener");
                    if (reader instanceof Acr3901us1Reader) {
                        /* The connected reader is ACR3901U-S1 reader. */
                        LogUtils.d(TAG, "On Acr3901us1Reader Detected.");
                    } else if (reader instanceof Acr1255uj1Reader) {
                        /* The connected reader is ACR1255U-J1 reader. */
                        LogUtils.d(TAG, "On Acr1255uj1Reader Detected.");
                    } else {
                        LogUtils.d(TAG, "The device is not supported!");
                        setonCardDisconnected("The device is not supported!");
                        /* Disconnect Bluetooth reader */
                        LogUtils.d(TAG, "Disconnect reader!!!");
                        disconnectReader();
                        updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
                        return;
                    }

                    mBluetoothReader = reader;
                    setBluetoothReaderListener();
                    activateReader(reader);
                });
    }

    /*
     * Update listener
     */
    private void setBluetoothReaderListener() {
        /* Update status change listener */
//        if (mBluetoothReader instanceof Acr1255uj1Reader) {
//            ((Acr1255uj1Reader) mBluetoothReader)
//                    .setOnBatteryLevelChangeListener((bluetoothReader, batteryLevel) -> Log.i(TAG, "mBatteryLevelListener data: "
//                            + batteryLevel));
//        }
        /*******listener card status change********/
        mBluetoothReader
                .setOnCardStatusChangeListener((bluetoothReader, sta) -> {
                    Log.i(TAG, "mCardStatusListener sta: " + sta);
                    LogUtils.i(TAG, getCardStatusString(sta));
                    currentCardStatus = sta;
                    if (sta == BluetoothReader.CARD_STATUS_PRESENT && !TextUtils.isEmpty(uuId)) {
                        LogUtils.i(TAG, " get new card");
                        if (mConnectedCallback != null)
                            mConnectedCallback.onCardConnected(Resource.success(BleCardReader.this));
                    } else if (sta == BluetoothReader.CARD_STATUS_PRESENT && TextUtils.isEmpty(uuId) && mConnectState == BluetoothProfile.STATE_CONNECTED) {
                        handler.postDelayed(() -> {
                            if (TextUtils.isEmpty(uuId)) {
                                LogUtils.i(TAG, " Start powerOnCard.");
                                if (mBluetoothReader != null) {
                                    if (!mBluetoothReader.powerOnCard()) {
                                        mConnectedCallback.onCardDisconnected(Resource.error(ErrorCode.NOT_FIND_CARD,"Can not find card"));
                                        LogUtils.i(TAG, "powerOnCard card_reader_not_ready");
                                    } else {
                                        LogUtils.i(TAG, "powerOnCard");
                                    }
                                }
                            }
                        }, 1000);
                    } else if (sta == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
                        setonCardDisconnected("Power saving mode");
                        disconnectReader();
                    }
                });

        /* Wait for authentication completed. */
        mBluetoothReader
                .setOnAuthenticationCompleteListener((bluetoothReader, errorCode) -> {
                    if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                        handler.removeCallbacks(authenticationFailRunnable);
                        LogUtils.i(TAG, "Authentication Success!");
                        mConnectedCallback.onCardConnected(Resource.bluetoothParsingCard("connected"));
                        LogUtils.i(TAG, " Start polling.");
                        if (!mBluetoothReader.transmitEscapeCommand(AUTO_POLLING_START)) {
                            setonCardDisconnected("onAuthenticationComplete card_reader_not_ready");
                            LogUtils.i(TAG, "onAuthenticationComplete card_reader_not_ready");
                        }
                    } else {
                        setonCardDisconnected("Authentication Failed!");
                        LogUtils.i(TAG, "Authentication Failed!");
                    }
                });

        /* Wait for receiving ATR string. [after power on]*/
        mBluetoothReader
                .setOnAtrAvailableListener((bluetoothReader, atr, errorCode) -> {
                    LogUtils.i(TAG, "onAtrAvailable UUID=" + ByteUtil.toHexString(atr));
                    uuId = ByteUtil.toHexString(atr);
                    if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                        handler.postDelayed(() -> {
                            if (mConnectedCallback != null)
                                mConnectedCallback.onCardConnected(Resource.success(BleCardReader.this));
                        }, 300);
                    } else {
                        mConnectedCallback.onCardDisconnected(Resource.error(ErrorCode.NOT_FIND_CARD,"Can not find card"));
                    }
                });

        /* Wait for power off response. */
        mBluetoothReader
                .setOnCardPowerOffCompleteListener((bluetoothReader, result) -> LogUtils.i(TAG, "onCardPowerOffComplete"));

        /* Wait for response APDU. */
        mBluetoothReader
                .setOnResponseApduAvailableListener((bluetoothReader, apdu, errorCode) -> {
                    LogUtils.i(TAG, "ApduAvailableListener=" + ByteUtil.toHexString(apdu));
                    if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                        apduResponse = ByteUtil.toHexString(apdu);
                    }
                    apduError = errorCode;
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                });

        /* Wait for escape command response. */
        mBluetoothReader
                .setOnEscapeResponseAvailableListener((bluetoothReader, response, errorCode) -> {
                    LogUtils.i(TAG, "onEscapeResponseAvailable=" + ByteUtil.toHexString(response));
                    if ("e100004001".equals(ByteUtil.toHexString(response))) {
                        handler.postDelayed(() -> {
                            LogUtils.i(TAG, " Start powerOnCard.");
                            if (mBluetoothReader != null) {
                                if (!mBluetoothReader.powerOnCard()) {
                                    setonCardDisconnected("card reader not ready for powerOnCard");
                                    LogUtils.i(TAG, "powerOnCard card_reader_not_ready");
                                } else {
                                    LogUtils.i(TAG, "powerOnCard");
                                }
                            }
                        }, 300);

                    }
                });

        /* Handle on slot status available. */
        mBluetoothReader
                .setOnCardStatusAvailableListener((bluetoothReader, cardStatus, errorCode) -> {
                    if (errorCode != BluetoothReader.ERROR_SUCCESS) {
                        LogUtils.i(TAG, getErrorString(errorCode));
                    } else {
                        LogUtils.i(TAG, getCardStatusString(cardStatus));
                    }
                });

        mBluetoothReader
                .setOnEnableNotificationCompleteListener((bluetoothReader, result) -> {

                    if (result != BluetoothGatt.GATT_SUCCESS) {
                        /* Fail */
                        LogUtils.i(TAG, "The device is unable to set notification!");
                        setonCardDisconnected("The device is unable to set notification!");
                    } else {
                        LogUtils.i(TAG, "The device is ready to use!");
                        if (mBluetoothReader instanceof Acr3901us1Reader) {
                            authenticationKey = DEFAULT_3901_MASTER_KEY;
                        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
                            try {
                                authenticationKey = ByteUtil.toHexString(DEFAULT_1255_MASTER_KEY
                                        .getBytes("UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        authentication();

                    }
                });
    }

    private void authentication() {
        LogUtils.i(TAG, "authenticationKey=" + authenticationKey);
        byte masterKey[] = ByteUtil.hexStringToBytes(authenticationKey);
        if (masterKey != null && masterKey.length > 0) {
            /* Clear response field for the result of authentication. */
            LogUtils.i(TAG, " Start authentication.");
            /* Start authentication. */
            if (!mBluetoothReader.authenticate(masterKey)) {
                setonCardDisconnected("card reader not ready for authenticate");
                LogUtils.i(TAG, " authenticate card_reader_not_ready");
            } else {
                handler.postDelayed(authenticationFailRunnable, 1000);
                mConnectedCallback.onCardConnected(Resource.bluetoothParsingCard("connecting"));
                LogUtils.i(TAG, "Authenticating...");
            }
        } else {
            setonCardDisconnected("authenticate character format error");
            LogUtils.i(TAG, "Character format error!");
        }
    }

    /* Disconnects an established connection. */
    private void disconnectReader() {
        LogUtils.i(TAG, "disconnectReader");
        if (mBluetoothGatt == null) {
            updateConnectionState(BluetoothReader.STATE_DISCONNECTED);
            return;
        }
        mIsConnected.set(false);
        updateConnectionState(BluetoothReader.STATE_DISCONNECTING);
        mBluetoothGatt.disconnect();
        uuId = "";
    }


    /* Update the display of Connection status string. */
    private void updateConnectionState(final int connectState) {
        LogUtils.i(TAG, "updateConnectionState=" + connectState);
        mConnectState = connectState;
        if (connectState == BluetoothReader.STATE_CONNECTING) {
            LogUtils.i(TAG, "updateConnectionState=" + "connecting");
        } else if (connectState == BluetoothReader.STATE_CONNECTED) {
            LogUtils.i(TAG, "updateConnectionState=" + "connected");
        } else if (connectState == BluetoothReader.STATE_DISCONNECTING) {
            LogUtils.i(TAG, "updateConnectionState=" + "disconnecting");
        } else {
            LogUtils.i(TAG, "updateConnectionState=" + "disconnected");
        }

    }

    private String getErrorString(int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            return "";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            return "The checksum is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            return "The data length is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            return "The command is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            return "The command ID is unknown.";
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            return "The card operation failed.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            return "Authentication is required.";
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            return "The battery is low.";
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            return "Error characteristic is not found.";
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            return "Write command to reader is failed.";
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            return "Timeout.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            return "Authentication is failed.";
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            return "Undefined error.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            return "Received data error.";
        } else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
            return "The command failed.";
        }
        return "Unknown error.";
    }

    private String getCardStatusString(int cardStatus) {
        if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
            return "Absent.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
            return "Present.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
            return "Powered.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
            return "Power saving mode.";
        }
        return "The card status is unknown.";
    }

    private void setonCardDisconnected(String error) {
        LogUtils.i(TAG, "onCardDisconnected: " + error);
        if (error.contains("Ble connection lost")) {
            uuId = "";
            synchronized (lock) {
                lock.notifyAll();
            }
        }
        if (mConnectedCallback != null)
            mConnectedCallback.onCardDisconnected(Resource.error(ErrorCode.BLUETOOTH_DISCONNECT,"disconnect bluetooth"));
    }

    private Runnable authenticationFailRunnable = new Runnable() {
        @Override
        public void run() {
            disconnectReader();
            if (mConnectedCallback != null)
                mConnectedCallback.onCardDisconnected(Resource.error(ErrorCode.NOT_FIND_CARD,"Can not find card"));
        }
    };
}
