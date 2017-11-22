package com.atg.onecontrolv3.ServiceHandler;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ramprasad on 8/13/2016
 */
public class ServiceHandler {
    //public static String baseUrl = "http://103.14.96.128/OneControlService/OCService.svc/";//   158.69.244.175
    //public static String baseUrl = "http://onecontrol.azurewebsites.net/OCService.svc/";
    public static String baseUrl = "http://atghas.com/OneControlService/OCService.svc/";
    private static String TAG = "HttpHandler";
    private InputStream is = null;
    private JSONObject jsonObj;

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
        } catch (Exception e) {
            Log.e(TAG, "reader error");
        }
        return sb.toString();
    }

    public JSONObject getJSONFromUrl(String serviceName, String args) throws Exception {

        HttpURLConnection conn = null;
        try {
            Log.e(TAG, "getJsonFromURL : " + getWebserviceUrl(serviceName, args));
            URL url = new URL(getWebserviceUrl(serviceName, args));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Pragma", "no-cache");

            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B)AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();

            String json = convertStreamToString(is);
            //Log.e(TAG, "response--" + json);
            jsonObj = new JSONObject(json);
        } catch (Exception e) {
            Log.e(TAG, "exception--" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOExecption--" + e.getMessage());
                //e.printStackTrace();
                //throw e;
            }
            if (conn != null)
                conn.disconnect();
        }
        return jsonObj;
    }

    private String getWebserviceUrl(String serviceName, String args) {

        StringBuilder sb = new StringBuilder();
        /*if(serviceName.equalsIgnoreCase("CreatePassCode")|| serviceName.equalsIgnoreCase("RegisterUser") || serviceName.equalsIgnoreCase("UpdateUserProfile")){
            sb.append("http://103.14.96.128/OneControlService/OCService.svc/").append(serviceName).append("?");
        }else {*/
        sb.append(baseUrl).append(serviceName).append("?");
        //}
        if (args != null) {
            sb.append(args);
            //sb.append("&");
        }
        return sb.toString();
    }
}
