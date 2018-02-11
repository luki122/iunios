package com.netmanage.model;

import com.netmanage.interfaces.SendMsgCallBack;
import com.netmanage.utils.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
/**
 * 发送短信
 * @author Administrator
 */
public class SendMsgModel{	
	private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	private Context context;
	private String TAG = SendMsgModel.class.getName();
	private SmsManager sms ;
	private HandlerThread mBackgroundThread;
	private BackgroundHandler mBackgroundHandler;  
    private int PendingIntentCode=0;
    private SendMsgCallBack callBack;
    public static boolean isSendMsgByAuroraWay = true;//是否用aurora自带的方法发短信
	
	private SendMsgModel(){}
	
    public SendMsgModel(Context context,SendMsgCallBack callBack){
    	this();
    	this.context = context;  
    	this.callBack = callBack;
    	sms = SmsManager.getDefault();  
    	PendingIntentCode =0;
        mBackgroundThread = new HandlerThread(TAG+":Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());    
    	registerBroadcast();
    }
    
    public void destroy(){
    	mBackgroundThread.stop();
    	unregisterReceiver(context, sendMessage);
    }
    
    /**
     * 静默发送短信
     * @param phoneNumber 不能为null
     * @param msg 不能为null
     */
    public void sendSMS(String key,String phoneNumber,String msg) {  
        if(phoneNumber == null || msg == null){
        	return ;
        }
    	Message handlerMsg = mBackgroundHandler.obtainMessage();
    	handlerMsg.obj = new SendInfo(key,phoneNumber,msg);
    	mBackgroundHandler.sendMessage(handlerMsg);
    } 
    
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        	if(isSendMsgByAuroraWay){
        		sendMsgByAuroraWay((SendInfo)msg.obj);
        	}else{
        		sendMsgFunc((SendInfo)msg.obj);
        	}     	
	    };
    }

    
/**********************************************************************/    
    /**
     * 用aurora的方法发送短信
     * @param sendInfo
     */
    private void sendMsgByAuroraWay(SendInfo sendInfo){
		if(sms == null || 
				sendInfo == null ){
			return ;
		}				
		try{
		    Log.i(TAG,"执行短信发送操作, MSG= "+sendInfo.msg+", num="+sendInfo.num);		    
		    Intent smsItent = getInstantTextIntent(sendInfo.num,sendInfo.msg,
		    		new ComponentName("com.android.mms",
		    				"com.android.mms.ui.NoConfirmationSendServiceProxy"));	    
		    context.startService(smsItent);		    
		    if(callBack != null){
            	callBack.result(sendInfo.key,sendInfo.num, sendInfo.msg, true);
            }
		}catch(Exception e){
			Log.i(TAG,e.toString());
			e.printStackTrace();
		}	
    }
    
    private Intent getInstantTextIntent(String phoneNumber, String message,
            ComponentName component) {
        final Uri uri = Uri.fromParts(Intent.ACTION_SENDTO, phoneNumber, null);
        Intent intent = new Intent("android.intent.action.RESPOND_VIA_MESSAGE", uri);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra("simId",Utils.getDataEnabledSimCard(context));
        intent.setComponent(component);
        return intent;
    }   
/************************************************************************************/    
    
    /**
     * 用android标准的方法发送短信
     * @param sendInfo
     */
    private void sendMsgFunc(SendInfo sendInfo){
		if(sms == null || 
				sendInfo == null ){
			return ;
		}				
		try{
		    Intent sentIntent = new Intent(SENT_SMS_ACTION);  
		    sentIntent.putExtra("key", sendInfo.key);
		    sentIntent.putExtra("num", sendInfo.num);
		    sentIntent.putExtra("msg", sendInfo.msg);
		    PendingIntent sentPI = PendingIntent.getBroadcast(context, 
		    		PendingIntentCode++, 
		    		sentIntent,
		    		PendingIntent.FLAG_UPDATE_CURRENT);
		    Log.i(TAG,"执行短信发送操作, MSG= "+sendInfo.msg+", num="+sendInfo.num);
		    SmsManager.getDefault().sendTextMessage(sendInfo.num,null,sendInfo.msg,sentPI,null);
		}catch(Exception e){
			Log.i(TAG,e.toString());
			e.printStackTrace();
		}	
    }
    
    private BroadcastReceiver sendMessage = new BroadcastReceiver() {    	  
        @Override  
        public void onReceive(Context context, Intent intent) {
        	String key = null,msg = null,num= null;
        	if(intent != null && intent.getExtras() != null){
        		key = intent.getExtras().getString("key");
            	msg = intent.getExtras().getString("msg");
            	num = intent.getExtras().getString("num");
            	
        	}
            boolean isSucess;
            //判断短信是否发送成功   
            switch (getResultCode()) {  
	        case Activity.RESULT_OK:  
	        	isSucess = true;
		        Log.i(TAG,"短信发送成功, MSG= "+msg+", num="+num);
		        break;
	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	        case SmsManager.RESULT_ERROR_NO_SERVICE:
	        case SmsManager.RESULT_ERROR_NULL_PDU:
	        case SmsManager.RESULT_ERROR_RADIO_OFF:
	        default: 
	        	isSucess = false;
		        Log.i(TAG,"发送失败, MSG= "+msg+", num="+num+",ResultCode="+getResultCode());
		        break;  
	        }  
            
            if(callBack != null){
            	callBack.result(key,num, msg, isSucess);
            }
        }  
    }; 
    
    private void registerBroadcast(){
    	context.registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));  
    }
    
    private void unregisterReceiver(Context context,
    		BroadcastReceiver receiver){
    	if(context == null || 
    			receiver == null){
    		return ;
    	}
    	
    	try{
    		context.unregisterReceiver(receiver);
    	}catch(Exception e){
    		//ignore
    	}  	
    }

    private static class SendInfo {
		public SendInfo(String key,String num,String msg){
			this.key = key;
    		this.num = num;
    		this.msg= msg;
    	}
		String key;
    	String num;
    	String msg;
    }
}
