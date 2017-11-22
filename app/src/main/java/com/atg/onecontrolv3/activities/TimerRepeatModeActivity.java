package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.adapters.TimerRepeatModeAdapter;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;

import java.util.ArrayList;

public class TimerRepeatModeActivity extends BaseActivity implements OnItemClickListener {

    String macNameStr;
    TextView repeatTv;
    CheckBox repeatCb;
    OnItemClickListener mListener;
    private TimerRepeatModeAdapter adapter;
    private boolean mTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_repeat_mode_activtiy);
        //macNameStr = MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext());
        ListView listView = (ListView) findViewById(R.id.days_lv);
        TextView save = (TextView) findViewById(R.id.save_tv);
        TextView cancel = (TextView) findViewById(R.id.cancel_tv);
        repeatTv = (TextView) findViewById(R.id.repeat_tv);
        repeatCb = (CheckBox) findViewById(R.id.repeat_cb);
        ArrayList<String> daysArrLst = new ArrayList<>();
        daysArrLst.add("Sunday");
        daysArrLst.add("Monday");
        daysArrLst.add("Tuesday");
        daysArrLst.add("Wednesday");
        daysArrLst.add("Thursday");
        daysArrLst.add("Friday");
        daysArrLst.add("Saturday");
        mListener = this;
        adapter = new TimerRepeatModeAdapter(TimerRepeatModeActivity.this, daysArrLst, mListener);
        setRepeatCheckBox();
        listView.setAdapter(adapter);
        setToolBar();
        mTouch = false;
        repeatCb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouch = true;
                return false;
            }
        });

        repeatCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mTouch) {
                    if (isChecked) {
                        for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {
                            adapter.isDaySelectedArrLst.set(i, true);
                        }
                    } else {
                        for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {
                            adapter.isDaySelectedArrLst.set(i, false);
                        }
                    }
                    setRepeatCheckBox();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setRepeatCheckBox() {
        int allDays = 0;
        for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {
            if (adapter.isDaySelectedArrLst.get(i)) {
                allDays++;
            }
        }
        if (allDays == 7) {
            repeatCb.setChecked(true);
        } else {
            repeatCb.setChecked(false);
        }
        allDays = 0;
    }

    private void setToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Edit Timer");
        getSupportActionBar().setSubtitle(macNameStr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void repeatClick(View view) {
        switch (view.getId()) {
            case R.id.repeat_tv:
                if (repeatCb.isChecked()) {
                    // repeatTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
                    repeatCb.setChecked(false);
                    for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {
                        adapter.isDaySelectedArrLst.set(i, false);
                    }
                } else {
                    repeatCb.setChecked(true);
                    // repeatTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {
                        adapter.isDaySelectedArrLst.set(i, true);
                    }
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.cancel_tv:
                onBackPressed();
                break;
            case R.id.save_tv:
                StringBuilder sb = new StringBuilder();
                if (adapter != null) {
                    for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {
                        if (adapter.isDaySelectedArrLst.get(i)) {
                            sb.append("1");
                        } else {
                            sb.append("0");
                        }
                    }

                    if (Utils.getTimerModelObj != null) {
                        if (repeatCb.isChecked()) {
                            Utils.getTimerModelObj.setRepeat(true);
                        } else {

                            if (!Utils.getTimerModelObj.isRepeat())
                                Utils.getTimerModelObj.setRepeat(false);
                        }
                    }

                }

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TimerRepeatModeActivity.this);
                SharedPreferences.Editor outState = sp.edit();
                outState.putString("days", sb.toString());
                outState.apply();

                Intent intent = new Intent(TimerRepeatModeActivity.this, EditTimerActivity.class);
                intent.putExtra("days", sb.toString());
                intent.putExtra("isFromRepeat", true);
                intent.putExtra("IsFrom", "2");
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        mTouch = false;
        if (adapter != null) {

            int count = 0;
            for (int i = 0; i < adapter.isDaySelectedArrLst.size(); i++) {

                if (adapter.isDaySelectedArrLst.get(i)) {
                    count++;
                }
            }

            if (count == 7) {
                repeatCb.setChecked(true);
            } else {
                repeatCb.setChecked(false);
            }
            count = 0;
        }
    }
}
