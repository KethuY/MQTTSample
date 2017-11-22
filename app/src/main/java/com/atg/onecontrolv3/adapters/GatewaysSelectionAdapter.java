package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.SingleMacModel;

import java.util.List;

/**
 * Created by Bharath on 31-Aug-17
 */

public class GatewaysSelectionAdapter extends RecyclerView.Adapter<GatewaysSelectionAdapter.ViewHolder> {

    private Context context;
    private List<SingleMacModel> mUserMacsArrLst;
    private OnItemClickListener mListener;
    private Vibrator vibrator;

    public GatewaysSelectionAdapter(Context context, List<SingleMacModel> mUserMacsArrLst, OnItemClickListener mListener) {
        this.context = context;
        this.mUserMacsArrLst = mUserMacsArrLst;
        this.mListener = mListener;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gateways_selection_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //char g1 = gatewayNames.get(position).charAt(gatewayNames.get(position).length() - 1);
        /*char g1 = mUserMacsArrLst.get(position).getMacName().charAt(0);
        holder.gatewayNameTv1.setText(String.valueOf(g1));*/
        String gtNme = mUserMacsArrLst.get(position).getMacName();
        String arrS[] = gtNme.split("~");
        if (arrS.length > 1) {
            char one = arrS[0].charAt(0);
            char two = arrS[1].charAt(0);
            String finalStr = (String.valueOf(one) + String.valueOf(two)).toUpperCase();
            holder.gatewayNameTv1.setText(finalStr);
        } else {
            char g1 = mUserMacsArrLst.get(position).getMacName().charAt(0);
            holder.gatewayNameTv1.setText(String.valueOf(g1));
        }
        gtNme = gtNme.replaceAll("~", " ");
        holder.gatewayNameTv2.setText(gtNme);

        holder.gatewayNameLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                mListener.onItemClick(holder.gatewayNameLl, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserMacsArrLst.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout gatewayNameLl;
        TextView gatewayNameTv1, gatewayNameTv2;

        ViewHolder(View itemView) {
            super(itemView);
            gatewayNameTv1 = itemView.findViewById(R.id.gateway_1);
            gatewayNameTv2 = itemView.findViewById(R.id.gateway_2);
            gatewayNameLl = itemView.findViewById(R.id.gateway_ll);
        }
    }
}
