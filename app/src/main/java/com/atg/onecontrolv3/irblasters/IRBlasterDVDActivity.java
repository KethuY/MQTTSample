package com.atg.onecontrolv3.irblasters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.Database.DatabaseHelperForDVD;
import com.atg.onecontrolv3.activities.BaseActivity;
import com.atg.onecontrolv3.models.IRAppliancesModel;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.ZmotesViewAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.IRGetKeysModel;
import com.atg.onecontrolv3.models.IRSendKeysModel;
import com.atg.onecontrolv3.models.MqttRoomStatusModel;
import com.atg.onecontrolv3.models.RoomStatusModel;
import com.atg.onecontrolv3.models.RoomsProvider;
import com.atg.onecontrolv3.models.ZmotesModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class IRBlasterDVDActivity extends BaseActivity implements MqttHelper.responseListener {

    public static final String TAG = IRBlasterDVDActivity.class.getSimpleName();
    private static final String dvd = "d", eject = "e", pause = "a", play = "l", stop = "s", forward = "f", rewind = "r",
            fastForward = "w", fastRewind = "n", volPlus = "u", volMinus = "i", menu = "m", mute = "t", subtitle = "b", display = "y";
    String secret = "", id = "", zmoteId = "", localIP = "";
    String res = null;
    String operationMode = "";
    String roomId = "", applianceType = "", keyName = "";
    int appliancePos;

    DatabaseHelperForDVD dbHelper;
    IRAppliancesModel model;
    boolean isValueStored;
    Timer timer;
    List<RoomStatusModel> statusData;

    MqttClient client;
    ArrayList<ZmotesModel> zmotesList = new ArrayList<>();
    ZmotesViewAdapter adapter;
    String uuidFromDB = "";
    String macNameStr;
    boolean isNotDissmissed = false;

    Button mFastForwardBtn, mFastRevindBtn,
            mVolMinusBtn, mVolPlusBtn, mMenuBtn, mMuteBtn;
    TextView tvLearn, tvImportSync, mRelayTv, mPowerTv, mEjectTv, mPauseTv, mPlayTv, mStopTv, mForwardTv, mRevindTv, mSubtitleTv, mDisplayTv;
    LinearLayout mPowerLl;
    Animation animator, animatorZoom;
    Vibrator vibrator;
    MqttHelper.responseListener mqttListener = null;
    // private SeekBar mSeekBar;
    private Typeface tf, tf1;
    private MqttHelper helper;
    private ArrayList<IRSendKeysModel> irImportSyncModelArrLst;
    private ArrayList<String> irKeysArrLst;
    private int colorPrimaryDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster_dvd);
        setToolBar();
        initializeViews();
        mqttListener = this;
        helper = new MqttHelper(IRBlasterDVDActivity.this, mqttListener);
        colorPrimaryDark=Utils.getColorPrimary(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            setButtonStatusInitially();
            timer = new Timer();
            if (!roomId.equals("20051")) {
                mPowerLl.setVisibility(View.VISIBLE);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getReceiverStatus();
                    }
                }, 1000, 1000);
            } else {
                mPowerLl.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.getMessage());
            //Utils.showMessageDialog("No Internet.", this);
        }
    }

    private void getReceiverStatus() {
        //getActivity().runOnUiThread(Timer_Tick);
        if (Utils.isNetworkAvailable) {
            new GetRoomStatusTask().execute(roomId);
        } else {
            try {
                Utils.showMessageDialog("No Internet.", this);
            } catch (Exception e) {
                Log.e(TAG, "Exception" + e.getMessage());
            }
        }
    }

    private void setRelayStatus(List<RoomStatusModel> model) {

        //mRelayTv.setTypeface(tf1);

        int color = 0;
        String symbol = "";

        for (int i = 0; i < model.get(0).getApplianceTypeArrLst().size(); i++) {
            if (appliancePos == (i + 1)) {
                if (model.get(0).getApplianceTypeArrLst().get(i).equals("J")) {
                    if (model.get(0).getAppStatusArrLst().get(i)) {
                        color = ContextCompat.getColor(this, R.color.white);
                        symbol = "&";
                    } else {
                        color = ContextCompat.getColor(this, R.color.gray);
                        symbol = "%";
                    }
                    break;
                } else {
                    break;
                }
            }
        }
        mRelayTv.setTextColor(color);
        mRelayTv.setText(symbol);
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DVD");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {

        //Typeface initialization..!
        tf = Typeface.createFromAsset(getAssets(), "untitled-font-3.ttf");
        tf1 = Typeface.createFromAsset(getAssets(), "uifont.ttf");

        //Buttons initialization..!
        mFastForwardBtn = (Button) findViewById(R.id.fast_forward_dvd_btn);
        mFastRevindBtn = (Button) findViewById(R.id.fast_revind_dvd_btn);
        mVolMinusBtn = (Button) findViewById(R.id.vol_minus_dvd_btn);
        mVolPlusBtn = (Button) findViewById(R.id.vol_plus_dvd_btn);
        mMenuBtn = (Button) findViewById(R.id.menu_dvd_btn);
        mMuteBtn = (Button) findViewById(R.id.mute_dvd_btn);

        //TextViews initialization..!
        tvLearn = (TextView) findViewById(R.id.tvLearn);
        tvImportSync = (TextView) findViewById(R.id.tvImportSync);
        mRelayTv = (TextView) findViewById(R.id.relay_dvd_tv);
        mPowerTv = (TextView) findViewById(R.id.power_dvd_tv);
        mEjectTv = (TextView) findViewById(R.id.eject_dvd_tv);
        mPauseTv = (TextView) findViewById(R.id.pause_dvd_tv);
        mPlayTv = (TextView) findViewById(R.id.play_dvd_tv);
        mStopTv = (TextView) findViewById(R.id.stop_dvd_tv);
        mForwardTv = (TextView) findViewById(R.id.forward_dvd_tv);
        mRevindTv = (TextView) findViewById(R.id.revind_dvd_tv);
        mSubtitleTv = (TextView) findViewById(R.id.sub_dvd_tv);
        mDisplayTv = (TextView) findViewById(R.id.disp_dvd_tv);

        //LinearLayout initialization..!
        mPowerLl = (LinearLayout) findViewById(R.id.power_dvd_ll);

        //Setting Typeface to Buttons..!
        mFastForwardBtn.setTypeface(tf);
        mFastRevindBtn.setTypeface(tf);
        mVolMinusBtn.setTypeface(tf);
        mVolPlusBtn.setTypeface(tf);
        mMenuBtn.setTypeface(tf);
        mMuteBtn.setTypeface(tf);

        //Setting Typeface to TextViews..!
        mRelayTv.setTypeface(tf1);
        mRelayTv.setTypeface(tf1);
        mPowerTv.setTypeface(tf);
        mEjectTv.setTypeface(tf);
        mPauseTv.setTypeface(tf);
        mPlayTv.setTypeface(tf);
        mStopTv.setTypeface(tf);
        mForwardTv.setTypeface(tf);
        mRevindTv.setTypeface(tf);

        //Animator & Vibrator initialization..!
        animator = AnimationUtils.loadAnimation(this, R.anim.shake);
        animatorZoom = AnimationUtils.loadAnimation(this, R.anim.zoom);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        dbHelper = new DatabaseHelperForDVD(this);

        //Getting Intents data..!
        try {
            operationMode = getIntent().getStringExtra("CONTROLLING");
            roomId = getIntent().getStringExtra("ROOM_ID");
            appliancePos = getIntent().getIntExtra("APPLIANCE_POS", 0);
            applianceType = getIntent().getStringExtra("APPLIANCE_TYPE");
            uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }

        model = new IRAppliancesModel();
        model.setAppliance_no(appliancePos + "");
        model.setAppliance_type(applianceType);
        model.setRoomId(roomId);
        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
            irKeysArrLst = new ArrayList<>();
        }

        if (Utils.isNetworkAvailable) {
            makeIRClientRequest();
        } else {
            try {
                Utils.showMessageDialog("No Internet.", this);
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        }
    }

    private void makeIRClientRequest() {
        Utils.showProgressDialog(IRBlasterDVDActivity.this);
        String url = "http://api.zmote.io/client/register";
        Log.v("makeIRClientRequest()", "req : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("makeIRClientRequest()", "Res : " + response);
                parseJson(response);
                Log.e("sec", "response::" + response);
                Utils.hideProgressDialog();
                if (!secret.trim().equals("") && !id.trim().equals("")) {
                    if (Utils.isNetworkAvailable) {
                        makeIRWidgetRegister(secret, id);
                    } else {
                        try {
                            Utils.showMessageDialog("No internet access", IRBlasterDVDActivity.this);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception:-:" + e.getMessage());
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRClientRequest()", "Error : " + error.getMessage());
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void parseJson(String res) {
        try {
            JSONObject object = new JSONObject(res);
            secret = object.getString("secret");
            id = object.getString("_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeIRWidgetRegister(String secret, String id) {
        Utils.showProgressDialog(IRBlasterDVDActivity.this);
        String url = "http://api.zmote.io/widgets";
        final String strBody = "{\"secret\":\"" + secret + "\",\"_id\":\"" + id + "\"}";
        Log.v("makeIRWidgetRequest()", "req : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("makeIRWidgetRequest()", "Res : " + response);
                Utils.hideProgressDialog();
                zmotesList = parseWidgetsJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRWidgetRequest()", "Error : " + error.getMessage());
                Utils.hideProgressDialog();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return strBody == null ? null : strBody.getBytes("utf-8");
                } catch (Exception e) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", strBody, "utf-8");
                    return null;
                }
            }
        };

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private ArrayList<ZmotesModel> parseWidgetsJson(String res) {
        ArrayList<ZmotesModel> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(res);
            ZmotesModel model;
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                model = new ZmotesModel();
                model.set_id(object.getString("_id"));
                model.setChipID(object.getString("chipID"));
                model.setExternalIP(object.getString("extIP"));
                model.setLicence(object.getString("license"));
                model.setLocalIP(object.getString("localIP"));
                model.setName(object.getString("name"));
                model.setState(object.getString("state"));

                list.add(model);

//                zmoteId = object.getString("chipID");
//                localIP = object.getString("localIP");
            }

            // callIRRequest(localIP, zmoteId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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

    public void onDvdClick(View view) {
        vibrator.vibrate(150);
        switch (view.getId()) {
            case R.id.tvLearn:
                if (tvLearn.getText().toString().trim().equalsIgnoreCase("Learn")) {
                    tvImportSync.setText("Import");
                    if (zmotesList != null && zmotesList.size() > 0) {
                        ShowDialogToChooseZmote(IRBlasterDVDActivity.this, zmotesList);
                    } else {
                        try {
                            Utils.showMessageDialog("No Zmotes available", this);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception" + e.getMessage());
                        }
                    }
                } else {
                    tvImportSync.setText("Import");
                    operationMode = "Control";
                    tvLearn.setText("Learn");
                }
                setButtonStatusInitially();
                break;
            case R.id.tvImportSync:
                if (tvImportSync.getText().toString().equalsIgnoreCase("Import")) {

                    if (zmoteId != null && !TextUtils.isEmpty(zmoteId)) {
                        if (Utils.isNetworkAvailable) {
                            sendVolleyGetRequest();
                        } else {
                            Utils.showMessageDialog("No Internet", this);
                        }
                    } else {
                        zmoteIdRequestDialog();
                    }

                } else if (tvImportSync.getText().toString().equalsIgnoreCase("Sync")) {
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
                }
                break;
            case R.id.relay_dvd_tv:
                mPowerTv.startAnimation(animator);
                if (Utils.isNetworkAvailable) {
                    /*new InvokeSwitchTask().execute(roomId, appliancePos + "", Utils.MACID);
                    new CallReceiversStatusCInServerTask().execute(roomId, Utils.MACID);*/
                    helper.sendMsg("A|" + roomId + "|" + appliancePos);
                } else {
                    try {
                        Utils.showMessageDialog("No Internet.", this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception:-:" + e.getMessage());
                    }
                }
                break;
            case R.id.power_dvd_tv:
                mPowerTv.startAnimation(animator);
                keyName = dvd;
                sendIR();
                break;
            case R.id.eject_dvd_tv:
                mEjectTv.startAnimation(animator);
                keyName = eject;
                sendIR();
                break;
            case R.id.fast_forward_dvd_btn:
                mFastForwardBtn.startAnimation(animator);
                keyName = fastForward;
                sendIR();
                break;
            case R.id.fast_revind_dvd_btn:
                mFastRevindBtn.startAnimation(animator);
                keyName = fastRewind;
                sendIR();
                break;
            case R.id.vol_minus_dvd_btn:
                mVolMinusBtn.startAnimation(animator);
                keyName = volMinus;
                sendIR();
                break;
            case R.id.vol_plus_dvd_btn:
                mVolPlusBtn.startAnimation(animator);
                keyName = volPlus;
                sendIR();
                break;
            case R.id.menu_dvd_btn:
                mMenuBtn.startAnimation(animator);
                keyName = menu;
                sendIR();
                break;
            case R.id.sub_dvd_tv:
                mSubtitleTv.startAnimation(animatorZoom);
                keyName = subtitle;
                sendIR();
                break;
            case R.id.disp_dvd_tv:
                mDisplayTv.startAnimation(animatorZoom);
                keyName = display;
                sendIR();
                break;
            case R.id.forward_dvd_tv:
                mForwardTv.startAnimation(animator);
                keyName = forward;
                sendIR();
                break;
            case R.id.revind_dvd_tv:
                mRevindTv.startAnimation(animator);
                keyName = rewind;
                sendIR();
                break;
            case R.id.pause_dvd_tv:
                mPauseTv.startAnimation(animator);
                keyName = pause;
                sendIR();
                break;
            case R.id.play_dvd_tv:
                mPlayTv.startAnimation(animatorZoom);
                keyName = play;
                sendIR();
                break;
            case R.id.stop_dvd_tv:
                mStopTv.startAnimation(animator);
                keyName = stop;
                sendIR();
                break;
            case R.id.mute_dvd_btn:
                mMuteBtn.startAnimation(animator);
                keyName = mute;
                sendIR();
                break;
        }
    }

    public void zmoteIdRequestDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.zmote_id_req_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText chipId = (EditText) dialogView.findViewById(R.id.chip_id);
        Button submit = (Button) dialogView.findViewById(R.id.btn_submit);
        Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strChipId = chipId.getText().toString().trim();
                strChipId = strChipId.contains(" ") ? strChipId.replaceAll(" ", "") : strChipId;
                if (strChipId != null && !TextUtils.isEmpty(strChipId)) {
                    zmoteId = strChipId;
                    if (Utils.isNetworkAvailable) {
                        sendVolleyGetRequest();
                    } else {
                        Utils.showMessageDialog("No Internet", IRBlasterDVDActivity.this);
                    }
                    alertDialog.cancel();
                } else {
                    chipId.setError("Required!");
                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void sendVolleyGetRequest() {
        String strMethodName = "GetIRBlasterCode";
        String strURL = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
                + "&ReceiverId=" + roomId + "&RelayId=" + appliancePos + "&ChipId=" + zmoteId + "&KeyCode=";
        Log.e(TAG, "GetIRBlasterCode URL:-:" + strURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseIRKeys(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error:-:" + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void parseIRKeys(String response) {
        ArrayList<IRGetKeysModel> irGetKeysModelArrLst = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("IR");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                IRGetKeysModel model = new IRGetKeysModel();
                model.setKeyCode(jsonObject1.getString("KeyCode"));
                model.setKeyCodeVale(jsonObject1.getString("KeyCodeValue"));
                irGetKeysModelArrLst.add(model);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }

        if (irGetKeysModelArrLst.size() > 0) {
            HashMap<String, String> keyValHM = new HashMap<>();
            for (int i = 0; i < irGetKeysModelArrLst.size(); i++) {
                keyValHM.put(irGetKeysModelArrLst.get(i).getKeyCode(), irGetKeysModelArrLst.get(i).getKeyCodeVale());
            }
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

            for (int i = 0; i < irGetKeysModelArrLst.size(); i++) {
                if (irKeysArrLst != null && irKeysArrLst.size() > 0) {
                    if (irKeysArrLst.contains(irGetKeysModelArrLst.get(i).getKeyCode())) {
                        String value = keyValHM.get(irGetKeysModelArrLst.get(i).getKeyCode());
                        Log.e(TAG, "zmoteId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                + appliancePos + "applianceType:-:" + applianceType + "value:-:" + value);
                        long id = dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", irGetKeysModelArrLst.get(i).getKeyCode(), Utils.getBytesFromString(value),
                                irGetKeysModelArrLst.get(i).getUserId(), irGetKeysModelArrLst.get(i).getPassword());
                        Log.e(TAG, "IRIMPORT id:-:" + id);
                    } else {
                        model.setKey_name(irGetKeysModelArrLst.get(i).getKeyCode());
                        model.setValue(Utils.getBytesFromString(irGetKeysModelArrLst.get(i).getKeyCodeVale()));
                        model.setChipID(zmoteId);
                        /*Newly added attributes..!*/
                        model.setId(id);
                        model.setSecretId(secret);
                        //---------------------------
                        dbHelper.addIRDevice(model);
                    }
                } else {
                    model.setKey_name(irGetKeysModelArrLst.get(i).getKeyCode());
                    model.setValue(Utils.getBytesFromString(irGetKeysModelArrLst.get(i).getKeyCodeVale()));
                    model.setChipID(zmoteId);
                    /*Newly added attributes..!*/
                    model.setId(id);
                    model.setSecretId(secret);
                    //---------------------------
                    dbHelper.addIRDevice(model);
                }
            }
            setButtonStatusInitially();
            Toast.makeText(IRBlasterDVDActivity.this, "Imported successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No keys to import", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(IRBlasterDVDActivity.this, "Synced successfully!", Toast.LENGTH_SHORT).show();
                        setButtonStatusInitially();
                    } else {
                        Toast.makeText(IRBlasterDVDActivity.this, "Sync failed, Please try again!", Toast.LENGTH_SHORT).show();
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

    private void sendIR() {

        if (Utils.isNetworkAvailable) {
            if (operationMode.equalsIgnoreCase("Learn")) {
                //TODO Need to check if key is already stored in DB
                makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
            } else {
                uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
                if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                    byte[] keyValueInBytes = new byte[0];
                    keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB, keyName, roomId, appliancePos + "");
                    if (keyValueInBytes != null && keyValueInBytes.length > 0) {
                        String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                        new MQTTTask().execute(uuidFromDB, keyValueInString);
                    } else {
                        Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                }
                /*byte[] keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, keyName, roomId, appliancePos + "", applianceType);
                if (keyValueInBytes == null) {
                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                } else {
                    String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                    new MQTTTask().execute(uuidFromDB, keyValueInString);
                }*/
            }
        } else {
            try {
                Utils.showMessageDialog("No Internet.", this);
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        }
    }

    private void makeIRRequest(String localIp, final String zmoteId, final String keyName, final String body) {
        Utils.showProgressDialog(IRBlasterDVDActivity.this);
        String url = "http://" + localIp + "/v2/" + zmoteId;
        final String reqBody = body;//"get_IRL";
        Log.v("IR Request", "url : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String res = response;
                res = res.replace("IR Learner Enabled", "");
                res = res.replace("\r", "");
                res = res.replace("\n", "");
                Log.v("IR res", response + ", trimmed : " + res.trim());
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
                                    + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName);

                            /*long id = */
                            dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", keyName, keyValueInBytes, id, secret);
                            //Log.e(TAG, "updateId:-:" + id);
                        } else {

                            Log.e(TAG, "1111chipId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                    + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName);
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
                                + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName);
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
                        Utils.showMessageDialog("Learn succeed", IRBlasterDVDActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }
                   /* byte[] keyValueInBytes = Utils.getBytesFromString(res.trim());
                    boolean isValueExists = dbHelper.isKeyValueExists(zmoteId, keyName, false);
                    if (isValueExists) {     //updating
                        dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", applianceType, keyName, keyValueInBytes);
//                        showUpadteAlertDialog(IRBlasterTVActivity.this, "Already learned, learn again ?", roomId, appliancePos+"", applianceType, keyName, keyValueInBytes, zmoteId);
                    } else {     //inserting
                        model.setKey_name(keyName);
                        model.setValue(keyValueInBytes);
                        model.setChipID(zmoteId);
                        dbHelper.addIRDevice(model);
                    }
                    try {
                        Utils.showMessageDialog("Learn succeed", IRBlasterDVDActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }*/
                }

                //Storing in Database
//                if(res.contains("sendir")) {
//                    isValueStored = true;
//                        model.setKey_name(keyName);
//                        model.setValue(res.trim());
//                        dbHelper.addIRDevice(model);
//
//                }else if(res.contains("completeir")){
//                    isValueStored = true;
//                }else {
//                    isValueStored = false;
//                    Utils.showMessageDialog("Not recognized properly", IRBlasterACActivity.this);
//                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("IR", "Error : " + error.getMessage());
                res = "Error";
                isValueStored = false;
                try {
                    Utils.showMessageDialog("Not recognized properly", IRBlasterDVDActivity.this);
                } catch (Exception e) {
                    Log.e(TAG, "Exception" + e.getMessage());
                }
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

    public void doDemo(String host, String port, String userName, String password, String topic, String chipId, String msg) {
        try {
            Log.e("MQTT", "host : " + host);//c
            Log.e("MQTT", "port : " + port);//c
            Log.e("MQTT", "username : " + userName);
            Log.e("MQTT", "password : " + password);
            Log.e("MQTT", "topic : " + topic);//c
            Log.e("MQTT", "chip id : " + chipId);
            Log.e("MQTT", "msg : " + msg);

            client = new MqttClient("tcp://" + host + ":" + port, "pahomqttpublish2", new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(userName);
            options.setPassword(password.toCharArray());
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            options.setKeepAliveInterval(60);
            options.setCleanSession(true);
            client.connect(options);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    try {
                        client.connect();
                    } catch (MqttException e) {
                        Log.e("MQTT", "Exception reconnect : " + e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    Log.e("MQTT", "mqttMessage:-:" + mqttMessage);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    Log.e("MQTT", "iMqttDeliveryToken:-:" + iMqttDeliveryToken);
                }
            });
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            client.publish(topic + chipId, message);
            client.disconnect();
        } catch (MqttException e) {
            Log.e("MQTT", "Exception : " + e.getMessage());
//            Utils.showMessageDialog(e.getMessage(), IRBlasterTVActivity.this);
        }
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

            setStatusToButtons(irKeysArrLst.contains(fastForward), mFastForwardBtn);
            setStatusToButtons(irKeysArrLst.contains(fastRewind), mFastRevindBtn);
            setStatusToButtons(irKeysArrLst.contains(volPlus), mVolPlusBtn);
            setStatusToButtons(irKeysArrLst.contains(volMinus), mVolMinusBtn);
            setStatusToButtons(irKeysArrLst.contains(menu), mMenuBtn);
            setStatusToButtons(irKeysArrLst.contains(mute), mMuteBtn);

            setStatusToTextViews(irKeysArrLst.contains(dvd), mPowerTv);
            setStatusToTextViews(irKeysArrLst.contains(eject), mEjectTv);
            setStatusToTextViews2(irKeysArrLst.contains(pause), mPauseTv);
            setStatusToTextViews2(irKeysArrLst.contains(play), mPlayTv);
            setStatusToTextViews2(irKeysArrLst.contains(stop), mStopTv);
            setStatusToTextViews2(irKeysArrLst.contains(forward), mForwardTv);
            setStatusToTextViews2(irKeysArrLst.contains(rewind), mRevindTv);
            setStatusToTextViews3(irKeysArrLst.contains(subtitle), mSubtitleTv);
            setStatusToTextViews3(irKeysArrLst.contains(display), mDisplayTv);
        }
    }

    private void setStatusToButtons(boolean isValueExists, Button btn) {
        if (isValueExists) {
            btn.setTextColor(colorPrimaryDark);
            btn.setBackground(ContextCompat.getDrawable(this,R.drawable.oc_appliance_count_bg_white2));
        } else {
            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setStatusToTextViews(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            if (tv == mPowerTv || tv == mEjectTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                tv.setTextColor(colorPrimaryDark);
            }
        } else {
            if (tv == mPowerTv || tv == mEjectTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.gray_ir));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.red));
            }
        }
    }
    private void setStatusToTextViews2(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            tv.setTextColor(colorPrimaryDark);
        } else {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }
    private void setStatusToTextViews3(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            if (tv == mSubtitleTv || tv == mDisplayTv) {
                tv.setTextColor(colorPrimaryDark);
                tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white));
            }
        } /*else {
            if (tv == mSubtitleTv || tv == mDisplayTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white));
            } else {
                tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));
                tv.setTextColor(colorPrimaryDark);

            }
        }*/
    }
    public void ShowDialogToChooseZmote(Context context, final ArrayList<ZmotesModel> data) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.wifi_networks_list);
        //dialog.setTitle("Choose Zmote");

        //Toast.makeText(IRBlasterDVDActivity.this, "Learn the controls", Toast.LENGTH_LONG).show();
        operationMode = "Learn";
        tvLearn.setText("Control");
        tvImportSync.setText("Sync");

        final ListView lvWifiList = (ListView) dialog.findViewById(R.id.lvWifiList);

        if (data != null && data.size() > 0) {
            adapter = new ZmotesViewAdapter(IRBlasterDVDActivity.this, data);
            lvWifiList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }


        lvWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                isNotDissmissed = true;
                localIP = data.get(pos).getLocalIP();
                zmoteId = data.get(pos).getChipID();
//                makeIRRequest(localIP, chipID, keyName, "get_IRL");//localIP, zmoteId
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!isNotDissmissed) {
                    tvLearn.setText("Learn");
                    tvImportSync.setText("Import");
                } else {
                    isNotDissmissed = false;
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onMqttResponse(String res) {
        Log.e(TAG, "resStr:-:" + res);
        parseMqttResponse(res);
    }

    private void parseMqttResponse(String mqttMessage) {
        if (mqttMessage.contains("ReceiverStatus:")) {
            String[] arrS = mqttMessage.split(Pattern.quote("{"));
            MqttRoomStatusModel mqttRoomStatusModel = new MqttRoomStatusModel();
            ArrayList<String> arr = new ArrayList<>();
            String appDimmTypeStatStr = "", autoRevokeStr = "";
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < arrS.length; i++) {

                if (i == 0) {
                    String str = arrS[i];
                    String mStrResult = str.replace(":", "").trim();
                    continue;
                }

                    /*if (mStrResult.equals("ReceiverStatus"))*/

                String rooms = arrS[i].contains("}") ? arrS[i].replace("}", "") : arrS[i];
                String[] strInfo = rooms.split(Pattern.quote(";"));
                mqttRoomStatusModel.setRoomId(Integer.parseInt(strInfo[0]));
                mqttRoomStatusModel.setAutoRevoke(strInfo[1]);


                strBuilder.append(mqttRoomStatusModel.getRoomId()).append(";");
                strBuilder.append(mqttRoomStatusModel.getAutoRevoke());
                /*if (!strInfo[2].equals("?")) {
                    appDimmTypeStatStr = strInfo[2];
                    autoRevokeStr = strInfo[1];
                    Log.e(TAG, "autoRevokeStr:-:" + autoRevokeStr);
                }*/
                arr = new ArrayList<>();
                int index = 0;

                if (!strInfo[2].equals("?")) {
                    while (index < strInfo[2].length()) {
                        String str = strInfo[2].substring(index, Math.min(index + 3, strInfo[2].length()));
                        strBuilder.append(str);
                        arr.add(str);
                        index += 3;
                    }
                    mqttRoomStatusModel.setAppDimTypeStatusArrLst(arr);
                }
            }

            if (Utils.isNetworkAvailable) {
                SetReceiversStatusExt(strBuilder.toString());
                strBuilder = new StringBuilder();
            }

           /* for (int i = 0; i <mqttRoomStatusModel.getAppDimTypeStatusArrLst().size() ; i++) {
                if (Integer.parseInt(roomId) == i){
                    char onOffStatus = mqttRoomStatusModel.getAppDimTypeStatusArrLst().get(i).charAt(2);
                    if (onOffStatus == '1'){
                        tvTVAppliance.setText("h");
                        tvTVAppliance.setTextColor(ContextCompat.getColor(IRBlasterTVActivity.this,R.color.cyan2));
                    }else {
                        tvTVAppliance.setText("f");
                        tvTVAppliance.setTextColor(ContextCompat.getColor(IRBlasterTVActivity.this,R.color.gray));
                    }
                }
            }*/
        }
    }

    private void SetReceiversStatusExt(String mqttMsg) {
        String url = ServiceHandler.baseUrl + "SetReceiversStatusExt?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
                + "&Value=" + Utils.MAC_ID + ";" + mqttMsg;
        Log.e(TAG, "SetReceiversStatusExt:-:" + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response1:-:" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse1:-:" + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }

    //Getting Room status from server AsyncTask
    private class GetRoomStatusTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            RoomsProvider provider = new RoomsProvider(IRBlasterDVDActivity.this);
            statusData = provider.getRoomStatusFromServer(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (statusData != null && statusData.size() != 0) {
                setRelayStatus(statusData);
            }
        }
    }

    private class MQTTTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Utils.showProgressDialog(IRBlasterTVActivity.this);
        }

        @Override
        protected String doInBackground(String... voids) {
            doDemo("api.zmote.io", "2883", id, secret, "zmote/towidget/", voids[0], voids[1]);
            return null;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            // Utils.hideProgressDialog();
        }
    }
}
