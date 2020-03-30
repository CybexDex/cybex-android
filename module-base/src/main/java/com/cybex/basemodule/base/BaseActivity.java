package com.cybex.basemodule.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cybex.basemodule.R;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.LoadDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.help.StoreLanguageHelper;
import com.cybex.basemodule.injection.component.BaseActivityComponent;
import com.cybex.basemodule.injection.component.DaggerBaseActivityComponent;
import com.cybex.basemodule.injection.module.BaseActivityModule;
import com.cybex.basemodule.receiver.NetWorkBroadcastReceiver;
import com.cybex.basemodule.receiver.NetworkChangedCallback;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.PublicKey;
import com.cybex.provider.graphene.chain.Types;
import com.cybex.provider.utils.NetworkUtils;
import com.cybex.provider.utils.SpUtil;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.core.Callback;
import io.enotes.sdk.core.CardManager;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.utils.ReaderUtils;
import io.enotes.sdk.utils.Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;


public abstract class BaseActivity extends AppCompatActivity {

    private final String TAG = BaseActivity.class.getSimpleName();

    private static final String PARAM_NETWORK_AVAILABLE = "param_network_available";
    private static final int REQUEST_CODE_WRITE_SETTING = 100;

    public static boolean isActive;

    private LoadDialog mLoadDialog;
    private AlertDialog mHintDialog;

    private NetWorkBroadcastReceiver mNetWorkBroadcastReceiver;
    private NetworkChangedCallback mNetworkChangedCallback;
    private BaseActivityComponent mBaseActivityComponent;

    public CardManager cardManager;
    public Card currentCard;
    public static Card cardApp;
    private String mUserName;
    private boolean mIsLoggedIn;
    private SharedPreferences mSharedPreferences;
    private static Application mContext;


    protected Dialog dialog;
    private Dialog mEnotesPasswordDialog;
    private int tagLostCount;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateResources(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplication();
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                /**
                 * fix bug
                 * Android6.0 注册广播无法动态申请CHANGE_NETWORK_STATE权限，跳转至系统界面手动开启WRITE_SETTINGS权限
                 */
                requestPermissions();
            } else {
                onLazyLoad();
            }
        } else {
            registerNetWorkReceiver();
            onLazyLoad();
        }
        cardManager = new CardManager(this);
        setCardReaderCallback();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        EventBus.getDefault().register(this);
    }

    public static Context getContext() {
        return mContext;
    }

    private void setCardReaderCallback() {
        if (cardManager != null) {
            cardManager.setReadCardCallback(new Callback<Card>() {
                @Override
                public void onCallBack(Resource<Card> resource) {
                    if (resource.status == Status.SUCCESS) {
                        readCardOnSuccess(resource.data);
                    } else if (resource.status == Status.NFC_CONNECTED) {
                        nfcStartReadCard();
                    } else if (resource.status == Status.ERROR) {
                        readCardError(resource.errorCode, resource.message);
                    } else if (resource.status == Status.BLUETOOTH_PARSING) {
                    }
                }
            });
        }
    }

    protected void nfcStartReadCard() {
        if (tagLostCount < 4) {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_loading_nfc);
            dialog.show();
        }
    }

    /**
     * read card successful
     *
     * @param card
     */
    protected void readCardOnSuccess(final Card card) {
        currentCard = card;
        cardApp = card;
        mUserName = mSharedPreferences.getString(PREF_NAME, "");
        mIsLoggedIn = mSharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        tagLostCount = 0;
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        Types.public_key_type typesPublicKey = new Types.public_key_type(new PublicKey(card.getBitCoinECKey().getPubKeyPoint().getEncoded(true), true), true);
        Log.e("cardName", card.getAccount());
        if (mIsLoggedIn) {
            if (TextUtils.isEmpty(card.getAccount())) {
                if (!typesPublicKey.toString().equals(getLoginCybPublicKey())) {
                    CybexDialog.showLimitOrderCancelConfirmationDialog(this, String.format(getResources().getString(R.string.nfc_dialog_change_account_content), typesPublicKey.toString()), null,
                            new CybexDialog.ConfirmationDialogClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    try {
                                        dialog.dismiss();
                                        checkeNotesPassword(card);
                                    } catch (CommandException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } else {
                    if (!isLoginFromENotes()) {

                        try {
                            checkeNotesPassword(card);
                        } catch (CommandException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.nfc_toast_message_already_logged_in), R.drawable.ic_check_circle_green);
                    }
                }
            } else {
                if (!card.getAccount().equals(mUserName)) {
                    CybexDialog.showLimitOrderCancelConfirmationDialog(this, String.format(getResources().getString(R.string.nfc_dialog_change_account_content), card.getAccount()), null,
                            new CybexDialog.ConfirmationDialogClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    try {
                                        dialog.dismiss();
                                        checkeNotesPassword(card);
                                    } catch (CommandException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } else {
                    if (!isLoginFromENotes()) {

                        try {
                            checkeNotesPassword(card);
                        } catch (CommandException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.nfc_toast_message_already_logged_in), R.drawable.ic_check_circle_green);
                    }
                }
            }
        } else {
            try {
                checkeNotesPassword(card);
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkeNotesPassword(final Card card) throws CommandException {
        if (!cardManager.isConnected()) {
            if (ReaderUtils.supportNfc(this)) {
                showToast(this, getResources().getString(R.string.error_connect_card));
            } else {
                showToast(this, getString(R.string.error_connect_card_ble));
            }
            return;
        }
        if (!cardManager.isPresent()) {
            if (ReaderUtils.supportNfc(this)) { showToast(this, getString(R.string.error_connect_card)); } else {
                showToast(this, getString(R.string.error_connect_card_ble));
            }
            return;
        }
        Types.public_key_type typesPublicKey = new Types.public_key_type(new PublicKey(card.getBitCoinECKey().getPubKeyPoint().getEncoded(true), true), true);
        Types.public_key_type unCompressedPublicKey = new Types.public_key_type(new PublicKey(card.getBitCoinECKey().getPubKeyPoint().getEncoded(false), false), false);
        Map<String, Types.public_key_type> mapForPublicKeyFromEnotes = new ConcurrentHashMap<>();
        mapForPublicKeyFromEnotes.put(typesPublicKey.getAddress(), typesPublicKey);
        mapForPublicKeyFromEnotes.put(typesPublicKey.getPTSAddress(typesPublicKey.key_data), typesPublicKey);
        mapForPublicKeyFromEnotes.put(unCompressedPublicKey.getAddress(), typesPublicKey);
        mapForPublicKeyFromEnotes.put(unCompressedPublicKey.getPTSAddress(unCompressedPublicKey.key_data_uncompressed), typesPublicKey);

        Log.i("eNotes", typesPublicKey.toString());
        if (cardManager.getTransactionPinStatus() == 0) {
            setLoginPublicKey(card.getCurrencyPubKey());
            setPublicKeyFromCard(mapForPublicKeyFromEnotes);
            Log.e("pinStatus", "noPin");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loginByENotes(typesPublicKey.toString());
//                    loginByENotes(card.getAccount(), "123456789");
                }
            }, 500);
        } else {

            final Map<Long, String> cardIdToCardPasswordMap = SpUtil.getMap(this, "eNotesCardMap");
            if (cardIdToCardPasswordMap == null || cardIdToCardPasswordMap.size() == 0) {
                if (mEnotesPasswordDialog == null) {
                    CybexDialog.showVerifyEnotesCardPasswordDialog(
                            this,
                            getResources().getString(R.string.nfc_dialog_verify_enotes_password_titile),
                            new CybexDialog.ConfirmationDialogClickWithButtonTimerListener() {
                                @Override
                                public void onClick(Dialog dialog, Button button, EditText editText, TextView textView) {
                                    String cardPassword = editText.getText().toString().trim();
                                    textView.setVisibility(View.GONE);
                                    mEnotesPasswordDialog = dialog;
                                    try {
                                        if (cardManager.verifyTransactionPin(cardPassword)) {
                                            dialog.dismiss();
                                            Map<Long, String> map = new HashMap<Long, String>();
                                            map.put(card.getId(), cardPassword);
                                            SpUtil.putMap(BaseActivity.this, "eNotesCardMap", map);
                                            setLoginPublicKey(card.getCurrencyPubKey());
                                            setPublicKeyFromCard(mapForPublicKeyFromEnotes);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
//                                                    loginByENotes(card.getAccount(), "123456789");
                                                    loginByENotes(typesPublicKey.toString());
                                                }
                                            }, 500);
                                        } else {
                                            textView.setVisibility(View.VISIBLE);
                                            textView.setText(getResources().getString(R.string.error_incorrect_password));
                                        }
                                    } catch (CommandException e) {
                                        e.printStackTrace();
                                        textView.setVisibility(View.VISIBLE);
                                        textView.setText(getResources().getString(R.string.error_incorrect_password));

                                    }
                                }
                            }, new CybexDialog.ConfirmationDialogCancelListener() {
                                @Override
                                public void onCancel(Dialog dialog) {
                                    mEnotesPasswordDialog = null;
                                }
                            });
                }
            } else {
                String cardPasswordFromSp = cardIdToCardPasswordMap.get(card.getId());
                if (cardManager.verifyTransactionPin(cardPasswordFromSp)) {
                    setLoginPublicKey(card.getCurrencyPubKey());
                    setPublicKeyFromCard(mapForPublicKeyFromEnotes);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loginByENotes(typesPublicKey.toString());
//                            loginByENotes(card.getAccount(), "123456789");
                        }
                    }, 500);
                }
            }
        }
    }

    private void loginByENotes(final String email, final String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return;
        }
        showLoadDialog(true);
        try {
            BitsharesWalletWraper.getInstance().get_account_object(email, new MessageCallback<Reply<AccountObject>>() {
                @Override
                public void onMessage(Reply<AccountObject> reply) {
                    AccountObject accountObject = reply.result;
                    int result = BitsharesWalletWraper.getInstance().import_account_password(accountObject, email, password);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadDialog();
                            setLoginFrom(true);
                            EventBus.getDefault().post(new Event.LoginIn(email));
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putBoolean(PREF_IS_LOGIN_IN, true).apply();
                            sharedPreferences.edit().putString(PREF_NAME, email).apply();
                            ToastMessage.showNotEnableDepositToastMessage(BaseActivity.this, getResources().getString(R.string.nfc_toast_message_logged_in_successful_by_eNotes), R.drawable.ic_check_circle_green);
                            mEnotesPasswordDialog = null;
                        }
                    });

                }

                @Override
                public void onFailure() {
                    hideLoadDialog();
                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void loginByENotes(final String pubKey) {
        if (TextUtils.isEmpty(pubKey)) {
            return;
        }
        showLoadDialog(true);
        try {
            Set<String> pubKeys = new HashSet<>();
            pubKeys.add(pubKey);
            BitsharesWalletWraper.getInstance().get_key_references(pubKeys, new MessageCallback<Reply<List<List<String>>>>() {
                @Override
                public void onMessage(Reply<List<List<String>>> reply) {
                    try {
                        Log.e("keyRef", reply.toString());
                        Set<String> accountIds = new HashSet<String>(reply.result.get(0));

                        BitsharesWalletWraper.getInstance().get_account_objects(accountIds, new MessageCallback<Reply<List<AccountObject>>>() {
                            @Override
                            public void onMessage(Reply<List<AccountObject>> reply) {
                                if (reply.result != null && reply.result.size() > 0) {
                                    AccountObject accountObject = reply.result.get(0);
                                    int result = BitsharesWalletWraper.getInstance().import_account_password(accountObject, accountObject.name, "");
                                    Log.e("importPassword", Integer.toString(result));
                                    hideLoadDialog();
                                    setLoginFrom(true);
                                    EventBus.getDefault().post(new Event.LoginIn(accountObject.name));
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    sharedPreferences.edit().putBoolean(PREF_IS_LOGIN_IN, true).apply();
                                    sharedPreferences.edit().putString(PREF_NAME, accountObject.name).apply();
                                    ToastMessage.showNotEnableDepositToastMessage(BaseActivity.this, getResources().getString(R.string.nfc_toast_message_logged_in_successful_by_eNotes), R.drawable.ic_check_circle_green);
                                    mEnotesPasswordDialog = null;
                                    setLoginCybPublicKey(pubKey);
                                    EventBus.getDefault().post(new Event.EnotesLoginFromLoginPage(true));
                                }

                            }

                            @Override
                            public void onFailure() {
                                hideLoadDialog();
                            }
                        });
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {
                    hideLoadDialog();
                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    protected void readCardError(int code, String message) {
        if (code == ErrorCode.BLUETOOTH_DISCONNECT) {
            showToast(this, getString(R.string.bluetooth_connect_fail));
        } else if (code == ErrorCode.INVALID_CARD) {
            showToast(this, getString(R.string.invalid_card));
        } else if (code == ErrorCode.NOT_SUPPORT_CARD) {
            showToast(this, getString(R.string.not_support_card));
        } else if (code == ErrorCode.NOT_FIND_CARD) {
            showToast(this, getString(R.string.can_not_find_card));
        } else if (code == ErrorCode.NFC_DISCONNECTED) {
            showToast(this, getString(R.string.tag_connection_lost));
        } else if (code == ErrorCode.CALL_CERT_PUB_KEY_ERROR) {
            if (Utils.isNetworkConnected(this)) {
                showToast(this, message);
            } else {
                showToast(this, getString(R.string.network_unavailable));
            }
        } else {
            showToast(this, message);
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    protected void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

    }

    /**
     * 是否从eNotes登陆
     */
    protected boolean isLoginFromENotes() {
        SharedPreferences sharedPreferences = getSharedPreferences("enotes", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("from", false);
    }

    protected void setLoginFrom(boolean flag) {
        SharedPreferences.Editor editor = getSharedPreferences("enotes", Context.MODE_PRIVATE).edit();
        editor.putBoolean("from", flag);
        editor.apply();
    }

    protected String getLoginPublicKey() {
        SharedPreferences sharedPreferences = getSharedPreferences("enotes", Context.MODE_PRIVATE);
        return sharedPreferences.getString("key", "");
    }

    protected void setLoginPublicKey(String key) {
        SharedPreferences.Editor editor = getSharedPreferences("enotes", Context.MODE_PRIVATE).edit();
        editor.putString("key", key);
        editor.apply();
    }

    protected Map<String, Types.public_key_type> getPublicKeyFromCard() {
        return SpUtil.getMap(this, "publicKeyFromEnotes");
    }

    protected void setPublicKeyFromCard(Map<String, Types.public_key_type> map) {
        SpUtil.putMap(this, "publicKeyFromEnotes", map);
    }

    protected void setLoginCybPublicKey(String cybKey) {
        SharedPreferences.Editor editor = getSharedPreferences("enotes", Context.MODE_PRIVATE).edit();
        editor.putString("cybKey", cybKey);
        editor.apply();
    }

    protected String getLoginCybPublicKey() {
        SharedPreferences sharedPreferences = getSharedPreferences("enotes", Context.MODE_PRIVATE);
        return sharedPreferences.getString("cybKey", "");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        if (!isActive) {
            //app 从后台唤醒，进入前台
            isActive = true;
            Log.i("ACTIVITY", "程序从后台唤醒");
            EventBus.getDefault().post(new Event.IsOnBackground(false));
        }
        super.onResume();
        cardManager.enableNfcReader(this);
        MobclickAgent.onResume(this);
        Log.d(TAG, "onResume");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        cardManager.disableNfcReader(this);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        if (!isAppOnForeground()) {
            //app 进入后台
            isActive = false;//记录当前已经进入后台
            Log.i("ACTIVITY", "程序进入后台");
            EventBus.getDefault().post(new Event.IsOnBackground(true));
        }
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unregisterNetWorkCallback();
        } else {
            unregisterNetWorkReceiver();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Resources getResources() {
        /**
         * fix bug:CYM-455
         * app字体大小不跟随系统改变
         */
        Resources resources = super.getResources();
        if (resources.getConfiguration().fontScale != 1) {
            Configuration newConf = new Configuration();
            newConf.setToDefaults();
            resources.updateConfiguration(newConf, resources.getDisplayMetrics());
        }
        return resources;
    }

    private void requestPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(Manifest.permission.CHANGE_NETWORK_STATE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Permission>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Permission permission) {
                        if (!permission.granted && !canWriteSetting()) {
                            Intent intentSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intentSetting.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intentSetting, REQUEST_CODE_WRITE_SETTING);
                        } else {
                            onLazyLoad();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        finish();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean canWriteSetting() {
        return Settings.System.canWrite(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTING) {
            if (!canWriteSetting()) {
                finish();
            } else {
                onLazyLoad();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerNetWorkCallback() {
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkChangedCallback = new NetworkChangedCallback(this);
        conn.requestNetwork(new NetworkRequest.Builder().build(), mNetworkChangedCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void unregisterNetWorkCallback() {
        if (mNetworkChangedCallback != null) {
            ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            conn.unregisterNetworkCallback(mNetworkChangedCallback);
        }
    }

    /**
     * 注册广播
     */
    private void registerNetWorkReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetWorkBroadcastReceiver = new NetWorkBroadcastReceiver();
        registerReceiver(mNetWorkBroadcastReceiver, intentFilter);
    }

    /**
     * 注销广播
     */
    private void unregisterNetWorkReceiver() {
        if (mNetWorkBroadcastReceiver != null) {
            unregisterReceiver(mNetWorkBroadcastReceiver);
            mNetWorkBroadcastReceiver = null;
        }
    }

    protected final void showHintDialog(@StringRes int messageId) {
        if (mHintDialog == null) {
            mHintDialog = new AlertDialog.Builder(this, R.style.LoadDialog)
                    .setMessage(messageId)
                    .create();
        }
        mHintDialog.setMessage(getString(messageId));
        mHintDialog.show();
    }

    protected final void hideHintDialog() {
        if (mHintDialog != null && mHintDialog.isShowing()) {
            mHintDialog.dismiss();
        }
    }

    //show load dialog
    public final void showLoadDialog() {
        showLoadDialog(false);

    }

    public final void showLoadDialog(boolean isCancelable) {
        if (mLoadDialog == null) {
            mLoadDialog = new LoadDialog(this, R.style.LoadDialog);
        }
        mLoadDialog.setCancelable(isCancelable);
        mLoadDialog.show();
    }

    //hide load dialog
    public final void hideLoadDialog() {
        if (mLoadDialog != null && mLoadDialog.isShowing()) {
            mLoadDialog.dismiss();
        }
    }

    public BaseActivityComponent baseActivityComponent() {
        if (mBaseActivityComponent == null) {
            mBaseActivityComponent = DaggerBaseActivityComponent.builder()
                    .baseActivityModule(new BaseActivityModule(this))
                    .build();
        }
        return mBaseActivityComponent;
    }

    private Context updateResources(Context context) {
        String language = StoreLanguageHelper.getLanguageLocal(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkStateChanged(Event.NetWorkStateChanged event) {
        onNetWorkStateChanged(event.getState() != NetworkUtils.TYPE_NOT_CONNECTED);
    }

    public abstract void onNetWorkStateChanged(boolean isAvailable);

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    public void onLazyLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerNetWorkCallback();
        }
    }
}
