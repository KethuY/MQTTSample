package com.atg.onecontrolv3.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.activities.BaseActivity;
import com.atg.onecontrolv3.interfaces.OnItemClickListener2;
import com.atg.onecontrolv3.interfaces.OnLongClickListener;
import com.atg.onecontrolv3.interfaces.OnTempChangeListener;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by Bharath on 07-09-2017
 */

public class AcClassView  implements View.OnClickListener {
    private static final String TAG = AcClassView.class.getSimpleName();
    public static int countForACTemp = 16;
    private android.os.Bundle savedInstanceState;
    String modeType = "c";
    char degreeSymbol = (char) 0X00B0;
    private Context context;
    private OnItemClickListener2 mListener;
    private OnTempChangeListener mTempListener;
    private TextView mDegreesTV;
    private OnLongClickListener mLongListener;
    private Vibrator vibrator;


    public AcClassView(Context context, OnItemClickListener2 mListener, OnTempChangeListener mTempListener, OnLongClickListener mLongListener) {
        this.context = context;
        this.mListener = mListener;
        this.mLongListener = mLongListener;
        this.mTempListener = mTempListener;
    }


    public View relayButton(Activity activity, String mAppDimmTypeStatus, String roomAppPos, int last) {
        View v = activity.getLayoutInflater().inflate(R.layout.ac_view, null);
        //v.setTag(singleRoomModel);
        final TextView mainRelayBtnLlAc = v.findViewById(R.id.tv_relay_ac);
        TextView tvTempDownBtn = v.findViewById(R.id.tv_temp_down);
        mDegreesTV = v.findViewById(R.id.tv_temp_display);
        TextView tvTempUpBtn = v.findViewById(R.id.tv_temp_up);
        TextView tvLowBtn = v.findViewById(R.id.tv_low);
        TextView tvMedBtn = v.findViewById(R.id.tv_med);
        TextView tvHighBtn = v.findViewById(R.id.tv_high);
        TextView tvRedirectBtn = v.findViewById(R.id.tv_redirect);
        TextView quickTimerTv = v.findViewById(R.id.quick_timer_tv);
        View view = v.findViewById(R.id.view_ac);
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);


        if (last == 1) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }

        mainRelayBtnLlAc.setTag(roomAppPos);
        tvRedirectBtn.setTag(roomAppPos);
        tvLowBtn.setTag(roomAppPos);
        tvMedBtn.setTag(roomAppPos);
        tvHighBtn.setTag(roomAppPos);
        tvTempUpBtn.setTag(roomAppPos);
        tvTempDownBtn.setTag(roomAppPos);

        char degreeSymbol = (char) 0X00B0;
        mDegreesTV.setText("16" + degreeSymbol);

        Typeface mUiFontTf = Typeface.createFromAsset(context.getAssets(), "uifont.ttf");
        Typeface mUiFontTf9 = Typeface.createFromAsset(context.getAssets(), "untitled_font_9.ttf");
        Typeface mUiFontTf3 = Typeface.createFromAsset(context.getAssets(), "untitled-font-3.ttf");
        Typeface mOCV3Font = Typeface.createFromAsset(context.getAssets(), "oc_font_v3.ttf");


        mainRelayBtnLlAc.setTypeface(mOCV3Font);
        tvTempDownBtn.setTypeface(mUiFontTf3);
        //tvDisplayBtn.setTypeface(mUiFontTf9);
        tvTempUpBtn.setTypeface(mUiFontTf3);
        quickTimerTv.setTypeface(mUiFontTf9);

        //tvLowBtn.setTypeface(mUiFontTf3);
        //tvMedBtn.setTypeface(mUiFontTf3);
        // tvHighBtn.setTypeface(mUiFontTf9);
        tvRedirectBtn.setTypeface(mOCV3Font);

        mainRelayBtnLlAc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mLongListener.onLongClick(mainRelayBtnLlAc);
                return true;
            }
        });


        final char appStatus = mAppDimmTypeStatus.charAt(2);
        if (appStatus == '1') {
            mainRelayBtnLlAc.setText("g");
        } else {
            mainRelayBtnLlAc.setText("h");
            mainRelayBtnLlAc.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        mainRelayBtnLlAc.setOnClickListener(this);
        tvRedirectBtn.setOnClickListener(this);
        tvLowBtn.setOnClickListener(this);
        tvMedBtn.setOnClickListener(this);
        tvHighBtn.setOnClickListener(this);
        tvTempUpBtn.setOnClickListener(this);
        tvTempDownBtn.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_relay_ac:
                vibrator.vibrate(150);
                mListener.onItemClick(view);

                break;
            case R.id.tv_redirect:
                vibrator.vibrate(150);
                mListener.onItemClick(view);

                break;
            case R.id.tv_low:
                vibrator.vibrate(150);
                mListener.onItemClick(view);

                break;
            case R.id.tv_med:
                vibrator.vibrate(150);
                mListener.onItemClick(view);

                break;
            case R.id.tv_high:
                vibrator.vibrate(150);
                mListener.onItemClick(view);

                break;
            case R.id.tv_temp_up:
                vibrator.vibrate(150);
                mTempListener.onTempClickListener(view, getKeyNameBasedOnTemp(countForACTemp, "plus"));
                break;
            case R.id.tv_temp_down:
                vibrator.vibrate(150);
                mTempListener.onTempClickListener(view, getKeyNameBasedOnTemp(countForACTemp, "minus"));
                break;
        }
    }

    private String getKeyNameBasedOnTemp(int countForACTempDup, String type) {
        String name = "";
        if (type.equalsIgnoreCase("plus")) {
            countForACTempDup++;
        } else {
            countForACTempDup--;
        }
        if (countForACTempDup == 16) {
            name = modeType + "16";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp = countForACTempDup;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 17) {
            name = modeType + "17";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 18) {
            name = modeType + "18";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 19) {
            name = modeType + "19";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 20) {
            name = modeType + "20";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 21) {
            name = modeType + "21";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 22) {
            name = modeType + "22";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 23) {
            name = modeType + "23";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 24) {
            name = modeType + "24";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 25) {
            name = modeType + "25";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 26) {
            name = modeType + "26";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 27) {
            name = modeType + "27";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 28) {
            name = modeType + "28";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 29) {
            name = modeType + "29";
            if (type.equalsIgnoreCase("plus")) {
                countForACTemp++;
            } else {
                countForACTemp--;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        } else if (countForACTempDup == 30) {
            name = modeType + "30";
            if (type.equalsIgnoreCase("minus")) {
                countForACTemp--;
            } else {
                countForACTemp = countForACTempDup;
            }
            mDegreesTV.setText(countForACTemp + "" + degreeSymbol + "C");
        }
        return name;
    }
}
