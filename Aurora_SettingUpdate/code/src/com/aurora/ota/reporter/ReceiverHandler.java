package com.aurora.ota.reporter;

import java.util.HashMap;
import java.util.Map;



import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aurora.ota.database.RepoterManager;


public class ReceiverHandler {
	private static volatile ReceiverHandler mInstance = null;
	private final Map<String, IReceiverHandler> RECEIVER_MAP =
			new HashMap<String, IReceiverHandler>();
	
    private Context mContext;
	private ReceiverHandler(Context context){
		
        mContext = context;
		RECEIVER_MAP.put(Intent.ACTION_BOOT_COMPLETED, new BootComplete());
		RECEIVER_MAP.put(Constants.ACTION_CONNECTION_CHANGED, new ConnectionChange());
		RECEIVER_MAP.put(Constants.ACTION_SHUTDOWN, new Shutdown());
		RECEIVER_MAP.put(Intent.ACTION_DATE_CHANGED, new DateChange());
		
	}
	
	public static synchronized  ReceiverHandler getInstance(Context context){
	    Log.e("luofu", " ReceiverHandler getInstance");
		if(mInstance ==null){
			mInstance = new ReceiverHandler(context);
		}
		return mInstance;
	}
	public void doAction(Intent intent) {
	    Log.e("luofu", " ReceiverHandler doAction");
        String action = intent.getAction();
        if (RECEIVER_MAP.containsKey(action)) {
        	RECEIVER_MAP.get(action).handleIntent(intent,mContext);
        }
    }
	
	private void startService( Context context){
		Intent startServiceIntent = new Intent();
		startServiceIntent.setAction(Constants.ACTION_START_REPORT_SERVICE);
		context.startService(startServiceIntent);
	}
	private class BootComplete implements IReceiverHandler{

		@Override
		public void handleIntent(Intent intent, Context context) {
			// TODO Auto-generated method stub
			RepoterManager.getInstance(context).storeTime(Constants.KEY_START_UP_TIME);
			startService(context);
		}
		
	}
	
	private class Shutdown implements IReceiverHandler{

		@Override
		public void handleIntent(Intent intent, Context context) {
			// TODO Auto-generated method stub
			RepoterManager.getInstance(context).storeTime(Constants.KEY_SHUTDOWN_TIME);
		}
		
	}
	
	private class ConnectionChange implements IReceiverHandler{

		@Override
		public void handleIntent(Intent intent, Context context) {
			// TODO Auto-generated method stub
			startService(context);
		}
		
	}
	
	
	
	   private class DateChange implements IReceiverHandler{

	        @Override
	        public void handleIntent(Intent intent, Context context) {
	            // TODO Auto-generated method stub
	            ReporterItem item = ReporterItem.createItem(context);
	            RepoterManager.getInstance(context).insert(context.getContentResolver(), item);
	        }
	        
	    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
