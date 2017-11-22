package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.PIRApplianceAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.GetMacInfoModel;
import com.atg.onecontrolv3.models.PIRDBModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.atg.onecontrolv3.helpers.Utils.getPIRSObj;


public class PIRApplianceActivity extends BaseActivity implements OnItemClickListener, MqttHelper.responseListener {

    private static final String TAG = PIRApplianceActivity.class.getSimpleName();
    ListView mApplianceLv;
    OnItemClickListener mListener;
    MqttHelper.responseListener mqttListener = null;
    ArrayList<Integer> pirNumbersArrLst = new ArrayList<>();
    private TransparentProgressDialog pd;
    private ArrayList<GetMacInfoModel> getMacInfoArrLst;
    private PIRApplianceAdapter adapter;
    private MqttHelper helper;
    private int pirMqttNo = 1;
    private ArrayList<Integer> pirNoArrLst = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pirappliance2);

        initializeViews();
    }

    private void initializeViews() {
        mListener = this;
        pd = new TransparentProgressDialog(PIRApplianceActivity.this, R.drawable.progress);
        mApplianceLv = (ListView) findViewById(R.id.appliance_lv);
        setToolBar();

        mqttListener = this;
        try {
            if (Utils.isNetworkAvailable) {
                helper = new MqttHelper(PIRApplianceActivity.this, mqttListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }

        if (getIntent() != null) {
            pirNumbersArrLst = (ArrayList<Integer>) getIntent().getSerializableExtra("PIRNbrArrLst");

        }
    }


    private void setToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Select PIR Appliances");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            getMacInfo();
        } else {
            Utils.showMessageDialog("No Internet.", PIRApplianceActivity.this);
        }
    }

    private void getMacInfo() {
        pd.show();
        String finalUrl = ServiceHandler.baseUrl + "GetMacInfo?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "finalUrl:-:" + finalUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                parseJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRClientRequest()", "Error : " + error.getMessage());
                pd.dismiss();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("MacInfo");
            getMacInfoArrLst = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                int indCnt = 0;
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                GetMacInfoModel model = new GetMacInfoModel();
                model.setActiveCount(jsonObject1.getInt("ActiveCount"));
                model.setInfo(jsonObject1.getString("Info"));
                model.setNumOfRelays(jsonObject1.getInt("NumOfRelays"));
                model.setRoomId(jsonObject1.getInt("RoomId"));
                String roomName = jsonObject1.getString("RoomName");
                roomName = roomName.contains("~") ? roomName.replace("~", " ") : roomName;
                model.setRoomName(roomName);
                String appRevokeDimmTypeStatus = model.getInfo();
                String appRevokeDimmTypeStatusTrim = appRevokeDimmTypeStatus.substring(1);
                ArrayList<String> arr = new ArrayList<>();
                int index = 0;

                while (index < appRevokeDimmTypeStatusTrim.length()) {
                    String str = appRevokeDimmTypeStatusTrim.substring(index, Math.min(index + 3, appRevokeDimmTypeStatusTrim.length()));

                    char[] chars = str.toCharArray();
                    chars[2] = '0';
                    String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                    arr.add(str1);
                    index += 3;
                }

                model.setActiveListItemCnt(indCnt);
                model.setAutoRevoke(String.valueOf(appRevokeDimmTypeStatus.charAt(0)));
                model.setAppDimmTypeStatusArrLst(arr);
                getMacInfoArrLst.add(model);


            }
            if (getMacInfoArrLst != null && getMacInfoArrLst.size() > 0) {
                changeGetMacInfoArrLst();
                setAdapter();
            } else {
                Utils.showMessageDialog("Data not available", this);
            }
        } catch (Exception e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
            pd.dismiss();
        }
    }

    private void getPirsVolley() {
        pd.show();
        String strMethodName = "GetPIRExt";
        String strURL = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "strURL:-:" + strURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                parseJsonPIR(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Log.e(TAG, "volleyError:-:" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);

    }

    private void parseJsonPIR(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("GetPIRExtResult");
            for (int i = 0; i < jsonArray.length(); i++) {
                if (getPIRSObj.getPirNumber() == (i + 1)) {
                    Log.e(TAG, "PirNbr:-:" + (i + 1));
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    PIRDBModel pirdbModel = new PIRDBModel();
                    pirdbModel.setPirName(jsonObject1.getString("PIRName"));
                    pirdbModel.setPirNumber(jsonObject1.getInt("PIRNumber"));
                    pirdbModel.setTimerTime(jsonObject1.getInt("TimerTime"));
                    pirdbModel.setValue(jsonObject1.getString("value"));

                    //20006:1234;20007:1234

                    String valueStr = jsonObject1.getString("value");
                    String arrS[] = valueStr.split(";");//[0]20006:1234[1]20007:1234

                    ArrayList<String> roomIdArr = new ArrayList<>();
                    ArrayList<String> relaysArr = new ArrayList<>();

                    for (String roomRelayStr : arrS) {
                        String arrS2[] = roomRelayStr.split(":");//[0]20006[1]1234
                        roomIdArr.add(arrS2[0]);
                        relaysArr.add(arrS2[1]);
                    }

                    pirdbModel.setRoomId(roomIdArr);
                    pirdbModel.setRelays(relaysArr);

                    getPIRSObj.setRoomId(roomIdArr);
                    getPIRSObj.setRelays(relaysArr);
                    getPIRSObj.setPirNumber(jsonObject1.getInt("PIRNumber"));
                    adapter.notifyDataSetChanged();
                    break;
                }
            }

        } catch (JSONException e) {
            pd.dismiss();
            Log.e(TAG, "JSONException:-" + e.getMessage());
        }
    }

    private void changeGetMacInfoArrLst() {
        try {
            if (getPIRSObj != null) {
                if (Utils.isNetworkAvailable) {
                    getPirsVolley();
                }

                ArrayList<String> relaysArr = getPIRSObj.getRelays();
                ArrayList<String> roomIdArr = getPIRSObj.getRoomId();


                for (int i = 0; i < roomIdArr.size(); i++) {
                    setAppOnOffToModelArr(Integer.parseInt(roomIdArr.get(i)), relaysArr.get(i));
                }

                /*if (relaysArr != null && relaysArr.size() > 0) {
                    for (int i = 0; i < relaysArr.size(); i++) {
                        char relaysChArr[] = relaysArr.get(i).toCharArray();
                    }
                }*/

            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    private void setAppOnOffToModelArr(int roomId, String appliances) {
        for (int i = 0; i < getMacInfoArrLst.size(); i++) {
            if (getMacInfoArrLst.get(i).getRoomId() == roomId) {
                ArrayList<String> mApp = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst();

                char[] appArr = appliances.toCharArray();

                for (char app : appArr) {
                    Log.e(TAG, "app:-:" + app);
                    int appNumber = Integer.parseInt(String.valueOf(app));

                    switch (appNumber) {
                        case 1:
                            setAppOnOff(mApp, 0, i);
                            break;
                        case 2:
                            setAppOnOff(mApp, 1, i);
                            break;
                        case 3:
                            setAppOnOff(mApp, 2, i);
                            break;
                        case 4:
                            setAppOnOff(mApp, 3, i);
                            break;
                        case 5:
                            setAppOnOff(mApp, 4, i);
                            break;
                        case 6:
                            setAppOnOff(mApp, 5, i);
                            break;
                        case 7:
                            setAppOnOff(mApp, 6, i);
                            break;
                        case 8:
                            setAppOnOff(mApp, 7, i);

                            break;
                    }
                }
            }
        }
    }

    private void setAppOnOff(ArrayList<String> mApp, int appPos, int itemPos) {
        Log.e(TAG, "mApp:-:" + mApp + " appPos:-:" + appPos + " itemPos:-:" + itemPos);
        String appString = mApp.get(appPos);
        char[] chars = appString.toCharArray();
        chars[2] = '1';
        String str1 = chars[0] + "" + chars[1] + "" + chars[2];
        mApp.set(appPos, str1);
        Log.e(TAG, "AfmApp:-:" + mApp + " appPos:-:" + appPos + " itemPos:-:" + itemPos);
        getMacInfoArrLst.get(itemPos).setAppDimmTypeStatusArrLst(mApp);

    }

    private void setAdapter() {
        adapter = new PIRApplianceAdapter(this, getMacInfoArrLst, mListener);
        mApplianceLv.setAdapter(adapter);
        pd.dismiss();
    }

    public void appliClick(View view) {
        switch (view.getId()) {
            case R.id.save_tv:
                StringBuilder appSb = new StringBuilder();
                for (int i = 0; i < getMacInfoArrLst.size(); i++) {
                    ArrayList<String> appArr = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst();
                    for (int j = 0; j < appArr.size(); j++) {
                        String str = appArr.get(j);
                        if (str.charAt(2) == '1') {
                            appSb.append(String.valueOf(j + 1));
                        }
                    }
                    if (Utils.isNetworkAvailable) {
                        if (appSb.length() > 0) {
                            /*if (getPIRSObj != null) {
                                pirNoArrLst.add(Utils.getPIRSObj.getPirNumber());
                                for (int j = 1; j < 9; j++) {
                                    if (!pirNoArrLst.contains(j)) {
                                        pirMqttNo = j;
                                        break;
                                    }
                                }
                            }*/

                            if (pirNumbersArrLst != null && pirNumbersArrLst.size() > 0) {
                                for (int j = 1; j < 9; j++) {
                                    if (!pirNumbersArrLst.contains(j)) {
                                        pirMqttNo = j;
                                        break;
                                    }
                                }
                            } else {
                                Log.e(TAG, "pirNumbersArrLst empty");
                                if (getPIRSObj != null) {
                                    pirMqttNo = getPIRSObj.getPirNumber();
                                }
                            }
                            String finalMsg = "X|" + getMacInfoArrLst.get(i).getRoomId() + "|" +
                                    pirMqttNo + "|" + appSb;
                            Log.e(TAG, "finalMsg:-:" + finalMsg);
                            helper.sendMsg(finalMsg);
                            sendExtVolley(getMacInfoArrLst.get(i).getRoomId(), appSb.toString());
                            appSb = new StringBuilder();
                        }
                    }
                }
                break;
            case R.id.cancel_tv:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.one_tv:
                sendAppStatus(position, 0);
                break;
            case R.id.two_tv:
                sendAppStatus(position, 1);
                break;
            case R.id.three_tv:
                sendAppStatus(position, 2);
                break;
            case R.id.four_tv:
                sendAppStatus(position, 3);
                break;
            case R.id.five_tv:
                sendAppStatus(position, 4);
                break;
            case R.id.six_tv:
                sendAppStatus(position, 5);
                break;
            case R.id.seven_tv:
                sendAppStatus(position, 6);
                break;
            case R.id.eight_tv:
                sendAppStatus(position, 7);
                break;
        }
    }

    private void sendAppStatus(int position, int i) {

        String str = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().get(i);//0B1 sinle button status changes
        Log.e(TAG, "sendAppStatusstr " + str);

        char[] chars = str.toCharArray();

        if (chars[2] == '0') {
            chars[2] = '1';
        } else {
            chars[2] = '0';
        }

        String str1 = chars[0] + "" + chars[1] + "" + chars[2];
        getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().set(i, str1);

        Log.e(TAG, "after" + getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().get(i));

        getMacInfoArrLst.get(position).setChecked(false);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMqttResponse(String res) {
        Log.e(TAG, "mqttResStr:-:" + res);
        if (res.contains("Notification")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "45":
                    Toast.makeText(this, "PIR setting success", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void sendExtVolley(int roomId, String relays) {
        String strMethod = "SetPIRExt";
        //void SetPIRExt(string MacId, string IMEI, int RoomId, int PIR, string Value);
        String strUrl = ServiceHandler.baseUrl + strMethod + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
                + "&RoomId=" + roomId + "&PIR=" + pirMqttNo + "&Value=" + relays;
        Log.e(TAG, "strUrl:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "PirSetError:-:" + error.getMessage());
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
        Intent intent = new Intent(this, EditPIRActivity.class);
        startActivity(intent);
    }
}
