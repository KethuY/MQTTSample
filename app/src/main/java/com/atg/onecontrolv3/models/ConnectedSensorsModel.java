package com.atg.onecontrolv3.models;

/**
 * Created by Bharath on 11-Aug-17
 */

public class ConnectedSensorsModel {

    private int id;
    private int isWired;
    private String location;
    private int sensorNo;
    private int sensorTypeId;
    private String zone;
    private String sensorType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsWired() {
        return isWired;
    }

    public void setIsWired(int isWired) {
        this.isWired = isWired;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSensorNo() {
        return sensorNo;
    }

    public void setSensorNo(int sensorNo) {
        this.sensorNo = sensorNo;
    }

    public int getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(int sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
}
