package com.atg.onecontrolv3.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.adapters.SubUserRoomsAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.RoomsModel;
import com.atg.onecontrolv3.models.RoomsProvider;
import com.atg.onecontrolv3.models.SubUserRoomsModel;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubuserRoomsActivity extends BaseActivity implements OnItemClickListener, View.OnClickListener {

    public static Set<String> childRooms;
    RecyclerView recyclerView;
    SubUserRoomsAdapter roomsAdapter;
    Toolbar toolBar;
    TextView title;
    Button btnAddRooms;
    OneControlPreferences ocPreferences;
    String userId = "";
    private OnItemClickListener mListener;
    private ArrayList<RoomsModel> data = new ArrayList<RoomsModel>();
    private ArrayList<SubUserRoomsModel> getSubUserRoomsData = new ArrayList<>();
    private TransparentProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subuser_rooms);
        mListener = this;
        ocPreferences = new OneControlPreferences(this);
        pd = new TransparentProgressDialog(this, R.drawable.progress);//spinner
        userId = getIntent().getStringExtra("USER_ID");
        initializingViews();
        //title.setText(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        setToolBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable) {
            new GetRoomsInfoTask().execute();
        } else {
            Utils.showMessageDialog("No Internet.", this);
        }
    }

    private void initializingViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        btnAddRooms = (Button) findViewById(R.id.btnAddRooms);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        title = (TextView) findViewById(R.id.title);

        btnAddRooms.setOnClickListener(this);
    }


    private void setToolBar() {
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Assign Rooms");
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
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.btnToggle:

                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddRooms:
                ocPreferences.setChildRooms(childRooms);
                childRooms = ocPreferences.getChildRooms();
                String[] rooms = new String[childRooms.size()];
                childRooms.toArray(rooms);
                String value = "";

                if (rooms.length >0) {
                    for (int i = 0; i < rooms.length; i++) {
                        if (i == 0) {
                            value = rooms[i];
                        } else {
                            value += "," + rooms[i];
                        }
                    }
                    Log.v("Child rooms ", "are : " + value);

                    //Calling to add rooms to sub users web method
                    if (Utils.isNetworkAvailable) {
                        setSubUserRoomsRequest(value);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }
                } else {
                    Utils.showMessageDialog("Please select rooms to add", SubuserRoomsActivity.this);
                }
                break;
        }
    }

    private void creatingRoomsAndSetData(List<RoomsModel> listData) {

        for(int i=0;i<getSubUserRoomsData.size();i++){
            for (int j=0;j<listData.size();j++){

                if(getSubUserRoomsData.get(i).getRoomName().equals(listData.get(j).getRoomName())){

                    listData.get(j).setEnabled(true);
                }
            }
        }

        roomsAdapter = new SubUserRoomsAdapter(SubuserRoomsActivity.this, listData, getSubUserRoomsData, mListener);
        recyclerView.setAdapter(roomsAdapter);
    }

    private void setSubUserRoomsRequest(String rooms) {
        Utils.showProgressDialog(SubuserRoomsActivity.this);
        String url = ServiceHandler.baseUrl + "SetSubUserRooms?UserId=" + userId + "&UID=" +
                Utils.MAC_ID + "&RoomNumbers=" + rooms;
        StringRequest strRequest = new StringRequest(Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Utils.showMessageDialog("Rooms Added Successfully", SubuserRoomsActivity.this);
                Utils.hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    //Getting Rooms info from server AsyncTask
    private class GetRoomsInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            RoomsProvider provider = new RoomsProvider(SubuserRoomsActivity.this);
            data = provider.getRoomsFromServer();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new GetSubUserRooms().execute();

        }
    }

    private class GetSubUserRooms extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            RoomsProvider provider = new RoomsProvider(SubuserRoomsActivity.this);
            getSubUserRoomsData = provider.getSubUserRomms(userId);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();

            if (/*getSubUserRoomsData != null && getSubUserRoomsData.size() != 0*/data != null && data.size() != 0) {
                btnAddRooms.setVisibility(View.VISIBLE);
                creatingRoomsAndSetData(data);
            } else {
                btnAddRooms.setVisibility(View.GONE);
            }
        }
    }
}
