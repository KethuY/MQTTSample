package com.atg.onecontrolv3.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Bharath on 26-Sep-17
 */

public class SceneTimerModel {
    @SerializedName("GetScenesResult")
    private ArrayList<SingleSceneTimerModel> singleSceneTimerModelArrLst;

    public SceneTimerModel(ArrayList<SingleSceneTimerModel> singleSceneTimerModelArrLst) {
        this.singleSceneTimerModelArrLst = singleSceneTimerModelArrLst;
    }

    public ArrayList<SingleSceneTimerModel> getSingleSceneTimerModelArrLst() {
        return singleSceneTimerModelArrLst;
    }
}
