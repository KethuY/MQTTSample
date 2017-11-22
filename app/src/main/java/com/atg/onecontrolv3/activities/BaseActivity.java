package com.atg.onecontrolv3.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.atg.onecontrolv3.preferances.MyPreferences;
import com.atg.onecontrolv3.helpers.NetworkUtil;
import com.atg.onecontrolv3.helpers.Utils;

import static com.atg.onecontrolv3.helpers.Utils.isNetworkAvailable;

public class BaseActivity extends AppCompatActivity {

    public static final String NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private static BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyPreferences.applyTheme(this);
        super.onCreate(savedInstanceState);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isNetworkAvailable = NetworkUtil.getConnectivityStatus(BaseActivity.this) != 0;
                if (!isNetworkAvailable) {
                    Utils.showToast(BaseActivity.this, "No Internet");
                }
            }
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(NETWORK_CHANGE));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
