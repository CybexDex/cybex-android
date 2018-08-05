package com.cybexmobile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.cybexmobile.event.Event;

import org.greenrobot.eventbus.EventBus;

import static com.cybexmobile.utils.NetworkUtils.TYPE_MOBILE;
import static com.cybexmobile.utils.NetworkUtils.TYPE_NOT_CONNECTED;
import static com.cybexmobile.utils.NetworkUtils.TYPE_WIFI;

public class NetWorkBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(conn == null){
                return;
            }
            NetworkInfo networkWifi = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(networkWifi != null && networkWifi.isConnected()){
                EventBus.getDefault().post(new Event.NetWorkStateChanged(TYPE_WIFI));
                return;
            }
            NetworkInfo networkMobile = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(networkMobile != null && networkMobile.isConnected()){
                EventBus.getDefault().post(new Event.NetWorkStateChanged(TYPE_MOBILE));
                return;
            }
            EventBus.getDefault().post(new Event.NetWorkStateChanged(TYPE_NOT_CONNECTED));
        }
    }
}
