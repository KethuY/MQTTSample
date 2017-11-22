package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.GetMacInfoModel;

import java.util.ArrayList;

/**
 * Created by Bharath on 21-Jun-17
 */

public class PIRApplianceAdapter extends BaseAdapter {

    private Typeface tf1, tf2;
    private Context context;
    private ArrayList<GetMacInfoModel> mGetMacInfoModelArrLst = new ArrayList<>();
    private OnItemClickListener mListener;
    private Vibrator vibrator;

    public PIRApplianceAdapter(Context context, ArrayList<GetMacInfoModel> mGetMacInfoModelArrLst, OnItemClickListener mListener) {
        this.context = context;
        this.mGetMacInfoModelArrLst = mGetMacInfoModelArrLst;
        this.mListener = mListener;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        tf1 = Typeface.createFromAsset(context.getAssets(), "oc_font_v3.ttf");
        tf2 = Typeface.createFromAsset(context.getAssets(), "uifont.ttf");
    }

    @Override
    public int getCount() {
        return mGetMacInfoModelArrLst.size();
    }

    @Override
    public Object getItem(int i) {
        return mGetMacInfoModelArrLst.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pir_appliance_adapter_item, parent, false);
            holder = new ViewHolder();
            holder.roomNameTv = (TextView) convertView.findViewById(R.id.room_name_tv);
            holder.appOneTV = (TextView) convertView.findViewById(R.id.one_tv);
            holder.appTwotv = (TextView) convertView.findViewById(R.id.two_tv);
            holder.appThreeTv = (TextView) convertView.findViewById(R.id.three_tv);
            holder.appFourTv = (TextView) convertView.findViewById(R.id.four_tv);
            holder.appFiveTv = (TextView) convertView.findViewById(R.id.five_tv);
            holder.appSixTv = (TextView) convertView.findViewById(R.id.six_tv);
            holder.appSevenTv = (TextView) convertView.findViewById(R.id.seven_tv);
            holder.appEightTv = (TextView) convertView.findViewById(R.id.eight_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.roomNameTv.setText(mGetMacInfoModelArrLst.get(position).getRoomName());
        ArrayList<String> arrLst = mGetMacInfoModelArrLst.get(position).getAppDimmTypeStatusArrLst();
        settingDataFromArrList(arrLst, holder, position);

        holder.appOneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appOneTV);
                mListener.onItemClick(holder.appOneTV, position);
            }
        });

        holder.appTwotv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appTwotv);
                mListener.onItemClick(holder.appTwotv, position);
            }
        });
        holder.appThreeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appThreeTv);
                mListener.onItemClick(holder.appThreeTv, position);
            }
        });
        holder.appFourTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appFourTv);
                mListener.onItemClick(holder.appFourTv, position);
            }
        });
        holder.appFiveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appFiveTv);
                mListener.onItemClick(holder.appFiveTv, position);
            }
        });
        holder.appSixTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appSixTv);
                mListener.onItemClick(holder.appSixTv, position);
            }
        });
        holder.appSevenTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appSevenTv);
                mListener.onItemClick(holder.appSevenTv, position);
            }
        });
        holder.appEightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appEightTv);
                mListener.onItemClick(holder.appEightTv, position);
            }
        });


        return convertView;
    }

    private void startAnimVibrate(TextView tv) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.shake);
        vibrator.vibrate(150);
        tv.startAnimation(animation);
    }

    private void settingDataFromArrList(ArrayList<String> arrLst, ViewHolder holder, int position) {

        for (int i = 0; i < arrLst.size(); i++) {
            switch (i) {
                case 0:
                    setSymbolStatus(holder.appOneTV, arrLst.get(0));
                    break;
                case 1:
                    setSymbolStatus(holder.appTwotv, arrLst.get(1));
                    break;
                case 2:
                    setSymbolStatus(holder.appThreeTv, arrLst.get(2));
                    break;
                case 3:
                    setSymbolStatus(holder.appFourTv, arrLst.get(3));
                    break;
                case 4:
                    setSymbolStatus(holder.appFiveTv, arrLst.get(4));
                    break;
                case 5:
                    setSymbolStatus(holder.appSixTv, arrLst.get(5));
                    break;
                case 6:
                    setSymbolStatus(holder.appSevenTv, arrLst.get(6));
                    break;
                case 7:
                    setSymbolStatus(holder.appEightTv, arrLst.get(7));
                    break;
            }
        }
    }


    private void setSymbolStatus(TextView appOneTV, String s) {
        String status;
        switch (s.charAt(1)) {
            case 'A':
                if (s.charAt(2) == '1') {
                    status = "a";
                } else {
                    status = "b";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'B':
                if (s.charAt(2) == '1') {
                    status = "v";
                } else {
                    status = "u";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'C':
                if (s.charAt(2) == '1') {
                    status = "e";
                } else {
                    status = "f";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'D':
                if (s.charAt(2) == '1') {
                    status = "g";
                } else {
                    status = "h";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'E':
                if (s.charAt(2) == '1') {
                    status = "p";
                } else {
                    status = "o";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'F':
                if (s.charAt(2) == '1') {
                    status = "n";
                } else {
                    status = "m";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'G':
                if (s.charAt(2) == '1') {
                    status = "F";
                } else {
                    status = "E";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'H':
                if (s.charAt(2) == '1') {
                    status = "O";
                } else {
                    status = "N";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'I':
                if (s.charAt(2) == '1') {
                    status = "l";
                } else {
                    status = "k";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'J':
                if (s.charAt(2) == '1') {
                    status = "H";
                } else {
                    status = "K";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'K':
                if (s.charAt(2) == '1') {
                    status = "D";
                } else {
                    status = "C";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'L':
                if (s.charAt(2) == '1') {
                    status = "r";
                } else {
                    status = "q";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case '@':
                setAppStatus(appOneTV, ":", s);
                break;
        }
    }

    /*private void setSymbolStatus(TextView appOneTV, String s) {
        String status;
        switch (s.charAt(1)) {
            case 'A':
                if (s.charAt(2) == '1') {
                    status = "G";
                } else {
                    status = "H";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'B':
                if (s.charAt(2) == '1') {
                    status = "Q";
                } else {
                    status = "P";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'C':
                if (s.charAt(2) == '1') {
                    status = "h";
                } else {
                    status = "f";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'D':
                if (s.charAt(2) == '1') {
                    status = "K";
                } else {
                    status = "a";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'E':
                if (s.charAt(2) == '1') {
                    status = "i";
                } else {
                    status = "j";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'F':
                if (s.charAt(2) == '1') {
                    status = "l";
                } else {
                    status = "x";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'G':
                if (s.charAt(2) == '1') {
                    status = "b";
                } else {
                    status = "d";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'H':
                if (s.charAt(2) == '1') {
                    status = "q";
                } else {
                    status = "p";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'I':
                if (s.charAt(2) == '1') {
                    status = "B";
                } else {
                    status = "C";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'J':
                if (s.charAt(2) == '1') {
                    status = "&";
                } else {
                    status = "%";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'K':
                if (s.charAt(2) == '1') {
                    status = "(";
                } else {
                    status = "'";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case 'L':
                if (s.charAt(2) == '1') {
                    status = "*";
                } else {
                    status = ")";
                }
                setAppStatus(appOneTV, status, s);
                break;
            case '@':
                setAppStatus(appOneTV, ":", s);
                break;
        }
    }*/

    /*private void setAppStatus(TextView appOneTV, String g, String s) {
        appOneTV.setTypeface(tf1);
        appOneTV.setText(g);
        String appStatusStr = String.valueOf(s.charAt(2));

        if (appStatusStr.equals("1")) {
            appOneTV.setTextColor(ContextCompat.getColor(context, R.color.cyan2));
        } else {
            appOneTV.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }
    }*/

    private void setAppStatus(TextView appOneTV, String g, String s) {
        if (s.charAt(1) == '@') {
            appOneTV.setTypeface(tf2);
            appOneTV.setTextSize(45);
            appOneTV.setTextColor(ContextCompat.getColor(context, R.color.gray));
        } else {
            appOneTV.setTypeface(tf1);
            appOneTV.setTextSize(50);
            String appStatusStr = String.valueOf(s.charAt(2));
            if (appStatusStr.equals("1")) {
                appOneTV.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                appOneTV.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }
        }
        appOneTV.setText(g);
    }

    class ViewHolder {
        TextView roomNameTv, appOneTV, appTwotv, appThreeTv,
                appFourTv, appFiveTv, appSixTv, appSevenTv, appEightTv;
    }
}
