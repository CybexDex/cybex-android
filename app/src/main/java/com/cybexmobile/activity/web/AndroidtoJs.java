package com.cybexmobile.activity.web;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.data.GameJson;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;

public class AndroidtoJs extends Object {
    private FullAccountObject fullAccountObject;
    private BaseActivity mContext;

    public AndroidtoJs(BaseActivity context) {
        this.mContext = context;
    }

    @JavascriptInterface
    public String hello(String msg) {
        return "kkk";
    }

    @JavascriptInterface
    public String login() {
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(mContext.getSupportFragmentManager(), fullAccountObject.account, fullAccountObject.account.name, new UnlockDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                    Operations.base_operation withdrawOperation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(fullAccountObject.account.name, 0, 0, null, null, getExpiration());
                    String signedSignature = getSignedSignature(withdrawOperation);
                    double balanceUSDT = getUSDTBalance();
                    double feeBalance = getFeeBalance();
                    String result = gson.toJson(createLogInRequest(withdrawOperation, signedSignature, balanceUSDT, feeBalance));
                    EventBus.getDefault().post(new Event.onGameUnlocked(result));
                }
            });
        } else {
            Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
            Operations.base_operation withdrawOperation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(fullAccountObject.account.name, 0, 0, null, null, getExpiration());
            String signedSignature = getSignedSignature(withdrawOperation);
            double balanceUSDT = getUSDTBalance();
            double feeBalance = getFeeBalance();
            String result = gson.toJson(createLogInRequest(withdrawOperation, signedSignature, balanceUSDT, feeBalance));
            Log.e("LoginString", result);
            return result;
        }
        return null;
    }

    @JavascriptInterface
    public void redirected(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void collect(String toAccountName, String feeAssetId, String transferAssetId, String feeAmount, String transferAmount) {
        try {
            BitsharesWalletWraper.getInstance().get_account_object(toAccountName, new MessageCallback<Reply<AccountObject>>() {
                @Override
                public void onMessage(Reply<AccountObject> reply) {
                    AccountObject toAccountObject = reply.result;
                    if (toAccountObject != null) {
                        Log.e("ToAccountName", toAccountObject.name);
                        Log.e("ToAccountMemo", toAccountObject.options.memo_key.toString());
                        String memo = "game:deposit:" + fullAccountObject.account.name;
                        Operations.base_operation transferOperation = BitsharesWalletWraper.getInstance().getTransferOperation(
                                fullAccountObject.account.id,
                                toAccountObject.id,
                                ObjectId.create_from_string(transferAssetId),
                                (long) Double.parseDouble(feeAmount),
                                ObjectId.create_from_string(feeAssetId),
                                (long) Double.parseDouble(transferAmount),
                                memo,
                                fullAccountObject.account.options.memo_key,
                                toAccountObject.options.memo_key);

                        try {
                            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                                @Override
                                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                                            fullAccountObject.account, transferOperation, ID_TRANSER_OPERATION, reply.result);
                                    Log.e("AndroidToJs", signedTransaction.toString());
                                    try {
                                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mTransferCallback);
                                    } catch (NetworkStatusException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure() {

                                }
                            });
                        } catch (NetworkStatusException e) {
                            e.printStackTrace();
                        }
                    } else {
                        EventBus.getDefault().post(new Event.onGameDeposit(Constant.GAME_STATUS_NO_ACCOUNT));
                    }

                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private MessageCallback<Reply<String>> mTransferCallback = new MessageCallback<Reply<String>>() {
        @Override
        public void onMessage(Reply<String> reply) {
            if (reply.result == null && reply.error == null) {
                EventBus.getDefault().post(new Event.onGameDeposit(Constant.GAME_STATUS_SUCCESS));
            } else {
                EventBus.getDefault().post(new Event.onGameDeposit(Constant.GAME_STATUS_FAIL));

            }
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.onGameDeposit(Constant.GAME_STATUS_FAIL));
        }
    };

    @JavascriptInterface
    public void getAccountObject(String accountName) {
        try {
            BitsharesWalletWraper.getInstance().get_account_object(accountName, new MessageCallback<Reply<AccountObject>>() {
                @Override
                public void onMessage(Reply<AccountObject> reply) {
                    AccountObject accountObject = reply.result;
                    Log.e("ToAccountName", accountObject.name);
                    Log.e("ToAccountMemo", accountObject.options.memo_key.toString());

                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private Date getExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        return calendar.getTime();
    }

    public void setAccountObject(FullAccountObject fullAccountObject) {
        this.fullAccountObject = fullAccountObject;
    }

    public AccountObject getAccountObject() {
        return fullAccountObject.account;
    }

    private String getSignedSignature(Operations.base_operation withdrawOperation) {
        return BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(fullAccountObject.account, withdrawOperation);
    }

    private GameJson createLogInRequest(Operations.base_operation operation, String signature, double usdtBalance, double feeBalance) {
        GameJson gameJson = new GameJson();
        gameJson.setOp(operation);
        gameJson.setSigner(signature);
        gameJson.setBalance(usdtBalance);
        gameJson.setFee_balance(feeBalance);
        return gameJson;
    }

    private double getUSDTBalance() {
        List<AccountBalanceObject> accountBalanceObjectList = fullAccountObject.balances;
        for (AccountBalanceObject accountBalanceObject : accountBalanceObjectList) {
            if (accountBalanceObject.asset_type.toString().equals(Constant.ASSET_ID_USDT)) {
                return accountBalanceObject.balance / Math.pow(10, 6);
            }
        }
        return 0;
    }

    /**
     * Fee balance, CYB forced
     * @return Current fee balance
     */
    private double getFeeBalance() {
        List<AccountBalanceObject> accountBalanceObjectList = fullAccountObject.balances;
        for (AccountBalanceObject accountBalanceObject : accountBalanceObjectList) {
            if (accountBalanceObject.asset_type.toString().equals(Constant.ASSET_ID_CYB)) {
                return accountBalanceObject.balance / Math.pow(10, 5);
            }
        }
        return 0;
    }
}
