package com.cybex.basemodule.constant;

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
    public static final String INTENT_PARAM_FEE = "intent_param_fee";
    public static final String INTENT_PARAM_CYB_ASSET_OBJECT = "intent_param_cyb_asset_object";
    public static final String INTENT_PARAM_FULL_ACCOUNT_OBJECT = "intent_param_full_account_object";
    public static final String INTENT_PARAM_TRANSFER_OPERATION = "intent_param_transfer_operation";
    public static final String INTENT_PARAM_TRANSFER_FROM_ACCOUNT = "intent_param_transfer_from_account";
    public static final String INTENT_PARAM_TRANSFER_TO_ACCOUNT = "intent_param_transfer_to_account";
    public static final String INTENT_PARAM_TRANSFER_MY_ACCOUNT = "intent_param_transfer_my_account";
    public static final String INTENT_PARAM_TRANSFER_FEE_ASSET = "intent_param_transfer_fee_asset";
    public static final String INTENT_PARAM_TRANSFER_ASSET = "intent_param_transfer_asset";
    public static final String INTENT_PARAM_TRANSFER_BLOCK = "intent_param_transfer_block";
    public static final String INTENT_PARAM_LOAD_MODE = "intent_param_load_mode";
    public static final String INTENT_PARAM_ACCOUNT_BALANCE_ITEMS = "intent_param_account_balance_items";
    public static final String INTENT_PARAM_ETO_PROJECT_DETAILS = "intent_param_eto_project_details";

    public static final String INTENT_PARAM_ITEMS = "intent_param_items";
    public static final String INTENT_PARAM_SELECTED_ITEM = "intent_param_selected_item";
    public static final String INTENT_PARAM_ADDRESS = "intent_param_address";
    public static final String INTENT_PARAM_CRYPTO_NAME = "cryptoName";
    public static final String INTENT_PARAM_CRYPTO_ID = "cryptoId";
    public static final String INTENT_PARAM_CRYPTO_MEMO = "cryptoMemo";

    public static final String INTENT_PARAM_LOGIN_IN = "loginIn";
    public static final String INTENT_PARAM_NAME = "name";

    public static final int FREQUENCY_MODE_ORDINARY_MARKET = 1;
    public static final int FREQUENCY_MODE_REAL_TIME_MARKET = 2;
    public static final int FREQUENCY_MODE_REAL_TIME_MARKET_ONLY_WIFI = 3;

    /**
     * SharePreference参数
     */
    public static final String PREF_IS_LOGIN_IN = "isLoginIn";
    public static final String PREF_NAME = "name";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_LOAD_MODE = "load_mode";
    public static final String PREF_SERVER = "pref_server";

    /**
     *
     */
    public static final String SERVER_OFFICIAL = "server_official";
    public static final String SERVER_TEST = "server_test";

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
    public static final String BUNDLE_SAVE_NAME = "bundle_save_name";
    public static final String BUNDLE_SAVE_FEE = "bundle_save_fee";
    public static final String BUNDLE_SAVE_CYB_FEE = "bundle_save_cyb_fee";
    public static final String BUNDLE_SAVE_CYB_ASSET_OBJECT = "bundle_save_cyb_asset_object";
    public static final String BUNDLE_SAVE_FULL_ACCOUNT_OBJECT = "bundle_save_full_account_object";
}
