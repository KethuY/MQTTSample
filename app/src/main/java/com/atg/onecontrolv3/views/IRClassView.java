package com.atg.onecontrolv3.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.Database.DatabaseHelperForTV;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener2;
import com.atg.onecontrolv3.interfaces.OnLongClickListener;
import com.atg.onecontrolv3.models.IRAppliancesModel;
import com.atg.onecontrolv3.models.ZmotesModel;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.atg.onecontrolv3.helpers.Utils.getDateStr;
import static com.atg.onecontrolv3.helpers.Utils.splitStringTag;

/**
 * Created by Bharath on 07-09-2017
 */

public class IRClassView implements View.OnClickListener {
    private static final String TAG = IRClassView.class.getSimpleName();
    private static final String chPlus = "c";
    /*IR ...!*/
    IRAppliancesModel model;
    String secret = "", id = "", zmoteId = "", localIP = "";
    ArrayList<ZmotesModel> zmotesList = new ArrayList<>();
    String uuidFromDB = "";
    DatabaseHelperForTV dbHelper;
    MqttClient client;
    private Context context;
    private OnItemClickListener2 mListener;
    private int mRoomId, mAppliancePos;
    private char mApplianceType;
    private String keyName;
    private OnLongClickListener mLongListener;
    private Vibrator vibrator;

    public IRClassView(Context context, OnItemClickListener2 mListener, OnLongClickListener mLongListener) {
        this.context = context;
        this.mListener = mListener;
        this.mLongListener = mLongListener;
    }

    public View relayButton(Activity activity, String mAppDimmTypeStatus, String roomAppPos, int roomId, int last) {
        View v = activity.getLayoutInflater().inflate(R.layout.tv_view, null);
        final TextView mainRelayBtnLlTv = v.findViewById(R.id.tv_relay_tv);
        TextView tvChUpBtn = v.findViewById(R.id.tv_ch_up);
        TextView tvVolUpBtn = v.findViewById(R.id.tv_vol_up);
        TextView tvMuteBtn = v.findViewById(R.id.tv_mute);
        TextView tvPower = v.findViewById(R.id.tv_power);
        TextView tvChDownBtn = v.findViewById(R.id.tv_ch_down);
        TextView tvVolDownBtn = v.findViewById(R.id.tv_vol_down);
        TextView tvRedirectBtn = v.findViewById(R.id.tv_redirect);
        TextView quickTimerTv = v.findViewById(R.id.quick_timer_tv);
        LinearLayout lstOpLL = v.findViewById(R.id.lst_op_ir_ll);
        TextView dateTimeTv = v.findViewById(R.id.date_time_ir_tv);
        View view = v.findViewById(R.id.view_ir);
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);

        if (last == 1) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }

        Typeface mUiFontTf = Typeface.createFromAsset(context.getAssets(), "uifont.ttf");
        Typeface mUiFontTf9 = Typeface.createFromAsset(context.getAssets(), "untitled_font_9.ttf");
        Typeface mUiFontTf3 = Typeface.createFromAsset(context.getAssets(), "untitled-font-3.ttf");
        Typeface mOCV3Font = Typeface.createFromAsset(context.getAssets(), "oc_font_v3.ttf");

        mainRelayBtnLlTv.setTypeface(mOCV3Font);
        tvChUpBtn.setTypeface(mOCV3Font);
        tvVolUpBtn.setTypeface(mOCV3Font);
        tvMuteBtn.setTypeface(mOCV3Font);
        tvChDownBtn.setTypeface(mOCV3Font);
        tvVolDownBtn.setTypeface(mOCV3Font);
        tvRedirectBtn.setTypeface(mOCV3Font);
        tvPower.setTypeface(mOCV3Font);
        quickTimerTv.setTypeface(mUiFontTf9);

        /*Setting TAG to views..!*/
        mainRelayBtnLlTv.setTag(roomAppPos);
        tvRedirectBtn.setTag(roomAppPos);
        tvChUpBtn.setTag(roomAppPos);
        tvChDownBtn.setTag(roomAppPos);
        tvVolUpBtn.setTag(roomAppPos);
        tvVolDownBtn.setTag(roomAppPos);
        tvMuteBtn.setTag(roomAppPos);
        tvPower.setTag(roomAppPos);

        setAppType(mAppDimmTypeStatus, mainRelayBtnLlTv);

        mainRelayBtnLlTv.setOnClickListener(this);
        tvRedirectBtn.setOnClickListener(this);
        tvChUpBtn.setOnClickListener(this);
        tvChDownBtn.setOnClickListener(this);
        tvVolUpBtn.setOnClickListener(this);
        tvVolDownBtn.setOnClickListener(this);
        tvMuteBtn.setOnClickListener(this);
        tvPower.setOnClickListener(this);

        OneControlPreferences mPreferences = new OneControlPreferences(context);
        int arrI[] = splitStringTag(roomAppPos);
        String lstOperated = mPreferences.getLstOperated("QT" + Utils.MAC_ID + roomId + (arrI[1] + 1));
        if (lstOperated != null && !lstOperated.isEmpty()) {
            lstOpLL.setVisibility(View.VISIBLE);
            if (null != getDateStr(lstOperated) && !getDateStr(lstOperated).isEmpty())
                dateTimeTv.setText(getDateStr(lstOperated));
        } else {
            lstOpLL.setVisibility(View.INVISIBLE);
        }


        mainRelayBtnLlTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mLongListener.onLongClick(mainRelayBtnLlTv);
                return true;
            }
        });

        return v;
    }

    private void setAppType(String appDimmTypeStatus, TextView mainRelayBtnLl) {
        char appType = appDimmTypeStatus.charAt(1);
        char appStatus = appDimmTypeStatus.charAt(2);
        String symbolTxt;
        switch (appType) {
            case 'C':
                if (appStatus == '1') {
                    symbolTxt = "e";
                } else {
                    symbolTxt = "f";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'H':
                if (appStatus == '1') {
                    symbolTxt = "2";
                } else {
                    symbolTxt = "3";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'J':
                if (appStatus == '1') {
                    symbolTxt = "H";
                } else {
                    symbolTxt = "K";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'K':
                if (appStatus == '1') {
                    symbolTxt = "D";
                } else {
                    symbolTxt = "C";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'L':
                if (appStatus == '1') {
                    symbolTxt = "r";
                } else {
                    symbolTxt = "q";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
        }
    }

    private void setAppSymbolStatus(TextView mainRelayBtnLl, String symbolTxt, char appStatus) {
        mainRelayBtnLl.setText(symbolTxt);
        if (appStatus == '0') {
            mainRelayBtnLl.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_relay_tv:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_redirect:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_ch_up:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_ch_down:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_vol_down:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_vol_up:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_mute:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
            case R.id.tv_power:
                vibrator.vibrate(150);
                mListener.onItemClick(view);
                break;
        }
    }
}
