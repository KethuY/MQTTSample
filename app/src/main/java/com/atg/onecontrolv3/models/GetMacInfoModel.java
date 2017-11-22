package com.atg.onecontrolv3.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Bharath on 08-Apr-17
 */

public class GetMacInfoModel implements Serializable {

    private int activeCount;
    private String info;
    private int numOfRelays;
    private int RoomId;
    private String roomName;
    private String autoRevoke;
    private int activeListItemCnt;
    //From RoomWiseTimer..!
    private boolean isChecked;
    private ArrayList<String> appDimmTypeStatusArrLst;


    public int getActiveListItemCnt() {
        return activeListItemCnt;
    }

    public void setActiveListItemCnt(int activeListItemCnt) {
        this.activeListItemCnt = activeListItemCnt;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getNumOfRelays() {
        return numOfRelays;
    }

    public void setNumOfRelays(int numOfRelays) {
        this.numOfRelays = numOfRelays;
    }

    public int getRoomId() {
        return RoomId;
    }

    public void setRoomId(int roomId) {
        RoomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getAutoRevoke() {
        return autoRevoke;
    }

    public void setAutoRevoke(String autoRevoke) {
        this.autoRevoke = autoRevoke;
    }

    public ArrayList<String> getAppDimmTypeStatusArrLst() {
        return appDimmTypeStatusArrLst;
    }

    public void setAppDimmTypeStatusArrLst(ArrayList<String> appDimmTypeStatusArrLst) {
        this.appDimmTypeStatusArrLst = appDimmTypeStatusArrLst;
    }

    //From RoomWiseTimer..!

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
