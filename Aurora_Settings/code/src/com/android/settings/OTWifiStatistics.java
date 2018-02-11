package com.android.settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.database.Cursor;
public class OTWifiStatistics {
	private static final String TAG="OTWifiStatistics";
    private Context mContext;
    private final IntentFilter filter;
    long tempBeginTime;
    long tempEndTime;
    private static final String URI_STR="content://com.iuni.reporter/module/";
    private static final String MODULE_KEY="module_key";
    private static final String ITEM_TAG="item_tag";
    private static final String VALUE="value";
    private static final String WIFI="wifi";
	private static final Uri URI=Uri.parse(URI_STR);
    private ContentResolver resolver;
	
	public OTWifiStatistics(Context mContext)
	{
		this.mContext=mContext;
		resolver=mContext.getContentResolver();
		filter=new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceive();
	}
	
	public void onStop()
	{
		unregisterReceiver();
	}
	
	private void unregisterReceiver()
	{
		mContext.unregisterReceiver(mReceiver);
	}
	
	private void registerReceive()
	{
		mContext.registerReceiver(mReceiver, filter);
	}
	
	private BroadcastReceiver mReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
			{
				Log.d(TAG, "OTWifiStatistics action="+intent.getAction().toString());
	            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
	                    WifiManager.EXTRA_NETWORK_INFO);
	            if(info.isConnected())
	            {
	            	tempBeginTime=System.currentTimeMillis();
	            }else
	            {
	            	tempEndTime=System.currentTimeMillis();
	            }
	            if(tempBeginTime==0)
	            {
	            	return ;
	            }
	            if(tempEndTime>tempBeginTime)
	            {
	            	long totalMinutes = ((tempEndTime-tempBeginTime) / 1000)/60;
                    if(totalMinutes>10)
                    {
//                         Log.d(TAG," > 30 minutes time -->  totalMinutes="+totalMinutes);
                         update((int) totalMinutes);
                         tempBeginTime=0;
                         tempEndTime=0;
                    }
	            }
			}
		}
	};
	
    private void update(final int i)
    {
    	ContentValues values=new ContentValues();
    	values.put(MODULE_KEY, "setting");
    	values.put(ITEM_TAG, "wifi");
    	values.put(VALUE, i);
    	resolver.update(URI, values, null	, null);
    	
    	Cursor cursor=null;
    	try {
    		cursor=resolver.query(URI, null, getQueryWhere(), getQueryValue(), null, null);
//    		cursor=resolver.query(URI, null, null, null, null, null);
        	if(cursor!=null)
        	{
        		if(cursor.moveToNext())
        		{
        			String module=cursor.getString(cursor.getColumnIndex(MODULE_KEY));
        			String item_tag=cursor.getString(cursor.getColumnIndex(ITEM_TAG));
        			int value=cursor.getInt(cursor.getColumnIndex(VALUE));
        			Log.d(TAG, "  module= "+module+" item_tag="+item_tag+ "  value"+value);
//    				int period = cursor.getInt(cursor.getColumnIndex(VALUE));
        		}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}finally
		{
			if(!cursor.equals(null))
			{
				cursor.close();
			}
		}
    }
    
    private  String getQueryWhere(){
    	return ITEM_TAG+" = ?";
    }
    
    private  String[] getQueryValue(){
    	String[] whereValue = {WIFI};
    	return whereValue;
    }   
	

}
