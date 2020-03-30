package io.enotes.sdk.repository.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;


public class Resource<T> {

    @NonNull
    public final int status;

    @NonNull
    public final int errorCode;

    @Nullable
    public final String message;

    @Nullable
    public final T data;

    public Resource(int status, int errorCode, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, ErrorCode.NOT_ERROR, data, null);
    }

    public static <T> Resource<T> success(@Nullable T data, String message) {
        return new Resource<>(Status.SUCCESS, ErrorCode.NOT_ERROR, data, message);
    }

    public static Resource bluetoothParsingCard(String message) {
        return new Resource(Status.BLUETOOTH_PARSING, ErrorCode.NOT_ERROR, null, message);
    }

    public static Resource bluetoothScanFinish(String message){
        return new Resource(Status.BLUETOOTH_SCAN_FINISH, ErrorCode.NOT_ERROR, null, message);
    }

    public static Resource nfcConnected(String message){
        return new Resource(Status.NFC_CONNECTED, ErrorCode.NOT_ERROR, null, message);
    }

    public static Resource cardParseFinish(){
        return new Resource(Status.CARD_PARSE_FINISH, ErrorCode.NOT_ERROR, null, "");
    }

    public static  Resource error(int errorCode, String msg) {
        return new Resource(Status.ERROR, errorCode, null, msg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Resource<?> resource = (Resource<?>) o;
        if (status != resource.status) {
            return false;
        }
        if (message != null ? !message.equals(resource.message) : resource.message != null) {
            return false;
        }
        return data != null ? data.equals(resource.data) : resource.data == null;
    }

    @Override
    public int hashCode() {
        int result = status;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
