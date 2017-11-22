package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.preferances.MyPreferences;

public class ThemeSelectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_selection);
    }

    public void themeClick(View view) {
        int i = 1;
        switch (view.getId()) {
            /*case R.id.themeOneBtn:
                break;*/
            case R.id.themeTwoBtn:
                i = 2;
                break;
            case R.id.themeThreeBtn:
                i = 3;
                break;
            case R.id.themeFourBtn:
                i = 4;
                break;
        }
        MyPreferences.add(MyPreferences.PrefType.THEME_COLOR, i, getApplicationContext());
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        finish();
        startActivity(intent);
    }
}
