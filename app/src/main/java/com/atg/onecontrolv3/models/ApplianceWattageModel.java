package com.atg.onecontrolv3.models;

import java.util.ArrayList;

/**
 * Created by Bharath on 2/8/2017.
 */

public class ApplianceWattageModel {

    private int id,r1,r2,r3,r4,r5,r6,r7,r8,RoomNumber;
    private String MacId;
    private ArrayList<Integer> wattagesArr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getR1() {
        return r1;
    }

    public void setR1(int r1) {
        this.r1 = r1;
    }

    public int getR2() {
        return r2;
    }

    public void setR2(int r2) {
        this.r2 = r2;
    }

    public int getR3() {
        return r3;
    }

    public void setR3(int r3) {
        this.r3 = r3;
    }

    public int getR4() {
        return r4;
    }

    public void setR4(int r4) {
        this.r4 = r4;
    }

    public int getR5() {
        return r5;
    }

    public void setR5(int r5) {
        this.r5 = r5;
    }

    public int getR6() {
        return r6;
    }

    public void setR6(int r6) {
        this.r6 = r6;
    }

    public int getR7() {
        return r7;
    }

    public void setR7(int r7) {
        this.r7 = r7;
    }

    public int getR8() {
        return r8;
    }

    public void setR8(int r8) {
        this.r8 = r8;
    }

    public int getRoomNumber() {
        return RoomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        RoomNumber = roomNumber;
    }

    public String getMacId() {
        return MacId;
    }

    public void setMacId(String macId) {
        MacId = macId;
    }

    public ArrayList<Integer> getWattagesArr() {
        return wattagesArr;
    }

    public void setWattagesArr(ArrayList<Integer> wattagesArr) {
        this.wattagesArr = wattagesArr;
    }
}
