package com.atg.onecontrolv3.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Bharath on 07-Sep-17
 */

public class SingleRoomModel {
    @SerializedName("ActiveCount")
    private int activeCount;
    @SerializedName("Info")
    private String info;
    @SerializedName("NumOfRelays")
    private int numOfRelays;
    @SerializedName("RoomId")
    private int RoomId;
    @SerializedName("RoomName")
    private String roomName;

    private String itemTag;

    public int mIndex;
    private String AutoRevoke;
    private int arrayPos;
    private ArrayList<Integer> appPosArrLst;
    private ArrayList<String> appDimmTypeStatusArrLst;

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
        return AutoRevoke;
    }

    public void setAutoRevoke(String autoRevoke) {
        AutoRevoke = autoRevoke;
    }

    public ArrayList<Integer> getAppPosArrLst() {
        return appPosArrLst;
    }

    public void setAppPosArrLst(ArrayList<Integer> appPosArrLst) {
        this.appPosArrLst = appPosArrLst;
    }

    public int getArrayPos() {
        return arrayPos;
    }

    public void setArrayPos(int arrayPos) {
        this.arrayPos = arrayPos;
    }

    public ArrayList<String> getAppDimmTypeStatusArrLst() {
        return appDimmTypeStatusArrLst;
    }

    public void setAppDimmTypeStatusArrLst(ArrayList<String> appDimmTypeStatusArrLst) {
        this.appDimmTypeStatusArrLst = appDimmTypeStatusArrLst;
    }

    public String getItemTag() {
        return itemTag;
    }

    public void setItemTag(String itemTag) {
        this.itemTag = itemTag;
    }
}
