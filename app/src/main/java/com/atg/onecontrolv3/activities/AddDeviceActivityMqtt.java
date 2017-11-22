package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.ApplianceWattageModel;
import com.atg.onecontrolv3.models.ModeModel;
import com.atg.onecontrolv3.models.MqttRoomStatusModel;
import com.atg.onecontrolv3.models.RoomStatusModel;
import com.atg.onecontrolv3.models.RoomsInfoMqttModel;
import com.atg.onecontrolv3.models.RoomsModel;
import com.atg.onecontrolv3.models.RoomsProvider;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.regex.Pattern;

import static com.atg.onecontrolv3.helpers.Utils.getEmojiByUnicode;


public class AddDeviceActivityMqtt extends BaseActivity implements View.OnClickListener, MqttHelper.responseListener/*, AdapterView.OnItemSelectedListener*/ {

    String TAG = AddDeviceActivityMqtt.class.getSimpleName();
    //String macNameStr;
    String pairedRoomNameStr = "", PairedNumRelaysStr = "", PairedRoomIdStr = "";
    int spinTypePos;
    String spinSymbolLf;

    Toolbar toolbar;
    Button btnAssignName, btnAppliances, btnMultipair, btnSave, btnClear, btnMultiPairSave, btnWattage;
    Button btnWattageSave, btnWattageCancel;
    Button btnOkPair, btnOkPairReSubmit;
    Button btnControlsSave, btnControlsClear;
    Button btn1, btn2, btn3, btn4;
    Button btn5, btn6, btn7, btn8;
    EditText etAssignName, etPairDevice, etPairDeviceResubmit, etWattage;
    Spinner sprApplianceType, sprApplianceSymbol, sprApplianceDimmimg;
    TextView tvTitleRoom;
    TextView tvAssignName, tvAppliances, tvMultipair, tvWattage;
    boolean isbtnOkPairReSubmit;
    LinearLayout pairLayout, pairLayoutResubmit, applianceLayout, nameLayout, llControls, layout8Relay, llMultipair, wattageLayout;
    boolean isAssignNameClicked = false;
    boolean isControlsClicked = false;
    boolean isMultipairClicked = false;
    boolean isWattageClicked = false;
    String newRoomName, pairReceiverNo, newRoomFromServer, newRoomIdFromServer, newRelaysFromServer, btnText;
    boolean isFromEdit;
    //String newRoomExists, newRoomIdExists, newRelaysExists;
    String applianceTypeSelected, applianceSymbolSelected, applianceSymbolNumber, applianceDimmerSelected = "0";
    String strWattage;
    String relayStatus;
    String modeStatus;
    Bundle extras;
    Timer timer;
    String ModeValuesExt;
   //TransparentProgressDialog pd;
    String btn1Mode = "0", btn2Mode = "0", btn3Mode = "0", btn4Mode = "0";
    String btn5Mode = "0", btn6Mode = "0", btn7Mode = "0", btn8Mode = "0";
    MqttHelper.responseListener mqttListener = null;
    private List<RoomStatusModel> dataStatus = new ArrayList<>();
    private List<RoomsModel> allRoomsData = new ArrayList<RoomsModel>();
    private List<ModeModel> modesData = new ArrayList<>();
    private List<ApplianceWattageModel> applianceWattModel = new ArrayList<>();
    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            //This method runs in the same thread as the UI.
            //Do something to the UI thread here
            if (Utils.isNetworkAvailable) {
                new GetRoomsInfoTask().execute();
            } else {
                Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
            }
        }
    };
    private boolean isSpinnerTouched = false;
    private ArrayList<RoomsInfoMqttModel> roomsInfoMqttModelArr;
    private MqttRoomStatusModel mqttRoomStatusModel = new MqttRoomStatusModel();
    private MqttHelper helper;
    private boolean isRoomPairClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        try {
            Log.e(TAG, "OnCreateCalled");
            //macNameStr = MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext());
            extras = getIntent().getExtras();
            roomsInfoMqttModelArr = new ArrayList<>();
            mqttListener = this;
            helper = new MqttHelper(AddDeviceActivityMqtt.this, mqttListener);
            if (extras != null) {
                newRoomFromServer = extras.getString("ROOM_NAME");
                newRoomIdFromServer = extras.getString("ROOM_ID");
                newRelaysFromServer = extras.getString("NUM_OF_RELAYS");
                btnText = extras.getString("BTN_NAME");
                // isFromEdit = extras.getBoolean("RROM_EDIT");
            }
            initializeViews();
            setToolBar();
            timer = new Timer();
            btnAssignName.setText(btnText);

            if (getPreviousData()) {
                pairLayout.setVisibility(View.VISIBLE);
                applianceLayout.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Pair Device");
            } else {
                pairLayout.setVisibility(View.GONE);
                applianceLayout.setVisibility(View.VISIBLE);
                showingRoomAssigned(newRoomFromServer, newRoomIdFromServer, newRelaysFromServer);
                getSupportActionBar().setTitle("Edit Device");
            }
        } catch (Exception e) {
        Log.e(TAG,"OnCreate");
        }
    }

    private boolean getPreviousData() {
        boolean isFromSettingsFragment = false;
        if (newRoomFromServer.equalsIgnoreCase("Pairing settings_home") || newRoomIdFromServer.equalsIgnoreCase("Pairing settings_home") || newRelaysFromServer.equalsIgnoreCase("Pairing settings_home")) {
            isFromSettingsFragment = true;
        } else {
            isFromSettingsFragment = false;
        }
        return isFromSettingsFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            if (!getPreviousData()) {//from edit device
                //http
                new GetRoomStatusTask().execute(newRoomIdFromServer);
            } else {
                //mqtt
            }
        } else {
            Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        /*if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    private void getRoomsInfo() {
        this.runOnUiThread(Timer_Tick);
    }

    public void setToolBar() {

        setSupportActionBar(toolbar);
        //getSupportActionBar().setSubtitle(macNameStr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {
        //pd = new TransparentProgressDialog(AddDeviceActivityMqtt.this, R.drawable.progress);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        pairLayout = (LinearLayout) findViewById(R.id.pairLayout);
        pairLayoutResubmit = (LinearLayout) findViewById(R.id.pairLayoutResubmit);
        applianceLayout = (LinearLayout) findViewById(R.id.applianceLayout);
        llControls = (LinearLayout) findViewById(R.id.llControls);
        layout8Relay = (LinearLayout) findViewById(R.id.layout8Relay);
        llMultipair = (LinearLayout) findViewById(R.id.llMultipair);
        wattageLayout = (LinearLayout) findViewById(R.id.wattageLayout);

        btnAssignName = (Button) findViewById(R.id.btnAssignName);
        btnWattage = (Button) findViewById(R.id.btnWattage);
        btnAppliances = (Button) findViewById(R.id.btnAppliances);
        btnMultipair = (Button) findViewById(R.id.btnMultipair);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnOkPair = (Button) findViewById(R.id.btnOkPair);
        btnOkPairReSubmit = (Button) findViewById(R.id.btnOkPairReSubmit);
        btnControlsSave = (Button) findViewById(R.id.btnControlsSave);
        btnControlsClear = (Button) findViewById(R.id.btnControlsClear);
        btnMultiPairSave = (Button) findViewById(R.id.btnMultiPairSave);
        btnWattageSave = (Button) findViewById(R.id.btnWattageSave);
        btnWattageCancel = (Button) findViewById(R.id.btnWattageClear);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        btn7 = (Button) findViewById(R.id.btn7);
        btn8 = (Button) findViewById(R.id.btn8);

        nameLayout = (LinearLayout) findViewById(R.id.nameLayout);
        etAssignName = (EditText) findViewById(R.id.etAssignName);
        etPairDevice = (EditText) findViewById(R.id.etPairDevice);
        etPairDeviceResubmit = (EditText) findViewById(R.id.etPairDeviceResubmit);
        etWattage = (EditText) findViewById(R.id.etWattage);
        tvTitleRoom = (TextView) findViewById(R.id.tvTitleRoom);

        sprApplianceType = (Spinner) findViewById(R.id.sprTypes);
        sprApplianceSymbol = (Spinner) findViewById(R.id.sprSymbols);
        sprApplianceDimmimg = (Spinner) findViewById(R.id.sprDimming);

        //Setting adapters to spinners
        ArrayAdapter<CharSequence> adapterTypes = null;
        if (PairedNumRelaysStr.equals("6")) {
            adapterTypes = ArrayAdapter.createFromResource(this, R.array.array_appliance_6_types, android.R.layout.simple_spinner_dropdown_item);
        } else {
            adapterTypes = ArrayAdapter.createFromResource(this, R.array.array_appliance_8_types, android.R.layout.simple_spinner_dropdown_item);
        }
        adapterTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprApplianceType.setAdapter(adapterTypes);
        //setDataToSymbolSpinner(0);

        ArrayAdapter<CharSequence> adapterSymbols = ArrayAdapter.createFromResource(AddDeviceActivityMqtt.this, R.array.array_appliance_typessymbols, android.R.layout.simple_spinner_dropdown_item);
        //sprApplianceSymbol.setSelection(0);
        adapterSymbols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprApplianceSymbol.setAdapter(adapterSymbols);

        ArrayAdapter<CharSequence> adapterDimmer = ArrayAdapter.createFromResource(AddDeviceActivityMqtt.this, R.array.array_appliance_dimmer, android.R.layout.simple_spinner_dropdown_item);
        adapterDimmer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprApplianceDimmimg.setAdapter(adapterDimmer);

        btnAssignName.setOnClickListener(this);
        btnAppliances.setOnClickListener(this);
        btnMultipair.setOnClickListener(this);
        btnWattage.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnOkPair.setOnClickListener(this);
        btnOkPairReSubmit.setOnClickListener(this);
        btnControlsSave.setOnClickListener(this);
        btnControlsClear.setOnClickListener(this);
        btnMultiPairSave.setOnClickListener(this);
        btnWattageCancel.setOnClickListener(this);
        btnWattageSave.setOnClickListener(this);
        ////////////

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);

        //Textviews settings icons
        tvAssignName = (TextView) findViewById(R.id.tvAssignName);
        tvAppliances = (TextView) findViewById(R.id.tvAppliances);
        tvMultipair = (TextView) findViewById(R.id.tvMultipair);
        tvWattage = (TextView) findViewById(R.id.tvWattage);
        Typeface tf = Typeface.createFromAsset(getAssets(), "settings_font.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(), "uifont.ttf");
        tvAssignName.setTypeface(tf);
        tvAppliances.setTypeface(tf);
        tvMultipair.setTypeface(tf);
        tvWattage.setTypeface(tf1);
    }

    private void displayPopupWindow(final View anchorView) {
        final PopupWindow popup = new PopupWindow(this);
        View layout = getLayoutInflater().inflate(R.layout.modes_popup_layout, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        //popup.setBackgroundDrawable(new BitmapDrawable());

        final ShapeDrawable bgShape = new ShapeDrawable();
        bgShape.setShape(new OvalShape());

        Button btnMode1 = (Button) layout.findViewById(R.id.btnMode1);
        Button btnMode2 = (Button) layout.findViewById(R.id.btnMode2);
        Button btnMode3 = (Button) layout.findViewById(R.id.btnMode3);
        btnMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bgShape.getPaint().setColor(Color.parseColor("#5aa366"));
                anchorView.setBackgroundDrawable(bgShape);
                if (anchorView == btn1) {
                    btn1Mode = "1";
                } else if (anchorView == btn2) {
                    btn2Mode = "1";
                } else if (anchorView == btn3) {
                    btn3Mode = "1";
                } else if (anchorView == btn4) {
                    btn4Mode = "1";
                } else if (anchorView == btn5) {
                    btn5Mode = "1";
                } else if (anchorView == btn6) {
                    btn6Mode = "1";
                } else if (anchorView == btn7) {
                    btn7Mode = "1";
                } else if (anchorView == btn8) {
                    btn8Mode = "1";
                }
                popup.dismiss();
            }
        });
        btnMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bgShape.getPaint().setColor(Color.BLUE);
                anchorView.setBackgroundDrawable(bgShape);
                if (anchorView == btn1) {
                    btn1Mode = "2";
                } else if (anchorView == btn2) {
                    btn2Mode = "2";
                } else if (anchorView == btn3) {
                    btn3Mode = "2";
                } else if (anchorView == btn4) {
                    btn4Mode = "2";
                } else if (anchorView == btn5) {
                    btn5Mode = "2";
                } else if (anchorView == btn6) {
                    btn6Mode = "2";
                } else if (anchorView == btn7) {
                    btn7Mode = "2";
                } else if (anchorView == btn8) {
                    btn8Mode = "2";
                }
                popup.dismiss();
            }
        });
        btnMode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bgShape.getPaint().setColor(Color.parseColor("#ac4228"));
                anchorView.setBackgroundDrawable(bgShape);
                if (anchorView == btn1) {
                    btn1Mode = "3";
                } else if (anchorView == btn2) {
                    btn2Mode = "3";
                } else if (anchorView == btn3) {
                    btn3Mode = "3";
                } else if (anchorView == btn4) {
                    btn4Mode = "3";
                } else if (anchorView == btn5) {
                    btn5Mode = "3";
                } else if (anchorView == btn6) {
                    btn6Mode = "3";
                } else if (anchorView == btn7) {
                    btn7Mode = "3";
                } else if (anchorView == btn8) {
                    btn8Mode = "3";
                }
                popup.dismiss();
            }
        });
        popup.showAsDropDown(anchorView);
    }

    @Override
    public void onClick(View v) {
        /*PopupMenu popupMenu = new PopupMenu(this, v);
        PopupWindow popup = new PopupWindow(this);
        View popupLayout = getLayoutInflater().inflate(R.layout.modes_popup_layout, null);
        popup.setContentView(popupLayout);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);*/

        switch (v.getId()) {
            case R.id.btnAssignName:

                if (isAssignNameClicked) {
                    isAssignNameClicked = false;
                    nameLayout.setVisibility(View.GONE);

                    btnAssignName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                } else {
                    nameLayout.setVisibility(View.VISIBLE);
                    isAssignNameClicked = true;
                    llControls.setVisibility(View.GONE);
                    llMultipair.setVisibility(View.GONE);
                    wattageLayout.setVisibility(View.GONE);
                    btnAssignName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    btnAppliances.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnMultipair.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnWattage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                }
                break;
            case R.id.btnAppliances:
                if (isControlsClicked) {
                    isControlsClicked = false;
                    llControls.setVisibility(View.GONE);
                    btnAppliances.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                } else {
                    llControls.setVisibility(View.VISIBLE);
                    isControlsClicked = true;
                    nameLayout.setVisibility(View.GONE);
                    llMultipair.setVisibility(View.GONE);
                    wattageLayout.setVisibility(View.GONE);
                    btnAppliances.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    btnAssignName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnMultipair.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnWattage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                }
                break;
            case R.id.btnMultipair:
                if (isMultipairClicked) {
                    isMultipairClicked = false;
                    llMultipair.setVisibility(View.GONE);
                    btnMultipair.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                } else {
                    llControls.setVisibility(View.GONE);
                    nameLayout.setVisibility(View.GONE);
                    wattageLayout.setVisibility(View.GONE);
                    llMultipair.setVisibility(View.VISIBLE);
                    isMultipairClicked = true;
                    btnMultipair.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    btnAppliances.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnAssignName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnWattage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                }
                break;
            case R.id.btnWattage:
                if (isWattageClicked) {
                    isWattageClicked = false;
                    wattageLayout.setVisibility(View.GONE);
                    btnWattage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                } else {
                    llControls.setVisibility(View.GONE);
                    llMultipair.setVisibility(View.GONE);
                    nameLayout.setVisibility(View.GONE);
                    wattageLayout.setVisibility(View.VISIBLE);
                    isWattageClicked = true;
                    btnWattage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    btnAppliances.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnMultipair.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    btnAssignName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                }
                break;
            case R.id.btnSave:
                newRoomName = etAssignName.getText().toString().trim();
                newRoomName = newRoomName.contains(" ") ? newRoomName.replaceAll(" ", "~") : newRoomName;
                Log.e(TAG, "newRoomName:-:" + newRoomName);

                //if (getPreviousData()) {
                pairReceiverNo = etPairDevice.getText().toString();
                /*} else {
                    pairReceiverNo = newRoomIdFromServer;
                }*/

                if (newRoomName.length() == 0) {
                    Utils.showMessageDialog("Enter Name", AddDeviceActivityMqtt.this);
                } else {
                    //Service call
                    if (Utils.isNetworkAvailable) {
                        String newRoomNameMqtt = etAssignName.getText().toString().trim();
                        new SendMqttMsgTask().execute("SetRoomNameTask", newRoomNameMqtt);
                    } else {
                        Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
                    }
                }
                break;
            case R.id.btnClear:
                etAssignName.setText("");
                tvTitleRoom.setText("");
                break;
            case R.id.btnControlsSave:
                strWattage = etWattage.getText().toString().trim();
                Log.e(TAG, "strWattage " + strWattage);
                //if (!"".equals(strWattage) && !"0".equals(strWattage))
                if (!"".equals(strWattage)) {
                    if (!strWattage.matches("[0]+")) {
                        applianceSymbolChangeReq();
                    } else {
                        if (!strWattage.matches(".*0.*0.*")) {
                            applianceSymbolChangeReq();

                        } else {
                            etWattage.setError("Wattage shouldn't contain more than one 0");
                            //Utils.showMessageDialog("Wattage shouldn't contain more than one 0", AddDeviceActivityMqtt.this);
                        }
                    }
                } else {
                    etWattage.setError("Wattage required");
                }
                break;
            case R.id.btnControlsClear:
                etWattage.setText("");
                sprApplianceType.setSelection(0);
                sprApplianceSymbol.setSelection(0);
                break;
            case R.id.btnOkPair:
                pairReceiverNo = etPairDevice.getText().toString();
                if (pairReceiverNo.length() != 5) {
                    Utils.showMessageDialog("Please enter 5-digit serial number", this);
                } else if (pairReceiverNo.equalsIgnoreCase("00000") || Integer.parseInt(pairReceiverNo) > 65536) {
                    Utils.showMessageDialog("Enter proper serial number", this);
                } else {
                    /*pairLayout.setVisibility(View.GONE);
                    pairLayoutResubmit.setVisibility(View.VISIBLE);
                    etPairDeviceResubmit.requestFocus();*/
                    if (Utils.isNetworkAvailable) {
                        new SendMqttMsgTask().execute("SetPairReceiverNumberTask");
                        //new SetPairReceiverNumberTask().execute();
                    } else {
                        Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
                    }
                }
                break;
            case R.id.btnOkPairReSubmit:
                /*isbtnOkPairReSubmit = true;
                pairReceiverNo = etPairDeviceResubmit.getText().toString();
                if (!etPairDevice.getText().toString().trim().equalsIgnoreCase(pairReceiverNo)) {
                    etPairDeviceResubmit.setError("Enter the same serial number");
                } else if (pairReceiverNo.length() != 5) {
                    Utils.showMessageDialog("Please enter 5-digit serial number", this);
                } else if (pairReceiverNo.equalsIgnoreCase("00000") || Integer.parseInt(pairReceiverNo) > 65536) {
                    Utils.showMessageDialog("Enter proper serial number", this);
                } else {
                    if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this)) {
                        new SendMqttMsgTask().execute("SetPairReceiverNumberTask");
                        //new SetPairReceiverNumberTask().execute();
                    } else {
                        Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
                    }
                    //showMessageDialog("Receiver paired Successfully", AddDeviceActivityMqtt.this);
                }*/
                break;
            case R.id.btnWattageClear:
                etWattage.setText("");
                break;
            case R.id.btnWattageSave:
//                strWattage = etWattage.getText().toString().trim();
//                if (strWattage.length() == 0) {
//                    Utils.showMessageDialog("Select Wattage", this);
//                }else{
//                    // wattage service request
//                    if(Utils.isNetworkAvailable(this)) {
//                        setPowerRatingsRequest(strWattage);
//                    }else{
//                        Utils.showMessageDialog("No Internet.", this);
//                    }
//                }
                break;
            case R.id.btn1:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn1));
                popupMenu.show();*/
                displayPopupWindow(btn1);
                break;
            case R.id.btn2:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn2));
                popupMenu.show();*/
                displayPopupWindow(btn2);
                break;
            case R.id.btn3:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn3));
                popupMenu.show();*/
                displayPopupWindow(btn3);
                break;
            case R.id.btn4:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn4));
                popupMenu.show();*/
                displayPopupWindow(btn4);
                break;
            case R.id.btn5:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn5));
                popupMenu.show();*/
                displayPopupWindow(btn5);
                break;
            case R.id.btn6:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn6));
                popupMenu.show();*/
                displayPopupWindow(btn6);
                break;
            case R.id.btn7:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn7));
                popupMenu.show();*/
                displayPopupWindow(btn7);
                break;
            case R.id.btn8:
                /*popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener(btn8));
                popupMenu.show();*/
                displayPopupWindow(btn8);
                break;
            case R.id.btnMultiPairSave:
                //Service call
                String modeValues = btn1Mode + btn2Mode + btn3Mode + btn4Mode +
                        btn5Mode + btn6Mode + btn7Mode + btn8Mode;
                ModeValuesExt = modeValues;
                if (modeValues.equals("00000000") || modeValues.equals("000000")) {
                    Utils.showMessageDialog("Select mode to apply", AddDeviceActivityMqtt.this);
                } else {
                    if (Utils.isNetworkAvailable) {
                        //new SetModeMTask().execute(modeValues);
                        new SendMqttMsgTask().execute("SetModeMTask", modeValues);
                    } else {
                        Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
                    }
                }
                break;
        }
    }

    void applianceSymbolChangeReq() {
        if (sprApplianceType.getSelectedItemPosition() == 0) {
            Utils.showMessageDialog("Select Appliance Number", this);
        } else if (sprApplianceSymbol.getSelectedItemPosition() == 0) {
            Utils.showMessageDialog("Select Appliance Symbol", this);
        } else {
            if (sprApplianceDimmimg.getVisibility() == View.VISIBLE && sprApplianceDimmimg.getSelectedItemPosition() == 0) {
                Utils.showMessageDialog("Select Dimmer", this);
            } else {
                //Service call
                if (Utils.isNetworkAvailable) {
                    //new SetRelayTypeMTask().execute(strWattage.length() == 0 ? "0" : strWattage);
                    new SendMqttMsgTask().execute("SetRelayTypeMTask");
                    //makeReceiverStatusCRequest(newRoomIdFromServer);
                } else {
                    Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
                }
            }
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_go_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.e(TAG, "getPreviousData:-:" + getPreviousData());
        if (getPreviousData())
            menu.findItem(R.id.action_home).setVisible(false);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            /*case R.id.action_home:
                onBackPressed();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMessageDialog(String message, Context context) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                //getRoomsInfo();

                /*timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getRoomsInfo();
                    }
                }, 1000, 1000);*/

                if (Utils.isNetworkAvailable) {
                    new GetRoomsInfoTask().execute();
                } else {
                    Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
                }
            }
        });
        alertBox.show();
    }

    private void showingRoomAssigned(String newRoomName, String newRoomId, String relays) {
        if (newRoomName.contains("~")) {
            newRoomName.replaceAll("~", " ");
        }
        etAssignName.setText(newRoomName);
        tvTitleRoom.setText(newRoomName);
        pairLayoutResubmit.setVisibility(View.GONE);
        if (relays.equalsIgnoreCase("8")) {
            layout8Relay.setVisibility(View.VISIBLE);
            btn7Mode = "0";
            btn8Mode = "0";
        } else {
            layout8Relay.setVisibility(View.GONE);
            btn7Mode = "";
            btn8Mode = "";
        }
        applianceLayout.setVisibility(View.VISIBLE);
        timer.cancel();

        /*if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this)) {
            new GetModeCTask().execute();
        } else {
            Utils.showMessageDialog("No Internet.", AddDeviceActivityMqtt.this);
        }*/
    }

    /**
     * Calling service for make request for receiverstatusC after setting the controls for the room
     */
    /*private void makeReceiverStatusCRequest(String roomId) {
        Utils.showProgressDialog(AddDeviceActivityMqtt.this);
        String url = ServiceHandler.baseUrl + "GetReceiversStatusC?MacId=" + Utils.MACID + "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomId;
        StringRequest strRequest = new StringRequest(Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onMqttResponse(String response) {
                Utils.hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Add Device", "error in receiver status C : " + error.getMessage());
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }*/
    private void parseMqttResponse(String mqttMsg) {
        if (mqttMsg.contains("Notification:")) {
            Log.e(TAG, "strBodyN:-:" + mqttMsg);
            String notiStrArr[] = mqttMsg.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "1":
                    showConfirmationDialogExt("Already Paired", "Already Paired!", "", AddDeviceActivityMqtt.this);
                    break;
                case "4":
                    showConfirmationDialogExt("Pairing Failed", "Pairing Failed " + getEmojiByUnicode(0x1F61E), "", AddDeviceActivityMqtt.this);
                    break;
                case "8":
                    nameLayout.setVisibility(View.GONE);
                    btnAssignName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    isAssignNameClicked = false;
                    /*if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this))--new*/
                {
                        /*if (!message.contains("?"))*/
                        /*{
                            setHttpCallExt("Room Name Changed Successfully");
                        }*/
                    if (getPreviousData()) {
                        showConfirmationDialogExt("Room Name Changed Successfully", "Successful", mqttMsg, AddDeviceActivityMqtt.this);
                    } else {
                        showConfirmationDialogExt("Room Name Changed Successfully", "Successfully Updated", mqttMsg, AddDeviceActivityMqtt.this);
                    }
                    tvTitleRoom.setText(pairedRoomNameStr);
                } /*else {
                        Utils.showMessageDialog("No Internet", AddDeviceActivityMqtt.this);
                    }--new*/
                break;
                case "9":
                    if (getPreviousData()) {
                        //Utils.showMessageDialog("Room Name Changing Failed", AddDeviceActivityMqtt.this);
                        Utils.showMessageDialog("Please Retry", AddDeviceActivityMqtt.this);
                    } else {
                        Utils.showMessageDialog("Update Failed", AddDeviceActivityMqtt.this);
                    }
                    break;
                case "12":
                    llControls.setVisibility(View.GONE);
                    btnAppliances.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    isControlsClicked = false;
                    /*if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this)) --new*/
                {
                    //setHttpCallExt("Appliance Pairing Success");
                    showConfirmationDialogExt("Appliance Pairing Success", "Successfully Updated", mqttMsg, AddDeviceActivityMqtt.this);
                } /*else {
                        Utils.showMessageDialog("No Internet", AddDeviceActivityMqtt.this);
                    }--new*/
                break;
                case "13":
                    //Utils.showMessageDialog("Appliance Pairing Failed", AddDeviceActivityMqtt.this);
                    Utils.showMessageDialog("Update Failed", AddDeviceActivityMqtt.this);
                    break;
                case "10":
                    llMultipair.setVisibility(View.GONE);
                    btnMultipair.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    isMultipairClicked = false;
                    /*if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this))--new*/
                {
                    //setHttpCallExt("Modes Set Successfully");
                    showConfirmationDialogExt("Modes Set Successfully", "Successfully Updated", mqttMsg, AddDeviceActivityMqtt.this);
                } /*else {
                        Utils.showMessageDialog("No Internet", AddDeviceActivityMqtt.this);
                    }--new*/
                break;
                case "11":
                    //Utils.showMessageDialog("Modes Set Failed", AddDeviceActivityMqtt.this);
                    Utils.showMessageDialog("Update Failed", AddDeviceActivityMqtt.this);
                    break;
                default:
                    Utils.showMessageDialog("Something went wrong, Please try again!", AddDeviceActivityMqtt.this);
                    break;
            }

        } else if (mqttMsg.contains("Receiver:")) {
            //Receiver:{2;6;venkat}
            Log.e(TAG, "strBodyRe:-:" + mqttMsg);
            String receiverStrArr[] = mqttMsg.split(Pattern.quote("{"));
            String msgKey = receiverStrArr[0];
            msgKey = msgKey.replace(":", "");
            String msgValue = receiverStrArr[1];
            String msgValueArr[] = msgValue.split(Pattern.quote(";"));
            String roomIdStr = msgValueArr[0];//{2
            roomIdStr = roomIdStr.contains("{") ? roomIdStr.replace("{", "") : roomIdStr;
            PairedRoomIdStr = roomIdStr;
            PairedNumRelaysStr = msgValueArr[1];
            String roomName = msgValueArr[2];//venkat}
            roomName = roomName.contains("}") ? roomName.replace("}", "") : roomName;
            roomName = roomName.replaceAll(" ","~");
            pairedRoomNameStr = roomName;
            tvTitleRoom.setText(pairedRoomNameStr);
            /*if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this))--new*/
            {
                // setHttpCallExt("Room Paired Successfully");
                try {
                    showConfirmationDialogExt("Room Paired Successfully", "Pairing Success " + getEmojiByUnicode(0x1F60A), mqttMsg, AddDeviceActivityMqtt.this);
                    setRoomNamesExt(PairedRoomIdStr, pairedRoomNameStr);
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
            }/* else {
                Utils.showMessageDialog("No Internet", AddDeviceActivityMqtt.this);
            }--new*/

        } /*else if (stringBody.contains("RoomsInfo:")) {
            //RoomsInfo:{1;Embedded}{2;TEST}{3;IT 2}
            Log.e(TAG, "strBodyRI:-:" + stringBody);

            String mStrResult = "";
            RoomsInfoMqttModel roomsInfoMqttModel;
            try {
                //RoomsInfo:{1;Embedded}{2;TEST}
                String[] arrS = stringBody.split(Pattern.quote("{"));
                for (int i = 0; i < arrS.length; i++) {
                    roomsInfoMqttModel = new RoomsInfoMqttModel();

                    if (i == 0) {
                        String str = arrS[i];
                        mStrResult = str.replace(":", "").trim();
                        continue;
                    }

                    if (mStrResult.equals("RoomsInfo")) {
                        String rooms = arrS[i].contains("}") ? arrS[i].replace("}", "") : arrS[i];
                        //rooms = rooms.replace(";", "");
                        //Log.e(TAG, "rooms:-:" + rooms);
                        String arrS1[] = rooms.split(";");
                        String roomId = arrS1[0];
                        String roomName = arrS1[1];
                        roomsInfoMqttModel.setRoomId(roomId);
                        roomsInfoMqttModel.setRoomName(roomName);
                        *//*if (roomsInfoMqttModelArr != null && roomsInfoMqttModelArr.size() > 0){
                            for (int j = 0; j <roomsInfoMqttModelArr.size() ; j++) {
                                if (roomsInfoMqttModelArr.get(j).getRoomId().equals(roomId)){
                                    roomsInfoMqttModel.setRoomId(roomId);
                                    roomsInfoMqttModel.setRoomName(roomName);
                                }
                            }
                        }else {
                            roomsInfoMqttModel.setRoomId(roomId);
                            roomsInfoMqttModel.setRoomName(roomName);
                        }*//*
                        roomsInfoMqttModelArr.add(roomsInfoMqttModel);
                    }
                }
                for (int i = 0; i < roomsInfoMqttModelArr.size(); i++) {

                    if (!stringBody.contains("?")) {
                        String roomId = roomsInfoMqttModelArr.get(i).getRoomId();
                        String newRoomName = roomsInfoMqttModelArr.get(i).getRoomName();
                        newRoomName = newRoomName.replaceAll(" ", "~");
                        if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this)) {
                            setRoomNamesExt(roomId, newRoomName);
                        }
                    }
                    if (pairedRoomNameStr.equals(roomsInfoMqttModelArr.get(i).getRoomName())) {
                        etAssignName.setText(roomsInfoMqttModelArr.get(i).getRoomName().replaceAll("~", " "));
                    }
                }

                tvTitleRoom.setText(newRoomName);
                pairLayoutResubmit.setVisibility(View.GONE);
                applianceLayout.setVisibility(View.VISIBLE);

                *//*Intent intent = new Intent(Intent.ACTION_SYNC, null, AddDeviceActivityMqtt.this, MqttConnectionManagerService.class);
                intent.putExtra("receiver", mReceiver);
                intent.putExtra("msg", "C|" + PairedRoomIdStr);
                startService(intent);*//*


                *//*if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this)) {
                    try {
                        if (!stringBody.contains("?")) {
                            Log.e(TAG, "roomsInfoMqttModelArr size:-:" + roomsInfoMqttModelArr.size());
                            for (int j = 0; j < roomsInfoMqttModelArr.size(); j++) {
                                String roomId = roomsInfoMqttModelArr.get(j).getRoomId();
                                String newRoomName = roomsInfoMqttModelArr.get(j).getRoomName();
                                newRoomName = newRoomName.replace(" ", "~");


                                Log.e(TAG, "RumNmw:-:" + roomsInfoMqttModelArr.get(j).getRoomName() + "j " + j);
                                String url = ServiceHandler.baseUrl + "SetRoomNameMExt?MacId=" + Utils.MACID +
                                        "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomId + "&RoomName=" + newRoomName;
                                Log.e(TAG, "SetReceiversStatusExt:-:" + url);
                                StringRequest strRequest = new StringRequest(Method.GET, url, new Response.Listener<String>() {
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
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception:-:" + e.getMessage());
                    }
                }*//*

                helper.sendMsg("C|" + PairedRoomIdStr);

            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }

        }*/ /*else if (mqttMsg.contains("ReceiverStatus:")) {
            //{ReceiverStatus:1;1;0B10B00B10B00B10B00B10B0}
            Log.e(TAG, "strBodyRS:-:" + mqttMsg);
            String[] arrS = mqttMsg.split(Pattern.quote("{"));
            //mqttRoomStatusModel = new MqttRoomStatusModel();
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < arrS.length; i++) {

                String mStrResult = "";
                if (i == 0) {
                    String str = arrS[i];
                    mStrResult = str.replace(":", "").trim();
                    continue;
                }
               *//* if (mStrResult.equals("ReceiverStatus")) *//*
                {
                    String rooms = arrS[i].contains("}") ? arrS[i].replace("}", "") : arrS[i];
                    String[] strInfo = rooms.split(Pattern.quote(";"));
                    mqttRoomStatusModel.setRoomId(Integer.parseInt(strInfo[0]));
                    mqttRoomStatusModel.setAutoRevoke(strInfo[1]);
                    strBuilder.append(mqttRoomStatusModel.getRoomId()).append(";");
                    strBuilder.append(mqttRoomStatusModel.getAutoRevoke());
                    ArrayList<String> arr = new ArrayList<>();
                    int index = 0;
                    Log.e(TAG, "strInfo[2]:-:" + strInfo[2]);
                    while (index < strInfo[2].length()) {
                        String str = strInfo[2].substring(index, Math.min(index + 3, strInfo[2].length()));
                        arr.add(str);
                        strBuilder.append(str);
                        index += 3;
                        Log.e(TAG, "arr:-:" + arr);
                    }
                    mqttRoomStatusModel.setAppDimTypeStatusArrLst(arr);
                    for (String arrS1 : mqttRoomStatusModel.getAppDimTypeStatusArrLst()) {
                        Log.e(TAG, "arrS1:-:" + arrS1);
                    }

                }
            }

            if (Utils.isNetworkAvailable(AddDeviceActivityMqtt.this)) {
                SetReceiversStatusExt(strBuilder.toString());
                strBuilder = new StringBuilder();
            }

            new GetPowerRatingsTask().execute(PairedRoomIdStr);
        }*/
    }

    private void SetReceiversStatusExt(String strBuilder) {
        String url = ServiceHandler.baseUrl + "SetReceiversStatusExt?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
                + "&Value=" + Utils.MAC_ID + ";" + strBuilder;
        Log.e(TAG, "SetReceiversStatusExt:-:" + url);
        StringRequest strRequest = new StringRequest(Method.GET, url, new Response.Listener<String>() {
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

    private void setRoomNamesExt(String roomId, String newRoomName) {
        newRoomName = newRoomName.replaceAll(" ","~");
        String url = ServiceHandler.baseUrl + "SetRoomNameMExt?MacId=" + Utils.MAC_ID +
                "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomId + "&RoomName=" + newRoomName;
        Log.e(TAG, "setRoomNamesExt:-:" + url);
        StringRequest strRequest = new StringRequest(Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                /*try {
                    Utils.hideProgressDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse:-:" + error.getMessage());
                /*try {
                    Utils.hideProgressDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    public void showConfirmationDialogExt(final String message, String s, final String mqttMsg, Context context) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(s);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (message) {
                    case "Already Paired":
                        pairLayoutResubmit.setVisibility(View.GONE);
                        pairLayout.setVisibility(View.VISIBLE);
                        break;
                    case "Pairing Failed":
                        pairLayoutResubmit.setVisibility(View.GONE);
                        pairLayout.setVisibility(View.VISIBLE);
                        break;
                    case "Room Paired Successfully":
                        pairLayoutResubmit.setVisibility(View.GONE);
                        pairLayout.setVisibility(View.GONE);
                        applianceLayout.setVisibility(View.VISIBLE);
                        if (Utils.isNetworkAvailable && !mqttMsg.contains("?")) {
                            setHttpCallExt("Room Paired Successfully");
                        }
                        //helper.sendMsg("H");
                        break;
                    case "Room Name Changed Successfully":
                        if (Utils.isNetworkAvailable && !mqttMsg.contains("?")) {
                            setHttpCallExt("Room Name Changed Successfully");
                        }
                        break;
                    case "Appliance Pairing Success":
                        if (Utils.isNetworkAvailable && !mqttMsg.contains("?")) {
                            setHttpCallExt("Appliance Pairing Success");
                        }
                        break;
                    case "Modes Set Successfully":
                        if (Utils.isNetworkAvailable && !mqttMsg.contains("?")) {
                            setHttpCallExt("Modes Set Successfully");
                        }
                        break;
                }
            }
        });
        alertBox.show();
    }

    @Override
    public void onMqttResponse(String res) {
        parseMqttResponse(res);
        /*if (null != pd && pd.isShowing()) {
            pd.dismiss();
        }*/
        //Toast.makeText(AddDeviceActivityMqtt.this, stringBody, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "stringBody:-:" + res);
    }

    @Override
    public void onMqttFailure() {
        /*if (null != pd && pd.isShowing()) {
            pd.dismiss();
        }*/
    }

    @Override
    public void onDeliveryComplete() {
        /*if (null != pd && pd.isShowing()) {
            pd.dismiss();
        }*/
    }

    private void setDataToSymbolSpinner(int i) {
        ArrayAdapter<CharSequence> adapterSymbols;

        if (i < 3) {
            adapterSymbols = ArrayAdapter.createFromResource(AddDeviceActivityMqtt.this, R.array.array_appliance_fan_light, android.R.layout.simple_spinner_dropdown_item);

        } else {
            adapterSymbols = ArrayAdapter.createFromResource(AddDeviceActivityMqtt.this, R.array.array_appliance_typessymbols, android.R.layout.simple_spinner_dropdown_item);

        }

        sprApplianceSymbol.setSelection(0);
        adapterSymbols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprApplianceSymbol.setAdapter(adapterSymbols);
    }

    void setHttpCallExt(String msg) {

        try {
            String url = "";
            String roomId = "";
            if (!getPreviousData()) {
                roomId = newRoomIdFromServer;
            } else {
                roomId = PairedRoomIdStr;
            }
            switch (msg) {
                case "Room Paired Successfully":
                    if (roomId != null && !TextUtils.isEmpty(roomId)) {
                        if (isRoomPairClicked) {
                            isRoomPairClicked = false;
                            url = ServiceHandler.baseUrl + "SetReceiverNumberExt?MacId=" + Utils.MAC_ID +
                                    "&IMEI=" + Utils.IMEI + "&Number=" + roomId;
                        }
                    } else {
                        return;
                    }
                    break;
                case "Room Name Changed Successfully":
                    url = ServiceHandler.baseUrl + "SetRoomNameMExt?MacId=" + Utils.MAC_ID +
                            "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomId + "&RoomName=" + newRoomName;
                    break;
                case "Appliance Pairing Success":
                    //String args = "MacId=" + Utils.MACID + "&IMEI=" + Utils.IMEI +
                    // "&RoomNumber=" + roomId + "&RelayNumber=" + relayNumber + "&Type=" + type + "&Wattage=" + wattage;
                    url = ServiceHandler.baseUrl + "SetRelayTypeMExt?MacId=" + Utils.MAC_ID +
                            "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomId + "&RelayNumber="
                            + applianceTypeSelected + "&Type=" + applianceSymbolNumber + "&Wattage=" + strWattage;
                    break;
                case "Modes Set Successfully":
                    url = ServiceHandler.baseUrl + "SetModeMExt?MacId=" + Utils.MAC_ID +
                            "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomId + "&ModeValue=" + ModeValuesExt;
                    break;
            }
            Log.e(TAG, "setHttpCallExtURL:-:" + url);
            StringRequest strRequest = new StringRequest(Method.GET, url, new Response.Listener<String>() {
                int code = 0;
                String message = "";

                @Override
                public void onResponse(String response) {
                    Log.e(TAG, "response:-:" + response);
                    //{"SetReceiverNumberExtResult":[{"Code":200,"Message":"Added Successfully"}]}
                    try {
                        if (response.contains("SetReceiverNumberExtResult")) {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("SetReceiverNumberExtResult");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                code = jsonObject1.getInt("Code");
                                message = jsonObject1.getString("Message");
                            }
                            /*if (code != 200) {
                                setHttpCallExt("Room Paired Successfully");
                            }*/
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException:-:" + e.getMessage());
                    }
                    //Utils.hideProgressDialog();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "onErrorResponse:-:" + error.getMessage());
                    //Utils.hideProgressDialog();
                }
            });

            AppController.getInstance().addToRequestQueue(strRequest);
        } catch (Exception e) {
            Log.e(TAG, "setHttpCallExt exep:-:" + e.getMessage());
        }
    }


    private class SendMqttMsgTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*if (pd != null && !pd.isShowing()) {
                pd.show();
            }*/
        }

        @Override
        protected String doInBackground(String... params) {
            String roomId = "";
            if (!getPreviousData()) {
                roomId = newRoomIdFromServer;
            } else {
                roomId = PairedRoomIdStr;
            }
            switch (params[0]) {
                case "SetPairReceiverNumberTask":
                    isRoomPairClicked = true;
                    helper.sendMsg("O|" + pairReceiverNo);
                    break;
                case "SetRoomNameTask":
                    helper.sendMsg("D|" + roomId + "|" + params[1]);
                    break;
                case "SetRelayTypeMTask":
                    if (!(applianceTypeSelected.equals("1") || applianceTypeSelected.equals("2"))) {
                        helper.sendMsg("G|" + roomId + "|" + applianceTypeSelected + applianceSymbolNumber);
                    } else {
                        helper.sendMsg("G|" + roomId + "|" + applianceTypeSelected + applianceSymbolNumber
                                + applianceDimmerSelected);
                    }
                    break;
                case "SetModeMTask":
                    helper.sendMsg("E|" + roomId + "|" + params[1]);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            /*if (null != pd && pd.isShowing()) {
                pd.dismiss();
            }*/
        }
    }

    //Getting Rooms info from server AsyncTask
    private class GetRoomsInfoTask extends AsyncTask<Void, Void, String> {
        //private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress = ProgressDialog.show(AddDeviceActivityMqtt.this, null, "Loading...");
            /*if (pd != null && !pd.isShowing()) {
                pd.show();
            }*/
        }

        @Override
        protected String doInBackground(Void... params) {
            RoomsProvider provider = new RoomsProvider(AddDeviceActivityMqtt.this);
            allRoomsData = provider.getRoomsFromServer();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progress.dismiss();
            /*if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }*/
            if (allRoomsData.size() != 0) {
                for (int i = 0; i < allRoomsData.size(); i++) {
                    newRoomFromServer = allRoomsData.get(i).getRoomName();
                    newRoomIdFromServer = allRoomsData.get(i).getRoomId();
                    newRelaysFromServer = allRoomsData.get(i).getNumOfRelays();
                }
            }

            showingRoomAssigned(newRoomFromServer, newRoomIdFromServer, newRelaysFromServer);
        }
    }

    private class GetPowerRatingsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            RoomsProvider provider = new RoomsProvider(AddDeviceActivityMqtt.this);
            applianceWattModel = provider.getPowerRatings(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //progress.dismiss();
            if (mqttRoomStatusModel != null) {
                //creatingRoomsAndSetData(data18, dataStatus);

                sprApplianceType.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        isSpinnerTouched = true;
                        return false;
                    }
                });

                sprApplianceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (isSpinnerTouched && i > 0) {
                            applianceTypeSelected = sprApplianceType.getSelectedItem().toString();
                            String strSymbl = "";
                            sprApplianceDimmimg.setVisibility(View.GONE);
                            if (!getPreviousData())
                                strSymbl = dataStatus.get(0).getApplianceTypeArrLst().get(i - 1);
                            else
                                strSymbl = String.valueOf(mqttRoomStatusModel.getAppDimTypeStatusArrLst().get(i - 1).charAt(1));

                            Log.e(TAG, "mqttRoomStatusModel:-:" + mqttRoomStatusModel);

                            spinTypePos = i;

                            //setDataToSymbolSpinner(i);

                            if (strSymbl.equalsIgnoreCase("A")) {
                                applianceSymbolNumber = "Fan";
                            } else if (strSymbl.equalsIgnoreCase("B")) {
                                applianceSymbolNumber = "Light";
                            } else if (strSymbl.equalsIgnoreCase("C")) {
                                applianceSymbolNumber = "TV";
                            } else if (strSymbl.equalsIgnoreCase("D")) {
                                applianceSymbolNumber = "AC";
                            } else if (strSymbl.equalsIgnoreCase("E")) {
                                applianceSymbolNumber = "Plug";
                            } else if (strSymbl.equalsIgnoreCase("F")) {
                                applianceSymbolNumber = "LED";
                            } else if (strSymbl.equalsIgnoreCase("G")) {
                                applianceSymbolNumber = "Washing-machine";
                            } else if (strSymbl.equalsIgnoreCase("H")) {
                                applianceSymbolNumber = "MP";
                            } else if (strSymbl.equalsIgnoreCase("I")) {
                                applianceSymbolNumber = "Geyser";
                            } else if (strSymbl.equalsIgnoreCase("J")) {
                                applianceSymbolNumber = "DVD";
                            } else if (strSymbl.equalsIgnoreCase("K")) {
                                applianceSymbolNumber = "Home Theater";
                            } else if (strSymbl.equalsIgnoreCase("L")) {
                                applianceSymbolNumber = "Projector";
                            } else if (strSymbl.equalsIgnoreCase("@")) {
                                applianceSymbolNumber = "Dummy";
                                etWattage.setVisibility(View.GONE);
                            }

                            boolean isEntered = false;
                            String[] sArr = getResources().getStringArray(R.array.array_appliance_typessymbols);

                            for (int j = 0; j < sArr.length; j++) {
                                if (sArr[j].equals(applianceSymbolNumber) && !isEntered) {
                                    sprApplianceSymbol.setSelection(j);
                                    if (applianceWattModel.size() > 0) {
                                        String strWattage = String.valueOf(applianceWattModel.get(0).getWattagesArr().get(i - 1));
                                        Log.e(TAG, "strWattage:-:" + strWattage + "i:-:" + (i - 1));
                                        etWattage.setText(strWattage);
                                        isEntered = true;
                                    } else {
                                        etWattage.setText("0");
                                    }
                                }
                            }
                        }
//                Toast.makeText(AddDeviceActivityMqtt.this, ""+applianceTypeSelected, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                sprApplianceSymbol.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        isSpinnerTouched = true;
                        return false;
                    }
                });

                sprApplianceSymbol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (isSpinnerTouched) {
                            applianceSymbolSelected = sprApplianceSymbol.getSelectedItem().toString().trim();

                            spinSymbolLf = applianceSymbolSelected;

                            if ((spinTypePos == 1 || spinTypePos == 2) && (spinSymbolLf.equalsIgnoreCase("Fan") || spinSymbolLf.equalsIgnoreCase("Light"))) {
                                sprApplianceDimmimg.setVisibility(View.VISIBLE);
                            } else {
                                sprApplianceDimmimg.setVisibility(View.GONE);
                            }

                            if (applianceSymbolSelected.equalsIgnoreCase("Fan")) {
                                applianceSymbolNumber = "A";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Light")) {
                                applianceSymbolNumber = "B";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("TV")) {
                                applianceSymbolNumber = "C";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("AC")) {
                                applianceSymbolNumber = "D";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Plug")) {
                                applianceSymbolNumber = "E";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("LED")) {
                                applianceSymbolNumber = "F";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Washing-machine")) {
                                applianceSymbolNumber = "G";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("MP")) {
                                applianceSymbolNumber = "H";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Geyser")) {
                                applianceSymbolNumber = "I";
                                etWattage.setVisibility(View.VISIBLE);
                            } else if (applianceSymbolSelected.equalsIgnoreCase("DVD")) {
                                applianceSymbolNumber = "J";
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Home Theater")) {
                                applianceSymbolNumber = "K";
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Projector")) {
                                applianceSymbolNumber = "L";
                            } else if (applianceSymbolSelected.equalsIgnoreCase("Dummy")) {
                                applianceSymbolNumber = "@";
                                strWattage = "0";
                                etWattage.setVisibility(View.GONE);
                            }
//                Toast.makeText(AddDeviceActivityMqtt.this, ""+applianceSymbolSelected, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                sprApplianceDimmimg.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        isSpinnerTouched = true;
                        return false;
                    }
                });

                sprApplianceDimmimg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (isSpinnerTouched && i > 0) {
                            //applianceDimmerSelected = sprApplianceDimmimg.getSelectedItem().toString().trim();
                            if (i == 1) {
                                applianceDimmerSelected = "1";
                            } else {
                                applianceDimmerSelected = "0";
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        }
    }

    //Getting Room status from server AsyncTask
    private class GetRoomStatusTask extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress = ProgressDialog.show(mActivity, null, "Loading...");
            /*if (pd != null && !pd.isShowing()) {
                pd.show();
            }*/
        }

        @Override
        protected String doInBackground(String... params) {
            RoomsProvider provider = new RoomsProvider(AddDeviceActivityMqtt.this);
            dataStatus = provider.getRoomStatusFromServer(params[0], 0);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new GetPowerRatingsTask().execute(newRoomIdFromServer);
            /*if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }*/
        }
    }
}
