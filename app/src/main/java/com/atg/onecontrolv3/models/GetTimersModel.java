package com.atg.onecontrolv3.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Bharath on 19-Apr-17
 */

public class GetTimersModel implements Serializable {
    private String execTime;
    private String name;
    private String serverName;
    private String relays;
    private String uid;
    private String serverTime;
    private int id;
    private int timertype;
    private boolean action;
    private boolean isRepeat;
    private boolean status;
    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    private ArrayList<Boolean> isDaysRepetArrLst = new ArrayList<>();


    public String getExecTime() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getRelays() {
        return relays;
    }

    public void setRelays(String relays) {
        this.relays = relays;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimertype() {
        return timertype;
    }

    public void setTimertype(int timertype) {
        this.timertype = timertype;
    }

    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public ArrayList<Boolean> getIsDaysRepetArrLst() {
        return isDaysRepetArrLst;
    }

    public void setIsDaysRepetArrLst(ArrayList<Boolean> isDaysRepetArrLst) {
        this.isDaysRepetArrLst = isDaysRepetArrLst;
    }
}
