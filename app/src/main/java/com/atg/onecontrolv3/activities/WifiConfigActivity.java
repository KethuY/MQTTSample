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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

public class WifiConfigActivity extends BaseActivity implements View.OnClickListener, MqttHelper.responseListener {

    private static final String TAG = WifiConfigActivity.class.getSimpleName();
    // Create an android.os.Handler that you can use to post Runnable
    // to the UI thread.
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    boolean isSocketConnected = false;
    Button btnPairing, btnWifiSearch, btnRouterSearch, btnPwdView;
    TextView tvSkip;
    EditText etSSID, etPwd;
    Toolbar toolbar;
    String strSSID, strPwd;
    //Wifi Configuration
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    String[] wifiArray;
    ListView lvWifiList;
    ListViewAdapter adapter;
    OneControlPreferences mPreferences;
    boolean isClicked = false;
    TransparentProgressDialog pDialog;
    MqttHelper.responseListener mqttListener = null;
    private BufferedReader br = null;
    private PrintWriter pw = null;
    private String ip = "192.168.91.1";//
    private int port = 4000;
    private Socket sock;
    // Create a Runnable that will poll a server and send updates to the
    // UI thread.
    private final Thread mConnectAndPoll = new Thread(new Runnable() {

        @Override
        public void run() {
            connect();
            if (isSocketConnected)
                sendMessage("1|" + strSSID + "|" + strPwd + "|1||");
        }
    });
    private boolean isV3 = false;
    private MqttHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);
        mPreferences = new OneControlPreferences(this);
        pDialog = new TransparentProgressDialog(WifiConfigActivity.this, R.drawable.progress);
        isV3 = !(Utils.MAC_ID.contains("20f85eee") || Utils.MAC_ID.contains("20f85ef2"));
        initializeViews();
        setToolBar();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mqttListener = this;
        helper = new MqttHelper(WifiConfigActivity.this, mqttListener);

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (isPermissionRequired()) {
            wifiScanStart();
        }
    }

    private void setToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Configure Wi-Fi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        etSSID = (EditText) findViewById(R.id.etSSID);
        etPwd = (EditText) findViewById(R.id.etPwd);
        tvSkip = (TextView) findViewById(R.id.tvSkip);

        if (getIntent().getBooleanExtra("IS_REGISTER", false)) {
            tvSkip.setVisibility(View.VISIBLE);
        } else {
            tvSkip.setVisibility(View.GONE);
        }

        btnWifiSearch = (Button) findViewById(R.id.btnWifiSearch);
        btnPairing = (Button) findViewById(R.id.btnPairing);
        btnRouterSearch = (Button) findViewById(R.id.btnRouterSearch);
        btnPwdView = (Button) findViewById(R.id.btnPwdView);

        btnPairing.setOnClickListener(this);
        if (!isV3) {
            btnWifiSearch.setOnClickListener(this);
        }
//        etSSID.setOnClickListener(this);
        tvSkip.setOnClickListener(this);
        btnRouterSearch.setOnClickListener(this);
        btnPwdView.setOnClickListener(this);
    }

    private void wifiScanStart() {
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

    private boolean isPermissionRequired() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                wifiScanStart();
            } else {
                isPermissionRequired();
            }
        }
    }

    @Override
    protected void onPause() {
        if (isPermissionRequired()) {
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
        menu.add(0, 0, 0, "Refresh");
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
                strSSID = etSSID.getText().toString().trim();
                strPwd = etPwd.getText().toString().trim();
                if (strSSID.length() == 0) {
                    Toast.makeText(this, "Enter SSID Name", Toast.LENGTH_LONG).show();
                } else if (strPwd.length() == 0) {
                    Toast.makeText(this, "Enter password", Toast.LENGTH_LONG).show();
                } else {
                    if (Utils.isNetworkAvailable) {
                        //Newly added code..!
                        if (isV3) {
                            helper.sendMsg("K|0|" + strSSID + "|" + strPwd + "|" + 1);
                        } else {
                            //Service call to configure wifi
                            new WifiConfigTask().execute();
                        }
                    } else {
                        Utils.showMessageDialog("No internet access", this);
                    }
                }
                break;
//            case R.id.etSSID:
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.hideSoftInputFromWindow(etSSID.getWindowToken(), 0);  // hide the soft keyboard
//                }ShowDialogForWifiNetworks(this);
//                break;
            case R.id.btnWifiSearch:
                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                startActivity(intent);*/
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                break;
            case R.id.tvSkip:
                Intent intent = new Intent(WifiConfigActivity.this, LandingPageActivity.class);//HomeActivity
                startActivity(intent);
                new OneControlPreferences(WifiConfigActivity.this).setPreference(true);
                finish();
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

        if (wifiArray != null && wifiArray.length > 0) {
            adapter = new ListViewAdapter(WifiConfigActivity.this, wifiArray);
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

    //================================================

    private void sendMessage(String msg) {
        pw.print(msg);
        msg = msg + "\n";
        pw.flush();
    }

    public void showMessageDialog(String message, Context context) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                new OneControlPreferences(WifiConfigActivity.this).setPreference(true);
                handleRouterCheck();
            }
        });
        alertBox.show();
    }

    private void connect() {
        try {
            sock = new Socket(ip, port);

            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // Start the thread
            WinInputThread wit = new WinInputThread(sock, br);
            wit.start();
            isSocketConnected = true;
        } catch (IOException e) {
            isSocketConnected = false;
            e.printStackTrace();
        }
    }

    private void turnGPSOn() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);

        int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
        Log.e("location mode ", locationMode + "");
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

    // Better way to check location service status
    protected boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            // check location state for api version 19 or greater
            int locationMode = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.LOCATION_MODE,
                    0
            );

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {

            String locationProviders = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            );

            return !TextUtils.isEmpty(locationProviders);
        }
    }


    // Broadcast receiver class called its receive method
    // when number of wifi connections changed

    private void handleRouterCheck() {
        pDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(WifiConfigActivity.this, MainActivity.class);//HomeActivity
                startActivity(intent);
                pDialog.dismiss();
                finish();*/
                showSuccessAlert();
            }
        }, 20000);
    }

    void showSuccessAlert() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(WifiConfigActivity.this);
        LayoutInflater inflater = WifiConfigActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.success_alart_dialog, null);
        dialogBuilder.setView(dialogView);

        Button close = (Button) dialogView.findViewById(R.id.close_dialog_btn);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WifiConfigActivity.this, LandingPageActivity.class);//HomeActivity
                startActivity(intent);
                pDialog.dismiss();
                finish();
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public void onMqttResponse(String res) {
        if (res.contains("Notification:")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "51":
                    showMsgDialog("Successfully Configured");
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

    public void showMsgDialog(final String message) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(WifiConfigActivity.this);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                startActivity(new Intent(WifiConfigActivity.this, LandingPageActivity.class));
            }
        });
        alertBox.show();
    }

    public static class ViewHolder {

        RadioButton rbSSIDName;
        RadioGroup radioGroup;
        TextView tvSSIDName;
    }

    private class WifiConfigTask extends AsyncTask<Void, Void, Void> {

        TransparentProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new TransparentProgressDialog(WifiConfigActivity.this, R.drawable.progress);
        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pd.isShowing()) pd.dismiss();
            try {
                mConnectAndPoll.start();
            } catch (Exception e) {
                Toast.makeText(WifiConfigActivity.this, "Please choose gateway first", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        }
    }

    // Create a subclass of Runnable that will update textResponse.
    private class UpdateResponseRunnable implements Runnable {
        private final String mValue;

        UpdateResponseRunnable(String value) {
            mValue = value;
        }

        @Override
        public void run() {
            Log.v("Value in UI", mValue + "");
            if (mValue.contains("Done")) {
                showMessageDialog("Configured successfully.", WifiConfigActivity.this);
                mPreferences.storeSSID(strSSID);
            } else {
                Utils.showMessageDialog("Not configured", WifiConfigActivity.this);
            }
        }
    }

    private class WinInputThread extends Thread {
        private Socket sock = null;
        private BufferedReader br = null;

        WinInputThread(Socket sock, BufferedReader br) {
            this.sock = sock;
            this.br = br;
        }

        public void run() {
            try {

                char[] buf = new char[4096];
                int read = 0;
                StringBuilder sb = new StringBuilder();

                // wait the receiving packet until socket close
                while ((read = br.read(buf)) != 0) {
                    if (read != -1) {
                        System.out.println("insert to try \n");

                        sb.append(new String(buf, 0, read));
                        sb.append("\n");
                        Log.e("Final result is : ", sb.toString());
                        UI_HANDLER.post(new UpdateResponseRunnable(sb.toString()));

                    } else {
                        break;
                    }
                }
                sock.close();

            } catch (Exception e) {

                try {
                    // if a problem occurs in socket, It'll close the socket.
                    e.printStackTrace();
                    System.out.println("insert to catch \n");
                    sock.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }


            } finally {
                try {
                    if (br != null)
                        br.close();

                } catch (Exception ex) {
                    Log.e(TAG, "Exception:-:" + ex.getMessage());
                }

            }

        }
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\n        Number Of Wifi connections : ").append(wifiList.size()).append("\n\n");

            wifiArray = new String[wifiList.size()];
            for (int i = 0; i < wifiList.size(); i++) {

                sb.append(Integer.valueOf(i + 1).toString()).append(". ");
                sb.append((wifiList.get(i)).toString());
                sb.append("\n\n");
                Log.v(TAG, "ssid : " + wifiList.get(i).SSID);
                Log.v(TAG, "capabilities : " + wifiList.get(i).capabilities);
                Log.v(TAG, "bssid : " + wifiList.get(i).BSSID);

                wifiArray[i] = wifiList.get(i).SSID;

            }
        }

    }

    private class ListViewAdapter extends BaseAdapter {

        Context context;
        String[] data;
        LayoutInflater inflater = null;

        ListViewAdapter(Context context, String[] data) {
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
            ViewHolder holder;
            if (view == null) {

                /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
                vi = inflater.inflate(R.layout.wifi_list_row, null);

                /****** View Holder Object to contain tabitem.xml file elements ******/

                holder = new ViewHolder();
                holder.rbSSIDName = (RadioButton) vi.findViewById(R.id.rbSSIDName);
                holder.radioGroup = (RadioGroup) vi.findViewById(R.id.radioGroup);
                holder.tvSSIDName = (TextView) vi.findViewById(R.id.tvSSIDName);

                /************  Set holder with LayoutInflater ************/
                vi.setTag(holder);
            } else
                holder = (ViewHolder) vi.getTag();

            if (data.length != 0) {
                //((RadioButton) holder.radioGroup.getChildAt(i)).setText(String.valueOf(data[i]));
                //holder.rbSSIDName.setText(String.valueOf(data[i])+"");
                holder.tvSSIDName.setText(data[i] + "");
            }
            return vi;
        }
    }
}
