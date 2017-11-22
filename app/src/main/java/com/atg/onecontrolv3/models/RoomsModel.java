package com.atg.onecontrolv3.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ramprasad on 8/13/2016.
 */
public class RoomsModel implements Parcelable {

    String numOfRelays;
    String roomId;
    String roomName;
    boolean isEnabled;
    int isSettingCheckIn3Ways;

    public int isSettingCheckIn3Ways() {
        return isSettingCheckIn3Ways;
    }

    public void setSettingCheckIn3Ways(int settingCheckIn3Ways) {
        isSettingCheckIn3Ways = settingCheckIn3Ways;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getActiveCount() {
        return ActiveCount;
    }

    public void setActiveCount(String activeCount) {
        ActiveCount = activeCount;
    }

    String ActiveCount;

    public String getNumOfRelays() {
        return numOfRelays;
    }

    public void setNumOfRelays(String numOfRelays) {
        this.numOfRelays = numOfRelays;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /*public RoomsModel(Parcel in){
        roomId = in.readString();
        roomName = in.readString();
        numOfRelays = in.readString();
        ActiveCount = in.readString();
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(roomId);
        parcel.writeString(roomName);
        parcel.writeString(numOfRelays);
        parcel.writeString(ActiveCount);
    }

    public static final Parcelable.Creator<RoomsModel> CREATOR = new Parcelable.Creator<RoomsModel>() {

        public RoomsModel createFromParcel(Parcel in) {
            RoomsModel model = new RoomsModel();
            model.roomId = in.readString();
            model.roomName = in.readString();
            model.numOfRelays = in.readString();
            model.ActiveCount = in.readString();
            return model;
        }

        public RoomsModel[] newArray(int size) {
            return new RoomsModel[size];
        }

    };
}
