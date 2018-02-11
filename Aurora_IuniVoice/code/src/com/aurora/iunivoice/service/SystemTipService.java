package com.aurora.iunivoice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
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
import com.aurora.datauiapi.data.bean.SystemMsg;
import com.aurora.datauiapi.data.bean.SystemMsgHolder;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.MainActivity;
import com.aurora.iunivoice.utils.Globals;

public class SystemTipService extends Service implements INotifiableController {

	public static final String SYSTEM_TIP_SERVICE_ACTION = "com.iunivoice.systemtip.service.action";
	
	private static final String TAG = SystemTipService.class.getSimpleName();
	
	public static boolean isServiceAlive = false;
	private TimerTask timerTask;
	private Timer timer;
	private static final long TIME = 3*60*1000;//每隔多少秒访问一次 这里是每隔三分钟取一次
	private IuniVoiceManager iuniVoiceManager;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		isServiceAlive = true;
		iuniVoiceManager = new IuniVoiceManager(this);
		Log.e(TAG, "onCreate");
		timerTask = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				reqeustMsg();
			}
		};
		timer = new Timer();
		timer.schedule(timerTask, 0, TIME);
	}
	
	private static final int NOTIFICATION_ID = 0x15454;
	private int requestCode  = 0;
	public static final String  FROM_TIP_SERVICE_KEY = "fromTipService";
	private static final String PACKAGE_NAME = "com.aurora.iunivoice";
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			if(msg.what == UPDATE_NOTIFICATION)
			{
				sendNotification();
			}
		};
	};
	
	private static final String SP_NAME = "SYSTEM_TIP_MSG";
	private static final String MSG_ID ="msg_id";
	
	private void saveMsgInfo(String id){
		SharedPreferences sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		sp.edit().putString(MSG_ID, id).commit();
	}
	
	private boolean isContainsId(String id){
		SharedPreferences sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		String localId = sp.getString(MSG_ID, "");
		if(id.equals(localId))
		{
			return true;
		}
		saveMsgInfo(id);
		return false;
	}
	
	private static final int UPDATE_NOTIFICATION = 0X14;
	
	private void reqeustMsg(){
		iuniVoiceManager.getPushMessage(new DataResponse<SystemMsgHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null && value.getReturnCode() == Globals.CODE_SUCCESS)
				{
					if(value.getData() != null)
					{
						ArrayList<SystemMsg> msgs = value.getData().getList();
						if(msgs != null && msgs.size() >0)
						{
							if(!isContainsId(msgs.get(0).getId()) && msgs.get(0).getStatus().equals("0"))
							{
								handler.sendEmptyMessage(UPDATE_NOTIFICATION);
							}
						}
					}
				}
			}
		},1);
	}
	
	private void sendNotification() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> infos = manager.getRunningTasks(50);

		if (infos != null
				&& !infos.get(0).topActivity.getPackageName().equals(
						PACKAGE_NAME)) {
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.putExtra(FROM_TIP_SERVICE_KEY, true);
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
					getString(R.string.app_name), "您有未读消息！",
					weatherManPending);
			n.flags |= Notification.FLAG_AUTO_CANCEL;
			nm.notify(NOTIFICATION_ID, n);
		}
	}
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		isServiceAlive = false;
		timerTask.cancel();
		timer.cancel();
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
