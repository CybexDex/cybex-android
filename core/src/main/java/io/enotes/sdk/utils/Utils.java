package io.enotes.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigInteger;

import io.enotes.sdk.repository.db.entity.Mfr;


public class Utils {
    public static  boolean btcTestNetFlag=true;
    public static void showToast(Context context, String text) {
        new Handler(context.getMainLooper()).post(() -> {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        });
    }

    public static void saveShareTransactionId(Context context, String address, String txId) {
        SharedPreferences.Editor editor = context.getSharedPreferences("eNotes", Context.MODE_PRIVATE).edit();
        editor.putString(address, txId);
        editor.commit();
    }

    public static String getShareTransactionId(Context context, String address) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("eNotes", Context.MODE_PRIVATE);
        return sharedPreferences.getString(address, "");
    }

    /**
     * hex to Big Integer String
     *
     * @param hex
     * @return
     */
    public static String hexToBigIntString(String hex) {
        if (TextUtils.isEmpty(hex)) return "0";
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        return new BigInteger(hex, 16).toString();
    }

    public static int hexToBigInt(String hex) {
        if (TextUtils.isEmpty(hex)) return 0;
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        return new BigInteger(hex, 16).intValue();
    }

    public static BigInteger hexToBigInteger(String hex) {
        if (TextUtils.isEmpty(hex)) return new BigInteger("0");
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        return new BigInteger(hex, 16);
    }

    public static String intToHexString(String value) {
        if (TextUtils.isEmpty(value)) return "0x0";
        return new BigInteger(value).toString(16);
    }

    /**
     * whether connect network
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


}
