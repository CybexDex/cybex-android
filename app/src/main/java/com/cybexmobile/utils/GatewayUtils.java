package com.cybexmobile.utils;

import com.cybex.provider.graphene.chain.Operations;
import com.cybexmobile.data.GatewayLogInRecordRequest;

import java.util.Calendar;
import java.util.Date;

public class GatewayUtils {

    public static Date getExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        return calendar.getTime();
    }

    public static GatewayLogInRecordRequest createLogInRequest(Operations.base_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = new GatewayLogInRecordRequest();
        gatewayLogInRecordRequest.setOp(operation);
        gatewayLogInRecordRequest.setSigner(signature);
        return gatewayLogInRecordRequest;
    }
}
