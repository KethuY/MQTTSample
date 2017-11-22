package com.atg.onecontrolv3.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.Database.DatabaseHelperForAC;
import com.atg.onecontrolv3.Database.DatabaseHelperForDVD;
import com.atg.onecontrolv3.Database.DatabaseHelperForHT;
import com.atg.onecontrolv3.Database.DatabaseHelperForMP;
import com.atg.onecontrolv3.Database.DatabaseHelperForPROJ;
import com.atg.onecontrolv3.Database.DatabaseHelperForTV;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener2;
import com.atg.onecontrolv3.interfaces.OnLongClickListener;
import com.atg.onecontrolv3.interfaces.OnSeekBarChangeListener;
import com.atg.onecontrolv3.interfaces.OnTempChangeListener;
import com.atg.onecontrolv3.irblasters.IRBlasterAcNewActivity;
import com.atg.onecontrolv3.irblasters.IRBlasterDVDActivity;
import com.atg.onecontrolv3.irblasters.IRBlasterHomeTheaterActivity;
import com.atg.onecontrolv3.irblasters.IRBlasterProjector;
import com.atg.onecontrolv3.irblasters.IRBlasterTVNewActivity;
import com.atg.onecontrolv3.models.MacInfoModel;
import com.atg.onecontrolv3.models.MqttRoomStatusModel;
import com.atg.onecontrolv3.models.SingleRoomModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.preferances.OneControlPreferences;
import com.atg.onecontrolv3.rest.ApiClient;
import com.atg.onecontrolv3.rest.ApiInterface;
import com.atg.onecontrolv3.views.AcClassView;
import com.atg.onecontrolv3.views.IRClassView;
import com.atg.onecontrolv3.views.RegularRelayBtnView;
import com.atg.onecontrolv3.views.RoomNameView;
import com.atg.onecontrolv3.views.ViewGroupUtils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.atg.onecontrolv3.R.id.tv_relay_btn;
import static com.atg.onecontrolv3.helpers.Utils.getColorPrimary;
import static com.atg.onecontrolv3.helpers.Utils.getCurrentDateTime;
import static com.atg.onecontrolv3.helpers.Utils.getCurrentDateTimeQT;
import static com.atg.onecontrolv3.helpers.Utils.splitStringTag;

/**
 * Created by Bharath on 06-Sep-17
 */

public class DashboardFragment extends Fragment implements View.OnClickListener, OnItemClickListener2, OnTempChangeListener, MqttHelper.responseListener,
        OnSeekBarChangeListener, OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DashboardFragment";
    //private static final String chPlus = "c", chMinus = "h", mute = "m", volPlus = "p", volMinus = "i";
    private static final String chPlus = "chPlus", chMinus = "chMinus", mute = "mute", volPlus = "volPlus", volMinus = "volMinus", stb = "stb";
    private static final String fan1 = "1", fan2 = "2", fan3 = "3";
    private static final String TYPE_TV = "TV";
    private static final String TYPE_AC = "AC";
    private static final String TYPE_DVD = "DVD";
    private static final String TYPE_HT = "HT";
    private static final String TYPE_MP = "MP";
    private static final String TYPE_PROJ = "PROJ";
    private static String FINAL_TYPE = "";
    ScrollView scrollView;
    ArrayList<SingleRoomModel> mSingleRoomArrLst;
    OnItemClickListener2 mListener;
    OnTempChangeListener mTempListener;
    OnLongClickListener mLongListener;
    OnSeekBarChangeListener mSeekListener;
    MqttClient client;
    String uuidFromDB = "";
    DatabaseHelperForTV dbHelperTv;
    DatabaseHelperForAC dbHelperAc;
    DatabaseHelperForDVD dbHelperDVD;
    DatabaseHelperForHT dbHelperHT;
    DatabaseHelperForMP dbHelperMP;
    DatabaseHelperForPROJ dbHelperPROJ;
    String secret = "", id = "";
    TransparentProgressDialog pd;
    private int roomActiveCnt;
    private int totalActiveCnt;
    private LinearLayout mainLl;
    private Context context;
    private MqttHelper helper;
    private ActiveCountListener listener;
    private SwipeRefreshLayout swipeRefreshLayout;
    /*IR variables..!*/
    private String keyName;
    private PopupWindow popupWindow;
    private View lngView;
    private Activity mActivity;
    private boolean isViewShown = false;
    private OneControlPreferences mPreferences;
    private int mTimerTime = 0;

    /*fifteenMTv1
thirtyMTv1
sixtyMTv1 =
nrMTv1 = po*/
    private TextView fiveMTv, fifteenMTv, thirtyMTv, sixtyMTv, nrMTv, fiveMTv1, fifteenMTv1, thirtyMTv1, sixtyMTv1, nrMTv1;
    private int mWhichBtnInt;
    private int roomStatusPos;
    private ArrayList<Integer> mViewsTagsInLinearLayout;
    private HashMap<Integer, ArrayList<Integer>> mMap;
    private ArrayList<Integer> mTempIndexes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);
        scrollView = view.findViewById(R.id.main_ll);

        if (!isViewShown) {
            getGatewayDetails();
            mPreferences = new OneControlPreferences(mActivity);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        mainLl = new LinearLayout(context);
        mainLl.setOrientation(LinearLayout.VERTICAL);
        pd = new TransparentProgressDialog(mActivity, R.drawable.progress);
        /*Mqtt Conn..!*/
        handleMqttConnection();

        mListener = this;
        mTempListener = this;
        mLongListener = this;
        listener = (ActiveCountListener) getActivity();
        mSeekListener = this;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            isViewShown = true;
            getGatewayDetails();
        } else {
            isViewShown = false;
        }
    }

    private void handleMqttConnection() {
        MqttHelper.mPublishTopic = Utils.P_TOPIC + Utils.MAC_ID;
        MqttHelper.mSubscribeTopic = Utils.S_TOPIC + Utils.MAC_ID;
        MqttHelper.mSecurityTopic = Utils.SECURITY_TOPIC + Utils.MAC_ID;
        MqttHelper.mArmDisArmTopic = Utils.ARMDISARM_TOPIC + Utils.MAC_ID;
        MqttHelper.responseListener mqttListener;
        mqttListener = this;
        helper = new MqttHelper(context, mqttListener);
    }

    private void getGatewayDetails() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MacInfoModel> call = apiService.getGatewayDetails(Utils.MAC_ID, Utils.IMEI);

        call.enqueue(new Callback<MacInfoModel>() {
            @Override
            public void onResponse(Call<MacInfoModel> call, Response<MacInfoModel> response) {
                String strUrl = response.raw().request().url().toString();
                int statusCode = response.code();
                Utils.printLog(TAG, "statusCode:-:" + statusCode + " strUrl:-:" + strUrl);

                if (statusCode == 200) {
                    mSingleRoomArrLst = response.body().getSingleRoomArrLst();
                    if (null != mSingleRoomArrLst && !mSingleRoomArrLst.isEmpty()) {
                        mSingleRoomArrLst = parseJson(mSingleRoomArrLst);
                        //Utils.printLog(TAG, "" + mSingleRoomArrLst.size());
                        if (null != mSingleRoomArrLst && !mSingleRoomArrLst.isEmpty()) {
                            Utils.mSingleRoomArrLst = null;
                            Utils.mSingleRoomArrLst = mSingleRoomArrLst;
                            addViewsDynamically();
                        } else {
                            //Utils.printLog(TAG, "Something went wrong too, Please try again!");
                            Utils.showToast(context, "Something went wrong, Please try again!!");
                        }
                    } else {
                        //Utils.printLog(TAG, "No rooms found!");
                        Utils.showToast(context, "No rooms found!");
                    }
                } else {
                    //Utils.printLog(TAG, "Something went wrong, Please try again!");
                    Utils.showToast(context, "Something went wrong, Please try again!");
                }
            }

            @Override
            public void onFailure(Call<MacInfoModel> call, Throwable t) {
                Utils.printLog(TAG, "Retrofit error while parsing :-:" + t.getMessage());
                Utils.showToast(context, "Something went wrong, Please try again!");
            }
        });
    }

    private void addViewsDynamically() {

        Utils.ROOMS_CNT = 0;
        Utils.APP_CNT = 0;
        Utils.ROOMS_CNT = mSingleRoomArrLst.size();
        int tag = 0;

        // mViewsTagsInLinearLayout=new ArrayList<Integer>();
        mMap = new HashMap<>();
        ArrayList<SingleRoomModel> mTemRooms = mSingleRoomArrLst;

        for (int i = 0; i < mSingleRoomArrLst.size(); i++) {

            mTempIndexes = new ArrayList<Integer>();
            String roomName = Utils.replaceTilt(mSingleRoomArrLst.get(i).getRoomName());
            int roomId = mSingleRoomArrLst.get(i).getRoomId();
            int activeCount = mSingleRoomArrLst.get(i).getActiveCount();
            String autoRevoke = mSingleRoomArrLst.get(i).getAutoRevoke();
            RoomNameView classRoomName = new RoomNameView(mActivity, roomName, activeCount, mListener, i);
            mTemRooms.add(mSingleRoomArrLst.get(i));
            mainLl.addView(classRoomName.relayButton(mActivity, autoRevoke, roomId));
            mTempIndexes.add(mainLl.getChildCount() - 1);
            settingAppStatus(mSingleRoomArrLst.get(i), null);
            mMap.put(mSingleRoomArrLst.get(i).getRoomId(), mTempIndexes);
        }
        scrollView.addView(mainLl);
        /*if (null != pd && pd.isShowing())*/
        {
            //pd.dismiss();
        }
    }

    private ArrayList<SingleRoomModel> parseJson(ArrayList<SingleRoomModel> mSingleRoomArrLst) {
        totalActiveCnt = 0;
        for (int i = 0; i < mSingleRoomArrLst.size(); i++) {
            roomActiveCnt = 0;
            SingleRoomModel model = mSingleRoomArrLst.get(i);
            String appDimmTypeStatusStr = model.getInfo().substring(1);
            model.setAppDimmTypeStatusArrLst(getArrList(appDimmTypeStatusStr));
            char autoRevoke = model.getInfo().charAt(0);
            model.setAutoRevoke(String.valueOf(autoRevoke));
            model.setActiveCount(roomActiveCnt);
            totalActiveCnt += roomActiveCnt;
            model.setNumOfRelays(model.getNumOfRelays());
            model.setRoomId(model.getRoomId());
            model.setRoomName(model.getRoomName());
            model.setItemTag(String.valueOf((model.getRoomId() * 100) + i));
            mSingleRoomArrLst.set(i, model);
        }
        int rumId = mSingleRoomArrLst.get(0).getRoomId();
        if (rumId == 0) {
            rumId = 1;
        }
        listener.onCountChangeListener(totalActiveCnt, rumId);

        return mSingleRoomArrLst;
    }

    private ArrayList<String> getArrList(String info) {
        ArrayList<String> arr = new ArrayList<>();
        int index = 0;
        while (index < info.length()) {
            String str = info.substring(index, Math.min(index + 3, info.length()));
            arr.add(str);

            try {
                if (str.charAt(2) == '1') {
                    //mIntTotalActiveCnt += 1;
                    roomActiveCnt++;
                }
            } catch (Exception e) {
                Utils.printLog(TAG, "Active cnt Exception:-:" + e.getMessage());
            }

            index += 3;
        }
        return arr;
    }

    @Override
    public void onItemClick(View view) {
        int roomIdAppPos[];
        int roomId;
        String mqttCmd = "";
        int appliPos;
        char appSymbol;
        switch (view.getId()) {
            //------------Regular Views..!------------------
            case tv_relay_btn:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                //mqttCmd = "A|" + roomId + "|" + roomIdAppPos[1];//[1]-- appPos
                mqttCmd = "A|" + "1" + "|" + "2";//[1]-- appPos
                setLstOperated("QT" + Utils.MAC_ID + roomId + (roomIdAppPos[1] + 1));
                break;
            case R.id.tv_relay_tv:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                mqttCmd = "A|" + roomId + "|" + roomIdAppPos[1];//[1]-- appPos
                setLstOperated("QT" + Utils.MAC_ID + roomId + (roomIdAppPos[1] + 1));
                break;
            case R.id.tv_relay_ac:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                mqttCmd = "A|" + roomId + "|" + roomIdAppPos[1];//[1]-- appPos
                setLstOperated("QT" + Utils.MAC_ID + roomId + (roomIdAppPos[1] + 1));
                break;
            case R.id.room_all_ll:
                roomId = mSingleRoomArrLst.get((int) view.getTag()).getRoomId();
                mqttCmd = "A|" + roomId + "|R";
                setLstOperated("QT" + Utils.MAC_ID + roomId);
                break;
            case R.id.revoke_tv:
                roomId = mSingleRoomArrLst.get((int) view.getTag()).getRoomId();
                mqttCmd = "J|" + roomId;
                break;
            //------------IR Views..!------------------
            case R.id.tv_redirect:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                Utils.printLog(TAG, "appSymbol:-:" + appSymbol);
                sendToIRBlaster(appSymbol, roomId, roomIdAppPos[0], roomIdAppPos[1]);
                break;
            case R.id.tv_ch_up:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                getKeyName(appSymbol, chPlus);
                switch (appSymbol) {
                    case 'C':
                        dbHelperTv = new DatabaseHelperForTV(context);
                        break;
                    case 'H':
                        dbHelperMP = new DatabaseHelperForMP(context);
                        break;
                    case 'J':
                        dbHelperDVD = new DatabaseHelperForDVD(context);
                        break;
                    case 'K':
                        dbHelperHT = new DatabaseHelperForHT(context);
                        break;
                    case 'L':
                        dbHelperPROJ = new DatabaseHelperForPROJ(context);
                        break;

                }
                //keyName = chPlus;
                clickIRBtn(roomId, appliPos, FINAL_TYPE);
                break;
            case R.id.tv_ch_down:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                getKeyName(appSymbol, chMinus);
                //keyName = chMinus;
                switch (appSymbol) {
                    case 'C':
                        dbHelperTv = new DatabaseHelperForTV(context);
                        break;
                    case 'H':
                        dbHelperMP = new DatabaseHelperForMP(context);
                        break;
                    case 'J':
                        dbHelperDVD = new DatabaseHelperForDVD(context);
                        break;
                    case 'K':
                        dbHelperHT = new DatabaseHelperForHT(context);
                        break;
                    case 'L':
                        dbHelperPROJ = new DatabaseHelperForPROJ(context);
                        break;

                }
                clickIRBtn(roomId, appliPos, FINAL_TYPE);
                break;
            case R.id.tv_vol_down:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                getKeyName(appSymbol, volMinus);
                switch (appSymbol) {
                    case 'C':
                        dbHelperTv = new DatabaseHelperForTV(context);
                        break;
                    case 'H':
                        dbHelperMP = new DatabaseHelperForMP(context);
                        break;
                    case 'J':
                        dbHelperDVD = new DatabaseHelperForDVD(context);
                        break;
                    case 'K':
                        dbHelperHT = new DatabaseHelperForHT(context);
                        break;
                    case 'L':
                        dbHelperPROJ = new DatabaseHelperForPROJ(context);
                        break;

                }
                //keyName = volMinus;
                clickIRBtn(roomId, appliPos, FINAL_TYPE);
                break;
            case R.id.tv_vol_up:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                getKeyName(appSymbol, volPlus);
                switch (appSymbol) {
                    case 'C':
                        dbHelperTv = new DatabaseHelperForTV(context);
                        break;
                    case 'H':
                        dbHelperMP = new DatabaseHelperForMP(context);
                        break;
                    case 'J':
                        dbHelperDVD = new DatabaseHelperForDVD(context);
                        break;
                    case 'K':
                        dbHelperHT = new DatabaseHelperForHT(context);
                        break;
                    case 'L':
                        dbHelperPROJ = new DatabaseHelperForPROJ(context);
                        break;

                }
                //keyName = volPlus;
                clickIRBtn(roomId, appliPos, FINAL_TYPE);
                break;
            case R.id.tv_mute:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                getKeyName(appSymbol, mute);
                switch (appSymbol) {
                    case 'C':
                        dbHelperTv = new DatabaseHelperForTV(context);
                        break;
                    case 'H':
                        dbHelperMP = new DatabaseHelperForMP(context);
                        break;
                    case 'J':
                        dbHelperDVD = new DatabaseHelperForDVD(context);
                        break;
                    case 'K':
                        dbHelperHT = new DatabaseHelperForHT(context);
                        break;
                    case 'L':
                        dbHelperPROJ = new DatabaseHelperForPROJ(context);
                        break;
                }
                //keyName = mute;
                clickIRBtn(roomId, appliPos, FINAL_TYPE);
                break;
            case R.id.tv_power:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                appSymbol = mSingleRoomArrLst.get(roomIdAppPos[0]).getAppDimmTypeStatusArrLst().get(roomIdAppPos[1] - 1).charAt(1);
                getKeyName(appSymbol, stb);
                switch (appSymbol) {
                    case 'C':
                        dbHelperTv = new DatabaseHelperForTV(context);
                        break;
                    case 'H':
                        dbHelperMP = new DatabaseHelperForMP(context);
                        break;
                    case 'J':
                        dbHelperDVD = new DatabaseHelperForDVD(context);
                        break;
                    case 'K':
                        dbHelperHT = new DatabaseHelperForHT(context);
                        break;
                    case 'L':
                        dbHelperPROJ = new DatabaseHelperForPROJ(context);
                        break;

                }
                //keyName = mute;
                clickIRBtn(roomId, appliPos, FINAL_TYPE);


                break;
            //------------IR AC Views..!------------------
            case R.id.tv_low:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                dbHelperAc = new DatabaseHelperForAC(context);
                keyName = fan1;
                clickIRBtn(roomId, appliPos, TYPE_AC);
                break;
            case R.id.tv_med:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                dbHelperAc = new DatabaseHelperForAC(context);
                keyName = fan2;
                clickIRBtn(roomId, appliPos, TYPE_AC);
                break;
            case R.id.tv_high:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                dbHelperAc = new DatabaseHelperForAC(context);
                keyName = fan3;
                clickIRBtn(roomId, appliPos, TYPE_AC);
                break;
        }

        if (!mqttCmd.isEmpty()) {
            helper.sendMsg(mqttCmd);
        }
    }

    private void getKeyName(char appSymbol, String keyType) {
        switch (appSymbol) {
            case 'C':
                FINAL_TYPE = TYPE_TV;
                switch (keyType) {
                    case chPlus:
                        keyName = "c";
                        break;
                    case chMinus:
                        keyName = "h";
                        break;
                    case mute:
                        keyName = "m";
                        break;
                    case volPlus:
                        keyName = "p";
                        break;
                    case volMinus:
                        keyName = "i";
                        break;
                    case stb:
                        keyName = "s";
                        break;
                }
                break;
            case 'H':
                FINAL_TYPE = TYPE_MP;
                switch (keyType) {
                    case chPlus:
                        keyName = "c";
                        break;
                    case chMinus:
                        keyName = "s";
                        break;
                    case mute:
                        keyName = "m";
                        break;
                    case volPlus:
                        keyName = "p";
                        break;
                    case volMinus:
                        keyName = "i";
                        break;
                    case stb:
                        keyName = "h";
                        break;
                }
                break;
            case 'J':
                FINAL_TYPE = TYPE_DVD;
                switch (keyType) {
                    case chPlus:
                        keyName = "w";
                        break;
                    case chMinus:
                        keyName = "n";
                        break;
                    case mute:
                        keyName = "t";
                        break;
                    case volPlus:
                        keyName = "u";
                        break;
                    case volMinus:
                        keyName = "i";
                        break;
                    case stb:
                        keyName = "d";
                        break;
                }
                break;
            case 'K':
                FINAL_TYPE = TYPE_HT;
                switch (keyType) {
                    case chPlus:
                        keyName = "c";
                        break;
                    case chMinus:
                        keyName = "s";
                        break;
                    case mute:
                        keyName = "m";
                        break;
                    case volPlus:
                        keyName = "p";
                        break;
                    case volMinus:
                        keyName = "i";
                        break;
                    case stb:
                        keyName = "h";
                        break;
                }
                break;
            case 'L':
                FINAL_TYPE = TYPE_PROJ;
                switch (keyType) {
                    case chPlus:
                        keyName = "g";
                        break;
                    case chMinus:
                        keyName = "i";
                        break;
                    case mute:
                        keyName = "q";
                        break;
                    case volPlus:
                        keyName = "f";
                        break;
                    case volMinus:
                        keyName = "b";
                        break;
                    case stb:
                        keyName = "j";
                        break;
                }
                break;
        }
    }

    private void setLstOperated(String key) {
        mPreferences.storeLstOperated(key, getCurrentDateTimeQT());
    }

    private void clickIRBtn(int mRoomId, int mAppliancePos, String appType) {
        String mRoomIdStr = String.valueOf(mRoomId);
        String mAppliancePosStr = String.valueOf(mAppliancePos);
        ArrayList<String> zmoteIdSecretArrLst = new ArrayList<>();
        Utils.printLog(TAG, "appType:-:" + appType);
        switch (appType) {
            case TYPE_TV:
                uuidFromDB = dbHelperTv.getUUID(mRoomIdStr, mAppliancePosStr);
                zmoteIdSecretArrLst = dbHelperTv.getIdSecret(mRoomIdStr, mAppliancePosStr);
                break;
            case TYPE_AC:
                uuidFromDB = dbHelperAc.getUUID(mRoomIdStr, mAppliancePosStr);
                zmoteIdSecretArrLst = dbHelperAc.getIdSecret(mRoomIdStr, mAppliancePosStr);
                break;
            case TYPE_DVD:
                uuidFromDB = dbHelperDVD.getUUID(mRoomIdStr, mAppliancePosStr);
                zmoteIdSecretArrLst = dbHelperDVD.getIdSecret(mRoomIdStr, mAppliancePosStr);
                break;
            case TYPE_HT:
                uuidFromDB = dbHelperHT.getUUID(mRoomIdStr, mAppliancePosStr);
                zmoteIdSecretArrLst = dbHelperHT.getIdSecret(mRoomIdStr, mAppliancePosStr);
                break;
            case TYPE_MP:
                uuidFromDB = dbHelperMP.getUUID(mRoomIdStr, mAppliancePosStr);
                zmoteIdSecretArrLst = dbHelperMP.getIdSecret(mRoomIdStr, mAppliancePosStr);
                break;
            case TYPE_PROJ:
                uuidFromDB = dbHelperPROJ.getUUID(mRoomIdStr, mAppliancePosStr);
                zmoteIdSecretArrLst = dbHelperPROJ.getIdSecret(mRoomIdStr, mAppliancePosStr);
                break;
        }
        if ((uuidFromDB != null && !TextUtils.isEmpty(uuidFromDB)) && (null != zmoteIdSecretArrLst && !zmoteIdSecretArrLst.isEmpty())) {
            byte[] keyValueInBytes = new byte[0];
            id = zmoteIdSecretArrLst.get(0);
            secret = zmoteIdSecretArrLst.get(1);
            Utils.printLog(TAG, "id:-:" + id + " secret:-:" + secret);
            switch (appType) {
                case TYPE_TV:
                    keyValueInBytes = dbHelperTv.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, String.valueOf(mRoomId), String.valueOf(mAppliancePos), "");
                    break;
                case TYPE_AC:
                    keyValueInBytes = dbHelperAc.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, String.valueOf(mRoomId), String.valueOf(mAppliancePos));
                    break;
                case TYPE_DVD:
                    keyValueInBytes = dbHelperDVD.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, String.valueOf(mRoomId), String.valueOf(mAppliancePos));
                    break;
                case TYPE_HT:
                    keyValueInBytes = dbHelperHT.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, String.valueOf(mRoomId), String.valueOf(mAppliancePos));
                    break;
                case TYPE_MP:
                    keyValueInBytes = dbHelperMP.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, String.valueOf(mRoomId), String.valueOf(mAppliancePos));
                    break;
                case TYPE_PROJ:
                    keyValueInBytes = dbHelperPROJ.getIRDeviceKeyValue(/*uuidFromDB == null ? "" : */uuidFromDB, keyName, String.valueOf(mRoomId), String.valueOf(mAppliancePos));
                    break;
            }
            Utils.printLog(TAG, "kkeyName:-:" + keyName);
            if (keyValueInBytes != null && keyValueInBytes.length > 0) {
                String keyValueInString = Utils.getStringFromBytes(keyValueInBytes);
//                            makeIRRequest(localIP, zmoteId, keyName, keyValueInString);
                new MQTTTask().execute(uuidFromDB, keyValueInString);
            } else {
                Toast.makeText(context, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Learn the control with IR Blaster first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTempClickListener(View view, String temp) {
        int roomIdAppPos[];
        int roomId;
        int appliPos;
        switch (view.getId()) {
            case R.id.tv_temp_up:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                dbHelperAc = new DatabaseHelperForAC(context);
                keyName = temp;
                clickIRBtn(roomId, appliPos, TYPE_AC);
                break;
            case R.id.tv_temp_down:
                roomIdAppPos = splitStringTag(view.getTag().toString());
                roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();//[0]-- roomId
                appliPos = roomIdAppPos[1];
                dbHelperAc = new DatabaseHelperForAC(context);
                keyName = temp;
                clickIRBtn(roomId, appliPos, TYPE_AC);
                break;
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

    private void sendToIRBlaster(char appSymbol, int roomId, int position, int appPos) {
        Intent intent = null;
        switch (appSymbol) {
            case 'C':
                intent = new Intent(getActivity(), IRBlasterTVNewActivity.class);
                intent.putExtra("APPLIANCE_TYPE", "TVN");
                break;
            case 'D':
                intent = new Intent(getActivity(), IRBlasterAcNewActivity.class);
                intent.putExtra("APPLIANCE_TYPE", "AC");
                break;
            case 'H':
                intent = new Intent(getActivity(), IRBlasterHomeTheaterActivity.class);
                intent.putExtra("APPLIANCE_TYPE", "MusicPlayer");
                break;
            case 'J':
                intent = new Intent(getActivity(), IRBlasterDVDActivity.class);
                intent.putExtra("APPLIANCE_TYPE", "DVD");
                break;
            case 'K':
                intent = new Intent(getActivity(), IRBlasterHomeTheaterActivity.class);
                intent.putExtra("APPLIANCE_TYPE", "HT");
                break;
            case 'L':
                intent = new Intent(getActivity(), IRBlasterProjector.class);
                intent.putExtra("APPLIANCE_TYPE", "PRO");
                break;
        }
        assert intent != null;
        intent.putExtra("CONTROLLING", "Control");
        intent.putExtra("ROOM_ID", String.valueOf(roomId));
        intent.putExtra("ROOM_NAME", mSingleRoomArrLst.get(position).getRoomName());
        intent.putExtra("APPLIANCE_POS", appPos);
        startActivity(intent);
    }

    @Override
    public void onMqttResponse(String res) {
        parseMqttResponse(res);
    }

    private void parseMqttResponse(String res) {
        SingleRoomModel model = null;

        SingleRoomModel singleRoomModel = null;
        if (res.contains("ReceiverStatus:") && !res.contains("?")) {
            //ReceiverStatus:{2;0;0D00@00@00@00@00@00@00@0}
            MqttRoomStatusModel mqttRoomStatusModel = new MqttRoomStatusModel();
            ArrayList<String> arr;
            String arrS[] = res.split(Pattern.quote("{"));
            String roomResponse = arrS[1];//2;0;0D00@00@00@00@00@00@00@0}
            roomResponse = roomResponse.contains("}") ? roomResponse.replace("}", "") : roomResponse;//2;0;0D00@00@00@00@00@00@00@0
            String arrS2[] = roomResponse.split(Pattern.quote(";"));
            mqttRoomStatusModel.setRoomId(Integer.parseInt(arrS2[0]));
            mqttRoomStatusModel.setAutoRevoke(arrS2[1]);
            arr = getArrList(arrS2[2]);
            mqttRoomStatusModel.setAppDimTypeStatusArrLst(arr);
            roomActiveCnt = 0;
            if (!arr.isEmpty()) {
                for (int i = 0; i < mSingleRoomArrLst.size(); i++) {

                    int indCnt = 0;
                    if (mqttRoomStatusModel.getRoomId() == mSingleRoomArrLst.get(i).getRoomId()) {

                        roomStatusPos = i;
                        mSingleRoomArrLst.get(i).setAutoRevoke(mqttRoomStatusModel.getAutoRevoke());
                        mSingleRoomArrLst.get(i).setAppDimmTypeStatusArrLst(mqttRoomStatusModel.getAppDimTypeStatusArrLst());

                        for (int j = 0; j < mSingleRoomArrLst.get(i).getAppDimmTypeStatusArrLst().size(); j++) {
                            //Utils.printLog(TAG, "roomActiveCnt:-:" + indCnt);
                            if (mSingleRoomArrLst.get(i).getAppDimmTypeStatusArrLst().get(j).charAt(2) == '1') {
                                indCnt++;
                                //Utils.printLog(TAG, "roomActiveCnt2:-:" + indCnt);
                            }
                        }
                        roomActiveCnt = indCnt;
                        mSingleRoomArrLst.get(i).setActiveCount(roomActiveCnt);
                        singleRoomModel = mSingleRoomArrLst.get(i);
                        break;
                    }
                }
            }
            totalActiveCnt = 0;
            for (int i = 0; i < mSingleRoomArrLst.size(); i++) {
                int count = mSingleRoomArrLst.get(i).getActiveCount();
                totalActiveCnt = totalActiveCnt + count;
            }
            listener.onCountChangeListener(totalActiveCnt, mqttRoomStatusModel.getRoomId());
            ArrayList<Integer> integers = new ArrayList<>();
            for (int i = 0; i < mSingleRoomArrLst.size(); i++) {
                integers.add(mSingleRoomArrLst.get(i).getRoomId());
            }

            if (null != singleRoomModel)
                if (mMap != null) {
                    for (int roomId : mMap.keySet()) {
                        if (singleRoomModel.getRoomId() == roomId) {
                            ArrayList<Integer> roomAppliancesIndexInLinear = mMap.get(roomId);
                            roomAppliancesIndexInLinear.remove(0); //bcz particular roomId Index but we need appliances index
                            // ArrayList<String> list = singleRoomModel.getAppDimmTypeStatusArrLst();
                            settingAppStatus(singleRoomModel, roomAppliancesIndexInLinear);

                            break;
                        }


                    }
                }
        }
    }

    private void settingAppStatus(SingleRoomModel model, ArrayList<Integer> roomAppliancesIndexInLinear) {

        ArrayList<String> list = model.getAppDimmTypeStatusArrLst();
        int roomId = model.getRoomId();
        for (int i = 0; i < list.size(); i++) {
            int last;
            char appType = list.get(i).charAt(1);
            String ij = roomId + ":"/*String.valueOf(i)*/ + String.valueOf(i + 1);

            if (appType == 'A' || appType == 'B' || appType == 'E' || appType == 'G' || appType == 'I' || appType == 'F') {
                Utils.APP_CNT = Utils.APP_CNT + 1;

                if (list.size() - 1 == i) {
                    last = 1;
                } else {
                    last = 0;
                }
                RegularRelayBtnView testingClass = new RegularRelayBtnView(mActivity, mListener, mSeekListener, mLongListener);
                View newView = testingClass.relayButton(mActivity, list.get(i), roomStatusPos + "" + (i + 1), roomId, 1);

                if (null == roomAppliancesIndexInLinear) {
                    mainLl.addView(newView);
                    mTempIndexes.add(mainLl.getChildCount() - 1);
                } else {
                    View oldView = mainLl.getChildAt(roomAppliancesIndexInLinear.get(i));
                    ViewGroupUtils.replaceView(oldView, newView);
                }


            } else if (appType == 'C' || appType == 'H' || appType == 'J' || appType == 'K' || appType == 'L') {
                Utils.APP_CNT = Utils.APP_CNT + 1;
                if (list.size() - 1 == i) {
                    last = 1;
                } else {
                    last = 0;
                }

                IRClassView testingClassTv = new IRClassView(mActivity, mListener, mLongListener);
                View newView = testingClassTv.relayButton(mActivity, list.get(i), ij, roomId, last);

                if (null == roomAppliancesIndexInLinear) {
                    mainLl.addView(newView);
                    mTempIndexes.add(mainLl.getChildCount() - 1);
                } else {
                    View oldView = mainLl.getChildAt(roomAppliancesIndexInLinear.get(i));
                    ViewGroupUtils.replaceView(oldView, newView);
                }


            } else if (appType == 'D') {

                Utils.APP_CNT = Utils.APP_CNT + 1;
                if (list.size() - 1 == i) {
                    last = 1;
                } else {
                    last = 0;
                }

                AcClassView testingClassAc = new AcClassView(mActivity, mListener, mTempListener, mLongListener);
                View newView = testingClassAc.relayButton(mActivity, list.get(i), ij, last);

                if (null == roomAppliancesIndexInLinear) {
                    mainLl.addView(newView);
                    mTempIndexes.add(mainLl.getChildCount() - 1);
                } else {
                    View oldView = mainLl.getChildAt(roomAppliancesIndexInLinear.get(i));
                    ViewGroupUtils.replaceView(oldView, newView);
                }

            }
        }

    }

    @Override
    public void onMqttFailure() {
        if (null != swipeRefreshLayout && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void onDeliveryComplete() {
        if (null != swipeRefreshLayout && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i/*, boolean b*/) {
        if (seekBar.getId() == R.id.dimm_seek_level) {
            int roomIdAppPos[] = splitStringTag(seekBar.getTag().toString());
            int roomId = mSingleRoomArrLst.get(roomIdAppPos[0]).getRoomId();
            String mqqtCmd = "B|" + roomId + "|" + roomIdAppPos[1] + i;
            helper.sendMsg(mqqtCmd);
        }
    }

    @Override
    public void onLongClick(View view) {
        switch (view.getId()) {
            case tv_relay_btn:
                showPopup(view);
                break;
            case R.id.tv_relay_tv:
                showPopup(view);
                break;
            case R.id.tv_relay_ac:
                showPopup(view);
                break;
        }
    }

    private void showPopup(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.revert_timer_dialog, null);

        fiveMTv1 = popupView.findViewById(R.id.five_m_tv_1);
        fifteenMTv1 = popupView.findViewById(R.id.fifteen_m_tv_1);
        thirtyMTv1 = popupView.findViewById(R.id.thirty_m_tv_1);
        sixtyMTv1 = popupView.findViewById(R.id.sixty_m_tv_1);
        nrMTv1 = popupView.findViewById(R.id.nr_tv_1);

        fiveMTv = popupView.findViewById(R.id.five_m_tv);
        fifteenMTv = popupView.findViewById(R.id.fifteen_m_tv);
        thirtyMTv = popupView.findViewById(R.id.thirty_m_tv);
        sixtyMTv = popupView.findViewById(R.id.sixty_m_tv);
        nrMTv = popupView.findViewById(R.id.nr_tv);

        fiveMTv1.setOnClickListener(this);
        fifteenMTv1.setOnClickListener(this);
        thirtyMTv1.setOnClickListener(this);
        sixtyMTv1.setOnClickListener(this);
        nrMTv1.setOnClickListener(this);

        fiveMTv.setOnClickListener(this);
        fifteenMTv.setOnClickListener(this);
        thirtyMTv.setOnClickListener(this);
        sixtyMTv.setOnClickListener(this);
        nrMTv.setOnClickListener(this);

        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        //popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //TODO do sth here on dismiss
            }
        });
        popupWindow.showAsDropDown(v);
        lngView = v;
    }

    @Override
    public void onRefresh() {
        if (Utils.isNetworkAvailable) {
            //pd.show();
            //helper.sendMsg("C|0");
            //helper.sendMsg("H");
        } else {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    //Toast.makeText(context, "Failed to update, Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(context, "Failed to update, Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }, 10000);*/
    }

    // For Quick Timers Click Event..!
    @Override
    public void onClick(View view) {
        int roomIdAppArr[] = Utils.splitStringTag(lngView.getTag().toString());
        int roomId = mSingleRoomArrLst.get(roomIdAppArr[0]).getRoomId();
        int appPos = roomIdAppArr[1];
        String appStatus = "";
        int timerTime = 0;
        int isFromAction1 = 0;
        String ab = "";
        for (int i = 0; i < mSingleRoomArrLst.size(); i++) {
            if (roomId == mSingleRoomArrLst.get(i).getRoomId()) {
                ArrayList<String> list = mSingleRoomArrLst.get(i).getAppDimmTypeStatusArrLst();
                for (int j = 0; j < list.size(); j++) {
                    if (j + 1 == appPos) {
                        /*if (list.get(j).charAt(2) == '1') {
                            appStatus = "0";
                        } else {
                            appStatus = "1";
                        }*/
                        appStatus = String.valueOf(list.get(j).charAt(2));
                        break;
                    }
                }
            }
        }

        switch (view.getId()) {
            //------------Action - A----------
            case R.id.five_m_tv_1:
                mWhichBtnInt = 5;
                timerTime = 5;
                mTimerTime = 0;
                mTimerTime = timerTime;
                isFromAction1 = 1;
                ab = "A";
                setBGToTxtView();
                break;
            case R.id.fifteen_m_tv_1:
                mWhichBtnInt = 15;
                timerTime = 15;
                mTimerTime = 0;
                mTimerTime = timerTime;
                isFromAction1 = 1;
                ab = "A";
                setBGToTxtView();
                break;
            case R.id.thirty_m_tv_1:
                mWhichBtnInt = 30;
                timerTime = 30;
                mTimerTime = 0;
                mTimerTime = timerTime;
                isFromAction1 = 1;
                ab = "A";
                setBGToTxtView();
                break;
            case R.id.sixty_m_tv_1:
                mWhichBtnInt = 60;
                timerTime = 60;
                mTimerTime = 0;
                mTimerTime = timerTime;
                isFromAction1 = 1;
                ab = "A";
                setBGToTxtView();
                break;
            case R.id.nr_tv_1:
                timerTime = 0;
                mTimerTime = 0;
                mTimerTime = timerTime;
                isFromAction1 = 1;
                ab = "A";
                if (popupWindow != null)
                    popupWindow.dismiss();
                break;
            //------------Action - B----------
            case R.id.five_m_tv:
                if (mTimerTime != 0) {
                    timerTime = 5;
                    mTimerTime += timerTime;
                    mWhichBtnInt = 100;
                    isFromAction1 = 2;
                    ab = "B";
                    setBGToTxtView();
                }
                break;
            case R.id.fifteen_m_tv:
                if (mTimerTime != 0) {
                    timerTime = 15;
                    mTimerTime += timerTime;
                    mWhichBtnInt = 99;
                    isFromAction1 = 2;
                    ab = "B";
                    setBGToTxtView();
                }
                break;
            case R.id.thirty_m_tv:
                if (mTimerTime != 0) {
                    timerTime = 30;
                    mTimerTime += timerTime;
                    mWhichBtnInt = 98;
                    isFromAction1 = 2;
                    ab = "B";
                    setBGToTxtView();
                }
                break;
            case R.id.sixty_m_tv:
                if (mTimerTime != 0) {
                    timerTime = 60;
                    mTimerTime += timerTime;
                    mWhichBtnInt = 97;
                    isFromAction1 = 2;
                    ab = "B";
                    setBGToTxtView();
                }
                break;
            case R.id.nr_tv:
                if (mTimerTime != 0) {
                    timerTime = 0;
                    mTimerTime = 0;
                    mTimerTime += timerTime;
                    isFromAction1 = 2;
                    ab = "B";
                    setBGToTxtView();
                }
                break;
        }

        if (timerTime > 0) {
            String appStatusR = "";

            switch (isFromAction1) {
                case 1:
                    if (appStatus.equals("1")) {
                        appStatusR = "0";
                    } else {
                        appStatusR = "1";
                    }
                    setQuickTimer(timerTime, roomId + ":" + appPos, appStatusR, roomId, appPos, ab);
                    break;
                case 2:
                    setQuickTimer(mTimerTime, roomId + ":" + appPos, appStatus, roomId, appPos, ab);
                    break;
            }

        }
    }

    private void setBGToTxtView() {
        //fiveMTv, fifteenMTv, thirtyMTv, sixtyMTv, nrMTv, fiveMTv1, fifteenMTv1, thirtyMTv1, sixtyMTv1, nrMTv1
        int colorPrimary = getColorPrimary(context);
        switch (mWhichBtnInt) {
            case 5:
                fiveMTv1.setTextColor(ContextCompat.getColor(context, R.color.white));
                fifteenMTv1.setTextColor(colorPrimary);
                thirtyMTv1.setTextColor(colorPrimary);
                sixtyMTv1.setTextColor(colorPrimary);

                fiveMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                fifteenMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                thirtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                sixtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                break;
            case 15:
                fiveMTv1.setTextColor(colorPrimary);
                fifteenMTv1.setTextColor(ContextCompat.getColor(context, R.color.white));
                thirtyMTv1.setTextColor(colorPrimary);
                sixtyMTv1.setTextColor(colorPrimary);

                fiveMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                fifteenMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                thirtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                sixtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                break;
            case 30:
                fiveMTv1.setTextColor(colorPrimary);
                fifteenMTv1.setTextColor(colorPrimary);
                thirtyMTv1.setTextColor(ContextCompat.getColor(context, R.color.white));
                sixtyMTv1.setTextColor(colorPrimary);

                fiveMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                fifteenMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                thirtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                sixtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                break;
            case 60:
                fiveMTv1.setTextColor(colorPrimary);
                fifteenMTv1.setTextColor(colorPrimary);
                thirtyMTv1.setTextColor(colorPrimary);
                sixtyMTv1.setTextColor(ContextCompat.getColor(context, R.color.white));

                fiveMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                fifteenMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                thirtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                sixtyMTv1.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                break;
            case 100:
                fiveMTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                fifteenMTv.setTextColor(colorPrimary);
                thirtyMTv.setTextColor(colorPrimary);
                sixtyMTv.setTextColor(colorPrimary);

                fiveMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                fifteenMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                thirtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                sixtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                break;
            case 99:
                fiveMTv.setTextColor(colorPrimary);
                fifteenMTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                thirtyMTv.setTextColor(colorPrimary);
                sixtyMTv.setTextColor(colorPrimary);

                fiveMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                fifteenMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                thirtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                sixtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                break;
            case 98:
                fiveMTv.setTextColor(colorPrimary);
                fifteenMTv.setTextColor(colorPrimary);
                thirtyMTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                sixtyMTv.setTextColor(colorPrimary);


                fiveMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                fifteenMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                thirtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                sixtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                break;
            case 97:
                fiveMTv.setTextColor(colorPrimary);
                fifteenMTv.setTextColor(colorPrimary);
                thirtyMTv.setTextColor(colorPrimary);
                sixtyMTv.setTextColor(ContextCompat.getColor(context, R.color.white));

                fiveMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                fifteenMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                thirtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_white);
                sixtyMTv.setBackgroundResource(R.drawable.rectangle_et_style_ovel_theme_bg);
                break;
        }
    }

    private void setQuickTimer(int i, String relays, String appStatus, int roomId, int appPos, final String ab) {
        String finalUrl = ServiceHandler.baseUrl + "SetQuickTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=3&Value="
                + "QuickTimer" + ab + +roomId + appPos + "-" + relays + "-" + "1" + "-" + getCurrentDateTime(i)
                + "-" + "0" + "-" + "0000000" + "-" + appStatus;
        Utils.printLog(TAG, "finalUrl:-:" + finalUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (ab.equals("B")) {
                    if (popupWindow != null)
                        popupWindow.dismiss();
                    mTimerTime = 0;
                }
                Utils.showToast(context, "Quick Timer created");
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRClientRequest()", "Error : " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    public interface ActiveCountListener {
        void onCountChangeListener(int totalActiveCnt, int rumId);
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
