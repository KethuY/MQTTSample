package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.SubUsersModel;

import java.util.ArrayList;

/**
 * Created by Bharath on 19-11-2016
 */

public class SubusersAdapter extends RecyclerView.Adapter<SubusersAdapter.SubUserViewHolder> {
    private static final String TAG = "SubusersAdapter";
    Context context;
    OnItemClickListener listener;
    ArrayList<SubUsersModel> data;
    Typeface tf;

    public SubusersAdapter(Context context, OnItemClickListener listener, ArrayList<SubUsersModel> data) {
        this.context = context;
        this.listener = listener;
        this.data = data;
        tf = Typeface.createFromAsset(context.getAssets(), "onecontrolfont.ttf");

    }

    @Override
    public SubUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_sub_user, parent, false);
        return new SubUserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SubUserViewHolder holder, final int position) {

        String userName = data.get(position).getMobile();
        userName = userName.contains("~") ? userName.replace("~", " ") : userName;
        holder.tvSubUserName.setText(userName);
        holder.tvSubUserDelete.setTypeface(tf);
        holder.tvSubUserDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(holder.tvSubUserDelete, position);
                notifyDataSetChanged();
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (lov == 2) {*/
                listener.onItemClick(holder.cardView, position);
                // }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class SubUserViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubUserName;
        TextView tvSubUserDelete;
        CardView cardView;

        public SubUserViewHolder(View itemView) {
            super(itemView);
            tvSubUserDelete = (TextView) itemView.findViewById(R.id.tvSubUserDelete);
            tvSubUserName = (TextView) itemView.findViewById(R.id.tvSubUserName);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
        }
    }

}
