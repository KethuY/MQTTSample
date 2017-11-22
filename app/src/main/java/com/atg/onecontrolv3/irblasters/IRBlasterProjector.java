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
import com.atg.onecontrolv3.Database.DatabaseHelperForPROJ;
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

public class IRBlasterProjector extends BaseActivity implements MqttHelper.responseListener {

    private static final String TAG = IRBlasterProjector.class.getSimpleName();
    private static String projector = "j", home = "h", search = "s", comp = "c", video = "v", usb = "u", lan = "n",
            menu = "m", esc = "e", user = "r", mouse = "o", arwUp = "p", arwDown = "d", arwLeft = "l", arwRight = "t",
            ok = "k", pagePlus = "g", pageMinus = "i", zoomPlus = "z", zoomMinus = "a", volPlus = "f", volMinus = "b",
            avMute = "q", split = "w", freeze = "x";
    Button mCompBtn, mVideoBtn,
            mUSBBtn, mLANBtn, mMenuBtn, mEscBtn, mUserBtn, mMouseBtn,
            mPagePlusBtn, mPageMinusBtn, mZoomPlusBtn, mZoomMinusBtn, mVolPlusBtn, mVolMinusBtn;
    TextView mRelayTv, mPowerTv, mHomeTv, mSearchTv, mUpTv, mDownTv, mLeftTv, mRightTv, mCenterTv, mMuteTv, mSplitTv, mFreezeTv, tvLearn, tvImportSync;
    Animation animator;
    Vibrator vibrator;
    String secret = "", id = "", zmoteId = "", localIP = "";
    String res = null;
    String operationMode = "";
    String roomId = "", applianceType = "", keyName = "";
    int appliancePos;
    DatabaseHelperForPROJ dbHelper;
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
    private ArrayList<IRSendKeysModel> irImportSyncModelArrLst;
    private ArrayList<String> irKeysArrLst;
    private int colorPrimaryDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster_projector);
        setToolBar();
        initializeViews();
        mqttListener = this;
        helper = new MqttHelper(IRBlasterProjector.this, mqttListener);
        colorPrimaryDark=Utils.getColorPrimary(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            setButtonStatusInitially();
            timer = new Timer();
            if (roomId.equals("20051")) {
                mRelayTv.setVisibility(View.INVISIBLE);
            } else {
                mRelayTv.setVisibility(View.VISIBLE);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getReceiverStatus();
                    }
                }, 1000, 1000);
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
            /*case R.id.action_refresh:
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
        Typeface tf3 = Typeface.createFromAsset(getAssets(), "untitled-font-13.ttf");

        //Buttons initialization..!
        mCompBtn = (Button) findViewById(R.id.comp_pro_btn);
        mVideoBtn = (Button) findViewById(R.id.video_pro_btn);
        mUSBBtn = (Button) findViewById(R.id.usb_pro_btn);
        mLANBtn = (Button) findViewById(R.id.lan_pro_btn);
        mMenuBtn = (Button) findViewById(R.id.menu_pro_btn);
        mEscBtn = (Button) findViewById(R.id.esc_pro_btn);
        mUserBtn = (Button) findViewById(R.id.user_pro_btn);
        mMouseBtn = (Button) findViewById(R.id.mouse_pro_btn);
        mPagePlusBtn = (Button) findViewById(R.id.page_plus_pro_btn);
        mPageMinusBtn = (Button) findViewById(R.id.page_minus_pro_btn);
        mZoomPlusBtn = (Button) findViewById(R.id.zoom_plus_pro_btn);
        mZoomMinusBtn = (Button) findViewById(R.id.zoom_minus_pro_btn);
        mVolPlusBtn = (Button) findViewById(R.id.vol_plus_pro_btn);
        mVolMinusBtn = (Button) findViewById(R.id.vol_minus_pro_btn);

        //TextViews initialization..!
        mPowerTv = (TextView) findViewById(R.id.power_pro_tv);
        mRelayTv = (TextView) findViewById(R.id.relay_pro_tv);
        mHomeTv = (TextView) findViewById(R.id.home_pro_tv);
        mSearchTv = (TextView) findViewById(R.id.search_pro_tv);
        mUpTv = (TextView) findViewById(R.id.up_pro_tv);
        mDownTv = (TextView) findViewById(R.id.down_pro_tv);
        mLeftTv = (TextView) findViewById(R.id.left_pro_tv);
        mRightTv = (TextView) findViewById(R.id.right_pro_tv);
        mCenterTv = (TextView) findViewById(R.id.center_pro_tv);
        mMuteTv = (TextView) findViewById(R.id.mute__pro_tv);
        mSplitTv = (TextView) findViewById(R.id.split_pro_tv);
        mFreezeTv = (TextView) findViewById(R.id.freeze_pro_tv);
        tvLearn = (TextView) findViewById(R.id.tvLearn);
        tvImportSync = (TextView) findViewById(R.id.tvImportSync);

        //Setting Typeface to Buttons..!
       /* mCompBtn.setTypeface(tf);
        mVideoBtn.setTypeface(tf);
        mUSBBtn.setTypeface(tf);
        mLANBtn.setTypeface(tf);*/
        mMenuBtn.setTypeface(tf);
        //mEscBtn.setTypeface(tf);
        mUserBtn.setTypeface(tf);
        mMouseBtn.setTypeface(tf);
        mPagePlusBtn.setTypeface(tf);
        mPageMinusBtn.setTypeface(tf);
        mZoomPlusBtn.setTypeface(tf);
        mZoomMinusBtn.setTypeface(tf);
        mVolPlusBtn.setTypeface(tf);
        mVolMinusBtn.setTypeface(tf);

        //Setting Typeface to TextViews..!
        mRelayTv.setTypeface(tf2);
        mPowerTv.setTypeface(tf);
        mHomeTv.setTypeface(tf3);
        mSearchTv.setTypeface(tf3);
        mUpTv.setTypeface(tf1);
        mDownTv.setTypeface(tf1);
        mLeftTv.setTypeface(tf1);
        mRightTv.setTypeface(tf1);
        /*mCenterTv.setTypeface(tf);
        mMuteTv.setTypeface(tf);
        mSplitTv.setTypeface(tf);
        mFreezeTv.setTypeface(tf);*/

        //Animator & Vibrator initialization..!
        animator = AnimationUtils.loadAnimation(this, R.anim.shake);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        dbHelper = new DatabaseHelperForPROJ(this);

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
        Utils.showProgressDialog(IRBlasterProjector.this);
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
                            Utils.showMessageDialog("No internet access", IRBlasterProjector.this);
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
        Utils.showProgressDialog(IRBlasterProjector.this);
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
        getSupportActionBar().setTitle("Projector");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    private void makeIRRequest(String ip, final String chipId, final String keyName, final String body) {
        Utils.showProgressDialog(IRBlasterProjector.this);
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
                        Utils.showMessageDialog("Learn succeed", IRBlasterProjector.this);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception" + e.getMessage());
                    }


                    /*byte[] keyValueInBytes = Utils.getBytesFromString(res.trim());
                    boolean isValueExists = dbHelper.isKeyValueExists(chipId, keyName, false);
                    if (isValueExists) {     //updating
                        dbHelper.updateIRDeviceKeyValue(chipId, roomId, appliancePos + "", keyName, keyValueInBytes);
//                        showUpadteAlertDialog(IRBlasterTVActivity.this, "Already learned, learn again ?", roomId, appliancePos+"", applianceType, keyName, keyValueInBytes, zmoteId);
                    } else {     //inserting
                        model.setKey_name(keyName);
                        model.setValue(keyValueInBytes);
                        model.setChipID(chipId);
                        dbHelper.addIRDevice(model);
                    }
                    try {
                        Utils.showMessageDialog("Learn succeed", IRBlasterProjector.this);
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
                    Utils.showMessageDialog("Not recognized properly", IRBlasterProjector.this);
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
            btn.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));
        } else {
            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    } private void setStatusToButtons2(boolean isValueExists, Button btn) {
        if (isValueExists) {
            btn.setTextColor(colorPrimaryDark);
            btn.setBackground(ContextCompat.getDrawable(this,R.drawable.oc_appliance_count_bg_white2));
        } else {
            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setStatusToTextViews(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            if (tv == mPowerTv || tv == mHomeTv || tv == mSearchTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                tv.setTextColor(colorPrimaryDark);

            }
        } else {
            if (tv == mPowerTv || tv == mHomeTv || tv == mSearchTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.gray_ir));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.red));
            }
        }
    }
    private void setStatusToTextViews2(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            tv.setTextColor(colorPrimaryDark);
            tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));

        } else {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    public void ShowDialogToChooseZmote(Context context, final ArrayList<ZmotesModel> data) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.wifi_networks_list);
        //dialog.setTitle("Choose Zmote");

        Toast.makeText(IRBlasterProjector.this, "Learn the controls", Toast.LENGTH_LONG).show();
        operationMode = "Learn";
        tvLearn.setText("Control");
        tvImportSync.setText("Sync");

        final ListView lvWifiList = (ListView) dialog.findViewById(R.id.lvWifiList);

        if (data != null && data.size() > 0) {
            adapter = new ZmotesViewAdapter(IRBlasterProjector.this, data);
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
                if (model.get(0).getApplianceTypeArrLst().get(i).equals("L")) {
                    if (model.get(0).getAppStatusArrLst().get(i)) {
                        color = ContextCompat.getColor(this, R.color.white);
                        symbol = "*";
                    } else {
                        color = ContextCompat.getColor(this, R.color.gray);
                        symbol = ")";
                    }
                    break;
                } else {
                    break;
                }
            }
        }
        mRelayTv.setTextColor(color);
        mRelayTv.setText(symbol);

        /*for (int i = 0; i < model.size(); i++) {
            if (appliancePos == 1) {
                if (model.get(i).getType1().equals("C")) {
                    if (model.get(i).getStatus1().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 2) {
                if (model.get(i).getType2().equals("C")) {
                    if (model.get(i).getStatus2().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 3) {
                if (model.get(i).getType3().equals("C")) {
                    if (model.get(i).getStatus3().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 4) {
                if (model.get(i).getType4().equals("C")) {
                    if (model.get(i).getStatus4().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 5) {
                if (model.get(i).getType5().equals("C")) {
                    if (model.get(i).getStatus5().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 6) {
                if (model.get(i).getType6().equals("C")) {
                    if (model.get(i).getStatus6().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 7) {
                if (model.get(i).getType7().equals("C")) {
                    if (model.get(i).getStatus7().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            } else if (appliancePos == 8) {
                if (model.get(i).getType8().equals("C")) {
                    if (model.get(i).getStatus8().equalsIgnoreCase("true")) {
                        mRelayTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        mRelayTv.setTextColor(Color.RED);
                    }
                }
            }
        }
        mRelayTv.setText("");
        mRelayTv.setTypeface(tf2);*/
    }


    public void onProClick(View view) {
        vibrator.vibrate(150);
        view.startAnimation(animator);
        switch (view.getId()) {
            case R.id.tvLearn:
                if (tvLearn.getText().toString().trim().equalsIgnoreCase("Learn")) {
                    tvImportSync.setText("Import");
                    if (zmotesList != null && zmotesList.size() > 0) {
                        ShowDialogToChooseZmote(IRBlasterProjector.this, zmotesList);
                    } else {
                        try {
                            Utils.showMessageDialog("No Zmotes available", this);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception" + e.getMessage());
                        }
                    }
                } else {
                    operationMode = "Control";
                    tvLearn.setText("Learn");
                    tvImportSync.setText("Import");
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
            case R.id.relay_pro_tv:
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
            case R.id.power_pro_tv:
                keyName = projector;
                sendIR();
                break;
            case R.id.home_pro_tv:
                keyName = home;
                sendIR();
                break;
            case R.id.search_pro_tv:
                keyName = search;
                sendIR();
                break;
            case R.id.comp_pro_btn:
                keyName = comp;
                sendIR();
                break;
            case R.id.video_pro_btn:
                keyName = video;
                sendIR();
                break;
            case R.id.usb_pro_btn:
                keyName = usb;
                sendIR();
                break;
            case R.id.lan_pro_btn:
                keyName = lan;
                sendIR();
                break;
            case R.id.menu_pro_btn:
                keyName = menu;
                sendIR();
                break;
            case R.id.esc_pro_btn:
                keyName = esc;
                sendIR();
                break;
            case R.id.user_pro_btn:
                keyName = user;
                sendIR();
                break;
            case R.id.mouse_pro_btn:
                keyName = mouse;
                sendIR();
                break;
            case R.id.page_plus_pro_btn:
                keyName = pagePlus;
                sendIR();
                break;
            case R.id.page_minus_pro_btn:
                keyName = pageMinus;
                sendIR();
                break;
            case R.id.zoom_plus_pro_btn:
                keyName = zoomPlus;
                sendIR();
                break;
            case R.id.zoom_minus_pro_btn:
                keyName = zoomMinus;
                sendIR();
                break;
            case R.id.vol_plus_pro_btn:
                keyName = volPlus;
                sendIR();
                break;
            case R.id.vol_minus_pro_btn:
                keyName = volMinus;
                sendIR();
                break;
            case R.id.up_pro_tv:
                keyName = arwUp;
                sendIR();
                break;
            case R.id.down_pro_tv:
                keyName = arwDown;
                sendIR();
                break;
            case R.id.left_pro_tv:
                keyName = arwLeft;
                sendIR();
                break;
            case R.id.right_pro_tv:
                keyName = arwRight;
                sendIR();
                break;
            case R.id.center_pro_tv:
                keyName = ok;
                sendIR();
                break;
            case R.id.mute__pro_tv:
                keyName = avMute;
                sendIR();
                break;
            case R.id.split_pro_tv:
                keyName = split;
                sendIR();
                break;
            case R.id.freeze_pro_tv:
                keyName = freeze;
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
                        Utils.showMessageDialog("No Internet", IRBlasterProjector.this);
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
            Toast.makeText(IRBlasterProjector.this, "Imported successfully!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(IRBlasterProjector.this, "Synced successfully!", Toast.LENGTH_SHORT).show();
                        setButtonStatusInitially();
                    } else {
                        Toast.makeText(IRBlasterProjector.this, "Sync failed, Please try again!", Toast.LENGTH_SHORT).show();
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

   /* private void setButtonStatusInitially() {
        byte[] keyValueInBytesForPower = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Power", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForHome = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Home", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForSearch = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Search", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForComp = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Comp", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForVideo = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Video", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForUSB = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_USB", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForLAN = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_LAN", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForMenu = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Menu", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForEsc = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Esc", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForUser = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_User", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForMouse = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Mouse", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForPagePlus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Page_Plus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForPageMinus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Page_Minus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForZoomPlus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Zoom_Plus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForZoomMinus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Zoom_Minus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForVolPlus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Vol_Plus", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForVolMinus = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Btn_Vol_Minus", roomId, appliancePos + "", applianceType);

        byte[] keyValueInBytesForUp = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Up", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForDown = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Down", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForLeft = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Left", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForRight = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Right", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForCenter = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Center", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForMute = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Mute", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForSplit = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Split", roomId, appliancePos + "", applianceType);
        byte[] keyValueInBytesForFreeze = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, "PRO_Tv_Freeze", roomId, appliancePos + "", applianceType);

        boolean isValueExistsForPower = keyValueInBytesForPower != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Power", true) : false;
        boolean isValueExistsForHome = keyValueInBytesForHome != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Home", true) : false;
        boolean isValueExistsForSearch = keyValueInBytesForSearch != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Search", true) : false;
        boolean isValueExistsForComp = keyValueInBytesForComp != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Comp", true) : false;
        boolean isValueExistsForVideo = keyValueInBytesForVideo != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Video", true) : false;
        boolean isValueExistsForUSB = keyValueInBytesForUSB != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_USB", true) : false;
        boolean isValueExistsForLAN = keyValueInBytesForLAN != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_LAN", true) : false;
        boolean isValueExistsForMenu = keyValueInBytesForMenu != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Menu", true) : false;
        boolean isValueExistsForEsc = keyValueInBytesForEsc != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Esc", true) : false;
        boolean isValueExistsForUser = keyValueInBytesForUser != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_User", true) : false;
        boolean isValueExistsForMouse = keyValueInBytesForMouse != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Mouse", true) : false;
        boolean isValueExistsForPageMinus = keyValueInBytesForPagePlus != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Page_Minus", true) : false;
        boolean isValueExistsForPagePlus = keyValueInBytesForPageMinus != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Page_Plus", true) : false;
        boolean isValueExistsForZoomMinus = keyValueInBytesForZoomMinus != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Zoom_Minus", true) : false;
        boolean isValueExistsForZoomPlus = keyValueInBytesForZoomPlus != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Zoom_Plus", true) : false;
        boolean isValueExistsForVolPlus = keyValueInBytesForVolPlus != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Vol_Plus", true) : false;
        boolean isValueExistsForVolMinus = keyValueInBytesForVolMinus != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Btn_Vol_Minus", true) : false;
        boolean isValueExistsForUp = keyValueInBytesForUp != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Up", true) : false;
        boolean isValueExistsForDown = keyValueInBytesForDown != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Down", true) : false;
        boolean isValueExistsForLeft = keyValueInBytesForLeft != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Left", true) : false;
        boolean isValueExistsForRight = keyValueInBytesForRight != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Right", true) : false;
        boolean isValueExistsForCenter = keyValueInBytesForCenter != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Center", true) : false;
        boolean isValueExistsForMute = keyValueInBytesForMute != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Mute", true) : false;
        boolean isValueExistsForSplit = keyValueInBytesForSplit != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Split", true) : false;
        boolean isValueExistsForFreeze = keyValueInBytesForFreeze != null ? dbHelper.isKeyValueExists(uuidFromDB, "PRO_Tv_Freeze", true) : false;

        setStatusToButtons(isValueExistsForComp, mCompBtn);
        setStatusToButtons(isValueExistsForVideo, mVideoBtn);
        setStatusToButtons(isValueExistsForUSB, mUSBBtn);
        setStatusToButtons(isValueExistsForLAN, mLANBtn);
        setStatusToButtons(isValueExistsForMenu, mMenuBtn);
        setStatusToButtons(isValueExistsForEsc, mEscBtn);
        setStatusToButtons(isValueExistsForUser, mUserBtn);
        setStatusToButtons(isValueExistsForMouse, mMouseBtn);
        setStatusToButtons(isValueExistsForPageMinus, mPageMinusBtn);
        setStatusToButtons(isValueExistsForPagePlus, mPagePlusBtn);
        setStatusToButtons(isValueExistsForZoomMinus, mZoomMinusBtn);
        setStatusToButtons(isValueExistsForZoomPlus, mZoomPlusBtn);
        setStatusToButtons(isValueExistsForVolPlus, mVolMinusBtn);
        setStatusToButtons(isValueExistsForVolMinus, mVolPlusBtn);

        setStatusToTextViews(isValueExistsForPower, mPowerTv);
        setStatusToTextViews(isValueExistsForHome, mHomeTv);
        setStatusToTextViews(isValueExistsForSearch, mSearchTv);
        setStatusToTextViews(isValueExistsForUp, mUpTv);
        setStatusToTextViews(isValueExistsForDown, mDownTv);
        setStatusToTextViews(isValueExistsForLeft, mLeftTv);
        setStatusToTextViews(isValueExistsForRight, mRightTv);
        setStatusToTextViews(isValueExistsForCenter, mCenterTv);
        setStatusToTextViews(isValueExistsForMute, mMuteTv);
        setStatusToTextViews(isValueExistsForSplit, mSplitTv);
        setStatusToTextViews(isValueExistsForFreeze, mFreezeTv);

    }*/

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
            setStatusToTextViews(irKeysArrLst.contains(projector), mPowerTv);
            setStatusToTextViews(irKeysArrLst.contains(home), mHomeTv);
            setStatusToTextViews(irKeysArrLst.contains(search), mSearchTv);
            setStatusToTextViews(irKeysArrLst.contains(arwUp), mUpTv);
            setStatusToTextViews(irKeysArrLst.contains(arwDown), mDownTv);
            setStatusToTextViews(irKeysArrLst.contains(arwLeft), mLeftTv);
            setStatusToTextViews(irKeysArrLst.contains(arwRight), mRightTv);
            setStatusToTextViews(irKeysArrLst.contains(ok), mCenterTv);
            setStatusToTextViews2(irKeysArrLst.contains(avMute), mMuteTv);
            setStatusToTextViews2(irKeysArrLst.contains(split), mSplitTv);
            setStatusToTextViews2(irKeysArrLst.contains(freeze), mFreezeTv);

            setStatusToButtons(irKeysArrLst.contains(comp), mCompBtn);
            setStatusToButtons(irKeysArrLst.contains(video), mVideoBtn);
            setStatusToButtons(irKeysArrLst.contains(usb), mUSBBtn);
            setStatusToButtons(irKeysArrLst.contains(lan), mLANBtn);
            setStatusToButtons2(irKeysArrLst.contains(menu), mMenuBtn);
            setStatusToButtons2(irKeysArrLst.contains(esc), mEscBtn);
            setStatusToButtons2(irKeysArrLst.contains(user), mUSBBtn);
            setStatusToButtons2(irKeysArrLst.contains(mouse), mMouseBtn);
            setStatusToButtons2(irKeysArrLst.contains(pagePlus), mPagePlusBtn);
            setStatusToButtons2(irKeysArrLst.contains(pageMinus), mPageMinusBtn);
            setStatusToButtons2(irKeysArrLst.contains(zoomPlus), mZoomPlusBtn);
            setStatusToButtons2(irKeysArrLst.contains(zoomMinus), mZoomMinusBtn);
            setStatusToButtons2(irKeysArrLst.contains(volPlus), mVolPlusBtn);
            setStatusToButtons2(irKeysArrLst.contains(volMinus), mVolMinusBtn);

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
            RoomsProvider provider = new RoomsProvider(IRBlasterProjector.this);
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
