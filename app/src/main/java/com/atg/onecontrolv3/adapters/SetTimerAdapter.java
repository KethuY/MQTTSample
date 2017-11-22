package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.activities.EditTimerActivity;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.GetTimersModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.atg.onecontrolv3.helpers.Utils.getColorPrimaryDark;

/**
 * Created by Bharath on 19-Apr-17
 */

public class SetTimerAdapter extends BaseAdapter {
    private static final String TAG = "SetTimerAdapter";
    private final Typeface tf1;
    private String displayText = "";
    private int colorPrimaryDark;
    private LayoutInflater inflater;
    private String descStr;
    private SendDataToServerListener mListener;
    private Context context;
    private ArrayList<GetTimersModel> getTimersModelArrLst;
    private TransparentProgressDialog pd;
    private boolean isSwitchTouched = false;
    private TextView mEditTv;
    private ArrayList<Boolean> isDeleteArrLst;
    private OnItemClickListener mListener1;
    private String dateTimeSelectedStr = "";
    private int j;

    public SetTimerAdapter(Context context, ArrayList<GetTimersModel> getTimersModelArrLst, SendDataToServerListener mListener, TextView mEditTv, OnItemClickListener mListener1, int j) {
        this.context = context;
        this.getTimersModelArrLst = getTimersModelArrLst;
        pd = new TransparentProgressDialog(context, R.drawable.progress);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListener = mListener;
        this.mEditTv = mEditTv;
        this.mListener1 = mListener1;
        this.j = j;
        tf1 = Typeface.createFromAsset(context.getAssets(), "onecontrolfont.ttf");
        isDeleteArrLst = new ArrayList<>();
        for (int i = 0; i < getTimersModelArrLst.size(); i++) {
            if (j == 0) {
                isDeleteArrLst.add(false);
            } else if (j == 1) {
                isDeleteArrLst.add(true);
            }
        }
        colorPrimaryDark = getColorPrimaryDark(context);
    }

    @Override
    public int getCount() {
        return getTimersModelArrLst.size();
    }

    @Override
    public Object getItem(int i) {
        return getTimersModelArrLst.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.set_timer_list_item, parent, false);
            holder = new ViewHolder();
            holder.nameTv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.timeTv = (TextView) convertView.findViewById(R.id.time_tv);
            holder.actionTv = (TextView) convertView.findViewById(R.id.action_tv);
            holder.descTv = (TextView) convertView.findViewById(R.id.desc_tv);
            holder.sunTv = (TextView) convertView.findViewById(R.id.sun_tv);
            holder.monTv = (TextView) convertView.findViewById(R.id.mon_tv);
            holder.tuesTv = (TextView) convertView.findViewById(R.id.tues_tv);
            holder.wedTv = (TextView) convertView.findViewById(R.id.wednes_tv);
            holder.thursTv = (TextView) convertView.findViewById(R.id.thurs_tv);
            holder.friTv = (TextView) convertView.findViewById(R.id.fri_tv);
            holder.saturTv = (TextView) convertView.findViewById(R.id.satur_tv);
            holder.ADSwitch = (Switch) convertView.findViewById(R.id.ad_switch);
            holder.RlItem = (RelativeLayout) convertView.findViewById(R.id.rl_item);
            holder.deleteTv = (TextView) convertView.findViewById(R.id.delete_tv);
            holder.nextExecTv = (TextView) convertView.findViewById(R.id.next_exec_tv);
            holder.lLDays = (LinearLayout) convertView.findViewById(R.id.ll_days);
            holder.deleteTv.setTypeface(tf1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String sub = null;
        try {
            String name = getTimersModelArrLst.get(position).getName();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            holder.nameTv.setText(name);
            String time = getTimersModelArrLst.get(position).getExecTime();
            sub = time.substring(time.length() - 5);
            //holder.timeTv.setText("Timer : " + sub);
            holder.timeTv.setText(sub);
            if (isDeleteArrLst.get(position)) {
                holder.deleteTv.setVisibility(View.VISIBLE);
            } else {
                holder.deleteTv.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
        /*switch (getTimersModelArrLst.get(position).getTimertype()) {
            case 1:
                descStr = "All Rooms in " + macNameStr;
                break;
            case 2:
                String[] arrS1 = getTimersModelArrLst.get(position).getRelays().split(",");
                descStr = "All Appliances in " + macNameStr + "-" + getTimersModelArrLst.get(position).getName();
                break;
            case 3:
                String[] arrS = getTimersModelArrLst.get(position).getRelays().split(":");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Utils.mDashBoardArrLst.size(); i++) {
                    if (Utils.mDashBoardArrLst.get(i).getRoomId() == Integer.parseInt(arrS[0])) {
                        sb.append(Utils.mDashBoardArrLst.get(i).getRoomName()).append(",");
                    }
                }
                try {
                    sb.setLength(Math.max(sb.length() - 1, 0));
                    descStr = arrS[1] + " in " + macNameStr + "-" + getTimersModelArrLst.get(position).getName();
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                arrS = new String[]{};
                //sb = new StringBuilder();
                break;
        }
        holder.descTv.setText(descStr);
        descStr = "";*/

        if (getTimersModelArrLst.get(position).isAction()) {
            holder.actionTv.setText("Turn ON");
            holder.actionTv.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.actionTv.setText("Turn OFF");
            holder.actionTv.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
        holder.ADSwitch.setChecked(getTimersModelArrLst.get(position).isStatus());
        if (getTimersModelArrLst.get(position).isStatus()) {
            holder.timeTv.setTextColor(colorPrimaryDark);
            holder.nameTv.setTextColor(colorPrimaryDark);//Changed
        } else {
            holder.timeTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
            holder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        if (!getTimersModelArrLst.get(position).isMonday()) {
            holder.monTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
            holder.monTv.setTextColor(colorPrimaryDark);
        } else {
            holder.monTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.monTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        if (!getTimersModelArrLst.get(position).isTuesday()) {
            holder.tuesTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
            holder.tuesTv.setTextColor(colorPrimaryDark);
        } else {
            holder.tuesTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.tuesTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        if (!getTimersModelArrLst.get(position).isWednesday()) {
            holder.wedTv.setTextColor(colorPrimaryDark);
            holder.wedTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
        } else {
            holder.wedTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.wedTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        if (!getTimersModelArrLst.get(position).isThursday()) {
            holder.thursTv.setTextColor(colorPrimaryDark);
            holder.thursTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
        } else {
            holder.thursTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.thursTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        if (!getTimersModelArrLst.get(position).isFriday()) {
            holder.friTv.setTextColor(colorPrimaryDark);
            holder.friTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
        } else {
            holder.friTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.friTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        if (!getTimersModelArrLst.get(position).isSaturday()) {
            holder.saturTv.setTextColor(colorPrimaryDark);
            holder.saturTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
        } else {
            holder.saturTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.saturTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        if (!getTimersModelArrLst.get(position).isSunday()) {
            holder.sunTv.setTextColor(colorPrimaryDark);
            holder.sunTv.setBackgroundResource(R.drawable.oc_appliance_count_bg);
        } else {
            holder.sunTv.setBackgroundResource(R.drawable.timer_off_repeat);
            holder.sunTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        holder.RlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.deleteTv.getVisibility() == View.VISIBLE) {
                    Utils.getTimerModelObj = null;
                    Utils.getTimerModelObj = getTimersModelArrLst.get(position);
                    //set  Utils.roomIdFromJson=getTimersModelArrLst.get(position).
                /*Intent intent = new Intent(context, ViewSetAlarmActvity.class);
                context.startActivity(intent);*/
                    //  intent.putExtra("position", position);
                    //  intent.putExtra("getTimersModelArrLst", getTimersModelArrLst);

                    StringBuilder sb = new StringBuilder();

                    if (Utils.getTimerModelObj.isSunday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }

                    if (Utils.getTimerModelObj.isMonday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }
                    if (Utils.getTimerModelObj.isTuesday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }

                    if (Utils.getTimerModelObj.isWednesday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }
                    if (Utils.getTimerModelObj.isThursday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }

                    if (Utils.getTimerModelObj.isFriday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }
                    if (Utils.getTimerModelObj.isSaturday()) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }
                    Intent intent = new Intent(context, EditTimerActivity.class); //redirect from EditTimer Class
                    intent.putExtra("Type", Utils.getTimerModelObj.getTimertype());
                    intent.putExtra("sb", Utils.getTimerModelObj.getRelays());
                    intent.putExtra("IsFrom", "1");
                    intent.putExtra("isFromRepeat", true);
                    intent.putExtra("days", sb.toString());
                    context.startActivity(intent);
                }
            }
        });

        holder.ADSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSwitchTouched = true;
                return false;
            }
        });
        holder.ADSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isSwitchTouched) {
                    dateTimeSelectedStr = getTimersModelArrLst.get(position).getServerTime();
                    String serverDate = dateTimeSelectedStr.substring(0, 8);
                    String serverTime = dateTimeSelectedStr.substring(8, 12);
                    //201706131248                   Next Execution:                            Next Execution:
                    if (displayText.equalsIgnoreCase("Tomorrow") || displayText.equalsIgnoreCase("Now")) {
                        Calendar cl = Calendar.getInstance();
                        cl.setTimeInMillis(System.currentTimeMillis());
                        cl.add(Calendar.DATE, 1);
                        Date currentDatePlusOne = cl.getTime();
                        String currentDatePlusOneStr = dateToString(currentDatePlusOne);
                        dateTimeSelectedStr = currentDatePlusOneStr + serverTime;
                                                        //Next Execution:
                    } else if ((displayText.equalsIgnoreCase("Today"))) {
                        dateTimeSelectedStr = Utils.getCurrentDate() + serverTime;
                    }
                    if (b) {
                        holder.timeTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                        holder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                        setTimer(position, "1");
                    } else {
                        holder.timeTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
                        holder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
                        setTimer(position, "0");
                    }
                }
            }
        });

        if (!getTimersModelArrLst.get(position).isRepeat()) {
        /*if (getTimersModelArrLst.get(position).getIsDaysRepetArrLst().contains(false) && !getTimersModelArrLst.get(position).getIsDaysRepetArrLst().contains(true)) {*/
            holder.lLDays.setVisibility(View.GONE);
            holder.nextExecTv.setVisibility(View.VISIBLE);
            Log.e(TAG, "compareTime:-:" + compareTime(sub));
            holder.ADSwitch.setPadding(10, 0, 0, 0);
            holder.nextExecTv.setText(compareTime(sub));
        } else {
            dateTimeSelectedStr = getTimersModelArrLst.get(position).getServerTime();
            holder.lLDays.setVisibility(View.VISIBLE);
            holder.nextExecTv.setVisibility(View.GONE);
        }

        mEditTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditTv.getText().toString().equalsIgnoreCase("Edit")) {
                    mEditTv.setText("Done");
                } else {
                    mEditTv.setText("Edit");
                }
                for (int i = 0; i < getTimersModelArrLst.size(); i++) {
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
        notifyDataSetChanged();

        return convertView;
    }

    private String compareTime(String sub) {
        //String displayText = "";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);

        String arrS[] = sub.split(":");

        int mIntSelectedHr = Integer.parseInt(arrS[0]);
        int mIntSelectedMin = Integer.parseInt(arrS[1]);
        Log.e(TAG, "mIntSelectedHr:-:" + mIntSelectedHr + " mIntSelectedMin:-:" + mIntSelectedMin + " Sub:-:" + sub);
        if (mIntSelectedHr < hour) {
            displayText = /*Next Execution: */"Tomorrow ";
        }

        if (mIntSelectedHr > hour) {//mInt = 10 -- curr = 11
            displayText = /*Next Execution: */"Today";
        } else if (mIntSelectedHr == hour) {
            if (mIntSelectedMin <= minutes) {
                displayText = /*Next Execution: */"Tomorrow";
            } else if (mIntSelectedMin >= minutes) {
                displayText = /*Next Execution: */"Today";
            } else if (mIntSelectedMin == minutes) {
                displayText = /*Next Execution: */"Now";
            }
        }
        return displayText;
    }

    private void setTimer(int position, final String aswitch) {
        String finalUrl = "";

        String sceneName = getTimersModelArrLst.get(position).getServerName();
        sceneName = sceneName.replaceAll(" ", "~");
        String repeatStr = "0";

        if (getTimersModelArrLst.get(position).isRepeat()) {
            repeatStr = "1";
        }
        String ADStr = "0";
        if (getTimersModelArrLst.get(position).isAction()) {
            ADStr = "1";
        }

        StringBuilder sb = new StringBuilder();
        getDaysStatus(getTimersModelArrLst.get(position).isSunday(), sb);
        getDaysStatus(getTimersModelArrLst.get(position).isMonday(), sb);
        getDaysStatus(getTimersModelArrLst.get(position).isTuesday(), sb);
        getDaysStatus(getTimersModelArrLst.get(position).isWednesday(), sb);
        getDaysStatus(getTimersModelArrLst.get(position).isThursday(), sb);
        getDaysStatus(getTimersModelArrLst.get(position).isFriday(), sb);
        getDaysStatus(getTimersModelArrLst.get(position).isSaturday(), sb);

        String days = sb.toString();

        pd.show();
        //String dateTimeSelectedStr = getTimersModelArrLst.get(position).getServerTime();
        String checkedStatusStr = getTimersModelArrLst.get(position).getRelays();
        String baseUrl = "http://atghas.com/OneControlService/OCService.svc/";
        if (getTimersModelArrLst.get(position).getTimertype() == 1) {
            finalUrl = baseUrl + "SetTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=1&Value=" + sceneName
                    + "-" + aswitch + "-" + dateTimeSelectedStr + "-" + repeatStr + "-" + days + "-" + ADStr;
        } else if (getTimersModelArrLst.get(position).getTimertype() == 2) {
                    /*http://atghas.com/OneControlService/OCService.svc/SetTimer?MacId=20f85eeee93e&IMEI=352087074323842
                    &TimerType=1&Value=Test22-20001,20005-0-201704180218-1-1000001-1 */
            finalUrl = baseUrl + "SetTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=2&Value=" + sceneName
                    + "-" + checkedStatusStr + "-" + aswitch + "-" + dateTimeSelectedStr + "-" + repeatStr + "-" + days + "-" + ADStr;

        } else if (getTimersModelArrLst.get(position).getTimertype() == 3) {
                    /*http://atghas.com/OneControlService/OCService.svc/SetTimer?MacId=20f85eeee93e&IMEI=352087074323842
                    &TimerType=3&Value=Test33-20001-1,3,5,9-0-201704170118-1-1000001-0*/
            /*String arrS[] = checkedStatusStr.split(":");
            finalUrl = baseUrl + "SetTimer?MacId=" + Utils.MACID + "&IMEI=" + Utils.IMEI + "&TimerType=3&Value=" + sceneName
                    + "-" + arrS[0] + "-" + arrS[1] + "-" + s + "-" + dateTimeSelectedStr + "-" + repeatStr + "-" + days + "-" + ADStr;*/
            finalUrl = ServiceHandler.baseUrl + "SetTimer?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI + "&TimerType=3&Value=" + sceneName
                    + "-" + checkedStatusStr + "-" + aswitch + "-" + dateTimeSelectedStr + "-" + repeatStr + "-" + days + "-" + ADStr;
        }
        Log.e(TAG, "finalUrl:-:" + finalUrl);
        StringRequest strRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                mListener.onSwitchChangeListener(response, aswitch);
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

    private void getDaysStatus(boolean isDay, StringBuilder daysSb) {

        if (isDay) {
            daysSb.append("1");
        } else {
            daysSb.append("0");
        }
    }

    private Date stringToDate(String serverDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = format.parse(serverDate);
            System.out.println(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    private String dateToString(Date strToDate) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
        String datetime = "";
        try {
            datetime = dateformat.format(strToDate);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return datetime;
    }

    public interface SendDataToServerListener {
        void onSwitchChangeListener(String string, String s);
    }

    private class ViewHolder {
        TextView nameTv, timeTv, actionTv, descTv, deleteTv, nextExecTv;
        TextView monTv, tuesTv, wedTv, thursTv, friTv, saturTv, sunTv;
        Switch ADSwitch;
        LinearLayout lLDays;
        RelativeLayout RlItem;
    }
}
