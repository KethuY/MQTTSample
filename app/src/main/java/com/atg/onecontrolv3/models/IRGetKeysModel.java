package com.atg.onecontrolv3.models;

/**
 * Created by Bharath on 04-Jul-17
 */

public class IRGetKeysModel {
    private String chipId;
    private String keyCode;
    private String keyCodeVale;
    private String macId;
    private String receiverId;
    private String relayId;

    //Newly added fields..!
    private String userId;
    private String password;

    public String getChipId() {
        return chipId;
    }

    public void setChipId(String chipId) {
        this.chipId = chipId;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getKeyCodeVale() {
        return keyCodeVale;
    }

    public void setKeyCodeVale(String keyCodeVale) {
        this.keyCodeVale = keyCodeVale;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getRelayId() {
        return relayId;
    }

    public void setRelayId(String relayId) {
        this.relayId = relayId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
