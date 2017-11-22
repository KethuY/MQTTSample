package com.atg.onecontrolv3.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Bharath on 26-Sep-17
 */

public class SingleSceneTimerModel {

    @SerializedName("Id")
    private int id;
    @SerializedName("Image")
    private String image;
    @SerializedName("Name")
    private String name;
    @SerializedName("Relays")
    private String relays;
    @SerializedName("active")
    private int active;
    @SerializedName("wifiname")
    private String wifiName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelays() {
        return relays;
    }

    public void setRelays(String relays) {
        this.relays = relays;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }
}
