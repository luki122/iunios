package com.gionee.settings.push;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.android.settings.R;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceGroup;
import android.util.Log;
import android.view.WindowManager;

//import static com.android.settings.NotifyPushSettings.PREF_PUSH_SWITCH;
import static com.android.settings.NotifyPushSettings.ACTION_GPE_REG;
import static com.android.settings.NotifyPushSettings.ACTION_GPE_UNREG;
import static com.android.settings.NotifyPushSettings.EXTRA_PKG_NAME;
import static com.android.settings.NotifyPushSettings.EXTRA_NEW_VALUE;

public class NotifyPushReceiver extends BroadcastReceiver {
	private static final String TAG = "NotifyPushReceiver";
	
	public static final String GN_PUSH_APP_ADDED = "gn.push.app.ADDED";
	public static final String GN_PUSH_APP_REMOVED = "gn.push.app.REMOVED";
	
	// Gionee <liuran> <2013-3-11> add for CR00814184 begin
	// Note:once external app request notify push close master switch when we are background,
	// we need to sync app switch state and refresh our interface
	public static final String GN_PUSH_REFRESH = "gn.push.action.REFRESH";
	// Gionee <liuran> <2013-3-11> add for CR00814184 end

	private final String GN_PUSH_PERM = "com.gionee.cloud.permission.RECEIVE";
	
	
	//-----ATTENTION START----
	//below registration code is provided by GPE,do not modify them
	private final int REG_OK = 1;
	private final int REG_FAIL = 2;
	private final int UNREG_OK = 3;
	private final int UNREG_FAIL = 4;
	private final int REG_SWITCH_OFF = 8;
	
	private final int REG_ING = 11;
	private final int UNREG_ING = 12;
	//-----ATTENTION END----
	//-----ATTENTION START------
	//I really hate to declare any class member or some static stuff,as once onReceive has finished,
	//this receiver will be destroyed,then,our preference for system dialog will be cleaned to null
	//it means dialog will be out of our control,so make it static,we can still control dialog even onReceive has finished
	private static Dialog mDialog;
	//-----ATTENTION END--------
	//
	private static HashMap<String, Integer> mPkgRegTimesMap = new HashMap<String, Integer>();
	private static HashMap<String, Integer> mPkgUnregTimesMap = new HashMap<String, Integer>();
	
	private final int MAX_TRY_TIMES = 3;
	
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        Log.v(TAG,"action: " + action);
        
        if ("com.gionee.cloud.intent.REGISTRATION.COPY".equals(action)) 
        {
        	dispatchGpeMsg(context,intent);
        }
        else if(Intent.ACTION_PACKAGE_ADDED.equals(action) 
        		&& !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
        {
    		Uri uri = intent.getData();
    		String pkgName = uri.getSchemeSpecificPart();
    		
			Intent pgkAdded = new Intent();
			
			pgkAdded.setAction(GN_PUSH_APP_ADDED);
			pgkAdded.putExtra(EXTRA_PKG_NAME, pkgName);
			
			context.sendBroadcast(pgkAdded);
        }
        else if(Intent.ACTION_PACKAGE_REMOVED.equals(action) 
        		&& !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
        {
        	Uri uri = intent.getData();
        	String pkgName = uri.getSchemeSpecificPart();
        
			// GIONEE liuran 2013-1-14 add for CR00763792 start
        	if(isAppToFilter(context, pkgName))
        		return;
        	// GIONEE liuran 2013-1-14 add for CR00763792 end

        	/***************ATTENTION*****************/
        	//once apk has been removed,check permission doesn't make any sense
        	/*
    		final PackageManager pm = context.getPackageManager();
    		boolean isGnPushApp = pm.checkPermission(GN_PUSH_PERM, pkgName) == PackageManager.PERMISSION_GRANTED;
    		*/
			
			ContentResolver cr = context.getContentResolver();
/*			int count = cr.delete(PushApp.CONTENT_URI, PushApp.Column.PACKAGE + "=?", new String [] { pkgName });
			
			//If count is zero,then app removed is not a gionee push app,
			//and we won't fire below broadcast
			if(count != 0)
			{
				Log.v(TAG, "gn push app removed,pkg name: " + pkgName);
				
				Intent pgkRemoved = new Intent();
				
				pgkRemoved.setAction(GN_PUSH_APP_REMOVED);
				pgkRemoved.putExtra(EXTRA_PKG_NAME, pkgName);
				
				context.sendBroadcast(pgkRemoved);
			}
*/
        }
        else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
        {
        	handleConnChange(context, intent);
        }
        else if("com.gionee.action.CLOSE_SYSTEM_DIALOGS".equals(action))
        {
        	if(mDialog != null && mDialog.isShowing())
        	{
        		mDialog.dismiss();
        	}
        }
        else if("gn.push.action.TURN_MAIN_SWITCH_OFF".equals(action))
        {
        	updateMainSwitchDbData(false,context);
        	handleMainSwitchStateChange(false,context);
        	
        	// Gionee <liuran> <2013-3-11> add for CR00814184 begin
        	requestPushCenterRefresh(context);
			// Gionee <liuran> <2013-3-11> add for CR00814184 end
        }
        else if("gn.push.action.TURN_MAIN_SWITCH_ON".equals(action))
        {
        	updateMainSwitchDbData(true,context);
        	
        	// Gionee <liuran> <2013-3-11> add for CR00814184 begin
        	requestPushCenterRefresh(context);
			// Gionee <liuran> <2013-3-11> add for CR00814184 end
        }
        else if("gn.push.action.ALTER_APP_SWITCH".equals(action))
        {
        	handleAppSwitchAlterAction(context,intent);
        	
        	// Gionee <liuran> <2013-5-20> add for CR00814184 begin
        	requestPushCenterRefresh(context);
        	// Gionee <liuran> <2013-5-20> add for CR00814184 end
        }
    }
	
	private void handleAppSwitchAlterAction(Context context,Intent intent)
	{
    	final String pkgName = intent.getStringExtra(EXTRA_PKG_NAME);
    	final int newValue = intent.getIntExtra(EXTRA_NEW_VALUE, 0);
    	
    	Log.v(TAG," app alter pkgName: " + pkgName + " newValue: " + newValue);
    	//update db data
    	ContentResolver CR = context.getContentResolver();
    	
		Cursor CS = CR.query(PushApp.CONTENT_URI, 
				new String []{PushApp.Column.PACKAGE}, 
				PushApp.Column.PACKAGE + "=?", 
				new String []{pkgName}, null);
		
		if(CS != null)
		{
			if(CS.moveToFirst())
			{
				ContentValues cv = new ContentValues();
				cv.put(PushApp.Column.SWITCH, newValue);
				
				CR.update(PushApp.CONTENT_URI, cv, PushApp.Column.PACKAGE + "=?", new String [] { pkgName });
			}
			else
			{
				ContentValues cv = new ContentValues();
				
				cv.put(PushApp.Column.PACKAGE, pkgName);
				cv.put(PushApp.Column.SWITCH, newValue);
				cv.put(PushApp.Column.REGISTER, 0);
				cv.put(PushApp.Column.DLG_SHOW_TIMES, 1);//another control center trun app switch on,as if we have shown dialog
				cv.put(PushApp.Column.SEEN, 1);
				
				CR.insert(PushApp.CONTENT_URI, cv);
			}
			CS.close();
		}
		
		//no matter master switch is closed or not,once app decide to turn itself on
		//master switch gotta be on either
		boolean enable = newValue == 1;
		if(enable)
		{
			updateMainSwitchDbData(true,context);
		}
		
		
		String targetAction = enable ? ACTION_GPE_REG : ACTION_GPE_UNREG;
		
		Intent newIntent = new Intent();
        
		newIntent.setAction(targetAction);
		newIntent.putExtra(EXTRA_PKG_NAME, pkgName);
        
		context.startService(newIntent);
	}
	
	private void handleMainSwitchStateChange(boolean switchIsOn,Context ctx)
	{
		if(!switchIsOn)
		{
			//main switch is off,unreg all app which is still registered
			ContentResolver CR = ctx.getContentResolver();
			// GIONEE liuran 2013-3-8 modify for CR00780326 start
			// Note:only update those pkgs which displayed for user

			Cursor CS = CR.query(PushApp.CONTENT_URI, 
					new String []{PushApp.Column.PACKAGE}, 
					PushApp.Column.SWITCH + "=?" + " AND " + PushApp.Column.SEEN + "=?", 
					new String []{"1","1"}, null);
			// GIONEE liuran 2013-3-8 modify for CR00780326 end
			
			if(CS != null)
			{
				final int N = CS.getCount();
				
				for (int i = 0; i < N; ++i) 
				{
					if(CS.moveToPosition(i))
					{
						String pkgName = CS.getString(0);
						
						Log.v(TAG, "Battery Saver Ask " + pkgName + " to unreg ");
						
						ContentValues CV = new ContentValues();
						CV.put(PushApp.Column.SWITCH, 0);
						CR.update(PushApp.CONTENT_URI, CV, PushApp.Column.PACKAGE + "=?", new String [] { pkgName });
						
						Intent reg = new Intent();
				        
				        reg.setAction(ACTION_GPE_UNREG);
				        reg.putExtra(EXTRA_PKG_NAME, pkgName);
				        
				        ctx.startService(reg);
					}
				}
				
				CS.close();
			}
		}
	}
	
	private void handleConnChange(final Context context, final Intent intent)
	{
		boolean noConn = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if(!noConn)
		{
			final ContentResolver CR = context.getContentResolver();
			Cursor cs = CR.query(PushApp.CONTENT_URI, 
					new String [] {PushApp.Column.PACKAGE,PushApp.Column.SWITCH,PushApp.Column.REGISTER },
					PushApp.Column.SEEN + "!=?", 
					new String [] {"0"}, null);
			
			if(cs != null)
			{
				final int N = cs.getCount();
				
				for (int i = 0; i < N; i++) 
				{
					if(cs.moveToPosition(i))
					{
						final String pkgName  = cs.getString(0);
						
						int switchValue = cs.getInt(1);
						int regValue    = cs.getInt(2);
						
						Log.v(TAG, "pkg: " + pkgName + " switchValue: " + switchValue + " regValue: " + regValue);
						
						//switch off,but unreg failed
						if(switchValue == 0 && regValue == 1)
						{
							//START UNREGISTRATION 
	                		Timer timer = new Timer();
	                		TimerTask tt = new TimerTask() {
	        					@Override
	        					public void run() {
	        						Log.v(TAG, "Continue to Unreg after 10s delay for wating GPE got ready");
	        						Intent unreg = new Intent();
	    					        
	    					        unreg.setAction(ACTION_GPE_UNREG);
	    					        unreg.putExtra(EXTRA_PKG_NAME, pkgName);
	    					        
	    					        context.startService(unreg);
	        					}
	        				};
	        				
	        				timer.schedule(tt, 10000);
						}
						else if(switchValue == 1 && regValue != 1)//switch on,but reg failed
						{
							//START REGISTRATION 
	                		Timer timer = new Timer();
	                		TimerTask tt = new TimerTask() {
	        					@Override
	        					public void run() {
	        						Log.v(TAG, "Continue to reg after 10s delay for wating GPE got ready");
	    							Intent reg = new Intent();
	    					        
	    					        reg.setAction(ACTION_GPE_REG);
	    					        reg.putExtra(EXTRA_PKG_NAME, pkgName);
	    					        
	    					        context.startService(reg);
	        					}
	        				};
	        				
	        				timer.schedule(tt, 10000);
						}
					}
				}
				
				cs.close();
			}
		}
	}
	
	private void dispatchGpeMsg(Context context, Intent intent)
	{
        if (intent.getStringExtra("registration_id") != null) 
        {
        	final String pkgName = intent.getStringExtra("packagename");
        	Log.v(TAG,"reg succeed , pkg: " + pkgName);
        	
			// GIONEE liuran 2013-1-6 add for CR00752305 start
        	if(isAppToFilter(context, pkgName))
        		return;
        	// GIONEE liuran 2013-1-6 add for CR00752305 end
        	
        	ContentValues cv = new ContentValues();
			cv.put(PushApp.Column.REGISTER, 1);
			
	    	ContentResolver cr = context.getContentResolver();
			cr.update(PushApp.CONTENT_URI, cv, PushApp.Column.PACKAGE + "=?", new String [] { pkgName });
			
			mPkgRegTimesMap.remove(pkgName);
        } 
        else if (intent.getStringExtra("cancel_RID") != null) 
        {
        	final String pkgName = intent.getStringExtra("packagename");
        	
        	Log.v(TAG,"unreg succeed , pkg: " + pkgName);
			// GIONEE liuran 2013-1-6 add for CR00752305 start
        	if(isAppToFilter(context, pkgName))
        		return;
        	// GIONEE liuran 2013-1-6 add for CR00752305 end
        	
        	ContentValues cv = new ContentValues();
        	cv.put(PushApp.Column.REGISTER, 0);
        	//unregstration maybe launched by app itself,so turn switch off
        	//we prefer not to change switch value in registration processing -- for CR00696511
        	//cv.put(PushApp.Column.SWITCH, 0); 
			
	    	ContentResolver cr = context.getContentResolver();
			cr.update(PushApp.CONTENT_URI, cv,PushApp.Column.PACKAGE + "=?", new String [] { pkgName } );
			
			mPkgUnregTimesMap.remove(pkgName);
        }
        else if (intent.getStringExtra("error") != null) 
        {
        	handleGpeError(context,intent);
        }
	}
	
	
	private void handleGpeError(final Context ctx,final Intent intent)
	{
    	int errorCode = intent.getIntExtra("resultcode", REG_FAIL);
    	final String pkgName = intent.getStringExtra("packagename");
        
    	Log.v(TAG," GPE error code: " + errorCode + " pkgName: " + pkgName);
        
        switch(errorCode)
        {
        	case REG_FAIL:
        	{
        		if(networkAvailable(ctx))
        		{
        			int times = 0;
        			
        			if(mPkgRegTimesMap.containsKey(pkgName))
        			{	
        				times = mPkgRegTimesMap.get(pkgName);
        			}
        			
        			Log.v(TAG," times tried: " + times);
        			
        			if(times <= MAX_TRY_TIMES)
        			{
        				times++;
        				
                		Timer timer = new Timer();
                		TimerTask tt = new TimerTask() {
        					
        					@Override
        					public void run() {
        						// TODO Auto-generated method stub
        						Log.v(TAG,"Reg Timer Time's up for pkg: " + pkgName );
        						
        						final ContentResolver CR = ctx.getContentResolver();
        						Cursor cs = CR.query(PushApp.CONTENT_URI, 
        								new String [] { PushApp.Column.SWITCH,PushApp.Column.REGISTER}, 
        								PushApp.Column.PACKAGE + "=?", 
        								new String []{pkgName}, null);
        						boolean continueToReg = true;
        						if(cs != null)
        						{
        							if(cs.moveToFirst())
        							{
        								int switchValue = cs.getInt(0);
        								int regValue = cs.getInt(1);
        								
        								Log.v(TAG,"Current switchValue: " + switchValue + " regValue: " + regValue );
        								continueToReg = (switchValue != 0 ) && (regValue != 1);
        							}
        							cs.close();
        						}
        						
        						if(continueToReg)
        						{
            						Log.v(TAG,"Continue to reg!!!"); 
            						
            				        Intent reg = new Intent();
            				        
            				        reg.setAction(ACTION_GPE_REG);
            				        reg.putExtra(EXTRA_PKG_NAME, pkgName);
            				        
            				        ctx.startService(reg);
        						}
        						else
        						{
        							Log.v(TAG,"switch is disabled,we abort this reg task");
        						}
        					}
        				};
        				
        				timer.schedule(tt, getDelayTime(times));
        				
        				mPkgRegTimesMap.put(pkgName, times);
        			}
        		}
				
        		break;
        	}
        		
        	case UNREG_FAIL:
        	{
        		if(networkAvailable(ctx))
        		{
        			int times = 0;
        			
        			if(mPkgUnregTimesMap.containsKey(pkgName))
        			{	
        				times = mPkgUnregTimesMap.get(pkgName);
        			}
        			
        			Log.v(TAG," times tried: " + times);
        			
        			if(times <= MAX_TRY_TIMES)
        			{
        				times++;
        				
        				Timer timer = new Timer();
                		TimerTask tt = new TimerTask() {

        					@Override
        					public void run() {
        						Log.v(TAG,"Unreg Timer Time's up for pkg: " + pkgName );
        						
        						final ContentResolver CR = ctx.getContentResolver();
        						Cursor cs = CR.query(PushApp.CONTENT_URI, 
        								new String [] { PushApp.Column.SWITCH,PushApp.Column.REGISTER}, 
        								PushApp.Column.PACKAGE + "=?", 
        								new String []{pkgName}, null);
        						
        						boolean continueToUnreg = true;
        						if(cs != null)
        						{
        							if(cs.moveToFirst())
        							{
        								int switchValue = cs.getInt(0);
        								int regValue = cs.getInt(1);
        								
        								Log.v(TAG,"Current switchValue: " + switchValue + " regValue: " + regValue );
        								continueToUnreg = (switchValue != 1) && (regValue != 0);
        							}
        							cs.close();
        						}
        						
        						if(continueToUnreg)
        						{
        							Log.v(TAG,"Continue to unreg!!!");
        							
            		        		Intent unreg = new Intent();
            				        
            				        unreg.setAction(ACTION_GPE_UNREG);
            				        unreg.putExtra(EXTRA_PKG_NAME, pkgName);
            				        
            				        ctx.startService(unreg);
        						}
        						else
        						{
        							Log.v(TAG,"switch is enabled,we abort this unreg task");
        						}
        					}
                		};
                		
                		timer.schedule(tt, getDelayTime(times));
                		
                		mPkgUnregTimesMap.put(pkgName, times);
        			}
        		}
        		break;
        	}
        	
        	case REG_SWITCH_OFF:
        		handleAppSwitchOff(ctx, pkgName);
        		break;
        	case REG_ING:
        		Log.v(TAG," GPE is busy doing reg,ignore this reg request");
        		break;
        	case UNREG_ING:
        		Log.v(TAG," GPE is busy doing unreg,ignore this unreg request");
        		break;
    		default:
    			break;
        }
	}
	
	private int getDelayTime(int times)
	{
		//DELAY X^2*1 MIN 
		return (int) Math.pow(times, 2)*60000;
	}
	
    private boolean networkAvailable(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) 
        {
            NetworkInfo networkinfo = cm.getActiveNetworkInfo();
            return networkinfo != null && networkinfo.isAvailable();
        }
        
        return false;
    }

	
	private void handleAppSwitchOff(final Context ctx,final String pkgName)
	{
		final ContentResolver CR = ctx.getContentResolver();
		Cursor cs = CR.query(PushApp.CONTENT_URI, 
				new String [] { PushApp.Column._ID,PushApp.Column.DLG_SHOW_TIMES }, 
				PushApp.Column.PACKAGE + "=?", 
				new String []{pkgName}, null);
		
		if(cs != null)
		{
			if(cs.moveToFirst())
			{
				int times = cs.getInt(1);
				
				if(times == 0)
				{
					ContentValues cv = new ContentValues();
					cv.put(PushApp.Column.DLG_SHOW_TIMES, 1);
					
					long id = cs.getLong(0);
					final Uri uri = ContentUris.withAppendedId(PushApp.CONTENT_URI, id);
					CR.update(uri, cv, null, null);
					
					String aiLabel = getAppLabel(ctx,pkgName);
					String msg = ctx.getString(R.string.notify_push_dlg_message, aiLabel);
					
			    	AuroraAlertDialog.Builder adb = new AuroraAlertDialog.Builder(ctx);
			    	adb.setTitle(R.string.notify_push_dlg_title);
			    	adb.setMessage(msg);
			    	adb.setPositiveButton(R.string.notify_push_dlg_confirm, new OnClickListener() {
						
						public void onClick(DialogInterface di, int arg1) {
							
							//check notify master switch value,if it is off ,turn it on
							/*
							SharedPreferences sp = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE);
							
							boolean masterSwitch = sp.getBoolean(PREF_PUSH_SWITCH, false);
							if(!masterSwitch)
							{
								Editor ed = sp.edit();
								ed.putBoolean(PREF_PUSH_SWITCH, true);
								ed.commit();
							}
							*/
							
							updateMainSwitchDbData(true,ctx);
							
							//update app db value
							ContentValues cv = new ContentValues();
							cv.put(PushApp.Column.SWITCH, 1);
							
							CR.update(uri, cv, null, null);
							
					        Intent intent = new Intent();
					        
					        intent.setAction(ACTION_GPE_REG);
					        intent.putExtra(EXTRA_PKG_NAME, pkgName);
					        
					        ctx.startService(intent);
						}
					});
			    	adb.setNegativeButton(R.string.notify_push_dlg_deny, null);
			    	
			    	mDialog = adb.create();
			    	mDialog.setOnDismissListener(mDlgDismissLsn);
			    	mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);		
			    	mDialog.show();
				}
			}
			else
			{
				//insert record first before show alert dialog 
				ContentValues cv = new ContentValues();
				
				cv.put(PushApp.Column.PACKAGE, pkgName);
				cv.put(PushApp.Column.SWITCH, 0);
				cv.put(PushApp.Column.REGISTER, 0);
				cv.put(PushApp.Column.DLG_SHOW_TIMES, 1);
				cv.put(PushApp.Column.SEEN, 1);
				
				CR.insert(PushApp.CONTENT_URI, cv);
				
				String aiLabel = getAppLabel(ctx,pkgName);
				String msg = ctx.getString(R.string.notify_push_dlg_message, aiLabel);
				
		    	AuroraAlertDialog.Builder adb = new AuroraAlertDialog.Builder(ctx);
		    	adb.setTitle(R.string.notify_push_dlg_title);
		    	adb.setMessage(msg);
		    	adb.setPositiveButton(R.string.notify_push_dlg_confirm, new OnClickListener() {
					
					public void onClick(DialogInterface di, int arg1) {
						
						//check notify master switch value,if it is off ,turn it on
						/*
						SharedPreferences sp = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE);
						
						boolean masterSwitch = sp.getBoolean(PREF_PUSH_SWITCH, false);
						if(!masterSwitch)
						{
							Editor ed = sp.edit();
							ed.putBoolean(PREF_PUSH_SWITCH, true);
							ed.commit();
						}
						*/
						
						updateMainSwitchDbData(true,ctx);
						
						//update app db value
						ContentValues cv = new ContentValues();
						cv.put(PushApp.Column.SWITCH, 1);
						
						CR.update(PushApp.CONTENT_URI, cv, PushApp.Column.PACKAGE + "=?", new String [] { pkgName });
						
				        Intent intent = new Intent();
				        
				        intent.setAction(ACTION_GPE_REG);
				        intent.putExtra(EXTRA_PKG_NAME, pkgName);
				        
				        ctx.startService(intent);
					}
				});
		    	adb.setNegativeButton(R.string.notify_push_dlg_deny, null);
		    	
		    	mDialog = adb.create();
		    	mDialog.setOnDismissListener(mDlgDismissLsn);
		    	mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);		
		    	mDialog.show();
			}
			
			cs.close();
		}
	}
	
    private void updateMainSwitchDbData(boolean newState,Context ctx)
    {
    	//save main switch state in push table as it is a fake app
/*    	ContentResolver CR = ctx.getContentResolver();
		Cursor CS = CR.query(PushApp.CONTENT_URI, 
				new String []{PushApp.Column.SWITCH}, 
				PushApp.Column.PACKAGE + "=?", 
				new String []{PREF_PUSH_SWITCH}, null);
		
		int newValue = newState ? 1 : 0;
		
		if(CS != null)
		{
			if(CS.moveToFirst())
			{
				if(CS.getInt(0) != newValue)
				{
					ContentValues cv = new ContentValues();
					cv.put(PushApp.Column.SWITCH, newValue);
					
					CR.update(PushApp.CONTENT_URI, cv, PushApp.Column.PACKAGE + "=?", new String [] { PREF_PUSH_SWITCH });
				}
			}
			else
			{
				ContentValues cv = new ContentValues();
				
				cv.put(PushApp.Column.PACKAGE, PREF_PUSH_SWITCH);
				cv.put(PushApp.Column.SWITCH, newValue);
				cv.put(PushApp.Column.REGISTER, 1);
				cv.put(PushApp.Column.DLG_SHOW_TIMES, 1);
				cv.put(PushApp.Column.SEEN, 0);
				
				CR.insert(PushApp.CONTENT_URI, cv);
			}
			
			CS.close();
		}
*/  }
	private OnDismissListener mDlgDismissLsn = new  OnDismissListener()
	{
		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			mDialog = null;
		}
	};
	
	private String getAppLabel(Context ctx,String pkgName)
	{
		PackageManager pm = ctx.getPackageManager();
		String aiLabel = null;
		try 
		{
			ApplicationInfo ai = pm.getApplicationInfo(pkgName, 0);
			aiLabel = ai.loadLabel(pm).toString();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return aiLabel;
	}
	
	// GIONEE liuran 2013-1-14 add for CR00763792 start
	// We have loaded a white list in NotifyPushProvider,so if this 
	// app has appeared in this list,we won't show it to user
    private boolean isAppToFilter(Context ctx,String pkgName)
    {
    	boolean rtn = false;
    	
    	ContentResolver cr = ctx.getContentResolver();
		Cursor cs = cr.query(PushApp.CONTENT_URI, 
				new String []{PushApp.Column.SEEN}, 
				PushApp.Column.PACKAGE + "=?", 
				new String []{pkgName}, null);
		if(cs != null)
		{
			if(cs.moveToFirst())
			{
				//a regular record hasn't set SEEN value,so make a null-checking 
				rtn = !cs.isNull(0) && ( cs.getInt(0) == 0);
			}
			cs.close();
		}
		return rtn;
    }
    // GIONEE liuran 2013-1-14 add for CR00763792 end
    
	// Gionee <liuran> <2013-3-11> add for CR00814184 begin
	private void requestPushCenterRefresh(Context ctx)
	{
		//refresh push interface if we are in showing
		Intent refresh = new Intent(GN_PUSH_REFRESH);
		ctx.sendBroadcast(refresh);
	}
	// Gionee <liuran> <2013-3-11> add for CR00814184 end

}
