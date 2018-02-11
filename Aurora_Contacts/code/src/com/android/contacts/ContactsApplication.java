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
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.test.InjectedServices;
import com.android.contacts.util.Constants;
import com.android.contacts.util.GnHotLinesUtil;
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
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StrictMode;
import aurora.preference.AuroraPreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.os.SystemProperties;

import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SimAssociateHandler;
import com.gionee.CellConnService.GnCellConnMgr;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;

//import android.app.ThemeManager;

import com.privacymanage.service.AuroraPrivacyUtils;
import com.android.contacts.widget.GnTextView;

public final class ContactsApplication extends Application {
	private long syncStart=0;
	private long syncEnd=0;
	private static InjectedServices sInjectedServices;
	private AccountTypeManager mAccountTypeManager;
	private ContactPhotoManager mContactPhotoManager;
	private ContactListFilterController mContactListFilterController;
	private boolean syncFlag=true;
	static ContactsApplication sMe;
	public static Context context;

	public static int screenW,screenH;
	public static boolean isMultiSimEnabled;
	public static boolean isShowDouble;
	public static boolean phoneIsCdma=false;

	private int currentPage=0;

	public int getCurrentPage() {
		return currentPage;
	}


	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	private void getScreenInfo() {
		// TODO Auto-generated method stub
		//		getDisplayInfomation();
		//		getDensity();
		//		getScreenSizeOfDevice();
		//		getScreenSizeOfDevice2();

		//		Log.d(TAG, "test123:"+RawContacts.CONTENT_URI.toString()+" "+Contacts.CONTENT_URI.toString());
		//		Cursor cursor=context.getContentResolver().query(RawContacts.CONTENT_URI,null,null,null,null);
		//		if(cursor.getCount()>0){
		//			cursor.moveToLast();
		//			String id=cursor.getString(cursor.getColumnIndex("_id"));
		//			Log.d(TAG,"id:"+id);
		//
		//			ContentValues values = new ContentValues();
		////			values.put("is_privacy", 3);
		//			values.put("is_whitelist", 5);
		//			context.getContentResolver().update(RawContacts.CONTENT_URI, values, 
		//					RawContacts.CONTACT_ID + "="+id, null);
		//		}


//		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		screenW = dm.widthPixels;// 获取分辨率宽度
		//				screenH = dm.heightPixels;// 获取分辨率高度
		//				Log.d(TAG,"screenW:"+screenW+" screenH:"+screenH);

		isMultiSimEnabled=GNContactsUtils.isMultiSimEnabled();
		isShowDouble = ContactsUtils.isShowDoubleButton();

	}

	public static void sendSimContactBroad() {
		Intent intent = new Intent("com.android.action.LAUNCH_CONTACTS_LIST");
		context.sendBroadcast(intent);
	}

	public static void sendSimContactBroad(int flag) {
		Intent intent = new Intent("com.android.action.LAUNCH_CONTACTS_LIST");
		intent.putExtra("flag", flag);
		context.sendBroadcast(intent);
	}



	public static final boolean sGemini = FeatureOption.MTK_GEMINI_SUPPORT;
	public static final boolean sDialerSearchSupport = FeatureOption.MTK_DIALER_SEARCH_SUPPORT;
	public static final boolean sSpeedDial = true;

	public static List<Activity> mPrivacyActivityList = new ArrayList<Activity>();

	public static final String GN_GUEST_MODE = "gionee_guest_mode";
	private static final String TAG = "liyang-ContactsApplication";

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

		long time=System.currentTimeMillis();
		//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
		//		.detectAll()
		//		.penaltyLog()  
		//		.penaltyDeath()  
		//		.build());  
		//
		//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		//		.detectDiskReads()
		//		.detectDiskWrites()
		//		.detectNetwork()
		//		.penaltyLog()
		//		.build());


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
		// aurora <wangth> <2013-11-2> remove for aurora begin
		//        SimAssociateHandler.getInstance().load();

		//        cellConnMgr = new GnCellConnMgr();
		//        cellConnMgr.register(getApplicationContext());

		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			SIMInfoWrapper.getDefault();
		}
		//  aurora <wangth> <2013-11-2> remove for aurora end
		HyphonManager.getInstance();
		//        new Thread(new Runnable() {
		//            public void run() {
		//            	long lStart = System.currentTimeMillis();
		//                HyphonManager.getInstance().formatNumber(TEST_NUMBER);
		//                Log.i(Constants.PERFORMANCE_TAG," Thread HyphonManager formatNumber() use time :" + (System.currentTimeMillis() - lStart));
		//            }
		//        }).start();
		/**
		 * added by mediatek .inc end
		 */

		initInThreadDelay();

		//aurora move liguangyu 20140923 for #8554 start
		if (ContactsApplication.sIsHotLinesSupport) {
			getContentResolver().query(GnHotLinesUtil.INIT_HOT_LINES_URI, null, null, null, null);
		}
		//aurora move liguangyu 20140923 for #8554 end

		ResConstant.init(this);

		//        GnTextView.onTextSizeChanged(this); // aurora <wangth> <2013-11-2> remove for aurora
		AuroraCardTypeUtils.init(sMe.getApplicationContext());
		YuloreUtils.getInstance(this).bind();

		getScreenInfo();
		Log.d(TAG, "spend time:"+(System.currentTimeMillis()-time));
		
		sIsAuroraPrivacySupport = ContactsUtils.is7505()?false:true;
		Log.d(TAG, "sIsAuroraPrivacySupport:"+sIsAuroraPrivacySupport);
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

	public static boolean sIsAuroraPrivacySupport = !SystemProperties.get("ro.gn.gnprojectid").contains("7505");

	// above is Mtk
	public static final boolean sIsGnContactsSupport = true;//SystemProperties.get("ro.gn.contacts.newfeature").equals("yes");
	public static final boolean sIsGnAreoNumAreaSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
	/*(SystemProperties.get("ro.gn.gioneenumarea.prop").equals("gionee") ||
                                            SystemProperties.get("ro.gn.gioneenumarea.prop").equals("aora")) && sIsGnContactsSupport*/;
                                            public static final boolean sIsIpDialSupport = SystemProperties.get("ro.gn.ip.dial").equals("yes");
                                            // Gionee:huangzy 20120530 add for CR00608714 start
                                            public static final boolean sIsGnZoomClipSupport = true;//SystemProperties.get("ro.gn.zoomclipview.prop").equals("yes") && sIsGnContactsSupport;
                                            // Gionee:huangzy 20120530 add for CR00608714 end

                                            //Gionee:huangzy 20120806 add for CR00667827 start
                                            public static boolean sIsGioneeCloudSpport = false;//com.gionee.featureoption.FeatureOption.GN_FEATURE_GIONEE_CLOUD_ENTER;
                                            //Gionee:huangzy 20120806 add for CR00667827 end

                                            //Gionee:huangzy 20120621 add for CR00624998 start
                                            public static final int MAX_BATCH_HANDLE_NUM = 100;
                                            //Gionee:huangzy 20120621 add for CR00624998 end

                                            //Gionee:huangzy 20120823 add for CR00614805 start
                                            //    public static final boolean sIsHotLinesSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
                                            public static final boolean sIsHotLinesSupport = true;
                                            //Gionee:huangzy 20120823 add for CR00614805 end

                                            // Gionee lixiaohu 2012-08-28 added for CR00681687 start
                                            public static final boolean sGnGeminiDialSupport = SystemProperties.get("ro.gn.gemini.dialpad.support").equals("yes") && sGemini;
                                            // Gionee lixiaohu 2012-08-28 added for CR00681687 end

                                            //Gionee:huangzy 20120906 add for CR00688166 start
                                            public static final boolean sIsGnCombineCalllogMatchNumber = true;//SystemProperties.get("ro.gn.calllog.combinenumber").equals("yes");
                                            public static final int GN_MATCH_CONTACTS_NUMBER_LENGTH = SystemProperties.getInt("ro.gn.match.numberlength", 11);
                                            //Gionee:huangzy 20120906 add for CR00688166 end

                                            //Gionee:huangzy 20121011 add for CR00710695 start
                                            public static final boolean sIsGnDialerSearchSupport = true;//SystemProperties.get("ro.gn.contacts.gndialersearch").equals("yes");
                                            //Gionee:huangzy 20121011 add for CR00710695 end

                                            //Gionee:xuhz 20121014 add for CR00686812 start
                                            public static final boolean sIsGnQwertDialpadSupport = sIsGnDialerSearchSupport;
                                            //Gionee:xuhz 20121014 add for CR00686812 end

                                            public static final boolean sIsGnGGKJ_V2_0Support = true;
                                            public static final boolean sIsGnDualSimSelectSupport = false;
                                            // gionee xuhz 20121225 add for show popup window when key down start
                                            public static final boolean sIsGnQwertKeyboardShowPop = sIsGnQwertDialpadSupport && true;
                                            // gionee xuhz 20121225 add for show popup window when key down end

                                            //Goinee:huangzy 20130320 modify for CR00786812 start
                                            //gionee xuhz 20130226 add for speed dial start
                                            public static boolean sIsGnSpeedDialSupport =
                                            		com.gionee.featureoption.FeatureOption.GN_FEATURE_SPEED_DIAL || 
                                            		SystemProperties.get("ro.gn.speed.dial").equals("yes");

                                            //gionee xuhz 20130226 add for speed dial end

                                            public static boolean sIsGnQwertKeyboardNumberSupport = sIsGnQwertDialpadSupport && 
                                            		com.gionee.featureoption.FeatureOption.GN_FEATURE_QWERT_KEYBOARD_NUMBER;

                                            public static boolean sIsGroupMemberLongClickSupport = sIsGnQwertKeyboardNumberSupport;

                                            public static boolean sIsColorfulContactPhotoSupport = 
                                            		false;//com.gionee.featureoption.FeatureOption.GN_FEATURE_COLORFUL_CONTACT_PHOTO;
                                            //Goinee:huangzy 20130320 modify for CR00786812 end

                                            // gionee xuhz 20130312 add for CR00780006 start 
                                            public static boolean sIsWidgetThreeColumns = SystemProperties.get("ro.gn.widget.three.columns.prop").equals("yes");
                                            // gionee xuhz 20130312 add for CR00780006 end

                                            //Gionee:huangzy 20130314 add for CR00784577 start
                                            public static boolean sIsGnListItemActionAlterable = 
                                            		com.gionee.featureoption.FeatureOption.GN_FEATURE_LISTITEM_ACTION_ALTERABLE;
                                            //Gionee:huangzy 20130314 add for CR00784577 end

                                            // Gionee:wangth 20130321 add for CR00787281 begin
                                            public static boolean sIsSwitchCallLogHeader = 
                                            		com.gionee.featureoption.FeatureOption.GN_FEATURE_CALL_LOG_HEADER;
                                            // Gionee:wangth 20130321 add for CR00787281 end

                                            // Gionee:xuhz 20130328 add for CR00790874 start
                                            public static boolean sIsHandSensorDial = false;//com.gionee.featureoption.FeatureOption.GN_FEATURE_HAND_SENSOR_DIAL;
                                            // Gionee:xuhz 20130328 add for CR00790874 end

                                            //Gionee <xuhz> <2013-07-20> add for CR00824492 begin
                                            public static boolean sIsInternalSdcardOnly = SystemProperties.get("ro.gn.sdcard.type").equals("internal");
                                            //Gionee <xuhz> <2013-07-20> add for CR00824492 end

                                            public static boolean sIsChinaProduct = "false".equals(SystemProperties.get("phone.type.oversea"));
                                            public static boolean sIsAuroraRejectSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
                                            public static boolean sIsAuroraYuloreSupport = false;//sIsChinaProduct;

                                            // gionee xuhz 20120509 add for theme control start
                                            public static boolean sIsGnTransparentTheme = false;
                                            public static boolean sIsGnDarkTheme = false;
                                            public static boolean sIsGnLightTheme = true;

                                            public static boolean sIsGnDarkStyle = false;

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
                                            			Cursor cursor= null;
                                            			try {
                                            				cursor = getContentResolver().query(Uri.parse("content://com.android.contacts/sync/sync_up_size"), null, null, null, null);
                                            			} catch (SQLiteException ex) {
                                            				ex.printStackTrace();
                                            			}
                                            			//Cursor cursor=getContentResolver().query(Uri.parse("content://com.android.contacts/sync/sync_up_size"), null, null, null, null);
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

}
