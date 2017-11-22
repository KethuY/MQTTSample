package com.atg.onecontrolv3.irblasters;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.Database.DatabaseHelperForAC;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.activities.BaseActivity;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.IRAppliancesModel;
import com.atg.onecontrolv3.models.IRSendKeysModel;
import com.atg.onecontrolv3.preferances.MyPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

;

public class IRBlasterACLearnActivity extends BaseActivity {

    private static final String TAG = "ACLearnActivity";
    private static final String on = "o", off = "f", fan1 = "1", fan2 = "2", fan3 = "3", fanAuto = "a", swingLR = "l",
            swingUD = "t",
            cool = "c", hot = "h", dry = "y", dFan = "d";
    Button mOnBtn, mOffBtn, mCoolBtn, mHotBtn, mDryBtn, mFanBtn, /*mSwDownBtn, mSwLeftBtn,*/
            mF1Btn, mF2Btn, mF3Btn, mFAutoBtn;
    LinearLayout mSwUpLl, mSwRightLl;
    TextView tvDefaultTemp, tvImportSync, mswingUpTv, mswingDownTv, mswingLeftTv, mswingRightTv;
    DatabaseHelperForAC dbHelper;
    IRAppliancesModel model;
    String res = null;
    boolean isValueStored;
    char degreeSymbol = (char) 0X00B0;
    String uuidFromDB = "";
    private String keyName = "";
    private String zmoteId = "";
    private String localIP = "";
    private String roomId = "";
    private String id = "";
    private String secret = "";
    private int appliancePos = 0;
    private String applianceType = "";
    private ArrayList<IRSendKeysModel> irImportSyncModelArrLst;
    private ArrayList<String> irKeysArrLst;
    private int colorPrimary;
    private TextView mSwingName, mSwingName2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster_aclearn);
        setToolBar();
        initializeViews();
        colorPrimary = Utils.getColorPrimary(this);

    }


    private void initializeViews() {
        mOnBtn = (Button) findViewById(R.id.btnACOn);
        mOffBtn = (Button) findViewById(R.id.btnACOff);
        mCoolBtn = (Button) findViewById(R.id.btnCool);
        mHotBtn = (Button) findViewById(R.id.btnHot);
        mDryBtn = (Button) findViewById(R.id.btnDry);
        mFanBtn = (Button) findViewById(R.id.btnFan);
        mSwUpLl = (LinearLayout) findViewById(R.id.llSU);
        /*mSwDownBtn = (Button) findViewById(R.id.btnSD);
        mSwLeftBtn = (Button) findViewById(R.id.btnSL);*/
        mSwRightLl = (LinearLayout) findViewById(R.id.llSR);
        mF1Btn = (Button) findViewById(R.id.btnF1);
        mF2Btn = (Button) findViewById(R.id.btnF2);
        mF3Btn = (Button) findViewById(R.id.btnF3);
        mFAutoBtn = (Button) findViewById(R.id.btnAuto);
        mSwingName = (TextView) findViewById(R.id.swing_name);
        mSwingName2 = (TextView) findViewById(R.id.swing_name_2);

        tvDefaultTemp = (TextView) findViewById(R.id.tvDefaultTemp);
        tvImportSync = (TextView) findViewById(R.id.tvImportSync);
        mswingUpTv = (TextView) findViewById(R.id.swing_up_ac_tv);
        mswingDownTv = (TextView) findViewById(R.id.swing_down_ac_tv);
        mswingLeftTv = (TextView) findViewById(R.id.swing_left_ac_tv);
        mswingRightTv = (TextView) findViewById(R.id.swing_right_ac_tv);

        Typeface tf = Typeface.createFromAsset(getAssets(), "ir_devices_font.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(), "untitled_font_9.ttf");
        Typeface tf2 = Typeface.createFromAsset(getAssets(), "untitled-font-3.ttf");

        mOnBtn.setTypeface(tf);
        mOffBtn.setTypeface(tf);

        mswingUpTv.setTypeface(tf1);
        mswingDownTv.setTypeface(tf1);
        mswingLeftTv.setTypeface(tf1);
        mswingRightTv.setTypeface(tf1);

        mCoolBtn.setTypeface(tf2);
        mHotBtn.setTypeface(tf2);
        mDryBtn.setTypeface(tf2);
        mFanBtn.setTypeface(tf2);

        dbHelper = new DatabaseHelperForAC(this);

        roomId = getIntent().getStringExtra("ROOM_ID");
        appliancePos = getIntent().getIntExtra("APPLIANCE_POS", 0);
        applianceType = getIntent().getStringExtra("APPLIANCE_TYPE");
        localIP = getIntent().getStringExtra("CHIP_IP");
        zmoteId = getIntent().getStringExtra("CHIP_NAME");
        id = getIntent().getStringExtra("ID");
        secret = getIntent().getStringExtra("SECRET_ID");
        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");

        model = new IRAppliancesModel();
        model.setAppliance_no(appliancePos + "");
        model.setAppliance_type(applianceType);
        model.setRoomId(roomId);

    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AC Learn");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setButtonStatusInitially();
    }

    private void setButtonStatusInitially() {
        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = new ArrayList<>();
            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
        }

        if (irImportSyncModelArrLst != null && irImportSyncModelArrLst.size() > 0) {
            irKeysArrLst = new ArrayList<>();
            for (int i = 0; i < irImportSyncModelArrLst.size(); i++) {
                irKeysArrLst.add(irImportSyncModelArrLst.get(i).getKeyName());
            }
        }

        if (irKeysArrLst != null && irKeysArrLst.size() > 0) {

            setStatusToButtons(irKeysArrLst.contains(on), mOnBtn);
            setStatusToButtons(irKeysArrLst.contains(off), mOffBtn);
            setStatusToButtons(irKeysArrLst.contains(fan1), mF1Btn);
            setStatusToButtons(irKeysArrLst.contains(fan2), mF2Btn);
            setStatusToButtons(irKeysArrLst.contains(fan3), mF3Btn);
            setStatusToButtons(irKeysArrLst.contains(fanAuto), mFAutoBtn);
            setStatusToButtons(irKeysArrLst.contains("c18"), mCoolBtn);
            setStatusToButtons(irKeysArrLst.contains("h18"), mHotBtn);
            setStatusToButtons(irKeysArrLst.contains("y18"), mDryBtn);
            setStatusToButtons(irKeysArrLst.contains("d18"), mFanBtn);
            setStatusToLinearLayoutLR(irKeysArrLst.contains(swingLR), mswingLeftTv, mswingRightTv);
            setStatusToLinearLayoutUD(irKeysArrLst.contains(swingUD), mswingUpTv, mswingDownTv);

        }
    }


    /*private void setButtonStatusInitially() {
        byte[] keyValueInBytesForPowerOn = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_ON", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForPowerOff = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_OFF", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForCool = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_Temp_18D_COOL", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForHot = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_Temp_18D_HOT", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForDry = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_Temp_18D_DRY", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForFan = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_Temp_18D_FAN", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForF1 = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_F1", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForF2 = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_F2", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForF3 = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_F3", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForAuto = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_AUTO", roomId, appliancePos + "", applianceType);
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------!
        byte[] keyValueInBytesForSud = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_SUD", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForSlr = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "AC_SLR", roomId, appliancePos + "", applianceType);

        boolean isValueExistsForPowerOn = keyValueInBytesForPowerOn != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_ON", true) : false;
        boolean isValueExistsForPowerOff = keyValueInBytesForPowerOff != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_OFF", true) : false;
        boolean isValueExistsForCool = keyValueInBytesForCool != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_Temp_18D_COOL", true) : false;
        boolean isValueExistsForHot = keyValueInBytesForHot != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_Temp_18D_HOT", true) : false;
        boolean isValueExistsForDry = keyValueInBytesForDry != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_Temp_18D_DRY", true) : false;
        boolean isValueExistsForFan = keyValueInBytesForFan != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_Temp_18D_FAN", true) : false;
        boolean isValueExistsForF1 = keyValueInBytesForF1 != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_F1", true) : false;
        boolean isValueExistsForF2 = keyValueInBytesForF2 != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_F2", true) : false;
        boolean isValueExistsForF3 = keyValueInBytesForF3 != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_F3", true) : false;
        boolean isValueExistsForAuto = keyValueInBytesForAuto != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_AUTO", true) : false;
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------!
        boolean isValueExistsForSud = keyValueInBytesForSud != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_SUD", true) : false;
        boolean isValueExistsForSlr = keyValueInBytesForSlr != null ? dbHelper.isKeyValueExists(uuidFromDB, "AC_SLR", true) : false;

        setStatusToButtons(isValueExistsForPowerOn, mOnBtn);
        setStatusToButtons(isValueExistsForPowerOff, mOffBtn);
        setStatusToButtons(isValueExistsForCool, mCoolBtn);
        setStatusToButtons(isValueExistsForHot, mHotBtn);
        setStatusToButtons(isValueExistsForDry, mDryBtn);
        setStatusToButtons(isValueExistsForFan, mFanBtn);
        setStatusToButtons(isValueExistsForF1, mF1Btn);
        setStatusToButtons(isValueExistsForF2, mF2Btn);
        setStatusToButtons(isValueExistsForF3, mF3Btn);
        setStatusToButtons(isValueExistsForAuto, mFAutoBtn);

        setStatusToLinearLayoutUD(isValueExistsForSud, mswingUpTv, mswingDownTv);
        setStatusToLinearLayoutLR(isValueExistsForSlr, mswingLeftTv, mswingRightTv);
    }*/

    private void setStatusToButtons(boolean isValueExists, Button btn) {
        if (isValueExists) {
            btn.setTextColor(colorPrimary);
            btn.setBackground(ContextCompat.getDrawable(this, R.drawable.oc_appliance_count_bg_white2));
        } else {

            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setStatusToLinearLayoutUD(boolean isValueExists, TextView mswingUpTv, TextView mswingDownTv) {
        if (isValueExists) {
            mswingUpTv.setTextColor(colorPrimary);
            mswingDownTv.setTextColor(colorPrimary);
            mSwUpLl.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangle_et_style_ovel_white1));
            mSwingName.setTextColor(colorPrimary);
        } else {
            mSwingName.setTextColor(ContextCompat.getColor(this, R.color.white));
            mswingUpTv.setTextColor(ContextCompat.getColor(this, R.color.white));
            mSwUpLl.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangle_et_style_ovel_white));
            mswingDownTv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setStatusToLinearLayoutLR(boolean isValueExists, TextView mswingLeftTv, TextView mswingRightTv) {
        if (isValueExists) {
            mswingLeftTv.setTextColor(colorPrimary);
            mswingRightTv.setTextColor(colorPrimary);
            mSwingName2.setTextColor(colorPrimary);
            mSwRightLl.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangle_et_style_ovel_white1));
        } else {
            mSwingName2.setTextColor(ContextCompat.getColor(this, R.color.white));
            mswingLeftTv.setTextColor(ContextCompat.getColor(this, R.color.white));
            mswingRightTv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    public void onAcLearn(View view) {
        switch (view.getId()) {
            case R.id.btnACOn:
                keyName = on;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.btnACOff:
                keyName = off;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.btnCool:
                showDialogToLearnTemp(cool, false);
                break;
            case R.id.btnHot:
                showDialogToLearnTemp(hot, false);
                break;
            case R.id.btnDry:
                showDialogToLearnTemp(dry, false);
                break;
            case R.id.btnFan:
                showDialogToLearnTemp(dFan, false);
                break;
            case R.id.llSU:
                keyName = swingUD;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            /*case R.id.btnSD:
                keyName = "AC_SD";
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;*/
            /*case R.id.btnSL:
                keyName = "AC_SL";
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;*/
            case R.id.llSR:
                keyName = swingLR;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.btnF1:
                keyName = fan1;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.btnF2:
                keyName = fan2;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.btnF3:
                keyName = fan3;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.btnAuto:
                keyName = fanAuto;
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                break;
            case R.id.tvDefaultTemp:
                showDialogToLearnTemp(cool, true);
                break;
            case R.id.tvImportSync:
                uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
                if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                    Log.e(TAG, "irImportSyncModelArrLst Size:-:" + irImportSyncModelArrLst.size());
                    irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                }
                if (irImportSyncModelArrLst != null && irImportSyncModelArrLst.size() > 0) {
                    final JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < irImportSyncModelArrLst.size(); i++) {
                        String keyName = irImportSyncModelArrLst.get(i).getKeyName();
                        String keyValue = irImportSyncModelArrLst.get(i).getKeyValue();
                        final JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("MacId", Utils.MAC_ID);
                            jsonObject.put("IMEI", Utils.IMEI);
                            jsonObject.put("ReceiverId", roomId);
                            jsonObject.put("RelayId", String.valueOf(appliancePos));
                            jsonObject.put("ChipId", uuidFromDB);
                            jsonObject.put("UserId", id);
                            jsonObject.put("Password", secret);
                            jsonObject.put("KeyCode", keyName);
                            jsonObject.put("KeyCodeValue", keyValue);
                            jsonArray.put(jsonObject);

                            Log.e(TAG, "POST jsonArray:-:" + jsonArray);

                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException:-:" + e.getMessage());
                        }
                    }

                    if (jsonArray != null && jsonArray.length() > 0) {
                        if (Utils.isNetworkAvailable) {
                            sendVolleyPostReq(jsonArray);
                        } else {
                            Utils.showMessageDialog("No Internet", this);
                        }
                    } else {
                        Utils.showMessageDialog("Learn the control with IR Blaster first", this);
                    }
                } else {
                    Utils.showMessageDialog("Learn the control with IR Blaster first", this);
                }
                break;
        }
    }

    private void sendVolleyPostReq(final JSONArray jsonArray) {

        String url = ServiceHandler.baseUrl + "SetIRBlasterKeyValue";

        Log.e(TAG, "POST url:-:" + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "POST response:-:" + response);
                //{"Code":200,"Message":"Inserted Successfully"}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("Code").equalsIgnoreCase("200")) {
                        Toast.makeText(IRBlasterACLearnActivity.this, "Synced successfully!", Toast.LENGTH_SHORT).show();
                        setButtonStatusInitially();
                    } else {
                        Toast.makeText(IRBlasterACLearnActivity.this, "Sync failed, Please try again!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException:-:" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "POST Error:-:" + error.getMessage());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return jsonArray == null ? null : jsonArray.toString().getBytes("utf-8");
                } catch (Exception e) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", jsonArray.toString(), "utf-8");
                    return null;
                }
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest);
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

    private void makeIRRequest(String ip, final String chipId, final String keyName, final String body) {
        Utils.showProgressDialog(IRBlasterACLearnActivity.this);
        String url = "http://" + ip + "/v2/" + chipId;
        final String reqBody = body;//"get_IRL";
        Log.v("IR Request", "url : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String res = response;
                res = res.replace("IR Learner Enabled", "");
                res = res.replace("\r", "");
                res = res.replace("\n", "");
                Log.e("IR res", response + ", trimmed : " + res.trim());
                Utils.hideProgressDialog();

                if (body.equalsIgnoreCase("get_IRL")) {       //Learning the device

                    byte[] keyValueInBytes = Utils.getBytesFromString(res.trim());

                    uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");

                    if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                        irImportSyncModelArrLst = new ArrayList<>();
                        irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                    }

                    if (irImportSyncModelArrLst != null && irImportSyncModelArrLst.size() > 0) {
                        irKeysArrLst = new ArrayList<>();
                        for (int i = 0; i < irImportSyncModelArrLst.size(); i++) {
                            irKeysArrLst.add(irImportSyncModelArrLst.get(i).getKeyName());
                        }
                    }
                    Log.e(TAG, "irKeysArrLst:-:" + irKeysArrLst);
                    if (irKeysArrLst != null && irKeysArrLst.size() > 0) {
                        if (irKeysArrLst.contains(keyName)) {
                            Log.e(TAG, "zmoteId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                    + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName
                                    + " Id:-:" + id + " secret:-:" + secret);

                            /*long id = */
                            dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", keyName, keyValueInBytes, id, secret);
                            //Log.e(TAG, "updateId:-:" + id);
                        } else {

                            Log.e(TAG, "1111chipId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                    + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName
                                    + " Id:-:" + id + " secret:-:" + secret);
                            model.setKey_name(keyName);
                            model.setValue(keyValueInBytes);
                            model.setChipID(zmoteId);
                            /*Newly added attributes..!*/
                            model.setId(id);
                            model.setSecretId(secret);
                            //---------------------------
                            long addId = dbHelper.addIRDevice(model);
                            Log.e(TAG, "addId:-:" + addId);

                        }
                    } else {
                        Log.e(TAG, "2222chipId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName
                                + " Id:-:" + id + " secret:-:" + secret);
                        model.setKey_name(keyName);
                        model.setValue(keyValueInBytes);
                        model.setChipID(zmoteId);
                        /*Newly added attributes..!*/
                        model.setId(id);
                        model.setSecretId(secret);
                        //---------------------------
                        long addId = dbHelper.addIRDevice(model);
                        Log.e(TAG, "addId:-:" + addId);
                    }
                    try {
                        setButtonStatusInitially();
                        Utils.showMessageDialog("Learn succeed", IRBlasterACLearnActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }



                    /*byte[] keyValueInBytes = Utils.getBytesFromString(res.trim());
                    Log.v("key in bytes", Arrays.toString(keyValueInBytes) + "");
                    boolean isValueExists = dbHelper.isKeyValueExists(chipId, keyName, false);
                    Log.v("key val exists", isValueExists + "");
                    if (isValueExists) {     //updating
                        dbHelper.updateIRDeviceKeyValue(chipId, roomId, appliancePos + "", keyName, keyValueInBytes);
                        Log.e("chip", chipId + "");
                        Log.e("room id", roomId + "");
                        Log.e("appl pos", appliancePos + "");
                        Log.e("appl type", applianceType + "");
                        Log.e("key name", keyName + "");
                        Log.e("key in bytes", Arrays.toString(keyValueInBytes) + "");
//                        showUpadteAlertDialog(IRBlasterACActivity.this, "Already learned, learn again ?", roomId, appliancePos+"", applianceType, keyName, keyValueInBytes, zmoteId);
                    } else {     //inserting
                        model.setKey_name(keyName);
                        model.setValue(keyValueInBytes);
                        model.setChipID(chipId);
                        dbHelper.addIRDevice(model);
                    }
                    Utils.showMessageDialog("Learn succeed", IRBlasterACLearnActivity.this);*/
                } else {          //Controlling the device with actual value

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("IR", "Error : " + error.getMessage());
                res = "Error";
                isValueStored = false;
                Utils.showMessageDialog("Not recognized properly", IRBlasterACLearnActivity.this);
                Utils.hideProgressDialog();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "text/plain";//; charset=utf-8
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return reqBody == null ? null : reqBody.getBytes();//.getBytes("utf-8");
                } catch (Exception e) {
                    res = "Error";
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", reqBody, "utf-8");
                    return null;
                }
            }
        };

        AppController.getInstance().addToRequestQueue(strRequest);

    }

    private void showDialogToLearnTemp(final String type, final boolean isDefault) {


        final Dialog dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_temp_learn);
        dialog.setTitle("Learn Temperature");

        RelativeLayout relativeLayout = dialog.findViewById(R.id.main_lay);
        int crntTheme = MyPreferences.getInt(MyPreferences.PrefType.THEME_COLOR, getApplicationContext());
        switch (crntTheme) {
            case 1:
                relativeLayout.setBackgroundResource(R.drawable.theme_one_ldpi);
                break;
            case 2:
                relativeLayout.setBackgroundResource(R.drawable.theme_two_ldpi);
                break;
            case 3:
                relativeLayout.setBackgroundResource(R.drawable.theme_three_ldpi);
                break;
            case 4:
                relativeLayout.setBackgroundResource(R.drawable.theme_four_ldpi);
                break;
        }


        Button btn16D = dialog.findViewById(R.id.btn16D);
        Button btn17D = dialog.findViewById(R.id.btn17D);
        Button btn18D = dialog.findViewById(R.id.btn18D);
        Button btn19D = dialog.findViewById(R.id.btn19D);
        Button btn20D = dialog.findViewById(R.id.btn20D);
        Button btn21D = dialog.findViewById(R.id.btn21D);
        Button btn22D = dialog.findViewById(R.id.btn22D);
        Button btn23D = dialog.findViewById(R.id.btn23D);
        Button btn24D = dialog.findViewById(R.id.btn24D);
        Button btn25D = dialog.findViewById(R.id.btn25D);
        Button btn26D = dialog.findViewById(R.id.btn26D);
        Button btn27D = dialog.findViewById(R.id.btn27D);
        Button btn28D = dialog.findViewById(R.id.btn28D);
        Button btn29D = dialog.findViewById(R.id.btn29D);
        Button btn30D = dialog.findViewById(R.id.btn30D);
        Button btnOK = dialog.findViewById(R.id.btnOK);

        btn16D.setText("16" + degreeSymbol + "C");
        btn17D.setText("17" + degreeSymbol + "C");
        btn18D.setText("18" + degreeSymbol + "C");
        btn19D.setText("19" + degreeSymbol + "C");
        btn20D.setText("20" + degreeSymbol + "C");
        btn21D.setText("21" + degreeSymbol + "C");
        btn22D.setText("22" + degreeSymbol + "C");
        btn23D.setText("23" + degreeSymbol + "C");
        btn24D.setText("24" + degreeSymbol + "C");
        btn25D.setText("25" + degreeSymbol + "C");
        btn26D.setText("26" + degreeSymbol + "C");
        btn27D.setText("27" + degreeSymbol + "C");
        btn28D.setText("28" + degreeSymbol + "C");
        btn29D.setText("29" + degreeSymbol + "C");
        btn30D.setText("30" + degreeSymbol + "C");

        View[] views = {btn16D, btn17D, btn18D, btn19D, btn20D, btn21D, btn22D, btn23D, btn24D,
                btn25D, btn26D, btn27D, btn28D, btn29D, btn30D};

        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = new ArrayList<>();
            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
        }

        if (irImportSyncModelArrLst != null && irImportSyncModelArrLst.size() > 0) {
            irKeysArrLst = new ArrayList<>();
            for (int i = 0; i < irImportSyncModelArrLst.size(); i++) {
                irKeysArrLst.add(irImportSyncModelArrLst.get(i).getKeyName());
            }
        }

        if (irKeysArrLst != null && !irKeysArrLst.isEmpty()) {
            setStateToButtons(irKeysArrLst.contains(type + "16"), btn16D);
            setStateToButtons(irKeysArrLst.contains(type + "17"), btn17D);
            setStateToButtons(irKeysArrLst.contains(type + "18"), btn18D);
            setStateToButtons(irKeysArrLst.contains(type + "19"), btn19D);
            setStateToButtons(irKeysArrLst.contains(type + "20"), btn20D);
            setStateToButtons(irKeysArrLst.contains(type + "21"), btn21D);
            setStateToButtons(irKeysArrLst.contains(type + "22"), btn22D);
            setStateToButtons(irKeysArrLst.contains(type + "23"), btn23D);
            setStateToButtons(irKeysArrLst.contains(type + "24"), btn24D);
            setStateToButtons(irKeysArrLst.contains(type + "25"), btn25D);
            setStateToButtons(irKeysArrLst.contains(type + "26"), btn26D);
            setStateToButtons(irKeysArrLst.contains(type + "27"), btn27D);
            setStateToButtons(irKeysArrLst.contains(type + "28"), btn28D);
            setStateToButtons(irKeysArrLst.contains(type + "29"), btn29D);
            setStateToButtons(irKeysArrLst.contains(type + "30"), btn30D);
        }


        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn16D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "16";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 16" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();

            }
        });
        btn17D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "17";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 17" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn18D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "18";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 18" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn19D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "19";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 19" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn20D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "20";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 20" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn21D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "21";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 21" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn22D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "22";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 22" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn23D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "23";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 23" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn24D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "24";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 24" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn25D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "25";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 25" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn26D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "26";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 26" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn27D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "27";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 27" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn28D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "28";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 28" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn29D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "29";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 29" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        btn30D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyName = type + "30";
                if (isDefault) {
                    MyPreferences.add(MyPreferences.PrefType.DEFAULT_AC_KEY, keyName, getApplicationContext());
                    Toast.makeText(IRBlasterACLearnActivity.this, "Default temperature set to 30" + degreeSymbol, Toast.LENGTH_SHORT).show();
                } else
                    btnTempOnClick();
            }
        });
        dialog.show();
    }

    private void setStateToButtons(boolean contains, Button btnD) {
        if (contains) {
            btnD.setTextColor(colorPrimary);
            btnD.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white1);
        } else {
            btnD.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnD.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
        }
    }

    private void btnTempOnClick() {
        if (Utils.isNetworkAvailable) {
            //TODO Need to check if key is already stored in DB
            Log.e("AcLearn", "localIP:-:" + localIP + " zmoteId:-:" + zmoteId + " keyName:-:" + keyName);
            makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
        } else {
            Utils.showMessageDialog("No Internet.", IRBlasterACLearnActivity.this);
        }
    }
}
