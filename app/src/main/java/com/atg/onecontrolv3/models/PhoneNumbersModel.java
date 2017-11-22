package com.atg.onecontrolv3.models;

import android.graphics.drawable.Drawable;

/**
 * Created by Bharath on 30-11-2016
 */

    public class PhoneNumbersModel {

    String mobileNumber;
    int orderId;
    Drawable color;

    public Drawable getColor() {
        return color;
    }

    public void setColor(Drawable color) {
        this.color = color;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
