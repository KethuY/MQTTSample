package com.atg.onecontrolv3.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;

import java.util.Vector;

public class RelayButtonActivity extends BaseActivity {
    LinearLayout mainRelayBtnLl;
    Typeface mUiFontTf;
    TextView mRelayTv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relay_button_view);
        mainRelayBtnLl = (LinearLayout) findViewById(R.id.main_relay_btn_ll);
        mUiFontTf = Typeface.createFromAsset(getAssets(), "uifont.ttf");
        mRelayTv = (TextView) findViewById(R.id.tv_relay_btn);
    }
}
