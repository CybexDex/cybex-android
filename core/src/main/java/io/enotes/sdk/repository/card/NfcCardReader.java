package io.enotes.sdk.repository.card;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.annotation.NonNull;

import org.ethereum.util.ByteUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.LogUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * NFC Controller Card Reader
 */
public class NfcCardReader implements ICardReader {
    private static final String TAG = NfcCardReader.class.getSimpleName();
    private static Object mLock = new Object();
    private static final int DEFAULT_CONNECT_TIMEOUT = 1000; // 1 second
    IsoDep mIsoDep;
    private ConnectedCallback mConnectedCallback;
    private int mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private Disposable mDisposable;

    /**
     * @param connectedCallback
     */
    public NfcCardReader(ConnectedCallback connectedCallback) {
        this(connectedCallback, DEFAULT_CONNECT_TIMEOUT);
    }

    /**
     * @param connectedCallback
     * @param connectTimeout    timeout value in milliseconds
     */
    public NfcCardReader(ConnectedCallback connectedCallback, int connectTimeout) {
        setConnectedCallback(connectedCallback);
        this.mConnectTimeout = connectTimeout;
    }

    @Override
    public void setConnectedCallback(ConnectedCallback connectedCallback) {
        this.mConnectedCallback = connectedCallback;
    }

    @Override
    public boolean isConnected() {
        synchronized (mLock) {
            return mIsoDep != null && mIsoDep.isConnected();
        }
    }

    @Override
    public boolean isPresent() {
        return isConnected();
    }

    @Override
    public void disconnect() {
        synchronized (mLock) {
            if (mIsoDep != null && mIsoDep.isConnected()) try {
                mIsoDep.close();
                mIsoDep = null;
                if (mDisposable != null) {
                    mDisposable.dispose();
                    mDisposable = null;
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public void parseAndConnect(@NonNull Reader reader) {
        if (reader.getTag() != null) onParseTag(reader.getTag());
    }

    @Override
    public void prepareToRead(String aid) throws CommandException {
        Command command = Commands.selectAID(aid);
        LogUtils.d(TAG, "Requesting remote AID: " + command);
        transceive(command);
    }

    @NonNull
    @Override
    public String transceive(@NonNull Command command) throws CommandException {
        IsoDep isoDep = mIsoDep;
        if (isoDep == null) throw new CommandException(ErrorCode.INVALID_CARD,"no connected card");
        try {
            LogUtils.d("command", "execute " + command + " start");
            String result = ByteUtil.toHexString(isoDep.transceive(command.getCmdByte()));
            LogUtils.d("command", "execute " + command + " end");
            LogUtils.i(TAG, "command response=" + result);
            if (!Commands.isSuccessResponse(result)) {
                LogUtils.d("command", "execute " + command + " invalid response");
                throw new CommandException(ErrorCode.INVALID_CARD,"invalid response: " + result + " for " + command);
            }
            return result.substring(0, result.length() - 4);
        } catch (IOException e) {
            LogUtils.d("command", "execute " + command + " exception", e);
            throw new CommandException(e);
        }
    }

    @NonNull
    @Override
    public TLVBox transceive2TLV(@NonNull Command command) throws CommandException {
        byte[] bytes = ByteUtil.hexStringToBytes(transceive(command));
        return TLVBox.parse(bytes, 0, bytes.length);
    }

    private void onParseTag(Tag tag) {
        LogUtils.i(TAG, "Parse a New tag: " + tag);
        IsoDep isoDep = IsoDep.get(tag);
        setIsoDep(isoDep);
        try {
            if (isoDep != null) {
                if (mConnectedCallback != null) {
                    mConnectedCallback.onCardConnected(Resource.nfcConnected("Parse a New tag"));
                }
                // Connect to the remote NFC device
                if (!isoDep.isConnected()) {
                    isoDep.setTimeout(mConnectTimeout);
                    isoDep.connect();
                }
                if (mConnectedCallback != null) {
                    mConnectedCallback.onCardConnected(Resource.success(this));
                    // start connect status check
                    synchronized (mLock) {
                        if (mDisposable != null) {
                            mDisposable.dispose();
                        }
                        mDisposable = Observable.interval(500, 500, TimeUnit.MILLISECONDS)
                                .observeOn(Schedulers.trampoline())
                                .subscribe(i -> checkConnection());
                    }
                }
            } else {
                throw new CommandException(ErrorCode.NFC_DISCONNECTED,"not get IsoDep");
            }
        } catch (IOException | CommandException e) {
            LogUtils.e(TAG, "Parse a New tag: " + e.getLocalizedMessage());
            if (mConnectedCallback != null)
                // TODO pass specific error
                mConnectedCallback.onCardConnected(Resource.error(ErrorCode.NFC_DISCONNECTED,"not get IsoDep"));
            setIsoDep(null);
        }
    }

    private void checkConnection() {
        if (!isConnected()) {
            LogUtils.e(TAG,"checkConnection is Tag connection lost");
            IsoDep tmpIsoDep;
            synchronized (mLock) {
                if (mDisposable != null) {
                    mDisposable.dispose();
                    mDisposable = null;
                }
                tmpIsoDep = mIsoDep;
            }
//            if (tmpIsoDep != null && mConnectedCallback != null)
//                mConnectedCallback.onCardDisconnected(Resource.error(ErrorCode.NFC_DISCONNECTED,"Tag connection lost"));
        }
    }

    private void setIsoDep(IsoDep isoDep) {
        synchronized (mLock) {
            mIsoDep = isoDep;
        }
    }
}
