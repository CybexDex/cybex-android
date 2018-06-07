package com.cybexmobile.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.R;
import com.cybexmobile.adapter.CommonRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.LockUpAssetObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LockAssetsActivity extends BaseActivity {

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    CommonRecyclerViewAdapter mAdapter;
    private List<String> mAddresses = new ArrayList<>();
    private List<LockUpAssetObject> mLockupAssetObjects = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_assets);
        initViews();
        loadData();
    }

    private void loadData(){
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreference.getString("name", "");
        String password = sharedPreference.getString("password","");
        mAddresses.addAll(BitsharesWalletWraper.getInstance().getAddressList(name, password));
        try {
            BitsharesWalletWraper.getInstance().get_balance_objects(mAddresses, mLockupAssetCallback);
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommonRecyclerViewAdapter(mLockupAssetObjects);
        mRecyclerView.setAdapter(mAdapter);
    }

    private long getTimeStamp(String strTimeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        TimeZone mTimeZone = calendar.getTimeZone();
        int mOffset = mTimeZone.getRawOffset();
        try {
            Date parsedDate = dateFormat.parse(strTimeStamp);
            return parsedDate.getTime() + mOffset;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mAdapter.notifyDataSetChanged();
        }
    };

    private WebSocketClient.MessageCallback mLockupAssetCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LockUpAssetObject>>>(){

        @Override
        public void onMessage(WebSocketClient.Reply<List<LockUpAssetObject>> reply) {
            List<LockUpAssetObject> lockUpAssetObjects = reply.result;
            for (LockUpAssetObject lockUpAssetObject : lockUpAssetObjects) {
                long timeStamp = getTimeStamp(lockUpAssetObject.vesting_policy.begin_timestamp);
                long currentTimeStamp = System.currentTimeMillis();
                long duration = lockUpAssetObject.vesting_policy.vesting_duration_seconds;
                if (timeStamp + duration * 1000 > currentTimeStamp) {
                    mLockupAssetObjects.add(lockUpAssetObject);
                    mHandler.sendEmptyMessage(1);
                }
            }
        }

        @Override
        public void onFailure() {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
