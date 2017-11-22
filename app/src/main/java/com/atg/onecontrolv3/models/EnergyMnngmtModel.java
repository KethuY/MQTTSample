package com.atg.onecontrolv3.models;

/**
 * Created by Bharath on 17-09-2016
 */
public class EnergyMnngmtModel {
    String macId;
    String roomNumber;
    String RoomName;

    public String getRoomName() {
        return RoomName;
    }

    public void setRoomName(String roomName) {
        RoomName = roomName;
    }

    String usageTime;
    String relay1, relay2, relay3, relay4;
    String relay5, relay6, relay7, relay8;

    String allRoomsTotal;

    public String getAllRoomsTotal() {
        return allRoomsTotal;
    }

    public void setAllRoomsTotal(String allRoomsTotal) {
        this.allRoomsTotal = allRoomsTotal;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(String usageTime) {
        this.usageTime = usageTime;
    }

    public String getRelay1() {
        return relay1;
    }

    public void setRelay1(String relay1) {
        this.relay1 = relay1;
    }

    public String getRelay2() {
        return relay2;
    }

    public void setRelay2(String relay2) {
        this.relay2 = relay2;
    }

    public String getRelay3() {
        return relay3;
    }

    public void setRelay3(String relay3) {
        this.relay3 = relay3;
    }

    public String getRelay4() {
        return relay4;
    }

    public void setRelay4(String relay4) {
        this.relay4 = relay4;
    }

    public String getRelay5() {
        return relay5;
    }

    public void setRelay5(String relay5) {
        this.relay5 = relay5;
    }

    public String getRelay6() {
        return relay6;
    }

    public void setRelay6(String relay6) {
        this.relay6 = relay6;
    }

    public String getRelay7() {
        return relay7;
    }

    public void setRelay7(String relay7) {
        this.relay7 = relay7;
    }

    public String getRelay8() {
        return relay8;
    }

    public void setRelay8(String relay8) {
        this.relay8 = relay8;
    }
}
