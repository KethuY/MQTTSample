package com.atg.onecontrolv3.models;

import android.util.Log;

import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bharath on 29-09-2016
 */

public class RegistrationProvider {

    private static final String TAG = "SecurityProvider";
    JSONObject response;
    //public String MACID = "5002";//5001
    //public String IMEI = "354115071374597";//352087074323842

    //CreatePassCode
    public List<PassCodeModel> serviceCreatePassCode(String mobileNumber) {
        String statusMsg = null;
        List<PassCodeModel> list = new ArrayList<PassCodeModel>();
        String errorCode;

        try {
            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "MobileNumber=" + mobileNumber;
            response = serviceHandler.getJSONFromUrl("CreatePassCode", args);

            PassCodeModel model;
            JSONArray jsonArray = response.getJSONArray("PassCode");
            for (int i = 0; i < jsonArray.length(); i++) {
                model = new PassCodeModel();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String codeId = jsonObject.getString("CodeId");
                String codeValue = jsonObject.getString("CodeValue");

                model.setCodeId(codeId);
                model.setCodeValue(codeValue);

                list.add(model);
            }
            statusMsg = "Success";      //Temperory
        } catch (Exception e) {
            e.printStackTrace();
            statusMsg = "Error";
        }
        return list;
    }

    //Registering
    public List<RegisterModel> serviceRegistration(String mobileNumber, String imei, String uid, String pin,
                                                   String firstName, String lastName, String email, String macName) {
        String statusMsg = null;
        List<RegisterModel> list = new ArrayList<>();

        try {
            ServiceHandler serviceHandler = new ServiceHandler();
            //RegisterUser?Mobile=9963004836&IMEI=123123123123123&UID=20f85eeee965&MACPIN=XRWDVAUXLXHULWE&
            // FirstName=GangadharTest&LastNameBobbaraTest&EMail=Emailtest&MacName=GangaMacTest
            String args = "Mobile=" + mobileNumber + "&IMEI=" + imei + "&UID=" + uid + "&PIN=" + pin
                    + "&FirstName=" + firstName + "&LastName=" + lastName + "&EMail=" + email + "&MacName=" + macName;
            response = serviceHandler.getJSONFromUrl("RegisterUser", args);

            RegisterModel model;
            JSONArray jsonArray = response.getJSONArray("Register");
            for (int i = 0; i < jsonArray.length(); i++) {
                model = new RegisterModel();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String code = jsonObject.getString("Code");
                String description = jsonObject.getString("Description");

                model.setCode(code);
                model.setDescription(description);

                list.add(model);
            }

            statusMsg = "Success";      //Temperory
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
            e.printStackTrace();
            statusMsg = "Error";
        }
        return list;
    }

    //Updating user profile
    public String serviceUpdateUserProfile(String mobileNumber, String imei, String firstName, String lastName, String email, String macName, String macId) {
        String statusMsg;

        try {
            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "MacId=" + macId + "&IMEI=" + imei + "&MobileNumber=" + mobileNumber + "&FirstName=" + firstName +
                    "&LastName=" + lastName + "&EMail=" + email + "&MacName=" + macName;
            Log.e(TAG, "args:-:" + args);
            response = serviceHandler.getJSONFromUrl("UpdateUserProfile", args);
            statusMsg = "Success";      //Temperory
        } catch (Exception e) {
            e.printStackTrace();
            statusMsg = "Error";
        }
        return statusMsg;
    }
}
