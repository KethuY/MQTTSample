package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.SceneEditActivity;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.SingleSceneTimerModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;

import static com.atg.onecontrolv3.helpers.Utils.replaceTilt;

/**
 * Created by Bharath on 9/23/2017
 */

public class SceneTimerAdapter extends BaseAdapter  {

    OnItemClickListener mListener;
    private Context context;
    private ArrayList<SingleSceneTimerModel> singleSceneTimerModelArrLst;
    private Typeface ocV3Tf;
    private LayoutInflater inflater;
    private int roomCnt;
    private int appCnt;

    public SceneTimerAdapter(Context context, ArrayList<SingleSceneTimerModel> singleSceneTimerModelArrLst, OnItemClickListener mListener) {
        this.context = context;
        this.singleSceneTimerModelArrLst = singleSceneTimerModelArrLst;
        ocV3Tf = Typeface.createFromAsset(context.getAssets(), "oc_font_v3.ttf");
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListener = mListener;
    }

    @Override
    public int getCount() {
        return singleSceneTimerModelArrLst.size();
    }

    @Override
    public Object getItem(int i) {
        return singleSceneTimerModelArrLst.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.scenese_timer_item, parent, false);
            holder = new ViewHolder();
            holder.mainLl = convertView.findViewById(R.id.main_ll);
            holder.imgLl = convertView.findViewById(R.id.img_ll);
            holder.sceneTypeTv = convertView.findViewById(R.id.scene_type_tv);
            holder.sceneEditTv = convertView.findViewById(R.id.scene_edit_tv);
            holder.applianceCountTv = convertView.findViewById(R.id.appliances_count_tv);
            holder.roomCountTv = convertView.findViewById(R.id.room_count_tv);
            holder.sceneStatusTv = convertView.findViewById(R.id.status_tv);
            holder.sceneEditTv.setTypeface(ocV3Tf);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.sceneTypeTv.setText(replaceTilt(singleSceneTimerModelArrLst.get(position).getName()));

        setBgImg(holder.imgLl, singleSceneTimerModelArrLst.get(position).getImage());

        final String relays = singleSceneTimerModelArrLst.get(position).getRelays();
        /*if (singleSceneTimerModelArrLst.get(position).getTimertype() == 1) {
            holder.applianceCountTv.setText(Utils.APP_CNT + " Appliances");
            holder.roomCountTv.setText(Utils.ROOMS_CNT + " Rooms");
        } else */
        {
            returnCnt(relays);
            if (appCnt == 1) {
                holder.applianceCountTv.setText(appCnt + " Appliance");
            } else {
                holder.applianceCountTv.setText(appCnt + " Appliances");
            }
            if (roomCnt == 1) {
                holder.roomCountTv.setText(roomCnt + " Room");
            } else {
                holder.roomCountTv.setText(roomCnt + " Rooms");
            }
        }

        holder.mainLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.mainLl, position);

               /* if (singleSceneTimerModelArrLst.get(position).getTimertype() == 1) {
                    if (Utils.isNetworkAvailable) {
                        helper.sendMsg("A|" + "1" + "|M");// TODO: room id should be dynamic..!
                    } else {
                        Utils.showMessageDialog(Utils.NO_INTERNET, context);
                    }

                } else if (singleSceneTimerModelArrLst.get(position).getTimertype() == 3) {

                }*/
            }
        });

        holder.sceneEditTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.getScenesObj = singleSceneTimerModelArrLst.get(position);
                context.startActivity(new Intent(context, SceneEditActivity.class).putExtra("IsFrom", 2));
            }
        });

        return convertView;
    }

    private void setBgImg(final LinearLayout imgLl, String image) {
        switch (image) {
            case "Scene_1":
                //imgLl.setBackgroundResource(R.drawable.scene_one);
                setImageBG("scene_one", imgLl);
                break;
            case "Scene_2":
                //imgLl.setBackgroundResource(R.drawable.scene_two);
                setImageBG("scene_two", imgLl);
                break;
            case "Scene_3":
                //imgLl.setBackgroundResource(R.drawable.scene_three);
                setImageBG("scene_three", imgLl);
                break;
            case "Scene_4":
                //imgLl.setBackgroundResource(R.drawable.scene_four);
                setImageBG("scene_four", imgLl);
                break;
            case "Scene_5":
                //imgLl.setBackgroundResource(R.drawable.scene_five);
                setImageBG("scene_five", imgLl);
                break;
            case "Scene_6":
                //imgLl.setBackgroundResource(R.drawable.scene_six);
                setImageBG("scene_six", imgLl);
                break;
            case "Scene_7":
                //imgLl.setBackgroundResource(R.drawable.scene_seven);
                setImageBG("scene_seven", imgLl);
                break;
        }
    }

    private void setImageBG(String name, final LinearLayout imgLl) {
        Glide.with(context).load(getImage(name)).asBitmap().into(new SimpleTarget<Bitmap>(80, 150) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(resource);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    imgLl.setBackground(drawable);
                }
            }
        });
    }

    private void returnCnt(String relays) {
        roomCnt = 0;
        appCnt = 0;
        if (relays.length() > 0) {
            if (relays.contains(",")) {
                String[] roomIdsApp = relays.split(",");
                roomCnt = roomIdsApp.length;
                for (String aRoomIdsApp : roomIdsApp) {
                    String[] appliances = aRoomIdsApp.split(":");
                    char[] appArr = appliances[1].toCharArray();
                    appCnt += appArr.length;
                }
            } else {
                String[] roomApp = relays.split(":");
                roomCnt = 1;
                String app = roomApp[1];
                char[] appArr = app.toCharArray();
                appCnt += appArr.length;
            }
        }
    }

    private int getImage(String imageName) {

        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    private static class ViewHolder {
        LinearLayout mainLl, imgLl;
        TextView sceneTypeTv, sceneEditTv, applianceCountTv, roomCountTv, sceneStatusTv;
    }
}
