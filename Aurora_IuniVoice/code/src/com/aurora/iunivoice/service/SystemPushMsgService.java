package com.aurora.iunivoice.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.SystemPushMsg;
import com.aurora.datauiapi.data.bean.SystemPushMsgHolder;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.PageDetailActivity;
import com.aurora.iunivoice.utils.Globals;

public class SystemPushMsgService extends Service implements INotifiableController{

	public static final String SYSTEM_PUSHMSG_SERVICE_ACTION = "com.iunibbs.action.system.pushmsg.service.action";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private static final String PUSH_MSG_ID_KEY = "pushMsgId";
	private static final String PUSH_MSG_TIME_KEY = "pushMsgTime";
	private static final String SAVE_PUSH_MSG_NAME = "pushMsgName";
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		getSystemPushMsg();
		
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			SystemPushMsgHolder value = (SystemPushMsgHolder) msg.obj;
			sendNotification(value.getData().getPush().get(0), value.getFormhash());
		};
	};
	
	private void getSystemPushMsg(){
		IuniVoiceManager iuniVoiceManager = new IuniVoiceManager(this);
		iuniVoiceManager.getSystemPushMsg(new DataResponse<SystemPushMsgHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null && value.getReturnCode() == Globals.CODE_SUCCESS)
				{
					
					if(value.getData().getPush().size() > 0)
					{
						boolean isShowNotification = checkSystemPushMsg(value.getData().getPush().get(0));
						if(isShowNotification)
						{
							Message msg = new Message();
							msg.obj = value;
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
	}
	
	private static int requestCode = 1;
	private static final int NOTIFICATION_ID = 0X45454;
	private void sendNotification(SystemPushMsg msg,String forunHash){
		Intent intent = new Intent(getApplicationContext(),
				PageDetailActivity.class);
		intent.putExtra(PageDetailActivity.PAGE_ID_KEY, msg.getId());
		intent.putExtra(PageDetailActivity.FORM_HASH_KEY, forunHash);
		intent.putExtra(PageDetailActivity.PAGE_TITLE_KEY, msg.getTitle());
		PendingIntent weatherManPending = PendingIntent.getActivity(
				getApplicationContext(), requestCode++, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager nm = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launcher,
				getString(R.string.app_name), System.currentTimeMillis());
		n.sound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		n.setLatestEventInfo(getApplicationContext(),
				getString(R.string.app_name), msg.getTitle(), weatherManPending);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(NOTIFICATION_ID, n);
		stopSelf();
			}
	
	private boolean checkSystemPushMsg(SystemPushMsg msg){
		SharedPreferences sp = getSharedPreferences(SAVE_PUSH_MSG_NAME, Context.MODE_PRIVATE);
		String id = sp.getString(PUSH_MSG_ID_KEY, "");
		String time = sp.getString(PUSH_MSG_TIME_KEY, "");
		
		if(msg.getId().equals(id) && msg.getTime().equals(time))
		{
			return false;
		}
		
		sp.edit().putString(PUSH_MSG_ID_KEY, msg.getId()).putString(PUSH_MSG_TIME_KEY, msg.getTime()).commit();
		
		return true;
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		response.run();
	}
	
}
