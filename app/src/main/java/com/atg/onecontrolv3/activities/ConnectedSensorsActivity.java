package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.ConnectedSensorsAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.ConnectedSensorsModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ConnectedSensorsActivity extends BaseActivity implements OnItemClickListener, ConnectedSensorsAdapter.onUpdateSensorListener, MqttHelper.responseListener {

    private static final String TAG = ConnectedSensorsActivity.class.getSimpleName();
    RecyclerView mConnectedSensorsRv;
    MqttHelper.responseListener mqttListener = null;
    private ArrayList<ConnectedSensorsModel> mConnectedSensorsArrLst;
    private TextView mEditTv;
    private OnItemClickListener mListener1;
    private ConnectedSensorsAdapter.onUpdateSensorListener mUpdateListener;
    private ConnectedSensorsAdapter adapter;
    private MqttHelper helper;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_sensors);

        mConnectedSensorsRv = (RecyclerView) findViewById(R.id.connected_sensors_rv);
        mConnectedSensorsRv.setLayoutManager(new LinearLayoutManager(this));
        mEditTv = (TextView) findViewById(R.id.edit_tv);
        setToolBar();
        mListener1 = this;
        mUpdateListener = this;

        mqttListener = this;
        if (Utils.isNetworkAvailable) {
            helper = new MqttHelper(ConnectedSensorsActivity.this, mqttListener);
        }
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //((RelativeLayout) findViewById(R.id.rlHeaderLogo)).setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            getConnectedSensorsList(0);
        } else {
            Utils.showMessageDialog("No Internet.", this);
        }
    }

    private void getConnectedSensorsList(final int i) {
        String strMethodName = "GetSensorDetails";
        String strUrl = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "strUrl:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJson(response, i);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error:-:" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void parseJson(String response, int j) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("GetSensorDetailsResult");
            mConnectedSensorsArrLst = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                ConnectedSensorsModel model = new ConnectedSensorsModel();
                model.setId(jsonObject1.getInt("Id"));
                model.setIsWired(jsonObject1.getInt("IsWired"));
                String loc = jsonObject1.getString("Location");
                model.setLocation(loc.replaceAll("~", " "));
                model.setSensorNo(jsonObject1.getInt("SensorNo"));
                model.setSensorTypeId(jsonObject1.getInt("SensorTypeId"));
                String zone = jsonObject1.getString("Zone");
                model.setZone(zone.replaceAll("~", " "));

                switch (model.getSensorTypeId()) {
                    case 1:
                        model.setSensorType("Curtain");
                        break;
                    case 2:
                        model.setSensorType("Gas");
                        break;
                    case 3:
                        model.setSensorType("Glass Break");
                        break;
                    case 4:
                        model.setSensorType("Magnetic");
                        break;
                    case 5:
                        model.setSensorType("Panic");
                        break;
                    case 6:
                        model.setSensorType("PIR");
                        break;
                    case 7:
                        model.setSensorType("Smoke");
                        break;
                    case 8:
                        model.setSensorType("Vibration");
                        break;
                }

                mConnectedSensorsArrLst.add(model);
            }
            Log.e(TAG, "mConnectedSensorsArrLst:-:" + mConnectedSensorsArrLst.size());
            setAdapter(j);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    private void setAdapter(int j) {
        if (null != mConnectedSensorsArrLst && !mConnectedSensorsArrLst.isEmpty()) {
            mEditTv.setVisibility(View.VISIBLE);
            adapter = new ConnectedSensorsAdapter(this, mConnectedSensorsArrLst, mEditTv, mListener1, j, mUpdateListener);
            mConnectedSensorsRv.setAdapter(adapter);
        } else {
            mConnectedSensorsRv.setAdapter(null);
            mEditTv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.delete_tv:
                mPosition = position;
                if (Utils.isNetworkAvailable) {
                    showConfirmationDialogDelete();
                } else {
                    Utils.showMessageDialog("No Internet", this);
                }
                break;
        }
    }

    private void showConfirmationDialogDelete() {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
        alertBox.setMessage("Do you want to delete?");
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    try {
                        helper.sendMsg("c|0|" + mConnectedSensorsArrLst.get(mPosition).getSensorNo());
                    } catch (Exception e) {
                        Log.e(TAG, "Exception:-:" + e.getMessage());
                    }
                } else {
                    Utils.showMessageDialog("No Internet", ConnectedSensorsActivity.this);
                }
            }
        });
        alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertBox.show();

    }

    private void deleteSensor(int position) {
        int id = mConnectedSensorsArrLst.get(position).getId();
        String zone = mConnectedSensorsArrLst.get(position).getZone();
        int sensorNo = mConnectedSensorsArrLst.get(position).getSensorNo();
        int isWired = mConnectedSensorsArrLst.get(position).getIsWired();
        int sensorTypeId = mConnectedSensorsArrLst.get(position).getSensorTypeId();
        String strName = mConnectedSensorsArrLst.get(position).getLocation();
        strName = strName.replaceAll(" ", "~");
        zone = zone.replaceAll(" ", "~");
        String strUrl = ServiceHandler.baseUrl + "SetSensorCRUD?Id=" + id + "&SType=" + sensorTypeId + "&MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
                + "&Location=" + strName + "&Zone=" + zone + "&PortNumber=" + sensorNo + "&IsWired=" + isWired + "&TypeofCall=3";
        Log.e(TAG, "strUrl:-:" + strUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response1:-:" + response);
                //{"SetSensorCRUDResult":{"Code":200,"Message":"Successfully Updated"}}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("SetSensorCRUDResult");
                    int code = jsonObject1.getInt("Code");
                    if (code == 200) {
                        Utils.showMessageDialog("Deleted Successfully", ConnectedSensorsActivity.this);
                        if (Utils.isNetworkAvailable) {
                            mConnectedSensorsArrLst = new ArrayList<>();
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            getConnectedSensorsList(1);
                        } else {
                            Utils.showMessageDialog("No Internet.", ConnectedSensorsActivity.this);
                        }
                    } else {
                        Utils.showMessageDialog("Deletion failed", ConnectedSensorsActivity.this);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "JSONException:-:" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse1:-:" + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }


    @Override
    public void onUpdateChangeListener(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonObject1 = jsonObject.getJSONObject("SetSensorCRUDResult");
            int code = jsonObject1.getInt("Code");
            String message = jsonObject1.getString("Message");
            if (code == 200) {
                getConnectedSensorsList(0);
                Utils.showMessageDialog(message, this);
            } else {
                Utils.showMessageDialog("Update failed", this);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    @Override
    public void onMqttResponse(String res) {
        if (res.contains("Notification")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "35":
                    //Utils.showMessageDialog("Done", ConnectedSensorsActivity.this);
                    deleteSensor(mPosition);
                    break;
                case "36":
                    Utils.showMessageDialog("Deletion failed", ConnectedSensorsActivity.this);
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
}
