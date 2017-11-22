package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;


/**
 * Created by Bharath on 9/24/2016
 */

public class SelectImageAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    OnItemClickListener mListener;
    private Context context;
    private int[] homepageItemsImgs;


    public SelectImageAdapter(Context context, int[] homepageItemsImgs, OnItemClickListener mListener) {
        this.context = context;
        this.homepageItemsImgs = homepageItemsImgs;
        this.mListener = mListener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return homepageItemsImgs.length;
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
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.image_item, viewGroup, false);
            holder = new ViewHolder();
            holder.img = convertView.findViewById(R.id.grid_image);
            holder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.img.setPadding(10, 10, 10, 20);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.img.setImageResource(homepageItemsImgs[position]);

        if (Utils.getScenesObj != null) {
            setBorder(holder.img, Utils.getScenesObj.getImage(), position);
        }

        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.img, position);
            }
        });

        return convertView;
    }

    private void setBorder(ImageView img, String image, int position) {
        switch (image) {
            case "Scene_1":
                if (position == 0) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
            case "Scene_2":
                if (position == 1) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
            case "Scene_3":
                if (position == 2) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
            case "Scene_4":
                if (position == 3) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
            case "Scene_5":
                if (position == 4) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
            case "Scene_6":
                if (position == 5) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
            case "Scene_7":
                if (position == 6) {
                    img.setBackgroundResource(R.drawable.rectangle_et_style_sharp);
                }
                break;
        }
    }

    public class ViewHolder {
        ImageView img;
    }
}

