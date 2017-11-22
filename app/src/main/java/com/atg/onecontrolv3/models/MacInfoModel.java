package com.atg.onecontrolv3.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Bharath on 07-Sep-17
 */

public class MacInfoModel {
    @SerializedName("MacInfo")
    private ArrayList<SingleRoomModel> singleRoomArrLst;

    public MacInfoModel(ArrayList<SingleRoomModel> singleRoomArrLst) {
        this.singleRoomArrLst = singleRoomArrLst;
    }

    public ArrayList<SingleRoomModel> getSingleRoomArrLst() {
        return singleRoomArrLst;
    }
}
