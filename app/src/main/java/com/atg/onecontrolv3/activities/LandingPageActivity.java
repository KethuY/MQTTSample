package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.preferances.MyPreferences;

public class LandingPageActivity extends BaseActivity {

    TextView mApplTv, mSafety, mSurvilenceTv, mTimer, mScene, mGateway, mRegisterTv;
    CheckBox mShowInStartUpCb;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        initializations();
    }

    private void initializations() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mApplTv = (TextView) findViewById(R.id.appln_tv);
        mSafety = (TextView) findViewById(R.id.sfty_tv);
        mSurvilenceTv = (TextView) findViewById(R.id.serlinc_tv);
        mTimer = (TextView) findViewById(R.id.timer_tv);
        mScene = (TextView) findViewById(R.id.secen_tv);
        mGateway = (TextView) findViewById(R.id.gatewa_tv);
        mShowInStartUpCb = (CheckBox) findViewById(R.id.show_in_startup_cb);
        mRegisterTv = (TextView) findViewById(R.id.register_tv);


        Typeface mOCV3Font = Typeface.createFromAsset(this.getAssets(), "oc_font_v3.ttf");
        mApplTv.setTypeface(mOCV3Font);
        mSafety.setTypeface(mOCV3Font);
        mSurvilenceTv.setTypeface(mOCV3Font);
        mTimer.setTypeface(mOCV3Font);
        mScene.setTypeface(mOCV3Font);
        mGateway.setTypeface(mOCV3Font);

        String showInStartUp = MyPreferences.getString(MyPreferences.PrefType.SHOW_IN_START_UP, getApplicationContext());
        if (null != showInStartUp) {
            if (showInStartUp.equals("1")) {
                mShowInStartUpCb.setChecked(true);
            } else {
                mShowInStartUpCb.setChecked(false);
            }
        } else {
            MyPreferences.add(MyPreferences.PrefType.SHOW_IN_START_UP, "1", getApplicationContext());
            mShowInStartUpCb.setChecked(true);
        }

        mShowInStartUpCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    MyPreferences.add(MyPreferences.PrefType.SHOW_IN_START_UP, "1", getApplicationContext());
                } else {
                    MyPreferences.add(MyPreferences.PrefType.SHOW_IN_START_UP, "0", getApplicationContext());
                }
            }
        });

        mRegisterTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                startActivity(new Intent(LandingPageActivity.this, OTPActivity.class));
            }
        });

    }

    public void launchClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        switch (view.getId()) {
            case R.id.appln_tv:
                vibrator.vibrate(150);
                intent.putExtra("from", 1);
                break;
            case R.id.sfty_tv:
                vibrator.vibrate(150);
                intent.putExtra("from", 2);
                break;
            case R.id.serlinc_tv:
                vibrator.vibrate(150);
                intent.putExtra("from", 3);
                break;
            case R.id.timer_tv:
                vibrator.vibrate(150);
                intent.putExtra("from", 4);
                break;
            case R.id.secen_tv:
                vibrator.vibrate(150);
                intent.putExtra("from", 5);
                break;
            case R.id.gatewa_tv:
                vibrator.vibrate(150);
                intent.putExtra("from", 1);
                break;
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
