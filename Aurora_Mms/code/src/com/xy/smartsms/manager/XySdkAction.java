package com.xy.smartsms.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
// Aurora xuyong 2016-03-11 added for bug #20730 start
import android.os.Looper;
// Aurora xuyong 2016-03-11 added for bug #20730 end
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.transaction.SmsMessageSender;
import com.aurora.mms.util.Utils;
// Aurora xuyong 2016-03-11 added for bug #20730 start
import com.xy.smartsms.location.LocationProvider;
// Aurora xuyong 2016-03-11 added for bug #20730 end

import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.StringUtils;

public class XySdkAction extends AbsSdkDoAction{
	 public static final String TAG = "XIAOYUAN";

	 public static int sSelectedSimId = 0;

	 public static void setSelectedSimId(int simId) {
		 sSelectedSimId = simId;
	 }
//	    /**
//	     * open local app by appName
//	     * @param context
//	     * @param appName
//	     * @param extend
//	     */
//	    public void openAppByAppName(Context context, String appName,HashMap<String, String> extend){
//	        try{
//	            PackageManager pm = context.getPackageManager();
//	            Intent it =pm.getLaunchIntentForPackage(appName);
//	            context.startActivity(it);
//	        } catch(Exception e){
//	            android.util.Log.e(TAG, "SmartSmsSdkDoAction.openAppByAppName error: "+e.getMessage());
//	            e.printStackTrace();
//	        }
//	    }

	    @Override 
	    public List<JSONObject> getReceiveMsgByReceiveTime(
	            String phone,
	            long startReceiveTime,
	            long endReceiveTime,
	            int limit){
	        List<JSONObject> jsonList = null;
	        String[] projection = new String[] { "_id", "address", "body", "service_center", "date"};
	        StringBuffer sbSelection = new StringBuffer(" date > ");
	        sbSelection.append(startReceiveTime);
	        sbSelection.append("  and date < ");
	        sbSelection.append(endReceiveTime);
	        //String selection = " date > "+startReceiveTime +"  and date < "+endReceiveTime;
	        String[] selectionArgs = null;
	        if(!StringUtils.isNull(phone)){
	            sbSelection.append(" and address = ? ");
	            selectionArgs = new String[]{phone};
	        }
	        Cursor cusor = null;
	        try{
	            cusor = Constant.getContext().getContentResolver()
	                    .query(Uri.parse("content://sms/inbox") , projection, sbSelection.toString(), selectionArgs,"date desc LIMIT "+limit+" OFFSET 0");
	            if (cusor != null && cusor.getCount() > 0) {
	                jsonList = new ArrayList<JSONObject>();
	                JSONObject smsJson = null;
	                while (cusor.moveToNext()) {
	                    smsJson = new JSONObject();
	                    smsJson.put("msgId", cusor.getString(0));
	                    smsJson.put("phone", cusor.getString(1));
	                    smsJson.put("msg", cusor.getString(2));
	                    smsJson.put("centerNum", cusor.getString(3));
	                    smsJson.put("smsReceiveTime", cusor.getString(4));
	                    jsonList.add(smsJson);
	                }
	            }
	        }catch(JSONException e){
	            e.printStackTrace();
	        }finally{
	            if(cusor != null){
	                cusor.close();
	                cusor = null;
	            }
	        }
	        return jsonList;
	    }

	    @Override
		public void sendSms(Context context, String phoneNum, String sms,
				int simIndex, Map<String, String> params) {
			// TODO Auto-generated method stub
			//发送短信
			final Set<String> recipientsSet = new HashSet<String>();
			recipientsSet.add(phoneNum);
			long threadId = Utils.getOrCreateThreadId(context, recipientsSet, 0l);
			SmsMessageSender smsMessageSender;
			String[] dests = new String[] {"" + phoneNum};
			if (MmsApp.mGnMultiSimMessage) {
				if (sSelectedSimId == -1) {
					Toast.makeText(context, R.string.gn_no_sim_card, Toast.LENGTH_SHORT).show();
					return;
				}
				smsMessageSender = new SmsMessageSender(context, dests,
						sms, threadId, sSelectedSimId, true);
			} else {
				smsMessageSender = new SmsMessageSender(context, dests,
						sms, threadId, true);
			}
			try {
				// This call simply puts the message on a queue and sends a broadcast to start
				// a service to send the message. In queing up the message, however, it does
				// insert the message into the DB.
				smsMessageSender.sendMessage(threadId);
			} catch (Exception e) {
				Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
			}
		}

		@Override
		public void openSms(Context context, String phoneNum,
				Map<String, String> params) {
			// TODO Auto-generated method stub
			//打开短信原文
		}

		@Override
		public String getContactName(Context context, String phoneNum) {
			// TODO Auto-generated method stub
			return null;
		}

		
		@Override
		public void markAsReadForDatabase(Context context, String msgId) {
			// TODO Auto-generated method stub
			//需要开发者实现标记已读
		}

		@Override
		public void deleteMsgForDatabase(Context context, String msgId) {
			// TODO Auto-generated method stub
			//暂不需实现
		}
		
		/**
	     * 重写定位
	     */
	    @Override
        // Aurora xuyong 2016-03-11 modiified for bug #20730 start
	    public void getLocation(final Context context, final Handler handler) {
        // Aurora xuyong 2016-03-11 modiified for bug #20730 end
	        //MapLocation.getLocation(context, handler);
            // Aurora xuyong 2016-03-11 added for bug #20730 start
			try {
			    System.loadLibrary("locSDK6a");
			} catch(UnsatisfiedLinkError error) {
				android.util.Log.e(LocationProvider.TAG, error.toString());
			}
			Handler mainHandler = new Handler(Looper.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					LocationProvider provider = new LocationProvider(context.getApplicationContext(), handler);
					provider.start();
					Log.e(LocationProvider.TAG, "code is " + provider.request());
				}
			});
            // Aurora xuyong 2016-03-11 added for bug #20730 end
	    }
}
