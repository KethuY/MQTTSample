package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.TimerApplianceAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.GetMacInfoModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.atg.onecontrolv3.helpers.Utils.getTimerModelObj;


public class TimerApplianceActivity extends BaseActivity implements OnItemClickListener {

    private static final String TAG = "TimerApplianceActivity";
    OnItemClickListener mListener;
    String macNameStr;
    private TransparentProgressDialog pd;
    private ArrayList<GetMacInfoModel> getMacInfoArrLst;
    private GetMacInfoModel model;
    private TimerApplianceAdapter adapter;
    private ListView macInfoListView;
    private CheckBox mMasterCb;
    private Toolbar toolBar;
    private boolean mTouched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_appliance);
        mListener = this;
        pd = new TransparentProgressDialog(TimerApplianceActivity.this, R.drawable.progress);
        //macNameStr = MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext());
        setToolBar();
        macInfoListView = (ListView) findViewById(R.id.dashboard_lv);
        mMasterCb = (CheckBox) findViewById(R.id.master_cb);
        mTouched = false;
        mMasterCb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouched = true;
                return false;
            }
        });
        mMasterCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (null != getMacInfoArrLst) {
                        for (int i = 0; i < getMacInfoArrLst.size(); i++) {
                            for (int j = 0; j < getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst().size(); j++) {
                                String str = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst().get(j);
                                char[] chars = str.toCharArray();
                                chars[2] = '1';
                                String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                                getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst().set(j, str1);
                            }
                            getMacInfoArrLst.get(i).setChecked(true);
                        }
                    }
                } else {
                    if (null != getMacInfoArrLst) {
                        for (int i = 0; i < getMacInfoArrLst.size(); i++) {
                            for (int j = 0; j < getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst().size(); j++) {
                                String str = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst().get(j);
                                char[] chars = str.toCharArray();
                                chars[2] = '0';
                                String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                                getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst().set(j, str1);
                            }
                            getMacInfoArrLst.get(i).setChecked(false);
                        }
                    }
                }
                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setToolBar() {
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Edit Timer");
        getSupportActionBar().setSubtitle(macNameStr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            getMacInfoArrLst = (ArrayList<GetMacInfoModel>) getIntent().getSerializableExtra("TA");

            if (getMacInfoArrLst != null && getMacInfoArrLst.size() > 0) {
                setAdapter();
            } else {
                getMacInfo();
            }
        } else {
            Utils.showMessageDialog("No Internet.", TimerApplianceActivity.this);
        }
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
                model = new GetMacInfoModel();
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
                Utils.mDashBoardArrLst = getMacInfoArrLst;
            }

            changeGetMacInfoArrLst();
            setAdapter();
        } catch (Exception e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
            pd.dismiss();
        }
    }

    private void changeGetMacInfoArrLst() {
        try {
            if (getTimerModelObj != null) {

                if (getTimerModelObj.getTimertype() == 1) {
                    mMasterCb.setChecked(true);

                    for (int i = 0; i < getMacInfoArrLst.size(); i++) {
                        getMacInfoArrLst.get(i).setChecked(true);

                        ArrayList<String> mApp = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst();

                        for (int j = 0; j < mApp.size(); j++) {

                            String str = mApp.get(j);

                            char[] chars = str.toCharArray();
                            chars[2] = '1';
                            String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                            // arr.add(str1);

                            mApp.set(j, str1);
                        }

                        getMacInfoArrLst.get(i).setAppDimmTypeStatusArrLst(mApp);

                    }


                } else {
                    String relays = getTimerModelObj.getRelays();

                    // relays="20001:123,20001:345";

                    if (relays.length() > 0) {
                        if (relays.contains(",")) {
                            String[] roomIdsApp = relays.split(",");

                            for (String aRoomIdsApp : roomIdsApp) {

                                String[] appliances = aRoomIdsApp.split(":");
                                int roomId = Integer.parseInt(appliances[0]);
                                Log.e(TAG, "roomId," + roomId + " appliances," + appliances[1]);
                                setAppOnOffToModelArr(roomId, appliances[1]);
                            }

                        } else {
                            String[] roomApp = relays.split(":");
                            int roomId = Integer.parseInt(roomApp[0]);
                            String app = roomApp[1];
                            Log.e(TAG, "roomId:-:" + roomId + " appliances:-:" + app);
                            setAppOnOffToModelArr(roomId, app);

                        }

                    }

                    for (int j = 0; j < getMacInfoArrLst.size(); j++) {

                        int appSize = 0;

                        for (int i = 0; i < getMacInfoArrLst.get(j).getAppDimmTypeStatusArrLst().size(); i++) {

                            String appArrLst = getMacInfoArrLst.get(j).getAppDimmTypeStatusArrLst().get(i);

                            if (appArrLst.charAt(2) == '1') {
                                appSize++;
                            }
                        }

                        if ((getMacInfoArrLst.get(j).getNumOfRelays() == 6 || getMacInfoArrLst.get(j).getNumOfRelays() == 7) && appSize == 6 || ((getMacInfoArrLst.get(j).getNumOfRelays() == 8 || getMacInfoArrLst.get(j).getNumOfRelays() == 9) && appSize == 8)) {
                            getMacInfoArrLst.get(j).setChecked(true);

                        }
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    private void setAppOnOffToModelArr(int roomId, String appliance) {

        for (int j = 0; j < getMacInfoArrLst.size(); j++) {

            if (getMacInfoArrLst.get(j).getRoomId() == roomId) { //if matches room
                ArrayList<String> mApp = getMacInfoArrLst.get(j).getAppDimmTypeStatusArrLst();

                char[] appArr = appliance.toCharArray();


                for (char anAppArr : appArr) {

                    Log.e(TAG, "anAppArr:-:" + anAppArr);

                    int appNumber = Integer.parseInt(String.valueOf(anAppArr));

                    switch (appNumber) {
                        case 1:
                            setAppOnOff(mApp, 0, j);
                            break;
                        case 2:
                            setAppOnOff(mApp, 1, j);
                            break;
                        case 3:
                            setAppOnOff(mApp, 2, j);
                            break;
                        case 4:
                            setAppOnOff(mApp, 3, j);
                            break;
                        case 5:
                            setAppOnOff(mApp, 4, j);
                            break;
                        case 6:
                            setAppOnOff(mApp, 5, j);
                            break;
                        case 7:
                            setAppOnOff(mApp, 6, j);
                            break;
                        case 8:
                            setAppOnOff(mApp, 7, j);

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
        if (getMacInfoArrLst != null && getMacInfoArrLst.size() > 0) {
            adapter = new TimerApplianceAdapter(TimerApplianceActivity.this, getMacInfoArrLst, mListener);
            macInfoListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Utils.showMessageDialog("Data not available", TimerApplianceActivity.this);
        }
        pd.dismiss();
    }

    @Override
    public void onItemClick(View view, int position) {
       /* switch (mView.getId()) {
            case R.id.roomall_cb:
                if (adapter.mIsChecked) {

                    ArrayList<String> mRoomLst = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst();

                    for (int j = 0; j < mRoomLst.size(); j++) {
                        String str = mRoomLst.get(j);
                        char[] chars = str.toCharArray();
                        chars[2] = '1';
                        String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                        getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().set(j, str1);
                    }
                    getMacInfoArrLst.get(position).setChecked(true);
                    adapter.mIsChecked = false;

                } else {

                    ArrayList<String> mRoomLst = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst();

                    for (int j = 0; j < mRoomLst.size(); j++) {
                        String str = mRoomLst.get(j);
                        char[] chars = str.toCharArray();
                        chars[2] = '0';
                        String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                        getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().set(j, str1);

                    }
                    getMacInfoArrLst.get(position).setChecked(false);
                    adapter.mIsChecked = false;
                }

                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                }
                break;
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
*/


        mTouched = false;
        switch (view.getId()) {
            case R.id.roomall_cb:
                if (adapter.mIsChecked) {

                    ArrayList<String> mRoomLst = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst();

                    for (int j = 0; j < mRoomLst.size(); j++) {
                        String str = mRoomLst.get(j);
                        char[] chars = str.toCharArray();
                        chars[2] = '1';
                        String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                        getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().set(j, str1);

                    }

                    getMacInfoArrLst.get(position).setChecked(true);
                    adapter.mIsChecked = false;


                } else {

                    ArrayList<String> mRoomLst = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst();

                    for (int j = 0; j < mRoomLst.size(); j++) {
                        String str = mRoomLst.get(j);
                        char[] chars = str.toCharArray();
                        chars[2] = '0';
                        String str1 = chars[0] + "" + chars[1] + "" + chars[2];
                        getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().set(j, str1);

                    }

                    mMasterCb.setChecked(false);
                    getMacInfoArrLst.get(position).setChecked(false);
                    adapter.mIsChecked = false;
                }

                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                }

                break;

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

        adapter.notifyDataSetChanged();

    }
      /*  int size = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().size();
        int count = 0;

        boolean hasDimVal=false;

        for (int i = 0; i < size; i++) {

            char onStatus = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().get(i).charAt(2);
            char dimm=getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().get(i).charAt(0);

            if(dimm=='0'){

                if (onStatus == '0') {
                    count--;
                } else if (onStatus == '1') {
                    count++;
                }
            }
           else{
                if (onStatus == '0') {
                    count--;
                } else if (onStatus == '1') {
                    count++;
                }
                hasDimVal=true;

            }
        }

        if(hasDimVal)
        {
            count++;
            hasDimVal=false;
        }

        if(count==getMacInfoArrLst.get(position).getNumOfRelays())
            getMacInfoArrLst.get(position).setChecked(true);
        else
            getMacInfoArrLst.get(position).setChecked(false);*/
      /*  if (size == count) {

            getMacInfoArrLst.get(position).setChecked(true);
        } else {


        }*/
    //   setAdapter();


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

        //if(!getMacInfoArrLst.get(position).isChecked())
        mMasterCb.setChecked(false);
       /* int size = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().size();//8 ob1's
        int appSize = 0;

        for (int j = 0; j < size; j++) {

            String singleBtn = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst().get(j);

            if (singleBtn.charAt(2) == '1') {
                appSize++;

                if ((getMacInfoArrLst.get(position).getNumOfRelays() == 6 || getMacInfoArrLst.get(position).getNumOfRelays() == 7) && appSize == 6
                        || ((getMacInfoArrLst.get(position).getNumOfRelays() == 8 || getMacInfoArrLst.get(position).getNumOfRelays() == 9) && appSize == 8)) {
                  //  getMacInfoArrLst.get(position).setChecked(true);
                    getMacInfoArrLst.get(position).setChecked(false);
                    appSize = 0;
                } else {
                    getMacInfoArrLst.get(position).setChecked(false);
                }

            }
        }*/

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
       /* } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }*/
    }

    public void appliClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                onBackPressed();
                break;
            case R.id.save_tv:
                Intent intent = null;
                try {
                    intent = new Intent(TimerApplianceActivity.this, EditTimerActivity.class);
                    intent.putExtra("isFromRepeat", false);
                    intent.putExtra("IsFrom", "2");
                    intent.putExtra("TA", getMacInfoArrLst);
                    //Roomid1:Relay1,Roomid1:Relay2,Roomid2:Relay1,Roomid2:Relay2\
                    if (mMasterCb.isChecked()) {
                        if (getTimerModelObj != null) {
                            getTimerModelObj.setTimertype(1);
                            getTimerModelObj.setRelays("");
                        }
                        intent.putExtra("Type", "1");
                        intent.putExtra("sb", "");
                        startActivity(intent);

                    } else {
                        StringBuilder sb = new StringBuilder("");
                       /* StringBuilder sb = new StringBuilder("");

                        for (int i = 0; i < getMacInfoArrLst.size(); i++) {

                            if (getMacInfoArrLst.get(i).isChecked()) {
                                sb.append(getMacInfoArrLst.get(i).getRoomId()).append(",");
                            }else{

                                ArrayList<String> appArr = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst();

                                for (int j = 0; j < appArr.size(); j++) {

                                    String str = appArr.get(j);

                                    if (str.charAt(2) == '1') {
                                        sb = new StringBuilder();
                                        break;
// sb.append(getMacInfoArrLst.get(i).getRoomId()).append(":").append(String.valueOf(j + 1)).append(",");
                                    }
                                }
                            }

                        }*/
                        /*if (sb.length() > 0) {
                            // type2
                            // Intent intent = new Intent(TimerApplianceActivity.this, EditTimerActivity.class);
                            sb.setLength(sb.length() - 1);
                            if (getTimerModelObj != null) {
                                getTimerModelObj.setTimertype(2);
                                getTimerModelObj.setRelays(sb.toString());
                            }
                            Log.e(TAG, "sb2" + sb.toString());
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TimerApplianceActivity.this);
                            SharedPreferences.Editor outState = sp.edit();
                            outState.putString("sb", sb.toString());
                            outState.apply();
                            intent.putExtra("Type", "2");
                            intent.putExtra("sb", sb.toString());
                            startActivity(intent);
                        } else */
                        {//type3
                            StringBuilder appsb = new StringBuilder();

                            for (int i = 0; i < getMacInfoArrLst.size(); i++) {
                                ArrayList<String> appArr = getMacInfoArrLst.get(i).getAppDimmTypeStatusArrLst();


                                // sb.append().append(":");


                                boolean isCheckedEnry = false;


                                for (int j = 0; j < appArr.size(); j++) {

                                    String str = appArr.get(j);

                                    if (str.charAt(2) == '1') {
                                        isCheckedEnry = true;
                                        appsb.append((String.valueOf(j + 1)));
                                    }
                                }

                                if (isCheckedEnry) {
                                    sb.append(getMacInfoArrLst.get(i).getRoomId()).append(":").append(appsb).append(",");
                                    appsb = new StringBuilder();
                                }

                            }

                            if (getTimerModelObj != null) {
                                getTimerModelObj.setTimertype(3);
                                getTimerModelObj.setRelays(sb.toString());
                            }

                            sb.setLength(sb.length() - 1);
                            Log.e(TAG, "sb3" + sb.toString());
                            intent.putExtra("Type", "3");
                            intent.putExtra("sb", sb.toString());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                startActivity(intent);
                break;
        }
    }
}

