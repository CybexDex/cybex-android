package io.enotes.sdk.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.List;

public class ReaderUtils {

    /**
     * Check whether the device supports nfc or not.
     *
     * @param context
     * @return
     */
    public static boolean supportNfc(@NonNull Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }

    /**
     * Check whether nfc is enabled or not.
     *
     * @param context
     * @return
     */
    public static boolean isNfcEnable(@NonNull Context context) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    /**
     * Check whether nfc is close.
     *
     * @param context
     * @return
     */
    public static boolean isNfcClose(@NonNull Context context) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return nfcAdapter != null && !nfcAdapter.isEnabled();
    }

    /**
     * Open NFC system setting page.
     *
     * @param context
     * @return false if open fail
     */
    public static boolean startNfcSetting(@NonNull Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * Check whether the device supports bluetooth_le or not.
     *
     * @param context
     * @return
     */
    public static boolean supportBluetooth(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Check whether bluetooth is enabled or not.
     *
     * @return
     */
    public static boolean isBluetoothEnable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Issues a request to enable Bluetooth through the system settings.
     *
     * @param fragment
     * @param requestCode
     * @return false if open fail
     */
    public static boolean enableBluetooth(@NonNull Fragment fragment, int requestCode) {
        try {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            fragment.startActivityForResult(enableBtIntent, requestCode);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * Check whether there is any app can handle the intent.
     *
     * @param context
     * @param intent
     * @return
     */
    public static boolean isActivityCallable(Context context, Intent intent) {
        List<ResolveInfo> ris = context.getPackageManager().queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return ris.size() > 0;
    }

    /**
     * Check whether gps and network location provider is enable.
     *
     * @param context
     * @return
     */
    public static boolean isLocationProviderEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
