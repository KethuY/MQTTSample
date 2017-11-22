package com.atg.onecontrolv3.models;

import java.util.ArrayList;

/**
 * Created by Bharath on 12-Sep-17
 */

public class MqttRoomStatusModel {

    private int roomId;
    private String autoRevoke;
    private ArrayList<String> appDimTypeStatusArrLst;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getAutoRevoke() {
        return autoRevoke;
    }

    public void setAutoRevoke(String autoRevoke) {
        this.autoRevoke = autoRevoke;
    }

    public ArrayList<String> getAppDimTypeStatusArrLst() {
        return appDimTypeStatusArrLst;
    }

    public void setAppDimTypeStatusArrLst(ArrayList<String> appDimTypeStatusArrLst) {
        this.appDimTypeStatusArrLst = appDimTypeStatusArrLst;
    }
}
