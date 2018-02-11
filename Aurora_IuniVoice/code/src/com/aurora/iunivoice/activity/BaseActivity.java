package com.aurora.iunivoice.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.interfaces.MessageHandler;
import com.aurora.iunivoice.interfaces.WeakHandler;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ToastUtil;

public abstract class BaseActivity extends AppActivity implements
		INotifiableController, MessageHandler {

	protected final String TAG = getClass().getSimpleName();

	protected IuniVoiceManager mComanager;
	protected WeakHandler mHandler = new WeakHandler(this);

	public abstract void setupViews();

	public void setupAuroraActionBar() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		IuniVoiceApp.getInstance().addActivity(this);
		setBackItemRes(R.drawable.aurora_back_selector);
		mComanager = new IuniVoiceManager(this);
		setupAuroraActionBar();
	}

	public void changeStatusBar() {
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify("aurorablackBG8345", 0, builder.build());
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (AccountUtil.getInstance().isIuniOS()) {
//			changeStatusBar();
		}
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case Globals.NETWORK_ERROR:
			ToastUtil.longToast(R.string.network_exception);
			networkError();
			break;
		case Globals.NO_NETWORK:
			ToastUtil.longToast(R.string.network_not_available);
			noNetwork();
			break;

		default:
			break;
		}
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		Log.e("jadon3", "code  = " + code + "     message = " + message);
		// TODO Auto-generated method stub
		switch (code) {
		case INotifiableController.CODE_UNKNONW_HOST:
		case INotifiableController.CODE_WRONG_DATA_FORMAT:
		case INotifiableController.CODE_REQUEST_TIME_OUT:
		case INotifiableController.CODE_CONNECT_ERROR:
		case INotifiableController.CODE_GENNERAL_IO_ERROR:
		case INotifiableController.CODE_NOT_FOUND_ERROR:
		case INotifiableController.CODE_JSON_PARSER_ERROR:
		case INotifiableController.CODE_JSON_MAPPING_ERROR:
		case INotifiableController.CODE_UNCAUGHT_ERROR:
			mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
			
			break;
		case INotifiableController.CODE_NOT_NETWORK:
			mHandler.sendEmptyMessage(Globals.NO_NETWORK);
			
			break;

		default:
			break;
		}
	}
	
	protected void networkError() {
		
	}

	protected void noNetwork() {
		
	}
	
	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	//	System.out.println(this.getClass().getSimpleName()+"111111111111111111111111111111111");
	   IuniVoiceApp.getInstance().clearActivityByIndex(this.getClass().getSimpleName());
	}
}
