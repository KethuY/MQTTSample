package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.models.ZmotesModel;

import java.util.ArrayList;

/**
 * Created by sireesha on 12-01-2017.
 */

public class ZmotesViewAdapter extends BaseAdapter {

        Context context;
        ArrayList<ZmotesModel> data;
        LayoutInflater inflater = null;

        public ZmotesViewAdapter(Context context, ArrayList<ZmotesModel> data){
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater)context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View vi = view;
            ViewHolder holder;
            if(view==null){

                /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
                vi = inflater.inflate(R.layout.wifi_list_row, null);

                /****** View Holder Object to contain tabitem.xml file elements ******/

                holder = new ViewHolder();
                holder.tvZmoteName = (TextView) vi.findViewById(R.id.tvSSIDName);

                /************  Set holder with LayoutInflater ************/
                vi.setTag( holder );
            }
            else
                holder=(ViewHolder)vi.getTag();

            if(data.size() != 0){
                //((RadioButton) holder.radioGroup.getChildAt(i)).setText(String.valueOf(data[i]));
                //holder.rbSSIDName.setText(String.valueOf(data[i])+"");
                holder.tvZmoteName.setText(data.get(i).getChipID()+"");
            }

            return vi;
        }

    public static class ViewHolder{

        public TextView tvZmoteName;
    }
}
