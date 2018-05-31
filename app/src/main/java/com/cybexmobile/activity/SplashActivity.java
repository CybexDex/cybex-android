package com.cybexmobile.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cybexmobile.BuildConfig;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.R;
import com.g00fy2.versioncompare.Version;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends BaseActivity implements MarketStat.startFirstActivityListener {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("No Internet Connection, Please turn on Internet");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            checkVersion();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void startToRunFirstActivity() {
        Intent i = new Intent(SplashActivity.this, BottomNavigationActivity.class);
        startActivity(i);
        finish();
    }

    private void checkVersion() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://app.cybex.io/Android_update.json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                final String versionResponse = response.body().string();
                Log.e("mydata", versionResponse);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(versionResponse);
                    String versionName = jsonObject.getString("version");
                    final String updateUrl = jsonObject.getString("url");
                    JSONObject forceObject = jsonObject.getJSONObject("force");
                    boolean ifForce = forceObject.getBoolean(versionName);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    Version localVersion = new Version(BuildConfig.VERSION_NAME);
                    Version remoteVersion = new Version(versionName);
                    if (localVersion.isLowerThan(remoteVersion)) {
                        builder.setCancelable(false);
                        builder.setTitle("Update Available");
                        builder.setMessage("A new version of CybexDex is available. Please update to newest version now");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                                startActivity(browseIntent);
                            }
                        });
                        if (!ifForce) {
                            builder.setNegativeButton("Next Time", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MarketStat.getInstance().getWebSocketConnect(SplashActivity.this);

                                        }
                                    }, 2000);
                                }
                            });
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                builder.create().show();
                            }
                        });

                    } else {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MarketStat.getInstance().getWebSocketConnect(SplashActivity.this);
                            }
                        }, 2000);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
