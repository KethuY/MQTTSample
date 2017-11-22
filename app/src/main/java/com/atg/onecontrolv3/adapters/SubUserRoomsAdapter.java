package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.SubuserRoomsActivity;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.RoomsModel;
import com.atg.onecontrolv3.models.SubUserRoomsModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Bharath on 19-11-2016
 */

public class SubUserRoomsAdapter extends RecyclerView.Adapter<SubUserRoomsAdapter.RoomsDisplayViewHolder> {
    List<RoomsModel> data;
    ArrayList<SubUserRoomsModel> getSubUserRoomsData;
    RoomsDisplayViewHolder viewHolder;
    Context context;
    private OnItemClickListener mListener;

    public SubUserRoomsAdapter(Context context, List<RoomsModel> data, ArrayList<SubUserRoomsModel> getSubUserRoomsData, OnItemClickListener mListener) {
        this.context = context;
        this.data = data;
        this.mListener = mListener;
        this.getSubUserRoomsData = getSubUserRoomsData;
        SubuserRoomsActivity.childRooms = new HashSet<>();
    }

    @Override
    public RoomsDisplayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_user_rooms_adapter, parent, false);
        viewHolder = new RoomsDisplayViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RoomsDisplayViewHolder holder, final int position) {
        holder.tvRoomTitle.setText(data.get(position).getRoomName());


        //if(data.get(position).getRoomName().equals(getSubUserRoomsData.get(position).getRoomName()))

        holder.btnToggle.setChecked(data.get(position).isEnabled());

        if(data.get(position).isEnabled()){
            SubuserRoomsActivity.childRooms.add(data.get(position).getRoomId());
        }


        holder.btnToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    holder.btnToggle.setChecked(true);
                 //   data.get(position).setEnabled(true);

                    SubuserRoomsActivity.childRooms.add(data.get(position).getRoomId());
                    Log.e("isChecked","dataaa:::"+data.get(position).getRoomId());
                } else {
                    holder.btnToggle.setChecked(false);
                  //  data.get(position).setEnabled(false);

                    SubuserRoomsActivity.childRooms.remove(data.get(position).getRoomId());
                    Log.e("!isChecked","dataaa:::"+data.get(position).getRoomId());
                }
            }
        });
        /*holder.btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //mListener.onItemClick(holder.btnToggle, position);
            }
        });*/
        holder.rlRoomsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.rlRoomsLayout, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class RoomsDisplayViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle;
        ToggleButton btnToggle;
        RelativeLayout rlRoomsLayout;

        public RoomsDisplayViewHolder(View itemView) {
            super(itemView);
            tvRoomTitle = (TextView) itemView.findViewById(R.id.tvRoomTitle);
            btnToggle = (ToggleButton) itemView.findViewById(R.id.btnToggle);
            rlRoomsLayout = (RelativeLayout) itemView.findViewById(R.id.rlRoomsLayout);
        }
    }
}
