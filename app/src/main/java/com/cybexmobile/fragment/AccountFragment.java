package com.cybexmobile.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.crypto.Sha256Object;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.CoinAgeObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.apihk.LimitOrderWrapper;
import com.cybexmobile.R;
import com.cybex.provider.SettingConfig;
import com.cybexmobile.activity.address.AddressManagerActivity;
import com.cybexmobile.activity.balance.AccountBalanceActivity;
import com.cybexmobile.activity.gateway.GatewayActivity;
import com.cybexmobile.activity.hashlockup.HashLockupActivity;
import com.cybexmobile.activity.lockassets.LockAssetsActivity;
import com.cybexmobile.activity.login.LoginActivity;
import com.cybexmobile.activity.orders.OrdersHistoryActivity;
import com.cybexmobile.activity.setting.SettingActivity;
import com.cybexmobile.shake.AntiShake;
import com.cybexmobile.utils.KotlinAvatarJavaBridge;
import com.pixplicity.sharp.Sharp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

/**
 * 帐户界面
 */
public class AccountFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener{

    private static final String TAG = "AccountFragment";

    private static final int REQUEST_CODE_LOGIN = 1;

    private Unbinder mUnbinder;

    private boolean mIsLoginIn;
    private String mName;
    private AccountObject mAccountObject;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            WebSocketService mWebSocketService = binder.getService();
            if (mWebSocketService != null) {
                FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
                if (fullAccountObject != null) {
                    mAccountObject = fullAccountObject.account;
                    if (mAccountObject != null) {
                        loadCoinAge();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.account_tv_name)
    TextView mTvName;
    @BindView(R.id.account_iv_avatar)
    ImageView mIvAvatar;
    @BindView(R.id.account_coin_age_layout)
    LinearLayout mLlCoinAge;
    @BindView(R.id.account_coin_age)
    TextView mTvCoinAge;
    @BindView(R.id.account_coin_age_question_marker)
    ImageView mIvCoinAge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIsLoginIn = preferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = preferences.getString(PREF_NAME, "");
        if (mIsLoginIn) {
            Intent intent = new Intent(getActivity(), WebSocketService.class);
            getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mToolbar.inflateMenu(R.menu.menu_setting);
        mToolbar.setOnMenuItemClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViewData();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIsLoginIn) {
            getActivity().unbindService(mServiceConnection);
        }
    }

    @OnClick(R.id.account_coin_age_question_marker)
    public void onBalanceInfoClick(View view) {
        if (AntiShake.check(view.getId())) { return; }
        CybexDialog.showBalanceDialog(getContext(), getResources().getString(R.string.text_coin_age), getResources().getString(R.string.text_coin_age_dialog_content));
    }

    @OnClick(R.id.account_tv_name)
    public void onLoginClick(View view){
        if (AntiShake.check(view.getId())) { return; }
        if(mIsLoginIn){
            return;
        }
        toLogin();
    }

    @OnClick(R.id.account_layout_item_my_portfolio)
    public void onAllPortfolioClick(View view){
        if (AntiShake.check(view.getId())) { return; }
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getActivity(), AccountBalanceActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_address_manager)
    public void onAddressManagerClick(View view) {
        if (AntiShake.check(view.getId())) { return; }
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getActivity(), AddressManagerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_gateway)
    public void onGatewayClick(View view){
        if (AntiShake.check(view.getId())) { return; }
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getContext(), GatewayActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_open_order)
    public void onOpenOrderClick(View view){
        if (AntiShake.check(view.getId())) { return; }
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getActivity(), OrdersHistoryActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_lockup_asset)
    public void onLockupAssetsClick(View view){
        if (AntiShake.check(view.getId())) { return; }
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getContext(), LockAssetsActivity.class);
        intent.putExtra(INTENT_PARAM_NAME, mName);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_hash_lock)
    public void onHashLockupClick(View view) {
        if (AntiShake.check(view.getId())) { return; }
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getContext(), HashLockupActivity.class);
        intent.putExtra(INTENT_PARAM_NAME, mName);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event) {
        mIsLoginIn = false;
        mName = null;
        setViewData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginIn(Event.LoginIn event){
        mName = event.getName();
        mIsLoginIn = true;
        setViewData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateAccount(Event.UpdateFullAccount event) {
        mAccountObject = event.getFullAccount().account;
        loadCoinAge();
    }

    private void loadCoinAge() {
        Log.e("testAccount", mAccountObject.id.toString());
        LimitOrderWrapper.getInstance().get_account_token_age(mAccountObject.id.toString(), new MessageCallback<Reply<List<CoinAgeObject>>>() {
            @Override
            public void onMessage(Reply<List<CoinAgeObject>> reply) {
                if (reply != null && reply.result != null && reply.result.size() > 0) {
                    double score = reply.result.get(0).score;
                    double factor = SettingConfig.getInstance().getAgeRate();
                    Log.e("test", String.valueOf(reply.result.get(0).score));
                    Log.e("testFactor", String.valueOf(SettingConfig.getInstance().getAgeRate()));
                    int coinAge = (int) ((score / Math.pow(10,5)) * (1 - factor));
                    if (score == 0 || factor == 0) {
                        mLlCoinAge.setVisibility(View.GONE);
                    } else {
                        mLlCoinAge.setVisibility(View.VISIBLE);
                    }
                    mTvCoinAge.setText(String.valueOf(coinAge));
                }
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void toLogin(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    private void setViewData() {
        mTvName.setText(mIsLoginIn ? getResources().getString(R.string.account_hello) + " " + mName : getResources().getString(R.string.log_in_cybex));
        if (mIsLoginIn) {
            loadAvatar(mIvAvatar, 56);
            if(mAccountObject != null) {
                loadCoinAge();
            }
        } else {
            mIvAvatar.setImageResource(R.drawable.ic_account_avatar);
            mLlCoinAge.setVisibility(View.GONE);
        }
    }

    private void loadAvatar(ImageView imageView, int size) {
        Sha256Object.encoder encoder = new Sha256Object.encoder();
        encoder.write(mName.getBytes());
        String encoderString = encoder.result().toString();
        String svgString = KotlinAvatarJavaBridge.getAvatarSvg(encoderString, size, 0);
        Sharp.loadString(svgString).into(imageView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(INTENT_PARAM_LOGIN_IN, false)) {
                mName = data.getStringExtra(INTENT_PARAM_NAME);
                mIsLoginIn = true;
                setViewData();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (AntiShake.check(item.getItemId())) { return false; }
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;
        }
        return false;
    }

}
