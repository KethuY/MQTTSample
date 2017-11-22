package com.atg.onecontrolv3.models;

/**
 * Created by Bharath on 2/5/2017
 */

public class SubUserRoomsModel {
    private int activeCount;
    private int numOfRelays;
    private String roomName;
    private String roomNumber;
    private boolean isEntered;

    public boolean isEntered() {
        return isEntered;
    }

    public void setEntered(boolean entered) {
        isEntered = entered;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getNumOfRelays() {
        return numOfRelays;
    }

    public void setNumOfRelays(int numOfRelays) {
        this.numOfRelays = numOfRelays;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}
