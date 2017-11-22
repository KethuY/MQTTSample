package com.atg.onecontrolv3.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.SubusersAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.SubUsersModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddSubUserActivity extends BaseActivity implements View.OnClickListener, OnItemClickListener {

    Toolbar toolbar;
    FloatingActionButton floatingBtnAddSubUser;
    RecyclerView userRecyclerView;
    ArrayList<SubUsersModel> data = new ArrayList<>();
    SubusersAdapter adapter;
    private String TAG = AddSubUserActivity.class.getSimpleName();
    private OnItemClickListener listener;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sub_user);
        listener = this;
        setToolBar();

        initializeViews();
        //title.setText(MyPreferences.getString(MyPreferences.PrefType.MacName,getApplicationContext()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSubUsersFromServer();
    }

    private void initializeViews() {
        floatingBtnAddSubUser = (FloatingActionButton) findViewById(R.id.floatingBtnAddSubUser);
        userRecyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);
        LinearLayoutManager llMgr = new LinearLayoutManager(this);
        llMgr.setOrientation(LinearLayoutManager.VERTICAL);
        userRecyclerView.setLayoutManager(llMgr);
        title = (TextView) findViewById(R.id.title);

        floatingBtnAddSubUser.setOnClickListener(this);
       /* MultiTouchListener touchListener=new MultiTouchListener(this);
        floatingBtnAddSubUser.setOnTouchListener(touchListener);*/
    }

    private void setToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sub-Users");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floatingBtnAddSubUser:
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                showAddUserDialog();
                break;
        }
    }

    private void showAddUserDialog() {
        final String isSuperUser = "";
        final Dialog userDialog = new Dialog(this);
        userDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        userDialog.setContentView(R.layout.dialog_add_sub_user);
        userDialog.setCanceledOnTouchOutside(false);

        final EditText etSubUserMobNo = (EditText) userDialog.findViewById(R.id.etSubUserMobNo);
        final EditText etSubUserIMEI = (EditText) userDialog.findViewById(R.id.etSubUserIMEI);
        Button btnAddSubUser = (Button) userDialog.findViewById(R.id.btnAddSubUser);
        final ToggleButton btnToggle = (ToggleButton) userDialog.findViewById(R.id.btnToggle);
        btnToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    btnToggle.setChecked(true);
                } else {
                    btnToggle.setChecked(false);
                }
            }
        });
        btnAddSubUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subMobile = etSubUserMobNo.getText().toString().trim();
                subMobile = subMobile.contains(" ") ? subMobile.replace(" ", "~") : subMobile;
                String subIMEI = etSubUserIMEI.getText().toString().trim();
                if (subMobile.length() == 0) {
                    etSubUserMobNo.setError("Enter sub user name");
                } else if (subIMEI.length() != 15) {
                    etSubUserIMEI.setError("Enter 15 digits IMEI");
                } else {
                    if (Utils.isNetworkAvailable) {
                        addSubuserRequest("Add", subMobile, subIMEI, "", btnToggle.isChecked() ? "true" : "false");
                    } else {
                        Utils.showMessageDialog("No Internet.", AddSubUserActivity.this);
                    }
                    userDialog.dismiss();
                }
            }
        });

        userDialog.show();
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
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.tvSubUserDelete:
                showMessageDialog("Do you want to Delete this user?", AddSubUserActivity.this, position);
                break;
            case R.id.cardView:
                //Calling sub user rooms activity
                if (data.get(position).getLov() != 2) {
                    Intent intent = new Intent(AddSubUserActivity.this, SubuserRoomsActivity.class);
                        intent.putExtra("USER_ID", data.get(position).getUserId());
                    startActivity(intent);
                }
                break;
        }
    }

    //Delete sub user dialog
    public void showMessageDialog(String message, Context context, final int position) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setTitle("OneControl");
        alertBox.setMessage(message);
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    addSubuserRequest("Delete", "", "", data.get(position).getUserId(), "");
                } else {
                    Utils.showMessageDialog("No Internet.", AddSubUserActivity.this);
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

    private void addSubuserRequest(final String mode, String mobile, String imei, String userId, String superUser) {
        Utils.showProgressDialog(AddSubUserActivity.this);
        String url = "";
        if (mode.equalsIgnoreCase("Add")) {
            url = ServiceHandler.baseUrl + "AddSubUser?MIMEI=" + Utils.IMEI + "&UID=" + Utils.MAC_ID +
                    "&IMEI=" + imei + "&MobileNumber=" + mobile + "&IsSuperUser=" + superUser;
        } else {
            url = ServiceHandler.baseUrl + "DeleteSubUser?UserId=" + userId + "&UID=" + Utils.MAC_ID + "&MIMEI=" + Utils.IMEI;
        }

        Log.v(TAG, "add sub user req : " + url);
        StringRequest strRequest = new StringRequest(Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    if (mode.equalsIgnoreCase("Add")) {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("RetVal:AddSubUser");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            Utils.showMessageDialog(object.getString("Description"), AddSubUserActivity.this);
                        }
                    } else {
                        Utils.showMessageDialog("User Deleted successfully", AddSubUserActivity.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Utils.hideProgressDialog();
                onResume();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "addSubuserRequest() response error : " + error.getMessage());
                if (mode.equalsIgnoreCase("Delete"))
                    Utils.showMessageDialog("Error deleting user occured, Please try again", AddSubUserActivity.this);
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    //Volley Request
    private void getSubUsersFromServer() {
        Utils.showProgressDialog(AddSubUserActivity.this);

        String url = ServiceHandler.baseUrl + "GetSubUsers?UID=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        StringRequest strRequest = new StringRequest(Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                data = parseJSON(response);
                setDataBinding(data);
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

    private ArrayList<SubUsersModel> parseJSON(String json) {
        ArrayList<SubUsersModel> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("SubUserRooms");
            SubUsersModel model;
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                model = new SubUsersModel();

                String mobile = object.getString("Mobile");
                String uid = object.getString("UID");
                String userid = object.getString("UserId");
                int lov = object.getInt("LOV");

                model.setMobile(mobile);
                model.setUid(uid);
                model.setUserId(userid);
                model.setLov(lov);

                list.add(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "parseJSON error : " + e.getMessage());
        }
        return list;
    }

    private void setDataBinding(ArrayList<SubUsersModel> model) {
        if (model != null && model.size() >= 0) {
            adapter = new SubusersAdapter(AddSubUserActivity.this, listener, model);
            userRecyclerView.setAdapter(adapter);
        }
    }
}
