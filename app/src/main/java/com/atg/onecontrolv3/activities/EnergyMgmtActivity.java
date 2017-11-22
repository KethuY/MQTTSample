package com.atg.onecontrolv3.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.EnergyMngmtProvider;
import com.atg.onecontrolv3.models.EnergyMnngmtModel;
import com.atg.onecontrolv3.models.RoomsModel;
import com.atg.onecontrolv3.models.RoomsProvider;
import com.atg.onecontrolv3.preferances.OneControlPreferences;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EnergyMgmtActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = EnergyMgmtActivity.class.getSimpleName();
    View rootView;
    RadioGroup typeRg;
    TextView currentTypeTv;
    TextView mTotalTv;
    LinearLayout startEndDateLl;

    BarChart mChartAppliances, mChartRooms;
    Button btnDay, btnWeek, btnMonth;
    TextView tvDateOK;
    LinearLayout mStartDateLl, mEndDateLl;
    ImageButton mStartDateIb, mEndDateIb;
    EditText tvStartDate, tvEndDate;
    Button btnLeftAppl, btnRightAppl;
    TextView tvAppliances;
    String selectedStartDate = "Start", selectedEndDate = "End";
    boolean isStartDateClicked = false;
    boolean isEndDateClicked = false;
    String buttonStatus = "Day";
    HashMap<String, ArrayList<EnergyMnngmtModel>> mapEMS = new HashMap<>();
    String userMode;
    OneControlPreferences mPreferences;

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String strDate = String.valueOf(dayOfMonth)
                    + "/" + String.valueOf(monthOfYear + 1)
                    + "/" + String.valueOf(year);

            selectedStartDate = strDate;
            selectedEndDate = strDate;
            if (isStartDateClicked) {
                tvStartDate.setText(selectedStartDate);
            }
            if (isEndDateClicked) {
                tvEndDate.setText(selectedEndDate);
            }
        }
    };


    private Activity mActivity;
    private TransparentProgressDialog pd;
    private List<RoomsModel> data = new ArrayList<RoomsModel>();
    private int count = 0;
    private Paint paint;
    private int[] rainbow;

    public EnergyMgmtActivity() {
        // Required empty public constructor
    }

    public static String ConvertJsonDate(String format) {
        String jsondate = format;
        jsondate = jsondate.replace("/Date(", "").replace(")/", "");
        long time = 0;
        try {
            time = Long.parseLong(jsondate);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NUMExcp:-:" + e.getMessage());
        }
        Date d = new Date(time);
        Log.e("Date", "Convertd date is:" + new SimpleDateFormat("dd/MM/yyyy").format(d));

        return new SimpleDateFormat("MM/dd/yyyy").format(d);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_energy_mgmt);
        mActivity = EnergyMgmtActivity.this;

        //rootView = inflater.inflate(R.layout.fragment_energy_mgmt, container, false);
        mPreferences = new OneControlPreferences(mActivity);
        pd = new TransparentProgressDialog(EnergyMgmtActivity.this, R.drawable.progress);//spinner
        Log.e("current date", getCurrentDate() + ", previous date : " + getPreviousDate() + ", current year : " + getCurrentYear());
        initializeViews();
        setToolBar();
        rainbow = EnergyMgmtActivity.this.getResources().getIntArray(R.array.rainbow);


        //userMode = mPreferences.getUserMode();
        Log.e(TAG, "user mode : " + userMode);

        /*if (Utils.isNetworkAvailable(mActivity)) {
            new GetEnergyDataTask().execute();
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }*/

        if (Utils.isNetworkAvailable) {
            new GetRoomsInfoTask().execute();
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }
    }

    private void setToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Energy Monitoring");
        //getSupportActionBar().setSubtitle(MyPreferences.getString(MyPreferences.PrefType.MacName, getApplicationContext()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_energy_mgmt, container, false);
        mPreferences = new OneControlPreferences(mActivity);
        pd = new TransparentProgressDialog(getActivity(), R.drawable.progress);//spinner
        Log.e("current date", getCurrentDate() + ", previous date : " + getPreviousDate() + ", current year : " + getCurrentYear());
        initializeViews();

        userMode = mPreferences.getUserMode();
        Log.e(TAG, "user mode : " + userMode);

        *//*if (Utils.isNetworkAvailable(mActivity)) {
            new GetEnergyDataTask().execute();
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }*//*

        if (Utils.isNetworkAvailable(mActivity)) {
            new GetRoomsInfoTask().execute();
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }

        //setGraph();
//        setRoomWiseGraph();
//        setAllRoomsGraph();
        return rootView;
    }*/


    private void initializeViews() {
        mChartAppliances = (BarChart) findViewById(R.id.chartAppliances);
        mChartRooms = (BarChart) findViewById(R.id.chartRooms);

        btnDay = (Button) findViewById(R.id.btnDay);
        btnWeek = (Button) findViewById(R.id.btnWeek);
        btnMonth = (Button) findViewById(R.id.btnMonth);
        tvDateOK = (TextView) findViewById(R.id.tvDateOK);
        Typeface tf = Typeface.createFromAsset(getAssets(), "untitled-font-13.ttf");
        tvDateOK.setTypeface(tf);
        mTotalTv = (TextView) findViewById(R.id.total_tv);

        tvStartDate = (EditText) findViewById(R.id.tvStartDate);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tvStartDate.setShowSoftInputOnFocus(false);
        }
        tvEndDate = (EditText) findViewById(R.id.tvEndDate);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tvEndDate.setShowSoftInputOnFocus(false);
        }
        mStartDateLl = (LinearLayout) findViewById(R.id.lvStartDate);
        mEndDateLl = (LinearLayout) findViewById(R.id.lvEndDate);

        mStartDateIb = (ImageButton) findViewById(R.id.ibStartDate);
        mEndDateIb = (ImageButton) findViewById(R.id.ibEndDate);

        btnLeftAppl = (Button) findViewById(R.id.btnLeftAppl);
        btnRightAppl = (Button) findViewById(R.id.btnRightAppl);
        tvAppliances = (TextView) findViewById(R.id.tvAppliances);

        typeRg = (RadioGroup) findViewById(R.id.type_rg);
        currentTypeTv = (TextView) findViewById(R.id.current_type_tv);
        startEndDateLl = (LinearLayout) findViewById(R.id.start_end_date_ll);

        btnDay.setOnClickListener(this);
        btnWeek.setOnClickListener(this);
        btnMonth.setOnClickListener(this);
        tvDateOK.setOnClickListener(this);

        tvStartDate.setOnClickListener(this);
        tvEndDate.setOnClickListener(this);

        mStartDateLl.setOnClickListener(this);
        mEndDateLl.setOnClickListener(this);

        mStartDateIb.setOnClickListener(this);
        mEndDateIb.setOnClickListener(this);

        btnLeftAppl.setOnClickListener(this);
        btnRightAppl.setOnClickListener(this);

        typeRg.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvDateOK:
                String fromDate = "", toDate = "";
                String fDate = tvStartDate.getText().toString().trim();
                String tDate = tvEndDate.getText().toString().trim();
                try {
                    SimpleDateFormat srcDF = new SimpleDateFormat("dd/MM/yyyy");
                    Date srcFromDate = srcDF.parse(fDate);
                    Date srcToDate = srcDF.parse(tDate);

                    SimpleDateFormat destDF = new SimpleDateFormat("yyyyMMdd");
                    fromDate = destDF.format(srcFromDate);
                    toDate = destDF.format(srcToDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.e("EMS", "from and to dates : " + fromDate + ", " + toDate);

                try {
                    callEMSDataToServer("Date_Selection", data.get(count).getRoomId(), "", fromDate, toDate, "", "");
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                break;
            case R.id.tvStartDate:
            case R.id.lvStartDate:
            case R.id.ibStartDate:
                isStartDateClicked = true;
                isEndDateClicked = false;
                showDatePicker();
                break;
            case R.id.tvEndDate:
            case R.id.lvEndDate:
            case R.id.ibEndDate:
                isStartDateClicked = false;
                isEndDateClicked = true;
                showDatePicker();
                break;
            case R.id.btnLeftAppl:
                decrement();
                try {
                    if (buttonStatus.equalsIgnoreCase("week")) {
                        String[] datesLeftAppl = getLastWeek().split("\\-");
                        callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), getButtonsStatus(buttonStatus), datesLeftAppl[0], datesLeftAppl[1], getButtonsStatus(buttonStatus), "");
                    } else if (buttonStatus.equalsIgnoreCase("Month")) {
                        callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), "", "", "", getButtonsStatus(buttonStatus), getCurrentYear() + "");
                    } else {
                        callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), getButtonsStatus(buttonStatus), "", "", "", "");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                break;
            case R.id.btnRightAppl:
                increment();
                try {
                    if (buttonStatus.equalsIgnoreCase("week")) {
                        String[] datesRightAppl = getLastWeek().split("\\-");
                        callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), getButtonsStatus(buttonStatus), datesRightAppl[0], datesRightAppl[1], getButtonsStatus(buttonStatus), "");
                    } else if (buttonStatus.equalsIgnoreCase("Month")) {
                        callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), "", "", "", getButtonsStatus(buttonStatus), getCurrentYear() + "");
                    } else {
                        callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), getButtonsStatus(buttonStatus), "", "", "", "");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                break;

        }
    }

    private String getButtonsStatus(String status) {
        String finalStatus = "";
        if (status.equalsIgnoreCase("Day")) {
            finalStatus = getPreviousDate();
        } else if (status.equalsIgnoreCase("Week")) {
            finalStatus = getLastWeek();
        } else if (status.equalsIgnoreCase("Month")) {
            finalStatus = getCurrentMonth() + "";
        }
        return finalStatus;
    }

    private void increment() {
        if (count != data.size() - 1) {
            count++;
            int temp = count;
            tvAppliances.setText(data.get(count).getRoomName() + "(R" + (temp + 1) + ")");
        }
    }

    private void decrement() {
        if (count != 0) {
            count--;
            int temp = count;
            tvAppliances.setText(data.get(count).getRoomName() + "(R" + (temp + 1) + ")");
        }
    }

    private void callEMSDataToServer(String callType, String roomId, String date, String fromDate, String toDate, String month, String year) throws Exception {
        if (Utils.isNetworkAvailable) {
            new GetEnergyDataTask().execute(callType, roomId, date, fromDate, toDate, month, year);
            new GetEnergyDataAllRoomsTask().execute(callType, "0", date, fromDate, toDate, month, year);
        } else {
            Utils.showMessageDialog("No Internet.", mActivity);
        }
    }

    private void setGraph(List<EnergyMnngmtModel> model) {
        // HorizontalBarChart barChart= (HorizontalBarChart) findViewById(R.id.chart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));
        entries.add(new BarEntry(12f, 3));
        entries.add(new BarEntry(18f, 4));
        entries.add(new BarEntry(9f, 5));

        BarDataSet dataset = null;
        try {
            dataset = new BarDataSet(entries, "# of Calls");
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }

        ArrayList<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        /* for create Grouped Bar chart
        ArrayList<BarEntry> group1 = new ArrayList<>();
        group1.add(new BarEntry(4f, 0));
        group1.add(new BarEntry(8f, 1));
        group1.add(new BarEntry(6f, 2));
        group1.add(new BarEntry(12f, 3));
        group1.add(new BarEntry(18f, 4));
        group1.add(new BarEntry(9f, 5));

        ArrayList<BarEntry> group2 = new ArrayList<>();
        group2.add(new BarEntry(6f, 0));
        group2.add(new BarEntry(7f, 1));
        group2.add(new BarEntry(8f, 2));
        group2.add(new BarEntry(12f, 3));
        group2.add(new BarEntry(15f, 4));
        group2.add(new BarEntry(10f, 5));

        BarDataSet barDataSet1 = new BarDataSet(group1, "Group 1");
        //barDataSet1.setColor(Color.rgb(0, 155, 0));
        barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);

        BarDataSet barDataSet2 = new BarDataSet(group2, "Group 2");
        barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);

        ArrayList<BarDataSet> dataset = new ArrayList<>();
        dataset.add(barDataSet1);
        dataset.add(barDataSet2);
        */

        BarData data = new BarData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        mChartAppliances.setData(data);
        mChartAppliances.setDescription("");
        mChartAppliances.animateY(5000);

    }

    private void setRoomWiseGraph(List<EnergyMnngmtModel> model) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        if (model != null) {
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay1()), 0));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay2()), 1));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay3()), 2));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay4()), 3));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay5()), 4));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay6()), 5));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay7()), 6));
            entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay8()), 7));

            BarDataSet dataset = null;
            try {
                dataset = new BarDataSet(entries, "All Appliances for " + tvAppliances.getText().toString());
            } catch (Exception e) {
                Log.e(TAG, "Exception2:-:" + e.getMessage());
            }

            //labels.add(ConvertJsonDate(model.get(i).getUsageTime()));

            /*labels.add("A.C");
            labels.add("Light");
            labels.add("Fan");
            labels.add("Plug");
            labels.add("TV");
            labels.add("Others");
            labels.add("Bulb");
            labels.add("Fan");*/

            labels.add("A1");
            labels.add("A2");
            labels.add("A3");
            labels.add("A4");
            labels.add("A5");
            labels.add("A6");
            labels.add("A7");
            labels.add("A8");

            BarData data = new BarData(labels, dataset);
            /*dataset.setColors(ColorTemplate.COLORFUL_COLORS);*/ //
            dataset.setColor(rainbow[count]);
            //dataset.setColor(mActivity.getResources().getColor(R.color.cyan2));
            dataset.setBarSpacePercent(70f);
            mChartAppliances.setData(data);
            mChartAppliances.setDescription("");
            mChartAppliances.animateY(3000);
            mChartAppliances.setMinimumWidth(15);
            mChartAppliances.getAxisRight().setEnabled(false);
        }

        /*mChartAppliances.setTouchEnabled(true);
        mChartAppliances.setDragEnabled(true);
        mChartAppliances.setScaleEnabled(true);
        mChartAppliances.animateXY(3000, 3000);
        mChartAppliances.setHorizontalScrollBarEnabled(true);
        mChartAppliances.setDoubleTapToZoomEnabled(true);
        mChartAppliances.setDescription("Dépenses");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        ArrayList<IBarDataSet> dataSets = null;

        BarDataSet barDataSet1 = new BarDataSet(entries, "your title");
                dataSets=new ArrayList<>();
        dataSets.add(barDataSet1);

        BarData Data = new BarData(labels,barDataSet1);
        mChartAppliances.setData(Data);*/


        // - X Axis
        /*XAxis xAxis = mChartAppliances.getXAxis();
//        xAxis.setTypeface(tf);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setEnabled(true);
        xAxis.disableGridDashedLine();
        xAxis.setSpaceBetweenLabels(5);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

// - Y Axis
        YAxis leftAxis = mChartAppliances.getAxisLeft();
        leftAxis.removeAllLimitLines();
//        leftAxis.setTypeface(tf);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaxValue(1000f);
        leftAxis.setAxisMinValue(0f); // to set minimum yAxis
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawLimitLinesBehindData(true);
        leftAxis.setDrawGridLines(true);
        mChartAppliances.getAxisRight().setEnabled(false);


//-----------------
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); *//*--- x Axis*//*
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);*/ /*--- x Axis*/
    }

    private void setAllRoomsGraph(List<EnergyMnngmtModel> model) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Double> allRumsTotArrLst = new ArrayList<>();

        if (model != null && model.size() > 0) {
            for (int i = 0; i < model.size(); i++) {
                entries.add(new BarEntry(Float.parseFloat(model.get(i).getAllRoomsTotal()), i));
                labels.add("R" + (i + 1));
                Log.e(TAG, "i :-:" + i);
                allRumsTotArrLst.add(Double.valueOf(model.get(i).getAllRoomsTotal()));
                //labels.add("");
            }
        }
        Log.e(TAG, "allRumsTotArrLst:-:" + allRumsTotArrLst);
        Double tempD = 0d;
        for (int i = 0; i < allRumsTotArrLst.size(); i++) {
            tempD += allRumsTotArrLst.get(i);
        }
        Log.e(TAG, "tempD:-:" + tempD);
        //int finalTotal = (tempD.intValue() / 1000);
        tempD = (tempD / 1000);
        mTotalTv.setText("Total No. of Units: " + String.valueOf(roundTo2Decimals(tempD)));
        /*entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay1()), 0));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay2()), 1));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay3()), 2));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay4()), 3));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay5()), 4));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay6()), 5));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay7()), 6));
        entries.add(new BarEntry(Float.parseFloat(model.get(0).getRelay8()), 7));*/

        BarDataSet dataset = new BarDataSet(entries, "All Rooms");

        //labels.add(ConvertJsonDate(model.get(i).getUsageTime()));

        /*labels.add("R1");
        labels.add("R2");
        labels.add("R3");
        labels.add("R4");
        labels.add("R5");
        labels.add("R6");
        labels.add("R7");
        labels.add("R8");*/

        /*labels.add("Light");
        labels.add("Fan");
        labels.add("Others");
        labels.add("Plug");
        labels.add("Fan");
        labels.add("A.C");
        labels.add("TV");
        labels.add("Bulb");*/

        Log.e(TAG, "labels:-:" + labels);

        BarData data = new BarData(labels, dataset);

        //for (int i = 0; i < model.size(); i++) {
        //paint.setColor(rainbow[i]);
        // Do something with the paint.
        dataset.setColors(rainbow);
        //}

        //dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        //dataset.setColor(mActivity.getResources().getColor(R.color.cyan2));
        dataset.setBarSpacePercent(70f);
        mChartRooms.setData(data);
        mChartRooms.setDescription("");
        mChartRooms.animateY(3000);
        mChartRooms.setMinimumWidth(5);
        mChartRooms.getAxisRight().setEnabled(false);
    }

    //Showing date picker
    private void showDatePicker() {
        DatePickerFragment date = new DatePickerFragment();
        /**
         * Set Up Current Date Into dialog
         */
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);

        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate);
        date.show(getSupportFragmentManager(), "Date Picker");
    }

    /**
     * Getting previous date for day wise show EMS
     */
    private String getPreviousDate() {
        String yesterdayAsString = "";
        try {
            String sDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date date = dateFormat.parse(sDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);
            yesterdayAsString = dateFormat.format(calendar.getTime());
            Log.e("EMS", "date : " + yesterdayAsString);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EMS", "getPreviousDate() : " + e.getMessage());
        }
        return yesterdayAsString;
    }

    private String getCurrentDate() {
        String currentDate = "";
        try {
            Calendar c = Calendar.getInstance();
            System.out.println("Current time => " + c.getTime());

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            currentDate = df.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentDate;
    }

    private int getCurrentDay() {
        int day = 0;
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    private int getCurrentMonth() {
        int month = 0;
        Calendar calendar = Calendar.getInstance();
        month = calendar.get(Calendar.MONTH) + 1;
        return month;
    }

    private int getCurrentYear() {
        int year = 0;
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        return year;
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    private String getLastWeek() {
        String lastWeek = "";
        String sDate = "";
        try {
            //sDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            sDate = new SimpleDateFormat("yyyyMMdd").format(yesterday());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date date = dateFormat.parse(sDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -6);
            lastWeek = dateFormat.format(calendar.getTime());
            Log.e("EMS", "last date : " + lastWeek + "-" + sDate);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EMS", "getPreviousDate() : " + e.getMessage());
        }
        return lastWeek + "-" + sDate;
    }

    private String getLastMonth() {
        String lastMonth = "";
        String sDate = "";
        try {
            //sDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            sDate = new SimpleDateFormat("yyyyMMdd").format(yesterday());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date date = dateFormat.parse(sDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -29);
            lastMonth = dateFormat.format(calendar.getTime());
            Log.e("EMS", "last date : " + lastMonth + "-" + sDate);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EMS", "getPreviousDate() : " + e.getMessage());
        }
        return lastMonth + "-" + sDate;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_go_home, menu);
        return true;
    }*/

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.day_rb:
                currentTypeTv.setVisibility(View.VISIBLE);
                startEndDateLl.setVisibility(View.GONE);
                buttonStatus = "Day";
                try {
                    callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), getPreviousDate(), "", "", "", "");
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                try {
                    currentTypeTv.setText(dateStringSplit(getPreviousDate()));
                } catch (Exception e) {
                    Log.e(TAG, "Exception1:-:" + e.getMessage());
                }
                break;
            case R.id.week_rg:
                currentTypeTv.setVisibility(View.VISIBLE);
                startEndDateLl.setVisibility(View.GONE);
                buttonStatus = "Week";
                String[] dates = getLastWeek().split("\\-");
                Log.e("EMS", "from and to dates : " + dates[0] + ", " + dates[1]);
                try {
                    callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), "", dates[0], dates[1], "", "");
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }
                /*String fromFormatedDate = dateStringSplit(dates[0]);
                String toFormatedDate = dateStringSplit(dates[1]);*/
                try {
                    currentTypeTv.setText("From: " + dateStringSplit(dates[0]) + "      To: " + dateStringSplit(dates[1]));
                } catch (Exception e) {
                    Log.e(TAG, "Exception2:-:" + e.getMessage());
                }
                break;
            case R.id.month_rg:
                currentTypeTv.setVisibility(View.VISIBLE);
                startEndDateLl.setVisibility(View.GONE);
                buttonStatus = "Month";
                try {
                    String[] dates1 = getLastMonth().split("\\-");
                    currentTypeTv.setText("From: " + dateStringSplit(dates1[0]) + "      To: " + dateStringSplit(dates1[1]));
                    callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), "", "", "", getCurrentMonth() + "", getCurrentYear() + "");
                } catch (Exception e) {
                    Log.e(TAG, "Exception3:-:" + e.getMessage());
                }
                break;
            case R.id.custom_rg:
                buttonStatus = "custom";
                currentTypeTv.setVisibility(View.GONE);
                startEndDateLl.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String dateStringSplit(String dateStr) throws Exception {
        String year = dateStr.substring(0, 4);
        String month = dateStr.substring(4, 6);
        String day = dateStr.substring(6, 8);
        return day + "-" + month + "-" + year;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            /*case R.id.action_home:
                Intent intent = new Intent(EnergyMgmtActivity.this, MainActivity.class);
                startActivity(intent);
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    double roundTo2Decimals(double val) {
        DecimalFormat df2 = null;
        try {
            df2 = new DecimalFormat("###.##");
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException:-:" + e.getMessage());
        }
        return Double.valueOf(df2.format(val));
    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment {
        DatePickerDialog.OnDateSetListener ondateSet;
        private int year, month, day;

        public DatePickerFragment() {
        }

        public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
            ondateSet = ondate;
        }

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            year = args.getInt("year");
            month = args.getInt("month");
            day = args.getInt("day");
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), ondateSet, year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            return datePickerDialog;
        }
    }

    //Getting Energy data from server AsyncTask
    private class GetEnergyDataTask extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;
        String callType, roomId, date, fromDate, toDate, month, year;
        List<EnergyMnngmtModel> list;
        private TransparentProgressDialog pd = new TransparentProgressDialog(EnergyMgmtActivity.this, R.drawable.progress);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progress = ProgressDialog.show(RoomActivity.this, null, "Loading...");
        }

        @Override
        protected String doInBackground(String... params) {
            callType = params[0];
            roomId = params[1];
            date = params[2];
            fromDate = params[3];
            toDate = params[4];
            month = params[5];
            year = params[6];

            EnergyMngmtProvider provider = new EnergyMngmtProvider();
            list = provider.GetEMSData(callType, roomId, date, fromDate, toDate, month, year);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) pd.dismiss();
//            progress.dismiss();
            if (list != null && list.size() != 0) {
                for (int i = 0; i < list.size(); i++) {
                    ConvertJsonDate(list.get(i).getUsageTime());
                }
                //setGraph(list);
                setRoomWiseGraph(list);
            } else {
                setRoomWiseGraph(null);
            }
        }
    }

    //Getting Energy data from server AsyncTask
    private class GetEnergyDataAllRoomsTask extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;
        String callType, roomId, date, fromDate, toDate, month, year;
        List<EnergyMnngmtModel> list;
        private TransparentProgressDialog pd = new TransparentProgressDialog(EnergyMgmtActivity.this, R.drawable.progress);
        ;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progress = ProgressDialog.show(RoomActivity.this, null, "Loading...");
        }

        @Override
        protected String doInBackground(String... params) {
            callType = params[0];
            roomId = params[1];
            date = params[2];
            fromDate = params[3];
            toDate = params[4];
            month = params[5];
            year = params[6];

            EnergyMngmtProvider provider = new EnergyMngmtProvider();
            list = provider.GetEMSData(callType, roomId, date, fromDate, toDate, month, year);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) pd.dismiss();
//            progress.dismiss();
            if (list != null && list.size() != 0) {
                for (int i = 0; i < list.size(); i++) {
                    ConvertJsonDate(list.get(i).getUsageTime());
                }
                //setGraph(list);
                setAllRoomsGraph(list);
            }
        }
    }

    //Getting Rooms info from server AsyncTask
    private class GetRoomsInfoTask extends AsyncTask<Void, Void, String> {
        //private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress = ProgressDialog.show(mActivity, null, "Loading...");
            //progressBar.setVisibility(View.VISIBLE);
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            RoomsProvider provider = new RoomsProvider(mActivity);
            data = provider.getRoomsFromServer();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progress.dismiss();
            //progressBar.setVisibility(View.GONE);
            pd.dismiss();
            if (data != null && data.size() != 0) {
                tvAppliances.setText(data.get(0).getRoomName() + "(R1)");
                try {
                    currentTypeTv.setText(dateStringSplit(getPreviousDate()));
                } catch (Exception e) {
                    Log.e(TAG, "Exception1:-:" + e.getMessage());
                }
                //need to check condition here that if date is not match with today, we need to call web method
                //other wise, need to get data from hash map
                try {
                    callEMSDataToServer("Day", data.get(0).getRoomId(), getPreviousDate(), "", "", "", "");
                } catch (Exception e) {
                    Log.e(TAG, "Exception:-:" + e.getMessage());
                }

            }
        }
    }




    /*case R.id.btnDay:
                buttonStatus = "Day";
                btnDay.setBackgroundColor(mActivity.getResources().getColor(R.color.cyan2));
                btnDay.setTextColor(mActivity.getResources().getColor(R.color.white));

                btnWeek.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnWeek.setTextColor(mActivity.getResources().getColor(R.color.cyan2));

                btnMonth.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnMonth.setTextColor(mActivity.getResources().getColor(R.color.cyan2));

                callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), getPreviousDate(), "", "", "", "");
                break;
            case R.id.btnWeek:
                buttonStatus = "Week";
                btnDay.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnDay.setTextColor(mActivity.getResources().getColor(R.color.cyan2));

                btnWeek.setBackgroundColor(mActivity.getResources().getColor(R.color.cyan2));
                btnWeek.setTextColor(mActivity.getResources().getColor(R.color.white));

                btnMonth.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnMonth.setTextColor(mActivity.getResources().getColor(R.color.cyan2));

                String[] dates = getLastWeek().split("\\-");
                Log.e("EMS", "from and to dates : " + dates[0] + ", " + dates[1]);
                callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), "", dates[0], dates[1], "", "");
                break;
            case R.id.btnMonth:
                buttonStatus = "Month";
                btnDay.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnDay.setTextColor(mActivity.getResources().getColor(R.color.cyan2));

                btnWeek.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
                btnWeek.setTextColor(mActivity.getResources().getColor(R.color.cyan2));

                btnMonth.setBackgroundColor(mActivity.getResources().getColor(R.color.cyan2));
                btnMonth.setTextColor(mActivity.getResources().getColor(R.color.white));

                callEMSDataToServer(buttonStatus, data.get(count).getRoomId(), "", "", "", getCurrentMonth() + "", getCurrentYear() + "");
                break;*/
}
