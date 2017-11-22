package com.atg.onecontrolv3.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.ConnectedSensorsModel;

import java.util.ArrayList;

/**
 * Created by Bharath on 11-Aug-17
 */

public class ConnectedSensorsAdapter extends RecyclerView.Adapter<ConnectedSensorsAdapter.ViewHolder> {
    private static final String TAG = "ConnectedSensorsAdapter";
    private static Typeface tf1;
    private Context context;
    private ArrayList<ConnectedSensorsModel> mConnectedSensorsArrLst;
    private TextView mEditTv;
    private OnItemClickListener mListener1;
    private ArrayList<Boolean> isDeleteArrLst;
    private int j;
    private onUpdateSensorListener mUpdateListener;

    public ConnectedSensorsAdapter(Context context, ArrayList<ConnectedSensorsModel> mConnectedSensorsArrLst, TextView mEditTv, OnItemClickListener mListener1, int j, onUpdateSensorListener mUpdateListener) {
        this.context = context;
        this.mConnectedSensorsArrLst = mConnectedSensorsArrLst;
        this.mEditTv = mEditTv;
        this.mListener1 = mListener1;
        this.j = j;
        this.mUpdateListener = mUpdateListener;
        tf1 = Typeface.createFromAsset(context.getAssets(), "onecontrolfont.ttf");
        isDeleteArrLst = new ArrayList<>();
        for (int i = 0; i < mConnectedSensorsArrLst.size(); i++) {
            if (j == 0) {
                isDeleteArrLst.add(false);
            } else if (j == 1) {
                isDeleteArrLst.add(true);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connected_sensors_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String text = mConnectedSensorsArrLst.get(position).getLocation() + " - " + mConnectedSensorsArrLst.get(position).getZone()
                + " (" + mConnectedSensorsArrLst.get(position).getSensorType() + ")";
        holder.itemTv.setText(text);

        holder.itemTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSensorName(position);
            }
        });

        if (isDeleteArrLst.get(position)) {
            holder.deleteTv.setVisibility(View.VISIBLE);
        } else {
            holder.deleteTv.setVisibility(View.GONE);
        }

        mEditTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditTv.getText().toString().equalsIgnoreCase("Edit")) {
                    mEditTv.setText("Done");
                } else {
                    mEditTv.setText("Edit");
                }
                for (int i = 0; i < mConnectedSensorsArrLst.size(); i++) {
                    if (!isDeleteArrLst.get(i)) {
                        isDeleteArrLst.set(i, true);
                    } else {
                        isDeleteArrLst.set(i, false);
                    }
                }
                notifyDataSetChanged();
            }
        });
        holder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener1.onItemClick(holder.deleteTv, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mConnectedSensorsArrLst.size();
    }

    private void updateSensorName(final int position) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.sensor_edit_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText locationNameEt = (EditText) dialogView.findViewById(R.id.location_name);
        Button submit = (Button) dialogView.findViewById(R.id.btn_submit);
        Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strName = locationNameEt.getText().toString().trim();
                if (null != strName && !TextUtils.isEmpty(strName)) {
                    strName = strName.replaceAll(" ", "~");
                    if (Utils.isNetworkAvailable) {
                        updateSensorLocation(strName, position);
                        alertDialog.dismiss();
                    } else {
                        Utils.showMessageDialog("No Internet", context);
                    }
                } else {
                    locationNameEt.setError("!Required");
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

    private void updateSensorLocation(String strName, int position) {
        int id = mConnectedSensorsArrLst.get(position).getId();
        String zone = mConnectedSensorsArrLst.get(position).getZone();
        int sensorNo = mConnectedSensorsArrLst.get(position).getSensorNo();
        int isWired = mConnectedSensorsArrLst.get(position).getIsWired();
        int sensorTypeId = mConnectedSensorsArrLst.get(position).getSensorTypeId();
        strName = strName.replaceAll(" ", "~");
        zone = zone.replaceAll(" ", "~");
        String strUrl = ServiceHandler.baseUrl + "SetSensorCRUD?Id=" + id + "&SType=" + sensorTypeId + "&MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI
                + "&Location=" + strName + "&Zone=" + zone + "&PortNumber=" + sensorNo + "&IsWired=" + isWired + "&TypeofCall=2";
        Log.e(TAG, "strUrl:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response:-:" + response);
                //{"SetSensorCRUDResult":{"Code":200,"Message":"Successfully Updated"}}
                mUpdateListener.onUpdateChangeListener(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError:-" + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public interface onUpdateSensorListener {
        void onUpdateChangeListener(String response);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTv, deleteTv;
        LinearLayout ll;
        public ViewHolder(View v) {
            super(v);
            ll = (LinearLayout) v.findViewById(R.id.ll);
            itemTv = (TextView) v.findViewById(R.id.item_tv);
            deleteTv = (TextView) v.findViewById(R.id.delete_tv);
            deleteTv.setTypeface(tf1);
        }
    }
}
