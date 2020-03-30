package io.enotes.sdk.repository.api;

import android.arch.lifecycle.LiveData;
import android.bluetooth.BluetoothDevice;

import java.util.List;

import io.enotes.sdk.repository.api.entity.ResponseEntity;
import io.enotes.sdk.repository.api.entity.response.simulate.ApduEntity;
import io.enotes.sdk.repository.api.entity.response.simulate.BluetoothEntity;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SimulateCardService {
    @GET("sdk/bluetooth/list")
    LiveData<ApiResponse<ResponseEntity<List<BluetoothEntity>>>> getBluetoothList();

    @GET("sdk/bluetooth/connect")
    LiveData<ApiResponse<ResponseEntity<BluetoothEntity>>> connectBluetooth(@Query("address") String address);

    @FormUrlEncoded
    @POST("sdk/card/transceive")
    LiveData<ApiResponse<ResponseEntity<ApduEntity>>> transceiveApdu(@Field("id") long id, @Field("apdu") String apdu);
}
