package com.aurora.community.activity.fragment;

import com.aurora.community.R;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.common.MessageHandler;
import com.aurora.community.common.WeakHandler;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.ToastUtil;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class BaseFragment extends Fragment implements MessageHandler,
		INotifiableController {

	public interface INewsNotifiableController{
		public void onCategoryClickResponse(String tid,String title,String postCount);
		public void onNewsClickResponse();
	}
	
	public INewsNotifiableController mDelegate;
	
	public CommunityManager mComanager;
	protected WeakHandler mHandler = new WeakHandler(this);

	public abstract void setupViews();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setupViews();
	}

	
/*	@Override
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
	}*/

	@Override
	public void onMessage(String message) {
		 Log.e("linp", "msg=" + message);
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// dismissProgressDialog();
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		 Log.e("linp", "the code=" + code + "  msg=" + message);
		// dismissProgressDialog();

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
	
	public void onCategoryClickResponse(String tid,String title,String postCount) {
		if (mDelegate != null)
			mDelegate.onCategoryClickResponse(tid,title,postCount);
	}

	public void onNewsClickResponse() {
		if (mDelegate != null)
			mDelegate.onNewsClickResponse();
	}
	
}
