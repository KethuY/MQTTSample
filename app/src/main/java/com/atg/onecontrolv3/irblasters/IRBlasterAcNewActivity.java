package com.atg.onecontrolv3.irblasters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.atg.onecontrolv3.Database.DatabaseHelperForAC;
import com.atg.onecontrolv3.activities.BaseActivity;
import com.atg.onecontrolv3.models.IRAppliancesModel;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.AcAdapter;
import com.atg.onecontrolv3.adapters.ZmotesViewAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.ACKeyModel;
import com.atg.onecontrolv3.models.ACModel;
import com.atg.onecontrolv3.models.IRGetKeysModel;
import com.atg.onecontrolv3.models.IRSendKeysModel;
import com.atg.onecontrolv3.models.MqttRoomStatusModel;
import com.atg.onecontrolv3.models.RoomStatusModel;
import com.atg.onecontrolv3.models.RoomsProvider;
import com.atg.onecontrolv3.models.ZmotesModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.preferances.MyPreferences;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

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

public class IRBlasterAcNewActivity extends BaseActivity implements MqttHelper.responseListener {
    public static final String TAG = IRBlasterAcNewActivity.class.getSimpleName();
    private static final String on = "o", off = "f", fan1 = "1", fan2 = "2", fan3 = "3", fanAuto = "a", swingLR = "l", swingUD = "t",
            cool = "c", hot = "h", dry = "y", dFan = "d";
    public static int countForACTemp = 16;
    public static String baseUrl = "http://atghas.com/OneControlService/OCService.svc/";
    TextView mPowerOnTv, mPowerOffTv, mDegreesTV, mSwingUpTV, mSwingDownTV, mSwingleftTV, mSwingRightTV, mModelTv, mF2Tv, mArrowTv, mTempMinusTv,
            mTempPlusTv, tvLearn, tvImportSync, tvACAppliance;
    Button mCoolBtn, mHotBtn, mDryBtn, mFanBtn, mFanLevel1, mFanLevel2, mFanLevel3, mFanAuto;
    LinearLayout mSwingUpDownLl, mSwingLeftRightLl;
    String secret = "", id = "", zmoteId = "", localIp = "";
    OneControlPreferences mPreferences;
    String res = null;
    String operationMode = "";
    Toolbar toolbar;
    String roomId, applianceType, keyName, modeType = "c";
    //    Button btnPower, btnTemp, btnAuto, btnCool, btnDry, btnMode, btnSwing, btnSleep;
    int appliancePos;
    DatabaseHelperForAC dbHelper;
    IRAppliancesModel model;
    boolean isValueStored;
    Timer timer;
    ArrayList<ACModel> acModelArrLst;
    ArrayList<ACKeyModel> acModelKeyArrLst;
    //    MqttAndroidClient mMqttClient;
    List<RoomStatusModel> statusData;
    ArrayList<ZmotesModel> zmotesList = new ArrayList<>();
    ZmotesViewAdapter adapter;
    String uuidFromDB = "";
    char degreeSymbol = (char) 0X00B0;
    String macNameStr;
    boolean isClickedImport = false;
    MqttClient client;
    MqttHelper.responseListener mqttListener = null;
    private Vibrator vibrator;
    private Animation animator;
    private Animation animatorZ;
    private Dialog mDialog;
    private boolean isNotDissmissed;
    private OnItemClickListener mListener;
    private boolean isPowerOn = false;
    private MqttHelper helper;
    private ArrayList<IRSendKeysModel> irImportSyncModelArrLst;
    private ArrayList<String> irKeysArrLst;
    private int colorPrimaryDark;

    private TextView mSwingName,mSwingName2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster_ac_new);
        setToolBar();
        initializeViews();
        mqttListener = this;
        helper = new MqttHelper(IRBlasterAcNewActivity.this, mqttListener);
        colorPrimaryDark=Utils.getColorPrimary(this);
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AC");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {

        Typeface tf = Typeface.createFromAsset(getAssets(), "untitled-font-3.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(), "untitled_font_9.ttf");
        Typeface tf2 = Typeface.createFromAsset(getAssets(), "uifont.ttf");
        Typeface mOCV3Font = Typeface.createFromAsset(getAssets(), "oc_font_v3.ttf");

        //TextView initializations..!
        mPowerOnTv = (TextView) findViewById(R.id.power_ac_on_btn);
        mPowerOffTv = (TextView) findViewById(R.id.power_ac_off_btn);
        mDegreesTV = (TextView) findViewById(R.id.degree_ac_tv);
        mSwingUpTV = (TextView) findViewById(R.id.swing_up_ac_tv);
        mSwingDownTV = (TextView) findViewById(R.id.swing_down_ac_tv);
        mSwingleftTV = (TextView) findViewById(R.id.swing_left_ac_tv);
        mSwingRightTV = (TextView) findViewById(R.id.swing_right_ac_tv);
        mModelTv = (TextView) findViewById(R.id.model_ac_btn);
        mF2Tv = (TextView) findViewById(R.id.f2_ac_btn);
        mArrowTv = (TextView) findViewById(R.id.arrow_ac_btn);
        mTempPlusTv = (TextView) findViewById(R.id.temp_plus_ac_tv);
        mTempMinusTv = (TextView) findViewById(R.id.temp_minus_ac_tv);
        tvLearn = (TextView) findViewById(R.id.tvLearn);
        tvImportSync = (TextView) findViewById(R.id.tvImportSync);
        tvACAppliance = (TextView) findViewById(R.id.tvACAppliance);
        mSwingName=(TextView)findViewById(R.id.swing_name_ac);
        mSwingName2=(TextView)findViewById(R.id.swing_name_2_ac);


        //Buttons initializations..!
        mCoolBtn = (Button) findViewById(R.id.cool_ac_btn);
        mHotBtn = (Button) findViewById(R.id.hot_ac_btn);
        mDryBtn = (Button) findViewById(R.id.dry_ac_btn);
        mFanBtn = (Button) findViewById(R.id.fan_ac_btn);
        mFanLevel1 = (Button) findViewById(R.id.level_1);
        mFanLevel2 = (Button) findViewById(R.id.level_2);
        mFanLevel3 = (Button) findViewById(R.id.level_3);
        mFanAuto = (Button) findViewById(R.id.level_auto);

        //LinearLayout initializations..!
        mSwingUpDownLl = (LinearLayout) findViewById(R.id.swing_up_down_ll);
        mSwingLeftRightLl = (LinearLayout) findViewById(R.id.swing_left_right_ll);


        //mDegreesTV.setTypeface(tf);
        mSwingUpTV.setTypeface(tf1);
        mSwingDownTV.setTypeface(tf1);
        mSwingleftTV.setTypeface(tf1);
        mSwingRightTV.setTypeface(tf1);
        mModelTv.setTypeface(tf);
        //mF2Tv.setTypeface(tf);
        mPowerOnTv.setTypeface(tf);
        mPowerOffTv.setTypeface(tf);
        mTempPlusTv.setTypeface(tf);
        mTempMinusTv.setTypeface(tf);
        mArrowTv.setTypeface(tf);
        tvACAppliance.setTypeface(mOCV3Font);

        mCoolBtn.setTypeface(tf);
        mHotBtn.setTypeface(tf);
        mDryBtn.setTypeface(tf);
        mFanBtn.setTypeface(tf);
        mFanBtn.setTypeface(tf);

        animator = AnimationUtils.loadAnimation(this, R.anim.shake);
        animatorZ = AnimationUtils.loadAnimation(this, R.anim.zoom);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        dbHelper = new DatabaseHelperForAC(this);
        operationMode = getIntent().getStringExtra("CONTROLLING");
        roomId = getIntent().getStringExtra("ROOM_ID");
        appliancePos = getIntent().getIntExtra("APPLIANCE_POS", 0);
        applianceType = getIntent().getStringExtra("APPLIANCE_TYPE");
        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
        model = new IRAppliancesModel();
        model.setAppliance_no(appliancePos + "");
        model.setAppliance_type(applianceType);
        model.setRoomId(roomId);

        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
            irImportSyncModelArrLst = dbHelper.getApplianceIRValues(uuidFromDB, roomId, String.valueOf(appliancePos));
            irKeysArrLst = new ArrayList<>();
        }

        if (Utils.isNetworkAvailable) {
            try {
                makeIRClientRequest();
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        } else {
            Utils.showMessageDialog("No internet access", this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        operationMode = "Control";
        try {
            timer = new Timer();
            if (!roomId.equals("20051")) {
                tvACAppliance.setVisibility(View.VISIBLE);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getReceiverStatus();
                    }
                }, 1000, 1000);
            } else {
                tvACAppliance.setVisibility(View.INVISIBLE);
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
            setButtonStatusInitially();
            getACModels();
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.getMessage());
        }
    }

    private void getACModels() {
        String finalUrl = baseUrl + "GetACModels?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("makeIRClientRequest()", "Res : " + response);
                parseAcJson(response);
                Utils.hideProgressDialog();
                if (!secret.trim().equals("") && !id.trim().equals("")) {
                    if (Utils.isNetworkAvailable) {
                        makeIRWidgetRegister(secret, id);
                    } else {
                        Utils.showMessageDialog("No internet access", IRBlasterAcNewActivity.this);
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

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }


    private void getReceiverStatus() {
        //getActivity().runOnUiThread(Timer_Tick);
        if (Utils.isNetworkAvailable) {
            new GetRoomStatusTask().execute(roomId);
        } else {
            try {
                Utils.showMessageDialog("No Internet.", this);
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        }
    }

    private void showAcModelsDialog() {
        final Dialog dialog = new Dialog(IRBlasterAcNewActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.ac_model_item);

        final ListView acModelLV = (ListView) dialog.findViewById(R.id.ac_model_lv);

        if (acModelArrLst != null && acModelArrLst.size() > 0) {
            AcAdapter acAdapter = new AcAdapter(IRBlasterAcNewActivity.this, acModelArrLst, mListener);
            acModelLV.setAdapter(acAdapter);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        dialog.show();

        mDialog = dialog;
    }

    private void parseAcKeysJson(String response) {
        try {
            acModelKeyArrLst = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("AC Keys");
            for (int i = 0; i < jsonArray.length(); i++) {
                ACKeyModel acModel = new ACKeyModel();
                JSONObject object = jsonArray.getJSONObject(i);
                acModel.setCode(object.getString("Code"));
                acModel.setKey(object.getString("Key"));
                acModelKeyArrLst.add(acModel);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    public String getKeyNameBasedOnTemp(int countForACTempDup, String type) {
        String name = "";
        if (type.equalsIgnoreCase("plus")) {
            countForACTempDup++;
        } else {
            countForACTempDup--;
        }
        if (countForACTempDup == 16) {
            name = modeType + "16";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp = countForACTempDup;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 17) {
            name = modeType + "17";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 18) {
            name = modeType + "18";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 19) {
            name = modeType + "19";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 20) {
            name = modeType + "20";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 21) {
            name = modeType + "21";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 22) {
            name = modeType + "22";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 23) {
            name = modeType + "23";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 24) {
            name = modeType + "24";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 25) {
            name = modeType + "25";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 26) {
            name = modeType + "26";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 27) {
            name = modeType + "27";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 28) {
            name = modeType + "28";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 29) {
            name = modeType + "29";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 30) {
            name = modeType + "30";
            if (type.equalsIgnoreCase("minus")) {
                countForACTemp--;
            } else {
                countForACTemp = countForACTempDup;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        }
        return name;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            /*case R.id.action_refresh:
                //makeIRClientRequest();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }


    private void makeIRClientRequest() throws Exception {
        Utils.showProgressDialog(IRBlasterAcNewActivity.this);
        String url = "http://api.zmote.io/client/register";
        Log.v("makeIRClientRequest()", "req : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJson(response);
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

    private void parseAcJson(String response) {

        try {
            acModelArrLst = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("AC Models");
            for (int i = 0; i < jsonArray.length(); i++) {
                ACModel acModel = new ACModel();
                JSONObject object = jsonArray.getJSONObject(i);
                acModel.setId(object.getInt("Id"));
                acModel.setKey(object.getString("Key"));
                acModelArrLst.add(acModel);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
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
        Utils.showProgressDialog(IRBlasterAcNewActivity.this);
        String url = "http://api.zmote.io/widgets";
        final String strBody = "{\"secret\":\"" + secret + "\",\"_id\":\"" + id + "\"}";
        Log.v("makeIRWidgetRequest()", "req : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("makeIRWidgetRequest()", "Res : " + response);
                Utils.hideProgressDialog();
                zmotesList = parseWidgetsJson(response);
                Log.e(TAG, "zmotesList:-:" + zmotesList);
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

    private void setRelayStatus(List<RoomStatusModel> model) {
        /*Typeface tf1 = Typeface.createFromAsset(getAssets(), "uifont.ttf");
        tvACAppliance.setTypeface(tf1);*/
        ArrayList<String> appliances = model.get(0).getApplianceTypeArrLst();
        ArrayList<Boolean> status = model.get(0).getAppStatusArrLst();
        //Log.e(TAG, "appliances:-:" + appliances + " status:-:" + status);
        int color = 0;
        String symbol = "";
        for (int i = 0; i < appliances.size(); i++) {
            if (appliancePos == (i + 1)) {
                if (appliances.get(i).equals("D")) {
                    if (status.get(i)) {
                        color = ContextCompat.getColor(this, R.color.white);
                        symbol = "g";
                    } else {
                        color = ContextCompat.getColor(this, R.color.gray);
                        symbol = "h";
                    }
                    break;
                } else {
                    break;
                }
            }
        }
        tvACAppliance.setTextColor(color);
        tvACAppliance.setText(symbol);
    }

    public void doDemo(String host, String port, String userName, String password, String topic, String chipId, String msg) {
        try {
            Log.e("MQTT", "host : " + host);
            Log.e("MQTT", "port : " + port);
            Log.e("MQTT", "username : " + userName);
            Log.e("MQTT", "password : " + password);
            Log.e("MQTT", "topic : " + topic);
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
                        e.printStackTrace();
                    }
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            client.publish(topic + chipId, message);
            client.disconnect();
        } catch (MqttException e) {
            Log.e("MQTT", "Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ShowDialogToChooseZmote(Context context, final ArrayList<ZmotesModel> data) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.wifi_networks_list);

        Toast.makeText(IRBlasterAcNewActivity.this, "Learn the controls", Toast.LENGTH_LONG).show();
        operationMode = "Learn";
        tvLearn.setText("Control");
        tvImportSync.setText("Sync");

        final ListView lvWifiList = dialog.findViewById(R.id.lvWifiList);

        if (data != null && data.size() > 0) {
            adapter = new ZmotesViewAdapter(IRBlasterAcNewActivity.this, data);
            lvWifiList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        lvWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                localIp = data.get(pos).getLocalIP();
                zmoteId = data.get(pos).getChipID();
                //isNotDissmissed = true;
                senDataToACLearn();
               /* if (isClickedImport) {
                    isClickedImport = false;
                    showAcModelsDialog();
                }*/
//                makeIRRequest(localIP, chipID, keyName, "get_IRL");//localIP, zmoteId
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
               /* if (!isNotDissmissed)*/
                tvLearn.setText("Learn");
                mTempMinusTv.setEnabled(true);
                mTempPlusTv.setEnabled(true);
                operationMode = "Control";
                tvImportSync.setText("Import");
              /*  else
                    isNotDissmissed = false;*/
            }
        });
        dialog.show();
        mDialog = dialog;
    }


    public void onACClick(View view) {
        vibrator.vibrate(150);
        view.startAnimation(animator);
        int redColor = ContextCompat.getColor(this, R.color.red);
        switch (view.getId()) {

            case R.id.power_ac_on_btn:
                /*if (!isPowerOn) {
                    isPowerOn = true;
                    keyName = "AC_ON";
                } else {
                    isPowerOn = false;
                    keyName = "AC_OFF";
                }*/
                keyName = on;
                if (Utils.isNetworkAvailable) {
                    if (operationMode.equalsIgnoreCase("Learn")) {
                        //TODO Need to check if key is already stored in DB
                        //showDialogToLearnTemp(this, "_Cool", true);
                        // makeIRRequest(localIP, zmoteId, keyName, "get_IRL");//localIP, zmoteId
                        senDataToACLearn();
                    } else {

                        uuidFromDB = dbHelper.getUUID(roomId, appliancePos + "");
                        if (uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) {
                            String defaultTemp = MyPreferences.getString(MyPreferences.PrefType.DEFAULT_AC_KEY, getApplicationContext());
                            if (null != defaultTemp && !TextUtils.isEmpty(defaultTemp)) {
                                byte[] keyValueInBytes = new byte[0];
                                keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB, defaultTemp, roomId, appliancePos + "");
                                if (keyValueInBytes != null && keyValueInBytes.length > 0) {
                                    String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                                    new MQTTTask().execute(uuidFromDB, keyValueInString);
                                } else {
                                    byte[] keyValueInBytes1 = new byte[0];
                                    keyValueInBytes1 = dbHelper.getIRDeviceKeyValue(uuidFromDB, keyName, roomId, appliancePos + "");
                                    if (keyValueInBytes1 != null && keyValueInBytes1.length > 0) {
                                        String keyValueInString = Utils.getStringFromBytes(keyValueInBytes1);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                                        new MQTTTask().execute(uuidFromDB, keyValueInString);
                                    } else {
                                        Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                byte[] keyValueInBytes = new byte[0];
                                keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB, keyName, roomId, appliancePos + "");
                                if (keyValueInBytes != null && keyValueInBytes.length > 0) {
                                    String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                                    new MQTTTask().execute(uuidFromDB, keyValueInString);
                                } else {
                                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                        }

                    }
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;

            case R.id.power_ac_off_btn:
                /*if (!isPowerOn) {
                    isPowerOn = true;
                    keyName = "AC_ON";
                } else {
                    isPowerOn = false;
                    keyName = "AC_OFF";
                }*/
                keyName = off;
                if (Utils.isNetworkAvailable) {
                    if (operationMode.equalsIgnoreCase("Learn")) {
                        //TODO Need to check if key is already stored in DB
                        //showDialogToLearnTemp(this, "_Cool", true);
                        // makeIRRequest(localIP, zmoteId, keyName, "get_IRL");//localIP, zmoteId
                        senDataToACLearn();
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

                    }
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.cool_ac_btn:
                mModelTv.setText("2");
                if (mCoolBtn.getCurrentTextColor() == redColor) {
                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                } else {
                    modeType = cool;
                }
                break;
            case R.id.hot_ac_btn:
                mModelTv.setText("3");
                if (mCoolBtn.getCurrentTextColor() == redColor) {
                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                } else {
                    modeType = hot;
                }
                break;
            case R.id.dry_ac_btn:
                mModelTv.setText("Y");
                if (mCoolBtn.getCurrentTextColor() == redColor) {
                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                } else {
                    modeType = dry;
                }
                break;
            case R.id.fan_ac_btn:
                mModelTv.setText("Z");
                if (mCoolBtn.getCurrentTextColor() == redColor) {
                    Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                } else {
                    modeType = dFan;
                }
                break;
            case R.id.level_1:
                mF2Tv.setText("F1");
                keyName = fan1;
                if (Utils.isNetworkAvailable) {

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

                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.level_2:
                mF2Tv.setText("F2");
                keyName = fan2;
                if (Utils.isNetworkAvailable) {


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


                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.level_3:
                mF2Tv.setText("F3");
                keyName = fan3;
                if (Utils.isNetworkAvailable) {

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


                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.level_auto:
                mF2Tv.setText("Auto");
                keyName = fanAuto;
                if (Utils.isNetworkAvailable) {

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

                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.tvACAppliance://Appliance Type
                if (Utils.isNetworkAvailable) {
                   /* new InvokeSwitchTask().execute(roomId, appliancePos + "", Utils.MACID);
                    new CallReceiversStatusCInServerTask().execute(roomId, Utils.MACID);*/
                    helper.sendMsg("A|" + roomId + "|" + appliancePos);
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
        }
    }

    private void senDataToACLearn() {
        Intent intent = new Intent(IRBlasterAcNewActivity.this, IRBlasterACLearnActivity.class);
        intent.putExtra("ROOM_ID", roomId);
        intent.putExtra("APPLIANCE_POS", appliancePos);
        intent.putExtra("APPLIANCE_TYPE", applianceType);
        intent.putExtra("CHIP_IP", localIp);
        intent.putExtra("CHIP_NAME", zmoteId);
        intent.putExtra("SECRET_ID", secret);
        intent.putExtra("ID", id);
        startActivity(intent);
    }

    public void onACClick1(View view) {
        vibrator.vibrate(150);
        view.setVisibility(View.VISIBLE);
        switch (view.getId()) {
            case R.id.tvLearn:
                if (tvLearn.getText().toString().trim().equalsIgnoreCase("Learn")) {
                    tvImportSync.setText("Import");
                    if (zmotesList != null && zmotesList.size() > 0) {
                        ShowDialogToChooseZmote(IRBlasterAcNewActivity.this, zmotesList);
//                        btnTempChoose.setEnabled(true);
                    } else {
                        Utils.showMessageDialog("No Zmotes available", this);
//                        btnTempChoose.setEnabled(true);
                    }

                    mTempPlusTv.setEnabled(false);
                    mTempMinusTv.setEnabled(false);

                } else {
                    tvImportSync.setText("Import");
                    operationMode = "Control";
                    tvLearn.setText("Learn");
                    mTempPlusTv.setEnabled(true);
                    mTempMinusTv.setEnabled(true);
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
            case R.id.swing_up_down_ll:
                keyName = swingUD;
                if (Utils.isNetworkAvailable) {


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


                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.swing_left_right_ll:
                keyName = swingLR;
                if (Utils.isNetworkAvailable) {

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
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;

            case R.id.temp_minus_ac_tv:
                keyName = getKeyNameBasedOnTemp(countForACTemp, "minus");
                Utils.printLog(TAG, "keyNameFirstMinus:-:" + keyName);
                Log.e(TAG, "minus keyName:-:" + keyName);
                if (Utils.isNetworkAvailable) {


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

                   /* if (operationMode.equalsIgnoreCase("Learn")) {
                        //TODO Need to check if key is already stored in DB
                        //makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                    } else {*/
                    /*byte[] keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, keyName, roomId, appliancePos + "");
                    if (keyValueInBytes == null) {
                        Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                    } else {
                        String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                        new MQTTTask().execute(uuidFromDB, keyValueInString);
                    }*/
                    //}
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.temp_plus_ac_tv:
                keyName = getKeyNameBasedOnTemp(countForACTemp, "plus");
                Utils.printLog(TAG, "keyNameFirstPlus:-:" + keyName);
                Log.e(TAG, "plus keyName:-:" + keyName);
                if (Utils.isNetworkAvailable) {

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



                    /*if (operationMode.equalsIgnoreCase("Learn")) {
                        //TODO Need to check if key is already stored in DB
                        //makeIRRequest(localIP, zmoteId, keyName, "get_IRL");
                    } else {*/
                    /*byte[] keyValueInBytes = dbHelper.getIRDeviceKeyValue(uuidFromDB == null ? "" : uuidFromDB, keyName, roomId, appliancePos + "");
                    if (keyValueInBytes == null) {
                        Toast.makeText(this, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
                    } else {
                        String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                        new MQTTTask().execute(uuidFromDB, keyValueInString);
                    }*/
                    // }
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
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
                        Utils.showMessageDialog("No Internet", IRBlasterAcNewActivity.this);
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
            Toast.makeText(IRBlasterAcNewActivity.this, "Imported successfully!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(IRBlasterAcNewActivity.this, "Synced successfully!", Toast.LENGTH_SHORT).show();
                        setButtonStatusInitially();
                    } else {
                        Toast.makeText(IRBlasterAcNewActivity.this, "Sync failed, Please try again!", Toast.LENGTH_SHORT).show();
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


//        final ShapeDrawable bgShape = new ShapeDrawable();
//        bgShape.setShape(new OvalShape());


        if (isValueExistsForPowerOn) {
            mPowerOnTv.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.green));
        } else {
            mPowerOnTv.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.gray_ir));
        }

        if (isValueExistsForPowerOff) {
            mPowerOffTv.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        } else {
            mPowerOffTv.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.gray_ir));
        }

        if (isValueExistsForCool) {
            mCoolBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mCoolBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }

        if (isValueExistsForHot) {
            mHotBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mHotBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForDry) {
            mDryBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mDryBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForFan) {
            mFanBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mFanBtn.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }

        if (isValueExistsForSud) {
            mSwingUpTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
            mSwingDownTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mSwingUpTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
            mSwingDownTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForF1) {
            mFanLevel1.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mFanLevel1.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForF2) {
            mFanLevel2.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mFanLevel2.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForF3) {
            mFanLevel3.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mFanLevel3.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForAuto) {
            mFanAuto.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mFanAuto.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
        if (isValueExistsForSlr) {
            mSwingRightTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
            mSwingleftTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
        } else {
            mSwingRightTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
            mSwingleftTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.red));
        }
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

            setStatusToTextViews(irKeysArrLst.contains(on), mPowerOnTv);
            setStatusToTextViews(irKeysArrLst.contains(off), mPowerOffTv);
            setStatusToButtons(irKeysArrLst.contains(fan1), mFanLevel1);
            setStatusToButtons(irKeysArrLst.contains(fan2), mFanLevel2);
            setStatusToButtons(irKeysArrLst.contains(fan3), mFanLevel3);
            setStatusToButtons(irKeysArrLst.contains(fanAuto), mFanAuto);
            setStatusToButtons(irKeysArrLst.contains("c18"), mCoolBtn);
            setStatusToButtons(irKeysArrLst.contains("h18"), mHotBtn);
            setStatusToButtons(irKeysArrLst.contains("y18"), mDryBtn);
            setStatusToButtons(irKeysArrLst.contains("d18"), mFanBtn);


            if (irKeysArrLst.contains(swingLR)) {
                mSwingRightTV.setTextColor(colorPrimaryDark);
                mSwingleftTV.setTextColor(colorPrimaryDark);
                mSwingLeftRightLl.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));
                mSwingName2.setTextColor(colorPrimaryDark);
            } else {
                mSwingName.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
                mSwingRightTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
                mSwingleftTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
            }
            if (irKeysArrLst.contains(swingUD)) {
                mSwingUpTV.setTextColor(colorPrimaryDark);
                mSwingDownTV.setTextColor(colorPrimaryDark);
                mSwingUpDownLl.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangle_et_style_ovel_white1));
                mSwingName.setTextColor(colorPrimaryDark);
            } else {
                mSwingName.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
                mSwingUpTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
                mSwingDownTV.setTextColor(ContextCompat.getColor(IRBlasterAcNewActivity.this, R.color.white));
            }

        }
    }

    private void setStatusToButtons(boolean isValueExists, Button btn) {
        if (isValueExists) {
            btn.setTextColor(colorPrimaryDark);
            btn.setBackground(ContextCompat.getDrawable(this, R.drawable.oc_appliance_count_bg_white2));
        } else {
            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setStatusToTextViews(boolean isValueExists, TextView tv) {
        if (isValueExists) {
            if (tv == mPowerOffTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else if (tv == mPowerOnTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.green));
            } else {
                tv.setTextColor(colorPrimaryDark);
            }
        } else {
            if (tv == mPowerOffTv || tv == mPowerOnTv) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.gray_ir));
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
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

            /*for (int i = 0; i < mqttRoomStatusModel.getAppDimTypeStatusArrLst().size(); i++) {
                if (Integer.parseInt(roomId) == (i + 1)) {
                    char onOffStatus = mqttRoomStatusModel.getAppDimTypeStatusArrLst().get(i).charAt(2);
                    if (onOffStatus == '1') {
                        tvACAppliance.setText("K");
                        tvACAppliance.setTextColor(ContextCompat.getColor(IRBlasterACActivity.this, R.color.cyan2));
                    } else {
                        tvACAppliance.setText("a");
                        tvACAppliance.setTextColor(ContextCompat.getColor(IRBlasterACActivity.this, R.color.gray));
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
            RoomsProvider provider = new RoomsProvider(IRBlasterAcNewActivity.this);
            statusData = provider.getRoomStatusFromServer(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (statusData != null && statusData.size() != 0) {
                //Log.e(TAG, "StatusData:-:" + statusData);
                setRelayStatus(statusData);
            } else {
                Log.e(TAG, "StatusData null");
            }

            Utils.hideProgressDialog();
        }
    }

    private class MQTTTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utils.showProgressDialog(IRBlasterAcNewActivity.this);
        }

        @Override
        protected String doInBackground(String... voids) {
            doDemo("api.zmote.io", "2883", id, secret, "zmote/towidget/", voids[0], voids[1]);
            return null;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            Utils.hideProgressDialog();
        }
    }
}
