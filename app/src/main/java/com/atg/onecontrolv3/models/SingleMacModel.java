package com.atg.onecontrolv3.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Bharath on 08-Sep-17
 */

public class SingleMacModel {

    @SerializedName("Id")
    private int id;
    @SerializedName("IsMaster")
    private boolean isMaster;
    @SerializedName("LOV")
    private int lov;
    @SerializedName("MacName")
    private String macName;
    @SerializedName("UID")
    private String uid;

    public SingleMacModel(int id, boolean isMaster, int lov, String macName, String uid) {
        this.id = id;
        this.isMaster = isMaster;
        this.lov = lov;
        this.macName = macName;
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public int getLov() {
        return lov;
    }

    public String getMacName() {
        return macName;
    }

    public String getUid() {
        return uid;
    }
}
