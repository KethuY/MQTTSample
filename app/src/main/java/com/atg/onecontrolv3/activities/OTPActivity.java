package com.atg.onecontrolv3.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atg.onecontrolv3.R;
import com.atg.onecontrolv3.helpers.TransparentProgressDialog;
import com.atg.onecontrolv3.helpers.Utils;
import com.atg.onecontrolv3.models.PassCodeModel;
import com.atg.onecontrolv3.models.RegistrationProvider;

import java.util.List;

public class OTPActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = OTPActivity.class.getSimpleName();
    LinearLayout llMobile, llOTP;
    Button btnSubmitMobile, btnSubmitOTP;
    EditText etMobileNumber, etOTP;
    TextView tvResend, tvTerms;
    List<PassCodeModel> list;
    String strOTP;
    private TransparentProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        initializeViews();
    }

    private void initializeViews() {
        llMobile = (LinearLayout) findViewById(R.id.llMobileNumber);
        llOTP = (LinearLayout) findViewById(R.id.llOtp);
        btnSubmitMobile = (Button) findViewById(R.id.btnSubmitMobile);
        btnSubmitOTP = (Button) findViewById(R.id.btnSubmitOTP);
        etMobileNumber = (EditText) findViewById(R.id.etMobileNumber);
        etOTP = (EditText) findViewById(R.id.etOTP);
        tvResend = (TextView) findViewById(R.id.tvResend);
        tvTerms = (TextView) findViewById(R.id.tvTerms);

        SpannableString ss = new SpannableString("By registering, you agree to our Terms & Conditions");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://onecontrol.in/"));
                startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 33, 51, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTerms.setText(ss);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        //tvTerms.setHighlightColor(getResources().getColor(R.color.cyan2));

        btnSubmitMobile.setOnClickListener(this);
        btnSubmitOTP.setOnClickListener(this);
        tvResend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String strMobileNumber = etMobileNumber.getText().toString();
        String strOTPUserEntry = etOTP.getText().toString();
//        strMobileNumber = strMobileNumber.replace("+91 ", "");

        switch (view.getId()) {
            case R.id.tvResend:
                if (Utils.isNetworkAvailable) {
                    new GetOTPFromServer().execute(strMobileNumber);
                } else {
                    Utils.showMessageDialog("No Internet.", this);
                }
                break;
            case R.id.btnSubmitMobile:
                //Temperory

                if (strMobileNumber.length() == 0 || strMobileNumber.length() != 10) {
                    etMobileNumber.setError("Enter valid mobiile number");
                } else {
                    if (Utils.isNetworkAvailable) {
                        new GetOTPFromServer().execute(strMobileNumber);
                    } else {
                        Utils.showMessageDialog("No Internet.", this);
                    }
                }

                /*llMobile.setVisibility(View.GONE);
                llOTP.setVisibility(View.VISIBLE);
                strOTP = list.get(0).getCodeValue();
                etOTP.setText(strOTP);*/

                break;
            case R.id.btnSubmitOTP:
                if (strOTPUserEntry.length() == 0) {
                    etOTP.setError("Enter code");
                } else if (!strOTP.equalsIgnoreCase(strOTPUserEntry.trim())) {
                    etOTP.setError("Enter valid code");
                } else {
                    Intent intent = new Intent(OTPActivity.this, RegisterActivity.class);//RegisterActivity
                    intent.putExtra("OTP", strOTP);
                    intent.putExtra("MOBILE", strMobileNumber);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    //Generating OTP
    private class GetOTPFromServer extends AsyncTask<String, Void, String> {
        //private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new TransparentProgressDialog(OTPActivity.this, R.drawable.progress);
            pd.show();
        }


        @Override
        protected String doInBackground(String... params) {
            RegistrationProvider provider = new RegistrationProvider();
            list = provider.serviceCreatePassCode(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (list != null && list.size() != 0) {
                llMobile.setVisibility(View.GONE);
                llOTP.setVisibility(View.VISIBLE);
                strOTP = list.get(0).getCodeValue();
                Log.e(TAG, "strOTP:-:" + strOTP);
//                etOTP.setText(strOTP);
            }
        }
    }
}
