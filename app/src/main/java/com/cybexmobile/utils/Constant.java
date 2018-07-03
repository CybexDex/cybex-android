package com.cybexmobile.utils;

/**
 * App常量
 */
public class Constant {

    /**
     * 交易界面买卖操作
     */
    public static final String ACTION_BUY = "action_buy";
    public static final String ACTION_SELL = "action_sell";

    /**
     * 界面跳转参数
     */
    public static final String INTENT_PARAM_ACTION = "intent_param_action";
    public static final String INTENT_PARAM_WATCHLIST = "intent_param_watchlist";
    public static final String INTENT_PARAM_FROM = "intent_param_from";
    public static final String INTENT_PARAM_ACCOUNT_BALANCE = "intent_param_account_balance";

    public static final String INTENT_PARAM_LOGIN_IN = "loginIn";
    public static final String INTENT_PARAM_NAME = "name";

    /**
     * SharePreference参数
     */
    public static final String PREF_IS_LOGIN_IN = "isLoginIn";
    public static final String PREF_NAME = "name";
    public static final String PREF_PASSWORD = "password";

    /**
     *  币ID
     */
    public static final String ASSET_ID_ETH = "1.3.2";
    public static final String ASSET_ID_CYB = "1.3.0";
    public static final String ASSET_ID_USDT = "1.3.27";
    public static final String ASSET_ID_BTC = "1.3.3";

    /**
     * 币symbol
     */
    public static final String ASSET_SYMBOL_ETH = "ETH";
    public static final String ASSET_SYMBOL_CYB = "CYB";
    public static final String ASSET_SYMBOL_USDT = "USDT";
    public static final String ASSET_SYMBOL_BTC = "BTC";

    /**
     * intent request and result code
     */
    public static final int REQUEST_CODE_SELECT_WATCHLIST = 1;
    public static final int RESULT_CODE_SELECTED_WATCHLIST = 1;

    /**
     * bundle save state参数
     */
    public static final String BUNDLE_SAVE_ACTION = "bundle_save_action";
    public static final String BUNDLE_SAVE_WATCHLIST = "bundle_save_watchlist";
    public static final String BUNDLE_SAVE_ACCOUNT_BALANCE = "bundle_save_account_balance";
    public static final String BUNDLE_SAVE_IS_LOGIN_IN = "bundle_save_is_login_in";
}
