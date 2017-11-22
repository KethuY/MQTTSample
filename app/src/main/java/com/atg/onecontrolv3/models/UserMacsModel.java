package com.atg.onecontrolv3.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Bharath on 08-Sep-17
 */

public class UserMacsModel {
    @SerializedName("User Macs")
    private ArrayList<SingleMacModel> singleMacArrLst;

    public ArrayList<SingleMacModel> getSingleMacArrLst() {
        return singleMacArrLst;
    }

    public void setSingleMacArrLst(ArrayList<SingleMacModel> singleMacArrLst) {
        this.singleMacArrLst = singleMacArrLst;
    }
}
