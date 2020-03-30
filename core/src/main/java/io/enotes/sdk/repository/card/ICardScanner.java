package io.enotes.sdk.repository.card;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.enotes.sdk.repository.base.Resource;


/**
 * Interface definition that abstracts away the feature of a Card Scanner.
 */
public interface ICardScanner {
    int NFC_MODE = 1; // Detect by Nfc
    int BLE_MODE = 2; // Scan by Ble
    int BOTH_MODE = NFC_MODE ^ BLE_MODE; // Detect/Scan by Nfc + Ble

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NFC_MODE, BLE_MODE, BOTH_MODE})
    @interface ScanMode {
    }

    /**
     * Foreground mode for some kinds of scanner, such as nfc
     */
    void enterForeground(Activity activity);

    /**
     * Background mode for some kinds of scanner, such as nfc
     */
    void enterBackground(Activity activity);

    /**
     * Start scanning Card
     */
    void startScan();

    /**
     * Release resources
     */
    void destroy();

    /**
     * Delivery card from other source.
     *
     * @param reader
     */
    void deliveryCard(@NonNull Reader reader);

    /**
     * Set a ScanCallback for the scanner.
     *
     * @param scanCallback
     */
    void setScanCallback(@Nullable ScanCallback scanCallback);

    interface ScanCallback {
        /**
         * A callback to be invoked when finding a card.
         * May have error.
         *
         * @param card
         */
        void onCardScanned(@NonNull Resource<Reader> card);
    }
}
