package com.cybex.basemodule.receiver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.support.annotation.RequiresApi;


import com.cybex.basemodule.event.Event;
import com.cybex.provider.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkChangedCallback extends ConnectivityManager.NetworkCallback {

    private Context context;

    public NetworkChangedCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        EventBus.getDefault().post(new Event.NetWorkStateChanged(NetworkUtils.getConnectivityStatus(context)));
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        EventBus.getDefault().post(new Event.NetWorkStateChanged(NetworkUtils.TYPE_NOT_CONNECTED));
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
    }

    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties);
    }
}
