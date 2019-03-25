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
    public static final String INTENT_PARAM_GAME_CONTEST_TRUE = "intent_param_game_contest_true";
    public static final String INTENT_PARAM_FROM = "intent_param_from";
    public static final String INTENT_PARAM_FEE = "intent_param_fee";
    public static final String INTENT_PARAM_CYB_ASSET_OBJECT = "intent_param_cyb_asset_object";
    public static final String INTENT_PARAM_FULL_ACCOUNT_OBJECT = "intent_param_full_account_object";
    public static final String INTENT_PARAM_TRANSFER_OPERATION = "intent_param_transfer_operation";
    public static final String INTENT_PARAM_TRANSFER_FROM_ACCOUNT = "intent_param_transfer_from_account";
    public static final String INTENT_PARAM_TRANSFER_TO_ACCOUNT = "intent_param_transfer_to_account";
    public static final String INTENT_PARAM_TRANSFER_MY_ACCOUNT = "intent_param_transfer_my_account";
    public static final String INTENT_PARAM_TIMESTAMP = "intent_param_timestamp";
    public static final String INTENT_PARAM_TRANSFER_FEE_ASSET = "intent_param_transfer_fee_asset";
    public static final String INTENT_PARAM_TRANSFER_ASSET = "intent_param_transfer_asset";
    public static final String INTENT_PARAM_LOAD_MODE = "intent_param_load_mode";
    public static final String INTENT_PARAM_UNLOCK_WALLET_PERIOD = "intent_param_unlock_wallet_period";
    public static final String INTENT_PARAM_ACCOUNT_BALANCE_ITEMS = "intent_param_account_balance_items";
    public static final String INTENT_PARAM_ETO_PROJECT_DETAILS = "intent_param_eto_project_details";
    public static final String INTENT_PARAM_ETO_ATTEND_ETO = "intent_param_eto_attend_eto";
    public static final String INTENT_PARAM_ETO_PROJECT_ID = "intent_param_eto_project_id";
    public static final String INTENT_PARAM_QR_CODE_TRANCTION = "intent_param_qr_code_transaction";
    public static final String INTENT_PARAM_TRANSACTIONID = "intent_param_transactionid";

    public static final String INTENT_PARAM_PRECISION = "intent_param_precision";
    public static final String INTENT_PARAM_PRECISION_SPINNER_POSITION = "intent_param_precision_spinner_position";
    public static final String INTENT_PARAM_SHOW_BUY_SELL_SPINNER_POSITION = "intent_param_show_bug_sell_spinner_position";
    public static final String INTENT_PARAM_ITEMS = "intent_param_items";
    public static final String INTENT_PARAM_SELECTED_ITEM = "intent_param_selected_item";
    public static final String INTENT_PARAM_ADDRESS = "intent_param_address";
    public static final String INTENT_PARAM_CRYPTO_NAME = "cryptoName";
    public static final String INTENT_PARAM_CRYPTO_ID = "cryptoId";
    public static final String INTENT_PARAM_CRYPTO_MEMO = "cryptoMemo";
    public static final String INTENT_PARAM_CRYPTO_TAG = "cryptoTag";

    public static final String INTENT_PARAM_LOGIN_IN = "loginIn";
    public static final String INTENT_PARAM_NAME = "name";
    public static final String INTENT_PARAM_URL = "intent_param_url";
    public static final String INTENT_PARAM_IS_MEMOKEY_NEEDED = "intent_param_is_memokey_needed";

    public static final String INTENT_PARAM_CHANNEL = "intent_param_channel";
    public static final String INTENT_PARAM_CHANNEL_TITLE = "intent_param_channel_title";

    public static final String INTENT_PARAM_FROM_BROWSER = "intent_param_from_browser";

    public static final String INTENT_PARAM_IS_LOAD_ALL = "intent_param_is_load_all";

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
    public static final String PREF_UNLOCK_WALLET_PERIOD = "pref_unlock_wallet_period";
    public static final String PREF_SERVER = "pref_server";
    public static final String PREF_GAME_INVITATION = "pref_game_invitation";
    public static final String PREF_HISTORY_URL = "pref_history_url";
    public static final String PREF_IS_CLICK_NO_MORE_REMINDER = "pref_is_clicked_no_more_reminder";
    public static final String PREF_PARAM_UNLOCK_BY_CARDS = "pref_param_unlock_by_cards";
    public static final String PREF_IS_CLOUD_PASSWORD_SET = "pref_is_cloud_password_set";
    public static final String PREF_IS_CARD_PASSWORD_SET = "pref_is_card_password_set";
    public static final String PREF_ADDRESS_TO_PUB_MAP = "pref_address_to_pub_map";

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
     * 币ID_TEST
     */
    public static final String ASSET_ID_ETH_TEST = "1.3.2";
    public static final String ASSET_ID_CYB_TEST = "1.3.0";
    public static final String ASSET_ID_USDT_TEST = "1.3.23";
    public static final String ASSET_ID_BTC_TEST = "1.3.3";


    public static final String ASSET_ID_ARENA_USDT = "1.3.1148";
    public static final String ASSET_ID_ARENA_ETH = "1.3.1149";
    public static final String ASSET_ID_ARENA_EOS = "1.3.1150";
    public static final String ASSET_ID_ARENA_BTC = "1.3.1151";

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
    public static final int REQUEST_CODE_UPDATE_ACCOUNT = 2;
    public static final int RESULT_CODE_UPDATE_ACCOUNT = 2;

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
    public static final String BUNDLE_SAVE_PRECISION = "bundle_save_precision";
    public static final String BUNDLE_SAVE_PRECISION_SPINNER_POSITION = "bundle_save_spinner_precision";
    public static final String BUNDEL_SAVE_SHOW_BUY_SELL_SPINNER_POSITION = "bundle_save_show_buy_sell_spinner_position";
    public static final String BUNDLE_SAVE_IS_LOAD_ALL = "bundle_save_is_load_all";

    /**
     * withdraw deposit fundType
     */
    public static final String WITHDRAW = "WITHDRAW";
    public static final String DEPOSIT = "DEPOSIT";
    public static final String[] Types = {"ALL", "WITHDRAW", "DEPOSIT"};

    /**
     * google play store application id
     */
    public static final String APPLICATION_ID = "com.cybexmobile.google";

    /**
     * market page spinner
     */
    public static final String[] DURATION_SPINNER = {"5m", "1h", "1d"};
    public static final String[] MA_INDEX_SPINNER = {"MA", "EMA", "MACD", "BOLL"};
    public static final String DURATION5M = "5m";
    public static final String DURATION1H = "1h";
    public static final String DURATION1D = "1D";
    public static final String INDEXMA = "MA";
    public static final String INDEXEMA = "EMA";
    public static final String INDEXMACD = "MACD";
    public static final String INDEXBOLL = "BOLL";

    /**
     *  game deposit status
     */
    public static final String GAME_STATUS_SUCCESS = "0";
    public static final String GAME_STATUS_FAIL = "1";
    public static final String GAME_STATUS_NO_ACCOUNT = "2";

    /**
     * Game Contest constant
     */
    public static final String CYBEX_CONTEST_FLAG = "GameContest";

    /**
     *  scan Result
     */
    public static final String SCAN_RESULT = "scan_result";
}
