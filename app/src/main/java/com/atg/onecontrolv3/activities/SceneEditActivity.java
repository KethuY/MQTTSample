package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.SingleSceneTimerModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.atg.onecontrolv3.helpers.Utils.replaceSpace;
import static com.atg.onecontrolv3.helpers.Utils.replaceTilt;

/**
 * Created by Bharath on 27-Sep-17
 */

public class SceneEditActivity extends BaseActivity {

    private static final String TAG = SceneEditActivity.class.getSimpleName();
    int active = 0, send = 0;
    private TextView setTimeTv;
    private TextView appliances;
    private TransparentProgressDialog pd;
    private Switch actionSw;
    private EditText sceneName;
    private TextView deleteBtn;
    private LinearLayout labelLl;
    private LinearLayout actionLl;
    private View v1, vt, v2, v3, v4, v5;
    private boolean isActionSwTouched;
    private String ADStr = "0";
    private TextView saveImage;
    private SingleSceneTimerModel sceneTimerModel;
    private int mIsFrom = 1;
    private String mRelays = "";
    private String mSb = "";

    private String locName = "", locWifi = "", locRelays = "", locImg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenes_selection);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mIsFrom = getIntent().getIntExtra("IsFrom", 0);
        initializeViews();

    }

    private void initializeViews() {
        pd = new TransparentProgressDialog(SceneEditActivity.this, R.drawable.progress);
        //  timePicker = (TimePicker) findViewById(R.id.time_picker);
        appliances = (TextView) findViewById(R.id.appliances2_tv);
        //repeat = (TextView) findViewById(R.id.repeat_tv);
        TextView cancel = (TextView) findViewById(R.id.cancel_tv);
        TextView save = (TextView) findViewById(R.id.save_tv);
        saveImage = (TextView) findViewById(R.id.select_image_tv);
        actionSw = (Switch) findViewById(R.id.action_sw);
        sceneName = (EditText) findViewById(R.id.scene_name_et);
        deleteBtn = (TextView) findViewById(R.id.btn_delete);

        //Animation views..!
        setTimeTv = (TextView) findViewById(R.id.set_time_tv);
        labelLl = (LinearLayout) findViewById(R.id.label_ll);
        actionLl = (LinearLayout) findViewById(R.id.action_ll);
        v1 = findViewById(R.id.v);
        vt = findViewById(R.id.vt);
        v2 = findViewById(R.id.v2);
        v3 = findViewById(R.id.v3);
        v4 = findViewById(R.id.v4);
        v5 = findViewById(R.id.v5);

        setAnimation();

     /*   mIntSelectedHr = timePicker.getCurrentHour();
        mIntSelectedMin = timePicker.getCurrentMinute();*/

        isActionSwTouched = false;
        actionSw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isActionSwTouched = true;
                return false;
            }
        });

        actionSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    locWifi = "1";
                    ADStr = "1";
                    if (isActionSwTouched) {
                        Utils.showToast(SceneEditActivity.this, "ON");
                        if (sceneTimerModel != null) {
                            sceneTimerModel.setActive(1);
                        }
                    }
                } else {
                    locWifi = "0";
                    ADStr = "0";
                    if (isActionSwTouched) {
                        Utils.showToast(SceneEditActivity.this, "OFF");
                        if (sceneTimerModel != null) {
                            sceneTimerModel.setActive(0);
                        }
                    }
                }
            }
        });
    }

    private void setAnimation() {
        //Setting Animation..!
        Animation animationLeft = AnimationUtils.loadAnimation(SceneEditActivity.this, R.anim.left_to_right);
        Animation animationRight = AnimationUtils.loadAnimation(SceneEditActivity.this, R.anim.right_to_left);
        Animation animationTop = AnimationUtils.loadAnimation(SceneEditActivity.this, R.anim.top_to_bottom);
        Animation animationBottom = AnimationUtils.loadAnimation(SceneEditActivity.this, R.anim.bottom_to_top);


        RelativeLayout topRl = (RelativeLayout) findViewById(R.id.header_rl);
        topRl.startAnimation(animationTop);
       /* if (deleteBtn.getVisibility() == View.VISIBLE) {
            deleteBtn.startAnimation(animationBottom);
        }*/
//        setTimeTv.startAnimation(animationLeft);
        //   timePicker.startAnimation(animationLeft);
        labelLl.startAnimation(animationRight);
        actionLl.startAnimation(animationLeft);
        // repeat.startAnimation(animationRight);
        appliances.startAnimation(animationLeft);
        saveImage.startAnimation(animationRight);
        v1.startAnimation(animationTop);
        vt.startAnimation(animationLeft);
        v2.startAnimation(animationRight);
        v3.startAnimation(animationLeft);
        v4.startAnimation(animationRight);
        v5.startAnimation(animationLeft);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsFrom = getIntent().getIntExtra("IsFrom", 0);
        if (mIsFrom == 1) {
            Utils.name = "";
            Utils.wifiName = "";
            Utils.relays = "";
            Utils.image = "";

            locName = Utils.name;
            locWifi = Utils.wifiName;
            locRelays = Utils.relays;
            locImg = Utils.image;

            deleteBtn.setVisibility(View.GONE);

        } else if (mIsFrom == 2) {

            if (Utils.getScenesObj != null) {
                Utils.name = Utils.getScenesObj.getName();
                Utils.wifiName = Utils.getScenesObj.getWifiName();
                Utils.relays = Utils.getScenesObj.getRelays();
                Utils.image = Utils.getScenesObj.getImage();

                locName = Utils.name;
                locWifi = Utils.wifiName;
                locRelays = Utils.relays;
                locImg = Utils.image;

            } else {
                locName = Utils.name;
                locWifi = Utils.wifiName;
                locRelays = Utils.relays;
                locImg = Utils.image;
            }


        }

        if (null != locName && !locName.isEmpty()) {
            sceneName.setText(replaceTilt(locName));
        }

        if (locWifi != null && !locWifi.isEmpty()) {
            if (locWifi.equals("1")) {
                actionSw.setChecked(true);
            } else if (locWifi.equals("0")) {
                actionSw.setChecked(false);
            }
        }
    }

    public void sceneClick(View view) {
        switch (view.getId()) {
            case R.id.appliances2_tv:
                Utils.name = sceneName.getText().toString();
                Utils.wifiName = locWifi;
                Utils.relays = locRelays;
                Utils.image = locImg;

                if (Utils.getScenesObj != null) {
                    Utils.getScenesObj.setName(Utils.name);
                    Utils.getScenesObj.setWifiName(locWifi);
                }

                startActivity(new Intent(this, SceneTimerApplianceActivity.class).putExtra("IsFrom", mIsFrom)
                        .putExtra("sb", locRelays));
                break;
            case R.id.select_image_tv:
                Utils.name = sceneName.getText().toString();
                Utils.wifiName = locWifi;
                Utils.relays = locRelays;
                Utils.image = locImg;

                if (Utils.getScenesObj != null) {
                    Utils.getScenesObj.setName(Utils.name);
                    Utils.getScenesObj.setWifiName(locWifi);
                }

                startActivity(new Intent(this, GridImageViewActivity.class).putExtra("IsFrom", mIsFrom)
                        .putExtra("img", locImg));
                break;
            case R.id.save_tv:
                if (!locName.isEmpty()) {
                    if (!locRelays.isEmpty()) {
                        if (!locImg.isEmpty()) {
                            saveSceneTimer(locName, locRelays, locImg, locWifi, active);
                        } else {
                            Utils.showToast(this, "Please select scene image");
                        }
                    } else {
                        Utils.showToast(this, "Please select appliances");
                    }
                } else {
                    sceneName.setError("Required!");
                    Utils.showToast(this, "Scene name required");
                }
                break;

            case R.id.btn_delete:
                if (Utils.getScenesObj != null) {
                    //DeleteScene?MacId=20f85eeee93e&IMEI=352087074323842&SceneId=1
                    deleteScene(Utils.getScenesObj.getId());
                }
                break;
            case R.id.cancel_tv:
                onBackPressed();
                break;
        }
    }

    private void deleteScene(int id) {

        String strUrl = ServiceHandler.baseUrl + "DeleteScene?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&SceneId=" + id;
        Utils.printLog("TAG", "strUrl:-:" + strUrl);
        StringRequest request = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("DeleteScene");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        int code = jsonObject1.getInt("Code");
                        if (code == 200) {
                            String msg = jsonObject1.getString("Message");
                            showMessageDialog(msg, SceneEditActivity.this);
                        } else {
                            Utils.showMessageDialog("Something went wrong, Please try again!", SceneEditActivity.this);
                        }

                    }
                } catch (JSONException e) {
                    Utils.printLog(TAG, "JSONException:-:" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.printLog(TAG, "error:-:" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(request);
    }

    private void saveSceneTimer(String name, String relays, String image, String wifiName, int active) {
        //SetScene?MacId=20f85eeee93e&IMEI=352087074323842&Name=Gangadhar~Test&Relays=2:234&Image=0&WifiName=0&Active=0
        String strUrl = ServiceHandler.baseUrl + "SetScene?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&Name=" + replaceSpace(name)
                + "&Relays=" + relays + "&Image=" + image + "&WifiName=" + wifiName + "&Active=" + active;

        Utils.printLog(TAG, "strUrl:-:" + strUrl);

        StringRequest request = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("SetScene");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        int code = jsonObject1.getInt("Code");
                        if (code == 200) {
                            String msg = jsonObject1.getString("Message");
                            showMessageDialog(msg, SceneEditActivity.this);
                        } else {
                            Utils.showMessageDialog("Something went wrong, Please try again!", SceneEditActivity.this);
                        }

                    }
                } catch (JSONException e) {
                    Utils.printLog(TAG, "JSONException:-:" + e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.printLog(TAG, "error:-:" + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void showMessageDialog(final String message, final Context context) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                startActivity(new Intent(context, MainActivity.class));
            }
        });
        alertBox.show();
    }
}
