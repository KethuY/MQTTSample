package com.atg.onecontrolv3.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.activities.EditTimerActivity;
import com.atg.onecontrolv3.adapters.SetTimerAdapter;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.GetTimersModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SetTimerFragment extends Fragment implements SetTimerAdapter.SendDataToServerListener, OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "SetTimerFragment";
    TextView floatingBtnAddTimer, mEditTv;
    ListView mTimerDbLv;
    TransparentProgressDialog pd;
    ArrayList<GetTimersModel> getTimersModelArrLst;
    SetTimerAdapter.SendDataToServerListener mListener;
    private OnItemClickListener mListener1;
    private SetTimerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isViewShown = false;
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_set_timer, container, false);
        if (!isViewShown) {

            mListener = this;
            initializeViews(view);
            Utils.getTimerModelObj = null;
            mListener1 = this;
            swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(this);
            floatingBtnAddTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.getTimerModelObj = null;
                    Intent intent = new Intent(mActivity, EditTimerActivity.class);
                    intent.putExtra("IsFrom", "1");
                    startActivity(intent);
                }
            });

            //-----------------------------------
            Utils.getTimerModelObj = null;
            if (Utils.isNetworkAvailable) {
                getTimerDashboard(0);
            } else {
                Utils.showMessageDialog("No Internet.", mActivity);
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            isViewShown = true;
            Utils.getTimerModelObj = null;
            if (Utils.isNetworkAvailable) {
                getTimerDashboard(0);
            } else {
                Utils.showMessageDialog("No Internet.", mActivity);
            }
        } else {
            isViewShown = false;
        }
    }
    /*@Override
    public void onResume() {
        super.onResume();
        Utils.getTimerModelObj = null;
        if (Utils.isNetworkAvailable) {
            getTimerDashboard(0);
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }
    }*/

    public void getTimerDashboard(final int i) {
        pd.show();
        String finalUrl = ServiceHandler.baseUrl + "GetTimers?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI;
        Log.e(TAG, "finalUrl:-:" + finalUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                parseJson(response, i);
                pd.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("makeIRClientRequest()", "Error : " + error.getMessage());
                pd.dismiss();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void parseJson(String response, int j) {
        try {
            getTimersModelArrLst = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("GetTimersResult");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                GetTimersModel model = new GetTimersModel();
                model.setServerName(jsonObject1.getString("Name"));
                String name = jsonObject1.getString("Name");
                name = name.replace("~", " ");
                name = name.replace("~", " ");
                model.setName(name);
                model.setUid(jsonObject1.getString("UID"));
                String dateTime = jsonObject1.getString("ExecTimeDisplay");
                model.setServerTime(dateTime);
                //2017-04-18 02:18
                dateTime = new StringBuilder(dateTime).insert(4, "-").toString();
                dateTime = new StringBuilder(dateTime).insert(7, "-").toString();
                dateTime = new StringBuilder(dateTime).insert(10, " ").toString();
                dateTime = new StringBuilder(dateTime).insert(13, ":").toString();
                model.setExecTime(dateTime);
                model.setRelays(jsonObject1.getString("Relays"));
                model.setId(jsonObject1.getInt("Id"));
                model.setTimertype(jsonObject1.getInt("TimerType"));
                model.setAction(jsonObject1.getBoolean("Action"));
                model.setRepeat(jsonObject1.getBoolean("IsRepeat"));
                model.setSunday(jsonObject1.getBoolean("Sunday"));
                model.setMonday(jsonObject1.getBoolean("Monday"));
                model.setTuesday(jsonObject1.getBoolean("Tuesday"));
                model.setWednesday(jsonObject1.getBoolean("Wednesday"));
                model.setThursday(jsonObject1.getBoolean("Thursday"));
                model.setFriday(jsonObject1.getBoolean("Friday"));
                model.setSaturday(jsonObject1.getBoolean("Saturday"));
                model.setStatus(jsonObject1.getBoolean("Status"));
                /*---------Adding Days status to ArrayList-------*/
                ArrayList<Boolean> isDayArrLst = new ArrayList<>();
                isDayArrLst.add(jsonObject1.getBoolean("Sunday"));
                isDayArrLst.add(jsonObject1.getBoolean("Monday"));
                isDayArrLst.add(jsonObject1.getBoolean("Tuesday"));
                isDayArrLst.add(jsonObject1.getBoolean("Wednesday"));
                isDayArrLst.add(jsonObject1.getBoolean("Thursday"));
                isDayArrLst.add(jsonObject1.getBoolean("Friday"));
                isDayArrLst.add(jsonObject1.getBoolean("Saturday"));

                model.setIsDaysRepetArrLst(isDayArrLst);

                getTimersModelArrLst.add(model);
            }
            setDataToAdapter(j);
        } catch (JSONException e) {
            pd.dismiss();
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    private void setDataToAdapter(int j) {

        Log.e(TAG, "arrSize:-:" + getTimersModelArrLst.size());

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (null != mActivity && null != getTimersModelArrLst && getTimersModelArrLst.size() > 0) {
            mEditTv.setVisibility(View.VISIBLE);
            adapter = new SetTimerAdapter(mActivity, getTimersModelArrLst, mListener, mEditTv, mListener1, j);
            mTimerDbLv.setAdapter(adapter);
            pd.dismiss();
        } else {
            mEditTv.setVisibility(View.INVISIBLE);
            mTimerDbLv.setAdapter(null);
            pd.dismiss();
        }

       /* try {
            adapter = new SetTimerAdapter(mActivity, getTimersModelArrLst, mListener, mEditTv, mListener1);
            mTimerDbLv.setAdapter(adapter);
            pd.dismiss();
        } catch (Exception e) {
            pd.dismiss();
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }*/
    }

    private void initializeViews(View view) {
        pd = new TransparentProgressDialog(mActivity, R.drawable.progress);
        floatingBtnAddTimer = view.findViewById(R.id.floatingBtnAddTimer);
        mTimerDbLv = view.findViewById(R.id.timer_db_lv);
        mEditTv = view.findViewById(R.id.edit_tv);
    }

    @Override
    public void onSwitchChangeListener(String response, String s) {
        Log.e(TAG, "response:-:" + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("SetTimer");
            int code = 0;
            String Msg = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                code = jsonObject1.getInt("Code");
                Msg = jsonObject1.getString("Message");
            }

            showConfirmationDialogSwitch(Msg, code, s);
        } catch (JSONException e) {
            pd.dismiss();
            Log.e(TAG, "JSONException:-:" + e.getMessage());
        }
    }

    public void showConfirmationDialogSwitch(final String message, final int code, String s) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(mActivity);
        alertBox.setTitle("OneControl");
        if (code == 200) {
            if (s.equals("1")) {
                alertBox.setMessage("Timer Activated");
            } else {
                alertBox.setMessage("Timer Deactivated");
            }
        } else {
            alertBox.setMessage("Timer Activation failed, Please try again.");
        }
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code == 200) {
                    if (Utils.isNetworkAvailable) {
                        getTimerDashboard(0);
                    } else {
                        Utils.showMessageDialog("No Internet.", mActivity);
                    }
                }
            }
        });
        alertBox.show();
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
            case R.id.delete_tv:
                if (Utils.isNetworkAvailable) {
                    showConfirmationDialogDelete(position);
                } else {
                    Utils.showMessageDialog("No Internet", mActivity);
                }
                break;
        }
    }

    private void showConfirmationDialogDelete(final int position) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(mActivity);
        alertBox.setMessage("Do you want to delete?");
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    try {
                        deleteTimer(String.valueOf(getTimersModelArrLst.get(position).getId()));
                    } catch (Exception e) {
                        Log.e(TAG, "Exception:-:" + e.getMessage());
                    }
                } else {
                    Utils.showMessageDialog("No Internet", mActivity);
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

    private void deleteTimer(String timerId) {
        pd.show();
        //http://atghas.com/OneControlService/OCService.svc/DeleteTimer?MacId=20f85eeee93e&IMEI=352087074323842&TimerId=199.
        String url = Utils.baseUrl + "DeleteTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerId=" + timerId;
        Log.e(TAG, "SetReceiversStatusExt:-:" + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            int code = 0;
            String message = "";

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response1:-:" + response);
                //{"DeleteTimerResult":[{"Code":200,"Message":"Successfully deleted"}]}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("DeleteTimerResult");
                    int code = 0;
                    String Msg = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        code = jsonObject1.getInt("Code");
                        Msg = jsonObject1.getString("Message");
                    }

                    sendConfirmationDialogDelete(code);
                } catch (JSONException e) {
                    pd.dismiss();
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

    private void sendConfirmationDialogDelete(final int code) {
        final AlertDialog.Builder alertBox = new AlertDialog.Builder(mActivity);
        alertBox.setTitle("OneControl");
        pd.dismiss();
        if (code == 200) {
            alertBox.setMessage("Timer Deleted Successfully");
        } else {
            alertBox.setMessage("Timer Deletion failed, Please try again.");
        }
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    getTimersModelArrLst = new ArrayList<>();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    getTimerDashboard(1);
                } else {
                    Utils.showMessageDialog("No Internet.", mActivity);
                }
                dialogInterface.dismiss();
            }
        });
        alertBox.show();
    }

    @Override
    public void onRefresh() {
        if (Utils.isNetworkAvailable) {
            getTimerDashboard(0);
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }
    }
}
