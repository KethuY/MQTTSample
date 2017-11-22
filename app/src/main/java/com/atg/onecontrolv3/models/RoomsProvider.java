package com.atg.onecontrolv3.models;

import android.content.Context;

import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.helpers.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


/**
 * Created by Ramprasad on 8/13/2016
 */
public class RoomsProvider {

    private static final String TAG = "RoomsProvider";
    //    public String MACID = "5003";
//    public String IMEI = "865374021439230";
    Timer timer;
    private JSONObject response;
    //public String MACID = "5002";//5001
    //public String IMEI = "354115071374597";//352087074323842
    private Context ctx;

    public RoomsProvider(Context ctx) {
        this.ctx = ctx;
        timer = new Timer();
//        nProvider = new NotificationProvider(ctx);
//        ocTimerTask = new OCTimerTask(ctx);
    }

    public List<RoomStatusModel> getRoomStatusFromServer(String roomId) {
        List<RoomStatusModel> list = new ArrayList<RoomStatusModel>();
        try {
            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&ReceiverId=" + roomId;
            response = serviceHandler.getJSONFromUrl("GetReceiversStatus", args);

            RoomStatusModel model;
            JSONArray jsonArray = response.getJSONArray("ReceiverStatus");

            ArrayList<String> strAppType = new ArrayList<>();
            ArrayList<Boolean> bStatusType = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                model = new RoomStatusModel();
                JSONObject dataObj = jsonArray.getJSONObject(i);

                String macId = dataObj.getString("MacId");
                String roomName = dataObj.getString("RName");
                String receiverId = dataObj.getString("ReceiverId");
                String receiverInfoId = dataObj.getString("ReceiversInfoId");
                String status = dataObj.getString("Status");

                String status1 = dataObj.getString("S1");
                String status2 = dataObj.getString("S2");
                String status3 = dataObj.getString("S3");
                String status4 = dataObj.getString("S4");
                String status5 = dataObj.getString("S5");
                String status6 = dataObj.getString("S6");
                String status7 = dataObj.getString("S7");
                String status8 = dataObj.getString("S8");

                bStatusType.add(Boolean.parseBoolean(status1));
                bStatusType.add(Boolean.parseBoolean(status2));
                bStatusType.add(Boolean.parseBoolean(status3));
                bStatusType.add(Boolean.parseBoolean(status4));
                bStatusType.add(Boolean.parseBoolean(status5));
                bStatusType.add(Boolean.parseBoolean(status6));
                bStatusType.add(Boolean.parseBoolean(status7));
                bStatusType.add(Boolean.parseBoolean(status8));

                String type1 = dataObj.getString("T1");
                String type2 = dataObj.getString("T2");
                String type3 = dataObj.getString("T3");
                String type4 = dataObj.getString("T4");
                String type5 = dataObj.getString("T5");
                String type6 = dataObj.getString("T6");
                String type7 = dataObj.getString("T7");
                String type8 = dataObj.getString("T8");

                strAppType.add(type1);
                strAppType.add(type2);
                strAppType.add(type3);
                strAppType.add(type4);
                strAppType.add(type5);
                strAppType.add(type6);
                strAppType.add(type7);
                strAppType.add(type8);


                model.setMacId(macId);
                model.setReceiverId(receiverId);
                model.setReceiverInfoId(receiverInfoId);
                model.setRoomName(roomName);
                model.setStatus(status);

                model.setStatus1(status1);
                model.setStatus2(status2);
                model.setStatus3(status3);
                model.setStatus4(status4);
                model.setStatus5(status5);
                model.setStatus6(status6);
                model.setStatus7(status7);
                model.setStatus8(status8);

                model.setType1(type1);
                model.setType2(type2);
                model.setType3(type3);
                model.setType4(type4);
                model.setType5(type5);
                model.setType6(type6);
                model.setType7(type7);
                model.setType8(type8);


                model.setApplianceTypeArrLst(strAppType);
                model.setAppStatusArrLst(bStatusType);

                list.add(model);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<RoomsModel> getRoomsFromServer() {
        ArrayList<RoomsModel> list = new ArrayList<RoomsModel>();
        try {

            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
            response = serviceHandler.getJSONFromUrl("GetAllRoomsInfo", args);

            if (response == null) {
                return null;
            }

            RoomsModel model;
            JSONArray jsonArray = response.getJSONArray("RoomsInfo");

            for (int i = 0; i < jsonArray.length(); i++) {

                model = new RoomsModel();
                JSONObject dataObj = jsonArray.getJSONObject(i);

                String numOfRelays = dataObj.getString("NumOfRelays");
                String roomId = dataObj.getString("RoomId");
                String roomName = dataObj.getString("RoomName");
                String activeCount = dataObj.getString("ActiveCount");

                model.setNumOfRelays(numOfRelays);
                model.setRoomId(roomId);
                model.setRoomName(roomName.contains("~") ? roomName.replace("~", " ") : roomName);
                model.setActiveCount(activeCount);

                list.add(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public ArrayList<ApplianceWattageModel> getPowerRatings(String roomNumber) {
        ArrayList<ApplianceWattageModel> applianceWattArr = new ArrayList<>();
        ServiceHandler serviceHandler = new ServiceHandler();
        String args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&RoomNumber=" + roomNumber;
        try {
            response = serviceHandler.getJSONFromUrl("GetPowerRatings", args);
            if (response == null) {
                return null;
            }
            ApplianceWattageModel applianceWattModel;
            ArrayList<Integer> wattagesArr = new ArrayList<>();
            JSONArray jsonArray = response.getJSONArray("Power Ratings");
            for (int i = 0; i < jsonArray.length(); i++) {
                applianceWattModel = new ApplianceWattageModel();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                applianceWattModel.setId(jsonObject.getInt("Id"));
               /* applianceWattModel.setR1(jsonObject.getInt("R1"));
                applianceWattModel.setR2(jsonObject.getInt("R2"));
                applianceWattModel.setR3(jsonObject.getInt("R3"));
                applianceWattModel.setR4(jsonObject.getInt("R4"));
                applianceWattModel.setR5(jsonObject.getInt("R5"));
                applianceWattModel.setR6(jsonObject.getInt("R6"));
                applianceWattModel.setR7(jsonObject.getInt("R7"));
                applianceWattModel.setR8(jsonObject.getInt("R8"));*/
                applianceWattModel.setRoomNumber(jsonObject.getInt("RoomNumber"));
                applianceWattModel.setMacId(jsonObject.getString("MacId"));

                ArrayList<Integer> arrayList = new ArrayList<>();
                arrayList.add(jsonObject.getInt("R1"));
                arrayList.add(jsonObject.getInt("R2"));
                arrayList.add(jsonObject.getInt("R3"));
                arrayList.add(jsonObject.getInt("R4"));
                arrayList.add(jsonObject.getInt("R5"));
                arrayList.add(jsonObject.getInt("R6"));
                arrayList.add(jsonObject.getInt("R7"));
                arrayList.add(jsonObject.getInt("R8"));

                applianceWattModel.setWattagesArr(arrayList);
                applianceWattArr.add(applianceWattModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applianceWattArr;
    }

    public List<RoomStatusModel> getRoomStatusFromServer(String roomId, int dummy) {
        List<RoomStatusModel> list = new ArrayList<RoomStatusModel>();
        try {

            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&ReceiverId=" + roomId;
            response = serviceHandler.getJSONFromUrl("GetReceiversStatus", args);

            RoomStatusModel model;
            JSONArray jsonArray = response.getJSONArray("ReceiverStatus");

            for (int i = 0; i < jsonArray.length(); i++) {

                model = new RoomStatusModel();

                JSONObject dataObj = jsonArray.getJSONObject(i);

                String macId = dataObj.getString("MacId");
                String roomName = dataObj.getString("RName");
                String receiverId = dataObj.getString("ReceiverId");
                String receiverInfoId = dataObj.getString("ReceiversInfoId");
                String status = dataObj.getString("Status");

                String status1 = dataObj.getString("S1");
                String status2 = dataObj.getString("S2");
                String status3 = dataObj.getString("S3");
                String status4 = dataObj.getString("S4");
                String status5 = dataObj.getString("S5");
                String status6 = dataObj.getString("S6");
                String status7 = dataObj.getString("S7");
                String status8 = dataObj.getString("S8");

                String type1 = dataObj.getString("T1");
                String type2 = dataObj.getString("T2");
                String type3 = dataObj.getString("T3");
                String type4 = dataObj.getString("T4");
                String type5 = dataObj.getString("T5");
                String type6 = dataObj.getString("T6");
                String type7 = dataObj.getString("T7");
                String type8 = dataObj.getString("T8");

                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(type1);
                arrayList.add(type2);
                arrayList.add(type3);
                arrayList.add(type4);
                arrayList.add(type5);
                arrayList.add(type6);
                arrayList.add(type7);
                arrayList.add(type8);

                model.setApplianceTypeArrLst(arrayList);


                model.setMacId(macId);
                model.setReceiverId(receiverId);
                model.setReceiverInfoId(receiverInfoId);
                model.setRoomName(roomName);
                model.setStatus(status);

                model.setStatus1(status1);
                model.setStatus2(status2);
                model.setStatus3(status3);
                model.setStatus4(status4);
                model.setStatus5(status5);
                model.setStatus6(status6);
                model.setStatus7(status7);
                model.setStatus8(status8);

                model.setType1(type1);
                model.setType2(type2);
                model.setType3(type3);
                model.setType4(type4);
                model.setType5(type5);
                model.setType6(type6);
                model.setType7(type7);
                model.setType8(type8);

                list.add(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<SubUserRoomsModel> getSubUserRomms(String userId) {
        ArrayList<SubUserRoomsModel> listArr = new ArrayList<>();
        ServiceHandler serviceHandler = new ServiceHandler();
        String args = "UserId=" + userId + "&UID=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        try {
            response = serviceHandler.getJSONFromUrl("GetSubUserRooms", args);
            if (response == null) {
                return null;
            }
            SubUserRoomsModel subUserRoomsModel;
            JSONArray jsonArray = response.getJSONArray("SubUserRooms");

            for (int i = 0; i < jsonArray.length(); i++) {
                subUserRoomsModel = new SubUserRoomsModel();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int activeCount = jsonObject.getInt("ActiveCount");
                int numOfRelays = jsonObject.getInt("NumOfRelays");
                String roomName = jsonObject.getString("RoomName");
                String roomNumber = jsonObject.getString("RoomNumber");
                subUserRoomsModel.setActiveCount(activeCount);
                subUserRoomsModel.setNumOfRelays(numOfRelays);
                subUserRoomsModel.setRoomName(roomName.contains("~") ? roomName.replaceAll("~", " ") : roomName);
                subUserRoomsModel.setRoomNumber(roomNumber);

                listArr.add(subUserRoomsModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listArr;
    }
}
