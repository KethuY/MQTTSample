package com.atg.onecontrolv3.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.OpenedSensorsAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.ConnectedSensorsModel;
import com.atg.onecontrolv3.models.OpenedSensorsModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by Bharath on 05-Aug-17
 */

public class SafetyFragNew extends Fragment implements View.OnClickListener, MqttHelper.responseListener {

    private static final String TAG = "SafetyFragNew";
    TextView mSystemArmedSym, mSystemArmedTv, mArmTv, mDisArmTv;
    Typeface typeface;
    MqttHelper.responseListener mqttListener = null;
    AlertDialog alertDialog;
    private MqttHelper helper;
    private ArrayList<OpenedSensorsModel> mOpenedSensorsArrLst;
    private RecyclerView mOpenedSensorsRv;
    private Button mOkBtn;
    private LinearLayout mOpenedSenorsLl, mArmDisarmLl;
    private String[] arrS2;
    private boolean isArmBtnClicked;
    private TextView mtitleTv;
    private ArrayList<ConnectedSensorsModel> mConnectedSensorsArrLst;
    private boolean isAlertShowing = false;
    private TextView msgTv;
    private String showingMsg;

    private boolean isViewShown = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!isViewShown) {
            if (Utils.isNetworkAvailable) {
                getConnectedSensorsList();
                if (helper != null) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            helper.sendMsg("U");
                        }
                    }, 3000);
                }
            }
        }
        return inflater.inflate(R.layout.safety, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            isViewShown = true;
            if (Utils.isNetworkAvailable) {
                getConnectedSensorsList();
                if (helper != null) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            helper.sendMsg("U");
                        }
                    }, 3000);
                }
            }
        } else {
            isViewShown = false;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOpenedSensorsRv = (RecyclerView) getActivity().findViewById(R.id.opened_sensors_rv);
        mOpenedSensorsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mOpenedSenorsLl = (LinearLayout) getActivity().findViewById(R.id.opened_sensors_ll);
        mArmDisarmLl = (LinearLayout) getActivity().findViewById(R.id.arm_disarm_ll);
        mOkBtn = (Button) getActivity().findViewById(R.id.open_ok_btn);
        mtitleTv = (TextView) getActivity().findViewById(R.id.title);

        mqttListener = this;
        if (Utils.isNetworkAvailable) {
            helper = new MqttHelper(getContext(), mqttListener);
        }

        mSystemArmedSym = (TextView) getActivity().findViewById(R.id.system_armed_sym);
        mSystemArmedTv = (TextView) getActivity().findViewById(R.id.system_armed_tv);
        mArmTv = (TextView) getActivity().findViewById(R.id.arm_tv);
        mDisArmTv = (TextView) getActivity().findViewById(R.id.disarm_tv);

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "uifont.ttf");
        mSystemArmedSym.setTypeface(typeface);
        mArmTv.setTypeface(typeface);
        mDisArmTv.setTypeface(typeface);

        mArmTv.setOnClickListener(this);
        mDisArmTv.setOnClickListener(this);
        mOkBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "Entered onResume");
        if (Utils.isNetworkAvailable) {
            getConnectedSensorsList();
            if (helper != null) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        helper.sendMsg("U");
                    }
                }, 3000);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.arm_tv:
                if (Utils.isNetworkAvailable) {
                    isArmBtnClicked = true;
                    Log.e(TAG, "isArmBtnClicked:-:" + isArmBtnClicked);
                    helper.sendMsg("f|0|A");
                }
                break;
            case R.id.disarm_tv:
                if (Utils.isNetworkAvailable) {
                    helper.sendMsg("f|0|D");
                }
                break;
            case R.id.open_ok_btn:
                if (helper != null) {
                    if (Utils.isNetworkAvailable) {
                        helper.sendMsg("U");
                    }
                }
                mOpenedSenorsLl.setVisibility(View.GONE);
                mArmDisarmLl.setVisibility(View.VISIBLE);
                mtitleTv.setText("Safety");
                break;
        }
    }

    @Override
    public void onMqttResponse(String res) {
        //A|1 - ARM successful
        //D|1 - DISARM successful
        Log.e(TAG, "MqttResStr:-:" + res + isArmBtnClicked);
        if (res.contains("|E130")) {
            //A|9;10|E130
            String arrS[] = res.split(Pattern.quote("|"));
            String openSensorsStr = arrS[1];
            //Log.e(TAG, "arrS:-:" + Arrays.toString(arrS));
            arrS2 = openSensorsStr.split(Pattern.quote(";"));
            Log.e(TAG, "arrS2:-:" + Arrays.toString(arrS2));
            if (Utils.isNetworkAvailable) {
                getConnectedSensorsList();
            }
            isArmBtnClicked = false;
        } else if (res.contains("E130")) {
            String arrS2[] = res.split(Pattern.quote(";"));
            Log.e(TAG, "arrS2[1]:-:" + arrS2[1]);
            showAlertPopUp(arrS2[1]);
        } else {
            switch (res) {
                case "A|1":
                    mSystemArmedSym.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    mSystemArmedTv.setText("SYSTEM ARMED");
                    break;
                case "D|1":
                    mSystemArmedSym.setTextColor(ContextCompat.getColor(getContext(), R.color.green_thick));
                    mSystemArmedTv.setText("SYSTEM DISARMED");
                    break;
                case "1":
                    Utils.showMessageDialog("Already Paired", getContext());
                    break;
            }
        }
    }

    private void showAlertPopUp(String sensorNo) {
        if (mConnectedSensorsArrLst != null && !mConnectedSensorsArrLst.isEmpty()) {
            for (int i = 0; i < mConnectedSensorsArrLst.size(); i++) {
                if (mConnectedSensorsArrLst.get(i).getSensorNo() == Integer.parseInt(sensorNo)) {
                    String location = mConnectedSensorsArrLst.get(i).getLocation();
                    String zone = mConnectedSensorsArrLst.get(i).getZone();
                    int type = mConnectedSensorsArrLst.get(i).getSensorTypeId();
                    String msg = "";
                    switch (type) {
                        case 1:
                            msg = "Curtain break detected at";
                            break;
                        case 2:
                            msg = "Gas leak detected at ";
                            break;
                        case 3:
                            msg = "Glass break detected at ";
                            break;
                        case 4:
                            msg = "Intruder detected at ";
                            break;
                        case 5:
                            msg = "Panic detected at ";
                            break;
                        case 6:
                            msg = "Motion detected at ";
                            break;
                        case 7:
                            msg = "Smoke detected at ";
                            break;
                        case 8:
                            msg = "Vibration detected at ";
                            break;
                    }
                    msg += location + " - " + zone;
                    if (!isAlertShowing) {
                        Log.e(TAG, "isAl:-:" + isAlertShowing);
                        displayAlertDialog(msg);
                    } else {
                        if (alertDialog != null) {
                            alertDialog.dismiss();
                        }
                        //if (null != showingMsg && !showingMsg.equals(msg)) {
                        msgTv.setText(msg);
                        alertDialog.show();
                        showingMsg = msg;
                        // }
                    }
                }
            }
        }
    }

    private void displayAlertDialog(String msg) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.security_alert_dialog, null);
        dialogBuilder.setView(dialogView);

        msgTv = (TextView) dialogView.findViewById(R.id.msg_tv);
        Button okBtn = (Button) dialogView.findViewById(R.id.ok_btn);
        alertDialog = dialogBuilder.create();
        /*WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        alertDialog.getWindow().setGravity(Gravity.START|Gravity.END);
        layoutParams.x = 100;
        layoutParams.y = 100;*/
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        msgTv.setText(msg);
        alertDialog.show();
        isAlertShowing = alertDialog.isShowing();
        showingMsg = msg;
        Log.e(TAG, "isAl1234:-:" + isAlertShowing + " msg:-:1234" + msg + " showingMsg1234:-:" + showingMsg);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isAlertShowing = alertDialog.isShowing();
            }
        });
    }


    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.e(TAG, "Entered is visible");
            if (helper != null) {
                Log.e(TAG, "Entered helper");
                helper.sendMsg("U");
            } else {
                Log.e(TAG, "Entered not");
            }
        }
    }*/

    private void setAdapter() {
        mArmDisarmLl.setVisibility(View.GONE);
        mOpenedSenorsLl.setVisibility(View.VISIBLE);
        mtitleTv.setText("Doors Left Open");
        OpenedSensorsAdapter adapter = new OpenedSensorsAdapter(getContext(), mOpenedSensorsArrLst);
        mOpenedSensorsRv.setAdapter(adapter);
    }

    private void getConnectedSensorsList() {
        String strMethodName = "GetSensorDetails";
        String strUrl = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "strUrl:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                parseJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error:-:" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("GetSensorDetailsResult");
            mConnectedSensorsArrLst = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                ConnectedSensorsModel model = new ConnectedSensorsModel();
                model.setId(jsonObject1.getInt("Id"));
                model.setIsWired(jsonObject1.getInt("IsWired"));
                String loc = jsonObject1.getString("Location");
                model.setLocation(loc.replaceAll("~", " "));
                model.setSensorNo(jsonObject1.getInt("SensorNo"));
                model.setSensorTypeId(jsonObject1.getInt("SensorTypeId"));
                String zone = jsonObject1.getString("Zone");
                model.setZone(zone.replaceAll("~", " "));

                switch (model.getSensorTypeId()) {
                    case 1:
                        model.setSensorType("Curtain");
                        break;
                    case 2:
                        model.setSensorType("Gas");
                        break;
                    case 3:
                        model.setSensorType("Glass Break");
                        break;
                    case 4:
                        model.setSensorType("Magnetic");
                        break;
                    case 5:
                        model.setSensorType("Panic");
                        break;
                    case 6:
                        model.setSensorType("PIR");
                        break;
                    case 7:
                        model.setSensorType("Smoke");
                        break;
                    case 8:
                        model.setSensorType("Vibration");
                        break;
                }

                mConnectedSensorsArrLst.add(model);
            }
            if (null != mConnectedSensorsArrLst && !mConnectedSensorsArrLst.isEmpty()) {
                mOpenedSensorsArrLst = new ArrayList<>();
                if (null != arrS2 && arrS2.length > 0) {
                    for (String s : arrS2) {
                        //Log.e(TAG, "s:-:" + s);
                        int i = Integer.parseInt(s);
                        for (int j = 0; j < mConnectedSensorsArrLst.size(); j++) {
                            //Log.e(TAG, "sensorNbr:-:" + mConnectedSensorsArrLst.get(j).getSensorNo() + " i:-:" + i);
                            if (mConnectedSensorsArrLst.get(j).getSensorNo() == i) {
                                OpenedSensorsModel model = new OpenedSensorsModel();
                                model.setLocation(mConnectedSensorsArrLst.get(j).getLocation());
                                model.setZone(mConnectedSensorsArrLst.get(j).getZone());
                                mOpenedSensorsArrLst.add(model);
                            }
                        }
                    }
                }
                //Log.e(TAG, "mOpenedSensorsArrLst:-:" + mOpenedSensorsArrLst.size());

                if (null != mOpenedSensorsArrLst && !mOpenedSensorsArrLst.isEmpty()) {
                    setAdapter();
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }
}
