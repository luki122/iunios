/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.contacts.calllog.AuroraManagePrivate;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.test.InjectedServices;
import com.android.contacts.util.Constants;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.YuloreUtils;
import com.google.common.annotations.VisibleForTesting;

import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StrictMode;
import aurora.preference.AuroraPreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.os.SystemProperties;

import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SimAssociateHandler;
import com.gionee.CellConnService.GnCellConnMgr;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

//import android.app.ThemeManager;

import com.privacymanage.service.AuroraPrivacyUtils;
import com.android.contacts.widget.GnTextView;
import com.aurora.android.contacts.AuroraSubInfoNotifier;

import android.telephony.*;

public final class ContactsApplication extends Application {
    private long syncStart=0;
    private long syncEnd=0;
    private static InjectedServices sInjectedServices;
    private AccountTypeManager mAccountTypeManager;
    private ContactPhotoManager mContactPhotoManager;
    private ContactListFilterController mContactListFilterController;
    private boolean syncFlag=true;
    private static ContactsApplication sMe;
    private static Context context;

    public static final boolean sDialerSearchSupport = FeatureOption.MTK_DIALER_SEARCH_SUPPORT;
    public static final boolean sSpeedDial = true;
    
    public static List<Activity> mPrivacyActivityList = new ArrayList<Activity>();
    

    /**
     * Overrides the system services with mocks for testing.
     */
    @VisibleForTesting
    public static void injectServices(InjectedServices services) {
        sInjectedServices = services;
    }

    public static InjectedServices getInjectedServices() {
        return sInjectedServices;
    }

    @Override
    public ContentResolver getContentResolver() {
        if (sInjectedServices != null) {
            ContentResolver resolver = sInjectedServices.getContentResolver();
            if (resolver != null) {
                return resolver;
            }
        }
        return super.getContentResolver();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (sInjectedServices != null) {
            SharedPreferences prefs = sInjectedServices.getSharedPreferences();
            if (prefs != null) {
                return prefs;
            }
        }

        return super.getSharedPreferences(name, mode);
    }

    @Override
    public Object getSystemService(String name) {
        if (sInjectedServices != null) {
            Object service = sInjectedServices.getSystemService(name);
            if (service != null) {
                return service;
            }
        }

        if (AccountTypeManager.ACCOUNT_TYPE_SERVICE.equals(name)) {
            if (mAccountTypeManager == null) {
                mAccountTypeManager = AccountTypeManager.createAccountTypeManager(this);
            }
            return mAccountTypeManager;
        }

        if (ContactPhotoManager.CONTACT_PHOTO_SERVICE.equals(name)) {
            if (mContactPhotoManager == null) {
                mContactPhotoManager = ContactPhotoManager.createContactPhotoManager(this);
                registerComponentCallbacks(mContactPhotoManager);
                mContactPhotoManager.preloadPhotosInBackground();
            }
            return mContactPhotoManager;
        }

        if (ContactListFilterController.CONTACT_LIST_FILTER_SERVICE.equals(name)) {
            if (mContactListFilterController == null) {
                mContactListFilterController =
                        ContactListFilterController.createContactListFilterController(this);
            }
            return mContactListFilterController;
        }

        return super.getSystemService(name);
    }
    
    public static ContactsApplication getInstance() {
        return sMe;
    }
    
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Priming caches to placate the StrictMode police
        context = getApplicationContext();
        AuroraPreferenceManager.getDefaultSharedPreferences(context);
//        AccountTypeManager.getInstance(context); //  aurora <wangth> <2013-11-2> remove for aurora 
        sMe = this;
        
        if (sIsAuroraPrivacySupport) {
        	AuroraPrivacyUtils.bindService(sMe.getApplicationContext());
        	AuroraManagePrivate.init(sMe.getApplicationContext());
        }

        /**
         * added by mediatek .inc
         * description : initialize CellConnService and SIM associate handler
         */
        SimAssociateHandler.getInstance().prepair();
        

        HyphonManager.getInstance();
        new Thread(new Runnable() {
            public void run() {
            	long lStart = System.currentTimeMillis();
                HyphonManager.getInstance().formatNumber(TEST_NUMBER);
                Log.i(Constants.PERFORMANCE_TAG," Thread HyphonManager formatNumber() use time :" + (System.currentTimeMillis() - lStart));
            }
        }).start();
        /**
         * added by mediatek .inc end
         */

        initInThreadDelay();
          
        
        ResConstant.init(this);
        
//        GnTextView.onTextSizeChanged(this); // aurora <wangth> <2013-11-2> remove for aurora
        AuroraCardTypeUtils.init(sMe.getApplicationContext());
        YuloreUtils.getInstance(this).bind();
        
        //aurora add by liguangyu
        SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        ContactListFilter filter = ContactListFilter.createFilterWithType(
                ContactListFilter.FILTER_TYPE_CUSTOM);
        mPrefs.edit().putInt("filter.type", filter == null ? ContactListFilter.FILTER_TYPE_DEFAULT : filter.filterType).apply();
        mAuroraSubInfoNotifier = AuroraSubInfoNotifier.getInstance();
    }
    
    public void finalize() throws Throwable{
    	super.finalize();
    }

    /* below are added by mediatek .inc */
    public GnCellConnMgr cellConnMgr;

    protected String TEST_NUMBER = "10086";

    // Should be single thread, as we don't want to simultaneously handle contacts 
    // copy-delete-import-export request.
    public final ExecutorService singleTaskService = Executors.newSingleThreadExecutor();
    
    public static final boolean auroraContactsSupport = true;
    
    public static final boolean sIsAuroraPrivacySupport = true;
    
    // above is Mtk  
    public static final boolean sIsGnAreoNumAreaSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
    	
    // Gionee:huangzy 20120530 add for CR00608714 start
    public static final boolean sIsGnZoomClipSupport = true;
    // Gionee:huangzy 20120530 add for CR00608714 end
       
    
    //Gionee:huangzy 20120621 add for CR00624998 start
    public static final int MAX_BATCH_HANDLE_NUM = 100;
    //Gionee:huangzy 20120621 add for CR00624998 end
    
	

	
	//Gionee:huangzy 20120906 add for CR00688166 start
	public static final boolean sIsGnCombineCalllogMatchNumber = true;//SystemProperties.get("ro.gn.calllog.combinenumber").equals("yes");
	public static final int GN_MATCH_CONTACTS_NUMBER_LENGTH = SystemProperties.getInt("ro.gn.match.numberlength", 11);
	//Gionee:huangzy 20120906 add for CR00688166 end

	
    //Goinee:huangzy 20130320 modify for CR00786812 start
    //gionee xuhz 20130226 add for speed dial start
    public static boolean sIsGnSpeedDialSupport =
    			SystemProperties.get("ro.gn.speed.dial").equals("yes");
    
    //gionee xuhz 20130226 add for speed dial end
    
 
    
    public static boolean sIsChinaProduct = "false".equals(SystemProperties.get("phone.type.oversea"));
    public static boolean sIsAuroraRejectSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
    public static boolean sIsAuroraYuloreSupport = false;//sIsChinaProduct;
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	
    	ResConstant.init(this);
    	GnTextView.onTextSizeChanged(this);
    }
    
    private void initInThreadDelay() {
    	new Thread() {
    		public void run() {
    			try {
    				sleep(1000);
				} catch (InterruptedException e) {
				}
			
				AccountTypeManager.getInstance(getApplicationContext());
				cellConnMgr = new GnCellConnMgr();
				cellConnMgr.register(getApplicationContext());
    		};
    	}.start();
    }
    
    public int getColor(int id) {
    	return sMe.getResources().getColor(id);
    }
    
    public void sendSyncBroad(){
		Log.i("qiaohu", "sendSyncBroad");
		syncStart=System.currentTimeMillis();
		Log.i("qiaohu", "start="+syncStart);
		if(!syncFlag){
			return;
		}
 		new Thread(){
     		public void run() {
     			syncFlag=false;
     			do{
     				try {
						Thread.sleep(10 * 1000l);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
     				syncEnd=System.currentTimeMillis();
     				Log.i("qiaohu", "end="+syncEnd);
     				Log.i("qiaohu", (syncEnd-syncStart)+"");
     			}while(syncEnd-syncStart<10000);
     			Cursor cursor=getContentResolver().query(Uri.parse("content://com.android.contacts/sync/sync_up_size"), null, null, null, null);
     			Intent syncIntent = new Intent("com.aurora.account.START_SYNC");  
            	syncIntent.putExtra("packageName", "com.android.contacts");
            	if(cursor!=null){
            		if(cursor.moveToNext()){
            			syncIntent.putExtra("size", cursor.getInt(0));
                		Log.i("qiaohu", cursor.getInt(0)+"");                	
            		}
            		cursor.close();
            	}
            	sendBroadcast(syncIntent); 
            	syncFlag=true;
     		};
     	}.start();
	} 
    
    AuroraSubInfoNotifier mAuroraSubInfoNotifier;
    
}
