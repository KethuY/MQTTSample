package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;

import static com.atg.onecontrolv3.helpers.Utils.getTimerModelObj;


/**
 * Created by Bharath on 11-May-17
 */

public class TimerRepeatModeAdapter extends BaseAdapter {
    private static final String TAG = "TimerRepeatModeAdapter";
    public ArrayList<Boolean> isDaySelectedArrLst;
    Context context;
    private ArrayList<String> daysArrLst;
    private LayoutInflater inflater;
    OnItemClickListener mListener;

    public TimerRepeatModeAdapter(Context context, ArrayList<String> daysArrLst, OnItemClickListener listener) {
        this.context = context;
        this.daysArrLst = daysArrLst;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        isDaySelectedArrLst = new ArrayList<>();
        mListener=listener;
        SharedPreferences savedInstanceState = PreferenceManager.getDefaultSharedPreferences(context);
        String daysChecked = savedInstanceState.getString("days", "");
        Log.e(TAG, "DaysChecked:-:" + daysChecked);


        if (getTimerModelObj != null && getTimerModelObj.isRepeat()) {

            if (getTimerModelObj.isSunday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }

            if (getTimerModelObj.isMonday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }
            if (getTimerModelObj.isTuesday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }


            if (getTimerModelObj.isWednesday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }
            if (getTimerModelObj.isThursday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }

            if (getTimerModelObj.isFriday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }
            if (getTimerModelObj.isSaturday()) {
                isDaySelectedArrLst.add(true);
            } else {
                isDaySelectedArrLst.add(false);
            }

        } else if (daysChecked != null && daysChecked.length() > 0) {
            try {

                for (int i = 0; i < 7; i++) {
                    isDaySelectedArrLst.add(false);
                }

                char daysarr[] = daysChecked.toCharArray();
                Log.e(TAG, "daysarr:-:" + Arrays.toString(daysarr));
                for (int i = 0; i < daysarr.length; i++) {
                    if (daysarr[i] == '1') {
                        setDay(i);
                    }
                }
                notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, "Exception:-:" + e.getMessage());
            }
        } else {
            for (int i = 0; i < 7; i++) {
                isDaySelectedArrLst.add(false);
            }
        }
    }

    private void setDay(int i) {
        switch (i) {
            case 0:
                isDaySelectedArrLst.set(i, true);
                break;
            case 1:
                isDaySelectedArrLst.set(i, true);
                break;
            case 2:
                isDaySelectedArrLst.set(i, true);
                break;
            case 3:
                isDaySelectedArrLst.set(i, true);
                break;
            case 4:
                isDaySelectedArrLst.set(i, true);
                break;
            case 5:
                isDaySelectedArrLst.set(i, true);
                break;
            case 6:
                isDaySelectedArrLst.set(i, true);
                break;
        }
    }


    @Override
    public int getCount() {
        return daysArrLst.size();
    }

    @Override
    public Object getItem(int position) {
        return daysArrLst.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.reapeat_iv_item, parent, false);
            holder = new ViewHolder();
            holder.daysTv = (TextView) convertView.findViewById(R.id.day_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.daysTv.setText(daysArrLst.get(position));

        if (isDaySelectedArrLst.get(position)) {
            holder.daysTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
        } else {
            holder.daysTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDaySelectedArrLst.get(position)) {
                    isDaySelectedArrLst.set(position, false);
                } else {
                    isDaySelectedArrLst.set(position, true);
                }

                mListener.onItemClick(finalConvertView,position);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView daysTv;
    }
}
