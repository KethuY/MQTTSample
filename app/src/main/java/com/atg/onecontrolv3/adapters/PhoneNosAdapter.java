package com.atg.onecontrolv3.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.ServiceHandler.ServiceHandler;
import com.atg.onecontrolv3.application.AppController;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.PhoneNumbersModel;
import com.atg.onecontrolv3.mqtt.MqttHelper;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by user on 30-Mar-17
 */

public class PhoneNosAdapter extends BaseAdapter implements MqttHelper.responseListener/*MyResultReceiver.Receiver*/ {
    private static final String TAG = PhoneNosAdapter.class.getSimpleName();
    TransparentProgressDialog pd;
    Context context;
    //private MyResultReceiver mReceiver;
    MqttHelper.responseListener mqttListener = null;
    private ArrayList<PhoneNumbersModel> phonenbrArrLst;
    private MqttHelper helper;

    private String mOrderId, mMobileNo;
    private boolean mIsAdd;

    public PhoneNosAdapter(Context context, ArrayList<PhoneNumbersModel> phonenbrArrLst) {
        this.context = context;
        this.phonenbrArrLst = phonenbrArrLst;
        mqttListener = this;
        helper = new MqttHelper(context, mqttListener);
        /*mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);*/
        pd = new TransparentProgressDialog(context, R.drawable.progress);

        for (int i = 0; i < phonenbrArrLst.size(); i++) {
            if (i < 3) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    phonenbrArrLst.get(i).setColor(context.getDrawable(R.drawable.rectangle_et_style_phone));
                } else {
                    phonenbrArrLst.get(i).setColor(ResourcesCompat.getDrawable(context.getResources(), R.drawable.rectangle_et_style_phone, null));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    phonenbrArrLst.get(i).setColor(context.getDrawable(R.drawable.rectangle_et_style_black));
                } else {
                    phonenbrArrLst.get(i).setColor(ResourcesCompat.getDrawable(context.getResources(), R.drawable.rectangle_et_style_black, null));
                }
            }

        }
    }

    @Override
    public int getCount() {
        return phonenbrArrLst.size();
    }

    @Override
    public Object getItem(int position) {
        return phonenbrArrLst.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.phone_nos, parent, false);
            holder = new ViewHolder();
            holder.snoTv = (TextView) convertView.findViewById(R.id.sno_tv);
            holder.mobileNoEt = (EditText) convertView.findViewById(R.id.mobile_no_et);
            holder.saveBtn = (Button) convertView.findViewById(R.id.save_btn);


           /* if (mIntSelectedItemPosition < 3) {
                holder.mobileNoEt.setBackground(context.getDrawable(R.drawable.rectangle_et_style));
            } else {
                holder.mobileNoEt.setBackground(context.getDrawable(R.drawable.rectangle_et_style_black));
            }*/
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String snoC = String.valueOf(position + 1);
        if (snoC.length() < 2) {
            holder.snoTv.setText("0" + snoC);
        } else {
            holder.snoTv.setText(snoC);
        }

        holder.mobileNoEt.setText(phonenbrArrLst.get(position).getMobileNumber());
        holder.mobileNoEt.setBackground(phonenbrArrLst.get(position).getColor());
        holder.mobileNoEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    holder.saveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String mobileNbr = holder.mobileNoEt.getText().toString().trim();
                            if (!TextUtils.isEmpty(mobileNbr)) {
                                if (mobileNbr.length() == 10) {
                                    deleteMobileNumbersRequest(true, mobileNbr, String.valueOf(phonenbrArrLst.get(position).getOrderId()));
                                } else {
                                    Utils.showMessageDialog("Enter valid no", context);
                                }

                            } else if (position > 2) {

                                String deletingMobile = phonenbrArrLst.get(position).getMobileNumber();
                                if (position > 0) {
                                    if (!TextUtils.isEmpty(deletingMobile)) {
                                        deleteMobileNumbersRequest(false, deletingMobile, String.valueOf(phonenbrArrLst.get(position).getOrderId()));
                                    } else {
                                        deleteMobileNumbersRequest(false, "0", String.valueOf(phonenbrArrLst.get(position).getOrderId()));
                                    }

                                } else {
                                    Utils.showMessageDialog("First number is mandatory!", context);
                                }
                            } else {
                                holder.mobileNoEt.setError("Required!");
                                Toast.makeText(context, "First 3 Mobile numbers mandetory", Toast.LENGTH_SHORT).show();
                            }

                            phonenbrArrLst.get(position).setMobileNumber(mobileNbr);
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    holder.saveBtn.setOnClickListener(null);
                }
            }
        });

        return convertView;
    }

    private void deleteMobileNumbersRequest(final boolean isAdd, String mobileNo, String orderId) {
        Log.e(TAG, "orderId:-:" + orderId + "mobileNo:-:" + mobileNo);
        mMobileNo = mobileNo;
        mOrderId = orderId;
        mIsAdd = isAdd;
        pd.show();
        try {
            /*Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MqttConnectionManagerService.class);
            intent.putExtra("receiver", mReceiver);*/
            if (!mobileNo.contains(" ")) {
                if (isAdd) {
                    pd.show();
                    helper.sendMsg("d|0|" + mobileNo + "|" + orderId);
                } else {
                    pd.show();
                    helper.sendMsg("e|0|0|" + orderId);
                }
            } else {
                Utils.showMessageDialog("Mobile number must not contain whitespaces", context);
            }
        } catch (Exception e) {
            pd.dismiss();
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
    }

    /*@Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        String stringBody = resultData.getString("result");
        Log.e(TAG, "stringBody:-:" + stringBody);

        parseMqtt(stringBody);

    }*/

    private void parseMqtt(String stringBody) {
        if (stringBody.contains("Notification:")) {
            String notiStrArr[] = stringBody.split(Pattern.quote(":"));
            pd.dismiss();
            switch (notiStrArr[1]) {
                case "33":
                    Utils.showMessageDialog("Mobile no. added successfully", context);
                    sendVolleyReq();
                    break;
                case "34":
                    Utils.showMessageDialog("Mobile no. adding failed", context);
                    break;
                case "37":
                    Utils.showMessageDialog("Mobile no. deletion successfully", context);
                    sendVolleyReq();
                    break;
                case "38":
                    Utils.showMessageDialog("Mobile no. deletion failed", context);
                    break;
            }
        }
    }

    private void sendVolleyReq() {
        String url;
        String methodName;
        if (mIsAdd) {
            methodName = "SetMacMobileNumbers";
        } else {
            methodName = "DeleteMacMobileNumber";
        }

        url = ServiceHandler.baseUrl + methodName + "?MacId=" + Utils.MAC_ID + "&IMEI=" + Utils.IMEI +
                "&MobileNumber=" + mMobileNo + "&Orderid=" + mOrderId;
        Log.e(TAG, "url:-:" + url);
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "response error : " + error.getMessage());
                Utils.showMessageDialog(error.getMessage(), context);
                Utils.hideProgressDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(strRequest);
    }

    @Override
    public void onMqttResponse(String resStr) {
        parseMqtt(resStr);
    }

    @Override
    public void onMqttFailure() {

    }

    @Override
    public void onDeliveryComplete() {
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    static class ViewHolder {
        TextView snoTv;
        EditText mobileNoEt;
        Button saveBtn;
    }
}
