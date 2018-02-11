package com.aurora.voiceassistant.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import com.aurora.voiceassistant.model.Tts;
import com.aurora.voiceassistant.R;

public class CheckAuthorizeAsy extends AsyncTask<String, String, Boolean> {

	private ProgressDialog dialog;
	private Handler myHandler;
	private Tts tts;
	private NetworkInfo netinfo;

	public CheckAuthorizeAsy(Context context, Handler myhandler,Tts tts){
		dialog = new ProgressDialog(context);
		dialog.setTitle(context.getString(R.string.vs_init));
		//dialog.show();
		
		ConnectivityManager conMng = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		netinfo = conMng.getActiveNetworkInfo();
		if(netinfo != null &&  netinfo.isConnected()){
			dialog.show();
		}
		this.myHandler = myhandler;
		this.tts = tts;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		try {
			if(tts != null){
				return tts.init();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return Boolean.FALSE;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		//dialog.cancel();
		if(result.booleanValue()){
			myHandler.sendEmptyMessageDelayed(5, 1000);
		}else{
			myHandler.sendEmptyMessage(6);
		}
	}

	public void stopDialog(){
		if(dialog != null){
			dialog.cancel();
			dialog = null;
		}
	}
	
}
