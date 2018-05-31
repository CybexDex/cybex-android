package com.cybexmobile.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.R;
import com.cybexmobile.adapter.CommonRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
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
    SharedPreferences mSharedPreference;
    private List<String> nameList = new ArrayList<>();
    private List<LockUpAssetObject> mLockupAssetObjects = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_assets);
        initViews();
    }

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String name = mSharedPreference.getString("name", "");
        String password = mSharedPreference.getString("password","");
        nameList.addAll(BitsharesWalletWraper.getInstance().getAddressList(name, password));
        try {
            List<LockUpAssetObject> lockUpAssetObjects = BitsharesWalletWraper.getInstance().get_balance_objects(nameList);
            for (LockUpAssetObject lockUpAssetObject : lockUpAssetObjects) {
                long timeStamp = getTimeStamp(lockUpAssetObject.vesting_policy.begin_timestamp);
                long currentTimeStamp = System.currentTimeMillis();
                long duration = lockUpAssetObject.vesting_policy.vesting_duration_seconds;
                if (timeStamp + duration * 1000 > currentTimeStamp) {
                    mLockupAssetObjects.add(lockUpAssetObject);
                }
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        CommonRecyclerViewAdapter adapter = new CommonRecyclerViewAdapter(mLockupAssetObjects);
        mRecyclerView.setAdapter(adapter);
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
