package com.cybex.provider.graphene.chain;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class FullNodeServerSelect {

    //是否是正式服务器环境
    private boolean isOfficialServer = true;

    //正式节点
    private List<String> mListNode = new ArrayList<>();
    //测试节点
    private List<String> mListNode_test = Arrays.asList(
            "ws://47.100.98.113:38090",
        "wss://hangzhou.51nebula.com/",
        "wss://shenzhen.51nebula.com/"
    );

    private FullNodeServerSelect () {

    }

    public static FullNodeServerSelect getInstance() {
        return FullNodeServerSelectProvider.factory;
    }

    private static class FullNodeServerSelectProvider{
        private static final FullNodeServerSelect factory = new FullNodeServerSelect();
    }

    public String getServer() {
        return getAutoSelectServer(isOfficialServer ? mListNode : mListNode_test);
    }

    private String getAutoSelectServer(List<String> nodes) {
        List<WebSocket> listWebsocket = new ArrayList<>();
        final Object objectSync = new Object();
        final int nTotalCount = nodes.size();
        final List<String> listSelectedServer = new ArrayList<>();
        for (final String strServer : nodes) {
            Request request = new Request.Builder().url(strServer).build();
            OkHttpClient okHttpClient = new OkHttpClient();
            WebSocket webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(""); // 失败，则填空

                        if (listSelectedServer.size() == nTotalCount) {
                            objectSync.notify();
                        }
                    }
                }

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    super.onOpen(webSocket, response);
                    synchronized (objectSync) {
                        listSelectedServer.add(strServer);
                        objectSync.notify();
                    }
                }
            });
            listWebsocket.add(webSocket);
        }

        String strResultServer = "";
        synchronized (objectSync) {
            if (!listSelectedServer.isEmpty() && listSelectedServer.size() < nTotalCount ) {
                for (String strServer : listSelectedServer) {
                    if (!strServer.isEmpty()) {
                        strResultServer = strServer;
                        break;
                    }
                }
            }

            if (strResultServer.isEmpty()) {
                try {
                    objectSync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!listSelectedServer.isEmpty() && listSelectedServer.size() < nTotalCount ) {
                    for (String strServer : listSelectedServer) {
                        if (!strServer.isEmpty()) {
                            strResultServer = strServer;
                            break;
                        }
                    }
                }
            }
        }

        for (WebSocket webSocket : listWebsocket) {
            webSocket.close(1000, "close");
        }

        return strResultServer;
    }

    public void setOfficialServer(boolean officialServer) {
        isOfficialServer = officialServer;
    }

    public void setmListNode(List<String> mListNode) {
        this.mListNode = mListNode;
    }
}
