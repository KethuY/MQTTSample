package com.atg.onecontrolv3.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Bharath on 21-Jun-17
 */

public class PIRDBModel implements Serializable {
    //private int pirId;
    //private String relays;
    //private int roomId;
    //private String uid;

    private String pirName;
    private int pirNumber;
    private String value;
    private int timerTime;
    private ArrayList<String> roomId;
    private ArrayList<String> relays;

    public String getPirName() {
        return pirName;
    }

    public void setPirName(String pirName) {
        this.pirName = pirName;
    }

    public int getPirNumber() {
        return pirNumber;
    }

    public void setPirNumber(int pirNumber) {
        this.pirNumber = pirNumber;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getTimerTime() {
        return timerTime;
    }

    public void setTimerTime(int timerTime) {
        this.timerTime = timerTime;
    }

    public ArrayList<String> getRoomId() {
        return roomId;
    }

    public void setRoomId(ArrayList<String> roomId) {
        this.roomId = roomId;
    }

    public ArrayList<String> getRelays() {
        return relays;
    }

    public void setRelays(ArrayList<String> relays) {
        this.relays = relays;
    }
}
