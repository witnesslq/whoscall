package com.tianlupan.call;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.tianlupan.util.Utils;
import com.tianlupan.whoscall.FloatWindow;

public class PhoneStatReceiver extends BroadcastReceiver{
	
    private static final String TAG = "PhoneStatReceiver";
    
//    private static MyPhoneStateListener phoneListener = new MyPhoneStateListener();
    
    private static boolean incomingFlag = false;
    
    private static String incoming_number = null;

    @Override
    public void onReceive(final Context context, Intent intent) {

            //如果是拨打电话
            if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){                        
                    incomingFlag = false;
                    String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);        
                    Log.i(TAG, "call OUT:"+phoneNumber);                        
            }else{                        
                    //如果是来电
                    TelephonyManager tm = 
                        (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);                        
                    
                    switch (tm.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                            incomingFlag = true;//标识当前是来电
                            incoming_number = intent.getStringExtra("incoming_number");
                            Log.i(TAG, "RINGING :" + incoming_number);

                            if(Utils.isConnected(context) && Utils.getShowOnCall(context)) {
                                RemoteAPI.getPhone(incoming_number, new RemoteAPICallback() {

                                    @Override
                                    public void onResult(final PhoneResult phoneResult) {
                                        Log.i(TAG, "onResult" + phoneResult.toString());

                                        new Handler(context.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                FloatWindow.getInstance().show(context, new PhoneResult(phoneResult.toString()));
                                            }
                                        });

                                    }

                                    @Override
                                    public void onFinish() {
                                        Log.i(TAG, "onFinished");
                                    }
                                });
                            }

                            break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:                                
                            if(incomingFlag){
                                    Log.i(TAG, "incoming ACCEPT :"+ incoming_number);
                            }

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                        FloatWindow.getInstance().closeFloatView();
                                }
                            });


                            break;
                    
                    case TelephonyManager.CALL_STATE_IDLE:                                
                            if(incomingFlag){
                                    Log.i(TAG, "incoming IDLE");                                
                            }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                FloatWindow.getInstance().closeFloatView();
                            }
                        });


                            break;
                    } 
            }
    }
}