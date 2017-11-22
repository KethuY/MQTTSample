package com.atg.onecontrolv3.preferances;

import android.content.Context;
import android.content.SharedPreferences;

import com.atg.onecontrolv3.helpers.Utils;

import java.util.Set;

/**
 * Created by Bharath on 09-Sep-17
 */

public class OneControlPreferences {
    private SharedPreferences mPreferences;

    public OneControlPreferences(Context context) {
        String preferenceName = "OCPreferences";
        mPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public void storeMACPin(String macId, String macPin) {
        Utils.printLog("Utils", "macId:-:" + macId + " macPin:-:" + macPin);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(macId, macPin);
        editor.commit();
    }

    public String getMACPin(String macId) {
        return mPreferences.getString(macId, null);
    }


    public void storeLstOperated(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getLstOperated(String key) {
        return mPreferences.getString(key, null);
    }

    public void setChildRooms(Set<String> rooms) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet("CHILD_ROOMS", rooms);
        editor.commit();
    }

    public Set<String> getChildRooms() {
        Set<String> rooms = mPreferences.getStringSet("CHILD_ROOMS", null);
        return rooms;
    }

    public void storeSSID(String ssid) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("USER_SSID", ssid);
        editor.commit();
    }

    public String getSSID() {
        String ssid = mPreferences.getString("USER_SSID", null);
        return ssid;
    }

    public boolean getPreference() {
        boolean value = false;
        value = mPreferences.getBoolean("isRegister", false);
        return value;
    }

    public void setPreference(boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("isRegister", value);
        editor.commit();
    }

    public void storeIRBlasterUUID(String uuid) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("IR_BLASTER_UUID", uuid);
        editor.commit();
    }

    public String getIRBlasterUUID() {
        String macAddr = mPreferences.getString("IR_BLASTER_UUID", null);
        return macAddr;
    }

    public void storeMACAddress(String macAddress) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("MAC_ADDRESS", macAddress);
        editor.commit();
    }

    public String getMACAddress() {
        String macAddr = mPreferences.getString("MAC_ADDRESS", null);
        return macAddr;
    }

    public void storeUserMode(String userMode) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("USER_MODE", userMode);
        editor.commit();
    }

    public String getUserMode() {
        String userMode = mPreferences.getString("USER_MODE", null);
        return userMode;
    }

    public void storeIMEI(String imei) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("USER_IMEI", imei);
        editor.commit();
    }

    public String getIMEI() {
        String macAddr = mPreferences.getString("USER_IMEI", null);
        return macAddr;
    }
}
