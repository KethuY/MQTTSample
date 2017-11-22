package com.atg.onecontrolv3.rest;

import com.atg.onecontrolv3.models.MacInfoModel;
import com.atg.onecontrolv3.models.SceneTimerModel;
import com.atg.onecontrolv3.models.UserMacsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("GetMacInfo")
    Call<MacInfoModel> getGatewayDetails(@Query("MacId") String MacId, @Query("IMEI") String IMEI);

    @GET("GetUserMacs")
    Call<UserMacsModel> getUserMacs(@Query("IMEI") String IMEI);

    @GET("GetScenes")
    Call<SceneTimerModel> getScene(@Query("MacId") String MacId, @Query("IMEI") String IMEI);
}
