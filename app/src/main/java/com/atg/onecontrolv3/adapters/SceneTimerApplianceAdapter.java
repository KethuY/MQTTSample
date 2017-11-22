package com.atg.onecontrolv3.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.interfaces.OnItemClickListener;
import com.atg.onecontrolv3.models.GetMacInfoModel;

import java.util.ArrayList;

/**
 * Created by Bharath on 11-May-17
 */

public class SceneTimerApplianceAdapter extends BaseAdapter {
    private static final String TAG = SceneTimerApplianceAdapter.class.getSimpleName();
    public int mIntSelectedBtnPos = 0;
    public boolean mIsChecked = false;
    Context context;
    ViewHolder holder;
    OnItemClickListener mListener;
    private Typeface tf1, tf2;
    private Vibrator vibrator;
    private int mIntResItemActiveCount = 0;
    private ArrayList<GetMacInfoModel> getMacInfoArrLst;
    private LayoutInflater inflater;
    private boolean mTouched;

    public SceneTimerApplianceAdapter(Context context, ArrayList<GetMacInfoModel> getMacInfoArrLst, OnItemClickListener mListener) {
        this.context = context;
        this.mListener = mListener;
        this.getMacInfoArrLst = getMacInfoArrLst;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tf1 = Typeface.createFromAsset(context.getAssets(), "oc_font_v3.ttf");
        tf2 = Typeface.createFromAsset(context.getAssets(), "uifont.ttf");
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int getCount() {
        return getMacInfoArrLst.size();
    }

    @Override
    public Object getItem(int i) {
        return getMacInfoArrLst.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.scene_timer_appliance_item, parent, false);
            holder = new ViewHolder();
            holder.LlRoomAll = (LinearLayout) convertView.findViewById(R.id.ll_room_all);
            holder.roomNameTv = (TextView) convertView.findViewById(R.id.room_name_tv);
            holder.appOneTV = (TextView) convertView.findViewById(R.id.one_tv);
            holder.appTwotv = (TextView) convertView.findViewById(R.id.two_tv);
            holder.appThreeTv = (TextView) convertView.findViewById(R.id.three_tv);
            holder.appFourTv = (TextView) convertView.findViewById(R.id.four_tv);
            holder.appFiveTv = (TextView) convertView.findViewById(R.id.five_tv);
            holder.appSixTv = (TextView) convertView.findViewById(R.id.six_tv);
            holder.appSevenTv = (TextView) convertView.findViewById(R.id.seven_tv);
            holder.appEightTv = (TextView) convertView.findViewById(R.id.eight_tv);
            holder.modeOneTv = (TextView) convertView.findViewById(R.id.mode_one_tv);
            holder.modeTwoTv = (TextView) convertView.findViewById(R.id.mode_two_tv);
            holder.modeThreetV = (TextView) convertView.findViewById(R.id.mode_three_tv);
            holder.autoRevokeTv = (TextView) convertView.findViewById(R.id.auto_revoke_tv);
            holder.bottomView = convertView.findViewById(R.id.bottom_view);
            holder.roomCb = (CheckBox) convertView.findViewById(R.id.roomall_cb);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == getMacInfoArrLst.size() - 1) {
            holder.bottomView.setVisibility(View.GONE);
        } else {
            holder.bottomView.setVisibility(View.VISIBLE);
        }

        holder.roomNameTv.setText(getMacInfoArrLst.get(position).getRoomName());
        ArrayList<String> arrLst = getMacInfoArrLst.get(position).getAppDimmTypeStatusArrLst();

        mTouched = false;

        holder.roomCb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouched = true;
                return false;
            }
        });

        holder.roomCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mTouched) {
                    mIsChecked = isChecked;
                    mListener.onItemClick(holder.roomCb, position);

                }
            }
        });


        holder.roomCb.setChecked(getMacInfoArrLst.get(position).isChecked());

        settingDataFromArrList(arrLst, holder, position);

        if (getMacInfoArrLst.get(position).getNumOfRelays() == 6 || getMacInfoArrLst.get(position).getNumOfRelays() == 7) {
            holder.appSevenTv.setVisibility(View.INVISIBLE);
            holder.appEightTv.setVisibility(View.INVISIBLE);
        } else if (getMacInfoArrLst.get(position).getNumOfRelays() == 8 || getMacInfoArrLst.get(position).getNumOfRelays() == 9) {
            holder.appSevenTv.setVisibility(View.VISIBLE);
            holder.appEightTv.setVisibility(View.VISIBLE);
        }

        if (getMacInfoArrLst.get(position).getAutoRevoke().equals("1")) {
            holder.autoRevokeTv.setTextColor(ContextCompat.getColor(context, R.color.cyan2));
        } else {
            holder.autoRevokeTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        holder.appOneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appOneTV);
                mIntSelectedBtnPos = 1;
                Log.e(TAG, "getId:-:" + view.getId());
                mListener.onItemClick(holder.appOneTV, position);
            }
        });

        holder.appTwotv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.appTwotv);
                mIntSelectedBtnPos = 2;
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
        holder.modeOneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.modeOneTv);
                mListener.onItemClick(holder.modeOneTv, position);
            }
        });
        holder.modeTwoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.modeTwoTv);
                mListener.onItemClick(holder.modeTwoTv, position);
            }
        });
        holder.modeThreetV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.modeThreetV);
                mListener.onItemClick(holder.modeThreetV, position);
            }
        });
        holder.autoRevokeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimVibrate(holder.autoRevokeTv);
                mListener.onItemClick(holder.autoRevokeTv, position);
            }
        });

        return convertView;
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

        getMacInfoArrLst.get(position).setActiveCount(mIntResItemActiveCount);
        mIntResItemActiveCount = 0;

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
                mIntResItemActiveCount++;
                appOneTV.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                appOneTV.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }
        }
        appOneTV.setText(g);


    }

    private void startAnimVibrate(TextView tv) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.shake);
        vibrator.vibrate(150);
        tv.startAnimation(animation);
    }

    class ViewHolder {
        LinearLayout LlRoomAll/*, LlAppOne, LlAppTwo, LlAppThree, LlAppFour,
                LlAppFive, LlAppSix, LlAppSeven, LlAppEight*/;
        TextView roomNameTv, roomCountTv, appOneTV, appTwotv, appThreeTv,
                appFourTv, appFiveTv, appSixTv, appSevenTv, appEightTv,
                modeOneTv, modeTwoTv, modeThreetV, autoRevokeTv/*, dimmOneTv, dimmTwoTv*/;
        View bottomView;
        CheckBox roomCb;
        int ref = 0;
    }
}

