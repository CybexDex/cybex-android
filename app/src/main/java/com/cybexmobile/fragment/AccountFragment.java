package com.cybexmobile.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.AccountBalanceActivity;
import com.cybexmobile.activity.GatewayActivity;
import com.cybexmobile.activity.LockAssetsActivity;
import com.cybexmobile.activity.LoginActivity;
import com.cybexmobile.activity.OpenOrdersActivity;
import com.cybexmobile.activity.SettingActivity;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.crypto.Sha256Object;
import com.cybexmobile.event.Event;
import com.cybexmobile.utils.KotlinAvatarJavaBridge;
import com.pixplicity.sharp.Sharp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_NAME;
import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;

/**
 * 帐户界面
 */
public class AccountFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener{

    private static final String TAG = "AccountFragment";

    private static final int REQUEST_CODE_LOGIN = 1;

    private Unbinder mUnbinder;

    private boolean mIsLoginIn;
    private String mName;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.account_tv_name)
    TextView mTvName;
    @BindView(R.id.account_iv_avatar)
    ImageView mIvAvatar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIsLoginIn = preferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = preferences.getString(PREF_NAME, "");
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
    }

    @OnClick(R.id.account_tv_name)
    public void onLoginClick(View view){
        if(mIsLoginIn){
            return;
        }
        toLogin();
    }

    @OnClick(R.id.account_layout_item_my_portfolio)
    public void onAllPortfolioClick(View view){
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getActivity(), AccountBalanceActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_gateway)
    public void onGatewayClick(View view){
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getContext(), GatewayActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_open_order)
    public void onOpenOrderClick(View view){
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getActivity(), OpenOrdersActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.account_layout_item_lockup_asset)
    public void onLockupAssetsClick(View view){
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        Intent intent = new Intent(getContext(), LockAssetsActivity.class);
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

    private void toLogin(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    private void setViewData() {
        mTvName.setText(mIsLoginIn ? getResources().getString(R.string.account_hello) + " " + mName : getResources().getString(R.string.log_in_cybex));
        if (mIsLoginIn) {
            loadAvatar(mIvAvatar, 56);
        } else {
            mIvAvatar.setImageResource(R.drawable.account_avatar);
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
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;
        }
        return false;
    }

}
