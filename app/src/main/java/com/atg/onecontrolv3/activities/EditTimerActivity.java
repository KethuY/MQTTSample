package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.GetMacInfoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static com.atg.onecontrolv3.helpers.Utils.getTimerModelObj;

public class EditTimerActivity extends BaseActivity {

    private static final String TAG = "EditTimerActivity";
    String macNameStr;
    String daysChecked = "0000000";
    String sb;
    String type = null;
    String isFrom = "";
    TransparentProgressDialog pd;
    String day = "";
    boolean isActionSwTouched;
    private Toast mToastToShow;
    private int mIntSelectedHr;
    private int mIntSelectedMin;
    private String repeatStr = "";
    private Switch actionSw;
    private String ADStr = "0";
    private TimePicker timePicker;
    private String sceneNameSt;
    private EditText sceneName;
    private int firstTimeEntry = 0;
    private TextView deleteBtn;
    private TextView repeat;
    private ArrayList<GetMacInfoModel> mMacInfoModels;
    private TextView setTimeTv;
    private LinearLayout labelLl, actionLl;
    private TextView appliances;
    private View v1, v2, v3, v4, v5, vt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_timer);
        //macNameStr = MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext());
        setToolBar();
        initializeViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState1) {
        super.onSaveInstanceState(outState1);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EditTimerActivity.this);
        SharedPreferences.Editor outState = sp.edit();
        outState.putString("sb", sb);
        String str = sceneName.getText().toString();
        outState.putString("sceneName", str);
        outState.putString("action", ADStr);
        outState.putString("days", daysChecked);
        outState.putInt("hr", mIntSelectedHr);
        outState.putInt("min", mIntSelectedMin);
        outState.putInt("e", firstTimeEntry);
        outState.apply();

        SharedPreferences savedInstanceState = PreferenceManager.getDefaultSharedPreferences(EditTimerActivity.this);
        sb = savedInstanceState.getString("sb", "");
        Log.e(TAG, "onSaveInstanceState:-:" + sb);
    }

    private void getPreviousData() {

        SharedPreferences savedInstanceState = PreferenceManager.getDefaultSharedPreferences(EditTimerActivity.this);
        sb = savedInstanceState.getString("sb", "");
        Log.e(TAG, "getPreviousData:-:" + sb);
        sceneNameSt = savedInstanceState.getString("sceneName", "");
        ADStr = savedInstanceState.getString("action", "0");
        daysChecked = savedInstanceState.getString("days", "");
        mIntSelectedHr = savedInstanceState.getInt("hr", 0);
        mIntSelectedMin = savedInstanceState.getInt("min", 0);
        Log.e(TAG, "Time:-:" + mIntSelectedHr + mIntSelectedMin);
        timePicker.setCurrentHour(mIntSelectedHr);
        timePicker.setCurrentMinute(mIntSelectedMin);

        if (sceneName != null)
            sceneName.setText(sceneNameSt);

        if (actionSw != null) {
            if (ADStr.equals("0")) {
                actionSw.setChecked(false);
            } else {
                actionSw.setChecked(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null) {
            type = getIntent().getStringExtra("Type");
            sb = getIntent().getStringExtra("sb");
            isFrom = getIntent().getStringExtra("IsFrom");

            Log.e(TAG, "sb:-:" + sb + " type:-:" + type);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EditTimerActivity.this);
            SharedPreferences.Editor outState = sp.edit();
            outState.putString("sb", sb);
            outState.apply();

            Log.e(TAG, "onResume sb:-:" + sb + " type:-:" + type);
            boolean isFromRepeat = getIntent().getBooleanExtra("isFromRepeat", false);

            if (isFrom.equals("2")) {
                getPreviousData();
            } else if (isFrom.equals("1")) {
                clearDataFromSp();
            }

            if (isFromRepeat) {
                daysChecked = getIntent().getStringExtra("days");
            } else {
                mMacInfoModels = new ArrayList<>();
                mMacInfoModels = (ArrayList<GetMacInfoModel>) getIntent().getSerializableExtra("TA");
            }
            int everyDayCnt = 0;
            try {
                if (daysChecked != null && daysChecked.length() > 0) {
                    char daysarr[] = daysChecked.toCharArray();
                    for (int i = 0; i < daysarr.length; i++) {
                        if (daysarr[i] == '1') {
                            everyDayCnt++;
                            setDay(i);
                        }
                    }
                    if (day.length() > 0) {
                        day = day.substring(0, day.length() - 1);
                        if (everyDayCnt > 0 && everyDayCnt == 7) {
                            String s = "Repeat - Everyday";
                            /*SpannableString ss1 = new SpannableString(s);
                            ss1.setSpan(new RelativeSizeSpan(1.1f), 0, 6, 0);*/
                            if (getTimerModelObj != null && getTimerModelObj.isRepeat()) {
                                repeat.setText(s);
                            }
                            everyDayCnt = 0;
                        } else if (everyDayCnt == 5 && (!day.contains("Sun,") && !day.contains("Sat."))) {
                            String s = "Repeat - Weekdays";
                           /* SpannableString ss1 = new SpannableString(s);
                            ss1.setSpan(new RelativeSizeSpan(1.1f), 0, 6, 0);*/
                            if (getTimerModelObj != null && getTimerModelObj.isRepeat()) {
                                repeat.setText(s);
                            }
                        } else if (day.length() > 0) {
                            String s = "Repeat - " + day;
                            /*SpannableString ss1 = new SpannableString(s);
                            ss1.setSpan(new RelativeSizeSpan(1.1f), 0, 6, 0);*/
                            if (getTimerModelObj != null && getTimerModelObj.isRepeat()) {
                                repeat.setText(s);
                            }
                        }/*else {
                            repeat.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                        }*/
                        day = "";
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        }
        if (getTimerModelObj != null) {
            sceneName.setEnabled(false);
            String serverTime = getTimerModelObj.getServerTime();

            String sub = getTimerModelObj.getServerTime().substring(serverTime.length() - 4);
            String serverHr = sub.substring(0, 2);
            String serverMin = sub.substring(sub.length() - 2);
            Log.e(TAG, "sub:-:" + sub + " serverHr:-:" + serverHr + " serverMin:-:" + serverMin);
            //Log.e(TAG, "serverHr1:-:" + serverHr1 + "serverHr2:-:" + serverHr2 +" serverMin1:-:" + serverMin1 + " serverMin2:-:" + serverMin2 + "getTimerModelObj.getExecTime():-:" + getTimerModelObj.getServerTime());

            timePicker.setCurrentHour(Integer.valueOf(serverHr));
            timePicker.setCurrentMinute(Integer.valueOf(serverMin));

            sceneName.setText(getTimerModelObj.getName());
            actionSw.setChecked(getTimerModelObj.isAction());
            sb = getTimerModelObj.getRelays();
            type = String.valueOf(getTimerModelObj.getTimertype());
            deleteBtn.setVisibility(View.VISIBLE);
        } else {
            deleteBtn.setVisibility(View.GONE);
        }
    }

    private void setDay(int i) {
        switch (i) {
            case 0:
                day += "Sun,";
                break;
            case 1:
                day += "Mon,";
                break;
            case 2:
                day += "Tue,";
                break;
            case 3:
                day += "Wed,";
                break;
            case 4:
                day += "Thu,";
                break;
            case 5:
                day += "Fri,";
                break;
            case 6:
                day += "Sat.";
                break;
        }
    }

    private void initializeViews() {
        pd = new TransparentProgressDialog(EditTimerActivity.this, R.drawable.progress);
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        appliances = (TextView) findViewById(R.id.appliances_tv);
        repeat = (TextView) findViewById(R.id.repeat_tv);
        TextView cancel = (TextView) findViewById(R.id.cancel_tv);
        TextView save = (TextView) findViewById(R.id.save_tv);
        actionSw = (Switch) findViewById(R.id.action_sw);
        sceneName = (EditText) findViewById(R.id.scene_name_et);
        deleteBtn = (TextView) findViewById(R.id.btn_delete);

        //Animation views..!
        setTimeTv = (TextView) findViewById(R.id.set_time_tv);
        labelLl = (LinearLayout) findViewById(R.id.label_ll);
        actionLl = (LinearLayout) findViewById(R.id.action_ll);
        v1 = findViewById(R.id.v);
        vt = findViewById(R.id.vt);
        v2 = findViewById(R.id.v2);
        v3 = findViewById(R.id.v3);
        v4 = findViewById(R.id.v4);
        v5 = findViewById(R.id.v5);

        setAnimation();


        mIntSelectedHr = timePicker.getCurrentHour();
        mIntSelectedMin = timePicker.getCurrentMinute();

        isActionSwTouched = false;
        actionSw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isActionSwTouched = true;
                return false;
            }
        });

        actionSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    ADStr = "1";
                    if (isActionSwTouched) {
                        showToast("ON");
                        if (getTimerModelObj != null) {
                            getTimerModelObj.setAction(true);
                        }
                    }
                } else {
                    ADStr = "0";
                    if (isActionSwTouched) {
                        showToast("OFF");
                        if (getTimerModelObj != null) {
                            getTimerModelObj.setAction(false);
                        }
                    }
                }
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mIntSelectedHr = hourOfDay;
                mIntSelectedMin = minute;
               /* SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EditTimerActivity.this);
                SharedPreferences.Editor outState = sp.edit();
                outState.putInt("hr", mIntSelectedHr);
                outState.putInt("min", mIntSelectedMin);
                outState.apply();*/
            }
        });
    }

    private void setAnimation() {
        //Setting Animation..!
        Animation animationLeft = AnimationUtils.loadAnimation(EditTimerActivity.this, R.anim.left_to_right);
        Animation animationRight = AnimationUtils.loadAnimation(EditTimerActivity.this, R.anim.right_to_left);
        Animation animationTop = AnimationUtils.loadAnimation(EditTimerActivity.this, R.anim.top_to_bottom);
        Animation animationBottom = AnimationUtils.loadAnimation(EditTimerActivity.this, R.anim.bottom_to_top);


        RelativeLayout topRl = (RelativeLayout) findViewById(R.id.header_rl);
        topRl.startAnimation(animationTop);
        if (deleteBtn.getVisibility() == View.VISIBLE) {
            deleteBtn.startAnimation(animationBottom);
        }
        setTimeTv.startAnimation(animationLeft);
        timePicker.startAnimation(animationLeft);
        labelLl.startAnimation(animationRight);
        actionLl.startAnimation(animationLeft);
        repeat.startAnimation(animationRight);
        appliances.startAnimation(animationLeft);
        v1.startAnimation(animationTop);
        vt.startAnimation(animationLeft);
        v2.startAnimation(animationRight);
        v3.startAnimation(animationLeft);
        v4.startAnimation(animationRight);
        v5.startAnimation(animationLeft);
    }

    private void showToast(String msg) {

        // Set the toast and duration
        int toastDurationInMilliSeconds = 500;
        mToastToShow = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToastToShow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.END, 180, 240);

        // Set the countdown to display the toast
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
                mToastToShow.show();
            }

            public void onFinish() {
                mToastToShow.cancel();
            }
        };

        // Show the toast and starts the countdown
        mToastToShow.show();
        toastCountDown.start();
    }

    private void setToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Edit scene_home");
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

    public void timerClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                onBackPressed();
                break;
            case R.id.appliances_tv:
                if (getTimerModelObj != null) {
                    getTimerModelObj.setServerTime(currentDate());
                }
                startActivity(new Intent(EditTimerActivity.this, TimerApplianceActivity.class).putExtra("TA", mMacInfoModels));
                break;
            case R.id.save_tv:
                sceneNameSt = sceneName.getText().toString().trim();
                sceneNameSt = sceneNameSt.replace(" ", "~");
                if (!TextUtils.isEmpty(sceneNameSt) && sceneNameSt.length() > 0) {

                    if (type != null && type.length() > 0) {
                        char[] chars = daysChecked.toCharArray();
                        int zerosCnt = 0;

                        for (char aChar : chars) {
                            if (aChar == '0') {
                                zerosCnt++;
                            }
                        }

                        if (zerosCnt == 7) {
                            repeatStr = "0";
                            checkValidTimeBasedOnRepeat(sceneNameSt);
                        } else {

                            if (getTimerModelObj != null) {
                                if (getTimerModelObj.isRepeat())
                                    repeatStr = "1";
                                else
                                    repeatStr = "0";
                            } else {
                                repeatStr = "1";
                            }
                            saveTimer(sceneNameSt);
                        }
                    } else {
                        Utils.showMessageDialog("Please select appliances", EditTimerActivity.this);
                    }
                 /*   if (TextUtils.isEmpty(daysChecked) && daysChecked.length() == 0) {

                        checkValidTimeBasedOnRepeat(sceneNameSt);
                    } else {

                    }*/
                } else {
                    sceneName.setError("Required!");
                }
                break;
            case R.id.repeat_tv:
                if (getTimerModelObj != null) {
                    getTimerModelObj.setServerTime(currentDate());
                }
                startActivity(new Intent(EditTimerActivity.this, TimerRepeatModeActivity.class));
                break;
            case R.id.btn_delete:
                showConfirmationDialogDelete();
                break;
        }
    }

    private void showConfirmationDialogDelete() {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(EditTimerActivity.this);
        alertBox.setMessage("Are you sure you want to delete?");
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    deleteTimer(String.valueOf(getTimerModelObj.getId()));
                } else {
                    Utils.showMessageDialog("No Internet", EditTimerActivity.this);
                }
            }
        });
        alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertBox.show();

    }

    private void deleteTimer(String timerId) {
        pd.show();
        //http://atghas.com/OneControlService/OCService.svc/DeleteTimer?MacId=20f85eeee93e&IMEI=352087074323842&TimerId=199.
        String url = ServiceHandler.baseUrl + "DeleteTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerId=" + timerId;
        Log.e(TAG, "SetReceiversStatusExt:-:" + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            int code = 0;
            String message = "";

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response1:-:" + response);
                //{"DeleteTimerResult":[{"Code":200,"Message":"Successfully deleted"}]}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("DeleteTimerResult");
                    int code = 0;
                    String Msg = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        code = jsonObject1.getInt("Code");
                        Msg = jsonObject1.getString("Message");
                    }

                    sendConfirmationDialogDelete(code);
                } catch (JSONException e) {
                    pd.dismiss();
                    Log.e(TAG, "JSONException:-:" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse1:-:" + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void sendConfirmationDialogDelete(final int code) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(EditTimerActivity.this);
        alertBox.setTitle("OneControl");
        pd.dismiss();
        if (code == 200) {
            alertBox.setMessage("Done");
        } else {
            alertBox.setMessage("Timer Deletion failed, Please try again.");
        }
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code == 200) {
                    if (Utils.isNetworkAvailable) {
                        Intent intent = new Intent(EditTimerActivity.this, MainActivity.class);
                        intent.putExtra("From", "CreateTimer");
                        startActivity(intent);
                    } else {
                        Utils.showMessageDialog("No Internet.", EditTimerActivity.this);
                    }
                }
            }
        });
        alertBox.show();

    }

    private void checkValidTimeBasedOnRepeat(String sceneNameSt) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);

        if (mIntSelectedHr < hour) {

            Utils.showMessageDialog("Can't select previous time", EditTimerActivity.this);
            return;
        }

        if (mIntSelectedHr > hour) {
            saveTimer(sceneNameSt);
        } else if (mIntSelectedHr == hour) {
            if (mIntSelectedMin <= minutes) {
                Utils.showMessageDialog("Selected minute should be grater than current minute", EditTimerActivity.this);
                return;
            }
            saveTimer(sceneNameSt);
        }

    }/*else*/


    private void saveTimer(String sceneNameSt) {
        pd.show();
        String finalUrl = "";
        //repeatStr = "1";

        if (getTimerModelObj != null) {
            sceneNameSt = getTimerModelObj.getServerName();
        }

        if (sb.endsWith(",")) {
            sb = sb.replaceFirst(".$", "");
        }
        switch (type) {
            case "1":
                finalUrl = ServiceHandler.baseUrl + "SetTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=1&Value=" + sceneNameSt
                        + "-1" + "-" + currentDate() + "-" + repeatStr + "-" + daysChecked + "-" + ADStr;
                break;
            case "2":
                finalUrl = ServiceHandler.baseUrl + "SetTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=2&Value=" + sceneNameSt
                        + "-" + sb + "-1-" + currentDate() + "-" + repeatStr + "-" + daysChecked + "-" + ADStr;

                break;
            case "3":
                finalUrl = ServiceHandler.baseUrl + "SetTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=3&Value=" + sceneNameSt
                        + "-" + sb + "-1-" + currentDate() + "-" + repeatStr + "-" + daysChecked + "-" + ADStr;

                break;
        }

        Log.e(TAG, "finalUrl:-:" + finalUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            private String Msg = "";
            private int code = 0;

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                //{"SetTimer":[{"Code":200,"Message":"Successfully added"}]}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("SetTimer");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        code = jsonObject1.getInt("Code");
                        Msg = jsonObject1.getString("Message");
                    }

                    try {
                        showConfirmationDialogExt(Msg, code);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception:-:" + e.getMessage());
                    }
                } catch (JSONException e) {
                    pd.dismiss();
                    Log.e(TAG, "JSONException:-:" + e.getMessage());
                }

                pd.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRClientRequest()", "Error : " + error.getMessage());
                pd.dismiss();
            }
        });
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    public void showConfirmationDialogExt(final String message, final int code) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(EditTimerActivity.this);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code == 200) {
                    Intent intent = new Intent(EditTimerActivity.this, MainActivity.class);
                    intent.putExtra("From", "CreateTimer");
                    startActivity(intent);
                }
            }
        });
        alertBox.show();
    }

    String currentDate() {

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int year1 = c.get(Calendar.YEAR);
        int month1 = c.get(Calendar.MONTH) + 1;
        int day1 = c.get(Calendar.DAY_OF_MONTH);

        String mon = String.valueOf(month1);//String.valueOf(monthNumber + 1);
        String hr24 = "" + mIntSelectedHr;

        if (hr24.length() < 2)
            hr24 = "0" + hr24;

        String min24 = "" + mIntSelectedMin;

        if (min24.length() < 2)
            min24 = "0" + min24;

        if (mon.length() < 2)
            mon = "0" + mon;

        String day24 = String.valueOf(day1);

        Log.e(TAG, "day24" + day24);

        if (day24.length() < 2)
            day24 = "0" + day24;

        String dateTimeSelectedStr = year1 + "" + mon + day24 + hr24 + min24;
        return dateTimeSelectedStr;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearDataFromSp();
        /*Intent intent = new Intent(EditTimerActivity.this, MainActivity.class);
        intent.putExtra("From", "CreateTimer");
        startActivity(intent);*/
    }

    private void clearDataFromSp() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EditTimerActivity.this);
        SharedPreferences.Editor outState = sp.edit();
        outState.putString("sb", "");
        outState.putString("sceneName", "");
        outState.putString("action", "");
        outState.putString("days", "");
        outState.putInt("hr", 0);
        outState.putInt("min", 0);
        outState.apply();
        // firstTimeEntry=sp.getInt("e",0);
    }
}
