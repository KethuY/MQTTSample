package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.preferances.MyPreferences;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    Runnable r;
    Handler h;
    String showInStartUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        showInStartUp = MyPreferences.getString(MyPreferences.PrefType.SHOW_IN_START_UP, getApplicationContext());
        h = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                Utils.printLog(TAG, "showInStartUp:-:" + showInStartUp);
                if (showInStartUp != null) {
                    if (showInStartUp.equals("1")) {
                        startActivity(new Intent(SplashActivity.this, LandingPageActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                } else {
                    startActivity(new Intent(SplashActivity.this, LandingPageActivity.class));
                }
            }
        };
        h.postDelayed(r, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(r);
    }
}
