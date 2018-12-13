package com.cybexmobile.intent;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cybex.eto.activity.details.EtoDetailsActivity;
import com.cybexmobile.activity.game.GameActivity;
import com.cybexmobile.activity.gateway.GatewayActivity;
import com.cybexmobile.activity.login.LoginActivity;
import com.cybexmobile.activity.transfer.TransferActivity;
import com.cybexmobile.activity.web.WebActivity;
import com.github.mikephil.charting.charts.Chart;

import java.util.HashMap;
import java.util.Map;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ETO_PROJECT_ID;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_URL;
import static com.cybexmobile.activity.gateway.GatewayActivity.INTENT_IS_DEPOSIT;

public class IntentFactory {

    private static final String PREFIX_HTTP = "http://";
    private static final String PREFIX_HTTPS = "https://";
    private static final String PREFIX_APP = "cybexapp://";
    private static final int ACTION_INVALID = 0;
    private static final int ACTION_HTTP = 1;
    private static final int ACTION_APP = 2;

    private int action;
    private String url;
    private boolean isLogin;//是否已经登录
    private boolean isNeedLogin;//是否跳转前需要登录
    private HashMap<String, Object> param = new HashMap<>();

    public IntentFactory(){

    }

    public IntentFactory action(String url){
        if(TextUtils.isEmpty(url)){
            action = ACTION_INVALID;
            return this;
        }
        this.url = url;
        if(url.startsWith(PREFIX_HTTP) || url.startsWith(PREFIX_HTTPS)){
            action = ACTION_HTTP;
        } else if(url.startsWith(PREFIX_APP)){
            action = ACTION_APP;
        } else {
            action = ACTION_INVALID;
        }
        return this;
    }

    public IntentFactory checkLogin(boolean isLogin) {
        this.isLogin = isLogin;
        return this;
    }

    public void intent(Context context){
        if(action == ACTION_INVALID){
            return;
        }
        Intent intent = null;
        try {
            intent = new Intent(context, mappingClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Map.Entry<String, Object> entry : param.entrySet()){
            if(entry.getValue() instanceof Byte) {
                intent.putExtra(entry.getKey(), (byte)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Short) {
                intent.putExtra(entry.getKey(), (short)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Integer) {
                intent.putExtra(entry.getKey(), (int)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Long) {
                intent.putExtra(entry.getKey(), (long)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Float) {
                intent.putExtra(entry.getKey(), (float)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Double) {
                intent.putExtra(entry.getKey(), (double)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Integer) {
                intent.putExtra(entry.getKey(), (int)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Chart) {
                intent.putExtra(entry.getKey(), (char)entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof Boolean) {
                intent.putExtra(entry.getKey(), (boolean) entry.getValue());
                continue;
            }
            if(entry.getValue() instanceof String) {
                intent.putExtra(entry.getKey(), (String)entry.getValue());
                continue;
            }
        }
        /**
         * fix online crash
         * android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity
         * context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
         */
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private Class<?> mappingClass() throws Exception{
        if(action == ACTION_INVALID){
            throw new Exception("------------------url invalid------------------");
        }
        if(action == ACTION_HTTP){
            isNeedLogin = false;
            param.put(INTENT_PARAM_URL, url);
            return WebActivity.class;
        }
        String urlCache = url.replace(PREFIX_APP, "");
        String[] urlSplit = urlCache.split("/");
        if(urlSplit.length == 1 && urlCache.equals("deposit")){
            isNeedLogin = true;
            if(isLogin){
                param.put(INTENT_IS_DEPOSIT, true);
                return GatewayActivity.class;
            }
            return LoginActivity.class;
        }
        if(urlSplit.length == 1 && urlCache.equals("withdraw")){
            isNeedLogin = true;
            if(isLogin){
                param.put(INTENT_IS_DEPOSIT, false);
                return GatewayActivity.class;
            }
            return LoginActivity.class;
        }
        if(urlSplit.length == 1 && urlCache.equals("transfer")){
            isNeedLogin = true;
            if(isLogin){
                return TransferActivity.class;
            }
            return LoginActivity.class;
        }

        if (urlSplit.length == 1 && urlCache.equals("game")) {
            isNeedLogin = true;
            if (isLogin) {
                return GameActivity.class;
            }
            return LoginActivity.class;
        }
        if(urlSplit.length == 2){
            throw new Exception("------------------url mapping failed------------------");
        }
        if(urlSplit.length == 3 && urlCache.contains("eto/project")){
            isNeedLogin = false;
            param.put(INTENT_PARAM_ETO_PROJECT_ID, urlSplit[2]);
            return EtoDetailsActivity.class;
        }
        throw new Exception("------------------url mapping failed------------------");
    }

}
