package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.RoomsModel;

import java.util.List;


/**
 * Created by sireesha on 26-08-2016
 */
public class EditDevicesAdapter extends RecyclerView.Adapter<EditDevicesAdapter.EditDevicesViewHolder> {
    Context context;
    List<RoomsModel> model;
    OnItemClickListener mListener;
    Typeface tf, tf1;

    public EditDevicesAdapter(Context context, List<RoomsModel> model, OnItemClickListener mListener) {
        this.context = context;
        this.mListener = mListener;
        this.model = model;
        tf = Typeface.createFromAsset(context.getAssets(), "settings_font.ttf");
        tf1 = Typeface.createFromAsset(context.getAssets(), "onecontrolfont.ttf");
    }

    @Override
    public EditDevicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_device_row_view, parent, false);
        return new EditDevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EditDevicesViewHolder holder, final int position) {
        /*Animation animationLeft = AnimationUtils.loadAnimation(context, R.anim.left_to_right);
        Animation animationRight = AnimationUtils.loadAnimation(context, R.anim.right_to_left);
        if (position % 2 == 0) {
            holder.cardView.startAnimation(animationLeft);
        } else {
            holder.cardView.startAnimation(animationRight);
        }*/
        holder.tvRoomName.setText(model.get(position).getRoomName());
        holder.tvEditRoom.setTypeface(tf);
        holder.tvDeleteRoom.setTypeface(tf1);
        holder.llRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.llRooms, position);
            }
        });

        holder.tvEditRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.tvEditRoom, position);
            }
        });
        holder.tvDeleteRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.tvDeleteRoom, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public void remove(int position) {
        model.remove(position);
        notifyItemRemoved(position);
    }

    public class EditDevicesViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout llRooms;
        TextView tvRoomName, tvEditRoom, tvDeleteRoom;
        CardView cardView;

        public EditDevicesViewHolder(View itemView) {
            super(itemView);
            llRooms = (RelativeLayout) itemView.findViewById(R.id.llRooms);
            tvRoomName = (TextView) itemView.findViewById(R.id.tvRoomName);
            tvEditRoom = (TextView) itemView.findViewById(R.id.tvEditRoom);
            tvDeleteRoom = (TextView) itemView.findViewById(R.id.tvDeleteRoom);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
        }
    }

}
