package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.models.OpenedSensorsModel;

import java.util.ArrayList;

/**
 * Created by Bharath on 16-Aug-17
 */

public class OpenedSensorsAdapter extends RecyclerView.Adapter<OpenedSensorsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<OpenedSensorsModel> mOpenedSensorsArrLst;

    public OpenedSensorsAdapter(Context context, ArrayList<OpenedSensorsModel> mOpenedSensorsArrLst) {
        this.context = context;
        this.mOpenedSensorsArrLst = mOpenedSensorsArrLst;
    }

    @Override
    public OpenedSensorsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.opened_sensors_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OpenedSensorsAdapter.ViewHolder holder, int position) {
        String locZoneStr = "Left open " + mOpenedSensorsArrLst.get(position).getLocation() + " - " + mOpenedSensorsArrLst.get(position).getZone();
        holder.locationTv.setText(locZoneStr);
    }

    @Override
    public int getItemCount() {
        return mOpenedSensorsArrLst.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationTv;

        public ViewHolder(View v) {
            super(v);
            locationTv = (TextView) v.findViewById(R.id.location_tv);
        }
    }
}
