package com.android.incallui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;

import com.privacymanage.service.IPrivacyManageService;

public class AuroraPrivacyUtils {
	
	private static final String TAG = "Phone_AuroraPrivacyUtils";
	public static boolean mIsPrivacyMode = false;
	public static boolean mIsServiceConnected = false;
	private static final String SERVICE_ACTION = "com.privacymanage.service.IPrivacyManageService";
	private static final Intent intent = new Intent(SERVICE_ACTION);
	
	private static IPrivacyManageService mPrivacyManSer;
	public static long mCurrentAccountId = 0;
	public static String mCurrentAccountHomePath = null;
	public static int mPrivacyContactsNum = 0;
	
	private static void logs(String str) {
		Log.i(TAG, str);
	}
	
	public static void bindService(Context context) {
		if (!mIsServiceConnected) {
			try {
				context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
				mIsPrivacyMode = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
					
		}
	}
	
	public static void unbindService(Context context) {
		if (mIsServiceConnected) {
			context.unbindService(serviceConnection);
		}
	}
	
	private static void initCurrentAccountId() {
		try {
			if (mPrivacyManSer != null) {
				mCurrentAccountId = 
						mPrivacyManSer.getCurrentAccount(
								"com.android.contacts", 
								"com.android.contacts.activity.AuroraPrivacyContactListActivity")
								.getAccountId();
				mCurrentAccountHomePath = 
						mPrivacyManSer.getCurrentAccount(
								"com.android.contacts", 
								"com.android.contacts.activity.AuroraPrivacyContactListActivity")
								.getHomePath();
				logs("initCurrentAccountId mCurrentAccountId = " + mCurrentAccountId + "  mCurrentAccountHomePath = " + mCurrentAccountHomePath);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException se) {
			se.printStackTrace();
		}
		
	}
	
	public static void setPrivacyNum(Context context, final String calssName, final int number, final long accountId) {
		try {
			if (mPrivacyManSer == null) {
				if (!mIsServiceConnected) {
					context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
				}
			}
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int i = 0;
					while(true) {
						if (mIsServiceConnected) {
							break;
						}
						
						try {
							Thread.sleep(10);
							i++;
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						if (i > 15) {
							break;
						}
					}
					
					logs("mIsServiceConnected = " + mIsServiceConnected);
					if (mPrivacyManSer != null && mIsServiceConnected) {
						try {
							logs("calssName = " + calssName +  "  number = " + number + "  accountId = " + accountId);
							mPrivacyManSer.setPrivacyNum("com.android.contacts", calssName, number, accountId);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void resetPrivacyNumOfAllAccount(Context context, String pkgName, String className) {
	    try {
	    	if (mPrivacyManSer == null) {
				if (!mIsServiceConnected) {
					context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
				}
			} else {
                mPrivacyManSer.resetPrivacyNumOfAllAccount(pkgName, className);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
	}
	
	private static ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			logs("onServiceConnected");
			mIsPrivacyMode = true;
			mIsServiceConnected = true;
			mPrivacyManSer = IPrivacyManageService.Stub.asInterface(service);
			initCurrentAccountId();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			logs("onServiceDisconnected");
			mIsServiceConnected = false;
			mPrivacyManSer = null;
			mCurrentAccountId = 0;
			mCurrentAccountHomePath = null;
			mIsPrivacyMode = false;
			
			killPrivacyActivity();
		}
	};
	
	public static void killPrivacyActivity() {
		try {
			if (InCallApp.mPrivacyActivityList != null) {
				for (Activity ac : InCallApp.mPrivacyActivityList) {
					ac.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static long getCurrentAccountId() {
		return mCurrentAccountId;
	}
	
	 public static boolean isSupportPrivate() {
	    	return true;
	    }
	    
	    public static int[] getPrivateData(Uri uri) {
			Log.v("getPrvateData", " uri = " + uri);
			if(!isSupportPrivate() || uri == null) {
				return null;
			}
			
	        String url = uri.toString();
			
			if (url.startsWith("content://com.android.contacts/contacts/lookup/") || url.startsWith("content://com.android.contacts/contacts/")) {
				long RawContactId = ProviderUtils.queryForRawContactId(InCallApp.getInstance()
						.getContentResolver(), Long.parseLong(uri.getLastPathSegment()));
				Log.v("getPrvateData", " RawContactId= " + RawContactId);
			  	int[] result = new int[3]; 
				result[0] = (int) RawContactId;
				result[1] = 0;
				result[2] = 0;

				Cursor c = InCallApp.getInstance().getContentResolver()
						.query(RawContacts.CONTENT_URI, new String[] { "is_privacy", "call_notification_type" },
								RawContacts._ID + "=" + RawContactId + " and is_privacy > -1 ", null, PRIVATE_SQL_ORDER);
				if (c != null && c.moveToFirst()) {
					result[1] = c.getInt(0);
					result[2] = c.getInt(1);
				}
				Log.v("getPrvateData", " cursor data1 = " + result[1] + " cursor data2 = " + result[2]);
				if (c != null) {
					c.close();
				}
				return result;
			} else if (url.startsWith("content://com.android.contacts/phone_lookup/")) {
				return getPrivateData(uri.getLastPathSegment());
			} else {
				Cursor cursor = null ;
				try {
					cursor = InCallApp.getInstance().getContentResolver()
							.query(uri, new String[] { "raw_contact_id", "is_privacy",
							"call_notification_type" },  " is_privacy > -1 ", null, PRIVATE_SQL_ORDER);
					if (cursor != null && cursor.getCount() > 0) {
						cursor.moveToFirst();
						int[] result = new int[3];
						for (int i = 0; i < 3; i++) {
							result[i] = cursor.getInt(i);
							Log.v("getPrvateData", " cursor data = " + result[i]);
						}
						return result;
					}
					return null;
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
			
			return null;
			

	    }
	    
	    public static int[] getPrivateData(String number) { 
			Log.v("getPrvateData", " number = " + number);
			
			if(!isSupportPrivate() || TextUtils.isEmpty(number)) {
				return null;
			}

			Cursor cursor = InCallApp.getInstance().getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number), 
					new String[]{"raw_contact_id", "is_privacy", "call_notification_type"},
					" is_privacy > -1 ", null, PRIVATE_SQL_ORDER);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
				  	int[] result = new int[3]; 
				   	for(int i=0; i<3; i++) {
			    		result[i] = cursor.getInt(i);
						Log.v("getPrvateData", " cursor data = " + result[i]);
			    	}
					return result;
				}
		    	return null;
			} finally {
				if(cursor != null) {
					cursor.close();
				}
			}    
	    }
	    
	    private static String PRIVATE_SQL_ORDER = "is_privacy DESC LIMIT 0,1";
	    
	    private static final String URL_STR  = "content://com.privacymanage.provider.ConfigProvider";
	    private static final Uri PRIVATE_CONTENT_URI = Uri.parse(URL_STR);      
	    private static final String MSG_NOTIFY_SWITCH = "msg_notify_switch";
	    private static final String MSG_NOTIFY_HINT = "msg_notify_hint";
	    private static final String ACCOUNT_ID = "account_id";  
	    
	    
	    
		public static String getPrivateRingNotificationText(long accountId) {	
			Log.v("getPrivateRingNotificationText", " accountId = " + accountId);
		   	String defaultText = InCallApp.getInstance().getResources().getString(R.string.private_notification_text);
			String[] columns={MSG_NOTIFY_HINT};
			
			Cursor cursor = InCallApp.getInstance().getContentResolver().query(PRIVATE_CONTENT_URI,
					columns,
					getQueryWhere(),
					getQueryValue(accountId),
					null);	 
				
	    	if (cursor != null){
	    		Log.v("getPrivateRingNotificationText", " not null");
	    		if(cursor.moveToFirst()){
	    			defaultText =  cursor.getString(0); 
	        		Log.v("getPrivateRingNotificationText", " defaultText =" + defaultText);
	    		}   
	    		cursor.close();  
	    	}	
			
	    	return defaultText ;
		}
		
		public static String getPrivateHomePath(long accountId) {	
		    final Uri PRIVATE_PATH_URI = Uri.parse("content://com.privacymanage.provider.AccountProvider"); 
			
		   	String path = "";
			String[] columns = {"homePath"};
			
			Cursor cursor = InCallApp.getInstance().getContentResolver().query(PRIVATE_PATH_URI,
					columns,
					getQueryWhere(),
					getQueryValue(accountId),
					null);	 
				
	    	if (cursor != null){
	    		Log.v("getPrivateHomePath", " not null");
	    		if(cursor.moveToFirst()){
	    			path = cursor.getString(0); 
	        		Log.v("getPrivateHomePath", " path =" + path);
	    		}   
	    		cursor.close();  
	    	}	
			
	    	return path ;
		}
	    	
	    private static String getQueryWhere(){
	    	return ACCOUNT_ID+" = ?";
	    }
	    
	    private static String[] getQueryValue(long accountId){
	    	String[] whereValue = {"" + accountId};
	    	return whereValue;
	    }
	    
		public static boolean isPrivateSendSms(long accountId) {
			boolean defaultValue = true;
			String[] columns={MSG_NOTIFY_SWITCH};
			
			Cursor cursor = InCallApp.getInstance().getContentResolver().query(PRIVATE_CONTENT_URI,
					columns,
					getQueryWhere(),
					getQueryValue(accountId),
					null);	 
				
	    	if (cursor != null){
	    		if(cursor.moveToFirst()){
	    			defaultValue =  cursor.getInt(0) > 0;   			
	    		}   
	    		cursor.close();  
	    	}	
			return defaultValue;
		}
	    
	    
}