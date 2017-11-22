package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.atg.onecontrolv3.helpers.Utils.getEmojiByUnicode;
import static com.atg.onecontrolv3.helpers.Utils.getPIRSObj;


public class EditPIRActivity extends BaseActivity implements /*NumberPicker.OnValueChangeListener,*/ MqttHelper.responseListener {

    private static final String TAG = EditPIRActivity.class.getSimpleName();
    EditText mPIRName;
    TextView mApplianceTv;
    NumberPicker mNpHour, mNpMinute;
    //int mIntSelectedHr, mIntSelectedMin;
    MqttHelper.responseListener mqttListener = null;
    ArrayList<Integer> pirNumbersArrLst = new ArrayList<>();
    private String[] mNpHoursArr, mNpMinutesArr;
    private MqttHelper helper;
    //private ArrayList<PIRDBModel> pirdbModelObj = new ArrayList<>();
    // private PIRDBModel pirdbModelObj;
    private ArrayList<Integer> pirNoArrLst = new ArrayList<>();
    private int pirMqttNo = 1;
    private String minutesStr;
    private String pirNameStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pir);
        initializeViews();
    }

    private void initializeViews() {

        //pirdbModelObj = (ArrayList<PIRDBModel>) getIntent().getSerializableExtra("pirDashboardArrLst");
        //pirdbModelObj = (PIRDBModel) getIntent().getSerializableExtra("pirDashboardArrLst");

        /*if (pirdbModelObj != null && pirdbModelObj.size() > 0) {
            for (int i = 0; i < pirdbModelObj.size(); i++) {
                pirNoArrLst.add(pirdbModelObj.get(i).getPirNumber());
            }
        }*/

        mPIRName = (EditText) findViewById(R.id.pir_name_tv);
        mApplianceTv = (TextView) findViewById(R.id.appliances_tv);
        mNpHour = (NumberPicker) findViewById(R.id.np_hours);
        mNpMinute = (NumberPicker) findViewById(R.id.np_minutes);

        mqttListener = this;
        if (Utils.isNetworkAvailable) {
            helper = new MqttHelper(this, mqttListener);
        }


        if (getPIRSObj != null) {
            try {
                mPIRName.setText(getPIRSObj.getPirName().replace("~", " ").trim());
                pirNoArrLst.add(getPIRSObj.getPirNumber());
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        }

        setToolBar();

        mNpHoursArr = getResources().getStringArray(R.array.array_pir_timer_hours);
        mNpMinutesArr = getResources().getStringArray(R.array.array_pir_timer_minutes);

        numberPickerHandler();

        try {
            if (getIntent() != null) {
                pirNumbersArrLst = (ArrayList<Integer>) getIntent().getSerializableExtra("PIRNbrArrLst");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    private void setToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Edit PIR");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void numberPickerHandler() {
        Log.e(TAG, "mNpHoursArr:-:" + Arrays.toString(mNpHoursArr));

        mNpHour.setMinValue(0);
        mNpHour.setMaxValue(8);
        mNpMinute.setMinValue(1);
        mNpMinute.setMaxValue(59);

        if (getPIRSObj != null) {
            int hrs = (getPIRSObj.getTimerTime() / 60);
            int mins = (getPIRSObj.getTimerTime() % 60);

            if (getPIRSObj.getTimerTime() == 0) {
                mNpHour.setValue(0);
                mNpMinute.setValue(1);
            } else {
                mNpHour.setValue(hrs);
                mNpMinute.setValue(mins);
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        switch (numberPicker.getId()) {
            case R.id.np_hours:
                mIntSelectedHr = (newValue * 60);
                break;
            case R.id.np_minutes:
                mIntSelectedMin = newValue;
                break;
        }
    }*/

    public void pirEditClick(View view) {

        /*if (Utils.getPIRSObj != null) {
            for (int i = 1; i < 9; i++) {
                if (!pirNoArrLst.contains(i)) {
                    pirMqttNo = i;
                    break;
                }
            }
        }*/

        switch (view.getId()) {
            case R.id.appliances_tv:
                Intent intent = new Intent(this, PIRApplianceActivity.class);
                intent.putExtra("PIRNbrArrLst", pirNumbersArrLst);
                startActivity(intent);
                break;
            case R.id.cancel_tv:
                onBackPressed();
                break;
            case R.id.save_tv:
                /*if (pirNumbersArrLst.size() > 0) {
                    for (int i = 1; i < 9; i++) {
                        if (!pirNumbersArrLst.contains(i)) {
                            pirMqttNo = i;
                            break;
                        }
                    }
                }else {
                    Log.e(TAG, "pirNumbersArrLst empty");
                }*/

                if (getPIRSObj != null) {
                    pirMqttNo = getPIRSObj.getPirNumber();
                }

                //String minutesStr = String.valueOf((mIntSelectedHr + mIntSelectedMin));
                int npHr = (mNpHour.getValue() * 60);
                int npMin = mNpMinute.getValue();
                minutesStr = String.valueOf((npHr + npMin));
                pirNameStr = mPIRName.getText().toString().replaceAll(" ", "~").trim();
                Log.e(TAG, "minutesStr:-:" + minutesStr + "pirNameStr:-:" + pirNameStr + "pirMqttNo:-:" + pirMqttNo);
                if (minutesStr != null && minutesStr.length() > 0) {
                    if (pirNameStr != null && pirNameStr.length() > 0) {
                        if (Utils.isNetworkAvailable) {
                            helper.sendMsg("F|" + pirMqttNo + "|" + minutesStr);
                        }
                    } else {
                        mPIRName.setError("Please give PIR name");
                    }
                } else {
                    Utils.showMessageDialog("Please select time", this);
                }
                break;
        }
    }

    @Override
    public void onMqttResponse(String res) {
        Log.e(TAG, "mqttResStr:-:" + res);
        if (res.contains("Notification")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "47":
                    if (Utils.isNetworkAvailable) {
                        //Utils.showMessageDialog("Done " + getEmojiByUnicode(0x1F60A), this);
                        showSuccessFailDialog();
                    }
                    break;
            }
        }
    }

    void showSuccessFailDialog() {

        final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
        alertBox.setMessage("Done " + getEmojiByUnicode(0x1F60A));
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    sendExtVolley();
                } else {
                    Utils.showMessageDialog("No Internet", EditPIRActivity.this);
                }
                Intent intent = new Intent(EditPIRActivity.this, PIRDashboardActivity.class);
                startActivity(intent);
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

    private void sendExtVolley() {
        String strMethodName = "SetTimeForPIR";
        //http://atghas.com/OneControlService/OCService.svc/SetTimeForPIR?MacId=20f85eeee93e&IMEI=865374023900866&PIR=1&Timer=1
        String strUrl = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI +
                "&PIR=" + pirMqttNo + "&Timer=" + minutesStr + "&PIRName=" + pirNameStr;
        Log.e(TAG, "SetTimeForPIRURL:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "SetTimeForPIRRes:-:" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "SetTimeForPIRErrorVolley:-:" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, PIRDashboardActivity.class);
        startActivity(intent);
    }
}
