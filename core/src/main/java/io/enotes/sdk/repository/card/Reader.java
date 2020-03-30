package io.enotes.sdk.repository.card;

import android.bluetooth.BluetoothDevice;
import android.nfc.Tag;

import io.enotes.sdk.repository.api.entity.response.simulate.BluetoothEntity;


/**
 * A wrapper class for a NFC tag <b>or</b> some BLE devices.
 * Notice: this class do not support wrap NFC tag and BLE devices at the same time.
 */
public class Reader {
    public static String DEFAULT_TARGET_AID = "654e6f7465734170706c6574";
    private Tag tag;
    private BluetoothEntity device;

    public Tag getTag() {
        return tag;
    }

    /**
     * Set the wrapped tag and clean the ble devices if any.
     *
     * @param tag
     * @return
     */
    public Reader setTag(Tag tag) {
        this.tag = tag;
        if (tag != null) {
            this.device = null;
        }
        return this;
    }

    public BluetoothEntity getDeviceInfo() {
        return device;
    }

    /**
     * Set the ble devices and clean the tag if any.
     *
     * @param device
     * @return
     */
    public Reader setDeviceInfo(BluetoothDevice device) {
        this.device = new BluetoothEntity();
        this.device.setName(device.getName());
        this.device.setAddress(device.getAddress());
        if (device != null) {
            this.tag = null;
        }
        return this;
    }

    public Reader setDeviceInfo(BluetoothEntity device) {
        this.device = device;
        if (device != null) {
            this.tag = null;
        }
        return this;
    }


}
