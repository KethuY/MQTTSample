package com.atg.onecontrolv3.mqtt;

import android.content.Context;
import android.util.Log;

import com.atg.onecontrolv3.helpers.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Bharath on 10-Apr-17
 */

public class MqttHelper {

    private static final String SERVER_URL = "tcp://atghas.com:1883";
    private static final String TAG = "MqttHelper";
    public static String mPublishTopic = "";
    public static String mSubscribeTopic = "";
    public static String mSecurityTopic = "";
    public static String mArmDisArmTopic = "";
    public String strMsg;
    public String finalRes = "";
    public MqttAndroidClient client;
    private boolean mIsConnected;
    private responseListener responseList;
    private Context context;
    private String msg;
    private MqttConnectOptions options;
    private String mUserName = "";//Utils.MACID;
    private String mPassword = "";

    public MqttHelper(Context context, responseListener mResponseListener) {
        Log.e(TAG, "Step1");
        this.context = context;
        responseList = mResponseListener;
        //OneControlPreferences mPreferences = new OneControlPreferences(context);

        mUserName = Utils.MAC_ID;//mPreferences.getMACAddress();
        Log.e(TAG, "UsrNme:-:" + mUserName);

        //Utils.MACID = mPreferences.getMACAddress();
        //String pin = mPreferences.getMACPin(Utils.MACID);

        /*if (null != pin && (!pin.equals("") || pin.length() > 0)) {
            mPassword = pin;
        } else {
            mPassword = Utils.MACPIN;
            Log.e(TAG, "Pwd:-:" + mPassword);
        }*/
        try {
            establishMqttConn();
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.getMessage());
        }

    }

    private void establishMqttConn() {
        try {
            //Toast.makeText(context,"pd.show()",Toast.LENGTH_SHORT).show();
            client = createMqttAndroidClient();
            options = createMqttConnectOptions();
            Log.e(TAG, "Step2");
            if (null != client && null != options) {
                connect(client, options);
            } else {
                Log.e(TAG, "Mqtt client and options null");
            }
        } catch (Exception e) {
            Log.e(TAG, "establish Exception:-:" + e.getMessage());
        }
    }

    private MqttConnectOptions createMqttConnectOptions() {
        //create and return options
        try {
            options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        /*options.setUserName("20f85eeef343");
        options.setPassword("GVFLQRJTAGWENEL".toCharArray());*/
            options.setUserName(Utils.MAC_ID);
            options.setPassword(Utils.MAC_PIN.toCharArray());
            options.setKeepAliveInterval(64827);
            options.setCleanSession(true);
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
        return options;
    }

    private MqttAndroidClient createMqttAndroidClient() {
        //create and return client
        try {
            client = new MqttAndroidClient(context, SERVER_URL, MqttClient.generateClientId(), new MemoryPersistence());
        } catch (Exception e) {
            Log.e(TAG, "Exception:-:" + e.getMessage());
        }
        return client;
    }

    public void disconnect() {
        if (null != client) {
            try {
                client.disconnect();
                client = null;
                mUserName = "";
                mPassword = "";
            } catch (Exception e) {
                Log.e(TAG, "Disconnect Exception:-: Discon" + e.getMessage());
            }
        }
    }

    public void connect(final MqttAndroidClient client, MqttConnectOptions options) {
        Log.e(TAG, "Step3");
        try {
            if (!client.isConnected()) {
                Log.e(TAG, "Step4");
                //Toast.makeText(context,"pd.dismiss",Toast.LENGTH_SHORT).show();
                try {
                    IMqttToken token = null;
                    try {
                        token = client.connect(options);
                    } catch (MqttException e) {
                        Log.e(TAG, "token Exception:-:" + e.getMessage());
                    }
                    //on successful connection, publish or subscribe as usual
                    if (null != token) {
                        Log.e(TAG, "Step6");
                        token.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken iMqttToken) {
                                try {
                                    Log.e(TAG, "onSuccess Called");
                                    Log.e(TAG, "Step11");
                                    mIsConnected = true;
                                    responseList.onMqttResponse("");
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception" + e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                                Log.e(TAG, "onMqttFailure:-:" + throwable.getLocalizedMessage());
                                Log.e(TAG, "Step12");
                                try {
                                    /*if (null != client) {
                                        client.unregisterResources();
                                        client.close();
                                    }*/
                                    mIsConnected = false;
                                    responseList.onMqttFailure();
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception:-:" + e.getMessage());
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "Step7");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "!client Exception:-:" + e.getMessage());
                }
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        Log.e(TAG, "Step8");
                        try {
                            Log.e(TAG, "setCallback Called");
                            mIsConnected = false;
                            responseList.onMqttFailure();
                            //client.connect();
                            establishMqttConn();
                        } catch (Exception e) {
                            Log.e(TAG, "connectionLost Exception" + e.getMessage());
                        }
                    }

                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                        Log.e(TAG, "messageArr:-:" + s);
                        //  receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                        Log.e(TAG, "Step9");
                        Log.e(TAG, "messageArrived Called");
                        try {
                            String results = String.valueOf(mqttMessage);

                            if (results.length() > 0) {
                                Log.e(TAG, "String.valueOf(mqttMessage)" + String.valueOf(mqttMessage));

                                Log.e(TAG, "P" + mPublishTopic);
                                Log.e(TAG, "S" + mSubscribeTopic);
                                Log.e(TAG, "mSecurityTopicMqtt:-:" + mSecurityTopic);
                                Log.e(TAG, "mArmDisArmTopicMqtt:-:" + mArmDisArmTopic);
                                responseList.onMqttResponse(results);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "messageArrived Exception:-:" + e.getMessage());
                        }
                        Log.e(TAG, "Service Stopping!");
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        try {
                            Log.e(TAG, "Step10");
                            Log.e(TAG, "deliveryComplete Called");
                            responseList.onDeliveryComplete();
                        } catch (Exception e) {
                            Log.e(TAG, "Exception" + e.getMessage());
                        }
                    }
                });
            } else {
                Log.e(TAG, "Step5");
            }
        } catch (Exception e) {
            Log.e(TAG, "connect() Exception:-:" + e.getMessage());
        }
    }

    public void sendMsg(String msg) {
        Log.e(TAG, "MqttMsg:-:" + msg);
        Log.e(TAG, "UserNameMqtt:-:" + mUserName);
        Log.e(TAG, "PasswordMqtt:-:" + mPassword);
        Log.e(TAG, "PublishTopicMqtt:-:" + mPublishTopic);
        Log.e(TAG, "SubscribeTopicMqtt:-:" + mSubscribeTopic);
        Log.e(TAG, "mSecurityTopicMqtt:-:" + mSecurityTopic);
        Log.e(TAG, "mArmDisArmTopicMqtt:-:" + mArmDisArmTopic);
        if (mIsConnected) {
            MqttMessage message = new MqttMessage();
            //String msg = "A|3|5";
            message.setPayload(msg.getBytes());
            message.setRetained(false);
            try {
                //String topic2 = mSubscribeTopic;
                String topic2[] = {mSubscribeTopic, mSecurityTopic, mArmDisArmTopic};
                int qos[] = {2, 2, 2};
                client.subscribe(topic2, qos);
                String topic = mPublishTopic;
                client.publish(topic, message);
            } catch (Exception e) {
                Log.e(TAG, "sendMsg1 Exception:-:" + e.getMessage());
            }
        } else {
            establishMqttConn();
            Log.e(TAG, "MqttMsg:-:" + msg);
            Log.e(TAG, "UserNameMqtt:-:" + mUserName);
            Log.e(TAG, "PasswordMqtt:-:" + mPassword);
            Log.e(TAG, "PublishTopicMqtt:-:" + mPublishTopic);
            Log.e(TAG, "SubscribeTopicMqtt:-:" + mSubscribeTopic);
            Log.e(TAG, "mSecurityTopicMqtt:-:" + mSecurityTopic);
            Log.e(TAG, "mArmDisArmTopicMqtt:-:" + mArmDisArmTopic);
            if (mIsConnected) {
                MqttMessage message = new MqttMessage();
                //String msg = "A|3|5";
                message.setPayload(msg.getBytes());
                message.setRetained(false);
                try {
                    //String topic2 = "mobile/20f85eeee95c";
                    //String topic2 = mSubscribeTopic;
                    //client.subscribe(topic2, 2);
                    String topic2[] = {mSubscribeTopic, mSecurityTopic, mArmDisArmTopic};
                    int qos[] = {2, 2, 2};
                    client.subscribe(topic2, qos);
                    //String topic = "gateway/20f85eeee95c";
                    String topic = mPublishTopic;
                    client.publish(topic, message);
                } catch (Exception e) {
                    Log.e(TAG, "sendMsg2 Exception:-:" + e.getMessage());
                }
            } else {
                Log.e(TAG, "mIsConnected false");
                /*establishMqttConn();*/
                if (Utils.isNetworkAvailable) {
                    MqttHelper helper = new MqttHelper(context, responseList);
                }
            }
        }
    }

    public interface responseListener {

        void onMqttResponse(String res);

        void onMqttFailure();

        void onDeliveryComplete();
    }
}
