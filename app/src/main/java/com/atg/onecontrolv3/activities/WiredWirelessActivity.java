package com.atg.onecontrolv3.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.regex.Pattern;

public class WiredWirelessActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, View.OnTouchListener, MqttHelper.responseListener {

    private static final String TAG = WiredWirelessActivity.class.getSimpleName();
    TextView mWiredTv, mWirelessTv;
    LinearLayout mSensorTypeLl, mSensorTypeLl2;
    View mView;
    Spinner mPortsSpinner, mSensorTypeSpinner, mSensorTypeSpinner2;
    EditText mLocationEt, mZoneEt, mLocationEt2, mZoneEt2;
    MqttHelper.responseListener mqttListener = null;
    private boolean isSpinnerTouched1, isSpinnerTouched2, isSpinnerTouched3;
    private int mPortInt;
    private int mSensorTypeWiredInt;
    private int mSensorTypeWirelessInt;
    private MqttHelper helper;
    private String locationStr, zoneStr, locationStr2, zoneStr2;
    private int mFromWiredWireless = 0;
    private boolean isBtnClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiredn_wireless);
        mqttListener = this;
        if (Utils.isNetworkAvailable) {
            helper = new MqttHelper(WiredWirelessActivity.this, mqttListener);
        }
        initializeView();
        setToolBar();
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Configure Sensors");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
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

    private void initializeView() {
        //Wired..!
        mWiredTv = (TextView) findViewById(R.id.wired_tv);
        mLocationEt = (EditText) findViewById(R.id.location_et);
        mZoneEt = (EditText) findViewById(R.id.zone_et);
        mSensorTypeLl = (LinearLayout) findViewById(R.id.select_sensor_type_ll);
        mPortsSpinner = (Spinner) findViewById(R.id.port_spinner);
        mSensorTypeSpinner = (Spinner) findViewById(R.id.type_spinner);

        //Wireless..!
        mWirelessTv = (TextView) findViewById(R.id.wireless_tv);
        mLocationEt2 = (EditText) findViewById(R.id.location_et2);
        mZoneEt2 = (EditText) findViewById(R.id.zone_et2);
        mSensorTypeLl2 = (LinearLayout) findViewById(R.id.select_sensor_type_ll2);
        mView = findViewById(R.id.view);
        mSensorTypeSpinner2 = (Spinner) findViewById(R.id.type_spinner2);

        //Setting Data to Spinners..!
        ArrayAdapter<CharSequence> portsAdapter = ArrayAdapter.createFromResource(this, R.array.ports, android.R.layout.simple_spinner_dropdown_item);
        mPortsSpinner.setAdapter(portsAdapter);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.sensor_type, android.R.layout.simple_spinner_dropdown_item);
        mSensorTypeSpinner.setAdapter(typeAdapter);
        mSensorTypeSpinner2.setAdapter(typeAdapter);

        //To Check whether spinner is touched/selected..!
        mPortsSpinner.setOnTouchListener(this);
        mSensorTypeSpinner.setOnTouchListener(this);
        mSensorTypeSpinner2.setOnTouchListener(this);

        //Enabling Spinner Selection..!
        mPortsSpinner.setOnItemSelectedListener(this);
        mSensorTypeSpinner.setOnItemSelectedListener(this);
        mSensorTypeSpinner2.setOnItemSelectedListener(this);
    }

    public void wireClick(View view) {
        switch (view.getId()) {
            case R.id.wired_tv:
                mFromWiredWireless = 1;
                if (mSensorTypeLl.getVisibility() == View.VISIBLE) {
                    mSensorTypeLl.setVisibility(View.GONE);
                    mWirelessTv.setVisibility(View.VISIBLE);
                    mView.setVisibility(View.VISIBLE);
                } else {
                    mSensorTypeLl.setVisibility(View.VISIBLE);
                    mWirelessTv.setVisibility(View.GONE);
                    mView.setVisibility(View.GONE);
                }
                break;
            case R.id.wireless_tv:
                mFromWiredWireless = 2;
                if (mSensorTypeLl2.getVisibility() == View.VISIBLE) {
                    mSensorTypeLl2.setVisibility(View.GONE);
                    mWiredTv.setVisibility(View.VISIBLE);
                    mView.setVisibility(View.VISIBLE);
                } else {
                    mSensorTypeLl2.setVisibility(View.VISIBLE);
                    mWiredTv.setVisibility(View.GONE);
                    mView.setVisibility(View.GONE);
                }
                break;
        }
    }

    //btn click event..!
    public void pairClick(View view) {
        switch (view.getId()) {
            case R.id.wired_pair_btn:
                locationStr = mLocationEt.getText().toString();
                zoneStr = mZoneEt.getText().toString();
                if (null != locationStr && !TextUtils.isEmpty(locationStr)) {
                    if (null != zoneStr && !TextUtils.isEmpty(zoneStr)) {
                        if (mPortInt > 0) {
                            if (mSensorTypeWiredInt > 0) {
                                if (Utils.isNetworkAvailable) {
                                    isBtnClicked = true;
                                    helper.sendMsg("a|0|" + mSensorTypeWiredInt + "|" + mPortInt + "|1");
                                }
                            } else {
                                Toast.makeText(this, "Please select sensor type", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Please select port number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mZoneEt.setError("Required!");
                    }
                } else {
                    mLocationEt.setError("Required!");
                }
                break;
            case R.id.wireless_pair_btn:
                locationStr2 = mLocationEt2.getText().toString();
                zoneStr2 = mZoneEt2.getText().toString();
                if (null != locationStr2 && !TextUtils.isEmpty(locationStr2)) {
                    if (null != zoneStr2 && !TextUtils.isEmpty(zoneStr2)) {
                        if (mSensorTypeWirelessInt > 0) {
                            if (Utils.isNetworkAvailable) {
                                isBtnClicked = true;
                                helper.sendMsg("a|0|" + mSensorTypeWirelessInt + "|1" + "|0");
                            }
                        } else {
                            Toast.makeText(this, "Please select sensor type", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mZoneEt2.setError("Required!");
                    }
                } else {
                    mLocationEt2.setError("Required!");
                }
                break;
        }
    }

    //Spinner Selection changing event..!
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.port_spinner:
                if (isSpinnerTouched1) {
                    if (i > 0) {
                        mPortInt = i;
                    }
                }
                break;
            case R.id.type_spinner:
                if (isSpinnerTouched2) {
                    if (i > 0) {
                        mSensorTypeWiredInt = i;
                    }
                }
                break;
            case R.id.type_spinner2:
                if (isSpinnerTouched3) {
                    if (i > 0) {
                        mSensorTypeWirelessInt = i;
                        Log.e(TAG, "Test mSensorTypeWirelessInt:-:" + mSensorTypeWirelessInt);
                    }
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.port_spinner:
                isSpinnerTouched1 = true;
                break;
            case R.id.type_spinner:
                isSpinnerTouched2 = true;
                break;
            case R.id.type_spinner2:
                isSpinnerTouched3 = true;
                break;
        }
        return false;
    }

    @Override
    public void onMqttResponse(String res) {
        Log.e(TAG, "onMqttResponseStr:-:" + res);
        //Port:3
        if (res.contains("Port")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            Log.e(TAG, "notiStrArr:-:" + Arrays.toString(notiStrArr));
            if (isBtnClicked) {
                isBtnClicked = false;
                sendVolleyRequest(notiStrArr[1]);
            }
            //Notification:32
        } else if (res.contains("Notification")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "32":
                    Utils.showMessageDialog("Pairing failed", WiredWirelessActivity.this);
                    break;
            }
        }
    }

    private void sendVolleyRequest(String portNo) {
        /*?Id=0&SType=1
&MacId=20f85eeee93e&IMEI=321321321321321&Location=IT2&Zone=Left&PortNumber=1
&IsWired=1&TypeofCall=1*/
        int sType = 0, portNbr = 0;
        String location = "", zone = "", isWired = "";
        String strMethodName = "SetSensorCRUD";
        switch (mFromWiredWireless) {
            case 1:
                sType = mSensorTypeWiredInt;
                portNbr = Integer.parseInt(portNo);
                location = locationStr;
                zone = zoneStr;
                isWired = "1";
                break;
            case 2:
                sType = mSensorTypeWirelessInt;
                portNbr = Integer.parseInt(portNo);
                location = locationStr2;
                zone = zoneStr2;
                isWired = "0";
                break;
        }
        location = location.replaceAll(" ", "~");
        zone = zone.replaceAll(" ", "~");
        if (sType != 0 && null != location && location.length() > 0 && null != zone && zone.length() > 0 && portNbr != 0 && isWired.length() > 0) {
            String strUrl = ServiceHandler.baseUrl + strMethodName + "?Id=0&SType=" + sType + "&MacId=" + Utils.MAC_ID +
                    "&IMEI=" + Utils.IMEI + "&Location=" + location + "&Zone=" + zone + "&PortNumber=" + portNbr
                    + "&IsWired=" + isWired + "&TypeofCall=1";
            Log.e(TAG, "strUrl:-:" + strUrl);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e(TAG, "response:-:" + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("SetSensorCRUDResult");
                        int code = jsonObject1.getInt("Code");
                        if (code == 200) {
                            Utils.showMessageDialog("Sensor Added Successfully", WiredWirelessActivity.this);
                            mSensorTypeLl.setVisibility(View.GONE);
                            mSensorTypeLl2.setVisibility(View.GONE);
                            mWiredTv.setVisibility(View.VISIBLE);
                            mWirelessTv.setVisibility(View.VISIBLE);
                            mView.setVisibility(View.VISIBLE);
                        } else {
                            Utils.showMessageDialog("Sensor adding failed", WiredWirelessActivity.this);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSONException:-" + e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "VolleyError:-:" + error.getMessage());
                }
            });
            AppController.getInstance().addToRequestQueue(stringRequest);

        } else {
            Log.e(TAG, "From other Mobile");
        }
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }
}
