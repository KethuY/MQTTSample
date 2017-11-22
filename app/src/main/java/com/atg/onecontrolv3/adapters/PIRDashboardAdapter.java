package com.atg.onecontrolv3.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.activities.EditPIRActivity;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.PIRDBModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.atg.onecontrolv3.helpers.Utils.getEmojiByUnicode;


/**
 * Created by Bharath on 21-Jun-17
 */

public class PIRDashboardAdapter extends BaseAdapter implements MqttHelper.responseListener {
    private static final String TAG = PIRDashboardAdapter.class.getSimpleName();
    private final Typeface tf1;
    MqttHelper.responseListener mqttListener = null;
    private Context context;
    private ArrayList<PIRDBModel> mPirdbModelArrLst = new ArrayList<>();
    private TextView mEditTv;
    private ArrayList<Boolean> isDeleteArrLst;
    private OnItemClickListener mListener;
    private MqttHelper helper;
    private int switchPos;
    //private boolean mSwitchBool;
    private int finalMins = 0;
    private boolean isFromUndo;
    private boolean isSwitchTouched = false;

    public PIRDashboardAdapter(Context context, ArrayList<PIRDBModel> pirdbModelArrLst, TextView mEditTv, OnItemClickListener mListener) {
        this.context = context;
        this.mPirdbModelArrLst = pirdbModelArrLst;
        this.mEditTv = mEditTv;
        this.mListener = mListener;
        tf1 = Typeface.createFromAsset(context.getAssets(), "onecontrolfont.ttf");
        isDeleteArrLst = new ArrayList<>();
        for (int i = 0; i < mPirdbModelArrLst.size(); i++) {
            isDeleteArrLst.add(false);
        }

        mqttListener = this;
        if (Utils.isNetworkAvailable) {
            helper = new MqttHelper(context, mqttListener);
        }
    }

    @Override
    public int getCount() {
        return mPirdbModelArrLst.size();
    }

    @Override
    public Object getItem(int position) {
        return mPirdbModelArrLst.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pir_dashboard_item, parent, false);
            holder = new ViewHolder();
            holder.PIRDBRl = (RelativeLayout) convertView.findViewById(R.id.llPIR);
            holder.pirNameTv = (TextView) convertView.findViewById(R.id.pir_name_tv);
            holder.ADSwitch = (ToggleButton) convertView.findViewById(R.id.on_off_pir_switch);
            holder.deleteTv = (TextView) convertView.findViewById(R.id.delete_tv);
            holder.deleteTv.setTypeface(tf1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String pirName = mPirdbModelArrLst.get(position).getPirName();
        pirName = pirName.replace("~", " ");
        holder.pirNameTv.setText(pirName);

        if (mPirdbModelArrLst.get(position).getTimerTime() > 0) {
            holder.ADSwitch.setChecked(true);
        } else {
            holder.ADSwitch.setChecked(false);
        }

        if (isDeleteArrLst.get(position)) {
            holder.deleteTv.setVisibility(View.VISIBLE);
            holder.ADSwitch.setVisibility(View.GONE);
        } else {
            holder.deleteTv.setVisibility(View.GONE);
            holder.ADSwitch.setVisibility(View.VISIBLE);
        }

        holder.PIRDBRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.deleteTv.getVisibility() == View.VISIBLE) {
                    Utils.getPIRSObj = null;
                    Utils.getPIRSObj = mPirdbModelArrLst.get(position);
                    Log.e(TAG, "getPIRSObj:-:" + Utils.getPIRSObj.getPirName());
                    context.startActivity(new Intent(context, EditPIRActivity.class)/*.putExtra("pirDashboardArrLst", mPirdbModelArrLst.get(position))*/);
                }
            }
        });

        holder.ADSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isSwitchTouched = true;
                return false;
            }
        });

        holder.ADSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isSwitchTouched) {
                    switchPos = position;
                    //mSwitchBool = b;
                    Log.e(TAG, "Entered switch at " + switchPos);
                    if (b) {
                        showNumberPickerDialog(holder.ADSwitch);
                    } else {
                        if (!isFromUndo) {
                            helper.sendMsg("F|" + mPirdbModelArrLst.get(position).getPirNumber() + "|0");
                        } else {
                            isFromUndo = false;
                        }
                    }
                }
            }
        });

        mEditTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mPirdbModelArrLst.size(); i++) {
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
                mListener.onItemClick(holder.deleteTv, position);
            }
        });

        return convertView;
    }

    @Override
    public void onMqttResponse(String res) {
        Log.e(TAG, "resPIRDBAdapter:-:" + res);
        //void DeletePIRExt(string MacId, string IMEI, int PIRNumber);

        Log.e(TAG, "mqttResStr:-:" + res);
        if (res.contains("Notification")) {
            String notiStrArr[] = res.split(Pattern.quote(":"));
            switch (notiStrArr[1]) {
                case "47":
                    showSuccessFailDialog();
                    break;
            }
        }

    }

    private void showSuccessFailDialog() {

        final AlertDialog.Builder alertBox = new AlertDialog.Builder(context);
        alertBox.setMessage("Done " + getEmojiByUnicode(0x1F60A));
        alertBox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isNetworkAvailable) {
                    sendExtVolley();
                }
            }
        });
        /*alertBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });*/
        alertBox.show();
    }

    private void sendExtVolley() {
        String strMethodName = "SetTimeForPIR";
        //http://atghas.com/OneControlService/OCService.svc/SetTimeForPIR?MacId=20f85eeee93e&IMEI=865374023900866&PIR=1&Timer=1
        String strUrl = ServiceHandler.baseUrl + strMethodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI +
                "&PIR=" + mPirdbModelArrLst.get(switchPos).getPirNumber() + "&Timer=" + finalMins + "&PIRName=" + mPirdbModelArrLst.get(switchPos).getPirName();
        Log.e(TAG, "SetTimeForPIRURL:-:" + strUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "SetTimeForPIRRes:-:" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "SetTimeForPIRErrorVolley:-:" + error.getMessage());
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

    private void showNumberPickerDialog(final ToggleButton ADSwitch) {
        Log.e(TAG, "switchPos:-:" + switchPos);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
        dialogBuilder.setView(dialogView);

        final NumberPicker npHr = (NumberPicker) dialogView.findViewById(R.id.np_hr);
        final NumberPicker npMin = (NumberPicker) dialogView.findViewById(R.id.np_min);

        Button updatePIRBtn = (Button) dialogView.findViewById(R.id.btn_update);
        Button undoPIRBtn = (Button) dialogView.findViewById(R.id.btn_undo);

        npHr.setMinValue(0);
        npHr.setMaxValue(8);
        npMin.setMinValue(1);
        npMin.setMaxValue(59);

        int hrs = (mPirdbModelArrLst.get(switchPos).getTimerTime() / 60);
        int mins = (mPirdbModelArrLst.get(switchPos).getTimerTime() % 60);

        if (mPirdbModelArrLst.get(switchPos).getTimerTime() == 0) {
            npHr.setValue(0);
            npMin.setValue(1);
        } else {
            npHr.setValue(hrs);
            npMin.setValue(mins);
        }

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        updatePIRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable) {
                    finalMins = (npHr.getValue() * 60) + npMin.getValue();
                    Log.e(TAG, "finalMins:-:" + finalMins + "npHr:-:" + (npHr.getValue() * 60) + "npMin:-:" + npMin.getValue());
                    helper.sendMsg("F|" + mPirdbModelArrLst.get(switchPos).getPirNumber() + "|" + finalMins);
                    alertDialog.dismiss();
                }
            }
        });

        undoPIRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFromUndo = true;
                ADSwitch.setChecked(false);
                alertDialog.dismiss();
            }
        });

        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.e(TAG, "Entered on Dismiss listener");
                isFromUndo = true;
                ADSwitch.setChecked(false);
            }
        });

    }

    private static class ViewHolder {
        TextView pirNameTv, deleteTv;
        ToggleButton ADSwitch;
        RelativeLayout PIRDBRl;
    }
}
