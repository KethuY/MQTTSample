package com.atg.onecontrolv3.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.GatewaysSelectionAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.fragments.DashboardFragment;
import com.atg.onecontrolv3.fragments.SafetyFragNew;
import com.atg.onecontrolv3.fragments.ScenesTimerFragment;
import com.atg.onecontrolv3.fragments.SetTimerFragment;
import com.atg.onecontrolv3.fragments.SettingFragment;
import com.atg.onecontrolv3.fragments.TabFragment;
import com.atg.onecontrolv3.helpers.BottomNavigationViewHelper;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.SingleMacModel;
import com.atg.onecontrolv3.models.UserMacsModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.preferances.MyPreferences;
import com.atg.onecontrolv3.preferances.OneControlPreferences;
import com.atg.onecontrolv3.rest.ApiClient;
import com.atg.onecontrolv3.rest.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.atg.onecontrolv3.helpers.Utils.MAC_ID;
import static com.atg.onecontrolv3.helpers.Utils.MAC_PIN;
import static com.atg.onecontrolv3.helpers.Utils.isNetworkAvailable;
import static com.atg.onecontrolv3.helpers.Utils.replaceTilt;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabFragment.PageChangeListener, OnItemClickListener, MqttHelper.responseListener, DashboardFragment.ActiveCountListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    public static String imei;
    OnItemClickListener mListener;
    TextView mGatewayNameTv, mTotalActiveCntTv;
    LinearLayout mContentMainLl;
    MqttHelper helper;
    int rumId = 1;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    String userModeStr = "";
    private TabFragment mTabFragment;
    private int mFrom;
    private AlertDialog alertDialog;
    private Vibrator vibrator;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mTabFragment = new TabFragment();
            mFragmentManager = getSupportFragmentManager();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            //isSettingsClicked = false;

            if (Utils.MAC_PIN != null && !Utils.MAC_PIN.isEmpty()) {
                switch (item.getItemId()) {
                    case R.id.navigation_appliance:
                        mTabFragment.curPos = 0;
                        mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                        return true;
                    case R.id.navigation_safety:
                        mTabFragment.curPos = 1;
                        mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                        return true;
                    case R.id.navigation_surveillance:
                        mTabFragment.curPos = 2;
                        mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                        return true;
                    case R.id.navigation_timer:
                        mTabFragment.curPos = 3;
                        mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                        return true;
                    case R.id.navigation_settings:
                        mTabFragment.curPos = 4;
                        //isSettingsClicked = true;
                        mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                        return true;
                }
            } else {
                Utils.showToast(MainActivity.this, "Please give Gateway Pin to proceed further");
            }

            /*if (menu != null) {
                onPrepareOptionsMenu(menu);
            }*/
            return false;
        }

    };

    private ArrayList<SingleMacModel> mUserMacsArrLst;
    private RecyclerView mGatewaySelectionRv;
    private OneControlPreferences mPreferences;
    private NavigationView mNavigationView;
    private BottomNavigationView navigation;
    private DrawerLayout drawer;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.nav_menu);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.over_flow);
        toolbar.setOverflowIcon(drawable);
        setSupportActionBar(toolbar);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mListener = this;
        mPreferences = new OneControlPreferences(MainActivity.this);

        /*mPreferences.storeMACPin("20f85eeee93e","KWPNYLEXZNZMHXV");
        mPreferences.storeMACPin("20f85eeee968","YWXXFHDRCDGCGAS");
        mPreferences.storeMACPin("20f85eeef2ce","BQDMDBUADRIGZL9");
        mPreferences.storeMACPin("20f85eeef343","GVFLQRJTAGWENEL");
        mPreferences.storeMACPin("20f85ef2b294","GVFLQRJTAGWEXXX");
        mPreferences.storeMACPin("20f85eeee9aa","MZSTWVGNKCHNAWF");
        mPreferences.storeMACPin("20f85eeee94d","NEPKDJOSFFWCPSV");
        mPreferences.storeMACPin("20f85eeef316","WGFVOSYWYNBEDZP");
        mPreferences.storeMACPin("20f85eeee95c","JSACTHGITLTCGQS");
        mPreferences.storeMACPin("20F85EEEF2CE","BQDMDBUADRIGZL9");
        mPreferences.storeMACPin("20f85ef2b7a4","AOVTYWMBXWNVHOA");
        mPreferences.storeMACPin("20f85ef2b79e","AOVTYWMBXWNVHOC");*/

        mFrom = getIntent().getIntExtra("from", 1);

        initializeViews();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        /*Newly added code..!*/
        /*toggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.lighting_home, getTheme());
        toggle.setHomeAsUpIndicator(drawable);*/
        //------------------------------------------------
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mGatewaySelectionRv = (RecyclerView) findViewById(R.id.gateways_selection_rv);

        /*ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setIcon(R.drawable.nav_menu);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadIMEI();
            }
        }, 500);

        mTotalActiveCntTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                if (Utils.isNetworkAvailable) {
                    helper.sendMsg("A|" + rumId + "|M");
                }
            }
        });

        mGatewayNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                if (Utils.isNetworkAvailable) {
                    helper.sendMsg("A|" + rumId + "|M");
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getUserMacs() {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<UserMacsModel> call = apiService.getUserMacs(Utils.IMEI);
        call.enqueue(new Callback<UserMacsModel>() {
            @Override
            public void onResponse(Call<UserMacsModel> call, Response<UserMacsModel> response) {
                String strUrl = response.raw().request().url().toString();
                int statusCode = response.code();
                Utils.printLog(TAG, "statusCode:-:" + statusCode + " strUrl:-:" + strUrl);
                if (statusCode == 200) {
                    mUserMacsArrLst = response.body().getSingleMacArrLst();
                    if (null != mUserMacsArrLst && !mUserMacsArrLst.isEmpty()) {

                        //Checking for GATEWAY_NAME in Preferences..!
                        String gatewayName = MyPreferences.getString(MyPreferences.PrefType.GATEWAY_NAME, getApplicationContext());
                        if (null != gatewayName && !gatewayName.isEmpty())
                            mGatewayNameTv.setText(replaceTilt(gatewayName).toUpperCase());
                        else
                            mGatewayNameTv.setText(replaceTilt(mUserMacsArrLst.get(0).getMacName().toUpperCase()));

                        int userMode = MyPreferences.getInt(MyPreferences.PrefType.LOV, getApplicationContext());
                        if (userMode == 0) {
                            userMode = mUserMacsArrLst.get(0).getLov();
                            MyPreferences.add(MyPreferences.PrefType.LOV, mUserMacsArrLst.get(0).getLov(), getApplicationContext());
                        }
                        if (userMode == 1) {
                            userModeStr = "Master";
                        } else if (userMode == 2) {
                            userModeStr = "SuperUser";
                        } else {
                            userModeStr = "SubUser";
                        }
                        if (!userModeStr.isEmpty()) {
                            if (null != menu) {
                                onPrepareOptionsMenu(menu);
                            }
                        }

                        //Checking for MAC_ID in Preferences..!
                        String macId = MyPreferences.getString(MyPreferences.PrefType.MAC_ID, getApplicationContext());
                        if (null != macId && !macId.isEmpty()) {
                            Utils.MAC_ID = macId;//Storing MAC_ID
                        } else {
                            Utils.MAC_ID = mUserMacsArrLst.get(0).getUid();//Storing MAC_ID
                            MyPreferences.add(MyPreferences.PrefType.MAC_ID, MAC_ID, getApplicationContext());
                        }

                        //Getting MAC_PIN from MAC_ID..!
                        // Utils.printLog(TAG, "MacId:-:" + Utils.MAC_ID + " MacPinnnn:-:" + mPreferences.getMACPin(Utils.MAC_ID));
                        Utils.MAC_PIN = mPreferences.getMACPin(Utils.MAC_ID);//Storing MAC_PIN
                        if (Utils.MAC_PIN != null && !Utils.MAC_PIN.isEmpty()) {
                            launchFragment();
                            handleMqttConnection();
                        } else {
                            requestPIN(MainActivity.this, mUserMacsArrLst.get(0).getUid());
                        }

                        //Checking for GATEWAY_POS in Preferences..!
                        int gatewayPos = MyPreferences.getInt(MyPreferences.PrefType.GATEWAY_POS, getApplicationContext());
                        LinearLayoutManager horizontalLayoutManager
                                = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                        //horizontalLayoutManager.setInitialPrefetchItemCount(3);
                        horizontalLayoutManager.scrollToPositionWithOffset(gatewayPos, 10);
                        mGatewaySelectionRv.setLayoutManager(horizontalLayoutManager);

                        GatewaysSelectionAdapter adapter = new GatewaysSelectionAdapter(MainActivity.this, mUserMacsArrLst, mListener);
                        mGatewaySelectionRv.setAdapter(adapter);

                    } else {
                        Utils.printLog(TAG, "No Gateways found!");
                        //Utils.showToast(MainActivity.this, "No rooms found!");
                    }
                } else {
                    Utils.printLog(TAG, "Something went wrong, Please try again!");
                    Utils.showToast(MainActivity.this, "Something went wrong, Please try again!");
                }
            }

            @Override
            public void onFailure(Call<UserMacsModel> call, Throwable t) {
                Utils.printLog(TAG, "Retrofit error while parsing :-:" + t.getMessage());
                Utils.showToast(MainActivity.this, "Something went wrong, Please try again!");
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (userModeStr.equalsIgnoreCase("Master")) {
            menu.findItem(R.id.action_sub_user).setEnabled(true);
        } else {
            menu.findItem(R.id.action_sub_user).setEnabled(false);
        }
        invalidateOptionsMenu();
        return true;
    }

    public void launchFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        int menuPos = 0;
        switch (mFrom) {
            case 1:
                transaction.replace(R.id.containerView, new DashboardFragment());
                break;
            case 2:
                menuPos = 1;
                transaction.replace(R.id.containerView, new SafetyFragNew());
                break;
            case 3:
                                /*menuPos = 2;
                                transaction.replace(R.id.containerView, new SurveillanceFragment());*/
                break;
            case 4:
                menuPos = 3;
                transaction.replace(R.id.containerView, new SetTimerFragment());
                break;
            case 5:
                menuPos = 2;
                transaction.replace(R.id.containerView, new ScenesTimerFragment());
                break;
            case 6:
                       /* menuPos = 5;
                        transaction.replace(R.id.containerView, new SettingFragment());*/
                break;
            default:
                transaction.replace(R.id.containerView, new DashboardFragment());
                break;
        }
        transaction.commit();
        navigation.getMenu().getItem(menuPos).setChecked(true);
    }


    public void requestPIN(Context context, final String macId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.enter_mac_pin_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText macPinEt = dialogView.findViewById(R.id.pir_name);
        final TextView macIDTv = dialogView.findViewById(R.id.mac_id_tv);
        macIDTv.setText(macId);
        Button submit = dialogView.findViewById(R.id.btn_submit);
        Button cancel = dialogView.findViewById(R.id.btn_cancel);
        alertDialog = dialogBuilder.create();
        alertDialog.show();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strPin = macPinEt.getText().toString().trim();
                strPin = strPin.replace(" ", "~");
                Log.e(TAG, "strName:-:" + strPin);
                if (null != strPin && !TextUtils.isEmpty(strPin)) {
                    if (strPin.length() == 15) {
                        if (Utils.isNetworkAvailable) {
                            pinValidationCheck(strPin, macId);
                        } else {
                            Utils.showMessageDialog("No Internet", MainActivity.this);
                        }
                    } else {
                        macPinEt.setError("Please enter valid PIN");
                    }
                } else {
                    macPinEt.setError("Required!");
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


    private void pinValidationCheck(final String mStrPin, final String macId) {
        String finalUrl = ServiceHandler.baseUrl + "IsValidMacPin?MacId=" + Utils.MAC_ID + "&PIN=" + mStrPin + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "finalUrl:-:" + finalUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                parseJson(response, mStrPin, macId);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRClientRequest()", "Error : " + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void parseJson(String response, String mStrPin, String macId) {
        try {
            int code = 0;
            String msg = "";
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonObject1 = jsonObject.getJSONObject("IsValidMacPinResult");
            code = jsonObject1.getInt("Code");
            msg = jsonObject1.getString("Message");
            if (code == 200) {
                if (msg.equalsIgnoreCase("Valid")) {
                    //mPreferences.storeMACAddress(data.get(mPosition).getUid());
                    MyPreferences.add(MyPreferences.PrefType.MAC_ID, macId, getApplicationContext());
                    //Utils.MAC_ID = mPreferences.getMACAddress();
                    Utils.MAC_ID = MyPreferences.getString(MyPreferences.PrefType.MAC_ID, getApplicationContext());
                    mPreferences.storeMACPin(Utils.MAC_ID, mStrPin);

                    Utils.MAC_PIN = mPreferences.getMACPin(Utils.MAC_ID);
                    if (MAC_PIN != null && !MAC_PIN.isEmpty()) {
                        handleMqttConnection();
                        navigation.getMenu().getItem(0).setChecked(true);

                        FragmentManager manager = getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.containerView, new DashboardFragment());
                        transaction.commit();
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                    } else {
                        Utils.printLog(TAG, "MQTT Conn failure..");
                    }
                } else if (msg.equalsIgnoreCase("Invalid")) {
                    Utils.showMessageDialog("MACID and PIN mismatched, Please try again", MainActivity.this);
                }
            } else {
                Utils.showMessageDialog("Something went wrong, Please try again", MainActivity.this);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    private void handleMqttConnection() {
        MqttHelper.mPublishTopic = Utils.P_TOPIC + Utils.MAC_ID;
        MqttHelper.mSubscribeTopic = Utils.S_TOPIC + Utils.MAC_ID;
        MqttHelper.mSecurityTopic = Utils.SECURITY_TOPIC + Utils.MAC_ID;
        MqttHelper.mArmDisArmTopic = Utils.ARMDISARM_TOPIC + Utils.MAC_ID;
        MqttHelper.responseListener mqttListener;

        mqttListener = this;
        helper = new MqttHelper(MainActivity.this, mqttListener);
    }

    private void initializeViews() {

        mGatewayNameTv = (TextView) findViewById(R.id.gateway_name_tv);
        mContentMainLl = (LinearLayout) findViewById(R.id.content_main);
        mTotalActiveCntTv = (TextView) findViewById(R.id.total_active_cnt_tv);
    }

    /*@Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_energy_monitoring:
                startActivity(new Intent(this, EnergyMgmtActivity.class));
                break;
            case R.id.action_themes:
                startActivity(new Intent(this, ThemeSelectionActivity.class));
                break;
            case R.id.action_sub_user:
                if (userModeStr.equalsIgnoreCase("Master")) {
                    startActivity(new Intent(this, AddSubUserActivity.class));
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        try {
            itemSelection(id);
        } catch (Exception e) {
            Utils.printLog(TAG, "navItemClick EXCP:-:" + e.getMessage());
        }
        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void itemSelection(int mSelectedId) throws Exception {
        // MyPreferences.add(MyPreferences.PrefType.IS_ALREADY_ENTERED, false, getApplicationContext());
        //FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mNavigationView.setCheckedItem(mSelectedId);
        switch (mSelectedId) {
            case R.id.nav_item_home:
                Intent intentHome = new Intent(MainActivity.this, LandingPageActivity.class);
                startActivity(intentHome);
                finish();
                break;
            case R.id.nav_item_gateways:
                /*if (helper != null) {
                    try {
                        helper.client.unsubscribe(MqttHelper.mSubscribeTopic);
                    } catch (Exception e) {
                        Log.e(TAG, "unsubscribeEXC:-:" + e.getMessage());
                    }
                }
                Intent intentGW = new Intent(MainActivity.this, SelectionActivity.class);
                startActivity(intentGW);
                finish();*/
                break;
            case R.id.nav_item_configuration:
                Intent intentWifiConfig = new Intent(MainActivity.this, WifiConfigActivity.class);
                intentWifiConfig.putExtra("IS_REGISTER", false);
                startActivity(intentWifiConfig);
                break;
            case R.id.nav_item_unpair_wifi:
                showConfirmationDialog("Do you want to unpair Gateway?", MainActivity.this);
                break;
            case R.id.nav_item_config_ir_remote:
                Intent intentSwitching = new Intent(MainActivity.this, IRBlasterActivity.class);
                startActivity(intentSwitching);
                break;
            /*case R.id.nav_item_subuser:
                if (userMode.equalsIgnoreCase("Master")) {
                    Intent intentAddUser = new Intent(MainActivity.this, AddSubUserActivity.class);
                    startActivity(intentAddUser);
                }
                break;*/
            case R.id.nav_item_account_details:
                break;
            case R.id.nav_item_feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@onecontrol.in"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
                break;
            case R.id.nav_item_faq:
                break;
            case R.id.nav_item_about_oc:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.gateway_ll:
                FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
                List<Fragment> fragments = fragmentManager.getFragments();

                FragmentManager managerA = getSupportFragmentManager();
                FragmentTransaction transactionA = managerA.beginTransaction();
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        if (fragment != null && fragment.isVisible()) {
                            if (fragment instanceof DashboardFragment) {
                                transactionA.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                            } else if (fragment instanceof SafetyFragNew) {
                                transactionA.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                            } else if (fragment instanceof ScenesTimerFragment) {
                                transactionA.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                            } else if (fragment instanceof SetTimerFragment) {
                                transactionA.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                            } else if (fragment instanceof SettingFragment) {
                                transactionA.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                            }
                        }
                    }
                }


                /*Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.containerView);

                if (currentFragment instanceof DashboardFragment) {
                    Log.e(TAG, "your Fragment is Visible");
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                } else if (currentFragment instanceof SafetyFragNew) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                } else if (currentFragment instanceof ScenesTimerFragment) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                } else if (currentFragment instanceof SetTimerFragment) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                } else if (currentFragment instanceof SettingFragment) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(getSupportFragmentManager().findFragmentById(R.id.containerView)).commit();
                }*/

                if (helper != null) {
                    try {
                        helper.client.unsubscribe(MqttHelper.mSubscribeTopic);
                    } catch (Exception e) {
                        Log.e(TAG, "unsubscribeEXC:-:" + e.getMessage());
                    }
                }

                Utils.MAC_ID = "";
                Utils.MAC_PIN = "";

                mGatewayNameTv.setText(replaceTilt(mUserMacsArrLst.get(position).getMacName().toUpperCase()));
                Utils.MAC_ID = mUserMacsArrLst.get(position).getUid();//Storing MAC_ID
                String gatewayName = mUserMacsArrLst.get(position).getMacName();
                MyPreferences.add(MyPreferences.PrefType.MAC_ID, MAC_ID, getApplicationContext());
                Utils.MAC_PIN = mPreferences.getMACPin(Utils.MAC_ID);//Storing MAC_PIN
                Utils.printLog(TAG, "ppppp:-:" + Utils.MAC_PIN);
                Utils.printLog(TAG, "iiiii:-:" + Utils.MAC_ID);
                MyPreferences.add(MyPreferences.PrefType.GATEWAY_NAME, gatewayName, getApplicationContext());
                MyPreferences.add(MyPreferences.PrefType.GATEWAY_POS, position, getApplicationContext());
                MyPreferences.add(MyPreferences.PrefType.LOV, mUserMacsArrLst.get(position).getLov(), getApplicationContext());
                int userMode = MyPreferences.getInt(MyPreferences.PrefType.LOV, getApplicationContext());
                if (userMode == 1) {
                    userModeStr = "Master";
                } else if (userMode == 2) {
                    userModeStr = "SuperUser";
                } else {
                    userModeStr = "SubUser";
                }
                if (!userModeStr.isEmpty()) {
                    if (null != menu) {
                        onPrepareOptionsMenu(menu);
                    }
                }

                //Change Mqtt connection..!
                if (Utils.MAC_PIN != null && !Utils.MAC_PIN.isEmpty()) {
                    handleMqttConnection();
                    navigation.getMenu().getItem(0).setChecked(true);

                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.containerView, new DashboardFragment());
                    transaction.commit();
                } else {
                    requestPIN(MainActivity.this, mUserMacsArrLst.get(position).getUid());
                }
                break;
        }
    }

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission Request")
                    .setMessage("Permission for enable to read phone state")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        }
                    })
                    .setIcon(R.drawable.onecontrol_icon)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doPermissionGrantedStuffs();
            } else {
                alertAlert("Permission for enable to read phone state");
            }
        }
    }

    private void alertAlert(String msg) {
        new AlertDialog.Builder(MainActivity.this)
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

    @SuppressLint("HardwareIds")
    public void doPermissionGrantedStuffs() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
        Utils.IMEI = imei;//Storing MAC_ID
        //Utils.IMEI = "358224073370292";//Storing MAC_ID
        MyPreferences.add(MyPreferences.PrefType.IMEI, Utils.IMEI, getApplicationContext());
        if (Utils.IMEI != null && !TextUtils.isEmpty(Utils.IMEI)) {
            if (isNetworkAvailable) {
                getUserMacs();
            }
        }
        Utils.printLog(TAG, "IMEI :-:" + imei);
    }

    @Override
    public void onMqttResponse(String res) {

    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }

    @Override
    public void onCountChangeListener(int totalActiveCnt, int rumId) {
        this.rumId = rumId;
        mTotalActiveCntTv.setText(String.valueOf(totalActiveCnt));
    }

    @Override
    public void onViewPageChangeListener(int pos) {

    }

    @Override
    public void onBackPressed() {
        if (null != mTabFragment) {
            if (mTabFragment.curPos == 0) {
                //MyPreferences.add(MyPreferences.PrefType.IS_ALREADY_ENTERED, false, getApplicationContext());
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            } else {

                //   onViewPageChangeListener(0);

                navigation.getMenu().getItem(0).setChecked(true);
                mFragmentManager = getSupportFragmentManager();
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mTabFragment = new TabFragment();
                mTabFragment.curPos = 0;
                mFragmentTransaction.replace(R.id.containerView, mTabFragment).commit();
                //isSettingsClicked = false;
                /*if (menu != null) {
                    onPrepareOptionsMenu(menu);
                }*/
            }
        } else {
            mTabFragment = new TabFragment();
            //MyPreferences.add(MyPreferences.PrefType.IS_ALREADY_ENTERED, false, getApplicationContext());
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }

        drawer.closeDrawer(Gravity.START);

        /*Intent intent = new Intent(MainActivity.this, LandingPageActivity.class);
        startActivity(intent);
        finish();*/
    }

    public void showConfirmationDialog(String message, Context context) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //new UnPairGatewayTask(MainActivity.this);
                if (Utils.isNetworkAvailable) {
                    helper.sendMsg("u");//need response from Gateway..!
                } else {
                    Utils.showMessageDialog("No Internet", MainActivity.this);
                }
            }
        });

        alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertBox.show();
    }
}
