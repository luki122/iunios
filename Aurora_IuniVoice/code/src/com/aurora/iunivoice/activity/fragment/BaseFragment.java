package com.aurora.iunivoice.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.iunivoice.interfaces.MessageHandler;
import com.aurora.iunivoice.interfaces.WeakHandler;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.Log;

public abstract class BaseFragment extends Fragment implements MessageHandler,
		INotifiableController {

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
		 Log.e("BaseFragment", "msg=" + message);
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
		 Log.e("BaseFragment", "the code=" + code + "  msg=" + message);
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
	
}
