package com.atg.onecontrolv3.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.SceneEditActivity;
import com.atg.onecontrolv3.adapters.SceneTimerAdapter;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.MqttRoomStatusModel;
import com.atg.onecontrolv3.models.SceneTimerModel;
import com.atg.onecontrolv3.models.SingleRoomModel;
import com.atg.onecontrolv3.models.SingleSceneTimerModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.rest.ApiClient;
import com.atg.onecontrolv3.rest.ApiInterface;

import java.util.ArrayList;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Bharath on 9/22/2017
 */

public class ScenesTimerFragment extends Fragment implements View.OnClickListener, OnItemClickListener, MqttHelper.responseListener {
    private static final String TAG = "ScenesTimerFragment";
    ListView mTimerDbLv;
    OnItemClickListener mListener;
    MqttHelper.responseListener mqttListener = null;
    //ArrayList<GetTimersModel> getTimersModelArrLst;
    private TextView mEditTv, mAddSceneTv;
    private TransparentProgressDialog pd;
    private ArrayList<SingleSceneTimerModel> singleSceneTimerModelArrLst;
    private boolean isViewShown = false;
    private Activity mActivity;
    private MqttHelper helper;
    private ArrayList<SingleRoomModel> mSingleRoomArrLst;
    private boolean isAllRoomsStatusEquals = false;
    private ArrayList<TempRoom> mTempRooms;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scenes_timer_fragment, container, false);
        if (!isViewShown) {
            initializeViews(view);
            Utils.getTimerModelObj = null;
            if (Utils.isNetworkAvailable) {
                getTimerDashboard();
            } else {
                Utils.showMessageDialog("No Internet.", getActivity());
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = this;

        mqttListener = this;
        helper = new MqttHelper(mActivity, mqttListener);
    }

    private void initializeViews(View view) {
        pd = new TransparentProgressDialog(mActivity, R.drawable.progress);
        mTimerDbLv = view.findViewById(R.id.timer_db_lv);
        mEditTv = view.findViewById(R.id.edit_tv);
        mAddSceneTv = view.findViewById(R.id.floatingBtnAddTimer);
        mAddSceneTv.setOnClickListener(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            isViewShown = true;
            Utils.getTimerModelObj = null;
            if (Utils.isNetworkAvailable) {
                getTimerDashboard();
            } else {
                Utils.showMessageDialog("No Internet.", getActivity());
            }
        } else {
            isViewShown = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.getTimerModelObj = null;
        if (Utils.isNetworkAvailable) {
            if (Utils.mSingleRoomArrLst != null && !Utils.mSingleRoomArrLst.isEmpty()) {
                mSingleRoomArrLst = Utils.mSingleRoomArrLst;
            }
            helper.sendMsg("C|0");
        } else {
            Utils.showMessageDialog("No Internet.", getActivity());
        }
    }

    public void getTimerDashboard() {
        pd.show();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<SceneTimerModel> call = apiService.getScene(Utils.MAC_ID, Utils.IMEI);
        call.enqueue(new Callback<SceneTimerModel>() {
            @Override
            public void onResponse(Call<SceneTimerModel> call, Response<SceneTimerModel> response) {
                String strUrl = response.raw().request().url().toString();
                Utils.printLog(TAG, "strUrl:-:" + strUrl);
                singleSceneTimerModelArrLst = response.body().getSingleSceneTimerModelArrLst();
                if (null != singleSceneTimerModelArrLst && !singleSceneTimerModelArrLst.isEmpty()) {
                    setDataToAdapter();
                } else {
                    pd.dismiss();
                    //Utils.showMessageDialog("Unable to fetch data", getContext());
                }
            }

            @Override
            public void onFailure(Call<SceneTimerModel> call, Throwable t) {
                pd.dismiss();
                Utils.printLog(TAG, "onFailure:-:" + t.getMessage());
            }
        });
    }

    private void setDataToAdapter() {
        SceneTimerAdapter adapter = new SceneTimerAdapter(getContext(), singleSceneTimerModelArrLst, mListener);
        mTimerDbLv.setAdapter(adapter);
        pd.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floatingBtnAddTimer:
                Utils.getScenesObj = null;
                getActivity().startActivity(new Intent(getActivity(), SceneEditActivity.class).putExtra("IsFrom", 1));
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.main_ll:
                if (Utils.isNetworkAvailable) {
                    final String relays = singleSceneTimerModelArrLst.get(position).getRelays();
                    new TriggerSceneTask().execute(relays);
                } else {
                    Utils.showMessageDialog("No Internet.", getActivity());
                }
                break;
        }
    }

    @Override
    public void onMqttResponse(String res) {
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
            if (!arr.isEmpty()) {
                for (int i = 0; i < mSingleRoomArrLst.size(); i++) {
                    if (mqttRoomStatusModel.getRoomId() == mSingleRoomArrLst.get(i).getRoomId()) {
                        mSingleRoomArrLst.get(i).setAutoRevoke(mqttRoomStatusModel.getAutoRevoke());
                        mSingleRoomArrLst.get(i).setAppDimmTypeStatusArrLst(mqttRoomStatusModel.getAppDimTypeStatusArrLst());
                        //break;
                    }
                }
            }


        }
    }

    private ArrayList<String> getArrList(String info) {
        ArrayList<String> arr = new ArrayList<>();
        int index = 0;
        while (index < info.length()) {
            String str = info.substring(index, Math.min(index + 3, info.length()));
            arr.add(str);
            index += 3;
        }
        return arr;
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }

    private void triggerTimerAppliance(String relays) {
        if (relays.length() > 0) {
            isAllRoomsStatusEquals = false;
            if (relays.contains(",")) {
                String[] roomIdsApp = relays.split(",");
                for (String aRoomIdsApp : roomIdsApp) {
                    getSingleRumStatus(aRoomIdsApp);
                }
            } else {
                getSingleRumStatus(relays);
            }
        }
    }

    private void getSingleRumStatus(String aRoomIdsApp) {
        String[] appliances = aRoomIdsApp.split(":");//4:23
        int roomId = Integer.parseInt(appliances[0]);
        char[] appArr = appliances[1].toCharArray();

        for (int i = 0; i < mSingleRoomArrLst.size(); i++) {
            int rumId = mSingleRoomArrLst.get(i).getRoomId();

            if (roomId == rumId) {

                ArrayList<String> appStatusArrLst = mSingleRoomArrLst.get(i).getAppDimmTypeStatusArrLst();

                for (char anAppArr : appArr) {
                    String status = appStatusArrLst.get(Character.getNumericValue(anAppArr) - 1);
                    TempRoom room = new TempRoom();
                    room.setRoomId(roomId);
                    room.setApplianceId(Character.getNumericValue(anAppArr));

                    if (status.charAt(2) == '1') {
                        isAllRoomsStatusEquals = true;
                        room.setStatusOn(true);
                    }
                    Utils.printLog(TAG, "room:-:" + room);

                    mTempRooms.add(room);

                    //mTempRooms data as like 2:2345,3:12
                    // roomId applianceid on/off sta
                    //2 2 true/false
                    //2 3 true/false
                    //2 4 true/false
                    //2 5 true/false
                    //3 1 true/false
                    //3 2 true/false
                }
                break;
            }
        }
        // checking all 0's/1's/unEquality

        if (null != mTempRooms) {
            if (isAllRoomsStatusEquals) {//
                for (int i = 0; i < mTempRooms.size(); i++) {
                    boolean onStatus = mTempRooms.get(i).isStatusOn();
                    if (onStatus) {
                        helper.sendMsg("A|" + mTempRooms.get(i).getRoomId() + "|" + mTempRooms.get(i).getApplianceId());
                    }
                }
                isAllRoomsStatusEquals = false;
            } else {//All zeros/ones
                for (int i = 0; i < mTempRooms.size(); i++) {
                    helper.sendMsg("A|" + mTempRooms.get(i).getRoomId() + "|" + mTempRooms.get(i).getApplianceId());
                }
            }
        }

        mTempRooms.clear();
    }

    private class TriggerSceneTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTempRooms = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(String... relays) {
            triggerTimerAppliance(relays[0]);
            return null;
        }
    }

    private class TempRoom {
        int roomId;
        int applianceId;
        boolean isStatusOn;

        public int getApplianceId() {
            return applianceId;
        }

        public void setApplianceId(int applianceId) {
            this.applianceId = applianceId;
        }

        public boolean isStatusOn() {
            return isStatusOn;
        }

        public void setStatusOn(boolean statusOn) {
            isStatusOn = statusOn;
        }

        public int getRoomId() {
            return roomId;
        }

        public void setRoomId(int roomId) {
            this.roomId = roomId;
        }
    }
}
