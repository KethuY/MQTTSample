package com.atg.onecontrolv3.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.activities.AddDeviceActivityMqtt;
import com.atg.onecontrolv3.activities.ConnectedSensorsActivity;
import com.atg.onecontrolv3.activities.EditDeviceActivity;
import com.atg.onecontrolv3.activities.PIRDashboardActivity;
import com.atg.onecontrolv3.activities.PhoneNosActivity;
import com.atg.onecontrolv3.activities.WiredWirelessActivity;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * Created by Bharath on 7/29/2015
 */
public class SettingFragment extends Fragment implements View.OnClickListener, MqttHelper.responseListener {

    public static final String TAG = SettingFragment.class.getSimpleName();
    View rootView;
    Button btnAddDevice, btnDeviceAdded, btnFavourites, btnMotionLighting, btnRemove;
    Button btnConnectSensor, btnSMSAlert, btnActiveDeactive;
    TextView tvAddDevice, tvDeviceAdded, tvFavourites, tvMultipair, tvRemove;
    TextView tvConnectSensor, tvSMSAlert, tvActiveDeactive;
    TextView tvCamera, tvVideo;
    LinearLayout layoutOffline;
    int pirActivationTime;
    String hoursSelected, minsSelected;
    OneControlPreferences mPreferences;
    String userMode;
    TransparentProgressDialog pd;
    MqttHelper.responseListener mqttListener = null;
    private Activity mActivity;
    private MqttHelper helper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.setting_layout, container, false);
        mPreferences = new OneControlPreferences(mActivity);
        initializeViews();
        //userMode = mPreferences.getUserMode();

        mqttListener = this;
        helper = new MqttHelper(getContext(), mqttListener);

        pd = new TransparentProgressDialog(mActivity, R.drawable.progress);

//        new NetCheckBeforeOfflineTask(mActivity).execute();

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    private void initializeViews() {
        btnAddDevice = (Button) rootView.findViewById(R.id.btnAddDevice);
        btnDeviceAdded = (Button) rootView.findViewById(R.id.btnDeviceAdded);
        btnFavourites = (Button) rootView.findViewById(R.id.btnFavourites);
        btnMotionLighting = (Button) rootView.findViewById(R.id.btnMotionLighting);
        btnRemove = (Button) rootView.findViewById(R.id.btnRemove);
        btnConnectSensor = (Button) rootView.findViewById(R.id.btnConnectSensor);
        btnSMSAlert = (Button) rootView.findViewById(R.id.btnSMSAlert);
        btnActiveDeactive = (Button) rootView.findViewById(R.id.btnActiveDeactive);
        layoutOffline = (LinearLayout) rootView.findViewById(R.id.layoutOffline);

        btnAddDevice.setOnClickListener(this);
        btnDeviceAdded.setOnClickListener(this);
        btnFavourites.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnConnectSensor.setOnClickListener(this);
        btnSMSAlert.setOnClickListener(this);
        btnActiveDeactive.setOnClickListener(this);
        btnMotionLighting.setOnClickListener(this);

        //Textviews settings icons

        tvAddDevice = (TextView) rootView.findViewById(R.id.tvAddDevice);
        tvDeviceAdded = (TextView) rootView.findViewById(R.id.tvDevicesAdded);
        tvFavourites = (TextView) rootView.findViewById(R.id.tvFavourites);
        tvMultipair = (TextView) rootView.findViewById(R.id.tvMultipair);
        tvRemove = (TextView) rootView.findViewById(R.id.tvRemove);
        tvConnectSensor = (TextView) rootView.findViewById(R.id.tvConnectSensor);
        tvSMSAlert = (TextView) rootView.findViewById(R.id.tvSMSAlert);
        tvActiveDeactive = (TextView) rootView.findViewById(R.id.tvActiveDeactive);
        tvCamera = (TextView) rootView.findViewById(R.id.tvCamera);
        tvVideo = (TextView) rootView.findViewById(R.id.tvVideo);

        Typeface tf = Typeface.createFromAsset(mActivity.getAssets(), "uifont.ttf");
        Typeface tf1 = Typeface.createFromAsset(mActivity.getAssets(), "onecontrolfont.ttf");
        Typeface tf2 = Typeface.createFromAsset(mActivity.getAssets(), "settings_font.ttf");

        tvAddDevice.setTypeface(tf2);
        tvDeviceAdded.setTypeface(tf2);
        tvFavourites.setTypeface(tf);
        tvMultipair.setTypeface(tf1);
        tvRemove.setTypeface(tf);
        tvConnectSensor.setTypeface(tf1);
        tvSMSAlert.setTypeface(tf1);
        tvActiveDeactive.setTypeface(tf2);
        tvCamera.setTypeface(tf);
        tvVideo.setTypeface(tf1);

        /*//Applying animations to buttons
        Animation animationLeft = AnimationUtils.loadAnimation(mActivity, R.anim.left_to_right);
        Animation animationRight = AnimationUtils.loadAnimation(mActivity, R.anim.right_to_left);

        btnAddDevice.startAnimation(animationLeft);
        btnDeviceAdded.startAnimation(animationRight);
        btnFavourites.startAnimation(animationLeft);
        btnMotionLighting.startAnimation(animationRight);
        btnRemove.startAnimation(animationLeft);
        btnConnectSensor.startAnimation(animationRight);
        btnSMSAlert.startAnimation(animationLeft);
        btnActiveDeactive.startAnimation(animationRight);*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddDevice:
                Intent intent = new Intent(getActivity(), AddDeviceActivityMqtt.class);
                intent.putExtra("ROOM_NAME", "Pairing settings_home");
                intent.putExtra("ROOM_ID", "Pairing settings_home");
                intent.putExtra("NUM_OF_RELAYS", "Pairing settings_home");
                intent.putExtra("BTN_NAME", "Assign Name");
                //intent.putExtra("RROM_EDIT", false);
                startActivity(intent);
                break;
            case R.id.btnDeviceAdded:
                Intent intent1 = new Intent(getActivity(), EditDeviceActivity.class);
                startActivity(intent1);
                break;
            /*case R.id.btnFavourites:
                Intent intentFav = new Intent(getActivity(), FavouriteRoomsActivity.class);
                startActivity(intentFav);
                break;*/
            case R.id.btnMotionLighting:
                //Todo need to open new page for pir settings
                /*PopupMenu popupMenuML = new PopupMenu(mActivity, btnMotionLighting);
                popupMenuML.getMenuInflater().inflate(R.menu.menu_pirs, popupMenuML.getMenu());
                popupMenuML.setOnMenuItemClickListener(new OnMotionLightingClickListener());
                popupMenuML.show();
                startActivity(new Intent(getContext(), PIRListActivity.class));*/
                //startActivity(new Intent(getContext(), PIRListActivity.class));
                startActivity(new Intent(getContext(), PIRDashboardActivity.class));

                break;
            /*case R.id.btnRemove:
                Intent removeIntent = new Intent(getActivity(), RemoveRoomsActivity.class);
                startActivity(removeIntent);
                break;*/
            case R.id.btnConnectSensor:
                Intent intent3 = new Intent(mActivity, WiredWirelessActivity.class);//PortConfigurationActivity
                startActivity(intent3);
               /* PopupMenu popupMenu = new PopupMenu(mActivity, btnConnectSensor);
                popupMenu.getMenuInflater().inflate(R.menu.connect_sensor_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener());
                popupMenu.show();*/
                break;
            case R.id.btnSMSAlert:
                Intent intent2 = new Intent(mActivity, PhoneNosActivity.class);
                startActivity(intent2);
                break;
            case R.id.btnActiveDeactive:
                //setPirName(getContext());

                startActivity(new Intent(getContext(), ConnectedSensorsActivity.class));

               /* Intent intent3 = new Intent(mActivity, MainActivity.class);
                intent3.putExtra("Next_Page", "1");
                startActivity(intent3);
                mActivity.finish();*/

                break;
        }
    }

    public void setPirName(Context context) {

        /*final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.set_pir_name);

        final EditText pirNameEt = (EditText) dialog.findViewById(R.id.pir_name);
        dialog.sewt
        dialog.show();*/

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_pir_name, null);
        dialogBuilder.setView(dialogView);

        final EditText pirName = (EditText) dialogView.findViewById(R.id.pir_name);
        Button submit = (Button) dialogView.findViewById(R.id.btn_submit);
        Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strName = pirName.getText().toString().trim();
                Log.e(TAG, "strName:-:" + strName);
                if (strName.length() != 5) {
                    Utils.showMessageDialog("Please enter 5-digit serial number", getContext());
                } else if (strName.equalsIgnoreCase("00000") || Integer.parseInt(strName) > 65536) {
                    Utils.showMessageDialog("Enter proper serial number", getContext());
                } else {
                    strName = strName.contains(" ") ? strName.replaceAll(" ", "~") : strName;
                    pd.show();
                    helper.sendMsg("g|" + strName);
                    //new SetPirSecurityTask().execute(strName);
                    alertDialog.cancel();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    /*public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewPager.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }*/

    private void openPIROptionsDialg(final String pirNo) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        LinearLayout layout = new LinearLayout(mActivity);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(paramsLayout);

        TextView tvActivate = new TextView(mActivity);
        TextView tvDeActivate = new TextView(mActivity);
        TextView tvConfigure = new TextView(mActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100);

        View view1 = new View(mActivity);
        View view2 = new View(mActivity);
        View view3 = new View(mActivity);
        LinearLayout.LayoutParams paramsView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        view1.setLayoutParams(paramsView);
        view2.setLayoutParams(paramsView);
        view3.setLayoutParams(paramsView);
        view1.setBackgroundColor(getResources().getColor(R.color.gray));
        view2.setBackgroundColor(getResources().getColor(R.color.gray));
        view3.setBackgroundColor(getResources().getColor(R.color.gray));

        tvActivate.setLayoutParams(params);
        tvDeActivate.setLayoutParams(params);
        tvConfigure.setLayoutParams(params);

        tvActivate.setTextSize(20);
        tvActivate.setGravity(Gravity.CENTER);
        tvDeActivate.setTextSize(20);
        tvDeActivate.setGravity(Gravity.CENTER);
        tvConfigure.setTextSize(20);
        tvConfigure.setGravity(Gravity.CENTER);

        tvActivate.setText("Activate");
        tvDeActivate.setText("Deactivate");
        tvConfigure.setText("Configure");

        tvActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable) {
                    setPIRTimerPicker(pirNo);
                    dialog.dismiss();
                } else {
                    Utils.showMessageDialog("No Internet.", mActivity);
                }
            }
        });
        tvDeActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable) {
                    makePIRStatusRequest(pirNo, "0");
                    dialog.dismiss();
                } else {
                    Utils.showMessageDialog("No Internet.", mActivity);
                }
            }
        });
       /* tvConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentML = new Intent(getActivity(), PIRSetttingActivity.class);
                intentML.putExtra("PIR", pirNo);
                startActivity(intentML);
                dialog.dismiss();
            }
        });*/

        layout.addView(tvActivate);
        layout.addView(view1);
        layout.addView(tvDeActivate);
        layout.addView(view2);
        layout.addView(tvConfigure);
        layout.addView(view3);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void setPIRTimerPicker(final String pirNo) {
        // TODO Auto-generated method stub
       /* Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
//                eReminderTime.setText( selectedHour + ":" + selectedMinute);
                pirActivationTime = (selectedHour * 60) + selectedMinute;
                Log.v(TAG, "time is : " +selectedHour + ":" + selectedMinute + ", minutes is : " + pirActivationTime);

            }
        }, hour, minute, true);//Yes 24 hour time

        mTimePicker.setTitle("Select Time");
        mTimePicker.show();*/

        final Dialog dialog = new Dialog(mActivity);
        dialog.setTitle("Time Picker");
        dialog.setContentView(R.layout.pir_time_picker_dialog);

        final Spinner sprHours = (Spinner) dialog.findViewById(R.id.sprHours);
        final Spinner sprMinutes = (Spinner) dialog.findViewById(R.id.sprMinutes);
        Button btnSave = (Button) dialog.findViewById(R.id.btnSave);
        Button btnClear = (Button) dialog.findViewById(R.id.btnClear);

        ArrayAdapter<CharSequence> adapterHours = ArrayAdapter.createFromResource(mActivity, R.array.array_pir_timer_hours, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(mActivity, R.array.array_pir_timer_minutes, android.R.layout.simple_spinner_dropdown_item);

        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sprHours.setAdapter(adapterHours);
        sprMinutes.setAdapter(adapterMinutes);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sprHours.getSelectedItemPosition() == 0) {
                    Utils.showMessageDialog("Select Hours", mActivity);
                } else if (sprMinutes.getSelectedItemPosition() == 0) {
                    Utils.showMessageDialog("Select Minutes", mActivity);
                } else {
                    pirActivationTime = (Integer.parseInt(hoursSelected) * 60) + Integer.parseInt(minsSelected);
                    if (pirActivationTime > 512) {
                        Utils.showMessageDialog("Maximum time to set is upto 8 hours 30 minutes.", mActivity);
                    } else {
                        if (Utils.isNetworkAvailable) {
                            makePIRStatusRequest(pirNo, pirActivationTime + "");
                            dialog.dismiss();
                        } else {
                            Utils.showMessageDialog("No Internet.", mActivity);
                        }
                    }
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sprHours.setSelection(0);
                sprMinutes.setSelection(0);
            }
        });

        sprHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hoursSelected = sprHours.getSelectedItem().toString();
//                Toast.makeText(mActivity, "hours : " + hoursSelected, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sprMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                minsSelected = sprMinutes.getSelectedItem().toString();
//                Toast.makeText(mActivity, "mins : " + minsSelected, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialog.show();
    }

    private void makePIRStatusRequest(String pirNo, String timer) {
        Utils.showProgressDialog(mActivity);
        String url = ServiceHandler.baseUrl + "SetPIRStatus?MacId=" + Utils.MAC_ID + "&IMEI="
                + Utils.IMEI + "&PIR=" + pirNo + "&timer_home=" + timer;
        Log.v(TAG, "PIR Status url : " + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String res = response.toString();
                Log.v(TAG, "pir status res : " + res);
                Utils.hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "error in setPIRStatus");
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);

    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }*/

    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
            MyPreferences.add(MyPreferences.PrefType.IS_SETTING_VISIBLE, isVisibleToUser, getContext());
    }*/

    @Override
    public void onStart() {
        super.onStart();

    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onMqttResponse(String resStr) {
        pd.dismiss();
        if (resStr.contains("Notification:")) {
            String notiStrArr[] = resStr.split(Pattern.quote(":"));
            pd.dismiss();
            switch (notiStrArr[1]) {
                case "39":
                    Utils.showMessageDialog("Pairing Safety Success", getContext());
                    break;
                case "40":
                    Utils.showMessageDialog("Pairing Safety Failed", getContext());
                    break;
            }
        }
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }

    /*public class OnMenuItemClickListener implements MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            Intent intent = new Intent(mActivity, PortConfigurationActivity.class);
            switch (menuItem.getItemId()) {
                case R.id.port1:
                    intent.putExtra("PORT", "Port1");
                    break;
                case R.id.port2:
                    intent.putExtra("PORT", "Port2");
                    break;
                *//*case R.id.port3:
                    intent.putExtra("PORT", "Port3");
                    break;
                case R.id.port4:
                    intent.putExtra("PORT", "Port4");
                    break;*//*
            }
            startActivity(intent);
            return true;
        }
    }

    public class OnMotionLightingClickListener implements MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            //Intent intent = new Intent(mActivity, PIRSetttingActivity.class);
            switch (menuItem.getItemId()) {
                case R.id.pir1:
                    openPIROptionsDialg("1");
                    break;
                case R.id.pir2:
                    openPIROptionsDialg("2");
                    break;
                case R.id.pir3:
                    openPIROptionsDialg("3");
                    break;
                case R.id.pir4:
                    openPIROptionsDialg("4");
                    break;
                case R.id.pir5:
                    openPIROptionsDialg("5");
                    break;
                case R.id.pir6:
                    openPIROptionsDialg("6");
                    break;
                case R.id.pir7:
                    openPIROptionsDialg("7");
                    break;
                case R.id.pir8:
                    openPIROptionsDialg("8");
                    break;
            }
            //startActivity(intent);
            return true;
        }
    }*/

    private class SetPirSecurityTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            ServiceHandler serviceHandler = new ServiceHandler();
            String args = "MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&Address=" + params[0];
            try {
                JSONObject response = serviceHandler.getJSONFromUrl("PairSecurity", args);
                if (response != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showMessageDialog("Paired successfully", getContext());
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showMessageDialog("Pairing failed", getContext());
                        }
                    });

                }
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
        }
    }
}
