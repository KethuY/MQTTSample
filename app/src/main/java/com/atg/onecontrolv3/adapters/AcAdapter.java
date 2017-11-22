package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.ACModel;

import java.util.ArrayList;

/**
 * Created by Bharath on 14-Mar-17
 */

public class AcAdapter extends BaseAdapter {
    ArrayList<ACModel> acModelArrLst;
    Context context;
    OnItemClickListener mListener;

    public AcAdapter(Context context, ArrayList<ACModel> acModelArrLst, OnItemClickListener mListener) {
        this.context = context;
        this.acModelArrLst = acModelArrLst;
        this.mListener = mListener;
    }

    @Override
    public int getCount() {
        return acModelArrLst.size();
    }

    @Override
    public Object getItem(int position) {
        return acModelArrLst.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = inflater.inflate(R.layout.ac_model_lv_item, parent, false);
            holder = new ViewHolder();
            holder.acBrand = (Button) view.findViewById(R.id.ac_brand);
            holder.acImport = (Button) view.findViewById(R.id.ac_import);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.acBrand.setText("Brand " + (position + 1));
        holder.acBrand.setText("Import " + (position + 1));

        holder.acBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(holder.acBrand, position);
            }
        });
        holder.acImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(holder.acImport, position);
            }
        });
        return view;
    }

    class ViewHolder {
        Button acBrand, acImport;
    }
}
