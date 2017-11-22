package com.atg.onecontrolv3.models;

/**
 * Created by sireesha on 17-12-2016
 */

public class IRAppliancesModel {
    private String roomId;
    private String appliance_no;
    private String key_name;
    private byte[] value;
    private String appliance_type;
    private String key_status;
    private String chipID;

    private String secretId;
    private String  id;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getAppliance_no() {
        return appliance_no;
    }

    public void setAppliance_no(String appliance_no) {
        this.appliance_no = appliance_no;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String getAppliance_type() {
        return appliance_type;
    }

    public void setAppliance_type(String appliance_type) {
        this.appliance_type = appliance_type;
    }

    public String getKey_status() {
        return key_status;
    }

    public void setKey_status(String key_status) {
        this.key_status = key_status;
    }

    public String getChipID() {
        return chipID;
    }

    public void setChipID(String chipID) {
        this.chipID = chipID;
    }


    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
