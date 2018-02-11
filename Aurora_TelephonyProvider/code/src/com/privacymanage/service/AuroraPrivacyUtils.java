package com.privacymanage.service;

import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.util.Log;
import gionee.provider.GnTelephony.Mms;

public class AuroraPrivacyUtils {
    
    private static boolean mIsServiceConnected = false;
    private static final String SERVICE_ACTION = "com.privacymanage.service.IPrivacyManageService";
    
    private static final String PACKAGENAME = "com.android.mms";
    private static final String CLASSNAME = "com.aurora.mms.ui.AuroraPrivConvActivity";
    
    private static IPrivacyManageService mPrivacyManSer;
    public static Context sContext;
    
    
    public static void bindService(Context context) {
        if (context == null) {
            return;
        }
        sContext = context.getApplicationContext();
        Intent intent = new Intent(SERVICE_ACTION);
        if (!mIsServiceConnected) {
            boolean c = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
        }
    }
    
    public static void resetPrivacyNumOfAllAccount() {
        if (mPrivacyManSer == null) {
            bindService(sContext);
        }
        if (null != mPrivacyManSer) {
            try {
                mPrivacyManSer.resetPrivacyNumOfAllAccount(PACKAGENAME, CLASSNAME);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static void setPrivacy(int num, long accountId) {
        if (mPrivacyManSer == null) {
            bindService(sContext);
        }
        if (null != mPrivacyManSer) {
            Log.e("com.privacymanage.service.PrivacyManageService", "step here!!!!");
            try {
                mPrivacyManSer.setPrivacyNum(PACKAGENAME, CLASSNAME, num, accountId);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                Log.e("com.privacymanage.service.PrivacyManageService", e.toString());
                e.printStackTrace();
            }
        }
    }
    
    public static long sCurrentAccountId = 0;
    
    public static long setCurrentAccountId(Context context) {
        long result = 0;
        try {
           bindService(context);
            if (mPrivacyManSer != null) {
                result = mPrivacyManSer.getCurrentAccount(PACKAGENAME, CLASSNAME).getAccountId();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        sCurrentAccountId = result;
        return sCurrentAccountId;
    }
    
    public static long getCurrentAccountId() {
        if (mPrivacyManSer != null) {
            return sCurrentAccountId;
        } else {
            return setCurrentAccountId(sContext);
        }
    }
    
    
    private static ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mIsServiceConnected = true;
            mPrivacyManSer = IPrivacyManageService.Stub.asInterface(service);
            try {
                // Aurora xuyong 2015-04-20 modified for bug #13071 start
                if (mPrivacyManSer != null) {
                    sCurrentAccountId = mPrivacyManSer.getCurrentAccount(PACKAGENAME, CLASSNAME).getAccountId();
                }
                // Aurora xuyong 2015-04-20 modified for bug #13071 end
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mIsServiceConnected = false;
            mPrivacyManSer = null;
        }
        
    };
    // Aurora xuyong 2014-11-10 modified for bug #9720 start
    public static long getFristPrivacyId(Context context, String number) {
   // Aurora xuyong 2014-11-10 modified for bug #9720 end
        String selection = "mimetype_id = 5 and PHONE_NUMBERS_EQUAL(data1, " + number + ", 0)" + " and is_privacy > 0";
        Cursor c = context.getContentResolver().query(
                   Data.CONTENT_URI, 
                   new String[]{"is_privacy"}, 
                   selection , 
                   null, 
                  // Aurora xuyong 2014-11-10 modified for bug #9577 start
                   "is_privacy desc");
                  // Aurora xuyong 2014-11-10 modified for bug #9577 end
        try {
	    	  if (c != null && c.moveToFirst()) {
		          // Aurora xuyong 2014-11-10 modified for bug #9720 start
		            return c.getLong(0);
		          // Aurora xuyong 2014-11-10 modified for bug #9720 end
	          }       
	    } finally {
	    	 if(c != null) {
	         	c.close();
	         }
	    }
        return 0;
    }
    
    public static long getOrCreateThreadId(
               Context context, Set<String> recipients, long privacy) {
           Uri.Builder uriBuilder = Uri.parse("content://mms-sms/threadID").buildUpon();

           for (String recipient : recipients) {
               if (Mms.isEmailAddress(recipient)) {
                   recipient = android.provider.Telephony.Mms.extractAddrSpec(recipient);
               }

               uriBuilder.appendQueryParameter("recipient", recipient);
               uriBuilder.appendQueryParameter("is_privacy", "" + privacy);
           }

           Uri uri = uriBuilder.build();
           //if (DEBUG) Rlog.v(TAG, "getOrCreateThreadId uri: " + uri);

           Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                   uri, new String[] { "_id" }, null, null, null);
           if (cursor != null) {
               try {
                   if (cursor.moveToFirst()) {
                       return cursor.getLong(0);
                   } else {
                       Log.e("Telephony", "getOrCreateThreadId returned no rows!");
                   }
               } finally {
                   cursor.close();
               }
           }

           Log.e("Telephony", "getOrCreateThreadId failed with uri " + uri.toString());
           throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
       }
}