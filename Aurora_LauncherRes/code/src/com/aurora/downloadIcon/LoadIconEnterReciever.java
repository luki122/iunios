package com.aurora.downloadIcon;

import java.util.ArrayList;
import java.util.List;

import com.aurora.downloadIcon.bean.IconResponseObject;
import com.aurora.downloadIcon.bean.IconResponseProp;
import com.aurora.downloadIcon.mananger.DownloadManager;
import com.aurora.downloadIcon.mananger.ThreadManager;
import com.aurora.downloadIcon.struct.Command;
import com.aurora.downloadIcon.struct.DataResponse;
import com.aurora.downloadIcon.struct.INotifiable;
import com.aurora.downloadIcon.struct.INotifiableController;
import com.aurora.downloadIcon.utils.Log;
import com.aurora.downloadIcon.utils.WifiHelper;
import com.aurora.downloadIcon.utils.WifiHelper.WifiStateException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class LoadIconEnterReciever extends BroadcastReceiver implements
		INotifiableController {
	DownloadManager mDownloadManager = null;
	Context mContext = null;
	public static final String TAG = "LoadIconEnterReciever";
	// notify others appp to update icon action
	private static final String NOTIFY_APPS_TO_UPDATEIICON = "com.aurora.action.pulbicres.update";
	Object initLock = new Object();
	@Override
	public void onReceive(Context context, Intent intent) {
		Boolean isWifiOn = true;
		//Log.i("test", "1:isWifiOn = "+isWifiOn+"    ,action = "+intent.getAction());
		try {
			WifiHelper.assertWifiState(context);
		} catch (WifiStateException e) {
			isWifiOn = false;
		}
		//Log.i("test", "2:isWifiOn = "+isWifiOn+"    ,action = "+intent.getAction());
		if(!isWifiOn){
			return;
		}
		String action = intent.getAction();
		init(context);
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			getIconResponseItems();
		} else if ((WifiManager.WIFI_STATE_CHANGED_ACTION).equals(action)) {
			getIconResponseItems();
		}else if(Intent.ACTION_PACKAGE_ADDED.equals(action)){
			//Toast.makeText(context, action +" is received." , Toast.LENGTH_LONG).show();
			final String packageName = intent.getData().getSchemeSpecificPart();
			final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			if(!replacing){
				getIconResponseItems();
			}
		}else if(NOTIFY_APPS_TO_UPDATEIICON.equals(action)){
			/*ArrayList<String> list = intent.getStringArrayListExtra("updates");
			for(String s : list){
				Log.i("test", s);
			}*/
		}
	}

	private void init(Context context) {
		synchronized (initLock) {
			if (mDownloadManager == null) {
				mContext = context;
				mDownloadManager = ThreadManager.getDownloadManager(this);
			}
			 
		}
	}

	@Override
	public void onWrongConnectionState(int state, INotifiable iNotifiable,
			Command<?> source) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int code, String message, INotifiable iNotifiable) {
		// TODO Auto-generated method stub
		Log.i("test",message);
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	public void getIconResponseItems() {
		//Toast.makeText(mContext, "xxxxxx", Toast.LENGTH_SHORT).show();
		mDownloadManager.getIconResponseProp(
				new DataResponse<IconResponseObject>() {
					@Override
					public void run() {
						super.run();
						Log.i(TAG, "Start get icon data!");
					}
				}, mContext);
	}

	@Override
	public void runOnUI(DataResponse<?> response) {

		//Log.i("DownloadManager", "response.responeType  = "
		//		+ response.responeType);
		if (response.responeType == DownloadManager.GET_ICON_INFO) {
			IconResponseObject iconResponseObject = (IconResponseObject) (response.value);
			if (iconResponseObject != null) {
				List<IconResponseProp> icons = iconResponseObject
						.getIconItems();
				if (icons.size() > 0) {
					mDownloadManager
							.getIcon(new DataResponse<IconResponseObject>() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									super.run();
								}
							}, iconResponseObject,
									(IconResponseObject) (response.tempValue),
									mContext);
				} else {
					Log.i(TAG, "no new icon to update on GET_ICON_INFO step.");
				}
			}
		} else if (response.responeType == DownloadManager.GET_ICON) {
			IconResponseObject iconResponseObject = (IconResponseObject) (response.value);
			if (iconResponseObject != null
					&& iconResponseObject.getIconItems() != null) {
				//Log.i("test", "runOnUIiconResponseObject.toJson() : "
				//		+ iconResponseObject.toJson());
				ArrayList<String> value = new ArrayList<String>();
				for (IconResponseProp prop : iconResponseObject.getIconItems()) {
					if (prop.getPackageName() == null
							|| "".equals(prop.getPackageName().trim()))
						continue;
					String item = prop.getPackageName();
					if (prop.getClassName() != null
							&& !"".equals(prop.getClassName().trim())) {
						item += "$" + prop.getClassName();
					}
					value.add(item);
				}
				if (value.size() > 0) {
					Intent intent = new Intent(NOTIFY_APPS_TO_UPDATEIICON);
					intent.putStringArrayListExtra("updates", value);
					mContext.sendBroadcast(intent);
				} else {
					Log.i(TAG, "no new icon to update on GET_ICON step.");
				}
			}
		}

	}

}
