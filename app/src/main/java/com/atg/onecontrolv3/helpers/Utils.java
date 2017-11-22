package com.atg.onecontrolv3.helpers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.BaseActivity;
import com.atg.onecontrolv3.models.GetMacInfoModel;
import com.atg.onecontrolv3.models.GetTimersModel;
import com.atg.onecontrolv3.models.PIRDBModel;
import com.atg.onecontrolv3.models.SingleRoomModel;
import com.atg.onecontrolv3.models.SingleSceneTimerModel;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Bharath on 04-Sep-17
 */

public class Utils extends BaseActivity {
    public static final String P_TOPIC = "gateway/";
    public static final String S_TOPIC = "mobile/";
    public final static String SECURITY_TOPIC = "security/";
    public final static String ARMDISARM_TOPIC = "armdisarm/";
    public static final String NO_INTERNET = "No Internet";
    private static final String TAG = Utils.class.getSimpleName();
    public static String IMEI = "358224073370292";
    public static String MAC_ID;
    public static String MAC_PIN;
    public static boolean isNetworkAvailable;
    public static GetTimersModel getTimerModelObj;
    public static List<GetMacInfoModel> mDashBoardArrLst;
    public static String baseUrl = "http://atghas.com/OneControlService/OCService.svc/";
    public static String name = "", image = "", wifiName = "", relays = "";
    public static PIRDBModel getPIRSObj;
    public static SingleSceneTimerModel getScenesObj;
    public static int ROOMS_CNT = 0;
    public static int APP_CNT = 0;
    @SuppressLint("StaticFieldLeak")
    private static TransparentProgressDialog pd;
    public static ArrayList<SingleRoomModel> mSingleRoomArrLst;
    public static void clearScenesData() {
        name = "";
        image = "";
        wifiName = "";
        relays = "";
    }

    public static int[] splitStringTag(String viewTag) {
        int roomId = Character.getNumericValue(viewTag.charAt(0));
        int appPos = Character.getNumericValue(viewTag.charAt(1));
        return new int[]{roomId, appPos};
    }

    public static void printLog(String TAG, String msg) {
        Log.e(TAG, msg);
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String replaceTilt(String name) {
        return name.replaceAll("~", " ");
    }

    public static String replaceSpace(String name) {
        return name.replaceAll(" ", "~");
    }

    //To get actual string from byte data from DB
    public static String getStringFromBytes(byte[] data) {
        String value = "";
        value = new String(data);
        return value;
    }

    //To get bytes from string
    public static byte[] getBytesFromString(String data) {
        byte[] value = null;
        value = data.getBytes();
        return value;
    }

    public static void showMessageDialog(final String message, final Context context) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        alertBox.show();
    }

    public static void showProgressDialog(Context context) {
//        pDialog = new ProgressDialog(context);
//        pDialog.setMessage("Loading..");
//        pDialog.setCancelable(false);
//        if(!pDialog.isShowing()){
//            pDialog.show();
//        }
        pd = new TransparentProgressDialog(context, R.drawable.progress);//spinner
        if (pd != null && !pd.isShowing()) {
            pd.show();
        }
    }

    public static void hideProgressDialog() {
//        if(pDialog.isShowing()){
//            pDialog.dismiss();
//        }
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    public static String getCurrentDate() {
        String currentDate = "";
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            currentDate = df.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentDate;
    }

    public static String getCurrentDateTime(int i) {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.MINUTE, i); // adds n minutes
        cal.getTime(); // returns new date object, one hour in the future

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        return df.format(cal.getTime());
    }

    public static String getCurrentDateTimeQT() {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.getTime(); // returns new date object, one hour in the future
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        return df.format(cal.getTime());
    }

     public static String getDateStr(String dateStr) {
         String dateTimeStr = "";
         SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm");
         try {
             Date myDate = dateFormatter.parse(dateStr);
             Calendar cal = Calendar.getInstance();
             cal.roll(Calendar.DATE, -1);
             if (myDate.before(cal.getTime())) {
                 //  myDate must be yesterday or earlier
                 dateTimeStr = dateStr.substring(0, dateStr.length() - 4);
                 StringBuilder sb = new StringBuilder(dateTimeStr);
                 sb.insert(4, '/');
                 sb.insert(7, '/');
                 String arrS[] = sb.toString().split("/");
                 dateTimeStr = arrS[2] + "/" + arrS[1] + "/" + arrS[0];
             } else {
                 //  myDate must be today or later
                 dateTimeStr = dateStr.substring(dateStr.length() - 4);
                 StringBuilder sb = new StringBuilder(dateTimeStr);
                 sb.insert(2, ':');
                 dateTimeStr = sb.toString();
             }
         } catch (Exception e) {
             Utils.printLog(TAG, "ParseException:-:" + e.getMessage());
         }
         return dateTimeStr;
     }
   /* public static String getDateStr(String dateStr) {
        String dateTimeStr = "";
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            Date myDate = dateFormatter.parse(dateStr);
            Date today = new Date();
            //Log.e(TAG, "myDate " + myDate + " today " + today);
            if (today.compareTo(myDate) == 0) {
                dateTimeStr = dateStr.substring(dateStr.length() - 4);
                StringBuilder sb = new StringBuilder(dateTimeStr);
                sb.insert(2, ':');
                dateTimeStr = sb.toString();
            } else if (myDate.compareTo(today) < 0) {
                dateTimeStr = dateStr.substring(0, dateStr.length() - 4);
                StringBuilder sb = new StringBuilder(dateTimeStr);
                sb.insert(4, '/');
                sb.insert(7, '/');
                String arrS[] = sb.toString().split("/");
                dateTimeStr = arrS[2] + "/" + arrS[1] + "/" + arrS[0];
            }
        } catch (Exception e) {
            Utils.printLog(TAG, "ParseException:-:" + e.getMessage());
        }
        return dateTimeStr;
    }*/

    public static int getColorPrimary(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int getColorPrimaryDark(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

}
