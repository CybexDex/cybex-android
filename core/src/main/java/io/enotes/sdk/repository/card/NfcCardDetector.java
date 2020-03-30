package io.enotes.sdk.repository.card;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;

import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.ReaderUtils;

/**
 * NFC Controller Card Detector.
 */

public class NfcCardDetector implements ICardScanner, NfcAdapter.ReaderCallback {
    private static final String TAG = NfcCardDetector.class.getSimpleName();
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    private static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private ICardScanner.ScanCallback mScanCallback;

    public NfcCardDetector(ICardScanner.ScanCallback scanCallback) {
        setScanCallback(scanCallback);
    }

    @Override
    public void setScanCallback(ScanCallback scanCallback) {
        this.mScanCallback = scanCallback;
    }

    @Override
    public void enterForeground(Activity activity) {
        if (activity == null || !ReaderUtils.supportNfc(activity)) {
            return;
        }
        Log.i(TAG, "Enabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.enableReaderMode(activity, this, READER_FLAGS, null);
        }
    }

    @Override
    public void enterBackground(Activity activity) {
        if (activity == null || !ReaderUtils.supportNfc(activity)) {
            return;
        }
        Log.i(TAG, "Disabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.disableReaderMode(activity);
        }
    }

    @Override
    public void startScan() {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }

    @Override
    public void deliveryCard(@NonNull Reader reader) {
        if (mScanCallback != null && reader.getTag() != null)
            mScanCallback.onCardScanned(Resource.success(reader));
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (mScanCallback != null)
            mScanCallback.onCardScanned(Resource.success(new Reader().setTag(tag)));
    }
}
