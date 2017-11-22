package com.atg.onecontrolv3.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.RegisterModel;
import com.atg.onecontrolv3.models.RegistrationProvider;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.preferances.MyPreferences;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import java.util.List;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    public static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    Button btnRegister, btnProfileRegister;
    EditText etMobile, etIMEI;
    EditText etUID, etPIN, etFirstName, etLastName, etEmail, etMacName;
    LinearLayout registerLayout, profileLayout;
    TextView tvClickHere;
    Toolbar toolbar;
    Intent in;
    String imei;
    String mobile;
    List<RegisterModel> list;
    TransparentProgressDialog pd;
    TelephonyManager telephonyManager;
    OneControlPreferences mPreferences;

    public static boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //requestPermission();
        mPreferences = new OneControlPreferences(RegisterActivity.this);
        in = getIntent();
        mobile = in.getStringExtra("MOBILE");
        setToolBar();
        initializeViews();
        loadIMEI();

        etMobile.setText(mobile);
        etIMEI.setText(imei);
        tvClickHere = (TextView) findViewById(R.id.tvClickHere);

        /*tvClickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });*/
    }

    private void initializeViews() {
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnProfileRegister = (Button) findViewById(R.id.btnProfileRegister);
        btnRegister.setOnClickListener(this);
        btnProfileRegister.setOnClickListener(this);

        registerLayout = (LinearLayout) findViewById(R.id.registerLayout);
        profileLayout = (LinearLayout) findViewById(R.id.profileLayout);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etMobile = (EditText) findViewById(R.id.etMobile);
        etIMEI = (EditText) findViewById(R.id.etIMEI);
        etUID = (EditText) findViewById(R.id.etUID);
        etPIN = (EditText) findViewById(R.id.etPIN);
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etMacName = (EditText) findViewById(R.id.etMacName);

    }

    /*private void requestPermission(){
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        100);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                getIMEI();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        *//*switch (requestCode){
            case 100:*//*
                if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getIMEI();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
        //}
    }

    private void getIMEI(){
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
    }*/

    public void setToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setTitle("REGISTER");
    }

    /**
     * Called when the 'loadIMEI' function is triggered.
     */
    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
        } else {
            // READ_PHONE_STATE permission is already been granted.
            doPermissionGrantedStuffs();
        }
    }

    /**
     * Requests the READ_PHONE_STATE permission.
     * If the permission has been denied previously, a dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Permission Request")
                    .setMessage("Permission for enable to read phone state")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(RegisterActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        }
                    })
                    .setIcon(R.drawable.onecontrol_icon)
                    .show();
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted, proceed with displaying IMEI Number
                //alertAlert(getString(R.string.permision_available_read_phone_state));
                doPermissionGrantedStuffs();
            } else {
                alertAlert("Permission for enable to read phone state");
            }
        }
    }

    private void alertAlert(String msg) {
        new AlertDialog.Builder(RegisterActivity.this)
                .setTitle("Permission Request")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do somthing here
                        doPermissionGrantedStuffs();
                    }
                })
                .setIcon(R.drawable.onecontrol_icon)
                .show();
    }

    public void doPermissionGrantedStuffs() {
        //Have an  object of TelephonyManager
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Get IMEI Number of Phone  //////////////// for this example i only need the IMEI
        imei = tm.getDeviceId();
        Log.e("Register", "IMEI : " + imei);
        etIMEI.setText(imei);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnRegister:
                //Temperory
                //etUID.setText("20F85EC28542");
                //etPIN.setText("424153562484632");
                //////////////////////////

                /*String uid = etUID.getText().toString().trim().replace(":", "");
                String pin = etPIN.getText().toString();

                if (uid.length() == 0) {
                    etUID.setError("Enter valid UID");
                } else if (pin.length() == 0) {
                    etPIN.setError("Enter valid MACPIN");
                } else {

//                    mPreferences.storeMACAddress(uid);
//                    mPreferences.storeIMEI(imei);

                    if (Utils.isNetworkAvailable(this)) {
                        new RegisterUserTask().execute(mobile, imei, uid, pin);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }

                    //registerLayout.setVisibility(View.GONE);
                    //profileLayout.setVisibility(View.VISIBLE);
                }*/
                break;
            case R.id.btnProfileRegister:


                /*String uid = etUID.getText().toString().trim().replace(":", "");
                String pin = etPIN.getText().toString();

                if (uid.length() == 0) {
                    etUID.setError("Enter valid UID");
                } else if (pin.length() == 0) {
                    etPIN.setError("Enter valid MACPIN");
                } else {

//                    mPreferences.storeMACAddress(uid);
//                    mPreferences.storeIMEI(imei);

                    if (Utils.isNetworkAvailable(this)) {
                        new RegisterUserTask().execute(mobile, imei, uid, pin);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }

                    //registerLayout.setVisibility(View.GONE);
                    //profileLayout.setVisibility(View.VISIBLE);
                }*/
                String uid = etUID.getText().toString().trim().replace(":", "").toLowerCase();
                String pin = etPIN.getText().toString();
                String firstName = etFirstName.getText().toString();
                firstName = firstName.contains(" ") ? firstName.replace(" ", "~") : firstName;
                String lastName = etLastName.getText().toString();
                lastName = lastName.contains(" ") ? lastName.replace(" ", "~") : lastName;
                String email = etEmail.getText().toString();
                String macName = etMacName.getText().toString();
                macName = macName.contains(" ") ? macName.replace(" ", "~") : macName;

                String macId = etUID.getText().toString().trim().replace(":", "");
                if (uid.length() == 0) {
                    etUID.setError("Enter valid UID");
                } else if (pin.length() == 0) {
                    etPIN.setError("Enter valid MACPIN");
                }
                if (firstName.length() == 0) {
                    etFirstName.setError("Enter name");
                } else if (lastName.length() == 0) {
                    etLastName.setError("Enter display name");
                } else if (email.length() == 0 || !isValidEmail(email)) {
                    etEmail.setError("Enter valid Email");
                } else if (macName.length() == 0) {
                    etMacName.setError("Enter MAC name");
                } else {
                    // mPreferences.storeUserName(lastName);
                    /*if (Utils.isNetworkAvailable(this)) {
                        new UpdateUserProfileTask().execute(mobile, imei, firstName, lastName, email, macName, macId);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }*/
                    if (Utils.isNetworkAvailable) {
                        new RegisterUserTask().execute(mobile, imei, uid, pin, firstName, lastName, email, macName);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }

                }

                //startActivity(new Intent(RegisterActivity.this, HomeActivity.class));

                break;
        }

    }

    public void showMessageDialog(final String macId, String message, Context context) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //registerLayout.setVisibility(View.GONE);
                //profileLayout.setVisibility(View.VISIBLE);
                Utils.MAC_ID = mPreferences.getMACAddress();
                Utils.MAC_PIN = mPreferences.getMACPin(macId);
//                    Utils.MACID = etUID.getText().toString();
                Utils.IMEI = imei;
                   /* Utils.MACID = mPreferences.getMACAddress();
                    Utils.IMEI = mPreferences.getIMEI();*/
                mPreferences.storeUserMode("Master");
                MqttHelper.mPublishTopic = Utils.P_TOPIC + Utils.MAC_ID;
                MqttHelper.mSubscribeTopic = Utils.S_TOPIC + Utils.MAC_ID;
                /*MqttHelper.mSecurityTopic = Utils.SECURITY_TOPIC + Utils.MACID;
                MqttHelper.mArmDisArmTopic = Utils.ARMDISARM_TOPIC + Utils.MACID;*/
                Intent intent = new Intent(RegisterActivity.this, WifiConfigActivity.class);//HomeActivity
                intent.putExtra("IS_REGISTER", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                mPreferences.setPreference(true);
                finish();
            }
        });
        alertBox.show();
    }

    //Generating OTP
    private class RegisterUserTask extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new TransparentProgressDialog(RegisterActivity.this, R.drawable.progress);
            pd.show();
        }


        @Override
        protected String doInBackground(String... params) {
            RegistrationProvider provider = new RegistrationProvider();
            list = provider.serviceRegistration(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();

            mPreferences.storeMACAddress((etUID.getText().toString().trim()).replace(":", ""));
            mPreferences.storeIMEI(imei);
            mPreferences.storeMACPin((etUID.getText().toString().trim()).replace(":", ""), etPIN.getText().toString().trim());

//            mPreferences.storeIMEI("356271078592596");

            if (list != null && list.size() != 0) {
                String code = list.get(0).getCode();
                String desc = list.get(0).getDescription();
                switch (code) {
                    case "1":
                        showMessageDialog((etUID.getText().toString().trim()).replace(":", ""), desc, RegisterActivity.this);
                        break;
                    case "2":
                        Utils.showMessageDialog(desc, RegisterActivity.this);
                        break;
                    case "3":
                        Utils.showMessageDialog(desc, RegisterActivity.this);
                        break;
                    case "4":
                        Utils.showMessageDialog(desc, RegisterActivity.this);
                        break;
                    case "5":
                        Utils.showMessageDialog(desc, RegisterActivity.this);
                        break;
                    case "6":
                        Utils.showMessageDialog("MAC ID and PIN are not matching", RegisterActivity.this);
                        break;
                    default:
                        //Registering mac id and imei for the user throughout the user
                        Utils.MAC_ID = mPreferences.getMACAddress();
                        Utils.MAC_PIN = mPreferences.getMACPin((etUID.getText().toString().trim()).replace(":", ""));
                        Log.e(TAG, "MACNAMEEE:-:" + etMacName.getText().toString().trim());
                        MyPreferences.add(MyPreferences.PrefType.MacName,
                                etMacName.getText().toString().trim(), getApplicationContext());
//                    Utils.MACID = etUID.getText().toString();
                        Utils.IMEI = imei;
                   /* Utils.MACID = mPreferences.getMACAddress();
                    Utils.IMEI = mPreferences.getIMEI();*/
                        mPreferences.storeUserMode("Master");
                        Intent intent = new Intent(RegisterActivity.this, WifiConfigActivity.class);//HomeActivity

                        intent.putExtra("IS_REGISTER", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                        mPreferences.setPreference(true);
                        finish();

                        //registerLayout.setVisibility(View.GONE);
                        //profileLayout.setVisibility(View.VISIBLE);
                        break;
                }

                /*registerLayout.setVisibility(View.GONE);
                profileLayout.setVisibility(View.VISIBLE);*/
            }
        }
    }

    //Generating OTP
    private class UpdateUserProfileTask extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;
        String status;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new TransparentProgressDialog(RegisterActivity.this, R.drawable.progress);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            RegistrationProvider provider = new RegistrationProvider();
            status = provider.serviceUpdateUserProfile(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (status.equalsIgnoreCase("Success")) {
                Utils.MAC_ID = mPreferences.getMACAddress();
                Utils.IMEI = mPreferences.getIMEI();
                mPreferences.storeUserMode("Master");
                Intent intent = new Intent(RegisterActivity.this, WifiConfigActivity.class);//HomeActivity
                intent.putExtra("IS_REGISTER", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                mPreferences.setPreference(true);
                finish();
            } else if (status.equalsIgnoreCase("Error")) {
                Toast.makeText(RegisterActivity.this, "User Profile Updating Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
