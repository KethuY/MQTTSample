package com.atg.onecontrolv3.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.interfaces.OnItemClickListener2;
import com.atg.onecontrolv3.interfaces.OnLongClickListener;
import com.atg.onecontrolv3.interfaces.OnSeekBarChangeListener;
import com.atg.onecontrolv3.preferances.OneControlPreferences;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.atg.onecontrolv3.helpers.Utils.getDateStr;
import static com.atg.onecontrolv3.helpers.Utils.splitStringTag;

/**
 * Created by Bharath on 07-Sep-17
 */
public class RegularRelayBtnView {
    private static final String TAG = RegularRelayBtnView.class.getSimpleName();
    //private static final String TAG = RegularRelayBtnView.class.getSimpleName();
    private Context context;
    private OnItemClickListener2 mListener;
    private OnLongClickListener mLongListener;
    private OnSeekBarChangeListener mSeekListener;
    private Vibrator vibrator;

    public RegularRelayBtnView(Context context, OnItemClickListener2 mListener, OnSeekBarChangeListener mSeekListener, OnLongClickListener mLongListener) {
        this.context = context;
        this.mListener = mListener;
        this.mLongListener = mLongListener;
        this.mSeekListener = mSeekListener;
    }

    public View relayButton(Activity activity, String mAppDimmTypeStatus, String roomAppPos, int roomId, int last) {

        View v = activity.getLayoutInflater().inflate(R.layout.relay_button_view, null);

        RelativeLayout dimmLevelRl = v.findViewById(R.id.dimm_rl);
        final TextView mainRelayBtnLl = v.findViewById(R.id.tv_relay_btn);
        TextView dimmMinusBtn = v.findViewById(R.id.dimm_minus);
        TextView dimmPlusBtn = v.findViewById(R.id.dimm_plus);
        SeekBar dimmLevelSeekBar = v.findViewById(R.id.dimm_seek_level);
        TextView quickTimerTv = v.findViewById(R.id.quick_timer_tv);
        TextView appTypeTv = v.findViewById(R.id.app_type_tv);
        TextView dateTimeTv = v.findViewById(R.id.date_time_tv);
        LinearLayout lstOpLL = v.findViewById(R.id.lstOp_ll);
        View view = v.findViewById(R.id.view_rb);
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);

        if (last == 1) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }

        mainRelayBtnLl.setTag(roomAppPos);
        dimmLevelSeekBar.setTag(roomAppPos);

        Typeface mUiFontTf = Typeface.createFromAsset(context.getAssets(), "uifont.ttf");
        Typeface mUnTitledFont9 = Typeface.createFromAsset(context.getAssets(), "untitled_font_9.ttf");
        Typeface mOCV3Font = Typeface.createFromAsset(context.getAssets(), "oc_font_v3.ttf");

        mainRelayBtnLl.setTypeface(mOCV3Font);
        dimmMinusBtn.setTypeface(mUiFontTf);
        dimmPlusBtn.setTypeface(mUiFontTf);
        quickTimerTv.setTypeface(mUnTitledFont9);


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

        int dimmLevel = Character.getNumericValue(mAppDimmTypeStatus.charAt(0));
        int appType = mAppDimmTypeStatus.charAt(1);
        if (dimmLevel > 0) {
            appTypeTv.setVisibility(View.GONE);
            dimmLevelRl.setVisibility(View.VISIBLE);
            dimmLevelSeekBar.setMax(4);
            dimmLevelSeekBar.setProgress(dimmLevel - 1);
            //char appType = mAppDimmTypeStatus.charAt(1);
            switch (appType) {
                case 'A':
                    dimmMinusBtn.setText("G");
                    dimmPlusBtn.setText("G");
                    break;
                case 'B':
                    dimmMinusBtn.setText("Q");
                    dimmPlusBtn.setText("Q");
                    break;
            }
        } else {
            dimmLevelRl.setVisibility(View.GONE);
            appTypeTv.setVisibility(View.VISIBLE);
            String appTypeStr = "";
            switch (appType) {
                case 'A':
                    appTypeStr = "FAN";
                    break;
                case 'B':
                    appTypeStr = "LIGHT";
                    break;
                case 'E':
                    appTypeStr = "PLUG";
                    break;
                case 'G':
                    appTypeStr = "WM";
                    break;
                case 'I':
                    appTypeStr = "GEYSER";
                    break;
                case 'F':
                    appTypeStr = "LED";
                    break;
            }
            int appPos[] = Utils.splitStringTag(roomAppPos);
            appTypeTv.setText(appTypeStr + " [ " + appPos[1] + " ]");
        }
        setAppType(mAppDimmTypeStatus, mainRelayBtnLl);

        mainRelayBtnLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                mListener.onItemClick(mainRelayBtnLl);
            }
        });

        mainRelayBtnLl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                vibrator.vibrate(150);
                mLongListener.onLongClick(mainRelayBtnLl);
                return true;
            }
        });

        dimmLevelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //mSeekListener.onProgressChanged(seekBar, i + 1, b);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress();
                mSeekListener.onProgressChanged(seekBar, i + 1);
            }
        });

        return v;
    }

    private void setAppType(String appDimmTypeStatus, TextView mainRelayBtnLl) {
        char appType = appDimmTypeStatus.charAt(1);
        char appStatus = appDimmTypeStatus.charAt(2);
        String symbolTxt;
        switch (appType) {
            case 'A':
                if (appStatus == '1') {
                    symbolTxt = "a";
                } else {
                    symbolTxt = "b";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'B':
                if (appStatus == '1') {
                    symbolTxt = "v";
                } else {
                    symbolTxt = "u";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'E':
                if (appStatus == '1') {
                    symbolTxt = "o";
                } else {
                    symbolTxt = "p";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'G':
                if (appStatus == '1') {
                    symbolTxt = "F";
                } else {
                    symbolTxt = "E";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'I':
                if (appStatus == '1') {
                    symbolTxt = "k";
                } else {
                    symbolTxt = "l";
                }
                setAppSymbolStatus(mainRelayBtnLl, symbolTxt, appStatus);
                break;
            case 'F':
                if (appStatus == '1') {
                    symbolTxt = "m";
                } else {
                    symbolTxt = "n";
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
}
