package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.PIRDashboardAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.PIRDBModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.atg.onecontrolv3.helpers.Utils.getEmojiByUnicode;
import static com.atg.onecontrolv3.helpers.Utils.showMessageDialog;

public class PIRDashboardActivity extends BaseActivity implements OnItemClickListener, MqttHelper.responseListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = PIRDashboardActivity.class.getSimpleName();
    ArrayList<PIRDBModel> mPirdbModelArrLst;
    ListView mPirDBListView;
    TransparentProgressDialog pd;
    TextView mEditTv, mAddPIRTv;
    MqttHelper.responseListener mqttListener = null;
    private OnItemClickListener mListener;
    private MqttHelper helper;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mIntDeletePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pirdashboard);
        setToolBar();
        mListener = this;
        mPirDBListView = (ListView) findViewById(R.id.pir_db_lv);
        pd = new TransparentProgressDialog(this, R.drawable.progress);
        mEditTv = (TextView) findViewById(R.id.edit_tv);
        mAddPIRTv = (TextView) findViewById(R.id.add_pir_tv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mqttListener = this;
        if (Utils.isNetworkAvailable) {
            helper = new MqttHelper(this, mqttListener);
        }
    }

    private void setToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Set PIR");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            getPirsVolley();
        } else {
            showMessageDialog("No Internet.", this);
        }
    }

    private void getPirsVolley() {
        Utils.getPIRSObj = null;
        pd.show();
        String strMethodName = "GetPIRExt";
        String strURL = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "strURL:-:" + strURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                parseJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Log.e(TAG, "volleyError:-:" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void parseJson(String response) {
        try {
            mPirdbModelArrLst = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("GetPIRExtResult");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                PIRDBModel pirdbModel = new PIRDBModel();
                pirdbModel.setPirName(jsonObject1.getString("PIRName"));
                pirdbModel.setPirNumber(jsonObject1.getInt("PIRNumber"));
                pirdbModel.setTimerTime(jsonObject1.getInt("TimerTime"));
                pirdbModel.setValue(jsonObject1.getString("value"));

                //20006:1234;20007:1234

                ArrayList<String> roomIdArr = new ArrayList<>();
                ArrayList<String> relaysArr = new ArrayList<>();
                String valueStr = jsonObject1.getString("value");
                if (valueStr != null && !valueStr.isEmpty()) {
                    String arrS[] = valueStr.split(";");//[0]20006:1234[1]20007:1234
                    for (String roomRelayStr : arrS) {
                        String arrS2[] = roomRelayStr.split(":");//[0]20006[1]1234
                        roomIdArr.add(arrS2[0]);
                        relaysArr.add(arrS2[1]);
                    }
                }

                pirdbModel.setRoomId(roomIdArr);
                pirdbModel.setRelays(relaysArr);

                mPirdbModelArrLst.add(pirdbModel);
            }

            if (mPirdbModelArrLst != null && mPirdbModelArrLst.size() > 0) {
                mEditTv.setVisibility(View.VISIBLE);
                setAdapter();
            } else {
                mEditTv.setVisibility(View.INVISIBLE);
                mPirDBListView.setAdapter(null);
                pd.dismiss();
            }
        } catch (Exception e) {
            pd.dismiss();
            Log.e(TAG, "JSONException:-" + e.getMessage());
        }
    }

    private void setAdapter() {
        PIRDashboardAdapter adapter = new PIRDashboardAdapter(this, mPirdbModelArrLst, mEditTv, mListener);
        mPirDBListView.setAdapter(adapter);
        pd.dismiss();
    }

    public void onPIRDbClick(View view) {
        switch (view.getId()) {
            case R.id.add_pir_tv:
                if (mPirdbModelArrLst != null && mPirdbModelArrLst.size() > 0) {
                    ArrayList<Integer> pirNumbersArrLst = new ArrayList<>();
                    if (mPirdbModelArrLst.size() > 0) {
                        for (int i = 0; i < mPirdbModelArrLst.size(); i++) {
                            pirNumbersArrLst.add(mPirdbModelArrLst.get(i).getPirNumber());
                        }
                        Log.e(TAG, "pirNumbersArrLst:-:" + pirNumbersArrLst);
                    }
                    if (mPirdbModelArrLst.size() < 8) {
                        startActivity(new Intent(this, EditPIRActivity.class).putExtra("PIRNbrArrLst", pirNumbersArrLst));
                    } else {
                        showMessageDialog("Max PIRs Limit is 8", this);
                    }
                } else {
                    startActivity(new Intent(this, EditPIRActivity.class).putExtra("PIRNbrArrLst", ""));
                }
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.delete_tv:
                if (Utils.isNetworkAvailable) {
                    mIntDeletePosition = position;
                    showConfirmationDialogDelete();
                }
                break;
        }
    }

    @Override
    public void onMqttResponse(String res) {
        Log.e(TAG, "mqttResStr:-:" + res);
        if (res.contains("Notification")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "49":
                    showSuccessFailDialog();
                    break;
            }
        }
    }


    void showSuccessFailDialog() {

        final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
        alertBox.setMessage("Done " + getEmojiByUnicode(0x1F60A));
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    deletePirExtVolley();
                } else {
                    showMessageDialog("No Internet", PIRDashboardActivity.this);
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

    private void showConfirmationDialogDelete() {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
        alertBox.setMessage("Do you want to delete?");
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    try {
                        if (Utils.isNetworkAvailable) {
                            helper.sendMsg("p|" + mPirdbModelArrLst.get(mIntDeletePosition).getPirNumber());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception:-:" + e.getMessage());
                    }
                } else {
                    showMessageDialog("No Internet", PIRDashboardActivity.this);
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

    private void deletePirExtVolley() {
        //void DeletePIRExt(string MacId, string IMEI, int PIRNumber);
        String strMethodName = "DeletePIRExt";
        String strUrl = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI="
                + Utils.IMEI + "&PIRNumber=" + mPirdbModelArrLst.get(mIntDeletePosition).getPirNumber();
        Log.e(TAG, "DeletePIRExtStrUrl:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "deletePirResStr:-:" + response);
                if (Utils.isNetworkAvailable) {
                    getPirsVolley();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "DeletePIRVolleyError:-:" + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {

    }

    @Override
    public void onRefresh() {
        if (Utils.isNetworkAvailable) {
            getPirsVolley();
        } else {
            showMessageDialog("No Internet.", this);
        }
    }
}
