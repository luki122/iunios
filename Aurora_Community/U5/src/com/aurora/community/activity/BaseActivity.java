package com.aurora.community.activity;
import com.aurora.utils.SystemUtils;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.common.MessageHandler;
import com.aurora.community.common.WeakHandler;
import com.aurora.community.utils.AccountUtil;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ToastUtil;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.tencent.mm.sdk.platformtools.Log;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends AppActivity implements INotifiableController,MessageHandler{
	
	protected final String TAG = getClass().getSimpleName();
	
	protected CommunityManager mComanager;
	protected WeakHandler mHandler = new WeakHandler(this);
	public abstract void setupViews();
	
	public void setupAuroraActionBar(){
		
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setBackItemRes(R.drawable.aurora_back_selector);
		setupAuroraActionBar();
                SystemUtils.setStatusBarBackgroundTransparent(this);

	}
	public void changeStatusBar(){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("aurorablackBG8345", 0, builder.build());
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(AccountUtil.getInstance().isIuniOS())
		{
		   //changeStatusBar();
                   SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, this);

		}
	}

	
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case Globals.NETWORK_ERROR:
			ToastUtil.longToast(R.string.network_exception);
			break;
		case Globals.NO_NETWORK:
			ToastUtil.longToast(R.string.network_not_available);
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
		Log.e("jadon3", "code  = "+code+"     message = "+message);
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

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}
	
}
