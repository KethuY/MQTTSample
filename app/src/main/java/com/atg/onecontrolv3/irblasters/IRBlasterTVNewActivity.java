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
import com.atg.onecontrolv3.Database.DatabaseHelperForTV;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.BaseActivity;
import com.atg.onecontrolv3.adapters.ZmotesViewAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.IRAppliancesModel;
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

public class IRBlasterTVNewActivity extends BaseActivity implements MqttHelper.responseListener {

    public static final String TAG = IRBlasterTVNewActivity.class.getSimpleName();
    private static final String tv = "t", stb = "s", mute = "m", back = "b", menu = "n", volPlus = "p", volMinus = "i", chPlus = "c", chMinus = "h",
            arwUp = "u", arwDown = "w", arwLeft = "l", arwRight = "r", ok = "o", one = "1", two = "2", three = "3", four = "4", five = "5",
            six = "6", seven = "7", eight = "8", nine = "9", zero = "0", x = "x", y = "y";
    Button mVolPlusBtn, mVolMinusBtn, mChPlusBtn, mChMinusBtn;
    TextView tvLearn, tvImportSync, mPowerTv, mRelayTv, mSTBtv, mMuteTv, mBackTv, mMenuTv, mLeftTv, mRightTv, mUpTv, mDownTv, mOkTv,
            mOneTv, mTwoTv, mThreeTv, mFourTv, mFiveTv, mSixTv, mSevenTv, mEightTv, mNineTv,
            mZeroTv, mXTv, mYTv;
    Animation animator;
    Vibrator vibrator;
    String secret = "", id = "", zmoteId = "", localIP = "";
    String res = null;
    String operationMode = "";
    String roomId = "", applianceType = "", keyName = "";
    int appliancePos;
    DatabaseHelperForTV dbHelper;
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
    private Typeface tf;
    private Typeface tf2;
    private MqttHelper helper;
    private boolean isValueExistsForPower, isValueExistsForDummy2, isValueExistsForMute, isValueExistsForVolPlus, isValueExistsForVolMinus, isValueExistsForChPlus, isValueExistsForChMinus, isValueExistsForBack, isValueExistsForMenu, isValueExistsForLeft, isValueExistsForRight, isValueExistsForUp, isValueExistsForDown, isValueExistsForOk, isValueExistsForOne, isValueExistsForTwo, isValueExistsForThree, isValueExistsForFour, isValueExistsForFive, isValueExistsForSix, isValueExistsForSeven, isValueExistsForEight, isValueExistsForNine, isValueExistsForZero, isValueExistsForDummyB, isValueExistsForDummyB2;
    private ArrayList<IRSendKeysModel> irImportSyncModelArrLst;
    private ArrayList<String> irKeysArrLst;
    private int colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster_tvnew);
        setToolBar();
        initializeViews();
        mqttListener = this;
        helper = new MqttHelper(IRBlasterTVNewActivity.this, mqttListener);
        colorPrimary = Utils.getColorPrimary(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
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
            Utils.showMessageDialog("No Internet.", this);
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
        //mRelayTv.setTypeface(tf2);

        int color = 0;
        String symbol = "";

        for (int i = 0; i < model.get(0).getApplianceTypeArrLst().size(); i++) {
            if (appliancePos == (i + 1)) {
                if (model.get(0).getApplianceTypeArrLst().get(i).equals("C")) {
                    if (model.get(0).getAppStatusArrLst().get(i)) {
                        color = ContextCompat.getColor(this, R.color.white);
                        symbol = "e";
                    } else {
                        color = ContextCompat.getColor(this, R.color.gray);
                        symbol = "f";
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

    private void initializeViews() {

        //Typeface initialization..!
        tf = Typeface.createFromAsset(getAssets(), "untitled-font-3.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(), "untitled_font_9.ttf");
        tf2 = Typeface.createFromAsset(getAssets(), "uifont.ttf");
        Typeface mOCV3Font = Typeface.createFromAsset(getAssets(), "oc_font_v3.ttf");

        //Buttons initialization..!
        mVolPlusBtn = (Button) findViewById(R.id.vol_plus_tv_btn);
        mVolMinusBtn = (Button) findViewById(R.id.vol_minus_tv_btn);
        mChPlusBtn = (Button) findViewById(R.id.ch_plus_tv_btn);
        mChMinusBtn = (Button) findViewById(R.id.ch_minus_tv_btn);

        //TextViews initialization..!
        tvLearn = (TextView) findViewById(R.id.tvLearn);
        tvImportSync = (TextView) findViewById(R.id.tvImportSync);
        mPowerTv = (TextView) findViewById(R.id.power_tv_tv);
        mRelayTv = (TextView) findViewById(R.id.relay_tv_tv);
        mSTBtv = (TextView) findViewById(R.id.stb_tv_tv);
        mMuteTv = (TextView) findViewById(R.id.mute_tv_tv);
        mBackTv = (TextView) findViewById(R.id.back_tv_tv);
        mMenuTv = (TextView) findViewById(R.id.menu_tv_tv);
        mLeftTv = (TextView) findViewById(R.id.left_tv_tv);
        mRightTv = (TextView) findViewById(R.id.right_tv_tv);
        mUpTv = (TextView) findViewById(R.id.up_tv_tv);
        mDownTv = (TextView) findViewById(R.id.down_tv_tv);
        mOkTv = (TextView) findViewById(R.id.ok_tv_tv);

        mOneTv = (TextView) findViewById(R.id.one_tv_tv);
        mTwoTv = (TextView) findViewById(R.id.two_tv_tv);
        mThreeTv = (TextView) findViewById(R.id.three_tv_tv);
        mFourTv = (TextView) findViewById(R.id.four_tv_tv);
        mFiveTv = (TextView) findViewById(R.id.five_tv_tv);
        mSixTv = (TextView) findViewById(R.id.six_tv_tv);
        mSevenTv = (TextView) findViewById(R.id.seven_tv_tv);
        mEightTv = (TextView) findViewById(R.id.eight_tv_tv);
        mNineTv = (TextView) findViewById(R.id.nine_tv_tv);
        mZeroTv = (TextView) findViewById(R.id.zero_tv_tv);
        mXTv = (TextView) findViewById(R.id.x_tv_btn);
        mYTv = (TextView) findViewById(R.id.y_tv_btn);

        //Setting Typeface to Buttons..!
        mPowerTv.setTypeface(tf);
        mMuteTv.setTypeface(tf);
        mVolMinusBtn.setTypeface(tf);
        mVolPlusBtn.setTypeface(tf);
        mChPlusBtn.setTypeface(tf);
        mChMinusBtn.setTypeface(tf);
        mRelayTv.setTypeface(mOCV3Font);
        mSTBtv.setTypeface(tf);

        //Setting Typeface to TextViews..!
        mLeftTv.setTypeface(tf1);
        mRightTv.setTypeface(tf1);
        mUpTv.setTypeface(tf1);
        mDownTv.setTypeface(tf1);

        //Animator & Vibrator initialization..!
        animator = AnimationUtils.loadAnimation(this, R.anim.shake);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        dbHelper = new DatabaseHelperForTV(this);


        //Getting Intents data..!
        // try {
        operationMode = getIntent().getStringExtra("CONTROLLING");
        roomId = getIntent().getStringExtra("ROOM_ID");
        appliancePos = getIntent().getIntExtra("APPLIANCE_POS", 0);
        applianceType = getIntent().getStringExtra("APPLIANCE_TYPE");
        Log.e(TAG, "roomId:-:" + roomId + " appliancePos:-:" + appliancePos);
       /* } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }*/

        model = new IRAppliancesModel();
        model.setAppliance_no(appliancePos + "");
        model.setAppliance_type(applianceType);
        model.setRoomId(roomId);

        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos), "");
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

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TV");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void makeIRClientRequest() {
        Utils.showProgressDialog(IRBlasterTVNewActivity.this);
        String url = "http://api.zmote.io/client/register";
        Log.e("makeIRClientRequest()", "req : " + url);
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
                            Utils.showMessageDialog("No internet access", IRBlasterTVNewActivity.this);
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
            Log.e(TAG, "id:-:" + id + " secret:-:" + secret);
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    private void makeIRWidgetRegister(String secret, String id) {
        Utils.showProgressDialog(IRBlasterTVNewActivity.this);
        String url = "http://api.zmote.io/widgets";
        final String strBody = "{\"secret\":\"" + secret + "\",\"_id\":\"" + id + "\"}";
        Log.e("makeIRWidgetRequest()", "req : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("makeIRWidgetRequest()", "Res : " + response);
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
                    return strBody.getBytes("utf-8");
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
            /*case R.id.action_refresh:
                makeIRClientRequest();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTvClick(View view) {
        vibrator.vibrate(150);
        view.startAnimation(animator);
        switch (view.getId()) {
            case R.id.tvLearn:
                if (tvLearn.getText().toString().trim().equalsIgnoreCase("Learn")) {
                    tvImportSync.setText("Import");
                    if (zmotesList != null && zmotesList.size() > 0) {
                        ShowDialogToChooseZmote(IRBlasterTVNewActivity.this, zmotesList);
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
                        irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos), applianceType);
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

                        if (jsonArray.length() > 0) {
                            if (Utils.isNetworkAvailable) {
                                sendVolleyPostReq(jsonArray);
                            } else {
                                Utils.showMessageDialog("No Internet", this);
                            }
                        } else {
                            Utils.showMessageDialog("Learn the control with IR Blaster first", IRBlasterTVNewActivity.this);
                        }
                    } else {
                        Utils.showMessageDialog("Learn the control with IR Blaster first", IRBlasterTVNewActivity.this);
                    }
                }
                break;
            case R.id.power_tv_tv:
                keyName = tv/*"TVN_Btn_Power"*/;
                sendIR();
                break;
            case R.id.relay_tv_tv:
                Log.e(TAG, "tvTVNewAppliance");
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
            case R.id.stb_tv_tv:
                keyName = stb/*"TVN_Btn_Dummy2"*/;
                sendIR();
                break;
            case R.id.mute_tv_tv:
                keyName = mute/*"TVN_Btn_Mute"*/;
                sendIR();
                break;
            case R.id.vol_plus_tv_btn:
                keyName = volPlus/*"TVN_Btn_Vol_Plus"*/;
                sendIR();
                break;
            case R.id.vol_minus_tv_btn:
                keyName = volMinus/*"TVN_Btn_Vol_Minus"*/;
                sendIR();
                break;
            case R.id.ch_plus_tv_btn:
                keyName = chPlus/*"TVN_Btn_Ch_Plus"*/;
                sendIR();
                break;
            case R.id.ch_minus_tv_btn:
                keyName = chMinus/*"TVN_Btn_Ch_Minus"*/;
                sendIR();
                break;
            case R.id.back_tv_tv:
                keyName = back/*"TVN_Tv_Back"*/;
                sendIR();
                break;
            case R.id.menu_tv_tv:
                keyName = menu/*"TVN_Tv_Menu"*/;
                sendIR();
                break;
            case R.id.up_tv_tv:
                keyName = arwUp/*"TVN_Tv_Up"*/;
                sendIR();
                break;
            case R.id.down_tv_tv:
                keyName = arwDown/*"TVN_Tv_Down"*/;
                sendIR();
                break;
            case R.id.left_tv_tv:
                keyName = arwLeft/*"TVN_Tv_Left"*/;
                sendIR();
                break;
            case R.id.right_tv_tv:
                keyName = arwRight/*"TVN_Tv_Right"*/;
                sendIR();
                break;
            case R.id.ok_tv_tv:
                keyName = ok/*"TVN_TV_Ok"*/;
                sendIR();
                break;
            case R.id.one_tv_tv:
                keyName = one/*"TVN_Tv_One"*/;
                sendIR();
                break;
            case R.id.two_tv_tv:
                keyName = two/*"TVN_Tv_Two"*/;
                sendIR();
                break;
            case R.id.three_tv_tv:
                keyName = three/*"TVN_Tv_Three"*/;
                sendIR();
                break;
            case R.id.four_tv_tv:
                keyName = four/*"TVN_Tv_Four"*/;
                sendIR();
                break;
            case R.id.five_tv_tv:
                keyName = five/*"TVN_Tv_Five"*/;
                sendIR();
                break;
            case R.id.six_tv_tv:
                keyName = six/*"TVN_Tv_Six"*/;
                sendIR();
                break;
            case R.id.seven_tv_tv:
                keyName = seven/*"TVN_Tv_Seven"*/;
                sendIR();
                break;
            case R.id.eight_tv_tv:
                keyName = eight/*"TVN_Tv_Eight"*/;
                sendIR();
                break;
            case R.id.nine_tv_tv:
                keyName = nine/*"TVN_Tv_Nine"*/;
                sendIR();
                break;
            case R.id.zero_tv_tv:
                keyName = zero/*"TVN_Tv_Zero"*/;
                sendIR();
                break;
            case R.id.x_tv_btn:
                keyName = x;
                sendIR();
                break;
            case R.id.y_tv_btn:
                keyName = y;
                sendIR();
                break;
        }
    }

    public void zmoteIdRequestDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IRBlasterTVNewActivity.this);
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
                        Utils.showMessageDialog("No Internet", IRBlasterTVNewActivity.this);
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
        String strURL = Utils.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
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
                model.setChipId(jsonObject1.getString("ChipId"));
                model.setMacId(jsonObject1.getString("MacID"));
                model.setReceiverId(jsonObject1.getString("ReceiverId"));
                model.setRelayId(jsonObject1.getString("RelayId"));
                model.setUserId(jsonObject1.getString("UserId"));
                model.setPassword(jsonObject1.getString("Password"));
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
                irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos), "");
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
                        long id = dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", applianceType,
                                irGetKeysModelArrLst.get(i).getKeyCode(), Utils.getBytesFromString(value),
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
            Toast.makeText(IRBlasterTVNewActivity.this, "Imported successfully!", Toast.LENGTH_SHORT).show();
            /*if (irKeysArrLst != null && irKeysArrLst.size() > 0) {
                if (irKeysArrLst.contains(one)) {
                    String value = keyValHM.get(one);
                    Log.e(TAG, "IRvalue:-:" + value);
                    Log.e(TAG, "localIP:-:" + localIP + "roomId:-:" + roomId + "appliancePos:-:"
                            + appliancePos + "applianceType:-:" + applianceType + "back:-:" + back);
                    long id = dbHelper.updateIRDeviceKeyValue(localIP, roomId, appliancePos + "", applianceType, one, Utils.getBytesFromString(value));
                    Log.e(TAG, "IRIMPORT id:-:" + id);
                } else {
                    String value = keyValHM.get(one);
                    Log.e(TAG, "IRvalue Null:-:");
                    model.setKey_name(back);
                    model.setValue(Utils.getBytesFromString(value));
                    model.setChipID(zmoteId);
                    dbHelper.addIRDevice(model);
                }
            } else {
                String value = keyValHM.get(back);
                Log.e(TAG, "IRvalue Null:-:");
                model.setKey_name(back);
                model.setValue(Utils.getBytesFromString(value));
                model.setChipID(zmoteId);
                long id = dbHelper.addIRDevice(model);
                Log.e(TAG, "row inserted count:-:" + id);
            }*/

            /*if (isValueExistsForOne) {
                String value = keyValHM.get(one);
                Log.e(TAG, "IRvalue:-:" + value);
                Log.e(TAG, "localIP:-:" + localIP + "roomId:-:" + roomId + "appliancePos:-:"
                        + appliancePos + "applianceType:-:" + applianceType + "back:-:" + back);
                long id = dbHelper.updateIRDeviceKeyValue(localIP, roomId, appliancePos + "", applianceType, one, Utils.getBytesFromString(value));
                Log.e(TAG, "IRIMPORT id:-:" + id);
            } else {
                String value = keyValHM.get(one);
                Log.e(TAG, "IRvalue Null:-:");
                model.setKey_name(back);
                model.setValue(Utils.getBytesFromString(value));
                model.setChipID(localIP);
                dbHelper.addIRDevice(model);
            }*/
        } else {
            Toast.makeText(this, "No keys to import", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendVolleyPostReq(final JSONArray jsonArray) {

        String url = Utils.baseUrl + "SetIRBlasterKeyValue";

        Log.e(TAG, "POST url:-:" + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "POST response:-:" + response);
                //{"Code":200,"Message":"Inserted Successfully"}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("Code").equalsIgnoreCase("200")) {
                        Toast.makeText(IRBlasterTVNewActivity.this, "Synced successfully!", Toast.LENGTH_SHORT).show();
                        setButtonStatusInitially();
                    } else {
                        Toast.makeText(IRBlasterTVNewActivity.this, "Sync failed, Please try again!", Toast.LENGTH_SHORT).show();
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
                    keyValueInBytes = dbHelper.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, roomId, appliancePos + "", applianceType);
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
                /*if (keyValueInBytes == null) {
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
        Utils.showProgressDialog(IRBlasterTVNewActivity.this);
        String url = "http://" + localIp + "/v2/" + zmoteId;
        final String reqBody = body;//"get_IRL";
        Log.e("IR Request", "url : " + url);
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
                        irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos), "");
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


                           /*long id =*/
                            dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", applianceType, keyName, keyValueInBytes, id, secret);
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
                    /*boolean isValueExists = dbHelper.isKeyValueExists(zmoteId, keyName, false);
                    Log.e(TAG, "isValueExists:-:" + isValueExists + " zmoteId:-:" + zmoteId + " keyName:-:" + keyName);
                    if (isValueExists) {     //updating

                        Log.e(TAG, "zmoteId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName);

                        dbHelper.updateIRDeviceKeyValue(zmoteId, roomId, appliancePos + "", applianceType, keyName, keyValueInBytes);
//                        showUpadteAlertDialog(IRBlasterTVActivity.this, "Already learned, learn again ?", roomId, appliancePos+"", applianceType, keyName, keyValueInBytes, zmoteId);
                    } else {     //inserting

                        Log.e(TAG, "1111chipId:-:" + zmoteId + "roomId:-:" + roomId + "appliancePos:-:"
                                + appliancePos + "applianceType:-:" + applianceType + "keyName:-:" + keyName);
                        model.setKey_name(keyName);
                        model.setValue(keyValueInBytes);
                        model.setChipID(zmoteId);
                        long addId = dbHelper.addIRDevice(model);
                        Log.e(TAG, "addId:-:" + addId);
                    }*/
                    try {
                        setButtonStatusInitially();
                        Utils.showMessageDialog("Learn succeed", IRBlasterTVNewActivity.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }
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
                    Utils.showMessageDialog("Not recognized properly", IRBlasterTVNewActivity.this);
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

    private void setButtonStatusInitially() {
        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = new ArrayList<>();
            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos), "");
        }

        if (irImportSyncModelArrLst != null && irImportSyncModelArrLst.size() > 0) {
            irKeysArrLst = new ArrayList<>();
            for (int i = 0; i < irImportSyncModelArrLst.size(); i++) {
                irKeysArrLst.add(irImportSyncModelArrLst.get(i).getKeyName());
            }
        }

        if (irKeysArrLst != null && irKeysArrLst.size() > 0) {

            setStatusToButtons(irKeysArrLst.contains(volPlus), mVolPlusBtn);
            setStatusToButtons(irKeysArrLst.contains(volMinus), mVolMinusBtn);
            setStatusToButtons(irKeysArrLst.contains(chPlus), mChPlusBtn);
            setStatusToButtons(irKeysArrLst.contains(chMinus), mChMinusBtn);

            setStatusToTextViews(irKeysArrLst.contains(tv), mPowerTv);
            setStatusToTextViews(irKeysArrLst.contains(stb), mSTBtv);
            setStatusToTextViews(irKeysArrLst.contains(mute), mMuteTv);
            setStatusToTextViews(irKeysArrLst.contains(back), mBackTv);
            setStatusToTextViews(irKeysArrLst.contains(menu), mMenuTv);
            setStatusToTextViews2(irKeysArrLst.contains(arwLeft), mLeftTv);
            setStatusToTextViews2(irKeysArrLst.contains(arwRight), mRightTv);
            setStatusToTextViews2(irKeysArrLst.contains(arwUp), mUpTv);
            setStatusToTextViews2(irKeysArrLst.contains(arwDown), mDownTv);
            setStatusToTextViews2(irKeysArrLst.contains(ok), mOkTv);
            setStatusToTextViews(irKeysArrLst.contains(one), mOneTv);
            setStatusToTextViews(irKeysArrLst.contains(two), mTwoTv);
            setStatusToTextViews(irKeysArrLst.contains(three), mThreeTv);
            setStatusToTextViews(irKeysArrLst.contains(four), mFourTv);
            setStatusToTextViews(irKeysArrLst.contains(five), mFiveTv);
            setStatusToTextViews(irKeysArrLst.contains(six), mSixTv);
            setStatusToTextViews(irKeysArrLst.contains(seven), mSevenTv);
            setStatusToTextViews(irKeysArrLst.contains(eight), mEightTv);
            setStatusToTextViews(irKeysArrLst.contains(nine), mNineTv);
            setStatusToTextViews(irKeysArrLst.contains(zero), mZeroTv);
            setStatusToTextViews(irKeysArrLst.contains(x), mXTv);
            setStatusToTextViews(irKeysArrLst.contains(y), mYTv);
        }
    }

    private void setStatusToButtons(boolean isValueExists, Button btn) {
        if (isValueExists) {
            btn.setTextColor(colorPrimary);
            btn.setBackground(ContextCompat.getDrawable(this,R.drawable.oc_appliance_count_bg_white2));

        } else {
            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setStatusToTextViews(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            if (tv == mPowerTv || tv == mSTBtv || tv == mMuteTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                tv.setTextColor(colorPrimary);
                tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));
            }
        } else {
            if (tv == mPowerTv || tv == mSTBtv || tv == mMuteTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.gray_ir));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
        }
    }
    private void setStatusToTextViews2(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            tv.setTextColor(colorPrimary);
        } else {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
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
        }
    }

    public void ShowDialogToChooseZmote(Context context, final ArrayList<ZmotesModel> data) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.wifi_networks_list);
        //dialog.setTitle("Choose Zmote");

        //Toast.makeText(IRBlasterTVNewActivity.this, "Learn the controls", Toast.LENGTH_LONG).show();
        operationMode = "Learn";
        tvLearn.setText("Control");
        tvImportSync.setText("Sync");

        final ListView lvWifiList = dialog.findViewById(R.id.lvWifiList);

        if (data != null && data.size() > 0) {
            adapter = new ZmotesViewAdapter(IRBlasterTVNewActivity.this, data);
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
        String url = Utils.baseUrl + "SetReceiversStatusExt?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
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
            RoomsProvider provider = new RoomsProvider(IRBlasterTVNewActivity.this);
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
            Log.e(TAG, "Uuid:-:" + voids[0] + "keyValue:-:" + voids[1]);
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
