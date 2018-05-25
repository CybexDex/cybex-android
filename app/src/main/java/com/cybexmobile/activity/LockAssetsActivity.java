package com.cybexmobile.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.R;
import com.cybexmobile.adapter.CommonRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class LockAssetsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_assets);
        initViews();
    }

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //actionBar.setDisplayHomeAsUpEnabled(true);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> arrs = new ArrayList<>();
        arrs.add("1");
        arrs.add("1");
        arrs.add("1");
        mRecyclerView.setAdapter(new CommonRecyclerViewAdapter<String>(this,
                R.layout.item_lock_assets, arrs) {
            @Override
            public void convert(RecyclerView.ViewHolder holder, String str) {

            }
        });
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
