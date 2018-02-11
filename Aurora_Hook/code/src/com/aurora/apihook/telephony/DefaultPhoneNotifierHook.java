package com.aurora.apihook.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.telephony.*;

import java.lang.reflect.Method;
import java.util.List;


//aurora add liguangyu 20140727 for reject start
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
//aurora add liguangyu 20140727 for reject end


import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import com.android.internal.telephony.PhoneConstants.State;

public class DefaultPhoneNotifierHook implements Hook {
	
	private static final String TAG="DefaultPhoneNotifierHook";
	
    public void before_notifyPhoneState(MethodHookParam param) {
		Log.e(TAG, "before_notifyPhoneState");
    	if(param != null){
			param.setResult(null);	
		}
		Phone sender =(Phone) param.args[0]; 
		ITelephonyRegistry mr = (ITelephonyRegistry) ClassHelper.getObjectField(param.thisObject, "mRegistry");
    	auroraNotifyPhoneState(param, sender , mr);
    }
    
    //aurora add liguangyu 20140727 for reject start
    public boolean auroraIsBlackNumber(Context context, String number) {
		Log.e(TAG, "auroraIsBlackNumber");
        String prop = SystemProperties.get("ro.aurora.reject.support");
		if(!prop.contains("yes") || TextUtils.isEmpty(number)) {
			return false;
		}
	    Uri black_uri = Uri.parse("content://com.android.contacts/black");
	    
	    String[] BLACK_PROJECTION = new String[] {
	    	"_id",   //唯一标示，递增
	    	"isblack",   // 标记黑白名单（0: 白名单/1:黑名单）
	    	"lable",    //通话记录表中获取的标记String, 或添加黑名单时直接搜搜狗获取的标记
	    	"black_name",  // 黑名单中的名字
	    	"number", //号码
	    	"reject" //标示是否拦截通话，短信（0：不拦截/ 1：拦截通话/2:拦截短信/3同时拦截通话、短信）
	    };
		Cursor cursor = context.getContentResolver().query(black_uri, BLACK_PROJECTION,
		"(reject = '1' OR reject = '3') AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {			
				return true;
			}
	    	return false;
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
    }
    
    public void auroraNotifyPhoneState(MethodHookParam param, Phone sender, ITelephonyRegistry mRegistry) {
      Call ringingCall = sender.getRingingCall();
      String incomingNumber = "";
      if (ringingCall != null && ringingCall.getEarliestConnection() != null){
          incomingNumber = ringingCall.getEarliestConnection().getAddress();
          if(auroraIsBlackNumber(sender.getContext(), incomingNumber)) {
          	  return; 
          }
      }
      try {
//  		  Class<?> c1 = Class.forName("com.android.internal.telephony.DefaultPhoneNotifier");
//  		 Class<?> p1 = Class.forName("com.android.internal.telephony.PhoneConstants.State");
//  		 Method convertCallState = XposedHelpers.findMethodExact(c1, "convertCallState", p1);
//  		convertCallState.invoke(param.thisObject, sender.getState());
//  		Object result = convertCallState.invoke(param.thisObject, sender.getState());
  		mRegistry.notifyCallState(convertCallState(sender.getState()), incomingNumber);
    	  Log.e(TAG, "auroraNotifyPhoneState end");	
    
      } catch (Exception ex) {
    	  ex.printStackTrace();
          // system process is dead
      } 
    }
    //aurora add liguangyu 20140727 for reject end
    
    private static int convertCallState(PhoneConstants.State state) {
        switch (state) {
            case RINGING:
                return TelephonyManager.CALL_STATE_RINGING;
            case OFFHOOK:
                return TelephonyManager.CALL_STATE_OFFHOOK;
            default:
                return TelephonyManager.CALL_STATE_IDLE;
        }
    }
    
}