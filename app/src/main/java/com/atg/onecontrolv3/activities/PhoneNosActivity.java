package com.atg.onecontrolv3.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.PhoneNosAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.PhoneNumbersModel;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by user on 30-Mar-17
 */

public class PhoneNosActivity extends BaseActivity {

    private static final String TAG = PhoneNosActivity.class.getSimpleName();
    ArrayList<PhoneNumbersModel> phonenbrArrLst = new ArrayList<>();
    PhoneNosAdapter adapter;
    ListView mobileNoLv;
    Toolbar toolbar;
    //String macNameStr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phne_nos);

        mobileNoLv = (ListView) findViewById(R.id.phne_nos_lv);
       // macNameStr = MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext());
        setToolBar();
    }

    private void setToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Feed Phone No's Alert");
        //getSupportActionBar().setSubtitle(macNameStr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            getMobileNumbersRequest();
        } else {
            Utils.showMessageDialog("No Internet", PhoneNosActivity.this);
        }
    }

    private void getMobileNumbersRequest() {
        Utils.showProgressDialog(PhoneNosActivity.this);

        String url = ServiceHandler.baseUrl + "GetMacMobileNumbers?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e("Phone no", url + "");
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                phonenbrArrLst = parseMobileNumbers(response);
                if (phonenbrArrLst != null && phonenbrArrLst.size() > 0) {
                    setDataToAdapter(phonenbrArrLst);
                }
                Utils.hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "response error : " + error.getMessage());
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void setDataToAdapter(ArrayList<PhoneNumbersModel> model) {
        try {
            if (model != null && model.size() > 0) {

                for (int i = 0; i < phonenbrArrLst.size(); i++) {
                    Log.e(TAG, "OrderID:-:" + phonenbrArrLst.get(i).getMobileNumber());
                }

                adapter = new PhoneNosAdapter(PhoneNosActivity.this, phonenbrArrLst);
                adapter.notifyDataSetChanged();
                mobileNoLv.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    private ArrayList<PhoneNumbersModel> parseMobileNumbers(String json) {
        ArrayList<PhoneNumbersModel> orderIdArrLst = new ArrayList<>();
        try {

            for (int i = 0; i < 10; i++) {
                PhoneNumbersModel model1 = new PhoneNumbersModel();
                model1.setOrderId(i + 1);
                model1.setMobileNumber("");
                orderIdArrLst.add(model1);
            }

            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("Mobile Numbers");

            for (int i = 0; i < array.length(); i++) {

                JSONObject object = array.getJSONObject(i);
                String mobileNumber = object.getString("MobileNumber");
                int orderId = object.getInt("OrderId");

                for (int j = 0; j < orderIdArrLst.size(); j++) {

                    if (orderIdArrLst.get(j).getOrderId() == orderId) {
                        orderIdArrLst.get(j).setMobileNumber(mobileNumber);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderIdArrLst;
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
}
