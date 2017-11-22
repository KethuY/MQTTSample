package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.EditDevicesAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.RoomsModel;
import com.atg.onecontrolv3.models.RoomsProvider;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.atg.onecontrolv3.helpers.Utils.getEmojiByUnicode;


public class EditDeviceActivity extends BaseActivity implements OnItemClickListener, MqttHelper.responseListener {

    private static final String TAG = EditDeviceActivity.class.getSimpleName();
    Toolbar toolBar;
    RecyclerView device_recyclerView;
    EditDevicesAdapter adapter;
    //TransparentProgressDialog pd;
    TextView title;
    //String macNameStr;
    MqttHelper.responseListener mqttListener = null;
    private List<RoomsModel> data = new ArrayList<>();
    private OnItemClickListener mListener;
    private MqttHelper helper;
    private String roomIdExt;
    private boolean isDeleteClicked;
    private int testCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_device);
            mqttListener = this;
            helper = new MqttHelper(EditDeviceActivity.this, mqttListener);
            //macNameStr = MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext());
            initializeView();
            setToolBar();
            //title.setText(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
            //pd = new TransparentProgressDialog(EditDeviceActivity.this, R.drawable.progress);
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (Utils.isNetworkAvailable) {
                new GetRoomsInfoTask().execute();
            } else {
                Utils.showMessageDialog("No Internet.", this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setToolBar() {
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Paired Devices");
        //getSupportActionBar().setSubtitle(macNameStr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeView() {
        title = (TextView) findViewById(R.id.title);
        mListener = this;
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        device_recyclerView = (RecyclerView) findViewById(R.id.device_recycler_view);

        LinearLayoutManager llMgr = new LinearLayoutManager(this);
        llMgr.setOrientation(LinearLayoutManager.VERTICAL);
        device_recyclerView.setLayoutManager(llMgr);
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_go_home, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            /*case R.id.action_home:
                onBackPressed();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        /*if(view.getId() == R.id.llRooms){
            Intent intent = new Intent(this, AddDeviceActivity.class);
            intent.putExtra("ROOM_NAME", data.get(position).getRoomName());
            intent.putExtra("ROOM_ID", data.get(position).getRoomId());
            intent.putExtra("NUM_OF_RELAYS", data.get(position).getNumOfRelays());
            intent.putExtra("BTN_NAME","Edit Name");
            //intent.putExtra("RROM_EDIT", true);
            startActivity(intent);
        }*/

        switch (view.getId()) {
            case R.id.llRooms:
            case R.id.tvEditRoom:
                Intent intent = new Intent(this, AddDeviceActivityMqtt.class);
                intent.putExtra("ROOM_NAME", data.get(position).getRoomName());
                intent.putExtra("ROOM_ID", data.get(position).getRoomId());
                intent.putExtra("NUM_OF_RELAYS", data.get(position).getNumOfRelays());
                intent.putExtra("BTN_NAME", "Edit Name");
                //intent.putExtra("RROM_EDIT", true);
                startActivity(intent);
                break;

            case R.id.tvDeleteRoom:
                roomIdExt = data.get(position).getRoomId();
                showMessageDialog("Are you sure you want to delete this Room?");
                break;
        }
    }

    public void showMessageDialog(String message) {
        try {
            final AlertDialog.Builder alertBox = new AlertDialog.Builder(EditDeviceActivity.this);
            alertBox.setTitle("OneControl");
            alertBox.setMessage(message);
            alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Service call to unpair receiver
                    if (Utils.isNetworkAvailable) {
                        if (data.size() != 0) {
                            isDeleteClicked = true;
                            helper.sendMsg("Q|" + roomIdExt);
                            //new SetUnPairReceiverNumberTask().execute(roomIdExt/*, position + ""*/);
                        } /*else if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }*/
                    } else {
                        Utils.showMessageDialog("No Internet.", EditDeviceActivity.this);
                    }

                }
            });

            alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertBox.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void setDataToAdapter(List<RoomsModel> data) {
        try {
            if (data != null && data.size() > 0) {
                adapter = new EditDevicesAdapter(this, data, mListener);
                device_recyclerView.setAdapter(adapter);
            } else {
                device_recyclerView.setAdapter(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    /*private void handleNotifications(final int position) {
        *//*pd = new TransparentProgressDialog(EditDeviceActivity.this, R.drawable.progress);
        pd.show();*//*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //pd.dismiss();
                new NotificationsTask().execute(position + "");
            }
        }, 5000);
    }*/

    @Override
    public void onMqttResponse(String stringBody) {
        try {
           /* if (null != pd && pd.isShowing()) {
                pd.dismiss();
            }*/
            if (stringBody.contains("Notification:")) {
                Log.e(TAG, "strBodyN:-:" + stringBody);
                String notiStrArr[] = stringBody.split(Pattern.quote(":"));
                switch (notiStrArr[1]) {
                    case "6":
                        Log.e(TAG, "testCount:-:" + testCount);
                        //showConfirmationDialog2("Room deleted successfully", EditDeviceActivity.this);
                        if (Utils.isNetworkAvailable) {
                            if (isDeleteClicked) {
                                isDeleteClicked = false;
                                UnPairReceiverExt();
                            }
                        } else {
                            Utils.showMessageDialog("No Internet", EditDeviceActivity.this);
                        }
                        showConfirmationDialog2("Done " + getEmojiByUnicode(0x1F44D), EditDeviceActivity.this);
                        break;
                    case "7":
                        //showConfirmationDialog2("Room deletion failed", EditDeviceActivity.this);
                        Utils.showMessageDialog("Failed", EditDeviceActivity.this);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMqttFailure() {
        try {
            /*if (null != pd && pd.isShowing()) {
                pd.dismiss();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeliveryComplete() {
        try {
            /*if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showConfirmationDialog2(String message, Context context) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /*if (Utils.isNetworkAvailable(EditDeviceActivity.this)) {
                    UnPairReceiverExt();
                } else {
                    Utils.showMessageDialog("No Internet", EditDeviceActivity.this);
                }*/
            }
        });

       /* alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });*/
        alertBox.show();
    }

    void UnPairReceiverExt() {
        try {
            Log.e(TAG, "roomIdExt:-:" + roomIdExt);
            if (roomIdExt != null && !roomIdExt.isEmpty()) {
                String url = ServiceHandler.baseUrl + "UnPairReceiverExt?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&Address=" + roomIdExt;
                Log.e(TAG, "UnPairReceiverExt:-:" + url);
                StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    int code = 0;
                    String message = "";

                    @Override
                    public void onResponse(String response) {
                        /*if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }*/
                        Log.e(TAG, "response2:-:" + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("UnPairReceiverExtResult");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                message = jsonObject1.getString("Message");
                                code = jsonObject1.getInt("Code");
                            }
                            if (code != 200) {
                                //UnPairReceiverExt();
                            } else {
                                if (Utils.isNetworkAvailable) {
                                    new GetRoomsInfoTask().execute();
                                } else {
                                    /*if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }*/
                                    Utils.showMessageDialog("No Internet.", EditDeviceActivity.this);
                                }
                            }
                        } catch (JSONException e) {
                            /*if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }*/
                            Log.e(TAG, "JSONException:-:" + e.getMessage());
                        }

                        // new GetRoomsInfoTask().execute();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        /*if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }*/
                        Log.e(TAG, "onErrorResponse2:-:" + error.getMessage());
                    }
                });

                AppController.getInstance().addToRequestQueue(strRequest);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }*/
    }

    //Setting pair device name task in server
    private class SetUnPairReceiverNumberTask extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;
        String status;
        //int position;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress = ProgressDialog.show(RemoveRoomsActivity.this, null, "Loading...");
            /*try {
                pd = new TransparentProgressDialog(EditDeviceActivity.this, R.drawable.progress);
                pd.show();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }

        @Override
        protected String doInBackground(String... params) {
            //position = Integer.parseInt(params[1]);
            /*RoomsProvider provider = new RoomsProvider(EditDeviceActivity.this);
            status = provider.SetUnPairReceiverInServer(params[0]);*/
            //roomIdExt = Integer.parseInt(params[1]);
            Log.e(TAG, "Delete msg:-:" + "Q|" + params[0]);
            // helper.sendMsg("Q|" + params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                /*if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            //progress.dismiss();
            //pd.dismiss();
            /*if (!status.equals("")) {
                //Todo
//                Utils.showMessageDialog(status, EditDeviceActivity.this);
                handleNotifications(position);
            }*/
        }
    }

    //Getting Rooms info from server AsyncTask
    private class GetRoomsInfoTask extends AsyncTask<Void, Void, String> {
        //private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress = ProgressDialog.show(EditDeviceActivity.this, null, "Loading...");
            try {
               /* pd = new TransparentProgressDialog(EditDeviceActivity.this, R.drawable.progress);
                if (pd != null && !pd.isShowing()) {
                    pd.show();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            RoomsProvider provider = new RoomsProvider(EditDeviceActivity.this);
            data = provider.getRoomsFromServer();

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progress.dismiss();
            try {
                /*if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }*/
                setDataToAdapter(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*else{
                Utils.showMessageDialog("No Internet.", EditDeviceActivity.this);
            }*/
        }
    }

    /*private class NotificationsTask extends AsyncTask<String, Void, String> {

        String errorDesc;
        int pos;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                *//*pd = new TransparentProgressDialog(EditDeviceActivity.this, R.drawable.progress);
                pd.show();*//*
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            pos = Integer.parseInt(params[0]);
            NotificationProvider provider = new NotificationProvider(EditDeviceActivity.this);
            errorDesc = provider.GetMobileNotifications();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progress.dismiss();
            try {
                *//*if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }*//*
                if (!errorDesc.equals("")) {
                    if (errorDesc.equals("UnPair Failed")) {
                        Utils.showMessageDialog(errorDesc, EditDeviceActivity.this);
                    } else if (errorDesc.equalsIgnoreCase("UnPair Success")) {
                        Utils.showMessageDialog(errorDesc, EditDeviceActivity.this);
                        adapter.remove(pos);
                        adapter.notifyDataSetChanged();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
}
