package com.atg.onecontrolv3.models;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.helpers.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sireesha on 17-09-2016
 */
public class EnergyMngmtProvider {

    JSONObject response;
    //public String MACID = "5002";//5001
    //public String IMEI = "354115071374597";//352087074323842

    //    public String MACID = "5003";
//    public String IMEI = "865374021439230";


    //SetUnPairReciever
    public List<EnergyMnngmtModel> GetEMSData(String callType, String roomId, String date, String fromDate, String toDate, String month, String year) {
        String statusMsg = null;
        List<EnergyMnngmtModel> list = new ArrayList<EnergyMnngmtModel>();
        String errorCode;
        try {
            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "";
            String methodName = "";

            if(callType.equalsIgnoreCase("Day")){
                args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&RoomId=" + roomId + "&Date=" + date;
                methodName = "GetTotalEnergyDataDayWiseSum";
            } else if (callType.equalsIgnoreCase("Week") || callType.equalsIgnoreCase("Date_Selection")){
                args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&RoomId=" + roomId + "&FromDate=" + fromDate + "&ToDate=" + toDate;
                methodName = "GetTotalEnergyDataWeekWiseSum";
            } else if(callType.equalsIgnoreCase("Month")){
                args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&RoomId=" + roomId + "&Month=" + month + "&Year=" + year;
                methodName = "GetTotalEnergyDataMonthWiseSum";
            }

            response = serviceHandler.getJSONFromUrl(methodName, args);

            //Call and clear Notifications
           /* NotificationProvider nProvider = new NotificationProvider();
            list = nProvider.GetNotifications();
            if(list.size() != 0){
                for(int i=0;i<list.size();i++){
                    errorCode = list.get(i).getErrorCode();
                    if(errorCode.equals("8")){
                        statusMsg = "Success";
                    }else if(errorCode.equals("21")){
                        statusMsg = "Error";
                    }

                    if(errorCode.equals("8") || errorCode.equals("21")){
                        nProvider.ClearNotification(errorCode);
                    }
                }
            }*/

            EnergyMnngmtModel model;
            JSONObject Obj = new JSONObject(response.toString());
            JSONArray jsonArray = Obj.getJSONArray("Energy Data");
            for (int i = 0; i < jsonArray.length(); i++) {

                model = new EnergyMnngmtModel();
                JSONObject dataObj = jsonArray.getJSONObject(i);

                String macId = dataObj.getString("MacId");
                String roomNumber = dataObj.getString("RoomNumber");
                String roomName = dataObj.getString("RoomName");
                String usageTime = dataObj.getString("UsageTime");

                if(usageTime.contains("+0000")) usageTime = usageTime.replace("+0000", "");

                String relay1 = dataObj.getString("R1");
                String relay2 = dataObj.getString("R2");
                String relay3 = dataObj.getString("R3");
                String relay5 = dataObj.getString("R5");
                String relay4 = dataObj.getString("R4");
                String relay6 = dataObj.getString("R6");
                String relay7 = dataObj.getString("R7");
                String relay8 = dataObj.getString("R8");

                if(roomId.equals("0")) {
                    double dr1 = Double.parseDouble(relay1);
                    double dr2 = Double.parseDouble(relay2);
                    double dr3 = Double.parseDouble(relay3);
                    double dr4 = Double.parseDouble(relay4);
                    double dr5 = Double.parseDouble(relay5);
                    double dr6 = Double.parseDouble(relay6);
                    double dr7 = Double.parseDouble(relay7);
                    double dr8 = Double.parseDouble(relay8);
                    model.setAllRoomsTotal((dr1 + dr2 + dr3 + dr4 + dr5 + dr6 + dr7 + dr8) + "");
                }
                model.setMacId(macId);
                model.setRoomNumber(roomNumber);
                model.setRoomName(roomName);
                model.setUsageTime(usageTime);

                model.setRelay1(relay1);
                model.setRelay2(relay2);
                model.setRelay3(relay3);
                model.setRelay4(relay4);
                model.setRelay5(relay5);
                model.setRelay6(relay6);
                model.setRelay7(relay7);
                model.setRelay8(relay8);

                list.add(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
