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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.Database.DatabaseHelperForHT;
import com.atg.onecontrolv3.Database.DatabaseHelperForMP;
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

public class IRBlasterHomeTheaterActivity extends BaseActivity implements MqttHelper.responseListener {
    public static final String TAG = IRBlasterHomeTheaterActivity.class.getSimpleName();
    private static String ht = "h", eject = "e", mute = "m", forward = "f", rewind = "w", fastForward = "o", fastRewind = "t",
            arwUp = "u", arwDown = "d", arwLeft = "l", arwRight = "r", volPlus = "p", volMinus = "i", cenPlus = "c", cenMinus = "s",
            bassPlus = "b", bassMinus = "n", ok = "k";
    Button mRewindBtn, mForwardBtn, mFastForwardBtn, mFastRewindBtn,
            mVolMinusBtn, mVolPlusBtn, mCenMinusBtn, mCenPlusBtn, mBassMinusBtn, mBassPlusBtn;
    TextView tvLearn, tvImportSync, mRelayTv, mPowerTv, mEjectTv, mMuteHdTv, mUpTv, mDownTv, mLeftTv, mRightTv, mOkTv;
    Animation animator;
    Vibrator vibrator;
    String secret = "", id = "", zmoteId = "", localIP = "";
    String res = null;
    String operationMode = "";
    String roomId = "", applianceType = "", keyName = "";
    int appliancePos;
    DatabaseHelperForHT dbHelper;
    DatabaseHelperForMP dbHelper1;
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
    MqttHelper.responseListener mqttListener = null;
    private ArrayList<IRSendKeysModel> irImportSyncModelArrLst;
    private ArrayList<String> irKeysArrLst;
    private Typeface tf;
    private Typeface tf2;
    private MqttHelper helper;
    private int colorPrimaryDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster_home_theater);
        initializeViews();
        setToolBar();
        mqttListener = this;
        helper = new MqttHelper(IRBlasterHomeTheaterActivity.this, mqttListener);
        colorPrimaryDark=Utils.getColorPrimary(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            setButtonStatusInitially();
            timer = new Timer();
            if (!roomId.equals("20051")) {
                mRelayTv.setVisibility(View.VISIBLE);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getReceiverStatus();
                    }
                }, 1000, 1000);
            } else {
                mRelayTv.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.getMessage());
            //Utils.showMessageDialog("No Internet.", this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
           /* case R.id.action_refresh:
                makeIRClientRequest();
                break;*/
        }
        return super.onOptionsItemSelected(item);
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

    private void initializeViews() {
        //Typeface initialization..!
        tf = Typeface.createFromAsset(getAssets(), "untitled-font-3.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(), "untitled_font_9.ttf");
        tf2 = Typeface.createFromAsset(getAssets(), "uifont.ttf");

        //Buttons initialization..!
        mRewindBtn = (Button) findViewById(R.id.rewind_ht_btn);
        mForwardBtn = (Button) findViewById(R.id.forward_ht_btn);
        mFastForwardBtn = (Button) findViewById(R.id.fast_forward_ht_btn);
        mFastRewindBtn = (Button) findViewById(R.id.fast_revind_ht_btn);
        mVolMinusBtn = (Button) findViewById(R.id.vol_minus_ht_btn);
        mVolPlusBtn = (Button) findViewById(R.id.vol_plus_ht_btn);
        mCenMinusBtn = (Button) findViewById(R.id.cen_minus_ht_btn);
        mCenPlusBtn = (Button) findViewById(R.id.cen_plus_ht_btn);
        mBassMinusBtn = (Button) findViewById(R.id.bass_minus_ht_btn);
        mBassPlusBtn = (Button) findViewById(R.id.bass_plus_ht_btn);

        //TextViews initialization..!
        tvLearn = (TextView) findViewById(R.id.tvLearn);
        tvImportSync = (TextView) findViewById(R.id.tvImportSync);
        mPowerTv = (TextView) findViewById(R.id.power_ht_tv);
        mRelayTv = (TextView) findViewById(R.id.relay_ht_tv);
        mEjectTv = (TextView) findViewById(R.id.eject_th_tv);
        mMuteHdTv = (TextView) findViewById(R.id.mute_ht_tv);
        mUpTv = (TextView) findViewById(R.id.up_ht_btn);
        mDownTv = (TextView) findViewById(R.id.down_ht_btn);
        mLeftTv = (TextView) findViewById(R.id.left_ht_btn);
        mRightTv = (TextView) findViewById(R.id.right_ht_btn);
        mOkTv = (TextView) findViewById(R.id.ok_ht_btn);

        //Setting Typeface to Buttons..!
        mRewindBtn.setTypeface(tf);
        mForwardBtn.setTypeface(tf);
        mFastForwardBtn.setTypeface(tf);
        mFastRewindBtn.setTypeface(tf);
        mVolMinusBtn.setTypeface(tf);
        mVolPlusBtn.setTypeface(tf);
        mCenMinusBtn.setTypeface(tf);
        mCenPlusBtn.setTypeface(tf);
        mBassMinusBtn.setTypeface(tf);
        mBassPlusBtn.setTypeface(tf);

        //Setting Typeface to TextViews..!
        mPowerTv.setTypeface(tf);
        mRelayTv.setTypeface(tf2);
        mEjectTv.setTypeface(tf);
        mMuteHdTv.setTypeface(tf);
        mUpTv.setTypeface(tf1);
        mDownTv.setTypeface(tf1);
        mLeftTv.setTypeface(tf1);
        mRightTv.setTypeface(tf1);

        //Animator & Vibrator initialization..!
        animator = AnimationUtils.loadAnimation(this, R.anim.shake);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Getting Intents data..!
        try {
            operationMode = getIntent().getStringExtra("CONTROLLING");
            roomId = getIntent().getStringExtra("ROOM_ID");
            appliancePos = getIntent().getIntExtra("APPLIANCE_POS", 0);
            applianceType = getIntent().getStringExtra("APPLIANCE_TYPE");
            Log.e(TAG, "applianceType:-:" + applianceType);
            TextView powerTxtTv = (TextView) findViewById(R.id.power_txt_tv);
            if (applianceType.equalsIgnoreCase("HT")) {
                mRelayTv.setText("'");
                dbHelper = new DatabaseHelperForHT(this);
                uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
                powerTxtTv.setText("HT");
            } else {
                mRelayTv.setText("p");
                dbHelper1 = new DatabaseHelperForMP(this);
                uuidFromDB = dbHelper1.getUUID(roomId, appliancePos + "");
                powerTxtTv.setText("MP");
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }

        model = new IRAppliancesModel();
        model.setAppliance_no(appliancePos + "");
        model.setAppliance_type(applianceType);
        model.setRoomId(roomId);

        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            if (applianceType.equalsIgnoreCase("HT")) {
                irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
            } else {
                irImportSyncModelArrLst = dbHelper1.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
            }
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
        Utils.showProgressDialog(IRBlasterHomeTheaterActivity.this);
        String url = "http://api.zmote.io/client/register";
        Log.v("makeIRClientRequest()", "req : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("makeIRClientRequest()", "Res : " + response);
                parseJson(response);
                Log.e("sec", "response::" + response);
                Utils.hideProgressDialog();
                if (!secret.trim().equals("") && !id.trim().equals("")) {
                    if (Utils.isNetworkAvailable) {
                        makeIRWidgetRegister(secret, id);
                    } else {
                        try {
                            Utils.showMessageDialog("No internet access", IRBlasterHomeTheaterActivity.this);
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
        Utils.showProgressDialog(IRBlasterHomeTheaterActivity.this);
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

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (applianceType.equalsIgnoreCase("HT")) {
            getSupportActionBar().setTitle("Home Theater");
        } else {
            getSupportActionBar().setTitle("Music Player");
        }
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onHTClick(View view) {
        vibrator.vibrate(150);
        view.startAnimation(animator);
        switch (view.getId()) {
            case R.id.tvLearn:
                if (tvLearn.getText().toString().trim().equalsIgnoreCase("Learn")) {
                    tvImportSync.setText("Import");
                    if (zmotesList != null && zmotesList.size() > 0) {
                        ShowDialogToChooseZmote(IRBlasterHomeTheaterActivity.this, zmotesList);
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
                    if (applianceType.equalsIgnoreCase("HT")) {
                        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
                    } else {
                        uuidFromDB = dbHelper1.getUUID(roomId, appliancePos + "");
                    }
                    if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                        Log.e(TAG, "irImportSyncModelArrLst Size:-:" + irImportSyncModelArrLst.size());
                        if (applianceType.equalsIgnoreCase("HT")) {
                            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                        } else {
                            irImportSyncModelArrLst = dbHelper1.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                        }

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
            case R.id.relay_ht_tv:
                Log.e(TAG, "tvHTAppliance");
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
            case R.id.power_ht_tv:
                keyName = ht;
                sendIR();
                break;
            case R.id.eject_th_tv:
                keyName = eject;
                sendIR();
                break;
            case R.id.mute_ht_tv:
                keyName = mute;
                sendIR();
                break;
            case R.id.rewind_ht_btn:
                keyName = rewind;
                sendIR();
                break;
            case R.id.forward_ht_btn:
                keyName = forward;
                sendIR();
                break;
            case R.id.fast_forward_ht_btn:
                keyName = fastForward;
                sendIR();
                break;
            case R.id.fast_revind_ht_btn:
                keyName = fastRewind;
                sendIR();
                break;
            case R.id.vol_minus_ht_btn:
                keyName = volMinus;
                sendIR();
                break;
            case R.id.vol_plus_ht_btn:
                keyName = volPlus;
                sendIR();
                break;
            case R.id.cen_minus_ht_btn:
                keyName = cenMinus;
                sendIR();
                break;
            case R.id.cen_plus_ht_btn:
                keyName = cenPlus;
                sendIR();
                break;
            case R.id.bass_minus_ht_btn:
                keyName = bassMinus;
                sendIR();
                break;
            case R.id.bass_plus_ht_btn:
                keyName = bassPlus;
                sendIR();
                break;
            case R.id.up_ht_btn:
                keyName = arwUp;
                sendIR();
                break;
            case R.id.down_ht_btn:
                keyName = arwDown;
                sendIR();
                break;
            case R.id.left_ht_btn:
                keyName = arwLeft;
                sendIR();
                break;
            case R.id.right_ht_btn:
                keyName = arwRight;
                sendIR();
                break;
            case R.id.ok_ht_btn:
                keyName = ok;
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
        /*submit.setTextColor(colorPrimaryDark);
        cancel.setTextColor(colorPrimaryDark);*/
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
                        Utils.showMessageDialog("No Internet", IRBlasterHomeTheaterActivity.this);
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

        if (irGetKeysModelArrLst != null && irGetKeysModelArrLst.size() > 0) {
            HashMap<String, String> keyValHM = new HashMap<>();
            for (int i = 0; i < irGetKeysModelArrLst.size(); i++) {
                keyValHM.put(irGetKeysModelArrLst.get(i).getKeyCode(), irGetKeysModelArrLst.get(i).getKeyCodeVale());
            }
            if (applianceType.equalsIgnoreCase("HT")) {
                uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");

            } else {
                uuidFromDB = dbHelper1.getUUID(roomId, appliancePos + "");
            }

            if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                irImportSyncModelArrLst = new ArrayList<>();
                if (applianceType.equalsIgnoreCase("HT")) {
                    irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));

                } else {
                    irImportSyncModelArrLst = dbHelper1.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                }

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
                        if (applianceType.equalsIgnoreCase("HT")) {
                            long id = dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", irGetKeysModelArrLst.get(i).getKeyCode(), Utils.getBytesFromString(value),
                                    irGetKeysModelArrLst.get(i).getUserId(), irGetKeysModelArrLst.get(i).getPassword());
                            Log.e(TAG, "IRIMPORT id:-:" + id);
                        } else {
                            long id = dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", irGetKeysModelArrLst.get(i).getKeyCode(), Utils.getBytesFromString(value),
                                    irGetKeysModelArrLst.get(i).getUserId(), irGetKeysModelArrLst.get(i).getPassword());
                            Log.e(TAG, "IRIMPORT id:-:" + id);
                        }
                    } else {
                        model.setKey_name(irGetKeysModelArrLst.get(i).getKeyCode());
                        model.setValue(Utils.getBytesFromString(irGetKeysModelArrLst.get(i).getKeyCodeVale()));
                        model.setChipID(zmoteId);
                        /*Newly added attributes..!*/
                        model.setId(id);
                        model.setSecretId(secret);
                        //---------------------------
                        if (applianceType.equalsIgnoreCase("HT")) {
                            dbHelper.addIRDevice(model);

                        } else {
                            dbHelper1.addIRDevice(model);
                        }

                    }
                } else {
                    model.setKey_name(irGetKeysModelArrLst.get(i).getKeyCode());
                    model.setValue(Utils.getBytesFromString(irGetKeysModelArrLst.get(i).getKeyCodeVale()));
                    model.setChipID(zmoteId);
                    /*Newly added attributes..!*/
                    model.setId(id);
                    model.setSecretId(secret);
                    //---------------------------
                    if (applianceType.equalsIgnoreCase("HT")) {
                        dbHelper.addIRDevice(model);

                    } else {
                        dbHelper1.addIRDevice(model);
                    }
                }
            }
            Toast.makeText(IRBlasterHomeTheaterActivity.this, "Imported successfully!", Toast.LENGTH_SHORT).show();
            setButtonStatusInitially();

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
                        Toast.makeText(IRBlasterHomeTheaterActivity.this, "Synced successfully!", Toast.LENGTH_SHORT).show();
                        setButtonStatusInitially();
                    } else {
                        Toast.makeText(IRBlasterHomeTheaterActivity.this, "Sync failed, Please try again!", Toast.LENGTH_SHORT).show();
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
                if (applianceType.equalsIgnoreCase("HT")) {
                    uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");

                } else {
                    uuidFromDB = dbHelper1.getUUID(roomId, appliancePos + "");
                }
                if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                    byte[] keyValueInBytes = new byte[0];
                    if (applianceType.equalsIgnoreCase("HT")) {
                        keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB, keyName, roomId, appliancePos + "");

                    } else {
                        keyValueInBytes = dbHelper1.getIRDeviceKeyValue(uuidFromDB, keyName, roomId, appliancePos + "");
                    }

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

                /*byte[] keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, keyName, roomId, appliancePos + "");
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
        Utils.showProgressDialog(IRBlasterHomeTheaterActivity.this);
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
                    if (applianceType.equalsIgnoreCase("HT")) {
                        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
                    } else {
                        uuidFromDB = dbHelper1.getUUID(roomId, appliancePos + "");
                    }

                    if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                        irImportSyncModelArrLst = new ArrayList<>();
                        if (applianceType.equalsIgnoreCase("HT")) {
                            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                        } else {
                            irImportSyncModelArrLst = dbHelper1.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
                        }
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
                            if (applianceType.equalsIgnoreCase("HT")) {
                                /*long id = */
                                dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", keyName, keyValueInBytes, id, secret);
                                //Log.e(TAG, "updateId:-:" + id);
                            } else {
                                /*long id = */
                                dbHelper1.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", keyName, keyValueInBytes, id, secret);
                                //Log.e(TAG, "updateId:-:" + id);
                            }
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
                            if (applianceType.equalsIgnoreCase("HT")) {
                                long addId = dbHelper.addIRDevice(model);
                                Log.e(TAG, "addId:-:" + addId);
                            } else {
                                long addId = dbHelper1.addIRDevice(model);
                                Log.e(TAG, "addId:-:" + addId);
                            }

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
                        if (applianceType.equalsIgnoreCase("HT")) {
                            long addId = dbHelper.addIRDevice(model);
                            Log.e(TAG, "addId:-:" + addId);
                        } else {
                            long addId = dbHelper1.addIRDevice(model);
                            Log.e(TAG, "addId:-:" + addId);
                        }

                    }
                    try {
                        setButtonStatusInitially();
                        Utils.showMessageDialog("Learn succeed", IRBlasterHomeTheaterActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }


                    /*byte[] keyValueInBytes = Utils.getBytesFromString(res.trim());
                    boolean isValueExists = dbHelper.isKeyValueExists(zmoteId, keyName, false);
                    if (isValueExists) {     //updating
                        dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", keyName, keyValueInBytes);
//                        showUpadteAlertDialog(IRBlasterTVActivity.this, "Already learned, learn again ?", roomId, appliancePos+"", applianceType, keyName, keyValueInBytes, zmoteId);
                    } else {     //inserting
                        model.setKey_name(keyName);
                        model.setValue(keyValueInBytes);
                        model.setChipID(zmoteId);
                        dbHelper.addIRDevice(model);
                    }
                    try {
                        Utils.showMessageDialog("Learn succeed", IRBlasterHomeTheaterActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }*/
                } else {          //Controlling the device with actual value

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
                    Utils.showMessageDialog("Not recognized properly", IRBlasterHomeTheaterActivity.this);
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
            if (tv == mPowerTv || tv == mEjectTv || tv == mMuteHdTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                tv.setTextColor(colorPrimaryDark);
            }
        } else {
            if (tv == mPowerTv || tv == mEjectTv || tv == mMuteHdTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.gray_ir));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.red));
            }
        }
    }

    public void ShowDialogToChooseZmote(Context context, final ArrayList<ZmotesModel> data) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.wifi_networks_list);
        //dialog.setTitle("Choose Zmote");

        Toast.makeText(IRBlasterHomeTheaterActivity.this, "Learn the controls", Toast.LENGTH_LONG).show();
        operationMode = "Learn";
        tvLearn.setText("Control");
        tvImportSync.setText("Sync");

        final ListView lvWifiList = (ListView) dialog.findViewById(R.id.lvWifiList);

        if (data != null && data.size() > 0) {
            adapter = new ZmotesViewAdapter(IRBlasterHomeTheaterActivity.this, data);
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

    private void setRelayStatus(List<RoomStatusModel> model) {

        //mRelayTv.setTypeface(tf2);

        int color = 0;
        String symbol = "";

        for (int i = 0; i < model.get(0).getApplianceTypeArrLst().size(); i++) {
            if (appliancePos == (i + 1)) {
                if (applianceType.equalsIgnoreCase("HT")) {
                    if (model.get(0).getApplianceTypeArrLst().get(i).equals("K")) {
                        if (model.get(0).getAppStatusArrLst().get(i)) {
                            color = ContextCompat.getColor(this, R.color.white);
                            symbol = "(";
                        } else {
                            color = ContextCompat.getColor(this, R.color.gray);
                            symbol = "'";
                        }
                        break;
                    } else {
                        break;
                    }
                } else {
                    if (model.get(0).getApplianceTypeArrLst().get(i).equals("H")) {
                        if (model.get(0).getAppStatusArrLst().get(i)) {
                            color = ContextCompat.getColor(this, R.color.white);
                            symbol = "p";
                        } else {
                            color = ContextCompat.getColor(this, R.color.gray_ir);
                            symbol = "q";
                        }
                        break;
                    } else {
                        break;
                    }
                }

            }
        }
        mRelayTv.setTextColor(color);
        mRelayTv.setText(symbol);

       /* for (int i = 0; i < model.size(); i++) {
            if (appliancePos == 1) {
                if (model.get(i).getType1().equals("C")) {
                    if (model.get(i).getStatus1().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 2) {
                if (model.get(i).getType2().equals("C")) {
                    if (model.get(i).getStatus2().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 3) {
                if (model.get(i).getType3().equals("C")) {
                    if (model.get(i).getStatus3().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 4) {
                if (model.get(i).getType4().equals("C")) {
                    if (model.get(i).getStatus4().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 5) {
                if (model.get(i).getType5().equals("C")) {
                    if (model.get(i).getStatus5().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 6) {
                if (model.get(i).getType6().equals("C")) {
                    if (model.get(i).getStatus6().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 7) {
                if (model.get(i).getType7().equals("C")) {
                    if (model.get(i).getStatus7().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            } else if (appliancePos == 8) {
                if (model.get(i).getType8().equals("C")) {
                    if (model.get(i).getStatus8().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.cyan2));
                    } else {
                        mRelayTv.setTextColor(Color.GRAY);
                    }
                }
            }
        }
        mRelayTv.setText("");
        mRelayTv.setTypeface(tf2);*/
    }

   /* private void setButtonStatusInitially() {
        byte[] keyValueInBytesForPower = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Power", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForEject = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Eject", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForMute = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Mute", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForRewind = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Rewind", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForForward = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Forward", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForFastForward = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Fast_Forward", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForFastRewind = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Fast_Rewind", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForVolMinus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Vol_Minus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForVolPlus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Vol_Plus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForCenMinus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Cen_Minus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForCenPlus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Cen_Plus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForBaseMinus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Base_Minus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForBasePlus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Base_Plus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForUp = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Up", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForDown = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Down", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForLeft = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Left", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForRight = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "HT_Btn_Right", roomId, appliancePos + "", applianceType);

        boolean isValueExistsForPower = keyValueInBytesForPower != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Power", true) : false;
        boolean isValueExistsForEject = keyValueInBytesForEject != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Eject", true) : false;
        boolean isValueExistsForMute = keyValueInBytesForMute != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Mute", true) : false;
        boolean isValueExistsForRewind = keyValueInBytesForRewind != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Rewind", true) : false;
        boolean isValueExistsForForward = keyValueInBytesForForward != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Forward", true) : false;
        boolean isValueExistsForFastForward = keyValueInBytesForFastForward != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Fast_Forward", true) : false;
        boolean isValueExistsForFastRewind = keyValueInBytesForFastRewind != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Fast_Rewind", true) : false;
        boolean isValueExistsForVolMinus = keyValueInBytesForVolMinus != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Vol_Minus", true) : false;
        boolean isValueExistsForVolPlus = keyValueInBytesForVolPlus != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Vol_Plus", true) : false;
        boolean isValueExistsForCenMinus = keyValueInBytesForCenMinus != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Cen_Minus", true) : false;
        boolean isValueExistsForCenPlus = keyValueInBytesForCenPlus != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Cen_Plus", true) : false;
        boolean isValueExistsForBaseMinus = keyValueInBytesForBaseMinus != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Base_Minus", true) : false;
        boolean isValueExistsForBasePlus = keyValueInBytesForBasePlus != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Base_Plus", true) : false;
        boolean isValueExistsForUp = keyValueInBytesForUp != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Up", true) : false;
        boolean isValueExistsForDown = keyValueInBytesForDown != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Down", true) : false;
        boolean isValueExistsForLeft = keyValueInBytesForLeft != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Left", true) : false;
        boolean isValueExistsForRight = keyValueInBytesForRight != null ? dbHelper.isKeyValueExists(uuidFromDB, "HT_Btn_Right", true) : false;

        setStatusToButtons(isValueExistsForRewind, mRewindBtn);
        setStatusToButtons(isValueExistsForForward, mForwardBtn);
        setStatusToButtons(isValueExistsForFastForward, mFastForwardBtn);
        setStatusToButtons(isValueExistsForFastRewind, mFastRewindBtn);
        setStatusToButtons(isValueExistsForVolMinus, mVolMinusBtn);
        setStatusToButtons(isValueExistsForVolPlus, mVolPlusBtn);
        setStatusToButtons(isValueExistsForCenMinus, mCenMinusBtn);
        setStatusToButtons(isValueExistsForCenPlus, mCenPlusBtn);
        setStatusToButtons(isValueExistsForBaseMinus, mBassMinusBtn);
        setStatusToButtons(isValueExistsForBasePlus, mBassPlusBtn);
        setStatusToTextViews(isValueExistsForPower, mPowerTv);
        setStatusToTextViews(isValueExistsForEject, mEjectTv);
        setStatusToTextViews(isValueExistsForMute, mMuteHdTv);
        setStatusToTextViews(isValueExistsForUp, mUpTv);
        setStatusToTextViews(isValueExistsForDown, mDownTv);
        setStatusToTextViews(isValueExistsForLeft, mLeftTv);
        setStatusToTextViews(isValueExistsForRight, mRightTv);
    }*/

    private void setButtonStatusInitially() {
        if (applianceType.equalsIgnoreCase("HT")) {
            uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
        } else {
            uuidFromDB = dbHelper1.getUUID(roomId, appliancePos + "");
        }
        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = new ArrayList<>();
            if (applianceType.equalsIgnoreCase("HT")) {
                irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
            } else {
                irImportSyncModelArrLst = dbHelper1.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
            }
        }

        if (irImportSyncModelArrLst != null && irImportSyncModelArrLst.size() > 0) {
            irKeysArrLst = new ArrayList<>();
            for (int i = 0; i < irImportSyncModelArrLst.size(); i++) {
                irKeysArrLst.add(irImportSyncModelArrLst.get(i).getKeyName());
            }
        }

        if (irKeysArrLst != null && irKeysArrLst.size() > 0) {

            setStatusToButtons(irKeysArrLst.contains(forward), mForwardBtn);
            setStatusToButtons(irKeysArrLst.contains(rewind), mRewindBtn);
            setStatusToButtons(irKeysArrLst.contains(fastForward), mFastForwardBtn);
            setStatusToButtons(irKeysArrLst.contains(fastRewind), mFastRewindBtn);
            setStatusToButtons(irKeysArrLst.contains(volPlus), mVolPlusBtn);
            setStatusToButtons(irKeysArrLst.contains(volMinus), mVolMinusBtn);
            setStatusToButtons(irKeysArrLst.contains(cenPlus), mCenPlusBtn);
            setStatusToButtons(irKeysArrLst.contains(cenMinus), mCenMinusBtn);
            setStatusToButtons(irKeysArrLst.contains(bassPlus), mBassPlusBtn);
            setStatusToButtons(irKeysArrLst.contains(bassMinus), mBassMinusBtn);

            setStatusToTextViews(irKeysArrLst.contains(ok), mOkTv);
            setStatusToTextViews(irKeysArrLst.contains(ht), mPowerTv);
            setStatusToTextViews(irKeysArrLst.contains(eject), mEjectTv);
            setStatusToTextViews(irKeysArrLst.contains(mute), mMuteHdTv);
            setStatusToTextViews(irKeysArrLst.contains(arwUp), mUpTv);
            setStatusToTextViews(irKeysArrLst.contains(arwDown), mDownTv);
            setStatusToTextViews(irKeysArrLst.contains(arwLeft), mLeftTv);
            setStatusToTextViews(irKeysArrLst.contains(arwRight), mRightTv);

        }
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
            RoomsProvider provider = new RoomsProvider(IRBlasterHomeTheaterActivity.this);
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

    public class MQTTTask extends AsyncTask<String, Void, String> {

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
