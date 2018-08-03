package com.cybexmobile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.widget.Toast;

import com.cybexmobile.R;
import com.cybexmobile.event.Event;
import com.cybexmobile.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;

import static com.cybexmobile.utils.NetworkUtils.TYPE_NOT_CONNECTED;

public class NetWorkBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(conn == null){
                return;
            }
            conn.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    EventBus.getDefault().post(new Event.NetWorkStateChanged(NetworkUtils.getConnectivityStatus(conn)));
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    //无网
                    EventBus.getDefault().post(new Event.NetWorkStateChanged(TYPE_NOT_CONNECTED));
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                }

            });
        }
    }
}
