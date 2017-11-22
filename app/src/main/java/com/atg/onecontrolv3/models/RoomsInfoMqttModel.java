package com.atg.onecontrolv3.models;

/**
 * Created by user on 14-Mar-17
 */

public class RoomsInfoMqttModel {

    private String roomId;
    private String roomName;
    private String count;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
