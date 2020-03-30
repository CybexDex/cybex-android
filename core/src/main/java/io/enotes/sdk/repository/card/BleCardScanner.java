package io.enotes.sdk.repository.card;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.LogUtils;
import io.enotes.sdk.utils.Utils;


/**
 * Bluetooth Card Scanner.
 * Device must have bluetooth le feature and bluetooth is enabled.
 */
public class BleCardScanner implements ICardScanner {
    private static final String TAG = BleCardScanner.class.getSimpleName();
    private static final long SCAN_PERIOD = 3000L;
    private ICardScanner.ScanCallback mScanCallback;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
    private Handler mHandler = new Handler();
    private boolean mScanning;
    private Map<String, String> addressMap = new HashMap<>();

    public BleCardScanner(@NonNull Context context, ICardScanner.ScanCallback scanCallback) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        setScanCallback(scanCallback);
    }

    public void setScanCallback(ScanCallback scanCallback) {
        this.mScanCallback = scanCallback;
    }

    @Override
    public void enterForeground(Activity activity) {
        // do nothing
    }

    @Override
    public void enterBackground(Activity activity) {
        // do nothing
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    public void startScan() {
        if (mBluetoothAdapter == null) {
            Utils.showToast(context, "error_bluetooth_not_supported");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mScanCallback.onCardScanned(Resource.error(ErrorCode.BLUETOOTH_UNABLE,"bluetooth is unable"));
            LogUtils.d(TAG, "bluetooth is unable");
            return;
        }
        scanLeDevice(true);
    }

    private synchronized void scanLeDevice(final boolean enable) {
        if (enable) {
            /* Stops scanning after a pre-defined scan period. */
            mHandler.postDelayed(() -> {
                if (mScanning) {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanCallback.onCardScanned(Resource.bluetoothScanFinish("scan finish"));
                }
            }, SCAN_PERIOD);
            addressMap.clear();
            mScanning = true;
            new Thread(() -> mBluetoothAdapter.startLeScan(mLeScanCallback)).start();
        } else if (mScanning) {
            mScanning = false;
            mScanCallback.onCardScanned(Resource.bluetoothScanFinish("scan finish"));
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /* Device scan callback. */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            LogUtils.d(TAG, "mLeScanCallback: find " + device.getName() + " devices");
            if (mScanCallback != null && !addressMap.containsKey(device.getAddress()) && (device.getName() == null ? "" : device.getName().toUpperCase()).contains("ACR")) {
                mScanCallback.onCardScanned(Resource.success(new Reader().setDeviceInfo(device)));
                addressMap.put(device.getAddress(), device.getName());
            }
        }
    };

    @Override
    public void destroy() {
        scanLeDevice(false);
    }

    @Override
    public void deliveryCard(@NonNull Reader reader) {
        if (mScanCallback != null && reader.getDeviceInfo() != null)
            mScanCallback.onCardScanned(Resource.success(reader));
    }

}
