package com.atg.onecontrolv3.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import java.util.List;

public class IRBlasterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = IRBlasterActivity.class.getSimpleName();
    String res = null;
    Button btnLearning, btnControl, btnPairing;
    TextView tvResponse;

    EditText etSSID, etPwd, etUUID;
    Button btnRouterSearch, btnPwdView, btnWifiSearch;

    Toolbar toolbar;
    OneControlPreferences mPreferences;
    boolean isClicked = false;

    //Wifi Configurarion
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    String[] wifiArray;
    ListView lvWifiList;
    ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irblaster);
        mPreferences = new OneControlPreferences(this);
        initializeViews();
        setToolBar();
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(isPermissionRequired()) {
            wifiScanStart();
        }

    }

    private void setToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Configure IR");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnLearning = (Button) findViewById(R.id.btnLearning);
        btnControl = (Button) findViewById(R.id.btnControl);
        tvResponse = (TextView) findViewById(R.id.tvResponse);

        btnLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable) {
                    makeIRRequest("get_IRL");
                } else {
                    Utils.showMessageDialog("No Internet.", IRBlasterActivity.this);
                }
            }
        });

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!res.equalsIgnoreCase("Error")) {
                    Log.v("send key", res);
                    makeIRRequest(res.trim());
                }
            }
        });

        etSSID = (EditText) findViewById(R.id.etSSID);
        etPwd = (EditText) findViewById(R.id.etPwd);
        etUUID = (EditText) findViewById(R.id.etUUID);
        btnRouterSearch = (Button) findViewById(R.id.btnRouterSearch);
        btnWifiSearch = (Button) findViewById(R.id.btnWifiSearch);
        btnPwdView = (Button) findViewById(R.id.btnPwdView);

        btnPairing = (Button) findViewById(R.id.btnPairing);
        btnWifiSearch.setOnClickListener(this);
        btnPairing.setOnClickListener(this);
        btnRouterSearch.setOnClickListener(this);
        btnPwdView.setOnClickListener(this);
    }

    private void wifiScanStart(){
        turnGPSOn();
        /*if (mainWifi.isWifiEnabled() == false) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }*/
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
    }
    private boolean isPermissionRequired(){
        if(Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                return false;
            }
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                wifiScanStart();
            }else{
                isPermissionRequired();
            }
        }
    }

    @Override
    protected void onPause() {
        if(isPermissionRequired()) {
            unregisterReceiver(receiverWifi);
        }
        turnGPSOff();
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPairing:
                String uuid = etUUID.getText().toString().trim();
                String ssid = etSSID.getText().toString().trim();
                String password = etPwd.getText().toString().trim();
                if (uuid.length() == 0 || ssid.length() == 0 || password.length() == 0) {
                    Toast.makeText(this, "Enter all fields", Toast.LENGTH_LONG).show();
                } else {
                    if (Utils.isNetworkAvailable) {
                        makeIRBlasterInitialConfiguration(uuid, ssid, password);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }

                }
                break;
            case R.id.btnWifiSearch:
                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                startActivity(intent);*/
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                break;
            case R.id.btnRouterSearch:
                ShowDialogForWifiNetworks(this);
                break;
            case R.id.btnPwdView:
                int start, end;
                if (isClicked) {
                    isClicked = false;
                    start = etPwd.getSelectionStart();
                    end = etPwd.getSelectionEnd();
                    etPwd.setInputType(129);
                    etPwd.setSelection(start, end);
                } else {
                    isClicked = true;
                    start = etPwd.getSelectionStart();
                    end = etPwd.getSelectionEnd();
                    etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPwd.setSelection(start, end);
                }

                break;
        }
    }

    private void ShowDialogForWifiNetworks(Context context) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wifi_networks_list);
        //dialog.setTitle("Choose a network");


        final ListView lvWifiList = (ListView) dialog.findViewById(R.id.lvWifiList);
        final TextView chooseTitle = (TextView) dialog.findViewById(R.id.choose_title);
        chooseTitle.setText("Choose a network");
        Log.e(TAG,"wifiArray:-:"+wifiArray);
        if (wifiArray != null && wifiArray.length > 0) {
            adapter = new ListViewAdapter(IRBlasterActivity.this, wifiArray);
            lvWifiList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        lvWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v(TAG, "selected SSID : " + wifiArray[i]);
                etSSID.setText(wifiArray[i]);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void makeIRRequest(String body) {
        Utils.showProgressDialog(IRBlasterActivity.this);
        String url = "http://10.0.0.15/v2/CI00848f3c";
        final String reqBody = body;
        Log.v("IR Request", "url : " + url);
        StringRequest strRequest = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                res = response.toString();
                res = res.replace("IR Learner Enabled", "");
                res = res.replace("\n", "");
                Log.v("IR res", response.toString() + ", trimmed : " + res);
                tvResponse.setText(res);
                Utils.hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("IR", "Error : " + error.getMessage());
                res = "Error";
                tvResponse.setText(res);
                Utils.hideProgressDialog();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "text/plain";//; charset=utf-8
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return reqBody == null ? null : reqBody.getBytes();//.getBytes("utf-8");
                } catch (Exception e) {
                    res = "Error";
                    tvResponse.setText(res);
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", reqBody, "utf-8");
                    return null;
                }
            }
        };

        AppController.getInstance().addToRequestQueue(strRequest);

    }

    private void makeIRBlasterInitialConfiguration(final String uuid, final String ssid, String password) {
        Utils.showProgressDialog(IRBlasterActivity.this);
        String url = "http://zmote.io/v2/" + uuid;//CI00f2e02f
        final String reqBody = ("connectwifi," + ssid + "," + password).trim();
        Log.v("IR Request", "url : " + url);
        StringRequest strRequest = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String res = response;
                Log.e(TAG, "makeIRBlasterInitialConfiguration() : " + response + ", trimmed : " + res);
                tvResponse.setText(res);
                Utils.hideProgressDialog();
                if (res.contains(ssid)) {
                    mPreferences.storeIRBlasterUUID(uuid);
                    showMessageDialog("Configured successfully.", IRBlasterActivity.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("IR", "Error : " + error.getMessage());
                res = "Error";
                tvResponse.setText(res);
                Utils.hideProgressDialog();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "text/plain; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return reqBody == null ? null : reqBody.getBytes("utf-8");
                } catch (Exception e) {
                    res = "Error";
                    tvResponse.setText(res);
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", reqBody, "utf-8");
                    return null;
                }
            }
        };

        AppController.getInstance().addToRequestQueue(strRequest);

    }

    public void showMessageDialog(String message, Context context) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        alertBox.show();
    }

    // Broadcast receiver class called its receive method
    // when number of wifi connections changed

    private void turnGPSOn() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);

        int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
        Log.v("location mode ", locationMode + "");
        if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
        /*if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }*/
    }

    private void turnGPSOff() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);

        int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
        Log.v("location mode ", locationMode + "");

        if (locationMode != Settings.Secure.LOCATION_MODE_OFF) {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
        /*if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }*/
    }

    public static class ViewHolder {

        public RadioButton rbSSIDName;
        public RadioGroup radioGroup;
        TextView tvSSIDName;
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\n        Number Of Wifi connections : " + wifiList.size() + "\n\n");

            wifiArray = new String[wifiList.size()];
            for (int i = 0; i < wifiList.size(); i++) {

                sb.append(new Integer(i + 1).toString() + ". ");
                sb.append((wifiList.get(i)).toString());
                sb.append("\n\n");
                Log.v(TAG, "ssid : " + wifiList.get(i).SSID);
                Log.v(TAG, "capabilities : " + wifiList.get(i).capabilities);
                Log.v(TAG, "bssid : " + wifiList.get(i).BSSID);

                wifiArray[i] = wifiList.get(i).SSID;

            }
        }

    }

    public class ListViewAdapter extends BaseAdapter {

        Context context;
        String[] data;
        LayoutInflater inflater = null;

        public ListViewAdapter(Context context, String[] data) {
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View vi = view;
            WifiConfigActivity.ViewHolder holder;
            if (view == null) {

                /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
                vi = inflater.inflate(R.layout.wifi_list_row, null);

                /****** View Holder Object to contain tabitem.xml file elements ******/

                holder = new WifiConfigActivity.ViewHolder();
                holder.rbSSIDName = (RadioButton) vi.findViewById(R.id.rbSSIDName);
                holder.radioGroup = (RadioGroup) vi.findViewById(R.id.radioGroup);
                holder.tvSSIDName = (TextView) vi.findViewById(R.id.tvSSIDName);

                /************  Set holder with LayoutInflater ************/
                vi.setTag(holder);
            } else
                holder = (WifiConfigActivity.ViewHolder) vi.getTag();

            if (data.length != 0) {
                //((RadioButton) holder.radioGroup.getChildAt(i)).setText(String.valueOf(data[i]));
                //holder.rbSSIDName.setText(String.valueOf(data[i])+"");
                holder.tvSSIDName.setText(data[i] + "");
            }

            return vi;
        }
    }

}
